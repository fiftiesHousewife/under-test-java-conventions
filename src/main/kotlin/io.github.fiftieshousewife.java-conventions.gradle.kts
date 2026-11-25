// One set of code-quality standards for Java (why each is what it is: docs/decisions). Three layers:
//  - environment (proxy, package repository): never set here; it comes from the organisation's Gradle init script;
//  - code-quality standards: Checkstyle, PMD (complexity, the assertAll rule), copy-paste detection, SpotBugs with
//    FindSecBugs, forbidden APIs. The rules inside this plugin are defaults: `underTest.codeStyle` = TOOLKIT,
//    ORGANISATION (the organisation's rule files) or NONE (the organisation's own plugin does it), and
//    `underTest.codeStyleExcludes` leaves out paths a repo keeps below the standards on purpose;
//  - testing evidence: coverage verification, always on; only the threshold changes.
// Every underTest setting can also be a Gradle property, e.g. -PunderTest.codeStyle=NONE, or in an init script.
// Advice (for-each loops) is reported by pmdAdvice and never fails the build. Mutation testing is opt-in: pitest.
// SpotBugs, PIT and CPD run from their own command lines, so the plugin needs no Gradle plugin of theirs.

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import groovy.json.JsonSlurper
import io.github.fiftieshousewife.javaconventions.CodeStyleSource
import io.github.fiftieshousewife.javaconventions.InfoLineFilter
import io.github.fiftieshousewife.javaconventions.JavaPackages
import io.github.fiftieshousewife.javaconventions.MutationSummaryTask
import io.github.fiftieshousewife.javaconventions.SourcePatterns
import io.github.fiftieshousewife.javaconventions.SpotBugsCheck
import io.github.fiftieshousewife.javaconventions.UnderTestExtension
import java.io.OutputStream
import java.util.Properties

plugins {
    java
    jacoco
    checkstyle
    pmd
    id("de.thetaphi.forbiddenapis")
    id("io.github.ben-manes.versions")
}

// ---------- What ships in the plugin's jar: tool versions and rule files ----------
val bundled = UnderTestExtension::class.java
val toolVersions = Properties().apply { bundled.getResourceAsStream("versions.properties")!!.use { load(it) } }
fun version(name: String): String = toolVersions.getProperty(name)
fun bundledRules(name: String) = resources.text.fromString(bundled.getResource(name)!!.readText())

// ---------- Settings an organisation can change ----------
fun fileProperty(name: String) = providers.gradleProperty(name).map { rootProject.layout.projectDirectory.file(it) }
val underTest = extensions.create<UnderTestExtension>("underTest").apply {
    codeStyle.convention(
        providers.gradleProperty("underTest.codeStyle").map { CodeStyleSource.valueOf(it.trim().uppercase()) }
            .orElse(CodeStyleSource.TOOLKIT),
    )
    checkstyleConfig.convention(fileProperty("underTest.checkstyleConfig"))
    pmdRuleset.convention(fileProperty("underTest.pmdRuleset"))
    forbiddenApiSignatures.convention(fileProperty("underTest.forbiddenApiSignatures"))
    spotbugsExcludeFilter.convention(fileProperty("underTest.spotbugsExcludeFilter"))
    codeStyleExcludes.convention(
        providers.gradleProperty("underTest.codeStyleExcludes")
            .map { value -> value.split(',').map(String::trim).filter(String::isNotEmpty) }.orElse(emptyList()),
    )
    cpdMinimumTokens.convention(
        providers.gradleProperty("underTest.cpdMinimumTokens").orElse(version("cpd-minimum-tokens")).map { it.toInt() },
    )
    minMutationScore.convention(providers.gradleProperty("underTest.minMutationScore").map { it.toInt() })
    maxClassesPerPackage.convention(
        providers.gradleProperty("underTest.maxClassesPerPackage").orElse(version("max-classes-per-package"))
            .map { it.toInt() },
    )
    minLineCoverage.convention(
        providers.gradleProperty("underTest.minLineCoverage").orElse(version("min-line-coverage"))
            .map { it.toBigDecimal() },
    )
}
val codeStyle = underTest.codeStyle

// With ORGANISATION, a check uses the organisation's file; it fails when it runs if that setting is missing.
fun Task.useOrganisationFile(organisationFile: RegularFileProperty, setting: String, apply: (File) -> Unit): Boolean =
    when (codeStyle.get()) {
        CodeStyleSource.ORGANISATION -> {
            if (organisationFile.isPresent) {
                apply(organisationFile.get().asFile)
            } else {
                val message =
                    "underTest.codeStyle is ORGANISATION, so set underTest.$setting to the organisation's file"
                doFirst { throw GradleException(message) }
            }
            true
        }
        else -> false
    }

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(version("java"))
    }
}
val toolchainLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(version("java")) }

dependencies {
    // Logging: Lombok's @Slf4j on the SLF4J API. Log4j 2 behind it is added below, once it's known whether this is a
    // library.
    "compileOnly"("org.projectlombok:lombok:${version("lombok")}")
    "annotationProcessor"("org.projectlombok:lombok:${version("lombok")}")
    "testCompileOnly"("org.projectlombok:lombok:${version("lombok")}")
    "testAnnotationProcessor"("org.projectlombok:lombok:${version("lombok")}")
    "implementation"("org.slf4j:slf4j-api:${version("slf4j")}")

    "testImplementation"(platform("org.junit:junit-bom:${version("junit")}"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testImplementation"("org.assertj:assertj-core:${version("assertj")}")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")

}

// An application logs through Log4j 2 at run time. A library (java-library) gets it for its own tests only: it leaves
// its users to choose their logging, and its published dependencies name only the SLF4J API.
val log4j = listOf("log4j-core", "log4j-slf4j2-impl").map { "org.apache.logging.log4j:$it:${version("log4j")}" }
afterEvaluate {
    val configuration = if (plugins.hasPlugin("java-library")) "testRuntimeOnly" else "runtimeOnly"
    log4j.forEach { dependencies.add(configuration, it) }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Every warning about the code is an error. Not javac's "options" warnings, which are about its flags: compiling
    // for an older Java (options.release = 8) warns that 8 is obsolete, and that is the repo's choice, not a fault.
    options.compilerArgs.addAll(listOf("-Xlint:all,-processing,-options", "-Werror", "-parameters"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// ---------- Tests and coverage ----------
tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

jacoco {
    toolVersion = version("jacoco")
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
}

// The settings are read once the build script has set them, even if a task was created before then.
afterEvaluate {
    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        violationRules {
            rule {
                limit {
                    counter = "LINE"
                    minimum = underTest.minLineCoverage.get()
                }
            }
        }
    }
}

// JaCoCo skips its verification when no test ran, which would let code with no tests pass the floor.
// A plain file collection, not the source set itself, which the configuration cache can't store.
val mainCode = files(extensions.getByType<SourceSetContainer>()["main"].allJava)
val coverageData =
    files(tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification").map { it.executionData })
val requireTests = tasks.register("requireTests") {
    group = "verification"
    description = "Fails when there is main code but no test ran, so the coverage floor can't be skipped."
    dependsOn(tasks.named("test"))
    val code = mainCode
    val data = coverageData
    val floor = underTest.minLineCoverage
    doLast {
        if (floor.get().signum() > 0 && !code.isEmpty && data.none { it.exists() }) {
            throw GradleException(
                "There is main code but no test ran, so its line coverage is 0%, below the minimum of " +
                    "${floor.get().movePointRight(2).stripTrailingZeros().toPlainString()}%: add tests.",
            )
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"), requireTests)
}

// ---------- Code-quality standards (replaceable; see underTest.codeStyle) ----------
listOf(Checkstyle::class, Pmd::class, SpotBugsCheck::class, CheckForbiddenApis::class).forEach { type ->
    tasks.withType(type.java).configureEach {
        val style = underTest.codeStyle
        onlyIf("underTest.codeStyle is not NONE") { style.get() != CodeStyleSource.NONE }
    }
}

checkstyle {
    toolVersion = version("checkstyle")
    isIgnoreFailures = false
    maxWarnings = 0
}

// Whose rules each check uses is chosen once the build script has set underTest, even for a task created before then.
val advice = "pmdAdvice"
afterEvaluate {
    tasks.withType<Checkstyle>().configureEach {
        val usesOrganisationFile = useOrganisationFile(underTest.checkstyleConfig, "checkstyleConfig") {
            configFile = it
            configDirectory = it.parentFile
        }
        if (!usesOrganisationFile) {
            config = bundledRules("checkstyle.xml")
        }
    }
    tasks.withType<Pmd>().configureEach {
        if (name != advice && !useOrganisationFile(underTest.pmdRuleset, "pmdRuleset") { ruleSetFiles = files(it) }) {
            ruleSetConfig = bundledRules("pmd-ruleset.xml")
        }
    }
    // Code the repo keeps below the standards on purpose (underTest.codeStyleExcludes), in every checking task.
    tasks.withType<Checkstyle>().configureEach { exclude(underTest.codeStyleExcludes.get()) }
    tasks.withType<Pmd>().configureEach { exclude(underTest.codeStyleExcludes.get()) }
    tasks.withType<CheckForbiddenApis>().configureEach {
        exclude(SourcePatterns.forClasses(underTest.codeStyleExcludes.get()))
    }
    tasks.withType<CheckForbiddenApis>().configureEach {
        val usesOrganisationFile = useOrganisationFile(underTest.forbiddenApiSignatures, "forbiddenApiSignatures") {
            signaturesFiles = files(it)
        }
        if (!usesOrganisationFile) {
            signaturesURLs = setOf(bundled.getResource("forbidden-signatures.txt")!!)
        }
    }
}

pmd {
    toolVersion = version("pmd")
    ruleSets = emptyList()
    isConsoleOutput = true
    isIgnoreFailures = false
}

// The JDK's own classes, from the toolchain, so PMD resolves types against the JDK the code is built with.
val jdkClasses = files(toolchainLauncher.map { it.metadata.installationPath.file("lib/jrt-fs.jar") })
tasks.withType<Pmd>().configureEach {
    classpath = (classpath ?: files()).plus(jdkClasses)
}

// ---------- Advice: PMD rules that are reported but never fail the build, such as preferring streams ----------
val sourceSetsForAdvice = extensions.getByType<SourceSetContainer>()
val pmdAdvice = tasks.register<Pmd>(advice) {
    group = "verification"
    description = "Reports, without failing the build, what the plugin's advice rules find."
    source(sourceSetsForAdvice["main"].allJava, sourceSetsForAdvice["test"].allJava)
    classpath = sourceSetsForAdvice["test"].runtimeClasspath + jdkClasses
    ruleSetConfig = bundledRules("pmd-advice.xml")
    ignoreFailures = true
    reports.html.outputLocation = layout.buildDirectory.file("reports/pmd/advice.html")
    reports.xml.outputLocation = layout.buildDirectory.file("reports/pmd/advice.xml")
}
tasks.named("check") {
    dependsOn(pmdAdvice)
}

// ---------- Copy-paste detection: PMD's own CPD, run directly ----------
val cpdTool = configurations.create("cpdTool") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    cpdTool("net.sourceforge.pmd:pmd-cli:${version("pmd")}")
    cpdTool("net.sourceforge.pmd:pmd-java:${version("pmd")}")
}
val cpdCheck = tasks.register<JavaExec>("cpdCheck") {
    group = "verification"
    description = "Fails if underTest.cpdMinimumTokens (50) or more tokens of Java are copy-pasted (PMD's CPD)."
    val style = underTest.codeStyle
    onlyIf("underTest.codeStyle is not NONE") { style.get() != CodeStyleSource.NONE }
    // Every source set's Java folders, including those added after the plugin, such as integration tests.
    val sources = files(provider { sourceSetsForAdvice.flatMap { it.java.srcDirs } }).filter { it.isDirectory }
    val excludes = underTest.codeStyleExcludes
    inputs.files(sources).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("codeStyleExcludes", excludes)
    onlyIf("there is Java code to check") { SourcePatterns.files(sources.files, "java", excludes.get()).isNotEmpty() }
    classpath = cpdTool
    mainClass = "net.sourceforge.pmd.cli.PmdCli"
    javaLauncher = toolchainLauncher
    val minimumTokens = underTest.cpdMinimumTokens
    // The files to check, listed in a file, so that the paths left out by underTest.codeStyleExcludes stay out.
    val fileList = temporaryDir.resolve("files.txt")
    doFirst {
        fileList.writeText(SourcePatterns.files(sources.files, "java", excludes.get()).joinToString("\n"))
    }
    args("cpd", "--language", "java", "--format", "text")
    argumentProviders.add(CommandLineArgumentProvider {
        listOf("--minimum-tokens", "${minimumTokens.get()}", "--file-list", fileList.path)
    })
}
tasks.named("check") {
    dependsOn(cpdCheck)
}

// ---------- Package size: a package holding many classes is several packages ----------
val checkPackageSize = tasks.register("checkPackageSize") {
    group = "verification"
    description = "Fails if a package holds more than underTest.maxClassesPerPackage (20) classes, in any source set."
    val style = underTest.codeStyle
    onlyIf("underTest.codeStyle is not NONE") { style.get() != CodeStyleSource.NONE }
    val roots = provider { sourceSetsForAdvice.associate { it.name to it.java.srcDirs.filter(File::isDirectory) } }
    val limit = underTest.maxClassesPerPackage
    val excludes = underTest.codeStyleExcludes
    doLast {
        val tooBig = roots.get().flatMap { (sourceSet, dirs) ->
            JavaPackages.classCounts(dirs, excludes.get()).filterValues { it > limit.get() }.map { (name, count) ->
                "Package ${name.ifEmpty { "(default)" }} in $sourceSet has $count classes, " +
                    "more than ${limit.get()}: split it into packages by what its classes do."
            }
        }
        if (tooBig.isNotEmpty()) {
            throw GradleException(tooBig.sorted().joinToString("\n"))
        }
    }
}
tasks.named("check") {
    dependsOn(checkPackageSize)
}

// ---------- Bugs and security bugs: SpotBugs with FindSecBugs, run from its command line ----------
val spotbugsTool = configurations.create("spotbugsTool") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val spotbugsPlugins = configurations.create("spotbugsPlugins") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}
dependencies {
    spotbugsTool("com.github.spotbugs:spotbugs:${version("spotbugs")}")
    spotbugsTool("org.slf4j:slf4j-simple:${version("slf4j")}")
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:${version("findsecbugs")}")
}
val spotbugsReports = layout.buildDirectory.dir("reports/spotbugs")
// One task for each source set, including those added after the plugin: spotbugsMain, spotbugsTest, ...
sourceSetsForAdvice.configureEach {
    val sourceSet = this
    val spotbugs = tasks.register<SpotBugsCheck>(sourceSet.getTaskName("spotbugs", null)) {
        group = "verification"
        description = "Finds bug patterns and security bugs in the ${sourceSet.name} classes (SpotBugs, FindSecBugs)."
        classpath = spotbugsTool
        mainClass = "edu.umd.cs.findbugs.FindBugs2"
        javaLauncher = toolchainLauncher
        dependsOn(sourceSet.classesTaskName)
        classesDirs.from(sourceSet.output.classesDirs)
        auxClasspath.from(sourceSet.compileClasspath)
        pluginJars.from(spotbugsPlugins)
        sourceDirs.from(sourceSet.allSource.sourceDirectories)
        excludeFilter = underTest.spotbugsExcludeFilter
        excludes = underTest.codeStyleExcludes
        xmlReport = spotbugsReports.map { it.file("${sourceSet.name}.xml") }
        htmlReport = spotbugsReports.map { it.file("${sourceSet.name}.html") }
    }
    tasks.named("check") {
        dependsOn(spotbugs)
    }
}

// ---------- Forbidden APIs ----------
forbiddenApis {
    bundledSignatures = setOf(
        "jdk-unsafe",
        "jdk-deprecated",
        "jdk-internal",
        "jdk-non-portable",
        "jdk-system-out",
        "jdk-reflection",
    )
    ignoreFailures = false
}

// ---------- Out-of-date dependencies ----------
fun isPreRelease(version: String) =
    Regex("(?i).*[.-](alpha|beta|rc|cr|m|milestone|preview|snapshot|ea)[.\\d-]*").matches(version)

tasks.withType<DependencyUpdatesTask>().configureEach {
    rejectVersionIf { isPreRelease(candidate.version) && !isPreRelease(currentVersion) }
    outputFormatter = "json,plain"
    outputDir = layout.buildDirectory.dir("reports/dependencyUpdates").get().asFile.path
    reportfileName = "report"
}

// Reads the versions report. Warns about out-of-date dependencies, or fails when quality.failOnStaleDependencies=true.
val failOnStaleDependencies =
    providers.gradleProperty("quality.failOnStaleDependencies").map { it.toBoolean() }.orElse(false)
val dependencyReport = layout.buildDirectory.file("reports/dependencyUpdates/report.json")
val checkDependencyVersions = tasks.register("checkDependencyVersions") {
    group = "verification"
    description = "Reports out-of-date dependencies; fails if quality.failOnStaleDependencies is true."
    dependsOn(tasks.withType<DependencyUpdatesTask>())
    val report = dependencyReport
    val fail = failOnStaleDependencies
    doLast {
        @Suppress("UNCHECKED_CAST")
        val outdated = (JsonSlurper().parse(report.get().asFile) as Map<String, Any>)["outdated"] as Map<String, Any>
        val count = (outdated["count"] as Number).toInt()
        if (count > 0) {
            val message = "$count out-of-date dependencies; see ${report.get().asFile.parentFile}/report.txt"
            if (fail.get()) throw GradleException(message) else logger.warn(message)
        }
    }
}

if (providers.environmentVariable("CI").isPresent) {
    tasks.named("check") {
        dependsOn(checkDependencyVersions)
    }
}

// ---------- Mutation testing (opt-in): PIT, run from its command line ----------
val pitestTool = configurations.create("pitestTool") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    pitestTool("org.pitest:pitest-command-line:${version("pitest")}")
    pitestTool("org.pitest:pitest-junit5-plugin:${version("pitest-junit5")}")
}
val pitReports = layout.buildDirectory.dir("reports/pitest")
val mainSourceSet = sourceSetsForAdvice.named("main")
val mutationSummary = tasks.register<MutationSummaryTask>("mutationSummary") {
    group = "verification"
    description = "Summarises the last PIT run: coverage, planted bugs caught and missed, and where."
    reportDir = pitReports
    sourceDirs.from(mainSourceSet.map { it.java.srcDirs })
}
tasks.register<JavaExec>("pitest") {
    group = "verification"
    description = "Plants bugs (mutations) in the main code, runs the tests against each (PIT), and summarises."
    dependsOn(tasks.named("testClasses"))
    classpath = pitestTool + sourceSetsForAdvice["test"].runtimeClasspath
    mainClass = "org.pitest.mutationtest.commandline.MutationCoverageReport"
    javaLauncher = toolchainLauncher
    // The project's own classes: those in the packages its main code is in, whatever its group (or none).
    val sources = files(mainSourceSet.map { it.java.srcDirs }).filter { it.isDirectory }
    val classes = files(mainSourceSet.map { it.output.classesDirs })
    val reports = pitReports
    val minimumScore = underTest.minMutationScore
    // With underTest.minMutationScore, pitest fails when the tests catch fewer of the planted bugs, in percent.
    argumentProviders.add(CommandLineArgumentProvider {
        minimumScore.map { listOf("--mutationThreshold", "$it") }.getOrElse(emptyList())
    })
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "--targetClasses", JavaPackages.targets(sources.files).joinToString(","),
            "--sourceDirs", sources.files.joinToString(","),
            "--mutableCodePaths", classes.files.joinToString(","),
            "--reportDir", reports.get().asFile.path,
            "--outputFormats", "HTML,XML",
            "--timestampedReports=false",
            "--fullMutationMatrix=true",
            "--verbosity", "QUIET",
        )
    })
    // Only PIT's warnings and errors reach the console; mutationSummary says the rest in plain English.
    doFirst {
        val pit = this as JavaExec
        pit.standardOutput = OutputStream.nullOutputStream()
        pit.errorOutput = InfoLineFilter(System.err)
    }
    finalizedBy(mutationSummary)
}

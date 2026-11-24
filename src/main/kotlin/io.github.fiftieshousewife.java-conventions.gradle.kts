// The code-quality standards for all our Java. Three layers (see under-test's decision guides on using the toolkit in
// an organisation):
//  - environment (proxy, package repository): never set here; it comes from the organisation's Gradle init script;
//  - code-quality standards: Checkstyle, PMD (complexity, the assertAll rule), copy-paste detection, SpotBugs with
//    FindSecBugs, forbidden APIs. The rules inside this plugin are defaults: `underTest.codeStyle` = TOOLKIT,
//    ORGANISATION (the organisation's rule files) or NONE (the organisation's own plugin does it);
//  - testing evidence: coverage verification, always on; only the threshold changes.
// Every underTest setting can also be a Gradle property, e.g. -PunderTest.codeStyle=NONE, or in an init script.
// Advice (for-each loops) is reported by pmdAdvice and never fails the build. Mutation testing is opt-in: pitest.

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import groovy.json.JsonSlurper
import io.github.fiftieshousewife.javaconventions.CodeStyleSource
import io.github.fiftieshousewife.javaconventions.InfoLineFilter
import io.github.fiftieshousewife.javaconventions.MutationSummaryTask
import io.github.fiftieshousewife.javaconventions.UnderTestExtension
import java.io.OutputStream
import java.util.Properties

plugins {
    java
    jacoco
    checkstyle
    pmd
    id("com.github.spotbugs")
    id("de.thetaphi.forbiddenapis")
    id("io.github.ben-manes.versions")
    id("info.solidsoft.pitest")
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
    minLineCoverage.convention(
        providers.gradleProperty("underTest.minLineCoverage").orElse(version("min-line-coverage")).map { it.toBigDecimal() },
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
                val message = "underTest.codeStyle is ORGANISATION, so set underTest.$setting to the organisation's file"
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
    // Logging: Lombok's @Slf4j on the SLF4J API, with Log4j 2 behind it at run time.
    "compileOnly"("org.projectlombok:lombok:${version("lombok")}")
    "annotationProcessor"("org.projectlombok:lombok:${version("lombok")}")
    "testCompileOnly"("org.projectlombok:lombok:${version("lombok")}")
    "testAnnotationProcessor"("org.projectlombok:lombok:${version("lombok")}")
    "implementation"("org.slf4j:slf4j-api:${version("slf4j")}")
    "runtimeOnly"("org.apache.logging.log4j:log4j-core:${version("log4j")}")
    "runtimeOnly"("org.apache.logging.log4j:log4j-slf4j2-impl:${version("log4j")}")

    "testImplementation"(platform("org.junit:junit-bom:${version("junit")}"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testImplementation"("org.assertj:assertj-core:${version("assertj")}")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")

    "spotbugsPlugins"("com.h3xstream.findsecbugs:findsecbugs-plugin:${version("findsecbugs")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-processing", "-Werror", "-parameters"))
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
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = underTest.minLineCoverage.get()
            }
        }
    }
}

// JaCoCo skips its verification when no test ran, which would let code with no tests pass the floor.
val mainCode = extensions.getByType<SourceSetContainer>()["main"].allJava
val coverageData = files(tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification").map { it.executionData })
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
listOf(Checkstyle::class, Pmd::class, SpotBugsTask::class, CheckForbiddenApis::class).forEach { type ->
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

tasks.withType<Checkstyle>().configureEach {
    if (!useOrganisationFile(underTest.checkstyleConfig, "checkstyleConfig") { configFile = it; configDirectory = it.parentFile }) {
        config = bundledRules("checkstyle.xml")
    }
}

pmd {
    toolVersion = version("pmd")
    ruleSets = emptyList()
    isConsoleOutput = true
    isIgnoreFailures = false
}

val advice = "pmdAdvice"
tasks.withType<Pmd>().configureEach {
    if (name != advice && !useOrganisationFile(underTest.pmdRuleset, "pmdRuleset") { ruleSetFiles = files(it) }) {
        ruleSetConfig = bundledRules("pmd-ruleset.xml")
    }
    // The JDK's own classes, from the toolchain, so PMD resolves types against the JDK the code is built with.
    val jdkClasses = toolchainLauncher.map { it.metadata.installationPath.file("lib/jrt-fs.jar") }
    classpath = (classpath ?: files()).plus(files(jdkClasses))
}

// ---------- Advice: PMD rules that are reported but never fail the build, such as preferring streams to loops ----------
val sourceSetsForAdvice = extensions.getByType<SourceSetContainer>()
val pmdAdvice = tasks.register<Pmd>(advice) {
    group = "verification"
    description = "Reports, without failing the build, what the plugin's advice rules find."
    source(sourceSetsForAdvice["main"].allJava, sourceSetsForAdvice["test"].allJava)
    classpath = sourceSetsForAdvice["test"].runtimeClasspath
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
    description = "Fails if 75 or more tokens of Java code are copy-pasted (PMD's CPD)."
    val style = underTest.codeStyle
    onlyIf("underTest.codeStyle is not NONE") { style.get() != CodeStyleSource.NONE }
    val sources = files("src/main/java", "src/test/java", "src/integrationTest/java").filter { it.isDirectory }
    inputs.files(sources).withPathSensitivity(PathSensitivity.RELATIVE)
    classpath = cpdTool
    mainClass = "net.sourceforge.pmd.cli.PmdCli"
    javaLauncher = toolchainLauncher
    args("cpd", "--minimum-tokens", "75", "--language", "java", "--format", "text")
    argumentProviders.add(CommandLineArgumentProvider { sources.files.sorted().flatMap { listOf("--dir", it.path) } })
}
tasks.named("check") {
    dependsOn(cpdCheck)
}

// ---------- Bugs and security bugs ----------
spotbugs {
    toolVersion = version("spotbugs")
    effort = Effort.MAX
    reportLevel = Confidence.LOW
    ignoreFailures = false
}

tasks.withType<SpotBugsTask>().configureEach {
    reports.create("html") { required = true }
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

tasks.withType<CheckForbiddenApis>().configureEach {
    if (!useOrganisationFile(underTest.forbiddenApiSignatures, "forbiddenApiSignatures") { signaturesFiles = files(it) }) {
        signaturesURLs = setOf(bundled.getResource("forbidden-signatures.txt")!!)
    }
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
val failOnStaleDependencies = providers.gradleProperty("quality.failOnStaleDependencies").map { it.toBoolean() }.orElse(false)
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

// ---------- Mutation testing (opt-in) ----------
pitest {
    pitestVersion = version("pitest")
    junit5PluginVersion = version("pitest-junit5")
    targetClasses = provider { listOf("${project.group}.*") }
    outputFormats = listOf("HTML", "XML")
    timestampedReports = false
    fullMutationMatrix = true
    verbosity = "QUIET"
}

val mutationSummary = tasks.register<MutationSummaryTask>("mutationSummary") {
    group = "verification"
    description = "Summarises the last PIT run: coverage, planted bugs caught and missed, and where."
    reportDir = layout.buildDirectory.dir("reports/pitest")
    sourceDirs.from(sourceSetsForAdvice.named("main").map { it.java.srcDirs })
}
tasks.named<JavaExec>("pitest") {
    doFirst {
        val pit = this as JavaExec
        pit.standardOutput = OutputStream.nullOutputStream()
        pit.errorOutput = InfoLineFilter(System.err)
    }
    finalizedBy(mutationSummary)
}

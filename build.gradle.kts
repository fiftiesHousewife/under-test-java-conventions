// The java-conventions plugin: the code-quality standards for all our Java, applied by each repo at a pinned version.
// Its tool versions (from gradle/libs.versions.toml) and rule files go inside its jar, so a repo applying it reads
// nothing of its own but an organisation's rule files, when it chooses them.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
    alias(libs.plugins.nmcp)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.detekt)
}

// detekt checks this repo's own Kotlin, the plugin and its tests, as part of check. It's in this build only: the
// plugin's jar and POM carry nothing of it. detekt runs on the Kotlin it was built with, not the newer one Gradle has.
detekt {
    buildUponDefaultConfig = true
    config.setFrom("config/detekt.yml")
    source.setFrom(
        "src/main/kotlin", "src/test/kotlin", "src/functionalTest/kotlin", "build.gradle.kts", "settings.gradle.kts",
    )
}
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(libs.versions.detekt.kotlin.get())
        }
    }
}

group = "io.github.fiftieshousewife"

dependencies {
    implementation(libs.gradle.spotbugs.plugin)
    implementation(libs.gradle.forbiddenapis.plugin)
    implementation(libs.gradle.versions.plugin)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// The tool versions a repo applying the plugin gets, written where the plugin's classes read them.
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val toolVersions = listOf(
    "java", "junit", "assertj", "lombok", "slf4j", "log4j", "jacoco", "checkstyle", "pmd", "spotbugs", "findsecbugs",
    "pitest", "pitest-junit5", "min-line-coverage", "cpd-minimum-tokens", "max-classes-per-package",
)
val generatedResources = layout.buildDirectory.dir("generated/versions")
val writeVersions = tasks.register<WriteProperties>("writeVersions") {
    destinationFile =
        generatedResources.map { it.file("io/github/fiftieshousewife/javaconventions/versions.properties") }
    toolVersions.forEach { property(it, catalog.findVersion(it).get().requiredVersion) }
}
sourceSets.main {
    resources.srcDir(files(generatedResources).builtBy(writeVersions))
}

gradlePlugin {
    website = "https://github.com/fiftiesHousewife/under-test-java-conventions"
    vcsUrl = "https://github.com/fiftiesHousewife/under-test-java-conventions"
}

// The plugin's classes run in the applying build's Gradle, so they target the oldest JDK it may run on, whichever JDK
// builds them: Java 21.
java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

// What would be published, and what PublishedPluginTest applies the plugin from.
val localRepo = layout.buildDirectory.dir("local-repo")

// Unit tests: the plugin's classes, on their own, in seconds.
tasks.test {
    useJUnitPlatform()
}

// Functional tests: sample projects built with Gradle TestKit, in their own source set, as Gradle lays out a plugin's.
val functionalTest: SourceSet = sourceSets.create("functionalTest")
configurations[functionalTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[functionalTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())
dependencies {
    "functionalTestImplementation"(gradleTestKit())
}
gradlePlugin {
    testSourceSets(functionalTest)
}

// The sample builds need the network the outer build has.
val proxySettings = listOf(
    "https.proxyHost", "https.proxyPort", "http.proxyHost", "http.proxyPort", "https.proxyUser", "https.proxyPassword",
    "http.proxyUser", "http.proxyPassword", "http.nonProxyHosts", "jdk.http.auth.tunneling.disabledSchemes",
)
val functionalTestTask = tasks.register<Test>("functionalTest") {
    group = "verification"
    description = "Builds sample projects with the plugin, with Gradle TestKit: each rule's breach, and clean code."
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
    // Where the sample builds find JDKs when Gradle can't detect them itself (as inside a sandbox).
    val jdkPaths = providers.gradleProperty("org.gradle.java.installations.paths")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        proxySettings.mapNotNull { key -> System.getProperty(key)?.let { "-D$key=$it" } } +
            jdkPaths.map { listOf("-Djavaconventions.jdkPaths=$it") }.getOrElse(emptyList())
    })
    // PublishedPluginTest applies the plugin as a user's build does: by id and version, from the published jar, POM
    // and plugin marker, on this build's Gradle and on the oldest it supports.
    dependsOn("publishAllPublicationsToLocalRepository")
    val repo = localRepo
    val pluginVersion = project.version.toString()
    val oldestGradle = libs.versions.oldest.gradle
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Djavaconventions.repo=${repo.get().asFile.absolutePath}",
            "-Djavaconventions.version=$pluginVersion",
            "-Djavaconventions.oldestGradle=${oldestGradle.get()}",
        )
    })
}
tasks.check {
    dependsOn(functionalTestTask)
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(localRepo)
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "java-conventions"
            description = "The code-quality standards for Java: Checkstyle, PMD and its copy-paste detector, " +
                "SpotBugs with FindSecBugs, forbidden APIs and a coverage floor, as one Gradle plugin."
            url = "https://github.com/fiftiesHousewife/under-test-java-conventions"
            licenses {
                license {
                    name = "Apache License 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                }
            }
            developers {
                developer {
                    id = "fiftieshousewife"
                    name = "java-conventions maintainers"
                    url = "https://github.com/fiftiesHousewife"
                }
            }
            scm {
                url = "https://github.com/fiftiesHousewife/under-test-java-conventions"
                connection = "scm:git:https://github.com/fiftiesHousewife/under-test-java-conventions.git"
                developerConnection = "scm:git:https://github.com/fiftiesHousewife/under-test-java-conventions.git"
            }
        }
    }
}

// Signing needs signingInMemoryKey and signingInMemoryKeyPassword; without them, only the local repository accepts.
val signingKey = providers.gradleProperty("signingInMemoryKey")
signing {
    isRequired = signingKey.isPresent
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), providers.gradleProperty("signingInMemoryKeyPassword").orNull)
        sign(publishing.publications)
    }
}

// Maven Central, through its Central Portal: centralPortalUsername and centralPortalPassword (a Central Portal user
// token), as Gradle properties or ORG_GRADLE_PROJECT_ environment variables.
nmcpAggregation {
    centralPortal {
        username = providers.gradleProperty("centralPortalUsername")
        password = providers.gradleProperty("centralPortalPassword")
        publishingType = "AUTOMATIC"
    }
}
dependencies {
    nmcpAggregation(project(":"))
}

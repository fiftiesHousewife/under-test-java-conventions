// The java-conventions plugin: the code-quality standards for all our Java, applied by each repo at a pinned version.
// Its tool versions (from gradle/libs.versions.toml) and rule files go inside its jar, so a repo applying it reads
// nothing of its own but an organisation's rule files, when it chooses them.
plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
    alias(libs.plugins.nmcp)
    alias(libs.plugins.nmcp.aggregation)
}

group = "io.github.fiftieshousewife"

dependencies {
    implementation(libs.gradle.pitest.plugin)
    implementation(libs.gradle.spotbugs.plugin)
    implementation(libs.gradle.forbiddenapis.plugin)
    implementation(libs.gradle.versions.plugin)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(gradleTestKit())
    testRuntimeOnly(libs.junit.platform.launcher)
}

// The tool versions a repo applying the plugin gets, written where the plugin's classes read them.
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val toolVersions = listOf(
    "java", "junit", "assertj", "lombok", "slf4j", "log4j", "jacoco", "checkstyle", "pmd", "spotbugs", "findsecbugs",
    "pitest", "pitest-junit5", "min-line-coverage",
)
val generatedResources = layout.buildDirectory.dir("generated/versions")
val writeVersions = tasks.register<WriteProperties>("writeVersions") {
    destinationFile = generatedResources.map { it.file("io/github/fiftieshousewife/javaconventions/versions.properties") }
    toolVersions.forEach { property(it, catalog.findVersion(it).get().requiredVersion) }
}
sourceSets.main {
    resources.srcDir(files(generatedResources).builtBy(writeVersions))
}

gradlePlugin {
    website = "https://github.com/fiftiesHousewife/java-conventions"
    vcsUrl = "https://github.com/fiftiesHousewife/java-conventions"
}

java {
    withSourcesJar()
    withJavadocJar()
}

// The functional tests build sample projects with Gradle TestKit; they need the network the outer build has.
val proxySettings = listOf(
    "https.proxyHost", "https.proxyPort", "http.proxyHost", "http.proxyPort", "https.proxyUser", "https.proxyPassword",
    "http.proxyUser", "http.proxyPassword", "http.nonProxyHosts", "jdk.http.auth.tunneling.disabledSchemes",
)
tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1
    // Where the sample builds find JDKs when Gradle can't detect them itself (as inside a sandbox).
    val jdkPaths = providers.gradleProperty("org.gradle.java.installations.paths")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        proxySettings.mapNotNull { key -> System.getProperty(key)?.let { "-D$key=$it" } } +
            jdkPaths.map { listOf("-Djavaconventions.jdkPaths=$it") }.getOrElse(emptyList())
    })
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("local-repo"))
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "java-conventions"
            description = "The code-quality standards for Java: Checkstyle, PMD and its copy-paste detector, SpotBugs " +
                "with FindSecBugs, forbidden APIs and a coverage floor, as one Gradle plugin."
            url = "https://github.com/fiftiesHousewife/java-conventions"
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
                url = "https://github.com/fiftiesHousewife/java-conventions"
                connection = "scm:git:https://github.com/fiftiesHousewife/java-conventions.git"
                developerConnection = "scm:git:https://github.com/fiftiesHousewife/java-conventions.git"
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

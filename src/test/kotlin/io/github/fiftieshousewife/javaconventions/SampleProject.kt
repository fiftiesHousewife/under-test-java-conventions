package io.github.fiftieshousewife.javaconventions

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/**
 * A small Java project applying the plugin, built with Gradle TestKit. By default the plugin being tested is put on its
 * class path; [published] instead applies it as a user's build does, by id and version from the repository this build
 * publishes to, so its jar, POM and plugin marker are what's tested.
 */
class SampleProject(private val dir: File, private val published: Boolean = false) {

    private var gradleVersion: String? = null

    init {
        dir.resolve("settings.gradle.kts").writeText(
            (if (published) publishedPluginManagement() else "") +
                """
                dependencyResolutionManagement {
                    repositories {
                        mavenCentral()
                    }
                }
                rootProject.name = "sample"
                """.trimIndent(),
        )
        val version = if (published) " version \"${property("javaconventions.version")}\"" else ""
        dir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("io.github.fiftieshousewife.java-conventions")$version
            }
            group = "com.example"
            """.trimIndent(),
        )
    }

    fun main(path: String, source: String) = write("src/main/java/$path", source)

    fun test(path: String, source: String) = write("src/test/java/$path", source)

    fun file(path: String): File = dir.resolve(path)

    /** Adds lines to the end of the sample's build script, after its plugins block and group. */
    fun buildScript(lines: String): SampleProject = apply {
        dir.resolve("build.gradle.kts").appendText("\n" + lines.trimIndent() + "\n")
    }

    /** Builds with the given Gradle version, downloaded by TestKit, instead of the Gradle running this build. */
    fun onGradle(version: String): SampleProject = apply { gradleVersion = version }

    fun build(vararg arguments: String): BuildResult = runner(arguments).build()

    fun buildAndFail(vararg arguments: String): BuildResult = runner(arguments).buildAndFail()

    /** Writes a file at a path relative to the sample project. */
    fun write(path: String, source: String): SampleProject = apply {
        dir.resolve(path).apply { parentFile.mkdirs() }.writeText(source.trimIndent())
    }

    private fun runner(arguments: Array<out String>): GradleRunner = GradleRunner.create()
        .withProjectDir(dir)
        .apply { if (!published) withPluginClasspath() }
        .apply { gradleVersion?.let { withGradleVersion(it) } }
        .withArguments(listOf("--stacktrace") + proxyArguments() + arguments)
        .forwardOutput()

    private fun proxyArguments(): List<String> = PROXY_SETTINGS.mapNotNull { key ->
        System.getProperty(key)?.let { "-D$key=$it" }
    } + listOfNotNull(
        System.getProperty("javaconventions.jdkPaths")?.let { "-Porg.gradle.java.installations.paths=$it" },
    )

    private companion object {
        val PROXY_SETTINGS = listOf(
            "https.proxyHost", "https.proxyPort", "http.proxyHost", "http.proxyPort", "https.proxyUser",
            "https.proxyPassword", "http.proxyUser", "http.proxyPassword", "http.nonProxyHosts",
            "jdk.http.auth.tunneling.disabledSchemes",
        )

        fun property(name: String): String =
            requireNotNull(System.getProperty(name)) {
                "$name is set by the build's test task; run the tests with Gradle"
            }

        // The README's repositories, with the repository this build publishes to first.
        fun publishedPluginManagement(): String = listOf(
            "pluginManagement {",
            "    repositories {",
            "        maven { url = uri(\"${File(property("javaconventions.repo")).toURI()}\") }",
            "        gradlePluginPortal()",
            "        mavenCentral()",
            "    }",
            "}",
            "",
        ).joinToString("\n")
    }
}

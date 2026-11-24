package io.github.fiftieshousewife.javaconventions

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/** A small Java project applying the plugin, built with Gradle TestKit. */
class SampleProject(private val dir: File) {

    init {
        dir.resolve("settings.gradle.kts").writeText(
            """
            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                }
            }
            rootProject.name = "sample"
            """.trimIndent(),
        )
        dir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("io.github.fiftieshousewife.java-conventions")
            }
            group = "com.example"
            """.trimIndent(),
        )
    }

    fun main(path: String, source: String) = write("src/main/java/$path", source)

    fun test(path: String, source: String) = write("src/test/java/$path", source)

    fun file(path: String): File = dir.resolve(path)

    fun build(vararg arguments: String): BuildResult = runner(arguments).build()

    fun buildAndFail(vararg arguments: String): BuildResult = runner(arguments).buildAndFail()

    private fun write(path: String, source: String): SampleProject = apply {
        dir.resolve(path).apply { parentFile.mkdirs() }.writeText(source.trimIndent())
    }

    private fun runner(arguments: Array<out String>): GradleRunner = GradleRunner.create()
        .withProjectDir(dir)
        .withPluginClasspath()
        .withArguments(listOf("--stacktrace") + proxyArguments() + arguments)
        .forwardOutput()

    private fun proxyArguments(): List<String> = PROXY_SETTINGS.mapNotNull { key ->
        System.getProperty(key)?.let { "-D$key=$it" }
    } + listOfNotNull(System.getProperty("javaconventions.jdkPaths")?.let { "-Porg.gradle.java.installations.paths=$it" })

    private companion object {
        val PROXY_SETTINGS = listOf(
            "https.proxyHost", "https.proxyPort", "http.proxyHost", "http.proxyPort", "https.proxyUser",
            "https.proxyPassword", "http.proxyUser", "http.proxyPassword", "http.nonProxyHosts",
            "jdk.http.auth.tunneling.disabledSchemes",
        )
    }
}

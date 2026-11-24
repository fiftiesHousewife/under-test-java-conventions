package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * The plugin as a user gets it: applied by id and version from the published jar, POM and plugin marker (in the
 * repository this build publishes to), with the README's plugin repositories. A missing dependency, a wrong marker or
 * a resource left out of the jar fails here, where the class-path tests can't see it.
 */
class PublishedPluginTest {

    @Test
    fun `the published plugin passes check on a project that meets the standards`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir, published = true)).build("check")
        assertThat(result.task(":check")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `the published plugin passes check on the oldest Gradle it supports`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir, published = true))
            .onGradle(System.getProperty("javaconventions.oldestGradle"))
            .build("check")
        assertThat(result.task(":check")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `the jar is published as io_github_fiftieshousewife java-conventions, where the plugin marker points`() {
        val version = System.getProperty("javaconventions.version")
        val jar = File(System.getProperty("javaconventions.repo"))
            .resolve("io/github/fiftieshousewife/java-conventions/$version/java-conventions-$version.jar")
        assertThat(jar).isFile()
    }
}

package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipFile

class BuildOutputTest {

    @Test
    fun `a jar's entries carry no build-time timestamps, so the same code gives the same jar`(@TempDir dir: File) {
        val project = CleanSample.into(SampleProject(dir))
        project.build("jar")
        val times = ZipFile(project.file("build/libs/sample.jar")).use { jar ->
            jar.entries().asSequence().map { it.time }.toSet()
        }
        assertThat(times).hasSize(1)
    }

    @Test
    fun `check works with the configuration cache, in CI too`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).withEnvironment(mapOf("CI" to "true"))
            .build("check", "--configuration-cache")
        assertThat(result.task(":check")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

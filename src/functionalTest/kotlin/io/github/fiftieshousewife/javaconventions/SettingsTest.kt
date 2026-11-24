package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SettingsTest {

    private val codeStyleChecks = listOf(
        ":checkstyleMain", ":pmdMain", ":spotbugsMain", ":forbiddenApisMain", ":cpdCheck", ":checkPackageSize",
    )

    @Test
    fun `with codeStyle NONE every code-quality check is skipped`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir))
            .build(*codeStyleChecks.map { it.drop(1) }.toTypedArray(), "-PunderTest.codeStyle=NONE")
        assertAll(
            codeStyleChecks.map { task ->
                Executable { assertThat(result.task(task)?.outcome).`as`(task).isEqualTo(TaskOutcome.SKIPPED) }
            },
        )
    }
}

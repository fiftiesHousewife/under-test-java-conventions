package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CoverageTest {

    // Its test runs two of its three lines: 67%.
    private fun halfTested(project: SampleProject) = project.main(
        "com/example/Halves.java",
        """
        package com.example;

        public class Halves {
            public int first() {
                return 1;
            }

            public int second() {
                return 2;
            }
        }
        """,
    ).test(
        "com/example/HalvesTest.java",
        """
        package com.example;

        import static org.assertj.core.api.Assertions.assertThat;

        import org.junit.jupiter.api.Test;

        class HalvesTest {
            @Test
            void firstIsOne() {
                assertThat(new Halves().first()).isEqualTo(1);
            }
        }
        """,
    )

    @Test
    fun `line coverage under 80 percent fails the build`(@TempDir dir: File) {
        val result = halfTested(SampleProject(dir)).buildAndFail("jacocoTestCoverageVerification")
        assertThat(result.output).contains("lines covered ratio is 0.66, but expected minimum is 0.80")
    }

    @Test
    fun `an organisation can change the coverage floor`(@TempDir dir: File) {
        val result = halfTested(SampleProject(dir))
            .build("jacocoTestCoverageVerification", "-PunderTest.minLineCoverage=0.6")
        assertThat(result.task(":jacocoTestCoverageVerification")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

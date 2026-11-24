package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CopyPasteTest {

    // About 60 tokens: over the threshold of 50, under the 75 it replaced.
    private fun scorer(name: String) = """
        package com.example;

        public class $name {
            public int score(final int a, final int b) {
                final int sum = a + b;
                final int product = a * b;
                final int difference = sum - product;
                final int total = sum + product;
                return difference > total ? difference - total : total - difference;
            }
        }
    """

    @Test
    fun `a method copied into another class fails the copy-paste check`(@TempDir dir: File) {
        val result = SampleProject(dir)
            .main("com/example/First.java", scorer("First"))
            .main("com/example/Second.java", scorer("Second"))
            .buildAndFail("cpdCheck")
        assertThat(result.output).contains("duplication in the following files")
    }

    @Test
    fun `copied code in a source set added after the plugin is found too`(@TempDir dir: File) {
        val result = SampleProject(dir)
            .buildScript("""sourceSets.create("integrationTest")""")
            .main("com/example/First.java", scorer("First"))
            .write("src/integrationTest/java/com/example/Second.java", scorer("Second"))
            .buildAndFail("cpdCheck")
        assertThat(result.output).contains("src/integrationTest/java/com/example/Second.java")
    }

    @Test
    fun `a project with no Java code skips the copy-paste check`(@TempDir dir: File) {
        val result = SampleProject(dir).build("cpdCheck")
        assertThat(result.task(":cpdCheck")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `an organisation can raise the copy-paste limit`(@TempDir dir: File) {
        val result = SampleProject(dir)
            .main("com/example/First.java", scorer("First"))
            .main("com/example/Second.java", scorer("Second"))
            .build("cpdCheck", "-PunderTest.cpdMinimumTokens=100")
        assertThat(result.task(":cpdCheck")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

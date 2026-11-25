package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** Code a repo keeps below the standards on purpose, in a legacy package and a weak test, left out by a setting. */
class CodeStyleExcludesTest {

    private val legacy = "com/example/legacy"

    private fun scorer(name: String) = """
        package com.example.legacy;

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

    private val bugs = """
        package com.example.legacy;

        public class Bugs {
            public boolean same(final String a, final String b) {
                return a == b;
            }
        }
    """

    private val weakTest = """
        package com.example;

        import org.junit.jupiter.api.Test;

        class GreetingWeakTest {
            @Test
            void greets() {
                new Greeting().greet("Ada");
            }
        }
    """

    private fun belowTheStandards(dir: File): SampleProject = CleanSample.into(SampleProject(dir)).apply {
        (StyleBreaches.SAMPLES + ApiBreaches.SAMPLES).forEach { (path, source) ->
            main(path.replace("com/example/", "$legacy/"), source.replace("com.example;", "com.example.legacy;"))
        }
        (1..21).forEach { main("$legacy/Part$it.java", "package com.example.legacy;\n\npublic class Part$it {\n}\n") }
        main("$legacy/First.java", scorer("First"))
        main("$legacy/Second.java", scorer("Second"))
        main("$legacy/Bugs.java", bugs)
        test("com/example/GreetingWeakTest.java", weakTest)
    }

    private val checks = listOf(
        "checkstyleMain", "pmdMain", "pmdTest", "forbiddenApisMain", "cpdCheck", "checkPackageSize", "spotbugsMain",
    )

    @Test
    fun `without the setting, the code below the standards fails every code-quality check`(@TempDir dir: File) {
        val result = belowTheStandards(dir).buildAndFail(*(checks + "--continue").toTypedArray())
        assertAll(
            checks.map { task -> Executable { assertThat(result.task(":$task")?.outcome).isEqualTo(FAILED) } },
        )
    }

    @Test
    fun `the paths in codeStyleExcludes are left out of every code-quality check`(@TempDir dir: File) {
        val result = belowTheStandards(dir)
            .buildScript("underTest { codeStyleExcludes = listOf(\"**/legacy/**\", \"**/*WeakTest.java\") }")
            .build(*checks.toTypedArray())
        assertAll(
            checks.map { task -> Executable { assertThat(result.task(":$task")?.outcome).isEqualTo(SUCCESS) } },
        )
    }

    @Test
    fun `codeStyleExcludes can be a Gradle property, its patterns separated by commas`(@TempDir dir: File) {
        val result = belowTheStandards(dir).build(
            "checkstyleMain", "pmdTest", "-PunderTest.codeStyleExcludes=**/legacy/**, **/*WeakTest.java",
        )
        assertThat(result.task(":pmdTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

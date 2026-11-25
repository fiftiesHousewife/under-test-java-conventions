package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JavaConventionsTest {

    @Test
    fun `a project that meets the standards passes check`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).build("check")
        assertThat(result.task(":check")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `a project with code but no tests fails the coverage floor`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", CleanSample.GREETING).buildAndFail("check")
        assertThat(result.output).contains("There is main code but no test ran, so its line coverage is 0%")
    }

    @Test
    fun `a method of more than twenty lines fails Checkstyle`(@TempDir dir: File) {
        val lines = (1..21).joinToString("\n") { "        total += $it;" }
        val result = SampleProject(dir).main(
            "com/example/Long.java",
            "package com.example;\n\npublic class Long {\n    public int sum() {\n        int total = 0;\n$lines\n" +
                "        return total;\n    }\n}\n",
        ).buildAndFail("checkstyleMain")
        assertThat(result.output).contains("[MethodLength]")
    }

    @Test
    fun `a for-each loop is reported as advice and does not fail the build`(@TempDir dir: File) {
        val project = SampleProject(dir).main(
            "com/example/Names.java",
            """
            package com.example;

            import java.util.List;

            public class Names {
                public int letters(final List<String> names) {
                    int total = 0;
                    for (final String name : names) {
                        total += name.length();
                    }
                    return total;
                }
            }
            """,
        )
        project.build("pmdMain", "pmdAdvice")
        assertThat(project.file("build/reports/pmd/advice.xml").readText()).contains("PreferStreamsToForEachLoops")
    }

    @Test
    fun `PMD reads the JDK's classes from the toolchain, for advice too`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", CleanSample.GREETING)
            .build("pmdMain", "pmdAdvice")
        assertThat(result.output).doesNotContain("Adding current platform")
    }

    @Test
    fun `a test making two assertion statements fails PMD`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", CleanSample.GREETING).test(
            "com/example/GreetingTest.java",
            """
            package com.example;

            import static org.assertj.core.api.Assertions.assertThat;

            import org.junit.jupiter.api.Test;

            class GreetingTest {
                @Test
                void greetsByName() {
                    assertThat(new Greeting().greet("Ada")).startsWith("Hello");
                    assertThat(new Greeting().greet("Ada")).endsWith("Ada");
                }
            }
            """,
        ).buildAndFail("pmdTest")
        assertThat(result.output).contains("MultipleAssertionsMustUseAssertAll")
    }

    @Test
    fun `a test making no assertion fails PMD`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", CleanSample.GREETING).test(
            "com/example/GreetingTest.java",
            """
            package com.example;

            import org.junit.jupiter.api.Test;

            class GreetingTest {
                @Test
                void greetsByName() {
                    new Greeting().greet("Ada");
                }
            }
            """,
        ).buildAndFail("pmdTest")
        assertThat(result.output).contains("TestMustAssert")
    }

    @Test
    fun `with codeStyle NONE the code-quality checks are skipped`(@TempDir dir: File) {
        val lines = (1..21).joinToString("\n") { "        total += $it;" }
        val result = SampleProject(dir).main(
            "com/example/Long.java",
            "package com.example;\n\npublic class Long {\n    public int sum() {\n        int total = 0;\n$lines\n" +
                "        return total;\n    }\n}\n",
        ).build("checkstyleMain", "-PunderTest.codeStyle=NONE")
        assertThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `a method overriding a library's takes as many parameters as the library fixed`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Watcher.java",
            """
            package com.example;

            import java.awt.Image;
            import java.awt.image.ImageObserver;

            public class Watcher implements ImageObserver {
                @Override
                public boolean imageUpdate(final Image image, final int flags, final int x, final int y,
                        final int width, final int height) {
                    return flags == x + y + width + height && image != null;
                }
            }
            """,
        ).build("checkstyleMain", "pmdMain")
        assertThat(result.task(":pmdMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

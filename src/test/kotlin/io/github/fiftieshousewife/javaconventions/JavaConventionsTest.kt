package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JavaConventionsTest {

    private val greeting = """
        package com.example;

        public class Greeting {
            public String greet(final String name) {
                return "Hello, " + name;
            }
        }
    """

    private val greetingTest = """
        package com.example;

        import static org.assertj.core.api.Assertions.assertThat;

        import org.junit.jupiter.api.Test;

        class GreetingTest {
            @Test
            void greetsByName() {
                assertThat(new Greeting().greet("Ada")).isEqualTo("Hello, Ada");
            }
        }
    """

    @Test
    fun `a project that meets the standards passes check`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", greeting)
            .test("com/example/GreetingTest.java", greetingTest)
            .build("check")
        assertThat(result.task(":check")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
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
    fun `a test making two assertion statements fails PMD`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", greeting).test(
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
    fun `with codeStyle NONE the code-quality checks are skipped`(@TempDir dir: File) {
        val lines = (1..21).joinToString("\n") { "        total += $it;" }
        val result = SampleProject(dir).main(
            "com/example/Long.java",
            "package com.example;\n\npublic class Long {\n    public int sum() {\n        int total = 0;\n$lines\n" +
                "        return total;\n    }\n}\n",
        ).build("checkstyleMain", "-PunderTest.codeStyle=NONE")
        assertThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }
}

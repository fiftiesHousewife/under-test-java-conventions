package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class AssertionRulesTest {

    @Test
    fun `a test making two verify calls fails PMD, as two assertions do`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", CleanSample.GREETING).test(
            "com/example/GreetingTest.java",
            """
            package com.example;

            import static org.assertj.core.api.Assertions.assertThat;

            import org.junit.jupiter.api.Test;

            class GreetingTest {
                @Test
                void greetsByName() {
                    verifyGreets("Ada");
                    verifyGreets("Grace");
                }

                private static void verifyGreets(final String name) {
                    assertThat(new Greeting().greet(name)).endsWith(name);
                }
            }
            """,
        ).buildAndFail("pmdTest")
        assertThat(result.output).contains("MultipleAssertionsMustUseAssertAll")
    }

    @ParameterizedTest
    @ValueSource(strings = ["then()", "check()", "untilAsserted()", "shouldHold()"])
    fun `a test library's own assertion call counts as the test's assertion`(call: String, @TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Greeting.java", CleanSample.GREETING).test(
            "com/example/GreetingTest.java",
            """
            package com.example;

            import org.junit.jupiter.api.Test;

            class GreetingTest {
                @Test
                void greetsByName() {
                    new Checks(new Greeting().greet("Ada")).$call;
                }

                private static final class Checks {
                    private final String greeting;

                    Checks(final String greeting) {
                        this.greeting = greeting;
                    }

                    String then() {
                        return greeting;
                    }

                    String check() {
                        return greeting;
                    }

                    String untilAsserted() {
                        return greeting;
                    }

                    String shouldHold() {
                        return greeting;
                    }
                }
            }
            """,
        ).build("pmdTest")
        assertThat(result.task(":pmdTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

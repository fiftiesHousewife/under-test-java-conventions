package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
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
}

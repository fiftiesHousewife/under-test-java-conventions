package io.github.fiftieshousewife.javaconventions

/** A class and its test that meet every standard, so a sample project holding only them passes check. */
object CleanSample {

    const val GREETING = """
        package com.example;

        public class Greeting {
            public String greet(final String name) {
                return "Hello, " + name;
            }
        }
    """

    const val GREETING_TEST = """
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

    fun into(project: SampleProject): SampleProject =
        project.main("com/example/Greeting.java", GREETING).test("com/example/GreetingTest.java", GREETING_TEST)
}

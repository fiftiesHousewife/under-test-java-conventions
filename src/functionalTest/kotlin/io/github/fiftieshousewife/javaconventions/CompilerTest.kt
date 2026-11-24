package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CompilerTest {

    @Test
    fun `a compiler warning fails the build`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Raw.java",
            """
            package com.example;

            import java.util.ArrayList;
            import java.util.List;

            public class Raw {
                public List<?> names() {
                    final List names = new ArrayList();
                    return names;
                }
            }
            """,
        ).buildAndFail("compileJava")
        assertThat(result.output).contains("warnings found and -Werror specified")
    }

    @Test
    fun `Lombok's Slf4j annotation gives a class its logger`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Greeter.java",
            """
            package com.example;

            import lombok.extern.slf4j.Slf4j;

            @Slf4j
            public class Greeter {
                public void greet(final String name) {
                    log.info("Hello, {}", name);
                }
            }
            """,
        ).build("compileJava")
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

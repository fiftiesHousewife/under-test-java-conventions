package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** One sample project holding a breach of each rule; one build, and each rule must report its own. */
class EachRuleTest {

    private val breaches = mapOf(
        "com/example/LongFile.java" to "package com.example;\n\npublic class LongFile {\n" +
            (1..150).joinToString("\n") { "    // line $it" } + "\n}\n",
        "com/example/Grade.java" to """
            package com.example;

            public class Grade {
                public int grade(final int score) {
                    if (score > 90) { return 1; }
                    if (score > 80) { return 2; }
                    if (score > 70) { return 3; }
                    if (score > 60) { return 4; }
                    if (score > 50) { return 5; }
                    if (score > 40) { return 6; }
                    if (score > 30) { return 7; }
                    if (score > 20) { return 8; }
                    return 9;
                }
            }
        """,
        "com/example/Allowed.java" to """
            package com.example;

            public class Allowed {
                public boolean allowed(final boolean a, final boolean b, final boolean c) {
                    return a && b && c && !a || b;
                }
            }
        """,
        "com/example/Adder.java" to """
            package com.example;

            public class Adder {
                public int add(final int a, final int b, final int c, final int d) {
                    return a + b + c + d;
                }
            }
        """,
        "com/example/Point.java" to """
            package com.example;

            public record Point(int a, int b, int c, int d) {
            }
        """,
        "com/example/Sized.java" to """
            package com.example;

            public class Sized {
                // the size
                private final int size = 1;

                public int size() {
                    return size;
                }
            }
        """,
        "com/example/Hidden.java" to """
            package com.example;

            public class Hidden {
                public int answer() {
                    return value();
                }

                /** The value. */
                private int value() {
                    return 42;
                }
            }
        """,
        "com/example/Clock.java" to """
            package com.example;

            public class Clock {
                public long now() {
                    return System.currentTimeMillis();
                }
            }
        """,
    )

    @Test
    fun `each rule reports the breach made for it`(@TempDir dir: File) {
        val project = SampleProject(dir)
        breaches.forEach { (path, source) -> project.main(path, source) }
        val output = project.buildAndFail("checkstyleMain", "pmdMain", "forbiddenApisMain", "--continue").output
        assertAll(
            listOf(
                "[FileLength]", "[CyclomaticComplexity]", "[BooleanExpressionComplexity]", "[ParameterNumber]",
                "[RecordComponentNumber]", "A field has no comment", "[JavadocOnlyWhenPublic]", "CognitiveComplexity",
                "ExcessiveParameterList", "Forbidden method invocation: java.lang.System#currentTimeMillis()",
            ).map { expected -> Executable { assertThat(output).contains(expected) } },
        )
    }
}

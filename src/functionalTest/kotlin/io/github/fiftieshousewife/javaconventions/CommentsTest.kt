package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CommentsTest {

    @Test
    fun `a comment on an enum entry fails Checkstyle, as Javadoc or as a line before it`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Colour.java",
            """
            package com.example;

            public enum Colour {
                /** The colour of a stop sign. */
                RED,
                // the colour of grass
                GREEN,
                BLUE
            }
            """,
        ).buildAndFail("checkstyleMain")
        assertThat(Regex("\\[NoEnumEntryComment]").findAll(result.output).count()).isEqualTo(2)
    }

    @Test
    fun `a comment on a field fails Checkstyle, whatever its kind and wherever it sits`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Limits.java",
            """
            package com.example;

            import java.util.List;

            public class Limits {
                /** The most there can be. */
                public static final int MOST = 3;

                /* the fewest */
                static final int FEWEST = 1;

                // the default
                @SuppressWarnings("unused")
                private final int usual = 2;

                private final List<String> names = List.of(); // who they are

                public int sum() {
                    return MOST + FEWEST + usual + names.size();
                }
            }
            """,
        ).buildAndFail("checkstyleMain")
        assertThat(Regex("A field has no comment").findAll(result.output).count()).isEqualTo(4)
    }

    @Test
    fun `a comment on an interface's constant fails Checkstyle`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Sizes.java",
            """
            package com.example;

            public interface Sizes {
                /** The biggest size. */
                int BIGGEST = 10;
            }
            """,
        ).buildAndFail("checkstyleMain")
        assertThat(result.output).contains("A field has no comment")
    }
}

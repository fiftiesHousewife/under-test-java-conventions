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
}

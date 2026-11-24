package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ExemptionTest {

    private val fields = (1..20).map { "f$it" }

    // equals and hashCode of 20 fields: each more than 20 lines, with no branch or boolean operator to count.
    private val point = "package com.example;\n\npublic final class Point {\n" +
        fields.joinToString("") { "    private final int $it = 0;\n" } +
        "\n    @Override\n    public boolean equals(final Object other) {\n" +
        "        if (!(other instanceof Point)) {\n            return false;\n        }\n" +
        "        final Point that = (Point) other;\n        int differences = 0;\n" +
        fields.joinToString("") { "        differences += Math.abs(Integer.compare($it, that.$it));\n" } +
        "        return differences == 0;\n    }\n\n" +
        "    @Override\n    public int hashCode() {\n        int hash = 17;\n" +
        fields.joinToString("") { "        hash = 31 * hash + $it;\n" } +
        "        return hash;\n    }\n}\n"

    @Test
    fun `equals and hashCode may be longer than twenty lines`(@TempDir dir: File) {
        val result = SampleProject(dir).main("com/example/Point.java", point).build("checkstyleMain")
        assertThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

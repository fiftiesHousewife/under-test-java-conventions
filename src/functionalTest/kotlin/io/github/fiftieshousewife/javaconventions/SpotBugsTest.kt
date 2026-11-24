package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SpotBugsTest {

    @Test
    fun `a bug pattern and a hard-coded password each fail SpotBugs`(@TempDir dir: File) {
        val project = SampleProject(dir).main(
            "com/example/Bugs.java",
            """
            package com.example;

            import java.sql.Connection;
            import java.sql.DriverManager;
            import java.sql.SQLException;

            public class Bugs {
                public boolean same(final String a, final String b) {
                    return a == b;
                }

                public Connection connect(final String url) throws SQLException {
                    return DriverManager.getConnection(url, "admin", "hunter2");
                }
            }
            """,
        )
        project.buildAndFail("spotbugsMain")
        val report = project.file("build/reports/spotbugs/main.html").readText()
        assertAll(
            listOf("ES_COMPARING_PARAMETER_STRING_WITH_EQ", "HARD_CODE_PASSWORD")
                .map { bug -> Executable { assertThat(report).contains(bug) } },
        )
    }
}

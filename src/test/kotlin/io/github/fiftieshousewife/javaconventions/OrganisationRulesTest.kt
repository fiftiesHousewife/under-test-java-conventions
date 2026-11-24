package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class OrganisationRulesTest {

    private val longMethod = "package com.example;\n\npublic class Long {\n    public int sum() {\n        int total = 0;\n" +
        (1..21).joinToString("\n") { "        total += $it;" } + "\n        return total;\n    }\n}\n"

    @Test
    fun `the organisation's Checkstyle file is used, even for a task created before underTest is set`(@TempDir dir: File) {
        val result = SampleProject(dir)
            .write(
                "config/checkstyle.xml",
                """
                <?xml version="1.0"?>
                <!DOCTYPE module PUBLIC "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
                <module name="Checker"/>
                """,
            )
            .buildScript(
                """
                tasks.getByName("checkstyleMain")
                underTest {
                    codeStyle = io.github.fiftieshousewife.javaconventions.CodeStyleSource.ORGANISATION
                    checkstyleConfig = layout.projectDirectory.file("config/checkstyle.xml")
                }
                """,
            )
            .main("com/example/Long.java", longMethod)
            .build("checkstyleMain")
        assertThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

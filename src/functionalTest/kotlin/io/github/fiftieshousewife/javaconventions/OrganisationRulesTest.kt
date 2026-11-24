package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class OrganisationRulesTest {

    private val longMethod = "package com.example;\n\npublic class Long {\n    public int sum() {\n" +
        "        int total = 0;\n" +
        (1..21).joinToString("\n") { "        total += $it;" } + "\n        return total;\n    }\n}\n"

    @Test
    fun `the organisation's Checkstyle file is used, even by a task created before underTest is set`(
        @TempDir dir: File,
    ) {
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

    private fun organisation(project: SampleProject, setting: String, path: String) = project.buildScript(
        """
        underTest {
            codeStyle = io.github.fiftieshousewife.javaconventions.CodeStyleSource.ORGANISATION
            $setting = layout.projectDirectory.file("$path")
        }
        """,
    )

    @Test
    fun `the organisation's PMD rule set replaces the plugin's`(@TempDir dir: File) {
        val project = SampleProject(dir).write(
            "config/pmd.xml",
            """
            <?xml version="1.0"?>
            <ruleset name="organisation" xmlns="http://pmd.sourceforge.net/ruleset/2.0.0">
                <description>None of the plugin's rules.</description>
            </ruleset>
            """,
        ).main(
            "com/example/Adder.java",
            "package com.example;\n\npublic class Adder {\n" +
                "    public int add(final int a, final int b, final int c, final int d) {\n" +
                "        return a + b + c + d;\n    }\n}\n",
        )
        val result = organisation(project, "pmdRuleset", "config/pmd.xml").build("pmdMain")
        assertThat(result.task(":pmdMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `the organisation's forbidden-API signatures are checked`(@TempDir dir: File) {
        val project = SampleProject(dir)
            .write("config/forbidden.txt", "@defaultMessage The organisation forbids it\njava.lang.String#trim()\n")
            .main(
                "com/example/Trim.java",
                "package com.example;\n\npublic class Trim {\n    public String trim(final String text) {\n" +
                    "        return text.trim();\n    }\n}\n",
            )
        val result = organisation(project, "forbiddenApiSignatures", "config/forbidden.txt")
            .buildAndFail("forbiddenApisMain")
        assertThat(result.output).contains("java.lang.String#trim() [The organisation forbids it]")
    }

    @Test
    fun `with ORGANISATION and no file, the check says which setting is missing`(@TempDir dir: File) {
        val result = SampleProject(dir)
            .buildScript(
                "underTest { codeStyle = io.github.fiftieshousewife.javaconventions.CodeStyleSource.ORGANISATION }",
            )
            .main("com/example/Greeting.java", CleanSample.GREETING)
            .buildAndFail("checkstyleMain")
        assertThat(result.output).contains("underTest.codeStyle is ORGANISATION, so set underTest.checkstyleConfig")
    }
}

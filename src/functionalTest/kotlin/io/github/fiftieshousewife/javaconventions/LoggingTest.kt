package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class LoggingTest {

    private fun library(dir: File) = SampleProject(dir).buildScript("""apply(plugin = "java-library")""")

    @Test
    fun `an application logs through Log4j at run time`(@TempDir dir: File) {
        val result = SampleProject(dir).build("dependencies", "--configuration", "runtimeClasspath")
        assertThat(result.output).contains("org.apache.logging.log4j:log4j-core")
    }

    @Test
    fun `a library leaves its users to choose their logging`(@TempDir dir: File) {
        val result = library(dir).build("dependencies", "--configuration", "runtimeClasspath")
        assertThat(result.output).doesNotContain("log4j")
    }

    @Test
    fun `a library's own tests log through Log4j`(@TempDir dir: File) {
        val result = library(dir).build("dependencies", "--configuration", "testRuntimeClasspath")
        assertThat(result.output).contains("org.apache.logging.log4j:log4j-core")
    }
}

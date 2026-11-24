package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DependencyVersionsTest {

    private fun withOldGuava(dir: File) = SampleProject(dir)
        .buildScript("""dependencies { implementation("com.google.guava:guava:30.0-jre") }""")

    @Test
    fun `an out-of-date dependency is reported`(@TempDir dir: File) {
        val result = withOldGuava(dir).build("checkDependencyVersions")
        assertThat(result.output).contains("out-of-date dependencies; see")
    }

    @Test
    fun `an out-of-date dependency fails the build when asked`(@TempDir dir: File) {
        val result = withOldGuava(dir).buildAndFail("checkDependencyVersions", "-Pquality.failOnStaleDependencies=true")
        assertThat(result.output).contains("out-of-date dependencies; see")
    }

    @Test
    fun `in CI, check runs the dependency check`(@TempDir dir: File) {
        val result = withOldGuava(dir).withEnvironment(mapOf("CI" to "true")).build("check", "--dry-run")
        assertThat(result.output).contains(":checkDependencyVersions SKIPPED")
    }
}

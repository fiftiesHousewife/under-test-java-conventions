package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MutationTestingTest {

    @Test
    fun `pitest plants bugs in a project with no group and summarises what the tests caught`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir).buildScript("group = \"\"")).build("pitest")
        assertThat(result.output).containsPattern("Result: the tests caught \\d+ of [1-9]\\d* planted bugs")
    }
}

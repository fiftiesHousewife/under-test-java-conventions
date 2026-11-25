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

    @Test
    fun `with a minimum mutation score, pitest fails when the tests catch fewer planted bugs`(@TempDir dir: File) {
        val result = SampleProject(dir).main(
            "com/example/Halves.java",
            "package com.example;\n\npublic class Halves {\n    public int first() {\n        return 1;\n    }\n\n" +
                "    public int second() {\n        return 2;\n    }\n}\n",
        ).test(
            "com/example/HalvesTest.java",
            "package com.example;\n\nimport static org.assertj.core.api.Assertions.assertThat;\n\n" +
                "import org.junit.jupiter.api.Test;\n\nclass HalvesTest {\n    @Test\n    void firstIsOne() {\n" +
                "        assertThat(new Halves().first()).isEqualTo(1);\n    }\n}\n",
        ).buildAndFail("pitest", "-PunderTest.minMutationScore=90")
        assertThat(result.output).contains("below threshold of 90")
    }

    @Test
    fun `pitest stays out of check unless mutationTestingInCheck is set`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).build("check", "--dry-run")
        assertThat(result.output).doesNotContain(":pitest SKIPPED")
    }

    @Test
    fun `with mutationTestingInCheck, check runs pitest`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).buildScript("underTest { mutationTestingInCheck = true }")
            .build("check", "--dry-run")
        assertThat(result.output).contains(":pitest SKIPPED")
    }
}

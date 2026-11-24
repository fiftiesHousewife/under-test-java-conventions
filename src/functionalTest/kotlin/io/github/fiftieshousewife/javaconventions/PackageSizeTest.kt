package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PackageSizeTest {

    private fun withClasses(project: SampleProject, count: Int): SampleProject = project.apply {
        (1..count).forEach { main("com/example/Part$it.java", "package com.example;\n\npublic class Part$it {\n}\n") }
        main("com/example/package-info.java", "package com.example;\n")
    }

    @Test
    fun `a package of more than twenty classes fails`(@TempDir dir: File) {
        val result = withClasses(SampleProject(dir), 21).buildAndFail("checkPackageSize")
        assertThat(result.output).contains("Package com.example in main has 21 classes, more than 20")
    }

    @Test
    fun `a package of twenty classes passes`(@TempDir dir: File) {
        val result = withClasses(SampleProject(dir), 20).build("checkPackageSize")
        assertThat(result.task(":checkPackageSize")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `an organisation can change the package-size limit`(@TempDir dir: File) {
        val result = withClasses(SampleProject(dir), 21).buildScript("underTest { maxClassesPerPackage = 25 }")
            .build("checkPackageSize")
        assertThat(result.task(":checkPackageSize")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JavaPackagesTest {

    private fun sources(root: File): File = root.apply {
        listOf("com/example/A.java", "com/example/B.java", "com/example/package-info.java", "com/example/io/C.java",
            "Top.java", "com/example/notes.txt")
            .forEach { resolve(it).apply { parentFile.mkdirs() }.writeText("") }
    }

    @Test
    fun `classes are counted by package, leaving out package-info and other files`(@TempDir dir: File) {
        assertThat(JavaPackages.classCounts(listOf(sources(dir))))
            .isEqualTo(mapOf("com.example" to 2, "com.example.io" to 1, "" to 1))
    }

    @Test
    fun `PIT targets every named package`(@TempDir dir: File) {
        assertThat(JavaPackages.targets(listOf(sources(dir)))).containsExactly("com.example.*", "com.example.io.*")
    }
}

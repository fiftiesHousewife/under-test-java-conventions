package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SourcePatternsTest {

    private val lessonPatterns = listOf("**/today/**", "**/*WeakTest.java")

    @Test
    fun `paths are matched folder by folder, at any depth`() {
        val paths = listOf(
            "com/example/today/Prices.java",
            "today/Prices.java",
            "com/example/PricesWeakTest.java",
            "com/example/Prices.java",
            "com/example/todayish/Prices.java",
            "com/example/PricesWeakTestHelper.java",
        )
        assertThat(paths.filter { SourcePatterns.excluded(it, lessonPatterns) })
            .containsExactly("com/example/today/Prices.java", "today/Prices.java", "com/example/PricesWeakTest.java")
    }

    @Test
    fun `a single star and a question mark stay inside one name`() {
        val paths = listOf("com/A1.java", "com/AB1.java", "com/x/A1.java")
        assertThat(paths.filter { SourcePatterns.excluded(it, listOf("com/?1.java", "com/*B1.java")) })
            .containsExactly("com/A1.java", "com/AB1.java")
    }

    @Test
    fun `a pattern ending in a slash matches everything under that folder`() {
        assertThat(SourcePatterns.excluded("com/legacy/deep/Old.java", listOf("com/legacy/"))).isTrue()
    }

    @Test
    fun `a source file's pattern also matches its compiled class and nested classes`() {
        assertThat(SourcePatterns.forClasses(lessonPatterns))
            .containsExactly("**/today/**", "**/*WeakTest.class", "**/*WeakTest\$*.class")
    }

    @Test
    fun `files are listed by extension, leaving out the matched ones`(@TempDir dir: File) {
        listOf("com/today/A.java", "com/B.java", "com/C.txt", "com/DWeakTest.java")
            .forEach { dir.resolve(it).apply { parentFile.mkdirs() }.writeText("") }
        assertThat(SourcePatterns.files(listOf(dir), "java", lessonPatterns).map { it.relativeTo(dir).path })
            .containsExactly("com/B.java")
    }
}

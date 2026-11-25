package io.github.fiftieshousewife.javaconventions

import java.io.File

/** The Java packages under source folders, read from where the files are. */
object JavaPackages {

    private val NOT_CLASSES = setOf("package-info.java", "module-info.java")

    /**
     * Each package's name with the number of classes in it ("" is the default package), leaving out the files the
     * [SourcePatterns] given match.
     */
    fun classCounts(roots: Collection<File>, excludes: Collection<String> = emptyList()): Map<String, Int> =
        roots.filter(File::isDirectory).flatMap { root ->
            SourcePatterns.files(listOf(root), "java", excludes).filter { it.name !in NOT_CLASSES }
                .map { packageOf(root, it) }
        }.groupingBy { it }.eachCount()

    /** PIT's target classes: every named package with classes in it, as "com.example.*". */
    fun targets(roots: Collection<File>): List<String> =
        classCounts(roots).keys.filter { it.isNotEmpty() }.sorted().map { "$it.*" }

    private fun packageOf(root: File, file: File): String =
        file.parentFile.relativeTo(root).invariantSeparatorsPath.replace('/', '.')
}

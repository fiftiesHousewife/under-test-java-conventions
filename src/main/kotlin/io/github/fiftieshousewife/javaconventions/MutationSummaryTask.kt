package io.github.fiftieshousewife.javaconventions

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Prints [MutationSummary] after PIT runs, in place of PIT's long console output. */
@DisableCachingByDefault(because = "It only prints a summary of the last PIT report")
abstract class MutationSummaryTask : DefaultTask() {

    /** PIT's report folder, holding mutations.xml and index.html. */
    @get:Internal
    abstract val reportDir: DirectoryProperty

    /** The main source folders, to show the line of code each planted bug changed. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirs: ConfigurableFileCollection

    @TaskAction
    fun summarise() {
        val dir = reportDir.get().asFile
        val xml = dir.resolve("mutations.xml")
        if (!xml.isFile) {
            logger.quiet("No PIT report at $xml")
            return
        }
        val index = dir.resolve("index.html").takeIf { it.isFile }?.readText()
        MutationSummary(::sourceLine).lines(xml.readText(), index).forEach { logger.quiet(it) }
    }

    private fun sourceLine(fileName: String, line: Int): String =
        sourceDirs.asFileTree.matching { include("**/$fileName") }.files.firstOrNull()
            ?.readLines()?.getOrNull(line - 1)?.trim().orEmpty()
}

package io.github.fiftieshousewife.javaconventions

import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.work.DisableCachingByDefault
import java.io.File

/**
 * Runs SpotBugs from its command line on one source set's classes, at maximum effort and reporting from low
 * confidence, with its plugins (FindSecBugs), and fails if it finds anything. Writes an XML and an HTML report.
 */
@DisableCachingByDefault(because = "It is a check whose result is its reports, and runs quickly enough again")
abstract class SpotBugsCheck : JavaExec() {

    /** The compiled classes to check. With none, the task is skipped. */
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classesDirs: ConfigurableFileCollection

    /** The classes they use, which SpotBugs reads but doesn't report on. */
    @get:Classpath
    abstract val auxClasspath: ConfigurableFileCollection

    /** SpotBugs plugin jars, such as FindSecBugs. */
    @get:Classpath
    abstract val pluginJars: ConfigurableFileCollection

    /** The sources, so the HTML report can show the code each finding is in. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirs: ConfigurableFileCollection

    /** A SpotBugs filter file of findings to leave out, kept in the repo applying the plugin. */
    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val excludeFilter: RegularFileProperty

    /** Classes to leave out, as patterns of their source files' paths ([SourcePatterns]). */
    @get:Input
    abstract val excludes: ListProperty<String>

    @get:OutputFile
    abstract val xmlReport: RegularFileProperty

    @get:OutputFile
    abstract val htmlReport: RegularFileProperty

    override fun exec() {
        args = arguments()
        super.exec()
        val found = BUG.findAll(xmlReport.get().asFile.readText()).count()
        if (found > 0) {
            throw GradleException(
                "SpotBugs found $found bug patterns or security bugs; see ${htmlReport.get().asFile.toURI()}",
            )
        }
    }

    private fun arguments(): List<String> {
        val aux = temporaryDir.resolve("auxclasspath.txt")
        aux.writeText(auxClasspath.files.joinToString("\n"))
        val classes = temporaryDir.resolve("classes.txt")
        classes.writeText(
            SourcePatterns.files(classesDirs.files, "class", SourcePatterns.forClasses(excludes.get()))
                .joinToString("\n"),
        )
        return listOf(
            "-effort:max", "-low", "-noClassOk", "-quiet",
            "-pluginList", pluginJars.files.joinToString(File.pathSeparator),
            "-auxclasspathFromFile", aux.path,
            "-analyzeFromFile", classes.path,
            "-sourcepath", sourceDirs.files.filter(File::isDirectory).joinToString(File.pathSeparator),
            "-xml:withMessages=${xmlReport.get().asFile.path}",
            "-html=${htmlReport.get().asFile.path}",
        ) + (excludeFilter.orNull?.let { listOf("-exclude", it.asFile.path) } ?: emptyList())
    }

    private companion object {
        val BUG = Regex("<BugInstance ")
    }
}

package io.github.fiftieshousewife.javaconventions

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Ends a test task in one line, in place of Gradle's long message with the report's full path: "All 7 tests passed.",
 * or a failure, "3 of 7 tests failed. Report: build/reports/tests/test/index.html".
 */
@DisableCachingByDefault(because = "It only reads the last test results")
abstract class TestSummaryTask : DefaultTask() {

    /** The test task's JUnit XML results. */
    @get:Internal
    abstract val resultsDir: DirectoryProperty

    /** The test task's HTML report, as a path from the root project's folder. */
    @get:Input
    abstract val report: Property<String>

    /** With `true`, a test that didn't run fails the build too. */
    @get:Input
    abstract val skipsFail: Property<Boolean>

    /** What to do so that every test runs, said when [skipsFail] fails the build. */
    @get:Input
    abstract val whenSkipsFail: Property<String>

    @TaskAction
    fun summarise() {
        val counts = TestCounts.of(resultsDir.get().asFile)
        when {
            counts.total == 0 -> return
            counts.failed > 0 -> throw GradleException(counts.failed(report.get()))
            counts.skipped > 0 && skipsFail.get() ->
                throw GradleException("${counts.skipped} of ${counts.total} tests didn't run: ${whenSkipsFail.get()}")
            else -> logger.quiet(counts.passed())
        }
    }
}

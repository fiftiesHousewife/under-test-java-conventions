package io.github.fiftieshousewife.javaconventions

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** How many tests a test task ran, from its JUnit XML results, and what to say about them in one line. */
data class TestCounts(val total: Int, val skipped: Int, val failed: Int) {

    /** The line for a run with no failure. */
    fun passed(): String {
        val ran = total - skipped
        return when {
            skipped == 0 && total == 1 -> "1 test passed."
            skipped == 0 -> "All $total tests passed."
            ran == 0 -> "No tests ran; $skipped skipped."
            else -> "$ran of $total tests passed; $skipped didn't run."
        }
    }

    /** The line for a run with failures, naming the report by a path that shows no one's home folder. */
    fun failed(report: String): String = "$failed of $total tests failed. Report: $report"

    companion object {

        /** The counts summed over the XML result files in the folder; none when it holds none. */
        fun of(resultsDir: File): TestCounts = resultsDir.walkTopDown()
            .filter { it.isFile && it.extension == "xml" }
            .map(::ofSuite)
            .fold(TestCounts(0, 0, 0)) { sum, suite ->
                TestCounts(sum.total + suite.total, sum.skipped + suite.skipped, sum.failed + suite.failed)
            }

        private fun ofSuite(file: File): TestCounts {
            val factory = DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            val suite: Element = factory.newDocumentBuilder().parse(file).documentElement
            fun count(name: String) = suite.getAttribute(name).toIntOrNull() ?: 0
            return TestCounts(count("tests"), count("skipped"), count("failures") + count("errors"))
        }
    }
}

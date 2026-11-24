package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class MutationSummaryTest {

    private val test = "com.example.GreetingTest.[engine:junit-jupiter]/[class:com.example.GreetingTest]" +
        "/[method:greets()]"

    private fun mutation(status: String, line: Int, description: String, tests: String) =
        "<mutation detected='${status == "KILLED"}' status='$status' numberOfTestsRun='1'>" +
            "<sourceFile>Greeting.java</sourceFile><lineNumber>$line</lineNumber>" +
            "<killingTests>${if (status == "KILLED") tests else ""}</killingTests>" +
            "<succeedingTests>${if (status == "KILLED") "" else tests}</succeedingTests>" +
            "<description>$description</description></mutation>"

    private val report = "<mutations>" +
        mutation("KILLED", 5, "Replaced integer addition with subtraction", test) +
        mutation("SURVIVED", 9, "negated conditional", test) +
        mutation("NO_COVERAGE", 12, "changed conditional boundary", "") +
        "</mutations>"

    private val index = "<div class=\"coverage_legend\">8/10</div>"

    private val source = mapOf(5 to "return a + b;", 9 to "if (a > b) {", 12 to "while (a < b) {")

    private val summary = MutationSummary { _, line -> source[line].orEmpty() }.lines(report, index).joinToString("\n")

    @Test
    fun `the summary counts, places and rewords each planted bug`() = assertAll(
        listOf(
            "Line coverage: 8 of 10 lines run by the tests (80%)",
            "Planted bugs:  3, caught 1, missed 2",
            "return a + b;",
            "+ became -",
            "failed: greets",
            "if (a > b) {",
            "condition reversed",
            "passed: greets",
            "boundary moved by one",
            "no test ran this line",
            "Result: the tests caught 1 of 3 planted bugs (33%).",
        ).map { line -> Executable { assertThat(summary).contains(line) } },
    )

    @Test
    fun `with no index the summary leaves out line coverage`() {
        val lines = MutationSummary { _, _ -> "" }.lines(report, null)
        assertThat(lines).noneMatch { it.startsWith("Line coverage") }
    }

    @Test
    fun `test names that don't fit are counted instead`() {
        val names = MutationSummary.names((1..10).map { "aRatherLongTestName$it" })
        assertThat(names).isEqualTo("aRatherLongTestName1 and 9 more")
    }

    @Test
    fun `a description with no plain wording is kept as PIT wrote it`() {
        assertThat(MutationSummary.plain("removed call to foo")).isEqualTo("removed call to foo")
    }
}

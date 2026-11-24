package io.github.fiftieshousewife.javaconventions

/**
 * PIT's report in plain English: how many lines the tests ran, how many planted bugs (mutations) the tests caught, and
 * each one next to the line of code it changed. [sourceLine] gives a source file's line, by file name and number.
 */
class MutationSummary(private val sourceLine: (String, Int) -> String) {

    private data class Mutation(
        val status: String,
        val where: Pair<String, Int>,
        val description: String,
        val tests: List<String>,
    )

    /** The summary's lines, from PIT's mutations.xml and, when there is one, its index.html. */
    fun lines(mutationsXml: String, indexHtml: String?): List<String> {
        val mutations = BLOCK.findAll(mutationsXml).map { parse(it.value) }.toList()
        val caught = mutations.filter { it.status == "KILLED" || it.status == "TIMED_OUT" }
        val missed = mutations - caught.toSet()
        val score = if (mutations.isEmpty()) 0 else caught.size * PERCENT / mutations.size
        return listOf("") +
            listOfNotNull(coverage(indexHtml.orEmpty())?.let { "Line coverage: $it" }) +
            "Planted bugs:  ${mutations.size}, caught ${caught.size}, missed ${missed.size}" +
            report("Caught - a test failed, as it should:", caught, "failed") +
            report("Missed - every test that ran still passed:", missed, "passed") +
            listOf("", "Result: the tests caught ${caught.size} of ${mutations.size} planted bugs ($score%).")
    }

    private fun report(heading: String, mutations: List<Mutation>, verb: String): List<String> =
        if (mutations.isEmpty()) {
            emptyList()
        } else {
            listOf("", heading) + mutations.sortedBy { it.where.second }.map { line(it, verb) }
        }

    private fun line(mutation: Mutation, verb: String): String {
        val (file, number) = mutation.where
        val code = sourceLine(file, number).take(CODE_WIDTH).padEnd(CODE_WIDTH)
        val description = mutation.description.padEnd(DESCRIPTION_WIDTH)
        val lineNumber = number.toString().padEnd(LINE_NUMBER_WIDTH)
        return "  line $lineNumber $code $description $verb: ${names(mutation.tests)}"
    }

    private fun parse(block: String): Mutation {
        fun field(name: String) = Regex("<$name>([^<]*)</$name>").find(block)?.groupValues?.get(1).orEmpty()
        val status = Regex("status='([A-Z_]+)'").find(block)?.groupValues?.get(1).orEmpty()
        val tests = when (status) {
            "KILLED" -> field("killingTests").ifEmpty { field("killingTest") }
            else -> field("succeedingTests")
        }
        return Mutation(
            status = status,
            where = field("sourceFile") to field("lineNumber").toInt(),
            description = plain(field("description")),
            tests = tests.split("|").mapNotNull { TEST_METHOD.find(it)?.groupValues?.get(1) }.distinct(),
        )
    }

    private fun coverage(index: String): String? = COVERAGE.find(index)?.destructured?.let { (run, total) ->
        "$run of $total lines run by the tests (${run.toInt() * PERCENT / total.toInt()}%)"
    }

    internal companion object {
        const val CODE_WIDTH = 40
        const val LINE_NUMBER_WIDTH = 3
        const val PERCENT = 100
        const val DESCRIPTION_WIDTH = 22
        const val NAMES_WIDTH = 38
        val BLOCK = Regex("<mutation .*?</mutation>", RegexOption.DOT_MATCHES_ALL)

        /** A test's method name, without its class or package: "[method:smallOrder()]" gives "smallOrder". */
        val TEST_METHOD = Regex("\\[method:([^(\\]]+)")
        val COVERAGE = Regex("coverage_legend\">(\\d+)/(\\d+)<")
        val SYMBOLS = mapOf("multiplication" to "*", "division" to "/", "subtraction" to "-", "addition" to "+")

        /** PIT's descriptions, in plain words: the first pattern matching the whole description rewords it. */
        val REWORDINGS = listOf(
            reword("Replaced integer (\\w+) with (\\w+)") {
                "${SYMBOLS[it.groupValues[1]]} became ${SYMBOLS[it.groupValues[2]]}"
            },
            reword("Changed increment from (\\d+) to -(\\d+)") {
                "+= ${it.groupValues[1]} became -= ${it.groupValues[2]}"
            },
            reword("changed conditional boundary") { "boundary moved by one" },
            reword("replaced int return with 0.*") { "result replaced with 0" },
            reword("negated conditional.*") { "condition reversed" },
        )

        fun reword(pattern: String, words: (MatchResult) -> String) = Regex(pattern) to words

        fun plain(description: String): String = REWORDINGS
            .firstNotNullOfOrNull { (pattern, words) -> pattern.matchEntire(description)?.let(words) } ?: description

        /** As many test names as fit in [NAMES_WIDTH] characters, then how many more, so each bug fits on one line. */
        fun names(tests: List<String>): String {
            val shown = tests.runningFold(emptyList<String>()) { acc, test -> acc + test }
                .drop(1).takeWhile { it.size == 1 || it.joinToString(", ").length <= NAMES_WIDTH }.lastOrNull()
                ?: return "no test ran this line"
            val more = tests.size - shown.size
            return shown.joinToString(", ") + if (more > 0) " and $more more" else ""
        }
    }
}

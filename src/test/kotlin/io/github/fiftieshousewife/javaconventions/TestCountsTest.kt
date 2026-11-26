package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TestCountsTest {

    private fun suite(tests: Int, skipped: Int, failures: Int, errors: Int) =
        """<?xml version="1.0" encoding="UTF-8"?>
        <testsuite name="com.example.ATest" tests="$tests" skipped="$skipped" failures="$failures" errors="$errors">
        </testsuite>"""

    @Test
    fun `the counts are summed over every suite, failures and errors both counting as failed`(@TempDir dir: File) {
        dir.resolve("TEST-A.xml").writeText(suite(tests = 4, skipped = 1, failures = 1, errors = 0))
        dir.resolve("TEST-B.xml").writeText(suite(tests = 3, skipped = 0, failures = 0, errors = 2))
        dir.resolve("binary").apply { mkdirs() }.resolve("results.bin").writeText("not XML")
        assertThat(TestCounts.of(dir)).isEqualTo(TestCounts(total = 7, skipped = 1, failed = 3))
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        textBlock = """
        1 | 0 | 1 test passed.
        7 | 0 | All 7 tests passed.
        2 | 2 | No tests ran; 2 skipped.
        7 | 2 | 5 of 7 tests passed; 2 didn't run.""",
    )
    fun `a run with no failure says how many passed and how many didn't run`(total: Int, skipped: Int, line: String) {
        assertThat(TestCounts(total, skipped, 0).passed()).isEqualTo(line)
    }

    @Test
    fun `a run with failures names the report`() {
        assertThat(TestCounts(7, 0, 3).failed("build/reports/tests/test/index.html"))
            .isEqualTo("3 of 7 tests failed. Report: build/reports/tests/test/index.html")
    }
}

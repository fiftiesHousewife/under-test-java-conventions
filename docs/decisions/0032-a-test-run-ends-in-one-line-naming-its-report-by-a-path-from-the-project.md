---
status: accepted
decision-makers: Maintainers
group: Build
---

# A test run ends in one line, naming its report by a path from the project

## Context and Problem Statement

When a test fails, Gradle ends the build with "There were failing tests. See the report at:" and the report's full `file://` URL. That URL holds the folder the project is in, which is usually under a person's home folder, so their user name shows in CI logs, shared terminal output and screen recordings. A passing run says nothing about how many tests ran.

## Considered Options

* **Each test task (`test`, `integrationTest`) ignores its failures and is followed by a summary task (`testSummary`, `integrationTestSummary`) that reads its JUnit XML results, says the result in one line, and fails the build when a test failed**
* **Leave Gradle's message: it names the report, but by its full path**
* **Turn off Gradle's HTML and JUnit XML reports, so its message names no path, and build the HTML report in a task of its own: CI tools and IDEs read the JUnit XML results**

## Decision Outcome

A run with no failure ends with "All 7 tests passed.", "1 test passed.", "5 of 7 tests passed; 2 didn't run." or "No tests ran; 2 skipped.". A run with failures fails the build with "3 of 7 tests failed. Report: build/reports/tests/test/index.html", the path from the root project's folder. Failures and errors both count as failed. Gradle still logs a warning naming the report by its full path; a quiet build (`--quiet`), as a recording uses, leaves that out, so only the one line shows. A test task the repo adds itself keeps Gradle's own behaviour.

### Consequences

* `TestSummaryTask` and `TestCounts` do it; TestRunsTest checks a passing run's line, and that a failing quiet build fails naming the report with no path from outside the project.
* The test task itself succeeds when tests fail, and its summary fails the build: a repo looking for a failed `test` task looks for a failed `testSummary` instead. The test results are still written, so tools reading them see the failures.

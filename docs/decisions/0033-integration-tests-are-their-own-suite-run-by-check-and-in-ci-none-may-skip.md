---
status: accepted
decision-makers: Maintainers
group: Coverage and evidence
---

# Integration tests are their own suite, run by check, and in CI none may skip

## Context and Problem Statement

Tests that start containers, or need anything else outside the JVM, are slower than unit tests and need a machine that can run them. Mixed into the unit tests, they slow every run; set up by each repo, each wires its own suite and its own way into `check`. Such tests usually skip themselves where what they need is missing, such as a laptop without a container runtime. In CI a skip is silent, so a CI machine that lost its container runtime passes while running none of them.

## Considered Options

* **An `integrationTest` suite in `src/integrationTest`, with the unit tests' libraries, run by `check` after the unit tests; in CI (the `CI` environment variable set) an integration test that didn't run fails the build, unless `underTest.integrationTestsMaySkip` is `true`**
* **Each repo adds its own suite: every repo wires it differently, and CI skips stay silent**
* **Integration tests in the unit tests' suite, tagged: every unit-test run starts them**

## Decision Outcome

Every project gets the `integrationTest` suite, which is empty until the repo adds tests to `src/integrationTest/java`. It sees the main code, and the libraries, Lombok and logging the unit tests get. `check` runs it, and it ends in one line, like the unit tests (record 0032). In CI, a skipped integration test fails the build with what to do; `underTest.integrationTestsMaySkip = true` lets it pass, for a CI machine that can't give the tests what they need, such as a runner whose containers aren't the kind the tests start. Off CI, a skip passes, so a laptop without a container runtime still builds. Line coverage still comes from the unit tests only (record 0010).

### Consequences

* The plugin registers the suite with Gradle's JVM test suites, and `integrationTestSummary` reads its results. Checkstyle and PMD check its code as they do the unit tests', and the copy-paste and package-size checks include it.
* TestRunsTest checks that `check` runs the suite, that in CI a skipped integration test fails the build, and that with `integrationTestsMaySkip` it doesn't.
* A repo with its own `integrationTest` suite or task removes it: the plugin's has that name.

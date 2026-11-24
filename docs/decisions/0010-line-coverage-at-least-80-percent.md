---
status: accepted
decision-makers: Maintainers
group: Coverage and evidence
---

# Line coverage is at least 80%, and code with no tests fails

## Context and Problem Statement

Code no test runs can change without any test failing. JaCoCo skips its check when no test ran, so code with no tests at all would pass it.

## Considered Options

* **At least 80% of lines, and code with no tests fails**
* **At least 80%, as JaCoCo checks it alone**
* **No floor**

## Decision Outcome

Line coverage of at least 80%; a repo may raise it (`underTest.minLineCoverage`). A project with main code and no test that ran fails too, unless the floor is set to 0.

### Consequences

* `jacocoTestCoverageVerification` and `requireTests` run in `check`; the coverage report is written after the tests.

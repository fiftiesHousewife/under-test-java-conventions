---
status: accepted
decision-makers: Maintainers
group: Coverage and evidence
---

# A repo can run mutation testing in check

## Context and Problem Statement

Mutation testing is available, not required, and outside `check` (record 0015); a repo can require a minimum mutation score (record 0025). A repo requiring one wants it checked on every build, as coverage is, and had to add `tasks.check { dependsOn("pitest") }` to its own build file.

## Considered Options

* **A setting, `underTest.mutationTestingInCheck`, `false` by default**
* **Run pitest in check whenever a minimum mutation score is set**
* **Leave it to each repo's build file**

## Decision Outcome

`underTest.mutationTestingInCheck`, in a project's `underTest { }` block or as a Gradle property. With `true`, `check` runs `pitest`, and with it `mutationSummary`. It is separate from the minimum score, so a repo can see its mutation results on every build without a threshold, or keep a threshold for a CI job of its own. This builds on records 0015 and 0025, which otherwise stand.

### Consequences

* With the setting, `check` takes as long as the tests take for every planted bug: a repo turns it on where its tests are quick enough.
* MutationTestingTest checks that `check` leaves `pitest` out by default and runs it with the setting.

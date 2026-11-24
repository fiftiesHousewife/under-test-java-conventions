---
status: accepted
decision-makers: Maintainers
group: Coverage and evidence
---

# A repo can require a minimum mutation score

## Context and Problem Statement

Mutation testing is available, not required (record 0015): `pitest` plants bugs and says which the tests caught. A
repo whose code must be trusted more (an agent running inside other people's programs) requires its tests to catch a
share of the planted bugs, and with PIT's Gradle plugin gone, had no way to say so.

## Considered Options

* **A setting, `underTest.minMutationScore`, unset by default: with it, `pitest` fails below that percentage**
* **No setting: a repo needing one keeps its own PIT set-up**

## Decision Outcome

`underTest.minMutationScore`, a percentage, in a project's `underTest { }` block or as a Gradle property. With it,
`pitest` fails when the tests catch fewer of the planted bugs (PIT's own mutation threshold). Unset, nothing changes:
mutation testing stays available, not required, and outside `check`. This builds on record 0015, which otherwise
stands.

### Consequences

* A repo that needs a score runs `pitest` where it needs it, in CI for instance.
* MutationTestingTest checks a project whose tests miss planted bugs fails at 90%.

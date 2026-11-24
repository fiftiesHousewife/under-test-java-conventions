---
status: accepted
decision-makers: Maintainers
group: Style
---

# A for-each loop is advice, never failing the build

## Context and Problem Statement

A stream reads well for transforming a collection, and worse for a loop with side effects, an early exit or a checked exception.

## Considered Options

* **Report it, and the build still passes**
* **Fail the build, with suppressions**
* **No rule**

## Decision Outcome

Every for-each loop is reported as advice, suggesting a stream or `forEach`, and never fails the build.

### Consequences

* `pmdAdvice`, part of `check`, writes the advice to its own report.

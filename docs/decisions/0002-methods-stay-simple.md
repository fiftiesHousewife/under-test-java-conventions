---
status: accepted
decision-makers: Maintainers
group: Size and simplicity
---

# Methods stay simple: cyclomatic complexity 5, cognitive complexity 6, NPath under 50, boolean expressions of 3 operators

## Context and Problem Statement

A method with many paths through it needs many tests to cover, and is easy to misread.

## Considered Options

* **Cyclomatic complexity 5 or less, cognitive 6 or less, NPath under 50, boolean expressions of 3 operators or fewer**
* **Larger limits**
* **No limits**

## Decision Outcome

Cyclomatic complexity 5 or less, cognitive complexity 6 or less, NPath complexity under 50, and boolean expressions of 3 operators or fewer.

### Consequences

* Checkstyle and PMD both check, with the same limits: both report from cyclomatic complexity 6 and NPath 50; PMD reports cognitive complexity from 7; Checkstyle the boolean expressions.

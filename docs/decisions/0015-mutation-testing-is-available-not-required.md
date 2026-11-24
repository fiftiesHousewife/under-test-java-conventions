---
status: accepted
decision-makers: Maintainers
group: Coverage and evidence
---

# Mutation testing is available, not required

## Context and Problem Statement

Coverage says the tests run the code, not that they would notice it change. Mutation testing plants bugs and counts the ones the tests catch, and takes far longer than a build.

## Considered Options

* **Available on demand, with a plain-English summary**
* **In every build**
* **Not available**

## Decision Outcome

`pitest` runs PIT on the packages the main code is in, and `mutationSummary` then says, in plain English, which planted bugs the tests caught and missed, and where. It is not part of `check`.

### Consequences

* PIT's console output is reduced to warnings and errors; the full report stays in `build/reports/pitest`.

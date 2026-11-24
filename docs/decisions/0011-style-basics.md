---
status: accepted
decision-makers: Maintainers
group: Style
---

# Local variables final, braces always, no star imports

## Context and Problem Statement

A reassigned local, a missing brace or a star import each make code easier to misread.

## Considered Options

* **Enforce them**
* **Leave them to review**

## Decision Outcome

Local variables are `final` where not reassigned; every block has braces; no star imports, unused or redundant imports, or empty blocks; braces placed consistently; boolean expressions and returns simplified.

### Consequences

* Checkstyle fails each.

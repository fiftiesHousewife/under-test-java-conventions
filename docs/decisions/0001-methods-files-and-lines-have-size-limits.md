---
status: accepted
decision-makers: Maintainers
group: Size and simplicity
---

# Methods, files and lines have size limits

## Context and Problem Statement

Long methods and files are hard to read and to test, and long lines are hard to read side by side or in a review.

## Considered Options

* **Methods of 20 lines or fewer (blank lines not counted), files of 150 lines or fewer, lines of 120 characters or fewer**
* **Larger limits**
* **No limits**

## Decision Outcome

Methods of 20 lines or fewer, not counting blank lines; files of 150 lines or fewer; lines of 120 characters or fewer.

### Consequences

* Checkstyle's MethodLength, FileLength and LineLength fail the build.
* `equals` and `hashCode` are exempt from the method length, as generated-style code.

---
status: accepted
decision-makers: Maintainers
group: Size and simplicity
---

# At most three parameters, and at most three components in a record

## Context and Problem Statement

A long parameter list is easy to call with arguments in the wrong order, and hides a missing type.

## Considered Options

* **At most three, with a builder beyond that**
* **At most five**
* **No limit**

## Decision Outcome

At most three parameters for a method or constructor, and three components for a record. Beyond that, a Lombok `@Builder`.

### Consequences

* Checkstyle's ParameterNumber and RecordComponentNumber, and PMD's ExcessiveParameterList, fail the build.
* `equals` is exempt.

---
status: accepted
decision-makers: Maintainers
group: Duplication and structure
---

# No package holds more than 20 classes

## Context and Problem Statement

A package of many classes is usually several packages, by what the classes do, held in one.

## Considered Options

* **At most 20 classes in a package**
* **No limit**

## Decision Outcome

At most 20 classes in a package, in each source set. `package-info.java` and `module-info.java` aren't counted.

### Consequences

* `checkPackageSize`, part of `check`, fails a larger package.

---
status: accepted
decision-makers: Maintainers
group: Build
---

# Compiling for an older Java is not a warning that fails the build

## Context and Problem Statement

Code compiles with every warning an error (record 0013). A repo may build its classes for an older Java than the
toolchain's, with `options.release` (an agent that must load on Java 8, a library for Java 17). javac 25 warns that
source and target 8 are obsolete: a warning about its own flags, not the code, and one the repo chose. With every
warning an error, such a repo couldn't build at all.

## Considered Options

* **Leave out javac's "options" warnings, about its flags; every warning about the code is still an error**
* **Keep them errors: a repo building for an older Java can't use the plugin**

## Decision Outcome

javac's "options" warnings are not errors. Every other warning, about the code, still fails the build. This narrows
record 0013, which otherwise stands.

### Consequences

* A repo can build for Java 8 or 17 with `options.release` on the Java 25 toolchain the plugin sets.
* CompilerTest compiles a sample for release 8 and for release 17.

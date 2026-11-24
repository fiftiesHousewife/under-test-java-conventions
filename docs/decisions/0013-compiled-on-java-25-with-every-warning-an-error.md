---
status: accepted
decision-makers: Maintainers
group: Build
---

# Code compiles on Java 25, with every warning an error

## Context and Problem Statement

A warning ignored once is ignored for good; code built on different JDKs behaves differently.

## Considered Options

* **Java 25 by toolchain, all warnings as errors**
* **The JDK running the build, warnings allowed**

## Decision Outcome

Java 25 by toolchain, compiled with every lint warning (`-Xlint:all`, annotation processing aside) as an error, and with parameter names kept. Archives are built reproducibly.

### Consequences

* Lombok, SLF4J with Log4j 2 behind it, JUnit and AssertJ are added at the plugin's versions.

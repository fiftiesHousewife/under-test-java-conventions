---
status: accepted
decision-makers: Maintainers
group: Build
---

# An application logs through Log4j 2; a library leaves its users to choose their logging

## Context and Problem Statement

Code logs through Lombok's `@Slf4j` on the SLF4J API, with Log4j 2 behind it at run time. A library published with Log4j as a run-time dependency forces it on every program that uses the library, and clashes with whatever logging those programs chose.

## Considered Options

* **Log4j at run time for an application; for a library (one that applies `java-library`), only for its own tests**
* **Log4j at run time for everything**
* **No Log4j: each repo adds its own**

## Decision Outcome

An application gets Log4j 2 behind SLF4J at run time. A library gets the SLF4J API only, and Log4j for its own tests, so its published dependencies name only the SLF4J API. This narrows the logging set-up in record 0013, which otherwise stands.

### Consequences

* Whether a project is a library is read once its build script has run, so `java-library` may be applied before or after the plugin.
* LoggingTest checks an application's and a library's run-time and test dependencies.

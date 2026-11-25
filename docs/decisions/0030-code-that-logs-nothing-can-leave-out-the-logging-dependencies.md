---
status: accepted
decision-makers: Maintainers
group: Build
---

# Code that logs nothing can leave out the logging dependencies

## Context and Problem Statement

Every project gets the SLF4J API as an `implementation` dependency, and Log4j 2 at run time or for its tests (record 0021). The SLF4J API therefore goes into every published POM. Some code must log nothing and publish no logging library: a Java agent loaded into other people's programs, a test-framework extension that would push SLF4J onto its users' test class path, or a Maven extension that would add a second SLF4J next to Maven's own.

## Considered Options

* **A setting, `underTest.logging`, `true` by default: with `false`, neither the SLF4J API nor Log4j is added**
* **The SLF4J API as `compileOnly`: code that logs would then fail at run time without it**
* **Add the SLF4J API only when the code uses it: the plugin can't tell before compiling**

## Decision Outcome

`underTest.logging`, in a project's `underTest { }` block or as a Gradle property. With `false`, the plugin adds neither the SLF4J API nor Log4j, so the project's published dependencies name no logging library. Unset, nothing changes. The forbidden-APIs check still refuses `System.out` and `java.util.logging`. This narrows record 0021, which otherwise stands.

### Consequences

* Code using Lombok's `@Slf4j` with logging off doesn't compile: logging off means the code logs nothing.
* LoggingTest checks that with the setting, neither SLF4J nor Log4j is on the test run-time class path.

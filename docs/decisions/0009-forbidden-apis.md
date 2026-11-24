---
status: accepted
decision-makers: Maintainers
group: Bugs and APIs
---

# No unsafe, deprecated, internal or non-portable JDK APIs, no System.out, and time from an injected Clock

## Context and Problem Statement

Some JDK APIs are unsafe, deprecated, internal or behave differently on another machine; `System.out` bypasses logging; reading the clock directly makes code untestable in time.

## Considered Options

* **Forbid them, with a message saying what to use instead**
* **Review only**

## Decision Outcome

The forbidden-APIs check fails the build on: unsafe, deprecated, internal and non-portable JDK APIs; `System.out` and `System.err`; reflection tricks; `java.util.logging` (log through Lombok `@Slf4j`); and `now()` and `currentTimeMillis()` (inject a `java.time.Clock`).

### Consequences

* The JDK sets are forbidden-apis' bundled signatures; the logging and clock signatures ship in the plugin.

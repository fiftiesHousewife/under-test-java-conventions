---
status: accepted
decision-makers: Maintainers
group: Build
---

# PIT runs from its command line, and only SpotBugs' and the versions report's Gradle plugins come from the Gradle Plugin Portal

## Context and Problem Statement

Three Gradle plugins the plugin applied were on the Gradle Plugin Portal only: SpotBugs', the dependency-versions report's and PIT's (after 1.15.0). PIT's Gradle plugin, at its newest release, calls a Gradle method Gradle 10 removes, so every build applying the plugin printed a deprecation warning. The copy-paste detector already runs from its own command line.

## Considered Options

* **Run PIT from its command line, as the copy-paste detector runs**
* **Keep PIT's Gradle plugin and wait for a fixed release**
* **Drop mutation testing**

## Decision Outcome

PIT runs from its command line: `pitest` is a task of the plugin's own, with PIT and its JUnit 5 plugin at the versions in the jar, then `mutationSummary`. SpotBugs' and the versions report's Gradle plugins still come from the Gradle Plugin Portal, so a repo applying the plugin still lists `gradlePluginPortal()` and `mavenCentral()`. This replaces record 0018.

### Consequences

* No deprecation warning from PIT's Gradle plugin; one fewer dependency from the Portal. SpotBugs' Gradle plugin, at
  its newest (6.5.13), makes the same deprecated call when the plugin adds FindSecBugs, so a build still sees one
  warning; Gradle removes the method in Gradle 11. Running SpotBugs from its command line too would end it.
* A build that configured PIT through its Gradle plugin's `pitest { }` block can't: the plugin sets what PIT needs.
* MutationTestingTest runs PIT on a sample and checks its summary.

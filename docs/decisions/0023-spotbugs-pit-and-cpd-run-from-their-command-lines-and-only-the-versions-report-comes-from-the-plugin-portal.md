---
status: accepted
decision-makers: Maintainers
group: Build
---

# SpotBugs, PIT and CPD run from their command lines, and only the versions report comes from the Gradle Plugin Portal

## Context and Problem Statement

PIT and the copy-paste detector run from their own command lines (record 0022). SpotBugs ran through its Gradle plugin,
which is on the Gradle Plugin Portal only and, at its newest (6.5.13), calls a Gradle method Gradle 11 removes, so
every build applying the plugin printed a deprecation warning. A repo's own SpotBugs exclusions went on that Gradle
plugin's tasks (`excludeFilter`).

## Considered Options

* **Run SpotBugs from its command line too, with a setting for a repo's exclusions**
* **Keep SpotBugs' Gradle plugin and wait for a fixed release**

## Decision Outcome

SpotBugs runs from its command line: a task of the plugin's own for each source set (`spotbugsMain`, `spotbugsTest`,
and one for each source set added later), at maximum effort, reporting from low confidence, with FindSecBugs, writing
an XML and an HTML report and failing the build on any finding. A repo's exclusions are a SpotBugs filter file it keeps,
named by `underTest.spotbugsExcludeFilter`. The versions report is the only Gradle plugin left that comes from the
Gradle Plugin Portal. This replaces record 0022.

### Consequences

* No deprecation warning from SpotBugs' Gradle plugin; a repo applying the plugin still lists `gradlePluginPortal()`
  and `mavenCentral()`, for the versions report.
* A repo that set `excludeFilter` on SpotBugs' Gradle tasks sets `underTest.spotbugsExcludeFilter` instead.
* SpotBugsTest checks a bug pattern and a hard-coded password fail, and that the repo's exclusions leave them out.

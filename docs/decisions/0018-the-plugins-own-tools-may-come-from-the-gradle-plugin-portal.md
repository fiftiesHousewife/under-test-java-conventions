---
status: replaced by 0022
decision-makers: Maintainers
group: Build
---

# The plugin's own tools may come from the Gradle Plugin Portal

## Context and Problem Statement

The plugin is published to Maven Central. Three Gradle plugins it applies are not there: SpotBugs' Gradle plugin (`com.github.spotbugs.snom`), the dependency-versions report (`io.github.ben-manes`), and PIT's Gradle plugin after 1.15.0 (`info.solidsoft.gradle.pitest`); they are on the Gradle Plugin Portal only. The forbidden-APIs plugin is on Maven Central.

## Considered Options

* **Allow the Gradle Plugin Portal for the plugin's own dependencies**
* **Maven Central only: SpotBugs through its command line, PIT out of the plugin, the versions report replaced**
* **Maven Central only, PIT pinned to its last version there**

## Decision Outcome

Allow the Gradle Plugin Portal. The plugin itself is still published to Maven Central only; a repo applying it lists `gradlePluginPortal()` and `mavenCentral()` in `pluginManagement`, as the README shows. Organisations commonly proxy both.

### Consequences

* An organisation that proxies only Maven Central must proxy the Portal too, or the plugin doesn't resolve.
* PublishedPluginTest applies the plugin with exactly the README's repositories, so a change that breaks this fails the build.

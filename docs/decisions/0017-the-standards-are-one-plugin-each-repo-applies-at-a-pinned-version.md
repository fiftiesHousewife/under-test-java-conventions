---
status: accepted
decision-makers: Maintainers
group: Build
---

# The standards are one plugin that each repo applies at a pinned version, with its rule files and tool versions inside

## Context and Problem Statement

Repos that each keep a copy of the same Gradle conventions and rule files drift apart: one tightens its own, and the others don't follow. A convention plugin in a build's `build-logic/` is shared by that build's modules only, not between repos.

## Considered Options

* **One plugin, in its own repo, that each repo applies at a pinned version**
* **Keep the copies, synchronised by a script**
* **Keep the copies**

## Decision Outcome

One plugin, in this repo, published to Maven Central, signed, with its plugin marker. Each repo applies it by id at a pinned version and adds `mavenCentral()` to `pluginManagement`. The rule files and the tools' versions ship inside the jar; the plugin reads nothing of the repo applying it except an organisation's rule files, when it chooses them.

### Consequences

* A rule changes in one place, and reaches a repo when it moves to the new version.
* A repo that adopts it removes its own copies, and fixes what the rules find.
* It is for Java on current Gradle and JDKs; old repos on JDK 8 or old Gradle need something else.

---
status: accepted
decision-makers: Maintainers
group: Settings
---

# An organisation can replace the code-quality rules, but not the testing evidence

## Context and Problem Statement

An organisation may already have its own rules, or its own plugin that applies them. Coverage is evidence that the tests run the code, whoever's style rules apply.

## Considered Options

* **Rules replaceable or switched off; coverage always on, only its threshold changes**
* **Everything fixed**
* **Everything optional**

## Decision Outcome

`underTest.codeStyle` chooses whose rules the style, complexity, copy-paste, package-size, bug and forbidden-API checks use: the plugin's (`TOOLKIT`), the organisation's files (`ORGANISATION`), or none (`NONE`). Coverage is always checked; only its threshold changes. Each setting can be a Gradle property, so an init script can set it for every build.

### Consequences

* Settings are read once the build script has run, so a setting made after a task exists still counts.
* The build environment (proxy, package repository, credentials) is always the organisation's own, from its Gradle
  init script: the plugin never sets it.
* The alternatives were everything overridable, which makes the evidence mean different things in different repos, and
  everything fixed, which clashes with standards an organisation already has; forking the plugin to change a rule
  drifts from it.

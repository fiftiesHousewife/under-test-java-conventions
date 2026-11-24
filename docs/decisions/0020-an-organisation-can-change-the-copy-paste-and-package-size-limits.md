---
status: accepted
decision-makers: Maintainers
group: Settings
---

# An organisation can change the copy-paste and package-size limits

## Context and Problem Statement

An organisation can use its own Checkstyle, PMD and forbidden-API files, or switch the code-quality checks off. The copy-paste limit (50 tokens) and the package-size limit (20 classes) had no file and no setting: they could only be switched off with everything else.

## Considered Options

* **A setting for each limit, like the coverage floor**
* **Leave them fixed**

## Decision Outcome

`underTest.cpdMinimumTokens` (50) and `underTest.maxClassesPerPackage` (20), in the `underTest { }` block or as Gradle properties, so an init script can set them for every build. They apply whatever `underTest.codeStyle` is, except `NONE`, which skips both checks.

### Consequences

* The defaults ship in the plugin's jar with the tool versions.
* CopyPasteTest and PackageSizeTest change each limit and see the check pass.

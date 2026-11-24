# Agent guide

Guidance for any AI coding assistant working in this repo, and for the people directing it.

**What this repo is:** java-conventions, the Gradle plugin carrying the code-quality standards for all of
[under-test](https://github.com/fiftiesHousewife/under-test)'s Java and its tools'. It follows under-test's
plain-English rules ([GLOSSARY.md](https://github.com/fiftiesHousewife/under-test/blob/main/GLOSSARY.md)).

## Where things are
| Path | What |
|---|---|
| `src/main/kotlin/io.github.fiftieshousewife.java-conventions.gradle.kts` | The plugin: what it applies and how |
| `src/main/resources/io/github/fiftieshousewife/javaconventions/` | The rule files: Checkstyle, PMD (failing, and advice), forbidden-API signatures |
| `gradle/libs.versions.toml` | The plugin's own build, and the tool versions it applies (written into the jar as `versions.properties`) |
| `src/test/kotlin/` | Functional tests: sample projects built with Gradle TestKit |

## Rules for a change
* A rule changes only by an under-test decision; the change names it.
* Every rule has a functional test with a sample it fails, and the clean sample still passes.
* Nothing in the plugin reads the applying repo's version catalogue or quality files, except an organisation's rule
  files when it chooses them.

## Before you finish
Run `./gradlew check`. It must pass.

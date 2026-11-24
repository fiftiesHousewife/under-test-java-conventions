# Agent guide

Guidance for any AI coding assistant working in this repo, and for the people directing it.

**What this repo is:** java-conventions, a Gradle plugin carrying one set of code-quality standards for Java, which
each repo applies at a pinned version. Each standard has a decision record in [docs/decisions](docs/decisions/README.md).
Cite nothing outside this repo: a record, a comment or the README stands on what the plugin does.

## Where things are
| Path | What |
|---|---|
| `src/main/kotlin/io.github.fiftieshousewife.java-conventions.gradle.kts` | The plugin: what it applies and how |
| `src/main/resources/io/github/fiftieshousewife/javaconventions/` | The rule files: Checkstyle, PMD (failing, and advice), forbidden-API signatures |
| `gradle/libs.versions.toml` | The plugin's own build, and the tool versions it applies (written into the jar as `versions.properties`) |
| `src/test/kotlin/` | Functional tests: sample projects built with Gradle TestKit |
| `docs/decisions/` | This repo's decision records: why each rule, limit and tool is what it is |
| `.github/skills/` | Skills for working here, such as writing a decision record |

## Rules for a change
* A rule, a limit or a tool changes only by a decision record in [docs/decisions](docs/decisions/README.md)
  ([how](.github/skills/write-a-decision-record/SKILL.md)); the change names it.
* Every rule has a functional test with a sample it fails, and the clean sample still passes.
* Nothing in the plugin reads the applying repo's version catalogue or quality files, except an organisation's rule
  files when it chooses them.

## Before you finish
Run `./gradlew check`. It must pass. It runs detekt on this repo's Kotlin as well as the functional tests. Its Gradle
daemon needs 2 GiB of heap and 1 GiB of metaspace, set in `gradle.properties` (see the README's "Memory").

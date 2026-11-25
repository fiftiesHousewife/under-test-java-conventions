# Agent guide

Guidance for any AI coding assistant working in this repo, and for the people directing it.

**What this repo is:** java-conventions, a Gradle plugin carrying one set of code-quality standards for Java, which
each repo applies at a pinned version. Each standard has a decision record in [docs/decisions](docs/decisions/README.md).
Cite nothing outside this repo: a record, a comment or the README stands on what the plugin does.

## Where things are
| Path | What |
|---|---|
| `src/main/kotlin/io.github.fiftieshousewife.java-conventions.gradle.kts` | The plugin: what it applies and how |
| `src/main/kotlin/io/github/fiftieshousewife/javaconventions/` | Its classes: the `underTest` settings, the SpotBugs task, the PIT summary, package counting, the path patterns of `codeStyleExcludes` |
| `src/main/resources/io/github/fiftieshousewife/javaconventions/` | The rule files: Checkstyle, PMD (failing, and advice), forbidden-API signatures |
| `gradle/libs.versions.toml` | The plugin's own build, and the tool versions it applies (written into the jar as `versions.properties`) |
| `src/functionalTest/kotlin/` | Functional tests: sample projects built with Gradle TestKit; `StyleBreaches` and `ApiBreaches` hold a sample breaking each rule |
| `src/test/kotlin/` | Unit tests of the plugin's classes |
| `docs/decisions/` | This repo's decision records: why each rule, limit and tool is what it is |
| `docs/releases/` | Each release's notes, `<version>.md`, which the release workflow puts on its GitHub release |
| `.github/skills/` | Skills for working here, such as writing a decision record (linked into `.claude/skills/` for Claude Code) |

## Rules for a change
* A rule, a limit or a tool changes only by a decision record in [docs/decisions](docs/decisions/README.md)
  ([how](.github/skills/write-a-decision-record/SKILL.md)); the change names it.
* Every rule has a functional test with a sample it fails, and the clean sample still passes. A setting has a test
  that changes it. A sample must compile with no warning: the build treats warnings as errors.
* The README's tables of standards and settings, and the decision records, say the same as the plugin.
* Nothing in the plugin reads the applying repo's version catalogue or quality files, except an organisation's rule
  files when it chooses them.

## Building and testing
`./gradlew check` runs detekt on this repo's own Kotlin (here only: a build applying the plugin gets none of it), the
unit tests (`test`), and the functional tests (`functionalTest`), which build small sample projects with Gradle
TestKit: each rule has a sample that breaks it, and a clean sample passes. They need a JDK 25 that Gradle can find, or
`-Porg.gradle.java.installations.paths=<its home>`.

The functional tests include the plugin as published: `check` publishes it to `build/local-repo` (what would go to
Maven Central), and a sample build applies it from there by id and version, on this build's Gradle and on the oldest
supported (`oldest-gradle` in [libs.versions.toml](gradle/libs.versions.toml)).

### Memory
The build's Gradle daemon needs **2 GiB of heap and 1 GiB of metaspace**: detekt and the Kotlin compiler run inside it,
and Gradle's defaults (512 MiB heap, 384 MiB metaspace) run out. [gradle.properties](gradle.properties) sets them
(`org.gradle.jvmargs`); allow about 3 GiB free for it. The functional tests run their sample builds one at a time, in a
TestKit Gradle daemon at Gradle's defaults, reused from one build to the next: running them in parallel was slower on
a laptop, as the daemons competed for memory and couldn't be reused. A build applying the plugin needs no more memory
for it.

## Releasing
Write `docs/releases/<version>.md`: what changed since the last release, each change with its decision record. Then
push a version tag, such as `v0.1.0`. The [release workflow](.github/workflows/release.yml) runs every check, then
publishes that version to Maven Central and makes a GitHub release, with `docs/releases/<version>.md` as its notes when
there is one. It needs four repository secrets: `CENTRAL_PORTAL_USERNAME` and `CENTRAL_PORTAL_PASSWORD` (a Central
Portal user token), and `SIGNING_KEY` and `SIGNING_KEY_PASSWORD` (an ASCII-armoured PGP private key and its
passphrase). A release on Maven Central can't be changed or removed. The tag's version is published, whatever
`gradle.properties` says. Then set the README's "Using it" to the released version, and raise `version` in
`gradle.properties` to the next one.

## Before you finish
Run `./gradlew check`. It must pass.

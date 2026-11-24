# java-conventions

Part of under-test.

**One set of code-quality standards for Java, as one Gradle plugin: each repo applies a pinned version instead of
keeping its own copy of the rules.**

It applies Checkstyle, PMD and its copy-paste detector, SpotBugs with FindSecBugs, forbidden APIs and a coverage
floor, with the rule files and tool versions inside the plugin. Why each standard is what it is:
[docs/decisions](docs/decisions/README.md).

## Using it

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal() // SpotBugs', the versions report's and PIT's Gradle plugins are only here
        mavenCentral()
    }
}

// build.gradle.kts
plugins {
    id("io.github.fiftieshousewife.java-conventions") version "0.1.0"
}
```

It needs Gradle 9.1 or later, running on JDK 21 or later, and a JDK 25 for the toolchain.

`./gradlew check` then fails on any breach of the standards below, and `pmdAdvice` reports advice that never fails
the build.

## The standards

| Standard | Checked by |
|---|---|
| Methods of 20 lines or fewer; files of 150 lines or fewer; lines of 120 characters or fewer | Checkstyle |
| Cyclomatic complexity 5 or less, cognitive complexity 6 or less, NPath complexity under 50; boolean expressions of 3 operators or fewer | Checkstyle and PMD |
| No more than three parameters, and no record with more than three components | Checkstyle, PMD |
| Exactly one assertion statement per test: one `assert...`/`assertThat` chain, or one `assertAll(...)` grouping several; a test with none fails | PMD |
| No comment on a field or an enum entry; Javadoc only on public API | Checkstyle |
| No copy-pasted code (50 tokens or more) | PMD's copy-paste detector |
| No package of more than 20 classes, in any source set | The plugin (`checkPackageSize`) |
| No bug patterns or security bugs | SpotBugs with FindSecBugs |
| No unsafe, deprecated, internal or non-portable JDK APIs, no `System.out`, no reflection tricks; time from an injected `Clock` | forbidden APIs |
| Line coverage at least 80% (raise it with `underTest.minLineCoverage`); code with no tests fails too | JaCoCo |
| Final local variables, braces always, no star imports | Checkstyle |
| Advice, never failing the build: a stream or `forEach` rather than a for-each loop | PMD, in `pmdAdvice` |

It also sets Java 25 by toolchain, compiles with all warnings as errors, adds Lombok, SLF4J with Log4j 2 behind it,
JUnit and AssertJ, reports out-of-date dependencies (`dependencyUpdates`; checked in CI), and makes mutation testing
available (`pitest`, with a plain-English summary after it).

## Settings an organisation can change

| Setting | Default | |
|---|---|---|
| `underTest.codeStyle` | `TOOLKIT` | `ORGANISATION` uses the organisation's rule files below; `NONE` switches the code-quality checks off, for an organisation whose own plugin does them |
| `underTest.checkstyleConfig`, `underTest.pmdRuleset`, `underTest.forbiddenApiSignatures` | none | The organisation's files, with `ORGANISATION` |
| `underTest.minLineCoverage` | `0.80` | |

Each can be set in the `underTest { }` block or as a Gradle property, so an organisation's init script can set it
for every build. A repo's own SpotBugs exclusions stay in the repo (`excludeFilter` on its SpotBugs tasks).

## Building it

`./gradlew check` runs detekt on this repo's own Kotlin (here only: a build applying the plugin gets none of it) and the functional tests, which build small sample projects with Gradle TestKit; they need a JDK
25 that Gradle can find (or `-Porg.gradle.java.installations.paths=<its home>`). They include the plugin as
published: `check` publishes it to `build/local-repo` (what would go to Maven Central), and a sample build applies it
from there by id and version, on this build's Gradle and on the oldest supported (`oldest-gradle` in
[libs.versions.toml](gradle/libs.versions.toml)). See [AGENTS.md](AGENTS.md).

### Memory

The build's Gradle daemon needs **2 GiB of heap and 1 GiB of metaspace**: detekt and the Kotlin compiler run inside it,
and Gradle's defaults (512 MiB heap, 384 MiB metaspace) run out. [gradle.properties](gradle.properties) sets them
(`org.gradle.jvmargs`); allow about 3 GiB free for it, and more for the sample builds the functional tests start,
one at a time. This is this repo's build only: a build applying the plugin needs no more memory for it.

## Releasing

Push a version tag, such as `v0.1.0`. The [release workflow](.github/workflows/release.yml) runs every check, then
publishes that version to Maven Central and makes a GitHub release, with `docs/releases/<version>.md` as its notes when
there is one. It needs four repository secrets: `CENTRAL_PORTAL_USERNAME` and `CENTRAL_PORTAL_PASSWORD` (a Central
Portal user token), and `SIGNING_KEY` and `SIGNING_KEY_PASSWORD` (an ASCII-armoured PGP private key and its
passphrase). A release on Maven Central can't be changed or removed.

## Licence

Apache License 2.0; see [LICENSE](LICENSE).

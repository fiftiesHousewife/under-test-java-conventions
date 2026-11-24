# java-conventions

[![Build](https://github.com/fiftiesHousewife/under-test-java-conventions/actions/workflows/build.yml/badge.svg)](https://github.com/fiftiesHousewife/under-test-java-conventions/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.fiftieshousewife/java-conventions)](https://central.sonatype.com/artifact/io.github.fiftieshousewife/java-conventions)
[![Licence](https://img.shields.io/badge/licence-Apache%202.0-blue)](LICENSE)

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
        gradlePluginPortal() // the versions report's Gradle plugin is only here
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
| Exactly one assertion statement per test: one `assert...`/`assertThat` chain, one Mockito `verify(...)`, or one `assertAll(...)` grouping several; a test with none fails | PMD |
| No comment on a field or an enum entry; Javadoc only on public API | Checkstyle |
| No copy-pasted code (50 tokens or more; `underTest.cpdMinimumTokens`) | PMD's copy-paste detector (`cpdCheck`) |
| No package of more than 20 classes, in any source set (`underTest.maxClassesPerPackage`) | The plugin (`checkPackageSize`) |
| No bug patterns or security bugs | SpotBugs with FindSecBugs |
| No unsafe, deprecated, internal or non-portable JDK APIs, no `System.out`, no reflection tricks, no `java.util.logging`; time from an injected `Clock` | forbidden APIs (deprecated and internal APIs are refused by the compiler first) |
| Line coverage at least 80% (raise it with `underTest.minLineCoverage`); code with no tests fails too | JaCoCo |
| Final local variables, braces always, no star, unused or redundant imports, no empty blocks, simplified boolean expressions and returns | Checkstyle |
| Advice, never failing the build: a stream or `forEach` rather than a for-each loop | PMD, in `pmdAdvice` |

It also sets Java 25 by toolchain, compiles with all warnings as errors, builds archives reproducibly, and adds
Lombok, the SLF4J API, JUnit and AssertJ. An application gets Log4j 2 behind SLF4J at run time; a library (one that
applies `java-library`) gets it for its own tests only, leaving its users to choose their logging. It reports
out-of-date dependencies (`dependencyUpdates`; checked in CI), and makes mutation testing available (`pitest`, PIT run
from its command line, with a plain-English summary after it). `check` works with the configuration cache.

## Settings an organisation can change

| Setting | Default | |
|---|---|---|
| `underTest.codeStyle` | `TOOLKIT` | `ORGANISATION` uses the organisation's rule files below; `NONE` switches the code-quality checks off, for an organisation whose own plugin does them |
| `underTest.checkstyleConfig`, `underTest.pmdRuleset`, `underTest.forbiddenApiSignatures` | none | The organisation's files, with `ORGANISATION` |
| `underTest.spotbugsExcludeFilter` | none | A SpotBugs filter file of findings to leave out, kept in the repo |
| `underTest.cpdMinimumTokens` | `50` | The fewest copied tokens that fail the copy-paste check |
| `underTest.maxClassesPerPackage` | `20` | The most classes a package may hold |
| `underTest.minLineCoverage` | `0.80` | The smallest line coverage, as a fraction |
| `quality.failOnStaleDependencies` | `false` | `true` fails the build on an out-of-date dependency (checked in CI) |

Each can be set in the `underTest { }` block or as a Gradle property, so an organisation's init script can set it
for every build. File settings are paths from the root project's folder.

## Working on it

How to build, test and release the plugin: [AGENTS.md](AGENTS.md).

## Licence

Apache License 2.0; see [LICENSE](LICENSE).

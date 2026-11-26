# java-conventions

[![build](https://img.shields.io/github/actions/workflow/status/fiftiesHousewife/under-test-java-conventions/build.yml?branch=main&label=build)](https://github.com/fiftiesHousewife/under-test-java-conventions/actions/workflows/build.yml)
[![maven central](https://img.shields.io/maven-central/v/io.github.fiftieshousewife/java-conventions?label=maven%20central)](https://central.sonatype.com/artifact/io.github.fiftieshousewife/java-conventions)
[![runs on](https://img.shields.io/badge/runs%20on-Gradle%209.1%2B%2C%20JDK%2021%2B-blue)](#using-it)
[![licence](https://img.shields.io/badge/licence-Apache--2.0-blue)](LICENSE)
[![decisions](https://img.shields.io/badge/decisions-MADR-informational)](docs/decisions/README.md)

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
    id("io.github.fiftieshousewife.java-conventions") version "0.5.1"
}
```

It needs Gradle 9.1 or later, running on JDK 21 or later, and a JDK 25 for the toolchain.

A repo moving to it drops the Gradle plugins whose tasks it now provides: PIT's (`info.solidsoft.pitest`) and
SpotBugs' (`com.github.spotbugs`). Their `pitest` and `spotbugsMain` tasks have the same names as the plugin's.

`./gradlew check` then fails on any breach of the standards below, and `pmdAdvice` reports advice that never fails
the build.

## The standards

| Standard | Checked by |
|---|---|
| Methods of 20 lines or fewer; files of 150 lines or fewer; lines of 120 characters or fewer | Checkstyle |
| Cyclomatic complexity 5 or less, cognitive complexity 6 or less, NPath complexity under 50; boolean expressions of 3 operators or fewer | Checkstyle and PMD |
| No more than three parameters, and no record with more than three components; a method with `@Override` takes the parameters its supertype fixed | Checkstyle, PMD |
| Exactly one assertion statement per test: one `assert...`/`assertThat` chain, one Mockito `verify(...)`, or one `assertAll(...)` grouping several; a test with none fails (REST Assured's `then()`, ArchUnit's `check(...)`, Awaitility's `until...(...)`, jetCheck's `shouldHold(...)` and JUnit's `fail(...)` count as assertions) | PMD |
| No comment on a field or an enum entry; Javadoc only on public API | Checkstyle |
| No copy-pasted code (50 tokens or more; `underTest.cpdMinimumTokens`) | PMD's copy-paste detector (`cpdCheck`) |
| No package of more than 20 classes, in any source set (`underTest.maxClassesPerPackage`) | The plugin (`checkPackageSize`) |
| No bug patterns or security bugs | SpotBugs with FindSecBugs |
| No unsafe, deprecated, internal or non-portable JDK APIs, no `System.out`, no reflection tricks, no `java.util.logging`; time from an injected `Clock` | forbidden APIs (deprecated and internal APIs are refused by the compiler first) |
| Line coverage at least 80% (raise it with `underTest.minLineCoverage`); code with no tests fails too | JaCoCo |
| Final local variables, braces always, no star, unused or redundant imports, no empty blocks, simplified boolean expressions and returns | Checkstyle |
| Advice, never failing the build: a stream or `forEach` rather than a for-each loop | PMD, in `pmdAdvice` |

It also sets Java 25 by toolchain (a repo may still build for an older Java with `options.release`), compiles with
every warning about the code as an error, builds archives reproducibly, and adds Lombok, the SLF4J API, JUnit and
AssertJ. An application gets Log4j 2 behind SLF4J at run time; a library (one that applies `java-library`) gets it
for its own tests only, leaving its users to choose their logging. It reports out-of-date dependencies
(`dependencyUpdates`; checked in CI), and makes mutation testing available (`pitest`, PIT run from its command line,
with a plain-English summary after it). `check` works with the configuration cache.

Tests end in one line: "All 7 tests passed.", or "3 of 7 tests failed. Report: build/reports/tests/test/index.html",
which fails the build and names the report by a path from the project's folder. In a quiet build (`--quiet`), as a
recording uses, nothing shows a person's home folder; at Gradle's usual level, Gradle's own warning still names the
report by its full path. Integration tests, such as those starting containers, go in their own suite, `src/integrationTest`,
with the unit tests' libraries; `check` runs them, and in CI one that didn't run fails the build. A test JVM with
Mockito on its class path starts with Mockito's agent, which JDK 21 and later warn about attaching at run time.

## Settings an organisation can change

| Setting | Default | |
|---|---|---|
| `underTest.codeStyle` | `TOOLKIT` | `ORGANISATION` uses the organisation's rule files below; `NONE` switches the code-quality checks off, for an organisation whose own plugin does them |
| `underTest.checkstyleConfig`, `underTest.pmdRuleset`, `underTest.forbiddenApiSignatures` | none | The organisation's files, with `ORGANISATION` |
| `underTest.codeStyleExcludes` | none | Paths the code-quality checks leave out, as Ant-style patterns from the source folder (`**/legacy/**`, `**/*WeakTest.java`): code kept below the standards on purpose. A Gradle property separates them with commas |
| `underTest.spotbugsExcludeFilter` | none | A SpotBugs filter file of findings to leave out, kept in the repo |
| `underTest.cpdMinimumTokens` | `50` | The fewest copied tokens that fail the copy-paste check |
| `underTest.integrationTestsMaySkip` | `false` | `true` lets an integration test that didn't run pass in CI, for a runner that can't give the tests what they need, such as one whose containers aren't the kind the tests start |
| `underTest.listEachTest` | `false` | `true` shows each test's result even in a quiet build (`--quiet`), as a recording uses; failures without stack traces |
| `underTest.mockitoAgent` | `true` | `false` starts no test JVM with Mockito's agent, so Mockito attaches it itself as it runs: for code that tests how that behaves |
| `underTest.maxClassesPerPackage` | `20` | The most classes a package may hold |
| `underTest.minLineCoverage` | `0.80` | The smallest line coverage, as a fraction |
| `underTest.minMutationScore` | none | With it, `pitest` fails unless the tests catch at least this percentage of planted bugs |
| `underTest.logging` | `true` | `false` adds neither the SLF4J API nor Log4j, for code that logs nothing and should publish no logging dependency |
| `underTest.mutationTestingInCheck` | `false` | `true` makes `check` run `pitest` too, so a minimum mutation score is checked on every build |
| `quality.failOnStaleDependencies` | `false` | `true` fails the build on an out-of-date dependency (checked in CI) |

Code a repo keeps below the standards on purpose, such as a teaching example of code before it is improved, goes in
`underTest.codeStyleExcludes` rather than switching the checks off for the whole project.

Each can be set in the `underTest { }` block or as a Gradle property, so an organisation's init script can set it
for every build. File settings are paths from the root project's folder.

## Working on it

How to build, test and release the plugin: [AGENTS.md](AGENTS.md).

## Licence

Apache License 2.0; see [LICENSE](LICENSE).

# java-conventions

**The code-quality standards for all our Java, as one Gradle plugin: each repo applies a pinned version instead of
keeping its own copy of the rules.**

It applies Checkstyle, PMD and its copy-paste detector, SpotBugs with FindSecBugs, forbidden APIs and a coverage
floor, with the rule files and tool versions inside the plugin. It is the decision of
[under-test](https://github.com/fiftiesHousewife/under-test) that all its Java, and its tools' (connection-paths,
under-test-tools), follow one set of standards
([why](https://github.com/fiftiesHousewife/under-test/blob/main/docs/decisions/0082-all-our-java-follows-one-set-of-code-quality-standards-a-plugin-published-to-maven-central-that-every-repo-applies.md)).

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

`./gradlew check` then fails on any breach of the standards below, and `pmdAdvice` reports advice that never fails
the build.

## The standards

| Standard | Checked by |
|---|---|
| Methods of 20 lines or fewer; files of 150 lines or fewer; lines of 120 characters or fewer | Checkstyle |
| Cyclomatic complexity 6 or less, cognitive complexity 7 or less, NPath complexity 50 or less; boolean expressions of 3 operators or fewer | Checkstyle and PMD |
| No more than three parameters, and no record with more than three components | Checkstyle, PMD |
| One assertion statement per test: one `assert...`/`assertThat` chain, or one `assertAll(...)` | PMD |
| No comment on a field; Javadoc only on public API | Checkstyle |
| No copy-pasted code (75 tokens or more) | PMD's copy-paste detector |
| No bug patterns or security bugs | SpotBugs with FindSecBugs |
| No unsafe, deprecated, internal or non-portable JDK APIs, no `System.out`, no reflection tricks; time from an injected `Clock` | forbidden APIs |
| Line coverage at least 80% (raise it with `underTest.minLineCoverage`) | JaCoCo |
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

`./gradlew check` runs the functional tests, which build small sample projects with Gradle TestKit; they need a JDK
25 that Gradle can find (or `-Porg.gradle.java.installations.paths=<its home>`). `./gradlew publishAllPublicationsToLocalRepository`
shows what would be published; publishing to Maven Central needs the signing key and a Central Portal token (see
[build.gradle.kts](build.gradle.kts)). See [AGENTS.md](AGENTS.md).

## Licence

Apache License 2.0; see [LICENSE](LICENSE).

---
status: proposed
decision-makers: Maintainers
group: Settings
---

# A repo can list each test's result in a quiet build

## Context and Problem Statement

A quiet build (`--quiet`) shows no test that passed, and no test's name unless it failed: Gradle lists test events at its usual level, not the quiet one. A repo whose builds are recorded or shown to people runs Gradle quietly so that only what matters shows, and then needs each test's line there. Without a setting, each of its build files configures the same test logging.

## Considered Options

* **A setting, `underTest.listEachTest`, `false` by default: with `true`, every test task lists each test as it passed, failed or was skipped, even in a quiet build, a failure with its message and no stack trace**
* **Always list each test: a large repo's every build would print a line per test**
* **Each repo configures its test logging: the same block in every build file**

## Decision Outcome

`underTest.listEachTest`, in a project's `underTest { }` block or as a Gradle property, so a repo sets it once in `gradle.properties`. With `true`, every test task, `test` and `integrationTest` among them, shows each test's result at the quiet level, with a failure's message in full and no stack trace, which the report holds. Unset, test output is Gradle's own, and each run still ends in one line (record 0032).

### Consequences

* The plugin sets each test task's quiet test logging; TestRunsTest checks that with the setting a quiet build shows each test's result, and that without it a quiet build shows no passing test.

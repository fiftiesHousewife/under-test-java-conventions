---
status: accepted
decision-makers: Maintainers
group: Settings
---

# Code kept below the standards on purpose can be left out of the code-quality checks

## Context and Problem Statement

Some repos keep code that breaks the standards on purpose: a teaching example of the code before it is improved, a test written weak to show what a weak test misses. The only way to keep the checks off it was `underTest.codeStyle = NONE`, which switches them off for the whole project, good code included. Leaving paths out in the repo's own build reached Checkstyle and PMD, but not the copy-paste check, the package-size check or SpotBugs, whose sources the plugin chooses.

## Considered Options

* **One setting of paths that every code-quality check leaves out**
* **`codeStyle = NONE` for such projects**
* **Each repo leaves the paths out of each task itself**

## Decision Outcome

`underTest.codeStyleExcludes`: a list of Ant-style path patterns from the source folder, such as every `legacy` package or every class whose name ends in `WeakTest`. Set in the `underTest { }` block, or as a Gradle property with the patterns separated by commas. Checkstyle, PMD (failing, and advice), the copy-paste check, the package-size check, SpotBugs and forbidden APIs all leave those files out. A pattern for a `.java` file also leaves out its compiled class and nested classes.

### Consequences

* The compiler, the tests, coverage and mutation testing still see the code: only the code-quality checks leave it out.
* Empty by default, so nothing is left out unless a repo says so, and its build file shows what.
* CodeStyleExcludesTest has code breaking every check in a `legacy` package and a weak test: each check fails it, and passes with the setting.

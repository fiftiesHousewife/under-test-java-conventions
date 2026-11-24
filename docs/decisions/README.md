# Decision records

The code-quality standards java-conventions applies, one record each: what each is, and why. A standard changes only
by a new record here, which the change names; an accepted record is never edited
([how to write one](../../.github/skills/write-a-decision-record/SKILL.md)).

Groups: **Size and simplicity**, **Tests**, **Comments**, **Duplication and structure**, **Bugs and APIs**, **Coverage and evidence**, **Style**, **Build**, **Settings**.

| Record | Standard | Group | Status |
|---|---|---|---|
| [0001](0001-methods-files-and-lines-have-size-limits.md) | Methods, files and lines have size limits | Size and simplicity | accepted |
| [0002](0002-methods-stay-simple.md) | Methods stay simple: cyclomatic complexity 5, cognitive complexity 6, NPath under 50, boolean expressions of 3 operators | Size and simplicity | accepted |
| [0003](0003-at-most-three-parameters-and-three-record-components.md) | At most three parameters, and at most three components in a record | Size and simplicity | accepted |
| [0004](0004-a-test-makes-exactly-one-assertion.md) | A test makes exactly one assertion statement | Tests | accepted |
| [0005](0005-comments-only-where-they-belong.md) | No comment on a field or an enum entry, and Javadoc only on public API | Comments | accepted |
| [0006](0006-copy-pasted-code-fails-from-50-tokens.md) | Copy-pasted code fails the build from 50 tokens | Duplication and structure | accepted |
| [0007](0007-no-package-holds-more-than-20-classes.md) | No package holds more than 20 classes | Duplication and structure | accepted |
| [0008](0008-no-bug-patterns-or-security-bugs.md) | No bug patterns or security bugs | Bugs and APIs | accepted |
| [0009](0009-forbidden-apis.md) | No unsafe, deprecated, internal or non-portable JDK APIs, no System.out, and time from an injected Clock | Bugs and APIs | accepted |
| [0010](0010-line-coverage-at-least-80-percent.md) | Line coverage is at least 80%, and code with no tests fails | Coverage and evidence | accepted |
| [0011](0011-style-basics.md) | Local variables final, braces always, no star imports | Style | accepted |
| [0012](0012-for-each-loops-are-advice.md) | A for-each loop is advice, never failing the build | Style | accepted |
| [0013](0013-compiled-on-java-25-with-every-warning-an-error.md) | Code compiles on Java 25, with every warning an error | Build | accepted |
| [0014](0014-an-organisation-can-replace-the-rules-not-the-evidence.md) | An organisation can replace the code-quality rules, but not the testing evidence | Settings | accepted |
| [0015](0015-mutation-testing-is-available-not-required.md) | Mutation testing is available, not required | Coverage and evidence | accepted |
| [0016](0016-out-of-date-dependencies-are-reported.md) | Out-of-date dependencies are reported, and fail only when asked | Build | accepted |
| [0017](0017-the-standards-are-one-plugin-each-repo-applies-at-a-pinned-version.md) | The standards are one plugin that each repo applies at a pinned version, with its rule files and tool versions inside | Build | accepted |
| [0018](0018-the-plugins-own-tools-may-come-from-the-gradle-plugin-portal.md) | The plugin's own tools may come from the Gradle Plugin Portal | Build | accepted |
| [0019](0019-dependencies-are-checked-for-known-vulnerabilities-against-osv.md) | Dependencies are checked for known vulnerabilities against the OSV database | Bugs and APIs | proposed |

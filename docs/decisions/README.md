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
| [0018](0018-the-plugins-own-tools-may-come-from-the-gradle-plugin-portal.md) | The plugin's own tools may come from the Gradle Plugin Portal | Build | replaced by 0022 |
| [0019](0019-dependencies-are-checked-for-known-vulnerabilities-against-osv.md) | Dependencies are checked for known vulnerabilities against the OSV database | Bugs and APIs | proposed |
| [0020](0020-an-organisation-can-change-the-copy-paste-and-package-size-limits.md) | An organisation can change the copy-paste and package-size limits | Settings | accepted |
| [0021](0021-a-library-leaves-its-users-to-choose-their-logging.md) | An application logs through Log4j 2; a library leaves its users to choose their logging | Build | accepted |
| [0022](0022-pit-runs-from-its-command-line-and-only-spotbugs-and-the-versions-report-come-from-the-plugin-portal.md) | PIT runs from its command line, and only SpotBugs' and the versions report's Gradle plugins come from the Gradle Plugin Portal | Build | replaced by 0023 |
| [0023](0023-spotbugs-pit-and-cpd-run-from-their-command-lines-and-only-the-versions-report-comes-from-the-plugin-portal.md) | SpotBugs, PIT and CPD run from their command lines, and only the versions report comes from the Gradle Plugin Portal | Build | accepted |
| [0024](0024-compiling-for-an-older-java-is-not-a-warning-that-fails-the-build.md) | Compiling for an older Java is not a warning that fails the build | Build | accepted |
| [0025](0025-a-repo-can-require-a-minimum-mutation-score.md) | A repo can require a minimum mutation score | Coverage and evidence | accepted |
| [0026](0026-code-kept-below-the-standards-on-purpose-can-be-left-out-of-the-code-quality-checks.md) | Code kept below the standards on purpose can be left out of the code-quality checks | Settings | accepted |
| [0027](0027-a-test-librarys-own-assertion-call-counts-as-the-tests-assertion.md) | A test library's own assertion call counts as the test's assertion | Tests | accepted |
| [0028](0028-junits-fail-counts-as-the-tests-assertion.md) | JUnit's fail(...) counts as the test's assertion | Tests | accepted |
| [0029](0029-a-repo-can-run-mutation-testing-in-check.md) | A repo can run mutation testing in check | Coverage and evidence | accepted |
| [0030](0030-code-that-logs-nothing-can-leave-out-the-logging-dependencies.md) | Code that logs nothing can leave out the logging dependencies | Build | accepted |
| [0031](0031-a-method-overriding-another-takes-the-parameters-its-supertype-fixed.md) | A method overriding another takes the parameters its supertype fixed | Size and simplicity | accepted |

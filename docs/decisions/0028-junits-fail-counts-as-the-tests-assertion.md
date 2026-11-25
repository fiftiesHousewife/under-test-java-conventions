---
status: accepted
decision-makers: Maintainers
group: Tests
---

# JUnit's fail(...) counts as the test's assertion

## Context and Problem Statement

TestMustAssert fails a test with no assertion (records 0004 and 0027). Some tests check by failing: a jetCheck property test catches `PropertyFalsified` and calls `fail(...)` with it, and an older test of an exception calls `fail()` after the call that should have thrown. JUnit's `fail(...)` is one of its assertions, but the rule didn't count it, so such tests had to suppress the rule.

## Considered Options

* **Count `fail(...)` anywhere in the test**
* **Count it only inside a `catch`**
* **Leave the rule as it is**

## Decision Outcome

TestMustAssert counts `fail(...)` anywhere in the test, as it counts `assert...` calls: after a call that should throw is as much a check as inside a `catch`. MultipleAssertionsMustUseAssertAll still counts only `assert...` and `verify...` calls. This narrows record 0004, as record 0027 does, and both otherwise stand.

### Consequences

* A test whose only check is `fail(...)` passes TestMustAssert.
* AssertionRulesTest has a test that fails in the catch of what it checks.

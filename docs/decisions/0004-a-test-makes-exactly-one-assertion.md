---
status: accepted
decision-makers: Maintainers
group: Tests
---

# A test makes exactly one assertion statement

## Context and Problem Statement

A test that stops at its first failed assertion hides the rest; a test with no assertion passes whatever the code does.

## Considered Options

* **Exactly one: one `assert...` call, one `assertThat` chain, or one `assertAll(...)` grouping several**
* **At most one**
* **No rule**

## Decision Outcome

Exactly one assertion statement per test. Several checks go in one `assertAll(...)`, so every failure is reported. Any `assert...` (including `assertThrows`) or `verify...` call anywhere in the test counts as its assertion. So do the assertion styles of established test libraries, which read as a call rather than an `assert`: `then()` (REST Assured, BDDMockito), `check(...)` (ArchUnit), `until...(...)` (Awaitility) and `shouldHold(...)` (jetCheck).

### Consequences

* PMD's MultipleAssertionsMustUseAssertAll fails more than one; TestMustAssert fails none.
* A test that asserts only inside a helper method it calls fails: the assertion goes in the test.
* The library calls are recognised by name alone, so a test calling some other method named `check` or `then` passes the rule; a test with no assertion at all still fails.
* MultipleAssertionsMustUseAssertAll counts only `assert...` and `verify...` calls: an Awaitility `untilAsserted(() -> assertThat(...))` is one assertion, not two.

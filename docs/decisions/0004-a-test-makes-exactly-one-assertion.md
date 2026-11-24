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

Exactly one assertion statement per test. Several checks go in one `assertAll(...)`, so every failure is reported. Any `assert...` (including `assertThrows`) or `verify...` call anywhere in the test counts as its assertion.

### Consequences

* PMD's MultipleAssertionsMustUseAssertAll fails more than one; TestMustAssert fails none.
* A test that asserts only inside a helper method it calls fails: the assertion goes in the test.

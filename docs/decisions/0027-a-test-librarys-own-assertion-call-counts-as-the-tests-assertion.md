---
status: accepted
decision-makers: Maintainers
group: Tests
---

# A test library's own assertion call counts as the test's assertion

## Context and Problem Statement

A test makes exactly one assertion statement (record 0004), and TestMustAssert fails a test with none, counting only `assert...` and `verify...` calls. Established test libraries assert with calls of other names: REST Assured's `given()...when()...then().statusCode(201)`, ArchUnit's `rule.check(classes)`, Awaitility's `await().until(...)`, jetCheck's `forAll(...).shouldHold(...)`. Tests written in those styles do check the code, and failed as if they checked nothing.

## Considered Options

* **Count those libraries' assertion calls by name in TestMustAssert**
* **Leave the rule as it is: such tests add an `assert...` call or are rewritten**

## Decision Outcome

TestMustAssert also counts `then()` (REST Assured, and BDDMockito's `then(mock).should()`), `check(...)` (ArchUnit), `until...(...)` (Awaitility) and `shouldHold(...)` (jetCheck). MultipleAssertionsMustUseAssertAll still counts only `assert...` and `verify...` calls, so an Awaitility `untilAsserted(() -> assertThat(...))` is one assertion, not two. This narrows record 0004, which otherwise stands.

### Consequences

* The calls are recognised by name alone, so a test calling another method named `check` or `then` passes TestMustAssert; a test with no assertion at all still fails.
* A test that asserts only inside a helper method it calls still fails: the assertion goes in the test.
* AssertionRulesTest has a test for each of the four calls.

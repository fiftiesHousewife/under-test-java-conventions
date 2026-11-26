---
status: proposed
decision-makers: Maintainers
group: Settings
---

# Code testing how Mockito attaches itself can leave out Mockito's agent

## Context and Problem Statement

A test JVM with Mockito on its class path starts with Mockito's agent (record 0034). Some code's tests check what happens when Mockito attaches its agent itself, as it does in a JVM started with no agent: a Java agent, for example, that must let other agents attach while it runs. With Mockito's agent already given at start, Mockito never attaches itself, and those tests stop testing what the code's users meet.

## Considered Options

* **A setting, `underTest.mockitoAgent`, `true` by default: with `false`, no test JVM starts with Mockito's agent**
* **No setting: such a repo removes the plugin's argument from each test task itself, which depends on how the plugin adds it**

## Decision Outcome

`underTest.mockitoAgent`, in a project's `underTest { }` block or as a Gradle property. With `false`, the plugin gives no test JVM Mockito's agent or `-Xshare:off`, and Mockito attaches its agent itself as it runs, with the JDK's warning. Unset, nothing changes. This narrows record 0034, which otherwise stands.

### Consequences

* TestRunsTest checks that with the setting off, a test JVM starts without Mockito's agent.

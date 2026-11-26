---
status: accepted
decision-makers: Maintainers
group: Build
---

# A test JVM with Mockito starts with Mockito's agent

## Context and Problem Statement

Mockito's inline mock maker, its default, needs a Java agent, and by default attaches it to the test JVM while that JVM runs. JDK 21 and later print a warning on every run about an agent attached that way, and say a later release will refuse it unless the JVM is started with a flag allowing it. Each repo starting the agent itself writes the same configuration, picking out Mockito's jar by hand.

## Considered Options

* **When `mockito-core` is on a test task's class path, the test JVM starts with that jar as its agent (`-javaagent`), with class sharing off**
* **A `mockitoAgent` configuration naming Mockito, at a version the plugin chooses: the repo's own Mockito version may differ**
* **Allow attaching at run time (`-XX:+EnableDynamicAgentLoading`): it hides the warning, and lets any library attach an agent**

## Decision Outcome

Every test task, as it starts, looks for `mockito-core-*.jar` on its class path: the repo's own Mockito, at the repo's version. When it is there, the test JVM gets it as its agent. Class sharing is off then (`-Xshare:off`): with an agent on the class path the JVM can share only its own classes, and says so on every run, while tests gain little from sharing. The plugin adds no Mockito dependency; a repo without Mockito gets no agent and no flag.

### Consequences

* TestRunsTest checks that a test JVM with Mockito on its class path starts with Mockito's agent.
* `pitest` runs its tests in JVMs of its own, which don't get the agent.

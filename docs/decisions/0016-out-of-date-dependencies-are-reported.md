---
status: accepted
decision-makers: Maintainers
group: Build
---

# Out-of-date dependencies are reported, and fail only when asked

## Context and Problem Statement

A dependency left behind collects fixes it doesn't have, and is harder to update the longer it waits.

## Considered Options

* **Reported in CI; failing when `quality.failOnStaleDependencies=true`**
* **Always failing**
* **Not checked**

## Decision Outcome

`dependencyUpdates` reports out-of-date dependencies, pre-releases ignored. In CI, `checkDependencyVersions` runs in `check` and warns, or fails when `quality.failOnStaleDependencies=true`.

### Consequences

* None beyond the outcome.

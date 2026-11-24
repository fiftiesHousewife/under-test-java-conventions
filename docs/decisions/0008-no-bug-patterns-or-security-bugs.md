---
status: accepted
decision-makers: Maintainers
group: Bugs and APIs
---

# No bug patterns or security bugs

## Context and Problem Statement

Many bugs and security holes have known shapes a tool finds in the compiled code.

## Considered Options

* **SpotBugs with FindSecBugs, at maximum effort, reporting from low confidence**
* **SpotBugs at its defaults**
* **None**

## Decision Outcome

SpotBugs with the FindSecBugs plugin, at maximum effort and reporting from low confidence, fails the build on any finding. A repo's own exclusions stay in the repo (`excludeFilter` on its SpotBugs tasks).

### Consequences

* SpotBugs runs on main and test classes alike.
* An HTML report is written for each SpotBugs task.
* FindSecBugs' hard-coded password and key detectors stay on and fail the build: on a developer's machine they are an
  early warning for credentials written into code. They see only the compiled code, not its history or settings files,
  so they are not the gate: a scan of the whole history, such as gitleaks in CI, decides whether a repo may be built.

---
status: proposed
decision-makers: Maintainers
group: Bugs and APIs
---

# Dependencies are checked for known vulnerabilities against the OSV database

## Context and Problem Statement

Everything a build brings in (libraries, quality tools, Gradle plugins and their own dependencies) must have no known vulnerabilities. OWASP dependency-check needs a key for the US National Vulnerability Database and fails without one. A one-off check of 105 resolved artifacts against OSV (osv.dev) found none, and correctly flagged a known-vulnerable control version. The plugin checks only that dependencies are current, not whether they are vulnerable.

## Considered Options

* **Check every resolved dependency against OSV: collect resolved artifacts from every configuration, query OSV in batches, fail at or above a chosen severity; an organisation without internet access points it at a mirror of OSV's data files**
* **OSV-Scanner, Google's command-line tool: complete, but a Go program many organisations must approve separately**
* **OWASP dependency-check: thorough, but needs an NVD key and a large database download**

## Decision Outcome

Proposed: the first option, run in CI on every build, after the check that dependencies are on their latest versions. Not yet in the plugin.

### Consequences

* A vulnerability-check task, with a severity threshold setting and a setting for an internal OSV mirror.
* Its own decision record, accepted, before it fails anyone's build.

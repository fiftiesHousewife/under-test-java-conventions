---
status: accepted
decision-makers: Maintainers
group: Duplication and structure
---

# Copy-pasted code fails the build from 50 tokens

## Context and Problem Statement

Copied code is fixed in one place and left broken in the others.

## Considered Options

* **50 tokens (about six to eight short lines)**
* **75 tokens**
* **100 tokens (PMD's default)**

## Decision Outcome

Copy-pasted Java of 50 tokens or more fails the build, in every source set. 50 catches real copy-paste without flagging boilerplate such as `equals` and `hashCode` or a builder chain.

### Consequences

* `cpdCheck` (PMD's copy-paste detector) runs in `check`, over every source set's Java, including ones added after the plugin; it is skipped when there is no Java code.
* Lowered from 75: a repo moving to this version may have copies to remove.

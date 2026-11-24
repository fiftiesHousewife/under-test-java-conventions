---
status: accepted
decision-makers: Maintainers
group: Comments
---

# No comment on a field or an enum entry, and Javadoc only on public API

## Context and Problem Statement

A field's or an enum entry's name says what it is; a comment beside it repeats the name or goes stale. Javadoc on private code documents what no caller sees.

## Considered Options

* **No comment on a field or enum entry; Javadoc only on public types and their public members, other comments as `//`**
* **Comments anywhere**

## Decision Outcome

No comment on a field or an enum entry: name it so it needs none, or say it on the class, method or enum. Javadoc only on a public type and its public members; elsewhere a `//` comment, or none.

### Consequences

* Checkstyle fails each: a field comment, NoEnumEntryComment, and JavadocOnlyWhenPublic.
* A comment inside an enum entry's own body (its methods) is not affected.

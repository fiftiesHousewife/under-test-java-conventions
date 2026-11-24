---
name: write-a-decision-record
description: Draft or replace a decision record in docs/decisions/ for one of java-conventions' code-quality standards. Use whenever a rule, a limit or a tool the plugin applies is added, changed or removed, or a conversation settles one.
---

# Writing a decision record

Each code-quality standard the plugin applies has one record in [docs/decisions](../../../docs/decisions/README.md),
in [MADR](https://adr.github.io/madr/)'s minimal form. A change to a rule, a limit or a tool comes with its record, and
the commit names it.

1. **One standard per record.** Copy [the template](../../../docs/decisions/template.md) to
   `docs/decisions/NNNN-<the-standard-in-kebab-case>.md`, numbered after the last record. The title states the
   standard in full, in plain English, with its limits ("No package holds more than 20 classes").
2. **Context** says what goes wrong without the standard: facts only. **Considered Options** are the real
   alternatives, the chosen one first. **Decision Outcome** is the standard and its limits. **Consequences** name the
   check that enforces it (the Checkstyle module, PMD rule or task) and what follows for a repo applying the plugin.
3. **Front matter:** `status` (`proposed` until the maintainers choose it, then `accepted`), `decision-makers:
   Maintainers`, and a `group` already listed in the folder's README.
4. **An accepted record is never edited.** A change is a new record that replaces it; set the old one's status to
   `replaced by NNNN`.
5. **Cite nothing outside this repo.** Each record stands on what the plugin does and why.
6. **Update the folder's README:** add the record's row to the table, with its group and status.
7. **The rule and its test go with the record.** Every rule has a functional test with a sample it fails, the clean
   sample still passes, and the README's table of standards says the same as the record.

## Discussing a standard with a person
Ask one question at a time, with the recommended option first, and write the answers into the record only once the
person confirms them. Mark it `accepted` only when they chose it.

---
status: accepted
decision-makers: Maintainers
group: Size and simplicity
---

# A method overriding another takes the parameters its supertype fixed

## Context and Problem Statement

At most three parameters for a method (record 0003). A method that overrides another, often a library's, can't choose its parameters: ASM's `ClassVisitor.visitMethod` takes five, and the JDK's `ImageObserver.imageUpdate` six. Such a method failed the rule with nothing a project could change, so whole files were left out of the code-quality checks to get past it.

## Considered Options

* **Methods marked `@Override` are exempt from the parameter limit**
* **Leave the rule as it is: such files go in `underTest.codeStyleExcludes`**

## Decision Outcome

A method marked `@Override` takes the parameters its supertype fixed: Checkstyle's ParameterNumber ignores it (`ignoreOverriddenMethods`), and PMD's ExcessiveParameterList leaves it out. The method it overrides, when it is in the repo, is still checked. This narrows record 0003, which otherwise stands.

### Consequences

* An override must carry `@Override` to be exempt, which the compiler then checks is true.
* JavaConventionsTest builds a class implementing the JDK's six-parameter `ImageObserver.imageUpdate`.

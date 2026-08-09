# ADR 0004 — Strategy with a registry for system-specific rules

**Status:** accepted
**Refines:** ADR 0003, which established that game systems are code rather than data.

## Context

ADR 0003 decided that each game system is implemented in Java, but did not say how a
request carrying a system identifier reaches the right implementation. Without an
explicit mechanism, the natural drift is a service that branches on the identifier,
which turns every new system into an edit of existing classes.

## Decision

System-specific behavior is expressed with the **Strategy pattern**. Each behavior
family is an interface; each supported system provides one implementation per
interface; a registry resolves the implementation by system identifier at runtime.

The flow is: controller receives the request → orchestrating service resolves the
strategy through the registry → the strategy applies the rules → the service handles
persistence and side effects.

## Rationale

- Strategy is defined as a family of interchangeable algorithms in separate classes,
  which is exactly the shape of "calculate hit points, per system".
- Registries built from injected implementations make Open/Closed real rather than
  aspirational: adding a system adds classes and edits none.
- Branching on a type code is a recognized code smell, and replacing conditionals with
  polymorphism is the standard refactoring away from it. Adopting the pattern up front
  avoids having to perform that refactoring later across many call sites.

## Consequences

- Every strategy interface exposes `systemId()`, and implementations are Spring
  components so the registry can be assembled automatically.
- Orchestrating services must stay free of game rules. A service that knows what a
  proficiency bonus is has absorbed logic that belongs to a strategy.
- An unknown system identifier is an explicit failure, never a silent default.
- Strategies must be grouped coarsely. One interface per behavior family, not one per
  formula, otherwise each new system costs a dozen classes and registrations.
- Testing improves: each system's rules are tested directly against its strategy,
  without HTTP or persistence.

## Rejected alternatives

**Conditional dispatch in the service** — simplest to write, but every new system
edits shared code, and the branches spread to every call site over time.

**Inheritance with Template Method** — a base class per behavior with per-system
subclasses. Rejected because systems differ too much for a shared skeleton, and
inheritance would couple every system to whatever the base class assumes. Composition
through Strategy keeps them independent.

**Declarative rule packs interpreted at runtime** — the eventual generic solution,
already rejected in ADR 0003 as premature. The registry seam keeps that door open.
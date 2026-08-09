# ADR 0003 — Game systems implemented as code, not data

**Status:** accepted

## Context

Supporting several game systems can be done two ways. Each system can be Java code
behind shared interfaces, or systems can be declarative rule packs that a generic
engine interprets at runtime.

## Decision

Each game system is implemented as Java classes behind the interfaces in the
`ruleset` package. Only the systems the owner actually plays will be implemented.

## Rationale

- Rule packs mean building an interpreter and an expression language before a single
  character can be created. That is a much larger project than the one intended.
- Typed implementations are debuggable, testable and refactorable with tooling.
- The realistic number of supported systems is small — the ones the owner plays — so
  the cost of adding one by writing code is acceptable.

## Consequences

- Adding a system means new classes and a deploy, never a configuration change.
- Open/Closed becomes a hard rule: a new system must not require editing existing
  classes. If it does, the abstraction is wrong and must be discussed before
  proceeding.
- Abstractions must be extracted from the second implementation, not invented before
  it. The first system is written concretely; genuine seams reveal themselves when the
  second arrives.

## Note

Persistence is deliberately generic anyway — system identifier plus JSONB payload —
so moving to declarative rule packs later would not require a data migration.
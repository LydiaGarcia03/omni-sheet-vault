# ADR 0001 — PostgreSQL with JSONB for character sheets

**Status:** accepted

## Context

Character sheets have two natures at once. Part of every sheet is identical across
game systems: owner, system, name, portrait, backstory. The rest differs completely —
D&D has armor class and spell slots, Vampire has Humanity and dice pools, Call of
Cthulhu has Sanity. No fixed set of columns fits all systems.

Options considered: a table per system, one wide table with all columns, an
entity-attribute-value model, or a document column inside a relational row.

## Decision

PostgreSQL, with common data in relational columns and the system-specific part in a
JSONB column.

## Rationale

- A table per system turns every generic query into a growing `UNION`.
- A wide table is mostly nulls and needs a migration per system.
- EAV loses types, makes the database unable to constrain anything, and turns reading
  one sheet into reassembling dozens of rows.
- JSONB is stored decomposed and binary, so it can be indexed with GIN and queried
  with real predicates. A text column holding JSON could not do this.

Secondary reasons: roll history is append-only and cheap in PostgreSQL but billed per
operation in document databases; and the same engine runs identically in Docker
locally and on the deployment VM.

## Consequences

- The database cannot validate payload contents. Bean Validation on typed Java
  records is the only gate, and this is now a hard rule.
- Every payload must carry a `schemaVersion`, and payload shape changes need
  migration code in Java, not only SQL.
- Adding a game system requires no schema migration.

## Rejected alternatives

**SQL Server** — the free editions either cap storage or forbid production use, so
the license would have to change mid-project.

**Firebase/Firestore** — pushes business rules toward the client and serverless
functions, which conflicts with a Java backend owning the rules. Firebase was
considered for auth and file storage only, then dropped entirely once Keycloak and
MinIO covered both, to avoid a dependency with no remaining purpose.
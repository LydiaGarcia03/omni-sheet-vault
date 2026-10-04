# Domain model

## Core concepts

**Player** — an authenticated person. Identity lives in Keycloak; the application
stores only a local row keyed by the token subject, plus a display name. No
credentials, no email/password handling.

**Game system** — a supported ruleset, identified by a stable string such as
`dnd-5e`. Implemented as Java classes, not stored as data. The identifier is the
lookup key used by the strategy registries, so it is part of the domain, not a label:
once a character is stored with it, it can never be renamed without a data migration.

**Character** — a sheet belonging to one player, bound to one game system. Split in
two: the common part (owner, system, name, portrait, backstory, timestamps) in
relational columns, and the system-specific part in a JSONB payload. A character is
either a **draft** (still in the creation flow: a possibly incomplete build, no
playable sheet) or **active**.

**Sheet payload** — the JSONB document. Typed in Java as a record per system
(`Dnd5eSheet` and so on). Its schema version is stored in a dedicated column and
mirrored inside the record, so old documents remain readable after the shape evolves.
See `database-schema.md` for why the column is authoritative.

**Roll** — an immutable record of one dice resolution: who rolled, when, for which
character, the expression, the context that triggered it, each die result, and the
total. Rolls are appended, never modified or deleted.

**Choice** — a decision offered during character creation (race, class, background),
along with what it grants. Produced by the system implementation so the UI can show
consequences before the choice is committed. `ruleset.CreationChoice` is
system-agnostic and never persisted. It has a deterministic id such as
`class:fighter:1:fighting-style`, a `type`, a `parentChoiceId` for sub-choices, and
its options. A system's `CharacterCreationFlow` produces choices as a `BuildPlan`.

**Build** — what a character is built from: species, subspecies, classes and levels,
background, the ability score and hit point methods, and every answered Choice
(`Dnd5eCharacterBuild`, stored inside the sheet payload as `build`). It is the source
of truth for the sheet's build-derived fields: the build resolver regenerates those
fields whenever the build changes, and leaves play state alone. A sheet without a
build is hand-authored. See `systems/dnd-5e/features/character-creation.md`, Stage B, option (a).
While a character is a draft, its build lives in `characters.creation_draft` and may
leave its structure unanswered. The plan then offers `build.species`,
`build.background`, `build.classes` and `build.abilityScores` as pending choices.
Only a complete build (the `Dnd5eCharacterBuild.Complete` validation group) can be
materialized.

**Catalogue entry** — one piece of reference content: a spell, item, feature or
creature. Not owned by a player — every authenticated player reads the same rows.
Loaded by an import pipeline run as a command, never written through a player-facing
endpoint. Its natural key is `(system_id, kind, slug)`, so re-importing the same
content updates the row rather than duplicating it. Its one piece of descriptive prose
is redactable: a server-side switch decides whether an API response includes the real
text or a marker — see `decisions/adr-0005-content-catalogue-and-redaction.md`.
Mechanics are never redacted, only prose.

## Persistence shape

| Table | Purpose |
| --- | --- |
| `players` | Local mirror of the token subject |
| `characters` | Common columns plus `system_id` and the JSONB `sheet` |
| `rolls` | Append-only roll history, foreign key to character |
| `catalogue_entries` | Reference content, keyed by `(system_id, kind, slug)`, not owned by a player |

Columns, types, indexes and the reasoning behind them live in `database-schema.md`.
This document stays conceptual; that one is authoritative for the actual schema.

Design rules:

- Anything filtered, sorted, joined or constrained lives in a real column.
- Anything that varies by system lives in the JSONB payload.
- Adding a game system requires no migration.
- The database does not validate the payload — Bean Validation on the typed record is
  the only gate.

## Invariants

- A character always belongs to exactly one player and one game system.
- A roll always belongs to a character, and a character to a player. Ownership is
  checked against the token subject, never against a client-supplied identifier.
- A sheet payload always has a recorded schema version.
- Deleting a character is a soft delete; the roll history survives it.
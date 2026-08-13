# ADR 0005 — Content catalogue scope and redaction

**Status:** accepted

## Context

The sheet needs a catalogue of game content: spells, items, features, species traits,
feats, creature stat blocks. For D&D 5e two scopes were possible.

The SRD 5.1 is published under Creative Commons and can be redistributed with
attribution, but it covers a fraction of the published material — a character built
from it cannot use most subclasses, spells or items the owner actually plays with.

The full published fifth edition covers everything the owner plays, but its text is
protected and this repository is public.

## Decision

The catalogue covers the **full published 2014 fifth edition**.

Descriptive prose is **redactable**: a server-side switch, configured per environment,
determines whether description text is included in API responses. Redaction is enabled
in the public deployment and disabled locally.

Redaction never affects mechanics. A redacted spell is still castable, still consumes
the right slot, still rolls its damage. Names, numbers, tags and source attribution are
always present. Only the prose is withheld.

## Rationale

The owner plays with the full published material and wants sheets that reflect it.
Restricting the catalogue to the SRD would make the application unusable for its
primary purpose, which is running the owner's own characters.

## Consequences

- The repository contains text from published books. This is protected material and
  redistribution is not covered by any fan-content policy. **The owner has evaluated
  this and accepted the risk deliberately.** Anyone maintaining this project should
  know the exposure is real and is not mitigated by redaction, by the absence of
  profit, or by a license file asserting fan-work status.
- Redaction is a display feature, not a legal control. It reduces what a visitor
  reads; it does not change what the repository holds.
- Redaction must be enforced server-side. Sending prose and hiding it with a CSS blur
  would be trivially defeated and is explicitly forbidden — see `ui-design-system.md`.
- Every system's content model needs a redactable text field type from the start.
  Retrofitting one later means touching every endpoint that returns descriptions.
- The client must render a designed placeholder, since it receives no text to style.

## Rejected alternatives

**SRD 5.1 only** — legally clean and publishable, but excludes most of what the owner
plays. Rejected on usefulness.

**Engine public, catalogue local** — ship the code and an SRD seed publicly, load the
full catalogue from a file kept out of version control. This keeps the repository
clean while preserving full local functionality. Rejected: the owner chose to accept
the risk rather than maintain a split distribution.
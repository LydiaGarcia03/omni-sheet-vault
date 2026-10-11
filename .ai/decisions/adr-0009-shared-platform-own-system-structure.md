# ADR-0009 — Shared platform, each system with its own sheet structure

Date: 2026-10-04 · Status: accepted (owner) · Refines adr-0003 and adr-0004

## Context

Adding Vampire: The Masquerade 5e showed that the ruleset seams built while
D&D 5e was the only system are D&D-shaped: `SheetCalculator` returns D&D's
`VitalsZone`, `SheetMutator` has ~60 D&D operations, `CharacterCreationFlow`
has level up, and the sheet endpoints follow D&D's fields
(`systems/vtm-v5/features/implementation-plan.md` §5b).

## Decision

- **Shared platform, routed by system id.** What every system does the same
  way stays in common code and only picks the system's implementation through
  a registry: creating a character or draft, choosing the system, portraits,
  the character list and summaries, the builder frame (draft, autosave,
  finish), sharing, the app header.
- **Each system owns its sheet.** Its stored sheet, calculations, play-state
  actions, rolls and endpoints live in its own package
  (`api/ruleset/<system>`, its own controllers and response types) and its own
  web folder (`web/src/systems/<system>`). Nothing forces one system's sheet
  shape on another. Common pieces are shared only when two systems really do
  the same thing.
- **D&D 5e keeps its code.** Its sheet seams and endpoints are D&D's own; they
  are not rewritten to fit Vampire.
- **Web reuses components, not shapes.** Generic visual pieces already built
  for D&D (sidebar shell and panes, framed boxes, tabs, dice tray, builder
  checklist and summary) are reused and themed per system; system-specific
  ones stay in the system's folder.

## Consequences

- The per-system registries stay the entry points (adr-0004); a new system
  adds its package, folder and registry entries.
- Shared seams with D&D-only methods (e.g. `withLevelUp`) become optional
  defaults instead of being implemented by every system.
- More code per system, in exchange for no system bending another's model.

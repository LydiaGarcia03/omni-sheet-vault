# ADR 0007 — A curated modifier overlay for mechanics 5etools only describes in prose

**Status:** accepted (owner approval, 2026-09-23)

Amends `adr-0006-5etools-as-content-source.md` for **mechanical encoding only**.
Every fact and every text still comes from 5etools.

## Context

`systems/dnd-5e/features/character-creation.md`'s Stage C3 turns the D&D 5e sheet into a living
one. Equipment, conditions, class features and lasting spells change derived values
(AC, saves, speed, HP maximum) and roll modes (advantage and disadvantage).

For that to work, every source of a change needs a machine-readable description of
what it changes. 5etools provides one only in some cases:

- **Structured:** items (`ac`, `bonusAc`, `bonusSavingThrow`, `bonusWeapon`,
  `strength`, `stealth`, `resist`, …) and parts of species, backgrounds and feats
  (`speed`, `darkvision`, `resist`, `ability`, `skillProficiencies`, …).
- **Prose only:** conditions ("A poisoned creature has disadvantage on attack rolls
  and ability checks"), exhaustion levels, many class features (Unarmored Defense,
  Fighting Style: Defense), and every spell effect (Mage Armor, Aid).

Under adr-0006 as written, "never hand-author a fact" would leave the prose-only
cases permanently manual.

**What the reference product does.** The owner treats D&D Beyond as the fidelity
target. Its rules engine was studied on 2026-09-23 in
`design-reference/markup/media.dndbeyond.com/packages/rules-engine/es/`.

- Every entity definition carries a list of **modifiers**. Each modifier has a
  `type`, a `subType` (the target), a value, dice, or an ability that supplies the
  value, a free-text `restriction`, `requiresAttunement`, and its origin.
- The engine code knows only that vocabulary; no code mentions individual content.
- The modifier data is authored by D&D Beyond's own staff in its own database.
  It isn't derived from the rulebook text automatically.

## Decision

**Mechanics that 5etools only describes in prose are encoded in a curated overlay
of modifier files, versioned in this repository.**

- **Layout:** `content/dnd-5e/modifiers/<kind>/<slug>.json`, one file per 5etools
  entity (condition, class feature, spell, …), keyed by the same slug ingestion
  already produces.
- **Contents:** only a `modifiers` list in this project's own
  `Dnd5eModifierType`/`Dnd5eModifierTarget` vocabulary, plus the source book and
  page the encoding was read from. It never contains names, descriptions or any
  other text; those still come from 5etools.
- **Merge:** ingestion merges the overlay into the converter's output for that
  entry. An overlay file whose slug matches no ingested entry fails ingestion.
- **Precedence:** a structured 5etools field wins over the overlay for the same
  target. The overlay fills gaps; it never contradicts real data.
- **Growth:** entries are added one at a time, as real characters use them. The
  first batch covers the 14 conditions, the 6 exhaustion levels, and whatever the
  Stage C audit characters need.
- **Rest-bound endings:** an overlay entry records that an effect ends on a short
  or long rest **only when the source's own text says so**. See Stage C3's
  "Active effects".

## Amendment — choices, 2026-09-23

Approved with `systems/dnd-5e/features/character-creation.md`'s C1a plan, after comparing with
D&D Beyond's own `Choice` engine, where choices are also hand-authored data on
class features.

- **Choices, not just modifiers.** The overlay may also encode **choices** that
  5etools only describes in prose: Rogue Expertise, the Battle Master's *Student
  of War* tool, and the Draconic Bloodline's dragon ancestor.
- **Shape:** a `choices` list in the grant shape, with a D&D Beyond-style `type`
  (e.g. `EXPERTISE`), under the same citation rule (source book and page, no
  text).
- **Location:** because a file now holds more than modifiers, the overlay lives
  in `content/dnd-5e/mechanics/<kind>/<slug>.json`, each file holding
  `{modifiers, choices}`. Nothing had been written under the original
  `modifiers/` path, so nothing moves.

**In use since 2026-09-23 (C1a).** There are three choice files:

- `class-features/rogue-expertise.json`;
- `subclass-features/fighter-battle-master-student-of-war.json`;
- `subclass-features/sorcerer-draconic-dragon-ancestor.json`, whose options come
  from the feature's own 5etools table via `optionsFromTable`, so none are typed
  by hand.

**Modifiers in use since 2026-09-24 (C3a).** The overlay now also targets
**feats** (`mechanics/feats/`) and **optional features**
(`mechanics/optional-features/`), each by name plus 5etools source code, so slug
disambiguation doesn't affect them. The "must match" check runs per target type.
The first modifier files are:

- Fighter's Extra Attack, (2) and (3): `SET EXTRA_ATTACKS`;
- Draconic Resilience: `BONUS HIT_POINTS_PER_LEVEL` counting Sorcerer levels,
  and `SET_BASE UNARMORED_ARMOR_CLASS 13`;
- the Tough feat: `BONUS HIT_POINTS_PER_LEVEL 2`;
- Fighting Style: Defense: `BONUS ARMORED_ARMOR_CLASS 1`.

**Conditions and spells since 2026-09-24 (C3c, C3d).** `mechanics/conditions/`
(with exhaustion's per-level `levels`) and `mechanics/spells/` are also targeted
by name plus source. A spell file holds an `effect` (`modifiers`, `endsOnRests`
exactly as the text names them, `durationText`), which becomes an active effect
when the spell is cast. The first spell file is Mage Armor.

## Rationale

- **It is how the reference product works.** D&D Beyond's modifiers are curated
  data, not parsed prose. Matching that design keeps this project's behavior
  explainable against the fidelity target.
- **It keeps adr-0006's intent.** adr-0006 exists so that no fact is guessed. The
  overlay holds no facts, only an encoding of rules text that 5etools itself
  supplies. Each entry cites book and page, so it can be checked against that
  text.
- **Alternatives rejected:**
  - Parsing 5etools prose: fragile, and silently wrong when a sentence doesn't
    match the expected pattern.
  - Structured data only: would leave Mage Armor, Aid and every condition as
    manual adjustments, which is exactly D&D Beyond's own gap.

## Consequences

- A new, small, reviewable kind of hand-maintained file exists. Its correctness is
  checked by Stage C's data-fidelity audit, like everything else on the sheet.
- The modifier vocabulary (`Dnd5eModifierType`, `Dnd5eModifierTarget`) is an enum.
  It grows by adding values, never by content-specific code (Open/Closed), and
  uses D&D Beyond's `ModifierTypeEnum`/`ModifierSubTypeEnum` names as its naming
  reference.
- `systems/dnd-5e/features/5etools-ingestion.md` gains an overlay-merge step when C3's first
  slice lands.

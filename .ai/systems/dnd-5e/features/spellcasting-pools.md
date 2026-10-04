# Feature — Spellcasting pools: Pact Magic and feature-granted casting

Status: **done.** P1 built 2026-09-28, P2 built 2026-10-03. Next in the queue:
slice 9c of `sidebar-fidelity-and-customization.md`.

## Why

The owner reported (2026-09-28) that Mez test, a Warlock 3 / Fighter 3 (Battle
Master), had no spell slots, and that casting Mage Armor did nothing: the
sidebar opened with no Cast control.

- The sheet had no Pact Magic: the materializer skipped pact casters (C1c,
  `open-items.md`).
- Mage Armor (Armor of Shadows) and False Life (Fiendish Vigor) are cast at
  will, and Burning Hands (Fire Genasi) once per long rest, without a slot.
  Their frequency was only text in the spell's notes, and the Cast control
  only appears when a slot can pay.

## What D&D Beyond does

Observed on the owner's Warlock, `characters/118003256` (Mez Feldrir, read
only; her Fighter has a spellcasting subclass there, so she also has regular
1st-level slots and a second caster in the Spells header).

- **Spells tab:** each level heading ends with its slot groups
  (`ct-spells-level-casting__slot-group`): the boxes, then a Roboto Condensed
  700 13px uppercase label, 5px from them: "SLOTS" for the regular pool,
  "PACT" for Pact Magic.
- The Pact Magic level's section ("2ND LEVEL" … "PACT") lists the pact class's
  own lower-level spells too (Armor of Agathys, Command, Hellish Rebuke), each
  with a blue badge carrying its own level ("1st") on its Cast button
  (`ct-spells-spell__scaled`: `#1C9AEF`, 1px white 50% border, 2px radius,
  `1px 3px`, "1" 8px and "st" 6px, 8px above the button) and its effect scaled
  (Hellish Rebuke 3d10). Spells from other sources (species, invocations)
  stay out.
- **Cast control** (`.ct-spell-caster`, measured on Copy of Raya's Magic
  Missile and Mez's Command): right under the subtitle, before Customize, with
  10px margin and 10px padding above.
  - "CAST" (Roboto Condensed 700 13px) and one theme-filled button (min 62×28,
    Roboto Condensed 10px uppercase, 3px radius) with a count badge 9px above
    its left edge (white, 1px theme border, 3px radius, 10px bold).
  - "LEVEL" and a − / + stepper (28×28, 3px radius; theme-filled when enabled,
    `#EAEAEA` with `#777` text when disabled) around the level ("1st", Roboto
    13px, 30px wide).
  - The button reads **"Spell Slot"** at a regular slot's level and **"Pact
    Slot"** at the Pact Magic level; the stepper walks every level with a pool.
  - Below: the effect ("1d4+1 [icon] Damage", Roboto 14px).
- **Row's first column** (for P2): "CAST" for a spell paid with a slot, "USE"
  for a limited one (Burning Hands, "1/LR" in its notes), "AT WILL" for an
  invocation's spell (Disguise Self).

## Slices

### P1. Pact Magic — ✓ built 2026-09-28

- **Data:** `Dnd5eSpellSlotLevel` gained `pact` and `className`; the Pact Magic
  pool sits in `spellSlots` beside the regular pools (it may share a level).
  The materializer reads the class's `pactSlotsByLevel` (5etools, already in
  the catalogue) at its level and keeps the used count across re-applies.
- **Rules:** a short rest refills the pact pool (a long rest refills every
  pool). A pact slot casts any spell of its level or lower, always at its own
  level (PHB multiclass rule).
- **API:** `POST …/spell-slots/{level}/consume|restore?pact=true`;
  `CastSpellRequest.pact`; `SpellSlotLevelResponse` carries `pact` and
  `className`.
- **Web:** slot groups with "Slots" / "Pact" labels; the pact level section
  with scaled rows (`SpellRow.castLevel`, `ScaledLevelBadge`); `SpellCast`
  restyled to D&D Beyond's measurements, with "Spell Slot" / "Pact Slot" by
  level. A spell's Cast control now sits before Customize.
- **Existing sheets:** Mez test (the only pact caster) got her pool by a
  one-off update equal to what the materializer produces (2 slots of 2nd
  level, class Warlock); any other sheet gets it when its build is re-applied.

### P2. Feature-granted casting without a slot — ✓ built 2026-10-03

Plan as approved:
- The frequency becomes data on the spell (at will; N per short rest; N per
  long rest) with a used count, instead of text in the notes.
- Cast without a slot: at will always; a limited one spends a use, regained on
  its rest. The row's first column reads "AT WILL" / "USE" / "CAST" as on D&D
  Beyond. A self-only grant (Armor of Shadows) fixes the target to Self.
- Existing sheets need their build re-applied; the owner's play state is saved
  first and restored after.

As built:
- **Data:** `Dnd5eSpell.usage` (`Dnd5eSpellUsage`): mode `AT_WILL` or
  `LIMITED`, max and used uses, recharge, shared `pool`, `castLevel`,
  `selfOnly`. Null for a spell cast with slots. The notes text ("1/day") is
  kept as it was.
- **Where it comes from** (`Dnd5eSpellPlanner`, from 5etools
  `additionalSpells`), for leveled spells only (cantrips are always at will):
  - a plain `innate` grant, or `will`, is at will (Armor of Shadows);
  - `daily` is limited per long rest, `rest` per short or long rest;
  - "1e" gives each spell its own uses; "1" over several spells shares one
    pool between them;
  - `ritual` and `resource` stay notes only;
  - "#2" sets `castLevel`;
  - `selfOnly`: the granting feat's or optional feature's description says
    "cast <spell> on yourself". This is read from 5etools text, not
    hand-written.
- **Rules** (`Dnd5eSheetMutator.castSpell` with slot level 0):
  - a limited spell spends a use, and every spell in its pool spends one too;
    with no uses left, nothing happens;
  - an at-will spell spends nothing;
  - `selfOnly` forces the effect onto the caster;
  - a short rest restores `SHORT_OR_LONG_REST` uses, a long rest restores all.
  - A re-applied build keeps the uses already spent.
- **API:** `SpellResponse.usage`.
- **Web:**
  - `SpellRow` shows "AT WILL" or a "USE" button.
  - `SpellCast`'s slotless control:
    - "CAST", then "USE" (70×28, measured on D&D Beyond) or the at-will
      button;
    - "LEVEL" fixed, with no stepper;
    - for a limited spell, its frequency 10px below ("Once per Long Rest");
    - no target choice for a self-only spell.
- **Existing sheets:** re-applied with the new dev runner
  `bootRun --args='--rematerialize --player=<username>'`. It re-applies each
  character's own stored build and keeps play state.
  - The four test characters were backed up first (`--print-sheets`).
  - Every computed value was compared before and after (abilities, AC, HP,
    saves, skills, resistances, effects, slots, items, coins, hit dice): all
    identical.
  - Mez test now has Burning Hands 1/long rest, and Mage Armor and False Life
    at will, self-only.
- **Checked live** against D&D Beyond (Mez Feldrir, read only):
  - the row labels;
  - the Cast control's text, button size and line spacing.
- **Deviation awaiting the owner:** D&D Beyond shows "At Will" as plain text
  in the Cast control. Here it is a button, so casting can start the spell's
  lasting effect (Mage Armor's AC), as the rest of this app's casting does.
- **Not verified on D&D Beyond:**
  - the wording for more than two uses, and for short-rest spells ("N times
    per Short Rest" is assumed);
  - what D&D Beyond does after the last use (here: disabled), because using
    it would change the owner's D&D Beyond character.

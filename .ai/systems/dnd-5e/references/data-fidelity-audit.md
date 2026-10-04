# D&D 5e — Stage C data-fidelity audit

The gate defined in `systems/dnd-5e/features/character-creation.md` ("This stage ends with a
data-fidelity audit"):

- every value on the vitals zone and on all six tabs is traced to a real
  5etools fact or a resolved build choice;
- every Target outcome scenario is walked on each character.

Stage D2 does not start until the findings below are resolved or explicitly
accepted by the owner.

## Method (first pass, 2026-09-24)

- **Characters:** Aria Emberfall (Mountain Dwarf Folk Hero, Fighter 20 Battle
  Master), Liriel Moonwhisper (High Elf Urchin, Wizard 5 Evocation) and Vex
  (Changeling MPMM Urchin, Rogue 4 Thief / Sorcerer 3 Draconic). All three are
  built from `content/dnd-5e/builds/`.
- **Values:** `bootRun --args='--print-sheets=<dir> --player=test
  --server.port=0'` (`CharacterSheetPrintRunner`, dev profile). It writes each
  character's calculated sheet, exactly as the API returns it, plus the stored
  sheet. Running on a random port leaves a running API alone.
- **Checks:** each value is compared with the PHB/MPMM rule, the build's choices,
  and the recorded provenance (C2a). Disputed facts are re-read from the raw
  5etools data under `tools/5etools-data/`.

## Traced and correct

| Area | Aria | Liriel | Vex |
| --- | --- | --- | --- |
| Ability scores (base + species + ASIs/feats) | STR 20, DEX 16, CON 20 (incl. Durable, Dwarven Fortitude) | DEX 16, INT 18 | DEX 18, CHA 16 |
| Proficiency bonus | +6 | +3 | +3 |
| Max HP (per-level dice + Con + features) | 264 (incl. Tough 40) | 27 | 45 (incl. Draconic Resilience 3) |
| Speed, senses | 25, darkvision | 30, darkvision | 30, none |
| Saving throw proficiencies (starting class only) | STR, CON | INT, WIS | DEX, INT |
| Skills (class picks, species, background, expertise) | Perception, Intimidation, Animal Handling, Survival | Perception (Keen Senses), Stealth, Sleight of Hand, Arcana, Investigation | 8, Stealth expertise +10 |
| Passive senses | 17 / 11 / 10 | 14 / 11 / 17 | 14 / 14 / 13 |
| Armor, weapon, tool proficiencies and languages | ✓ (4 tools: artisan, dwarf, Student of War, vehicles) | ✓ (Elf Weapon Training; Draconic from Extra Language) | ✓ except the prose-only grants below |
| Spellcasting | — | Wizard +7 / DC 15; 4 cantrips; 9 prepared (INT 4 + level 5); a 14-spell spellbook; High Elf cantrip as its own caster | Sorcerer +6 / DC 14; 4 cantrips; 4 known |
| Spell slots | — | 4 / 3 / 2 | multiclass caster level 3 → 4 / 2 |
| Class features and counters | 9 maneuvers, 6 × d12 superiority dice, Second Wind 1, Action Surge 2, Indomitable 3, 4 attacks per action | Arcane Recovery 1, Evocation Savant, Sculpt Spells | Font of Magic 3, 2 metamagic, Dragon Ancestor: Red, Cunning Action, Shapechanger |
| Starting equipment and coins | Folk Hero + Fighter picks, 10 gp | Urchin + Wizard picks, 10 gp | Urchin + Rogue picks, 10 gp |
| Hit dice | d10 × 20 | d6 × 5 | d8 × 4 + d6 × 3 |

**Target outcome scenarios.** Checked live during C3/C2, on the characters
named:

1. Level implies everything below: all three.
2. Equipment changes values: attuning a Cloak of Protection raised AC and every
   save (Aria, C3e). **Partly open**, see F3.
3. Lasting spells: Mage Armor on Liriel (C3d), with manual end, rest survival,
   and the Self/Ally choice.
4. Conditions: Poisoned, Restrained and exhaustion (Aria, C3c).
5. Every changed value explains itself: breakdowns and roll-mode sources (C2a,
   C3c).
6. Rolls honor forced modes: `2d20kl1` under Poisoned (Aria, C3c).

## Findings

| # | Finding | Characters | Evidence | Proposed resolution |
| --- | --- | --- | --- | --- |
| F1 | **Starting equipment is added unequipped**, so no weapon rows appear on the Actions tab and armor doesn't count. Aria shows AC 13 instead of 19 (Chain Mail + Shield + Defense). | all | Every starting item has `equipped: false`; `attacks` is empty. | **Owner decision:** equip starting armor, shield and weapons when a build is applied, or leave equipping to the player. |
| F2 | **No Unarmed Strike.** It exists only as a hand-authored attack, and built characters have none. It is a rule every creature has (PHB 195), and D&D Beyond always lists it. | all | `attacks` is empty on every built sheet. | Derive it in the calculator: 1 + STR modifier bludgeoning, proficient. |
| F3 | **Armor side effects aren't applied.** Heavy armor below its Strength requirement doesn't lower speed by 10 ft. Armor with stealth disadvantage doesn't mark Stealth. Both are part of Target outcome scenario 2. | Aria (Chain Mail: STR 13, stealth disadvantage) | `strengthRequirement` / `stealthDisadvantage` are stored but never read. | Add them as C3 modifiers from equipped armor: `SPEED` −10 and `DISADVANTAGE` on Stealth checks, sourced to the armor. |
| F4 | **Prose-only grants are missing.** Vex lacks Thieves' Cant (Rogue 1) and **Draconic** (Dragon Ancestor: "You can speak, read, and write Draconic"). | Vex | 5etools has these only in the feature's text. | adr-0007 overlay grant data (language grants). |
| F5 | **Situational save notes are missing.** Dwarven Resilience ("advantage on saving throws against poison") and Fey Ancestry ("advantage … against being charmed") show nowhere. D&D Beyond lists them under Saving Throw Modifiers. | Aria, Liriel | The C3 design's `restriction` modifiers, listed as notes and never summed, were not built. | Add `restriction` to the modifier model and list those notes under the saves grid (the C3c summary line already has the spot). |
| F6 | **A duplicate tool proficiency isn't replaced.** Vex gets Thieves' Tools from both Urchin and Rogue. The PHB (p. 125) lets the player pick another proficiency of the same kind instead. | Vex | One Thieves' Tools entry; no replacement choice. | **Owner decision:** offer the replacement as a build choice, or accept. |
| F7 | Background grants are labelled "Background" rather than the background's name. | all | C2a provenance. | Use the background's name in the planner. |
| F8 | Scenario 3's second example (Aid raises the HP maximum) has no overlay. No target character knows Aid. | — | Only `mage-armor.json` exists. | Write overlays as characters use the spells (the agreed rule), or add Aid now to close the scenario literally. |

## Owner decisions (2026-09-24)

- **F1 — accepted as is.** Items are added unequipped; equipping is always the
  player's action, including for the dev characters rebuilt on every apply. To
  check AC or attacks on Aria, Liriel or Vex, equip their items first. This is
  intended behavior, not a defect.
- **F6 — fix.** When a proficiency would be granted twice, the build offers a
  replacement choice of the same kind (PHB p. 125).
- **F8 — on demand.** The owner asked whether Liriel could learn Aid from a
  scroll. She can't: Aid is not on the wizard list (5etools: Cleric, Paladin,
  Artificer; optional Bard, Ranger), and a wizard copies only wizard spells
  (PHB p. 114). Scenario 3 stays demonstrated by Mage Armor, and the Aid overlay
  is written when a character that can cast it exists.

## Fix slices (proposed order)

1. **F2 + F3 — derived combat values: done (2026-09-24).**
   - **Unarmed Strike:** the calculator adds it last unless one is
     hand-authored. It is proficient, uses Strength, and deals 1 + STR
     bludgeoning with no dice (PHB 195; the old hand-authored one dealt STR
     only).
   - **Worn armor:** it adds `BONUS SPEED −10` below its Strength requirement
     and `DISADVANTAGE` on the new `STEALTH_CHECKS` target, both sourced to the
     armor. `Dnd5eModifiers.speed` now applies `BONUS` before `SET`/`HALVE`.
   - **Known limit:** a dwarf's "speed is not reduced by wearing heavy armor"
     isn't modeled. Aria meets Chain Mail's STR 13 anyway.
   - **Tests:** `Dnd5eArmorAndUnarmedTest` (4). The re-print shows Aria's
     Unarmed Strike at +11 / 6, and Liriel's and Vex's at +2 / 0.
2. **F5 — situational notes: done.**
   - `Dnd5eModifier.restriction`: a situational modifier is never summed and
     forces no roll mode.
   - `Dnd5eModifiers.rollNotes()` → `VitalsZone.rollNotes` (`RollNote`).
   - The saves panel lists the notes below the grid, D&D Beyond style: "[A]
     against poison (Dwarven Resilience)".
   - New species overlays: `mechanics/species/dwarf-phb.json` (Dwarven
     Resilience) and `elf-phb.json` (Fey Ancestry).
   - Tests: `Dnd5eRollNotesTest` (2), `SavingThrowsPanel.test.tsx` (2).
   - The panel's misplaced doc comment was moved back onto `SavingThrowsPanel`.
3. **F4 + F7 — grants: done.**
   - The overlay now carries `grants.languages`; `copyMechanics` copies it, and
     the planner grants those languages with the feature as source.
   - Files: `class-features/rogue-thieves-cant.json` (PHB 94); Dragon
     Ancestor's overlay gains `draconic`.
   - Language labels try `<key>-phb` before falling back to the title-cased key,
     so they read "Thieves' Cant".
   - Background grants carry the background's name ("Urchin"), and so does the
     "still offered" label on a background-granted option. The
     `BACKGROUND_SOURCE` constant is gone.
4. **F6 — duplicate proficiencies: done.**
   - The planner's `grantOrReplace`: a skill or tool already granted by another
     source (fixed or chosen) opens a choice "Replace duplicate X (already from
     Y)", offering only ungranted ones. With nothing to offer, it just keeps the
     grant.
   - Tests: two new planner cases.
   - Vex's build now has one pending choice,
     `class:rogue:tools:replace:thieves-tools`, which waits for the owner's
     pick.

Backend `:apps:api:check` is green at 416 tests; `npm test` (33) passes. The
classes, subclasses and species were re-ingested and imported.

## Second pass (2026-09-24)

- **Setup:**
  - the owner picked **Poisoner's Kit** for Vex's duplicate Thieves' Tools,
    now recorded in `builds/vex.json`;
  - the three builds were re-applied, which resets play state;
  - Liriel's Mage Armor and spent 1st-level slot were restored directly in the
    database from the saved first-pass sheet.
- **`--print-sheets` again:**
  - F2: Unarmed Strike on all three (Aria +11 / 6; Liriel and Vex +2 / 0);
  - F4: Vex gains Thieves' Cant (Rogue 1) and Draconic (Draconic Bloodline 1);
  - F5: Aria notes "against poison (Dwarven Resilience)", and Liriel notes
    "against being charmed (Fey Ancestry)";
  - F6: Vex has Poisoner's Kit (Rogue 1) and keeps Thieves' Tools
    (Expertise);
  - F7: sources read "Urchin" and "Folk Hero".
  - F3 only shows with armor equipped, which is a manual step (F1). It is
    covered by `Dnd5eArmorAndUnarmedTest`.
- **Every finding is resolved or accepted.**

## Live check and close (2026-09-24)

- **Compared against D&D Beyond** (Helga, a dwarf):
  - their saves line reads "[A] against Poison", with no source;
  - their Unarmed Strike reads "Melee Attack · 5ft. Reach · +4 · 2", which
    confirms 1 + STR (Helga STR 13).
- **Aligned to match:**
  - the note shows only the restriction; the source moved into the icon's
    tooltip;
  - the Unarmed Strike subtitle is now "Melee Attack". The Actions tab
    recognizes it by its `unarmed-strike` key, so it still sorts last with the
    unarmed icon.
- **Verified live:**
  - Aria: "[A] against poison", "Unarmed Strike · Melee Attack · 5 ft. · +11 ·
    +6" as the last row;
  - Liriel: AC 16 (Mage Armor), "against being charmed", Draconic;
  - Vex: Poisoner's Kit, Thieves' Cant, Draconic.
- **Stage C audit closed.** Stage D2 is unblocked (`roadmap.md` phase 11).

The audit is re-run with `--print-sheets` after they land.

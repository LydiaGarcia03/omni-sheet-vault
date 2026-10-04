# Feature — Character creation, and the rules engine it depends on

`roadmap.md` phase 11's own specification, per its "write this before planning it"
instruction. Scope grew past the phase's original one-line description ("the guided
flow... levelling up... portrait upload") once the current sheet's own data sources
were audited — see "Why this is bigger than phase 11 looked" below.

## Target outcome

Owner statement, 2026-09-23. The end state this document works toward is a
**conversational pseudo-creation**: the owner names a build ("rebuild Aria as a
level 5 High Elf Wizard with the Sage background"). The assistant then asks every
choice that build implies, one question per choice, with options pulled verbatim
from real 5etools data. The result replaces the dev character's sheet with a
generated one, not a hand-typed one.

The generated sheet must be **living**, not a snapshot. Every scenario below is a
checklist item for Stage C's data-fidelity audit:

1. **A level implies everything below it.** A level 5 Wizard gets every class
   feature from levels 1–5, level 5 spell slots, a +3 proficiency bonus, and hit
   points from five hit dice. The build also asks for the subclass at the class's
   subclass level and for the level 4 choice between an ability score increase and
   a feat.
2. **Equipment changes derived values.** Donning or doffing armor or a shield, or
   attuning an item like a Ring of Protection, recomputes AC and saving throws.
   Heavy armor whose Strength requirement isn't met lowers speed. Armor with
   stealth disadvantage shows it on the Stealth skill.
3. **A lasting spell changes the sheet while it lasts.** Mage Armor sets base AC
   to 13 + Dex while no armor is worn. Aid raises the HP maximum. The effect ends
   on a manual "end", when another concentration spell is cast (if it is itself a
   concentration spell), or on a rest **only if its own text says so**. Effects shorter
   than 1 hour (Shield, Haste, Bless) are deliberately not tracked; see C3's
   "Active effects".
4. **Conditions show their mechanics.** Poisoned puts a disadvantage marker on
   attack rolls and ability checks, skills included. Prone, Restrained and every
   exhaustion level (disadvantage on checks, halved speed, disadvantage on attacks
   and saves, halved HP maximum, speed 0) do the same for what they affect.
5. **Every changed value explains itself.** AC shows contributions such as "Mage
   Armor 13 · Dex +3 · Ring of Protection +1", and a roll-mode marker names its source
   ("Disadvantage — Poisoned"). `CalculatedValue`/`Contribution` already give AC
   this shape; the effects engine extends it to every value and to roll modes.
6. **Rolls honor it.** `RollService` applies the forced advantage or disadvantage
   the sheet reports. Its current overloaded-Strength special case becomes one
   effect among many instead of a hardcoded exception.

### What already exists toward it

- `CalculatedValue` + `Contribution`: a derived value already carries its
  labelled sources.
- `Dnd5eSheetCalculator.armorClass()`/`.attacks()` already derive from an
  equipped item's `itemKind`. Attunement and item-granted spells already work.
- Conditions and exhaustion are stored (`activeConditions`, `exhaustionLevel`)
  but **mechanically inert**: nothing reads them to change a value or a roll mode.
- Forced disadvantage exists in exactly one place, hardcoded in
  `RollService.isOverloadedStrengthRoll`.
- The sheet has **no notion of an active spell or of concentration**. Casting only
  spends a slot and rolls.

## Status: blocked

**Direct owner decision, 2026-09-22: Stage D2 (the guided creation flow in the UI —
phase 11 proper) does not start until Stages A and C below are both complete.**
Stage D1 (assistant-driven rebuild) is exempt because it produces the characters
Stage C's audit checks. The sheet
phase 11 would build characters *into* has to be verified 100% correct first,
against two independent standards at once:

- **D&D Beyond's own live sheet** — visual and behavioral fidelity. Already the
  standing rule since phase 10 (`systems/dnd-5e/sheet-ui.md`'s "Verifying fidelity against
  D&D Beyond"), unchanged here.
- **Real 5etools data** — mechanical correctness. No value the sheet shows may
  trace back to a hand-typed guess or a mocked placeholder; every number and fact
  must be traceable to either a real 5etools entry or a real player choice.

Phase 10 only ever checked the first standard. This document's Stage C is what
checks the second, and nothing in phase 10 or the work since satisfies it — see the
audit table below.

## Why this is bigger than phase 11 looked

`domain-model.md` already names the concept phase 11 needs: **"Choice — a decision
offered during character creation (race, class, background), along with what it
grants. Produced by the system implementation so the UI can show consequences
before the choice is committed."** Nothing implements it. `Dnd5eSheet` today has no
species/class/background reference at all — every ability score, proficiency,
speed, sense, and known spell is a flat, independently-authored value
(`DevCharacterSeeder.java`'s hand-typed JSON, `ground-rules.md`'s "no business rules
in the frontend" not withstanding — there are barely any *backend* rules governing
these fields either). A guided creation flow with nothing behind it to compute
consequences from would just be a form that writes the same kind of flat, hand-typed
JSON `DevCharacterSeeder` already does, by hand, through a nicer UI — not what "show
the consequences of each choice" or "the sheet is fully dynamic" ask for.

Two things have to exist before a choice can have a shown consequence:

1. **The real facts to compute from** (Stage A — 5etools content this project
   doesn't have yet: classes, species, full backgrounds, creatures).
2. **A mechanism that derives sheet values from those facts plus equipped items,
   instead of storing them flat** (Stage C — extends work already proven possible:
   `Dnd5eSheetCalculator.armorClass`/`.attacks()` already derive AC and the attack
   list from an equipped item's own `itemKind`, confirmed working live 2026-09-22
   once `DevCharacterSeeder`'s own data was corrected to exercise it. The same
   "derive from a real fact, not a stored flat value" idea has to cover race
   traits, class features, and feats too).

## Current state of 5etools-backed content — 2026-09-23

| Content kind | `CatalogueEntryKind` | Real 5etools data | Converter |
| --- | --- | --- | --- |
| Spells | `SPELL` | 570 files | `SpellConverter` — done |
| Items | `ITEM` | 1,997 files | `ItemConverter` — done |
| Feats | `FEAT` | 227 files | `FeatConverter` — done |
| Classes | `CLASS` | 14 files | `ClassConverter` — done (A1) |
| Subclasses | `SUBCLASS` | 130 files | `SubclassConverter` — done (A1) |
| Backgrounds | `BACKGROUND` | 101 files | `BackgroundConverter` — done (A3); the hand-typed Soldier is replaced |
| Optional features (Fighting Styles, Invocations, …) | `OPTIONAL_FEATURE` | 163 files | `OptionalFeatureConverter` — done (A1b) |
| Species (with subspecies and variants nested) | `SPECIES` | 140 files | `SpeciesConverter` — done (A2) |
| Creatures | `CREATURE` | 0 | none — kind reserved since adr-0005, never used |

Every row below "Feats" is Stage A's own scope.

## Stage A — Finish 5etools ingestion (prerequisite)

Same shape as the three already-done converters — see `systems/dnd-5e/features/5etools-ingestion.md`
for the pipeline itself (shared, kind-independent: a 5etools loader, `TagMarkupStripper`,
`FiveEToolsSourceClassifier`) and `FeatConverter.java` for the most recent worked
example of adding a new kind without touching the shared pieces. Each of the four
below is its own slice: its own field-mapping table (written once real 5etools sample
files are in hand, not guessed up front), its own changelog entry, its own
`./gradlew :apps:api:ingest5etools --args="--kind=<x>"` run against the owner's real
`tools/5etools-data/`, the same "re-run produces byte-identical output, zero
validation failures" done-when spells/items/feats already met.

- **A1. Classes and subclasses.** New `CatalogueEntryKind.CLASS`. Needs: hit die,
  saving throw proficiencies, starting proficiencies (armor/weapons/tools/skills,
  including any "choose N of" list), a level-by-level feature table (1–20), the
  spellcasting table for a casting class (slots per level, cantrips known,
  `castingType` per `systems/dnd-5e/sheet-ui.md`'s existing `KNOWN`/`PREPARED` split), and
  the subclass choice point (which level, and the subclass's own feature table).
  **Blocks everything else in this document** — without it there is no source for
  a class's own granted proficiencies/features/spell progression to derive from.
- **A2. Species.** New `CatalogueEntryKind.SPECIES` (this app's own established term
  for the concept PHB itself calls "race" — `systems/dnd-5e/sheet-ui.md`'s Features and
  Traits filter chips already say "species traits", not "race traits"). Needs:
  ability score increase(s), size, speed, traits (Darkvision and other fixed senses
  — reuses `Dnd5eSenseType`, already modeled), any species-granted spells, and
  subspecies where 5etools models one (e.g. Hill Dwarf vs. Mountain Dwarf).
- **A3. Backgrounds, the full set.** Reuses the existing `BACKGROUND` kind — no new
  enum value. Replaces the single hand-typed Soldier file. Needs: skill/tool/language
  proficiencies, starting equipment, the background's own feature, and the
  personality-trait/ideal/bond/flaw suggestion tables the Background tab's Text
  Field mold already renders (`content/dnd-5e/backgrounds/`'s existing shape for
  Soldier is the reference for what a real file must match).
- **A4. Creatures.** Reuses the existing `CREATURE` kind. Needs: a stat block
  (ability scores, AC, HP, speed, senses, CR, actions) in the shape
  `ExtraStatBlockView.tsx`/`Dnd5eExtraStatBlock` already renders — this is what lets
  a familiar, mount, or summoned creature added to Extras carry a real stat block
  instead of the hand-typed one `DevCharacterSeeder` gives Aria's own Warhorse
  today.

### A1 — classes and subclasses (built 2026-09-23)

Built from real files in `tools/5etools-data/data/class/class-*.json`. Run it with
`./gradlew :apps:api:ingest5etools --args="--kind=class"` and `--kind=subclass`.
Output goes to `content/dnd-5e/classes/` and `content/dnd-5e/subclasses/`.

**Source shape.** Each file holds five arrays: `class`, `subclass`, `classFeature`,
`subclassFeature` and `_meta`.

- A class's `classFeatures` list references features by a
  `"Name|Class|ClassSource|Level|FeatureSource"` key. An empty `ClassSource`
  defaults to `PHB`, and an empty `FeatureSource` defaults to the class source.
- An entry with `gainSubclassFeature: true` marks a subclass level.
- A subclass's `subclassFeatures` list uses
  `"Name|Class|ClassSource|Subclass|SubclassSource|Level|FeatureSource"`.
- Nested `refClassFeature`/`refSubclassFeature` blocks point at further features.

**Scope filter** (`FiveEToolsClassData`). The source code alone isn't enough:

- Each file carries both editions of the class. The 2024 one is tagged
  `edition: "one"`, and not always with an `X`-prefixed source: the 2024
  Artificer's source is `EFA`. Classes tagged `edition: "one"` are dropped.
- 2024 subclass copies keep their **old** source but point at the 2024 class
  (142 entries, e.g. War Magic [XGE] → Wizard/XPHB). A subclass is kept only when
  its parent class is kept.
- Sidekick classes (`isSidekick: true`, TCE) are not player classes and are
  dropped.
- **Unearthed Arcana is imported** (owner decision, 2026-09-23). Here that means
  the Mystic class and its 6 subclasses. Like collab sources, UA will be excluded
  or included per character by the character-creation source toggle (adr-0006).
- **Result: 14 classes and 130 subclasses.** The 14 classes are the 12 PHB ones,
  the TCE Artificer, and the UA Mystic. Every referenced feature resolved, and
  every referenced item slug exists in `content/dnd-5e/items/`.

**New catalogue kinds.** `CLASS` and `SUBCLASS`. `catalogue_entries.kind` is plain
`text` with no check constraint, so no migration was needed.

- Slugs are the class name (`wizard`) and `<class>-<subclass short name>`
  (`fighter-eldritch-knight`).
- Same-named entries from different sources get source-suffixed slugs, as in
  every kind (`ground-rules.md`, "Catalogue content").

**`CLASS` data** (`ClassConverter`). Ability keys use the sheet's own names
(`intelligence`), and skill keys use its camelCase keys (`sleightOfHand`).

| Field | 5etools source | Notes |
| --- | --- | --- |
| `hitDie` | `hd.faces` | |
| `savingThrows` | `proficiency` | |
| `proficiencies` | `startingProficiencies` | See "Proficiency shape" below |
| `startingEquipment` | `startingEquipment` | `text`, `goldAlternative`, `additionalFromBackground`, and `groups[]` of `options[]`. Each option has a `label` (`a`/`b`/`c`; `null` means a fixed grant) and `grants` of `{itemSlug \| equipmentType, quantity}` |
| `multiclassing` | `multiclassing` | `allOf` ability minimums (all required), `anyOf` (any one suffices), and `proficienciesGained` in the proficiency shape. `null` for the Mystic |
| `spellcasting` | `spellcastingAbility`, `casterProgression`, `preparedSpells`, `cantripProgression`, `spellsKnownProgression`, `spellsKnownProgressionFixed`, `classTableGroups` | See "Spellcasting shape" below |
| `tableColumns` | Every `classTableGroups` column with plain `rows` | `{label, valuesByLevel}` as PHB display strings. Dice render as `1d6`, bonuses as `+2`, speed bonuses as `+10 ft.` (`—` for 0). Any other cell shape fails ingestion |
| `optionalFeatureProgressions` | `optionalfeatureProgression` | `{name, featureTypes, countByLevel[20]}`. The sparse object form (`{"3":1}`) is carried forward |
| `subclassTitle`, `subclassLevel` | `subclassTitle`, first `gainSubclassFeature` | |
| `abilityScoreImprovementLevels` | Features named "Ability Score Improvement" | The only name-based rule |
| `features[]` | `classFeatures`, resolved | `{name, level, sourceBook, sourcePage, description, optional, grantsSubclassFeature, optionalFeatureOptions[]}`. `optional` is `isClassFeatureVariant` (TCE optional class features). `optionalFeatureOptions` holds the `refOptionalfeature` picks named in the text, as `{name, source}` |

**Proficiency shape** (`FiveEToolsProficiencies`, reusable by species and
backgrounds):

- `armor`;
- `weaponCategories`, `weaponItems` (item slugs), and `optionalWeaponCategories`
  (the Artificer's firearms);
- `notes` (the Druid's no-metal restriction);
- `toolAlternatives[]` and `skillAlternatives[]`, in the shared **grant shape**
  (changed 2026-09-23 with A2). The character gets exactly one alternative. Each
  alternative is `{fixed: [keys], choices: [{from, category, fromFilter, count,
  amount}]}`:
  - `from` lists the keys to pick among;
  - `category` is set instead for 5etools' `any`/`anyX` grants (`any`,
    `musicalInstrument`, `artisansTool`, `standard`, …);
  - `fromFilter` keeps 5etools' own filter string when that's how the options are
    given (e.g. "type=martial weapon").

**Spellcasting shape** (`FiveEToolsClassProgression`):

- `ability`;
- `casterProgression`: `full`, `1/2`, `1/3`, `artificer`, `pact`, or `null`;
- `preparedSpellsFormula`: `{levelDivisor, ability}`, parsed from
  `<$level$> [/ N] + <$abl_mod$>`. Any other formula shape fails ingestion;
- `cantripsKnownByLevel`, `spellsKnownByLevel`, `spellbookSpellsAddedByLevel`
  (the Wizard);
- `spellSlotsByLevel` (20 × up to 9);
- `pactSlotsByLevel`: `{slots, slotLevel}`, read from the Warlock table's "Spell
  Slots"/"Slot Level" columns.

**`SUBCLASS` data** (`SubclassConverter`):

- **Description:** the subclass's introduction. This is the prose of its
  top-level feature named after the subclass itself; that feature is not listed
  again as a feature.
- `classSlug` and `shortName`.
- `additionalSpells`: domain and oath spells, kept verbatim for C1.
- `spellcasting`: the same shape as a class's, for subclasses that cast on their
  own (Eldritch Knight, Arcane Trickster: `1/3`), otherwise `null`.
- `tableColumns` and `optionalFeatureProgressions`, e.g. Battle Master
  maneuvers.
- `features[]`: nested references are expanded in order.

**Descriptions.** `FiveEToolsEntries` holds the recursive entries flattening that
used to be private to `FeatConverter`.

- It also renders `abilityDc`/`abilityAttackMod` boxes in the PHB's wording
  ("Spell save DC = 8 + your proficiency bonus + your Intelligence modifier") and
  single `entry` children.
- Re-running feat ingestion produced byte-identical files, so feat output is
  unchanged.
- Tables inside feature text (e.g. Wild Magic Surge) are not rendered yet.

**Verified 2026-09-23** against the real data and the PHB:

- Fighter ASI levels are 4/6/8/12/14/16/19.
- Warlock pact slots: 1 × 1st at level 1, 2 × 3rd at level 5, 3 × 5th at
  level 11.
- Eldritch Knight: 2 1st-level slots at level 3.
- Fighting Style lists its 11 options.
- Re-running produces byte-identical output for all 144 files.
- Catalogue unit tests: `ClassConverterTest` and `SubclassConverterTest`.

**Pending:**

- **The import into Postgres (`--import-catalogue`) and the Testcontainers
  suites need Docker Desktop running.** It was off when this slice was built.
- ~~**The optional features themselves.**~~ Done in A1b, below.
- **Expertise, and any other choice that exists only in prose**, go to C1 with
  the adr-0007 overlay.
- **The multiclass spell-slot table** is computed in C1 from `casterProgression`.

**Found along the way, not fixed:**

- ~~**Feat slug collision.**~~ **Resolved the same day.** Same-named entries from
  different sources are now separate, source-suffixed entries in every kind; see
  `ground-rules.md`'s "Catalogue content".
- **Stale 2024 items.** `content/dnd-5e/items/` still holds 83 `XDMG` items from
  before the XDMG exclusion existed. The runner overwrites files but never
  deletes stale ones. The owner chose to keep the current catalogue as it is
  (2026-09-23).

### A1b — optional features (built 2026-09-23)

`OptionalFeatureConverter` reads `optionalfeatures.json` into 163
`OPTIONAL_FEATURE` entries in `content/dnd-5e/optional_features/`. Run it with
`--kind=optional_feature`. It covers Fighting Styles, Eldritch Invocations,
Maneuvers, Metamagic, Pact Boons, Arcane Shots, Elemental Disciplines, Artificer
Infusions, Runes, and the EFA Dragonmarked House renown picks. The scope filter is
the ordinary source rule: the 58 `XPHB` entries are dropped, and no same-name
collisions exist.

**Data:**

- `featureTypes`: 5etools' own codes, e.g. `FS:F`, `EI`, `MV:B`. A class or
  subclass `optionalFeatureProgressions[].featureTypes` lists the same codes, so
  "which options can this pick choose from" is a direct match.
- `optional`: `isClassFeatureVariant`, the TCE optional class features.
- `prerequisiteText`: rendered the way 5etools' own `render.js` does in its
  classic style ("7th level, hex spell or a warlock feature that curses",
  "15th level, Pact of the Chain", "eldritch blast cantrip"). It is also
  prepended to `description` as "Prerequisite: …", like feats.
- `prerequisites`: kept verbatim for C1 to evaluate. An unknown prerequisite key
  fails ingestion.
- `consumes`, e.g. `{name: "Superiority Die"}` or Sorcery Point amounts.
- `additionalSpells`, `senses` (`[{type, range}]`, e.g. Blind Fighting's 10 ft.
  blindsight), and `skillAlternatives`.
- `optionalFeatureProgressions`: Superior Technique grants one maneuver. The
  sparse `{"*": n}` form means every level.

**Verified.** All 95 `optionalFeatureOptions` references in class and subclass
features resolve to an entry, and every `featureTypes` code a progression uses
has options. Tests are in `OptionalFeatureConverterTest`.

**Found and fixed along the way: `TagMarkupStripper` rendered the source instead
of the name.** 5etools tags are `name|source|display…`, but the stripper treated
the 2nd segment as display text. As a result, `{@spell fireball|xge}` became
"xge" and `{@class fighter|phb|Battle Master|…}` became "phb".

- **Rewrite:** it now follows 5etools' own `Renderer.stripTags` per tag family,
  from `render.js`'s `_TagPipedDisplayText*` / `_TagDiceFlavor` classes:
  - display text in the 3rd segment for entity tags;
  - the 4th for card/deity, the 5th for subclass, the 6th for classFeature, and
    the 8th for subclassFeature;
  - the first segment for style and link-like tags;
  - dice-family rules for dice/damage/hit/dc/chance/scale tags;
  - `{@atk}`, `{@h}` and `{@quickref}`.
- **Unknown tags** now fail ingestion. Tags with no payload (`{@h}`) are now
  recognized.
- **Effect:** re-ingesting changed 790 files' text: 91 spells, 461 items, 134
  feats, 14 classes, 67 subclasses and 23 optional features.

**Still not rendered:** 15 items (the Absorbing Tattoos, Grenades and Grenade
Launcher) start with `{#itemEntry Name|SRC}`, a reference to a shared text
template in `items.json`'s `itemEntry` array with `{{item.*}}` placeholders.
`ItemConverter` doesn't resolve those templates yet. This was already the case
before the fix.

### A2 — species (built 2026-09-23)

`SpeciesConverter` reads `races.json` into 140 `SPECIES` entries in
`content/dnd-5e/species/`. Run it with `--kind=species`. This app says "species";
5etools and the 2014 PHB say "race".

**What `FiveEToolsSpeciesData` resolves:**

- **`_copy`** (16 races, 1 subrace). A copy inherits every field it doesn't define,
  except the ones describing the copied entry itself (`srd`, `page`,
  `reprintedAs`, `_versions`, …). Then its `_mod` is applied to `entries`.
- **Supported `_mod` modes:** `FiveEToolsCopies` implements exactly the subset real
  data uses: `replaceArr`, `appendArr`, `prependArr`, `removeArr` and
  `replaceTxt`, targeting `entries` only. Anything else fails ingestion.
- **`_versions`** become `variants`: named mods such as *Aasimar; Necrotic
  Shroud*, and `_abstract` templates filled per `_implementations` entry such as
  *Dragonborn (Black)*, with `{{color}}`/`{{damageType}}` substituted and the
  implementation's own fields (`resist`) overlaid.
  - A **subrace's** versions modify the *combined* race + subrace text, which is
    how 5etools renders them. The PHB Dragonborn colors remove the race's own
    "Draconic Ancestry" trait.
- **Edition filter.** Races tagged `edition: "one"` are dropped, including copies
  that inherit the tag from a 2024 original (Elf and Kithkin from LFL). See
  `open-items.md` #2. A subrace is kept only when its parent race is.

**Subspecies are nested**, which resolves the former open question. Each species
lists its subspecies under `subspecies`: Mountain Dwarf is inside Dwarf, not a row
of its own. A subspecies has a `name`, `sourceBook`, `sourcePage`, `description`,
`overwrite`, its own mechanics and its own `variants`.

- A subspecies with `name: null` is the race's unnamed default, e.g. the standard
  Human's +1 to every ability. The Variant Human is a named sibling.

**Merge rule (for C1).**

- A subspecies' non-null scalar (`size`, `speed`) replaces the species' value.
- A list field is added to the species' list, unless the subspecies names it in
  `overwrite` (mapped to this project's field names), in which case it replaces
  it.

**Mechanics** (the same for species, subspecies and variants):

- `size` (`["medium"]`, etc.; `null` when absent);
- `speed` (`{walk, fly, swim, climb, burrow}`; a 5etools `true` resolves to the
  walking speed);
- `senses` (`[{type, range}]`);
- `abilityAlternatives`: `{fixed: {strength: 2}, choices: [{from, count,
  amount}]}`;
- skill, tool, language, weapon, armor and feat alternatives in the grant shape;
- `skillToolLanguageChoices`, kept verbatim;
- `damageResistances`, `damageImmunities`, `damageVulnerabilities` and
  `conditionImmunities`, each `{fixed, choices}`;
- `creatureTypes`, `additionalSpells`, `age`, `heightAndWeight` and `lineage`,
  kept verbatim;
- `traits` (`[{name, description}]`, one per named entry).

**Verified 2026-09-23** against real data and the PHB:

- Dwarf: Con +2, 25 ft., darkvision 60, poison resistance, 4 weapons and a choice
  of 3 tools.
- Mountain Dwarf: Str +2, light and medium armor. This is Aria's species.
- Human default: +1 to all six abilities. Variant Human: 2 abilities of choice, 1
  skill, 1 feat.
- Dragonborn colors: 10 variants with the right resistance and breath text.
- Goblin in 7 books: 7 source-suffixed entries.
- Tests are in `SpeciesConverterTest` and `FiveEToolsCopiesTest`.

### A3 — backgrounds (built 2026-09-23)

`BackgroundConverter` reads `backgrounds.json` into 101 `BACKGROUND` entries in
`content/dnd-5e/backgrounds/`. Run it with `--kind=background`. The generated
`soldier.json` replaces the hand-typed Soldier file (source "SRD 5.1"), using the
same slug, so the Background tab's suggestion lookup is unchanged.

- **Scope:** the 70 backgrounds tagged `edition: "one"` are dropped. The 26
  `_copy` backgrounds (the Baldur's Gate and Variant ones, EGW's reskins) are
  resolved through `FiveEToolsCopies.resolveAll`, which is now shared with
  species.
- **New `_mod` support** added for this data: the `insertArr` mode, and a
  `replaceArr` selector given as `{"index": n}`.

**Data:**

- **Suggestion tables.** `personalityTraits`, `ideals`, `bonds` and `flaws` are
  the Background tab's existing shape. They are found by the table's last column
  label ("Personality Trait"/"Trait", "Ideal", "Bond", "Flaw") *anywhere* in the
  text, because some books put them outside "Suggested Characteristics"
  (Haunted One uses d12 tables). Real text is used, including the ideal's
  alignment suffix ("… (Good)").
- **`otherTables`:** every other table, e.g. Soldier's Specialty, as
  `{caption, colLabels, rows}`.
- **`features`:** `[{name, description}]` from blocks named "Feature: X", "Variant
  Feature: X" or "Baldur's Gate Feature: X".
- **Grants** in the grant shape: skill, tool, language and feat alternatives, and
  `skillToolLanguageChoices`.
- **`startingEquipment`:** groups in the class shape, extended with `special`
  (free-text trinkets), `valueCp` (coins), `displayName`, and
  `containsValueCp`/`worthValueCp` (a pouch's coins, a gem's worth).
- **Kept verbatim:** `additionalSpells` and `prerequisites` (campaign
  restrictions such as Dragonlance or Planescape).

**Real gap, not a bug:** about 30 backgrounds (most of SCAG's, plus Custom
Background) have no suggestion tables in 5etools. Their books point to another
background's tables. They are left empty rather than guessed. Several Strixhaven
and Book of Many Things backgrounds have only a personality-trait table, which
is also what their books contain.

**Verified 2026-09-23.** Soldier (Aria's background) matches the PHB:

- 8/6/6/6 suggestion entries;
- the Military Rank feature;
- Athletics and Intimidation;
- a gaming set of choice and land vehicles;
- insignia, trophy, common clothes, and a pouch with 10 gp;
- a choice of bone dice or playing cards.

Tests are in `BackgroundConverterTest`.

## Stage B — Domain model: a character linked to real choices, not flat values

### B plan (approved by the owner 2026-09-23, option (a)) — built 2026-09-23

**As built:**

- **Records:** `Dnd5eCharacterBuild`, `Dnd5eBuildClass`, `Dnd5eBuildChoice`,
  `Dnd5eAbilityScoreMethod`, `Dnd5eHitPointMethod`, `Dnd5eChoice` and
  `Dnd5eChoiceOption`, in `ruleset/dnd5e`.
- **Validation:** total levels ≤ 20, each class at most once, exactly the six
  ability names, and unique choice ids.
- **`Dnd5eSheet`:** gains a nullable `@Valid build`. `SCHEMA_VERSION` is 2.
- **Tests:** `Dnd5eCharacterBuildTest` covers the JSON round trip, validation,
  and a version-1 sheet reading with `build == null`.
- **Not written yet:** nothing writes a build. C1/D1 will call
  `Character.replaceSheet(json, 2)` when they do.

Written after reading `domain-model.md`, `database-schema.md` and the current
`Dnd5eSheet`: a 44-field record whose 15 `with*` methods each repeat the full
constructor, with 12 more constructions in tests.

**Key decision: how the sheet relates to its build.**

- **(a) Materialize with provenance — recommended.**
  - **Source of truth:** the build (species, classes, levels, background, the
    choices made) is the source of truth for everything a character *gets* from
    them.
  - **Regeneration:** the resolver (C1) regenerates the sheet's build-derived
    fields whenever the build changes: at creation, on a level up, or when a
    choice is changed. Those fields are ability scores, level, hit die, speed,
    saves, skills, proficiencies, languages, senses, resistances, features and
    traits, spellcasting classes and slot maxima, and the hit-point inputs.
  - **Untouched:** play state (current and temporary HP, conditions, spent slots
    and uses, inventory, coins, notes, background text, custom actions).
  - **Consequences:**
    - the existing calculator, mutators and UI keep working unchanged;
    - legacy sheets without a build keep working;
    - a later catalogue re-import never silently changes an existing character,
      which is the same copy-once principle as items and spells.

  Each regenerated value records where it came from, so C3 can show
  contributions ("Str 16 = 14 base + 2 Mountain Dwarf").
- **(b) Derive on every read.** The calculator reads species, class and
  background from the catalogue each time the sheet is shown. The downsides:
  - every calculator method is rewritten;
  - every read queries the catalogue;
  - a catalogue re-import silently changes existing characters, the opposite of
    how items and spells already work.

  Not recommended.

The rest of this plan assumes (a).

**What B builds.** Records, persistence and tests only. No resolution logic,
which is C1.

- **`Dnd5eCharacterBuild`:**
  - `speciesSlug`;
  - `subspeciesName` (`null` = the species' unnamed default);
  - `speciesVariantName`;
  - `backgroundSlug`;
  - `classes: [Dnd5eBuildClass(classSlug, subclassSlug, level)]`: multiclass from
    day one, with the first entry being the starting class;
  - `abilityScoreMethod` (`STANDARD_ARRAY`, `POINT_BUY`, `MANUAL`) and
    `baseAbilityScores`, before any species or feat increase;
  - `hitPointMethod` (`FIXED`, `ROLLED`) and `rolledHitPoints` (one per level
    after the first);
  - `choices: [Dnd5eBuildChoice(id, selections)]`.
- **Choice ids** are deterministic strings the resolver produces from the build,
  e.g. `class:fighter:1:skills`, `class:fighter:1:fighting-style`,
  `class:fighter:4:asi-or-feat`, `species:dwarf:tools`,
  `background:soldier:tools`, `class:fighter:equipment:2`. The same build always
  yields the same ids, so a stored selection keeps pointing at the right
  question.
- **`Dnd5eChoice`** is the offered choice, which is `domain-model.md`'s "Choice"
  and is not persisted: `id`, `prompt`, `sourceLabel` (e.g. "Fighter 1"),
  `count`, `options: [Dnd5eChoiceOption(key, label, sourceBook, summary)]`, and
  `selected`. C1 produces it and D1's `planBuild` prints it.
- **`Dnd5eSheet` gains one nullable field, `build`:**
  - `null` means a hand-authored sheet, like today's Aria.
  - `SCHEMA_VERSION` becomes 2. Version-1 rows read unchanged, because the absent
    field reads as `null`.
  - The `sheet_schema_version` column is set to 2 when a sheet is written with a
    build (`Character.replaceSheet(json, 2)`).
  - Threading the field through the 15 `with*` methods and 12 test
    constructions is mechanical. A refactor of that repetition is out of scope.
- **Validation.** Bean Validation on the new records: slugs non-blank, class
  levels 1–20 with a total of at most 20, and base scores 1–30.
- **Tests.**
  - JSON round trip of a full build.
  - A version-1 sheet (today's Aria JSON) still deserializes with
    `build == null`.
  - Validation failures.

**Out of scope for B:**

- turning a build into sheet values, and the provenance format (C1);
- any API endpoint to edit a build (D1 uses build files and the seeder; D2 the
  UI);
- the adr-0006 source toggles (D2).

The earlier draft of this stage proposed a separate
`Dnd5eGrantedFeature`/`Dnd5eGrantedProficiency` list and a `Dnd5eChoice` whose
options embed what they grant. This plan supersedes both:

- under (a), provenance on regenerated values replaces a separate
  granted-feature list;
- choice options carry a `summary` of what they grant, with the grant itself
  read from the catalogue when C1 applies the selection.

## Stage C — Rules engine: derive, don't store (prerequisite, the "100% correct" gate)

Three parts, each its own set of slices: C1 turns a build into a sheet, C2 derives
the fields that are flat today, and C3 makes the sheet react to what happens in
play.

### C1 — Build resolver

A `Dnd5eCharacterBuild` holds Stage B's references (species, subspecies, class
levels, background), the ability score method and base scores, and the resolved
choices. `Dnd5eBuildResolver` is a pure function of a build plus catalogue data.
It returns either the list of **pending choices**, each with its prompt and
options, or, once none are pending, the **materialized sheet**. It does no
persistence, so every rule is unit-testable against real catalogue fixtures.

What "level N implies everything below it" means concretely:

- **Features**: the class's feature table filtered to levels ≤ N, plus the
  subclass's own table once the subclass level is reached.
- **Hit points**: maximum hit die at level 1, then one entry per later level. Fixed
  average versus rolled is itself a choice, asked once per build.
- **Choice points emitted by level**: subclass, ability score increase or feat at
  each ASI level, fighting style, cantrips/spells known, and expertise. Each point
  is read from 5etools' own progression fields, never from a hand-kept level list.
- **Spellcasting**: slots, cantrips known and spells known/prepared counts come
  from the class's progression at level N.

C1 is split into three slices:

- **C1a** turns a build into its pending questions.
- **C1b** materializes the sheet with provenance, including the multiclass slot
  table.
- **C1c** handles spells known, prepared and in a spellbook, plus cantrips.

#### C1a plan — pending choices and `planBuild` (approved 2026-09-23) — built 2026-09-23

**As built.** The plan below holds, with these specifics:

- **Code:**
  - in `ruleset`: `CreationChoice`, `CreationChoiceOption`, `BuildPlan`,
    `CatalogueLookup`, `CatalogueRecord`, `CharacterCreationFlow`,
    `InvalidBuildException`, and the registry;
  - in `ruleset.dnd5e`: `Dnd5eCharacterCreationFlow`, `Dnd5eBuildPlanner`,
    `Dnd5eGrantState`, `Dnd5eChoiceOptions`, `Dnd5ePrerequisites` and
    `Dnd5eSkills`;
  - in `catalogue`: `ContentDirectoryCatalogue`, `PlanBuildMain`, and the
    Gradle `planBuild` task. By default it prints pending choices and problems;
    `--all` prints everything.
- **`subspeciesName`:** `null` = not chosen yet; `""` = the unnamed default. A
  subspecies name two books share is keyed "Name (Book)" (Eladrin, DMG vs. MTF).
- **Merging:** species and subspecies `*Alternatives` combine pairwise (grants
  add up) instead of becoming a pick. That was found by running Mountain Dwarf.
- **Optional class features:** TCE-style ones (`optional: true`: Blind Fighting,
  Ambush, …) are left out, as D&D Beyond does unless "Optional Class Features"
  is enabled. The toggle belongs with D2's source toggles.
- **Equipment categories** supported: simple/martial (melee) weapons, the three
  focus types, musical instruments, artisan's tools and gaming sets.
- **Starting gold:** the starting class offers "starting equipment or starting
  gold" first, like D&D Beyond.

**Verified 2026-09-23 against the PHB rules**, with skeleton builds of the three
target characters (kept out of `content/` until D1 fills them with the owner's
answers):

- **Fighter 20 (Battle Master):**
  - ASI at 4/6/8/12/14/16/19;
  - Fighting Style offering the 6 PHB styles;
  - maneuvers 3 + 2 + 2 + 2 at 3/7/10/15;
  - Student of War's artisan's tool;
  - Mountain Dwarf's tool pick;
  - Folk Hero's tool and equipment picks.
- **Wizard 5:** the Elf subspecies asked first, Arcane Tradition at 2, ASI at 4,
  and an over-budget point buy reported.
- **Sorcerer 3 / Rogue 4:**
  - the Changeling (MPMM) +2/+1 or +1/+1/+1 choice, plus size and a language;
  - Rogue Expertise limited to proficiencies already held;
  - Dragon Ancestor's 10 options;
  - Metamagic ×2 at Sorcerer 3;
  - rolled hit points pending.

Tests: `Dnd5eBuildPlannerTest` (8), `ContentDirectoryCatalogueTest` and
`FiveEToolsMechanicsOverlayTest`.

**Not done:** a click-through comparison with D&D Beyond's own character builder.
It needs a D&D Beyond account building these exact characters; the reference
characters are finished sheets, not the builder. The plan's "Done when" is met
against the PHB, not live D&D Beyond.

**Adapted from D&D Beyond's own rules engine**, studied 2026-09-23 at the owner's
request in
`design-reference/markup/media.dndbeyond.com/packages/rules-engine/es/engine/`
(`Feat/`, `Choice/`, `Prerequisite/`, `Core/constants.js`):

- **Choices belong to a component.** D&D Beyond attaches choices to a feat, class
  feature or racial trait as data, assembled per component (`generateBaseFeat`
  gathers a feat's `choices`, `modifiers`, `spells` and `options`).
  - A choice has a `type`: modifier choice, feature option, feat choice, subclass,
    racial trait, spell…
  - It has a `subType`: `PROFICIENCY`, `EXPERTISE`, `EXPERTISE_NO_REQUIREMENT`,
    `LANGUAGE`, `ABILITY_SCORE`, `KNOWN_SPELLS`, `KENSEI`.
  - It also has `isOptional`, and a `parentChoiceId` for sub-choices. A feat picked
    by an ASI-or-feat choice brings its own choices, listed right after the
    parent.
  - `isTodo` means not optional and not answered.

  **Adopted:** `CreationChoice` gains `type`, `parentChoiceId` and `optional`, and
  a pending choice is exactly D&D Beyond's `isTodo`.
- **Option lists are filtered by what the character already has**
  (`Choice/utils.js`, `getSortedRenderOptions`/`getRemainingOptions`):
  - **Proficiency and language picks** drop options another source already
    grants. The exception is a grant from the background, which stays offered
    and is labelled "(Background)".
  - **Expertise** offers only skills (and tools) the character is already
    proficient in and not already expert in.
    `EXPERTISE_NO_REQUIREMENT` drops the proficiency requirement.
  - **Ability-score picks** drop abilities already at their maximum (20).

  **Adopted:** the planner walks sources in their fixed order and keeps a running
  *grant state* (proficiencies with their source, expertise, ability scores,
  feats, optional features taken), and filters each choice's options against it.
  This is also the seed of C1b's materialization.
- **Prerequisites** (`Prerequisite/validators.js`) are groups of conditions: a
  group passes when all its conditions pass, and the whole check passes when any
  group passes. The condition types are ability score, level, proficiency,
  species, subspecies, size, class, class feature and feat. That is the same
  semantics as 5etools' `prerequisite` arrays (alternatives of AND-ed
  conditions). **Adopted:** feats and optional features whose prerequisites fail
  against the grant state are left out of the options. A selection that is
  unavailable anyway is reported in `problems`.
- **Choices that only exist in prose are hand-authored data at D&D Beyond too.**
  Their choices live on the class feature as data; no engine code mentions
  Expertise. **Adopted:** the adr-0007 overlay carries `choices` as well as
  `modifiers` (amended in the ADR). Because the overlay now holds more than
  modifiers, its directory becomes `content/dnd-5e/mechanics/`.

**Where it lives.** `architecture.md` already names this seam:
`CharacterCreationFlow`, "ordered choices and what each one grants". Today nothing
in `ruleset` touches the catalogue.

- **The seam.** A new `ruleset.CharacterCreationFlow` interface has
  `systemId()` and `plan(buildJson, CatalogueLookup) → BuildPlan`. It is
  implemented by `ruleset.dnd5e.Dnd5eCharacterCreationFlow`, and registered like
  the other strategies (`CharacterCreationFlowRegistry`).
- **Generic question type.** The question type becomes system-agnostic:
  `Dnd5eChoice`/`Dnd5eChoiceOption` from Stage B move to `ruleset.CreationChoice`
  and `CreationChoiceOption`, because the generic interface must return them.
  `BuildPlan` has `choices` (all of them, answered or not), `pending` (the
  subset still needing an answer), and `problems` (rule violations, e.g. an unmet
  multiclass requirement, or an invalid point-buy total).
- **The catalogue port.** `ruleset.CatalogueLookup` is a read-only interface:
  find one entry by kind and slug, and list a kind. The `catalogue` package
  provides two implementations:
  - one over Postgres, for the running API and `DevCharacterSeeder`;
  - `ContentDirectoryCatalogue`, over `content/<system>/<kind>/`, for
    `planBuild`.

  The dependency points from `catalogue` to `ruleset`'s interface, never the
  other way.
- **`planBuild`.** A Gradle `JavaExec` task, like `ingest5etools`, so no Docker is
  needed:
  `./gradlew :apps:api:planBuild --args="--build=content/dnd-5e/builds/aria.json"`.
  It prints the `BuildPlan` as JSON. Build files live in `content/dnd-5e/builds/`.

**Structural questions vs. grant choices.** Answers go to one of two places:

- **Build fields.** Questions whose answer is a field of the build have ids
  prefixed `build.`, and D1 writes the answer into that field:
  - `build.subspecies`: asked when the species has subspecies. A `null`-named
    default shows as "Standard".
  - `build.variant`: asked when the chosen species or subspecies has variants,
    e.g. the Dragonborn colors.
  - `build.class.<slug>.subclass`: asked once the class reaches its
    `subclassLevel`.
  - `build.rolledHitPoints`: asked when the hit point method is `ROLLED`.
- **Grant choices.** Every other question is a grant choice, answered in
  `choices`.

**Grant choices generated, in a fixed order** (species → background → classes in
build order → by level). Every count and option list is read from the catalogue
data written in A1–A3:

- **Species:** read after merging the subspecies and variant with the species by
  the A2 merge rule. Covers:
  - the ability alternative and its picks;
  - skill, tool, language, weapon and feat picks;
  - resistance picks;
  - `skillToolLanguageChoices`;
  - a size choice when more than one size is listed.
- **Background:** skill, tool, language and feat picks, and each starting-equipment
  group that has more than one option.
- **Starting class only:** skill and tool picks, and each equipment group with
  more than one option. An `equipmentType` grant (e.g. `weaponMartial`) becomes
  a pick among the catalogue items of that category.
- **Each later class:** the `multiclassing.proficienciesGained` picks.
- **Per class, per level ≤ its level:**
  - ASI-or-feat at each `abilityScoreImprovementLevels` level, followed by either
    the +2/+1+1 pick or the chosen feat's own picks;
  - optional-feature picks from each class and subclass
    `optionalFeatureProgressions` (Fighting Style, Maneuvers, Metamagic,
    Invocations…), counted by `countByLevel` and filtered by `featureTypes` and
    by level/pact prerequisites;
  - picks granted by a chosen optional feature (Superior Technique's extra
    maneuver).

**Two catalogue gaps C1a must close:**

1. **Feats carry no mechanics today.** `FeatConverter` writes empty `data`, but
   ASI-or-feat and Variant Human need a feat's own picks (e.g. Resilient's
   ability). Extend `FeatConverter` to map 5etools' structured feat fields
   (`ability`, `skillProficiencies`, `toolProficiencies`, `languageProficiencies`,
   `additionalSpells`, `prerequisite`) into the grant shape.
2. **Choices that exist only in prose** need the adr-0007 overlay:
   - Rogue Expertise (Rogue 4 at level 1);
   - the Battle Master's *Student of War* artisan's tool (Aria);
   - the Draconic Bloodline's dragon ancestor (a possible subclass of the
     Sorcerer 3).

   adr-0007 was written for *modifiers*. C1a would extend the overlay file to also
   hold a `choices` list in the same grant shape (e.g. Expertise: pick 2 of
   `proficientSkills` + `thieves-tools`), with the same citation rule. **Owner
   confirmation needed.**

**Validation reported as `problems`, not choices:**

- `STANDARD_ARRAY` base scores are a permutation of 15/14/13/12/10/8.
- `POINT_BUY` scores are 8–15 and total 27 points.
- Every non-starting class meets its `multiclassing` ability minimums, using
  base scores plus species increases.
- Every answered choice id is one the plan actually offers, and each selection
  is among its options.

**Tests:**

- One `Dnd5eCharacterCreationFlow` test per target character's shape (Fighter 20
  Battle Master with 7 ASI levels; Wizard 5; Sorcerer 3 / Rogue 4), against
  trimmed real catalogue fixtures.
- A `ContentDirectoryCatalogue` test.
- A `planBuild` smoke test.

**Done when.** `planBuild` against the owner's real `content/` prints the
complete, correct question list for skeleton builds of all three target
characters. That output is then checked by hand against D&D Beyond's own
creation flow for the same choices.

**Out of scope:**

- materializing the sheet, provenance, and the multiclass slot table (C1b);
- spells, cantrips and spellbooks (C1c);
- evaluating spell-based prerequisites, e.g. an invocation needing *eldritch
  blast*: those options are offered but flagged, and checked in C1c;
- the adr-0006 source toggles (D2).

#### C1b plan — materialize the sheet with provenance (approved 2026-09-23, with owner decisions)

**Owner decisions, 2026-09-23:**

- **Hit points: every character uses the average for now.** Choosing manual
  rolls, virtual rolls or the average is a phase 11 (D2) creation-UI feature.
  The average follows 5etools' own rule (`render.js`, "Hit Points at Higher
  Levels": `(number × faces) / 2 + 1`):
  - the maximum hit die at character level 1 (starting class);
  - then d6 = 4, d8 = 5, d10 = 6 and d12 = 7 per later level.

  `ROLLED` stays in the build model but no target character uses it.
- **Keep character level and per-class levels** (`level` + `classLevels`).
- **Proficiency has four levels.** The owner asked for this earlier, and the
  calculation may change. Following D&D Beyond (`ProficiencyLevelEnum` NONE /
  HALF / FULL / EXPERT and `deriveProficiencyBonusAmount` in its rules engine):
  - EXPERT = 2 × the proficiency bonus;
  - FULL = 1 ×;
  - HALF = half, rounded down, or rounded up when the source says so (the
    `half-proficiency-round-up` modifier, e.g. Remarkable Athlete);
  - NONE = 0.

  The skill's proficiency dot uses D&D Beyond's own icons from
  `design-reference/markup/svg-index.html`, section "Proficiency dots":
  - `ProficiencySvg` (a filled dot) for FULL;
  - `ProficiencyHalfSvg` for HALF;
  - `ProficiencyDoubleSvg` (a ring around a dot) for EXPERT;
  - today's empty dot for NONE.
- **Add `TREMORSENSE`.**

**How the four levels are modelled:**

- `skillProficiencies` stays the set of FULL skills, and the new `skillExpertise`
  holds EXPERT.
- HALF only ever comes from features (Jack of All Trades, Remarkable Athlete),
  so its source is an adr-0007 modifier in C3. C1b builds the level enum,
  `Dnd5eProficiencyLevel`, and the calculation.
- The API's skill map becomes a proficiency *level* per skill instead of a
  boolean, and the skill and saving-throw rows render the matching icon. That
  changes the sheet UI, so the live D&D Beyond comparison applies.

**What it does.** It turns a fully answered build into the sheet's build-derived
fields, following Stage B option (a). It runs only when the `BuildPlan` has no
pending choices *and* no problems; otherwise it refuses and returns the plan.
Everything else on the sheet is kept as it is: current and temporary HP,
conditions, spent slots and uses, items, coins already held, notes, the
background's personality text, and custom actions.

**Model gaps the current sheet has, found by reading it.** Each is closed in C1b,
because one of the target characters needs it:

- **Hit points.** `Dnd5eFormulas.maxHitPoints` assumes one hit die size and the
  fixed average. A Rogue 4 / Sorcerer 3 mixes d8 and d6, and rolled hit points
  aren't averages.
  - **Fix:** a new nullable `hitPointBase`, the sum of the hit die results per
    level, without Constitution: the starting class's maximum die at level 1,
    then each later level's fixed average or rolled value, in build order.
  - When `hitPointBase` is set, max HP = `hitPointBase` + character level × Con
    modifier + the Max HP Modifier (or the Override Max HP instead). A later Con change still flows through.
    Sheets without it keep today's formula.
- **Class levels.** The sheet stores only a total `level` and one `hitDieSize`.
  - **Fix:** a new `classLevels` list of `(className, subclassName, level,
    hitDieSize)`. `level` stays the total, and `hitDieSize` stays the starting
    class's die, for code that reads them today.
  - The hit-dice pool UI keeps showing one die size for now. Per-size pools are a
    C2 item, listed in `open-items.md`.
- **Expertise.** The sheet has no expertise, and the calculator only adds
  proficiency once.
  - **Fix:** a new `skillExpertise` set, and the four proficiency levels above.
    The contribution is labelled "Proficiency bonus ×2 (Expertise)".
  - Tool expertise (thieves' tools) is shown on the proficiency label, because
    tools have no computed value yet.
- **Senses.** `Dnd5eSenseType` has no `TREMORSENSE`; it's added.

**Provenance.** A new nullable `derivation` record on the sheet holds:

- **`abilityScores`:** per ability, a list of `{source, amount}` ("Base" 15,
  "Mountain Dwarf" +2, "Fighter 4" +1…);
- **`proficiencies`:** `{kind, key, source}` for every save, skill, expertise,
  tool, language, weapon and armor grant;
- **`hitPoints`:** `{source, amount}` per level;
- **`speed` and `senses`** with their sources.

C3 turns these into the `Contribution`s the sheet displays. All four new fields
join sheet schema version 2; no version-2 row has been written yet, so nothing
needs a new version.

**Regenerated fields:**

- **Ability scores and totals:** the six scores (base + every increase), `level`,
  `hitDieSize` and `speed` (walking).
- **Proficiencies:**
  - `savingThrowProficiencies`, from the starting class only;
  - `skillProficiencies` and `skillExpertise`;
  - armor, weapon and tool proficiencies and `languages`, as the display labels
    the sheet already uses ("Light", "Martial", "Thieves' Tools (Expertise)",
    "Dwarvish").
- **Defenses and senses:** resistances, immunities, vulnerabilities, condition
  immunities, and `specialSenses`.
- **`featureTraits`:**
  - class features up to each class's level, including the chosen subclass's;
  - chosen optional features: Fighting Style, maneuvers, metamagic;
  - species traits (from the merged species) and feats.

  Two kinds of entry are not listed: the subclass placeholder rows ("Martial
  Archetype feature"), which D&D Beyond doesn't show either, and the TCE optional
  features left out in C1a. Keys are deterministic, so an existing entry's
  `usedCount` survives a re-materialization.
- **`spellcastingClasses`** with their cantrips-known and spells-known/prepared
  maxima at each class's level. The spells themselves are C1c.
- **`spellSlots` maxima:**
  - a single spellcasting class uses its own table;
  - several use the PHB multiclass rule: caster level = full-caster levels +
    half of half-caster levels + a third of third-caster levels (the Artificer
    rounds up), then the slots are read from a full caster's own
    `spellSlotsByLevel` at that caster level, so no slot table is typed by hand.
  - Pact slots stay separate, for C1c.
  - The used-slot counts are kept, capped at the new maxima.
- **`background`:** `name`, `featureName` and `featureDescription` from the
  catalogue. The player's own text fields stay.
- **Starting equipment:** the resolver returns the chosen items as
  `startingItems` (catalogue slug, quantity, display name) plus starting coins
  (pouch contents, background gold, or class starting gold as rolled/entered).
  The D1 applier adds the items through the existing catalogue-copy path
  (`CharacterSheetService`'s item copy), so no new item-copy code is written.

**The seam.** `CharacterCreationFlow` gains
`materialize(currentSheetJson | null, buildJson, CatalogueLookup) →
MaterializedSheet(sheetJson, startingItems, startingCoins, plan)`. For D&D 5e,
`Dnd5eBuildMaterializer` reads the finished `Dnd5eGrantState`, which the planner
extends to also collect features, species mechanics, the background feature and
equipment picks.

**Tests:**

- The three target characters' shapes as fixtures (ability totals, HP with
  mixed dice, fixed vs. rolled HP, expertise doubling, multiclass slots
  Sorcerer 3 + Eldritch Knight-style third caster, and preserved play state).
- Calculator tests for the expertise contribution and `hitPointBase`.
- A version-1 sheet (today's Aria) still computing exactly as before.

**Out of scope:**

- spells, cantrips and pact slots (C1c);
- feature uses and recharge, and feature actions (Second Wind, Action Surge):
  5etools only has these in prose, so they need adr-0007 modifier data, which is
  C3;
- non-walking speeds on the sheet;
- per-size hit dice pools (C2);
- writing to the database (D1).

#### C1b as built (2026-09-23)

- **Seam:** `CharacterCreationFlow.materialize` returns
  `MaterializedSheet(sheetJson, startingItems, startingCopper, plan)`. When
  anything is pending or a problem exists, `sheetJson` is null and
  `isMaterialized()` is false. `PlanBuildMain --materialize` prints the result.
- **Planner:** `Dnd5eBuildPlanner` records a `Dnd5eBuildOutcome` while it walks:
  - classes taken, features, species mechanics, the background, and resistance
    picks;
  - starting items and starting copper;
  - each chosen optional feature as a "Prompt: Label" feature ("Dragon Ancestor:
    Red", "Metamagic: Twinned Spell").

  `plan()` walks once and caches the result, since the walk accumulates grant
  state.
- **Ability maximum:** every increase checks the score against its maximum (20,
  or the source's own `maximum`). Going over is a plan problem, so it blocks
  materialization. Options only hide an ability that is *already* at the
  maximum, so two picks of the same ability in one ASI could otherwise reach 21.
- **Materializer:** `Dnd5eBuildMaterializer` regenerates the fields listed above.
  Two labels have no catalogue entry and are built from the key instead:
  - tools from the key ("vehicles-land" → "Vehicles (Land)");
  - the 5etools `other` language (a species' own tongue, named only in prose)
    is left out.

  Senses are capped at 120 ft, the sheet's own limit.
- **Tests:** `Dnd5eBuildMaterializerTest` covers:
  - multiclass HP (maximum, then each class's average);
  - multiclass slots on the full-caster table;
  - a subclass caster's own table;
  - provenance;
  - play state kept and capped;
  - refusal while choices are pending.

  `Dnd5eBuildPlannerTest` covers the ability maximum, and
  `Dnd5eMaterializedFieldsCalculatorTest` covers the expertise contribution and
  `hitPointBase`.
- **Verified on real data** with `planBuild --materialize`:
  - **Mountain Dwarf Folk Hero, Battle Master 20:** HP 224 (base 124);
    Vehicles (Land); Darkvision 60; 51 features.
  - **Changeling (MPMM) Urchin, Rogue 4 Thief / Sorcerer 3 Draconic:**
    HP 42 (base 35); saving throws from the Rogue only; expertise in Stealth
    and Thieves' Tools; 4 cantrips; slots 1st ×4, 2nd ×2; "Dragon Ancestor:
    Red".

  These are sample answers, not the owner's D1 choices.
- **Known gaps, listed in `open-items.md`:**
  - languages granted only in prose (Rogue's Thieves' Cant);
  - hit points and AC from features (Draconic Resilience), which are C3
    modifiers;
  - the hit-dice pool UI still shows one die size (C2).

#### C1c plan — spells in the build (approved 2026-09-23) — built 2026-09-23

**As built.** The plan below holds, with these specifics:

- **Ingestion.** `SpellConverter` writes the class lists into two fields:
  - `classes`: the lists the spell is on;
  - `optionalClasses`: lists that only an optional class feature adds it to.

  How references are sorted:
  - `classVariant` isn't only TCE's optional lists. XGE, FTD and BMT use it to add
    their own spells to class lists normally. A variant counts as optional only
    when both hold:
    - its book defines `isClassFeatureVariant` features for that class (read from
      the class files);
    - its book isn't the spell's own book.

    So Aid's Bard/Ranger entries are optional (TCE), while Blade of Disaster,
    TCE's own spell, is on the Sorcerer, Warlock and Wizard lists.
  - References to 2024-edition classes (`edition: "one"`, i.e. XPHB, EFA) are
    dropped.

  69 spells have no class list at all, because 5etools gives none for our
  classes. These are from FRHoF, Arcana Unleashed, EGW dunamancy, one GGR spell
  and one EFA spell.
- **Code:**
  - `Dnd5eSpellLists`: class lists, name references, and 5etools filters
    (`level`, `class`, `school`, `source`, `spell attack`, and ritual);
  - `Dnd5eSpellPlanner`: called by `Dnd5eBuildPlanner`;
  - `Dnd5eGrantState`: tracks known spells, for prerequisites and to hide
    duplicates;
  - `Dnd5ePrerequisites`: checks `spell` and `spellcasting*`.
- **Choice ids:**
  - `class:<c>:cantrips`, `class:<c>:spells`, `class:<c>:spellbook` and
    `class:<c>:prepared`, one of each per class (pooled since 2026-09-25);
  - granted choices: `<prefix>:<known|prepared|innate>:<level>:<index>`, plus
    `:spell-set` and `:spell-ability` when the source offers alternatives.

  The wizard "prepared" question appears once the spellbook has spells in it.
- **Granted spells:**
  - A subclass's spells belong to its class. EK and Arcane Trickster lists are
    its `expanded` "all" filters, and patron lists use "s1" to "s5" keys, which
    unlock with the class's own slot level.
  - Any other source (species, feat, background, optional feature) becomes its
    own `spellcastingClasses` entry ("High Elf", INT, KNOWN). That's needed
    because rolls and the UI find a spell's attack bonus and DC through its
    `className`.
- **Prepared flags:**
  - always-prepared grants are stored with `prepared` false and `alwaysPrepared`
    true, so they don't count against the preparation limit;
  - usage limits go at the start of the spell's notes ("1/day, cast at level 2;
    V, S").
- **Tests:**
  - `Dnd5eSpellPlannerTest`: per-level increases, pact slot limit and patron
    list, invocation spell prerequisite, species filtered choice, duplicates
    hidden;
  - `Dnd5eBuildMaterializerTest`: EK list and table, spellbook with prepared
    subset, species caster and usage notes, prepared flags kept;
  - `SpellConverterTest`: class lists.

  The shared fixtures live in `Dnd5eBuildFixtures`.
- **Verified on real data** (sample answers, not the owner's D1 choices):
  - **High Elf Wizard 5 (Urchin):**
    - the questions follow the PHB: the High Elf cantrip, 3 + 1 wizard cantrips,
      and a spellbook of 6 + 2 + 2 + 2 + 2;
    - materialized: 14 spellbook spells, 9 prepared (5 + INT 4), slots 4/3/2,
      HP 32, and a High Elf INT caster entry.
  - **Rogue 4 / Sorcerer 3:** 4 cantrips and 2 + 1 + 1 spells, with 2nd level
    at Sorcerer 3 from its own table.
  - The Aria sample is unchanged.
- **Known gaps (in `open-items.md`):**
  - pact slots;
  - spells granted by optional features, e.g. invocations, get no caster entry
    when their data names no ability, so their DC doesn't show;
  - EK/AT school limits;
  - the 69 unlisted spells.
- **Live check still owed:** the Spells tab header and the Manage Spells panel
  showing a species caster entry next to the class. It gets checked against D&D
  Beyond when D1 writes the Elf Wizard.

**What the target characters need:**

- **Elf Wizard 5:**
  - a High Elf asks for one wizard cantrip (5etools `additionalSpells`
    `choose: "level=0|class=Wizard"`);
  - 4 wizard cantrips;
  - a spellbook of 6 + 2 × 4 = 14 spells;
  - 5 + INT modifier of them prepared.
- **Sorcerer 3 (with Rogue 4):** 4 cantrips and 4 known spells, of a level the
  Sorcerer's own table reaches (PHB multiclassing: spells known are decided
  "as if you were a single-classed member of that class").
- **Battle Master 20 and the Changeling:** no spells.

**What 5etools gives, and what's missing today:**

- **Class spell lists.** These are in `data/spells/sources.json` (spell →
  `class[]`, plus `classVariant[]` for TCE expanded lists), not in the spell
  files, so today's `SPELL` entries don't know which classes have them.
  - **Fix:** `SpellConverter` adds `classes` and `classVariants` (name +
    source) to each spell's data, and the spell files are regenerated.
  - `classVariant` lists are TCE optional class features, so they are left out
    by default, the same as open item 6.
- **Granted spells.** Species, subclasses, feats, backgrounds and optional
  features already keep 5etools' raw `additionalSpells`. C1c reads them:
  - `known`: always known ("thaumaturgy#c");
  - `prepared`: always prepared, e.g. domain spells;
  - `expanded`: added to the class's options, e.g. a warlock patron's list;
  - `innate`: cast without a slot, e.g. "hellish rebuke#2" once a day.

    The spell is listed with its use limit in the notes, and tracking the uses
    is C3.
  - `choose` filters ("level=0|class=Wizard", "level=1|school=D") become a
    pick among catalogue spells.

  Names resolve to catalogue slugs the same way languages do: by name, with the
  PHB version preferred when two sources share it.
- **Class progressions** are already in the catalogue:
  - `cantripsKnownByLevel`;
  - `spellsKnownByLevel`;
  - `spellbookSpellsAddedByLevel`;
  - `preparedSpellsFormula`.

**Choices the planner emits, pooled per class**, following D&D Beyond's three
caster kinds (`deriveClassSpellRules`). Each pool is asked once, after the
class's last level. Its source label names the class levels that add to it,
e.g. "Bard 1, 4, 10", and three or more consecutive levels collapse to a range
("Wizard 1–5"). This applies to the creation flow only; the sheet's spell
management is unchanged.

- **Cantrips:** "class:<c>:cantrips", with count = cantrips known at the class's
  level.
- **Known casters** (Bard, Sorcerer, Ranger, Warlock, and the Eldritch
  Knight/Arcane Trickster subclasses): "class:<c>:spells", with count = spells
  known at the class's level. Options are the class's list, up to the highest
  slot level the class's *own* table reaches at that level.
- **Spellbook casters** (Wizard): "class:<c>:spellbook", with count = the sum of
  `spellbookSpellsAddedByLevel` up to the class's level, under the same level
  limit. Then "class:<c>:prepared" asks which spellbook spells start prepared.
- **Claimed early:** before a class's levels are planned, the spells already
  selected in its pools count as known (`Dnd5eSpellPlanner.startClass`). This
  way prerequisites checked along the way still see them (Agonizing Blast needs
  Eldritch Blast), and other sources don't offer them again. The pools still
  offer them.
- **Prepared casters who know their whole list** (Cleric, Druid, Paladin,
  Artificer): only "class:<c>:prepared", from the full class list up to the
  slot limit.
- **Granted-spell choices:** "species:<s>:spells:<i>", "feat:…", and so on,
  from `choose`.
- **Spell prerequisites** (e.g. an invocation needing *eldritch blast*) are
  now checked against the spells chosen so far, closing the C1a flag.

**Materialization:**

- The sheet's `spells` list is regenerated from the build. Each entry comes from
  the catalogue spell through the existing copy path (`SpellCatalogueData`).
  `className` is the owning class, or the granting source's label ("High Elf",
  "Life Domain").
- **Play state:**
  - the current `prepared` flag of a spell that is still in the build is kept;
  - spells granted by items (`grantedByItemKey`) are kept as they are.

  The build's "prepared" answer only seeds a new sheet.
- `alwaysPrepared` is set for `prepared` grants.

**Out of scope, with a reason:**

- **Pact slots.** The sheet has one slot table. Pact Magic needs its own slots,
  which refresh on a short rest, plus UI and a rest change. No target character
  is a Warlock.
  - **Proposal:** a separate slice when a Warlock is built. It goes in
    `open-items.md`.
- **Swapping a known spell on level-up** (the PHB allows one per level for known
  casters). The build answers the final list directly, which gives the same
  result.
- **Eldritch Knight / Arcane Trickster school limits** (most picks must be
  abjuration/evocation or enchantment/illusion). They are in 5etools prose
  only, so they need an adr-0007 overlay. Until that exists, options are
  offered unfiltered with a note. No target character needs it.
- **Uses of `innate` spells:** C3.

**Tests:**

- planner tests for:
  - pooled cantrip, known and spellbook counts with their granting levels;
  - the spellbook level limit;
  - a multiclass using each class's own table;
  - `additionalSpells` `choose`/`known`/`prepared`;
- `SpellConverter` class lists from a trimmed `sources.json`;
- materializer tests for keeping prepared flags and item-granted spells.

Verified on real data with `planBuild --materialize` for the Elf Wizard 5 and
the Sorcerer 3 / Rogue 4.

### C2 — Derive the flat fields

Extends `Dnd5eSheetCalculator` so every value the sheet currently shows — ability
modifiers, proficiency bonus, saving throws, skills, senses, speed, armor class,
known/prepared spells, granted features — is derived from species + class(es) +
background + resolved choices + equipped items, the same "derive from a real fact"
treatment `armorClass()`/`.attacks()` already give an equipped item's `itemKind`.
Concretely: replace `Dnd5eSheet`'s flat `armorProficiencies`/`weaponProficiencies`/
`toolProficiencies`/`savingThrowProficiencies`/`skillProficiencies`/`speed`/
`specialSenses` fields (currently free-standing, hand-authored per character) with
values computed from Stage B's granted-feature/proficiency lists, falling back to
nothing invented where 5etools genuinely has no data for a case.

#### C2 plan — show where values come from; per-class hit dice (approved 2026-09-24)

**Why the original definition is out of date.** C1b already regenerates every flat
field listed above from the build (scores, proficiencies, senses, speed, features,
slots), and C3 applies modifiers on top. Two real gaps remain:

- **The provenance is never shown.** C1b records `Dnd5eSheet.derivation`
  ("so C3 can turn it into contributions"), but nothing reads it. The sheet shows
  STR 20 with no way to see "Base 15 + Mountain Dwarf +2 + Fighter 4 ASI +2 + …".
- **Hit dice are one pool of one size.** Vex (Rogue 4 d8 + Sorcerer 3 d6) shows
  7 × d8. Every short-rest roll then uses the wrong die for 3 of them.

**C2a — provenance on the sheet** (reference: D&D Beyond's `AbilityPane` /
`AbilityScoreManager`):

- **Ability scores.** A new `VitalsZone.abilityScoreDetails` holds a
  `CalculatedValue` per ability, with contributions from `derivation.abilityScores`
  (Base, species, each ASI or feat) plus the C3e "Set Score" from an item.
  - Clicking an ability box opens an Ability panel that mirrors D&D Beyond's:
    Total Score, Base Score, Species Bonus, Ability Improvements, Set Score, and
    the modifier.
  - Sheets without a derivation show "Base" only.
- **Hit points.** The max HP breakdown lists each level's die ("Fighter 1
  (maximum d10) 10", "Fighter 2 (average d10) 6"…) and "Constitution modifier
  × level", instead of one lump "hit point base".
- **Speed.** The speed box gets an explainer (species source, plus C3 SET/HALVE
  changes such as Grappled or Exhaustion).
- **Proficiencies.** Each save, skill, tool, language, weapon and armor entry
  carries its source(s) from `derivation.grants`. The explainers show
  "Proficiency bonus (Fighter)", and Proficiencies & Training shows the source
  as a tooltip.

**C2b — per-class hit dice** (reference: D&D Beyond's `ShortRestPane`, which
tracks `hitDiceUsed` per class):

- **Sheet:** a new `hitDiceUsedBySize` map (die size → used). The old
  `hitDiceUsed` count is read as used dice of the largest size, so no stored
  sheet breaks.
- **Response:** `VitalsZone.hitDice` becomes one pool per die size from
  `classLevels`, e.g. Vex d8 × 4 and d6 × 3. A single-class sheet looks as it
  does today.
- **Short rest:**
  - the panel shows one row per die size, and the player picks how many of each;
  - `RollService` rolls each size separately and records one roll event per
    size;
  - `spendHitDice` takes a count per size.
- **Long rest:** restores half the total (minimum 1).
  - **The player chooses which dice come back** (owner decision, 2026-09-24).
  - The long-rest panel shows the per-size rows with the number to recover.
  - With a single die size, nothing needs choosing.

**Tests:**

- calculator tests for each breakdown (with and without a derivation);
- mixed-die short and long rests;
- a legacy sheet with only `hitDiceUsed`;
- a Vitest test for the Ability panel and the per-size short-rest rows;
- a live comparison on Aria (single class) and Vex (multiclass).

**Out of scope:**

- D&D Beyond's "Other Modifier" / "Override Score" editing (a manual override
  the builder owns);
- non-walking speeds (no data path yet);
- pact slots, which stay their own agreed slice.


This is the part that makes Target outcome scenarios 2–6 true. The design copies
D&D Beyond's own modifier system and goes one step further for lasting spells.

#### How D&D Beyond does it

Studied 2026-09-23 in
`design-reference/markup/media.dndbeyond.com/packages/rules-engine/es/`, mainly in
`engine/Modifier/`, `engine/Condition/`, `engine/Character/generators/ArmorClass.js`
and `selectors/composite/engine.js`.

- **A modifier is data, not code.** Each entity's definition (race, class feature,
  feat, background, item, condition) carries a `modifiers` list. Each modifier has:
  - `type` — about 33 values: `bonus`, `set`, `set-base`, `advantage`,
    `disadvantage`, `resistance`, `immunity`, `proficiency`, `expertise`, `sense`,
    `speed-reduction`, `ignore`, and so on;
  - `subType`, the target — about 190 values: `armor-class`,
    `dexterity-saving-throws`, `stealth`, `speed-walking`, `darkvision`, `fire`,
    `unarmored-armor-class`, `hit-points-per-level`, and so on;
  - `value`, `dice` or `statId` (a value taken from an ability modifier, which is
    how Unarmored Defense adds Con);
  - `restriction`, free text for situational cases ("against being frightened").
    It is displayed but never evaluated;
  - `requiresAttunement`;
  - `dataOrigin`, the source entity, which labels the value's breakdown.
- **One global list.** `getValidGlobalModifiers` concatenates race + classes +
  feats + background + equipped items + active conditions. Every derived value
  filters that list by type and subType. Condition definitions carry their own
  modifiers, and exhaustion carries modifiers per level.
- **AC is a list of labelled "suppliers"**: armor, Dex bonus with its cap, shield,
  the highest unarmored set-base, bonuses, and manual adjustments. That is the same
  idea as our `CalculatedValue` + `Contribution`.
- **The engine knows only the vocabulary, never individual content.** No engine
  code mentions Mage Armor or Unarmored Defense. Exceptions leak in only through a
  handful of `hacks.js` files.
- **Spells are not a modifier source.** A spell's `modifiers` only scale its own
  damage or healing, and `isActive` means "castable" (prepared or known), not "in
  effect". **D&D Beyond does not apply Mage Armor or Aid to the sheet**: the
  player types a manual AC adjustment. This project closes that gap (see "Active
  effects" below).
- **D&D Beyond's modifier data is hand-authored by its own staff**, in its own
  database. 5etools has no equivalent. So a curated mechanics layer is how the
  reference product works too, not a workaround unique to this project.

#### Our design: the same vocabulary-driven shape

- **`Dnd5eModifier`** has `type`, `target`, `value`, an optional ability for
  "value = ability modifier", an optional `restriction`, `requiresAttunement`, and
  `source`. `Dnd5eModifierType` and `Dnd5eModifierTarget` are enums. The vocabulary
  grows by adding enum values, never by content-specific code, which keeps it
  Open/Closed. Start with the subset the audit characters need, and use D&D
  Beyond's names as the naming reference.
- **Every catalogue kind can carry `modifiers`** in its catalogue data: item,
  species, class feature, background, feat, condition, spell. They are copied onto
  the character like every other catalogue field ("copy once").
- **`Dnd5eSheetCalculator` builds one global list** from:
  - species;
  - class features up to the current level;
  - background and feats;
  - equipped items (and attuned ones, where attunement is required);
  - active conditions and the current exhaustion level;
  - **active effects**, the part that goes beyond D&D Beyond.

  Every derived value filters that list, and every applied modifier becomes a
  `Contribution` labelled with its source. Modifiers with a `restriction` are not
  summed. They are listed as notes next to the value, which is D&D Beyond's own
  behavior ("Advantage on saving throws against being frightened").
- **Stacking follows the PHB.** Among `set-base` modifiers the highest wins (armor
  vs. Mage Armor vs. Unarmored Defense). Two effects from the same spell don't
  combine. Advantage and disadvantage cancel out, which `RollMode` already does.
- **Roll modes.** The calculator returns a map keyed per d20 roll (attack type,
  check or save per ability, each skill), each entry listing its sources.
  `RollService` reads it, and its `isOverloadedStrengthRoll` special case becomes a
  modifier sourced from encumbrance.

#### Where modifiers come from

| Source | How | Authoring |
| --- | --- | --- |
| Items | `ItemConverter` maps 5etools structured fields (`ac`, `bonusAc`, `bonusSavingThrow`, `bonusWeapon`, `strength`, `stealth`, `resist`, …) | None |
| Species, backgrounds, feats, class features | The Stage A converters map 5etools structured fields (`speed`, `darkvision`, `resist`, `ability`, `skillProficiencies`, …) | None |
| Conditions and exhaustion | Curated overlay covering a small fixed PHB set: 14 conditions and 6 exhaustion levels | Once |
| Prose-only features and lasting spells (Unarmored Defense, Fighting Style: Defense, Mage Armor, Aid, …) | Curated overlay | One entry at a time, as characters use them |

**Curated modifier overlay.**

- **Layout:** `content/dnd-5e/modifiers/<kind>/<slug>.json`, one file per 5etools
  entity. Each file contains only a `modifiers` list plus the source book and page.
- **Merge:** ingestion merges the overlay into the converter's output, so every
  name and every text still comes from 5etools. This is the direct counterpart of
  D&D Beyond's staff-authored modifier data.
- **Validation:** an overlay file whose slug matches no ingested entry fails
  ingestion.
- **Decision record:** `decisions/adr-0007-curated-modifier-overlay.md`
  (accepted 2026-09-23), which amends adr-0006's "never hand-author" rule for
  mechanical encoding only.

#### Active effects: beyond D&D Beyond

**Owner rules, 2026-09-23:**

- Anything that ends quickly is not tracked.
- Anything lasting long enough to matter **is** shown on the sheet whenever it
  changes a value.
- **A rest ends an effect only when the source itself says so.** Duration and rests
  are independent: a character can go more than 24 hours without a short rest, so
  "8 hours" never implies "ends on a long rest".

**How it works:**

- **New sheet state.** `activeEffects` holds entries of `(sourceKind: SPELL |
  FEATURE | ITEM, slug, castAtLevel, concentration, endsOnRests, durationText)`.
  The entry's modifiers join the global list while it exists.
- **Which effects are tracked.** A spell, feature or item use can become an active
  effect when it has modifiers and either lasts **1 hour or more**, or ends
  explicitly on a rest. Anything shorter (Shield, Haste, Bless, anything measured
  in rounds or minutes) keeps today's behavior: the slot is spent, the roll is
  made, and nothing is tracked.
- **When it ends (`endsOnRests`)** comes only from the source's own words,
  recorded in the adr-0007 overlay with a citation:
  - **A set of rests**, such as `{LONG_REST}` or `{SHORT_REST, LONG_REST}`,
    exactly as the text names them ("until you finish a long rest", "until you
    finish a short or long rest"). The matching rest action removes the effect
    automatically.
  - **Empty** for everything else: every fixed duration (Mage Armor's or Aid's
    8 hours) and "until dispelled". The effect stays until the player ends it.
    The sheet shows `durationText` ("8 hours") next to it as a reminder, because
    there is no clock.
- **A manual "end" action** is always available: for when the duration runs out,
  the effect is dispelled, or concentration is broken.
- **Concentration.** A character concentrates on at most one spell (PHB), so
  casting a second concentration spell ends the first.

#### C3 slices (approved 2026-09-24)

C3 is split so each slice closes gaps the three target characters actually show:

| Slice | What | Closes |
| --- | --- | --- |
| **C3a** | Modifier vocabulary, feature/feat modifiers on the sheet, AC/HP/attacks | Tough (+40 HP), Extra Attack, Fighting Style: Defense, Draconic Resilience (HP, AC) |
| **C3b** | Feature uses and recharge from the overlay (`maxUses` + recharge on `featureTraits`) | Second Wind, Action Surge, Indomitable, Superiority Dice, Sorcery Points, Arcane Recovery |
| **C3c** | Conditions and exhaustion as modifiers; roll modes (advantage/disadvantage per d20 roll, with sources) read by `RollService` | Target outcome 4 ("if something gives me disadvantage, show it"); replaces `isOverloadedStrengthRoll` |
| **C3d** | Active effects (Mage Armor, Aid…): `activeEffects`, cast → effect, manual end, concentration, source-only rest endings | Target outcome 2–3 (cast a spell that changes AC) |
| **C3e** | Item modifiers from 5etools' structured fields (`bonusSavingThrow`, `resist`, `bonusWeapon`…) | Magic items beyond today's AC/attack bonuses |

Then the C audit.

#### C3a plan — modifiers on the sheet (approved 2026-09-24) — built 2026-09-24

**As built.** The plan below holds, with these specifics:

- **Code:**
  - `Dnd5eModifier(type, target, value, ability, classSlug, source)`: `classSlug`
    is set only for class-scoped per-level bonuses;
  - `Dnd5eModifierType` and `Dnd5eModifierTarget`;
  - `Dnd5eModifiers`: the sheet's active list, with bonuses, highest base, HP
    bonuses and extra attacks;
  - `Dnd5eFeatureTrait.modifiers`: `modifiersOrEmpty()` covers old sheets, and
    `withUsedCount` keeps the modifiers;
  - `FeatureOutcome` gains `modifiers` and `classSlug`;
  - `VitalsZone.attacksPerAction` / `CharacterSheetResponse.attacksPerAction`.
- **AC details:**
  - an unarmored base only replaces 10 when it is higher;
  - labels: "Base (Draconic Resilience)", "Fighting Style: Defense";
  - HP contributions read "Tough (+2 × 20)".
- **Tests:**
  - `Dnd5eModifiersCalculatorTest`: armored bonus, unarmored base, character
    vs. class levels (and that `Dnd5eFormulas` agrees), highest Extra Attack;
  - `Dnd5eBuildMaterializerTest`: modifiers copied with their source and class;
  - `FiveEToolsMechanicsOverlayTest`: feat target by name + source, and failure
    on a typo.
- **Verified live** after regenerating and importing classes, subclasses, feats
  and optional features, and re-applying the three builds:
  - **Aria:** 264 HP, "Attacks per Action: 4", AC 13 unequipped and 19 with
    Chain Mail + Shield. Both were equipped for the check and then unequipped.
  - **Vex:** 45 HP, AC 17 unarmored.

**Vocabulary.** D&D Beyond's names are the naming reference: `ModifierTypeEnum` for
`type` and `ModifierSubTypeEnum` for `target`. Only what the target characters need
goes in:

- **`Dnd5eModifierType`:**
  - `BONUS` adds;
  - `SET` means the highest wins;
  - `SET_BASE` is a base value that competes with armor, highest wins.
- **`Dnd5eModifierTarget`:**
  - `ARMOR_CLASS`;
  - `ARMORED_ARMOR_CLASS`, which applies only while wearing armor (D&D Beyond's
    `armored-armor-class`, used by Defense);
  - `UNARMORED_ARMOR_CLASS` (a `SET_BASE`, plus Dex, used by Draconic Resilience
    and later Unarmored Defense);
  - `HIT_POINTS_PER_LEVEL`;
  - `EXTRA_ATTACKS`.
- **`Dnd5eModifier`** fields: `type`, `target`, `value`, an optional `ability`
  (which adds that ability's modifier, e.g. Unarmored Defense's Con), a
  `levelScope` of `CHARACTER` or `CLASS`, and `source`.
  - The level scope matters: Tough counts character levels, while Draconic
    Resilience counts Sorcerer levels ("increases by 1 again whenever you gain a
    level in this class").
  - For `CLASS`, the materializer records which class.

**Overlay.**

- `FiveEToolsMechanicsOverlay` also reads `mechanics/feats/<slug>.json` and
  `mechanics/optional-features/<slug>.json`, targeted by slug. The same "must
  match, must cite" rule applies. Their `modifiers` are merged into the feat and
  optional-feature catalogue data.
- The first files, each citing book and page:
  - `class-features/fighter-extra-attack.json`, plus `(2)` and `(3)`:
    `SET EXTRA_ATTACKS 1/2/3`;
  - `optional-features/defense.json`: `BONUS ARMORED_ARMOR_CLASS 1`;
  - `feats/tough.json`: `BONUS HIT_POINTS_PER_LEVEL 2`, `CHARACTER`;
  - `subclass-features/sorcerer-draconic-draconic-resilience.json`:
    `BONUS HIT_POINTS_PER_LEVEL 1` (`CLASS`), and `SET_BASE
    UNARMORED_ARMOR_CLASS 13`.

**Sheet.**

- `Dnd5eFeatureTrait` gains `modifiers` (empty on old sheets). The materializer
  copies them from the feature, feat or optional feature, which is copy-once,
  like the rest of the catalogue data.
- The modifiers sit on the traits instead of in a separate list, so removing a
  feature removes its effects.

**Calculator.** `Dnd5eModifiers` collects the sheet's active modifiers (C3a: feature
traits only; C3c–C3e add conditions, effects and items). Each value filters that
list by target, and each applied modifier becomes a `Contribution` labelled with its
source.

- **AC:**
  - with armor: armor + Dex (capped) + shield + `ARMORED_ARMOR_CLASS` bonuses +
    `ARMOR_CLASS` bonuses;
  - without armor: the highest of 10 and every `UNARMORED_ARMOR_CLASS` base,
    then + Dex + that modifier's ability + shield + `ARMOR_CLASS` bonuses.
- **Max HP:** + `HIT_POINTS_PER_LEVEL` × (character or class level), in
  `Dnd5eFormulas.maxHitPoints` too, so damage and healing caps agree.
- **Attacks per action:** 1 + the highest `EXTRA_ATTACKS`. It goes into
  `VitalsZone`, and the Actions tab reads it instead of the fixed constant.

**Expected on the target characters:**

| Character | Change |
| --- | --- |
| Aria | 264 HP; 4 attacks per action; with Chain Mail + Shield equipped, AC 16 + 2 + 1 (Defense) = 19 |
| Vex | 45 HP; unarmored AC 13 + 4 (Dex) = 17 |
| Liriel | no change |

**Tests:**

- calculator tests for each target;
- materializer copying modifiers;
- the overlay's feat and optional-feature targets;
- the three builds re-applied and checked live.

**Out of scope:** species-trait modifiers (none needed yet), uses (C3b),
conditions (C3c), active effects (C3d), items (C3e).

#### Stage C progress — handoff, 2026-09-24

The owner approved every remaining Stage C step at once ("pode seguir com todos os
passos da etapa C").

**Done:**

- **C3a** — feature modifiers: AC, HP, attacks per action. Built, verified live.
- **C3b** — feature uses and actions: built. `:apps:api:check` is green and the
  real-data materialization is checked offline (Aria: Second Wind 1, Action Surge
  2, Superiority Dice 6 d12, Indomitable 3; Vex: 3 Sorcery Points, Shapechanger
  and Cunning Action as actions; Liriel: Arcane Recovery 1). What was built:
  - `Dnd5eFeatureUses` combines the counters;
  - `Dnd5eFeatureAction.traitKey` makes one counter shared by both tabs, and
    the mutator routes spending and restoring to the trait;
  - the overlay carries `uses` and `action`, and species `traits` overlays;
  - 14 new overlay files under `content/dnd-5e/mechanics/`.

  Classes (14), subclasses (130) and species (134) are imported into Postgres,
  and the three builds are re-applied (REPLACED). Verified live (below).
- **C3c backend** — conditions and roll modes: built, with `:apps:api:check`
  green at 362 tests.
  - A new `CONDITION` catalogue kind (`ConditionConverter`, from 5etools
    `conditionsdiseases.json`).
  - Overlay files in `mechanics/conditions/`: Blinded, Frightened, Grappled,
    Invisible, Paralyzed, Petrified, Poisoned, Prone, Restrained, Stunned,
    Unconscious, and Exhaustion with per-level `levels`.
  - Copy-once flow: `CharacterSheetService.toggleCondition`/`setExhaustionLevel`
    read the condition's catalogue data (`CatalogueService.findData`) and pass
    it to the new `SheetMutator` overloads. The sheet stores it in
    `conditionModifiers`.
  - New modifier types: `HALVE`, `ADVANTAGE`, `DISADVANTAGE`.
  - New targets: `HIT_POINT_MAXIMUM`, `SPEED`, `ATTACK_ROLLS`, `ABILITY_CHECKS`,
    `STRENGTH_ABILITY_CHECKS`, `SAVING_THROWS`, `STRENGTH_SAVING_THROWS`,
    `DEXTERITY_SAVING_THROWS`.
  - `Dnd5eModifiers` reads features, conditions, active effects and Overloaded.
  - `VitalsZone.rollModes` (`RollModeInfo`) is computed by the calculator.
    `RollService` applies the forced mode and no longer has a special case for
    Overloaded.
  - Speed and max HP apply `SET`/`HALVE`.
  - `Dnd5eSheet` gained `conditionModifiers` and `activeEffects` (the latter for
    C3d), and `Dnd5eActiveEffect` exists.

- **C3c frontend** — built. Placement follows D&D Beyond's own source
  (`design-reference/markup/.../Skills.tsx`, `InitiativeBox.tsx`,
  `SavingThrowsDetails.tsx`):
  - `RollModeMarker` reuses `dnd_icon_advantage.svg`/`dnd_icon_disadvantage.svg`
    (already green/red) through `FrameIcon`; the tooltip names the sources.
  - Skills: a 17 px `.skill-row__adjustments` column between the name and the
    bonus (`.ct-skills__col--adjustments`).
  - Initiative: a 20 px badge at the box's bottom-left (`InitiativeBox .advantage`).
  - Saving throws: one "on **Ability** (sources)" line per forced save below the
    grid (`.ct-saving-throws-box__modifiers`), not a marker in the row.
  - Attacks: no marker — D&D Beyond shows none on attack rows; `RollService`
    still applies the forced mode.
  - `RollModeMarker.test.tsx`; `npm test` (17) and `npm run build` pass.
  - 15 conditions ingested (`--kind=condition`, PHB only) and imported.

**Verified live on Aria:**
- Poisoned + Restrained marked every skill and initiative, with a "D on
  **Dexterity** (Restrained)" line under the saves. Speed dropped to 0.
- Acrobatics rolled `2d20kl1+3`.
- Tooltips named the right source.
- C3b counters: Second Wind 1, Action Surge 2, Indomitable 3, Combat
  Superiority d12 × 6.
- Both conditions were turned off again afterwards.

- **C3d** — active effects: built.
  - **Overlay:** `mechanics/spells/<slug>.json`, targeted by spell name plus
    source, carries `effect: {modifiers, endsOnRests, durationText}`.
    `SpellConverter` merges it into the spell's `data.effect` (null for every
    other spell). The first file is `mage-armor.json` (PHB 256):
    `SET_BASE UNARMORED_ARMOR_CLASS 13`, `endsOnRests: []`, "8 hours".
  - **Copied at cast time, not onto the build's spells** (a change from the
    plan above). `CharacterSheetService.castSpell` reads the spell's catalogue
    data and passes it to `SheetMutator.castSpell`, the same copy-once flow C3c
    uses for conditions. `Spell`, `Dnd5eSpell` and the learn/materialize paths
    stay unchanged, and characters that already know the spell get the effect
    without a re-apply.
  - **`castSpell(spellKey, slotLevel, spellData)`:**
    - spends the slot;
    - any concentration spell ends the current concentration effect, even one
      with no tracked effect (Hold Person ends a concentration buff);
    - recasting replaces the spell's own effect;
    - modifiers are labelled with the spell's name ("Base (Mage Armor)").
  - **Other mutations:** `endActiveEffect(effectKey)`. Short and long rests drop
    only the effects whose `endsOnRests` contains a matching trigger.
  - **API:**
    - `POST /spells/{spellKey}/cast` with `{slotLevel}` (0–9);
    - `POST /active-effects/{effectKey}/end`;
    - `VitalsZone`/`CharacterSheetResponse.activeEffects` (`ActiveEffectInfo`).
  - **UI:**
    - the Cast button sends `CAST_SPELL`;
    - the Conditions summary line includes effect names;
    - the Conditions sidebar lists "Active Effects" above the conditions, with
      the duration, concentration, the rest that ends it, and an End button.
    - The Conditions panel has a fixed frame, so the list lives in the sidebar
      rather than in the panel.
  - **Tests:** `Dnd5eActiveEffectsTest` (7), two spell overlay cases in
    `FiveEToolsMechanicsOverlayTest`, three `CharacterControllerTest` cases, and
    `ConditionsPanel.test.tsx` (3). `:apps:api:check` is green at 374 tests;
    `npm test` (20) and `npm run build` pass.
  - **Verified live on Liriel:**
    - Cast Mage Armor at 1st level: AC 13 → 16, with the breakdown
      "Base (Mage Armor) +13, Dexterity modifier +3". One 1st-level slot was
      spent, and the summary and sidebar showed "Mage Armor · 8 hours".
    - A long rest kept the effect.
    - End set AC back to 13.
    - Liriel was left as found.
  - **Self/Ally target** (the owner asked for it, 2026-09-24):
    - The Cast panel shows a "Target: Self | Ally" row, using the shared
      `FilterChips`, only for spells with a lasting effect. The frontend reads
      that from the catalogue data it already loads (`lastingEffectSpells.ts`
      context, `data.effect != null`).
    - `castSpell(..., onSelf)`. An ally effect is kept under the key
      `<spell>-ally`, so the same spell can be on the character and on an ally
      at once. It is tracked for its duration and concentration, but its
      modifiers stay out of the sheet (`Dnd5eActiveEffect.onSelf`; null on older
      sheets means Self).
    - The Conditions summary shows "Mage Armor (Ally)". The sidebar rows read
      "On you · 8 hours" and "On an ally · 8 hours", and their End buttons are
      labelled with the target.
    - End stays in the Conditions sidebar only (the owner confirmed).
    - Verified live on Liriel: an ally cast left AC at 16 and listed both
      effects; ending the ally one left the self one.
    - The sidebar's floated close button no longer narrows the first effect
      row.

- **C3e** — item mechanics: built.
  - **Scope, from a survey of the 1,755 2014-era items:** only fields with an
    unambiguous meaning are mapped, by `ItemMechanicsMapper` into
    `data.mechanics`, 198 items in all.

    | Field | Becomes |
    | --- | --- |
    | `bonusAc` on non-armor items (63 items with `bonusAc`, armor's own already counted) | `BONUS ARMOR_CLASS` |
    | `bonusSavingThrow` (23) | `BONUS SAVING_THROWS` |
    | `bonusSpellAttack` (41) | `BONUS SPELL_ATTACKS` |
    | `bonusSpellSaveDc` (28) | `BONUS SPELL_SAVE_DC` |
    | `ability.static` | `SET <ABILITY>_SCORE` (the higher score wins) |
    | `resist` / `immune` / `vulnerable` / `conditionImmune` (plain names only) | defense lists |

  - **Left out:**
    - additive ability scores: `{"con":2}` means both a worn Ioun Stone and a
      one-time Manual of Bodily Health, and the data can't tell them apart;
    - potions (consumables);
    - `bonusAbilityCheck` (2 items), `bonusProficiencyBonus` (1),
      `modifySpeed` and `critThreshold`;
    - conditional resistances (objects).
  - **Sheet:**
    - `Dnd5eItem.mechanics` (`Dnd5eItemMechanics`) is copied once through the
      new `SheetMutator.addCatalogueItem(sheet, item, itemDataJson)`, used for
      both player-added and starting items;
    - `copyItem` keeps it;
    - `Dnd5eItem.active()` means equipped, and attuned when required.
  - **Calculation:**
    - active items' modifiers join `Dnd5eModifiers`;
    - saves, spell attack and save DC read their bonuses;
    - defenses are merged;
    - `Dnd5eFormulas.withItemAbilityScores` raises set scores, is applied first
      in the calculator, and is used by `maxHitPoints` so HP and the healing cap
      agree.
    - New targets: `SPELL_ATTACKS`, `SPELL_SAVE_DC`, `<ABILITY>_SCORE`.
  - **Tests:**
    - `ItemMechanicsMapperTest` (6) and `Dnd5eItemMechanicsTest` (7);
    - `:apps:api:check` is green at 390 tests.
    - Items were regenerated (1,773) and imported.
  - **Verified live on Aria** with a Cloak of Protection from the catalogue:
    - equipped only: nothing changed;
    - equipped and attuned: AC 13 → 14 ("Cloak of Protection +1"), every save
      +1, and a Dexterity save rolled `1d20+4`;
    - the cloak was then removed, and Aria is back to AC 13.
  - **Existing sheets:** magic items added before C3e have no mechanics until
    they are added again. The three target characters carry none.

- **C2a** — provenance on the sheet: built.
  - **Backend:**
    - `Dnd5eProvenance` reads `Dnd5eSheet.derivation`: ability contributions,
      speed, grant sources, and hit dice per level with consecutive average
      levels merged ("Rogue 2–4 (average d8 × 3)").
    - A stored value that no longer matches its derivation gets an "Other"
      line. A sheet with no derivation shows "Base".
    - `VitalsZone.provenance` (`Provenance`: ability-score `CalculatedValue`s
      including an item's "Set Score", a speed `CalculatedValue` with C3
      SET/HALVE changes, and `ProficiencySource`s). Max HP lists each level.
    - Save and skill contributions name their source ("Proficiency bonus
      (Rogue 1)", "Proficiency bonus ×2 (Expertise, Rogue 1)").
    - `SourcedGrant` gains the `label` the proficiency lists show. The
      materializer's label code is split into per-key helpers so the lists
      and the derivation share it.
  - **UI:**
    - Clicking an ability box shows "Total Score N" and its sources.
    - The speed box shows its source and changes.
    - The HP panel has a "Max HP breakdown" section (`<details>`).
    - Proficiencies & Training shows "From <source>" tooltips.
  - **Compared live with D&D Beyond's Ability panel** (Helga, STR): theirs is a
    fixed table (Total Score, Modifier, Base Score, Bonus, Set Score, Stacking
    Bonus, Other Modifier, Override Score). Ours lists each named source instead.
    The total and modifier sit in the same title. The override fields are left
    out, as planned.
  - **Tests:** `Dnd5eProvenanceTest` (6), a materializer label assertion, and
    `ProficienciesPanel.test.tsx` (3). `npm test` (27) passes.
  - **Verified live on Vex:**
    - DEX 18 = Base 15 + Changeling 2 + ASI (Rogue 4) 1;
    - Stealth "Proficiency bonus ×2 (Expertise, Rogue 1)";
    - speed "Changeling +30";
    - Max HP 45 = 8 + 15 + 12 + Con 7 + Draconic Resilience 3;
    - tooltips "From Rogue 1", "From Background", "From Changeling".
  - **Re-applying builds resets play state.** The dev `--apply-builds` runner
    recreates each character from scratch (D1a design). Liriel's Mage Armor
    from the owner's test was wiped and then restored by casting it again.
  - **Small follow-up:** background grants are labelled "Background" rather
    than the background's name ("Urchin"); that label comes from the planner.

- **C2b** — per-class hit dice: built.
  - **Storage:** `Dnd5eClassLevel.hitDiceUsed` per class (D&D Beyond also
    tracks it per class). `Dnd5eSheet.hitDiceUsed` stays the total.
    `Dnd5eHitDicePools.normalized` reads a legacy total-only count as spent
    dice of the largest sizes, and the materializer carries each class's count
    across a rematerialization.
  - **Pools:** `HitDice` gains `pools` (one per die size, largest first; classes
    sharing a size share a pool) and `longRestRecoveryMax`. The old
    `dieSize`/`max`/`used` fields are now the summary.
  - **Rolls:** `RollKind.HIT_DICE` takes the die size as its key. The context
    reads "Hit Die (d6): heal".
  - **API:**
    - `POST /hit-dice/spend` takes an optional `dieSize`;
    - `POST /rest/short` takes `hitDiceBySize` and returns `rolls` (one per
      size) as well as `roll`;
    - `POST /rest/long` takes an optional `hitDiceRecovered`, the player's
      choice. It is checked against half the dice and against what was spent,
      and a bad choice returns 400 `InvalidHitDiceRecoveryException`. Without a
      choice, the largest dice come back first.
  - **UI:**
    - the Short Rest panel shows one Hit Dice box and one "to spend" field per
      size;
    - the Long Rest panel shows per-size "to recover" fields, only when there is
      a real choice (spent dice of several sizes, more spent than the limit),
      prefilled largest first and never over the limit.
  - **Tests:**
    - `Dnd5eHitDicePoolsTest` (9);
    - four new or updated `CharacterControllerTest` cases;
    - `RestBody.test.tsx` (4);
    - `:apps:api:check` is green at 408 tests; `npm test` (31) and
      `npm run build` pass.
  - **Verified through the API on Vex:** a short rest with `{8: 2, 6: 2}`
    rolled `2d8+2` and `2d6+2` separately and left pools d8 2/4 and d6 2/3
    spent. The Short Rest panel showed "4 / 4 Hit Dice (d8)" and "3 / 3 Hit
    Dice (d6)" with a field each.
  - **Verified live on Vex** (after a fresh login):
    - a short rest from the UI spent 1 d6 and rolled "Hit Die (d6): heal"
      `1d6+1`;
    - with 2 d8 and 3 d6 spent, the Long Rest panel offered the choice,
      prefilled "d8 2, d6 1" ("0 left");
    - choosing "d8 0, d6 3" brought back all three d6 and left the two d8
      spent;
    - with only d8 spent, the panel showed the plain button.
    - The token expired again before a last long rest, so **Vex still has 2 d8
      spent**. The 5-minute token is recorded in `open-items.md`.
  - **Dev-server note:** the first UI attempts failed with `hitDiceSpent is not
    defined`, because Vite served a half-written `api.ts` (an edit written in two
    steps). Touching the file fixed it; the source, tests and build were
    correct.

- **The C audit** — first pass done 2026-09-24:
  - see `systems/dnd-5e/references/data-fidelity-audit.md`;
  - it produced eight findings (F1–F8), three of them owner decisions;
  - its values come from the new dev runner `--print-sheets`
    (`CharacterSheetPrintRunner`).

**Remaining:**

- Nothing in Stage C. The audit closed on 2026-09-24: F2–F7 were fixed, F1 and
  F8 accepted by the owner, and the visible changes checked live against D&D
  Beyond (`systems/dnd-5e/references/data-fidelity-audit.md`). Next is Stage D2, the
  in-app creation flow.
- **The C audit** — the data-fidelity audit on Aria, Liriel and Vex.
- **After each slice:** regenerate and import the catalogue, re-apply the builds
  (`--apply-builds=../../content/dnd-5e/builds --player=test`, with the API
  stopped because the runner needs port 8090), check live, and update the docs
  and changelog.

#### C3b plan — feature uses and actions (approved 2026-09-24) — built 2026-09-24

**What the target characters need** (PHB, via the adr-0007 overlay):

| Character | Feature | Uses | Recharge | Action type |
| --- | --- | --- | --- | --- |
| Aria | Second Wind | 1 | short or long rest | bonus action |
| Aria | Action Surge → "(two uses)" at 17 | 1 → 2 | short or long rest | other |
| Aria | Indomitable → "(two uses)" at 13 → "(three uses)" at 17 | 1 → 2 → 3 | long rest | other |
| Aria | Combat Superiority + "Additional Superiority Die" at 7 and 15 | 4 + 1 + 1 = 6 superiority dice (d12 by level 18) | short or long rest | none (spent by maneuvers) |
| Vex | Font of Magic: sorcery points | the class table's "Sorcery Points" column at Sorcerer 3 = 3 | long rest | none |
| Vex | Cunning Action | at will | none | bonus action |
| Vex | Shapechanger (Changeling) | at will | none | action |
| Liriel | Arcane Recovery | 1 | long rest (the text says "once per day") | other |

**How D&D Beyond models it:** a feature's `limitedUse` carries a max (a fixed
number, a class-table value, an ability modifier or the proficiency bonus) plus a
reset type. The feature's action rows point at that same limited use, so the
Actions tab and the Features tab show **one** counter.

**Overlay additions** (the same per-feature files, each citing book and page):

- **`uses`:**
  - `{"resource": "superiority-dice", "count": 4, "recharge": "SHORT_OR_LONG_REST", "die": 8}`;
  - a feature can `add` to another's resource: Additional Superiority Die is
    `{"resource": "superiority-dice", "add": 1}`, and Improved Combat Superiority
    sets `"die": 10` / `12`;
  - a feature can `set` a higher count: Action Surge (two uses) is
    `{"resource": "action-surge", "count": 2}`;
  - `count` can instead come from a class table column (`"fromTableColumn":
    "Sorcery Points"`). Ability- and proficiency-based counts
    (`"ability": "charisma"`, `"proficiencyBonus": true`) are part of the
    vocabulary but not used yet.
- **`action`:** `{"type": "BONUS_ACTION"}`, which puts a row in the Actions tab.
- Species traits get the same overlay target (by species, source and trait
  name), for Shapechanger.

**Materialization:**

- For each resource, the counts are combined (the highest `count`, plus every
  `add`) using the class levels the build reached. The result is written to the
  trait that defines the resource: `maxUses`, `rechargeTrigger`, and the die
  shown in its summary.
- Used counts survive re-materialization, as they already do.
- A feature with an `action` also gets a `featureActions` row.

**One counter per feature (the design change).** `Dnd5eFeatureAction` gains a
`traitKey`.

- When it's set, the action row reads its uses from that trait, and
  spending or restoring from either tab changes the trait.
- Hand-made feature actions (no `traitKey`) keep their own counter, as today.
- Short and long rests already restore traits by `rechargeTrigger`, so nothing
  changes there.

**Tests:**

- overlay parsing;
- resource combination: Additional Superiority Die, Action Surge (two uses), the
  Sorcery Points column;
- the materializer creating action rows with `traitKey`;
- the mutator spending a linked action on its trait;
- rests;
- the three builds re-applied and checked live: Aria's Actions tab shows Second
  Wind 1, Action Surge 2 and Indomitable 3, and Vex has 3 sorcery points.

**Out of scope:**

- maneuvers spending superiority dice automatically, and metamagic spending
  sorcery points; the player spends from the counter;
- features that consume spell slots;
- recharge on dice ("Recharge 5–6").

**This stage ends with a data-fidelity audit**, the same shape as phase 10's own
`systems/dnd-5e/references/sheet-fidelity-audit.md` but checking numbers against 5etools
instead of layout against D&D Beyond: for a small set of real, fully-built
characters (not just Aria), every value on the vitals zone and all six tabs is
traced to either a real 5etools fact or a real resolved choice, with no exceptions
left unexplained. Those characters are produced with Stage D1's build files, and
the audit also walks through every Target outcome scenario on each of them.
**This audit passing is the literal definition of "the sheet is
100% correct" the owner's gate above refers to** — Stage D2 does not start before
it does.

## Stage D1 — Assistant-driven rebuild (the Target outcome)

The first consumer of the build resolver is a conversation, not a UI. It builds
the audit characters, so it is **part of the path to the Stage C gate**, not
gated behind it.

- **Build files** live in version control under `content/dnd-5e/builds/`, for
  example `aria.json`. Each file is a `Dnd5eCharacterBuild` in JSON: references
  plus resolved choices, never derived values. A build can be replayed after any
  rules or catalogue change.
- **`planBuild`**, a Gradle task mirroring `ingest5etools`
  (`--args="--build=<file>"`), runs the resolver and prints the pending choices as
  JSON, or reports that none are left.
- **`--apply-builds`** writes the build files' characters for the local `test`
  user. `DevCharacterSeeder` and its hand-typed JSON were deleted by owner
  decision (no default characters), which absorbs Stage E for the dev
  characters.

The conversation loop:

1. The owner names the build.
2. The assistant writes the skeleton build file.
3. The assistant runs `planBuild`.
4. The assistant asks each pending choice, with options taken verbatim from
   `planBuild`'s output.
5. The assistant records the answers in the build file.
6. Steps 3–5 repeat until nothing is pending.
7. Restart the API and verify the result live.

**Target characters (owner, 2026-09-23).** These are D1's deliverables and Stage
C's audit characters:

1. **Aria Emberfall**, overwriting today's hand-typed dev character: Fighter
   (Battle Master) 20, Mountain Dwarf, Folk Hero.
2. **A new character:** Wizard 5, Elf, Urchin. The Elf subspecies is asked during
   the build.
3. **A new multiclass character:** Sorcerer 3 / Rogue 4, Changeling, Urchin.
   - Which Changeling is asked during the build: the catalogue holds the ERLW and
     MPMM versions; EFA's is 2024 and excluded.
   - Class order (which class was taken at level 1) is asked too, because it
     decides the starting proficiencies.

**Minimum to run it:** A1–A3, the parts of Stage B that the build references,
and C1. The living-sheet scenarios (Target outcome 2–6) additionally need C3.

### D1 plan (approved 2026-09-23, with owner decisions)

**Owner decisions, 2026-09-23. These replace the matching parts of the plan
below.**

- **Aria starts from scratch.** Applying a build over an existing character
  replaces the whole sheet with a freshly materialized one plus the starting
  equipment. None of today's showcase data is kept. The play-state-preserving
  re-materialization stays in `CharacterCreationFlow` for later use (C3/D2),
  but `--apply-builds` doesn't use it.
- **No default characters.** No user gets a seeded character, in dev or
  otherwise. `DevCharacterSeeder` is deleted. The three target characters
  exist only for the local `test` user, applied with
  `--apply-builds=<dir> --player=<username>`.

D1 is split into an infrastructure slice (D1a) and the conversations (D1b).

**D1a as built (2026-09-23):**

- **Build files:** `CharacterBuildFile(characterName, systemId, build)`. `planBuild`
  also accepts the wrapped files and unwraps `build`.
- **Catalogue:** `CatalogueService.lookup(systemId)` returns a
  `DatabaseCatalogueLookup`, which reads each kind once per lookup.
- **Applier:** `CharacterBuildApplier` (character package) always materializes
  from scratch, then adds starting items and coins through
  `CharacterSheetService.withStartingEquipment`:
  - catalogue items go through the same copy as a player's own item, and a
    free-text item goes in as a custom item;
  - the item's display text goes in its notes;
  - coins split into gp / sp / cp.

  A character of the same name is replaced; otherwise one is created. A build
  that isn't ready throws `BuildNotReadyException`, naming the pending ids and
  the problems.
- **Runner:** `CharacterBuildRunner` (dev profile) applies every file in a
  directory to one player:
  `bootRun --args='--apply-builds=../../content/dnd-5e/builds --player=test'`.
  The player must have logged in once. Players are found by display name
  (`PlayerService.playersNamed`).
- **Seeder removed:** `DevCharacterSeeder` and its test are deleted.
  `PlayerCreatedEvent` still fires, but nothing listens to it.
- **Fix found on the way:** catalogue `sourceBook` values are full book names, so
  C1c's "prefer PHB" rule and `source=` filters never matched. `SpellConverter`
  now also writes the 5etools `sourceCode`, and `Dnd5eSpellLists` uses it.
- **Tests:**
  - `CharacterBuildApplierTest` (Testcontainers): creates with items and coins,
    replaces from scratch, refuses pending;
  - `Dnd5eSpellListsTest`: source code and PHB preference.

**D1b as built (2026-09-23):**

- **`content/dnd-5e/builds/aria-emberfall.json`**, answered by the owner in two
  rounds: STR standard array; brewer's supplies (species), mason's tools and a
  brewer's supplies kit (Folk Hero); Intimidation and Perception; Defense;
  carpenter's tools (Student of War); 9 maneuvers; Chain Mail, Warhammer +
  Shield, 2 Handaxes, Explorer's Pack. ASI/feats: +2 STR, +1 STR/+1 DEX,
  Durable, Tough, +2 CON, +2 DEX, Dwarven Fortitude. Result: STR 20, DEX 16,
  CON 20, 224 HP, 54 features.
- **`liriel-moonwhisper.json`** (High Elf Wizard 5, Evocation, Urchin) and
  **`vex.json`** (Changeling MPMM, Rogue 4 Thief → Sorcerer 3 Draconic/Red,
  Urchin). The owner delegated every choice to the assistant, keeping the
  standard array and starting equipment.
  - **Liriel:** INT 18, 27 HP, a 14-spell book with 9 prepared, and a High Elf
    Minor Illusion.
  - **Vex:** DEX 18, CHA 16, 42 HP, expertise in Stealth and Thieves' Tools, 4
    cantrips and 4 spells.
- All three were applied to `test` with `--apply-builds`: Aria replaced, the
  other two created.
- **Checked live:**
  - Aria's stats, proficiencies and senses;
  - Liriel's Actions and Spells tabs: cantrips, slots, attack and DC.

  Findings are in `open-items.md`: starting gear unequipped (AC 13), the
  species caster repeated in the Spells header, Tough HP and Extra Attack (C3).

**D1a — apply build files to characters.**

- **Build file format:** `content/dnd-5e/builds/<slug>.json`, shaped
  `{"characterName", "systemId", "build": {…}}`. The wrapper is generic;
  `build` is the system's own build (`Dnd5eCharacterBuild` for D&D 5e). A file
  holds references and answers only, never derived values.
- **`DatabaseCatalogueLookup`** in `catalogue` implements the `ruleset` port
  `CatalogueLookup` over `CatalogueService`, so the running API materializes
  from Postgres. `ContentDirectoryCatalogue` stays for the `planBuild` CLI.
- **`CharacterBuildApplier`** in `character` uses only generic types:
  `CharacterCreationFlowRegistry` → `materialize`.
  - It refuses a build with pending choices or problems, and reports them.
  - **New character:** it writes the sheet and adds the starting items through
    the existing catalogue item-copy path (`CharacterSheetService`). Starting
    coins are added as gp / sp / cp.
  - **Existing character with the same name:** it re-materializes over the
    current sheet (Stage B option (a)). Play state is kept (see owner question
    1), and starting items are not added again.
  - `Character` records the build: the sheet JSON already carries `build`.
- **Triggers (dev profile only):**
  - **`DevCharacterSeeder`** creates every character in `content/dnd-5e/builds/`
    for a new player, replacing the hand-typed seed JSON. This absorbs Stage E
    for dev characters.
  - **`--apply-builds=<dir>`**, a command-line runner mirroring
    `--import-catalogue`, applies every build file to every existing player:
    overwrite by name, create otherwise. That's how today's Aria gets rebuilt
    without deleting the player.
- **Tests:**
  - the applier (create, overwrite keeps play state, refuses pending);
  - `DatabaseCatalogueLookup` against Testcontainers;
  - a seeder test that the three build files materialize with no problems.

**D1b — the conversations, one character at a time.** This follows the loop
above:

1. The skeleton goes in the build file.
2. `planBuild` runs.
3. The owner is asked every pending choice in chat, with its options taken from
   `planBuild`: long lists (spells, feats) are shown grouped, with their source.
4. The answers are written, and steps 2–4 repeat until nothing is pending.
5. `--apply-builds`, then a live check at localhost:5173 against D&D Beyond.

The order is Aria, then the Elf Wizard, then the multiclass. Structural
questions come first: ability score method and scores, Changeling version,
class order, subclass.

**Owner questions:**

1. **Aria's current play state.** Today's Aria holds showcase data put there to
   exercise the UI: a Warhorse extra, attuned and unattuned items, spent slots
   and hit dice, 18 spells covering every school and damage type, and the
   invented "Battle Fervor" feature for the large-use stepper. A Battle Master
   has no spells, so re-materializing regenerates her spells (none) and her
   features.
   - **Option (a):** keep her inventory, extras, coins and background text, as
     Stage B option (a) says.
   - **Option (b):** start clean, with only her starting equipment.
   - **Recommendation:** (a). The rebuilt character stays realistic, and the
     inventory and extras rows still exercise the UI. The spell and feature
     showcase moves to the Wizard and the multiclass, which are real casters.
2. **New players' seed.** Should a new player get all three characters from
   build files (recommended), or only Aria?

**Out of scope:**

- effects (C3);
- per-size hit dice (C2);
- pact slots;
- the D2 creation UI.

## Stage D2 — Guided creation flow in the UI (phase 11 proper, gated on A + C)

D&D Beyond's own creation flow is the fidelity target here, same as the sheet
itself (`systems/dnd-5e/sheet-ui.md`'s standing rule extends to this new surface, not just
the sheet): species → class → background → ability scores (standard array, point
buy, and manual entry — D&D Beyond's own three methods) → choices (skill
proficiencies, fighting style, cantrips/spells known, starting equipment package
A/B, feat vs. ability score increase at an applicable level) → review → create.
Every `Dnd5eChoice` from Stage B renders as a real picker sourced from Stage A's
catalogue data — no hardcoded option list anywhere in this flow. This is also
where `decisions/adr-0006-5etools-as-content-source.md`'s still-open question
(a per-character toggle to exclude 5etools' non-core/collab sources) finally gets
built, per that ADR's own note that phase 11 is where it belongs. Leveling up and
portrait upload (this phase's other two original scope items) build on the same
Stage B/C machinery once it exists.

Research input: `systems/dnd-5e/references/dndbeyond-builder-walkthrough.md` (a live walk through
D&D Beyond's builder, 2026-09-24). It records what works and what doesn't there, and
the owner's answers on step order, drafts, portraits and preferences (§6).

**Layout decided (2026-09-24):** three columns. The step checklist with progress
is on the left, one form page per step in the middle, and the live character
summary on the right. The reference is
`design-reference/mockups/d2-builder/round-2/suggestion-1-three-columns.html`.
The owner still has adjustments to make.

**The visual reference is now round 3** (`design-reference/mockups/d2-builder/round-3/index.html`).
It has:
- the sheet's frame and fonts;
- Beyond-style Abilities and Equipment steps;
- multiclass and higher levels;
- Wear/Wield at creation.

The owner approved its look on 2026-09-24.

### D2 plan (approved 2026-09-24)

#### What already exists

- **Planner:** `CharacterCreationFlow.plan(buildJson)` returns every `CreationChoice`
  (id, type, prompt, options, selected, `isPending`) plus rule problems. It already
  checks the point-buy budget, the standard array, multiclass prerequisites and
  duplicate proficiencies, and it offers the equipment-or-gold choice.
- **Materializer:** `materialize(...)` turns a fully answered build into a sheet
  plus its starting items and coins. `CharacterBuildApplier` shows the full
  write path used by D1.
- **Catalogue:** `GET /api/catalogue?systemId&kind` lists entries.

#### Gaps

1. **No draft.** A character is either a sheet or nothing:
   - `Dnd5eCharacterBuild` requires species, background and at least one class;
   - `characters` has no status column.
2. **No partial preview.** `materialize` refuses a build with pending choices, but
   the live summary needs HP, AC, abilities, saves and attacks while choices are
   still open.
3. **No preferences on the build:**
   - allowed sources and partnered publishers (adr-0006's open toggle);
   - optional class features;
   - feat and multiclass prerequisites;
   - advancement type, encumbrance and coin weight (the last three are
     sheet-level).
4. **No build fields for the round-3 extras:**
   - per-ability "other modifier" and "override score";
   - which starting items begin worn or wielded.
5. **Front end:**
   - no router, since `AppShell` switches screens by state;
   - `CharacterList` has a bare name + system form.
6. **Portrait upload:** `portrait_key` exists, but nothing uploads to MinIO yet.

#### Slices (each a vertical slice with its tests)

**D2a — Draft backend + end-to-end skeleton**
- **Migration** `V5__add_character_status_and_draft.sql`:
  - `status text NOT NULL DEFAULT 'ACTIVE'` (`DRAFT` | `ACTIVE`);
  - `creation_draft jsonb` (nullable).
- **Draft document** (per system; for D&D, `Dnd5eCreationDraft`): a *partial*
  build (every build field nullable), preferences, and builder-only picks.
- **Planner:** accepts a partial build. A missing species, background or class
  becomes a `build.species` / `build.background` / `build.classes` choice instead
  of a validation error.
- **Endpoints:**
  - `POST /api/characters/drafts {name, systemId}` creates a draft;
  - `PUT /api/characters/{id}/draft` autosaves (idempotent);
  - `GET /api/characters/{id}/draft/plan` returns the plan plus the preview (D2b);
  - `POST /api/characters/{id}/draft/finish` materializes, adds the starting
    equipment, applies the worn/wielded picks, sets `ACTIVE` and clears the draft.
    It is refused with the pending list while anything is missing.
- **Scope:** drafts are excluded from the sheet endpoints, and the listing returns
  `status`.
- **Front end:**
  - the builder screen, with the three-column shell, checklist and step
    navigation;
  - autosave (debounced PUT) and a generic choice renderer;
  - "Create character" starts a draft;
  - the listing shows drafts with a "Draft · Continue" badge.

  Every step exists from day one, rendering the plan's choices with the generic
  renderer; the step-specific designs come in later slices.
- **Done when:** a character can be created end to end from the UI (ugly but
  complete) and finished into a working sheet.

**D2a as built (2026-09-24), verified live on 2026-09-25.** A draft "Thorin
Stonehelm" went through every step to Finish:
- Mountain Dwarf with Smith's tools;
- Fighter 1 (Athletics, Perception, Defense);
- Folk Hero with Carpenter's tools (Smith's is filtered out as already known);
- point buy 15/13/14/8/10/12, where an overspend showed "Point buy spends 33 of
  27 points";
- starting equipment picks.

The finished sheet opened with STR 17, CON 16, HP 13, AC 11 (gear unequipped),
STR/CON saves +5, and the right skills, tools, languages, resistance and
darkvision. The walk also led to sorting the catalogue options by name, then by
source. Follow-ups found:
- a step whose only problem is a rule problem (e.g. point-buy overspend) still
  shows ✓ in the checklist, although Finish stays blocked (D2g/D2i);
- the equipment checklist repeats "Choose starting equipment" per group (D2h
  gives the groups real labels).
- **Backend:**
  - `Dnd5eCharacterBuild`: the structural constraints moved to a `Complete`
    validation group, and missing lists and maps read as empty.
    `CharacterCreationFlow.emptyDraft()` was added. `plan` validates the default
    group only; `materialize` validates both.
  - `Dnd5eBuildPlanner` always offers `build.abilityScores`, `build.species`,
    `build.background` and `build.classes` (pending until answered, with the
    catalogue's entries as options). An unknown slug is reported once.
  - Migration `V5__add_character_status_and_creation_draft.sql`; `CharacterStatus`
    and `Character.createDraft` / `saveDraft` / `finish`.
  - `CharacterService.getMineActive` (every sheet and roll path; a draft gets 409)
    and `getMineDraft`.
  - `CharacterDraftService` + `CharacterDraftController`:
    - `POST /api/characters/drafts`;
    - `GET` and `PUT /api/characters/{id}/draft` (the PUT validates by planning,
      then stores, and returns the plan);
    - `POST /api/characters/{id}/draft/finish` (409 with the pending ids while
      anything is missing).
  - `CharacterResponse.status`.
- **Frontend:**
  - `react-router`, with routes `/`, `/characters/:id` (a draft redirects to its
    builder) and `/characters/:id/build/:step?`.
  - The generic `src/builder/`:
    - `CharacterBuilder` (three columns, 600 ms debounced autosave, stale-answer
      pruning, finish);
    - `BuilderChecklist`;
    - `ChoiceField` (chips, or a select above 16 options for a single pick);
    - `builder.css` (the sheet frame as a 9-slice, `builder_section_frame.svg`).
  - D&D in `systems/dnd5e/builder/`:
    - `dnd5eBuild.ts`: step mapping and build edits;
    - `Dnd5eStepPage.tsx`: basics = name, a classes editor with levels and
      multiclass, an abilities method + six scores, and review with the pending
      list and Finish.
  - `CharacterList` links a draft to its builder.
  - **System first (owner requirement, 2026-09-25):**
    - "Create character" opens `/characters/new` (`SystemPicker`), one card per
      supported system (only D&D 5e today);
    - picking a card creates the draft, named "New character" until Basics
      renames it, and opens that system's builder;
    - the listing's old name + system form is gone. It used to do nothing, with
      no message, when the name was empty.
- **Tests:**
  - backend: `CharacterDraftServiceTest` (Testcontainers), plus planner and build
    tests for drafts;
  - web: `dnd5eBuild.test.ts`, `ChoiceField.test.tsx`.
- **Deliberately rough until the later slices:** the summary column only shows
  the name and pending count (D2b); Basics has no rules options (D2c); each step
  uses the generic choice renderer instead of the round-3 designs (D2d–D2h).

**Built 2026-09-25, after the owner's first test of D2a. D2b complete; D2c, D2g
and the D2e spell picker brought forward:**
- **D2b live preview:**
  - `CharacterCreationFlow.preview` → `CreationPreview`. It returns the ability
    scores with their contributions always, plus a sheet materialized with
    pending choices left out once there is a class and every base score.
  - `CharacterDraftService` runs the sheet calculator on it.
  - `DraftResponse.preview` (`DraftPreviewResponse`) holds the abilities, and the
    vitals: HP + hit dice, AC, speed, initiative, proficiency, proficient saves,
    passive Perception, attacks, features, proficiencies, resistances and senses.
  - Web: `BuilderSummary`.
- **D2g abilities, Beyond layout:**
  - `CreationChoiceOption.data` (new, optional) carries each method's rules from
    the planner: array values, point-buy budget and costs, the manual range. The
    web app guides with them but never re-implements them.
  - Build `abilityScoreAdjustments` (`Dnd5eAbilityAdjustment`: other modifier,
    override score) is applied last by the planner, with contributions.
  - Web: `Dnd5eAbilitiesStep`:
    - a method select with a confirm-before-clearing bar;
    - point buy with points remaining, only affordable scores offered;
    - the standard array, with used values removed;
    - manual entry;
    - totals with sourced bonuses;
    - an ASI line per source;
    - Score Calculations cards with Other Modifier and Override Score.
- **D2e spell picker:**
  - Spell options carry their mechanics as `data`: level, school, casting time,
    range, duration, components, concentration, ritual, attack/save, damage and
    effect summary. The description stays behind redaction.
  - Web: `SpellPicker` for CANTRIP / SPELL / SPELLBOOK / PREPARED_SPELL choices:
    - search, level chips, school, damage type, casting time and source filters;
    - Concentration / Ritual / Chosen only;
    - a counter and removable chips;
    - an expandable row with duration, components, save or attack, and effect.
- **D2c Basics:**
  - Build `preferences` (`Dnd5ePreferences`):
    - `sources` (null = every book; PHB and DMG 2014 always allowed);
    - optional class features;
    - feat and multiclass prerequisites;
    - advancement;
    - encumbrance (standard/none, which sets `trackEncumbrance` on a new sheet);
    - ignore coin weight.
  - Builds without preferences keep today's behaviour; new drafts start from
    `forNewCharacter()`.
  - The planner reads the catalogue through `Dnd5eSourceFilteredCatalogue`: lists
    are filtered, lookups by slug are not.
  - `GET /api/catalogue/sources?systemId`.
  - Web: `Dnd5eBasicsStep`:
    - identity and a portrait placeholder;
    - a sources multi-select with locked books, "Enable all" and "Core only";
    - partnered content (none imported, stated plainly);
    - rules switches.
- **Not yet:**
  - partner publishers (no data);
  - portrait upload (D2j);
  - variant encumbrance, and coin-weight/advancement effects on the sheet (both
    stored only);
  - name suggestions (no name data imported);
  - the Classes page's per-level accordion and "Coming next" (rest of D2e);
  - Species/Background/Equipment designs (D2d, D2f, D2h);
  - Review cards (D2i).

**D2b — Live preview (the right column)**
- `CharacterCreationFlow.preview(buildJson)`: materialize with pending choices
  skipped, then run the existing calculator. It returns the summary shape: level
  line, abilities, HP with hit dice, AC, speed, initiative, proficiency, saves,
  passive Perception, attacks, features, proficiencies, defenses and senses.
- The preview honours the worn/wielded picks, which is why AC and attacks move
  when armor is worn.
- Front end: the summary panel, refreshed after each autosave.

**D2c — Basics step**
- A sources endpoint (`GET /api/catalogue/sources?systemId`) lists the imported
  sources grouped core / expanded / partnered (with the publisher).
- PHB 2014 and DMG 2014 are locked on.
- The planner filters options by the allowed sources, the optional-features toggle
  and the prerequisite toggles.
- Rules settings: advancement, fixed/rolled HP, encumbrance, coin weight.
- Name suggestions and the species placeholder portrait.

**D2d — Species step:** species + subspecies selects, the trait list, trait choices.

**D2e — Classes step**
- Class list: level per class, remove, "+ Add another class" with each class's
  prerequisite status, and the starting class marked.
- Per class:
  - a tab with a per-level accordion of choices and features;
  - subclass, ASI-or-feat and spells known live inside their level.
- "Coming next" preview: the class's feature table beyond the current level. It
  comes from a small `GET` over the class's catalogue progression.
- **As built (2026-09-26):**
  - Choices carry a `ChoicePlacement` (class slug, level) from the planner.
  - `Dnd5eClassLevels` shows class tabs and one collapsible section per level.
  - Pooled spells go under "Every level", because they are pooled per class
    since 2026-09-25, not per level.
  - "Coming next" is the class progression panel, served in the draft preview
    rather than by a `GET`. See `systems/dnd-5e/features/builder-refinements.md`, slice 4.
  - Not built: the "+ Add another class" list's prerequisite status, and hit
    points per level in the level summaries.

**D2f — Background step:** background select, grants, tool/language choices.

**D2g — Abilities step (Beyond layout)**
- Method, then point buy / standard array / manual.
- Species ASIs by source.
- Score Calculations cards. The build gains `abilityScoreAdjustments`
  (other modifier, override per ability), which the materializer applies with
  provenance.
- Changing the method asks for confirmation before it clears the scores.

**D2h — Equipment step (Beyond layout)**
- Equipment or gold, A-or-B picks from the plan's `EQUIPMENT` choices, "Add
  starting equipment" / "Clear all".
- The inventory preview uses the draft's starting items, with Wear/Wield toggles.
- Add items from the catalogue, other possessions, currency.

**D2d / D2f / D2h — plan approved 2026-09-26, in the order written.**
Owner answers:
- Currency is shown read-only: the money the character will start with, from
  the background's and the class's starting equipment. The player doesn't edit
  it.
- "Add items" and "Other possessions" come later.

The plan
follows the round-3 mock (`round-3/builder.js`: `species`, `background`,
`equipment`).
- **Shared (server):** the draft preview gains a `selections` block per chosen
  species and background: name, book, "facts", and "grants". The web draws it
  and knows no rule.
  - Facts are label/value pairs: ability bonuses, speed, size, languages,
    senses.
  - Grants are a title plus text: traits, features, proficiencies, equipment.
  - Built from catalogue data. The text is the catalogue's own prose, hidden
    when the catalogue redacts prose, as with the option summaries.
- **Slice 1 — D2d Species:**
  - "Choose": species, subspecies and variant selects, with summaries.
  - A species card: bonuses, speed, size, languages, senses.
  - "Traits": each mechanical trait with its text. The lore traits Age,
    Alignment, Size, Speed and Languages are left out, since the card already
    covers them.
  - "Choices": the species' tool, skill, language and feat picks.
- **Slice 2 — D2f Background:**
  - "Choose": the background select.
  - "What you get": skills, tools, languages, the feature with its text, and
    the equipment.
  - "Choices": the background's picks.
- **Slice 3 — D2h Wear/Wield:**
  - A "Current inventory" list of the starting items, including fixed ones,
    which aren't listed today.
  - Wear toggles for armor and shields, and Wield for weapons.
  - New build field `equippedStartingItems` (catalogue slugs). The
    materializer marks those `StartingItem`s equipped, and finishing equips
    them on the sheet.
  - The draft preview adds the starting items to the preview sheet, so the
    summary's AC and attacks follow what's worn and wielded.
- **Later (owner to decide):** "Add items" from the catalogue, "Other
  possessions" and "Currency" at creation. The sheet already does all three
  after finishing.

**D2d, D2f, D2h — built 2026-09-26.**
- Species and Background use the round-3 layout: "Choose", the
  `SelectionDetail` card and grants, then "Choices".
- The Equipment step adds "Current inventory" with Wear/Wield toggles
  (`equippedStartingItems`) and a read-only "Currency" line. The summary's AC
  and attacks follow the equipped items, and finishing equips them.
- See the changelog's 2026-09-26 entries.
- Checked live on 2026-09-26. Still to do: "Add items" and "Other
  possessions" at creation.

**D2i — Review + Finish:** the missing-choices list, per-step cards, and Finish.
*Built 2026-09-26:* `Dnd5eReviewCards` shows six cards (Basics, Species,
Classes, Background, Abilities, Equipment), each with a pending badge and an
Edit link, all fed by the plan and the draft preview. Checked live on
2026-09-26, together with the Equipment step's Wear/Wield.

**D2j — Portrait upload (MinIO):** upload, stored key, shown on the listing and the
sheet. It is independent of the others and could move later.

*Built 2026-09-25* (plan approved the same day):
- **Presets and uploads.** The 51 default portraits are bundled with the web app
  and stored as `preset:<id>`. Uploads go to MinIO:
  - the bucket is private, and browsers read through 1-hour signed URLs;
  - files are PNG or JPEG up to 3 MB, re-encoded as a centre-cropped PNG of at
    most 512 px.
- **API:**
  - `PUT /api/characters/{id}/portrait` `{presetId}` chooses a preset;
  - `POST` (multipart `file`) uploads;
  - `DELETE` removes;
  - a replaced or removed upload is deleted from storage;
  - `CharacterResponse` and `DraftResponse` carry `portrait` (`{kind, presetId,
    url}` or null).
- **Web:** `PortraitPicker` in Basics has Upload image, then "Pick a default
  portrait", which opens a separate section with the grid, then Remove. The
  portrait shows in the builder summary, the sheet header and the character
  list.
- **Not built:** presets grouped by the chosen species, and deleting a
  soft-deleted character's upload (see `open-items.md`).

**Order:** D2a → D2b → D2c–D2h (any order; suggested as written) → D2i → D2j.

#### Deliberately out of scope for D2

- Level-up on an existing character.
- Editing a finished character through the builder.
- Personality and backstory (written on the sheet).
- Pact slots (their own slice).
- Tasha's "Customize your origin".

#### Owner decisions (2026-09-24)

1. **Routing:** add `react-router`, recorded in `tech-stack.md` when D2a adds it.
   Each step gets its own URL (`/characters/:id/build/:step`).
2. **Level order:** no drag-to-reorder. In 2014 rules only the starting class
   matters (level 1 max HP, full proficiencies, saves), so the Classes step marks
   the starting class and the level strip is informational. The owner didn't
   follow the mock's "drag to reorder" hint; this is the assistant's default,
   open to change at D2e.
3. **Spells:** cantrips, spells known and spellbooks are chosen inside the caster
   class's level accordion. The picker must be **filter-rich**: level, school,
   casting time, range, concentration, ritual, damage type, source, and search. It
   shows enough per spell to decide without leaving the builder.
4. **Plan approved.** Start with D2a.
 — Retire the mocked seed data

Stage D1 already moves `DevCharacterSeeder.java` from hand-typed JSON to resolved
build files, the same choices Stage D2 later exposes in the UI — the exact
migration already proven for spells (`systems/dnd-5e/features/5etools-ingestion.md`'s slice 4:
`DevCharacterSeeder`'s hand-typed spell literals replaced by real catalogue
lookups, verified live). Remove the seeder's remaining hand-typed feature/
background/item literals once Stage A's equivalent real content covers them.

## Sequencing

```
A (ingestion)  ──┐
                 ├──▶ C1 (build resolver) ──▶ D1 (assistant-driven rebuild)
B (domain model)─┘         │                         │
                           ├──▶ C2 (derive flat fields)
                           └──▶ C3 (effects engine)  │
                                                     ▼
                          C audit (5etools facts + Target outcome scenarios)
                                                     │
                                                     ▼
                                    D2 (UI creation flow) ──▶ E (retire remaining mocks)
```

A and B can proceed in parallel. B's new fields have nothing to compute from until
A lands real content, but the record shapes themselves don't depend on A existing.
C1 depends on both. D1 starts as soon as C1 works, and grows with C2 and C3: each
new rule is checked by rebuilding Aria. D2 does not start until C's own audit
passes — see "Status" above.

**Recommended first milestone.** A1 (classes) → A2 (species) → A3 (backgrounds) →
B → C1 → D1, with Aria rebuilt at her current class and level. This is the
smallest path that proves the whole pipeline end to end. C3's effects come next,
one target at a time, starting with AC because it already has contributions and
covers scenarios 2 and 3.
Within A, A1 (classes) is the critical path: A2–A4 can proceed in any order once A1's
converter pattern is proven, but nothing in Stage B or C can derive a class's own
granted proficiencies/features without it.

Work proceeds one slice at a time inside each stage, per `ground-rules.md`'s own
"propose a plan before any change larger than one slice" — this document sequences
the stages, not a commitment to build all of Stage A before proposing A2's own plan.

## Token/cost efficiency strategy

This is by far the largest effort undertaken in this project so far — the following
is deliberate, not incidental:

- **Ingestion is code, not conversation.** The actual per-entry work (thousands of
  classes/species/background/creature entries) runs inside a Gradle task at
  ingestion time, exactly like the three existing converters. The assistant's own
  token cost per new converter is bounded to inspecting a handful of representative
  sample entries to design the field-mapping table — never the full source file.
- **Fork subagents for raw exploration.** Inspecting a large 5etools source file's
  shape (`class-fighter.json`, `races.json`, a `bestiary/*.json` file) happens in a
  forked agent that reports back a compact field list, keeping the raw JSON out of
  the main conversation's own context.
- **One slice, one approval.** Each converter (A1–A4), each domain-model piece (B),
  and each calculator extension (C) is proposed and reviewed on its own, matching
  every prior phase's own discipline — avoids cascading rework if a 5etools schema
  assumption turns out wrong, as already happened once with spells'
  `{@scaledice}` tag.
- **Reuse, never duplicate**, the shared pipeline pieces (`TagMarkupStripper`,
  `FiveEToolsNaming`, `FiveEToolsSourceClassifier`, `FiveEToolsConverter<T>`) and
  the shared "granted fact" shape (Stage B).
- **Batch fidelity verification** per slice, not per field — one live D&D Beyond
  (or 5etools cross-reference) comparison session per converter or calculator
  change, not one per value.
- **Persist findings in `.ai/` as they're made**, not just in conversation — this
  document itself is that principle applied to today's own research, so a future
  session starts from the state above instead of re-deriving it.

## Open questions

- Exact multiclass spell-slot table sourcing (5etools models it per-class; the
  combined multiclass table is a PHB rule this project has to compute itself,
  not read from a single 5etools field) — resolve during A1.
- ~~Whether a subspecies is its own row or nested inside its species.~~
  **Resolved in A2: nested.** See "A2 — species".
## Resolved questions

- **How to encode prose-only mechanics — resolved 2026-09-23:**
  `decisions/adr-0007-curated-modifier-overlay.md`, approved by the owner.
- **Round/turn tracking — resolved by the owner, 2026-09-23:** not tracked. Only
  effects that last until a rest are tracked; see C3's "Active effects".
- **Where the build resolver reads catalogue data from — resolved 2026-09-23.**
  The resolver depends on a `CatalogueLookup` port, with two implementations:
  - `planBuild` reads the ingested `content/dnd-5e/` files directly, so it needs
    no Docker and no database;
  - the running API (`DevCharacterSeeder`) reads the same entries from the
    Postgres catalogue they were imported into.

  Both implementations serve the same files, so the two can't disagree unless an
  import is stale.

# Feature — Builder refinements (round 1)

Status: **in progress** (2026-09-25). The owner approved the plan and the order
(slice 1 → 2 → 3). Slice 4 was added afterwards by the owner, and slice 3 was
widened.

Done so far:
- Slice 1, item 2 ("OTHER BOOKS" order).
- Slice 1, item 1 (dropups): `usePopupDirection`, applied to `SourceSelect`.

- Slice 1, item 4 (spell filters): collapsed behind a "Filters · N" button. The
  whole list (search, filters, grid) also collapses with "Hide/Show spells", and
  starts collapsed once the choice is complete.
- Slice 1, item 3 (`EEPC`, 2026-09-26): resolved at ingestion through
  `FiveEToolsSourceNames`' unlisted names, then species were re-ingested and
  re-imported. No other source falls back to its code.
- Playtest limiter (2026-09-26): built and verified live. See below.

**Slice 1 is done.**

**Slice 2 is done (2026-09-26).** See "Slice 2 — as built" below.

**Slice 3 is done (2026-09-26).** See "Slice 3 — as built" below.

**Slice 4 is done (2026-09-26).** See "Slice 4 — as built" below. **Round 1 of
the builder refinements is complete.**

Part of phase 11, Stage D2 (the in-app creation flow). It came out of the owner's
testing of the builder with the draft "Mez".

## Findings behind the plan

- **Rolled hit points show no options.** `Dnd5eBuildPlanner` emits
  `build.rolledHitPoints` (type `ROLLED_HIT_POINTS`, prompt "Roll one hit die per
  level after the first") with an empty options list. The web renders it with the
  generic `ChoiceField`, so the selector is empty. The build already stores
  `rolledHitPoints` (one integer per level after the first), and the
  materializer already uses them.
- **Dropdowns overflow the page.** `.source-select__popup` always opens below
  (`top: calc(100% + 4px)`). Near the bottom of the page it runs past the
  document and leaves a white band.
- **Many options carry no summary** of what they give the sheet, for example
  spells, cantrips and class options.
- **Partnered books have no limiter** because none are imported. D&D Beyond's
  "Partnered Content" is third-party books (Tal'Dorei Reborn, Humblewood, Grim
  Hollow…), which 5etools' main data does not carry. Of the 77 imported sources,
  the one non-book is `UATheMysticClass` (Unearthed Arcana, playtest).
- **`EEPC` shows its code as its book name.** It should read "Elemental Evil
  Player's Companion".
- **The dice engine** (`dice/RollService`) keeps one die for
  advantage/disadvantage, but has no "keep highest N". So it cannot roll 4d6 and
  drop the lowest.

## Decisions (owner, 2026-09-25)

- **"OTHER BOOKS" order:** first a fixed list in this order: XGE, TCE, GGR,
  ERLW, SCAG. Then every other book alphabetically.
  - The fixed list is the owner's choice, not a count ranking. EGW (114), BMT (79)
    and PSX (60) have more entries than ERLW (58) and SCAG (52).
- **Virtual ability rolls:**
  - Each ability gets a "Roll" button under it.
  - The result of 4d6, dropping the lowest die, becomes that ability's value
    directly.
  - The button stays available for rerolls.
  - Every roll is made server-side and kept in the character's roll history, as
    all rolls are.
- **Playtest limiter:** Unearthed Arcana gets its own toggle, off by default.
  - It lives in the same area as a future **Partnered content** limiter, as on
    D&D Beyond.
  - Partnered books themselves must be offered later. Their data source is still
    open; see `open-items.md`.

## Slice 1 — Small UI fixes

1. **Dropdowns open upward when needed.** When a popup opens, measure the space
   between the trigger and the bottom of the viewport. If the popup does not fit
   there and there is more room above, open it upward.
   - Start with `SourceSelect` (`apps/web/src/builder/SourceSelect.tsx`).
   - Then check every other popup in the builder for the same overflow.
   - The measurement goes in one shared hook, not copied into each popup.
2. **"OTHER BOOKS" order.** Apply the fixed-then-alphabetical order above, next
   to `leadingSources` in `BuilderDefinition`, the rule that already puts the PHB
   first.
3. **`EEPC` label.** Fix the book name at ingestion, then re-ingest. Check whether
   other sources hit the same fallback.
4. **Collapsible spell filters.** In
   `apps/web/src/systems/dnd5e/builder/SpellPicker.tsx`:
   - the filters (level, school, and so on) collapse behind a "Filters" toggle;
   - the toggle shows how many filters are active ("Filters · 2");
   - text search stays always visible;
   - the panel starts collapsed;
   - applies to both the spell and cantrip pickers.

Tests: the popup direction hook (fits below, flips up, neither fits), the book
order, and the filter toggle with the active count.

## Slice 2 — Rolls: abilities and hit points

1. **Dice engine: keep highest N.**
   - Extend expression parsing and resolution in `dice/` with `kh` (keep highest),
     so `4d6kh3` is valid.
   - The roll record keeps every die rolled and marks the dropped ones, so the
     history can show them.
   - Update `.ai/features` / `domain-model.md` wherever the dice expression
     grammar is described.
2. **Endpoint for creation rolls.** Builder rolls go through the existing dice
   API against the draft character, so they land in its roll history.
   - Ability rolls use the label "Strength (4d6 drop lowest)", and so on.
   - Hit point rolls use "Hit points, level N (d8)".
   - No new endpoint unless the existing one cannot carry the label or the
     character.
3. **Abilities, "Manual/rolled" method** (`Dnd5eAbilitiesStep.tsx`):
   - the number input stays, for dice rolled at the table;
   - a "Roll" button under each ability rolls `4d6kh3` on the server and writes
     the total into that ability;
   - the last roll's dice appear next to the button, with the dropped die struck
     through.
4. **Rolled hit points UI.** Add a dedicated field for `ROLLED_HIT_POINTS`,
   replacing the generic `ChoiceField`:
   - one row per level after the first, showing the class's hit die (d6–d12);
   - each row has a number input (1 to the die size) and a "Roll dX" button;
   - the running total is shown against the average method's total.
   - The backend choice should carry what the UI needs: the level count and the
     hit die per level, which matters for multiclass. Add these to the choice
     instead of recomputing them in the web.
   - Answers are validated server-side: each value must lie between 1 and the
     die size.
5. **Verify on "Mez"** in the browser: roll HP, roll abilities, then open the
   sheet and check both, and check the roll history.

Tests:
- Backend: dice parsing and resolution for `kh`; the hit-point choice payload;
  validation of out-of-range answers.
- Web: the ability "Roll" button, and the hit-point rows (manual entry and
  virtual roll).

### Slice 2 — as built (2026-09-26)

- **No expression parser.** `dice/` never parsed expressions: sheet rolls resolve
  through `RollKind`, and manual rolls come as dice groups. So `kh` became a new
  endpoint instead of a grammar change:
  - `POST /api/characters/{id}/rolls/creation` takes `{die, count, keepHighest?,
    context}` and allows a draft (`CharacterService.getMine`). It needed a new
    endpoint because the existing ones refuse drafts and can't carry a label.
  - `KeptDice` writes `4d6kh3`, keeps the highest dice, and reads the dropped ones
    back from a stored expression.
  - `RollResponse.dropped` lists them. No schema change.
- **The rules come from the plan, not the web:**
  - the MANUAL method option carries `roll: {count 4, sides 6, keepHighest 3,
    label "4d6 drop lowest"}`;
  - `ROLLED_HIT_POINTS` carries one option per level after the first, in the
    materializer's order, each with `sides`, `average` and `rollLabel`.
- **Validation:**
  - a rolled value outside 1..die is a plan problem ("Hit points for Wizard 1
    must be between 1 and 6");
  - `rolledHitPoints` may hold null for a level not rolled yet, and the
    materializer uses the average for it.
- **Web:**
  - Abilities (Manual/rolled): a "Roll" button under each ability. The last roll's
    dice appear next to it, with the dropped die struck through.
  - Classes step: `Dnd5eRolledHitPointsField` shows one row per level (class
    level, die, number input, "Roll dX"), plus "Rolled total" against "Average
    would be".
  - The checklist gets a "Hit points" line.
- **Verified live on "Mez test":**
  - five hit point rows (Warlock 2–3 d8, Fighter 1–3 d10); the summary went from
    48 to 41 HP with 1+2+5+6+7 rolled;
  - six ability rolls, each total equal to its three kept dice;
  - all 10 rolls are in `rolls` with their labels.
  - The draft was then restored to its point-buy scores and empty rolled hit
    points. The rolls stay in the history, which is append-only.
- **Not done:**
  - The sheet check after finishing the character wasn't possible, because Mez
    still has a pending choice. The sheet-side hit points already come from the
    materializer, which is tested.
  - The roll log (`LogPanel`) shows only the expression, so `4d6kh3` appears
    without the struck-through die.

## Slice 3 — Technical summaries on options

1. **Survey first.** List every choice type the builder offers, and for each one
   note whether its options carry a summary today. Examples: spell, cantrip,
   class feature option, fighting style, feat, invocation, metamagic,
   maneuver…
   - Write the survey into this file.
   - Show the owner one or two sample summaries per type before implementing.
2. **Summaries come from 5etools data only.** Never hand-authored text. For
   example:
   - a spell: level and school · casting time · range · components · duration ·
     damage or healing dice where present;
   - a feature option: a short excerpt of its own entries text.
3. **Where it's built:** in the planners (`Dnd5eBuildPlanner`,
   `Dnd5eSpellPlanner`), which already turn catalogue entries into
   `CreationChoice` options. Each option gets a `summary` field.
4. **The effect replaces the book name** (owner, 2026-09-25). Where an option
   today reads "Battle Master · Player's Handbook", it should read "Battle
   Master · <what it gives>".
   - The book stays as the code badge with its tooltip (as in `SourceSelect`),
     so no information is lost.
   - This applies in the dropdowns, the option lines and the chosen-value
     display.
5. **The survey decides where the effect shows.** It lists every place an option
   or a chosen value is labelled with its book, and marks which ones need the
   effect. Candidates the owner named:
   - class features and their options (subclasses, fighting styles, maneuvers,
     invocations…);
   - spells and cantrips;
   - backgrounds;
   - species and subspecies.
   - Plain items (weapons, gear) likely keep their current line.
6. Tests per choice type for the summary text.

### Slice 3 — survey (2026-09-26)

Every choice type the planners offer, whether its options carry a summary today,
and what the survey proposes. "Book" means the option line shows the source
book's name today.

| Choice type | Where | Book today | Summary today | Proposed summary, from 5etools data only |
| --- | --- | --- | --- | --- |
| `SPECIES` | Species step | yes | none | ability increases · speed · darkvision · resistances |
| `SUBSPECIES` | Species step | yes | none | the subspecies' own increases, senses, resistances, spells |
| `SPECIES_VARIANT` | Species step | yes (species) | none | the variant's increases or feat |
| `BACKGROUND` | Background step | yes | none | skills · tools · languages · feature name |
| `CLASSES` | Classes step ("Fighter (Player's Handbook)") | yes | none | hit die · saving throws · armor · weapons |
| `SUBCLASS` | Classes step | yes | none | features gained at the subclass's first level |
| `FEAT`, `ASI_OR_FEAT` | Classes/Species/Background, ASI field | yes | prerequisite only | ability increase · first benefit sentence · prerequisite |
| `OPTIONAL_FEATURE` (fighting styles, invocations, maneuvers, metamagic…) | Classes step | yes | prerequisite only | first sentence of its text · prerequisite |
| `CANTRIP`, `SPELL`, `SPELLBOOK`, `PREPARED_SPELL` | Spell picker | yes | `effectSummary` ("Damage") | level and school · casting time · range · components · duration · damage |
| `FEATURE_OPTION` (adr-0007 overlay) | Classes step | no | overlay `summary` | unchanged |
| `LANGUAGE` | several | yes | none | type · script ("Standard · Dwarvish script") |
| `TOOL`, `WEAPON`, `ARMOR`, `ITEM`, `EQUIPMENT_ITEM` | several | yes | none | unchanged: plain items keep their line |
| `EQUIPMENT`, `EQUIPMENT_METHOD` | Equipment step | no | contents in the label / gold | unchanged |
| `SKILL`, `ABILITY_SCORE`, `SIZE`, `RESISTANCE`, `EXPERTISE`, `SAVING_THROW`, `SKILL_TOOL_LANGUAGE`, `SPELL_SET`, `SPELLCASTING_ABILITY`, `ALTERNATIVE` | several | no | none | unchanged (no book to replace) |
| `ABILITY_SCORES`, `ROLLED_HIT_POINTS` | own UIs | no | rules as data | unchanged |

Places in the builder where an option or a chosen value names its book:
- `SourceSelect`: the code badge with the full name as a tooltip (trigger and
  popup). It stays as the source indicator.
- `ChoiceField` chosen values: "Dueling · Player's Handbook". This becomes
  "Dueling · <summary>".
- `Dnd5eAsiOrFeatField` feat list: "Alert · Player's Handbook · Prerequisite…".
- `SpellPicker` rows and chosen chips.
- `ClassesEditor`: "Fighter (Player's Handbook)".
- The sheet's own panels (feat, spell and inventory management) are out of scope.

Owner answers (2026-09-26): the samples are approved for now, and some may be
adjusted later.
- Subclass: only the features at its first level.
- With redaction on, feat and optional-feature summaries drop the quoted sentence
  and keep the structured parts.

### Slice 3 — as built (2026-09-26)

- **`Dnd5eOptionSummaries`** builds every summary from catalogue data:
  - species, subspecies and variants: increases · speed · senses · resistances ·
    level-1 spells;
  - background: skills · tools · languages · feature;
  - class: hit die · saves · armor · weapons;
  - subclass: first-level features, leaving out "… Options" index entries;
  - feat: increases · first benefit sentence;
  - optional feature: first sentence after its "Prerequisite:" line;
  - spell: level and school · casting time · range · components · duration ·
    damage;
  - language: type · script.
- **Where it's applied:**
  - `Dnd5eChoiceOptions.summarized` feeds the planner's catalogue options
    (species, background, class), subclasses, feats and optional features.
  - The prerequisite is appended after the summary.
  - Spells and languages get theirs in `Dnd5eChoiceOptions.option`. The spell
    picker's "Effect" column still reads `data.effectSummary`.
- **Redaction:** `CatalogueLookup.redactsProse()` (default false) reports the
  catalogue's redaction switch. `DatabaseCatalogueLookup` takes it from
  `CatalogueService`, and `Dnd5eSourceFilteredCatalogue` delegates.
- **Web:**
  - `SourceSelect` shows the summary under each option's name, and the filter
    box searches it.
  - Chosen values (`ChoiceField`, the feat field) read "Name · summary", and
    fall back to the book when an option has no summary (items).
  - `ClassesEditor` shows the class's code badge and its summary.
  - Spell rows show the code badge instead of the book name, since their columns
    already carry the technical line.
  - Chosen spell chips show the summary on hover.
  - `SourceBadge` is exported for reuse.
- **Verified live on "Mez test":** species, subspecies, backgrounds, classes,
  subclasses, fighting styles, pact boons, invocations, maneuvers and spells all
  show their summaries from real data. Items keep "Smith's Tools · Player's
  Handbook".

## Slice 4 — Class progression preview

Owner request (2026-09-25): a preview of the coming levels for each chosen class.

- **Placement:** at the bottom of the Classes step page, one panel per chosen
  class.
  - Each panel is always collapsed on load ("Class progression · Fighter ▾").
  - The class's current level is highlighted, and the levels after it are the
    preview.
- **Content:** mirrors the class table in the book, one row per level 1–20:
  - level and proficiency bonus;
  - the features gained at that level, including the chosen subclass's features
    once a subclass is picked;
  - the class's own columns: Rage/Rage Damage, Martial Arts/Ki, Sneak Attack,
    Cantrips Known, Spells Known, spell slots per level, Invocations, and so on.
- **Data:** only from 5etools. Its class JSON already carries this table
  (`classTableGroups`) and the feature-per-level list.
  - The API serves it as a table per class (columns plus rows), so the web only
    renders it and knows no class by name.
  - Where the table sits, whether in the catalogue entry or a new endpoint, is
    decided during implementation, after checking what ingestion already keeps.
- **Multiclass:** one panel per class, each with that class's own table.
- Tests:
  - backend: the table for a caster (Wizard slots), a martial (Barbarian Rage)
    and a subclass (Battle Master features);
  - web: the panel starts collapsed, and the current level is marked.

### Slice 4 — as built (2026-09-26)

- **Where the table sits:** ingestion already keeps everything needed in the
  class and subclass entries:
  - `tableColumns`;
  - `spellcasting.spellSlotsByLevel`;
  - the features with their level, `optional` and `grantsSubclassFeature`.
  So there's no new endpoint. The draft preview computes the tables.
- **Backend:**
  - `ProgressionTable` (ruleset, system-agnostic: name, current level, columns,
    rows) travels in `CreationPreview.progressions` and
    `DraftPreviewResponse.progressions`.
  - `Dnd5eClassProgression` builds levels 1–20 with these columns: level ·
    proficiency bonus · features · the class's and subclass's own columns ·
    spell slots up to the highest slot level reached.
  - Warlock pact slots already come in `tableColumns`.
  - A chosen subclass replaces the "… feature" placeholders with its own
    features, leaving out "… Options" entries. The subclass is shown even below
    its level, as a preview.
  - Optional class features appear only when the preference is on.
- **Web:** `ProgressionPanel` (generic, in `builder/`) is a collapsed `<details>`
  titled "Class progression · Fighter (Battle Master) ▾". The current level is
  marked, and earlier levels are muted.
  - Since 2026-09-26 (owner request), only the selected class tab's panel
    shows. It is the page's last section, after the class's levels and "Other
    class choices".
- **Verified live on "Mez test":**
  - Warlock (The Fiend): Cantrips/Spells Known, Spell Slots, Slot Level and
    Invocations Known columns; Dark One's Blessing at 1 and Dark One's Own Luck
    at 6.
  - Fighter (Battle Master): Battle Master features at 3, 7, 10, 15 and 18.
  - Both panels start collapsed.

## Partnered and playtest content limiter (goes with slice 1)

- In Basics, a **"Partnered & playtest content"** area replaces the current
  "none are imported" note.
  - **Playtest (Unearthed Arcana):** a toggle, off by default. When off, UA
    sources are excluded from every option list, in the same way unticked books
    are.
  - **Partnered content:** the same toggle pattern, listing partnered books once
    they exist. Until then it shows "No partnered books are available yet."
- Which sources count as playtest is decided server-side (source code prefix
  `UA`), not hard-coded in the web.
- **As built (2026-09-26):**
  - `PlaytestSources` classifies from the imported `source_code`, so no migration
    was needed.
  - `GET /api/catalogue/sources` returns `playtest`.
  - The preference is `Dnd5ePreferences.playtestContent`.
  - Playtest sources ignore the book list and aren't in the "Other books" menu.
- **Partnered content by brand (owner, 2026-09-26):**
  - `PartneredSources` maps licensed books to D&D Beyond's partner brands:
    Critical Role (EGW, CRCotN, the Wildemount adventures) and Rick and Morty
    (RMR, RMBRE).
  - `PartneredSources` knows all 27 of D&D Beyond's partner brands, in its
    order.
  - `GET /api/catalogue/partners` serves only those with imported books; today
    that's Critical Role and Rick and Morty (owner, 2026-09-26).
  - The row has an "Enable partnered content" switch
    (`Dnd5ePreferences.partneredContent`, on by default). While it's on, a
    dropdown lets the player check each partner (`partners`, null = every
    partner).
  - A partner's books ignore the book list.
  - Third-party publishers' books are still not imported; see `open-items.md`.

## Open

- Where third-party partnered books would come from, since 5etools' main data
  does not carry them. Recorded in `open-items.md`.

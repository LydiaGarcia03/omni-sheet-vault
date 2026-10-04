# Feature — 5etools ingestion pipeline

**Status: done.** This document is the plan `adr-0006-5etools-as-content-source.md`
promised ("a general 5etools → this project's catalogue-JSON converter is still not
built"). Written and implemented across four slices, all 2026-09-17 — see
`changelog.md`'s four "5etools ingestion pipeline, slice N" entries.
`./gradlew :apps:api:ingest5etools` ran for real against the owner's downloaded
5etools data the same day: 570 real spell files now live in `content/dnd-5e/spells/`
(up from 11 hand-typed ones), all imported into the local dev catalogue without a
single validation failure. `DevCharacterSeeder.java`'s own hand-typed spell copy is
gone too — it now builds Aria's spells from the same real catalogue data, verified
live in the running app. See "Done when" at the bottom for the full detail.

## Why this exists

`adr-0006-5etools-as-content-source.md` made 5etools this project's absolute source of
truth for D&D 5e content. The first pass under that policy only re-sourced four fields
(`saveAbility`, `components`, `materialComponent`, `duration`) for the ~29 spells already
seeded or catalogued — it never touched `description`. The owner caught this live
(2026-09-17): Fire Bolt's seeded description is a hand-shortened paraphrase ("A mote of
fire streaks toward a target within range.") instead of the PHB's real text. Hand-patching
descriptions one spell at a time repeats the same mistake at a smaller scale. This plan
is for a real, reusable **converter**: 5etools JSON in, this project's own catalogue JSON
out, covering every spell (not just the ones a test character happens to use), built so
the same shape of tool covers classes/species/items/feats/creatures later without a rewrite.

## Scope

**This pass: spells only**, all of them (not just Aria's roster), from the 2014 PHB and
its 2014-era expansions — never `XPHB`, per `adr-0006`'s scope rule. Classes, species,
items, feats and creatures are explicitly out of scope for the first build, but every
design choice below is made so extending to those kinds later is a new field-mapping
table, not a new pipeline.

**Later kinds:**

- Items (`ItemConverter`) and feats (`FeatConverter`) were built after spells.
- Classes and subclasses (`ClassConverter`, `SubclassConverter`, 2026-09-23) have
  their own field mapping, scope filter and helper classes, documented in
  `systems/dnd-5e/features/character-creation.md`, "A1 — classes and subclasses".
- Output directory names are the kind's plural (`classes`, `subclasses`).
- Shared helpers added for those kinds:
  - `FiveEToolsEntries`: entries flattening;
  - `FiveEToolsProficiencies`: the proficiency blocks shared by classes, species
    and backgrounds;
  - `FiveEToolsStartingEquipment`;
  - `FiveEToolsSlugs`: slug disambiguation for every kind (see below).
- **Added with C1a** (2026-09-23):
  - **Languages:** `LanguageConverter`, `LANGUAGE` kind, with 5etools' own
    `type`: standard, exotic, rare or secret.
  - **Feat mechanics:** `FeatConverter` now writes feat mechanics in the grant
    shape: abilities (with `maximum` and 5etools' `weighted` choices), skills,
    tools, languages, weapons, armor, saving throws, expertise and
    optional-feature progressions. Prerequisites and spells are kept verbatim.
  - **`focusType` on items:** `ItemConverter` writes `focusType` from 5etools'
    `scfType`.
  - **Lineage defaults:** `FiveEToolsSpeciesData` applies 5etools' own renderer
    defaults for legacy `lineage` races (VRGR/UA1 ability choices, Common plus
    one standard language).
  - **The adr-0007 overlay** (`content/dnd-5e/mechanics/`) is merged into class
    and subclass features by `FiveEToolsMechanicsOverlay`. An overlay file that
    matches no feature fails ingestion.
  - **Shared helpers:** `CatalogueContentLayout` holds the kind-to-directory
    naming, and `FiveEToolsAbilityGrants` the ability mapping shared by species
    and feats.
- **Backgrounds** (`BackgroundConverter`, 2026-09-23). They replace the hand-typed
  Soldier. See `systems/dnd-5e/features/character-creation.md`, "A3".
- **Species** (`SpeciesConverter`, 2026-09-23), with `FiveEToolsSpeciesData` and
  `FiveEToolsCopies`: the supported subset of 5etools' `_copy`/`_mod`/`_versions`.
  See `systems/dnd-5e/features/character-creation.md`, "A2". Species are written to
  `content/dnd-5e/species/`, an explicit irregular plural.
- **Optional features** (`OptionalFeatureConverter`, 2026-09-23), with
  `FiveEToolsPrerequisites`: see `systems/dnd-5e/features/character-creation.md`, "A1b".
- **`TagMarkupStripper` follows 5etools' own `Renderer.stripTags`** (2026-09-23).
  The display text's segment depends on the tag family; the 2nd segment of an
  entity tag is its *source*, never its display text. A tag with no known rule
  fails ingestion. This replaces the earlier "payload|display|source" assumption
  and its special cases.
- **Source names** (2026-09-23). Converters emit the raw 5etools source code.
  `FiveEToolsSourceNames` resolves it to the full name, using the `source` field of
  `books.json`/`adventures.json`, not their `id`. Two amendments from the owner
  (2026-09-24):
  - "(2014)" is dropped from core-book names ("Player's Handbook");
  - six codes that neither file lists get the names the owner supplied: TftYP,
    UATheMysticClass, HAT-LMI, RoTOS, EET and MCV2DC. EEPC ("Elemental Evil
    Player's Companion") joined them on 2026-09-26.
- **Playtest sources** (2026-09-26). A source whose code starts with `UA` is
  playtest material (`PlaytestSources`), classified from the imported
  `source_code`. The builder offers it only behind its own switch; see
  `systems/dnd-5e/features/builder-refinements.md`.
- **Partner brands** (2026-09-26). `PartneredSources` groups licensed books under
  D&D Beyond's partner brands: Critical Role (EGW, CRCotN, ToR, DD, FS, US) and
  Rick and Morty (RMR, RMBRE). This is a hand-kept table, because 5etools doesn't
  mark partner content. The same class lists all 27 of D&D Beyond's partner
  brands. The builder shows only those with imported books.
- **Source codes** (2026-09-25). Every output file also carries `sourceCode`, the
  raw 5etools code (e.g. `PHB`), next to `sourceBook`. It is imported into
  `catalogue_entries.source_code` and served by `GET /api/catalogue/sources`, for
  the builder's `SourceSelect` badge.
- **Scope by publication date** (2026-09-24, adr-0006 amendment).
  `FiveEToolsSourceClassifier.forRules2014` excludes every source published on or
  after `XPHB`'s date: 35 sources, including XDMG, XMM, FRHoF, EFA, LFL, AU, ABH,
  RHW and the 2024-era adventures.
  - The runner deletes every output file it didn't write this run. That covers
    now-excluded sources and retired plain slugs; it replaces the old
    retired-slug-only deletion.
  - Effect on the catalogue: spells 578 → 525, items 2,015 → 1,773, feats
    228 → 108, languages 167 → 150, optional features 163 → 151, species
    140 → 134.
- **Same-named entries from different sources** (2026-09-23, owner rule in
  `ground-rules.md`):
  - The runner gives every member of a slug-collision group a
    `<slug>-<source code>` slug and deletes the retired plain-slug file.
  - A collision within one source fails the run.
  - The first run retired 25 slugs: 8 spells, 16 items, 1 feat.
- **Stale rows in Postgres.** `CatalogueImportService` treats the imported
  directory as the source of truth for each (system, kind) it contains, and
  deletes rows of those kinds whose slug has no file anymore. Catalogue rows are
  only referenced by id at learn/add time (then copied onto the sheet), so
  removing a row never breaks a character.

## Current state (what this replaces)

Two hand-maintained, disconnected copies of spell data existed before this pipeline:

1. ~~**`content/dnd-5e/spells/*.json`**~~ — **resolved 2026-09-17.** The real catalogue
   import source (`CatalogueImportService`, `roadmap.md` phase 7): one file per spell,
   upserted into the `catalogue_entries` table by `(systemId, kind, slug)` via
   `./gradlew :apps:api:bootRun --args='--import-catalogue=<dir>'`. Was 11 hand-typed
   spells; now 570, all generated by this pipeline from real 5etools data.
2. ~~**`DevCharacterSeeder.java`**'s inline JSON text blocks~~ — **resolved
   2026-09-17, slice 4.** Was a separate hand-typed set (18 spells) used only to seed
   the dev test character (Aria), unrelated to the catalogue table; now builds those
   18 spells from real catalogue data by slug instead.

Neither read from 5etools directly; both were populated by hand, once, from
manually-copied 5etools text. That's the gap this pipeline closed for both (1) and,
in slice 4, (2) too.

## Pipeline stages

1. **Obtain raw 5etools data.** **Resolved 2026-09-17, direct owner decision**: source
   is the [5etools-mirror-3/5etools-src](https://github.com/5etools-mirror-3/5etools-src)
   GitHub repository (same source `adr-0006` already cites) — the owner downloads the
   latest release zip (e.g. `v2.35.1`, https://github.com/5etools-mirror-3/5etools-src/releases)
   and extracts its `data/` directory into a gitignored local folder, `tools/5etools-data/`
   (documented in `tech-stack.md`), matching how `.env` stays local-only. The tool reads
   from that folder; it does not fetch from GitHub itself. Re-running it after the owner
   drops in a newer release is exactly the "safe to re-run" requirement this doc's own
   "Done when" section already asks for.
2. **Filter to in-scope sources.** **Reverted 2026-09-17, same day as first resolved**
   — see `adr-0006`'s "Filtering out non-core sources" for the full story: the owner
   briefly chose to exclude collab/promotional sources at ingestion time, then reverted
   once real data was in hand, back to this ADR's original proposal — import
   **everything** except `XPHB` (the 2024/5.5e revision, `adr-0006`'s unchanged
   2014-only scope rule) and let phase 11 build a character-creation-time toggle for
   collab sources instead of deciding it once for the whole catalogue. **Built in
   slice 3** as `FiveEToolsSourceClassifier`, checked against each spell's own `source`
   field individually (not `sources.json` at all — sidesteps needing that file's exact
   schema). Its collab-exclusion mechanism exists but its set is deliberately empty —
   not a placeholder to fill in soon, the actual intended behavior until phase 11 exists.
3. **Strip `{@tag ...}` markup.** 5etools' `entries` (description) and other text
   fields use an inline tag syntax (`{@damage 1d10}`, `{@condition blinded}`,
   `{@spell fireball}`, `{@i text}`, etc.) — pipe-separated payloads, source citation as
   an optional trailing segment. **Built in slice 1** as `TagMarkupStripper`: implemented
   generically off the pipe convention itself (display segment if present, else payload)
   rather than a hand-maintained per-tag table.
4. **Map fields** to this project's own shape — see table below. **Built in slice 2**
   as `SpellConverter`.
5. **Emit `content/dnd-5e/spells/<slug>.json`**, one file per spell, in the exact shape
   `CatalogueEntryImport` already expects (`spare-the-dying.json` is the reference
   shape: `systemId`/`kind`/`slug`/`name`/`sourceBook`/`sourcePage`/`tags`/`description`/
   `data`). Overwrites existing files — the tool must be safe to re-run as mappings get
   fixed or 5etools' own data updates, not a one-shot. **Built in slice 3** as
   `Ingest5eToolsRunner`, driven by `./gradlew :apps:api:ingest5etools`.
6. **Existing step, unchanged:** `CatalogueImportService` picks up whatever's in
   `content/dnd-5e/spells/` via the existing `--import-catalogue` command.

## Field mapping (5etools spell → this project's schema)

| 5etools field | This project's field | Notes |
| --- | --- | --- |
| `name` | `name` | |
| `source`, `page` | `sourceBook`, `sourcePage` | Needs the book-code → display-name lookup from `sources.json` (e.g. `"PHB"` → `"Player's Handbook"`) |
| `level` | `data.level` | |
| `school` (letter code) | `data.school` | Already have the code→word map (`schoolLabel` on the frontend expects the full word already; confirm the letter→word table, e.g. `V`→`evocation`) |
| `time` (array) | `data.castingTime` | Formatter needed: `[{"number":1,"unit":"action"}]` → `"1 Action"`, matches this app's existing display convention |
| `range` (structured) | `data.range` | Formatter needed, same shape this app's `Spell.range` already expects |
| `components` (`{v,s,m}`) | `data.components`, `data.materialComponent` | `m` may be a string or `{text, cost, consume}` — extract `text` only for now (no cost/consumption modeled) |
| `duration` (array) | `data.duration`, `data.concentration` | Formatter needed; `concentration` flag lives inside the duration entry |
| `meta.ritual` | `data.ritual` | |
| `savingThrow` (array) | `data.saveAbility` | Already lowercase full ability names — matches this project's own `abilities.ts` keys exactly (confirmed in the first 5etools pass) |
| `damageInflict` (array) | `data.damageType` | This project models one `damageType`, not an array — first entry, or `null` if the spell deals no damage |
| `entries` (array, `{@tag}` markup) | `description` | Join into plain text after stripping markup (stage 3) |
| `entriesHigherLevel` | `data.higherLevelsDescription` | D&D Beyond's own "At Higher Levels" scaling text — **resolved 2026-09-17**, see open question 5 |
| `miscTags`, `conditionInflict` | `tags` (catalogue-level) **and** a new sidebar-facing tags source | See "Tags" below — this is the concrete answer to today's "how do we implement tags" question |
| `generated/gendata-spell-source-lookup.json` `subclass` (per lower-case source and name) | `subclasses` (`{className, classSource, subclassShortName, subclassSource}`) | Subclass spell lists (2026-09-24): Chronurgy/Graviturgy dunamancy, Eldritch Knight, domains, patrons. The planner adds them to the class's options for a character with that subclass. 2024-edition class references are dropped. |
| `source` (code) | `sourceCode` | Kept beside the full `sourceBook` name, for 5etools references (`name\|source`) and `source=` filters. |
| `spells/sources.json` `class` / `classVariant` (per source, per spell name) | `classes`, `optionalClasses` (`{name, source}`) | Which class lists the spell is on (C1c, 2026-09-23). A `classVariant` counts as optional only when its `definedInSource` book defines `isClassFeatureVariant` features for that class *and* isn't the spell's own book (TCE's expanded lists). References to 2024-edition classes are dropped. See `systems/dnd-5e/features/character-creation.md`, "C1c". |

**This table was incomplete — found during slice 2.** `SpellCatalogueData`'s actual
shape has five more fields this table never listed, all resolved while implementing
`SpellConverter` (see `changelog.md`'s slice 2 entry for the full reasoning):

| 5etools field | This project's field | Notes |
| --- | --- | --- |
| `spellAttack` (array) | `data.attackRoll` | Present and non-empty → `true` |
| First `{@damage NdM}` tag in `entries` | `data.damageDiceCount`, `data.damageDiceSides` | Parsed from the same markup stage 3 already strips; `null`/`null` if no damage tag is found |
| *(derived, not a 5etools field)* | `data.notes` | `duration + ", " + componentsWithMaterial` when concentration, else just `componentsWithMaterial` — verified against all 11 existing hand-typed files, not guessed |
| *(derived, not a 5etools field — see below)* | `data.effectSummary` | **Not a 5etools fact** — D&D Beyond's own hand-curated "Effect" column, transcribed by hand originally (`changelog.md`'s 2026-08-16 "Phase 10, slice 8: Spells tab" entry). Owner-accepted best-effort heuristic: damage present → "Damage"; `miscTags` has `HL` → "Healing"; `conditionInflict` non-empty → "Control"; school is divination → "Detection"; else → "Buff". Explicitly allowed to drift from D&D Beyond's real value — low-stakes, not a core feature |

## Tags — answering today's question

This app already has **two** unrelated "tags" concepts, which is the actual source of
confusion, not a missing feature:

1. **`CatalogueEntry.tags`** (backend, `text[]`) — always-visible even under redaction
   (`adr-0005`), currently populated by hand with facets like `["necromancy","cantrip"]`
   that just restate `school`/`level`. Not wired to the character-facing `Spell` type at
   all today — a catalogue entry's tags never reach `SpellRow`/`EntityDetailPanel`.
2. **`EntityDetailRequest.tags`** (frontend, shared Entity Detail mold) — currently fed
   only by `spell.concentration`/`spell.ritual` for spells (`spellDetail.tsx`). This is
   what renders in the sidebar today and is what the owner is comparing against D&D
   Beyond's own `.ct-spell-detail__tags` (a `TagGroup`: a "Tags:" label plus a row of
   plain badges — sourced from 5etools' `miscTags`/`conditionInflict`, categorical
   labels like "Damage", "Healing", "Utility", or a condition the spell inflicts).

**Recommendation:** don't approximate D&D Beyond's real tag content by reusing
`effectSummary` (a single value already serving a different UI purpose — the Spells
tab's own "Effect" column) or by hand-guessing categories now. Populate
`CatalogueEntry.tags` for real from 5etools' `miscTags`/`conditionInflict` in this same
ingestion pass (mapped to friendly labels, e.g. `"HL"` → `"Healing"`), then thread that
value through to the character-facing `Spell`/`SpellResponse` shape so
`buildSpellDetailRequest` can feed it into `EntityDetailRequest.tags` alongside — not
instead of — the existing Concentration/Ritual badges (those are correct, already-real
data, and conceptually distinct from 5etools' own categorical tags). This means the
"implement tags" work is mostly plumbing (`Dnd5eSpell`/`Spell`/`SpellResponse` all need
a `tags: string[]` field, same shape as the four fields the first 5etools pass already
added) that only becomes meaningful once real tag data exists to put in it — sequencing
this after the ingestion tool, not before, avoids building the plumbing twice.

## Open questions needing the owner before or during implementation

All resolved 2026-09-17 except the last, which stays deferred (see "Out of scope" below):

1. ~~Where does the raw 5etools JSON live locally?~~ Resolved — see pipeline stage 1.
2. ~~Which sources count as "2014-era"?~~ Resolved — see pipeline stage 2: every
   sourcebook except `XPHB` and collab/promotional sources, not a narrower 2014-print-date
   test. `sources.json` still needs checking once downloaded to find the actual
   machine-readable marker for "collab," per stage 2's own note.
3. ~~Does `DevCharacterSeeder.java` keep its own hand-typed spell copies, or read from
   the catalogue by slug?~~ **Resolved and built, slice 4, 2026-09-17.**
   `DevCharacterSeeder.java`'s inline JSON text blocks for Aria's spells are deleted,
   replaced with `CatalogueService.findBySlug` lookups matching `(systemId, kind,
   slug)` — `CatalogueEntryRepository` itself stays package-private, exposed through
   the new public `findBySlug` on `CatalogueService` instead, per architecture.md's
   "through the other feature's service" rule.
4. **Tool shape — resolved, refined design.** The owner confirmed the dependency-free
   `JavaExec` direction but asked it be structured for near-future reuse across content
   kinds (classes, species, items, feats, creatures), not just spells. Design: still one
   Gradle `JavaExec` task (no Spring context — a pure data transform doesn't need it).
   **Built in slice 1 inside the existing `dev.omnisheetvault.api.catalogue` package,
   not a new one** — `CatalogueEntryImport`, the target shape a converter builds, is
   package-private, and Java visibility doesn't extend across a parent/subpackage split,
   so a genuinely separate package would need it made `public` first for no real benefit.
   Internally it follows this codebase's own established strategy-per-kind shape (the
   same Open/Closed seam `ruleset`'s `GameSystem`/`SheetCalculator` registry already
   uses, per `ground-rules.md`):
   - Shared, kind-independent pipeline pieces, used by every converter: a 5etools JSON
     loader, the `{@tag}` markup stripper (stage 3), and the source classifier/filter
     (stage 2, reading `sources.json`).
   - A small `FiveEToolsConverter<T>`-shaped interface, one implementation per content
     kind — `SpellConverter` now, `ClassConverter`/`ItemConverter`/etc. later — each
     owning only its own field-mapping table (this doc's own "Field mapping" section for
     spells). Adding a new kind later means writing a new converter class, never editing
     `SpellConverter`, the same "adding a game system means adding classes" rule this
     project already applies to `ruleset`.
   - One `main()` entry point selecting a converter by a `--kind=spells` argument (or
     running every registered one), orchestrating load → classify/filter → strip → map →
     emit `content/dnd-5e/<kind>/<slug>.json`.
5. **`entriesHigherLevel` ("At Higher Levels" scaling text) — resolved and built,
   2026-09-17.** New nullable `higherLevelsDescription` field threaded end to end:
   `SpellCatalogueData` → `Spell`/`Dnd5eSpell` → `SpellResponse` → frontend `Spell`
   type, extracted by `SpellConverter` and rendered by `spellDetail.tsx` as a bolded
   "At Higher Levels." paragraph after the base description. Verified live against
   Cure Wounds' real scaling text. See `changelog.md`'s own entry for the
   `{@scaledice}`/`{@scaledamage}` tag-convention correction this required
   (`TagMarkupStripper`'s generic "2nd segment = display" rule is wrong for these two
   tags — confirmed against real, independently-known spell scaling facts) and for
   the same trap found in `{@variantrule}`.

## Out of scope for this pass

- Classes, species, creatures — same pipeline shape, later, once spells prove it
  out. **Items and feats are no longer out of scope** — see
  `systems/dnd-5e/features/inventory-equipment-mechanics.md`'s `ItemConverter` (built 2026-09-20)
  and `FeatConverter` (`changelog.md`'s own 2026-09-21 entry), both built on this
  exact pipeline shape.
- The character-creation-time "exclude collab/non-core sources" toggle
  (`adr-0006`'s own open question) — the source-classification step (stage 2) is where
  it will eventually hook in, not built now.
- ~~`entriesHigherLevel` modeling~~ — **resolved 2026-09-17**, see open question 5.

## Done when

Every 2014-sourced PHB-family spell in `content/dnd-5e/spells/` was generated by the
tool from real 5etools data, not hand-typed; re-running the tool against the same input
produces byte-identical output (proving it's a real deterministic mapping, not a
one-off script); and `DevCharacterSeeder.java`'s own fate (keep duplicating vs. read
from the catalogue) has been decided, not left ambiguous.

**Done, 2026-09-17.** The owner populated `tools/5etools-data/` (a 5etools-mirror-3
release zip, full checkout — the real data landed at `tools/5etools-data/data/`, one
level deeper than first assumed; `Ingest5eToolsMain`'s default path corrected to
match) and ran `./gradlew :apps:api:ingest5etools --args="--kind=spell"` for real:

- **578 spells converted into 570 files** (8 pairs share a slug — the same spell
  reprinted across two in-scope sourcebooks, e.g. "Enervation" in both `AU` and `XGE`;
  the later file in glob order wins, expected/benign, not a bug).
- **Every field-mapping assumption held.** Spot-checked Fire Bolt and Cure Wounds
  against real PHB text before running the full batch — `time`/`range`/`duration`/
  `components`/`spellAttack`/`miscTags`/`savingThrow`/`meta.ritual` all matched this
  pipeline's own assumptions exactly, no formatter changes needed. All 570 real files
  passed Bean Validation on import with zero failures — no unknown school codes or
  malformed entries anywhere in the batch.
- **Re-run produces byte-identical output** — hashed every file, ran the tool again,
  zero differences. Proves the deterministic-mapping requirement for real, not just
  against `Ingest5eToolsRunnerTest`'s synthetic fixtures.
- **Imported into the local dev catalogue for real**:
  `./gradlew :apps:api:bootRun --args='--import-catalogue=<content/dnd-5e/spells>'`
  reported "Imported 570 catalogue entries," confirming pipeline stage 6 (the existing,
  unchanged import step) still works end to end with this pipeline's real output.

**`DevCharacterSeeder.java`'s fate — decided and built, slice 4, same day.** Migrated
to the catalogue: its 18 hand-typed spell literals are gone, replaced by
`CatalogueService.findBySlug` lookups against the real data above. Verified live —
deleted the local dev `test` player and logged back in fresh; Aria Emberfall's Fire
Bolt sidebar showed the genuine PHB text, not the hand-shortened paraphrase that
triggered this entire feature. See `changelog.md`'s slice 4 entry for the full detail,
including a pre-existing, unrelated race condition found (not fixed) in
`PlayerService.currentPlayer` during that live verification.

Every criterion above is satisfied. This document's own work is complete.

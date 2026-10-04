# ADR 0006 — 5etools as the absolute content source for D&D 5e

**Status:** accepted

## Context

Spell data (mechanics, save ability, components, duration, description) was being
hand-authored or hand-guessed per entry — workable for a handful of seeded spells, but
not something that scales to the rest of the catalogue (classes, species, items,
feats, creatures) `roadmap.md` and `decisions/adr-0005-content-catalogue-and-redaction.md`
already commit to covering in full.

The owner already uses [5etools](https://5e.tools) (source: the
[5etools-mirror-3/5etools-src](https://github.com/5etools-mirror-3/5etools-src)
repository, which redistributes its own data under a self-hosting/sharing policy —
the same posture ADR-0005 already accepted for this project's own catalogue) as their
own reference for the full published game. Its data files are structured, versioned,
and already cover the same "full published material, not just the SRD" scope ADR-0005
committed to.

## Decision

**5etools is this project's single source of truth for D&D 5e game content.** Starting
with spells, and extending to classes, species, items, feats and creatures as those get
built: any fact about a piece of D&D content — mechanics, flavor text, tags — is pulled
from 5etools' own data files, never hand-authored or guessed, even when a human author
would probably get it right.

**Scope for now: the 2014 ruleset only.** 5etools carries both the 2014 fifth edition
(`source: "PHB"` and its expansions) and the 2024 revision — informally "D&D One" or
"5.5e" — under `source: "XPHB"` and its own family of source tags, in a **separate**
data file per book (`spells-phb.json` vs `spells-xphb.json`, etc.), not interleaved.
Only 2014-tagged sources are imported into this project's `dnd-5e` system for now — see
`changelog.md`'s 2026-09-17 spell-data entry for the first pass. **2024 content is a
candidate future ruleset in its own right — see `roadmap.md`'s "Second game system"
phase — never a filter or a variant bolted onto `dnd-5e` itself**, matching this
project's own strategy-per-system architecture (`adr-0003-code-per-game-system.md`):
a different edition of the rules is a different `GameSystem`, not a flag.

## Rationale

- **Correctness at scale.** Spells alone already surfaced a real bug (a save DC's
  ability was conflated with the caster's spellcasting ability) that hand-authoring
  let slip through unnoticed. Classes, items and creatures have far more numeric
  and cross-referencing detail than spells; pulling from one maintained, structured
  source is the only way to keep that accurate as the catalogue grows.
- **One conversion path, many content types.** 5etools' own per-entity-type file
  layout (`data/spells/`, and presumably `data/class/`, `data/items.json`, etc.) means
  the same "read 5etools JSON, strip its `{@tag}` markup, map fields, emit this
  project's own catalogue JSON" shape can be reused across content types instead of
  inventing a bespoke pipeline per kind of entity.
- **2014-only keeps the existing system's identity stable.** `dnd-5e` was built
  against 2014 rules throughout phases 3–10; silently absorbing 2024-revised spells
  (some of which change mechanics, not just wording) into the same system would
  quietly break that identity. Keeping them apart until the owner actually wants a
  second system avoids a half-migrated mess later.

## Consequences

- A general 5etools → this project's catalogue-JSON converter is **built**
  (`systems/dnd-5e/features/5etools-ingestion.md`, three slices, all 2026-09-17 — see
  `changelog.md`'s "5etools ingestion pipeline, slice 1/2/3" entries), triggered by a
  concrete violation the owner caught live: seeded spell `description` text was a
  hand-shortened paraphrase, not 5etools' real wording, because the first
  field-enrichment pass never touched `description`. The tool itself
  (`./gradlew :apps:api:ingest5etools`) is tested and works; it has not yet been run
  against the owner's real 5etools download, so `content/dnd-5e/spells/` still holds
  its original 11 hand-typed files — see that document's own "Done when" for what's
  still open.
- Every future content type needs its own field mapping from 5etools' schema to this
  project's own domain records, same as `Dnd5eSpell` gained `saveAbility`/
  `components`/`materialComponent`/`duration` from `spells-*.json`'s `savingThrow`/
  `components`/`duration` shapes.
- Catalogue entries already carry `sourceBook`/`sourcePage` (`CatalogueEntry`) — worth
  keeping populated from 5etools' own `source`/`page` fields for every future import,
  since the open question below depends on knowing which book an entry came from.

## Filtering out non-core sources — open again, reverted 2026-09-17

5etools' own source list includes official-but-unusual releases the owner doesn't
consider "core" material in spirit, even though Wizards of the Coast published them —
named examples: *A Copper for a Song*, *Dungeons & Dragons vs. Rick and Morty: Basic
Rules*. Many of 5etools' included sources are this kind of collaboration/promotional
one-shot rather than a mainline rulebook or setting book.

**Same-day reversal.** Earlier in the 2026-09-17 session that answered
`systems/dnd-5e/features/5etools-ingestion.md`'s open questions, the owner initially chose to exclude
collab/promotional sources at ingestion time entirely. Revisited later the same day,
once real 5etools data was actually in hand and the ingestion tool was about to run
for real against the owner's local `tools/5etools-data/`: the owner reverted to this
ADR's **original** proposal instead — **import everything** (still except `XPHB`, this
ADR's own 2014-only scope rule, unchanged) **and build a D&D Beyond-style
character-creation-time toggle in phase 11** letting the player choose whether to
include non-core/collab sources for their own character, rather than deciding it once
for the whole catalogue at ingestion time.

**What the same-day investigation found, for whoever builds phase 11's classification
step:** checked the real, downloaded `books.json`/`adventures.json` for a
machine-readable marker. Most of the 17 non-`XPHB` sources present in this machine's
2026-09-17 download have an ordinary `group` value (`core`, `supplement`, `setting`)
and look like legitimate full sourcebooks, including the MTG/Acquisitions-Incorporated
crossover ones (`GGR`, `EGW`, `SCC`, `AAG`, `AI`). Four stood out as likely
collab/promotional: `LLK` (Lost Laboratory of Kwalish, the one book with
`group: "supplement-alt"` — a distinct value from every other source) and `AitFR-AVT`/
`SatO`/`FRHoF`, none of which appear in `books.json` or `adventures.json` at all
(smaller D&D Beyond-exclusive content, not indexed as full books). The owner was shown
this exact list and chose not to exclude any of them from the catalogue — this finding
is a starting point for phase 11's own classification step, not a decision already
made for it.

**Unearthed Arcana follows the same rule — owner decision, 2026-09-23.** UA is
playtest material, not a published release, but it is imported like everything
else. At character creation, the player opts into UA content with its own toggle,
alongside the collab toggle above. Neither is decided at ingestion time. The one
ingestion-time exclusion remains the 2024 revision: the 2024 PHB and the other
`X` core books are treated as a different version of the game (informally "5.5e"),
never as part of `dnd-5e`. Where 5etools tags an entry with `edition: "one"` (class
files do), that tag also marks it as 2024 content.

**Consequence for the ingestion pipeline:** `FiveEToolsSourceClassifier`
(`systems/dnd-5e/features/5etools-ingestion.md`'s pipeline stage 2) excludes only 2024-rules
sources. Collab and UA sources stay in scope until phase 11's toggles exist. See
`roadmap.md`'s phase 11 entry.

**Amendment, owner decision 2026-09-24: "2024 rules" is decided by publication
date.** Every book or adventure that 5etools' `books.json` / `adventures.json`
dates on or after the 2024 Player's Handbook (`XPHB`, 2024-09-17) was written for
the 2024 rules, so it belongs to the future 5.5e system, not to `dnd-5e`.
- **Why a date, not a tag:** most of these books' entries (FRHoF, EFA, Lorwyn,
  Arcana Unleashed, Astarion's Book of Hungers, Ravenloft: The Horrors Within, and
  the 2024-era adventures) carry no `edition: "one"` tag, so the tag alone let
  their feats and spells into `dnd-5e`.
- **What the owner checked:** none of these books is a collab (all are by
  Wizards RPG Team).
- **What gets excluded:** 35 sources on 2026-09-24. The `edition: "one"` rule for
  classes, species and backgrounds stays as well.
- **Nothing is lost for 5.5e:** the raw 5etools data under `tools/5etools-data/`
  is untouched. A future 5.5e system ingests these same sources into its own
  `content/<system>/` tree.
- **The files follow:** the ingestion runner now deletes any file it no longer
  produces, and the catalogue import prunes the matching rows. That removed the
  stale XDMG/XMM item files.

**D&D 5e book names drop "(2014)"** (owner decision, 2026-09-24). Inside `dnd-5e`
there is only one edition of each core book, so "Player's Handbook (2014)" is shown
as "Player's Handbook".

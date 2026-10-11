# Feature — Vampire: The Masquerade 5th edition (second game system)

Status: **closed for now (2026-10-04, owner).** All slices are built
(creation, sheet, play state, XP, rolls, rituals, alchemy, loresheets,
inventory, coterie); see §5b for what was done and what is still open. This
is roadmap phase 12 ("Second game system").

The reference is Demiplane's VtM Nexus, used the way D&D Beyond is used for
D&D 5e: to learn what a player expects, not to clone it.
- Reference sheet: `https://app.demiplane.com/nexus/vampire/character-sheet/83ec857f-c1e5-4343-b87a-4f9ecc628f1b`
  ("Test VTM", the owner's character: Toreador, Neonate, Consensualist).
- Builder of the same character: `https://app.demiplane.com/nexus/vampire/character-builder/83ec857f-c1e5-4343-b87a-4f9ecc628f1b`.
- Character list: `https://app.demiplane.com/characters?nexusId=36`.

**Read-only rule for the reference character:** don't click trackers (health,
willpower, humanity, hunger), the Rouse Check button, "Re-roll", +/- on
ratings, or anything in the builder that selects or removes. All of those change
the owner's character. Normal rolls from the dice pool are safe.

---

## 1. Walkthrough of Demiplane's sheet (2026-10-03)

### 1.1 Layout, top to bottom

| Area | Content |
| --- | --- |
| Header | Portrait · name + ≡ menu · "Clan Generation-tier Predator type" subtitle · Bane and Compulsion (links) · round **Roll / Rouse Check** button · Experience `15/15` (available/total) |
| Attributes | Three columns, Physical / Social / Mental, three each: name, number, 5 dots |
| Skills | Same three columns, nine skills each. Collapsed: two sub-columns of name + number ("--" for 0). **Expand** shows one list with 5 dots and each skill's specialties under its name (e.g. Persuasion → "Victims"); an "S" badge marks a skill with a specialty |
| Trackers row | Health (n) boxes · Willpower (n) boxes · Humanity (10 boxes, filled to the rating) · Hunger (5 boxes) · Blood Potency (number) · Resonance (text) |
| Tabs | Disciplines & Powers · Merits & Flaws · Coterie · Concept & Lore · Inventory · Notes |
| Left dock | Roll log (each roll: name, successes, re-roll, die details) and the Dice Pool tray |
| Right sidebar | Detail of whatever was clicked (one at a time, with back and close) |

### 1.2 Tabs

| Tab | Content |
| --- | --- |
| Disciplines & Powers | Search + Manage. One group per discipline (icon, name, dots, collapse). Each power row: name · cost ("Free", "One Rouse Check") · dice pool (e.g. "Resolve + Auspex", red, clickable) · summary · level |
| Merits & Flaws | Search. Merits list (name, category, e.g. "Prey Exclusion — Feeding"), Backgrounds, Loresheets, each with Manage |
| Coterie | Name, type, city, chronicle tenets, goals, boons & debts; Domain (Chasse, Lien, Portillon) with description and resonance; coterie pool `0/2`; members; coterie backgrounds |
| Concept & Lore | Concept, chronicle, generation, sire, true age, apparent age, dates of birth and death, appearance, distinguishing features, history; Beliefs: convictions, touchstones, ambition, desire |
| Inventory | Search, "Add items", list ("No Items"), "Other possessions" free text |
| Notes | Free text |

### 1.3 Interactions

- **Clicking a trait name** (attribute, skill, tracker label, bane, power) opens
  the sidebar:
  - type label, name, rating with − / + and the **XP cost of the next dot**
    ("Cost: 6 EXP") — XP spending happens from the sheet;
  - specialties with Manage, a notes field;
  - the rules text, and for attributes and skills a table of what each dot
    means.
  - Health shows its maximum, notes and the healing rules; Bane shows its
    severity and text.
- **Clicking a rating number** adds the trait to the **dice pool** (the row gets
  an outline). The pool lists each trait with one diamond per die.
- **Clicking a power's dice pool** ("Resolve + Auspex") highlights both traits
  and loads the pool.
- **Dice tray:** Regular and Hunger buttons add single dice by hand; Clear;
  Roll. Hunger dice replace regular ones up to the current Hunger.
- **Roll result** in the log: "Athletics + Dexterity — Successes: 1", with each
  die's face and kind (regular / hunger). Each entry has **Re-roll** (the
  Willpower re-roll).
- **The round button** in the header is the Rouse Check (one die, 6+ succeeds;
  a failure raises Hunger).
- **≡ menu** opens the character panel: Character Builder button, name, portrait,
  six **sheet themes** (Embraced, The Camarilla, The Anarchs, The City Unlife,
  Bloodstained, Night Light), viewing permissions, share link, export to PDF.
- Not verified, to keep the reference character intact: how a tracker box cycles
  (expected: empty → superficial → aggravated → empty).

## 2. Walkthrough of Demiplane's builder

The owner's impression is that "Create character" lands on the sheet in edit
mode. **Demiplane also has a separate builder**, reached from the sheet's
≡ menu → "Character Builder". Its structure is useful input for ours:

| Step | Content |
| --- | --- |
| Getting Started | Pre-gen characters, name, **Sea of Time** (age tier, e.g. "Neonate: Embraced 15–50 years ago" → "+1 Blood Potency, +15 EXP"), portrait (upload or presets), preferences |
| Creation → Clan | Filterable clan list (13 + Caitiff and Thin-blooded; most locked behind a purchase); right pane: nicknames, art, description, disciplines, bane, compulsion |
| Creation → Attributes & Skills | **Slot placement:** attributes one at 4, three at 3, one at 1 (the rest stay 2). Pick a slot, then the right pane lists every trait with Select / Acquired / Selected. Skills: choose a distribution (Jack of All Trades / Balanced / Specialist), then the same slot placement, plus 3 specialties |
| Creation → Disciplines & Powers | Two clan disciplines (one at 2 dots, one at 1), then one power per dot |
| Creation → Predator Type | Its own choices: a specialty, a discipline dot and its power, and the merits and flaws it grants (Consensualist: Medicine or Persuasion specialty, Auspex or Fortitude, +1 Humanity, Dark Secret and Prey Exclusion flaws); its predator pool |
| Creation → Advantages & Flaws | Point budgets (Advantages `1/7`, Flaws `0/2`), Merits, Backgrounds, Loresheets, "Contribute to coterie (max 7)" |
| Concept & Lore | Character details (0/11 fields) and Beliefs (0/4) |
| Progression | Experience (age XP, total, available, add XP) and the XP cost table (attribute new level × 5, skill × 3, specialty 3, clan discipline × 5, other × 7, Caitiff × 6, ritual × 3, formula × 3, advantage 3 per dot); then spending pages for each trait family and Blood Potency |
| Coterie | Coterie details |

- The left rail shows a check per finished step and a red count of pending
  choices ("Creation 8", "Concept & Lore 28").
- The header keeps the derived values in view: Health, Willpower, Humanity,
  Blood Potency, Experience.
- Every step has a long "About…" rules pane on the right.

## 3. What we keep and what we don't

| Keep | Don't copy |
| --- | --- |
| Separate builder, then the sheet (the owner's choice; our D&D draft → finish flow already works this way) | Landing on the sheet in edit mode to create |
| Slot placement for attributes and skills, with the remaining slots counted | Content locked behind purchases |
| Live derived values while building (Health, Willpower, Humanity, BP, XP) | The long rules essays on every step: our explainer shows the consequence of the choice in hand |
| Dice pool built by clicking ratings; a power's pool loads in one click | |
| Hunger dice, successes, criticals, messy critical and bestial failure computed **on the server** | |
| Sidebar detail with the "what each dot means" table | |

## 4. Where Vampire breaks today's seams

Phase 12's rule: adding the system needs no edits to the shell, the character
list, the sidebar, the dice engine or D&D's code; if it does, stop and discuss.
These are the places where that rule already fails, so they are the discussion:

| Seam | Problem | Proposal |
| --- | --- | --- |
| `ruleset.RollKind` | A closed enum of D&D roll kinds | Ask the system for a roll by an open request: `kind` as text plus the trait keys of a pool |
| `ruleset.ResolvedRoll` / `dice.RollService` | `diceCount d diceSides + modifier`, total, advantage. VtM rolls a pool of d10s, counts successes (6+), pairs of 10s, and splits regular and hunger dice | A per-system `RollInterpreter` strategy: the dice to roll, then how to read them (successes, critical, messy, bestial). `Roll` keeps the raw dice and gains a JSONB `outcome` (migration) |
| Rolls that change the sheet | Rouse Check (Hunger), Willpower re-roll (willpower damage) | The same shape as `spendHitDice` today: one server call that rolls and mutates |
| `catalogue.CatalogueEntryKind` | A shared enum of D&D kinds | Per-system kinds (clan, discipline, power, predator type, merit, flaw, background, loresheet) — either more enum values or a kind text per system; decide in V1 |
| Web `CharacterSheetScreen` | Renders D&D components directly (developer-guide 3.4.2) | A sheet-layout registry per system, like `builderDefinitions.ts` |
| Web `CharacterSheet` type, `dice/` icons | D&D's shape and icons | Per-system sheet types; the dice tray renders a system's result |

## 5. Proposed plan (vertical slices)

Every slice ends with tests and a live comparison against the reference sheet,
as D&D slices end with D&D Beyond.

- **V0 — Decisions (no code).** The owner answers §6; an ADR for the content
  source; this document becomes the approved spec.
- **V1 — Generalize the seams (§4) with D&D still green.** Open roll requests,
  `RollInterpreter`, roll `outcome` column, catalogue kinds per system, the
  web sheet-layout registry. D&D's behaviour must not change: its tests pass and
  a live D&D Beyond check of rolls and the sheet confirms it.
- **V2 — VtM content.** `content/vtm-v5/`: clans, disciplines, powers, predator
  types, merits, flaws, backgrounds (loresheets later), imported by the
  existing pipeline.
- **V3 — Read-only sheet.** `ruleset.vtm5`: `Vtm5GameSystem`, `Vtm5Sheet` (JSONB,
  `schemaVersion`), `Vtm5SheetCalculator` (Health = Stamina + 3, Willpower =
  Composure + Resolve, Blood Potency effects, clan bane and compulsion).
  `src/systems/vtm5/`: header, attributes, skills (collapsed and expanded),
  trackers row, the six tabs, sidebar details. A test character from a build
  file, like D&D's.
- **V4 — Rolls.** Dice pool from ratings and power pools, hunger dice,
  successes, critical, messy critical, bestial failure, Rouse Check, Willpower
  re-roll, all in the game log.
- **V5 — Play state.** Health and Willpower damage (superficial and
  aggravated), Humanity stains, Hunger, notes, inventory, Concept & Lore fields.
- **V6 — Creation builder (separate screen).** On our `BuilderDefinition`:
  Basics (name, portrait, age tier) → Clan → Attributes → Skills (distribution,
  specialties) → Disciplines and powers → Predator type → Advantages and flaws
  (point budgets) → Concept and beliefs (convictions paired with touchstones) →
  Review. The summary keeps Health, Willpower, Humanity, BP and XP in view and
  shows what each choice changes before it is made.
- **V7 — Progression.** Spending XP with the cost table, as a separate page like
  D&D's level up, with the history kept.

### 5b. V1 design after reading the seams (2026-10-04) — needs the owner's OK

The owner said to go ahead with the sheet (creation and view), which answers
Q4 for the web registry. Reading the backend seams showed they are
D&D-shaped beyond what §4 listed, so nothing was coded yet:

- `SheetCalculator.calculateVitals` returns D&D's `VitalsZone`.
- `SheetMutator` is ~60 D&D methods (spells, hit dice, attunement, coins…).
- `CharacterCreationFlow.withLevelUp` assumes classes and levels.
- The sheet endpoints and `CharacterSheetResponse` follow D&D's fields.

Proposal (Open/Closed: D&D's code and endpoints stay as they are; they become
"D&D's own"):
1. **Read:** a new per-system seam `SheetPresenter { systemId(); Object
   present(String sheetJson, CatalogueLookup) }` behind a registry, and a
   generic endpoint `GET /api/characters/{id}/view` → `{ systemId, view }`.
   VtM implements it with `Vtm5SheetView` (attributes, skills + specialties,
   disciplines + powers, trackers, blood potency effects, bane/compulsion,
   identity, beliefs, merits, XP log).
2. **Write:** a per-system `SheetActionHandler { systemId(); String
   apply(String sheetJson, String type, JsonNode payload) }` and
   `POST /api/characters/{id}/actions` (spend XP, undo, add XP, damage, hunger,
   stains, notes, identity fields…). D&D keeps `SheetMutator`.
3. **Rolls (V4):** `POST /api/characters/{id}/pool-rolls` with trait keys; a
   `RollInterpreter` strategy reads successes, critical, messy, bestial; the
   roll row gains a JSONB `outcome` (migration). Rouse Check and Willpower
   re-roll roll and mutate in one call, like `spendHitDice`.
4. **Creation:** `withLevelUp` becomes a default method that throws
   `LevelUpNotAllowedException` (systems without levels don't override it).
   `Vtm5CreationFlow` plans the builder steps chosen in the mockups.
5. **Content:** for the first slices, clans, disciplines/powers, predator types
   and advantages ship as one owner-curated JSON in `ruleset/vtm5`
   resources (short summaries, our words), not through the catalogue import.
   Catalogue kinds per system wait until a list needs search or sources.
6. **Web:** `sheet/sheetLayouts.ts` registry (systemId → layout component);
   today's `CharacterSheetScreen` becomes D&D's entry. VtM's layout follows
   `sheet-v2.html`; its builder follows `builder-v2-c-aside.html` on
   `BuilderDefinition`.

**Revised order:** V1a (seams 1, 2, 4, 6 with D&D green) → V3 read-only sheet
from a build file → V6 builder → V4 rolls → V5 play state → V7 XP panel.

**Owner's answer (2026-10-04), recorded as adr-0009:** shared platform for
what all systems do alike (create character/draft, pick the system,
portraits, list, builder frame), routed by system id; everything else is each
system's own structure in its own folder, backend and web. So items 1–3 above
become **VtM's own** read endpoint, actions and rolls under `ruleset/vtm5`
(no generic `SheetPresenter`/`SheetActionHandler` for now); item 4 (optional
`withLevelUp`) and item 6 (web layout registry) stay. Web: reuse D&D's visual
components (sidebar, frames, tabs, dice tray, builder checklist/summary),
themed to match the mockups.

### Where it stopped

2026-10-04, V1a in progress:
1. ✅ Web `sheet/sheetLayouts.ts` registry (+ test); `AppShell`'s sheet route
   picks the layout by system and sends unknown systems back to the list.
2. ✅ `CharacterCreationFlow.withLevelUp` is a default that throws
   `LevelUpNotAllowedException`.
3. ✅ Survey. Already routed by system id: character creation
   (`GameSystemRegistry`), drafts and finishing (`CharacterCreationFlowRegistry`),
   list summaries (`CharacterSummarizerRegistry`), portraits, builder
   definitions, level-up pages, header marks. Still D&D-bound in the shared
   draft flow (`character/CharacterDraftService`, also `CharacterBuildApplier`):
   - finishing calls `CharacterSheetService.withStartingEquipment` (D&D items);
   - the draft preview computes D&D's `VitalsZone` via `SheetCalculator`, and
     `DraftView`/`DraftPreviewResponse` carry it.
   `CharacterSheetService`, `SpellCatalogueData` and the `ruleset` item/spell
   records are D&D's own sheet code (adr-0009: they stay).
4. ✅ Shared draft flow: finishing applies starting gear only when the system
   returns some; VtM's preview has no sheet, so no D&D vitals are computed.

**First deliverable, 2026-10-04 (owner: be practical, tests later):**
- API `ruleset/vtm5`: `Vtm5GameSystem` (`vtm-v5`), `Vtm5Content` (curated
  `resources/ruleset/vtm5/vtm5-content.json`, converted from the mockups'
  `vtm-data.js`), `Vtm5CharacterCreationFlow` (plan with the creation rules,
  materialize into the VtM sheet JSON, `buildOf`), `Vtm5CharacterSummarizer`,
  `Vtm5SheetController` (`GET /api/vtm5/content`,
  `GET /api/vtm5/characters/{id}/sheet` with derived values).
- Web `systems/vtm5`: builder on the shared frame (`vtm5BuilderDefinition`,
  `Vtm5StepPage`, `Vtm5Explain`; since the same day the whole page is VtM's own
  `Vtm5BuilderFrame` through `BuilderDefinition.Frame`, styled by
  `vtm5-builder.css`), read-only sheet `Vtm5SheetScreen` (sheet-v2
  layout, Lore & Identity tab, detail sidebar), `Vtm5SystemMark`, `vtm5-builder.css` and `vtm5-sheet.css`
  (scoped ports of the mockups' CSS). Registered in `supportedSystems`,
  `builderDefinitions`, `sheetLayouts`, `systemMarks`. Fonts Cormorant and
  EB Garamond added to `index.html`.
- **Not done / debt:** no VtM tests yet (owner deferred them); not run in a
  browser; the builder's left checklist keeps D&D's light styling; the sheet
  has no rolls, trackers editing, XP panel or notes editing (V4, V5, V7); the
  sidebar is VtM's own, not yet D&D's reused sidebar components.

**2026-10-04, V5 done (owner's pick):** play state and the XP panel — see
the changelog entry "VtM V5". Needs an API restart to try.

**2026-10-04, V4 done:** server-side pools, Rouse Check, Willpower re-roll
and the roll log (changelog "VtM V4"). Bestial failure is read as "no
successes" because rolls carry no Difficulty yet.

**2026-10-04:** VtM tests written (46, 98 % line coverage of `ruleset/vtm5`);
name and portrait editable from the sheet.

**2026-10-04:** Difficulty on rolls and advantages bought with XP done.

**2026-10-04:** rituals (free at creation, bought with XP), frenzy and
Remorse rolls, VtM footer, developer guide §2.4.2 done.

**2026-10-04:** thin-blood alchemy, loresheets, inventory and VtM web tests
done.

**2026-10-04:** coterie and domain done, per character like Demiplane
(owner's call: sheets aren't shared, only the owner edits), points by the
book. The plan's slices are complete.

**Still open:** a browser pass by the owner over everything added today;
before a public push, the licence of the Demiplane images and keeping
`character-rules.md` out of the remote. Later ideas not in this plan: sheet
themes, PDF export, ghouls and mortals.

**Later, out of this plan:** coterie and domain shared between players,
loresheets, sheet themes, blood sorcery rituals, thin-blood alchemy, ghouls and
mortals, PDF export, frenzy and remorse helpers.

## 5a. Mockups (2026-10-04)

Static mockups in `design-reference/mockups/vtm-v5/` (local only: that
folder is git-ignored; open `index.html`, or serve the folder with
`python -m http.server` and open `http://localhost:<port>/vtm-v5/`). All use
Demiplane's palette and type measured on the live sheet: red `#C82434`, rows
`#101010`, text `#CFCFCF`, Cormorant headings, Gill Sans labels (Cabin as the
web fallback), Bodoni group labels. Drawings and wording are ours.

- **Sheet, four structures:** 1 Demiplane (single column; interactive
  sidebar and dice pool with hunger dice), 2 our D&D sheet's structure,
  3 a play dashboard, 4 the printed sheet.
- **Creation, four structures:** 1 Demiplane (three columns, slot
  placement), 2 our D&D builder's structure, 3 guided stepper with cards,
  4 single page.

The owner picks one per screen (or mixes parts) before V3 / V6 start.

**Owner's picks (2026-10-04) and the v2 mockups:**
- **Sheet:** sheet 2's structure, plus the paper sheet's identity fields
  (Concept, Chronicle, Clan, Predator, Sire, Generation, Age, Ambition, Desire)
  as the Identity sub-section of the tab "Lore & Identity" (decided after v2:
  not a box above the Blood), plus Demiplane's sidebar for every trait: summary, what
  each dot means, Manage, notes. Built as `sheet-v2.html`. Its sidebar spends
  XP with −/+, i.e. it mocks the "also from the sidebar" answer to Q5; Q5 is
  still open.
- **Builder:** builder 2's structure; clan choice with builder 3's
  descriptions; a short description on every choice (as builder 1 does);
  attributes and skills by clicking dots as in builder 4. Where the long
  explanation lives is still open, so v2 has three interactive variants that
  share one draft: **A split** (beside a compact list in the middle),
  **B inline** (opens under the clicked item's row), **C aside** (the right
  column, summary moved to a strip). **Picked: C aside** — a Vampire
  character has few summary values and long option descriptions, so the
  summary goes up to the strip and the column goes to the descriptions.
  Steps built: basics, clan, attributes,
  skills with specialties, disciplines with powers, predator type,
  advantages and flaws, beliefs, review.
- **App header:** the mockups' system mark (red diamond, "Vampire: The
  Masquerade" in the display serif, "5th edition" muted) becomes VtM's entry
  in `apps/web/src/shell/systemMarks.ts`, which D&D already uses. The
  display serif is not loaded by the app yet; adding it is part of that
  slice and goes through `tech-stack.md`.

## 6. Questions for the owner

**Update 2026-10-03:** the owner supplied the core rulebook, the Camarilla
and Anarch sourcebooks, and Paradox's free basic-mechanics sheet. Their rules
and character options are compiled in `systems/vtm-v5/character-rules.md`.
**Answered 2026-10-03:**
- **Q1 (content source):** option (a), the owner's books.
- **Q2 (which books):** core plus the Camarilla and Anarch modules, with all
  their options: 9 clans plus Caitiff and thin-bloods, and all loresheets.
- **Attribute spread:** follow Demiplane (one at 4, three at 3, four at 2,
  one at 1).
- **Rule conflicts:** the books win; the basic-mechanics sheet is only a
  summary.

- **Q3 (timing):** D&D first. Vampire starts after the pending D&D queue:
  spellcasting P2, sidebar 9c–9g and 10, then desktop 0.2.0.

- **Q5 (XP spending), answered 2026-10-04:** from the sheet's sidebar, as
  Demiplane does, not a separate Progression page: Vampire has no levels,
  each spend is one dot. Plus an XP panel (opened from the header's XP):
  earned / spent / available, "Add XP" for Storyteller awards, and the
  spend history. A dot bought by mistake is refunded by undoing its history
  entry (the latest spend on that trait), not by a free "−" on the trait.
  Narrative conditions (out-of-clan needs a taste of the blood; raising a
  discipline needs matching Resonance) show as reminders, never block.
  Mocked in `sheet-v2.html`. This changes V7 (Progression) into "XP panel
  and history" on the sheet.

Question 4 is still open; it's asked again when V0 starts.

1. **Content source.** There is no 5etools for Vampire, and the books are
   Paradox's. Options:
   - a) owner-curated JSON from the books you own, mechanics plus short
     summaries, with the redaction switch (adr-0005) on outside the desktop
     edition;
   - b) an open community dataset, if one with a usable licence exists (to be
     researched);
   - c) mechanics only, no rules text.
   Recommendation: research (b) first, fall back to (a).
2. **Which books.** Core rulebook only to start, or also Players Guide,
   Camarilla, Anarch and Cults of the Blood Gods?
3. **Timing.** Pending D&D work (sidebar 9b–9g, spellcasting P2, desktop
   0.2.0) and the "push only when D&D is done" policy: does Vampire start now,
   in parallel, or after them? The roadmap says one phase at a time.
4. **V1 first?** It touches the dice engine and the D&D sheet screen, which
   phase 12 calls "stop and discuss". Approving V1 is approving those edits.
5. **XP spending:** only on a separate Progression page (recommended, like
   level up), or also from the sheet's sidebar as Demiplane does?

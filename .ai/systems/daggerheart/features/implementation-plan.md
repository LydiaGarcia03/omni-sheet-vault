# Daggerheart — implementation plan

Status: **D0–D7 done 2026-10-12: every planned slice shipped.**
Rules: `../character-rules.md`. Architecture: adr-0009 (shared platform
routed by system id; each system owns its sheet, actions, rolls and
endpoints). System id: `daggerheart`; code in `ruleset.daggerheart` and
`apps/web/src/systems/daggerheart`.

## 1. Scope

- Core Set **and Hope & Fear**: both are in the SRD 2.0, so one content
  import covers the 13 classes, 10 domains (incl. Dread), 24 ancestries,
  15 communities, transformations, 210 domain cards, Tier 1–4 weapons,
  armor, loot and consumables.
- Out: campaign frames (licence), adversaries and environments (GM-side),
  the GM's Fear pool, playtest and partner content.

## 2. Licence constraints (decide before code — D0)

- The DPCGL allows the SRD in **private, non-commercial play**. Sharing it
  publicly is limited to print, streams and five whitelisted VTTs; anything
  else needs DRP's written approval (`../character-rules.md` §0).
- Consequences:
  1. Same push policy as D&D: SRD-derived content never goes to the public
     remote until the licence question is settled.
  2. The desktop edition (one user, local) and a private server for a
     gaming group are fine.
  3. A public deployment with Daggerheart content needs approval, or a
     build without the content (adr-0005's redaction switch).
  4. Footer attribution + "Daggerheart™ Compatible"; no official logo or
     art — our own portraits and marks only.
- D0 writes this down as **adr-0010** once the owner agrees.

## 3. Demiplane walkthrough (reference, 2026-10-04)

Screenshots: `design-reference/screenshots/daggerheart-demiplane/`.

**Sheet.** Header: portrait, name, "Slyborne Fungril Ranger (Wayfinder)",
domain banner "Bone & Sage", hints toggle, Downtime, Conditions, Level.
Stat row: Evasion, Armor score, Armor slots, six traits with their verbs
(Agility — Sprint, Leap, Maneuver…). Second row: HP & Stress panel
(Minor / Major / Severe with the threshold numbers between them, HP and
Stress slot tracks with ±, a damage calculator), Active Weapons
(Proficiency pips, primary/secondary attack buttons), Hope (6 diamonds) and
Experiences. Tabs: *Effects & Features* (loadout domain cards with cost
buttons, "View Vault"; class Hope feature, class feature, subclass with
Spellcast trait, ancestry and community features), *Equipment* (gold
handfuls/bags/chest, item list with Active toggle), *Details* (description
prompts, background questions, connections), *Journal*. Dark navy panels,
gold frames, parchment card bodies.

**Builder.** Three columns: step rail (Getting Started, Class, Heritage,
Experiences, Level Up, with checkmarks), the form, a rules panel. Traits
always visible in the header. Class step also holds subclass, traits,
weapons, armor, inventory and the domain-card picks. Demiplane lands on an
editable sheet after creation; we keep our builder → finish flow (owner's
VtM preference).

**What we take:** the stat row, threshold strip between HP boxes, Hope
diamonds, cards-as-cards loadout with a vault, trait verbs. **What we
change:** separate creation screen, explanations on every choice (our
builder standard), server-side Duality rolls with history.

## 4. Slices

| Slice | Deliverable |
| --- | --- |
| **D0** | Done 2026-10-12: §6 answered, adr-0010 accepted, v3 mocks picked with the crest fix |
| **D1** | Done 2026-10-12 (§7): SRD PDF → `content/daggerheart/` (928 entries), count and cross-reference checks, catalogue behind the redaction switch |
| **D2** | Done 2026-10-12 (§8): backend `ruleset.daggerheart`, creation flow and validation, live preview, summarizer, sheet endpoint with derived values |
| **D3** | Done 2026-10-12 (§9): builder v3 as a `BuilderDefinition.Frame` page, hover preview, portrait presets, system mark, mixed ancestry, GM-allowed transformations, starting equipment, the tutorial |
| **D4** | Done 2026-10-12 (§10): sheet v3 via `sheetLayouts`, read-only, with the detail sidebar; Daggerheart is a supported system |
| **D5** | Done 2026-10-12 (§11). Play state + actions: HP / Stress / Armor slots / Hope / gold, conditions, loadout ↔ vault with Recall Cost, short/long rest moves, class resources (Prayer Dice, Favor…) |
| **D6** | Done 2026-10-12 (§12). Server rolls: Duality action and reaction rolls (Hope/Fear outcome, critical, advantage d6, Experience for 1 Hope), damage rolls by Proficiency, history |
| **D7** | Done 2026-10-12 (§13). Level up page via `levelUpPages`: tiers, achievements, two advancements per level, thresholds, new domain card, multiclass from level 5 |

Tests go with each slice (owner may defer them to save credits, as in VtM).

D3 and D4 must work at 1280 x 720 with no horizontal scroll. Prefer a layout
that reflows, and add screenshots at that size to the slice's check (see
`ui-design-system.md`, "Screen sizes").

## 5. Mockups

`design-reference/mockups/daggerheart/` (open `index.html`). Sample
character: Ilsa Thornwake, Slyborne Fungril Ranger (Wayfinder), level 1.

| Mock | Structure |
| --- | --- |
| Sheet 1 — Classic | Demiplane-like: header, stat row, HP/Stress + weapons + Hope row, tabs |
| Sheet 2 — Card table | Compact stat rail on the left; the page is the loadout as a hand of cards, vault drawer below, features as cards |
| Sheet 3 — Play dashboard | Big trackers, Duality roller in the center with the last result, actions list (attacks, Hope feature, card actions) |
| Sheet 4 — Printed sheet | Two-page official-sheet structure on parchment, everything visible at once |
| Builder 1 — Three columns | Step rail · form · rules panel (Demiplane) |
| Builder 2 — Deck building | Every choice is a card to pick; chosen cards fill a hand tray at the bottom |
| Builder 3 — Guided wizard | One question per screen, top stepper, big explainer of the consequence |
| Builder 4 — Fill the sheet | The sheet itself with empty slots; clicking a slot opens its picker |

### 5a. Round 2 (2026-10-05)

Owner feedback: sheet 1's structure with sheet 2's cards, smaller elements,
a sidebar; none of the builders — wanted three columns like D&D (progress ·
choices · how they change the character summary), informative but
uncluttered. Owner supplied Demiplane's graphics (stat-box frames, class
banners and plates, domain icons, title plates, builder background, default
portraits), copied to `design-reference/mockups/daggerheart/assets/`.

- `sheet-v2.html`: compact header (portrait, class banner, level), stat
  row (Evasion/Armor frames, armor slots, six trait frames with verbs),
  vitals row (thresholds strip, HP/Stress, weapons, Hope, Experiences),
  tabs; the loadout and features are clamped cards; clicking any element
  opens the right sidebar (full text, costs, actions, Duality roll with
  history). Floating dice button opens a plain roll.
- `builder-v2.html`: step rail with current pick and checkmarks; choices as
  compact options with one "More" detail at a time; summary column (banner,
  domains, stat frames, traits, thresholds, card hand, gear, last change).
  Hovering an option previews its effect on the summary as deltas.
- The domain sprite (`domain-icons.png`, 3×3, no Dread) was mapped by
  matching the Ranger banner: Codex, Grace, Midnight / Bone, Splendor, Sage /
  Valor, Arcana, Blade — check it.
- **These graphics are Demiplane's / Darrington Press's art**: fine for local
  mocks, not for the product (DPCGL forbids official art; Demiplane's own
  assets aren't licensed to us). D3/D4 need our own equivalents (CSS frames,
  own icons) in the same spirit.

### 5b. Round 3 (2026-10-05)

Owner feedback on round 2: builder incoherent (white text on white, cut
buttons, mixed section styles, corner ornaments unused); sheet layout OK but
misaligned, HP/Stress too small, custom roll unclear, AppHeader not the
shared one, no frame corners.

- `dh-v3.css` is one design system for both pages: the shared AppHeader
  (logo, app name, divider, Daggerheart mark, "← Your characters", user
  menu), `.dh-frame` with the four corner ornaments, a single section title,
  one button family, and an explicit text colour per surface (navy panels
  white, paper panels ink; nothing inherits across).
- `sheet-v3.html`: larger HP/Stress tracks and thresholds, a damage row,
  aligned stat row; the dice button opens a custom roll form (trait,
  advantage/disadvantage, Experience for 1 Hope, modifier, difficulty, damage
  dice) and the result shows above the form with history.
- `builder-v3.html`: rail steps with connected checks and the Demiplane
  "Character sheet" button; choices on paper tiles, class plates, real domain
  cards; the step explanation hides behind (i); summary on paper with stat
  frames, thresholds, traits, card hand, gear and the last change.
- Follow-up the same day: first step renamed **Basics** (platform standard)
  with the standard portrait picker (current portrait, Upload / Pick a
  default portrait / Remove, gallery below with Close). The Class step uses
  the class art the owner supplied (`assets/<class>-class-character-example.png`):
  the chosen class as a wide hero with banner, name plate, stats and
  features; the others as art tiles with plate, domains, Evasion and HP.
  Sheet: HP & Stress column narrower, Active Weapons wider with full words
  ("physical damage", range, burden, feature line, damage button).
- Domain icons use the sprite as a background (masks are blocked when the
  mock is opened from disk).

### 5c. Class crest fix (2026-10-12)

Owner: the class icons were composed wrong. Demiplane overlays them: the
name plate sits on top, and the class banner hangs from behind its left half.
- `builder-v3.html` now draws a `.crest` (plate on top, banner at 17% of the
  plate's width, 45% as wide) in the class hero and at the top of the
  summary.
- The summary's name and domains moved beside the crest.
- The crest (plate over banner) appears only for the **selected** class.
- The class list shows no plates (owner, same day). Each tile has the class
  name large in text, and a small banner on its left carrying the class's
  two symbols.
- Under the name, every tile has two rows: the domains, then Evasion and HP.
- The "Hope & Fear" badge shows only on the selected class.
- The sheet header keeps the banner alone with its domains caption, as
  Demiplane's does.

## 6. Owner questions (answered 2026-10-12)

1. **Licence:** same posture as D&D, with attribution in the footer →
   adr-0010.
2. **Mocks:** v3 is the base, with the class crest fixed (§5c).
3. **Transformations:** the SRD makes them optional "if the GM allows" (p. 4).
   So the builder offers them behind a "GM allows transformations" toggle,
   off by default.
4. **Mixed ancestry:** at creation, now. The player picks the top feature of
   one ancestry and the bottom feature of another.
5. **Starting gold and items:** the SRD sets them at creation:
   - Tier 1 weapons and armor;
   - torch, rope, basic supplies and a handful of gold;
   - a Minor Health or Stamina Potion;
   - a class item.

   So they are picked in the builder (Equipment step), like D&D.

Also required: **a Daggerheart creation tutorial** with its own steps and
style (`features/creation-tutorial.md`, slice 3). It ships with D3, not after
it.

## 7. D1 — content import (done 2026-10-12)

`node tools/daggerheart-import/import.mjs` reads
`tools/daggerheart-srd/DH_SRD_2_2026_08_25.pdf` (gitignored) and rewrites
`content/daggerheart/`. How to run and import: `developer-guide.md` §1.3.

| Directory | Kind | Count | `data` |
| --- | --- | --- | --- |
| `classes/` | `CLASS` | 13 | `domains`, `startingEvasion`, `startingHitPoints`, `classItems`, `hopeFeature`, `features`, `subclasses`, `backgroundQuestions`, `connectionQuestions`, `rules` (sections printed inside the class); Druid adds `beastforms` (24), Brawler `martialStances` (16) |
| `subclasses/` | `SUBCLASS` | 26 | `classSlug`, `spellcastTrait` (null for Brawler, Guardian, Warrior), `foundation`, `specialization`, `mastery` |
| `domains/` | `DOMAIN` | 10 | `classes` |
| `domain_cards/` | `DOMAIN_CARD` | 210 | `domain`, `level`, `type` (ability / spell / grimoire), `recallCost`; the card text is `description` |
| `ancestries/` | `ANCESTRY` | 24 | `features` (two: top, bottom, for mixed ancestry), `family` (Elemental Kin for the four kin) |
| `communities/` | `COMMUNITY` | 15 | `adjectives` (6), `feature` |
| `transformations/` | `TRANSFORMATION` | 6 | `features`, `questions` |
| `items/` | `ITEM` | 624 | `category`: `weapon` (315, incl. 12 combat wheelchairs: `slot`, `tier`, `trait`, `range`, `damage`, `burden`, `feature`, `wheelchair`), `armor` (69: `tier`, `baseThresholds`, `baseScore`, `feature`), `loot` (120) and `consumable` (120) with their `roll` |

- Source: `sourceBook` "Daggerheart SRD 2.0", `sourceCode` `DHSRD`,
  `sourcePage` the SRD page.
- Tags: `core-set` for what the SRD lists as Core Set (classes, domains,
  ancestries, communities), `expansion` for the rest. Loot and consumables
  carry the set the SRD names: `core-set` or `hope-and-fear`.
- Prose (`description`) is redacted by the existing switch (adr-0005).
  Feature texts inside `data` are not, as with D&D.
- Checks (the import fails otherwise): 10 / 13 / 26 / 24 / 15 / 6 / 210
  (3 + 2 per level for each domain) / 315 / 69 / 24 beastforms / 16 stances,
  rolls 1–60 in each loot and consumable set, every class's two domains
  list the class, every Core Set name exists.
- Verified against the PDF's raw text order: 14,541 six-word windows of the
  generated text; the only five that differ are three paragraphs joined
  across a page break and the two rebuilt roll tables (Witch's Commune,
  Moon's Lunar Phases). Weapon and armor numbers match the raw extraction.
- Not imported: Mixed Ancestry is a rule, not an entry (D3 builds it from two
  ancestries' features). Adversaries, environments, campaign frames and the
  supplemental campaign mechanics stay out (§1).

## 8. D2 — backend (done 2026-10-12)

Classes and where each rule lives: `developer-guide.md` §2.4.3.

**Build document** (what D3's builder writes):

```json
{"class":"ranger","subclass":"wayfinder",
 "ancestry":"fungril","mixedAncestry":null,
 "community":"slyborne","transformationsAllowed":false,"transformation":"",
 "traits":{"agility":2,"strength":0,"finesse":1,"instinct":1,"presence":0,"knowledge":-1},
 "experiences":["Tracker","Lone Wolf"],"boostedExperience":null,
 "domainCards":["<level-1 card>","<level-1 card>"],
 "primaryWeapon":"shortbow","secondaryWeapon":"","armor":"leather-armor",
 "potion":"minor-stamina-potion","classItem":"A trophy from your first kill"}
```

- Mixed ancestry: `"mixedAncestry":{"name":"Goblin-Orc","top":"goblin","bottom":"orc"}`;
  the top feature of the first, the bottom feature of the second.
- `transformation` only counts with `transformationsAllowed` (the GM toggle).
- `boostedExperience` (0 or 1) is asked only with Purposeful Design.
- Extra fields (description, background answers…) are kept in `build` untouched.

**Plan choices**, by step (`type`): `class` (class, subclass), `heritage`
(ancestry, community, optional transformation), `traits`, `equipment`
(primary weapon — optional for the Brawler, secondary weapon when the
primary is one-handed, armor, potion, class item), `experiences`
(experiences, boosted-experience), `domains` (domain-cards). Options carry a
one-line summary (domains · Evasion · HP, trait · range · damage · burden…).

**Derived values** carry their contributions, e.g. Evasion = class 12 +
Nimble 1. Always-on bonuses come from `modifiers`, which the importer reads
from the SRD's fixed phrasing (`modifiers.mjs`; 105 entries have them).
Conditional and roll bonuses (attack rolls, Spellcast Rolls, weapon damage)
are left to D5/D6.

**Live summary**: the draft's `selections` (generic `SelectionDetail` cards),
not `abilities`/`vitals`, which are D&D-shaped.

**Not in D2:**
- Selections a class or subclass feature asks for at creation (Warlock
  patron, Beastbound companion, Elemental Origin's element). These go to D3,
  as free text kept in the build.
- Play state and actions (D5), rolls (D6), level-up (D7).

## 9. D3 — builder (done 2026-10-12)

`apps/web/src/systems/daggerheart/` draws builder v3 through
`BuilderDefinition.Frame`. Where each piece lives: `developer-guide.md` §2.4.3.

- **Layout:** the progress rail, the step's choices, and the summary. Nine
  steps: Basics, Class, Subclass, Heritage, Traits, Equipment, Domain cards,
  Experiences, Story (optional). The rail's "Character sheet" button and the
  last step's "Finish character" finish the draft.
- **Display data:** catalogue entries (`GET /api/catalogue`). Which options can
  be picked comes from the plan's choices, and the numbers come from the
  preview's `selections`. No rule is computed in the browser.
- **Hover preview:** resting on an option for 180 ms posts the would-be build
  to `POST /api/characters/{id}/draft/preview`, a new platform endpoint that
  plans and previews without saving. The summary shows the differences in
  purple.
- **Art:** the mockups' Demiplane graphics, resized to WebP (21 MB → 1.1 MB),
  and the 33 Demiplane portraits as presets. See adr-0010 as amended.
- **Footer:** the DPCGL attribution.
- **Tour:** `builder:daggerheart`, whose launcher has its own navy and gold
  style.
- **Platform changes:**
  - `BuilderFrameProps` now carries `preview` and `previewBuild`.
  - `TutorialLauncher` takes the tour's theme class.
  - `previewDraft` added to `draftApi`.
- **Checked live in Chrome:** class, subclass, heritage, traits, equipment,
  domain cards, experiences, and hover deltas (e.g. Halberd shows Finesse −1).
  At 1280 × 720, in an iframe, there is no horizontal scroll. The sticky
  summary scrolls inside its frame.
- **Not done:**
  - Finishing a character was not tried, because the sheet is D4. The
    Daggerheart card stays "Coming soon" until D4 (owner); drafts open by URL.
  - Selections a class or subclass feature asks for at creation (Warlock
    patron, Beastbound companion, element) are not covered yet. The Story
    step's free text holds them for now.

## 10. D4 — read-only sheet (done 2026-10-12)

`apps/web/src/systems/daggerheart/sheet/` draws sheet v3, registered in
`sheetLayouts` as `daggerheart`. Where each piece lives:
`developer-guide.md` §2.4.3.

- **Data:** `GET /api/daggerheart/characters/{id}/sheet` (D2) with its
  `derived` values; no API change. The Details tab reads the class's
  background and connection questions from the catalogue.
- **Layout:** header (portrait, name, heritage line with domains, class
  banner, conditions, level); stat row (Evasion, Armor, Armor Slots, traits
  with verbs); vitals (thresholds strip, HP and Stress, active weapons with
  Proficiency pips and the SRD's unarmed attack, Hope, Experiences); tabs
  Cards & Features, Equipment, Details.
- **Sidebar:** every element opens its full text and its contributions
  (Evasion = Ranger 12, thresholds = armor base + level…). Rules wording
  checked against the SRD.
- **Not in the mock's D4:** the Short/Long rest buttons, ± on the tracks, the
  damage row, card cost buttons and the dice button. They are actions, so
  they come with D5 and D6. The Journal tab became Details' Notes.
- **Supported:** Daggerheart moved from `UPCOMING_SYSTEMS` to
  `SUPPORTED_SYSTEMS`; the home page offers "Create character".
- **Checked live in Chrome:** the test draft finished and opened on the
  sheet; sidebar, tabs, 1280 × 720 in an iframe (no horizontal scroll).

## 11. D5 — play state and actions (done 2026-10-12)

`POST /api/daggerheart/characters/{id}/actions` with one action per call;
`DaggerheartSheetActions` holds the rules (SRD pp. 50–53). Action list and
stored fields: `developer-guide.md` §2.4.3.

- **Trackers and damage:**
  - Slots and ± on HP, Stress, Armor Slots and Hope.
  - Damage against the thresholds, with an optional Armor Slot.
  - Stress overflow marks 1 HP.
  - The last Stress makes the character Vulnerable until one clears.
  - The last HP shows the death moves.
- **Rests:** two downtime moves each.
  - Short-rest moves roll 1d4 + Tier on the server.
  - Moves can be for an ally, and Prepare can be with the party.
  - Three short rests in a row force a long one.
- **Cards:** the loadout holds 5. Recall costs Stress, or is free during a
  rest. Moving a card to the vault is free.
- **Other edits:** conditions, gold with carry-over, inventory, notes, rename
  and portrait.
- **Class resources:** the slice table names Prayer Dice, Favor… They are
  generic **counters** (name, value, optional maximum) rather than one model
  per class: each class words its resource differently, and a counter also
  covers project countdowns.
- **Not in D5:** roll history (the rest dice aren't logged), automatic class
  resources, optional rules (massive damage), resistance, GM Fear.

## 12. D6 — server rolls (done 2026-10-12)

`DaggerheartRollController` resolves and stores every roll through the
platform `RollService`, as VtM does. `DaggerheartDice` reads them back from
their expressions. Endpoints and fields: `developer-guide.md` §2.4.3.

- **Duality:** the trait (or the Spellcast trait), Experiences at 1 Hope each,
  a modifier, one d6 of advantage or disadvantage, and an optional
  Difficulty.
  - With Hope: +1 Hope. A critical also clears a Stress.
  - Reaction rolls: no Hope, no Stress cleared (SRD p. 48).
- **Damage:** a weapon at Proficiency dice, unarmed [Proficiency]d4, or custom
  dice. A critical adds the dice's maximum.
- **History:** Duality, reaction, damage, and the rests' 1d4 + Tier.
- **Dice button:** bottom-left, with the owner's Demiplane icon
  (`assets/dice-open.webp`; covered by adr-0010 as amended). It opens the
  custom roll in the sidebar. Traits, weapons, Experiences and Spellcast
  features open it preset.
- **Not in D6:**
  - Help an Ally, group rolls, re-roll features.
  - The GM's Fear.
  - Feature-specific dice (rolled as custom dice).

## 13. D7 — level up (done 2026-10-12)

**Sources.** SRD pp. 53–54 for the steps. The SRD doesn't print the
advancement grid (slots per tier). It comes from Darrington Press's official
character sheet (`CharacterGuidesAndSheetsTile_HF_2026-07-23.pdf`,
daggerheart.com); the table is in the changelog's D7 entry. Demiplane
levels up inside its builder; we keep a separate page, as D&D does.

**Build.** `levels[]` holds one entry per level after the first:

```json
{"experience":"Quiet Steps","domainCard":"conjure-swarm",
 "advancements":[{"option":"traits","tier":2,"traits":["strength","instinct"]},{"option":"hitPoint","tier":2}]}
```

Options are `traits`, `hitPoint`, `stress`, `experiences` (`experiences`:
two indexes), `domainCard` (`card`), `evasion`, `subclass`, `proficiency`
and `multiclass` (`class`, `domain`, `subclass`). `tier` says which tier's
slot it marks.

**Where.**
- `DaggerheartLevels`: rules and the replay.
- `DaggerheartSheetAssembler`: applying the levels.
- `DaggerheartLevelUpController`: the draft on the sheet, and finishing.
- `DaggerheartLevelUpPage`: the web page.

The platform `CharacterLevelUpService` stays D&D's (adr-0009).

**Not done:**
- exchanging a card at level up;
- picking between two Spellcast traits after multiclassing;
- companion level-ups;
- choosing loadout or vault for the new card.

## Where it stopped

2026-10-12: D7 done (§13). Every slice in §4 has shipped.
- Web: 315 tests pass, type-check clean.
- API: `:apps:api:check` green.

Open follow-ups are listed under each slice's "Not done". The Daggerheart
content and the Demiplane art stay off the public remote (adr-0010).

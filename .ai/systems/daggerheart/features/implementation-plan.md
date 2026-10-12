# Daggerheart — implementation plan

Status: **proposed 2026-10-04, mockups awaiting the owner's pick.**
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
| **D0** | Owner answers §6; adr-0010 (SRD 2.0 + DPCGL); mock picks; v2 mocks if the owner mixes options |
| **D1** | Content import: SRD PDF → `content/daggerheart/*.json` (classes, subclasses + cards, ancestries, communities, transformations, domain cards, weapons, armor, items, consumables), layout-aware extraction + count checks (13/26/24/15/210); catalogue behind the redaction switch |
| **D2** | Backend `ruleset.daggerheart`: game system, creation flow, draft validation (trait array, 2 level-1 cards from class domains, starting equipment), summarizer, sheet endpoint with derived Evasion / Armor Score / thresholds / Proficiency |
| **D3** | Builder (picked mock) as a `BuilderDefinition.Frame` page; portrait presets; system mark |
| **D4** | Sheet (picked mock) via `sheetLayouts`; read-only first |
| **D5** | Play state + actions: HP / Stress / Armor slots / Hope / gold, conditions, loadout ↔ vault with Recall Cost, short/long rest moves, class resources (Prayer Dice, Favor…) |
| **D6** | Server rolls: Duality action and reaction rolls (Hope/Fear outcome, critical, advantage d6, Experience for 1 Hope), damage rolls by Proficiency, history |
| **D7** | Level up page via `levelUpPages`: tiers, achievements, two advancements per level, thresholds, new domain card, multiclass from level 5 |

Tests go with each slice (owner may defer them to save credits, as in VtM).

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

## 6. Owner questions

1. **Licence:** private use only (desktop + private server) until DRP
   approval — agreed? Footer attribution OK?
2. **Mocks:** which sheet and which builder (or which mix)?
3. **Transformations:** offer them at creation behind a "GM allows" toggle,
   or leave for later?
4. **Mixed ancestry** at creation (top feature of one, bottom of another):
   now or later?
5. **Starting gold & items:** pick from the class's item list in the
   builder, or fill them on the sheet afterwards?

## Where it stopped

2026-10-05: round 3 mocks built (§5b: `sheet-v3.html`, `builder-v3.html`);
waiting for the owner's review and the §6 answers (Q2 is now mostly
answered). The heritage parse in the scratch folder swaps features
between neighbouring ancestries/communities — redo it in D1 with
layout-aware extraction (`../character-rules.md` §8).

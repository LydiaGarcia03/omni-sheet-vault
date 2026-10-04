# D&D 5e sheet — fidelity audit (phase 10)

The plan `roadmap.md` phase 10 requires before any fixing starts: a tab-by-tab
comparison of this application's D&D 5e sheet against the live reference,
**https://www.dndbeyond.com/characters/50149479** (Helga Flinthand, a Hill Dwarf
Cleric 4 / Paladin 3), per `systems/dnd-5e/sheet-ui.md`'s "Verifying fidelity against D&D
Beyond."

**This pass is a first survey, not an exhaustive pixel audit.** It covers every tab,
the vitals zone and the sidebar molds at a structural level — layout, what triggers
what, what fields exist — so the real gaps are known before slice work starts.
Fine-grained measurement (exact spacing, colors, hover timing) still happens
per-slice during the actual fix, per the existing "measure, don't guess" rule
(`feedback-dndbeyond-fidelity-verification` in memory). A few interactions
mentioned below were not clicked through this pass (noted inline as "not verified
this pass") — check those live before building the slice that depends on them.

## How to read this document

Each finding is one of:

- **Match** — confirmed the same, or the difference is already a documented entry
  in `systems/dnd-5e/sheet-ui.md`'s "Deviations from D&D Beyond." No action needed beyond
  the fix itself.
- **Fix** — a real gap with an obvious right answer (add the missing field, adjust
  the layout). No scope decision needed, just build it.
- **Needs owner decision** — a real gap where more than one reasonable answer
  exists (rebuild to match exactly vs. formalize as a new deviation, or a genuine
  scope/cost trade-off). Do not build against a guess — ask, then either build the
  match or add the confirmed exception to "Deviations."

## Open questions for the owner

These are the decisions that shape how big phase 10 actually is. Answering them
first, before slice planning, avoids building something that gets reverted.

1. **Saving throws layout.** ~~D&D Beyond renders a 2-column × 3-row grid (STR/INT,
   DEX/WIS, CON/CHA). Our build docs record that a two-column layout was tried and
   deliberately reverted — widening the panel stretched the ornate frame's corner
   ornaments.~~ ~~Resolved 2026-08-15: try the 2-column layout again using the
   plain 9-slice frame...~~ **Superseded and built 2026-08-16 (phase 10 slice 3):**
   the owner's per-section frame assets (phase 10 asset refactor) made the planned
   9-slice retry moot — Saving Throws now has its own dedicated panel frame and the
   row now has its own dedicated frame too, so there is no shared asset left to
   stretch. Built the 2-column grid directly, sized from live measurements
   (`.ct-saving-throws-box`: 281×200, two 107px columns, three 34px rows) — see
   `systems/dnd-5e/sheet-build.md`'s "Saving throw row" row. See "Left column" below for
   the rest of this row's findings.
2. ~~**Inline descriptions vs. sidebar-for-everything.**~~ **Resolved 2026-08-15,
   correcting this audit's original finding:** the owner confirmed by testing D&D
   Beyond directly that features and traits **do** open a sidebar on click, same
   as attacks/spells/items — this audit's browser pass never actually clicked an
   individual feature's name to check, and wrongly concluded there was no sidebar
   trigger there. D&D Beyond's inline text is a summary; clicking opens the
   sidebar with a more detailed version. Our existing sidebar-for-everything
   pattern (`FeaturesTab.tsx`/`FeatureTraitRow.tsx`) is already architecturally
   correct — no rebuild needed. What's still worth verifying live during that
   slice (not a scope question, just a detail to measure): whether our current
   inline text (`FeatureTraitRow`'s `action-row__description`, identical to what
   the sidebar shows) should instead be a shorter summary distinct from a longer
   sidebar version, matching D&D Beyond's summary/detail split — or whether
   showing the same text in both places is fine. See "Tabs — general" below.
3. **Entity Detail's action bar for attacks.** **Resolved 2026-08-15, confirmed
   live twice** (clicked the Warhammer's hit value directly — it rolls inline
   right there, with its own dice animation and log notification; reopened the
   Warhammer's sidebar panel — no roll buttons, only info and Unequip/Move/Delete):
   D&D Beyond never rolls from the sidebar for an attack. Our `AttackRow.tsx`
   already rolls from the row itself, matching this — but `ActionsTab.tsx` (lines
   83-97) *also* duplicates Attack/Damage roll buttons into the Entity Detail
   `actionBar` when an attack's name is clicked, which D&D Beyond doesn't do.
   **Decision: drop the duplicate roll buttons from the attack's action bar** —
   the sidebar becomes informational only for attacks (until equipment management
   like Unequip/Move/Delete exists, it may end up with no action bar at all). Row
   is a small, well-scoped fix in `ActionsTab.tsx`.
4. **Entity Detail's metadata shape. Resolved and built 2026-08-16:** switched
   `EntityDetailRequest.metadata` from `string[]` to a labeled list
   (`EntityDetailMetadataEntry[]`, `{ label: string; value: string }`), rendered
   as label/value pairs instead of a joined inline line. Done as its own slice
   before touching any individual tab's Entity Detail content, since every
   consumer (`ActionsTab`'s attack and feature rows, `SpellsTab`, `ItemRow`,
   `FeaturesTab`, `ExtraRow`) needed its metadata call site updated together.
   The tags row D&D Beyond also shows (Concentration/Ritual on a spell) turned
   out cheap once the shape change was in, so it was folded into this same
   slice as a new optional `tags?: string[]` — see `systems/dnd-5e/sheet-build.md`.
5. **Background characteristics. Resolved 2026-08-15:** add the nine missing
   fields (Gender, Eyes, Size, Height, Faith, Hair, Skin, Age, Weight) to
   `Dnd5eBackground` — all free text, same treatment as the existing fields
   (Alignment, Personality Traits, etc.), no new mechanics. Backend: the record,
   the generic `Background` pass-through, `VitalsZone`/`BackgroundResponse`
   mapping, seed data. Frontend: `BackgroundTab.tsx`'s Characteristics grid gains
   the nine new `FieldBlock`s, laid out to match D&D Beyond's two-row grid.
6. **Extras stat block. Resolved 2026-08-15: invest in a structured stat block**,
   superseding last slice's free-text choice. Scope observed live on D&D Beyond's
   Cat familiar panel, to design in full when this slice starts (not decided
   here): a size/type/alignment line; AC; Initiative (modifier + passive value);
   HP (current/max already exist, plus a formula like "2 (1d4)"); Speed (multiple
   movement types, e.g. walking + climbing); the six ability scores each with a
   modifier and a save value; Skills as a list of skill/bonus pairs; Senses;
   Languages; CR (with XP and proficiency bonus); Traits and Actions each as a
   list of named, individually-described entries. This is the largest single
   slice in phase 10 — a real new domain model (`Dnd5eExtra` grows from one
   `statBlock` string to this whole shape), not a presentational tweak. Treat it
   as its own reviewed slice, same discipline as any other vertical slice in this
   project — propose the record shape before writing it, per `ground-rules.md`.
7. **Character panel mold. Resolved 2026-08-15: defer.** Most of its real content
   (manage character & levels, change sheet appearance) belongs to phase 11
   (character creation) or depends on phase 9 mechanics (short/long rest); a
   thin identity-only version built now would likely be rebuilt once those phases
   land. Not part of phase 10's slice order — revisit when phase 9 or 11 gives it
   real content to show.
   **Icons confirmed live 2026-09-10, ready for whenever this panel gets built:**
   opened via the header's `MANAGE` button — `PencilSvg` (edit character name,
   next to the name itself), `PaintBrushSvg` ("Change Sheet Appearance" button),
   `ManageLevelSvg` ("Manage Character & Levels"), `ManageXpSvg` (uncataloged this
   pass, presumably the XP-tracking equivalent). `PreferencesSvg` (the gear on
   "Character Settings") was already applied ahead of the panel itself — see
   `dnd_icon_settings.svg`, `SectionPanel.tsx`'s `.panel__settings` button,
   `changelog.md`'s 2026-09-10 entry — since it had its own real consumer
   (`ProficienciesPanel`'s gear) independent of this deferred mold. None of the
   other four are extracted yet.
8. **Conditions: checklist vs. "Add Active Conditions." Resolved and built
   2026-08-16 (phase 10 slice 4):** clicking "Add Active Conditions" opens a
   sidebar panel titled "Conditions" listing the same fourteen conditions,
   each with a toggle. Moved the checklist into the sidebar, matching D&D
   Beyond exactly — `DefensesConditionsPanel`'s Conditions column now shows a
   compact `.reveal` trigger ("Add Active Conditions" when none are active,
   or a comma-joined summary of the active ones) that opens the sidebar's new
   Conditions mold (`ui-design-system.md`'s eighth). Verified live: toggling
   a condition from the sidebar updates both the sidebar's own list and the
   trigger's summary immediately. See `systems/dnd-5e/sheet-build.md`'s "Defenses /
   conditions panel" and "Conditions mold" rows. (D&D Beyond's row also shows
   a chevron per condition, likely for Exhaustion's level — not built, since
   exhaustion isn't modeled as a level anywhere yet, unchanged from before
   this slice.)

**All eight open questions above were answered with the owner on 2026-08-15.** A
few smaller loose ends remain, not asked as numbered questions and not blocking
the slice order — resolve them live during the slice they belong to, or raise
them with the owner then if they turn out to matter more than expected:

- Hit points block layout (twin Heal/Damage inputs vs. our shared-input-plus-
  three-buttons) — see "Vitals zone — top row" below.
- Whether D&D Beyond's Actions tab duplicates passive class features beyond
  Features & Traits — see "Actions tab" below.
- Whether D&D Beyond's inline feature/spell text is meaningfully shorter than its
  sidebar detail — see "Tabs — general" and "Features and traits tab" below.
- The Spells tab's two icon-only filter chips' meaning — see "Spells tab" below.
- The Extras hit-points control's collapsed-by-default behavior — see "Extras
  tab" below.

## General re-verification pass (2026-08-16)

The owner asked for a fresh general check of the whole sheet against a wider
variety of D&D 5e builds than the original single-character audit above used
— three more reference characters, recorded in `systems/dnd-5e/sheet-ui.md`'s
"Verifying fidelity against D&D Beyond" section: a Hill Dwarf Cleric/Paladin
multiclass, a level-20 Vedalken Wizard, a level-17 Wood Elf Ranger, and a
Minotaur Barbarian (the one non-spellcaster in the set — deliberately picked
for that). Three real, previously-undocumented gaps found and fixed:

- **Missing column headers.** D&D Beyond shows a small muted header row above
  every tabular list — "PROF MOD SKILL BONUS" on Skills, "ATTACK RANGE HIT/DC
  DAMAGE NOTES" on Attacks (and the Spells tab's per-level lists, which share
  that row shape), "ACTIVE NAME WEIGHT QTY COST(GP) NOTES" on Inventory
  (weight skipped here, matching this app's own already-documented "no
  weight/encumbrance" deviation). This app's equivalent panels had none.
  **Correction, 2026-09-15:** this entry originally lumped Notes in with
  Weight and skipped both — wrong, Notes has nothing to do with weight
  tracking. Built: see changelog's "Inventory tab: added the missing Notes
  column."
  Built by reusing each row's own column-width classes inside a `--header`
  modifier (`.skill-row--header`, `.action-row--header`/`.spell-row--header`,
  `.item-row--header`), so header and data columns stay aligned by
  construction rather than a second hand-tuned width table.
- **Senses line was inert text, not a reveal target.** Confirmed across all
  four reference characters: the senses line at the bottom of the Senses
  panel — either a real value like "Darkvision 60 ft." or a placeholder
  "Additional Sense Types" when the character has no fixed sense — always
  opens a sidebar with the three passive scores again plus general rules
  text (what a passive check is, Blindsight/Darkvision/Truesight). This was
  a known, deferred gap ("Saving throw rows and sense rows have no reveal
  button at all yet... left for a future slice" — see `systems/dnd-5e/sheet-build.md`
  history) that the `specialSenses` feature (see below) inherited without
  reopening it. Built via the existing Entity Detail mold (`onOpenDetail`),
  reusing its `metadata`/`description` fields rather than extending the
  single-value Explainer mold — the senses panel shows several values plus
  static rules text, not one value with a contribution trace. The rules text
  is written in this project's own words, not copied from D&D Beyond's panel.
- **Spells tab shown unconditionally.** `Dnd5eTabbedSection.tsx`'s tab list
  always included "Spells", even with zero spellcasting classes. Confirmed
  live on the Minotaur Barbarian reference: D&D Beyond omits the Spells tab
  entirely for a non-caster rather than showing it empty. Fixed by making
  the tab conditional on `sheet.spellcasting.length > 0`, with a fallback to
  "actions" if the active tab is "spells" and spellcasting becomes
  unavailable (defensive; not reachable via any mutation this app has).

Two already-known, previously-confirmed deviations were reconfirmed still
accurate, not new findings: D&D Beyond splits "Background" and "Notes" into
two separate tabs where this app has one combined tab (`systems/dnd-5e/sheet-build.md`
already records this), and D&D Beyond keeps the hit dice pool only inside the
Short Rest panel where this app also keeps a persistent top-row box (the
owner's own confirmed scope call, phase 9 slice 2). The "MANAGE" header
button gap (see open question 7 above) was separately re-raised and deferred
the same session — see the changelog.

**Follow-up look at Features, Background and Extras, same session:**

- **Extras tab** was missing both name search (Inventory and Spells already
  have it) and a column header row — both built, same techniques as above
  (`SearchField`, an `.extra-row--header` reusing the row's own column
  classes). The portrait thumbnail D&D Beyond shows next to an extra's name
  is a reconfirmed, already-documented gap (no portrait storage for extras
  exists — phase 11 concern), not new.
- ~~**Features & Traits** surfaced a real gap...~~ **Built 2026-09-15.**
  Re-checked before finally building it: the backend already carried
  everything a large pool needs (`Dnd5eFeatureAction`/`Dnd5eFeatureTrait`'s
  existing `maxUses`/`usedCount`/`rechargeTrigger`) — no new resource type
  was actually required, this was a pure presentation threshold on the
  existing field, not the domain-model change originally feared. New
  `UsePoolStepper.tsx` (sibling to `BoxTrack.tsx`) renders a `−`/`+` counter
  instead of boxes once `maxUses >= 11` (D&D Beyond's own threshold, its
  `ActionDetail.tsx`'s `largePoolMinAmount`), wired at both existing call
  sites (`FeatureActionRow.tsx`, `FeatureTraitRow.tsx`) via a shared
  `LARGE_POOL_THRESHOLD` constant. Deliberately simplified vs. D&D Beyond's
  own `SlotManagerLarge`, which stages an edit locally with Confirm/Clear
  before firing one mutation — this app's `onUse`/`onRestore` already fire
  immediately per click (`BoxTrack`'s own established convention), so the
  new stepper does too rather than introducing a new "pending edit" pattern.
  Seeded a synthetic "Battle Fervor" feature trait (`maxUses: 15`, not a
  real 5e Fighter feature — see `DevCharacterSeeder`'s own doc comment) since
  none of Aria's real features cross the 11-use threshold, so the component
  has a live row to verify against. Verified live: the counter renders `9 of
  15`, `−` decrements to `8 of 15`, `+` restores back to `9 of 15`; existing
  small-pool features (Second Wind, Action Surge) still render `BoxTrack`
  unchanged.
- **Background** structure (combined characteristics panel, Personality/
  Ideals/Bonds/Flaws, Organizations/Allies/Enemies/Backstory/Other) already
  matched. D&D Beyond adds its own sub-filter chips (ALL/BACKGROUND/
  CHARACTERISTICS/APPEARANCE within the Background tab, ALL/ORGS/ALLIES/
  ENEMIES/BACKSTORY/OTHER within Notes) that this app's single combined tab
  doesn't have — downstream of the already-accepted merged-tab deviation,
  not treated as a gap on its own.

**Full-sheet DOM-measured typography pass, same session (2026-08-16):** the
owner asked for the same DevTools treatment across every remaining section —
`getComputedStyle` against D&D Beyond's live DOM rather than estimated
values, the same method already used for Skills above. The dominant finding:
this app's row/label text was consistently smaller than D&D Beyond's own
across nearly the whole sheet (labels ~8-9px where D&D Beyond runs ~10-13px,
values ~9-11px where D&D Beyond runs ~13-14px) — not a Skills-only issue.
Fixed in Initiative, Armor Class, Saving Throws, Senses, Proficiencies, the
shared panel-title caption, and every tab-content row/header (Actions,
Spells, Inventory, Extras, Background). Two items surfaced but deliberately
not built: Features & Traits' source citation (needs a new domain field, not
CSS) and Inventory's real equip-checkbox proportions (20×20px, sharp
corners, vs. this app's 16×16 rounded one). Full row-by-row measurements and
deltas are in `systems/dnd-5e/sheet-build.md`'s per-component rows and
`changelog.md`'s narrative entry for this date — not duplicated here.

## Findings by area

### Header

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Name row | Name + edit pencil, "MANAGE" button, species/class-with-levels line, level line | Matches per spec; "MANAGE"-equivalent not present | **Deferred 2026-08-16 (owner decision)** — on D&D Beyond, MANAGE opens their character-creation/level-up flow; the owner explicitly dislikes that flow's UX and doesn't want to commit to a shape for it yet. Revisit once phase 11 (character creation/editing) is designed, not before |
| Buttons | Share, Short Rest, Long Rest, Find a Group, Game Log (icon, unlabeled), a domain-flavored decorative icon (verify — may be character-specific, not universal chrome) | Only Game Log is built; Share/Edit have no feature yet, Short/Long Rest are phase 9, Find a Group excluded | Match (already tracked as pending on other phases, not a new gap) |
| Portrait/name click | Opens the Character panel | Not built | Deferred (resolved open question 7) — not this phase |

### Vitals zone — top row

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Ability boxes | Label, big modifier, circled score below | Same structure | **Re-measured 2026-08-16** (slice 12) via `getBoundingClientRect`: our ability box is exactly 81×95, D&D Beyond's own is 81×95 — pixel-identical, no drift. Proficiency/Speed badges use a fixed 74×74 owner-supplied SVG frame (native asset size, not a CSS approximation); D&D Beyond's own equivalent box measures 94×95 including its caption, not a directly comparable like-for-like shape — left as-is rather than distorting the asset to chase a differently-composed reference | Match, confirmed |
| Proficiency / Speed / Inspiration | Stat badges | Same | Match, re-measure only |
| Hit points block | HEAL (green) and DAMAGE (red) each with their own small input, stacked left; CURRENT/MAX/TEMP columns right | **Built 2026-08-16** (slice 12) — owner chose the twin-input layout over keeping the shared field. `HitPoints.tsx` now has independent Heal/Damage/Temp amount fields, Heal/Damage colored via new `--status-positive`/`--status-negative` tokens. Verified live: healing via the dedicated Heal field updated Current correctly and cleared after submit | Done |

### Left column

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Saving throws | 2-column × 3-row grid; a below-grid line for conditional bonuses (e.g. "against Poison") | **Built 2026-08-16:** matching 2-column × 3-row grid | Match (the below-grid conditional-bonus line stays deferred, same tier as condition-effects-on-rolls) |
| Conditional save bonuses (e.g. resistance-linked advantage) | Shown | Not modeled at all | Deferred, same tier as condition-effects-on-rolls already in "Deferred to later versions" — not part of this phase's slices |
| Senses | Three passive scores, plus a plain-text senses line ("Darkvision 60 ft.") | ~~Three passive scores only; no other-senses field~~ **Built 2026-08-16 (direct owner request)** — a new structured `specialSenses` field (type + range, not free text — the owner's own choice over an authored string) renders a text line per fixed sense the character has natively (species/feature-granted, not derived from ability scores), only when it has one. Seeded Aria's existing Mountain Dwarf Darkvision as the first real entry (60 ft.) | Done |
| Proficiencies & training | Armor/Weapons/Tools/Languages, gear icon | Same four categories, same gear affordance | Match |

### Center column — skills

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Skill list | Prof marker, ability, name, bonus; a red icon on skills affected by e.g. armor-stealth-disadvantage | Same core layout; no disadvantage indicator | **Deferred 2026-08-16** (slice 12) — `SkillRow.tsx` has no data source to know *which* skills are affected (armor-stealth-disadvantage isn't modeled anywhere: no armor-weight/proficiency tracking to derive it from). Adding an icon with nothing real behind it would be decoration, not the "real mechanical modeling" the audit's own note called for — deferred to the same tier as condition-effects-on-rolls, not built as a hollow icon |

### Right column

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Initiative / Armor Class | Explainer mold, contribution breakdown | Verified live — matches well | Match |
| Defenses | Icon (shield+exclamation) per entry + label | Single-letter prefix ("R") + label | **Built 2026-08-16** (slice 12) — DOM inspection of D&D Beyond's own `.ddbc-resistance-icon`/`.ddbc-immunity-icon` found the "shield+exclamation" guess was wrong: two *different* icon shapes, but both the exact same green (`#00c680`). Reproducing their SVG paths would copy another product's asset (forbidden); reproducing the confirmed exact color in our own letter-badge treatment (`DamageTypeIcon`, already established for Resistance) is not — extended to Immunity ("I", same green). Vulnerability/Condition Immunity stay plain text, still unconfirmed (this character has neither) |
| Conditions | "Add Active Conditions" prompt opens a sidebar panel with the toggle list | **Built 2026-08-16:** matching compact trigger opening the sidebar's Conditions mold | Match |

### Tabs — general

**Corrected 2026-08-15** (see open question 2): every clickable entity on D&D
Beyond — attacks, spells, items, ability scores, features and traits alike —
opens the same sidebar-click pattern. The inline text visible in a tab (e.g. a
feature's paragraph in Features & Traits) is a summary; the sidebar shows a more
detailed version on click. There is **one** disclosure pattern, not two, and our
existing sidebar-for-everything approach already matches it structurally. Still
worth checking live, during the relevant slice: whether D&D Beyond's inline
summary is meaningfully shorter than its sidebar detail (ours currently shows the
same text in both places).

### Actions tab

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Attack table | Name, range, hit/DC (roll target inline), damage (roll target inline), notes | Same core columns | Match |
| Standard actions | A flowing, comma-separated line of action names ("Actions in Combat"); clicking a name opens the sidebar with its description, nothing shown inline | **Re-corrected 2026-09-03, direct owner report**: this audit's own 2026-08-16 "Match" verdict below was itself wrong — never actually re-verified live, just re-asserted the original guess. The owner confirmed live D&D Beyond shows no inline description at all, only a compact clickable name list. Rebuilt: `ActionsTab.tsx`'s "Standard Actions" section renamed "Actions in Combat", each name now a `.reveal` target opening the Entity Detail mold with its description (`frames.css`'s `.actions-tab__standard-list`) | Fixed — see `systems/dnd-5e/sheet-build.md`'s "Attack row"/"Actions tab" rows |
| ~~Standard actions~~ (superseded above) | ~~Full description rendered inline, no click~~ | ~~Correcting this audit's original finding: already built this way~~ — **wrong, see the corrected row above** | ~~Match — no action needed~~ |
| Passive class features (e.g. Channel Divinity) shown in Actions | Rendered inline in the Actions tab itself, full description, same text also in Features & Traits | `sheet.featureActions` (action-costed, trackable features) render inline in Actions tab's own "Features" section via `FeatureActionRow`, full description; the same features also appear in Features & Traits | **Verified live 2026-08-16** against dndbeyond.com/characters/50149479: Channel Divinity and Divine Sense render with full inline description in both the Actions tab and Features & Traits tab — genuine duplication, not a summary/detail split. Our existing split already matches — Match, no rebuild needed |
| Attack's Entity Detail panel | Labeled metadata (Proficient/Attack Type/Reach/Damage/Damage Type/Weight/Cost/Properties/Source); no roll buttons, only Unequip/Move/Delete | `EntityDetailMetadataEntry[]` shape (Range/Damage Type); no roll buttons, no Unequip/Move/Delete (this app's attack model has no such mutations) | **Done 2026-08-16**: dropped the duplicate Attack/Damage roll buttons from `ActionsTab.tsx`'s attack `actionBar` — confirmed live D&D Beyond's own panel carries none either, rolling stays inline on the table row only (`AttackRow.tsx`, unchanged). Metadata shape already matched via slice 2; expanding to D&D Beyond's full field set (Proficient/Weight/Cost/Properties/Source) is out of scope for this slice — this app's attack model doesn't track those fields |

### Spells tab

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Per-class header | Compact paired values ("+4 \| +1") per stat, one row for all classes | **Built 2026-08-16** (slice 8) — `sheet.spellcasting.map(...).join(' | ')` per stat row, replacing the one-boxed-card-per-class layout | Done |
| Search | Full text: name, casting time, damage type, condition, tag *(the audit's own guess from the placeholder text, never tested live)* | Name only | **Corrected 2026-08-16**: verified live against dndbeyond.com/characters/50149479 — typing "necrotic", "bonus action", "touch" and "Radiant" against spells that genuinely have those exact values all returned zero results; only a name substring ("bolt") matched. D&D Beyond's own search is name-only in practice despite its placeholder text claiming otherwise. Our existing name-only search already matches — no fix needed, the original finding was wrong |
| Search + level filter row layout | Search (with its "Manage Spells" callout) and the level filter live on **separate rows** — `SpellsFilter`'s own row, then `Spells.tsx`'s `TabFilter` row | Both crammed into one inline-styled flex row | **Re-corrected 2026-09-16, direct owner report**: this row's 2026-08-16 "Done" verdict below was never tested against a character with enough spell levels to matter — once there are more than a handful of level chips, they wrap onto a second row and the search input (no `flex`/`min-width` of its own) collapses to near-invisible. Split into `.spells-actions-row` (search + Manage Spells) and `.spells-filter-row` (level/Concentration/Ritual chips), each its own line, matching the two separate D&D Beyond rows exactly. See `changelog.md`'s 2026-09-16 entry |
| Filter chips | Level chips plus two icon-only toggles — **identified live 2026-08-16**: a diamond (Concentration) and a book/scroll icon (Ritual), each toggling the visible list to that subset | Level chips only | ~~**Built 2026-08-16** (slice 8): two toggle buttons (`spell-toggle`)~~ — **that build never actually shipped as icon-only**: `SpellsTab.tsx`'s chips array rendered them as plain text chips ("Concentration"/"Ritual") alongside the level chips, adding to the row-width problem above. **Fixed 2026-09-16**: `FilterChips` now accepts a `ReactNode` label (D&D Beyond's own `TabFilter` takes an icon component as a filter's label the same way), and Concentration/Ritual render as icon-only chips reusing the already-built `dnd_icon_marker_concentration`/`_ritual` artwork (`SpellRow.tsx`'s own markers) |
| Level chip labels vs. section heading | Level tabs use a short abbreviation ("1ST"); the level's own section heading above its spell list uses the full name ("1st Level") — two different strings, confirmed live and in `Spells.tsx`'s source (`renderSpellLevelAbbreviation` vs `renderSpellLevelName`) | Both used the same full-name string (`levelLabel`) | **Fixed 2026-09-16**: new `levelAbbreviation()` (`SpellCast.tsx`) for the chip label only; `levelLabel()` stays as the section heading's full name, unchanged |
| Casting header spacing | `.ct-spells-level-casting__info-group` blocks space apart with a **15px margin**, not a flex gap | `.spellcasting-header { gap: 24px }` | **Fixed 2026-09-16**, measured against the reference markup's compiled CSS |
| "Manage Spells" placement | Sits inside the search row (`SpellsFilter`'s own `callout` prop), never overlapping the casting-info header | `position: absolute` over `.spellcasting-header`, no ancestor `position: relative` reserved for it on purpose | **Fixed 2026-09-16**: moved into `.spells-actions-row` as a normal flex sibling of the search field |
| Table columns | Name, Time, Range, Hit/DC (inline roll target), Effect, Notes | **Built 2026-08-16** (slice 8) — not purely presentational as first assumed: `Dnd5eSpell` gained `castingTime`/`range`/`notes`/`effectSummary` fields (none existed before), authored for all 3 seeded spells and all 11 `content/dnd-5e/spells/*.json` catalogue entries. `SpellRow.tsx` rebuilt as Name(+C/R markers)/Time/Range/Hit-DC/Effect/Notes. Hit/DC and Effect are derived, not stored — see `SpellRow.tsx`'s doc comment for the attack-roll/save-DC/no-roll split this app's current spell set supports | Done |
| Slot tracks | Shown per level | Deferred to phase 9 (already documented) | Match (correctly deferred) |

### Inventory tab

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Containers, weight/encumbrance | Present | Deliberately absent | Match — confirms existing deviations are accurate |
| Coins | Compact icon+number chips in the tab header | Separate coins panel | **Built 2026-08-16** (slice 12) — confirmed live D&D Beyond's own coin display is a single `role="button"` opening a management panel, not inline chips with their own controls. New `CoinChips.tsx` shows only nonzero denominations (colored dots, conventional metal tones — not copied artwork) in the Items header; clicking opens the existing `CoinsPanel` as the sidebar's Entity Detail `actionBar` (live-refreshed, same treatment as Extras' hit points), replacing the standalone "Coins" section. Verified live: chip showed "45", clicking it opened the panel, adding 10 gold updated both the panel and the chip behind it to 55 |
| Active/equipped marker | Icon in an "ACTIVE" column | ~~Letter-flag buttons (E/A)~~ **Built 2026-08-16 (direct owner request)** — `.item-row__flag` now renders as a real checkbox (empty box, checkmark + filled ink background when equipped), matching D&D Beyond's own equip control; the "A" (attunement) flag was already dropped in the phase 10 attunement rework (see that row above) | Done |
| Search | Full text: name, type, rarity, tag | Name only (`sheet/SearchField.tsx`, same primitive the Spells tab uses) | **Already built** when phase 10 slice 5 came up — this finding was stale by the time this slice was reached (a name-only search was added closing out phase 8's own "every list supports search and filtering" line, before this audit's slice order got here). Full-text search across type/rarity/tag would need those fields tracked on `Item` first (only `name`/`quantity`/`cost`/`notes` exist) — not attempted, since D&D Beyond's own richer item model isn't something this app tracks yet either |

### Features and traits tab

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Filter chips | All/Class Features/Species Traits/Feats | Same | Match |
| Grouping | By source, summary inline, full detail behind a sidebar click | By source, `summary` inline and `description` (full text) in the sidebar | **Built 2026-08-16** (slice 7). Verified live against dndbeyond.com/characters/50149479's Spellcasting entry: the inline text is a genuine short paraphrase, the sidebar shows the full multi-paragraph rules text — not the same string twice. `Dnd5eFeatureTrait`/`FeatureTrait`/`FeatureTraitResponse` gained a `summary` field alongside the existing `description`; `FeatureTraitRow.tsx` now renders `summary` inline, the Entity Detail panel keeps `description`. Seed data (`DevCharacterSeeder`) authored both for all 7 seeded features |
| Source citation | Book + page ("PHB, pg. 57") | Not tracked | Match — already a documented omission (no source/book field exists). **DOM-measured, 2026-08-16:** `.ct-feature-snippet__meta` is 13px/700, plain black, **not italic** — noted for whenever this gets built, see `systems/dnd-5e/sheet-build.md`'s Features and Traits tab row |

### Background and notes · slice 1, done 2026-08-15

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Background feature | Name + description | Same | Match |
| Characteristics | 10 fields: Alignment, Gender, Eyes, Size, Height, Faith, Hair, Skin, Age, Weight | Alignment only, among these | **Built.** Added the nine missing fields, plus an 11th (Lifestyle) found live during the slice — not in this audit's original list |
| Personality/Ideals/Bonds/Flaws | Free text, each opening its own panel with a d6–d8 roll-a-suggestion table from the character's background | Same fields, but display-only | **Built**, with a real correction to this audit's original guess (see below) — suggestions sourced from a seeded catalogue entry (`content/dnd-5e/backgrounds/soldier.json`, SRD 5.1) |
| Appearance | Own sub-section within Background | Own field within `Dnd5eBackground` | Match |
| Notes fields | Organizations/Allies/Enemies/Backstory/Other | Identical five fields | Match — confirms our field set is right |
| Editing | **Corrected 2026-08-15**, live-verified in detail before building (this audit's original "+ Add X" guess undersold it): three sub-patterns, not one. (1) Alignment plus the other ten characteristics share **one combined** "Characteristics and Details" panel (dropdowns and text inputs together) — not per-field panels as originally assumed. (2) Personality Traits/Ideals/Bonds/Flaws each open individually with a textarea **plus a roll-a-suggestion table** (Random button, per-row "+ Add"). (3) Appearance and every Notes field each open individually with just a textarea and a helper prompt — this one matches the original guess exactly | **Built**, all three patterns, confirmed with the owner to build faithfully rather than the originally-planned single-field-only scope. New seventh mold, "Text Field" (`ui-design-system.md`) — one shape (an array of fields) covers all three sub-patterns. See `systems/dnd-5e/sheet-build.md`'s "Background and notes tab" row for the full implementation note |

### Extras tab

| What | D&D Beyond | Ours | Status |
| --- | --- | --- | --- |
| Row | Portrait thumbnail, name, creature-type subtitle, AC, HP, speed, notes | **Built 2026-08-16** (slice 10) — name, subtitle (size + creature type, e.g. "Large Beast"), AC, HP, speed. Portrait stays out: this app has no portrait storage/upload for extras (a phase 11 concern), stated as a deliberate omission rather than approximated | Done except portrait (deferred) |
| Detail panel | Fully structured stat block: size/type/alignment, AC/Initiative/HP/Speed, ability scores with saves, skills, senses, languages, CR, Traits and Actions as individual entries | **Built 2026-08-16** (slice 10, resolved open question 6) — `Dnd5eExtraStatBlock` (size/creatureType/alignment/hitDiceLabel/additionalSpeeds/abilityScores/skills/senses/languages/challengeRating/traits/actions) replaces the free-text `statBlock` field; `EntityDetailRequest` gained an optional `body` escape hatch (same pattern as `actionBar`) so `ExtraStatBlockView.tsx` can render the real layout. Verified live against D&D Beyond's own Cat familiar panel before building | Done |
| Hit points control | Collapsed section header by default | **Confirmed live 2026-08-16**: yes, collapsed until clicked. Built as a `<details>` wrapping the existing `HitPoints` action bar | Done |

### Sidebar molds — cross-cutting

| Mold | Finding |
| --- | --- |
| Explainer | Verified live against Armor Class — matches well, including the "Customize" collapsible being correctly excluded (documented deviation) |
| Entity Detail | Exists and matches in spirit; metadata shape (resolved, open question 4) and attack action-bar content (resolved, open question 3) both have a decided fix pending |
| Collection Editor | Not re-verified this pass beyond the Proficiencies panel's gear icon (present on both sides) |
| Mechanic | Correctly deferred to phase 9, nothing to compare yet |
| Log | Not re-verified this pass; D&D Beyond's Game Log lives in the Character panel menu, consistent with our header button |
| **Text Field** (proposed new, sixth mold) | See "Background and notes" above |
| **Character panel** (defined in `ui-design-system.md`'s trigger table, never built) | Deferred (resolved open question 7) — not in this phase's slice order |

## Suggested slice order (once the open questions are answered)

Mirrors phase 8's own discipline — one slice at a time, each ending reviewable,
confirmed with the owner before the next starts. This order assumes all "Fix"
items proceed and groups the "Needs owner decision" items by how much they block
other work:

1. **Text Field mold — done 2026-08-15.** Turned out larger than "small,
   self-contained": live verification found three sub-patterns sharing one
   mold, not one, and the owner chose to build all three faithfully rather
   than start with a reduced scope. See "Background and notes" below.
2. **Entity Detail metadata shape — done 2026-08-16.** Switched
   `EntityDetailRequest.metadata` to `EntityDetailMetadataEntry[]`
   (`{label, value}`), plus a new optional `tags?: string[]` for flag-like
   badges (Concentration/Ritual) folded in as planned. All five consumers
   updated together and verified live: `ActionsTab.tsx` (attack, feature
   action), `SpellsTab.tsx`, `ItemRow.tsx`, `FeaturesTab.tsx`, `ExtraRow.tsx`.
   See `systems/dnd-5e/sheet-build.md`'s "Entity detail mold" row.
3. **Saving throws layout — done 2026-08-16** (open question 1) — a vitals-zone
   change, independent of the tabs.
4. **Conditions — done 2026-08-16** (move the checklist into a sidebar panel,
   open question 8).
5. ~~**Inventory search**~~ — already built (name-only, `sheet/SearchField.tsx`)
   by the time this slice was reached; the gap this closed was a phase-8 one,
   done before this audit's own slice order got here. Confirmed live
   2026-08-16, no new work needed.
6. **Actions tab — done 2026-08-16.** Dropped the duplicate Attack/Damage roll
   buttons from the attack's Entity Detail action bar (resolved, open question
   3). Verified live that D&D Beyond's Actions tab genuinely duplicates
   action-costed class features (Channel Divinity, Divine Sense) with full
   inline description, matching this app's existing `featureActions` split —
   no rebuild needed.
7. **Features & Traits tab — done 2026-08-16.** Verifying live showed D&D Beyond
   genuinely does split inline summary from sidebar detail (open question 2's
   structural match still holds; this is the separate "is the inline text
   shorter" question, and the answer was yes). Built the split: new `summary`
   field alongside `description` on `Dnd5eFeatureTrait` and its response
   shapes, `FeatureTraitRow.tsx` switched to render `summary`, seed data
   authored for all 7 features.
8. **Spells tab — done 2026-08-16.** Turned out larger than "purely a fix slice":
   live verification found the search-breadth finding was wrong (D&D Beyond's own
   search is name-only, matching ours already — no fix needed) and the table
   columns needed four new `Dnd5eSpell` fields (`castingTime`/`range`/`notes`/
   `effectSummary`), not just CSS. Header format and the two icon toggles
   (Concentration/Ritual, identified live) were the presentational parts that
   matched the original estimate.
9. ~~**Background characteristics**~~ — already built. This was resolved as
   part of slice 1's "Background and notes" work on 2026-08-15 (`Dnd5eBackground`
   already carries all nine fields plus Lifestyle), before this audit's own
   slice order reached it — the same situation as slice 5's Inventory search.
   Confirmed live 2026-08-16: `BackgroundTab.tsx`'s Characteristics section
   shows Alignment/Gender/Eyes/Size/Height/Faith/Hair/Skin/Age/Weight. No new
   work needed.
10. **Extras stat block — done 2026-08-16** (resolved open question 6). Verified
    live against D&D Beyond's own Cat familiar panel first, then proposed the
    record shape to the owner before building: `Dnd5eExtraStatBlock` plus
    `Dnd5eExtraAbilityScore`/`Dnd5eExtraSkill`/`Dnd5eExtraStatEntry`, threaded
    through the generic `ruleset` types, response DTOs and a new
    `ExtraStatBlockView.tsx`. Also resolved the hit-points-collapse question
    (yes, collapsed by default) in the same slice, since it's the same panel.
11. ~~Character panel~~ — deferred (resolved open question 7), dropped from this
    phase's slice order entirely.
12. **Presentational cleanup pass — done 2026-08-16.** Defenses icons (built —
    Immunity got Resistance's green badge treatment, exact color confirmed live
    via DOM inspection, no asset copied), coin-chip header (built — chips open
    the existing CoinsPanel as a live-refreshed sidebar actionBar), hit-points
    block layout (owner chose the twin-input layout — built), skill disadvantage
    indicator (deferred — no underlying data, would be a hollow icon), ability-
    box/badge remeasurement (re-measured, no drift — already pixel-exact).
    (Extras row's type line is done, slice 10 — its portrait thumbnail stays out
    permanently, no portrait storage for extras exists.)

Re-propose this order to the owner at the start of phase 10 — new findings during
slice 1 or 2 may reorder what follows, the same way phase 8's own build order
shifted slice to slice.

## Tracked, waiting on the owner

- ~~**Advantage/disadvantage rolling.**~~ **Built 2026-09-14, direct owner
  request** — see `changelog.md`'s entry for that date: `RollMode` in the `dice`
  package (2 dice kept high/low instead of 1, eligible only for a single d20
  roll), plus `RollModeMenu.tsx`'s right-click popover DOM-extracted live from
  dndbeyond.com/characters/50149479, wired onto all six d20 roll targets.
- **Advanced item/spell filtering by type, rarity or tag** — found live
  2026-09-10 (see `systems/dnd-5e/sheet-ui.md`'s "Deferred to later versions" for the
  full detail — D&D Beyond's funnel icon needs `type`/`rarity`/`tag` fields on
  `Item`/`Dnd5eSpell` this app doesn't track yet). Still open: decide whether it
  becomes its own phase/slice, or stays deferred.
- ~~**`CloseSvg`/`HealingSvg`.**~~ **Resolved 2026-09-10, direct owner report** —
  the owner named both live locations this pass: Healing is D&D Beyond's Spells
  tab heart glyph, shown in the exact same Effect-column slot the damage-type
  icons already occupy but only for a healing spell (DOM-confirmed against
  Cure Wounds on dndbeyond.com/characters/50149479, `ddbc-healing-icon__icon`);
  Close is the "Roll Dice" popover's own close button. Healing was genuinely
  missing (`dnd_icon_healing.svg` added, wired in `SpellRow.tsx`/
  `SpellAttackRow.tsx` — see `systems/dnd-5e/sheet-build.md`'s Damage-type icons row).
  Close turned out to already be correct — this app's `dnd_icon_close.svg`
  (`dice/CustomRollPicker.tsx`) DOM-compared byte-identical to D&D Beyond's own
  popover close button (both are FontAwesome's stock "times" glyph); the earlier
  "different, unrelated FontAwesome asset" note above was itself the mistake.

- ~~**Per-section frame assets.**~~ **Delivered and wired 2026-08-16** — the
  owner provided an individually-sized SVG frame for every section (the phase
  10 asset refactor, see `changelog.md`), exactly as anticipated when this
  item was first raised ("vou dar um jeito de disponibilizar no futuro cada
  frame para cada seção individualmente"). This is what made the saving
  throws 2-column grid (open question 1) finally viable without stretching a
  shared asset.

- ~~**Entity Detail sidebar header (icon + granting-class line) and the
  missing cast-block divider.**~~ **Built 2026-09-17** — see `changelog.md`'s
  "Entity Detail sidebar header: school icon and granting-class line" entry.
  `EntityDetailRequest` gained `parent`/`icon`, wired for spells from
  `Spell.className` and the existing `SCHOOL_ICONS`; the missing
  `.entity-detail__actions` divider above the cast block was added in the
  same pass. Verified live against Guiding Bolt on
  dndbeyond.com/characters/50149479.
  **Still open**: D&D Beyond's spell pane also has a "Customize" collapsible
  (override to-hit/damage/DC, rename, add notes) — this app has no
  customization system for spell/action overrides at all yet, a bigger
  feature than styling; deliberately not built as part of this fix.

## 2026-09-17 — owner punch list (planned, not yet started)

Seven findings from the owner's own live use, not yet investigated in depth —
this is a plan written to survive a context/credit reset, not a finished
audit entry. Investigate each properly (live DOM comparison where the row
below says so) before building; do not build straight from this summary.

1. ~~**List rows only open the sidebar from the name cell, not the whole
   row.**~~ **Built 2026-09-19** — see `changelog.md`'s "Shared `ListRow`
   primitive" entry. All seven now open Entity Detail from anywhere in the
   row via `sheet/ListRow.tsx`, with `stopPropagation` on every real
   interactive child. Verified live against Aria Emberfall on all four tabs.
2. ~~**Extract a shared, reusable list-row primitive.**~~ **Built
   2026-09-19**, same entry — surveying the seven components first (as this
   item asked) found the divider genuinely differs three ways, so
   `ListRow.tsx` deliberately covers only the click target and the
   stopPropagation convention, not divider CSS. **Still open**: the three
   divider treatments themselves stay unmerged — a real visual decision
   across three tabs, not something this fix's scope covered.
3. ~~**`ProficienciesPanel`'s `<dl>/<dt>/<dd>` should be plain `<div>`s.**~~
   **Built 2026-09-19** — see `changelog.md`'s own entry. `DefensesConditionsPanel.tsx`
   shared the identical `panel__list` markup/CSS and got the same latent bug,
   fixed together rather than leaving the shared class half-migrated.
4. ~~**Uppercase label typography is inconsistent with D&D Beyond across the
   sheet.**~~ **Built 2026-09-19** — see `changelog.md`'s own entry.
   `.ability__label`, `.badge__heading`, `.panel__title` (all panel headings,
   incl. "Defenses"/"Conditions"), and `.panel__list-label` (Proficiencies'
   category labels) each individually verified against the local CSS mirror
   and fixed: `font-family: var(--font-condensed)` added, guessed
   `letter-spacing` removed. **Still open**: the item's own "audit uppercase
   text handling generally" — `frames.css` has 40 total `text-transform:
   uppercase` rules; only the four named categories above were verified and
   fixed. The other ~36 (Skills/Spells/Inventory row headers, Background tab
   labels, Extra stat block headings, the sheet header user badge, etc.) are
   unaudited — each needs its own reference-value lookup before touching it,
   same discipline as this pass, not a blind sitewide find/replace.
5. ~~**Hit Points section font/size should match D&D Beyond exactly.**~~
   **Built 2026-09-19**, same entry. The plan's own assumption that the local
   CSS mirror would have `.ct-quick-info__health` was wrong — that mirror is
   built assets only, no captured DOM, and the class doesn't appear anywhere
   in it. Verified live instead, via `getComputedStyle` against
   dndbeyond.com/characters/50149479. Fixed `.hit-points__title`,
   `__stat-label`, `__stat-value`, `__stat-slash`, `__action` (the top-row
   inline box only, `HitPoints.tsx`) — `HpManagementPanel.tsx`'s own
   same-prefixed sidebar classes are a different mold and weren't touched,
   still open if the same typography bug applies there too.
6. ~~**Resistance/immunity icons need correcting.**~~ **Built 2026-09-19,
   corrected same day** — see `changelog.md`'s own entry. First pass built an
   original CSS shield shape; corrected to use D&D Beyond's own
   `ImmunitySvg`/`ResistanceSvg`/`VulnerabilitySvg` verbatim instead (from
   `design-reference/markup/svg-index.html`), a deliberate exception to this
   codebase's usual "no traced assets" stance. Vulnerability now gets the
   same icon treatment too (was plain text before). **Still open**:
   Vulnerability's `--status-negative` color is a placeholder, not confirmed
   live — this app's reference character has neither. Condition Immunity
   stays plain text (a different concept, not a damage type) and its own
   color/badge treatment is still unconfirmed.
7. ~~**"Manage Custom" actions — a real new feature, not a fidelity fix.**~~ **Built
   2026-09-20** — see `changelog.md`'s "Manage Custom Actions" entry. All three
   templates (General/Spell/Weapon), the full confirmed field set, and the
   `displayAsAttack` fold into the Attack table are implemented and verified live
   against Aria Emberfall (add General/Spell actions, confirm to-hit/damage math
   for a `displayAsAttack` one, confirm removal). Closes this punch list and
   Phase 10.
   **Still open, named rather than silently dropped**: the AoE Type dropdown's
   real option list and the Fixed Value/to-hit relationship were confirmed live
   during this build (see `Dnd5eCustomAction`'s own doc comment) — no longer
   open. What stayed unconfirmed and out of scope for this slice: whether
   "Snippet" renders anywhere else on the sheet (confirmed live it does not
   appear in the read-only sidebar summary; the Attack table's Notes column
   was left blank rather than guessing it belongs there), and the exact
   relationship between the core "Range" toggle and the Spell template's own
   "Spell Range Type" field beyond both being stored.

   The original item text, kept for its own investigation record:

   The
   Actions tab's "MANAGE CUSTOM" button already exists per-section
   (`ActionsTab.tsx`'s `openManageCustom`) but has no real panel behind it
   yet — it just opens a static "not supported yet" message.
   **Part (a) investigated live 2026-09-19** against Helga Flinthand
   (dndbeyond.com/characters/50149479, real, pre-existing custom actions —
   no test entries needed beyond one accidental create, immediately removed,
   see below). Findings, materially different from this item's own original
   guess:
   - **Three templates, not one shape.** "Manage Custom Actions" → "ADD NEW
     ACTIONS" offers **General**, **Spell**, **Weapon** — each almost
     certainly a different field subset. Only the Spell template was
     inspected in full (below); General and Weapon were *not* opened, to
     avoid more accidental creates (selecting a template creates the entry
     immediately, no confirmation step — a real, persisted write to the
     account this app authenticates against. One accidental "Custom Action
     3" was created this way and removed right after via its own "REMOVE
     ACTION" button; no other state was changed).
   - **The Spell template's full field set**, from editing the character's
     own real "Custom Action 2" (Ranged, CHA, 1d8+1 Force, +1 to hit, 180 ft,
     Activation Type "Action", Display as Attack ✓ — it shows in the Attack
     table exactly like a weapon row): a Melee/Ranged toggle, Stat (ability),
     Dice Count, Die Type, Fixed Value, Damage Type, Save Type (ability, for
     a save-based effect instead of an attack roll), Fixed Save DC, a
     *second* Range Type field ("Spell Range Type" — Ranged/Melee/Self,
     purpose relative to the first not yet confirmed), Range (feet), AoE
     Type + AoE Size, Activation Type, Activation Time, "Affected by Martial
     Arts" (checkbox), "Proficient" (checkbox — adds proficiency bonus),
     "Display as Attack" (checkbox), Name, Snippet, Description. Almost
     every field is optional: the character's other real entry, "Custom
     Action 1" (also a Spell-template action, never renamed from its
     default), has only To Hit/Damage/Damage Type/Stat set — no range, no
     casting time, no save — and its own read-only summary just omits every
     blank field rather than showing it empty.
   - **Activation Type's real option list**: `--`, Action, No Action, Bonus
     Action, Reaction, Minute, Hour, Special — four more values than this
     app's own four `Dnd5eActionType` buckets. Minute/Hour/Special/No Action
     have no obvious home in this app's Action/Bonus Action/Reaction/Other
     split; likely all fold into `OTHER`, needs a decision, not a guess.
   - **No per-section list.** This item's own premise (a custom action
     belongs to one of the four sections, `Dnd5eActionType`-shaped) doesn't
     match what was found: "Manage Custom Actions" is **one flat list**
     across the whole character, grouped in that panel by *template*
     (General/Spell/Weapon headings), not by section. Which of this app's
     four Actions-tab buckets an entry displays under is presumably derived
     from its own **Activation Type**, not a separately chosen field —
     opening "Manage Custom" from the Bonus Actions tab specifically still
     showed Custom Action 2, whose own Activation Type is "Action". The
     per-section "MANAGE CUSTOM" button this app already has may need to
     become one global entry point instead, not four scoped ones.
   - **All three templates now mapped** (General and Weapon inspected the
     same way: selected from "ADD NEW ACTIONS", which creates the entry
     immediately with blank fields — opened its Edit form, read every label,
     removed it right after via its own "REMOVE ACTION"). They share one
     core field set; Spell and Weapon each add a few of their own on top:
     - **Core (all three)**: Range (Melee/Ranged), Stat, Dice Count, Die
       Type, Fixed Value, Damage Type, Save Type, Fixed Save DC, Range (feet,
       a second, numeric field — distinct from the Melee/Ranged one above),
       AoE Type, AoE Size, Activation Type, Activation Time, "Affected by
       Martial Arts", Proficient, Display as Attack, Name, Snippet,
       Description.
     - **Spell adds**: "Spell Range Type" (Ranged/Melee/Self) — its relation
       to the core "Range" dropdown above is still unclear (possibly
       redundant, possibly used differently downstream).
     - **Weapon adds**: "Attack Type" (`--`/Natural/Unarmed Strike), "Long
       Range" (a ranged weapon's normal/long range pair, standard 5e stat),
       "Dual Wield", "Silvered".
     - General adds nothing — it's exactly the core set.
   - **Owner decision, 2026-09-19**: build D&D Beyond's full shape, all three
     templates, not a trimmed v1 — including AoE and Martial Arts scaling
     even though this app models neither anywhere else yet. This is a
     deliberate exception to `ground-rules.md`'s "no speculative generality"
     for this one feature, not a precedent for others.
   - **Still not investigated**: the AoE Type dropdown's own option list
     (Cone/Sphere/Line/Cube presumably, not confirmed); whether "Snippet" is
     shown anywhere on the sheet itself outside this edit form; whether a
     Spell-template custom action also surfaces on the Spells tab (its own
     grouping under "SPELLS" in the management panel suggests it might, not
     confirmed); the "Range" vs. "Spell Range Type" relationship above.
     Small enough gaps to close during (b)/(c) below rather than blocking
     on them now.
   - (b) A backend model — likely one new flat list on `Dnd5eSheet`
     (`Dnd5eCustomAction`, player-authored, distinct from `featureActions`,
     which are catalog/class-granted; one template, its own fields nullable
     rather than three separate record types, since General/Spell/Weapon
     differ only in which extra fields are populated, not in shape),
     `SheetMutator.addCustomAction`/`removeCustomAction` following the exact
     precedent `addItem`/`removeItem` already set (free-form fields, a
     generated key, no migration — JSONB). Every optional field above is a
     nullable record component, not a sentinel value. Activation Type needs
     its own enum, not a reuse of `Dnd5eActionType` — it has four more
     values (No Action/Minute/Hour/Special) with no clean fold into this
     app's four Actions-tab buckets; needs a decision on that mapping before
     writing the enum, not a guess.
   - (c) Wire the existing "Manage Custom" trigger(s) to a real panel — the
     Collection editor mold's single-free-text-field shape
     (`CollectionEditorPanel.tsx`) confirmed not to fit (multiple fields,
     several of them dropdowns/checkboxes) — needs a new sidebar mold, or
     an extension of an existing one, decided once (a)'s open decision is
     resolved.
   This is the largest of the seven items — still its own slice, proposed
   and confirmed before writing code, same as every other real feature
   addition in this project. Not started beyond investigation.

**Suggested order**: ~~2 → 1~~ → ~~3~~ → ~~4~~ → ~~5~~ → ~~6~~ (all done
2026-09-19) → ~~7~~ (built 2026-09-20). All seven punch-list items are closed —
Phase 10 is done; Phase 11 (character creation) is unblocked.

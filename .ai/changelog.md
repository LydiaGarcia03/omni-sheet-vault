# Changelog

Newest entries at the top. One entry per completed change.

Format: `## YYYY-MM-DD — short title`, then what changed, why, and anything
deliberately left out.

---

## 2026-10-04 — Paper backdrop for the D&D sheet and builder

- The D&D sheet and builder pages draw a textured off-white backdrop
  (`.parchment-page` in `frames.css`): the owner's `old-paper.jpg`
  (`systems/dnd5e/backgrounds/`) blended by luminosity over `#F6F5F2`, so
  only its texture shows, under a fine grain tile and a light white veil;
  fixed while scrolling.
- Per system, not global: `SheetShell` and `BuilderDefinition` gained an
  optional `pageClassName`; D&D passes `parchment-page`, other systems get
  nothing.
- Sections stay white: builder sections (`.builder-frame`) get a white
  padding box; sheet frames get a white rectangle inset 6px from their edge,
  and an ellipse inside the shaped ones (initiative, armor class, heroic
  inspiration). The large panels (saving throws, senses, proficiencies,
  skills, defenses, tabbed section, hit points) get two crossed rectangles,
  leaving each corner's 22px square to the frame.
- The white chamfered patch `.frame-box::before` (behind the header overlap)
  now applies only to the top-row boxes that overlap the header (abilities,
  proficiency/speed badges, inspiration, hit points). On every frame it showed
  as a white bar above the armor class and white corners outside saving
  throws and senses once the page wasn't white (spotted by the owner). The page is a stacking context (`isolation: isolate`) so the
  dark character header band still shows.
- Web 245 tests green.
- **To check before a public push:** where `old-paper.jpg` came from and its
  licence (it was saved from the web).

## 2026-10-04 — D&D header logo in brand red; VtM XP answer (Q5)

- `Dnd5eSystemMark`: the D&D ampersand logo before the name, made from the
  owner's `dnd-logo.png` (white background turned into alpha, trimmed,
  128 px) at `systems/dnd5e/brand/dnd-ampersand.png`. It is drawn as a CSS
  mask, so the logo and the name both take `--dnd5e-brand-red` (#EC2127,
  the logo's own red). Name down to 15 px (the app name is 17 px); the
  edition is centred on the name. No SVG: tracing it needs a tool the
  project doesn't have; the mask PNG scales fine at header size.
- **To check before a public push:** the ampersand is Wizards of the Coast's
  trademark logo; its use in a fan app must be checked against the Fan
  Content Policy, like `old-paper.jpg`'s licence.
- Web 249 tests green, `tsc` clean, `vite build` bundles the PNG.
- VtM: Q5 answered (sidebar spending + XP panel with history and undo);
  recorded in the implementation plan §6 and mocked in `sheet-v2.html`. The
  sheet's "−" is gone; undo lives in the XP history.

## 2026-10-04 — D&D header mark without icons; VtM mock dice pool

- `Dnd5eSystemMark`: the d20 and the red serif ampersand are gone. The name
  uses `'Modesto Bold Condensed', 'Modesto Condensed', 'Modesto'`, then the
  app's heading font. Modesto is commercial and no font file was added, so it
  only shows where it is installed.
- `sheet-v2` mock: the dice pool opens at the top left, under the app header,
  instead of above the dice button.

## 2026-10-04 — Game system name in the app header

- `AppHeader` gained an optional `systemId`; the builder, the sheet
  (`SheetShell`) and D&D's level-up page pass it. The header shows the
  system's mark after the app's name, behind a thin divider (`.app-system`
  in `theme.css`).
- Per-system styling through a registry, `shell/systemMarks.ts`, with no
  branch on the system id. D&D 5e's mark is `systems/dnd5e/Dnd5eSystemMark.tsx`
  (+ `systemMark.css`): a red d20 outline, "Dungeons & Dragons" with a red
  serif ampersand, "5th edition · 2014" muted.
- Narrow screens: the edition hides below 1280 px, the mark below 640 px.
- `AppHeader.test.tsx`: two new tests. Web 249 tests green, `tsc` clean.
- Not checked live; the D&D Beyond comparison rule covers the sheet itself,
  not this app bar.

## 2026-10-04 — Vampire 5e mockups v2: owner's follow-up

- `sheet-v2.html`: Identity left the left column; it is now the first
  sub-section of the tab renamed "Lore & Identity" (Identity, Backstory,
  Coterie), laid out as the paper sheet's three-column fields.
- Top bar of `sheet-v2` and the builder v2 pages now copies the app's compact
  `AppHeader` structure (logo and name, the builder's Draft and save state,
  "← Your characters" on the page column, the user pill), in black, plus the
  game system after the app name: a divider, a red diamond, "Vampire: The
  Masquerade" in the sheet's red display type and "5th edition" muted. The
  mock-only Reset and A/B/C links moved to the mock note.
- Builder: the owner picked variant C (aside). Recorded in
  `systems/vtm-v5/features/implementation-plan.md` §5a.

## 2026-10-04 — Vampire 5e mockups v2

- From the owner's picks (recorded in
  `systems/vtm-v5/features/implementation-plan.md` §5a): `sheet-v2.html`
  (sheet 2 + identity block + sidebar on every trait, tracker and field) and
  three builder variants `builder-v2-a-split`, `-b-inline`, `-c-aside` that
  differ only in where the explanation renders.
- Shared data in `vtm-data.js` (short descriptions in our own words, mechanics
  from `character-rules.md`); the builder engine is `builder-v2.js`, the
  variants share one browser-stored draft.
- Scripts syntax-checked and smoke-run in Node; not checked in a browser (the
  owner reviews them by hand). No app code changed.

## 2026-10-04 — Vampire 5e mockups

- Measured Demiplane's VtM sheet and builder (read only; no trackers, Rouse
  Check or builder selections touched): colors, fonts, row and tracker
  styles, the three-column builder.
- Eight static mockups in `design-reference/mockups/vtm-v5/` (git-ignored,
  local): four sheet structures and four creation structures, listed in
  `systems/vtm-v5/features/implementation-plan.md` §5a. Our own drawings and
  wording; no book or Demiplane text.
- No app code changed.

## 2026-10-04 — Desktop edition 0.2.0

- `apps/desktop/build.gradle`: `appVersion` 0.2.0; `packageDesktop` produced
  `OmniSheetVault-0.2.0-windows.zip` (README updated).
- **Update test** over a copy of the owner's 0.1.0 data (the real
  `%LOCALAPPDATA%\OmniSheetVault` untouched): V8 and V9 migrated, the 0.1.0
  character opens, the home dark theme toggles, Manage XP and Level up work.
  Stopped there at the owner's request.

## 2026-10-04 — Sidebar slice 10: docs

- `ui-design-system.md`: "The molds" rewritten to the panes as built (System
  pane, Entity detail, Management, Text field, Mechanic, Log, Conditions,
  HP Management, Character panel); the Explainer and Collection editor are
  gone.
- `systems/dnd-5e/sheet-ui.md`: the trigger table matches the sheet; the
  deviations list gained the two-panel characteristics decision, the
  pane omissions from slices 9d–9g and the removed "Spend Hit Dice"; "Value
  overrides" left the deferred list (built).
- The sidebar fidelity plan is complete (slices 1–10).

## 2026-10-04 — Sidebar slice 9g: text fields, Manage XP, Change Appearance

- **`TextFieldPanel` rebuilt:** a single field is a growing textarea with
  D&D Beyond's suggestions table (die column, striped rows, RANDOM / "+ ADD";
  a pick is now saved at once); several fields are labeled inputs.
  `TextFieldEntry` gained `options` (a select) and `suggestionDie`.
- **Background tab:** Alignment and Lifestyle are selects. Characteristics
  (Alignment, Faith, Lifestyle) and Appearance Details (the physical fields)
  stay separate panels — owner decision, a recorded deviation.
- **`ManageExperiencePanel`:** standard header, D&D Beyond's rules, field sizes
  and tab colors.
- **`ChangeAppearancePanel`:** Current Decorations (portrait and theme), then
  Browse Decorations with a collapsed Themes section.
  `ChangeAppearanceRequest.portrait` added.
- **Checked live** against 171527207 and Seyrie (48929054, read only).
- **Tests:** new `TextFieldPanel.test.tsx` (2); `ChangeAppearancePanel.test.tsx`
  opens Themes first. Web 245 green.

## 2026-10-04 — Skills box scrolls at the grid

- With custom skills added, the whole Skills box content scrolled (column
  header, rows, "Additional Skills" and the title together). Now only
  `.panel__rows` scrolls; the header, "Additional Skills" and the SKILLS
  title stay fixed (`frames.css`, `.skills-panel__content`).
- The proficiency marker in a skill pane's heading had no size, so the empty
  "Not Proficient" circle drew at its SVG's natural size. `.skill-pane__prof`
  is now 10×10, like the row's marker.
- Checked: a skill's proficiency (Customize → Proficiency Level) and a custom
  skill's Remove are reachable from the All Skills pane, as on D&D Beyond
  (171527207 compared).

## 2026-10-03 — Sidebar slice 9f: Manage Custom, Inventory, Spells, Feats

- **API:**
  - new `PUT /api/characters/{id}/custom-actions/{actionKey}`
    (`SheetMutator.updateCustomAction`, same body as the add);
  - a custom action's `activationType` may be null (a new action, D&D
    Beyond's "--"): the calculator neither buckets nor folds it into
    attacks.
- **Web:**
  - `ManageCustomActionsPanel` rebuilt: groups, REMOVE ACTION, a select that
    creates "Custom Action N"; new `customActionDetail.tsx` (the action's pane
    with an "Edit" field grid). `EntityCustomizeEditor` gained `select` and
    `textarea` fields and an optional reset; `customize.label` names the
    section.
  - `ManageInventoryPanel` rebuilt (Add Items with filter and type chips,
    Add Custom Item, My Inventory per location with weight, equip box and
    MOVE); `SidebarCollapsible` gained a `callout`.
  - `SpellManagementPanel` rebuilt (Spell Slots summary, Known / Prepared
    Spells per class with filter and level chips).
  - `FeatManagementPanel` rebuilt (feats grouped by source, Add / Remove).
- **Checked live** against 171527207 (custom action created and removed) and
  Helga (read only); this app on Mez test (a custom action created, edited to
  a bonus action, shown on the Actions tab, then removed).
- **Tests:** new `ManageCustomActionsPanel.test.tsx` (3),
  `ManageInventoryPanel.test.tsx` (3), `SpellManagementPanel.test.tsx` (3),
  `FeatManagementPanel.test.tsx` (1); mutator, calculator and controller tests
  for the update and the null activation type. Web 243 green.
- **Left out:** see the deviations in `sidebar-fidelity-and-customization.md`,
  9f.

## 2026-10-03 — Sidebar slice 9e: rests, game log, character menu

- **API:** `HitDice.Pool` (and `HitDiceResponse.PoolResponse`) carries
  `classNames`, the classes that bring each die size (display only; a
  3-argument constructor keeps the old shape).
- **Rests** (`RestBody.tsx`, `MechanicPanel.tsx`):
  - the Mechanic mold uses the standard `SidebarHeader`, and
    `MechanicRequest.mutedSummary` grays the short rest intro;
  - new `restRecovery` builds the RECOVER line;
  - hit dice are chosen with box rows per pool, headed "CLASS (Hit Die:
    1d8+2 • Total: 4)". Take / Reset buttons;
  - the separate "Spend Hit Dice" control and `HitDice.tsx` are gone (the
    short rest spends dice, as on D&D Beyond). `postSpendHitDice` and its
    endpoint stay, unused by the sheet.
- **Game log** (`LogPanel.tsx`): D&D Beyond's chat bubbles, newest at the
  bottom and expanded, the rest collapsed until clicked, relative time stamps,
  colored roll types. `LogRequest.senderName` is the character's name.
- **Character menu:** "Level N" under the species for milestone builds;
  40px group rows.
- **Checked live:** D&D Beyond Helga (rest panes opened, not taken) and the
  throwaway 171527207 (four test rolls for the log). This app: Vex (boxes
  marked, rest not taken; log; menu).
- **Tests:** `RestBody.test.tsx` rewritten (6), new `LogPanel.test.tsx` (3);
  `Dnd5eHitDicePoolsTest` expects the class names. Web 233 green;
  `:apps:api:check` green.
- **Left out** (listed in `sidebar-fidelity-and-customization.md`, 9e): the
  rest checkboxes, the 2024 rules radio, class art, the dice-set preview, and
  pinning the log to the pane bottom.

## 2026-10-03 — Sidebar slice 9d: feature, action and extra panes

- **API:** `FeatureAction` (and `FeatureActionResponse`) carries
  `parentName` / `parentKey`, the feature that grants the action;
  `Dnd5eSheetCalculator` fills them for trait-linked actions.
- **Web:**
  - new `systems/dnd5e/featureDetail.tsx` builds the feature-action and
    feature panes for both the Actions and Features tabs;
  - `EntityDetailRequest` gained `onOpenParent` (the parent line becomes a
    link) and `actionsPosition: 'section'` (actions under the header, with a
    separator);
  - action pane: parent link, "Limited Use" boxes, "Action Type: 1 Bonus
    Action", text. "Recharge" is gone;
  - feature pane: source line, paragraphs, and an extra block per granted
    action with its uses;
  - extra pane: category line, Hit Points under the header, the stat block
    redrawn as D&D Beyond's creature block (`ExtraStatBlockView`), Delete
    last.
- **Checked live** against Helga (read only). The extra pane was checked on
  Mez test with a temporary Cat written straight to the database and removed
  afterwards (there is no UI to add an extra).
- **Tests:** `featureDetail.test.tsx` (3) and `ExtraRow.test.tsx` (1); web
  228 green; `:apps:api:check` green.
- **Left out:** the extra's source line, Customize and art (no data for
  them). Measurements in `sidebar-fidelity-and-customization.md`, 9d.

## 2026-10-03 — Sidebar slice 9c: Conditions pane

Next in the approved D&D queue after P2.
- **`ConditionsPanel` rebuilt** to D&D Beyond's `ct-condition-manage-pane`:
  - the standard `SidebarHeader`;
  - 45px rows without dividers, 14px names (bold while active), a 32×14
    toggle with a 20px knob and the theme colors;
  - Exhaustion in its own group, with "Level --/N" and the "-- 1–6" level
    bar. The old −/+ stepper is gone, along with the unused
    `exhaustionMinusIcon` / `exhaustionPlusIcon` props.
  - Measurements are in `sidebar-fidelity-and-customization.md`, 9c.
- **The sheet's summary** reads "Exhaustion (Level 2)".
- **Checked live:**
  - D&D Beyond: Helga (read only), and the throwaway 171527207 (Blinded and
    Exhaustion 2 set, then cleared).
  - This app, on Mez test (set, then cleared): row height, positions and
    every on/off color match.
- **Tests:** three new `ConditionsPanel` tests; web 224 tests green.
- **`SidebarHeader` without an icon** now has no padding and no 37px minimum
  height, so the content starts 31.6px below the pane top, as on D&D Beyond.
  This changes every pane without an icon. Measured on D&D Beyond's
  Conditions and Speed panes: heading at the pane's own top-left, 21.6px tall.
  Panes with an icon are unchanged.

---

## 2026-10-03 — Spellcasting P2: feature-granted spells cast without a slot

Owner: finish D&D before Vampire, working down the approved queue (P2, then
sidebar 9c–9g and 10, then desktop 0.2.0). P2 was next.
- **Rules and data:**
  - New `Dnd5eSpellUsage` on `Dnd5eSpell` (at will, or N uses per
    long or short rest, with a shared pool, a cast level and self-only).
  - `Dnd5eSpellPlanner` derives it from 5etools `additionalSpells`.
  - `Dnd5eBuildMaterializer` stores it and keeps spent uses on a re-apply.
  - `Dnd5eSheetMutator.castSpell` (slot level 0) spends a use, or nothing for
    an at-will spell, and forces self-only effects onto the caster. Rests
    restore uses by trigger.
  - `Dnd5eSpell.withPrepared` now keeps every field; it used to drop the item
    fields and would drop the usage.
- **API:** `Spell.usage` / `SpellUsage`, and `SpellResponse.usage`.
- **Web:**
  - `SpellRow` shows "AT WILL" / "USE".
  - `SpellCast` has a slotless control: Use or At Will, a fixed level, and
    the frequency line.
  - The target choice and preview were extracted into `TargetChoice` and
    `CastPreview`, shared by both controls.
- **Dev tools:**
  - New `--rematerialize --player=<name>` runner: re-applies each stored
    build, keeping play state.
  - `--print-sheets` now skips drafts; it crashed on an empty draft sheet.
- **Tests:**
  - `Dnd5eSlotlessSpellsTest` (7) and three new `SpellCast` tests.
  - `:apps:api:check` green (645 tests), web 221 tests green.
- **Existing characters:** the four test characters were re-applied with the
  new runner after a `--print-sheets` backup. Every computed value is
  identical before and after. Mez test gained the slotless data.
- **Checked live** against D&D Beyond: Mez Feldrir's row labels and Cast
  control. USE is 70×28, and the frequency line sits 10px below it.
- **Left for the owner** (`open-items.md`): keeping "At Will" as a button
  (D&D Beyond shows text).
- **Not verified:** the wording for 3+ uses and for short-rest spells, and
  D&D Beyond's state after the last use. Checking them would change the
  owner's D&D Beyond character.

---

## 2026-10-03 — `.ai/` reorganized per game system

Owner asked for a `.ai/` structure that supports each game system's own
documents, with `CLAUDE.md` reading it correctly.
- **New `systems/<system-id>/`:** system-wide specs at the root, `features/`
  for one feature's plan, and `references/` for walkthroughs and audits.
  `systems/README.md` indexes the systems and maps the old paths to the new
  ones.
- **Moved:**
  - `rulesets/dnd-5e-*` → `systems/dnd-5e/` (the audits go to `references/`);
  - the eight D&D-only feature docs → `systems/dnd-5e/features/`;
  - the D&D Beyond walkthrough → `systems/dnd-5e/references/`;
  - `rulesets/vtm-v5-character-rules.md` → `systems/vtm-v5/character-rules.md`;
  - `features/vampire-v5.md` → `systems/vtm-v5/features/implementation-plan.md`.
  `rulesets/` no longer exists. `features/` now holds only platform features:
  character sheet, desktop, home page, private sharing, Oracle deployment.
- **References updated:** every `.ai/` doc, `CLAUDE.md`, and the doc paths
  quoted in code comments (Java, TypeScript, CSS, `build.gradle`; comments
  only, no behaviour change), plus the assistant's memory notes.
- **Not rewritten:** older changelog entries, which keep the paths valid when
  they were written.
- **`CLAUDE.md` §3:** new rows for `systems/README.md`, `systems/<id>/*.md`,
  `systems/<id>/features/` and `systems/<id>/references/`; an updated reading
  order; and a "Where a new document goes" rule.
- **Git (owner):** the moves were plain file moves. Stage both the deletions
  and the new paths (`git add -A .ai`), so Git records them as renames.

---

## 2026-10-03 — VtM 5e rules reference compiled

Owner supplied the VtM 5e core rulebook, the Camarilla and Anarch
sourcebooks, and the free basic-mechanics sheet, and asked for a compiled,
easy-to-consult reference like the D&D one.
- New `rulesets/vtm-v5-character-rules.md`, covering:
  - dice and Hunger dice, trackers and damage, the Blood Potency table;
  - character creation step by step, with XP costs;
  - attributes and skills, 9 clans plus Caitiff and thin-bloods, 10 predator
    types;
  - merits, flaws and backgrounds; coterie and domain;
  - every discipline power by level (cost, pool, amalgam), rituals and
    thin-blood alchemy;
  - Resonance, weapons and armor;
  - 37 loresheets with their five dot names;
  - what the sheet must store and compute.
- Written as summaries and tables in our own words; no book text copied.
  Keep it out of any public push, like the D&D Beyond material.
- Owner decisions, same day:
  - attribute spread follows Demiplane (1×4, 3×3, 4×2, 1×1);
  - where they conflict, the books win over the basic-mechanics sheet, so the
    Blood Potency table is the printed one;
  - the Camarilla and Anarch modules' options are in scope.
- Ruins of Carthage's six entries are read as one dot with two alternatives.
  This is inferred, not verified, and is listed in `open-items.md`.
- `features/vampire-v5.md` §6 points to it.
- Not compiled: the core book's Storyteller-side chapters (Advanced Systems,
  cities, chronicles, antagonists).

---

## 2026-10-03 — Vampire: The Masquerade 5e plan (phase 12, proposed)

Owner asked for a plan to create and view VtM sheets, using Demiplane's VtM
Nexus as the reference, the way D&D Beyond is used for D&D.
- New `features/vampire-v5.md`: a live walkthrough of the owner's "Test VTM"
  sheet and of Demiplane's builder; what to keep and not copy; the seams VtM
  breaks (closed `RollKind`, d20-shaped rolls, shared catalogue kinds, the web
  sheet screen); slices V0–V7; five questions for the owner.
- `roadmap.md` phase 12 and `open-items.md` point to it.
- During the walkthrough, two rolls landed in the reference character's
  Demiplane log: a Rouse Check (the header button; success, Hunger unchanged)
  and an Athletics + Dexterity roll. Nothing else on that character changed.
- No code changed.

---

## 2026-10-03 — Developer guide

Owner asked how to update the desktop executable after later changes, and for
an index of the API and web code.
- New `.ai/developer-guide.md`:
  - Part 1: what to run after each kind of change, refreshing the catalogue,
    building and testing a new desktop version (`appVersion`,
    `packageDesktop`, upgrade test over the previous version's data), and a
    release checklist.
  - Parts 2 and 3: API and web code maps, platform classes, and where each
    process happens (sheet change, roll, creation, level up, import, login),
    with one section per game system (D&D 5e now, a template for the next).
  - Part 4: the checklist for adding a game system.
  - Records the web's current direct couplings to `systems/dnd5e`
    (`CharacterSheetScreen`, the `CharacterSheet` type, dice icons).
- Indexed in `CLAUDE.md`; linked from `apps/desktop/README.md`.
- No code changed. The desktop package itself was not rebuilt.

---

## 2026-09-28 — Pact Magic (P1) and the Cast control restyled

Owner report: Mez test (Warlock 3 / Fighter 3) had no spell slots, and casting
Mage Armor did nothing. Plan approved in `features/spellcasting-pools.md`
(P1 Pact Magic, P2 casting without a slot); D&D Beyond measured on the owner's
Warlock (118003256) and Copy of Raya (56008841), read only.
- **Backend:**
  - `Dnd5eSpellSlotLevel` / `SpellSlotLevel` / `SpellSlotLevelResponse`
    gained `pact` and `className` (old three-argument constructors kept).
  - `Dnd5eBuildMaterializer` adds the Pact Magic pool from the class's
    `pactSlotsByLevel` and keeps its used count.
  - `consumeSpellSlot` / `restoreSpellSlot` / `castSpell` take `pact`
    (`?pact=true`; `CastSpellRequest.pact`). A short rest refills the pact
    pool; a long rest refills every pool.
- **Web:**
  - Spells tab: slot groups with D&D Beyond's "Slots" / "Pact" labels; the
    Pact Magic level lists the pact class's lower-level spells with a blue
    level badge on Cast and their effect scaled (`SpellRow.castLevel`,
    `spellDamageLabel(spell, castLevel)`, `scaleDiceCount` moved to
    `spellCombat.ts`).
  - `SpellCast` restyled to D&D Beyond's `.ct-spell-caster`: "Cast" + a
    theme-filled button with a count badge ("Spell Slot" or "Pact Slot" by
    level), "Level" + a − / + stepper over every pooled level, the effect
    preview in 14px. CSS `.spell-cast*` rewritten.
  - A spell's Cast control now sits right under the subtitle, before
    Customize (`entity-detail__actions--top`), as on D&D Beyond.
- **Data:** Mez test got her pool (2 × 2nd level, Warlock) by a one-off update
  equal to the materializer's output; nothing else on her sheet changed. The
  live check spent one pact slot and gave it back.
- **Tests:** materializer (pact pool, used count kept), mutator (pact spent
  apart, short rest), controller (`?pact=true`, response fields), active
  effects updated; `SpellCast.test.tsx` +2. Web: 218.
- **Checked live** on Mez test ("2ND LEVEL … PACT" with Armor of Agathys,
  Command, Hellish Rebuke 3d10, Witch Bolt 2d12; Command cast with "Pact
  Slot", 2 → 1, restored) and Vex (Chromatic Orb: Spell Slot 4, 1st, 3d8).
- Mage Armor still can't be cast: that is P2.

Suggested commit: `Add Warlock Pact Magic slots and restyle the spell Cast control like D&D Beyond`

---

## 2026-09-28 — Max HP Breakdown restyled

Owner request: style the HP Management pane's "Max HP Breakdown" better. It is
our own addition (D&D Beyond has none), so it now reuses the D&D Beyond patterns
already built:
- the themed `Customize` collapsible (label "Max HP Breakdown", chevron at the
  right; no top border, since the section above already draws one);
- inside it, the ability pane's table (`.hp-breakdown*`: 1px `#EDEAE8` lines,
  a 70px value column with a 2px `#D0CAC5` left border, the sign drawn apart);
  the first line (the level-1 hit die) is unsigned, the rest signed;
- a closing "Max Hit Points" row with the maximum, in bold condensed type under
  a 2px `#D0CAC5` line.
- Checked live on Vex (8, +15, +12, +7, +3 = 45). Web: 216 tests.

Suggested commit: `Restyle the Max HP Breakdown as a Customize collapsible with the ability pane's table`

---

## 2026-09-28 — Hit Points box: Heal / Damage buttons and field at D&D Beyond's size

Owner report: the top-row Hit Points box's Heal and Damage buttons were smaller
and thinner than D&D Beyond's. Measured on 50149479 (read only):
- HEAL / DAMAGE: 70×21, Roboto Condensed 500 8.8px, `5px 10px` padding, 1px
  `#92A2B3` border, 4px radius, text `#40D250` / `#D24040`. Ours were 76×15,
  9px, `1px 2px`, with a colored border.
- The amount field: 70×25, Roboto 13px, 4px padding, `#FEFEFE`, 1px `#92A2B3`.
  Ours was 76×15, 9px.
- Positions: HEAL at 16,8, the field at 16,32, DAMAGE at 16,60, "Hit Points"
  at y 64 (the Heal / Damage column runs the box's height; the title sits
  under the numbers). `.hit-points__content` now starts at the top with 8px
  padding, the row aligns to the top, and `.hit-points__title` is placed at
  64px. Ours now measures the same.
- **Left as is:** the Current / Max / Temp numbers use `--font-ui-native`
  (system-ui); D&D Beyond's are Roboto 500 26px / 31.2px.
- Web: 216 tests.

Suggested commit: `Size the Hit Points box's Heal / Damage buttons and field like D&D Beyond`

---

## 2026-09-28 — Sidebar fidelity, slice 9b: Proficiencies & Training

D&D Beyond's pane, observed on 171527207 (Elvish and a custom language were
added and removed; measurements in the feature doc).
- **Backend:**
  - `Dnd5eCustomProficiency` (key, type ARMOR / WEAPON / TOOL / LANGUAGE,
    name, custom, notes) in `Dnd5eCustomizations.proficiencies`, through the
    `proficiencies` group: `new` adds an existing one by name or an unnamed
    custom tool or language; a key edits the notes (and a custom one's name);
    removing drops it. A custom armor or weapon, or an existing one without a
    name, is refused.
  - The calculator joins the named ones to the build's lists (once, ignoring
    case), so the sheet and weapon attack proficiency count them. They are
    play state, so re-applying the build keeps them.
  - Removed: `POST` / `DELETE /api/characters/{id}/proficiencies/{category}`,
    `AddProficiencyRequest`, `SheetMutator.addProficiency` /
    `removeProficiency`, `Dnd5eProficiencyCategory`,
    `InvalidProficiencyCategoryException` and its handler. They wrote free
    text straight into the build's lists, which a rebuild lost, and could
    remove granted proficiencies.
- **Web:**
  - `Dnd5eProficienciesPane` (a `pane`, opened by the box's gear): hand-added
    entries per type with "Remove Proficiency", "Enter Name" for custom ones
    and "Enter Source Note..."; Add New Proficiencies with a type select
    (Existing / Custom) and, for an existing type, "Available Proficiencies"
    from the LANGUAGE and ITEM catalogues (`proficiencyOptions`: mundane only;
    sources shown); then the full lists. CSS `.proficiencies-pane*`.
  - `ProficienciesPanel` takes `onMutate` / `onOpenPane`; the Collection
    editor (`CollectionEditorPanel`, `CollectionEditorRequest`,
    `postAddProficiency`, `deleteProficiency`, the `collectionEditor` sidebar
    kind and its props) is gone.
- **Tests:** `Dnd5eSheetMutatorTest` (5 replace the old proficiency tests),
  `Dnd5eSheetCalculatorTest` +1, `CharacterControllerTest` (the add goes
  through the customization endpoint; 3 old tests removed);
  `Dnd5eProficienciesPane.test.tsx` (6). Backend 635, `check` green (the
  known-flaky `S3PortraitStorageTest` failed once with MinIO's 503 and passed
  on its own). Web: 49 files, 216 tests.
- **Checked live** on Vex after restarting the API (with the owner's go-ahead):
  Giant (existing) and a custom tool named "Glassblower's Tools" were added,
  showed in the pane and in the sheet's box, then were removed; Vex's lists
  are back as they were.
  - The first open crashed: Vite was serving a stale `customizations.ts`
    (its file watcher missed the edit). Touching the file fixed it; the code
    was right.
- Languages and tools list the Player's Handbook group first, then the other
  sources alphabetically.

Suggested commit: `Rebuild Proficiencies & Training as D&D Beyond's pane with hand-added proficiencies as customizations`

---

## 2026-09-28 — Boxed quotes and per-level headings in the rules text

The owner marked PHB sidebars (Hiding, Finding a Hidden Object) with a
`blockquote` line before and a `fim-blockquote` line after.
- `RulesText` reads the lines between the markers as Markdown and draws them
  in a `<blockquote>`; a paragraph stops at a marker.
- Measured on D&D Beyond's Dexterity pane (171527207): the box is `#FAFAFB`
  with 4px `rgba(138,146,153,.45)` bands above and below, 1px `#F4F2F2`
  sides, `12px 14px 2px` padding and `8px 2px 4px` margin. Its headings: `h4`
  is Roboto Condensed 700 14px / 19.6px with 18.62px margins, `h5` the same
  with 23.38px. Ours now follow (every heading was 15.21px before).
- **Tests:** `RulesText.test.tsx` +1. Web: 210. Checked live on Vex's
  Dexterity pane.
- **Noticed on D&D Beyond:** the throwaway character 171527207 showed 2 / 13
  HP. It was left at 13 / 13 after the death saves check, and this session
  didn't change it since.

Suggested commit: `Draw boxed quotes and per-level headings in the rules text`

---

## 2026-09-28 — Rules text reads Markdown

The owner filled `rulesText.ts` in Markdown: `####` headings with the paragraph
on the next line, `* ` bullets, and a pipe table for Exhaustion with a
"checkbox" column. `RulesText` only knew a subset, so it now reads the text
line by line:
- headings `#`–`######` (drawn alike, as `<h2>`–`<h6>`), paragraphs with line
  breaks, `-` / `*` / `+` bullets, numbered lists, `---` / `***` rules, pipe
  tables, `***bold italic***`, `**bold**`, `*italic*`;
- a table cell holding only `checkbox` draws a display-only checkbox (tied to
  the exhaustion level in slice 9c).
- CSS: every heading level shares the measured condition heading; `ol`, and a
  table (condensed bold headers, `#EAEAEA` row lines; not measured on D&D
  Beyond yet).
- Removed the `# Death Saving Throws Rules` line from `DEATH_SAVES_RULES_TEXT`:
  the pane already draws that subheading, so it showed twice.
- **Tests:** `RulesText.test.tsx` +3 (heading then paragraph and `* `
  bullets; table with checkbox; bold italic and numbered list). Web: 209.
  Checked live on Vex: the Strength pane and Exhaustion's table render.

Suggested commit: `Read the hand-written rules text as Markdown (headings, lists, tables)`

---

## 2026-09-28 — Rules text slots for conditions and death saves; HP Management +/− fixed

Owner request: every explanatory text D&D Beyond shows gets a slot. More
panes were checked on 171527207 (Heroic Inspiration was toggled and toggled
back; the character was dropped to 0 HP and healed back to 13 / 13):
- **Conditions:** each condition's row has a chevron that shows its rules
  (paragraphs, a rule, a heading, bullets). `ConditionsPanel` gains
  `ConditionItem`, a chevron beside the row that shows the text, only when it
  exists. `ConditionEntry.rulesText`, `ConditionsRequest.exhaustionRulesText`;
  `CONDITION_RULES_TEXT` (14) and `EXHAUSTION_RULES_TEXT` in `rulesText.ts`.
- **Death saves:** while dying, D&D Beyond's HP Management ends with a "Death
  Saving Throws Rules" subheading and text. `HpManagementRequest.dying` and
  `deathSavesRulesText`; `DEATH_SAVES_RULES_TEXT`.
- **No text:** Heroic Inspiration (no pane), the character menu, Change Sheet
  Appearance.
- `RulesText`: `### ` headings, `---` rules, a `plain` placement; list,
  heading and rule spacing measured on the condition text.
- **Fix to slice 9a:** D&D Beyond's "+" / "−" ("Increase / Decrease Hit
  Points") step the Healing / Damage field up by 1; APPLY CHANGES (100×30) and
  CANCEL (65×30) appear while something is pending and apply both. Ours
  applied on "+" / "−". CSS `.hp-management__apply*`; the rows are now
  selected by modifier class.
- `ChevronDown` is exported from `sidebarParts.tsx`.
- **Tests:** `ConditionsPanel.test.tsx` (2); `HpManagementPanel.test.tsx` now
  5 (stepping and Apply, Cancel, death saves text); `RulesText.test.tsx` +1.
  Web: 48 files, 206 tests. Checked live on Vex: "+" puts 1 in Healing,
  Apply / Cancel measure 100×30 and 65×30, Cancel clears.
- **Noticed, not caused by this change:** between 09:27 and 10:47 the web
  suite went from 48 files / 205 tests to 47 / 201 before
  `ConditionsPanel.test.tsx` was added. A test file with 4 tests is gone from
  `apps/web/src`; check `git status` for a deleted `*.test.tsx`.

Suggested commit: `Add condition and death save rules text slots; fix HP Management +/− to step instead of apply`

---

## 2026-09-28 — Rules text slots in the D&D 5e sidebar panes

Owner request: D&D Beyond closes most panes with explanatory rules text, and the
owner will write ours by hand. The slots and their styling are built, empty.
- Checked on D&D Beyond (171527207) which panes have the text: the six
  abilities, Proficiency Bonus, Speed, Initiative, Armor Class, Saving Throws
  (the same text in the grid pane and in each save), Senses, each skill, and
  both rests (an intro above the controls and the rules below). HP
  Management, Defenses, Conditions, Proficiencies & Training and All Skills
  have none. Only the paragraph counts were read; no text was copied.
- `RulesText` (`sheet/RulesText.tsx`): blank line → paragraph, line break →
  `<br>`, "- " lines → bullets, `**bold**`, `*italic*`; nothing for an empty
  text. CSS `.rules-text*` in `frames.css`, measured on D&D Beyond's
  `ddbc-html-content` (10px margin and padding, 1px `#EAEAEA` top border,
  Roboto 13px / 18.2px, 15px between paragraphs).
- `systems/dnd5e/rulesText.ts` holds every text. The Senses summary and the
  two rest summaries (our own wording) moved there; the rest are empty.
- Wired into `Dnd5eAbilityPane` (new `rulesText` prop), the Proficiency Bonus
  explainer (`ExplainerRequest.rulesText`), the Speed, Initiative and Armor
  Class panes, both saving throw panes, `SkillPane`, and `MechanicPanel`
  (`summary` is now the intro, plus `MechanicRequest.rulesText`).
- `features/rules-text-slots.md` lists each constant, its pane, how to open it,
  and the format.
- **Tests:** `RulesText.test.tsx` (4); web 205 tests. Checked live on Vex's
  Senses pane: the measured styles match.

Suggested commit: `Add hand-written rules text slots to the D&D 5e sidebar panes`

---

## 2026-09-28 — Sidebar fidelity, slice 9a: HP Management with Max HP Modifier and Override Max HP

Slice 9 is split into sub-slices 9a–9g (one panel group each, see
`features/sidebar-fidelity-and-customization.md`). 9a applies owner decision 3:
D&D Beyond's Max HP Modifier and Override Max HP replace the "manual
adjustment", and the pane takes D&D Beyond's layout (measured on 171527207, then
restored).
- **Backend:**
  - `Dnd5eCustomizations.hitPoints` (`maxModifier`, `overrideMax`), through the
    `hitPoints` group of the customization endpoint.
  - `Dnd5eFormulas.maxHitPoints`: the override replaces the calculated maximum
    and the modifier; exhaustion 4 still halves the result.
    `calculatedMaxHitPoints` is the maximum without either.
  - The calculator's trace shows "Max HP Modifier", or only "Override Max HP".
  - `Dnd5eSheet`'s compact constructor moves a stored `maxHitPointsAdjustment`
    into `maxModifier` and reads the field as 0, so existing sheets keep their
    value. `withMaxHitPointsAdjustment` is gone.
  - Customizing moves current hit points by the change in the maximum (D&D
    Beyond keeps the damage taken); a character at 0 stays at 0.
  - Removed: `POST /api/characters/{id}/hit-points/max-adjustment`,
    `MaxHitPointsAdjustmentRequest`, `SheetMutator.setMaxHitPointsAdjustment`.
  - `VitalsZone.maxHitPointsAdjustment` is now `calculatedMaxHitPoints` (also
    in `CharacterSheetResponse`).
- **Web:**
  - `HpManagementPanel` rebuilt: Current / Max / Temp; Healing and Damage
    boxes, each applied by its +/− button, with New HP between them; Max HP
    Modifier and Override Max HP saved on blur (blank clears). A customized
    maximum is green or red with the calculated one in parentheses. CSS
    `.hp-management*` in `frames.css`, with D&D Beyond's measured `#40D250` /
    `#D24040` for healing, damage and the customized maximum.
  - The Apply / Cancel pair and the "Max HP Adjustment" block are gone; the Max
    HP Breakdown stays as a collapsible at the end.
  - The top-row Hit Points box no longer bolds or stars the maximum.
- **Tests:** `Dnd5eSheetMutatorTest` (modifier, override and clearing,
  downed character, unknown field, legacy value), `Dnd5eSheetCalculatorTest`
  (modifier trace, override trace, floor at 1), `CharacterControllerTest`
  (customize returns `calculatedMaxHitPoints`); `HpManagementPanel.test.tsx`
  (3). `:apps:api:check` green; web 201 tests.
- **Checked live** on Vex against D&D Beyond's 171527207: modifier 3 gave
  48 (45) in green with current 45 → 48; override 10 gave 10 (45) in red with
  current 10; clearing both restored 45 / 45. Same result as D&D Beyond.

Suggested commit: `Replace the max HP adjustment with D&D Beyond's Max HP Modifier and Override Max HP`

---

## 2026-09-28 — Versatile two-handed damage; level up shows only the gained level's hit die, the class progression, and a bottom footer

Owner requests:
- **Versatile weapons.** A versatile weapon also rolls its larger die with
  two hands, as D&D Beyond shows. Measured on 50149479's Warhammer, the
  damage cell stacks two buttons:
  - one-handed on top: 70×36, 14px, with the damage icon;
  - two-handed below: 3px gap, same 36px height, 12px `#75838B`, no icon.
  - Backend: `AttackRow.versatileDiceCount` / `versatileDiceSides` (the old
    constructor is kept), filled from the item. `RollKind.ATTACK_DAMAGE_VERSATILE`
    rolls them with the same modifier ("<name>: two-handed damage"). The
    item's "Versatile" property reads "Versatile (1d8)", like the sources.
  - Web: `AttackRow` gains the second button. CSS
    `.action-row__value-cell--versatile` and `.action-row__value--versatile`.
- **Level up hit points.** Only the level being gained can be rolled or set;
  earlier levels no longer show. The planner puts `classLevel` in each
  option's data, and `Dnd5eRolledHitPointsField` takes a `classLevel`
  filter.
- **Class progression on the level up.** The class's `ProgressionPanel`
  closes the page, open, with the reached level marked and "After this level
  up: <Class> <level>, character level <n>." `ProgressionPanel` gained
  `defaultOpen` and `caption`.
- **Footer.** The level up page is at least as tall as the window
  (`.level-up-page`), so the footer rests at the bottom.
- **Tests:** a resolver test (versatile + refusal) and a versatile
  assertion in `Dnd5eCustomizationsTest`; backend 634, `check` green.
  `AttackRow.test.tsx` (2); `Dnd5eLevelUpPage.test.tsx` now 2 (the gained
  level only; the progression).

Suggested commit: `Add versatile two-handed damage and level up refinements (gained level HP only, class progression, footer)`

---

## 2026-09-28 — Sidebar fidelity, slice 8: item and spell Customize and rename

Owner request: D&D Beyond's Customize on items and spells, with the pencil
rename.
- **Backend:**
  - `Dnd5eItemCustomization` and `Dnd5eSpellCustomization`, stored in
    `Dnd5eCustomizations.items` and `.spells` (by item and spell key) through
    the `items` and `spells` groups. The editor now gets the sheet and
    refuses a key the sheet doesn't have.
  - `Dnd5eItem.customizedAs` applies name, notes, cost (gp) and weight (lb),
    and adds Silvered / Adamantine to the properties. The calculator applies
    it to the sheet first, so inventory, encumbrance, attacks and defense
    sources agree.
  - A weapon's attack takes To Hit Override (replaces the total), To Hit
    Bonus (a contribution), Damage Bonus and Notes. Display As Attack lists
    an unequipped weapon. Weapon proficiency also matches the catalogue slug,
    so renaming keeps it.
  - `Spell.adjustments` (`SpellAdjustments`, generic) carries the spell's
    attack override / bonus, damage bonus, DC override / bonus and Display As
    Attack; name and notes are applied. The resolver uses them for spell
    attack and damage rolls. `SpellResponse.adjustments`;
    `ItemResponse.properties`.
- **Web:**
  - `EntityDetailRequest` gained `customize` (editor + customized),
    `refresh` (rebuilds the whole panel from the live sheet) and `entityKey`
    (another entity remounts the panel).
  - `EntityDetailPanel` draws the pencil (`PencilIcon`, moved to
    `sidebarIcons.tsx`), which toggles Customize and focuses Name, plus
    "Name\*" and "Customize\*" when customized. `Customize` can now be
    controlled.
  - `EntityCustomizeEditor`: D&D Beyond's grid, saved on blur; "Remove
    Customizations" asks "Confirm (3)" first.
  - Items (`buildItemDetailRequest`, also opened from a weapon's attack row)
    and spells (`buildSpellDetailRequest`) use it. Spell rows, attack rows
    and the spell panel show the adjusted attack, DC and damage
    (`adjustedSpellAttack`, `adjustedSpellSaveDc`, `spellDamageLabel`).
- **Tests:** `Dnd5eCustomizationsTest` +4 (item rename / price / attack;
  override + Display As Attack; spell adjustments reaching the damage roll;
  refusals). Backend: 633, `check` green. `EntityCustomize.test.tsx` (3).
  Web: 45 files, 195 tests.
- **Checked live (DOM, the tab was in the background):** Mez test's Spear
  became "Oathspear\*", +4 → +5, with Customize kept open across the
  refresh, then was reset through Confirm. Eldritch Blast went +5 → +7 in
  the row and the panel, then was reset. The owner approved the look on
  2026-09-28. The spell Customize's position is still assumed (see the
  feature doc).

Suggested commit: `Add D&D Beyond's item and spell Customize with the pencil rename`

---

## 2026-09-28 — "Proficiency" badge heading uses `.text-bold-uppercase`

Owner request: the Proficiency Bonus heading takes `.text-bold-uppercase`;
Speed's "Walking" keeps the badge heading style. `StatBadge` gained an
optional `headingClassName`, and only the proficiency badge passes it.
- Checked live: "PROFICIENCY" is 13px, `--text-on-control`; "WALKING" is
  unchanged at 9px. The owner adjusted the fit by hand.

Suggested commit: `Style the proficiency badge heading with text-bold-uppercase`

---

## 2026-09-28 — Sidebar fidelity, slice 7: speed, initiative, armor class and defenses panes

Owner request: D&D Beyond's Customize on these panels, and defenses added by
hand (display only; option A: the 13 damage types, plus D&D Beyond's 15
conditions for immunity).
- **Backend:**
  - `Dnd5eCustomizations` gained `speeds`, `movementDisplay`, `armorClass`
    and `defenses` (`Dnd5eCustomDefense`), with the matching groups.
    `defenses/new` adds one; a key updates its notes; removal deletes it.
    Unknown sub-types are refused, and conditions are accepted only as
    immunities.
  - Walking speed override. Armor class: Override AC replaces the total;
    Override Base Armor + DEX replaces the first two contributions;
    Additional Magic / Misc Bonus add.
  - `VitalsZone.defenses` (`DefenseEntry`: type, name, source, custom,
    notes) feeds the four defense lists: the sheet's own, then active items,
    then custom ones. It is also in the response (`defenses`).
  - The materializer records species defenses as `DEFENSE` grants so the
    pane can name their source. Sheets materialized before this show no
    source until their build is re-applied.
- **Web:**
  - `Dnd5eCombatPanes.tsx`: Speed (rows, Override Speeds, Set Movement
    Display), Initiative (score, ±5 with advantage / disadvantage), Armor
    Class (contributions, 4-row Customize), Defenses (grouped with gray
    source and "(Custom)", Defense Type → Sub-Type adds at once, DELETE and
    a source note per custom entry).
  - The Speed badge shows the chosen movement when it has a speed. The
    Initiative caption, the AC value and the Defenses column open the panes.
    The Defenses box marks hand-added entries with "\*".
  - `onExplain` is no longer passed to `Dnd5eVitalsColumns`.
- **Owner-reported fixes:**
  - The saving throw labels (STR…CHA) had lost their style when they became
    buttons in slice 4 (`button.saving__abbr { font: inherit }` overrode
    them). They now use D&D Beyond's computed style: Roboto Condensed
    12px / 700, black.
  - The sidebar no longer follows the viewport. Like D&D Beyond's
    `ct-sidebar` (measured: `position: absolute`, top 72px), it is anchored
    to the sheet below the app header (`SheetShell`'s wrapper is
    `position: relative`) and scrolls away with the page.
- **Tests:** `Dnd5eCustomizationsTest` +6. Backend: 629 tests, `check`
  green. `Dnd5eCombatPanes.test.tsx` (6). Web: 44 files, 192 tests.
- **Checked live:** all four panes on Mez test. A custom Cold resistance
  showed "Cold\* (Custom)" and "Fire, Cold\*" on the sheet, then was deleted.
- **Left out:** rules text on these panes waits for slice 10.

Suggested commit: `Add D&D Beyond's speed, initiative, armor class and defenses panes with Customize`

---

## 2026-09-27 — Sidebar fidelity, slice 6: senses pane and Customize

Owner request: a gear on Senses, with D&D Beyond's Customize.
- **Backend:**
  - `Dnd5eNotedValue(value, notes)`;
  - `Dnd5eCustomizations.passives` (passivePerception / Investigation /
    Insight) and `.senses` (by `Dnd5eSenseType`);
  - the `passives` and `senses` groups, with removal.
  - The calculator computes skills first. A passive is now 10 + that skill's
    own bonus (PHB 2014, "Passive Checks"), so it follows the skill's
    customization; "Override Passive …" replaces it.
  - A sense distance replaces the native one, or adds the sense.
  - Unused modifier locals were removed.
- **Web:**
  - `Dnd5eSensesPane`: the passive and special-sense list, then Customize with
    three passive overrides (value + notes) and the Distance / Source-Notes
    table for the four senses;
  - `SensesPanel`: the gear on "Senses" and the bottom line open it,
    replacing the old entity-detail panel;
  - `Dnd5eLeftColumn` no longer needs `onOpenDetail`.
- **Sidebar fix:** `CharacterSheetScreen` now derives the sidebar's
  visibility (the hidden panel, or `SHOW_EMPTY`) instead of setting it in an
  effect. A panel opened right after the page loads could stay collapsed; it
  now opens on the first click.
- **Research correction:** the 15px difference against D&D Beyond's table
  widths is its scrollbar, not a styling difference.
- **Tests:**
  - `Dnd5eCustomizationsTest` +2 (the passive follows its skill and can be
    overridden; senses set, added or refused);
  - `Dnd5eSensesPane.test.tsx` (2).
  - Web: 43 files, 186 tests; all backend D&D 5e tests pass.
- **Checked live:**
  - Mez test's senses pane shows D&D Beyond's list, overrides and distance
    table (85×25 fields);
  - slice 5 was checked too (All Skills; a pager matching D&D Beyond to the
    pixel).

Suggested commit: `Add D&D Beyond's senses pane and Customize, with passives following their skills`

---

## 2026-09-27 — Sidebar fidelity, slice 5: skills panes, Customize and custom skills (live check pending)

Owner request: a gear on Skills, D&D Beyond's per-skill Customize, and its
custom skills.
- **Backend:**
  - `Dnd5eCustomizations` gains `skills` (`Dnd5eCheckCustomization`, with Stat
    Override) and `customSkills` (`Dnd5eCustomSkill`: name, stat, proficiency
    level, override, magic and misc bonuses, notes, description).
  - The `skills` and `customSkills` groups:
    - `PUT …/customizations/customSkills/new` adds "Custom Skill N"
      (proficient, no ability), as D&D Beyond does;
    - a key edits that custom skill, keeping its key.
  - New seam method `SheetMutator.removeCustomization`, exposed as
    `DELETE /api/characters/{id}/customizations/{group}/{target}`. It clears a
    customization or removes a custom skill.
  - Calculator:
    - a skill uses its Stat Override (`skillGoverningAbilities` follows) and
      its customized proficiency level;
    - bonuses and the override apply as for saves;
    - custom skills are calculated into the new `VitalsZone.customSkills`
      (`CustomSkill`), with a constructor keeping the previous signature.
  - `Dnd5eMechanicResolver` rolls a custom skill by its key
    ("Cartography: check").
- **Web:**
  - `SkillsPanel` gets the gear on "Skills" and an "Additional Skills" link,
    both opening `AllSkillsPane`. Its collapsibles are "Skills" (open) and
    "Custom Skills", with "+ Add Custom Skill". Custom skills are also listed
    at the end of the sheet's box, rolling like any skill.
  - `SkillPane`: `SidebarPager` (PREV / NEXT through the eighteen skills and
    then the custom ones), a "Skills" parent, the dot, ability and name, and
    `CheckCustomize` with Stat Override.
  - `CustomSkillPane`: the same pager, the "Edit" collapsible (Override,
    Magic / Misc Bonus, Stat, Proficiency Level, Name, Notes, Description)
    and Remove.
  - New parts: `SidebarCollapsible`, `SidebarPager`,
    `Customize label="Edit"`, and `.sidebar-remove-button` (the measured
    theme outline button).
  - `skills.ts` holds the shared list.
- **Tests:**
  - `Dnd5eCustomizationsTest` +4 (stat and level, custom skill add / edit /
    roll / remove, clearing, invalid input);
  - `Dnd5eSkillPanes.test.tsx` (3).
  - Full web suite: 42 files, 184 tests.
- **Not checked live yet:** Vite and the API were stopped by the system for
  low memory before the check.
- **Left for slice 6:** passive senses following a customized skill.

Suggested commit: `Add D&D Beyond's skill panes, skill Customize and custom skills`

---

## 2026-09-27 — Sidebar fidelity, slice 4: saving throws panes and Customize

Owner request: a gear on Saving Throws, and D&D Beyond's per-save Customize.
- **Backend:**
  - `Dnd5eCheckCustomization`, shared with skills in slice 5: override, magic
    bonus, misc bonus, proficiency level and stat override, each with source
    notes. Blank notes are stored as none.
  - `Dnd5eCustomizations.savingThrows`, and the `savingThrows` group in
    `Dnd5eCustomizationEditor`, which refuses a stat override.
  - The calculator applies them: the proficiency level feeds both the value
    and the sheet's dot; magic and misc bonuses add their own contributions;
    the override replaces the total.
  - `Provenance.customizations` sends the stored values, so the panes can
    fill their fields. `Dnd5eProficiencyLevel` and the record are now public
    for JSON.
- **Web:**
  - the Saving Throws box's title gets the gear, opening `SavingThrowsPane`:
    the grid, then "Saving Throw Modifiers" (the measured
    `.sidebar-subheading`);
  - each save's abbreviation opens `SavingThrowPane`: a clickable "Saving
    Throws" parent, the ability icon, "Strength Saving Throw +5", and
    `CheckCustomize`;
  - `CheckCustomize` holds four value / source-notes rows, the proficiency
    level as a select;
  - shared modules: `abilityIcons.ts` and `customizations.ts`.
- **Tests:**
  - `Dnd5eCustomizationsTest` +3 (level and bonuses, override, invalid
    input);
  - `SavingThrowsPanel.test.tsx` +2 (gear and abbreviation panes; Customize
    keeps the other stored values).
- **Checked live** on Mez test: the gear opens the grid pane; Charisma's pane
  shows the parent, the heading and the four rows with D&D Beyond's options.

Suggested commit: `Add D&D Beyond's saving throw panes and Customize`

---

## 2026-09-27 — Sidebar fidelity, slice 3: ability pane and the customization foundation

Owner request: D&D Beyond's "Customize" options, starting with the ability
pane's Other Modifier and Override Score.
- **Customization foundation (backend):**
  - `Dnd5eSheet` gains `customizations` (`Dnd5eCustomizations`), play state
    that survives re-materializing. It is null on older sheets and read
    through `customizationsOrEmpty()`. A constructor with the previous
    signature keeps the existing callers.
  - The generic seam is `SheetMutator.customize(sheet, group, target,
    valueJson)`, exposed as
    `PUT /api/characters/{id}/customizations/{group}/{target}`. The D&D 5e
    `Dnd5eCustomizationEditor` checks the group and target, reads the value
    and validates it with Bean Validation.
  - `InvalidCustomizationException` returns 400.
- **Abilities:** `Dnd5eAbilityCustomization(otherModifier, overrideScore)`.
  - The effective score is computed in `Dnd5eFormulas.withItemAbilityScores`:
    the Override Score replaces it; otherwise the item's set score applies,
    then the Other Modifier adds (clamped 1–30).
  - Max HP, saves, skills and attacks all follow. The customizations are
    consumed there, so applying them twice changes nothing.
  - The contributions show "Other Modifier" or "Override Score".
  - New `AbilityScoreBreakdown` in `Provenance.abilityBreakdowns`: total,
    modifier, base, bonus with its sources, set score, stacking bonus.
- **Web:**
  - a generic `pane` sidebar kind (a system panel drawn from the live sheet)
    and a `CUSTOMIZE` mutation;
  - `Dnd5eAbilityPane` replaces the ability explainer: D&D Beyond's table
    with the bonus sources, then the Other Modifier and Override Score
    fields, saved on blur;
  - `SidebarHeader` gains the "(+3)" format.
- **Tests:**
  - `Dnd5eCustomizationsTest` (5): other modifier, override (max HP follows),
    clearing, re-materializing, invalid input;
  - `CharacterControllerTest` +2;
  - `Dnd5eAbilityPane.test.tsx` (2).
- **Checked live:** Mez test's Strength pane shows D&D Beyond's rows (29px)
  and fields (70×27). The slice 2 rename also reached the database; the name
  was restored afterwards.
- **Left for later:** the rules text under the table (slice 10) and the
  table's width (slice 9).

Suggested commit: `Add sheet customizations and D&D Beyond's ability pane`

---

## 2026-09-27 — Sidebar fidelity, slice 2: rename the character

Owner request: rename the character from a pencil beside the name in the
character sidebar, as on D&D Beyond.
- **API:** `PUT /api/characters/{id}/name` with `{name}`:
  - `RenameCharacterRequest` requires a non-blank name of 128 characters at
    most;
  - `CharacterService.rename` trims it, only works on the caller's own active
    character, and returns the character.
- **Web:**
  - `renameCharacter` in `characters/api.ts`;
  - `CharacterMenuPanel`'s name becomes `EditableName`, with D&D Beyond's
    `PencilSvg`. A click on the name or the pencil opens the inline field;
    leaving it saves a changed, non-blank name. Enter doesn't save and
    Escape cancels, as observed on D&D Beyond;
  - `CharacterSheetScreen` keeps the name in state, so the header and the
    panel update at once, and reverts it with an error if the save fails.
- **Styles** measured on D&D Beyond: the name row, the 15×14 pencil, and the
  269×32 dark field.
- **Tests:**
  - `CharacterControllerTest` +2 (rename, blank or too long);
  - `CharacterServiceTest` +2 (trims, another player's character is not
    found);
  - `CharacterMenuPanel.test.tsx` +2 (saves on blur only; blank and Escape
    keep the name).
- **Checked live:**
  - the field opens with D&D Beyond's style;
  - after leaving it, the header and the panel show the new name;
  - the save hadn't reached the server when this was written: the tab was
    hidden, and Chrome holds its requests until it's visible again.

Suggested commit: `Rename the character from the character sidebar`

---

## 2026-09-27 — Sidebar fidelity, slice 1: D&D Beyond's sidebar shell

The owner approved `features/sidebar-fidelity-and-customization.md` (ten
slices), with four decisions:
- rules text from 5etools, formatted as D&D Beyond formats it;
- hide and lock controls only;
- HP switches to Max HP Modifier + Override;
- defenses stay display only.

- **Research (D&D Beyond):**
  - every sidebar type was observed on the reference character, and a
    manually added resistance on the throwaway character 171527207 (added,
    then removed);
  - the shell's frame, controls, header and Customize parts were measured
    there;
  - findings are in the feature doc.
- **`Sidebar.tsx`** rebuilt:
  - D&D Beyond's border SVG (top, and bottom rotated), a 10px gap, and 3px
    sides in `--theme-accent`; light and dark variants;
  - hide control (`SidebarRightSvg` / `SidebarLeftSvg`): hidden, it shrinks to
    a 25px strip and showing brings the last panel back;
  - lock (`LockSvg` / `UnlockSvg`), remembered in `localStorage`. Unlocked,
    Escape and a click outside hide it; locked, it stays open without the
    hide control;
  - the sheet opens with it hidden. The empty state reads "Select elements on
    the character sheet to display more information about", vertically
    centered.
- **`CharacterSheetScreen`:** a `sidebarHidden` state. Opening a panel shows
  the sidebar. The golden level up arrow also shows while the sidebar is
  hidden.
- **`sidebarParts.tsx`** (new, used from slice 2 on): `SidebarHeader`,
  `Customize`, `EditorBox`, `ValueEditorRow`.
- The old "×" (`.sidebar__close`) is gone.
- **Tests:**
  - `Sidebar.test.tsx` (5): hide, show, Escape / outside click, lock, empty
    state;
  - `sidebarParts.test.tsx` (3).
- **Checked live** by measuring the DOM:
  - controls 8px from the top, pane at 33px, 340×18 border, 10px gap, 3px red
    sides;
  - hiding and showing brings back the AC panel;
  - the character panel is dark.
  - The tab was hidden, so no screenshot.
- **Left for later slices:** PREV / NEXT (slice 5), subheadings (measured when
  first used).

Suggested commit: `Rebuild the sheet sidebar shell after D&D Beyond: frame, hide and lock`

---

## 2026-09-27 — XP starts where the character's level starts

A level 6 character created with XP advancement showed "Current XP Total:
300 (Level 2)" and asked for 300 XP to reach level 2. Per the owner, a
level N character has at least the XP that level starts at (PHB 2014 table).
- `Dnd5eSheet.experiencePointsOrDefault()` returns
  `max(stored, Dnd5eExperience.threshold(level))`. Every existing character
  reads correctly with no data migration: Mez test (level 6, 300 stored)
  now shows 14,000, heading for 23,000.
- `Dnd5eSheetMutator.setExperiencePoints` never stores less than that floor.
  Add and Remove already start from the floored value.
- Web, `ManageExperiencePanel`:
  - the New XP Total preview stops at the API's `currentLevelAt`;
  - "Set level" disables the levels below the current one.
- Replaces the earlier design line "removing XP shows the deficit".
- **Tests:**
  - `Dnd5ePlayStateTest`: the floor on set and remove, and a level 5 sheet
    reads 6,500;
  - `ManageExperiencePanel.test.tsx` (new, 3 tests): adding from the floor,
    the preview floor, and the disabled lower levels.
- Checked live on Mez test, read only.

Suggested commit: `Keep experience points at or above the current level's threshold`

---

## 2026-09-27 — Leveling and appearance, slice 8: live check and three level up fixes

The slice 8 live check found three defects, now fixed, and then passed on a
milestone and an XP character.

- **Builds saved before the spell pools lost their spells** (Vex, Liriel, and
  any desktop 0.1.0 spellcaster). The 2026-09-25 pooling renamed
  `class:<c>:<n>:cantrips|spells|spellbook` to `class:<c>:cantrips…`. Active
  sheets kept the old ids, so a level up showed "0 of 5 cantrips" plus "Answer
  for a choice this build doesn't offer" problems, and it couldn't finish.
  - New `Dnd5eLegacyChoiceIds`, applied in `Dnd5eCharacterBuild`'s
    constructor. It folds the per-level answers into the pooled id, in level
    order. An answer already stored under the pooled id wins.
  - Choices that aren't per-level spell ids are left untouched, so an ASI's
    `["charisma", "charisma"]` stays as it is.
  - It applies to every read (sheet, creation draft, level up draft), so no
    data migration is needed on either edition. A build is saved with the
    pooled ids the next time it's written.
- **Rolled hit points moved to another class's level.**
  - `rolledHitPoints` holds one value per level after the first, in class
    order. Leveling a class that isn't last shifted every later roll by one
    slot: Mez test's Fighter rolls went to Warlock 4 and to the wrong Fighter
    levels. A shifted roll can also exceed the new slot's die and block the
    level up.
  - `withLevelUp` now inserts an empty slot at the new level's own place.
- **The level up follows the build's hit point method** (owner decision).
  - **Fixed:** the average is used, and nothing is asked.
  - **Rolled:** the new level's empty slot makes "Hit Points" pending. The
    page shows the builder's `Dnd5eRolledHitPointsField`, limited to the
    leveled class's rows, with Roll, type-in and Use fixed.
- **Answered choices no longer vanish from the level up page.**
  - `StoredLevelUp` gains `openedChoiceIds`: the choices the new level left
    pending when it started (the grown spell pools, rolled HP, and the level's
    own choices).
  - `forNewLevel` keeps showing them after they're answered, so a pick can be
    reviewed or undone. A draft stored before this reads as an empty set.
- **Live check** (backed up, then restored byte for byte):
  - **Vex** (milestone, fixed HP), Sorcerer 3 → 4 with ASI, a cantrip and a
    spell. Result: level 8, CHA 18, HP 45 → 51 (+1 Draconic Resilience).
  - **Mez test** (XP, rolled HP):
    - Manage XP +22,700 shows the golden arrow, and the Level up button sits
      under Manage experience.
    - Warlock 3 → 4: the roll of 3 gives HP 48 → 53, and the Fighter rolls
      stay in place.
    - After Finish, the arrow disappears and the bar reads 23,000 / 34,000.
  - One test roll ("Hit points, Warlock lvl 4") stays in Mez test's history,
    because roll records are never deleted.
- **Browser automation note:** the earlier "hang on the Keycloak login" was
  a hidden Chrome tab (`visibilityState: hidden`). Chrome throttles its
  timers and rendering, so screenshots and scripts time out. The app wasn't
  at fault.
- **Tests:**
  - `Dnd5eCharacterBuildTest` +3: folding in level order, pooled wins,
    pooled untouched.
  - `Dnd5eLevelUpTest` +3: legacy spells carry over, rolled slots per class,
    and fixed HP asks nothing.
  - `LevelUpPlanTest` +1: opened choices stay shown.
  - `Dnd5eLevelUpPage.test.tsx` (new): the rolled field shows only the leveled
    class's rows.

Suggested commit: `Fix level up for pre-pooling spell ids and rolled hit points, and keep answered choices on the page`

---

## 2026-09-27 — Leveling and appearance, slice 8: level up page (built; live check pending)

Owner request: a level up page, reached from the sidebar's Level up button
and the golden arrow (`features/leveling-and-appearance.md`, slice 8).
- **Route:** `/characters/:id/level-up` (`AppShell.LevelUpRoute`). The page
  is picked by game system in `levelup/levelUpPages.ts`.
- **API client:** `levelup/levelUpApi.ts`. `getLevelUp` returns null when no
  level up is in progress.
- **`Dnd5eLevelUpPage`**, in the builder's look (DDB Red):
  - with no level up in progress, it offers "Class N → N+1" for each class
    the character has, and a select for a new class (multiclass);
  - once started, it shows what the level grants (the progression row's
    features) and the max HP before → after;
  - the choices use the builder's own `choiceRenderer`, now exported from
    `Dnd5eStepPage`, with a 400 ms autosave;
  - Finish leveling up is enabled once nothing is pending; Change class and
    Cancel sit beside it.
- **Tests:** two integration tests in `CharacterDraftServiceTest` (real
  Postgres):
  - a finished level up adds the level (max HP 12 → 20, current HP
    +8), keeps XP and the sheet theme, and clears the draft;
  - a cancelled level up leaves the sheet as it was.
- Web typecheck passes.

**Not verified live yet.** The browser automation hung on the Keycloak login
page (the local session had expired). Liriel was backed up before the
attempt and restored afterwards; she is untouched at level 5.

---

## 2026-09-27 — Leveling and appearance, slice 7: level up backend

Owner request: leveling up opens its own page, not the builder
(`features/leveling-and-appearance.md`, slice 7).
- **Migration V9:** `characters.level_up_draft jsonb`
  (`{classSlug, classLevel, build}`).
- **Seam (`CharacterCreationFlow`):**
  - `buildOf(sheetJson)` reads the build a sheet came from;
  - `withLevelUp(buildJson, classSlug, catalogue)` returns a `LevelUp`: the
    build with one more level in a class it has, or a new class at level 1,
    and the class level reached.
  - `LevelUpNotAllowedException` covers level 20 and an unknown class. The
    D&D 5e flow implements both methods.
- **`CharacterLevelUpService`** (generic):
  - **start:** refused when the character has no build, or when the new
    level brings problems the build didn't have (unmet multiclass
    prerequisites, while that preference is on).
  - **get, save, cancel.**
  - **finish:** re-materializes the sheet, keeping play state; raises current
    HP by the maximum gained; clears the draft.
  - The plan is cut to the new level: the choices filed under the leveled
    class at its new level, plus any pending one, with their ancestors and
    descendants.
- **API:**
  - `POST`, `GET`, `PUT` and `DELETE /api/characters/{id}/level-up`, and
    `POST …/level-up/finish`;
  - `LevelUpResponse` carries the plan and preview in the builder's shape,
    plus max HP before and after;
  - 409 when no level up is in progress, or when it isn't allowed.
- **Tests:**
  - `Dnd5eLevelUpTest` (9): an existing class, the subclass level, an ASI
    level, a spellcaster's new prepared spells, a new multiclass, unmet
    prerequisites, level 20, an unknown class, and reading the build;
  - `LevelUpPlanTest` (2).

Verified: the level up tests pass. The end-to-end flow is checked live in
slice 8.

---

## 2026-09-27 — Leveling and appearance, slice 6: sheet themes

Owner request: players pick a theme per character from the sidebar
(`features/leveling-and-appearance.md`, slice 6).
- **API:**
  - `Dnd5eAppearance.THEMES` lists D&D Beyond's 14 sheet themes;
  - `SheetMutator.setSheetTheme` sets the theme, and
    `InvalidSheetThemeException` gives 400 for an unknown one;
  - `PUT /api/characters/{id}/appearance` with `{theme}`;
  - `VitalsZone` and the sheet response gain `sheetTheme`, `ddb-red` by
    default.
- **Web:**
  - `themes.css` has a `:root[data-sheet-theme]` block per theme;
  - new `--theme-accent-strong` (Cleric Silver keeps `#2B69AB` for solid
    fills);
  - `useSheetTheme` applies the theme only while a sheet is open;
  - `sheetThemes.ts` holds the list;
  - `ChangeAppearancePanel` shows the current theme and a grid of 14 tiles,
    each a `ThemeSample` ability box ("Dexterity +5 / 20") in its color.
    Picking one applies it at once;
  - the character sidebar's "Change sheet appearance" opens the panel.
- **Tests:**
  - three API cases in the renamed `Dnd5ePlayStateTest` (default, a valid
    theme, an unknown one);
  - `ChangeAppearancePanel.test.tsx` (the panel and the hook).

Verified live on Vex:
- the panel shows the 14 tiles;
- picking Wizard Cobalt turned frames, chips, tabs, the header buttons and
  the dice button blue at once;
- leaving the sheet cleared the theme, so the builder stays red.
Vex was restored to the default afterwards. 163 web tests pass.

Left out: portraits, frames, backdrops and the Underdark mode, as planned.

---

## 2026-09-27 — Leveling and appearance, slice 5: character sidebar, XP bar, Manage XP, golden arrow

Owner request: a character sidebar opened from the portrait or the name, a
header XP bar, a Manage experience panel and a golden level-up arrow
(`features/leveling-and-appearance.md`, slice 5). Measured live on D&D
Beyond's Seyrie: the header bar, the character manage pane and the Manage XP
panel.
- **API:** `Experience` gains `classes` (name, subclass, level) for the
  sidebar's class rows.
- **`SheetShell`** gains generic hooks:
  - `progress`, drawn under the level line;
  - `onOpenCharacter`, which makes the portrait (a button) and the name open
    the sidebar;
  - `portraitBadge`.
- **`XpBar`** has three variants: `header` (225px), `menu` (284px) and
  `panel`, with a marker and the threshold at each end.
  - The labels use 10px Roboto Condensed.
  - The track is 1px `#5F6473`, filled in the theme color.
  - The points below are 10px `#A2ACB2`, in en-US format.
- **`CharacterMenuPanel`** (`sidebar--dark`):
  - on a gradient: a 130px portrait with a 3px theme border, a 24px name,
    the summary facts after the first, the XP bar (XP builds) and the class
    rows (level badge, name, subclass);
  - the MY CHARACTER menu: Manage experience, with a filled Level up button
    right under it once a level is available (XP builds), or Level up (milestone
    builds);
  - the PLAY menu: Game log, Short rest and Long rest.
- **`ManageExperiencePanel`:**
  - Current XP Total, the bar, Set level (to that level's threshold), Set
    XP, the Add/Remove tabs and a live "New XP Total (Level N)";
  - Apply and Cancel, shown only when the total changes;
  - Apply posts `{set}` and returns to the character sidebar.
- **Golden arrow:** a gold (`#D9A441`) up-arrow on the portrait while
  `levelUpAvailable` and the sidebar isn't open. It opens the sidebar.
- **Level up** navigates to `/characters/{id}/level-up`. The page arrives in
  slice 8.
- **Tests:** `CharacterMenuPanel.test.tsx` (6: XP and milestone menus, class
  rows, the bar helpers).

Verified live on Vex:
- as a milestone build, the menu shows Level up;
- switched to XP for the test, the header and sidebar bars appear, Manage XP
  previews and applies 34,000 XP, the Level up button appears, and the golden
  arrow shows once the sidebar closes.
Vex was restored afterwards: MILESTONE, no XP or death-save fields. 161 web
tests pass.

Left out:
- class icons in the class rows (we have no class art);
- renaming from the sidebar, Character settings and Manage character &
  levels;
- whether D&D Beyond's Manage XP has an Apply button (the browser stopped
  responding while checking; ours has one, as planned).

---

## 2026-09-27 — Leveling and appearance, slice 4: experience points backend

Owner request: XP following the 5e table
(`features/leveling-and-appearance.md`, slice 4).
- **`Dnd5eExperience`:** the PHB 2014 table (`THRESHOLDS`),
  `threshold(level)`, `levelFor(points)` and `levelUpAvailable`.
- **View:**
  - `VitalsZone` gains `Experience`, and the sheet response gains
    `experience`.
  - It holds `advancement`, `points`, `level`, `levelFromPoints`,
    `currentLevelAt`, `nextLevelAt` (null at 20) and the thresholds.
  - `levelUpAvailable` is only true for XP builds, and it drives the golden
    arrow.
  - `canLevelUp` is true for any build below level 20, and it drives the
    Level up button.
- **Mutation:** `SheetMutator.setExperiencePoints`, floored at 0; the level
  never changes there.
- **API:** `POST /api/characters/{id}/experience` takes exactly one of `add`,
  `remove` or `set` (400 otherwise). Removing never goes below 0.
- **Tests:**
  - `Dnd5eExperienceTest`, a parameterized test over every PHB threshold;
  - `ExperienceRequestTest`;
  - two sheet-level cases in `Dnd5eDeathSavesTest`;
  - two controller tests.

Verified: the D&D 5e and character test suites pass.

---

## 2026-09-27 — Leveling and appearance, slice 3: death saves

Owner request: death saving throws as on D&D Beyond
(`features/leveling-and-appearance.md`, slice 3).
- **Sheet payload (`Dnd5eSheet`)** gains three nullable play-state fields at
  once, so later slices don't repeat the edit:
  - `deathSaves`, `experiencePoints` and `appearance`, with `…OrDefault`
    accessors;
  - a constructor with the old signature keeps existing callers;
  - `withPlayStateOf` keeps all three when a build is re-materialized;
  - new records `Dnd5eDeathSaves` and `Dnd5eAppearance`.
- **Rules (`Dnd5eSheetMutator`, PHB 2014):**
  - dropping to 0 starts fresh counts;
  - damage at 0 is one failure, two when the request says `critical`;
  - overflow damage of at least the hit point maximum kills outright;
  - a stable character who takes damage starts dying again;
  - rolls: 10 or more succeeds, a natural 1 is two failures, a natural 20 is
    back at 1 HP.
  - Counts only exist at 0 HP: every write clears them once HP is above 0,
    so healing, long rests and hit dice all reset them.
- **Seam:**
  - `SheetMutator` gains `setDeathSaves`, `clearDeathSaves`,
    `applyDeathSaveRoll` and a `critical` overload of `applyDamage`;
  - `VitalsZone` gains a generic `DeathSaves` (`dying`, `stable`, `dead`).
- **API:**
  - `PUT` and `DELETE /api/characters/{id}/death-saves`;
  - `POST …/death-saves/roll`: the d20 is logged through
    `RollService.recordRoll`, and a character who isn't dying gets 409
    (`NotDyingException`);
  - damage accepts `critical`;
  - the sheet response carries `deathSaves`.
- **Web:** `DeathSavesBox` replaces the hit points box while
  `deathSaves.dying`, in the same frame and size, with the skull (the
  Unconscious icon) and the measured positions and colors.
  - Clicking a circle fills or empties it.
  - Clicking the skull rolls.
  - Clicking the rest of the box opens HP management.
- **Tests:**
  - `Dnd5eDeathSavesTest` (12, from the PHB rules);
  - controller tests for critical damage, the counts, the 400 and the 409;
  - `DeathSavesBox.test.tsx` (4).

Verified live on Vex:
- 100 damage killed outright;
- clicking circles set the counts;
- healing brought the hit points box back.
Vex was restored to 45/45 afterwards. No death save was rolled live, because
rolls stay in the history for good. The roll is covered by the tests.

Left out:
- D&D Beyond's dead and stable looks (not measured without changing Seyrie);
- a way to flag a critical hit in the web damage controls (the API supports
  it).

---

## 2026-09-27 — Leveling and appearance, slice 2: dark theme for the home

Owner request: a dark theme for the landing page and the character list,
switched from the user menu (`features/leveling-and-appearance.md`, slice 2).
- **`theme/appTheme.tsx`:**
  - `AppThemeProvider` (wrapped in `main.tsx`) and `useAppTheme()`;
  - the choice is kept in `localStorage` (`osv.app-theme`), and follows
    `prefers-color-scheme` until the player picks. All storage access is
    guarded.
- **`theme/AppPage.tsx`:** the page root with `data-app-theme`. The landing,
  home, credits and create-character pages use it.
- **`theme.css`:**
  - the direction A values under `[data-app-theme='dark']`;
  - new tokens `--app-danger-soft`, `--app-placeholder-bg` and
    `--app-placeholder-ink`, replacing hard-coded colors in `home.css`;
  - the "Dark theme" switch style;
  - gold links on the dark credits page.
- **`AppHeader`:** a "Dark theme" `menuitemcheckbox` in the user menu, in
  both editions.
- **Tests:** `appTheme.test.tsx` (3) and an `AppHeader` toggle test.

Verified: typecheck and tests pass. Live on the home: the theme switches both
ways, it is remembered, and it followed the browser's dark mode at first. The
banner menu and the user menu are dark too. The sheet and the builder are
unchanged.

Left out: checking the desktop edition, which moves to slice 9 with the
0.2.0 package.

---

## 2026-09-27 — Class features grouped by class, subclass shown under its feature

Owner request: the Features & Traits tab groups class features by class only
("Fighter"), not by class and level ("Fighter 1", "Battle Master 3"). The
feature text already says the level. The chosen subclass sits under the
feature that grants it, in the indented `feature-snippet__extra` block, as on
D&D Beyond (checked on character 55717755: Martial Archetype → Champion).
- **Planner (`Dnd5eBuildPlanner`):**
  - class features, subclass features and features chosen through them
    (Fighting Style: Defense, maneuvers, metamagic) take the class name as
    `source`;
  - choice labels and grant provenance keep the level ("Fighter 1").
  - The subclass is recorded as a choice of the feature that grants it
    (`Dnd5eBuildOutcome.addFeatureChoice`).
- **Sheet payload:**
  - `Dnd5eFeatureTrait` gains `choices` (null on older sheets);
  - `FeatureTrait` and `FeatureTraitResponse` pass it through.
- **Migration V8** (data only) rewrites stored sheets. Dry-run on the dev
  database, then applied:
  - no class feature keeps a level in its source;
  - Martial Archetype → Battle Master, Arcane Tradition → School of
    Evocation, Roguish Archetype → Thief, Sorcerous Origin → Draconic
    Bloodline.
- **Web:** `FeatureTraitRow` lists `choices` in a `feature-snippet__extra`
  block (12px, black, as measured on D&D Beyond).
- **Tests:** a materializer test for the grouping and the subclass choice,
  and a `FeaturesTab` test for the choice rendering.

Verified: `:apps:api:check` green, the web tests pass, and a live check on
Aria Emberfall.

Left out: D&D Beyond also nests other choices under their feature (Fighting
Style → Dueling). Ours still lists "Fighting Style: Defense" as its own
feature. Level-up's migration moves to V9.

---

## 2026-09-27 — Leveling and appearance, slice 1: DDB Red everywhere

Owner request: make DDB Red the D&D 5e default in the sheet, the builder and
the dice tray (`features/leveling-and-appearance.md`, slice 1).
- **New `systems/dnd5e/themes.css`:** `--theme-accent: #C53131`. It is
  imported by the sheet, the builder and the system picker.
- **Sheet (`frames.css`):**
  - `--frame-ink`, `--accent-control` and `--accent-control-saturated` now
    come from `--theme-accent`;
  - the temp HP box, the portrait, the header buttons and the rest/game-log
    icons use it too.
  - New `--ink-control` (`#4A5D6B`) covers the dark ink that D&D Beyond
    doesn't theme: the condition toggle, spell and attunement markers, and
    the HP button hover borders.
  - Proficiency dots (saves and skills) are always `#383838`, under every
    theme (owner request).
  - Manage Feats, Manage Spells and Manage Inventory are outlined theme
    buttons: text and border in the accent (owner request). Like the sidebar
    action buttons, they no longer change color on hover, as on D&D Beyond.
  - **Cast buttons** are filled theme buttons (owner request): white text on
    the accent. On hover they turn `--theme-accent-dark` with an inset
    `--theme-accent-darker` shadow, which is D&D Beyond's `#9C2727` and
    `#731D1D` for DDB Red. Both are mixed from the accent in `themes.css`.
- **Dice tray:** the dice button and the Roll button follow the theme.
- **Builder:**
  - `--builder-ink`, `--builder-chip-on` and `--builder-pending` use the
    theme accent;
  - pending tints and focus rings come from `color-mix`;
  - `builder_section_frame.svg` is drawn in `#C53131`.
- **Scan of D&D Beyond:** the other tabs, the hovers and the builder,
  recorded in the feature doc.
  - D&D Beyond's builder isn't themed. Ours is red by the owner's decision.
  - The other hovers stay as they were; the owner checked them against D&D
    Beyond and chose to keep them.
- **Docs:** `ui-design-tokens.md` and `ui-design-system.md`.

Verified: `npm run build` and `npm test` pass (146 tests). A live look at the
sheet showed red frames, chips and headers. No backend change.

Left out: the other 13 themes and the per-character choice (slice 6).

---

## 2026-09-27 — Plan revised: DDB Red default, level up arrow, home dark theme, death saves

The owner decided:
- **DDB Red** is the D&D 5e default, for the sheet, the builder and the level
  up page, with the blue-gray becoming "Cleric Silver".
- **Leveling up** is a Level up button under Manage experience, plus a golden
  up-arrow by the portrait while a level up is pending, with no automatic
  redirect.

They also added:
- a dark theme for the home and character list, toggled from the user menu;
- death saving throws in D&D Beyond's style.

`features/leveling-and-appearance.md` was rewritten. It now has:
- a scan of where D&D Beyond applies the theme color (Seyrie, DDB Red);
- the death saves box as observed at 0 HP, and the PHB death save rules;
- nine slices.

No code has changed yet.

---

## 2026-09-27 — Plan: experience, level up page and sheet themes (proposed)

Owner request: XP advancement, a level up page instead of the builder, a
character sidebar with Manage experience and Change sheet appearance, a thin
XP bar in the header, and all of it in the desktop edition afterwards.
- **New:** `features/leveling-and-appearance.md`. It records:
  - what D&D Beyond does, observed on characters 48929054 and 50149479,
    without changing either;
  - the 14 theme colors read from D&D Beyond's page. Our current look is its
    "Cleric Silver" (`#92a2b3`);
  - the 2014 PHB XP table;
  - the design and six slices;
  - two decisions for the owner.
- No code has changed yet.

---

## 2026-09-27 — Desktop edition, slice 3: launcher, Windows package and user guide

Owner request: build the launcher and the package, and write a support guide
on using the desktop edition.
- **New subproject `:apps:desktop`** (`settings.gradle`):
  - `DesktopLauncher`, a Swing `LauncherWindow` ("Open the vault" and
    "Quit"), `SingleInstance`, `PortChooser` and `AppFolders`;
  - 7 tests.
- **Packaging:** `:apps:desktop:packageDesktop` builds the desktop web app,
  stages the jars (Windows PostgreSQL binaries only), the web app and
  `content/`, and runs `jpackage --type app-image`. The result is
  `build/distributions/OmniSheetVault-0.1.0-windows.zip` (147 MB).
  - It is an app image rather than an installer, because an installer needs
    WiX, which isn't installed.
- **API:** the desktop profile writes its log to
  `<data>/logs/omni-sheet-vault.log`.
- **Found live and fixed:** the launcher first passed the folders and port as
  Spring default properties. `application-desktop.yml` outranked them, so the
  first real run imported the repository's `content/` instead of the bundled
  one, and would have found no web app on a friend's machine. They are now
  command-line arguments, covered by `DesktopLauncherTest`.
- **Checked live**, on a simulated friend's machine (extracted zip, empty
  `LOCALAPPDATA`, another working folder):
  - ready in 24 s on the first start, with 3,101 entries imported from the
    bundle;
  - the vault opens as the Windows user;
  - a second launch opens the running one;
  - closing the window stops everything, leaving no orphan database or lock
    files.
- **Guide:** `apps/desktop/README.md`. For the owner it covers building,
  trying, sharing and releasing. For players it covers requirements,
  installing (including SmartScreen), everyday use, where data lives,
  backup, updating, uninstalling and troubleshooting.
- **Docs:**
  - `CLAUDE.md` (layout, commands) and `architecture.md`;
  - `tech-stack.md` (`jpackage`);
  - `features/desktop-edition.md`.
  - CI also runs `:apps:desktop:test`.
- **Vite dev server:** `vite.config.ts` now ignores `dist/` and
  `dist-desktop/`. Packaging rewrote `dist-desktop` while the dev server was
  watching it, and the watcher crashed with `EBUSY`.
- **Left out:**
  - a custom `.exe` icon (needs an `.ico`);
  - an installer (needs WiX);
  - a run on a truly clean Windows user (slice 4).

Suggested commit: `Add the desktop launcher, Windows package and user guide`

---

## 2026-09-27 — Desktop edition, slice 2: the API serves the web app

- **Web:**
  - `src/edition.ts` (`isDesktopEdition`, from Vite's build mode) and
    `npm run build:desktop`, which writes `dist-desktop/` (gitignored).
  - In the desktop build the user menu drops "Account" and "Log out".
  - Test: `AppHeader.test.tsx`.
- **API:** `desktop.DesktopWebAppConfig` serves the built web app from
  `app.desktop.web-directory`.
  - Hashed assets are cached for a year; `index.html` is `no-cache`.
  - `/` is forwarded to `index.html`, and so is any path the router owns. An
    unknown API path stays 404.
  - 3 more cases in `DesktopEditionTest`.
- **Checks:**
  - all web tests (146) pass;
  - the desktop tests pass, and `:apps:api:check` passes;
  - live with `java -jar` in the desktop profile:
    - it opens straight to the home as the Windows user;
    - the menu has no account items;
    - a draft was created, and a portrait uploaded and shown from disk;
    - a reload on a deep link works;
    - autosave works, so the CSRF token holds;
    - the catalogue shows the full text.
- **Found, not fixed:** characters created directly through
  `POST /api/characters` can't render (`Dnd5eSheet.items()` null). It is a
  server-edition issue too, and the web app never calls that endpoint. It is
  recorded in `open-items.md`.

Suggested commit: `Serve the web app from the API in the desktop edition`

---

## 2026-09-27 — Desktop edition, slice 1: the backend's `desktop` profile

The owner approved the plan and the embedded PostgreSQL dependency.
`features/desktop-edition.md` has the full detail.
- **Dependency:** `io.zonky.test:embedded-postgres` 2.2.2, with binaries BOM
  17.11.0, recorded in `tech-stack.md`.
- **New:**
  - `desktop.DesktopProperties` and `desktop.EmbeddedDatabaseConfig`. The
    latter stops a PostgreSQL left running by a killed app before starting.
  - `shared.DesktopSecurityConfig`, `LocalPlayerSecurityContextRepository`
    and `LocalHostOnlyFilter`;
  - `storage.FileSystemPortraitStorage` and `LocalPortraitFileController`;
  - `catalogue.DesktopCatalogueBootstrap`;
  - `application-desktop.yml`.
- **Changed:** `SecurityConfig` and `S3PortraitStorage` are now
  `@Profile("!desktop")`. The test JVM's `java.io.tmpdir` is `build/tmp/test`.
- **Found and fixed on the way:**
  - A per-request login filter made Spring rotate the CSRF token on every
    request, so the cookie was cleared from the second request on. It was
    replaced by a `SecurityContextRepository`.
  - Spring's text-to-`Path` conversion rejects relative paths, so the folder
    properties are now bound as text.
  - Stopping the JVM by force leaves `postgres.exe` running; the next start
    now stops it.
- **Tests:**
  - `DesktopEditionTest`, 8 cases;
  - `EmbeddedDatabaseRestartTest`, 2 cases: data survives a restart, and an
    orphan server is stopped;
  - `FileSystemPortraitStorageTest`, 2 cases.
- **Checks:** `:apps:api:check` passes. Live, with the full content:
  - first start about 28 s, including a 10 s import of 3,101 entries;
  - a restart about 5 s, recovering from an orphan database;
  - over HTTP: the local player, full catalogue text, and the CSRF token kept
    across requests;
  - only `127.0.0.1:8095` listens, and another Host gets 403.
- **Architecture:** a new "Editions" section in `architecture.md`.

Suggested commit: `Add the desktop edition's backend profile: embedded PostgreSQL, local player, portraits on disk`

---

## 2026-09-27 — Desktop edition plan (proposed); Oracle plan dropped

Owner decision: no hosting. Friends run the vault on their own Windows
notebooks, without Docker.
- **New:** `features/desktop-edition.md`. It plans:
  - a `desktop` Spring profile with new classes only: embedded PostgreSQL, a
    local player with no login, portraits on disk, and the automatic catalogue
    import;
  - the web app served by the API;
  - a `jpackage` Windows installer with its own Java runtime, built by a new
    `:apps:desktop` subproject.
  - It needs one new dependency, `embedded-postgres`, which waits for the
    owner's approval.
- **Changed:** `features/oracle-deployment.md` is marked as dropped.
- No code has changed yet.

---

## 2026-09-26 — Oracle Cloud deployment plan (proposed)

Owner request: host the vault on Oracle Cloud Always Free, as optimized and
secure as possible.
- **New:** `features/oracle-deployment.md`. It holds:
  - the target architecture: Caddy with TLS, headers and rate limits in front
    of a single origin, and an internal-only Docker network;
  - four decisions for the owner: domain, redaction, existing data, and the
    account type;
  - five phases: the account, the production setup in the repository, the VM
    hardening, the first deployment, and verification.
- No code has changed yet; it waits for the owner's approval.

---

## 2026-09-26 — Bearer and the public web client removed (ADR-0008, stage 4)

The last stage. The API is now only the web app's backend-for-frontend.
- **`SecurityConfig`:**
  - `oauth2ResourceServer` removed, so no Bearer header is read;
  - CORS removed;
  - CSRF uses Spring's default matcher, so every write needs the token.
  - The resource-server starter stays for the `JwtDecoder` that
    `SessionTokenAuthenticationFilter` uses.
- **`application.yml`:** `cors.allowed-origins` removed.
- **Keycloak:** the public `omni-sheet-vault-web` client was removed from
  `realm-export.json` and deleted from the running realm with `kcadm.sh`.
- **Tests:**
  - `SessionAuthenticationTest`: `aBearerHeaderIsNotAWayIn` and
    `everyWriteNeedsTheCsrfTokenEvenWithoutASessionCookie`.
  - `CharacterPortraitControllerTest`: `requiresAToken` became
    `requiresASignedInPlayer`, which sends the CSRF token to still check the
    401.
  - The other controller tests are unchanged: spring-security-test's `jwt()`
    marks its request to skip CSRF.
- **Checks:** all API tests pass. Checked live:
  - a Bearer header gets 401;
  - a POST from another site gets 401;
  - login, then home as "test" with 5 characters;
  - a write without CSRF gets 403, and with CSRF it passes.
- **Docs:**
  - `ground-rules.md`, `architecture.md` and `tech-stack.md`;
  - the ADR, now marked implemented;
  - `open-items.md` (CSP and a same-origin HTTPS proxy before going public);
  - `features/private-sharing.md`.

Suggested commit: `Remove Bearer auth and the public web client (ADR-0008 stage 4)`

---

## 2026-09-26 — The web app switches to the session cookie (ADR-0008, stage 3)

Stage 3 of 4. The browser no longer holds any token.
- **Removed:**
  - the `oidc-client-ts` dependency (`npm uninstall`);
  - `auth/oidcConfig.ts`, `auth/Callback.tsx` and its test;
  - the `/callback` handling in `App.tsx`;
  - `VITE_KEYCLOAK_AUTHORITY`, `VITE_KEYCLOAK_CLIENT_ID` and `VITE_API_URL`.
- **`auth/session.ts`:** `redirectToLogin` and `redirectToRegistration` go to
  the API's `/oauth2/authorization/keycloak?returnTo=` (plus `&signup`).
  `signOutOfSession` does a form POST to `/logout` with `_csrf`. `csrfToken`
  and `loginUrl` are new.
- **`AuthProvider`:** asks `GET /api/me`; `user` is now that player
  (`CurrentUser`). `AppShell` no longer fetches `/api/me` itself.
- **`apiFetch`:** same-origin, with `X-XSRF-TOKEN` on writes. A 401 goes to
  the login.
- **`AppHeader`:** the "Account" link reads `VITE_KEYCLOAK_ACCOUNT_URL`, which
  defaults to the local account page.
- **Credits:** `oidc-client-ts` removed and Spring Session added, in
  `NOTICE.md` and `CreditsPage`.
- **Tests:**
  - `session.test.ts`: return paths, the login URL and the CSRF cookie;
  - `client.test.ts`: same origin, no `Authorization`, the CSRF header on
    writes, and 401 going to the login;
  - the `oidcConfig` mocks were removed from the page tests.
- **Checks:** the type-check and all 144 web tests pass. Checked live:
  - landing, login, home as "test";
  - "Create character" created a draft and the builder loaded;
  - the draft was deleted (204) and the list is back to 5;
  - "Log out" returned to the landing, and `/api/me` then got 401.
- **Docs:** `ground-rules.md` (Frontend), `architecture.md` (Request flow),
  `tech-stack.md`, `open-items.md` and `features/private-sharing.md`.

Suggested commit: `Switch the web app to the session cookie and drop oidc-client-ts (ADR-0008 stage 3)`

---

## 2026-09-26 — CSRF cookie, return path, sign-up and logout (ADR-0008, stage 2)

Stage 2 of 4. The SPA is untouched and still uses Bearer.
- **API:**
  - `CsrfCookieFilter` issues the `XSRF-TOKEN` cookie.
  - `BrowserLoginRequestResolver` keeps PKCE, remembers `?returnTo=` and turns
    `?signup` into `prompt=create`.
  - `LoginReturnPath` accepts same-app paths only; the login success handler
    redirects to the remembered path.
  - `POST /logout` goes through `KeycloakLogoutSuccessHandler` and ends the
    Keycloak session too.
- **Config:** `app.auth.end-session-uri` in `application.yml`.
- **Tests:**
  - `SessionAuthenticationTest`, 5 more cases: return path and sign-up, an
    outside return path, the CSRF cookie, logout, and logout without CSRF.
  - `LoginReturnPathTest`.
- **Checks:** the security tests pass. Checked live:
  - `returnTo` works;
  - the CSRF cookie arrives, and the header is required;
  - logout ends both sessions and returns to `/`;
  - `?signup` opens the registration page.
- **Docs:** `architecture.md` and `features/private-sharing.md`.

Suggested commit: `Add CSRF cookie, return path, sign-up and logout to the session login (ADR-0008 stage 2)`

---

## 2026-09-26 — Session login in the API (ADR-0008, stage 1)

Owner request: move the tokens out of the browser behind an `HttpOnly`
cookie. ADR-0008 is now Accepted. This is stage 1 of 4, and the SPA still
works as before.
- **Dependencies:** `spring-boot-starter-oauth2-client` and
  `spring-boot-starter-session-jdbc`, recorded in `tech-stack.md`.
- **Migration V7:** `spring_session` and `spring_session_attributes`,
  documented in `database-schema.md`.
- **`SecurityConfig`:**
  - `oauth2Login` with PKCE and the new confidential `omni-sheet-vault-bff`
    client;
  - tokens kept in the session (`HttpSessionOAuth2AuthorizedClientRepository`),
    with an authorized-client manager that renews them;
  - the new `SessionTokenAuthenticationFilter` gives controllers the same
    `JwtAuthenticationToken` a Bearer request gets;
  - a cookie CSRF token, required when a request carries a session;
  - a 401 instead of a login redirect, and no saved requests.
- **Config:** Keycloak endpoints are listed in `application.yml` (the API
  starts without Keycloak). The session cookie is `HttpOnly`, `SameSite=Lax`
  and `Secure`, except in the dev profile, with a 30-minute timeout.
  `KEYCLOAK_BFF_CLIENT_SECRET` was added to `.env.example`.
- **Keycloak:** the `omni-sheet-vault-bff` client is in `realm-export.json`
  without its secret. It was created in the running realm with a generated
  secret, which is written only to `.env`.
- **Vite:** proxies `/api`, `/oauth2`, `/login/oauth2` and `/logout` to 8090,
  keeping the Host header.
- **Docs:** `ground-rules.md` (Security), `architecture.md` (Request flow),
  the ADR, and `features/private-sharing.md`.
- **Tests:** `SessionAuthenticationTest`:
  - a session login reaches controllers as a `Jwt`;
  - a revoked session gets a 401;
  - a cookie write needs CSRF, a Bearer write doesn't;
  - an anonymous request gets a 401;
  - the login redirect uses the BFF client and PKCE.
- **Test config:** `src/test/resources/application.properties` turns off
  Spring Session's cleanup job. Otherwise it keeps polling the stopped
  Testcontainers databases of cached test contexts.
- **Checks:** `:apps:api:check` passes from scratch (`--rerun-tasks`). Checked
  live:
  - logging in through `localhost:5173` works;
  - `/api/me` answers 200 with the cookie alone;
  - the cookie is hidden from JavaScript;
  - a POST without CSRF gets 403;
  - the session is stored in Postgres.
- **Left for stage 2:** issuing the CSRF cookie, `POST /logout`, the return
  path, and `prompt=create`.

Suggested commit: `Add a session-cookie login to the API (ADR-0008 stage 1)`

---

## 2026-09-26 — Create an account through Keycloak (private sharing, slice 2)

Owner request: a "New in the vault? Create an account" entry next to the
login, handled by Keycloak, with some protection against bots creating
accounts in bulk.
- **Keycloak realm (`realm-export.json`, and the running realm via
  `kcadm.sh`):**
  - `registrationAllowed: true`;
  - password policy `length(8) and maxLength(128) and notUsername and notEmail`;
  - `bruteForceProtected: true`, with `failureFactor: 10`.
- **Login theme:** the messages "New in the vault?" / "Create an account",
  "Create your vault account" and "« Back to sign in".
- **Web:**
  - `redirectToRegistration` sends `prompt=create`;
  - `useAuth().signUp`;
  - a "Create account" button on the landing page.
- **Tests:** a "Create account" case in `LandingPage.test.tsx`.
- **Checks:**
  - all 144 web tests and the type-check pass;
  - live: the landing's button opens Keycloak's registration page;
  - live: the login page shows "New in the vault? Create an account".
- **Left out:**
  - email verification (no SMTP);
  - reCAPTCHA (needs owner keys);
  - a registration rate limit (belongs at the proxy once it's online).
  `open-items.md` lists all three. No account was created during the check.

Suggested commit: `Allow account registration through Keycloak`

---

## 2026-09-26 — License, credits footer and Credits page (private sharing, slice 1)

Owner request: the project is a non-commercial fan project under a license
that says so, with technical, graphic and content credits in the footer
(`features/private-sharing.md`, slice 1).
- **License:** `LICENSE` holds the PolyForm Noncommercial License 1.0.0, the
  license the owner picked. Its header says third-party material keeps its
  owners' terms.
- **Notice:** `NOTICE.md` lists the fan content notice, the game content
  (5etools, 2014 books), the graphics (D&D Beyond art, game-icons.net icons by
  Lorc, Skoll, Sbed and Daniel Zaitzev under CC BY 3.0, Flaticon icons by
  meaicon and Magnific), the fonts and the software licenses.
- **Web:** a shared `AppFooter` on every page with the fan content notice and a
  "Credits and licenses" link. The public `/credits` route shows
  `CreditsPage`, which mirrors `NOTICE.md`. The footer styles moved from
  `home.css` to `theme.css`.
- **Tests:** `CreditsPage.test.tsx`, and a footer case in
  `LandingPage.test.tsx`.
- **Checks:** all 143 web tests and the type-check pass. Checked `/credits`
  live while signed out.
- **Left out:** the school-of-magic symbols' source is still unconfirmed.
  `NOTICE.md` says so, and the owner should confirm it.

Suggested commit: `Add the PolyForm Noncommercial license, NOTICE and a credits footer`

---

## 2026-09-26 — Hit points rows drop the rolled-die display

Owner request: a hit point roll is a single die, so showing it next to the
field that already holds the result is redundant. The ability rolls, which
drop a die, keep theirs.
- **Web:** `Dnd5eRolledHitPointsField` no longer keeps or shows the last roll.
  A roll writes its total straight into the row's field, and the row has four
  columns (level, value, Roll dX, Use fixed).
- **Test:** `Dnd5eRolledHitPointsField.test.tsx`: a roll writes the value and
  shows no dice.
- **Checks:** all 141 web tests and the type-check pass. Checked live on "Mez
  test" (read-only).

Suggested commit: `Drop the rolled-die display from the hit point rows`

---

## 2026-09-26 — Rolled hit points per class, "Warlock lvl 2" labels

Owner request: in a multiclass build, each class shows its own hit point
section, and rows read "Warlock lvl 2" instead of "Warlock 2".
- **Backend:**
  - each `ROLLED_HIT_POINTS` option carries its class key as `data.group`;
  - the labels, roll labels ("Hit points, Warlock lvl 2 (d8)") and range
    problems use "<Class> lvl <n>";
  - rolls already in the history keep their old label.
- **Web:**
  - `Dnd5eRolledHitPointsField` takes an optional `group` and shows only that
    class's rows, writing each value at its place in `rolledHitPoints`. Its
    totals cover the shown rows.
  - `Dnd5eClassLevels` draws "Hit points · <Class>" for the selected tab, after
    its levels and before "Other class choices" and the progression. Its
    unrolled levels count in the tab's and the frame's "left" badges
    (`unrolledHitPointLevels`).
  - The hit points left "Other class choices", which now shows only if some
    other choice has no class.
- **Tests:**
  - `Dnd5eBuildMaterializerTest`: labels, group and the problem text;
  - `Dnd5eRolledHitPointsField.test.tsx`: the group filter and unrolled count;
  - `Dnd5eClassLevels.test.tsx`: the per-class section and the tab count.
- **Checks:** `:apps:api:check` is green (all tests, including
  `S3PortraitStorageTest`), and all 141 web tests and the type-check pass.
  Verified live on "Mez test" (read-only):
  - the Warlock tab shows "Warlock lvl 2–3", since level 1 of the starting
    class is the maximum;
  - the Fighter tab shows "Fighter lvl 1–3";
  - each tab shows its own count.

Suggested commit: `Split rolled hit points by class and label levels as "<Class> lvl <n>"`

---

## 2026-09-26 — Class progression follows the selected class tab

Owner request: with Warlock selected on Mez, both classes' progression panels
showed, which made no sense.
- **Web:** the `ProgressionPanel` moved from the bottom of the Classes step into
  `Dnd5eClassLevels`. It now renders only the selected tab's class and switches
  with the tab.
- **Last on the page (owner follow-up):** "Other class choices" (rolled hit
  points) is passed in as `beforeProgression`, so the progression panel is
  always the page's last section. The order is: Your classes, the class tabs,
  the class's levels, Other class choices, Class progression.
- **Tests:** `Dnd5eClassLevels.test.tsx`:
  - only the selected class's panel shows, and switching tabs switches it;
  - the panel comes after the other sections.
- **Checks:** all 139 web tests and the type-check pass. Verified live on "Mez
  test":
  - the Warlock tab shows only "Warlock (The Fiend)", and the Fighter tab only
    "Fighter (Battle Master)";
  - the panel is the last section, after "Other class choices".

Suggested commit: `Show the class progression of the selected class tab only`

---

## 2026-09-26 — Wear/Wield per line, Review hit points note, "Use fixed" hit points

Fixes the two findings of the live check, plus an owner request.
- **Wear/Wield per line:** `equippedStartingItems` now holds line keys: the
  item slug plus its occurrence among lines of that slug (`dagger#0`,
  `dagger#1`), from `Dnd5eBuildMaterializer.lineKeys`.
  - Two lines of the same item are equipped apart.
  - The preview's inventory entries carry that key.
  - Plain-slug entries from before this change no longer match. Only test
    drafts had any.
- **Review:** the Classes card reads "48 (average for 5 of 5 levels not rolled
  yet)" until every rolled level has a value, then "(rolled)".
- **Rolled hit points (owner request):** each level row has a "Use fixed (N)"
  button that writes the class's fixed value. N is the plan's `average` for
  that die, the same value the Fixed method uses.
- **Tests:**
  - `Dnd5eBuildMaterializerTest`: line keys, two daggers apart;
  - `Dnd5eStartingEquipmentPreviewTest`;
  - `Dnd5eEquipmentStep.test.tsx`: toggling by line key;
  - `Dnd5eRolledHitPointsField.test.tsx`: "Use fixed";
  - `Dnd5eReviewCards.test.tsx`: the hit points note.
- **Checks:**
  - All 137 web tests and the type-check pass.
  - `:apps:api:check`: 498 of 499 tests pass. `S3PortraitStorageTest`
    (untouched since 2026-09-25) fails only inside the full run, with MinIO
    answering 503 while creating its bucket, and passes when run alone. The
    machine was low on memory (3 GB free) and on disk (C: 32 GB free), which
    MinIO is known to refuse writes over. Recorded in `open-items.md`.
  - Not re-verified live.

Suggested commit: `Equip starting items per line, note unrolled hit points on review, add a fixed-value button to rolled hit points`

---

## 2026-09-26 — Live check: Equipment Wear/Wield and Review cards

The API and Vite were restarted. A stale API on 8090, started at 17:16 before
the D2h changes, was stopped first.
- **Equipment on "Mez test":**
  - "Current inventory (11)" lists the fixed Warlock items too (Leather Armor,
    2 daggers).
  - Wear on Leather Armor took the summary's AC from 12 to 13, shown as "Base
    (Leather Armor) + Dexterity modifier".
  - Wield on the Spear added the attack "Spear +3 · 1d6 piercing".
  - Currency read "10 gp".
- **Review on "Mez test":** all six cards showed the right values, the Classes
  card had its "1 left" badge (rolled hit points), and each card had an Edit
  link.
- The test's equipped items were removed from Mez's draft afterwards.
- **Found:**
  - Equipped items are stored by catalogue slug. When two inventory lines share
    an item, like the chosen Dagger and the Warlock's two fixed daggers,
    wielding one marks both.
  - The Classes card reads "48 (rolled)" while the rolled levels are still
    empty; the number is the average until they're rolled.
  - Both are recorded in `open-items.md`.

---

## 2026-09-26 — Review step: per-step cards (D2i)

Follows the round-3 mock's Review page.
- **Web:** `Dnd5eReviewCards` places a card per step between the "choices
  left" list and "Finish character". Each card has its own "N left" badge and
  an Edit link back to its step.
  - Basics: name, sources, rules.
  - Species: species, bonuses.
  - Classes: each class and level with its subclass, and hit points.
  - Background: background, skills, tools.
  - Abilities: method, totals.
  - Equipment: inventory count, equipped items, currency.
- **Data:** everything comes from what the draft already carries: the plan's
  choices and the preview's selections, abilities, vitals and starting
  equipment. No backend change.
- **Tests:** `Dnd5eReviewCards.test.tsx`: card contents, the pending badge and
  the Edit links.
- **Checks:** all 135 web tests and the type-check pass. The backend is
  unchanged since the last green `:apps:api:check`.
  - **Not verified live:** the Vite dev server is still stopped.
- **Leaves D2 complete**, apart from the later items in `open-items.md` and the
  live checks of the Equipment and Review steps.

Suggested commit: `Add per-step review cards to the builder's Review step`

---

## 2026-09-26 — Equipment step: starting inventory with Wear/Wield (D2h)

Slice 3 of the approved D2d/D2f/D2h plan.
- **Build:** `Dnd5eCharacterBuild.equippedStartingItems` (catalogue slugs;
  missing reads as empty).
- **Materializer:** `Dnd5eBuildMaterializer.startingItems(outcome, build)` sets
  `StartingItem.equipped` for those slugs.
- **Finishing:** `CharacterSheetService.withStartingEquipment` passes
  `equipped` to the item, and `Dnd5eSheetMutator.addCatalogueItem` keeps
  `item.equipped()` instead of forcing `false`. Every other caller still adds
  unequipped items.
- **Preview:**
  - `CreationPreview.startingEquipment` (`StartingEquipmentPreview`) carries the
    items, an inventory list and the starting money split into gp/sp/cp.
  - Each inventory entry has an `equipAction`: `WEAR` for armor and shields,
    `WIELD` for weapons, null otherwise.
  - `CharacterDraftService` computes the summary's vitals on the preview sheet
    with the starting items on it, so AC and attacks follow what starts
    equipped.
  - `DraftPreviewResponse.startingEquipment`.
- **Web:** the Equipment step ends with:
  - "Current inventory (N)": every starting item, fixed ones included, with
    Wear/Wield toggles (`withStartingItemEquipped`);
  - a read-only "Currency" line (owner, 2026-09-26: the money is shown, not
    edited).
- **Tests:**
  - `Dnd5eBuildMaterializerTest`: equipped starting items;
  - `Dnd5eSheetMutatorTest`: an item added equipped starts equipped;
  - `Dnd5eStartingEquipmentPreviewTest`: inventory, actions, money;
  - `Dnd5eEquipmentStep.test.tsx` and `dnd5eBuild.test.ts`.
- **Checks:** `:apps:api:check` is green, and all 133 web tests and the
  type-check pass.
  - **Not verified live:** the Vite dev server was stopped by the system for
    low memory and wasn't restarted.
- **Resolves:** fixed starting-equipment items are now listed on the Equipment
  step.
- **Later:** "Add items" and "Other possessions" at creation (owner,
  2026-09-26).

Suggested commit: `Let starting armor and weapons begin worn or wielded, with the starting inventory and money on the Equipment step`

---

## 2026-09-26 — Species and Background steps (D2d, D2f)

- **Done (slices 1–2):**
  - `SelectionDetail` (ruleset) in `CreationPreview.selections` and
    `DraftPreviewResponse.selections`.
  - `Dnd5eSelectionDetails` builds the species card (bonuses, speed, size,
    languages, senses, and traits other than Age, Alignment, Size and
    Languages) and the background's "What you get" (skills, tools, languages,
    feature with its text, equipment).
  - The planner's `grantsSummary` now shows money inside a container
    ("Pouch (with 10 gp)"); new `equipmentSummary`.
  - Web: `SelectionCard`, and a `DetailStep` for Species and Background with
    "Choose", the card or grants, and "Choices".
  - Tests: `Dnd5eSelectionDetailsTest` and `SelectionCard.test.tsx`.
  - Verified live on "Mez test" (read-only).
- **Slice 3 (Wear/Wield) was finished afterwards** in the entry above.

Suggested commit: `Add species and background detail cards to the builder`

---

## 2026-09-26 — Classes step: per-level accordion (rest of D2e)

Follows the approved round-3 mock (`design-reference/mockups/d2-builder/round-3`,
`fighterTab()`).
- **Backend:**
  - New `ChoicePlacement(group, level)` (ruleset) on `CreationChoice`.
    `Dnd5eBuildPlanner` stamps each choice offered while it plans a class with
    the class slug and the level being planned.
  - Pooled spell choices get a null level: they belong to the class as a whole.
  - Choices outside a class have no placement.
  - `DraftResponse.ChoiceResponse` gains `group` and `level`, and
    `ProgressionTable` gains `key` (the class slug).
- **Web:**
  - `Dnd5eClassLevels` gives each class a tab when there are several. Each tab
    reads "Fighter · levels 1–3", with a pending badge.
  - Each level reached is a collapsible section: "Level N", a badge (N choices
    / done / no choices) and that level's features from the progression table.
    Its body holds the features line and the level's choices.
  - A level starts open only while it has a pending choice.
  - Class-wide choices (pooled spells) sit under "Every level" after the
    levels.
  - Choices no class owns (rolled hit points) stay in "Other class choices".
  - The ASI-or-feat nesting and the spell picker render as before, now through
    the shared `choiceRenderer`.
- **Tests:**
  - `Dnd5eBuildPlannerTest`: placements at levels 1 and 4, none outside
    classes;
  - `Dnd5eClassProgressionTest`: the table key;
  - `Dnd5eClassLevels.test.tsx`: sections, open state, tabs, "Every level".
- **Checks:** `:apps:api:check` is green, and all 128 web tests and the
  type-check pass. Verified live on "Mez test": Warlock and Fighter tabs, three
  levels each with their features and choices, and the Warlock spells under
  "Every level".
- **Found during the check:** Mez's base ability scores were saved as point buy
  with every score at 8 at 16:02 (local time). That was after they had been
  restored following slice 2's live test.
  - The owner later said they had changed Mez themselves while testing.
  - The scores had already been restored from the backup (STR 11, DEX 14,
    CON 12, INT 10, WIS 10, CHA 15), and the owner was fine with keeping them.

Suggested commit: `File builder class choices by level in a per-class accordion`

---

## 2026-09-26 — Classes step: class progression preview

Slice 4 of `features/builder-refinements.md`, which completes round 1 of the
builder refinements.
- **Backend:**
  - New `ProgressionTable` (ruleset) in `CreationPreview.progressions`, served
    as `DraftPreviewResponse.progressions`.
  - `Dnd5eClassProgression` builds each chosen class's table for levels 1–20
    from its catalogue data: proficiency bonus, features (the chosen subclass's
    in place of placeholders), the class's and subclass's columns, and spell
    slots.
  - No new endpoint and no re-ingest.
- **Web:** `ProgressionPanel`: a collapsed panel per class at the bottom of the
  Classes step, with the current level marked.
- **Tests:**
  - `Dnd5eClassProgressionTest`: Wizard slots, Barbarian Rage columns, Battle
    Master features, optional features;
  - `CharacterDraftServiceTest`: no tables before a class;
  - `ProgressionPanel.test.tsx`: collapsed on load, current level marked.
- **Checks:** `:apps:api:check` is green, and all 125 web tests and the
  type-check pass. Verified live on "Mez test" (Warlock 3 / Fighter 3).

Suggested commit: `Add a class progression preview to the builder's Classes step`

---

## 2026-09-26 — Builder options say what they give

Slice 3 of `features/builder-refinements.md`. The owner approved the survey's
samples; details are in "Slice 3 — as built" there.
- **Backend:**
  - New `Dnd5eOptionSummaries` builds one-line summaries from catalogue data for
    species, subspecies, variants, backgrounds, classes, subclasses (first-level
    features only), feats, optional features, spells and languages.
  - Items keep a plain line.
  - Feats and optional features quote their first benefit sentence, unless the
    catalogue redacts prose: new `CatalogueLookup.redactsProse()`, fed by
    `CatalogueService`'s redaction switch.
  - Prerequisites are appended after the summary instead of replacing it.
- **Web:**
  - The summary replaces the book name in chosen values (`ChoiceField`, the feat
    field) and in the classes editor. The book stays as the code badge with its
    tooltip.
  - `SourceSelect` shows the summary under each option's name, and its filter
    searches it.
  - Spell rows show the code badge; chosen spell chips show the summary on hover.
- **Tests:**
  - `Dnd5eOptionSummariesTest`: one case per kind, plus redaction;
  - `SourceSelect.test.tsx`: summary shown and searchable;
  - `ChoiceField.test.tsx`: summary replaces the book, with the book as
    fallback.
- **Checks:** `:apps:api:check` is green, and all 123 web tests and the
  type-check pass. Verified live on "Mez test" across the Species, Background
  and Classes steps.

Suggested commit: `Show what each builder option gives in place of its book name`

---

## 2026-09-26 — Builder rolls: 4d6 drop lowest and rolled hit points

Slice 2 of `features/builder-refinements.md`.
- **Dice:**
  - New `POST /api/characters/{id}/rolls/creation` (`CreationRollRequest`:
    die, count 1–20, optional `keepHighest` ≤ count, label up to 120 chars).
    It allows drafts.
  - `KeptDice` handles `4d6kh3`: the expression, the kept total, and the dropped
    dice (ties drop the earliest). `RollResponse` returns `dropped`, read back
    from the expression, so there's no schema change.
  - The plan's "extend expression parsing" step didn't apply: `dice/` has no
    parser. Details in `features/builder-refinements.md`, "Slice 2 — as built".
- **Planner:**
  - The MANUAL method option carries its roll (4d6, keep 3, "4d6 drop lowest").
  - `ROLLED_HIT_POINTS` now has one option per level after the first, with the
    class die, the average and the roll label, in the materializer's order.
  - Out-of-range results are plan problems.
- **Build and materializer:** `rolledHitPoints` accepts null for a level not
  rolled yet; that level uses the average.
- **Web:**
  - Abilities (Manual/rolled): a "Roll" button under each ability shows the dice,
    with the dropped die struck through, and writes the total.
  - Classes step: `Dnd5eRolledHitPointsField` shows a row per level with a
    number input and "Roll dX", plus the rolled total against the average.
  - A "Hit points" checklist line.
  - `RolledDice` shows the dice.
- **Tests:**
  - `KeptDiceTest`;
  - `RollServiceTest` and `RollControllerTest` (creation rolls, validation);
  - `Dnd5eBuildMaterializerTest` (hit point rows, dice, out-of-range problem,
    rolled and unrolled levels);
  - `Dnd5eBuildPlannerTest` (MANUAL roll data);
  - `Dnd5eAbilitiesStep.test.tsx` and `Dnd5eRolledHitPointsField.test.tsx`.
- **Checks:** `:apps:api:check` is green, and all 121 web tests and the
  type-check pass.
  - Verified live on "Mez test": hit points went from 48 to 41 with the rolled
    values; the six ability rolls matched their kept dice; the 10 rolls are in
    `rolls` with their labels.
  - Mez's draft was restored afterwards to point buy and empty rolled hit
    points. The rolls stay in the append-only history.
- **Not done:**
  - The finished-sheet check: Mez still has a pending choice.
  - A struck-through die in the sheet's roll log, which shows only the
    expression.

Suggested commit: `Add server-side 4d6-drop-lowest ability rolls and rolled hit points to the builder`

---

## 2026-09-26 — Partner dropdown lists only partners with imported books

Owner follow-up: the dropdown should show only Critical Role and Rick and
Morty, the two partners whose books are imported.
- `CatalogueService.partners` drops partners without imported books.
  `PartneredSources` still knows all 27 brands, so a partner appears on its own
  once one of its books is imported.
- **Web:**
  - The "no books imported yet" label is gone.
  - With no partner at all, the dropdown says "No partnered books are available
    yet."
- **Checks:** `:apps:api:check` is green, and all 116 web tests and the
  type-check pass. Verified live on "Mez test": "Every partner (2)".
- **Found during the check:** the Mez draft had `partners: []`, most likely
  left over from the live checks of the earlier versions of this row. It
  couldn't be reproduced: re-checking both partners saves `null`, as expected.
  The draft is back on every partner.

Suggested commit: `Show only partners with imported books in the partner dropdown`

---

## 2026-09-26 — Basics: every D&D Beyond partner in a dropdown

Owner follow-up: every partner in D&D Beyond's list should be selectable, not
only the two brands with imported books. The picker should be a dropdown,
shown only while partnered content is on.
- **Partner list:** `PartneredSources` now holds all 27 of D&D Beyond's
  partner brands, in its order, from Critical Role to Rick and Morty. The owner
  supplied the list; it matches the builder's "Choose Partners".
  - Only Critical Role and Rick and Morty have imported books.
- **API:** `GET /api/catalogue/partners?systemId` (new) returns each partner with
  its imported books (`CataloguePartnerResponse`).
- **Preference:** `Dnd5ePreferences.partneredContent` is the master switch. It's
  on by default, and older drafts read as on.
  - A partner's books are offered only while it's on and the partner is chosen
    (`partners`, null = every partner).
- **Web:**
  - The Partnered row has an "Enable partnered content" switch. While it's on,
    a dropdown lists every partner with a checkbox, its imported books, or "no
    books imported yet".
  - The summary reads "Every partner (27)" or "N of 27 partners". Checking the
    last partner again collapses the choice back to "every partner".
  - The one-switch-per-brand rows from the previous entry are gone.
- **Tests:**
  - planner: the master switch off hides partnered entries;
  - `CatalogueServiceTest`: 27 partners, in order, with their books;
  - `Dnd5eBasicsStep.test.tsx`: dropdown, uncheck, hidden while off;
  - `dnd5eBuild.test.ts`: `withPartnerToggled` expand and collapse.
- **Checks:** `:apps:api:check` is green, and all 116 web tests and the
  type-check pass.
  - Verified live on "Mez test": unchecking Critical Role removes the EGW
    background Grinner; turning the switch off hides the dropdown.
  - The draft was left with every partner on.

Suggested commit: `List every D&D Beyond partner in a dropdown behind the partnered content switch`

---

## 2026-09-26 — Basics: partnered content by partner brand

The owner noticed the Partnered row was empty and expected books like Critical
Role and Rick and Morty there.
- **What D&D Beyond does:** its builder's "Choose Partners" list has 27 brands,
  including Critical Role and Rick and Morty. Its library, though, calls
  Explorer's Guide to Wildemount, Call of the Netherdeep and D&D vs. Rick and
  Morty "Official".
  - Third-party partnered books (Tal'Dorei Reborn, Humblewood, Grim Hollow…)
    aren't in 5etools' main data.
- **Owner's choice:** group the licensed books we already import by partner
  brand, as D&D Beyond's builder does. Nothing new is imported.
- **Grouping:** `PartneredSources` (catalogue package) maps 5etools codes to a
  brand.
  - Critical Role: EGW, CRCotN, and the Wildemount adventures ToR, DD, FS and
    US.
  - Rick and Morty: RMR, RMBRE.
  - This is D&D Beyond's grouping, not a 5etools fact, so it's a small
    hand-kept table.
- **API:**
  - `GET /api/catalogue/sources` returns `partner`.
  - `CatalogueRecord` carries `partner`.
  - `Dnd5ePreferences.partners` (null = every partner, the default) decides
    which brands are offered. A partner's books ignore the book list.
- **Web:**
  - Partnered books left the "Other books" menu. The Partnered row now has an
    "Enable all partnered content" switch plus one switch per brand, with its
    books listed.
  - Imported today: Critical Role (Wildemount, Call of the Netherdeep) and Rick
    and Morty (Big Rick Energy).
- **Tests:** planner (partners follow `partners`, whatever the book list),
  `CatalogueServiceTest`, `ContentDirectoryCatalogueTest`, and
  `Dnd5eBasicsStep.test.tsx`.
- **Checks:** `:apps:api:check` is green, and all 112 web tests and the
  type-check pass.
  - Verified live on "Mez test": with Critical Role off, the EGW background
    Grinner leaves the Background list; with it on, it's back.
  - The draft was left with every partner on.
- **Not done:** third-party partnered books would need a new data source (for
  example 5etools' homebrew repository). See `open-items.md`.

Suggested commit: `Group licensed books by partner brand in the builder's Partnered content`

---

## 2026-09-26 — Basics: playtest content limiter

Slice 1 of `features/builder-refinements.md` ("Partnered and playtest content
limiter").
- **Classification:** `PlaytestSources` (catalogue package) marks a source as
  playtest when its 5etools code starts with `UA`.
  - It reads the `source_code` already imported, so no migration or re-ingest
    was needed.
  - Today the only playtest source is `UATheMysticClass`: the Mystic class and
    its 6 subclasses.
- **API:**
  - `GET /api/catalogue/sources` returns `playtest` per source.
  - `CatalogueRecord` carries `playtest`, set by `DatabaseCatalogueLookup` and
    `ContentDirectoryCatalogue`.
- **Preference:** `Dnd5ePreferences.playtestContent`, off by default. Drafts
  saved before it read as off.
  - `Dnd5eSourceFilteredCatalogue` offers playtest entries only while it's on.
  - Playtest sources ignore the book list (`sources`): "Enable all" and "Core
    only" don't touch them.
- **Web:**
  - The Basics "Partnered content" frame became **"Partnered & playtest
    content"**, with two rows:
    - Playtest content: a switch that names the playtest books.
    - Partnered content: "No partnered books are available yet."
  - Playtest books no longer appear in the "Other books" menu.
- **Tests:**
  - planner: playtest entries are offered only with the switch on;
  - `ContentDirectoryCatalogueTest` and `CatalogueServiceTest`: the `playtest`
    flag;
  - `Dnd5eBasicsStep.test.tsx` (new) and `dnd5eBuild.test.ts`.
- **Checks:** `:apps:api:check` is green, and the web tests and type-check pass.
  - Verified live on "Mez test": with the switch on, Mystic appears in "Add
    another class", and the choice survives a reload. With it off, Mystic is
    gone.
  - The draft was left with the switch off.

Suggested commit: `Add a playtest content switch to the builder's Basics step`

---

## 2026-09-26 — EEPC book name fixed at ingestion

Slice 1, item 3 of `features/builder-refinements.md`.
- **Cause:** 5etools' `books.json` doesn't list `EEPC`, so
  `FiveEToolsSourceNames` fell back to the code itself.
  - A manual fix to `aarakocra-eepc.json` and `genasi-eepc.json` had covered
    the top-level `sourceBook` only. The 4 Genasi subspecies still read "EEPC",
    and a re-ingest would have undone the manual fix.
- **Fix:**
  - `EEPC` joined the unlisted names, as "Elemental Evil Player's Companion".
  - Species were re-ingested: only `genasi-eepc.json` changed.
  - Species were re-imported (134 entries). No `"sourceBook": "EEPC"` is left in
    the files or in Postgres.
- **Other sources:** no other content file has a raw code as its book name.
- **Test:** `Ingest5eToolsRunnerTest` covers `EEPC`.

Suggested commit: `Resolve the EEPC source code to its full book name at ingestion`

---

## 2026-09-25 — Spell picker: collapsible filters

Slice 1, item 4 of `features/builder-refinements.md`.
- **Filters panel:** in `SpellPicker` (spells and cantrips), the level, school,
  damage type, casting time and source filters, and the Concentration, Ritual
  and Chosen-only chips, now sit in a panel.
  - The panel starts collapsed.
  - A "Filters" button opens it and shows how many filters are set
    ("Filters · 2").
- **Always visible:** the search box and "Clear filters".
- **Whole list collapsible (owner follow-up):** a "Hide spells / Show spells"
  button next to the "N of M chosen" badge collapses the search, the filters and
  the spell grid.
  - Chosen spells stay visible as chips while it's collapsed.
  - Each picker starts open while its choice is incomplete, and collapsed once
    it's complete.
  - The button sits in the header rather than beside the search box, so it stays
    reachable when the search is hidden.
- **Test:** `SpellPicker.test.tsx` opens the panel and checks the count.
- **Checks:** all 106 web tests and the type-check pass. Not checked in the
  browser.

Suggested commit: `Collapse the spell picker filters behind a Filters button`

---

## 2026-09-25 — Builder dropdowns open upward when there's no room below

Slice 1, item 1 of `features/builder-refinements.md`.
- **Problem:** near the bottom of the page, a `SourceSelect` popup ran past the
  document and left a white band.
- **Hook:** `usePopupDirection` (`builder/usePopupDirection.ts`) measures the
  open popup against the window.
  - It opens downward when the popup fits below (with an 8 px margin).
  - Otherwise it opens toward the side with more room.
- **SourceSelect:** it uses the hook. `.source-select__popup.is-up` anchors the
  popup above the trigger.
- **Left out:** the Basics "Content sources" menu (`builder-multi__menu`). It
  always sits near the top of the page, so it can't overflow.
- **Tests:** `usePopupDirection.test.ts`.
- **Checks:** the builder tests and the web type-check pass. Not checked in the
  browser.

Suggested commit: `Open builder dropdowns upward when there's no room below`

---

## 2026-09-25 — Basics: "Other books" order

Slice 1, item 2 of `features/builder-refinements.md`.
- **Order:** the Content sources menu lists XGE, TCE, GGR, ERLW and SCAG first,
  in that order, then every other optional book alphabetically by name.
  - It used to follow the API's largest-first order.
  - The fixed list is the owner's choice, not a count ranking.
- **Where:** `orderOptionalSources` and `FEATURED_SOURCE_CODES` in
  `dnd5eBuild.ts`, matched by source code. `Dnd5eBasicsStep` uses them.
- **Test:** a new test in `dnd5eBuild.test.ts`.
- **Checks:** the web type-check and the build tests pass. Not checked in the
  browser.

Suggested commit: `Order the optional source books with the featured ones first`

---

## 2026-09-25 — Footer at the bottom of short pages

The owner asked for the footer to sit at the end of the page.
- **`.app-page`** is now a flex column at least `100vh` tall. Its footer gets
  `margin-top: auto`, and its direct `.app-wrap` children keep full width
  (`width: 100%`), since flex would otherwise shrink the auto-margined
  container. All in `theme.css`.
- **Home:** the footer's inline `marginTop: 30` became `.home-main {
  padding-bottom: 30px }`, so the gap stays without blocking the push.
- **Pages:** this applies to the landing page and the home. The builder and the
  sheet have no footer.
- **Checks:** web type-check and 102 tests green; not checked in the browser.

Suggested commit: `Keep the footer at the bottom of short pages`

---

## 2026-09-25 — Basics: the portrait on the name's line, right-aligned

The owner asked for the portrait to share the character name's line in Basics,
aligned to the right.
- **`PortraitPicker`** splits into `.builder-portrait__main` (its own "Portrait"
  label, the image and buttons, and the error) and the default-portrait
  gallery.
- **Layout.** `Dnd5eBasicsStep` puts the name and the picker in a
  `.builder-identity` grid: name on the left, the picker's main part in the
  second column with `justify-self: end`. The gallery still opens below across
  both columns, as the owner confirmed. The picker's box uses
  `display: contents` so its parts can join the grid.
- **Narrow screens (≤ 780 px):** the portrait drops under the name.
- **Checks:** web type-check and 102 tests green. The owner is checking the
  look in the browser.

Suggested commit: `Put the portrait on the character name's line in Basics`

---

## 2026-09-25 — "Create character" skips the old system-picker page

The owner saw the old system-picker page flash at `/characters/new/dnd-5e` on
every "Create character".
- **`builder/useCreateCharacter`:** one place that creates a draft and opens its
  builder.
- **Home:** the system cards, "＋ Create character" and the empty-state button
  create the draft right there ("Creating…" on the button) and navigate
  straight to the builder.
- **"Create character" without a system:** with a single supported system
  (`onlySystemId()` in `supportedSystems.ts`) it starts that system; the landing
  hero now returns to `/characters/new/dnd-5e` after the login.
- **`/characters/new/:systemId`:** now `builder/CreateCharacterPage`. It shows
  only the compact app header and "Creating your D&D 5e character…", creates one
  draft (a ref guards against StrictMode) and replaces itself with the builder.
  An unknown system goes home.
- **`SystemPicker` (`/characters/new`)** lost its auto-create code. It is only
  reached when there are several systems.
- **Tests:** `CreateCharacterPage.test.tsx` (3); `HomePage.test.tsx` checks
  direct creation from a card and from the header (2 new);
  `LandingPage.test.tsx` updated. Web type-check and 102 tests green.
- **Verified live:**
  - signed out, a landing card went login → `/characters/new/dnd-5e` →
    builder;
  - signed in, the recorded history for a home card was
    `/` → `/characters/<id>/build`, with nothing in between;
  - both test drafts were deleted afterwards through the confirmation dialog.
- **Left for the owner:** seven drafts created during their own tests
  (16:10–16:38): six empty, one with Yuan-ti chosen.

Suggested commit: `Create characters directly from the home and skip the system picker`

---

## 2026-09-25 — Pending: BFF with an HttpOnly session cookie

The owner asked how the Keycloak tokens are stored. Today they are in the tab's
`sessionStorage` (`oidc-client-ts`), and the owner judged the backend-for-frontend
pattern necessary: tokens held server-side, the browser holding only an
`HttpOnly; Secure` session cookie. It is recorded as a pre-deployment item in
`open-items.md` ("Security"), with the recommended approach and the rules it
would change (an ADR is needed first). No code changed.

---

## 2026-09-25 — One app header on the builder and the sheet

The owner asked for the builder's and the sheet's headers to carry the logo and
app name, a way back to the character list, and the user button in the corner.
- **`AppHeader`** moved from `home/` to `shell/AppHeader.tsx`, and its CSS from
  `home.css` to `theme/theme.css`. It gained three props:
  - `wide`: 1440 px, the builder's width;
  - `backToList`: a "← Your characters" link;
  - `children`: page status next to the logo.
- **Builder** (`CharacterBuilder`, `SystemPicker`): the old `builder-topbar` is
  replaced by `AppHeader`, with the "Draft" badge and save state kept as
  children. The player's name comes from `AppShell` (`userName`).
- **Sheet** (`SheetShell`): `AppHeader` sits above the character header. The
  header's own "← Back" and user label, and their CSS
  (`.sheet-header__account`, `__user`, `__back` in `frames.css`), were removed,
  and `SheetShell` lost its `onBack` prop.
- **Fidelity check** against D&D Beyond (`/characters/50149479`): Beyond also
  has a site bar (logo, navigation, user) above its character header, so the
  layout now matches that arrangement. The character header itself is
  unchanged.
- **Verified live** on Aria's sheet and a draft's builder. Web type-check and 97
  tests green.

**Follow-up (same day, owner's request):**
- the header is taller: `.app-header .app-wrap` uses `min-height: 96px`
  instead of `height: 58px`;
- it spans the full width (`max-width: none`, 24 px side padding), with the
  logo and name in the left corner and the user button in the right;
- the logo went from 32 to 48 px and the name from 17 to 20 px;
- `AppHeader`'s `wide` option, now redundant, was removed along with
  `.is-wide`.

Checked live on the landing page and a sheet; web type-check and 97 tests green.

**Second follow-up (same day):** the builder and the sheet use a compact header.
- **Compact:** `AppHeader compact` gives a 58 px bar with the 32 px logo. The
  landing page and the home keep the 96 px bar.
- **Back link:** `backToListWithin={px}` puts "← Your characters" at the right
  edge of the page's centred column, not next to the logo.
  - The column is an absolutely positioned `.app-header__column`, kept 200 px
    clear of both edges so it never reaches the user button.
  - The builder passes `BUILDER_COLUMN_WIDTH` (1440, `builder/builderLayout.ts`,
    matching `.builder-layout`).
  - The sheet measures its `fit-content` column with a ResizeObserver
    (`useElementWidth` in `SheetShell`).
- **Measured live at 1912 px:** the back link ends at the column's right edge
  in both, 1669 px in the builder and 1544 px on the sheet; the user button
  stays in the corner.

Suggested commit: `Use the app header on the builder and the sheet`

---

## 2026-09-25 — Home page, public landing and Keycloak login theme (H1–H6)

The home mock (direction B) is built into the app. The plan, decisions and
step-by-step record are in `.ai/features/home-page.md`.
- **H1:** app theme tokens in `apps/web/src/theme/theme.css`, ready for a later
  dark theme.
- **H2:** a per-system card summary.
  - `CharacterSummarizer` strategy plus registry; `Dnd5eCharacterSummarizer`
    gives each class with its own level, then the species, never a total level;
  - `CharacterSummaryService` returns empty on failure;
  - `CharacterResponse.summary`.
- **H3:** the app no longer forces login.
  - `/` is a public landing page;
  - "Log in", "Create character" and protected routes (`RequireSession`) log in
    and return to their path through the OIDC state, via `safeReturnPath`;
  - new `/characters/new/:systemId` creates that system's draft at once.
- **H4:** the signed-in home.
  - header with logo and user menu;
  - "Start something new" system cards, with the non-D&D ones "Coming soon";
  - a filter kept in `?system=`;
  - banners grouped by system, with the system colour bar;
  - "⋯" delete behind a confirmation;
  - empty states;
  - `characters/CharacterList.tsx` removed.
- **H5:** the Keycloak login theme.
  - `infra/keycloak-themes/omni-sheet-vault/login` (extends `keycloak.v2`,
    `darkMode=false`), mounted by `docker-compose.yml`;
  - `realm-export.json` `loginTheme` for fresh installs; the running realm was
    switched with `kcadm`;
  - registration and reset password stay off (`open-items.md`).
- **H6:** verified live.
  - landing, then login and back to the home with the real summaries;
  - the D&D card creates exactly one draft; deleting it with confirmation works;
    the filter URL and the user menu work;
  - Log out was not exercised, so as not to end the owner's session.

  Checks: `:apps:api:check` green; web type-check and 97 tests green.

Suggested commit: `Add the public landing page, the signed-in home with per-system character banners, and a Keycloak login theme`

---

## 2026-09-25 — App logo

The owner made the logo mark in Recraft: a vault shield holding a blue hexagon
with a keyhole. There's no wordmark; the name stays live text in Roboto
Condensed, uppercase, next to the mark.
- **Original:** `design-reference/brand/logo-recraft-original.svg`, as
  downloaded. It is 40 KB, almost all of it Recraft's C2PA provenance manifest,
  and it has a baked-in `#F3F3F1` square background.
- **Cleaned copies:** background and metadata removed, cropped to the mark
  (`viewBox="121 97 786 786"`), 1.8 KB each:
  - `apps/web/src/assets/brand/omni-logo.svg`: charcoal shield, for light
    backgrounds;
  - `apps/web/src/assets/brand/omni-logo-light.svg`: parchment shield, for the
    charcoal header, where the dark one would vanish;
  - `apps/web/public/favicon.svg`: the dark mark.
- **`index.html`:** the favicon link, and the title "Omni Sheet Vault".
- **Mock:** the home mock's `✦` placeholder is replaced by the light logo.
  `design-reference/brand/preview.html` shows both versions at 16–128 px on
  both backgrounds.
- **Recoloured:** at the owner's request, the hexagon's periwinkle `#6587FE` became
  the mock's gold `#D9A441` in all three cleaned files. The Recraft original keeps
  its colour.
- **Accent:** direction B's accent changed from blue `#2B69AB` to the same gold
  (`--accent: #D9A441`), with charcoal text on gold (`--accent-ink: #1F2327`),
  because white on this gold is too low-contrast to read. B's "Draft" badge uses
  a darker gold text (`#7A5510` on `#FBF3E2`). This is in the home mock only; the
  builder's and the sheet's blue are unchanged.

---

## 2026-09-25 — Home page mock (landing, Keycloak login, signed-in home)

The owner wants a proper home page:
- a header with the app name on one side, and login or the signed-in user on
  the other;
- a filter of the systems that have characters;
- characters listed per system as banners: portrait, name, and the system's
  facts (D&D: level, classes, species);
- a way to show, from the start, that characters can be created for many
  systems;
- a public landing page that sells the app, where "Create character" leads to
  a Keycloak login restyled in the chosen aesthetic.

Mock: `design-reference/mockups/home/index.html`. It uses the bundled portraits,
so serve it from the repository root. It has three screens:
- the landing page (signed out);
- a Keycloak login theme preview;
- the signed-in home.

Each screen comes in three directions:
- **A, Arcane Vault:** dark, with a gold accent and a level medallion.
- **B, Clean Parchment:** light, the builder's tokens, with a system-coloured
  edge.
- **C, Tavern Board:** parchment with Cinzel and Crimson Pro type, and a wax-seal
  level.

Systems other than D&D 5e (Vampire, Daggerheart, Pathfinder 2e, Call of
Cthulhu, Tormenta20) are placeholders marked "Coming soon". The filter lists
only systems that have characters, as agreed. Nothing is built yet.

**Chosen (same day): direction B, Clean Parchment**, pages as mocked. Direction
A (Arcane Vault) becomes a later "Dark theme", switched from the user menu in
the top-right corner.

**Banner facts, refined (same day):**
- no total character level, only each class's level ("Rogue 4 / Sorcerer 3");
- the species on its own line below the classes;
- a left bar in the system's colour (`--sys-color`, D&D 5e `#C2412F`) in both
  A and B.

A's level medallion was then removed too, at the owner's request. Every
character banner carries its system's colour as the left edge (D&D 5e: red,
the colour of its logo). The logo is open: the owner will make one with an
image-generation tool, and `✦` stays as a placeholder until then.

---

## 2026-09-25 — Builder progress sidebar: one line per kind of choice, no scrollbar

The owner found the left sidebar too detailed. It repeated every pick ("Choose 4
Bard cantrip(s)", "Choose 14 Bard spell(s)"…) with its chosen value, and grew a
scrollbar with many choices.
- `BuilderChecklist` shows each step and, under a step with several kinds of
  choice, one line per kind with ✓ or "n left". It never shows chosen values.
- The kinds come from the system:
  - `BuilderDefinition.checklistGroupOf` / `checklistGroupRank`;
  - D&D 5e maps choice types to Species, Class, Subclass, Background, Ability
    scores, Ability increases & feats, Proficiencies, Class features, Spells,
    Starting equipment and Other choices;
  - `checklistLines` does the folding.
- `.builder-layout__side` is no longer sticky and has no `max-height`, so it
  grows with the page. The summary column stays sticky.
- Test: `dnd5eBuild.test.ts` "checklist lines".

Suggested commit: `Simplify the builder progress sidebar to one line per kind of choice`

---

## 2026-09-25 — D2j: character portraits (defaults and uploads)

Plan approved by the owner:
- the AWS SDK S3 dependency, plus Testcontainers MinIO for tests;
- signed URLs;
- presets shown together, with grouping by species left for later.
- **Storage (`storage` package):**
  - `PortraitStorage` and `S3PortraitStorage`: lazy clients, bucket created on
    the first upload, signed GET URLs;
  - `StorageProperties`: `storage.*`, with `public-endpoint`, `region` and
    `url-ttl` defaults;
  - `application.yml`: credentials default to empty so contexts without MinIO
    still start; multipart capped at 3 MB per file;
  - `application-dev.yml` imports `../../.env` for the local MinIO credentials.
- **Character:**
  - `CharacterPortrait`: sealed, `preset:<id>` or an upload key;
  - `PortraitImage`: decodes PNG/JPEG only, at most 4096 px a side, re-encoded as
    a centre-cropped PNG of at most 512 px, which drops EXIF;
  - `CharacterPortraitService` and `CharacterPortraitController`:
    `PUT/POST/DELETE /api/characters/{id}/portrait`, owner-checked, with a
    replaced or removed upload deleted quietly;
  - `PortraitResponse` inside `CharacterResponse` and `DraftResponse`;
  - `InvalidPortraitException` → 400, an oversized upload → 413.
- **Concurrency.** `portrait_key` is now `updatable = false` and written only by
  `CharacterRepository.updatePortraitKey`. A draft autosave that loaded the row
  before a portrait change can't put back a stale value.
- **Web:**
  - `characters/portrait.ts` (types, API calls, client-side type and size
    check);
  - `portraitPresets.ts`, the preset registry per system. The D&D 5e presets
    come from `dnd5ePortraitPresets.ts` via `import.meta.glob` and
    `portraits.json`;
  - `CharacterPortrait`;
  - `PortraitPicker` in Basics. The owner asked mid-slice that "Pick a default
    portrait" be a button opening a separate section;
  - the portrait shows in the builder summary, the sheet header
    (`SheetShell`) and the character list;
  - `tsconfig.json` gained `resolveJsonModule`.
- **Verified live:**
  - picked a preset, then uploaded a 600×400 test image: centre-cropped square
    in Basics and the summary; the object sits in MinIO under
    `uploads/<id>/<uuid>.png`; an unsigned GET gets 403;
  - list thumbnail;
  - sheet header (Aria, set in the database for the check and reset to null);
  - Remove: key null, object deleted.

  One DELETE showed as a 503 in the browser's network log while the tab was
  frozen. The API logged nothing and the removal had in fact happened, so it was
  treated as a browser artifact; `removingAnUploadDeletesItAndRemovingAgainIsHarmless`
  covers the repeat.
- **Tests:**
  - `S3PortraitStorageTest` (3, real MinIO);
  - `PortraitImageTest` (5);
  - `CharacterPortraitServiceTest` (7, real Postgres and MinIO, including the
    stale-autosave case);
  - `CharacterPortraitControllerTest` (6);
  - web: `PortraitPicker.test.tsx` (6) and `portraitPresets.test.ts` (4).
- **Left out:** presets grouped by species; cleanup of uploads belonging to
  soft-deleted characters (both in `open-items.md`); a crop editor; WebP/GIF.

Suggested commit: `Add character portraits: default presets and uploads to MinIO (D2j)`

---

## 2026-09-25 — D&D Beyond default portraits downloaded (painted backgrounds)

The owner approved downloading Beyond's painted-background default portraits,
saved next to the item-kind icons, and using the Beyond character "test" if
needed. That character wasn't changed: its "Manage portrait" window was only
opened and cancelled.
- **Source:** the window's component state (`portraitData`) holds all 1,041
  default portraits, each with Beyond's `raceId`. No species had to be switched
  to reach the per-species sets.
- **Selection, 51 portraits, checked by eye against numbered contact sheets:**
  - every `/avatars/10/…` portrait (42) except `10/69`, a helmeted dwarf on the
    grey-textured background;
  - the painted dwarves `/avatars/17/969–978` without `973` (9).

  Everything else is a full illustration, grey-textured, or a newer
  white-background style.
- **Saved to `apps/web/src/systems/dnd5e/portraits/`:**
  - `dnd_portrait_<beyondAvatarId>.png`, 51 files, all 256×256, 5.6 MB in total,
    downloaded from `https://www.dndbeyond.com/avatars/…` (originals, no auth);
  - `portraits.json` lists each file with its Beyond avatar id and `raceId`.
- **Proprietary art:** D&D Beyond/Wizards of the Coast, like the
  `dnd_item_kind_*.jpg` icons. The owner keeps it local and hasn't pushed it.
- **Not built:** offering them in the builder. That is D2j, together with the
  upload; see `open-items.md`.

Suggested commit: `Add D&D Beyond's painted-background default portraits`

---

## 2026-09-25 — Equipment in Beyond's layout, a shorter ASI line, portrait research

- **Equipment step** (`Dnd5eEquipmentStep`, replacing the generic choice list):
  - one frame per package ("Bard starting equipment", then the background's),
    class first;
  - equipment or gold as `Segmented` buttons; taking gold hides the package and
    shows the amount;
  - each A-or-B pick as one line per option, a checkbox-style radio;
  - the unpicked line dims;
  - item picks ("Choose 1 martial weapon") sit inline under the chosen line, as
    Beyond does, with one `SourceSelect` per unit. The same weapon can be taken
    twice, which closes the equipment repeat gap in `open-items.md`.
- **Ability score increases:**
  - `Dnd5eGrantState.increase` joins a second increase from the same source into
    one contribution, so an ASI "+2 Dexterity" is one line instead of "+1, +1";
  - the source label stays "Ability Score Improvement (Wizard 4)". The Generation
    Method lines and the sheet are unchanged;
  - only the Score Calculations cards abbreviate it to "ASI (Wizard 4)", as the
    owner asked.
- **Default portraits: research only, nothing downloaded.** Beyond's "Manage
  portrait" window:
  - "Other Portraits" lists 998 portraits. Only the first 38 (all under
    `/avatars/10/`) have the painted brush-stroke background; the rest are full
    illustrations or grey-textured;
  - the current species' section (Hill Dwarf, 42) adds more painted ones, under
    `/avatars/10/` and `/avatars/17/`;
  - each species has its own set, shown only when the character is that species.

  Downloading needs the owner's permission. Collecting every species' set would
  mean switching a Beyond character's species. The art belongs to D&D
  Beyond/Wizards of the Coast, so shipping it raises the same exposure as
  adr-0005. Tracked in `open-items.md`.
- **Verified live** (view only) on the owner's draft: the Equipment lines and
  buttons, the merged "Dexterity +2" and the "ASI (Wizard 4)" row.
- **Tests:** `Dnd5eEquipmentStep.test.tsx` (4); `Dnd5eGrantStateTest`; the ASI
  abbreviation in `Dnd5eAbilitiesStep.test.tsx`.

Suggested commit: `Lay out starting equipment like D&D Beyond and merge same-source ability increases`

---

## 2026-09-25 — Builder: instant picks, PHB first, and ASI or feat in two steps

Three adjustments the owner asked for:
- **Input lag.** A pick only showed after the 600 ms autosave debounce plus the
  API round trip, because the screen drew each choice's `selected` from the
  saved plan.
  - `BuilderDefinition` gained `answerOf`. D&D 5e reads it the way
    `withSelection` writes it: `build.*` fields, subclasses and answers.
  - `withLocalAnswers` overlays the local answers on the plan, recomputing
    `pending`. `CharacterBuilder` passes that live plan to the step page,
    checklist and summary line.
  - The server's plan still drives stale-answer pruning.
- **PHB first.** `BuilderDefinition.leadingSources` (D&D 5e: `["Player's
  Handbook"]`) reaches `SourceSelect` through `SourceCodesProvider` /
  `useLeadingSources`. The other books follow, largest group first.
- **ASI or feat.** The new `Dnd5eAsiOrFeatField` replaces the single mixed list
  (ASI + every feat):
  - first a segmented "Ability Score Improvement | Feat";
  - **ASI:** "+2 to one ability | +1 to two abilities", then a single or a
    multiple `SourceSelect`. +2 is stored as the ability twice, so the ASI
    repeat gap is closed;
  - **Feat:** a feat `SourceSelect`, with the feat's nested choices (those whose
    `parentChoiceId` is the ASI choice) indented under it.

  The answer format is unchanged; this is frontend only. `Segmented` moved from
  `Dnd5eBasicsStep` to `builder/Segmented.tsx`.
- **Found while verifying:** in multiple mode the "Clear selection" row appeared
  at the top of the list after the first pick and pushed every row down, so the
  next click hit the wrong option. "Clear" now sits in the status line.
- **Verified live** on the Bard 5 / Wizard 3 draft:
  - picks are marked immediately and rows stay put;
  - Feat mode lists PHB feats first;
  - every test action was undone.

  That draft's `class:bard:tools:0` then showed drum, flute and lute, which this
  session didn't pick. Most likely the owner's own tab was editing the same
  draft; it was left as is.
- **Tests:** `Dnd5eAsiOrFeatField.test.tsx` (6); `answerOf` / `withLocalAnswers`
  in `dnd5eBuild.test.ts`; leading-book order and the status-line Clear in
  `SourceSelect.test.tsx`.

Suggested commit: `Show builder picks instantly, list the PHB first and split ASI or feat into two steps`

---

## 2026-09-25 — Source Select for every builder choice, with multiple picks

The owner asked for subclass, skill, tool and other chip-button choices to use
the Source Select too, with several picks up to the choice's count.
- **`SourceSelect` gained a multiple mode** (`multiple`, `values`, `max`):
  - the popup stays open while picking, and Enter toggles;
  - the trigger lists the picks with an `n/max` counter;
  - a status line reads "n of max chosen";
  - once full, unpicked options are `aria-disabled` and clicking a picked one
    removes it;
  - `aria-multiselectable` is set on the listbox.
- **No book headings** when no option has a book (skills, tools).
- **`ChoiceField`** now always renders `SourceSelect`: single with a clear
  option when `count === 1`, multiple otherwise. The chip picker and the
  16-option threshold are gone.
- **Verified live** on a Bard 5 / Wizard 3 draft without changing it: "Choose 3
  skills" shows 3/3, with the other skills blocked.
- **Tests:** `SourceSelect.test.tsx` covers the multiple mode and lists without
  books; `ChoiceField.test.tsx` was moved to the combobox.
- **Known gap, unchanged:** a choice that allows the same option twice (an ASI
  "+1 Intelligence, +1 Intelligence") can still pick each option only once, as
  the chips could.

Suggested commit: `Use Source Select for every builder choice, with multiple picks`

---

## 2026-09-25 — Source Select: builder dropdowns with book codes

The owner chose variant A of the Source Select mock and asked for the book
grouping shown on Species. It now replaces the builder's native selects: long
single picks in `ChoiceField` (species, background…) and "Choose a class / add
another class".

- **Source codes, end to end:**
  - migration `V6__add_catalogue_source_code.sql` adds a nullable
    `catalogue_entries.source_code`;
  - `CatalogueEntryImport` gained `sourceCode`. Its old 9-argument constructor
    stays for the converters, and `Ingest5eToolsRunner` fills the code in with
    `withSource(name, code)`;
  - `CatalogueEntry` and `CatalogueImportService` persist it;
  - `GET /api/catalogue/sources` returns `sourceCode` next to `sourceBook`.
- **Re-ingested and re-imported** (owner-approved). `ingest5etools` rewrote
  `content/dnd-5e/`: 3,101 files, with none added or removed. Compared with a
  copy taken before the run:
  - every file gained only its `sourceCode` line;
  - 259 feat and optional-feature files also gained `"uses"`, `"action"` and
    `"grants"` as `null`. The current converters already emit these; the
    committed content predated them.

  Every kind was then re-imported into the local database with
  `bootRun --args='--import-catalogue=… --server.port=0'`. All 3,101 entries have
  a code, and no book name maps to two codes.
- **Web:**
  - `builder/SourceSelect.tsx`: grouped by book (largest group first), a code
    badge, a full-name tooltip portalled to `<body>`, a filter box above 8
    options, keyboard support and a clear option;
  - `catalogue/SourceCodes.tsx`: `SourceCodesProvider` / `useSourceCode`, loaded
    once per system and retried when a response has no codes;
  - styles at the end of `builder.css`;
  - documented in `ui-design-system.md`, "Source select".
- **Verified live** on the Species and Classes steps: codes shown, tooltips on
  the top row included. The running API was restarted to pick up the change.
- **Tests:** `SourceSelect.test.tsx` (7 tests); `ChoiceField.test.tsx` updated;
  `Ingest5eToolsRunnerTest` and `CatalogueImportServiceTest` cover the code.
- **Left as is:** the other native selects are small fixed lists, not catalogue
  entries: ability method, point-buy scores, encumbrance, spell filters and the
  sheet's panels.

Suggested commit: `Add source codes to the catalogue and a Source Select dropdown to the builder`

---

## 2026-09-25 — Creation flow: one spell picker per class instead of one per level

A Bard 10 used to get a separate picker for every level that added a cantrip or a
spell. `Dnd5eSpellPlanner` now asks once per class, after its last level:
- `class:<c>:cantrips`: cantrips known at the class's level;
- `class:<c>:spells`: known casters, spells known at that level;
- `class:<c>:spellbook`: Wizard, the sum of the spellbook additions.

Each pool's source label names the levels that add to it ("Bard 1, 4, 10";
three or more in a row become "Wizard 1–5"). This applies to the creation flow
only; the sheet's spell management is unchanged.

- **Prerequisites.** Invocations are planned level by level, before the pooled
  choices. So that they still see Eldritch Blast, `startClass` claims the pool's
  current selections as known before the class's levels run. Other sources skip
  those spells, and the pools still offer them.
- **Removed:** `classLevel` and the per-level choice ids `class:<c>:<n>:cantrips`,
  `:spells` and `:spellbook`.
- **Content:** `content/dnd-5e/builds/vex.json` and `liriel-moonwhisper.json`
  were moved to the pooled ids with the same spells. Both still plan with 0
  pending and 0 problems.
- **Not migrated:** drafts saved in the database lose any spell picks made
  under the old per-level ids. They show as pending again.
- **Tests:** `Dnd5eSpellPlannerTest` covers pooled counts and labels, the
  spellbook pool, the prerequisite after pooling, and the level-range formatting.
  `Dnd5eBuildMaterializerTest` fixtures were moved to the new ids.

Suggested commit: `Pool creation spell choices per class instead of per level`

---

## 2026-09-25 — Source Select dropdown mock

The owner asked for a dropdown whose option text can be styled, with the entry's
name on the left and its source-book abbreviation on the right, and the full book
title in a tooltip on hover. It replaces the native `<select>`.
Mock: `design-reference/mockups/d2-builder/source-select/index.html`. It has three
style variants (A condensed + badge, B plain + muted code, C serif + outline tag),
a filter box on long lists, optional grouping by book, keyboard navigation, and
tap-to-show tooltips on touch screens.

Found while building it: the API only exposes `sourceBook` as the full title, so
the real component also needs the 5etools abbreviation exposed. The owner saw the
scrolling list clip the top row's tooltip. The mock now draws a single floating
tooltip on `<body>`, which flips below the abbreviation when there is no room
above. The real component must do the same (a portal). The owner chose variant A,
built in "Source Select: builder dropdowns with book codes" above.

---

## 2026-09-25 — D2: live summary, Beyond-style abilities, spell picker, Basics

After testing D2a, the owner found the builder too bare: Basics nearly empty,
spells rendered as a wall of buttons, changing the ability method did nothing,
and the side panel showed only the name. Built in response (details in
`features/character-creation.md`, "Built 2026-09-25"):

- **Live summary (D2b):** a preview computed by the API from the incomplete build.
- **Abilities (D2g):** the Beyond layout.
  - The method rules come from the API as option `data`.
  - Switching method asks first, then clears the scores.
  - Point buy shows the points remaining and offers only affordable scores.
  - Totals show sourced bonuses.
  - Score Calculations has Other Modifier and Override Score, a new
    `abilityScoreAdjustments` field in the build.
- **Spell picker (part of D2e):** search, and filters by level, school, damage
  type, casting time, source, concentration and ritual, with an expandable row.
- **Basics (D2c):**
  - a new build `preferences` field;
  - a source multi-select that really filters the builder's options;
  - PHB and DMG 2014 locked on;
  - `GET /api/catalogue/sources`;
  - switches for optional class features, feat and multiclass prerequisites,
    advancement, fixed/rolled HP, encumbrance and coin weight.
  - Partnered content says plainly that no partner book is imported.

Verification: `:apps:api:check` green. Web: `tsc` and 51 tests pass. Live, a
High Elf Wizard showed the cantrip and spellbook pickers, point buy with the
High Elf bonuses, an Other Modifier recalculated by the API, and the live summary.

Suggested commit: `feat: live summary, abilities, spell picker and basics in the character builder`

## 2026-09-24 — D2a: creation drafts and the end-to-end builder skeleton

The first D2 slice (see `features/character-creation.md`, "D2a as built").

- **Drafts:** a character can exist as a draft. Migration V5 adds `status` and
  `creation_draft`. The build may be incomplete: the planner turns missing
  species, background, classes or ability scores into pending `build.*` choices.
  The strict checks live in the `Complete` validation group, used only when the
  build is materialized.
- **Endpoints:** create, get, save (autosave) and finish a draft. The sheet and
  roll endpoints answer 409 for a draft.
- **Frontend:** `react-router` (a new dependency, recorded in `tech-stack.md`),
  and the three-column builder with its checklist, autosave, generic choice
  picker, and D&D step pages. "Create character" now starts a draft.
- **Verification:** `:apps:api:check` green; web `tsc`, 46 tests and the build
  pass. Migration V5 applied on the local database. Verified live on 2026-09-25:
  a Mountain Dwarf Fighter / Folk Hero went from "Create character" to a finished
  sheet with the expected values.
- **Fix found in that walk:** species, background and class options are sorted
  by name, then by source book.
- **System choice first:** "Create character" now opens a system picker
  (`/characters/new`) before the builder, as the owner asked. The listing's
  name + system form did nothing, silently, when the name was empty; it was
  replaced.

Suggested commit: `feat: add character creation drafts and builder skeleton (D2a)`

## 2026-09-24 — D2 plan drafted

`features/character-creation.md` Stage D2 now has a plan. It lists:
- what exists: the planner, the materializer, the catalogue API;
- the gaps: drafts, the partial preview, preferences on the build, the round-3
  build fields, routing, portrait upload;
- ten slices D2a–D2j, what stays out of scope, and four owner decisions.

Nothing is built yet; it awaits approval.

## 2026-09-24 — D2 builder sneak peek, round 3

The owner asked for adjustments to the chosen three-column structure. They are
built in `design-reference/mockups/d2-builder/round-3/` (`index.html`, `builder.js`,
`builder.css`) and listed in `features/dndbeyond-builder-walkthrough.md` §6b:

- the sheet's frame and fonts;
- a sources multi-select with PHB 2014 locked;
- partnered publishers;
- advancement, encumbrance and coin weight;
- multiclass and higher starting levels, with a level preview;
- Beyond-style abilities, with other modifier and override;
- Beyond-style equipment, with Wear/Wield so items start equipped;
- a roomier review.

A follow-up pass made three more changes:
- the frame weight is fixed: the SVG got an intrinsic size, the sides stay at
  14px, and the middle sections are a thin 8px;
- DMG 2014 is locked alongside PHB 2014;
- the summary's abilities are a compact strip.

The equipment accordions also use the thin frame, and the side-frame titles are
now uppercase. Verified live through the IDE's built-in server (localhost:63342).

The example is now Fighter 3 / Rogue 1. Checked with `node --check` and a smoke
render of every page, but not opened in a browser (no local server running).

Suggested commit: `docs: add round-3 D2 builder mockup`

## 2026-09-24 — D2 layout chosen

The owner chose round-2 suggestion 1 (three columns: checklist · step form · live
summary) as D2's structure, with adjustments still to come. It is recorded in
`features/dndbeyond-builder-walkthrough.md` §6b and in the Stage D2 section of
`features/character-creation.md`.

## 2026-09-24 — D2 builder sneak peeks, round 2

The owner gave feedback on round 1, now in `features/dndbeyond-builder-walkthrough.md`
§6b. Three new navigable shells are in `design-reference/mockups/d2-builder/round-2/`,
and every step page opens through hash routing. The shells share
`builder.js`/`builder.css`:

- step pages: Basics, Species, Class, Background, Abilities, Equipment, Review;
- the checklist with progress;
- the live summary.

Abilities are redesigned as rows. Checked with `node --check` and a smoke render.
They weren't opened in a browser, because the local server was reaped for memory.

Suggested commit: `docs: add round-2 D2 builder mockups`

## 2026-09-24 — D2 builder sneak peeks (layout mockups)

The owner asked for simple visual options for the creation screen. There are five
static, low-fidelity HTML mockups in `design-reference/mockups/d2-builder/`, with
`index.html` linking them and a shared `mockup.css`. They all use the same example
character:

- A: tabs plus a live mini-sheet preview;
- B: a checklist sidebar with sub-choice status;
- C: species cards with a compare drawer;
- D: a single-page form with a sticky summary;
- E: one question at a time, with a level timeline.

Not app code, and none of it is wired to the API.

Suggested commit: `docs: add D2 builder layout mockups`

## 2026-09-24 — D2 research: D&D Beyond builder walkthrough

The owner asked for a live look at how D&D Beyond guides character creation
before D2 is planned. The walk created "test", a Fighter 1 / Folk Hero / Mountain
Dwarf, from the listing page through Class, Background, Species, Abilities (point
buy and standard array), Equipment, portrait and What's Next, then opened the
sheet.

- New `features/dndbeyond-builder-walkthrough.md`:
  - the structure of every step;
  - what works (preview modals, pending-choice badges, constraints built into
    the options, and ability totals with sourced bonuses);
  - what doesn't (a wall of preferences up front, no final review, silent resets,
    UI-only constraints, edition mixing, a hidden commit step for equipment);
  - takeaways and five open questions for the owner.
- `features/character-creation.md` Stage D2 links it.

No code changed. Left out: Manual/Rolled, the gold alternative, level-up,
Quickbuilder and Premade.

The owner then answered the five questions, recorded in the walkthrough doc §6:
- free navigation, laid out Species → Class → Background;
- a draft with autosave;
- upload plus a placeholder per species;
- preferences: sources, HP fixed/rolled, optional class features, prerequisites;
- no d8 characteristic tables, since personality is written on the sheet later.

Suggested commit: `docs: record D&D Beyond builder walkthrough for D2`

## 2026-09-24 — Session renewal (Keycloak tokens no longer lapse after 5 minutes)

The owner asked for this before starting D2. It resolves the "Expired sessions
aren't renewed" gap in `open-items.md`.

**Root cause:** `Callback.tsx` exchanged the login code inside a `useEffect`,
and React's StrictMode runs effects twice in development. The second exchange
reused the code, and Keycloak treats that as an attack: it logged "Code …
already used" and dropped the session's client. From then on, every refresh
failed with "Session doesn't have required client". Nothing renewed the token
anyway, so after 5 minutes (`accessTokenLifespan` 300) every call got a 401,
shown as "failed to reach the server".

**What changed (web only; the realm is unchanged, and refresh tokens were
already issued):**

- `Callback.tsx` exchanges the code once per page load (a module-level
  promise).
- `oidcConfig.ts` sets `automaticSilentRenew: true`, so the refresh token
  renews the access token before it expires.
- New `auth/session.ts`:
  - `renewSession` deduplicates concurrent renewals;
  - `currentUser` renews an expired token first;
  - `redirectToLogin`.
- `apiFetch`:
  - uses `currentUser`;
  - on a 401, renews once and retries (the same body);
  - when the session can't be renewed, goes to the login page instead of
    failing silently.
- `AuthProvider` renews a lapsed session silently on load, and follows later
  renewals (`addUserLoaded`).

**Verification:**

- Tests: `client.test.ts` (3) and `Callback.test.tsx` (1, StrictMode → one
  exchange). `npm test` (37) and `npm run build` pass.
- Checked live:
  - after the fix, a fresh sign-in holds a refresh token, and `signinSilent`
    returns a new token (297 s);
  - Keycloak logs no more code reuse or refresh errors;
  - nothing loops: no extra requests while idle.
- Not observed live: the timer-driven renewal itself. Edge's sleeping tabs
  suspend a backgrounded tab after about 5 minutes, which also stops the
  automation. Returning to such a tab goes through `currentUser` / the 401
  retry, which the tests cover.

**Suggested commit message:** `Session renewal: single code exchange, automatic token refresh, retry on 401`

---

## 2026-09-24 — Stage C closed: live check against D&D Beyond

- **Changed to match D&D Beyond:**
  - save notes show only the restriction ("[A] against poison"), with the
    source in the icon's tooltip;
  - Unarmed Strike's subtitle is "Melee Attack";
  - `ActionsTab` detects the unarmed row by its `unarmed-strike` key
    (`isUnarmed`), and `AttackRow` takes an `unarmed` flag for its icon.
- **Verified live:** Aria, Liriel and Vex.
- **Status:** the Stage C audit is closed and D2 is unblocked (`roadmap.md`
  phase 11).
- **Tests:** `npm test` (33), `npm run build` and the backend tests pass.

**Suggested commit message:** `Stage C closed: match D&D Beyond save notes and unarmed strike label`

---

## 2026-09-24 — Stage C audit fixes (F2–F7) and second pass

**What changed:** detail in `rulesets/dnd-5e-data-fidelity-audit.md`.

- **F2:** Unarmed Strike is derived for every character (PHB 195: 1 + STR,
  no dice).
- **F3:** worn armor adds −10 ft. speed below its Strength requirement and
  disadvantage on the new `STEALTH_CHECKS` target. Speed now applies `BONUS`.
- **F5:**
  - `Dnd5eModifier.restriction` → `RollNote` / `VitalsZone.rollNotes`;
  - the saves panel lists the notes;
  - Dwarf and Elf (PHB) species overlays.
- **F4:**
  - overlay `grants.languages`, granted by the planner;
  - Rogue's Thieves' Cant overlay, and Draconic added to Dragon Ancestor's;
  - language labels resolve `<key>-phb`.
- **F7:** background grants carry the background's name.
- **F6:** `grantOrReplace` offers a replacement for a duplicate skill or tool.
  Vex took Poisoner's Kit (owner's pick).
- **Content:** classes, subclasses and species re-ingested and imported; the
  builds were re-applied; Liriel's Mage Armor was restored in the database.

**Verification:**

- `:apps:api:check` is green at 416 tests; `npm test` (33) and
  `npm run build` pass.
- The `--print-sheets` second pass confirms F2 and F4–F7 on the three
  characters.
- The live UI check of the new row and notes is pending: the API needs a
  restart and a login.

**Suggested commit message:** `Stage C audit fixes: unarmed strike, armor drawbacks, situational notes, prose grants, duplicate proficiencies`

---

## 2026-09-24 — Stage C audit, first pass

- **New dev runner:** `CharacterSheetPrintRunner`, run with
  `bootRun --args='--print-sheets=<dir> --player=test --server.port=0'`. It
  writes each character's calculated and stored sheet as JSON, so an audit
  needs no browser or token, and it can run beside a running API.
- **New audit document:** `rulesets/dnd-5e-data-fidelity-audit.md`, covering
  Aria, Liriel and Vex. Every value on the vitals zone and the tabs was traced
  to a rule or build choice; the key facts were re-read from 5etools.
- **Findings:**
  - F1: starting equipment is unequipped (owner decision);
  - F2: no Unarmed Strike;
  - F3: armor Strength requirement and stealth disadvantage aren't applied;
  - F4: prose-only language grants (Thieves' Cant, Draconic);
  - F5: situational save notes (Dwarven Resilience, Fey Ancestry);
  - F6: a duplicate tool proficiency has no replacement choice (owner
    decision);
  - F7: background grants are labelled "Background";
  - F8: there's no Aid overlay (owner decision).

**Suggested commit message:** `Stage C audit: sheet print runner and first-pass findings`

---

## 2026-09-24 — C2b: hit dice per die size (multiclass)

**What changed:** detail in `features/character-creation.md`, "C2b".

- `Dnd5eClassLevel.hitDiceUsed` tracks spent dice per class.
- `Dnd5eHitDicePools` builds the pools, and handles spending, the default
  recovery, the chosen recovery and the legacy normalization.
- `HitDice.pools` and `longRestRecoveryMax`.
- The hit-die roll takes the die size as its key.
- Endpoints:
  - `/hit-dice/spend` takes an optional `dieSize`;
  - `/rest/short` takes `hitDiceBySize` and returns `rolls`;
  - `/rest/long` takes an optional `hitDiceRecovered` (the owner's decision: the
    player chooses) and rejects an invalid choice with 400.
- **UI:**
  - the Short Rest panel has a box and a field per die size;
  - the Long Rest panel lets the player choose dice to recover when a choice
    exists.

**Verification:**

- `:apps:api:check` is green at 408 tests. `npm test` (31) and `npm run build`
  pass.
- A short rest through the API on Vex rolled d8 and d6 separately.
- Checked live from the UI:
  - a per-size short rest;
  - the long-rest choice (prefilled largest first; choosing all d6 left the d8
    spent);
  - the plain button when there is no choice.
- Vex is left with 2 d8 spent, because the 5-minute token expired before a last
  long rest; see `open-items.md`.

**Suggested commit message:** `C2b: hit dice pools per die size, per-size short rest and chosen long-rest recovery`

---

## 2026-09-24 — C2a: show where build-derived values come from

The owner approved C2a and C2b. For hit dice on a long rest, the player chooses
which dice come back.

**What changed:** detail in `features/character-creation.md`, "C2a".

- `Dnd5eProvenance` turns `Dnd5eSheet.derivation` into contributions:
  - ability scores, including an item's Set Score;
  - hit points per level, with average levels merged;
  - speed, with condition changes;
  - save and skill proficiency sources.
- `VitalsZone.provenance` / `CharacterSheetResponse.provenance`.
- `SourcedGrant.label`, recorded by the materializer.
- **UI:**
  - the ability explainer shows "Total Score" and its sources;
  - the speed explainer;
  - the "Max HP breakdown" in the HP panel;
  - proficiency source tooltips.

**Verification:**

- Backend tests are green: `Dnd5eProvenanceTest` plus the updated
  materializer test; `:apps:api:check` result below. `npm test` (27) and
  `npm run build` pass.
- Checked live on Vex and compared with D&D Beyond's Ability panel. The
  differences are noted in the handoff.

**Note:** re-applying the builds resets play state by design. Liriel's Mage
Armor was restored by casting it again.

**Suggested commit message:** `C2a: show ability, HP, speed and proficiency sources from the build`

---

## 2026-09-24 — C2 plan written (awaiting approval)

Documentation only. `features/character-creation.md` has the C2 plan. The
original C2 definition was superseded by C1b, which already regenerates the
flat fields. What remains:

- **C2a:** show `derivation` as contributions: ability score breakdown (D&D
  Beyond's Ability panel), per-level hit points, speed, and proficiency sources.
- **C2b:** per-class hit dice pools, for short and long rests.

One decision is open: the order in which mixed hit dice are recovered on a long
rest.

---

## 2026-09-24 — C3e: magic-item mechanics from 5etools structured fields

**What changed:** full detail in `features/character-creation.md`'s handoff, "C3e".

- **Ingestion:** `ItemMechanicsMapper` turns these fields into `data.mechanics`
  (198 items):
  - non-armor `bonusAc`, `bonusSavingThrow`, `bonusSpellAttack` and
    `bonusSpellSaveDc` become modifiers;
  - `ability.static` becomes `SET <ABILITY>_SCORE`;
  - `resist`/`immune`/`vulnerable`/`conditionImmune` become defenses.
- **Sheet:**
  - new `Dnd5eItem.mechanics`, copied once through the new
    `addCatalogueItem(..., itemDataJson)` overload;
  - `Dnd5eItem.active()` means equipped, and attuned when required.
- **Calculation:**
  - active items join `Dnd5eModifiers`;
  - saves, spell attack and save DC take bonuses;
  - defenses are merged;
  - set ability scores apply first (`Dnd5eFormulas.withItemAbilityScores`, also
    used by `maxHitPoints`).
- **Fixed:** a Cloak or Ring of Protection's +1 AC was ignored, because only
  armor and shields applied `armorClassBonus`.
- **Regenerated and imported:** items (1,773).

**Left out on purpose:**

- additive ability scores (a worn Ioun Stone and a read Manual share one shape);
- potions;
- `bonusAbilityCheck`, `bonusProficiencyBonus`, `modifySpeed`, `critThreshold`;
- conditional resistances.

**Verification:**

- `:apps:api:check` is green at 390 tests.
- Checked live on Aria with a catalogue Cloak of Protection:
  - equipped only: no change;
  - equipped and attuned: AC 14 with "Cloak of Protection +1", every save +1,
    and a Dexterity save rolled `1d20+4`;
  - the cloak was removed afterwards.

**Suggested commit message:** `C3e: magic-item mechanics (AC, saves, spell attack/DC, set scores, defenses)`

---

## 2026-09-24 — C3d: Self/Ally target for lasting-effect spells

The owner asked for a way to show whether a spell is cast on yourself or on an
ally, following the app's style. The owner also confirmed that End stays in the
Conditions sidebar.

**What changed:**

- **Cast panel:** a "Target: Self | Ally" row, built with the shared
  `FilterChips`, shown only for spells whose catalogue data has an `effect`. A new
  `lastingEffectSpells.ts` context is built from the catalogue spells the sheet
  already loads.
- **Backend:**
  - `SheetMutator.castSpell` takes `onSelf`; `CastSpellRequest.onSelf` is
    optional and defaults to true;
  - an ally effect is tracked under `<spell>-ally`, and its modifiers don't
    apply (`Dnd5eActiveEffect.onSelf`, `appliesToCharacter()`);
  - `ActiveEffectInfo`/`ActiveEffectResponse.onSelf`.
- **Conditions:**
  - the summary shows "(Ally)";
  - the sidebar rows read "On you" or "On an ally";
  - End buttons are labelled with the target;
  - rows clear the sidebar's floated close button.
- **Tests:** two new `Dnd5eActiveEffectsTest` cases, one controller case,
  `SpellCast.test.tsx` (3), and one extra `ConditionsPanel` case.

**Verification:**

- `:apps:api:check` is green at 377 tests.
- `npm test` (24) and `npm run build` pass.
- Checked live on Liriel:
  - an ally cast kept AC 16;
  - both effects were listed, and ending the ally one left the self one;
  - the slot spent by the test was restored.

**Resolves** the C3d target question in `open-items.md`.

**Suggested commit message:** `C3d: Self/Ally target for lasting spell effects`

---

## 2026-09-24 — C3d: active effects (cast Mage Armor, end, concentration, rests)

**What changed:** full detail in `features/character-creation.md`'s handoff, "C3d".

- **Overlay:** `content/dnd-5e/mechanics/spells/` holds spell effects;
  `mage-armor.json` is the first. `SpellConverter` merges `data.effect`.
- **Backend:**
  - `SheetMutator.castSpell`: spends the slot, ends the previous concentration
    effect, and copies the spell's effect from the catalogue at cast time;
  - `SheetMutator.endActiveEffect`;
  - rests end only the effects whose `endsOnRests` matches;
  - endpoints `POST /spells/{key}/cast` and `/active-effects/{key}/end`;
  - `ActiveEffectInfo` in `VitalsZone` and the sheet response.
- **Frontend:**
  - Cast sends `CAST_SPELL`;
  - active effects join the Conditions summary;
  - the Conditions sidebar lists them with End.
- **Regenerated and imported:** spells (525; only Mage Armor carries an effect).

**Changed from the plan:** the effect is copied when the spell is cast, not
onto the build's spells when they are learned. This mirrors conditions and
leaves `Spell`/`Dnd5eSpell` untouched.

**Verification:**

- `:apps:api:check` is green at 374 tests. New tests: `Dnd5eActiveEffectsTest`,
  spell overlay cases, controller cases.
- `npm test` (20) and `npm run build` pass.
- Checked live on Liriel: AC 13 → 16 with "Base (Mage Armor) +13", the slot was
  spent, the effect survived a long rest, and End restored AC 13.

**Left open:** effects always apply to the caster. The target choice is in
`open-items.md`.

**Suggested commit message:** `C3d: active effects from cast spells (Mage Armor), end, concentration and rest endings`

---

## 2026-09-24 — C3c frontend: forced roll mode markers; C3b and conditions imported

**What changed:**

- `RollModeMarker` now reuses the existing advantage/disadvantage icon files
  through `FrameIcon` instead of its own inline SVG copy.
- Where the marker shows follows D&D Beyond's own source in `design-reference/`:
  - a skill adjustments column (`SkillRow`, `.skill-row__adjustments`);
  - a badge at the initiative box's bottom-left (`Initiative`);
  - one summary line per forced saving throw below the saves grid
    (`SavingThrowsPanel`).
- Attack rows get no marker, since D&D Beyond shows none there. The roll still
  applies the forced mode server-side.
- New test: `RollModeMarker.test.tsx`.
- Ingested 15 conditions (PHB) and imported them into Postgres. Also imported
  classes (14), subclasses (130) and species (134) with the C3b overlay data,
  and re-applied the three builds.

**Verification:** `npm test` passes (17 tests) and `npm run build` passes.

Checked live on Aria with Poisoned and Restrained on:

- the markers showed on skills, initiative and the saves line;
- a skill check rolled `2d20kl1`;
- speed dropped to 0.

The C3b counters matched too: Second Wind 1, Action Surge 2, Indomitable 3,
Superiority d12 × 6. Both conditions were turned off afterwards.

**Suggested commit message:** `C3c: show forced advantage/disadvantage on skills, initiative and saves`

---

## 2026-09-24 — C3b built, C3c backend built (work in progress, handoff)

The owner approved all remaining Stage C steps. Work stopped mid-C3c at the
owner's request; `features/character-creation.md`, "Stage C progress — handoff",
lists exactly what is done and what is next.

**C3b (built):**

- feature uses (fixed count, class-table column, `add` and raised counts, die);
- action rows linked to one trait counter (`Dnd5eFeatureAction.traitKey`);
- species trait overlays;
- 14 overlay files;
- `Dnd5eFeatureUses`.

**C3c backend (built):**

- a `CONDITION` catalogue kind with 12 overlay files (conditions and
  exhaustion levels);
- condition modifiers copied onto the sheet when a condition turns on;
- roll modes in `VitalsZone.rollModes`, applied by `RollService` (this replaces
  the hard-coded Overloaded rule);
- speed and hit point maximum from `SET`/`HALVE`;
- `Dnd5eSheet.conditionModifiers` and `activeEffects`.

**Verification:** `:apps:api:check` is green with 362 tests. New test classes:
`Dnd5eFeatureUsesTest` and `Dnd5eConditionEffectsTest`, plus a `RollServiceTest`
case for spell attacks.

**Not done yet:**

- the C3c frontend: `RollModeMarker.tsx` exists but isn't wired;
- condition ingestion and import;
- the Postgres import and live check for C3b;
- C3d, C3e, C2 and the C audit.

---

## 2026-09-24 — C3b plan written (awaiting approval)

Documentation only. `features/character-creation.md` has the C3b plan:

- feature uses (fixed, class-table column, added to or raised by later
  features) and action rows in the adr-0007 overlay;
- the uses combined at materialization;
- one counter shared by the Features and Actions tabs through a new
  `Dnd5eFeatureAction.traitKey`, as on D&D Beyond.

---

## 2026-09-24 — C3a: feature modifiers on AC, HP and attacks; Manage Feats restored

**C3a (approved by the owner).**

- **Vocabulary:** `Dnd5eModifier`, `Dnd5eModifierType` (`BONUS`, `SET`,
  `SET_BASE`) and `Dnd5eModifierTarget` (`ARMOR_CLASS`, `ARMORED_ARMOR_CLASS`,
  `UNARMORED_ARMOR_CLASS`, `HIT_POINTS_PER_LEVEL`, `EXTRA_ATTACKS`), named after
  D&D Beyond's enums.
- **Overlay:** `FiveEToolsMechanicsOverlay` targets feats and optional features
  by name + source, with per-type match checks. New files: Extra Attack ×3,
  Draconic Resilience, Tough, Defense.
- **Sheet:** feature traits carry their modifiers, copied at materialization.
  `Dnd5eModifiers` feeds AC, max HP (also in `Dnd5eFormulas`, so damage and
  healing caps agree) and the new `attacksPerAction`, which replaces the Actions
  tab's fixed 1.
- **Results:**
  - Aria: 264 HP (was 224), 4 attacks per action, AC 19 when armored;
  - Vex: 45 HP (was 42), AC 17 unarmored.
- **Verification:** `:apps:api:check` is green with 350 tests; `npm test` has 11
  tests. Checked live.

**Manage Feats restored (owner report).** The Features tab only rendered the
Feats section, which holds the only "Manage Feats" button, when the character
already had a feat. Liriel and Vex have none, so the button disappeared. The Feats
section now always renders under "All" and "Feats", as on D&D Beyond; the style
is unchanged.

- Covered by `FeaturesTab.test.tsx`.
- Checked live on Liriel: the panel opens with "Known Feats (0)".
- The Vitest setup now unmounts between tests (`afterEach(cleanup)`); without it,
  one test's DOM leaked into the next.

---

## 2026-09-24 — C3 slices and C3a plan written (awaiting approval)

Documentation only. `features/character-creation.md` splits C3 into five slices:

- **C3a:** modifiers on the sheet (AC, HP, attacks);
- **C3b:** feature uses;
- **C3c:** conditions and roll modes;
- **C3d:** active effects;
- **C3e:** item modifiers.

It also details C3a: the D&D Beyond-named vocabulary, overlay files for feats and
optional features, modifiers carried on `featureTraits`, and the expected numbers
for Aria, Vex and Liriel.

---

## 2026-09-24 — Pending items resolved

The owner answered every open decision in `open-items.md`.

1. **XDMG/XMM files** and **2. `edition: "one"` content:** remove from `dnd-5e`.
   The 2024 data stays available for a future 5.5e system (the raw 5etools data
   is untouched).
3. **Names for six unlisted codes** were supplied by the owner and added to
   `FiveEToolsSourceNames`.
4. **Feats from 2024-rules books:** none of these books is a collab, so they
   leave `dnd-5e`. Implemented as a rule, not a list: every source published on
   or after the 2024 PHB (adr-0006 amendment; 35 sources). The ingestion runner
   now deletes files it no longer produces.
   - Catalogue: spells 578 → 525, items 2,015 → 1,773, feats 228 → 108,
     languages 167 → 150, optional features 163 → 151, species 140 → 134.
   - Postgres was re-imported and matches the files.
5. **DMG firearms in "any martial weapon":** kept (D&D Beyond has them too).
6. **Starting equipment:** not auto-equipped; the player equips it.
7. **Spells tab header:** follows D&D Beyond's `generateSpellCasterInfo`.
   - Only class casters are shown (the new `SpellcastingClassInfo.classCaster`).
   - Classes sharing a value show it once, with every class in its tooltip; a
     Bard/Paladin with the same CHA shows one "+4".
   - A sheet whose only casters are granted sources still shows theirs.
   - Implemented in `spellcastingHeader.ts`.
8. **Spells without class lists:** 5etools' generated
   `gendata-spell-source-lookup.json` has **subclass** spell lists (dunamancy
   under Wizard → Chronurgy/Graviturgy, Eldritch Knight, domains, patrons).
   `SpellConverter` writes them as `subclasses`, and the planner adds them to the
   class's options. Only Encode Thoughts (GGR) is still unlisted; it comes
   through grants.
9. **"Player's Handbook (2014)"** is now "Player's Handbook", in the catalogue,
   the overlay citations and `Dnd5eChoiceOptions`.
10. **Tasha's optional features:** unchanged, deferred to phase 11.
11. **D&D Beyond builder comparison:** deferred to phase 11.
12. **Frontend test tooling:** Vitest 3.2 + Testing Library + jsdom (recorded in
    `tech-stack.md`). `npm test` runs 9 tests: `spellcastingHeader` and
    `ProficiencyDot`.

**Verification:**

- `:apps:api:check` is green with 343 tests; `npm test` and `npm run build` pass.
- The three builds still plan with 0 pending choices and no problems, and were
  re-applied to `test`.
- Checked live: Liriel's Spells header shows a single "+4 · +7 · 15". The High
  Elf's Minor Illusion stays in the cantrip list.

---

## 2026-09-23 — D1b: the three target characters built and applied

- **Build files:** `content/dnd-5e/builds/aria-emberfall.json`,
  `liriel-moonwhisper.json` and `vex.json`, all with 0 pending choices and no
  problems.
  - Aria's choices are the owner's own, answered in two rounds.
  - The owner delegated Liriel's and Vex's choices to the assistant, keeping the
    standard array and starting equipment.
- **Applied** to the local `test` user with `--apply-builds`: Aria replaced,
  Liriel and Vex created.
- **Checked live** at localhost:5173. Findings recorded in `open-items.md`:
  - starting gear is unequipped (Aria's AC 13, no attacks);
  - the species caster is repeated in the Spells header;
  - two new catalogue questions: feats from 2024-rules books, and DMG firearms
    in "any martial weapon";
  - Tough HP and Extra Attack, which are C3.

---

## 2026-09-23 — D1a: apply build files to characters

**Owner approved D1**, with two decisions:

- Aria is rebuilt from scratch; no showcase data is kept.
- No user gets default characters. The three target characters exist only for
  the local `test` user.

**Built:**

- **Build files:** the `CharacterBuildFile` wrapper for `content/<system>/builds/`.
- **Catalogue:** `CatalogueService.lookup` returns a `DatabaseCatalogueLookup`
  (Postgres).
- **Applier:** `CharacterBuildApplier` materializes a build, adds starting items
  and coins through `CharacterSheetService.withStartingEquipment`, and creates or
  replaces the character by name. `BuildNotReadyException` covers builds that
  aren't ready.
- **Runner:** `CharacterBuildRunner`, run as
  `--apply-builds=<dir> --player=<username>` (dev profile).
- **Players:** `PlayerService.playersNamed`.
- **`planBuild`** reads wrapped build files.
- **Removed:** `DevCharacterSeeder` and `DevCharacterSeederTest`.

**Fixed:** C1c compared 5etools source codes against full book names.
`SpellConverter` now writes `sourceCode`, `Dnd5eSpellLists` uses it, and the
spells were regenerated and re-imported.

**Verification:** `:apps:api:check` is green with 342 tests. New tests are in
`CharacterBuildApplierTest` and `Dnd5eSpellListsTest`.

---

## 2026-09-23 — D1 plan written (awaiting approval)

Documentation only. `features/character-creation.md` now has the D1 plan:

- **D1a:**
  - build files in `content/dnd-5e/builds/`;
  - a Postgres-backed `CatalogueLookup`;
  - a generic `CharacterBuildApplier`;
  - the dev seeder driven by build files;
  - an `--apply-builds` runner to rebuild existing characters.
- **D1b:** the three conversations.

Two owner questions are open: what happens to Aria's current play state, and
which characters new players get.

---

## 2026-09-23 — C1c: spells in the build

The owner approved the C1c plan, with pact slots deferred to a separate slice.

**Built:**

- **Ingestion:** `SpellConverter` adds `classes` and `optionalClasses` from 5etools'
  `spells/sources.json`.
  - A `classVariant` is optional only when an optional class feature from
    another book adds it (TCE's expanded lists). XGE, FTD and BMT additions, and
    TCE's own spells, are on the lists normally.
  - 2024-edition class references are dropped.
  - `FiveEToolsDataSource.hasDataFile`.
  - The 578 spell files were regenerated and imported into Postgres.
- **Planner:** `Dnd5eSpellLists` and `Dnd5eSpellPlanner`, called from
  `Dnd5eBuildPlanner`:
  - per-level cantrip, known and spellbook choices, using each class's own table
    (pact slot level for Warlocks);
  - a "prepared" choice for spellbook and whole-list casters;
  - `additionalSpells` from species, backgrounds, feats, subclasses and optional
    features: known, prepared, innate with usage, filtered choices, expanded
    lists, alternative sets and ability choices.

  `Dnd5ePrerequisites` now checks `spell` and `spellcasting*`.
- **Materializer:**
  - the sheet's spells are regenerated from the catalogue;
  - prepared flags and item-granted spells are kept;
  - non-class sources get their own caster entry;
  - `Dnd5eSpellPlanner.spellcasting` is shared for slots.

**Verification:**

- `:apps:api:check` is green with 340 tests. New tests are in
  `Dnd5eSpellPlannerTest`, the updated `Dnd5eBuildMaterializerTest` and
  `SpellConverterTest`, with shared fixtures in `Dnd5eBuildFixtures`.
- Real-data samples:
  - High Elf Wizard 5: 14 spellbook spells, 9 prepared, slots 4/3/2, and a High
    Elf INT caster;
  - Rogue 4 / Sorcerer 3: 4 cantrips and 4 spells from the Sorcerer's own table.

**Left out** (see `open-items.md`):

- pact slots;
- EK/AT school limits;
- DCs for ability-less optional-feature spells;
- the 69 spells with no class list;
- the live D&D Beyond check of a species caster entry in the Spells tab, which
  happens when D1 writes the Elf Wizard.

---

## 2026-09-23 — C1c plan written (awaiting approval)

Documentation only. `features/character-creation.md` now has the C1c plan:

- class spell lists from 5etools' `spells/sources.json`;
- granted spells from `additionalSpells`;
- per-level cantrip, known, spellbook and prepared choices, following D&D
  Beyond's three caster kinds;
- materialization that keeps prepared flags and item-granted spells.

Pact slots are proposed as a later slice.

---

## 2026-09-23 — C1b: materialize a build into the sheet, with provenance

**Owner decisions applied:**

- hit points use the average (maximum die at level 1, then `faces/2 + 1`);
- the sheet keeps the character level and each class's level;
- four proficiency levels (NONE/HALF/FULL/EXPERT), shown with D&D Beyond's
  "Proficiency dots" icons;
- `TREMORSENSE` added.

**Built:**

- **Seam:** `CharacterCreationFlow.materialize` → `MaterializedSheet`, with
  `StartingItem`. It refuses while choices are pending or problems exist.
- **Planner:**
  - `Dnd5eBuildPlanner` collects a `Dnd5eBuildOutcome`;
  - `plan()` is cached, so a second call doesn't re-apply grants;
  - an ability increase past 20 (or the source's own maximum) is now a plan
    problem.
- **Materializer:** `Dnd5eBuildMaterializer` regenerates:
  - abilities, level, class levels and `hitPointBase`;
  - proficiencies with expertise;
  - defenses and senses;
  - features, keeping `usedCount`;
  - spellcasting classes, and slots (multiclass via the full-caster table,
    keeping used slots);
  - the background feature.

  Play state is kept.
- **Sheet model:**
  - `Dnd5eProficiencyLevel`, `Dnd5eClassLevel`, `Dnd5eDerivation`;
  - `skillExpertise`, `classLevels`, `hitPointBase`, `derivation` and `build`
    in schema version 2;
  - `Dnd5eFormulas.maxHitPoints` uses `hitPointBase` when it's set;
  - the calculator adds the expertise contribution;
  - the API reports a proficiency level per skill and save.
- **Web:** `ProficiencyDot` renders the four levels. `SkillRow` and
  `SavingThrowRow` take a `proficiency` level.
- **CLI:** `planBuild --materialize`.

**Verification:**

- `:apps:api:check` is green with 332 tests. New tests are in
  `Dnd5eBuildMaterializerTest`, `Dnd5eMaterializedFieldsCalculatorTest` and
  `Dnd5eBuildPlannerTest`.
- Two real-data sample builds materialized correctly; the numbers are in
  `features/character-creation.md`, "C1b as built".
- Checked live at localhost:5173 on the current Aria:
  - the FULL (filled) and NONE (empty) dots render in the skill and
    saving-throw rows as before;
  - the EXPERT and HALF icons couldn't be seen, because no stored character
    has them yet. They get checked live once D1 writes the multiclass character
    (Stealth expertise).

**Left out:**

- spells and pact slots (C1c);
- feature-driven HP/AC and feature uses (C3);
- prose-only language grants;
- per-size hit dice UI (C2);
- database writes (D1).

These are listed in `open-items.md`.

---

## 2026-09-23 — C1b plan written (awaiting approval)

Documentation only. `features/character-creation.md` now has the C1b plan:
materialize a fully answered build into the sheet's build-derived fields, with
provenance.

- **Model gaps it closes** (all needed by the target characters):
  - `hitPointBase`, for mixed hit dice and rolled HP;
  - `classLevels`;
  - `skillExpertise`, with the proficiency bonus doubled in the calculator;
  - `TREMORSENSE`.
- **Provenance:** a `derivation` record.
- **Spell slots:** the multiclass slot rule reads a full caster's own table.
- **Equipment:** starting items are returned for D1's existing item-copy path.
- **Out of scope:**
  - spells (C1c);
  - feature uses and actions (C3);
  - per-size hit dice pools (C2);
  - database writes (D1).

---

## 2026-09-23 — C1a: build planning (pending choices) and `planBuild`

**Owner approved the C1a plan** after asking for D&D Beyond's own rules engine to
be studied (`Feat/`, `Choice/`, `Prerequisite/`). Adopted from it:

- choices owned by components, with `type`/`parentChoiceId`/`optional` and the
  "todo" rule;
- options filtered by what the character already has (background grants stay,
  labelled "(Background)"; expertise only among held proficiencies; maxed
  abilities dropped);
- prerequisite groups (any group passes, all conditions within it);
- prose-only choices as hand-authored data. adr-0007 was amended for choices, and
  its overlay moved to `content/dnd-5e/mechanics/`.

**Built.**

- **`ruleset`:** the generic `CreationChoice`, `CreationChoiceOption`,
  `BuildPlan`, `CatalogueLookup`, `CatalogueRecord`, `CharacterCreationFlow` and
  registry. These replace Stage B's `Dnd5eChoice`/`Dnd5eChoiceOption`, which are
  deleted.
- **D&D 5e:** `Dnd5eCharacterCreationFlow`/`Dnd5eBuildPlanner` walk species →
  background → classes level by level with a running `Dnd5eGrantState`. They
  emit:
  - structural `build.*` questions;
  - ability, skill, tool, language, weapon, armor, feat, resistance and size
    picks;
  - equipment (with category item picks and the gold alternative);
  - ASI-or-feat with sub-choices;
  - optional features per level;
  - overlay choices;
  - problems (standard array, point buy, multiclass minimums, unknown answers,
    unmet feat prerequisites).
- **`catalogue`:** `ContentDirectoryCatalogue`, `PlanBuildMain`, and the Gradle
  `planBuild` task.
- **Catalogue additions:**
  - `LANGUAGE` kind (167 entries);
  - feat mechanics;
  - `focusType` on items (all 1,931 regenerated items changed only by it);
  - 5etools' own lineage defaults for legacy MPMM-style species;
  - three overlay choice files (Rogue Expertise, Student of War, Dragon Ancestor
    with options from its own table).
- **Refactors:** `Dnd5eSkills` shared with `Dnd5eSheetCalculator`;
  `CatalogueContentLayout`; `FiveEToolsAbilityGrants`.

**Bugs found by running real builds and fixed:**

- Mountain Dwarf's species and subspecies grants became a false "pick one"; they
  now combine.
- The `toolArtisan`/`setGaming` equipment categories were missing.
- Duplicate Eladrin subspecies keys.
- "a Arcane" became "an Arcane".
- TCE optional features were offered by default.

**Verification.** `:apps:api:check` is green with 321 tests. The six changed
kinds were regenerated and imported into Postgres. The `planBuild` output for all
three target characters was checked against the PHB; details are in
`features/character-creation.md` (C1a "As built").

**Left out:**

- spells and cantrips (C1c);
- sheet materialization and provenance (C1b);
- the Postgres-backed `CatalogueLookup` (with D1);
- the optional-class-features and source toggles (D2);
- a live D&D Beyond builder comparison, which needs an account building these
  characters.

---

## 2026-09-23 — C1 split and C1a plan written (awaiting approval)

Documentation only. `features/character-creation.md`'s C1 is split into three
slices: C1a (pending choices), C1b (materialize with provenance) and C1c
(spells). The C1a plan covers:

- **the seam:** `CharacterCreationFlow`, as named in `architecture.md`;
- **the catalogue port:** a `CatalogueLookup` interface, implemented by the
  `catalogue` package over Postgres and over `content/` files;
- **`planBuild`:** a JavaExec task;
- **questions:** structural `build.*` questions vs. grant choices, and the full
  list of grant choices generated from the A1–A3 data;
- **validation problems:** standard array, point buy, and multiclass
  requirements;
- **two gaps:** feats need structured mechanics, and prose-only choices
  (Expertise, Student of War, Dragon Ancestor) need adr-0007's overlay extended
  from modifiers to choices, pending owner confirmation.

---

## 2026-09-23 — Stage B: character build model

**Owner approved option (a):** the sheet is materialized from a build, with
provenance. The owner also named the three target characters for D1, now listed
in `features/character-creation.md`'s D1:

- Aria as a Battle Master 20, Mountain Dwarf, Folk Hero;
- an Elf Wizard 5, Urchin;
- a Changeling Sorcerer 3 / Rogue 4, Urchin.

**Built.**

- **Records:** new `Dnd5eCharacterBuild` and its parts (`Dnd5eBuildClass`,
  `Dnd5eBuildChoice`, and the method enums), plus the non-persisted
  `Dnd5eChoice`/`Dnd5eChoiceOption`. They carry Bean Validation, including total
  level ≤ 20, unique classes, all six abilities, and unique choice ids.
- **`Dnd5eSheet`:** gains a nullable `build` field and `SCHEMA_VERSION` 2.
  Version-1 rows, including today's Aria, still read with `build == null`, so no
  migration is needed. The field was threaded through the 15 `with*` methods and
  12 test constructions.

**Verification.** `Dnd5eCharacterBuildTest` (5) passes, and `:apps:api:check` is
green with 310 tests.

**Docs.** `domain-model.md` defines Build and the 5e Choice. `database-schema.md`
lists the sheet versions.

**Left for C1:** the resolver that turns a build into sheet values and choices,
and the provenance format.

---

## 2026-09-23 — Stage B plan written (awaiting approval)

Documentation only. `features/character-creation.md`'s Stage B now holds a
concrete plan, written after reading `domain-model.md`, `database-schema.md` and
`Dnd5eSheet`.

- **Key decision for the owner:** (a) materialize the sheet from the build with
  provenance, which is recommended, versus (b) derive everything from the
  catalogue on every read.
- **Records:**
  - `Dnd5eCharacterBuild`;
  - deterministic choice ids;
  - `Dnd5eChoice`, which is not persisted;
  - a nullable `build` field on `Dnd5eSheet`, with `SCHEMA_VERSION` 2, while
    version-1 sheets still read.
- **Out of scope:** resolution logic (C1), a build-editing API, and source
  toggles.
- **Superseded:** the earlier `Dnd5eGrantedFeature`/`GrantedProficiency` idea.

---

## 2026-09-23 — 5etools ingestion: backgrounds (slice A3)

**Backgrounds.** New `BackgroundConverter` (`--kind=background`, the existing
`BACKGROUND` kind) writes 101 entries to `content/dnd-5e/backgrounds/`.

- **Soldier:** the hand-typed Soldier (sourced "SRD 5.1") is replaced by the real
  PHB entry under the same slug. The Background tab's suggestion lookup works
  unchanged.
- **Suggestion tables** are found by column label anywhere in the text. Other
  tables go to `otherTables`, features to `features`.
- **Grants and equipment:** grants use the shared grant shape, and starting
  equipment uses the class group shape.
- **Kept verbatim:** spells and campaign prerequisites.
- **Scope:** 70 `edition: "one"` backgrounds are dropped, and 26 copies are
  resolved.

**Shared changes.**

- **`FiveEToolsCopies.resolveAll`:** copy resolution moved here from
  `FiveEToolsSpeciesData`, which now uses it. Species were re-ingested and are
  byte-identical.
- **`_mod` support:** the new `insertArr` mode, and `replaceArr` selectors by
  `{"index": n}`.
- **`FiveEToolsStartingEquipment` grant shape:** now always carries
  `itemSlug`, `equipmentType`, `special`, `valueCp`, `quantity`, `displayName`,
  `containsValueCp` and `worthValueCp`. Unknown keys fail. Classes were
  re-ingested.
- **Web:** a comment in `CharacterSheetScreen.tsx` that described the old
  Soldier-only catalogue was corrected. The build passes.

**Verification.** `:apps:api:check` is green with 305 tests
(`BackgroundConverterTest`: 4). Classes and backgrounds were re-imported into
Postgres (`BACKGROUND`: 101; Soldier has 8 personality traits).

**Real data gap, documented rather than filled:** about 30 backgrounds, mostly
SCAG's, have no suggestion tables in 5etools.

---

## 2026-09-23 — 5etools ingestion: species (slice A2), pending decisions list

**Species.** New `SPECIES` kind and `SpeciesConverter` (`--kind=species`), writing
140 entries to `content/dnd-5e/species/`.

- **`FiveEToolsCopies`** resolves 5etools' `_copy`/`_mod`/`_versions`. It
  supports only the subset real data uses: 5 entries-only `_mod` modes, named
  versions, and `_abstract` templates with `{{variables}}`. Anything else fails
  ingestion.
- **`FiveEToolsSpeciesData`** resolves copies, including copies of 2024 races,
  which inherit `edition: "one"` and are dropped. It attaches subraces to their
  race, and expands versions; a subrace's versions are expanded against the
  combined race + subrace text, as 5etools does.
- **Subspecies are nested** in their species, which resolves the open question.
  Merge rule: scalars replace; lists add, unless `overwrite`.
- **Verified against the PHB:** Dwarf and Mountain Dwarf (Aria), Human
  default/Variant, the 10 Dragonborn colors, and Goblin across 7 books.

**Shared grant shape.** `FiveEToolsProficiencies.grantAlternatives` replaces the
separate skill/tool mappers. Every grant block is now `{fixed, choices: [{from,
category, fromFilter, count, amount}]}`. For class skill choices this changed
`choose` into `choices[]`. Classes, subclasses and optional features were
re-ingested; only that shape changed.

**Tests.** New `SpeciesConverterTest` (6) and `FiveEToolsCopiesTest` (3).
`:apps:api:check` is green with 301 tests. Classes, subclasses, optional
features and species were re-imported into Postgres (`SPECIES`: 140).

**Pending decisions.** New `.ai/open-items.md` collects everything waiting on
the owner:

- the stale `XDMG`/`XMM` files;
- `edition: "one"` content from non-X books;
- the 6 unnamed source codes;
- web test tooling;
- migrating copied item sources;
- known gaps.

It is indexed in `CLAUDE.md`.

---

## 2026-09-23 — Optional features (slice A1b) and a tag-stripping fix across the catalogue

**A1b.**

- **Converter:** the new `OPTIONAL_FEATURE` kind and `OptionalFeatureConverter`
  (`--kind=optional_feature`) turn `optionalfeatures.json` into 163 entries:
  Fighting Styles, Invocations, Maneuvers, Metamagic, Pact Boons, Arcane Shots,
  Elemental Disciplines, Infusions, Runes, and EFA renown. 58 `XPHB` entries
  were dropped.
- **Data:** `featureTypes` uses the same codes class and subclass progressions
  list. There are also `optional`, `prerequisiteText` (rendered by the new
  `FiveEToolsPrerequisites` like 5etools' classic style), verbatim
  `prerequisites`, `consumes`, `additionalSpells`, `senses`, `skillAlternatives`
  and `optionalFeatureProgressions`.
- **Progressions:** `FiveEToolsClassProgression` now reads the `{"*": n}`
  every-level form.
- **Cross-check:** all 95 option references in class and subclass features
  resolve, and every progression's feature-type code has options.

**`TagMarkupStripper` rewritten (bug found while checking A1b output).**

- **The bug:** entity tags are `name|source|display`, but the stripper rendered
  the 2nd segment. 376 descriptions showed a source code in place of a name
  (`{@spell fireball|xge}` → "xge", Superior Technique's "available to the phb
  archetype").
- **The fix:** it now mirrors 5etools' `Renderer.stripTags` per tag family,
  taken from `render.js`. Unknown tags fail ingestion, and payload-less tags
  like `{@h}` are handled.
- **Tests:** `TagMarkupStripperTest` was rewritten, because its old cases
  encoded the wrong convention.
- **Re-run:** all six kinds were regenerated, and 790 files changed text. The
  remaining source codes in text are the 21 stale `XDMG` files the owner kept
  and 15 items using an unresolved `{#itemEntry …}` template, an `ItemConverter`
  gap that predates this change.

**Verification.** `:apps:api:check` is green with 292 tests. All six kinds were
re-imported into Postgres (`OPTIONAL_FEATURE`: 163). Superior Technique's stored
text now reads "the Battle Master archetype".

**Left out:** resolving `{#itemEntry}` templates in `ItemConverter`.

---

## 2026-09-23 — Full source-book names from 5etools, catalogue re-imported, verified live

**Source names.** `sourceBook` now holds the full name from 5etools' own
`books.json`/`adventures.json`, keyed by each entry's `source` code rather than
its `id` (e.g. `PSK` → "Plane Shift: Kaladesh"). When a book and an adventure
share a code, the book wins.

- **Where it happens:** new `FiveEToolsSourceNames`. `Ingest5eToolsRunner`
  resolves each entry's top-level name, and `FiveEToolsClassData` resolves class
  and subclass features, which carry their own `sourceBook`.
- **What was removed:** the hardcoded 9-book map in `FiveEToolsNaming`
  (`sourceBookName`). Converters now emit the raw code, and their unit tests
  assert the code.
- **Name change for PHB and DMG:** 5etools' own names are used as-is, including
  "Player's Handbook (2014)" and "Dungeon Master's Guide (2014)", which are also
  how D&D Beyond labels the legacy books. Items already copied onto a character
  keep their old "Player's Handbook, p. N" source text (copy-once).
- **Codes left as-is:** TftYP, UATheMysticClass, HAT-LMI, RoTOS, EET and MCV2DC
  appear in neither file (only in 5etools' site JavaScript), so they stay as
  codes. The 83 stale `XDMG` files and 1 `XMM` file were not regenerated and
  still show their codes.

**Re-ingestion.** All five kinds were re-run: 2,329 files changed, and only in
`sourceBook`. `Ingest5eToolsRunnerTest` gained a source-name test covering a
book, an adventure matched case-insensitively, and an unlisted code.

**Verification, now that Docker Desktop is running:**

- `:apps:api:check` is green: 282 tests, including the new
  `CatalogueImportServiceTest` prune test.
- The catalogue was re-imported into Postgres (578 spells, 2,015 items, 228
  feats, 14 classes, 130 subclasses). The retired plain-slug rows (e.g.
  `aberrant-dragonmark`, `trinket`) were pruned.
- Checked live at localhost:5173 on Aria's sheet:
  - Manage Feats lists both Aberrant Dragonmark entries, labelled
    "· Eberron: Forge of the Artificer" and "· Eberron: Rising from the Last War";
  - Manage Inventory shows each item's source in every row.
- **D&D Beyond comparison not done.** The "always show the source" rule is a
  direct owner decision that intentionally goes beyond D&D Beyond's own pickers.

**Import tip.** `--import-catalogue` must be given a path relative to `apps/api`
(e.g. `../../content/dnd-5e/spells`). Gradle's `--args` splits on spaces, and the
repository path contains spaces.

---

## 2026-09-23 — Same-named catalogue entries from different sources are kept apart

**Owner rule**, now in `ground-rules.md`'s "Catalogue content": when two sources
publish an entity with the same name, both are kept and listings always show the
source. Previously the last file written silently won. Real data had 25 such
groups: 8 spells (e.g. Catnap XGE/AU), 16 items (e.g. Deck of Many Things
DMG/BMT, Trinket in 4 books), and 1 feat (Aberrant Dragonmark ERLW/EFA).

**Backend.**

- `Ingest5eToolsRunner` converts everything first. `FiveEToolsSlugs.disambiguate`
  then gives each member of a collision group a `<slug>-<source>` slug, and the
  runner deletes the retired plain-slug file. A same-name, same-source pair fails
  the run.
- The class/subclass converters' own fail-on-collision check was removed; the
  runner now covers every kind.
- `CatalogueEntryImport.withSlug` was added.
- `CatalogueImportService.importFrom` now deletes rows of each imported
  (system, kind) whose slug has no file anymore, so the retired slugs don't
  linger in Postgres as a third entry.

**Frontend.** New `CatalogueSourceLabel`. The Manage Feats, Manage Spells and
Manage Inventory pickers show "· <source>" after each name, and their button
aria-labels include the source.

**Re-ran all five kinds.**

- 25 files were retired and 52 added, exactly the collision groups.
- 24 other spell/item files changed: they contain `{@filter …}` tags, and the
  2026-09-21 `TagMarkupStripper` fix ("beasts of challenge rating 2 or lower"
  instead of "bestiary") had never been applied to spells and items until this
  re-run.
- No slug used by `DevCharacterSeeder` changed.

**Tests.**

- `Ingest5eToolsRunnerTest` gained 2 tests, and both pass.
- `CatalogueImportServiceTest` gained a prune test. It is **not run yet**,
  because Docker Desktop is off.
- Web build and typecheck pass. The web app has no test tooling; adding one is
  a dependency decision for the owner.

**Pending.**

- The live D&D Beyond comparison of the three pickers (ground rule for sheet UI)
  needs the stack running.
- Source labels show 5etools codes (e.g. `ERLW`, `EFA`) for books outside
  `FiveEToolsNaming`'s 9-name map. Reading full names from 5etools'
  `books.json`/`adventures.json` would fix that for every kind.

---

## 2026-09-23 — 5etools ingestion: classes and subclasses (slice A1)

New `CLASS`/`SUBCLASS` catalogue kinds, and `ClassConverter`/`SubclassConverter`
registered in `Ingest5eToolsMain`. Run against the real `tools/5etools-data/`,
they wrote 14 classes to `content/dnd-5e/classes/` and 130 subclasses to
`content/dnd-5e/subclasses/`. The 14 classes are the 12 PHB ones, the TCE
Artificer, and the UA Mystic. A re-run was byte-identical.

**Scope filter** (`FiveEToolsClassData`):

- drops classes tagged `edition: "one"` (the 2024 ruleset, including the `EFA`
  Artificer);
- drops sidekick classes;
- keeps a subclass only when its parent class is kept, which removes the 142 2024
  subclass copies that carry old sources.

**Owner decision:** Unearthed Arcana is imported, and gated per character by a
future opt-in toggle like collab sources. Recorded in adr-0006.

**What each entry carries:**

- **Classes:** hit die, saves, proficiencies (with choices), starting equipment
  groups, multiclassing, full spellcasting progression (slots, pact slots,
  cantrips/spells known, prepared formula), class-table columns,
  optional-feature progressions, subclass level, ASI levels, and resolved
  features with descriptions.
- **Subclasses:** introduction, features, `additionalSpells`, and their own
  spellcasting (Eldritch Knight, Arcane Trickster).

**Shared helpers:**

- `FiveEToolsEntries` holds the entries flattening extracted from
  `FeatConverter`. Feat output was re-run and is byte-identical.
- `FiveEToolsProficiencies`, `FiveEToolsStartingEquipment` and `FiveEToolsSlugs`
  (fails on a slug collision) are new.
- `FiveEToolsNaming` gained `camelCaseKey` and `abilityKey`, so catalogue keys
  match the sheet's own (`sleightOfHand`, `intelligence`).
- `Ingest5eToolsMain` pluralizes a kind ending in "s" with "es".

**Tests:** `ClassConverterTest` (10) and `SubclassConverterTest` (4) pass. The
full `:apps:api:check` has **6 Testcontainers suites failing only because Docker
Desktop was not running**; the other 262 tests pass. The Postgres import
(`--import-catalogue`) was not run for the same reason.

**Left out:**

- slice A1b (the optional features themselves, a new `OPTIONAL_FEATURE` kind);
- rendering tables inside feature text;
- Expertise and other prose-only choices (C1 + adr-0007);
- the multiclass slot table (C1).

**Found, not fixed:**

- the Aberrant Dragonmark feat slug collision between ERLW and EFA (228 feats
  convert into 227 files);
- 83 stale `XDMG` item files, which the owner chose to keep.

`database-schema.md`, `features/character-creation.md`,
`features/5etools-ingestion.md` and adr-0006 were updated.

---

## 2026-09-23 — Character-creation plan: target outcome, build resolver, effects engine

Documentation only. `features/character-creation.md` gained the owner's target
outcome: a conversational rebuild ("level 5 High Elf Wizard, Sage") where the
assistant asks every implied choice from real 5etools data. The resulting sheet
is living: equipment, active spells and conditions change derived values and roll
modes. Six acceptance scenarios now feed Stage C's audit.

Stage C split into three parts:

- **C1 — build resolver.** `Dnd5eCharacterBuild` goes in, and either pending
  choices or a materialized sheet comes out.
- **C2 — derive the flat fields.** The existing derive-don't-store work.
- **C3 — effects engine.** A `Dnd5eEffect` model, active spells with
  one-concentration enforcement, contributions for every value, and a roll-mode
  map consumed by `RollService`.

Stage D split into two:

- **D1 — assistant-driven rebuild.** Build files in `content/dnd-5e/builds/`, a
  `planBuild` Gradle task, and `DevCharacterSeeder` resolving builds. Not gated,
  because it produces the audit characters.
- **D2 — the UI flow.** Still gated on Stages A and C.

The recommended first milestone and three open questions are recorded: how to
encode prose-only spell/feature mechanics (a curated registry is recommended and
needs an ADR), whether round/turn tracking stays out of scope, and where
`planBuild` reads catalogue data from.

Also fixed a corrupted character ("手-typed" → "hand-typed") and updated
`roadmap.md` phase 11 to match. No code changed.

**Same day, follow-up: C3 redesigned after studying D&D Beyond's rules engine**
(`design-reference/.../rules-engine/es/`).

- **What D&D Beyond does:** modifiers are data (`type` + `subType` + value,
  `restriction`, attunement, origin), attached to every entity definition and
  collected into one global list from race, class, feats, background, equipped
  items and conditions. Spells are not a modifier source: D&D Beyond does not
  auto-apply Mage Armor or Aid.
- **What C3 now specifies:**
  - an enum-based `Dnd5eModifier` vocabulary;
  - modifiers from 5etools structured fields where they exist;
  - a curated overlay (`content/dnd-5e/modifiers/`) for conditions and prose-only
    features and spells, pending adr-0007 approval;
  - `activeEffects` for spells and features lasting 1 hour or more, ending on a
    short rest, a long rest, or manually.
- **Owner rule:** short effects (Shield, Haste, Bless) are not tracked. Target
  outcome scenario 3 changed to match.
- **Resolved:** round/turn tracking (out of scope), and where the build resolver
  reads the catalogue (a `CatalogueLookup` port: `content/` files for
  `planBuild`, Postgres for the running API).

**Same day, second follow-up.**

- **adr-0007 accepted by the owner.** New file
  `decisions/adr-0007-curated-modifier-overlay.md`. It adds a mechanics-only
  overlay in `content/dnd-5e/modifiers/<kind>/<slug>.json`, with a book/page
  citation per entry and no text. A structured 5etools field takes precedence
  over the overlay.
- **Owner correction: an effect's duration never implies a rest.** A character
  can go more than 24 hours without resting. The previous rule (1–8 hours ends on
  a short rest, 8–24 hours on a long rest) was wrong and is removed.
  - `activeEffects` now carries `endsOnRests`, set only when the source's own
    text says the effect ends on a rest, and recorded in the overlay with a
    citation.
  - Everything else, including Mage Armor and Aid, ends manually, with
    `durationText` shown as a reminder.
  - Target outcome scenario 3 was updated to match.

**Same day, third follow-up: A1 slice plan written** in `character-creation.md`,
from real `class-*.json` files. It is awaiting owner approval.

- **Scope-filter findings:**
  - 2024 content is marked by `edition: "one"`, not only by `X` sources: the 2024
    Artificer's source is `EFA`.
  - 142 2024 subclass copies keep their old source but point at
    `classSource: "XPHB"`.
  - Sidekick classes are dropped.
- **Planned changes:**
  - new `CLASS`/`SUBCLASS` kinds (no migration: `kind` is unconstrained text);
  - the full field mapping, with pact slots and prepared-spell formulas;
  - `FeatConverter`'s entries flattening extracted into a shared helper;
  - Fighting Style and similar options split into slice A1b
    (`OPTIONAL_FEATURE`), which Aria, a Fighter, needs.
- **Open:** whether Unearthed Arcana content (only the Mystic class and its 6
  subclasses) is imported.

---

## 2026-09-22 — Entity Detail section order, item Weight/Source, itemKind icon fix, real armor-class regression uncovered

Direct owner correction after the same day's item-sidebar work: the four
D&D Beyond sidebar sections (header, properties, description, action bar)
were in the wrong order, the icon set wasn't actually differentiating in
practice, and the properties list was missing Weight and Source entirely.

**Section order (`EntityDetailPanel.tsx`).** DOM-confirmed against D&D
Beyond's own Bedroll sidebar: header → Weight/Cost/Source properties →
description → Quantity/Move/Delete *last*. The panel previously rendered
`actionBar` right after the header for every entity type, matching a
confirmed-live spell behavior that turned out not to generalize. New
`EntityDetailRequest.actionsPosition` (`'top' | 'bottom'`, default
`'bottom'`) lets a caller opt back into the old position; only
`buildSpellDetailRequest` does, since the spell Cast control's own
before-properties placement was the one thing actually confirmed live.
Every other consumer (items, actions, features, extras, coins) picks up the
new default with no changes of its own.

**Item properties.** `ItemRow.tsx`'s metadata gained Weight (`item.weightKg`,
per-unit, matching D&D Beyond's own per-unit figure — a stack's own quantity
is a separate column/stepper, not baked into this number) and Source, in
that order ahead of the existing Cost, matching Bedroll's own DOM order
exactly.

**Source, end to end.** `CatalogueEntry` already stored `sourceBook`/
`sourcePage` (adr-0005) but nothing copied them onto a character's own item
— `Item`/`Dnd5eItem` gained a `source` field (a pre-formatted "book, p. N"
string, `null` for a freeform item, same "copy once" treatment as every
other catalogue field), threaded through `CharacterSheetService.toItem`
(new `formatSource`), `Dnd5eSheetMutator.addCatalogueItem`/`copyItem`,
`Dnd5eSheetCalculator.toItem`, `ItemResponse`, and the frontend `Item` type.
Every raw-constructor call site in both production code and tests needed
the new trailing argument — mechanical but wide (both `Dnd5eSheetMutatorTest`
and `Dnd5eSheetCalculatorTest`). New test: adding a catalogue item preserves
its formatted source across an unrelated mutation (equip toggle).

**Icon classification was correct — the seed data wasn't.** Live testing
showed only ever one icon color across Aria Emberfall's own items, despite
`ItemPreviewIcon.tsx`'s WEAPON/ARMOR-SHIELD/GEAR mapping (added earlier the
same day) being correct. Root cause: `DevCharacterSeeder.java`'s hand-typed
JSON never set `itemKind` on any item, so every one of them fell back to
the compact constructor's own `GEAR` default — Longsword and Shield included.
Added explicit `"itemKind"` to the seeder's Longsword (`WEAPON`), Shield
(`SHIELD`) and Chain Mail (`ARMOR`) entries, then patched Aria's own
already-persisted row the same way (one-off SQL, same precedent as the
weight/encumbrance initiative's own patches) — `DevCharacterSeeder` only
seeds a brand-new player, so an existing dev character never picks up a
seeder-side fix on its own.

**Discovered in the process, not introduced by it:** `itemKind` already
drives real mechanics, not just icon choice — `Dnd5eSheetCalculator.armorClass`
only adds an equipped SHIELD's bonus (and an equipped ARMOR's own base AC)
when `itemKind` is set correctly, and `.attacks()` only lists an equipped
WEAPON. With Aria's Shield now correctly classified, her Armor Class went
from 12 to the correct 14 (base 10 + Dex 2 + Shield 2), and her own Longsword
now appears in the Actions tab's attack list for the first time — both were
silently wrong before today, not a regression from anything in this pass.
Also found and fixed while patching: Aria's Chain Mail was persisted
`equipped: true`, contradicting its own "Spare armor, carried but not worn"
notes text (and lacking `armorCategory`/`baseArmorClass`, which is what
actually threw the `NullPointerException` that first surfaced this) —
restored to `equipped: false` to match its own notes, and given its real
PHB stats (Heavy, AC 16, Str 13, stealth disadvantage) so it no longer NPEs
if a player equips it for real later. A stray "test"-named item in Other
Possessions, left over from earlier verification this same day whose
`Manage Inventory` removal apparently never actually persisted, was also
removed via the same SQL pass.

Verified live against Aria Emberfall: Longsword (red icon), Shield (blue
icon) and Cloak of Protection (green icon, Unequip/Unattune/Move/Delete, no
quantity) all show the corrected four-section layout in order; a freshly
catalogue-added Torch showed "Source: Player's Handbook, p. 153" correctly,
then was removed again; Cure Wounds' spell Cast control still renders first,
confirming the `'top'` exception still works. `./gradlew :apps:api:check`
and `npm run build` both green.

---

## 2026-09-22 — Item/extra sidebar action rows: Move/Delete/Attune, generic item icons, sidebar shell restyle

Direct owner request, DOM-measured live against D&D Beyond's own item
sidebar (dndbeyond.com/characters/50149479 — Bedroll for a plain item,
Cloak of Elvenkind/Wand of Fireballs for an attunable one) and its Extras
panel: the item/extra Entity Detail action bars were missing most of their
real mutate controls, and the sidebar shell itself was still an unmeasured,
inline-styled 280px guess.

**Item action bar (`ItemRow.tsx`).** Rebuilt to match `.ct-item-detail__actions`
exactly: a plain item shows Quantity + Equip/Unequip; an item flagged
`requiresAttunement` swaps Quantity for Equip/Unequip **and** Attune/Unattune
instead (magic items are effectively always singular — DOM-confirmed neither
Cloak of Elvenkind nor Wand of Fireballs shows a quantity stepper). Both
variants get Move and Delete. `TOGGLE_ITEM_ATTUNED` and `REMOVE_ITEM` were
already real, tested mutations (from the attunement-checkbox and other-
possessions-quick-add slices) — only the item's own sidebar was missing the
wiring.

**New `MoveButton.tsx`** (`sheet/`, system-agnostic): D&D Beyond's own
`buttonWithMenu` (DOM-confirmed `data-testid="theme-button-with-menu-move"`)
as an outline button that opens a small popover of the other storage
locations, closing on an outside click — not a plain `<select>`. Exported
`STORAGE_LOCATIONS` out of `ManageInventoryPanel.tsx` so both call sites
share one list; `ItemRow.tsx` mirrors that panel's own Bag-of-Holding gating
(only offered once the character owns one, equipped).

**Backend, to support Move from the item's own sidebar without an extra
step:** none needed — `MOVE_ITEM`/`REMOVE_ITEM` already existed from the
weight/encumbrance and other-possessions work; this pass only wires them
into a second UI surface.

**Generic item icons (`ItemPreviewIcon.tsx`).** D&D Beyond's own
`ct-sidebar__header-preview-image` shows a per-item unique illustration when
one exists (Cloak of Elvenkind, Wand of Fireballs), and a plain generic
silhouette otherwise — fetched live from its own CDN and confirmed the same
three files cover every mundane item checked (Bedroll/Amulet/Crossbow Bolts
→ `potion.jpg` as the catch-all; Crossbow/Light → `weapon.jpg`; Scale
Mail/Shield → `armor.jpg`), a 1:1 match for this app's own closed
`Dnd5eItemKind` set (WEAPON/ARMOR/SHIELD/GEAR — SHIELD shares ARMOR's
image, matching D&D Beyond's own Shield/Scale Mail rows exactly). Downloaded
directly (no auth required) and saved as `dnd_item_kind_{weapon,armor,gear}.jpg`
in `systems/dnd5e/frames/`. **These three are D&D Beyond/Wizards of the
Coast's own proprietary art, not CC-licensed like this app's game-icons.net
picks** — unlike every other borrowed asset here, no credits-page entry
covers this, since none would be owed under an open license; worth revisiting
if this app is ever deployed somewhere its use might matter, since it's
reused as-is for a personal dev project today. A per-item unique illustration
(Cloak of Elvenkind, etc.) is out of scope — this app has no per-item art
catalog, only these three generic fallbacks.

**Extras get Delete.** D&D Beyond's own Extras sidebar only ever offers
Delete beyond hit points — this app had no remove-an-extra mutation at all
yet. New `SheetMutator.removeExtra`/`Dnd5eSheetMutator.removeExtra` (same
filter-by-key shape as `removeItem`), `CharacterSheetService.removeExtra`,
`DELETE /api/characters/{id}/extras/{extraKey}`, frontend `REMOVE_EXTRA`
action/`deleteExtra`. `ExtraRow.tsx`'s `hitPointsActionBar` renamed
`extraActionBar` and gained the same `.sidebar-action-button` Delete button
the item action bar uses.

**Sidebar shell (`Sidebar.tsx`).** Moved off inline styles onto `.sidebar`/
`.sidebar__close` in `frames.css`, width corrected 280px → 340px
(DOM-measured `ct-sidebar__inner`) — the base font needed no override, since
`body`'s own `--font-body` (plain Roboto) already matches D&D Beyond's real
sidebar; only mold headings opt into this app's usual condensed font, same
as before.

**New shared CSS**, all in `frames.css`: `.item-action-bar` (the centered
flex row every item/extra action bar now uses), `.sidebar-action-button`
(the outline Move/Delete/Attune/Equip button — DOM-measured
`--accent-control` border/text, not the lighter `--border-control` this
app's pre-existing "Manage X" buttons happen to use; left those alone,
out of scope), `.item-preview-icon`, `.move-button`/`__menu`/`__option`.
`.item-quantity__button` restyled to match D&D Beyond's own filled
22px `--accent-control` buttons (was a 20px bordered-neutral clone); the
now-unused `.item-row__toggle` clone it used to share space with was removed.

Verified live against Aria Emberfall: Longsword's Move menu (right-anchored
under the button after an initial centered version overflowed the sidebar's
own right edge, fixed same pass) round-tripped Equipment → Backpack →
Equipment correctly; Cloak of Protection's sidebar showed Unequip/Unattune/
Move/Delete with no quantity stepper; Warhorse's Extras sidebar showed a
Delete button below its collapsed Hit Points details. `./gradlew
:apps:api:check` and `npm run build` both green.

Left out: the "In Equipment"/"Legacy • wondrous item, uncommon" intro lines
D&D Beyond's own sidebar shows above the action row — not requested by name
and the "Legacy" marker specifically conflicts with this app's own "no 2024
content and no legacy markers" deviation; a future pass can add a
Legacy-free version of the location line if wanted. The "Customize"
collapsible stays absent, per the existing deviations list.

## 2026-09-22 — Other Possessions' add prompt stays after the first item; thinner dotted attunement divider

Two small owner follow-ups to the same day's Other Possessions/Attunement
passes.

**Other Possessions.** The "+ Add other possessions..." prompt only showed
while the section was completely empty, so there was no way through the UI
to add a second item once the first existed — `renderSection`'s branching
now shows the grid whenever the section has items (unchanged) but always
renders the prompt for `OTHER_POSSESSIONS` specifically, right below the
list once there is one. `.inventory-empty-prompt` gained an 8px top margin
so it doesn't sit flush against the list above it.

**Attunement row divider.** `.attunement-row`'s `border-bottom` was a solid
1px `--border-control` line — heavier than the rest of the sheet's own
row-separator convention. Switched to `1px dotted var(--border-divider)`,
the same thin dotted rule `.action-list`'s own item rows already use.

Verified live against Aria Emberfall: added a second item to Other
Possessions right after the first without reopening the tab or using Manage
Inventory, then removed both via Manage Inventory to leave her sheet as
found.

## 2026-09-22 — Attunement checkbox, and Other Possessions' empty-state quick add

Direct owner request, live-verified against D&D Beyond's own Helga Flinthand
sheet (dndbeyond.com/characters/50149479): two follow-ups on the same day's
earlier Attunement/Other Possessions passes.

**Attunement checkbox.** "Items Requiring Attunement" swapped its plain text
name for a checkbox-plus-name row matching the main item list exactly: the
shared `MarkBox`/`.item-row__flag` control (same 20px box the equip flag
uses) followed by `.item-row__name` (rarity-colored, same as everywhere
else), clicking the checkbox firing the existing `TOGGLE_ITEM_ATTUNED`
mutation. This restores an attune/un-attune affordance to that list — the
earlier same-day pass had stripped its text button entirely — but as a
checkbox rather than a button, matching D&D Beyond's own real slot-manager
control (DOM-confirmed live: `role="checkbox"`, the same element type it
reuses for equip). `AttunementSection` takes `onMutate` again.

**Other Possessions empty state.** Confirmed live that D&D Beyond hides its
own item grid entirely while that section is empty, showing "+ Add other
possessions, treasure, or holdings for your character in this section."
instead (exact copy, DOM-extracted). `InventoryTab.tsx`'s `renderSection`
now checks the section's own `itemCount` (not the filtered `items` list, so
an active search doesn't mistake "no matches" for "genuinely empty") and
renders that prompt in place of the grid header/list when zero. Clicking it
opens a new sidebar body, `OtherPossessionsQuickAdd.tsx` — a single text
input; D&D Beyond's own equivalent is one big freeform textarea, but this
app models Other Possessions as real items (weight/encumbrance initiative),
so Enter/blur here adds one discrete item under that location and clears
the field for the next one, rather than accumulating one paragraph.

**Backend, to support the quick add:** items previously could only be
created into the default `EQUIPMENT` bucket, then moved — `AddItemRequest`
gained an optional `storageLocation` field (nullable, same normalize-to-
`EQUIPMENT` default the compact constructor already applied), threaded
through `SheetMutator.addItem` → `Dnd5eSheetMutator.addItem` (parsed via the
existing `parseNullableItemEnum`, so an invalid value still 400s through
`InvalidItemFieldException`) → `CharacterSheetService.addItem` →
`CharacterController`. `Dnd5eItem` gained a matching freeform-constructor
overload. New mutator tests: adding with an explicit storage location, and
with an invalid one.

Verified live against Aria Emberfall: attuned/un-attuned Ring of Protection
from the new checkbox (left slot filled/emptied in sync); added "Family
locket" via the Other Possessions quick add (grid appeared with the new row,
weight column showing "—" as before), then removed it via Manage Inventory
to leave her sheet as found. `./gradlew :apps:api:check` and `npm run build`
both green.

## 2026-09-22 — Removed Attune/Break attunement/remove-item buttons from Inventory

Direct owner request: stripped the three remaining mutate buttons in the
Inventory tab whose replacement (the sidebar's Entity Detail panel, once an
item row opens one) is a follow-up slice, not built yet — `AttunementSection.tsx`'s
"Items Requiring Attunement" list lost its Attune/Break attunement button
(now name-only, matching the already-buttonless "Attuned Items" slots from
earlier the same day) and `ItemRow.tsx` lost its "×" remove-item button.
`AttunementSection` no longer takes an `onMutate` prop at all — nothing in it
mutates anymore. `InventoryTab.tsx`'s grid header dropped its matching empty
`remove` spacer column; `frames.css` dropped `.attunement-row__button` and
`.item-row__remove` (both now unused).

**Net effect, until the sidebar wiring lands:** there is currently no way to
attune an item, break an attunement, or remove an item from inventory
anywhere in the UI. This is expected, not a regression to chase — the owner
confirmed these actions are moving to the sidebar, not disappearing.

Verified live: build clean, both lists (Inventory rows and the Attunement
section) render read-only with no console errors.

## 2026-09-22 — Attunement slot icon: no border/background, resized 16px → 25px

Direct owner follow-up to the same-day attunement slot icon change below:
`.attunement-slot__icon`'s own bordered/`--frame-paper`-filled circle removed
— the medallion's outline is already drawn by the frame asset itself, so a
second circle behind the glyph was a redundant boundary, not a needed one.
The glyph (`.attunement-slot__icon-glyph`) grew from 16px to 25px to fill the
medallion now that nothing else is competing for that space.

## 2026-09-22 — Attunement slot: icon in the round medallion instead of the item name, actions moved out

Direct owner request: the three attuned-item slots (`AttunementSlot`,
`AttunementSection.tsx`) fit the item's name text starting right at the
frame's own round end cap, where it visually collided with the outline —
the round part of that frame asset is a medallion meant to hold an icon, not
run-on text. Swapped for a plain circle (`.attunement-slot__icon`, 30px,
bordered, `--frame-ink` colored) sitting inside that medallion, holding a
generic equipment glyph — `dnd_icon_battle_gear.svg`, fetched from
game-icons.net (Lorc, CC BY 3.0 — a credits page/footer is still owed, same
running tally as the attack-category/damage-type icons, see
`dnd-5e-sheet-build.md`) — since this app has no per-item catalog art to
show the way D&D Beyond's own numbered slot picker does. The item's name
moved into the bar beside the icon (`.attunement-slot__content` restructured
from `justify-content: space-between` with a trailing button to a plain
icon-then-name flex row).

Same request: the slot's own "Break attunement" button is gone entirely —
that action, and removing the item from inventory outright, are meant to
live in the sidebar once an attuned item's row opens an Entity Detail panel
there, matching every other item row's own click-to-detail pattern; that
sidebar wiring is a follow-up slice, not built yet, so a filled slot is
purely informational for now. The "Items Requiring Attunement" list on the
right keeps its own Attune/Break attunement button unchanged — it remains
the only way to attune an item at all today.

Verified live against Aria Emberfall: both attuned items (Cloak of
Protection, Ring of Protection, the latter attuned and un-attuned again to
confirm) render the icon correctly in their own slot; the empty third slot's
"Choose an item from the right" placeholder is unaffected.

## 2026-09-22 — Attunement section: heading tier and "Items Requiring Attunement" listing attuned items too

Direct owner report: the Attunement subsection's own heading used
`.actions-tab__heading` (no border, no uppercase, no accent color) while
every sibling subsection (Equipment, Backpack, Bag of Holding, Other
Possessions) uses `.inventory-section-heading` — an inconsistent tier for a
subsection at the same level. DOM-confirmed live against
dndbeyond.com/characters/50149479's own Inventory tab: its "ATTUNEMENT"
heading is styled exactly like "EQUIPMENT"/"BACKPACK" (uppercase, muted
accent, bottom divider), with no "X / 3" count in the text — the count is
conveyed only by the three framed slots below it. `InventoryTab.tsx`'s
Attunement heading now renders through the same `inventory-section-heading`/
`inventory-section-heading__label` markup as `renderSection`, with the count
dropped from the label.

`AttunementSection.tsx`'s own "Attuned Items"/"Items Requiring Attunement"
column headers no longer reuse `.actions-tab__heading` either — DOM-measured
against D&D Beyond's `.ct-attunement__group-header` (10px, uppercase, muted
gray, no border), a nested tier below the section heading rather than a
repeat of the same one. New standalone `.attunement-section__heading` rule
in `frames.css`.

**Behavior fix, same DOM comparison:** "Items Requiring Attunement" was
filtering out any item already attuned (`!item.attuned`), but D&D Beyond's
own right-hand list keeps listing an attuned item there too, with its slot
picker toggled to the "used" state — confirmed live (both "Wand of
Fireballs" and "Cloak of Elvenkind" appear on the right list on
character #50149479 despite being attuned on the left). `attunableItems` now
filters only on `requiresAttunement`; the row's own button reads "Break
attunement" and fires the same `TOGGLE_ITEM_ATTUNED` mutation when the item
is already attuned, "Attune" (disabled at the 3-item cap) otherwise — giving
the right-hand list the same toggle-from-either-side behavior D&D Beyond's
checkbox slot manager has, still through this app's own established
button-instead-of-slot-picker simplification (`AttunementSection.tsx`'s own
doc comment, unchanged).

Verified live against Aria Emberfall: attuning Ring of Protection from its
row in "Items Requiring Attunement" filled the left slot and flipped that
same row's button to "Break attunement"; clicking it there broke the
attunement and emptied the slot again.

Left out: no visual change to the three attuned-item slots themselves, and
no change to `ItemRow.tsx`'s own attuned/warning-triangle markers in the
main item list — out of scope for this report.

## 2026-09-22 — Weight/encumbrance: storage locations, kg conversion, Overloaded disadvantage, real catalogue Manage Inventory

New initiative: the Inventory tab now tracks carried weight, grouped by
storage location instead of item kind, matching D&D Beyond's own structure
(confirmed live) but simplified to one fixed Backpack and a conditional Bag
of Holding rather than Beyond's arbitrary-many-named-containers system.

**Backend.** `Dnd5eStorageLocation` (EQUIPMENT/BACKPACK/BAG_OF_HOLDING/
OTHER_POSSESSIONS) added to `Item`/`Dnd5eItem`, mutable via the new
`moveItem` mutation — normalizes to `EQUIPMENT` for any item predating the
field, same compact-constructor pattern `itemKind` already established.
`Dnd5eSheet.trackEncumbrance` is a new per-character setting (not locked at
creation, editable any time via Manage Inventory — matches D&D Beyond's own
UX, not this app's minimal creation flow), both fields living in the sheet's
own versioned JSON, no Flyway migration needed. `Dnd5eSheetCalculator`
gained `encumbrance()`: sums Equipment + Backpack + coins (50 coins = 1 lb,
PHB) into `carriedWeightKg`, excluding Bag of Holding contents and Other
Possessions (neither is physically carried) — capacity is Strength score ×
15 lb, converted to kg. A Bag of Holding section only appears once the
character owns an item literally named "Bag of Holding", **equipped** — not
attuned, since the real catalogue entry has `requiresAttunement: false`
(verified live against the actual 5etools data after first wiring this to
`attuned`, which could never become true for it). `RollService.roll` now
forces disadvantage on Strength ability checks and Strength saving throws
when the character is Overloaded (`trackEncumbrance` on, weight exceeds
capacity) — even when the client requested neither, though it still
correctly cancels back to normal if the client also requested advantage
(the existing tabletop rule). Deliberately out of scope this pass: attack
rolls aren't covered by the same forced-disadvantage check — doing so would
need `RollService` to determine a specific attack's governing ability,
a larger change than this slice's own scope.

Unit conversion (lb → kg) happens server-side only, in `ItemResponse`/
`Dnd5eSheetCalculator` — the domain's own `weightLb` stays untouched (still
1:1 from 5etools, per adr-0006), and the frontend never computes a
conversion itself (ground-rules.md: "no business rules in the frontend").

**Frontend.** `InventoryTab.tsx` rebuilt: a header weight bar (only when
`trackWeight` is on) showing carried/capacity in kg and an Overloaded state;
filter chips per storage location (Bag of Holding's chip only appears when
owned); each section shows its own item count and weight, plus a capacity
for Backpack/Bag of Holding only — Equipment has no maximum, Other
Possessions is never physically carried at all, matching D&D Beyond
exactly. `ItemRow.tsx` gained a Weight column (kg, quantity already
multiplied in) and rarity-colored item names (`itemRarity.ts`, a plain
lb→uncommon/rare/very rare/legendary/artifact color mapping, DOM-color-
matched against D&D Beyond's own item links). `ManageInventoryPanel.tsx`
rebuilt around a real 5etools-backed catalogue picker (`AddCatalogueItemsSection`
— search plus a type filter, capped render at 200 results since the full
catalogue is 1,998 items) reusing the already-built-but-unwired
`/items/from-catalogue` endpoint, alongside the existing freeform "Add
Custom Item" form (same split D&D Beyond itself has); every known item also
gets a storage-location move control.

Verified live end-to-end against Aria Emberfall (`trackEncumbrance` enabled
directly in her dev-seed data and via a one-off SQL patch to her
already-persisted character row, since `DevCharacterSeeder` only seeds a
brand new player, not an existing one): weight bar math, section
weight/capacity math, adding "Bag of Holding" from the real catalogue
(rendered in its real uncommon-rarity green), equipping it to reveal its
section, and moving Chain Mail into it — confirmed its 24.9 kg dropped out
of the character's own carried-weight total the moment it moved in.

The Extras tab (Familiars/Mounts/Summoned Creatures/Vehicles) still used its
own bespoke header row (`.extra-row--header`, a solid divider) and a plain
inline-styled flex row for the search+chips bar, from before
`GridHeaderRow`/`GridScroll`/`.tab-actions-row` were established as the
shared grid conventions (Actions/Spells/Inventory/Features already use
them). Rebuilt to match: `.extras-tab`/`.extras-tab__list` fill the tabbed
section's height so only the row list scrolls (same fix as
`.inventory-tab`), the search field now sits in `.tab-actions-row` alone
(the category chips moved to their own `.grid-filter-row` line below, same
split Inventory's kind chips use), and the flat list is now grouped into
one section per category present — "Familiars"/"Mounts"/"Summoned
Creatures"/"Vehicles" (`.extras-section-heading`, same treatment as
`.inventory-section-heading`) — each with its own repeated `GridHeaderRow`
(Name/AC/Hit Points/Speed) instead of one header for the whole list
regardless of category. `ExtraRow.tsx`'s AC/Hit Points cells dropped their
inline "AC"/"HP" text now that the column header already labels them (Speed
keeps its "ft." unit, not a repeated label, same distinction ItemRow's cost
column already makes).

## 2026-09-21 — Background tab: section-heading consistency, Appearance split out, subsection filter

Matched the Background tab's headings to the same two-tier treatment
already established for the Features tab. "Background"/"Characteristics"/
"Appearance"/"Notes" now use `.actions-tab__section-heading` (new
`SectionHeading` in `BackgroundTab.tsx`, was `.actions-tab__heading` — the
per-feature-name style, wrong tier for a category label). The background's
own name (e.g. "Soldier") now uses `.features-tab__source-heading`, the
same class the Features tab gives a class/species source name (e.g.
"Fighter") nested under its category — was `.background-tab__feature-name`,
a near-duplicate rule now removed from `frames.css`.

Appearance was folded into the Characteristics grid; split into its own
subsection so the tab has four, matching the new filter chips. Added a
`FilterChips` row (`CHIPS`: All/Background/Characteristics/Appearance/Notes),
same primitive and pattern as Actions/Spells/Features/Inventory — each
section only renders when its own chip or "All" is active.

Follow-up: moved Gender/Eyes/Size/Height/Hair/Skin/Age/Weight out of the
Characteristics grid into Appearance's own grid (Alignment/Faith stayed).
The single combined "Characteristics and Details" edit panel was split to
match: `openCharacteristics` now edits only Alignment/Lifestyle/Faith, and a
new `openAppearanceDetails` edits the eight physical-trait fields —
Lifestyle stays grouped with Characteristics even though it has no grid
tile of its own, rather than surfacing in the Appearance edit panel it used
to share a single form with.

## 2026-09-21 — "Manage Feats" button restyled and repositioned to match D&D Beyond

The "Manage Feats" button read as a small text link
(`.actions-tab__manage-custom`, shared with the Actions tab's "Manage
Custom") instead of the bordered callout button used for "Manage Spells" and
"Manage Inventory". Checked D&D Beyond live (Helga Flinthand, Features &
Traits → Feats): its own "Manage Feats" control is the same
`.ct-theme-button--outline` DOM-measured elsewhere in this project for
Spells/Inventory (85.8×27px here, 10px condensed uppercase, 6px 10px
padding, 1px border, not bold) — not a text link. Added
`.features-tab__manage-feats-button` in `frames.css` (identical rules to
`.spells-manage-button`/`.inventory-manage-button`).

Follow-up: moved the button out of the "Feats" heading row and onto its own
row below, with a bottom border separating it from the feat listing —
matching D&D Beyond's `.ct-features__management-link` (block-level, 10px
padding/margin-bottom, `1px solid #eaeaea` divider, which this app's own
`--border-divider` token already matches exactly). New
`.features-tab__manage-feats-row` wrapper in `frames.css`;
`FeaturesTab.tsx`'s `CategoryHeading` now renders the title and the button
row as siblings instead of one flex row.

## 2026-09-21 — Feats: 5etools ingestion, real catalogue, and a "Manage Feats" picker

Direct owner request: replace the Features & Traits tab's redundant "Feat"
subsection heading with a real feat system, sourced only from 5etools — no
hand-authored feat facts, matching this project's own absolute-source-of-truth
policy (adr-0006) that spells and items already follow.

**Ingestion**: new `FeatConverter.java`, same `FiveEToolsConverter` pattern as
`SpellConverter`/`ItemConverter`, registered in `Ingest5eToolsMain`. New
`CatalogueEntryKind.FEAT`. A feat has no mechanical fields this app
calculates (same tier as a class feature — no modelled effect), so `data`
stays empty; the real content is `description`, built from 5etools' own
`entries` with `prerequisite` (when present) prepended as a plain line.
`prerequisiteText` covers the shapes real 2014-era feats use: ability score
minimums (`"ability": [{"int":13}, ...]`, multiple meaning "any one of
these"), armor/weapon proficiency, spellcasting, race, level, plus 5etools'
own `otherSummary` free-text fallback — never a guess for an uncovered
shape, the feat still imports without a prerequisite line instead.

Two bugs found and fixed in the shared pipeline while building this, both
affecting every converter, not just feats:
1. `FiveEToolsNaming.rawEntriesText` only joins an entries array's
   plain-string elements, skipping nested `"type":"list"`/`"type":"entries"`
   sub-blocks by design — fine for a spell, but a feat's actual benefits are
   almost always inside one of those (e.g. Alert's three bullet points),
   so the lead-in sentence was all that survived. `FeatConverter` carries
   its own local recursive flattening (`flattenEntries`) rather than
   changing the shared helper every other converter already relies on.
2. `TagMarkupStripper`'s generic "payload|display|source" pipe convention
   picked the wrong segment for `{@filter display|page|filter1|...}` tags
   (display-first, the opposite order) — e.g. Ritual Caster's class list
   rendered as "spells, spells, spells..." instead of "bard, cleric,
   druid...". Added `filter` to `NAME_ONLY_TAGS` (display = 1st segment),
   alongside the two tags already special-cased there.

Ran `./gradlew :apps:api:ingest5etools --args="--kind=feat"` for real: 228
feats converted (227 unique slugs — one pair shares a slug, same benign
"later file in glob order wins" story spells' own 8 pairs already have),
imported into the local dev catalogue with zero validation failures.

**Backend**: `SheetMutator.learnFeat`/`removeFeat` (same "no-op if already
known" shape as `learnSpell`, no per-class cap to check). `Dnd5eSheetMutator`
appends a `Dnd5eFeatureTrait` with `category=FEAT`, `source="Feat"`,
`maxUses=null` — the existing `Dnd5eFeatureTraitCategory.FEAT`/calculator
mapping already supported this category, only the mutation itself was
missing. `CharacterSheetService.learnFeat`/`toFeatureTrait` mirrors
`learnSpell`/`toSpell`'s cross-feature `CatalogueService` dependency;
`featSummary` derives the tab's short inline line mechanically from the real
description's own first sentence, since 5etools has no separate summary
field and this app doesn't hand-write one. New endpoints
`POST /api/characters/{id}/feats/learn` / `DELETE .../feats/{featureKey}`.
New mutator tests: learning a new feat, learning an already-known one
(no-op), removing a known one, removing an unknown key (no-op).

**Frontend**: `FeaturesTab.tsx` restructured to two heading tiers — the
category (`.actions-tab__section-heading`, reusing the Actions tab's own
top-level treatment) containing each of that category's own sources
(`.features-tab__source-heading`, a class/species name in an accent color,
15px/700, owner-supplied style). The Feats category renders its entries
flat, no source-heading tier at all — a feat's own `source` is a fixed
placeholder ("Feat"), not a real grouping key, so repeating it right below
"FEATS" was the redundancy being fixed. A "Manage Feats" button sits beside
the Feats category heading only (not Class Features/Species Traits, since
those aren't a player-added collection).

New `FeatManagementPanel.tsx` (`sheet/`), mirroring `SpellManagementPanel.tsx`'s
own catalogue-picker shape (`AddSpellsSection`) minus the per-class
sections/caps a feat has none of — closer to a flat list with its own
`SearchField`, needed since 227 real feats is far more than the seeded spell
subset. `CharacterSheetScreen.tsx` fetches `getCatalogue(systemId, 'FEAT')`
alongside spells/backgrounds; `onLearn`/`onRemove` are rebuilt every render
in `displayedSidebarContent`, same "avoid a stale `handleMutate` closure
rolling back a later success" reasoning `spellManagement` already documents.

**Verified live** (Aria Emberfall): Features tab shows "CLASS FEATURES" →
"Fighter" → features, "SPECIES TRAITS" → "Mountain Dwarf" → traits, "FEATS"
(no source subheading) → Alert, with "Manage Feats" beside the Feats
heading only. Opened the panel, searched "Grappler", clicked Learn — it
appeared in both "Known Feats" and the tab itself with its real PHB text
("Prerequisite: Strength 13 or higher..."), clicking it opened the normal
Entity Detail panel. Deleted it, confirmed the tab reverted to just Alert.
No console errors. Full backend test suite green; `tsc`/`vite build` clean.

---

## 2026-09-21 — Named-feature blocks (Action Surge, Second Wind, Sacred Oath, ...) reuse the shared heading class

Direct owner request: every block that names one of the character's features
— `FeatureActionRow.tsx` (Actions tab), `FeatureTraitRow.tsx` (Features &
Traits tab) and `ActionsTab.tsx`'s own `CustomActionRow`, the three
consumers of this shared row shape — should use `.actions-tab__heading` for
the feature's name instead of its own separate `.action-feature__name`
rule, and the name plus its description should sit in one wrapping block
with `margin-bottom: 13px`.

`.action-feature__name` (14px/700/black, no consumers left) removed; all
three components now render `<h3 className="reveal actions-tab__heading">`
(13px/700/black, matching `SpellSummaryList`'s "Spells"/"Ritual Spells" and
Inventory's "Attunement" heading exactly) wrapped together with the
description `<p>` in a new `.action-feature__body` div
(`margin-bottom: 13px`) — the box track (`.feature-snippet__extra`), when
present, stays a sibling after this block, unchanged.

**Verified live** (Aria Emberfall): Second Wind/Action Surge (Actions tab,
two different sections) and Second Wind/Action Surge/Battle Fervor/Fighting
Style: Defense/Extra Attack/Mountain Dwarf (Features & Traits tab) all
render the identical heading treatment, spacing looks consistent block to
block. No console errors.

---

## 2026-09-21 — MarkBox button padding: spend/restore box tracks were too widely spaced

Direct owner report: "o botão checkbox está com muito espaçamento entre os
itens ao seu redor, muito notável nos spells slots" — a regression from the
same day's earlier `MarkBox` extraction (see that entry). The old inline
`boxStyle` `BoxTrack.tsx` used to apply directly to its per-box `<button>`
(back when the button itself *was* the box) included `padding: 0`; once the
box's own visual moved into the separate `MarkBox` component, that reset
never made it onto the new wrapping `<button>`, so the browser's own default
button padding inflated each box's real footprint past its visual 20px —
widest, most visible effect on a multi-box track (spell slots, any
limited-use feature), each box getting noticeably more space than its
sibling than the 4px `gap` alone would produce. New shared `.mark-box-button`
class (`padding: 0`, `display: inline-flex`) on `BoxTrack.tsx`'s button.
Found and fixed the identical latent bug in `.item-row__flag` (Inventory's
equip control, same `MarkBox` consumer) in the same pass — no `box-sizing`/
`padding` reset there either, not yet reported but the same root cause.

**Verified live** (Aria Emberfall): 1st-level spell slot track's three boxes
now sit at their intended snug spacing; Inventory's equip checkboxes
unaffected in size/position. No console errors.

---

## 2026-09-21 — Actions tab: attack table and spell summaries bucketed by real action type

Direct owner request: "verificar como os filtros para todas as abas funcionam
no Beyond e replicar o funcionamento na nossa aplicação" — triggered by a
concrete report that the Actions tab's "Action" filter chip only showed the
standard actions list, not the weapon/spell attacks that also cost an
Action. Live comparison against every filter chip on all four tabs (Helga
Flinthand, dndbeyond.com/characters/50149479) found two real gaps in Actions
specifically; Spells/Inventory/Features & Traits' own filters already
matched (Inventory's own container-based filter is a documented, deliberate
deviation from this app's flat item list, not a gap).

**Bug**: the Attack table only ever rendered under the "Actions" section,
gated on the `attack` chip alone — selecting `Action`/`Bonus Action`/
`Reaction`/`Other` hid it entirely, even though a weapon or spell attack
genuinely costs that action type.

**Missing feature**: each of the four sections also needs its own "Spells"
line (a compact, comma-separated list of the character's non-attack spells
whose casting time matches that section) and a separate "Ritual Spells"
line — this app had no such summary anywhere; `Spell.castingTime`/`.ritual`
already existed on the sheet, just unused by this tab.

**Backend**: `AttackRow`/`AttackRowResponse` gained `actionType` (plain
string, same `adr-0003` convention as `FeatureAction.actionType`). A catalog
`Dnd5eAttack` or equipped `Dnd5eItem` weapon always resolves to `ACTION` (no
activation-type source exists for either — a real weapon attack is always
the PHB "Action" cost). A `displayAsAttack` custom action folded into the
same map now threads its own `Dnd5eActivationType.toActionType()` through
instead of discarding it — that mapping already existed
(`Dnd5eActivationType.java`) but had zero call sites before this. New test:
`aDisplayAsAttackCustomActionCarriesItsOwnActionTypeThroughTheFold`.

**Frontend**: `ActionsTab.tsx` restructured around a `SECTIONS` config
(bucket/title/detail/standardActions) and a `renderSection` function
replacing four hand-duplicated section blocks. Attacks are now split per
section by `AttackRow.actionType`; combat spells by the new
`spellActionType` (`spellCombat.ts`, parses `Spell.castingTime` the same way
`SpellRow.tsx`'s own `castingTimeAbbreviation` already does). The "Attack"
chip shows every section's own attack table (confirmed live D&D Beyond does
the same — Helga's "Custom Action 1" renders in its own "OTHER" sub-table,
not merged into the main one); the four action-type chips show only their
own section's table. New `SpellSummaryList` component renders "Spells"/
"Ritual Spells", DOM-measured against `.ct-feature-snippet__spells`: plain
italic 12px comma-separated entries opening the spell's own Entity Detail on
click, heading reusing the existing `.actions-tab__heading` (already an
exact match, 13px/700/black). D&D Beyond also appends " • <source book>"
per entry — omitted, this app has no source field on `Spell` yet, same gap
already noted on Features & Traits' own class-group heading.

**Verified live** (Aria Emberfall, after restarting the API to pick up the
backend field): "Action" filter now shows the attack table alongside the
standard list and a "Spells"/"Ritual Spells" line (Mage Armor/Detect
Magic/Fog Cloud/Charm Person/Minor Illusion/Cure Wounds, Detect Magic
ritual); "Bonus Action" filter shows Spiritual Weapon (a combat spell with a
Bonus Action casting time) in its own attack table plus a "Spells" line
(Shillelagh); "Attack" filter shows both the Actions table and a second
"BONUS ACTIONS" attack sub-table for Spiritual Weapon, matching D&D Beyond's
own cross-bucket behavior; "Reaction"/"Other"/"All" unchanged apart from the
intended additions. Clicked a spell in a summary line, confirmed it opens
the same Entity Detail/Cast panel as the Spells tab. No console errors.

---

## 2026-09-21 — Shared grid, header cell and checkbox primitives across Actions/Spells/Inventory

Direct owner report: the Inventory tab still looked visibly different from
Actions/Spells despite three tabs sharing the same conventions on paper —
the owner asked for real shared components instead of parallel per-tab
implementations, covering the grid list, the checkbox, the search bar and
the filter chip row.

**Grid.** New `sheet/ListGrid.tsx` (`GridHeaderRow`, `GridScroll`), used by
all three tabs' Attack/Spell-level/Item-kind sections. `GridHeaderRow` owns
the column-header row's wrapper/aria-hidden semantics; each column still
supplies its own row-type width class (the data shape genuinely differs per
tab) but now shares one label-styling class, `.grid-header-cell`
(`frames.css`) — this surfaced a real drift bug: Inventory's own header row
was missing the condensed font and the border/background/padding reset
Actions/Spells' headers already had, so "NAME/QTY/COST/NOTES" rendered in
the wrong font. `GridScroll` owns the single scrolling region below a tab's
fixed header/filter controls (`.grid-scroll`, replacing the separately
declared `.spells-tab__list`/`.inventory-tab__list`) — applied to the
Actions tab for the first time this pass, which previously had no such
region at all and scrolled its filter-chip row away with the list, unlike
Spells/Inventory.

**Checkbox.** New `sheet/MarkBox.tsx` — the "marked box" visual (20px,
sharp corners, centered accent mark) `BoxTrack.tsx` already had, now
extracted out of it. The Inventory tab's equip control (`ItemRow.tsx`) used
to be a *separate* CSS clone of this same treatment
(`.item-row__flag`, 16px/3px-radius/8px-mark) rather than the component
itself — a real, silently-drifted size mismatch against D&D Beyond's own
equip control, which the reference itself reuses at the identical 20px size
for both a feature's charge track and inventory equip (confirmed in
`BoxTrack.tsx`'s own doc comment history). Both now render `MarkBox`
directly, so Second Wind's box track and the Inventory equip checkbox are
pixel-identical.

**Header row / filter chip row.** `.spells-actions-row`/
`.inventory-actions-row` now share a `.tab-actions-row` base, each keeping
only its own genuinely different modifier (Spells' top margin under the
casting header; Inventory's `flex-wrap` for its extra coins control).
`.spells-filter-row`/`.inventory-filter-row` were identical rules under two
names — merged into one `.grid-filter-row`. Actions' own `FilterChips`
render without this wrapper, deliberately — it has no search row above it
to separate from and already gets its spacing from
`.tabbed-section__content`'s own gap, so adding the margin there would
double it up.

**Verified live**: Actions/Spells/Inventory all render unchanged apart from
the intended fixes; Inventory's header row now uses the condensed font
matching Actions/Spells; Actions' own filter-chip row now stays fixed while
only its attack/feature list scrolls, matching Spells/Inventory; toggled
Chain Mail's equip checkbox (now 20px, matching Second Wind's box track
pixel-for-pixel) on and confirmed it reverted correctly. No console errors.

**Corrected same day, direct owner report ("o checkbox está com um contorno
com cor que não tinha antes" / "os grids estão desconfigurados, principalmente
no nome das colunas, tamanho da fonte, tipo, peso e espaçamento"): two real
regressions in the pass above, both fixed without touching the components'
public shape.**

1. `.mark-box`'s border/inset-shadow used `var(--border-control)` (`#BFCCD6`,
   a bluer gray) instead of the literal `#D8D8D8` `BoxTrack.tsx`'s own
   DOM-measurement history had already verified against D&D Beyond — this
   silently recolored every existing `BoxTrack` consumer (Second Wind, spell
   slots, every limited-use feature track), not just the Inventory equip
   control the extraction targeted. Reverted to the literal value.
2. `.grid-header-cell` shipped as a single class, the same specificity as
   each row type's own column rule (`.item-row__name`, `.spell-row__time`,
   …) — whichever was later in `frames.css`'s source order won, and every
   column rule defined further down the file (all of Spells' and
   Inventory's) silently beat the header styling instead of the other way
   around, so those two tabs' header rows fell back to their data rows' own
   font/weight/spacing. Scoped to `.grid-row--header .grid-header-cell`
   (two classes) to make the override unconditional, matching how the
   original hand-written rule this replaced was already a compound
   selector for exactly this reason.

Also found and fixed while re-verifying live: Longsword had gotten toggled
unequipped during this same session's own click-testing (a stray click
during an earlier verification pass, unrelated to either bug above) — reset
via its Entity Detail panel's Equip button.

**Verified live again**: Inventory's checkbox and Second Wind's box track
render the identical `#D8D8D8` border side by side; all three tabs' header
rows (Actions "ATTACK/RANGE/HIT-DC/DAMAGE/NOTES", Spells
"NAME/TIME/RANGE/HIT-DC/EFFECT/NOTES", Inventory "NAME/QTY/COST/NOTES")
render bold/uppercase/condensed again; Longsword/Shield/Cloak of Protection
show equipped, Chain Mail does not, matching the seeded character's
original state. No console errors.

---

## 2026-09-20 — Inventory tab: equip checkbox, attunement layout, grid-only scroll

Three direct owner reports on the Inventory tab rebuild above, same day.

**Equip checkbox fill corrected.** It was a solid ink-filled square with a
reversed "✓" — this app's own already-established "marked box" treatment
(`BoxTrack.tsx`, DOM-measured against D&D Beyond's real `.ct-slot-manager__slot`
— notably the same component D&D Beyond itself reuses for both feature-charge
tracks and its own inventory equip control) never fills a box solid: the box
keeps its own paper background/border in both states, and a smaller
accent-colored square centered inside marks it "on". `.item-row__flag` and
`ItemRow.tsx` now match that exactly (`.item-row__flag-mark`) instead of the
old checkmark-in-solid-ink treatment.

**"A seção de attunement está completamente quebrada esteticamente" — root
cause was the scroll bug fixed in the same pass, not a separate defect in
`AttunementSection.tsx` itself (unchanged, still correct).** The whole tab's
content — header, filter chips, the new taller per-kind grid, and Attunement
— was sharing one scroll region (`.tabbed-section__scroll`, the box's own
outer scrollbar shared by every tab). The rebuilt grid is taller than the old
flat list, so Attunement was being pushed toward the fixed box's bottom edge
and squeezed/clipped against it. Fixed by giving Inventory the exact same
treatment `SpellsTab.tsx` already has for this (its own doc comment covers
the original 2026-09-17 report): `.inventory-tab` now fills
`.tabbed-section__scroll`'s height exactly and only `.inventory-tab__list`
(the item grid) scrolls internally — the header row, kind chips, and
Attunement section all stay in view rather than competing for the same
scroll space. This is also, directly, the owner's third report same session
("a scroll bar deve começar a fazer efeito somente na grid, excluindo a
parte de filtros") — one fix closed both.

**Verified live**: the equip checkbox on Longsword/Shield/Cloak of Protection
now shows the small inset accent square, not a solid ink fill; scrolling the
item grid moves only the rows beneath the header/filter row, which stay
fixed; the Attunement section (all three slots, "Items Requiring
Attunement") renders fully visible below the grid, no longer clipped against
the box's bottom edge.

---

## 2026-09-20 — Inventory tab restructured to match the standard grid

Direct owner report: the Inventory tab's grid had never been brought in line with
the standard pattern `ActionsTab.tsx`/`SpellsTab.tsx` already established (thin
dotted row dividers, gray-accent uppercase section headings, a search+filter+manage
header row, filter chips narrowing the same list) — it still had its own bespoke
header row, a solid border under every row, and an inline add-item form sitting in
the page flow, all predating those conventions.

**Backend**: `ItemResponse` (the actual HTTP DTO) never carried `itemKind` — the
same class of gap slice 7's `SpellResponse`/`ItemResponse` fix addressed for
`fixedSaveDc`/`charges` — so the frontend had no way to group items by kind at all.
Added.

**Frontend**: `InventoryTab.tsx` rebuilt to the standard shape: a
`.inventory-actions-row` header (coins, search, a filter button, "Manage
Inventory") matching `.spells-actions-row`; `ItemsAdvancedFilters.tsx` (new,
mirrors `SpellsAdvancedFilters.tsx`) for the filter button's Equipped/Attuned/
Requires Attunement toggles — the only three boolean dimensions `Item` exposes
today; `FilterChips` for kind (Weapons/Armor/Shields/Gear, derived from the
character's own items, same treatment as spell-level chips); one
`.inventory-section-heading` per kind present, each with its own repeated column
header row and `.action-list`. `.item-row`'s divider moved from a solid
border under every row to the shared dotted-between-rows treatment
(`.action-list > .item-row:not(:first-child)`), and `.item-row--header` dropped
its own border (the section heading above it already carries the one divider a
section needs) — both exactly matching `.action-row`/`.spell-row`'s own rules.

The inline add-item form (name/qty/cost/notes/requires-attunement) moved out of
the tab's own page flow into a new sidebar panel, `ManageInventoryPanel.tsx`,
triggered by "Manage Inventory" — the same relocation `ManageCustomActionsPanel`/
`SpellManagementPanel` already established for their own tabs, and the reason a
"Manage Inventory" trigger belongs in the header row at all: the grid itself now
holds only the list. `.inventory-add*` CSS (the old inline form's styling) was
dead code once the form moved and was deleted rather than kept.

Coins/search/Attunement section/`ItemRow.tsx` itself are otherwise unchanged —
the equip checkbox, attunement warning glyph and remove button were already
correct, only their surrounding grid was rebuilt.

**Verified live** against Aria Emberfall: reloading Inventory shows the new
header row and a single "Gear" section (all her items are freeform, defaulting
to that kind); adding a real catalogue weapon (Dagger, `itemKind: "WEAPON"`)
made a separate "Weapons" section and a "Weapons" filter chip appear
immediately, both disappearing again once the item was removed; the Equipped
filter correctly narrowed the list to Longsword/Shield/Cloak of Protection;
search for "cloak" correctly narrowed to Cloak of Protection; "Manage
Inventory" opened with the full item list and a working add/remove flow, live-
refreshing the main grid. Left restored to its pre-testing state afterward.

---

## 2026-09-20 — Dice tray: stopped resurrecting past rolls on load, added a manual close

Direct owner report: opening a character's sheet, or just reloading the page, showed
the *last roll ever made on that character* in the dice tray as if it had just
happened, full 30s countdown included. Root cause: `DiceTray`'s `latestRoll` prop was
`rollHistory[0]` — the server-persisted roll log, fetched fresh on every mount — so
the tray had no way to tell "a roll that just happened" apart from "the most recent
row of a log that goes back to whenever this character was last played". A 2026-09-03
fix had mirrored dismissal into `localStorage` to stop *re*-showing a roll the owner
had already watched fully dismiss, but that only patched the symptom for a roll that
had survived its full countdown once already — any reload before that still
resurrected it.

**Fixed at the source**: `CharacterSheetScreen.tsx` now tracks a separate
`sessionLatestRoll` state, set only by the four call sites that actually produce a
roll (`handleRoll`, `handleManualRoll`, `handleSpendHitDice`, `handleTakeShortRest`),
never by the initial `getRollHistory` fetch. `DiceTray` reads that instead of
`rollHistory[0]`; a fresh load or reload now has nothing to show until a roll happens
in that page's own lifetime, so the `localStorage`-mirrored dismissal workaround is
no longer needed and was removed — `dismissedKey` is plain in-memory state again.
`rollHistory` itself is untouched and still feeds the Game Log panel with the full
persisted history, unaffected by this change.

**Also added, same report**: a small "×" close button (`dice-tray__close`, top-right
of the tray, reusing `dnd_icon_close.svg` — already imported for this exact purpose
by `CustomRollPicker.tsx`'s own close button) so a roll can be dismissed immediately
instead of only via the 30s auto-dismiss countdown.

**Follow-up, same day**: the fix above still rendered the tray's own empty-state
placeholder ("No rolls yet") at all times while the sheet was open, sitting in the
same top-left corner the resurrected roll used to occupy — direct owner report that
this, too, was unwanted. `DiceTray` now returns `null` outright when there is no
error and no session roll to show, instead of rendering an empty box; `.dice-tray__empty`
and the now-unreachable `.dice-tray--dismissed` fade CSS were both removed as dead
code (the component unmounts itself now rather than fading an empty tray in place).

**Verified live** against Aria Emberfall, who already had roll history from earlier
testing (including a 30-damage Fireball): reloading the sheet now shows nothing in
the tray's corner at all, instead of resurrecting that Fireball or showing an empty
placeholder; rolling Fire Bolt shows it in
the tray with a working "×"; clicking "×" dismisses it immediately; reloading again
still shows "No rolls yet"; the Game Log panel still lists every past roll
(Fire Bolt, Fireball, a custom roll, a Longsword attack, Cure Wounds) unaffected.

---

## 2026-09-20 — Slice 7: item-granted spells by charge (Wand of Fireballs)

Final slice of `features/inventory-equipment-mechanics.md`. An item can now grant a
spell that's cast by spending the item's own charges instead of a spell slot —
5etools' `grantedSpells` array on an item (already extracted in slice 1's
`ItemConverter`, just unused until now) becomes real, playable mechanics.

**Backend**: `ItemGrantedSpell`/`Dnd5eGrantedSpell` grew from a 2-field stub
(`spellSlug`, `chargeCost`) to the full 21-field shape `Spell`/`Dnd5eSpell` already
have, plus `fixedSaveDc` — extracted from the item's own flavor text via a new
`{@dc N}` regex in `ItemConverter`, since a wand's save DC is fixed by the item, not
derived from the wielder's spellcasting ability. `Item`/`Dnd5eItem` grew a
`chargesUsed` field. `addCatalogueItem` now also denormalizes each granted spell into
`sheet.spells()` (same "copy once" precedent as a learned spell), tagged with the
new `grantedByItemKey`/`chargeCost`/`fixedSaveDc` fields on `Spell` itself — reusing
the entire existing spell-rendering/rolling pipeline rather than a parallel type.
`Dnd5eSheetCalculator.spells()` filters a granted spell out unless its owning item is
currently equipped (and attuned, if attunement is required) — `isGrantedSpellCurrentlyActive`.
A new `castItemGrantedSpell` mutation spends charges (capped at the item's max,
never negative); `applyLongRest` resets every item's `chargesUsed` to 0 (a documented
simplification of the real 1d6+1 partial recharge — same treatment slice 5's charge
work already gave feature resources).

**Frontend**: `ItemSpellCast.tsx` (new) is `SpellCast.tsx`'s sibling for the item-charge
case, reusing its `.spell-cast` layout with a charge cost/remaining pair instead of a
level stepper. `spellDetail.tsx`'s `castActionBar` picks it over `SpellCast` based on
`spell.grantedByItemKey`. `SpellRow`/`SpellAttackRow`/`spellDetail` all gained a
`spell.fixedSaveDc` fallback for when there's no matching `spellcasting` class entry.

**A real bug found and fixed during live verification, not just a happy-path check**:
after re-running the 5etools ingestion to pick up the new `fixedSaveDc` extraction and
re-importing the catalogue, Fireball's Hit/DC still showed "–" instead of "DEX 15".
Root cause: `SpellResponse`/`ItemResponse` (the HTTP DTOs `CharacterSheetResponse`
actually serializes) were never updated to carry the new `Spell`/`Item` fields —
`grantedByItemKey`/`chargeCost`/`fixedSaveDc` and `charges`/`chargesUsed` respectively
were computed correctly in the domain layer but silently dropped before reaching the
browser. Fixed by adding the missing fields to both response records.

**Verified live** against Aria Emberfall: added Wand of Fireballs from the corrected
catalogue entry, equipped and attuned it, confirmed Fireball appears only once
attuned and shows "DEX 15" under Hit/DC in the Spells tab (3rd level filter); cast it
from the Entity Detail panel (damage roll fired, charge counter dropped 7 → 6); took
a Long Rest and confirmed the charge counter reset to 7/7. Removed the test item
afterward, restoring Aria's sheet to its pre-testing state.

All 7 slices of `features/inventory-equipment-mechanics.md` are now done.

---

## 2026-09-20 — Correction: reverted slice 5's backend spell-attack fold, fixed a real healing bug

Direct owner report while reviewing slice 6 live: Fire Bolt and Chill Touch showed
twice in the Actions tab. Root cause, found by reading `ActionsTab.tsx` (which slice
5 should have been read before writing any backend code): a **complete, independent
frontend mechanism for exactly this already existed**, built 2026-09-03/04/10/15 —
`isCombatSpell`/`SpellAttackRow.tsx` — reading `sheet.spells`/`sheet.spellcasting`
directly and rendering attack-roll, save-based **and** healing spells in the same
Actions-tab table, confirmed live back then against Helga Flinthand and a second
reference character ("Copy of Raya"). It already handles the save-based case
(a flat DC shown under Hit/DC) that slice 5's own changelog entry claimed `AttackRow`
"has no DC slot" for — that claim was true of the *backend's* `AttackRow` shape, but
irrelevant, since the frontend never needed it to begin with. Slice 5's backend
addition to `Dnd5eSheetCalculator.attacks()` (attack-roll spells only) duplicated the
subset of spells both mechanisms cover.

**Fixed by reverting the redundant piece, not merging two mechanisms**:
`Dnd5eSheetCalculator.attacks()`'s spell-folding loop and its three now-unused helper
methods (`findSpellcastingClass`, `isSpellCurrentlyCastable`, `spellAttackRow`) are
removed, along with the five backend tests slice 5 added for them. `sheet.attacks()`
is weapons + custom actions again, exactly as before slice 5; spells reach the Actions
tab exclusively through the pre-existing frontend path.

**A real, independently-confirmed bug fixed in that older mechanism**: `isCombatSpell`
included `isHealingSpell(spell)` — this session's own live re-check of Helga's sheet
(filtering both "All" and "Action") found Cure Wounds never appears in D&D Beyond's
real Actions tab, so that inclusion was wrong, not just newly-decided-against. Fixed
in `spellCombat.ts`; `!isHealingSpell(spell)` is now checked explicitly, since
`damageDiceCount != null` alone doesn't exclude a healing spell — its own heal-roll
dice live in that same field (`Dnd5eSpell`'s own doc comment: only `damageType` stays
null for healing, `damageDiceCount`/`Sides` are set same as a real damage spell).
`SpellAttackRow.tsx`'s now-unreachable healing branch (the `dnd_icon_healing.svg`
icon, the `SPELL_HEAL` roll kind, healing-specific aria-labels) is removed as dead
code; a healing spell's own cast/roll in the Spells tab (`SpellRow.tsx`) is a
completely separate path, untouched.

**Also checked per the owner's own request**: whether Fire Bolt/Chill Touch were
being pulled from both `PHB` (2014) and `XPHB` (2024) sources, given `adr-0006`'s
2014-only scope rule. They were not — `content/dnd-5e/spells/fire-bolt.json` and
`chill-touch.json` are both sourced from `"Player's Handbook"` only;
`FiveEToolsSourceClassifier` (slice 1) already excludes `XPHB` before conversion, so
no 2024 duplicate ever entered the catalogue. Confirmed by direct inspection, not
assumed.

**Verified live**: reloaded Aria Emberfall's sheet (required restarting the local
`bootRun` process — it was still running slice 5's own code from before this fix; a
stray dev server from earlier in this session, not started fresh for this check).
Fire Bolt/Chill Touch each appear once now, italicized, correctly bucketed as
cantrips; Acid Splash/Vicious Mockery/Poison Spray/Sacred Flame/Thunderwave (all
save-based) render their flat DC correctly under Hit/DC; Cure Wounds no longer
appears anywhere in the Actions tab; Unarmed Strike still sits last.

Backend test count: 241 (down from 246 — the five slice-5 tests removed, no new
ones needed since the fix is a revert plus a one-line frontend condition change).
`:apps:api:check` green.

Corrects `features/inventory-equipment-mechanics.md`'s own slice 5 entry, which is
left as written (historical record) rather than rewritten — see that doc's current
status for the corrected picture.

---

## 2026-09-20 — Inventory equipment mechanics, slice 6: real attunement slot artwork

Sixth slice of `features/inventory-equipment-mechanics.md` — **narrower than the
plan described**: the plan's own slice 6 ("dedicated Attunement panel, not a
per-row checkbox") turned out to already be built, in an earlier phase
(`AttunementSection.tsx` already existed with two columns and a 3-limit before this
initiative started — the plan's own text describing it as still-a-checkbox was
stale). Confirmed with the owner before doing any work; the owner asked instead for
real visual parity with D&D Beyond's own attuned-slot artwork, using the actual
`AttunementSlotBoxSvg.tsx` asset from `design-reference/markup`.

**What changed**: the left "Attuned Items" column now renders exactly three fixed
slots (`AttunementSlot`, empty or filled) instead of only as many rows as are
actually attuned — matching D&D Beyond's real fixed-slot-picker shape, even though
this app's own filled-slot action stays a plain "Break attunement" button (no drag
target, no catalog-backed slot identity to drag into). The slot's own outline is
the real Beyond asset (`AttunementSlotBoxSvg.tsx`'s path), extracted as a new
`frames/dnd_icon_attunement_slot.svg`, `currentColor`-based like every other frame
in this app, rendered via the existing `FrameLayer` (no new rendering component
needed).

**A real thing learned only by rendering it, not from reading the source**: the
extracted path is a thin ornate *frame outline*, not a solid fill — D&D Beyond's own
component layers it behind a separate photo/background, it never fills a box on its
own. A first pass used the "paper" (white) tone for every slot, which rendered
correctly for empty slots (bordered by CSS) but was invisible for a filled one, and
a follow-up fix that gave filled slots white text (assuming a dark fill) made the
item name and button disappear entirely against the still-white page — both caught
live in the browser, neither would have been caught by `tsc`/a unit test. Filled
now uses the "ink" tone (the same dark accent this app's filled proficiency dots
already use for an "on" state); empty stays "paper" with an explicit CSS border,
since the outline alone is white-on-white without it.

Verified live: attuning "Ring of Protection" from the right column correctly filled
the second slot with its own frame and button, "Attunement (2 / 3)" updated, the
right column correctly emptied to "No eligible items." with all three eligible
slots consumed appropriately; breaking attunement correctly returned it. No backend
changes — the rules were already correct, this was a frontend-only slice.

**Found while verifying, not fixed (out of this slice's own scope, flagged for the
owner)**: with slice 5's new attack-roll-spell folding, Fire Bolt and Chill Touch
now visibly appear twice in the Actions tab — once in the real ATTACK table (correct,
slice 5's own work) and once more in a pre-existing plain-text "Spells:" reference
list under the ACTION filter category (an older feature, unrelated to this
initiative), which doesn't yet exclude a spell that already has its own attack-roll
row. D&D Beyond's own real Actions tab does not duplicate this way. Not fixed here —
narrower than slice 6's own scope, named rather than silently left for later.

---

## 2026-09-20 — Inventory equipment mechanics, slice 5: attack-roll spell → Actions

Fifth slice of `features/inventory-equipment-mechanics.md`. A known/prepared spell
with an attack roll now folds into the same Actions-tab attack table a weapon or
`Dnd5eAttack` already populates — confirmed live against Helga Flinthand's own
Actions tab (Guiding Bolt, Inflict Wounds), same row shape, proficiency bonus always
included (a caster is always proficient with their own spell attacks, PHB — unlike a
weapon, which is checked). Spell damage carries no ability modifier by rule (already
documented on `Dnd5eSpell` itself, enforced elsewhere by `Dnd5eMechanicResolver`), so
`damageModifier` is always `0` for one of these rows.

**"Currently castable" is a new concept this slice needed and didn't exist before**:
a cantrip or an always-prepared spell is always usable; a `KNOWN`-type class's own
leveled spell has no separate preparation step, so every one it knows is usable;
otherwise (a `PREPARED`-type class's own leveled spell) only an actually prepared one
counts — an unprepared spell is known but not currently an action. `range`/`damageType`/
`notes`/`category` (`"{ClassName} Spell"`) all reuse the spell's own already-converted
fields; no new formatting needed.

**A real scope boundary found while implementing, not guessed at**: `AttackRow` has no
DC slot — the exact same, already-documented limitation `Dnd5eCustomAction`'s own doc
comment accepted for a save-based custom action. A save-based spell (e.g. Fireball)
therefore **does not** fold in yet, even though the owner's own decision was to match
D&D Beyond's real behaviour, which does show both attack-roll and save-based spells in
that table (its "HIT / DC" column header supports either). Extending `AttackRow` with
a DC slot is real, scoped work for a future slice — covering both the custom-action
case and this one together, consistently, rather than solving it once for spells alone.

Five new tests in `Dnd5eSheetCalculatorTest`: a cantrip folding in regardless of
preparation, an unprepared leveled spell excluded, a prepared one included, a
`KNOWN`-type class's leveled spell needing no preparation, and a non-attack-roll spell
never appearing even when prepared. All passed on the first run.

---

## 2026-09-20 — Inventory equipment mechanics, slice 4: equipped armour/shield → armour class

Fourth slice of `features/inventory-equipment-mechanics.md`, closing the gap
`rulesets/dnd-5e-sheet-ui.md` had named since phase 8 but never built: `armorClass()`
was a flat `10 + dexterity modifier` formula with no awareness of the sheet's own
items at all. It now looks at equipped items: an equipped `ARMOR`-kind item replaces
the unarmored base with its own base AC plus a dexterity contribution capped by its
`armorCategory` (light: full, medium: `+2` cap, heavy: none — a standard PHB rule
applied here, not stored on the item, per `Dnd5eArmorCategory`'s own doc comment); an
equipped `SHIELD`-kind item adds its own flat AC on top, independently, whether or not
armour is worn. A rare named magic item's own `armorClassBonus` (slice 1) adds its own
labelled contribution on both. The dexterity contribution is always shown, even at
`0` for heavy armour, matching the "show every real factor" transparency the old
unarmored formula already had.

Six new tests in `Dnd5eSheetCalculatorTest`: medium armour's `+2` dexterity cap
(contributions checked exactly), light armour's uncapped dexterity, heavy armour's
zero-but-shown dexterity contribution, a shield stacking on top of worn armour, a
shield alone stacking on top of the unarmored base, and an unequipped armour/shield
never contributing. All passed on the first run.

Left out, named rather than silently dropped: multiple equipped body-armour items
(nothing currently prevents a player error there — the first one found is used, not a
documented cap) and unarmoured-defense class features (barbarian/monk use an ability
score instead of armour) — both pre-existing simplifications this slice didn't
introduce or need to fix.

---

## 2026-09-20 — Inventory equipment mechanics, slice 3: equipped weapon → Actions

Third slice of `features/inventory-equipment-mechanics.md`. `Dnd5eSheetCalculator.attacks()`
now folds every equipped, weapon-shaped item (`itemKind() == WEAPON`) into the same
`AttackRow` table `Dnd5eAttack` and a to-hit custom action already populate — confirmed
live against Helga Flinthand's own Actions tab, the exact rule (equipped + weapon-shaped,
nothing more) `inventory-equipment-mechanics.md` already recorded.

**Unlike a hand-authored `Dnd5eAttack` (proficiency always assumed), a real item's
proficiency is checked** against `sheet.weaponProficiencies()` — this app's own
free-text convention (`"Simple"`/`"Martial"`, confirmed against `DevCharacterSeeder`'s
real seed data), matched case-insensitively against the item's `weaponCategory`, or
against the item's own name for a class feature that grants a specific weapon instead
of a whole category (both real PHB ways to gain weapon proficiency). Ability selection
is computed, not stored, for the first time in this app's attack rows: ranged always
uses dexterity (PHB); melee uses strength unless the weapon has the finesse property,
in which case the character's own better modifier is used (no per-attack "which
ability did you pick" UI exists for a finesse weapon, so the optimal one is assumed).
A rare named item's own literal `weaponAttackBonus`/`weaponDamageBonus` (see slice 1)
adds its own labelled contribution to to-hit/damage. `range` is formatted from the
item's own data: `"5 ft. Reach"`/`"10 ft. Reach"` for melee (the Reach property),
`"{normal}/{long} ft."` for ranged; `category` reuses the item's own `typeLabel`
("Melee Weapon"/"Ranged Weapon") directly — no new formatting table needed, `ItemConverter`
already produces exactly this text.

Six new tests in `Dnd5eSheetCalculatorTest`: proficient fold-in (with contribution
count and every `AttackRow` field checked against a real Warhammer's data), proficiency
bonus omitted when not proficient, an unequipped weapon never appearing, a finesse
weapon picking the higher of strength/dexterity, a ranged weapon always using dexterity
regardless of a higher strength, and a named magic weapon's literal bonus adding to
both to-hit and damage. All passed on the first run — the ability-selection and
proficiency-matching logic held.

Left out, named rather than silently dropped: multiple attacks per action (Extra
Attack, etc.) — this app has no attack-economy model at all yet, for a hand-authored
`Dnd5eAttack` either. Ammunition consumption. A specific per-attack ability choice UI
for a finesse weapon (ability is always the better one, not a player toggle).

---

## 2026-09-20 — Inventory equipment mechanics, slice 2: sheet items reference the catalogue

Second slice of `features/inventory-equipment-mechanics.md`. A sheet item can now
carry a full, real 5etools item's weapon/armour/charge/granted-spell data — the same
"copy once at add-time, never re-read the catalogue" treatment `Dnd5eSpell` already
gives a learned spell — while a freeform/homebrew item (the Inventory tab's existing
free-text add form) stays exactly as it was.

**Backend**: `Dnd5eItem` grew from 8 to 37 fields (weapon: category/attack type/damage
dice + versatile/type/properties/finesse/range; armour: category/AC/stealth/strength;
a rare named item's own literal `+N` bonus; the Wand of Fireballs mechanic: charges/
recharge trigger+formula/granted spells) — three new enums (`Dnd5eItemKind`,
`Dnd5eWeaponCategory`, `Dnd5eArmorCategory`, reusing the existing `Dnd5eRangeCategory`
for melee/ranged) and `Dnd5eGrantedSpell`. **Every existing call site (production and
test) kept working unchanged**: both `Dnd5eItem` and the generic `ruleset.Item` gained
a second, 8-arg constructor matching their original shape exactly, delegating to the
full canonical one with the new fields defaulted — freeform `addItem` and every
existing test's `new Item(...)`/`new Dnd5eItem(...)` call needed zero changes. A
compact constructor on `Dnd5eItem` normalizes a null `itemKind` to `GEAR` and null
`properties`/`grantedSpells` to empty lists, so an already-persisted item predating
this field (or Jackson's own null-for-missing-object-field behavior) never produces a
surprising null where later slices will dispatch on it.

New `SheetMutator.addCatalogueItem(sheetJson, Item item)` — always allowed (no cap,
no no-op-if-known: looting a second longsword is normal, unlike learning a spell
twice) — mirrors `learnSpell`'s "generic type crosses the seam" shape exactly, with
its own `parseItemEnum`/`parseNullableItemEnum` and a new `InvalidItemFieldException`
mapped to **500**, not 400: unlike a custom action's own form input, an item's
closed-set fields are never client-typed, only ever produced by `ItemConverter` and
mapped by `CharacterSheetService#toItem` — an invalid value here can only be a
server-side data bug. `withEquipped`/`withAttuned`/`withQuantity` (the existing
equip/attune/quantity toggles) were rewritten through a shared `copyItem` so toggling
one flag on a catalogue-sourced item no longer risks silently dropping its weapon
data back to nulls — the bug the old 8-arg reconstruction would have caused the
moment a real item existed, caught before it shipped by a dedicated regression test.

New endpoint `POST /api/characters/{id}/items/from-catalogue` (`AddCatalogueItemRequest`,
just `catalogueEntryId` — no `className` concept like spells have) →
`CharacterSheetService#addCatalogueItem` → `CatalogueService#getUnredacted` → new
`toItem` mapper (mirrors `toSpell`) → mutator. New package-private `ItemCatalogueData`
(mirrors `SpellCatalogueData`) deserializes `ItemConverter`'s own `data` JSON shape;
`toItem` also formats `costGp` into the same "15 gp" display string freeform items
already use (`null` cost → `""`, matching the many magic items 5etools prices at "—").

Left out, named rather than silently dropped: a frontend "search 5etools items and
add" UI — this slice is backend-only, same precedent slice 1 set; the new endpoint is
reachable but nothing in the Inventory tab calls it yet. Actually wiring the copied
weapon/armour/charge data into the Actions tab, armour class or the Spells tab is
slices 3/4/7's own work, not started.

---

## 2026-09-20 — Inventory equipment mechanics, slice 1: 5etools item ingestion

First slice of `features/inventory-equipment-mechanics.md`, a new initiative (not a
roadmap phase) closing the gap between the Inventory tab's `equipped`/`attuned`
booleans and the Actions/Spells tabs/armour class, and re-sourcing item data from
5etools per `adr-0006`. Live research against Helga Flinthand's D&D Beyond sheet and
this app's own code preceded planning; see the feature doc for the decisions and the
facts confirmed live.

**This slice**: `ItemConverter`, a new `FiveEToolsConverter` mirroring `SpellConverter`,
reads both `items-base.json` (`baseitem`, mundane equipment) and `items.json` (`item`,
magic items) — single files directly under the data root, unlike spells' per-sourcebook
split — and emits `content/dnd-5e/items/<slug>.json`. Classifies each entry into
`itemKind` (`WEAPON`/`ARMOR`/`SHIELD`/`GEAR`) from the `weapon`/`armor` flags and the
`M`/`R`/`LA`/`MA`/`HA`/`S` type codes, decodes weapon properties/damage types/range,
armour AC/stealth/strength requirement, and — the Wand of Fireballs mechanic — charges,
recharge trigger/formula, and `attachedSpells.charges` into a `grantedSpells` list
(charge cost → spell slug), reusable by a future slice built on this app's existing
resource/recharge-trigger concept from phase 9. Registered in `Ingest5eToolsMain`
alongside `SpellConverter`.

Extracted `FiveEToolsNaming` (slug/source-book-name/entries-flattening/capitalize/
non-empty-array helpers) out of `SpellConverter`, since `ItemConverter` needed the
exact same logic — the "shared, kind-independent pipeline pieces" seam
`5etools-ingestion.md` already named. `SpellConverter`'s own behaviour and tests are
unchanged by the extraction (9/9 still pass).

**Real bug found running this against the owner's actual downloaded data**:
`FiveEToolsSourceClassifier` only excluded the `XPHB` 2024 revision — spells never
needed more than that, but `items.json` carries a 2024 `XDMG` duplicate of every core
magic item, and the last-file-wins slug collision was silently letting XDMG's text
(e.g. a paraphrased, tag-broken Cloak of Elvenkind/Wand of Fireballs) overwrite the
real 2014 DMG entry. Fixed by excluding the full `{XPHB, XDMG, XMM}` set (`XMM`
pre-emptively, ahead of creature ingestion) rather than patching around it in
`ItemConverter`, since the classifier is the shared pipeline piece for this rule. Also
found and fixed: one real item (Stonemaker War Pick) has a rollable `{@dice 1d6 + 1}`
charge count instead of a fixed number — `intOrNull` now returns `null` for a
non-numeric node rather than crashing the whole run; modelling a variable maximum
charge count is out of this pass's scope, named rather than guessed at.

Ran for real against the owner's `tools/5etools-data/`: 1,931 items converted (some
duplicate slugs across the two source files, same "last one wins, expected/benign"
precedent spells already established) into `content/dnd-5e/items/`, re-run verified
byte-identical, and Cloak of Elvenkind, Wand of Fireballs, Warhammer, Light Crossbow,
Scale Mail and Shield spot-checked against the feature doc's field mapping table —
all correct, including the Wand's `grantedSpells: [{spellSlug: "fireball",
chargeCost: 1}]` and its tag-stripped, non-XPHB-corrupted description.

Left out, named rather than silently dropped: importing the generated files into the
running local dev catalogue (`--import-catalogue`) — the dev server was already
occupying port 8090 under a process this session didn't start, so it wasn't touched;
the owner can run the import themselves. Generic magic-item variants
(`magicvariants.json`, e.g. a generic "+1 Longsword"), ammo consumption tracking,
weapon mastery, passive non-combat item effects, and item-level display tags all stay
out of scope per the feature doc's own list. Slices 2–7 (catalogue-referencing sheet
items, equipped weapon/armour/spell wiring into Actions/armour class/Spells, the
dedicated Attunement panel) are not started.

---

## 2026-09-20 — Manage Custom Actions

Owner punch list item 7, parts (a) continued/(b)/(c) — the last of the seven
findings, closing `dnd-5e-sheet-fidelity-audit.md`'s punch list and Phase 10.

Closed the remaining live-investigation gaps first: AoE Type's real option
list (Cone/Cube/Cylinder/Line/Sphere/Square/Square Feet/Emanation),
Activation Type's real eight values, and the Fixed Value/to-hit relationship
— to-hit is the stat's modifier plus proficiency bonus only when Proficient
is checked (confirmed live: an unchecked one showed a bare stat modifier);
damage is the dice plus the stat modifier automatically, Fixed Value an
extra flat bonus on top. Also confirmed live a Spell-template custom action
never surfaces on the Spells tab (Actions-tab-only, all templates), and
Snippet doesn't render in D&D Beyond's own read-only sidebar summary.

**Backend**: `Dnd5eCustomAction` (one record for all three templates, fields
nullable per template, same reasoning as `Dnd5eItem`), five new enums
(`Dnd5eCustomActionTemplate`, `Dnd5eActivationType` — models the real eight
values and folds the four this app's Actions tab has no section for into
`Dnd5eActionType.OTHER` via `toActionType()` — `Dnd5eRangeCategory`,
`Dnd5eSpellRangeType`, `Dnd5eAreaOfEffectType`, `Dnd5eWeaponAttackType`).
`Dnd5eSheet` gained a `customActions` field (appended at the end, same
incremental-addition pattern every prior phase used) and a `withCustomActions`
wither. `SheetMutator.addCustomAction`/`removeCustomAction` follow
`addItem`/`removeItem`'s exact pattern (generated key, no-op on an unknown
key); the generic `ruleset.CustomAction` record crosses the seam the same way
`Spell` already does for `learnSpell` — one shape for both display and
mutator input. `Dnd5eSheetCalculator.attacks()` folds a `displayAsAttack`
custom action in as an `AttackRow` only when it has a `stat` set (a real
to-hit roll); a save-based one (`saveType` set instead) stays list-only,
since `AttackRow` has no DC slot — a deliberate limitation, not a guess at
extending the shared shape from one unconfirmed case. New endpoints
`POST`/`DELETE /api/characters/{id}/custom-actions`.

**Frontend**: a new `ManageCustomActionsRequest` sidebar kind (matching
`SpellManagementRequest`'s own precedent, not the Collection editor mold —
too many multi-field/dropdown/checkbox fields) and `ManageCustomActionsPanel`,
listing the character's custom actions grouped by template plus a fill-then-
submit add form (unlike D&D Beyond's own create-blank-then-edit flow — this
app's own `InventoryTab` add-item precedent instead). All four of the Actions
tab's existing "Manage Custom" buttons open this one shared panel with the
complete list, matching D&D Beyond's real confirmed behavior (one flat list,
not scoped per section) rather than four separate ones. A non-`displayAsAttack`
custom action renders as its own row in whichever section its activation type
buckets into, alongside feature actions.

Dev-only: the one pre-existing seeded character in local Postgres predated
this field and needed a one-time `jsonb_set` backfill (`customActions: []`)
— JSONB evolves without Flyway migrations, so this is the same kind of fix
`Dnd5eItem.requiresAttunement`'s own doc comment already describes, just
applied once by hand instead of by a new field's default. `DevCharacterSeeder`'s
own seed JSON literal was updated too, so this is a one-time fix, not a
recurring one.

Left out, named rather than silently dropped: a save-based `displayAsAttack`
custom action's own to-hit-vs-DC Attack table treatment (see "Deviations from
D&D Beyond" in `dnd-5e-sheet-ui.md`); where (if anywhere) Snippet might
render; the exact relationship between the core Range toggle and the Spell
template's own Spell Range Type field beyond both being stored.

---

## 2026-09-19 — "Manage Custom" actions investigated live (no code this entry)

Owner punch list, item 7, part (a): a live look at Helga Flinthand's own
custom actions before designing anything, per the item's own instruction not
to guess the shape. Full findings recorded in
`dnd-5e-sheet-fidelity-audit.md`'s own item 7 entry — summary here.

The item's own premise (one custom action shape, chosen section like this
app's four `Dnd5eActionType` buckets) didn't hold. D&D Beyond actually offers
three templates (General/Spell/Weapon) and one flat list, not four
per-section lists; the Spell template alone has ~19 fields (dice, a stat, a
save DC, AoE, an "Affected by Martial Arts" toggle, "Display as Attack",
among others), almost all optional. General and Weapon's own field sets
weren't inspected, to limit how much of this exploration touched the real
account.

Selecting a template from "ADD NEW ACTIONS" creates the entry immediately,
no confirmation step — used deliberately for General and Weapon too (create,
open Edit, read every field label, remove via "REMOVE ACTION"), the same way
the first accidental "Custom Action 3" was created and cleaned up. The
character's own two pre-existing custom actions (the real reference data)
were untouched throughout; one D&D Beyond API error during a later removal
was confirmed harmless by reloading the sheet — the removal had already
gone through.

All three templates now mapped: one core field set (Range, Stat, Dice Count,
Die Type, Fixed Value, Damage Type, Save Type, Fixed Save DC, a second
numeric Range, AoE Type, AoE Size, Activation Type, Activation Time,
Affected by Martial Arts, Proficient, Display as Attack, Name, Snippet,
Description) shared by all three; Spell adds "Spell Range Type"; Weapon adds
Attack Type, Long Range, Dual Wield, Silvered; General adds nothing.

Owner decision: build the full shape, all three templates, not a trimmed v1
— including AoE and Martial Arts even though this app models neither
anywhere else yet, a deliberate one-off exception to `ground-rules.md`'s "no
speculative generality." No code changed this entry — (b) backend model and
(c) UI wiring are next, per the audit file's own item 7.

---

## 2026-09-19 — Resistance/Immunity/Vulnerability now use D&D Beyond's own shield icons

Owner punch list, item 6, corrected twice same day. First pass built an
original CSS `clip-path` shield (this app's own geometry, avoiding D&D
Beyond's proprietary artwork per this codebase's usual stance — see
`SpellRow.tsx`/`CoinChips.tsx`'s own doc comments on that). **Direct
correction**: use D&D Beyond's own icon components as-is instead —
`ImmunitySvg`/`ResistanceSvg`/`VulnerabilitySvg`
(`media.dndbeyond.com/character-app/static/js/.../smartComponents/Svg/icons/`,
indexed in `design-reference/markup/svg-index.html`), a deliberate exception
to the usual stance for this specific icon. Vulnerability was also added —
previously plain text under "Vulnerabilities" in the panel's generic list,
now its own icon row like Resistance/Immunity.

Three new assets, `frames/dnd_icon_{resistance,immunity,vulnerability}.svg`
— each `fill="currentColor"`, copied verbatim from `svg-index.html`
(Resistance and Vulnerability are two-subpath shield+letter silhouettes;
the letter reads as a cutout via the SVG's own opposing winding order, not a
separate white fill — same file structure D&D Beyond ships). `DamageTypeIcon`
rebuilt around `FrameIcon` instead of a text-letter `<span>`: takes `svg` +
a `variant` (`resistance`/`immunity`/`vulnerability`) instead of a `letter`.
`.damage-type-icon` in `frames.css` dropped the clip-path/circle entirely —
now a plain 12×12px icon (D&D Beyond's own `.ddbc-{immunity,resistance,
vulnerability}-icon` size), tinted via a `--variant` modifier class:
Resistance/Immunity keep the confirmed `--defense-badge` green; Vulnerability
uses `--status-negative` (a downside, not a protection) — not confirmed live,
this app's reference character has neither, a deliberate placeholder pending
a real character with it.

Verified live: Resistance (Poison, Aria Emberfall) renders as the real
shield with a green fill and a white "R" cutout. Immunity/Vulnerability
verified via a temporary, non-persisted DOM injection (same SVG markup,
green/red) rather than mutating the seeded character's own data — both
render correctly, shield shape with the letter reading clearly. Frontend
`tsc --noEmit` clean; no backend change; no console errors.

---

## 2026-09-19 — Uppercase label typography (item 4) and the Hit Points box (item 5) matched to D&D Beyond, DOM-verified

Owner punch list, items 4 and 5, done together since both needed the same
reference lookup. Item 4's own assumption — the local static CSS mirror at
`design-reference/markup/media.dndbeyond.com/character-app/static/css/` would
have everything — held for item 4 (found `.ddbc-ability-summary__label`,
`.ct-proficiency-bonus-box__heading`, `.ct-proficiency-groups__group-label`,
`.ct-combat__summary-label` there, all confirming: `font-family: "Roboto
Condensed", Roboto, Helvetica, sans-serif`, **no** letter-spacing). It did
**not** hold for item 5: `.ct-quick-info__health` doesn't exist anywhere in
that static mirror (it's a client-rendered React tree; the mirror only has
built assets, not a captured DOM) — DOM-verified live against
dndbeyond.com/characters/50149479 instead, via `getComputedStyle` on every
text node inside the real `.ct-quick-info__health`.

**Item 4** — fixed the four categories the owner named, each individually
verified rather than guessed, matching the item's own instruction:
- `.ability__label` (STR/DEX/etc names): added `font-family:
  var(--font-condensed)`, removed a guessed `letter-spacing: .01em`, and
  `--label-size` corrected `7.5px` → `10px` — safe to lift directly since
  `.ability`'s own `81×95px` already matches `.ddbc-ability-summary`'s real
  size exactly (unlike the badges below, no scaling judgment needed).
- `.badge__heading` ("Proficiency"/"Walking"): added `font-family`, removed
  `letter-spacing: .08em`. Font-size left at `7.5px` — `.badge` is `85×85px`,
  D&D Beyond's own `.ct-proficiency-bonus-box` is a differently-scaled
  `94×89px`, so its `12px` heading doesn't transfer 1:1 without its own
  visual check; out of scope here.
- `.panel__title` (every section panel's heading, including "Defenses"/
  "Conditions"): added `font-family` only — size/weight/uppercase already
  matched `.ct-combat__summary-label` exactly.
- `.panel__list-label` (Proficiencies' own category labels, e.g. "Armor"/
  "Weapons"): added `font-family` only — already matched
  `.ct-proficiency-groups__group-label` otherwise.

Left out: the item's own closing "audit uppercase text handling generally."
`frames.css` has 40 separate `text-transform: uppercase` rules; the four
above are the ones the owner explicitly named. Reconciling the rest against
verified reference values is real, separate work, not something to guess at
in the same pass — noted in the audit file as still open.

**Item 5** — `.hit-points__*` (the top-row inline box, `HitPoints.tsx`; the
sidebar `HpManagementPanel.tsx`'s own same-prefixed classes are a different
mold and weren't touched):
- `.hit-points__title` ("Hit Points" caption): `8px` → `13px`, added
  `font-family: var(--font-condensed)`, removed `letter-spacing: .09em`,
  recolored `var(--text-muted)` → `var(--text-primary)` — the real caption is
  near-black, not the muted gray this had guessed.
- `.hit-points__stat-label` (Current/Max/Temp): `8px` → `13px`, added
  `font-family`, removed `letter-spacing: .07em`. Color already correct.
- `.hit-points__stat-value` (the numbers): `20px` → `26px`, `font-weight: 600`
  → `500`, `font-family: inherit` → `var(--font-ui-native)` — D&D Beyond's own
  value uses a system-UI stack (`SF Pro Display`/`Inter`/...), not Roboto
  Condensed, for this one element; `--font-ui-native` is this app's existing
  token for the same intent, used instead of copying their exact stack.
  `.hit-points__stat-slash` ("/") matched too: `20px` → `26px`, weight `400` →
  `500`, `font-family: var(--font-body)` added explicitly (plain Roboto,
  which D&D Beyond's own slash also uses — this app's `body` already
  inherits that font, so this was cosmetically already correct, made
  explicit rather than left to inheritance).
- `.hit-points__action` (Heal/Damage): `8px` → `9px`, `font-weight: 700` →
  `500`, added `font-family`, removed `letter-spacing: .03em`. Colors
  (green/red via `--status-positive`/`--status-negative`) already matched.

All five font-size increases were verified safe before applying: `.hit-points`
is `317px` wide with a `400/113` aspect ratio (≈89px tall) — pixel-identical
to the real `.ct-quick-info__health`, so lifting its real values directly
carried no overflow risk, confirmed after the fact with a live screenshot
(no clipping, no wrapping).

Verified live against Aria Emberfall: ability boxes, Proficiency/Walking
badges, Hit Points box, Defenses/Conditions, Saving Throws, and Proficiencies
& Training all screenshotted after the change — no overflow, no console
errors. Frontend `tsc --noEmit` clean; no backend change.

---

## 2026-09-19 — `ProficienciesPanel`/`DefensesConditionsPanel`'s `<dl>` replaced with plain divs

Owner punch list, item 3: a `<dl>`'s own default browser margin (`margin: 1em
0` on the element itself, not cleared by `frames.css`'s existing `dt`/`dd`
overrides) added space inside the Proficiencies & Training panel that no rule
was meant to put there. `DefensesConditionsPanel.tsx`'s Vulnerabilities/
Condition Immunities list shares the exact same `panel__list` markup and CSS,
so it had the identical latent bug — fixed both together rather than leaving
one half of a shared class still emitting `<dt>`/`<dd>`.

Both components now render `<div className="panel__list" role="list">` with
one `<div className="panel__list-item" role="listitem">` per category,
holding `panel__list-label`/`panel__list-value` children — the same
label/value div pattern `EntityDetailPanel.tsx`'s own `entity-detail__property`
already uses, not a new one. `frames.css`'s `.panel__list dt`/`dd` tag
selectors became `.panel__list-label`/`.panel__list-value` class selectors,
with the 8px inter-item gap moved onto `.panel__list-item` itself — same
visual spacing, unchanged.

Verified live against Aria Emberfall: Proficiencies & Training (Armor/Weapons/
Languages) renders with no leftover gap. Defenses/Conditions' own list branch
is unexercised by this character (resistances only, no vulnerabilities/
condition immunities) — code inspection confirms the same markup, not a live
render. No console errors. Frontend `tsc --noEmit` clean; no backend change.

---

## 2026-09-19 — Shared `ListRow` primitive; every list row now opens Entity Detail from anywhere in the row

Owner punch list (`dnd-5e-sheet-fidelity-audit.md`, "2026-09-17 — owner punch
list"), items 2 then 1, done together as their own suggested order recommended:
`AttackRow`/`SpellAttackRow`/`FeatureActionRow`/`FeatureTraitRow`/`ItemRow`/
`ExtraRow` only opened Entity Detail from their name cell; `SpellRow` already
opened from anywhere in the row (an earlier, one-off fix), and D&D Beyond's own
rows all behave that way.

Surveyed the seven components before extracting anything, per the punch list's
own caution not to assume a shared shape. The real common piece turned out to
be just the click target and the stopPropagation convention — the divider
genuinely differs three ways (`AttackRow`/`SpellAttackRow`/`SpellRow`: dotted
top border, skips the first row; `ItemRow`/`ExtraRow`: solid bottom border,
every row; `FeatureActionRow`/`FeatureTraitRow`: no divider, just spacing), so
the new `sheet/ListRow.tsx` deliberately does not own divider styling — each
row keeps its own class for that, unchanged.

`ListRow` renders a `role="button"`/`tabIndex={0}` div (Enter/Space open it,
same as a click), extracted verbatim from `SpellRow.tsx`'s own pre-existing
inline version, which now uses the shared component instead of its own copy.
Every other row was ported: its name cell changed from a `<button>` to a
`<span>` (the row itself is the interactive element now — a button nested
inside a `role="button"` row would double up), and every real interactive
child got `event.stopPropagation()` added to its own `onClick`: the Hit/Damage
roll buttons (`AttackRow`, `SpellAttackRow`), the equip flag and remove button
(`ItemRow`), and the box-track/use-pool buttons. The last of those lives in
two shared primitives, `sheet/BoxTrack.tsx` and `sheet/UsePoolStepper.tsx` —
stopPropagation was added directly in their own button handlers rather than
threading it through every `onUse`/`onRestore` callback, since both are
already "stop the click here" primitives and the change is inert for their
other consumer (`SpellCast.tsx`'s slot tracker, which isn't inside a `ListRow`).

Verified live against the seeded Aria Emberfall: Actions tab (clicking a
Longsword row's Notes cell opened its detail; the Hit button still just
rolled, no detail panel), Inventory tab (Chain Mail's Notes cell opened detail;
its equip checkbox toggled without opening anything), Features tab (Second
Wind's description opened detail; Battle Fervor's pool stepper still spent/
restored a use without opening anything), Extras tab (Warhorse's AC cell
opened its stat block). Frontend `tsc --noEmit`/`vite build` clean; no backend
change.

Left out, per the punch list's own scope: unifying the three divider
treatments into one shared style (a real visual change across three tabs, not
requested), and the punch list's remaining items 3–7 (`ProficienciesPanel`'s
`<dl>`, uppercase typography, Hit Points typography, resistance icons, and the
"Manage Custom" feature) — untouched.

---

## 2026-09-17 — Hit Points box opens an "HP Management" sidebar, with a removable manual max-HP adjustment

**Direct owner request**: make the Hit Points box match D&D Beyond completely,
including its sidebar — clicking anywhere inside the box (except the numbers and
its own inline Heal/Damage/Temp controls) should open the same kind of "HP
Management" panel D&D Beyond opens from its own Hit Points box.

DOM-confirmed live against D&D Beyond's own sidebar panel: a Current/Max/Temp row,
then a Healing/Damage pair with a live "New HP" preview and Apply/Cancel shown only
once a pending edit exists. This app goes one further for Current (directly
editable here, display-only on D&D Beyond) and deliberately does **not** replicate
D&D Beyond's own "Max HP Modifier"/"Override Max HP" pair — the owner asked for a
single visible, removable adjustment instead, for a concrete case: a story effect
(a magic forest's blessing) temporarily raises max HP, and should be easy to remove
again once the character leaves the effect, rather than living as an opaque number
in a field nobody remembers the reason for.

**Backend**: `Dnd5eSheet` gained `maxHitPointsAdjustment` (an `int`, defaults to 0
on rows persisted before it existed — same lenient-primitive-default story every
prior field addition to this JSONB payload has used, no migration). Folded into
`Dnd5eFormulas.maxHitPoints` (floored at 1, so a punitive adjustment can never drop
max HP to zero or below) — every existing consumer of that formula (heal clamp, hit
dice full-heal, the calculator's own hit points contribution trace) picks it up for
free, no other call site needed touching. New `SheetMutator.setMaxHitPointsAdjustment`
sets it as an absolute value (`0` clears it, not additive) and clamps current hit
points down if the new max falls below it — the same PHB rule any max-HP reduction
follows. New endpoint `POST /api/characters/{id}/hit-points/max-adjustment`.

**Frontend**: new sidebar mold, "HP Management" (`sheet/HpManagementPanel.tsx`,
`HpManagementRequest` in `sheet/api.ts`) — see `ui-design-system.md`'s "The nine
molds" for why this needed its own mold rather than reusing Mechanic. `HitPoints.tsx`
gained an optional `onOpenManagement` — the whole box becomes a click target
(same "card opens the sidebar, its own live controls opt out via `stopPropagation`"
pattern `AbilityBox` already established), with the Current/Max stat-value numbers
also opting out per the owner's explicit exception, alongside the existing
Heal/Damage/Temp controls. `onOpenManagement` is optional so `ExtraRow.tsx`'s reuse
of `HitPoints` (already inside its own Entity Detail body) stays inert — opening a
second sidebar from within the first has no real target. Max HP renders bold with a
trailing asterisk (tooltip: the signed adjustment amount) whenever the adjustment is
nonzero, in both the inline box and the sidebar — a visible cue per the owner's own
request, rather than a silently different number. The sidebar panel stays in sync
with the inline box's own controls while both are visible, via the same live-refresh
treatment `spellManagement`/`conditions` already get in `CharacterSheetScreen.tsx`'s
`displayedSidebarContent`.

Verified live against the seeded Aria Emberfall (30/44, 5 temp): opened the panel
from the box interior, confirmed clicking the Current/Max numbers and the Heal
button do not also open it; added a +2 max HP adjustment (Max → 46, bold with
asterisk in both places), healed into the new max (44 → 46); removed the
adjustment and confirmed current clamped back down to 44 automatically; used the
sidebar's own Healing/Damage fields with a live "New HP" preview and confirmed
damage still consumes temporary hit points first (server rule, unchanged); used the
sidebar's Current and Temp fields directly to restore the character to its original
seeded values afterward. Backend `./gradlew :apps:api:test` green (new
`Dnd5eSheetMutatorTest`/`Dnd5eSheetCalculatorTest`/`CharacterControllerTest` cases
for the adjustment); frontend `tsc --noEmit`/`vite build` clean.

---

## 2026-09-17 — Spells tab row dividers matched to D&D Beyond

**Direct owner request**: the Spells tab was missing the thin dotted row divider
the Actions tab already has. DOM-measured live against D&D Beyond's own
`.ct-spells-spell` (Helga Flinthand's Spells tab): `border-top: 1px dotted
#eaeaea`, `padding: 8px 0` with no extra margin — matches this app's own
`--border-divider` token and `.spell-row`'s existing padding exactly. Spell rows
already share the Actions tab's `.action-list` container (grouped one list per
spell level), so the fix is one rule: `.action-list > .spell-row:not(:first-child)
{ border-top: 1px dotted var(--border-divider); }` — no extra spacing added,
unlike `.action-row`'s own divider rule, since the base padding already matches
D&D Beyond's real spacing. Confirmed live per level section (Cantrip, 1st, 2nd) —
the divider correctly resets at each section boundary, never bleeding between
levels. `tsc --noEmit`/`vite build` clean.

---

## 2026-09-17 — Casting at a higher spell slot level now scales and rolls the dice

Two problems, one slice: **(1)** `SpellConverter`'s dice extraction only recognized
5etools' `{@damage}` tag, so a healing spell's `{@dice}`-notated roll (Cure Wounds'
own `{@dice 1d8}`) was silently missing — no healing roll button existed anywhere
in the app. **(2)** casting a spell at a higher slot level had no effect on any
roll at all: "Cast" (the sidebar's slot-level picker) and the row's damage/heal
roll button were two disconnected actions, and 5etools' own "At Higher Levels"
scaling data (added earlier today) was prose only, not usable by a roll.

**Root cause of (1), fixed**: `SpellConverter.damageDice` now falls back to
`{@dice}` when `{@damage}` isn't found, gated on the spell being tagged healing
(`miscTags` has `HL`) so a non-healing spell's incidental `{@dice}` mention (rare,
but real risk) doesn't get mistaken for its primary roll.

**(2), designed after studying D&D Beyond's own real behavior live** (Helga
Flinthand's Guiding Bolt, dndbeyond.com/characters/50149479, at her actual 1st/3rd
level slots): D&D Beyond's own Cast button only logs "cast at level N" — clicking
it never rolls anything, and its damage/healing preview number is inert (no
`onclick`/`role`, DOM-confirmed). This app's whole premise is active mechanics, so
it goes further, direct owner request:

- `SpellConverter` gained structural (not just prose) scaling data:
  `higherLevelsDamageDiceCount`/`higherLevelsDamageDiceSides`, parsed from the
  same `{@scaledamage}`/`{@scaledice}` tag's 3rd pipe segment `higherLevelsDescription`
  already used for its own display text. Threaded through the full domain chain
  (`SpellCatalogueData` → `Spell`/`Dnd5eSpell` → `SpellResponse` → frontend `Spell`),
  same pattern as `higherLevelsDescription` itself.
- `RollRequest`/`MechanicResolver.resolve` gained an optional `castAtLevel`.
  `Dnd5eMechanicResolver#scaledDiceCount`: adds `higherLevelsDamageDiceCount` dice
  per slot level cast above the spell's own base level — falls back to the
  unscaled base count if `castAtLevel` is null (the row's own quick-roll target
  never sends one) or if the scaling die's sides don't match the base die's
  (`ResolvedRoll` only notates one die size; real 5e spells always scale with the
  same die type, but this guards a hypothetical mismatch rather than silently
  rolling the wrong die).
- `SpellCast.tsx` redesigned to match D&D Beyond's own layout, DOM-measured live
  (a `−`/`+` level stepper replacing the old `<select>`, the remaining-slots count
  as a small badge on the Cast button itself) plus a live damage/healing preview
  that recomputes as the level changes — **and, unlike D&D Beyond's own inert
  number, this one is real**: clicking Cast now also rolls — the spell's attack
  roll if it has one (damage stays a deliberate separate follow-up once a hit is
  confirmed, same as a weapon attack), else its damage/healing roll, already
  scaled to the chosen level. The row's own quick-roll target is untouched —
  always base level, a manual shortcut needing no slot.

**Verified live, not just unit-tested**: re-ran the ingestion pipeline for the
whole 570-spell corpus with the fixed extraction, re-imported, re-seeded Aria
Emberfall (her stored sheet was a snapshot predating these fields). Confirmed
Cure Wounds' healing roll button is back (`1d8 ❤`); temporarily granted Aria
2nd/3rd-level slots via a direct DB update (reverted after) to exercise the
stepper beyond her real 1st-level-only Eldritch Knight slots — the preview
correctly read 1d8 → 2d8 → 3d8 as the level changed, and clicking Cast at 3rd
level produced a real server-resolved roll (`3d8 (2, 7, 5) = 14`, no
spellcasting modifier since Aria's is +0) logged in the dice tray.

New backend tests: `Dnd5eMechanicResolverTest` (scale-up, no-scale-at-base-level,
no-scale-without-a-level, sides-mismatch-fallback), `SpellConverterTest` (the
`{@dice}` fallback, and a non-healing spell correctly *not* falling back to it).
`tsc --noEmit`/`vite build`/`:apps:api:check` all clean.

---

## 2026-09-17 — `PlayerService`'s player-creation race fixed

Fixes the race condition found and flagged (not fixed) during slice 4's live
verification (see this changelog's "5etools ingestion pipeline, slice 4" entry):
two concurrent first-sight requests for the same subject both pass
`currentPlayer`'s own `findBySubject` check, then both attempt to create the row;
the loser previously 500'd with an uncaught `DataIntegrityViolationException` on
`players_subject_key`.

The naive fix — a local try/catch around the insert — doesn't work: `save()` defers
the actual insert (and so the constraint check) to flush/commit time, which for the
method's own ambient `@Transactional` context happens well after the method already
returned, outside any local try/catch. `PlayerService.createOrFindPlayer` now runs
the insert inside its own `TransactionTemplate`-driven `PROPAGATION_REQUIRES_NEW`
transaction, forcing the commit (and so the constraint violation, if any) to happen
synchronously inside this method's own call frame. A losing attempt catches
`DataIntegrityViolationException` and re-reads the winner's row instead of failing
the request; no duplicate `PlayerCreatedEvent` fires for the loser.

New `PlayerServiceConcurrencyTest` (not `@Transactional`, unlike the existing
`PlayerServiceTest` — a shared test transaction would serialize what this needs to
genuinely race): 8 threads call `currentPlayer` concurrently for a brand-new subject,
asserts all resolve to the same player id, none throw. `PlayerServiceTest`'s own
`createsAPlayerOnFirstSightOfASubject` needed a small fix alongside this: it compared
the created `Player` against a re-read one with default (reference) equality, which
now fails because the two objects come from different JPA persistence contexts (the
new `REQUIRES_NEW` transaction has its own) — switched to comparing by `id()`, same
pattern the neighboring test already used.

`:apps:api:check` green, including the new concurrency test running for real against
Testcontainers Postgres.

---

## 2026-09-17 — `entriesHigherLevel` extracted and rendered (5etools-ingestion.md, open question 5)

Resolves the plan's last deferred item: 5etools' own "At Higher Levels" spell-scaling
text now flows end to end — extracted from real 5etools data, through the full
domain model, into the frontend, and verified rendering live in the running app.

**Investigated before writing any code**: forked research into real downloaded
5etools data found `entriesHigherLevel` on 200 of ~2,900 spells (106 in-scope PHB
spells), canonically shaped `[{"type":"entries","name":"At Higher
Levels","entries":[...]}]` — 22 non-PHB outliers use the label "Using a
Higher-Level Spell Slot" instead, harmless since the label itself isn't used, only
the nested `entries`. `{@scaledice}`/`{@scaledamage}` (92 real occurrences, always
exactly `base|levelRange|perLevel`) turned out to need a genuine correction: an
initial pass (mine, then independently the research fork's) both guessed the wrong
segment as "display text." Verified against real, independently-known scaling facts
(Inflict Wounds: 3d10 base, +1d10/level; Arms of Hadar: 2d6 base, +1d6/level) that the
**3rd segment** (the per-level increment) is correct — not the mechanical "2nd
segment = display" rule `TagMarkupStripper` uses for every other tag, and not the
base amount either. `@variantrule name|sourceCode` has the same "2nd segment isn't
display" trap (the source code, not the name). Both special-cased in
`TagMarkupStripper`, covered by 3 new tests.

**`SpellConverter`** gained `higherLevelsDescription()`: reads `entriesHigherLevel`'s
first element's `entries`, strips markup via the now-corrected `TagMarkupStripper`,
null when absent (most cantrips, some leveled spells). 2 new tests, one exercising the
`{@scaledamage}` correction directly.

**Threaded through the whole domain model** — a new nullable field, end to end:
`SpellCatalogueData` (backend, catalogue `data` shape) → `Spell`/`Dnd5eSpell`
(ruleset) → `SpellResponse` (API DTO) → frontend `Spell` type (`api.ts`). Every
existing call site constructing one of these four Java records (8 files, mostly
tests) updated for the new trailing constructor argument.

**Rendered live**: `spellDetail.tsx`'s `buildSpellDetailRequest` appends a bolded
"At Higher Levels." paragraph after the base description (matching D&D Beyond's own
layout) via `EntityDetailRequest`'s existing `body` escape hatch, only when the field
is present — spells without one render exactly as before.

**Verified for real, not just unit-tested**: re-ran `ingest5etools` for the whole
570-spell corpus (regenerating every file with the new field — spot-checked
`cure-wounds.json`/`inflict-wounds.json` against the same real facts used to correct
the tag stripper), re-imported into the local dev catalogue, re-seeded Aria Emberfall
(her stored sheet was a snapshot from before this field existed — had to force a
fresh seed the same way as slice 4's own verification), and confirmed Cure Wounds'
sidebar shows "At Higher Levels. When you cast this spell using a spell slot of 2nd
level or higher, the healing increases by 1d8 for each slot level above 1st." live in
the running app.

`tsc --noEmit`, `vite build`, and `:apps:api:check` all clean.

**Left out, on purpose**: the two other 5etools-ingestion follow-ups flagged earlier
today (tags frontend plumbing — `CatalogueEntry.tags` → `EntityDetailRequest.tags`;
extending the pipeline to classes/species/items/feats/creatures) — not started this
entry, still open.

---

## 2026-09-17 — 5etools ingestion pipeline, slice 4: DevCharacterSeeder migrated to the catalogue

Final slice of `features/5etools-ingestion.md` — closes the plan's "Done when" in
full. `DevCharacterSeeder.java`'s 18 hand-typed spell literals (the second copy that
could, and did, drift from the real one — the whole trigger for this feature) are
gone. Aria's spells are now built at seed time from real catalogue data:

- `CatalogueService` gains `findBySlug(systemId, kind, slug)` — unredacted, same
  reasoning as the existing `getUnredacted(UUID)`: a character's own copy of a spell
  is no longer catalogue browsing. Backed by the already-existing (package-private)
  `CatalogueEntryRepository.findBySystemIdAndKindAndSlug`.
- `CatalogueEntryNotFoundException` gains a slug-shaped constructor alongside its
  existing UUID one.
- `DevCharacterSeeder` keeps its `SEED_SHEET_JSON` template for everything except
  `spells` (attacks, features, background, extras, etc. — unchanged, still a
  literal); at seed time it parses the template, replaces `"spells"` with entries
  built from `ARIA_SPELLS` (18 `(key, catalogue slug)` pairs) via
  `CatalogueService.findBySlug`, and re-serializes. Still plain `JsonNode` tree
  manipulation, no `ruleset.dnd5e` import — the package's own architectural boundary
  (documented in its class comment) holds.
- Three new unit tests (`DevCharacterSeederTest`, mocked `CatalogueService`/
  `CharacterRepository`, synthetic `CatalogueEntry` fixtures — not real book text):
  array size matches `ARIA_SPELLS`, a Fire Bolt-shaped spot check on field mapping,
  and confirmation every other sheet section survives untouched.

**Verified live, not just unit-tested**: deleted the local dev `test` player/character/
rolls rows, logged back in fresh through the real app (same Keycloak account) against
the real Postgres catalogue (570 spells from slice 3's real run), and confirmed Fire
Bolt's sidebar now shows the genuine PHB text live in the running UI — the exact bug
("A mote of fire streaks toward a target within range.", a hand-shortened paraphrase)
that started this whole feature, now fixed end to end from raw 5etools data through
to the rendered sheet.

**Found, not fixed — pre-existing, unrelated to this change**: `PlayerService
.currentPlayer` has a TOCTOU race (check-then-insert, no unique-constraint recovery)
that surfaced during the live verification above — two concurrent requests for a
brand-new player both tried to insert the `players` row, one hit
`players_subject_key`'s unique constraint and 500'd (as an uncaught
`DataIntegrityViolationException`, showing "Could not load characters" in the UI)
while the other succeeded. Normally invisible since a player's local row persists
forever after their real first login; only surfaced here because this session
manually deleted an existing row to re-test fresh-login seeding. Flagged for the
owner, not fixed — out of scope for this slice.

`:apps:api:check` green throughout.

**`features/5etools-ingestion.md` is now fully done**: every criterion in its "Done
when" section is satisfied.

---

## 2026-09-17 — 5etools ingestion pipeline run for real: 570 spells replace the 11 hand-typed ones

The owner populated `tools/5etools-data/` and `./gradlew :apps:api:ingest5etools
--args="--kind=spell"` ran against it for real, closing out
`features/5etools-ingestion.md`'s "Done when" (see that document's own updated section
for the full detail — this entry is the summary):

- **578 spells converted into 570 files.** 8 slug collisions, all the same spell
  reprinted across two in-scope sourcebooks (e.g. "Enervation" in both `AU` and `XGE`)
  — expected, not a bug.
- **Every field-mapping assumption from slices 1–2 held against real data**, verified
  before the full run by spot-checking Fire Bolt and Cure Wounds against real PHB
  text — this is the fix for the original bug that started this whole feature
  (Fire Bolt's `description` used to be a hand-shortened paraphrase; it's now the
  real PHB text, damage scaling included). All 570 files passed Bean Validation on
  import with zero failures.
- **Re-run proven byte-identical** against the real corpus (hashed every file, ran
  the tool again, zero differences) — not just `Ingest5eToolsRunnerTest`'s synthetic
  fixtures.
- **Imported into the local dev catalogue for real**: "Imported 570 catalogue
  entries" — pipeline stage 6, the existing `CatalogueImportService`, needed no
  changes and worked end to end with this pipeline's real output.

`content/dnd-5e/spells/` now holds 570 tool-generated files instead of 11 hand-typed
ones. `tools/5etools-data/` stays gitignored and local-only, as designed.

**Left out, on purpose**: `DevCharacterSeeder.java`'s migration to the catalogue
(the plan's open question 3) — its own slice, now unblocked since real catalogue
data exists, not built this entry.

---

## 2026-09-17 — Collab-source exclusion reverted to a phase-11 player toggle; default data path fixed

Two corrections found while getting ready to run the ingestion tool for real, once
real 5etools data actually landed in `tools/5etools-data/`:

**Default `--data` path was wrong.** A 5etools-src release zip extracts its *whole*
repo checkout, not just `data/` — the owner's instructions said to extract only
`data/`'s contents, but the natural result of unzipping a GitHub release is the full
checkout, landing the real data one level deeper at `tools/5etools-data/data/`.
`Ingest5eToolsMain`'s `DEFAULT_DATA_DIRECTORY` updated to match; `tech-stack.md`'s
"Local-only data" entry corrected. Verified every field-shape assumption
`SpellConverter` made without real data in hand (`time`/`range`/`duration`/
`components`/`spellAttack`/`miscTags`/`savingThrow`/`meta.ritual`) against real Fire
Bolt and Cure Wounds entries — every one matched exactly, no formatter changes needed.

**Collab-source exclusion reverted.** Investigated the real, downloaded
`books.json`/`adventures.json` for a machine-readable "collab" marker before running
the tool — found `LLK` (Lost Laboratory of Kwalish) with a distinct `group:
"supplement-alt"`, and `AitFR-AVT`/`SatO`/`FRHoF` missing from both index files
entirely (likely smaller D&D Beyond-exclusive content). Presented this to the owner as
candidates to exclude from `FiveEToolsSourceClassifier`'s collab set. The owner instead
reverted the whole approach: import everything (still except `XPHB`), and build a
D&D Beyond-style toggle in phase 11 letting the player exclude collab sources for their
own character, rather than deciding it once for the whole catalogue at ingestion time
— `adr-0006`'s **original** proposal, briefly replaced earlier the same day (this
changelog's own "5etools ingestion pipeline: all five open questions resolved" entry)
and now reverted. `adr-0006`, `roadmap.md`'s phase 11 entry, and
`features/5etools-ingestion.md`'s stage 2 updated accordingly.
`FiveEToolsSourceClassifier` needed no behavior change (its collab set was already
empty by default) but gained case-insensitive source-code comparison — real data mixes
case inconsistently (`AitFR-AVT`, `SatO`), which the classifier's exact-match `Set`
lookup would have silently mismatched had a caller ever populated it.

`:apps:api:check` green.

---

## 2026-09-17 — 5etools ingestion pipeline, slice 3: CLI entry point and Gradle task

Third and final implementation slice of `features/5etools-ingestion.md`. The
orchestration itself is `Ingest5eToolsRunner` (new, testable): load a converter's raw
entries (`FiveEToolsConverter.loadRawEntries`, new on the interface — each converter
now owns its own kind's file layout, `SpellConverter`'s glob is `spells/spells-*.json`),
drop out-of-scope sources per-entry via `FiveEToolsSourceClassifier`, convert, write
one pretty-printed file per slug under the output directory — overwriting by slug, so
re-running is safe. `Ingest5eToolsMain` is a thin CLI wrapper around it (arg parsing,
default paths, the converter registry) so the orchestration itself is unit-testable
against scratch directories instead of the real `content/dnd-5e/`. New Gradle task:
`./gradlew :apps:api:ingest5etools --args="--kind=spell"` (`--data=` overrides the
default `tools/5etools-data/`), `JavaExec` with `workingDir` pinned to the repo root so
the default relative paths resolve regardless of where `gradlew` was invoked from.

**Bug found and fixed while smoke-testing the real task** (not just the unit tests):
a UTF-8 BOM at the start of a data file made Jackson reject the whole file as invalid
JSON. `Files.readString` doesn't strip a BOM on its own. `FiveEToolsDataSource` now
strips one if present, covered by a new test — found because the smoke-test fixture
happened to be written by a BOM-emitting tool, not something the unit tests (which
write fixtures via `Files.writeString`, no BOM) would have caught on their own.

**Verified for real**: ran the actual Gradle task (not just `Ingest5eToolsRunner`'s
unit tests) against a scratch data directory with one synthetic spell, confirmed the
output file's shape matches `CatalogueEntryImport` exactly, then deleted it — real
`content/dnd-5e/spells/` still has exactly its original 11 hand-typed files, untouched.
`:apps:api:check` green throughout.

**This closes `features/5etools-ingestion.md`'s own "Done when" for the tool itself.**
What's still open, deliberately, per that document's own scope: the owner has not yet
run this against the real 5etools download (`tools/5etools-data/` is still empty on
this machine), so no real spell file has actually been regenerated by the tool yet —
that's the owner's own next action, not a code task. `DevCharacterSeeder.java`'s
migration to the catalogue (the plan's open question 3) is its own slice, sequenced
after real spell data exists, not built this entry.

---

## 2026-09-17 — 5etools ingestion pipeline, slice 2: SpellConverter

Second implementation slice of `features/5etools-ingestion.md`. `FiveEToolsConverter`
(new interface — `kind()` + `convert(JsonNode)`, one implementation per content kind,
the Open/Closed seam the tool-shape decision asked for) and `SpellConverter`, its
first and only implementation, both in `dev.omnisheetvault.api.catalogue`.

`SpellConverter` implements the field-mapping table already in the plan doc
(school-code word map, casting time/range/duration/components formatters, save
ability/damage type from their respective arrays), plus five `SpellCatalogueData`
fields the plan's table had missed entirely — a real gap found while building this
slice, surfaced to the owner before writing the guesswork:

- `attackRoll` — from 5etools' own `spellAttack` array (present = true).
- `damageDiceCount`/`damageDiceSides` — parsed from the first `{@damage NdM}` tag
  found in `entries`.
- `notes` — derived, not a separate 5etools field: duration text + components (with
  material in parens) when concentration, else just components-with-material.
  Verified against all 11 existing hand-typed spell files before implementing, not
  guessed — the formula matches every one, including Bless's concentration+material
  case and Sleep's material-only case.
- `effectSummary` — **direct owner decision**: this field turned out not to be a
  5etools fact at all (traced to `changelog.md`'s 2026-08-16 "Phase 10, slice 8: Spells
  tab" entry: it was transcribed by hand off D&D Beyond's own "Effect" column, a UI
  categorization 5etools' raw data has no equivalent field for). The owner accepted a best-effort
  heuristic anyway, explicit that drift from D&D Beyond's real labels here is low-stakes:
  damage present → "Damage"; `miscTags` contains `HL` → "Healing"; `conditionInflict`
  non-empty → "Control"; school is divination → "Detection"; else → "Buff".

Also populates `CatalogueEntryImport.tags` (catalogue-level, not yet wired to the
frontend — see the plan doc's own "Tags" section for why that's a separate, later
slice) from the same `HL` check plus `conditionInflict`'s condition names,
title-cased.

Six unit tests, synthetic fixtures shaped to this converter's own assumptions about
5etools' JSON — **not verified against the real download yet**, since it isn't in
this session; covers the attack/damage cantrip, a healing spell, a condition-inflicting
control spell, a concentration+ritual spell with an object-shaped material and a cone
range, apostrophe slugification, and the source-book-name fallback. `:apps:api:check`
green.

**Left out, on purpose**: no CLI entry point yet (slice 3) — nothing is importable from
this slice either; `classes.fromClassList` (which classes can learn the spell) stays
unmapped, as the plan's own table already flagged; `entriesHigherLevel` stays deferred.

---

## 2026-09-17 — 5etools ingestion pipeline, slice 1: shared pipeline infrastructure

First implementation slice of `features/5etools-ingestion.md`, following the slice
order proposed and approved the same day. New classes, all in
`dev.omnisheetvault.api.catalogue` (kept in the existing package rather than a new
one — `CatalogueEntryImport`, the target shape a converter will eventually build,
is package-private, and Java visibility doesn't extend across a parent/subpackage
split):

- `FiveEToolsDataSource` — reads a raw 5etools JSON file by path relative to the
  owner's local `tools/5etools-data/` folder (now gitignored, documented in
  `tech-stack.md`'s new "Local-only data" section). Not a Spring bean — constructed
  directly, no application context needed for a pure data transform.
- `TagMarkupStripper` — strips 5etools' `{@tag payload|display|source}` inline
  markup (pipeline stage 3). Implemented generically off the pipe convention itself
  (display segment if present, else payload) rather than a hand-maintained per-tag
  table, so it needs no updates as 5etools adds tags; runs multiple passes to resolve
  tags nested inside another tag's payload.
- `FiveEToolsSourceClassifier` — pipeline stage 2's source filter: excludes `XPHB`
  (the 2024 revision, per `adr-0006`'s existing scope rule) and a caller-supplied set
  of collab/promotional source codes. That set is a placeholder today — `sources.json`
  still needs checking once downloaded for a machine-readable marker that could
  replace the hand-maintained list.
- `FiveEToolsIngestException` — this tool's own unchecked exception, matching
  `CatalogueImportException`'s existing precedent.

All three covered by unit tests using synthetic fixtures (not real book text, same
precedent as `CatalogueImportServiceTest`'s own note on why). `:apps:api:check` green.

**Left out, on purpose**: no converter yet (slice 2), no CLI entry point (slice 3) —
this slice is infrastructure only, nothing importable from it yet.

---

## 2026-09-17 — 5etools ingestion pipeline: all five open questions resolved (no code this entry)

The owner answered every open question `features/5etools-ingestion.md` left for a
later session:

1. **Raw data location**: [5etools-mirror-3/5etools-src](https://github.com/5etools-mirror-3/5etools-src)
   release zip, extracted by the owner into a new gitignored `tools/5etools-data/`
   (added to `.gitignore` and `tech-stack.md`'s new "Local-only data" section) —
   the tool reads from that folder, it does not fetch from GitHub itself.
2. **Source filtering**: import every sourcebook except `XPHB` (already excluded by
   `adr-0006`'s 2014-only scope) and collab/promotional sources (*A Copper for a
   Song* and similar) — this also resolves `adr-0006`'s own open question about
   letting the player toggle non-core sources at character creation: since collab
   sources are never imported at all, there is nothing left to toggle. `adr-0006`
   and `roadmap.md`'s phase 11 entry updated accordingly.
3. **`DevCharacterSeeder.java`**: migrates to reading Aria's spells from the real
   catalogue by slug, once the converter produces real data — sequenced as its own
   slice after the converter, not before.
4. **Tool shape**: a dependency-free Gradle `JavaExec` (no Spring context), but
   structured for the owner's stated near-future need (classes/species/items/feats/
   creatures, not just spells) — shared pipeline pieces (5etools loader, `{@tag}`
   stripper, source classifier) plus a `FiveEToolsConverter<T>`-per-kind seam
   mirroring this codebase's own `ruleset` registry pattern, so a new content kind
   is a new converter class, never an edit to `SpellConverter`.
5. **`entriesHigherLevel`** stays deferred, as already scoped — no action.

`features/5etools-ingestion.md` updated throughout (pipeline stages 1–2, the open
questions section, the status line). **Left out, on purpose**: no implementation
this entry — planning and documentation only. Next step is proposing a slice order
for the owner's approval before writing any code, per `ground-rules.md`'s workflow
rule for changes larger than one vertical slice.

---

## 2026-09-17 — Entity Detail sidebar header: school icon and granting-class line

Closed two of the three items logged the same day in "Entity Detail sidebar
gaps logged" (below) — the header icon/parent line; the "Customize" section
stays out of scope, unchanged.

`EntityDetailRequest` (`sheet/api.ts`) gains two optional fields matching
D&D Beyond's own shared `Header`: `parent` (a line above the name — the
class/item/feature that grants this entity) and `icon` (a `ReactNode` beside
the name). `EntityDetailPanel.tsx` renders them: `.entity-detail__parent`
above an `.entity-detail__heading` flex row holding the icon and title.
`spellDetail.tsx`'s `buildSpellDetailRequest` feeds `spell.className` as
`parent` and a `FrameIcon` of `SCHOOL_ICONS[spell.school]` (already built,
previously only used in `SpellAttackRow.tsx`) as `icon` — no new assets
needed. Every other Entity Detail trigger (attacks, features) has no
icon/class data yet and simply omits both fields, same precedent as the
type's other optional fields.

Also added the missing divider above `.entity-detail__actions` (D&D Beyond's
own `.ct-spell-caster` sidebarSeparator, confirmed live — the properties/
description/tags dividers were already correct from the prior restyle).

**Verified live** against dndbeyond.com/characters/50149479's Guiding Bolt
panel: parent-line styling adjusted from an initial bold-uppercase guess to
plain sentence case/weight after the live comparison showed D&D Beyond's own
"Cleric" line is plain, not an eyebrow label. Confirmed in this app for a
cantrip (Fire Bolt), a leveled spell with a cast action bar (Cure Wounds,
divider correct above and below the cast block), and a non-spell entity
(Longsword — no icon/parent, no visual gap left behind). `tsc --noEmit` and
`vite build` clean.

**Fixed same day, owner-reported**: the name sat top-aligned against the
icon instead of centered — `.entity-detail__title`'s own `margin-bottom: 8px`
became part of its flex-item box inside `.entity-detail__heading`, so
`align-items: center` centered that margin box rather than the visible text,
pulling the text upward relative to the icon. Moved the 8px gap onto
`.entity-detail__heading` itself and zeroed the title's own margin — same
total spacing below the row, icon and name now centered on the same line.

**Left out, on purpose**: the "Customize" collapsible — no override system
for spell/action values exists in this app yet, a real feature, not styling.

---

## 2026-09-17 — 5etools ingestion pipeline planned; spell detail sidebar gaps logged (no code this entry)

**Direct owner request**: while reviewing the just-restyled Entity Detail
sidebar, the owner flagged it's still not quite right compared to D&D
Beyond — missing the school-of-magic icon and the granting-class line above
it, one missing divider (before the cast/level-picker block), and no
"Customize" section — and asked these be logged as pending, not fixed this
pass. See `dnd-5e-sheet-fidelity-audit.md`'s "Tracked, waiting on the owner"
for the full DOM-measured detail (D&D Beyond's shared `Header`/`SpellCaster`
components).

**Also requested**: a written plan for ingesting 5etools spell data properly
(triggered by the owner separately catching that seeded spell descriptions
are hand-paraphrased, not 5etools' real text — a genuine `adr-0006` policy
violation), to execute in a later session, not this one. Written as
`features/5etools-ingestion.md`: pipeline stages, a full 5etools-field →
this-project's-schema mapping table, and a concrete answer to "how do we
implement tags" (this app has two disconnected "tags" concepts already —
`CatalogueEntry.tags`, unwired to the character sheet, and
`EntityDetailRequest.tags`, currently only Concentration/Ritual — the plan's
recommendation is to populate the former from 5etools' real `miscTags`/
`conditionInflict` and thread it through, rather than approximating with the
already-differently-purposed `effectSummary` field). Cross-linked from
`adr-0006-5etools-as-content-source.md`'s Consequences section.

**Left out, on purpose**: no implementation this entry — this is a planning
and documentation pass only, per the owner's own instruction ("monte o plano
mas executaremos depois").

---

## 2026-09-17 — Entity Detail sidebar restyled to match D&D Beyond's own (spell detail fidelity, item 1)

**Direct owner request**: the spell description sidebar "não está nada
parecida" with D&D Beyond's own — analyze its `.ts`/`.tsx`/`.css` and match
the layout. Read `_spell-detail.scss`/`_action-detail.scss`,
`SpellDetail.tsx`/`ActionDetail.tsx` and the shared `InfoItem` component from
`design-reference/markup/`: D&D Beyond's spell/action/item detail panels all
share the same underlying shape — a `sidebarSeparator` mixin (10px margin,
10px padding, `1px solid #eaeaea` top border — matches this app's own
`--border-divider` exactly already) applied independently to each section
(caster/limited-use block, properties list, description, tags), and a
properties list that's a vertical stack of one-row-per-property `InfoItem`s
(bold label, plain value, same line), not a single wrapped paragraph of tiny
label:value pairs.

**Fix**: this app's own `EntityDetailPanel.tsx` — the shared "Entity Detail"
mold every spell/attack/feature/item sidebar panel already goes through
(`ui-design-system.md`'s eight molds) — rewritten to match: metadata now
renders as `.entity-detail__properties` (a `role="list"` stack of
`.entity-detail__property` rows), each section (`__properties`,
`__description`, `__tags`) getting its own independent top-divider via
`frames.css`, same as D&D Beyond's own accumulating-dividers look. Section
order now matches D&D Beyond's real order too: name, optional `subtitle`
(new optional field on `EntityDetailRequest`, D&D Beyond's own
`__level-school` line), `actionBar` (their "Caster"/"Limited Use" block —
confirmed live it renders *before* properties, not after), properties,
description/body, tags. Moved from inline `style={}` objects to `frames.css`
classes, matching every other component's own convention.

`spellDetail.tsx`'s `buildSpellDetailRequest` now builds a `subtitle` (new
`levelSchoolSubtitle` — "Evocation Cantrip" for a cantrip, school first;
"1st Level Evocation" otherwise, level first, matching D&D Beyond's own
order swap) instead of folding Level/School into `metadata` alongside
Casting Time/Range/etc.

**Verified**: `tsc --noEmit` and `vite build` clean; confirmed live for a
cantrip with an attack roll (Fire Bolt), one with a save DC (Acid Splash),
a leveled concentration spell with a cast action bar and a Concentration tag
(Fog Cloud), and a non-spell entity (the Longsword attack, no subtitle/
actionBar/tags) — the shared component change didn't regress the other
entity types, and in fact brings the same fidelity improvement to their own
sidebar panels too, not just spells'.

**Left out, explicitly deferred by the owner to a separate planning pass**:
while verifying this, the owner independently noticed spell *descriptions*
don't match 5etools' real text (e.g. Fire Bolt's seeded description is a
hand-shortened paraphrase, not the PHB's actual wording) — a real violation
of `adr-0006-5etools-as-content-source.md`'s "SEMPRE" policy, since the
`saveAbility`/`components`/`materialComponent`/`duration` 5etools pass
never touched the pre-existing `description` field. The owner wants this
solved as a proper 5etools → catalogue ingestion pipeline (per ADR-0006's
own "still open" converter tool, generalized to classes/races/items later),
not a one-off hand-patch of ~29 spell descriptions — planning for that
starts after this styling pass, not folded into it.

---

## 2026-09-17 — Armor Class content laid out like D&D Beyond's own (flex, not percentage layers)

**Direct owner request**: replace this app's own hand-tuned
`--heading-y`/`--value-y`/`--caption-y` percentage-based absolute positioning
for "Armor"/the value/"Class" with D&D Beyond's actual technique — the owner
pasted `.ddbc-armor-class-box`'s real markup and CSS: a flex column with
`justify-content: center` stacking the three lines as ordinary in-flow
content, `text-transform: uppercase`/`font-weight: 700`/`text-align: center`
set once on the container instead of on each line, `letter-spacing: -1px`
on the value.

**Fix**: `.armor` (`frames.css`) now uses `display: flex; flex-direction:
column; justify-content: center` with the shared text properties promoted
to the container; `.armor__heading`/`.armor__value`/`.armor__caption` dropped
their own `position: absolute; top: var(--x-y)` in favor of the container's
centering. `--heading-y`/`--value-y`/`--caption-y` are gone — no longer
needed, and no longer something to re-measure by hand every time this box's
own size changes (as it just did in the prior entry).

**Bug caught during verification**: the first pass dropped `position:
relative` from the three text elements too, assuming `.frame-box`'s own
`position: relative` (on the container) was enough — it isn't. A positioned
box always paints above a non-positioned one regardless of DOM order, so
without their own `position: relative` the text rendered *behind* the
frame's absolutely-positioned paper/ink layers and disappeared entirely.
D&D Beyond's own CSS keeps `position: relative` on both label and value for
exactly this reason; restored to match.

**Second bug caught during verification, direct owner report ("Faltou um
peso nas fontes, tá meio estranho")**: `.armor__value` is a `<button>`
(this app's own `reveal` trigger for the Armor Class explainer), and
browsers' UA stylesheet resets a `<button>`'s `font-weight` to `normal`
regardless of what it would otherwise inherit — `.reveal` (the shared
button-reset class) only restores `font-family`, not `font-weight`, so the
value rendered un-bold next to the correctly-bold "Armor"/"Class" labels
(plain `div`s, unaffected by the button reset). D&D Beyond doesn't hit this
because their equivalent value is a `div`, not a button. Fixed with an
explicit `font-weight: 700` on `.armor__value` itself, not relying on
inheritance from `.armor`.

**Verified**: `tsc --noEmit` and `vite build` clean; confirmed live — "Armor
12 Class" renders centered inside the shield with consistent bold weight
across all three lines, and `.tabbed-section`'s bottom (973.2px) still
matches `.skills-panel`'s (973px), unaffected since `.armor`'s own
width/height (85x95) didn't change, only how its content is centered and
weighted inside that box.

---

## 2026-09-17 — Armor Class box resized, tabbed section realigned after combat column gap edit

**Direct owner edit + report**: the owner manually tightened
`Dnd5eCombatColumn.tsx`'s own gap (Initiative/Armor/Defenses row to the
tabbed section) from 16px to 8px, to shrink the whitespace between them.
That surfaced two side effects: the Armor Class box (`.armor`, 90x97) now
read as oversized next to the tighter gap, and — since `.tabbed-section`'s
own `height: 663px` was derived from the *old* 16px gap
(97 + 16 + 663 = 776, matching `.skills-panel`'s fixed 776px height so both
bottoms align) — the tabbed section's bottom edge no longer lined up with
Skills/Proficiencies (776 - 8 = 768, 8px short).

**Fix**: `.armor` sized down to 82x88 (`frames.css`) — `--heading-y`/
`--value-y`/`--caption-y` stay percentages of the box's own height, so the
value stays vertically centered at the same relative position without
further changes. `.tabbed-section`'s height recalculated for the new gap:
with Armor no longer the row's tallest element, Defenses/Conditions'
own aspect-ratio height (~96.23px) now sets the row height, so
`776 - 96.23 - 8 ≈ 672px` (rounded) replaces the old `663px`.

**Verified**: `tsc --noEmit` and `vite build` clean; confirmed live via
`getBoundingClientRect()` — `.tabbed-section`'s bottom (973.2px) now matches
`.skills-panel`'s bottom (973px) to within a fraction of a pixel, and the
Armor Class shield now reads proportionate to Initiative/Defenses with "12"
still centered inside it.

**Left out**: `.proficiencies-panel`'s own bottom was already ~7px off from
`.skills-panel`'s before this change (its column's gaps are estimated from
screenshots, not measured — see `Dnd5eVitalsColumns.tsx`'s doc comment) —
untouched here, not something this edit introduced or was asked to fix.

---

## 2026-09-17 — Spells tab scrollbar scoped to the spell list, not the whole tab

**Direct owner report**: the Spells tab's scrollbar ran the height of the
entire tab content (spellcasting header, search/filter row, spell list) —
`Dnd5eTabbedSection.tsx`'s single `.tabbed-section__scroll` wrapper scrolls
everything below the tab bar, for every tab. On D&D Beyond
(`TabFilter.tsx`), only its `.content` div (the per-level spell list)
scrolls — the casting header and `SpellsFilter` row (search, advanced-filter
toggle, Manage Spells) sit above it, outside any scrolling area. The owner
noticed a concrete symptom: opening the advanced-filter panel (shorter
content, no scrollbar) versus the plain spell list (taller, has a
scrollbar) changed the outer scrollbar's presence, which shifted the
container's available width and visibly resized the search field between
the two states.

**Fix**: `SpellsTab.tsx`'s root now carries a `spells-tab` class (flex
column, `height: 100%`, filling `.tabbed-section__scroll` exactly so that
outer scrollbar never triggers for this tab); both the plain spell list and
the advanced-filter panel are wrapped in a new `spells-tab__list` div
(`flex: 1 1 auto; min-height: 0; overflow-y: auto`) that is the only thing
that scrolls. The spellcasting header, search/filter/manage row, and the
level/concentration/ritual filter-chip row all stay outside it, matching
`TabFilter.tsx`'s own `.buttons` (fixed) vs `.content` (scrollable) split.

**Verified**: `tsc --noEmit` and `vite build` clean; confirmed live — the
scrollbar track now starts right below the filter-chip row and ends at the
tab box's own bottom edge, and toggling the advanced-filter panel no longer
changes the search field's width.

**Left out**: the other five tabs (Actions, Inventory, Features, Background,
Extras) keep scrolling as a whole via `.tabbed-section__scroll` — the owner
only flagged Spells, and D&D Beyond's own equivalents for those tabs weren't
compared here.

---

## 2026-09-17 — Proficiencies & Training manage icon matched to D&D Beyond

**Direct owner report**: the gear icon that opens the "Proficiencies &
Training" section editor wasn't well highlighted, and — once repositioned —
the placeholder cog shape used looked too different from D&D Beyond's own.
The owner supplied D&D Beyond's real markup/CSS
(`.ct-subsection__footer > .ddbc-manage-icon`, an `.ddbc-manage-icon__icon`
sitting `left: 100%` of a single tooltipped span wrapping the title text)
and its actual icon asset
(`Content/Skins/Waterdeep/images/icons/gear-grey.svg`).

**Fix**: `SectionPanel.tsx`'s title and its manage-icon are now one inline
clickable/focusable unit (a `<button>`) instead of two separate elements —
the title stays `.panel__title`, wrapped by a new `.panel__title--manage`
modifier when `onSettings` is supplied, with the icon rendered right after
the title text via a new `.panel__settings-icon` class (`margin-left: 3px`,
matching D&D Beyond's own value). The old absolutely-positioned
`.panel__settings` rule (bottom-right corner of the panel) is removed —
nothing renders that class alone anymore. The icon itself is now
`frames/dnd_icon_gear.svg`, the actual D&D Beyond cog path (found locally
under `design-reference/markup/www.dndbeyond.com/.../gear-grey.svg`,
recolored to `fill="currentColor"` for this app's own theming) — the
project's pre-existing `dnd_icon_settings.svg` (a different, thinner
gear-like shape) stays in use only where it already was (`SpellRow.tsx`'s
cast button), not reused here.

**Verified**: `tsc --noEmit` and `vite build` both clean; confirmed live —
Aria Emberfall's "Proficiencies and Training" panel shows the cog inline
after the title, and clicking it still opens the sidebar editor correctly.

**Left out**: the same manage-icon pattern likely applies to any other
`SectionPanel` with `onSettings` — this pass only touched Proficiencies &
Training, the one the owner flagged; a broader sweep wasn't requested.

---

## 2026-09-17 — Spell save-DC ability corrected; real 5etools data for all seeded/catalogue spells

**Direct owner report**: a spell's Hit/DC column showed the *caster's*
spellcasting ability (e.g. a Fighter/Eldritch-Knight's Intelligence) as the
save's ability label, when it should show the ability the spell's *target*
rolls against — a fixed rule per spell (Acid Splash is always a Dexterity
save; Vicious Mockery is always Wisdom), unrelated to the caster's own
spellcasting ability, which only sets the DC's numeric value. This was wrong
in both `SpellRow.tsx` (Spells tab) and `SpellAttackRow.tsx` (Actions tab).

**Model change**: `Dnd5eSpell`/`Spell`/`SpellResponse`/`SpellCatalogueData`
(backend) and the frontend `Spell` type gained four fields: `saveAbility`
(nullable — null for an attack-roll or no-roll spell), `components` (e.g.
"V, S, M"), `materialComponent` (nullable), `duration` (e.g. "Concentration,
up to 1 minute"). All call sites constructing these records (calculator,
mutator's learn/prepare paths, `CharacterSheetService.toSpell`, test
fixtures) updated together. `SpellRow.tsx`/`SpellAttackRow.tsx` now read
`spell.saveAbility` for the label (falling back to the spellcasting ability
only if a spell somehow has none, which real data never does).

**Data source**: the owner pointed at [5etools](https://github.com/5etools-mirror-3/5etools-src)'s
own spell data (`data/spells/spells-phb.json`, 2014 PHB only — 2024's
revised rules ship in a separate `spells-xphb.json` the owner deliberately
wants excluded for this system for now, filed as a future "5.5e as its own
system" roadmap idea rather than mixed into `dnd-5e`). Downloaded that file
and cross-referenced it directly (`source: "PHB"` only) for every one of
Aria's 18 seeded spells plus all 11 `content/dnd-5e/spells/*.json` catalogue
entries that already existed — filling in real `saveAbility`/`components`/
`materialComponent`/`duration` for each rather than guessing. Aria's
*already-persisted* character row (seeded once, on player creation — editing
`DevCharacterSeeder.java` doesn't touch existing rows) was patched directly
via a SQL `UPDATE` with the same enriched spell data, so her current save
slots/HP/inspiration state didn't get wiped by a reseed.

**Left out**: a general, reusable 5etools→catalogue converter tool (stripping
5etools' `{@tag}` markup, mapping the rest of its schema) — the owner asked
for this to be "left ready" for pulling in more spells later, but wasn't
built this pass; the 29 spells enriched above were mapped by hand against
the real downloaded data instead. Revisit if/when more spells are needed.

`./gradlew :apps:api:test`, `tsc --noEmit` and `vite build` all pass.

## 2026-09-17 — Spell row: whole row opens detail; sidebar enrichment; advanced filter panel

**Direct owner requests**, same session as the save-DC fix above:

1. **Whole-row click** (`SpellRow.tsx`): clicking anywhere on a spell's row
   now opens its Entity Detail panel, not just the name — confirmed against
   D&D Beyond's own `SpellsSpell.tsx`, whose outer row div itself carries
   the click handler. The row is now the `role="button"`/`tabIndex`
   element (keyboard-accessible, same pattern as `AbilityBox.tsx`); the name
   is plain text again. The Cast button and both roll targets call
   `event.stopPropagation()` so they still fire their own action instead of
   also opening the panel.

2. **Sidebar detail enrichment** (`spellDetail.tsx`): `buildSpellDetailRequest`
   now shows Casting Time, Range, Components (with the material text as a
   hover tooltip, not spelled out inline the way the table's `notes` column
   does), Duration, and Attack (the spell attack bonus) or Save (ability +
   DC together, e.g. "DEX DC 14") — matching D&D Beyond's own
   `SpellDetail.tsx` property list, previously just Level/School.
   `EntityDetailMetadataEntry.value` widened `string` → `ReactNode` (same
   shape-widening `FilterChips`'s label got recently) so the Components
   tooltip could be a real `<span title>` instead of baking the material
   text into the display string.

3. **Advanced filter panel** (`SpellsAdvancedFilters.tsx`, new): the funnel
   button no longer opens an Entity Detail sidebar explaining the feature
   isn't supported — confirmed against D&D Beyond's own `SpellsFilter.tsx`
   that it's an inline panel replacing the level tabs and spell list, not a
   sidebar. Built the three filter dimensions this app now has real data
   for — Casting Time, Saving Throw, Damage Type — as toggleable chip
   groups (multi-select, AND'd together, options derived from the
   character's own spells). Tags/Conditions/Attack Type stay out, same
   documented data-model gap as before.

Verified live against Aria Emberfall: clicking a row's Time/Range cell opens
the same panel the name used to; Fire Bolt's sidebar shows Casting Time/
Range/Components/Duration/Attack; toggling "Dexterity" in the new Saving
Throw filter narrows the list to Acid Splash/Sacred Flame/Burning Hands-style
spells only. `tsc --noEmit` and `vite build` pass.

## 2026-09-17 — Concentration/Ritual filter chip icon colors, all three states

**Direct owner correction, iterated live**: the diamond/book icon chips'
own "C"/"R" letter (a hardcoded white `<text>` shape, baked into
`dnd_icon_marker_concentration.svg`/`_ritual.svg` for the inline marker next
to a spell's name) went invisible once selected, because the chip's own
`currentColor` — which the diamond/book shape correctly inherits, to look
like every other chip — turned pale to match the selected chip's own
foreground color, and the hardcoded-white letter blended into it.

The fix is **not** a generic color inversion (tried and rejected: a
`mix-blend-mode: difference` pass computed the "wrong" gray, not the
project's actual muted-gray token) — the owner wants an *exact* two/three-way
color swap using the same tokens every other chip already uses for that
state, not a computed one. Both marker SVGs now expose their shape and
letter fills as `--marker-shape-color`/`--marker-letter-color` (via inline
`style="fill: var(--x, <original-default>)"`, falling back to the original
`currentColor`/white pair when unset — `.spell-row__marker`'s own inline-next-
to-a-name usage is untouched by any of this). `frames.css` sets those two
custom properties per `.filter-chip` state, reusing the exact tokens that
state's own text/background already use elsewhere:

- Unselected: shape = `--text-control-muted` (the chip's own default text
  color), letter = white.
- Hover (unselected): shape = `--surface-control` (the chip's own hover text
  color), letter = `--accent-control-hover` (the chip's own hover background).
- Selected: shape = `--frame-paper` (the chip's own selected text color),
  letter = `--text-control-muted` (the chip's own default text color, now
  reused as the "background" half of the pair).

`.filter-chip:not([aria-pressed="true"]):hover` scopes the hover pair so it
never fights the selected-state rule on specificity for an already-selected,
also-hovered chip. Verified live: unselected shows a muted-gray diamond with
a white "C"; selected shows a white diamond with a muted-gray "C" clearly
readable inside it. `tsc --noEmit` and `vite build` pass.

## 2026-09-17 — Small gap between the Spells tab's Hit/DC and Effect columns

**Direct owner request**: with zero gap between fixed-width columns (D&D
Beyond's own convention, `.spell-row { gap: 0 }`), Hit/DC's value sat flush
against Effect's, reading as one column rather than two. Added
`padding-right: 8px` to `.spell-row__hit-dc` (`box-sizing: border-box`, so
the column stays 45px wide) — pulls the value away from Effect's left edge
without changing any column width or the header row's own alignment (its
`padding: 0` override is untouched, so "HIT / DC" still sits flush at the
column's left edge). Verified live: a visible gap between "+3" and "1d10"
on Fire Bolt. `tsc --noEmit` and `vite build` both pass.

## 2026-09-17 — Spell row vertical alignment and roll-button padding

**Direct owner report**, inspecting `.spell-row` in devtools: every column's
content sat at the row's own top edge instead of its vertical center,
looking especially wrong for a two-line save DC ("INT" over "11") next to
single-line columns like Time or Range — and the Hit/DC roll button read as
cramped. Checked D&D Beyond's own `.ct-spells-spell` in `main.aabc3e43.css`:
`align-items: center`, not the `flex-start` this app's `.spell-row` used —
that earlier choice was never actually sourced from the reference, just a
guess that a wrapped name "shouldn't push other columns down."

`.spell-row`'s `align-items` corrected to `center`. `.spell-row__value--tohit`/
`--effect` (yesterday's size modifiers on top of `.action-row__value`) gained
their own `padding: 0 6px`/`0 5px`, replacing the base chip's tighter
`0 3px` now that the tohit text is bigger and bolder (16px/700). Verified
live against Thunderwave's save-DC row (Aria Emberfall, 1st level): the
two-line "INT / 11" now centers against the single-line Cast/Time/Range/
Notes columns beside it, and the damage roll chip has visible breathing
room around "2d8". `tsc --noEmit` and `vite build` both pass.

## 2026-09-16 — Removed the Spells tab's duplicate header divider

**Direct owner report**, same day as the CSS polish pass below: the column
header row ("NAME TIME RANGE...") still looked wrong next to Actions —
"2 linhas dividindo ao invés de uma, igual fizemos para actions." Comparing
the two tabs' CSS directly: `.actions-tab__section-heading` (the "ACTIONS"/
"BONUS ACTIONS" heading) carries the Actions tab's *only* divider
(`border-bottom`), and `.action-row--header` (the attack table's own column
labels) carries none. The Spells tab had two — `.spells-level__heading`
("CANTRIP"/"1ST LEVEL") already had its own `border-bottom` matching that
same pattern, but `.spell-row--header` (the NAME/TIME/RANGE/... column
labels right below it) had a *second*, independent `border-bottom` — two
divider lines stacked close together where Actions has one.

Dropped `.spell-row--header`'s `border-bottom`/`margin-bottom`, leaving only
`padding-bottom: 3px` (matching `.action-row--header` exactly). Verified
live: one line under "CANTRIP", the column labels sit directly below it.
`tsc --noEmit` and `vite build` both pass.

## 2026-09-16 — Spells tab CSS polish: casting header, Manage button, search, filter button, roll targets, Time column

**Direct owner report**, same day as the search/filter-row rework above: the
tab still looked far off from D&D Beyond even after that structural fix —
the casting header's type/spacing, the Manage Spells button's look, the
search box feeling too small with no filter button beside it, and the
Time column's content reading as cramped. Read D&D Beyond's own compiled
CSS (`main.aabc3e43.css`'s `.ct-spells-level-casting__*`, `.ct-spells-spell__*`,
`.ct-spells-filter__*` rules) and `SpellsSpell.tsx`/`Spells.tsx` source
directly rather than eyeballing the earlier screenshots again.

- **Casting header**: `.spellcasting-header` now sets `font-family:
  var(--font-condensed); font-weight:700; line-height:1` (D&D Beyond's own
  `.info-group` wrapper), the label size moved 8px→10px, its stray
  `letter-spacing` guess removed, and the multiclass divider matched exactly
  (no explicit width, `padding-left:6px` instead).
- **Manage Spells**: real button chrome instead of a half-styled
  `.mutate` — `.spells-manage-button` now sets condensed font, 10px/400,
  uppercase, `6px 10px` padding, muted control color — DOM-measured against
  D&D Beyond's own callout (91×27px).
- **Search box**: `.search-field`'s font-size 10px→13px and padding
  `4px 8px`→`5px 10px`, matching `.ct-spells-filter__box` exactly — the
  smaller size read as "too small" next to every other control on the row.
- **Filter button**: new `.spells-filter-button` (30×30, bordered, matching
  every other control) next to the search box, reusing a new original
  `dnd_icon_filter.svg` (a generic funnel/sliders glyph, not traced from
  D&D Beyond's own icon). Opens an inert Entity Detail explanation instead
  of a real filter — this app has no type/rarity/tag fields on `Dnd5eSpell`
  yet (`dnd-5e-sheet-fidelity-audit.md`'s "Tracked, waiting on the owner"),
  so a working advanced-filter panel is out of scope here.
- **Time column**: new `castingTimeAbbreviation()` (`SpellRow.tsx`) renders
  D&D Beyond's own compact form ("1A", "1m") instead of the full stored text
  ("1 Action", "1 Minute") — confirmed via `SpellsSpell.tsx`'s own
  `ActivationUtils.renderCastingTimeAbbreviation`; the full text stays
  available as the cell's `title` tooltip. Display-only — `castingTime`
  itself is unchanged.

**Direct owner correction, same day**: the Hit/DC and Effect roll buttons
were first changed to plain borderless bold text (`.ct-spells-spell__tohit`/
`.ddbc-damage` really do carry no border in D&D Beyond's own CSS), but the
owner flagged this as losing the roll-button affordance entirely — this
app's own visual language marks every roll target with visible chip chrome
(`.roll`/`.action-row__value`), consistently, everywhere else on the sheet,
and a literal copy of D&D Beyond's borderless treatment broke that
consistency more than it fixed anything. Reverted to `roll action-row__value`
as the base classes, keeping only `.spell-row__value--tohit`/`--effect` as
size/weight modifiers on top (16px/700 and 14px, still matching D&D Beyond's
real type scale) — the chip chrome stays, matching how every other roll
target on this sheet already looks.

Verified live against Aria Emberfall's Cantrip and 1st-level spells (attack
rolls, save-DC rows, healing/damage rolls, ritual/at-will/cast indicators).
`tsc --noEmit` and `vite build` both pass.

## 2026-09-16 — Spells tab: search row, level filter, and casting header rework

**Direct owner report**: the Spells tab was far off from D&D Beyond's own — no
visible search bar, a misaligned grid, spell levels splitting across two rows,
and a badly-spaced casting-info bar. Investigated both the local
`design-reference/markup/` mirror of D&D Beyond's own compiled frontend
(`Spells.tsx`, `SpellsFilter.tsx`, `SpellsLevelCasting`, `TabFilter`, and their
CSS) and a live comparison against dndbeyond.com/characters/50149479 before
touching anything.

**Root cause**: `SpellsTab.tsx` packed `<FilterChips>` (up to 12: All + one per
spell level + Concentration + Ritual) and `<SearchField>` into one
inline-styled flex row. `.filter-chips` already wraps, so the level chips
split onto a second row; `SearchField` had no `flex`/`min-width` of its own
(fully inline-styled, no CSS class at all), so in that crowded row it
collapsed to near-invisible — reading as "missing" rather than just tiny. The
"Manage Spells" button also sat `position: absolute` over the casting header
instead of living anywhere in normal flow.

D&D Beyond's own source confirms three separate, stacked rows — casting info,
then search-plus-callout (`SpellsFilter`'s own row, "Manage Spells" passed in
as its `callout`), then a `TabFilter` level-tab row — plus two label
mismatches this app's original build never made: level chips use a short
abbreviation ("1ST") while the section heading above the list keeps the full
name ("1st Level"), and Concentration/Ritual are icon-only chips in the same
row as the level chips, not separate text chips (an earlier slice's "Built"
claim in `dnd-5e-sheet-fidelity-audit.md` turned out to have shipped as plain
text chips, never actually icon-only).

**Fix**:
- `SpellsTab.tsx` split into three real rows: `.spellcasting-header` (now
  bare, no absolutely-positioned overlay), `.spells-actions-row`
  (`SearchField` + a normal `.spells-manage-button`), `.spells-filter-row`
  (`FilterChips` alone, matching how `ActionsTab.tsx`'s own chip row always
  gets a line to itself).
- New `levelAbbreviation()` (`SpellCast.tsx`) for the chip label only;
  `levelLabel()` keeps the section heading's full name.
- `FilterChips`'s `Chip.label` widened from `string` to `ReactNode` (plus an
  optional `ariaLabel`) so Concentration/Ritual can render as icon-only chips
  reusing the already-built `dnd_icon_marker_concentration`/`_ritual`
  artwork — same change shape as D&D Beyond's own `TabFilter`, which takes an
  icon component as a filter's label the same way.
- `SearchField.tsx` moved off inline styles onto a real `.search-field` class
  with `flex: 1 1 auto; min-width: 96px` so it can never collapse to
  invisible in a crowded row again (also picked up, harmlessly, by
  Inventory's and Extras' own search rows).
- `.spellcasting-header`'s `gap: 24px` replaced with a measured
  `margin-left: 15px` between stat groups, matching D&D Beyond's own
  `.ct-spells-level-casting__info-group` spacing exactly.

The `.spell-row` grid's column widths (35/135/35/55/45/85/flex:1) were
already an exact match for D&D Beyond's own measured columns — re-verified
live after the rows above were fixed, and confirmed the "misaligned grid"
complaint was a downstream visual effect of the crowded row above it, not a
real bug in the row grid itself; no column CSS changed.

Verified live: casting header spacing (`getComputedStyle` confirms 0/15/15px
margins), all filter chips fit one row, clicking a level chip shows only that
level's section (heading keeps the full name), Concentration/Ritual render
as bare icon buttons with the right `aria-label`. `tsc --noEmit` and
`vite build` both pass. `dnd-5e-sheet-ui.md` and
`dnd-5e-sheet-fidelity-audit.md` updated to match.

## 2026-09-16 — Fixed a page-wide horizontal scrollbar

**Direct owner report**: the whole page appeared wider than the screen,
producing a horizontal scrollbar that shouldn't exist. Measured
`document.documentElement.scrollWidth` (1905) against `clientWidth` (1897)
on the character sheet screen (which has a vertical scrollbar, so
`clientWidth` is narrower than the viewport) and confirmed an 8px overflow.
Bisected by temporarily neutralizing `.sheet-header::before`'s full-bleed
background — its `width: 100vw; left: 50%; transform: translateX(-50%)`
breakout (documented in `frames.css` as the deliberate technique for
letting the header's dark background escape its centered wrapper) is the
classic `100vw`-ignores-the-scrollbar-gutter bug: `100vw` includes the
vertical scrollbar's width, so once a vertical scrollbar exists the
breakout is wider than the actual viewport by exactly that amount. Disabling
it dropped `scrollWidth` to exactly `clientWidth`, confirming it as the sole
source (no other offender found scanning every element's bounding rect
against the viewport).

Rather than rework the breakout technique itself (still needed, and correct
for viewports without a vertical scrollbar), added `overflow-x: hidden` to
both `html` and `body` in `global.css`. Verified live that `body` alone was
insufficient — `window.scrollTo(200, 0)` still moved `scrollX` to 8 with
only `body` set, and only stopped scrolling once `html` also had it. Root
cause is cosmetic overflow (an invisible background strip bleeding 8px past
the edge), so clipping it has no visible effect; confirmed the header's
full-bleed background still renders correctly and `window.scrollTo(200, 0)`
now leaves `scrollX` at 0 on the actual sheet screen (vertical scrollbar
present). Frontend `tsc --noEmit` and `vite build` both pass; CSS-only
change.

## 2026-09-16 — Corrected the ability score's horizontal centering for single-digit scores

**Direct owner correction** of the same-day re-centering above: two-digit
scores (e.g. "16") looked right, but single-digit scores (e.g. "8") still
sat visibly left of the oval's center. `.ability`'s `--chin-x: 49%` was
already 0.76px left of the ellipse's true center (measured at `x=40.45` of
81, i.e. `49.94%`, via the same offscreen-canvas rasterization as the
vertical fix). That sub-pixel gap is negligible against an 18px-wide
two-digit glyph but is ~8% of a single digit's own ~9px width — enough to
read as a real leftward shift once the glyph is the only thing anchoring
the eye. `--chin-x` moved from `49%` to `50%`, matching the ellipse's
center directly instead of a hand-tuned approximation. Verified live
against all six of Aria Emberfall's ability boxes (mixed single- and
double-digit scores) with precisely box-relative zoom crops (an earlier,
by-eye crop had itself been off-center enough to produce a false read).
Frontend `tsc --noEmit` and `vite build` both pass; CSS-only change.

## 2026-09-16 — Re-centered the ability score inside its oval

**Direct owner report**: after the 2026-09-15 click-target rework, the score
number (e.g. "16") sat visibly high inside its oval, with a large empty gap
below it. Measured the actual artwork to find the true target: `dnd_frame.svg`'s
own second path is a filled ellipse at `cx=40.5, cy=78.5` (in the frame's
81×95 viewBox units), i.e. centered at 50%/82.6% of the box, sized 37×25 —
noticeably taller and wider than the `--chin-size: 25%` (20.25×20.25) box
`.ability__score` was centered in. The old `--chin-y: 81.5%` was already
close to the ellipse's own center, so the box's small size (leaving a
disproportionate, visually asymmetric margin once the ink layer's laurel
decoration below the ellipse is factored in) was the real driver, not the
position. Rendering the frame's two SVG layers to an offscreen canvas
(forcing solid fills to isolate each path) confirmed the ellipse's true
bounds directly rather than guessing from the CSS variables alone.

`.ability`'s `--chin-y` moved from `81.5%` to `84%` and `--chin-size` from
`25%` to `30%`, verified live (zoomed screenshots, before/after) against all
six ability boxes with Aria Emberfall's real scores (16/14/14/10/12/8) —
every one, single- and double-digit, now sits centered in its oval. Frontend
`tsc --noEmit` and `vite build` both pass; CSS-only change, no component
changes needed.

## 2026-09-15 — Corrected the ability box's click target and the Explainer sidebar's styling

**Direct owner correction** of the same-day ability-icon work above: the
initial implementation kept `.ability__score` as the sole click target (a
`<button>`) and left `ExplainerPanel` in its pre-existing generic layout —
both wrong. Live-inspected D&D Beyond's actual DOM
(`AbilitySummary.tsx`/`AbilitySummary.scss`, extracted from the design
reference) to fix both precisely instead of guessing again:

- **`AbilityBox.tsx`**: D&D Beyond's own `.ddbc-ability-summary` is a plain
  `<div onClick={handleClick}>` wrapping the *entire* box — not the score
  alone — and `.ddbc-ability-summary__secondary` (the score) is itself a
  bare, non-interactive `<div>`. Rebuilt to match: the outer box now carries
  the click (plus `role="button"`/`tabIndex`/`onKeyDown` for keyboard access,
  since D&D Beyond's own version has none and this app doesn't drop existing
  a11y), `.ability__score` is now a plain div, and the modifier roll button
  calls `stopPropagation()` so rolling doesn't also open the sidebar —
  the exact same pattern D&D Beyond's own `handleClick` uses
  (`stopPropagation()` + `nativeEvent.stopImmediatePropagation()`).
  `.ability`'s own frame-relative positioning (`--content-top`, `--chin-x`,
  etc.) was left untouched — that's this app's own owner-supplied frame
  artwork, a deliberate departure from D&D Beyond's flat box shape,
  independent of the click-target bug.
- **`ExplainerPanel.tsx`**: D&D Beyond's real ability sidebar
  (`AbilityPane.tsx` + the shared `Header.tsx`) shows one heading line —
  icon inline-left (`2rem`, `0.313rem` right margin), then "Strength 16"
  with the modifier trailing inline in parens at a smaller size
  (`Header.tsx`'s own `.modifier`: `0.875rem`, `0.313rem` left margin,
  `vertical-align: middle`) — not a big caption-plus-huge-number stack.
  Rebuilt to match exactly, reusing this app's own established sidebar
  title convention (`18px`/700/`--font-condensed`, same as
  `EntityDetailPanel.tsx`) for internal consistency. `Dnd5eVitalsTopRow.tsx`
  now folds the score into `title` (`"Strength 16"`) so `formattedValue`
  (`"+3"`) reads correctly once wrapped in parens. Also fixed the icon
  itself having no intrinsic size (same bug class as the stepper icons
  above) via a new `.explainer-icon` CSS rule.

**Verified live**: clicking anywhere on the Strength box (not just the score)
opens "Strength 16 (+3)" with the bull icon inline; clicking Dexterity opens
"Dexterity 14 (+2)"; rolling via the modifier button still rolls without
reopening or otherwise disturbing the sidebar. `tsc && vite build` passes.

## 2026-09-15 — Exhaustion level, ability score icons, +/- icon buttons

Three direct owner requests, same session (see the correction entry above
for a same-day follow-up fixing this entry's own ability-icon mistake).

**Exhaustion (0-6).** D&D Beyond tracks exhaustion as a level, not a boolean
like the other fourteen conditions — this app had deliberately left it out
until now (`Dnd5eSheet`'s own doc comment). Added end to end:
`Dnd5eSheet`/`VitalsZone`/`CharacterSheetResponse` all gained an
`exhaustionLevel` field (`Dnd5eSheet` is a 36-field record with a `withX`
method per mutable concern — every existing `withX` method plus 23 test call
sites across `Dnd5eSheetMutatorTest`/`Dnd5eSheetCalculatorTest`/
`CharacterControllerTest`/`Dnd5eMechanicResolverTest` needed the new
component threaded through; `./gradlew :apps:api:compileTestJava` caught
every missed one). New `SheetMutator.setExhaustionLevel` (clamped [0, 6] via
`Math.clamp`), `POST /api/characters/{id}/exhaustion`
(`ExhaustionLevelRequest`). Confirmed live: an existing character's
already-stored sheet JSON (no `exhaustionLevel` key at all) still loads
fine — Jackson defaults the missing int to 0, no data migration needed.

Frontend: a new `+`/`−` stepper (0-6, same immediate-mutation convention as
`UsePoolStepper`) at the bottom of the sidebar's Conditions panel, using the
already-staged `dnd_icon_condition_exhausted.svg` (from a prior session,
unwired) for its row icon. `DefensesConditionsPanel.tsx`'s summary trigger
now appends "Exhaustion N" to the active-conditions line when nonzero.

**Ability score icons (first pass).** Confirmed this app's own
`AbilityBox.tsx` already routed the score click to `onOpenExplainer`, so
only the icon itself was missing from that sidebar. `ExplainerRequest`
gained an optional `icon?: ReactNode`. Wired using the six already-staged
`dnd_icon_ability_*.svg` files (from a prior session, unwired) in
`Dnd5eVitalsTopRow.tsx`. **This pass's own click-target and sidebar-layout
choices were wrong** — see the correction entry above, written the same
day once the owner compared it against D&D Beyond directly.

**+/- icon buttons.** Every stepper's `−`/`+` control (`UsePoolStepper`,
`ItemQuantityControl`, `CoinsPanel`, the new exhaustion stepper) used plain
text glyphs; the owner asked for D&D Beyond's own icon-based buttons
instead. D&D Beyond's own asset for this (`plus_minus-white.svg`, one
sprite for both, background-position-switched) is proprietary CDN artwork,
not something already in this project — created two new original icons
instead (`dnd_icon_minus.svg`, `dnd_icon_plus.svg`), matching the same
plain-bar/cross geometry D&D Beyond's own `MinusIconSvg`/`PlusIconSvg`
components use (both trivially generic shapes, not distinctive game-content
art — same reasoning already applied to `dnd_icon_close.svg`). `UsePoolStepper`
gained optional `useIcon`/`restoreIcon` props (defaulting to the old text
glyphs) so the system-agnostic `sheet/` primitive doesn't import a
dnd5e-specific asset itself. New shared `.stepper-icon` CSS class
(9×9px, `svg { width/height: 100% }`) sizes the icon consistently inside
every one of these buttons — a bare `FrameIcon` has no intrinsic size on its
own, which was the first attempt's bug (icons invisible until this class
was added).

Verified live; frontend `tsc && vite build` and `./gradlew :apps:api:test`
both pass.

## 2026-09-15 — Square stepper buttons, feature limited-use border box, condition icons

Three direct owner requests, same session.

**Square buttons.** `UsePoolStepper`'s `−`/`+` buttons were circular
(`border-radius: 50%`); the owner asked for square, matching D&D Beyond's
own control shape. Changed to `border-radius: 3px`, 20×20px, using
`var(--border-control)` — the same bordered-square skin already established
for `CoinsPanel`/`ItemQuantityControl`'s own buttons.

**Feature limited-use border box.** Live DOM inspection of
dndbeyond.com/characters/50149479's "Lay on Hands" (Features & Traits) and
"Second Wind" (Actions) found the same left-border-accent box already built
for the Actions-in-Combat plain list (`.actions-tab__standard-list`) also
wraps a feature's own limited-use tracker (D&D Beyond's
`styles_extra__BgeMp`: `border-left: 3px solid #D8D8D8`, `margin: 5px 0 0
4px`, `padding: 5px 0 5px 8px`) — a distinct shade (`#D8D8D8`, this app's
own already-established literal control-border color, see `BoxTrack`) from
the standard-list's `#EAEAEA`. New `.feature-snippet__extra` class wraps
`BoxTrack`/`UsePoolStepper` in both `FeatureActionRow.tsx` and
`FeatureTraitRow.tsx`.

Building this surfaced a real layout gap: `FeatureTraitRow.tsx` used
`.action-row` (a single-line flex row shared with the Attack/Spell tables),
so the new bordered box rendered squeezed to the row's right edge instead of
stacked below the description — D&D Beyond's own feature snippet (both
Actions and Features & Traits) stacks name → description → extra box
vertically. Switched `FeatureTraitRow.tsx` from `.action-row` to
`.action-feature` (the block layout `FeatureActionRow.tsx` already used),
matching D&D Beyond's real structure and putting the two feature lists on
the same visual footing for the first time.

**Condition icons.** The sidebar's Conditions panel (14-row toggle list)
showed name + switch only, no icon — D&D Beyond's own
`ConditionManagePaneStandardCondition.tsx` puts a per-condition icon before
the name. D&D Beyond's own 14 icon SVGs are proprietary artwork (not
reproduced, per `ui-design-system.md`); this app already had 14 original
`dnd_icon_condition_*.svg` files staged (from a prior session, unwired).
Wired them in `CharacterSheetScreen.tsx` (only place already crossing the
sheet/dnd5e module boundary) via a new `icon?: ReactNode` field on
`ConditionEntry`, rendered in `ConditionsPanel.tsx`'s row.

**Verified live**: Battle Fervor and Second Wind/Action Surge (Actions tab)
and Battle Fervor (Features & Traits tab) all show square buttons and the
bordered box stacked below the description; the Conditions sidebar shows a
distinct icon per row (confirmed Blinded, Charmed, Deafened, Frightened,
Grappled, Paralyzed, Petrified, Poisoned, Prone, Restrained, Stunned,
Unconscious), and toggling Blinded on/off still works. `tsc && vite build`
passes.

## 2026-09-15 — Corrected the large-pool stepper threshold: 11 → 8

**Continuation of the same sweep**, one folder deeper — `FeatureSnippetLimitedUse.tsx`
(`CharacterSheet/components/FeatureSnippet/`) turned out to be the real
counterpart to this app's `FeatureActionRow.tsx`/`FeatureTraitRow.tsx` (an
inline sheet row), not `ActionDetail.tsx` (the sidebar detail panel) that
the earlier stepper slice had measured the threshold against. The two
D&D Beyond components disagree with each other: `ActionDetail.tsx`'s
`largePoolMinAmount` is 11, but `FeatureSnippetLimitedUse.tsx`'s own
`largeLimitedUseAmount` — the one actually governing the main-list row,
which is what this app's rows correspond to — defaults to 8.
`LARGE_POOL_THRESHOLD` in `FeatureActionRow.tsx` corrected to 8.

Also confirmed, not changed: D&D Beyond's own large-pool row shows a plain
"{max} / {reset label}" line plus a *separate* labeled stepper ("Current:
[−] [+]") below it, where this app's `UsePoolStepper` combines both into one
line ("{remaining} of {max} / {reset label}"). Kept as a deliberate
simplification — no information is lost, and splitting it into two rows
would add layout complexity for no functional gain.

## 2026-09-15 — Component sweep: `CharacterSheet/components` (page-level rows)

Moved the sweep one level deeper than `Shared/components/` into
`CharacterSheet/components/` — the sheet-page-specific components (rows,
boxes, summaries), as opposed to the cross-cutting shared ones already
checked. Three files read: `InventoryTableHeader.tsx`, `Skills.tsx`,
`SavingThrowsBox.tsx`, `DefensesSummary.tsx`.

- **Inventory header text.** `InventoryTableHeader.tsx` confirmed the real
  column label is "Cost (gp)", not "Cost" — `InventoryTab.tsx`'s header row
  updated to match.
- **Defenses: one row per defense type, not per damage type.**
  `DefensesSummary.tsx` groups all of a character's resistances (or
  immunities) under one icon with a comma-joined list — `DefensesConditionsPanel.tsx`
  was instead rendering a separate icon+row per damage type. Consolidated to
  one row per category. Aria's own seed data only has a single resistance
  (Poison), so this was invisible until now — verified live it still renders
  correctly with one item; the multi-item case is untested against a real
  character but the join logic itself is trivial.
- **Skills header** (`Prof`/`Mod`/`Skill`/`Bonus`) and **Saving Throws'**
  below-grid "Saving Throw Modifiers" summary line both confirmed to already
  match this app's own build, or match an already-documented deferred item
  (situational advantage/disadvantage on saves) — no changes.

Frontend `tsc && vite build` passes; no backend changes.

## 2026-09-15 — Inventory tab: added the missing Notes column

**Continuation of the same component sweep**, moved outside `Shared/
components/` into `CharacterSheet/components/` — the sheet-page-level row
components, one level more specific than the cross-cutting shared ones
already swept. `InventoryItem.tsx` (D&D Beyond's own real inventory row,
distinct from the `Shared/components/ItemDetail` sidebar already checked)
confirmed its column order is Equip/Name/Weight/Quantity/Cost/Notes
(`ct-inventory-item__notes`, rendering the item's own notes text inline).

This surfaced a mistake in this app's own earlier fidelity audit: the
2026-08-16 header-row pass (`dnd-5e-sheet-fidelity-audit.md`'s "Missing
column headers" entry) lumped Notes in with Weight under the already-accepted
"no weight/encumbrance" deviation and skipped both — but Notes has nothing
to do with weight tracking, and this app already stores `item.notes` (the
add-item form's own field, previously only surfaced in the Entity Detail
sidebar's description) — it just was never rendered inline in the list.

Added a `.item-row__notes` column to `ItemRow.tsx` and the header row in
`InventoryTab.tsx`, positioned last (after Cost), matching D&D Beyond's own
order. `.item-row__name`'s flex-basis narrowed from `auto` to `120px` so the
new column has room without starving the name column. **Verified live**:
Longsword shows "Versatile (1d10).", Chain Mail "Spare armor, carried but
not worn.", Potion of Healing "Restores 2d4+2 hit points.", both Cloak and
Ring of Protection "+1 to armor class and saving throws." — all aligned
under the new "NOTES" header. Frontend `tsc && vite build` passes (no
backend change — `item.notes` already existed on the record).

## 2026-09-15 — Editable item quantity control (`SET_ITEM_QUANTITY`)

**Continuation of the same component sweep** — cross-checking D&D Beyond's
`SimpleQuantity` against this app's own Inventory found a real, previously
undocumented gap: there was no way to change a stacked item's quantity (e.g.
using 1 of 2 Potions of Healing) without deleting the item outright and
re-adding it, losing its cost/notes/attunement flags in the process.
`ItemDetailActions.tsx` confirmed exactly where D&D Beyond puts this: its own
`SimpleQuantity` lives in the item's sidebar action bar, alongside Equip/
Attune/Move/Delete — never inline in the main list row, which only ever
shows quantity as plain text. This app's `ItemRow.tsx` already had that same
inert list-row text plus an `actionBar` with Equip/Unequip; the quantity
control now joins it there, matching D&D Beyond's own placement exactly.

**Backend**: new `SheetMutator.setItemQuantity(sheetJson, itemKey, quantity)`
— same shape and unknown-key no-op treatment as `toggleItemEquipped` —
implemented in `Dnd5eSheetMutator`, clamping to a minimum of 1 (dropping to
zero isn't how an item is removed; that stays `removeItem`'s own job, a
separate explicit action). New `POST /api/characters/{id}/items/{itemKey}
/quantity` endpoint (`ItemQuantityRequest(@Min(1) int quantity)`, same
validation pattern as `CoinAdjustmentRequest`), wired through
`CharacterSheetService`.

**Frontend**: new `SET_ITEM_QUANTITY` mutation type and `postSetItemQuantity`
(`sheet/api.ts`, mirroring `postToggleItemAttuned`'s call shape). `ItemRow.tsx`
gained `ItemQuantityControl` — DOM-measured against D&D Beyond's own
`.ct-simple-quantity` SCSS source (bold label with a 5px right margin, a
60px centered input with 3px side margins, controls in their own flex row)
— rendered in the item's Entity Detail action bar via a new `itemActionBar`
helper, wired with `refreshActionBar` (same live-refresh pattern
`ExtraRow.tsx`'s hit points and `InventoryTab.tsx`'s coins panel already use)
so the sidebar and the list row stay in sync after every click. The two
flanking buttons dispatch an immediate new quantity rather than replicating
D&D Beyond's own `SimpleQuantity` internal increment/decrement math — this
app's own established immediate-mutation convention (`BoxTrack`,
`UsePoolStepper`), not a staged edit. The buttons themselves use this app's
own bordered-neutral small-button skin (`CoinsPanel`/`UsePoolStepper`)
rather than D&D Beyond's filled-red `button-action-increase/decrease`,
which appears nowhere else in this sheet. The redundant "Quantity" metadata
line (now duplicated by the editable control) was dropped from the Entity
Detail panel's `metadata` array.

**Verified live**: Potion of Healing (seeded ×2) — clicking `+` moved both
the sidebar (`3`) and the list row (`×3`) together; clicking `−` restored
both to `2`; typing `5` directly into the input and pressing Tab (blur)
committed and synced both to `5`; typing `2` and pressing Enter committed
and synced both back to `2`. `./gradlew :apps:api:test` and the frontend
`tsc && vite build` both pass.

## 2026-09-15 — Large-pool numeric stepper for limited-use features (`UsePoolStepper`)

**Continuation of the same component sweep** — cross-checking D&D Beyond's
`SlotManager`/`SlotManagerLarge` against this app's own `BoxTrack` closed out
a gap the phase 10 audit had deferred (`dnd-5e-sheet-fidelity-audit.md`,
"Features and traits tab"): a limited-use resource with a **large pool**
(D&D Beyond's own threshold is 11+ total uses, e.g. a Paladin's Lay on
Hands) renders as a numeric `−`/`+` stepper on D&D Beyond, not a row of
checkbox dots — `BoxTrack`'s only shape, correct for a small pool (Second
Wind) but unusable at 15+ boxes. The audit originally assumed this needed a
new domain-model field and deferred it; re-checking before actually building
it found that's wrong — `Dnd5eFeatureAction`/`Dnd5eFeatureTrait` already
carry `maxUses`/`usedCount`/`rechargeTrigger`, exactly what a large pool
needs. **"Large" is a pure presentation threshold on the existing field, not
a new one** — this ended up a frontend-only slice.

Proposed as a plan and approved before building (`ground-rules.md`'s "larger
than a vertical slice → propose first"), since it still touched the
Features/Actions tabs' shared rendering logic even without a backend change.
Added `apps/web/src/sheet/UsePoolStepper.tsx`, a sibling primitive to
`BoxTrack.tsx` with the same props shape: a `−` button (spend a use), a
`{remaining} of {maxUses}` readout, a `+` button (restore a use), and the
same "/" + recharge-label suffix `BoxTrack` already uses. Deliberately
follows `BoxTrack`'s own immediate-mutation-per-click convention rather than
D&D Beyond's own `SlotManagerLarge`, which stages a pending edit locally
with Confirm/Clear before firing one mutation — introducing a "pending
edit" pattern nowhere else in this sheet uses wasn't worth matching D&D
Beyond exactly here. `FeatureActionRow.tsx` gained a `LARGE_POOL_THRESHOLD =
11` constant (imported by `FeatureTraitRow.tsx`, same existing cross-import
as `rechargeTriggerLabel`); both rows now pick `UsePoolStepper` over
`BoxTrack` once `feature.maxUses >= 11`, wired to the exact same
`USE_FEATURE_ACTION`/`RESTORE_FEATURE_ACTION`/`USE_FEATURE_TRAIT`/
`RESTORE_FEATURE_TRAIT` mutations, untouched.

Seeded one synthetic example, "Battle Fervor" (`DevCharacterSeeder.java`,
`maxUses: 15`, 6 already spent, `LONG_REST`) — explicitly documented in its
own doc comment as invented, not a real 5e Fighter feature, purely so the
new component has a live row to render and verify against, since none of
Aria's real features cross the 11-use threshold at level 5. Since
`DevCharacterSeeder` only fires on a brand-new player (`PlayerCreatedEvent`),
the existing `test` user's already-seeded Aria character needed this one
feature appended directly to its stored `sheet` JSONB via a one-off SQL
update, rather than a full reseed.

**Verified live**: Features tab, "Battle Fervor" renders `− 9 of 15 + / Long
rest`; clicking `−` moved it to `8 of 15`, `+` restored it to `9 of 15`;
Second Wind and Action Surge (small pools) still render `BoxTrack` unchanged.
`./gradlew :apps:api:test` and the frontend `tsc && vite build` both pass.

## 2026-09-15 — Component sweep: finished the span→div column-wrapper fix in Skills, Inventory, Extras and Senses

**Continuation of the same systemic fix** from earlier this date (Actions/Spells
tab column wrappers) — the owner asked to keep sweeping the rest of the
codebase for the same bug class: a `<span>` used as a real flex-item column
(carrying its own `flex`/width/padding/text-align) instead of a `<div>`.
Cross-checked every remaining D&D Beyond `Shared/components/*` file this
app has a rough equivalent of (`ActionDetail`, `ItemDetail`, `ExtraRow`) —
`ActionDetail.tsx`/`ItemDetail.tsx` only confirmed this app's already-
documented simplified Entity Detail scope (no Customize editor, no Weight/
Cost/Properties/Source fields — a deliberate, previously-decided omission,
not a new gap). `ExtraRow.tsx`, however, showed this app's own `ExtraRow`/
`ExtrasTab` had been missed by the original span→div sweep entirely.

Grepped every remaining `<span className="...">` in `apps/web/src/systems/
dnd5e/` and classified each against its own CSS: a plain inline text run
(no `flex-basis`/width of its own) stays a `<span>` — e.g. `.attunement-row__name`,
`.coins-panel__label`, `.saving__abbr`/`.saving__prof` (both `position:
absolute`, already exempt from the blockification issue entirely). Anything
acting as a genuine flex-item column — its own `flex`, width, or padding
driving layout, not just riding along — became a `<div>`, matching the
convention `AttackRow.tsx`/`SpellRow.tsx`/`SpellAttackRow.tsx` already
established:

- `ExtraRow.tsx` / `ExtrasTab.tsx`: `.extra-row__name-cell` and every
  `.extra-row__stat` (AC/HP/Speed), in both the header row and the data row.
- `SkillsPanel.tsx` / `SkillRow.tsx`: `.skill-row__prof-cell` and
  `.skill-row__ability` (`.skill-row__name`/`.skill-row__mod` were already
  fixed in an earlier pass, 2026-08-17).
- `InventoryTab.tsx` / `ItemRow.tsx`: `.item-row__quantity` and
  `.item-row__cost`, plus the header row's matching cells.
- `SenseRow.tsx`: `.sense-row__value`/`.sense-row__label` (the passive-score
  badges) — `.sense-row` is a real flex container, not the `position:
  absolute` layout `SavingThrowRow.tsx`'s frame badges use, so this one was a
  genuine instance of the bug, not a false positive.

No CSS changes needed — every affected rule already keys off the class name,
never a `span`-qualified selector. **Verified live**: Skills, Inventory, and
Extras tabs, plus the Senses panel's three passive-score badges, all render
identically to before the change — confirms this was purely a markup
correctness fix, not a visual one.

## 2026-09-15 — Ported D&D Beyond's left-border-accent list style onto `.actions-tab__standard-list`

**Direct owner request**, with the exact HTML/CSS D&D Beyond renders for its
own "Actions in Combat" plain lists (e.g. Two-Weapon Fighting under Bonus
Action): `.styles_list__8r0ai` — `border-left: 3px solid
var(--character-light-mode-border-default)`, `padding: 5px 8px 5px 8px`,
`margin: 5px 0 0 4px`, `font-size: 12px`. Confirmed via the reference CSS
(`--character-light-mode-border-default` resolves to `#eaeaea`, identical to
this app's own `--border-divider` token, so no new token was needed).

`ActionsInCombatList.tsx`'s `.actions-tab__standard-list` (rendered by
`frames.css`) previously had no border, padding, or margin at all and used
13px text. Updated to match: `border-left: 3px solid var(--border-divider)`,
`margin: 5px 0 0 4px`, `padding: 5px 0 5px 8px`, `font-size: 12px`; also
zeroed out the nested `.reveal` link's own padding/font-size so it doesn't
double up inside the new box.

**Verified live**: Aria Emberfall's Bonus Action tab, "Two-Weapon Fighting"
under "Actions in Combat" now renders inside a left-border accent box,
visually matching the D&D Beyond reference screenshot.

## 2026-09-15 — Collapsed FrameIcon's own redundant wrapper span everywhere it appeared

**Direct owner report**, DevTools screenshot of `.action-row__damage-type`:
its only child was an unnamed, unstyled `<span>` (16×19.5, no class) wrapping
the actual `<svg>` — `FrameIcon.tsx` always renders its own `<span>` around
the SVG markup (required for `dangerouslySetInnerHTML`, which needs some
element to attach to), so every call site that already wrapped `<FrameIcon
svg={...} />` in its own purpose-built `<span>`/`<div>` for sizing, a
`title`, or an `aria-*` attribute ended up with two nested elements doing one
job.

Root-caused rather than patched at the one reported spot: `FrameIcon` now
accepts `title`/`aria-hidden`/`aria-label` and forwards them onto its own
span, so a caller can pass its real class and attributes straight to
`FrameIcon` instead of wrapping it. Collapsed every call site with this exact
shape (a non-interactive wrapper whose only content was one `FrameIcon`) down
to a single element: `AttackRow.tsx` (`.action-row__icon`,
`.action-row__damage-type`), `SpellAttackRow.tsx` (`.action-row__icon`,
`.action-row__damage-type` ×2), `SpellRow.tsx` (`.spell-row__marker` ×2,
`.action-row__damage-type` ×2), `ItemRow.tsx`
(`.item-row__attunement-warning`), `RangeDisplay.tsx`
(`.spell-area-tag__icon`). Every affected CSS rule already keyed off the
class name and a `descendant svg` selector (e.g. `.action-row__icon svg`),
which still matches now that the svg is a direct child instead of a
grandchild — no CSS changes needed.

**Left alone, different shape:** `SectionPanel.tsx`'s settings icon and
`CustomRollPicker.tsx`'s close icon sit inside a `<button>` that's the real
interactive element (its own `aria-label` already covers accessibility) —
not the "span/div wrapper existing solely to hold one FrameIcon" pattern this
pass fixed, so left as-is rather than restructuring button internals nobody
flagged.

Verified live: `document.querySelectorAll('.action-row__damage-type')` (and
`.spell-row__marker`, `.action-row__icon`) across both the Actions and
Spells tabs — every one now reports `svg` as its direct `firstElementChild`
with `childCount: 1`, no nested span. Visual rendering unchanged (the
descendant selectors made the fix invisible on screen, same reason the
double-wrap itself went unnoticed).

## 2026-09-15 — "Exceeded" indicator on Manage Spells' known/prepared counts

**Direct owner request**, the one deferred idea from the sweep above:
`SpellManagementPanel.tsx`'s "Cantrips: X/Y"/"Known: X/Y"/"Prepared: X/Y"
lines now switch to `--status-negative` (bold) when the count exceeds its
max, matching D&D Beyond's own `--exceeded` class on the equivalent lines in
`ClassSpellManager.tsx`/`ClassSpellListManager.tsx`. New `statLineStyle()`
helper, applied per-line (cantrips/known/prepared are independent — one can
exceed without the others).

Confirmed this state can't actually be produced through this app's own
"Learn" flow, and that's correct, not a gap in the check: attempted it live
(learning an 18th spell for Aria, already at both her 11/11 cantrip and 7/7
known caps) and got a 400 — `Dnd5eSheetMutator.learnSpell` already throws
`SpellLimitExceededException` before a spell is added once its class is at
cap. D&D Beyond guards the same way (`SpellManagerItem.tsx`'s own `canAdd`/
`isAddDisabled`). The exceeded state only reachable from *outside* that
guard — a level-down, a class change, or a hand-edited row that leaves a
character over a cap it once fit under — same edge case D&D Beyond's own
styling defends against, confirmed by inspecting `Dnd5eSheetMutator.java`
directly rather than assuming from the failed live attempt.

**Unrelated infra hiccup hit while testing, fixed along the way:** the first
few learn attempts failed with "Change failed to reach the server" — traced
to the API log, not this change: `NimbusJwtDecoder` timing out fetching
Keycloak's JWKS (`.../certs`, `SocketTimeoutException`) after the Keycloak
container had been up ~6 hours in this session. Restarting `osv-keycloak`
alone wasn't enough (the API's own JWK cache stayed stuck); restarting the
API process too cleared it. Noted here since it cost real debugging time
before the actual 400 (a different, unrelated, correct response) surfaced in
the network tab.

## 2026-09-15 — Concentration/Ritual as filter tabs; finished the Spell component sweep

**Direct owner approval**, continuing the same day's sweep: two follow-ups.

**1. Concentration/Ritual moved into the level filter row.** `Spells.tsx`
(D&D Beyond's own Spells tab container) builds one `TabFilter` with a tab per
spell level plus, appended only when at least one spell qualifies, a
Concentration tab and a Ritual tab — a single mutually-exclusive selection,
not independent toggles layered on top of a level filter. `SpellsTab.tsx`'s
own separate `concentrationOnly`/`ritualOnly` boolean state plus two
`.spell-toggle` "C"/"R" square buttons is replaced with that shape: one
`activeFilter` string state ('all' | a level | 'concentration' | 'ritual'),
Concentration/Ritual appended to the same `chips` array `FilterChips` already
renders the level tabs from (shown only when `sheet.spells` has at least one
matching spell), single-select like every other chip. Selecting Concentration
now shows matching spells across *every* level (grouped by level same as
before), matching the reference's own `renderSpellLevels(levelSpells,
[ALL_LEVELS], false, true)` cross-level behavior, rather than layering onto
whichever single level tab happened to be active. `.spell-toggle`/
`.spell-toggle--active` (now unused) removed from `frames.css`. Text labels
("Concentration"/"Ritual"), not D&D Beyond's icon-only tabs — `FilterChips`
is a shared primitive with plain-text chips everywhere else it's used, and
extending its prop shape for two icon exceptions wasn't asked for. Verified
live: the Concentration chip shows exactly Detect Magic and Fog Cloud grouped
under "1st Level", chip marked active.

**2. Finished reading the remaining `Shared/components/*Spell*` files**
(`SpellManager.tsx`, `ClassSpellManager.tsx`, `SpellSlotChooser.tsx`,
`SimpleClassSpellList.tsx`, and the three `legacy/` equivalents
`SpellManagerGroup.tsx`/`ClassSpellListManager.tsx`/`SpellList.tsx`). All of
them are the "Manage Spells" feature's real implementation: a server-fetched
spell shop with official-source-category filters, a Marketplace upsell
callout, collapsible Prepared/Spellbook/Add-Spells groups per class with
"exceeded limit" (`!`) badges, a separate Ritual Spells sub-accordion, and
(`SpellSlotChooser.tsx`) a Spell-Slot-vs-Pact-Slot picker for multiclass pact
casters. The `legacy/` trio is an older class-component version of the same
feature, confirming nothing new — genuinely superseded, not a second
implementation to reconcile against. None of this is a sweep-sized fix: it is
the full feature `SpellManagementPanel.tsx` deliberately simplified away from
(`SpellManagementRequest`'s own doc comment already covers why), and pact
magic isn't modeled in this app's domain at all (`Dnd5eSpellcastingClass` has
no separate slot pool). Left untouched, reported instead of silently
expanded. One inexpensive idea surfaced for later, not built now: an
"exceeded" visual state (D&D Beyond's own `--exceeded` class) on
`SpellManagementPanel.tsx`'s "Known: X/Y" line when X > Y, since our own
model can already go over a cap without any current indication.

## 2026-09-15 — Full sweep of D&D Beyond's own Spell-related components

**Direct owner request:** continue the source analysis into every remaining
`Shared/components/*Spell*` file in `design-reference`, fixing whatever the
comparison turns up rather than stopping at the one file already checked.
Read `SpellCaster.tsx` (the cast-action panel inside the spell detail sidebar
— scaling/limited-use/damage-preview logic well beyond this app's documented
"no spell scaling" simplification, nothing actionable), `SpellsLevel.tsx` (the
Spells tab's per-level list + header — confirms our own per-level `.spell-row
spell-row--header` already matches its column set and order exactly:
Name/Time/Range/Hit-DC/Effect/Notes), `SpellsLevelCasting.tsx` and its mount
sites in `Spells.tsx` (confirms our architecture already matches: one
`showSlots={false}` instance at the top of the tab for the Modifier/Spell
Attack/Save DC header, one `showCastingInfo={false}` instance per level
section for the slot tracker beside the heading — exactly our
`.spellcasting-header` plus per-level `BoxTrack` split), and
`SpellManagerItem.tsx` (the "Manage Spells" picker row — confirmed our own
`SpellManagementPanel.tsx` deliberately covers the same Learn/Delete/Prepare/
Unprepare functions with a much plainer UI, already documented as an accepted
scope simplification, not a bug).

Two real, fixable findings came out of it:

1. **`SpellRow.tsx` never got the span→div fix** the Actions-tab pass gave
   `AttackRow.tsx`/`SpellAttackRow.tsx` earlier this same day — every
   non-interactive column wrapper (`.spell-row__cast`, `__name-cell`, `__time`,
   `__range`, `__hit-dc`, `__effect`, `__notes`) was still a `<span>`.
   `SpellsSpell.tsx`'s own equivalents (`.ct-spells-spell__name`, `__range`,
   `__attacking`, `__damage`, `__notes`) are all `<div>`. Fixed, plus the new
   `.spell-row__save` wrapper (added earlier today) is a `<div>` too, matching
   `.ct-spells-spell__save`. `SpellsTab.tsx`'s own per-level header row had the
   same span-not-div gap — fixed there too.
2. **"Hit/DC" was missing its spaces.** Both `.styles_col__3hY1K.styles_tohit__zsX78`
   (Actions tab) and `.ct-spells-level__spells-col--tohit` (Spells tab) read
   "Hit / DC" in the reference markup, not "Hit/DC" — corrected in
   `ActionsTab.tsx` and `SpellsTab.tsx`. Verified live it still fits on one
   line in the Actions tab's tighter 42px header cell.

**One real design difference surfaced, deliberately not touched without
asking:** `Spells.tsx` puts Concentration and Ritual as two extra icon *tabs*
in the same `TabFilter` row as the per-level tabs (only shown when at least
one spell qualifies), not as separate square toggle buttons beside the level
chips the way `.spell-toggle`'s `"C"`/`"R"` buttons work today. Left as-is —
this is a UI redesign decision, not a fidelity bug, and the current toggle-button
treatment may itself have been a deliberate earlier choice.

**Not opened:** `SpellManager.tsx`, `ClassSpellManager.tsx`,
`SimpleClassSpellList.tsx`, `SpellSlotChooser.tsx` (multiclass pact-magic slot
picking — this app doesn't model pact magic as a separate slot pool at all,
so this would be new feature work, not a sweep fix) and the `legacy/` folder
(superseded by the components above, lower priority by construction).

Verified live: Actions tab and Spells tab both re-checked end to end after
these changes — header text, alignment, DC/ability display all still correct.

## 2026-09-15 — Spells tab had the same missing-ability-on-DC bug, with the reverse stacking order

**Direct owner request**, same session as the Actions-tab fix above: asked to
analyze D&D Beyond's own source for how its `Spell` entity works, pointing at
`SpellDetail.tsx` and the rest of `Shared/components/*Spell*` in
`design-reference`. Read `SpellDetail.tsx` (the sidebar panel — confirms D&D
Beyond models a single combined "Attack/Save" concept via
`SpellUtils.getAttackSaveValue`/`getRequiresSavingThrow`/
`getSaveDcAbilityShortName`, i.e. exactly the shape the previous entry just
built) and `SpellsSpell.tsx` (the Spells tab's own row — our `SpellRow.tsx`'s
counterpart).

`SpellsSpell.tsx`'s `renderAttackInfo()` has the identical bare-DC bug
`SpellAttackRow.tsx` had before this session: `.ct-spells-spell__save` renders
a `save-label` (ability) and `save-value` (DC) span pair — `SpellRow.tsx` only
ever showed `spellcasting.spellSaveDc.value`. Fixed the same way, now that
`spellcasting.spellcastingAbility` exists.

**One real difference caught by checking the reference CSS instead of assuming
the Actions-tab fix would port over unchanged:** the two tabs stack the pair in
*opposite* order. `.ddbc-combat-attack__save` (Actions tab) has no
`display`/order override, so the DOM order (value, then label) is also the
visual order — value on top. `.ct-spells-spell__save` (Spells tab) has
`save-label` *first* in the DOM **and** `display: block`, which pushes the
inline `save-value` after it onto its own line below — label on top here
instead. New `.spell-row__save`/`__save-label`/`__save-value` in `frames.css`
render label-then-value to match, a deliberately different stack order from
the same day's `.action-row__value--dc`, not a copy-paste of it.

Also caught the same left-vs-center alignment bug from the Actions-tab pass,
present here too: `.spell-row__hit-dc` was `text-align: center` while its own
header label is left-aligned (`.spell-row--header`'s shared rule) — D&D
Beyond's `.ct-spells-spell__attacking` has no text-align at all (default
left). Changed to match.

Verified live: Acid Splash/Vicious Mockery/Poison Spray's Hit/DC cells in the
Spells tab now show "INT" over "11", left-aligned under the "HIT/DC" header
label.

## 2026-09-15 — Attack table: left-aligned value columns, and a save DC now shows its ability

**Direct owner report**, comparing a fresh screenshot against the previous pass:
"movemos um pouco pra left o nome das colunas e o conteúdo pra cada coluna não
movimentou junto" — the header labels had gained `text-align: left` in the
previous fix, but `.action-row__value-cell` (the actual Hit/DC and Damage
buttons' wrapper) was still `text-align: center`, so the roll buttons floated
centered under a left-aligned label instead of sitting flush against it.
Changed to `text-align: left`, matching D&D Beyond's own `.ddbc-combat-attack__
action`/`__damage` (no text-align set at all, and their button has no forced
width, so it naturally sits at the column's own start). Verified live:
`getBoundingClientRect().left` on the Range and Hit/DC header cells now equals
the same property on their own column's row content, for every row.

**Same report, second half:** "para hit/dc, quando for DC, temos que
especificar qual o modificador de save será" — a save-based-damage spell's
Hit/DC cell (`SpellAttackRow.tsx`, `isSaveBasedDamage`) showed a bare DC number
with no indication of which ability the target saves against, unlike D&D
Beyond's own `.ddbc-combat-attack__save` (a value span plus an ability-
abbreviation label span, e.g. "15" / "STR"). Threaded the spellcasting ability
through the whole stack as a proper vertical slice:

- `SpellcastingClassInfo`/`SpellcastingClassInfoResponse` (backend) gained a
  `spellcastingAbility` field (the same lowercase key `Dnd5eSpellcastingClass`
  already carries, e.g. `"intelligence"` — no new data, just exposed one level
  further); `Dnd5eSheetCalculator.spellcasting()` wires it through.
  `CharacterControllerTest`/`Dnd5eMechanicResolverTest`'s existing
  `SpellcastingClassInfo` constructor calls updated for the new parameter.
- Frontend `SpellcastingClassInfo` type (`sheet/api.ts`) gained the matching
  field. A new shared `abilities.ts` (`ABILITIES`, `abilityAbbreviation()`)
  replaces `SavingThrowsPanel.tsx`'s own local ability list, now that a second
  consumer needs the same key→abbreviation mapping.
- `SpellAttackRow.tsx`'s save-DC branch now renders a new
  `.action-row__value--dc` box: the DC number (14px/700) over the ability's
  abbreviation (10px/700/uppercase/muted) — DOM-measured against the
  reference's own two font sizes, both smaller than the roll button's 20px so
  the stacked pair fits the same 36px-tall box.

Already had real data to verify against without seeding anything new — Acid
Splash, Vicious Mockery, Sacred Flame and Thunderwave (`DevCharacterSeeder.java`)
are all `attackRoll: false` spells with damage dice, i.e. already
`isSaveBasedDamage`, and Aria's one spellcasting class carries
`abilityKey: "intelligence"`. Verified live: Acid Splash and Vicious Mockery's
Hit/DC cells now show "11" over "INT".

## 2026-09-15 — Attack table: div/span structure, header height, gap and Hit/DC button width

**Direct owner report**, following up on the same day's column-width pass: comparing
screenshots of our own DevTools inspection against D&D Beyond's, the owner asked
"por que estamos usando tanto `<span>` ao invés de `<div>`" — traced to
`SpellAttackRow.tsx`'s own doc comment, which quotes the owner's original request
("todos os elementos devem ser encapsulados por uma div") but whose implementation
used `<span>` anyway. Flexbox blockifies span children automatically, so the mistake
never produced a visible bug — a `<span>` and a `<div>` render identically once
forced block-level as a flex item, which is exactly why nobody caught it. Every
non-interactive column wrapper in `ActionsTab.tsx`'s header, `AttackRow.tsx` and
`SpellAttackRow.tsx` (icon, category, range, both value-cells, notes, and the
static — non-button — value spans) is a real `<div>` now, matching D&D Beyond's own
`.ddbc-combat-attack__*` markup; genuine inline text runs inside a `<button>`
(`.action-row__damage-value`/`__damage-type`) stay `<span>`, matching the reference's
own `<span class="ddbc-damage__value">` inside its own button.

**Same session, three more owner-reported comparisons against a second DevTools
screenshot:**

1. **Header cell height wasn't uniform.** `.action-row__value` (used for the
   header's "HIT/DC"/"DAMAGE" labels) was inheriting `height: 30px`,
   `display: inline-flex` and a `border` from its own base rule — the roll
   button's chrome — which the header-specific override never reset. Result:
   "ATTACK"/"RANGE"/"NOTES" sat at ~23px (auto height, text + padding) while
   "HIT/DC"/"DAMAGE" sat at 30px with an invisible bordered box behind the text.
   Fixed by adding `display: block; height: auto;` to the header override, so
   every header label now shares the same auto-height, left-aligned, borderless
   box — DOM-verified live: all four now measure 23px tall.
2. **Extra gap between columns.** `.action-row` had `gap: 10px`; D&D Beyond's own
   `.ddbc-combat-attack`/`.styles_tableHeader__Ow6Oy` are plain `display: flex`
   with no gap at all — column spacing there comes only from each column's own
   fixed width. Set to `gap: 0` to match; Range now sits directly against the
   name column instead of 10px further right.
3. **Hit/DC button was stretched to fill its column.** Found the reference's own
   rule for this specific button, missed in the first pass:
   `.ddbc-combat-attack__tohit .integrated-dice__container { font-size: 20px;
   height: 36px; width: 42px }` — the *column* is 55px
   (`.ddbc-combat-attack__action`, already correct), but the *button* inside it
   is a fixed 42px, centered, not stretched to fill the cell.
   `.action-row__value--prominent` changed from `width: 100%` to `width: 42px`.

**Not changed, on purpose:** the owner also asked whether `.action-row__name`
(140px) should shrink to close the gap further and rein in how wide `.action-row__notes`
looks. Declined — 140px is D&D Beyond's own confirmed value (`8.75rem` in its source
CSS, and live-DevTools-measured at 140×25.39 against a real character), verified
twice already; shrinking it would reintroduce a deviation this same day's earlier
pass had just fixed. The Notes column's own extra width is `flex: 1 1 auto` (same as
the reference's own `.styles_notes__mSXl3`) absorbing whatever total width remains
in our own container — almost certainly because our table's outer container is
wider than D&D Beyond's own `.styles_attackTable`, a container-level question, not a
column-width bug. Left open pending a DOM measurement of the reference's own total
table width, if the owner wants Notes capped to an exact figure rather than filling
the remainder.

Verified live throughout: `getBoundingClientRect` on every header cell (name/range/
hit-dc/damage all 23px tall; hit-dc button 42×36, centered in its 55px column) and a
screenshot showing Range sitting close against the name column with no visible gap.

## 2026-09-15 — Attack table column widths aligned to D&D Beyond's own pixel values

**Direct owner report:** the owner pasted D&D Beyond's own Actions-tab attack table
markup and CSS (`.ddbc-combat-attack__*`, `.styles_col__3hY1K` and its per-column
width classes, from the reference build's `main.aabc3e43.css`) and asked for our
`.action-row` columns (`ActionsTab.tsx`'s header, `AttackRow.tsx`, `SpellAttackRow.tsx`)
to match those widths exactly. The overall structure — a header row plus data rows,
same flex-with-fixed-column-widths shape, an unlabeled icon column before "Attack" —
was already right from earlier phase 10 work; only the numbers were off.

`frames.css` (`.action-row__icon`, `.action-row__name-cell`, `.action-row__name`,
`.action-row__range`, `.action-row__value-cell--prominent`,
`.action-row__value-cell--damage`) now use D&D Beyond's own widths: icon 27px
(20x20 glyph centered in it, sized in fixed pixels rather than 100% — same reason
as `.action-row__damage-type`, documented in its own comment), name 140px (=
8.75rem, confirmed against the reference CSS directly), range 55px, Hit/DC 55px,
Damage 80px **fixed** (previously `auto` — a deliberate earlier choice, now
reversed since the reference itself fixes this column and lets its content wrap).
Notes stays `flex: 1 1 auto`, unchanged.

Verified live: started the API and Vite dev server, logged in as `test`, opened
Aria Emberfall's Actions tab, and read the computed `width` of each column via
`getComputedStyle` — icon 27px, name-cell 140px, range 55px, Hit/DC 55px, Damage
80px, all matching exactly.

## 2026-09-15 — Damage-type icon fixed at 16px, and actually square this time

**Direct owner report:** the icon beside a Damage roll button's dice (Actions tab's
`AttackRow`/`SpellAttackRow`, Spells tab's `SpellRow`) was DOM-measured against
D&D Beyond's own `.ddbc-damage-type-icon__img` and found to be a fixed 16px in
*every* context — the combat attack table and the Spells tab list alike, not a
smaller size in the latter as a 2026-09-10 pass had assumed without checking.
`.action-row__damage-type`'s own `1em` (context-dependent: 14px in the Actions
tab, 12px in the Spells tab) is now a flat `16px`, and the now-redundant
`.action-row__value--damage .action-row__damage-type` override (which had scoped
16px to the Actions tab's Damage column only) is deleted.

**Second finding, same report:** setting `width`/`height: 16px` on the outer
`.action-row__damage-type` span alone didn't actually make the icon 16x16 —
live DOM measurement showed the rendered `<svg>` at 16x23px, not square. Cause:
`FrameIcon` wraps the `<svg>` in its own unnamed `<span>` (no `className` passed
at any of these call sites), which has no definite height as a flex item, so the
existing `.action-row__damage-type svg { height: 100% }` rule couldn't resolve
against it and fell back to the icon's own intrinsic aspect ratio instead.
Fixed by sizing the `<svg>` itself in fixed pixels (`width: 16px; height: 16px`)
rather than a percentage — this sidesteps the intermediate span's own sizing
entirely, rather than trying to style an anonymous, unclassed element. Verified
live afterward: all twelve damage types plus the healing icon render at exactly
16x16 in both the Actions and Spells tabs.

**Third finding, same report:** the owner pasted D&D Beyond's own damage button
markup (`.ddbc-damage` > `.ddbc-damage__value` + `.ddbc-damage__icon` >
`.ddbc-damage-type-icon`) — its dice text is never a bare text node in the
button, always its own wrapped span, the same "don't leave an element loose,
wrap it" rule this app's own `.action-row__value-cell` already follows one
level up (2026-09-10 entry, above). `damageLabel`/`diceLabel` in `AttackRow.tsx`,
`SpellRow.tsx` and `SpellAttackRow.tsx` were still bare text beside
`.action-row__damage-type` — now each wrapped in a new `.action-row__damage-value`
span, matching that structure. No visual or layout change: the parent button's
own flex `gap` already spaced a bare text node identically to a wrapped one.

**Fourth finding, same report:** the pasted markup's CSS also settled a point the
2026-09-10 audit had left as an assumption ("D&D Beyond's own markup applies it
via the icon's own margin rather than a flex `gap`; visually equivalent" — noted
but not acted on). `.ddbc-damage-type-icon__img`'s own rule is `margin-left: 3px;
margin-right: 0`, confirmed by the selector list covering all four contexts
(`ddbc-combat-attack__damage`, `ddbc-combat-item-attack__damage`,
`ddbc-spell-damage-effect__damages`, `ddbc-spell-damage-effect__healing`) — not a
flex `gap` on a shared container. `gap` removed from `.action-row__value` (base)
and `.action-row__value--damage`; `.action-row__damage-type` gained
`margin-left: 3px` instead, so the spacing is owned by the icon itself in every
context (Actions and Spells tabs alike), matching D&D Beyond's own mechanism, not
just its visual result. Verified live: value/icon gap measures exactly 3px via
`getBoundingClientRect`, icon still 16x16.

**New rule adopted this session, recorded in `ground-rules.md`'s new "Comments"
section:** code comments stop narrating what was asked and when — that goes in
this changelog instead. Existing narrative comments (including several added
earlier in this same session, before the rule existed) are left alone; a
dedicated cleanup pass will fold them into brief, purely functional comments
later, not as a side effect of unrelated work.

---

## 2026-09-14 — Advantage/disadvantage rolling (backend rollMode + right-click popover)

**Direct owner request**, resolving the one open item from phase 10's "Tracked,
waiting on the owner" list (`dnd-5e-sheet-fidelity-audit.md`) that had a concrete
spec: "se tiver marcação de vantagem na skill, rola 2x o d20 e considera o maior
resultado; desvantagem rola e pega o menor; os dois juntos cancelam para uma
rolagem normal." The advanced item/spell filtering half of that list entry is
still open — untouched by this slice.

**Backend** (`dice` package, no migration — `rolls.results` was already
`integer[]`):
- `RollRequest` gains two booleans, `advantage`/`disadvantage` (default `false`).
- New package-private `RollMode` enum (`NORMAL`/`ADVANTAGE`/`DISADVANTAGE`),
  `RollMode.from(advantage, disadvantage)` cancelling both-true back to `NORMAL`.
- `RollService.roll()`: a non-`NORMAL` mode rolls 2 dice instead of 1 and keeps the
  max (advantage) or min (disadvantage) instead of summing; both raw dice stay in
  `results[]` for the log. Eligibility is **not** "any single die" — a 1d4 damage
  roll is still one die but is never rolled twice for it — it's specifically
  `diceCount == 1 && diceSides == 20`, generic dice-mechanics logic with no
  `RollKind` branch, so it applies automatically to every d20 check/save/attack a
  future roll kind might add. A non-d20 roll requesting either flag gets a new
  `IneligibleRollModeException` → 400 via `ApiExceptionHandler`. The roll's
  `context` gets a `" (advantage)"`/`" (disadvantage)"` suffix rather than a new
  response field, matching the existing "Custom: roll"-style context convention.
- `RollServiceTest` (new, plain Mockito unit tests, no Spring/Testcontainers —
  the logic is pure arithmetic): advantage keeps the higher roll, disadvantage the
  lower, both-true cancels to normal, a multi-die roll and a non-d20 single-die
  roll (1d4) both reject with `IneligibleRollModeException`.

**Frontend** — DOM-extracted live from dndbeyond.com/characters/50149479's own
right-click "ROLL WITH" popover (a `[role=menu]`/`.roll-mode-menu`-style class
found via `document.querySelector`, not guessed): the green "A"/red "D" d20-shaped
badges (`dnd_icon_advantage.svg`/`dnd_icon_disadvantage.svg`, new), reusing the
already-extracted `dnd_icon_die_d20.svg` for "Flat (One Die)" (byte-identical path
data confirmed). New `RollModeMenu.tsx` (`apps/web/src/dice/`, alongside the
already-generic `CustomRollPicker.tsx` which sets the precedent for a system-
agnostic dice component importing dnd5e-specific icons): right-click opens it at
the cursor, picking a row only changes the highlighted selection (matching D&D
Beyond's own flow — the roll happens on the separate "ROLL 1d20+4" confirm button,
not on picking Advantage/Disadvantage itself), Escape or an outside click closes
it without rolling. `RollContextMenuHandler` threaded alongside the existing
`RollHandler`/`onRoll` seam through all 13 files that seam already passed through
(`Dnd5eVitalsTopRow`, `Dnd5eVitalsColumns`, `Dnd5eLeftColumn`, `SkillsPanel`,
`SavingThrowsPanel`, `Dnd5eCombatColumn`, `Dnd5eTabbedSection`, `ActionsTab`,
`SpellsTab`, `SpellRow`, `SpellAttackRow`, plus the four leaf buttons — `AbilityBox`,
`SkillRow`, `SavingThrowRow`, `Initiative`, `AttackRow`). Wired only onto the six
d20 roll targets (ability check, skill check, saving throw, initiative, attack hit,
spell attack) — damage/heal/hit-dice buttons keep their plain click-to-roll, no
right-click added, matching the backend's own d20-only eligibility. The popover's
"ROLL 1d20+X" label reuses the modifier each row already displays client-side
(never computed by the client — see ground-rules.md's Dice section).

**Deliberately narrower than D&D Beyond's own menu**: its "SEND TO: Everyone/Self"
section above "ROLL WITH" is left out — this app has no chat/whisper concept to
send a roll to. Its own checkmark icon on the selected row is a plain highlight
here instead, to avoid extracting a fourth icon for a cosmetic difference.

Verified live in the running app (`test`/`test-osv-123`, Aria Emberfall): right-
clicking Athletics (+6) → Advantage → "ROLL 1d20+6" produced "Athletics: check
(advantage)" at 18, dice tray showing `2d20+6 (12, 6)`; the same on Longsword's
Attack Hit (+6) with Disadvantage produced 13, `2d20+6 (12, 7)`; clicking outside
the open popover closed it with no roll recorded.

---

## 2026-09-10 — Hit/DC and Damage wrapped in their own flex cell (structural fix)

**Direct owner report:** in `.action-row` (Actions tab), every other column already
wraps its content in a sized cell separate from the content itself
(`.action-row__icon`, `__name-cell`, `__range`, `__notes`) — the Hit/DC and Damage
`<button>`s were the one exception, each being `.action-row`'s own flex item
*and* the bordered/padded interactive element in one DOM node. It's also the one
place this row shape didn't match the Spells tab's own equivalent columns —
`.spell-row__hit-dc`/`__effect` (`SpellRow.tsx`) already wrap their roll buttons in
a plain, non-flex, sized `<span>`.

- New `.action-row__value-cell` (plus `--prominent`/`--damage` width modifiers) is
  now the real flex item — `flex: 0 0 auto`, the column's width, `text-align:
  center`. `.action-row__value` (the button, or the static `<span>` fallback) lost
  its own `flex`/`width` and sits inside the cell, centered by it — same mechanism
  `.spell-row__hit-dc` already used.
- `AttackRow.tsx`, `SpellAttackRow.tsx` and `ActionsTab.tsx`'s own header row all
  updated to wrap their Hit/DC and Damage content in the new cell span.
- Purely structural — verified live the rendered result is pixel-identical to
  before (DOM-confirmed: the Damage cell auto-sizes to its button's own content,
  e.g. 67.47px for "1d8+3", the Hit/DC cell stays a fixed 42px with the button
  filling it at `width: 100%`), nothing visually changed, only the DOM shape did.

---

## 2026-09-10 — Damage button, DOM-audited against D&D Beyond (supersedes three same-day guesses)

**Direct owner request** after three same-day iterative tweaks to the Damage button
(more room, less cramped padding, left-aligned) — "analise somente a parte dos
botões de dano do D&D Beyond minuciosamente. Leia seu CSS via pastas markup." A
full `getComputedStyle` audit of D&D Beyond's own `.ddbc-combat-attack__damage`
button (Warhammer "1d8+1", Crossbow "1d8+1", Guiding Bolt "4d6", Inflict Wounds
"3d10" — dndbeyond.com/characters/50149479) found the three prior passes had
guessed wrong on every dimension that mattered:

- **Width is intrinsic, not a fixed column** — 70.47px/51.67px/58.69px across
  those four buttons, proportional to each one's own dice string; not a real table
  column in D&D Beyond's own markup. `.action-row__value--damage`'s `width: 64px`
  replaced with `width: auto` (sizes to content via the base rule's own
  `flex: 0 0 auto`) — safe since Damage is the row's last fixed-size column before
  `.action-row__notes`'s own flexible `flex: 1 1 auto`.
- **Content is centered**, not left-aligned — reverting this same day's own
  left-align change; the width fix above removes the cramped feeling that
  prompted it in the first place (short rolls no longer sit in an oversized box).
- **Padding is 3px uniform**, not `0 6px`.
- **Height is 36px** — matching `.action-row__value--prominent` (Hit/DC), which
  gained the same explicit height. Corrects a 2026-09-03 entry that guessed the
  two boxes couldn't share a height because of padding/font differences — it
  never checked for D&D Beyond's own explicit `height` rule, which pins both to
  36px directly.
- **The damage-type icon is a fixed 16px**, not `1em`/14px as the previous pass
  set it to — D&D Beyond sizes it slightly larger than its 14px value text, not
  equal to it. Scoped via `.action-row__value--damage .action-row__damage-type`
  so the Spells tab's own reuse of the bare class (still `1em`, not part of this
  audit) is unaffected.
- **Gap between text and icon set to 3px** (was 4px) — the one value without
  independent DOM confirmation of its exact mechanism (D&D Beyond applies it via
  the icon's own margin, not a flex `gap`), close enough to treat as matched.
- Font-size (14px) and text color (`--text-on-control`, already `.roll`'s own
  base color) needed no change — both already matched D&D Beyond's own
  `rgb(57,75,89)` before this pass, confirmed rather than assumed this time.

Verified live in both apps side by side: Hit/DC and Damage boxes now share one
height, Damage width tracks each row's own dice string instead of a fixed box,
content is centered, the icon reads clearly larger than the digits beside it.

---

## 2026-09-10 — Grid rows use fixed flex widths, not min-width (table-like alignment)

**Direct owner request:** every tab's grid row should behave like a real table via
`display: flex` — column name lined up with column value — without an HTML
`<table>`. All four already used `display: flex`; the bug was several columns using
`flex: 0 0 auto` + `min-width` instead of a genuinely fixed `width`, so a column
could grow past its own header's width whenever one row's content happened to be
longer than another's, silently drifting every column after it out of alignment
row to row. Found in `.action-row__name`/`__name-cell` (96px, shared by
`AttackRow.tsx`/`SpellAttackRow.tsx`'s Attack column **and** `FeatureTraitRow.tsx`'s
Features & Traits rows — same class, same bug, same fix), `.action-row__value`
(44px, Hit/DC and Damage), and `.extra-row__stat` (56px, AC/Hit Points/Speed).
`min-width` → `width` on all three; text that no longer fits wraps inside the fixed
box instead of pushing the box wider — the same precedent `.spell-row__name`'s own
2026-09-03 fix already established (that column was never affected by this bug,
already `width` from the start).

Also fixed: the Actions tab's own header row (`ActionsTab.tsx`) labeled "Hit/DC"
with plain `.action-row__value` (44px) while every real Hit/DC cell uses
`.action-row__value--prominent` (42px, 20px font) — the header didn't actually
match the column it was labeling. Added the missing modifier class.

Verified live: Actions tab (Longsword through Vicious Mockery, short and long
names alike) now lines up Range/Hit-DC/Damage/Notes at identical X positions every
row; Features & Traits tab (`FeatureTraitRow` reuses `.action-row`) — "Fighting
Style: Defense" and "Dwarven Resilience" wrap onto two lines instead of pushing
their description column right, confirmed every row's description now starts at
the same X regardless of name length. Spells and Inventory tabs were already
correct (`.spell-row__*`/`.item-row__*` already used fixed `width`) — unchanged,
re-verified no regression.

---

## 2026-09-10 — Unit normalized to "ft.", spell area detail moved from Range to Notes

**Direct owner correction**, same day as the entry below, after a meticulous live
comparison against **Copy of Raya** (dndbeyond.com/characters/56008841) specifically
requested to nail down the exact grid CSS: two things the first pass got wrong.

- **`"ft."` everywhere, not `"feet"`.** This app's own data mixed both (`"5 ft"` on
  weapons, `"120 feet"` on spells) — normalized to `"ft."` (with the period, matching
  D&D Beyond's own unit label exactly) across `DevCharacterSeeder.java`'s attacks and
  spells and every `content/dnd-5e/spells/*.json` catalogue entry. `RangeDisplay.tsx`'s
  `parseRange` now always renders the literal `"ft."` regardless of which variant it
  matched, so display stays consistent even if `"feet"` slips into data again later.
- **A spell's area detail (`"Self (15-foot cube)"`) no longer renders in the Range
  column.** DOM-confirmed live against Copy of Raya's own Actions and Spells tabs —
  Lightning Bolt's Range column shows only "Self"; its "100 ft." plus a line-shaped
  icon sit in the **Notes** column instead (`.ddbc-note-components`), same pattern
  confirmed on Ice Knife ("5 ft." + sphere icon, a secondary explosion radius) and
  Green-Flame Blade ("5 ft." + sphere icon). New `SpellAreaTag` (`RangeDisplay.tsx`)
  renders that detail — distance number plus unit, *not* bold (12px/400, unlike the
  Range column's own bold primary — DOM-confirmed deliberately quieter here) — in
  front of the existing notes text, joined by a CSS-supplied ", ". The shape word
  (`"cube"`) picks an icon from the six `dnd_icon_aoe_*.svg` files a prior session
  staged in `frames/` but never wired anywhere (`build-icon-index.ps1`'s own "staged,
  not yet applied" category) — same free-text-keyword-selects-an-icon convention
  `AttackRow.tsx`'s `attackIcon()` already uses, no new data field. Wired into
  `SpellRow.tsx` and `SpellAttackRow.tsx`; `RangeDisplay`'s own detail rendering
  removed.
- Also DOM-confirmed while comparing rows: this app's existing category-subtitle
  (10px/400/muted) and Hit/DC-button (20px/`--text-on-control`) treatments already
  matched D&D Beyond's own `.ddbc-combat-attack__meta-item`/`.ddbc-combat-attack__tohit`
  exactly — no changes needed there. D&D Beyond's own weapon-mastery/Green-Flame-Blade
  synergy notes (a structured `.ddbc-note-components` list mixing plain tags and an
  embedded damage-type icon) is a homebrew "Arcana Unleashed" playtest feature this
  app has no equivalent domain concept for (no weapon masteries) — not built, flagged
  rather than approximated.
- **Fixed mid-task:** a `Set-Content -Encoding utf8`/BOM mistake while bulk-editing
  `DevCharacterSeeder.java` and the catalogue JSON files mojibake-corrupted every
  em dash/arrow/accented character and left a BOM `javac` rejected outright — caught
  immediately by the compile-and-typecheck-before-reporting-done habit, repaired by
  reversing the bad codepage round-trip and rewriting every touched file without a
  BOM. No content was lost; flagging it here since the fix touched files beyond the
  ones this change was actually about.
- Verified live end to end (same running Docker/`bootRun`/`vite dev` stack, patched
  Aria's persisted `sheet` JSONB again to match): every weapon and spell range now
  shows "ft." with the period; Thunderwave's Range column shows only "Self" in both
  tabs, its "15 ft." + cube icon render correctly in Notes ahead of "V, S".

---

## 2026-09-10 — Range column typography (Actions and Spells tabs)

**Direct owner report:** the Range column's value rendered as one plain text run
("5 ft", "80/320 ft", "120 feet") instead of a bold number with a smaller
unit/property, unlike D&D Beyond's own treatment (cited: Reach under a weapon's
range).

- DOM-measured D&D Beyond's own `.ddbc-combat-attack__range` (Actions tab) and
  `.ct-spells-spell__range` (Spells tab) live: a bold number (kept at each row's own
  existing font size here, per the owner's own "normal size" — D&D Beyond's real
  14px wasn't ported) plus a smaller (10px) unit/long-range suffix on the same line,
  or — for a `"Self (15-foot cube)"`-shaped value — a smaller muted detail dropped to
  its own line below, the same visual slot D&D Beyond's "Reach" occupies under a
  weapon's range.
- New `RangeDisplay.tsx` (`systems/dnd5e/`) parses the existing `range` string into
  this shape — no new data field, since every `range` value this app stores (dual
  `"80/320 ft"`, simple `"120 feet"`/`"5 ft"`, a bare `"Touch"`/`"Self"`, or `"Self"`/
  `"Touch"` plus a parenthetical) already fits one of four cases. Wired into
  `AttackRow.tsx`, `SpellRow.tsx` and `SpellAttackRow.tsx` — the three consumers of
  a Range column.
- Same pass: `.spell-row__range`'s base text color was `--text-muted`, an
  unverified guess (D&D Beyond's own Spells tab range is plain black) — corrected to
  `--text-primary`, matching `.action-row__range`'s own 2026-09-03 fix. A bold muted
  number would have read wrong once the new split shipped.
- Verified live: Longsword ("5"/"ft"), Shortbow ("80"/"/320"), every spell's simple
  range ("120"/"feet"), Touch/Self (plain bold), and Thunderwave's "Self" +
  "(15-foot cube)" detail line all render correctly in both tabs, no layout
  breakage.

---

## 2026-09-10 — Healing icon, custom roll picker color, nine new spells for full damage-type coverage

**Direct owner request**, resolving the two loose ends the previous entry below left tracked
("Tracked, waiting on the owner" in `dnd-5e-sheet-fidelity-audit.md"): the owner named both
`CloseSvg`'s and `HealingSvg`'s real live locations, then asked for the healing icon to be wired
in, the custom roll picker's Roll button to stop being hard-coded red, and more spells added so
every spell damage-type icon could actually be seen.

- **Healing icon.** DOM-extracted live from dndbeyond.com/characters/50149479's Spells tab (Cure
  Wounds row, `ddbc-healing-icon__icon` — a heart, not the plain "H" a first guess might assume),
  same precedent as the attunement/settings icons (2026-09-10 entry below): a generic UI glyph,
  not traced game-content art. Saved as `dnd_icon_healing.svg`, wired into `SpellRow.tsx` and
  `SpellAttackRow.tsx` in the exact `.action-row__damage-type` slot `DAMAGE_TYPE_ICONS` already
  uses for a damaging spell's dice, now shown instead for a healing one (previously no icon at
  all). Verified live: Cure Wounds now shows a heart next to its `1d8` in both the Spells and
  Actions tabs.
- **Close icon — resolved as already correct, not changed.** DOM-compared the custom roll
  picker's `dnd_icon_close.svg` (`dice/CustomRollPicker.tsx`) against D&D Beyond's own "Roll Dice"
  popover close button live: byte-identical path data — both are FontAwesome's stock "times"
  glyph. The prior sweep's "different, unrelated FontAwesome asset" note was itself the mistake;
  corrected in the audit doc rather than changing working code.
- **Custom roll picker's Roll button.** Was hard-coded to D&D Beyond's own theme red (`#E40712`,
  DOM-measured against the live popover during the 2026-09-08 redesign) — this app has no
  per-character theme, so that red was a borrowed accent clashing with the sheet's own blue-gray
  palette everywhere else. New token `--accent-control-saturated` (`frames.css`, `#2B69AB`): the
  same ~211° hue as `--accent-control` (`#92A2B3`), pushed to a button-fill 60%/42%
  saturation/lightness instead of the gray's 18%/64%. `dice.css`'s `.custom-roll-picker__roll` now
  references it instead of the literal red. Verified live: the Roll button renders a solid medium
  blue with good white-text contrast, matching the rest of the app's own accent instead of D&D
  Beyond's.
- **Nine new spells**, `DevCharacterSeeder.java` — Acid Splash, Vicious Mockery, Shocking Grasp,
  Ray of Frost, Poison Spray, Sacred Flame, Thunderwave, Spiritual Weapon, Shillelagh — so the
  owner could actually see every damage-type icon rather than just the two (fire, necrotic) the
  roster happened to have. Covers acid/psychic/lightning/cold/poison/radiant/thunder/force/
  bludgeoning; piercing and slashing stay uncovered on purpose — no core PHB/SRD spell deals
  either, both are weapon-only damage types in the published rules (already demonstrated by this
  same character's own shortbow/longsword attacks), not a gap to fill. `cantripsKnownMax` 4 → 11,
  `spellsKnownMax` 5 → 7 to match the roster's real counts. Since seeding only fires on
  `PlayerCreatedEvent`, the existing dev character's persisted `sheet` JSONB was patched by hand to
  pick up the new roster — same precedent as Cure Wounds' own addition (entry further below).
  Verified live end to end (Docker Desktop + `bootRun` + `vite dev`, logged in as the standing
  `test` dev account): all eleven covered damage types render distinct icons correctly in both the
  Spells and Actions tabs, the 1st/2nd-level filter chips appear correctly for Thunderwave/
  Spiritual Weapon, and "Manage Spells" shows the corrected `11/11`/`7/7` counts with no layout
  breakage.
- `dnd-5e-sheet-fidelity-audit.md`'s "Tracked, waiting on the owner" section updated to mark both
  items resolved; `dnd-5e-sheet-build.md`'s Damage-type icons row and `ui-design-tokens.md`'s
  accent-token table both gained the corresponding notes.

---

## 2026-09-10 — Pending icon-sweep follow-ups documented for next session

**Direct owner request**, right after the entry below: record its leftover findings
somewhere durable instead of only in this changelog, so a future session can pick
them up without re-deriving the live investigation.

- `dnd-5e-sheet-ui.md`'s "Deferred to later versions" gained two entries: rolling
  with advantage/disadvantage (the exact D&D Beyond trigger — right-click any
  rollable value — and why it needs a `rollMode` concept in the dice engine, not
  just the `AdvantageSvg`/`DisadvantageSvg` icons) and advanced item/spell
  filtering by type/rarity/tag (`FilterSvg`'s live location, and why
  `SearchField.tsx` has nothing to filter by yet).
- `dnd-5e-sheet-fidelity-audit.md`'s open question 7 (Character panel, already
  deferred to phase 11) gained the four icons confirmed live for whenever that
  panel gets built: `PencilSvg`, `PaintBrushSvg`, `ManageLevelSvg`, `ManageXpSvg`.
- The same file's "Tracked, waiting on the owner" section (previously empty)
  now lists both of the above plus `CloseSvg`/`HealingSvg`, whose live location
  wasn't pinned down this pass — flagged for another look at different reference
  characters/interactions rather than concluded to not exist.

---

## 2026-09-10 — "Misc icons" live sweep: 2 applied (Attunement, Settings), several found but not yet wired

**Direct owner request:** locate, live on dndbeyond.com, where the `icons/` category's remaining
uncatalogued components actually appear — Advantage/Disadvantage, Attunement, Close, Healing, Filter,
Inspiration and others — by clicking through Short Rest, the Spells tab, and multiple reference
characters, then apply whichever ones map onto something real in this app.

**Found live** (right-click any rollable value to open D&D Beyond's roll context menu):
`AdvantageSvg`/`DisadvantageSvg` are the green "A"/red "D" badges under "ROLL WITH:"; `FilterSvg` is the
funnel next to the Spells/Inventory search boxes; `PencilSvg` (edit name), `PaintBrushSvg` ("Change Sheet
Appearance"), `PreferencesSvg` (gear, "Character Settings") and `ManageLevelSvg` ("Manage Character &
Levels") all live in the "Character" panel opened via the header's `MANAGE` button; `ShortRestSvg`/
`LongRestSvg` reconfirmed there too (already applied). `AttunementSvg` turned out to be a triangle with an
"A" cut out of it (not a plain warning glyph as a first low-zoom screenshot suggested — confirmed by
actually rendering the extracted path), shown next to an item's name on the live equipment list when it
requires attunement and isn't attuned yet. `AdvantageDisadvantageSvg` (the combined icon for a roll that
had both cancel out), `CloseSvg` and `HealingSvg` were not pinned to a specific live location this pass.

One tab-rendering glitch hit mid-session (a dice roll animation left the character-app painting only a
narrow strip of the page, reproducible across reloads and different characters) — resolved by closing
that tab and opening a fresh one, no lasting effect.

**Applied — real gap in this app, not decoration:**
- **`dnd_icon_attunement.svg`** (`ItemRow.tsx`) — this app had *no* indicator at all in the main
  equipment list for "requires attunement, not yet attuned" (only `AttunementSection.tsx` further down
  the tab showed it); now renders trailing the item name, same inline treatment as the existing `✦`
  attuned marker. Single-color source, plain `currentColor` swap.
- **`dnd_icon_settings.svg`** (`SectionPanel.tsx`) — replaced `.panel__settings`'s plain `"⚙"` text
  character with the real gear glyph. Source wrapped its path in a `<mask>`/`<use>` referencing a
  full-viewBox rectangle (a no-op) — dropped for a plain `<path>`, keeping the parent group's
  `fill-rule="evenodd"` explicit on the path itself so the gear's centre hole still renders (verified by
  rendering both icons standalone before wiring in — the gear's hole and the attunement "A" cutout both
  came through correctly).

**Not applied — no matching feature exists yet, would be a hollow icon:**
- Advantage/Disadvantage: this app's dice engine (`DiceRoller`/`RollRequest`/`RollService`) has no
  concept of rolling twice and taking the best/worst — adding the badges without that mechanic would be
  decoration, the same call already made for the skill disadvantage indicator. Building it for real is a
  genuine vertical slice (backend roll-mode + a UI trigger on every rollable row), not an icon swap —
  flagged for the owner to scope separately, not built this pass.
- Filter: this app's search fields (`SearchField.tsx`) are name-only; D&D Beyond's funnel opens an
  additional type/rarity/tag filter this app doesn't have.
- Pencil/PaintBrush/ManageLevel: all live inside the "Character" panel, which `dnd-5e-sheet-fidelity-
  audit.md`'s open question 7 already deferred to phase 11 (character creation/editing) — no panel exists
  yet for these to sit in.
- Close/Healing: no confirmed live location this pass (see above) — nothing to attach an icon to yet.

`build-icon-index.ps1` unchanged (both new files fall into the existing generic "Misc icons" bucket, same
as every other single-purpose icon that isn't part of a themed set); regenerated `icon-index.html`
(101 → 103). `svg-index.html`'s recommendations table gained two new `Applied` rows for these
(48 → 50 applied, 60 → 62 entries).

---

## 2026-09-10 — 35 not-yet-used D&D Beyond icons staged into frames/, closing out the icon sweep

**Context:** following up on the 2026-09-07 `frames/` swap and its "Recommendations for apps/web" table
(`design-reference/markup/svg-index.html`), the two remaining "Needs manual cleanup" entries
(`dnd_icon_attack_melee.svg`/`dnd_icon_attack_ranged.svg`) were investigated in detail: their source
components (`MeleeWeaponSvg.tsx`/`RangedWeaponSvg.tsx`, 830-845 lines each vs. 18-45 lines for every
sibling attack-type icon) turned out to be a corrupted mirror extraction — 124-155 `<path>` elements per
file with coordinates scattered from x=0-416/y=0-666 against a 16-19px viewBox, of which only 3-4 stray
subpath fragments actually land inside the visible icon (not a recoverable drawing). **Direct owner
decision: leave this app's own hand-made melee/ranged icons as-is, not pursued further** — the corruption
finding is recorded here and in `svg-index.html`'s table as the reason, rather than left as an open
"needs cleanup" item.

The remaining 5 "New capability" rows in the same table (ability scores, conditions, proficiency dots,
coins, AoE shapes — 35 icons total, none consumed by any component yet) were extracted and staged into
`frames/` anyway, on **direct owner request**: "todos os ícones não mapeados... devem ficar salvos num
índice de ícones/svgs para centralizar num lugar só, mesmo que ainda não tenhamos uso para eles ainda."
No application code changed — these are assets only, not wired into any component:

- **6 ability icons** (`dnd_icon_ability_{charisma,constitution,dexterity,intelligence,strength,wisdom}.svg`)
  — two-color sources (`fillColor`→`currentColor`, `secondaryFillColor`→`#FFFFFF`, checked individually
  per icon, same as the damage-type precedent). Found where D&D Beyond actually uses these while
  investigating the owner's "não sei onde estão" question: not on the ability score box itself, but as
  the large icon in that ability's own sidebar panel (`AbilityPane.tsx`'s `Preview` slot, opened by
  clicking the box) — this app's `AbilityBox.tsx`/ability sidebar has no equivalent slot yet.
- **15 condition icons** (`dnd_icon_condition_{blinded...unconscious}.svg`) — mixed single/two-color per
  icon (checked individually; `RestrainedSvg`'s `<text>R</text>` glyph converted to a plain SVG
  `<text>` with `fill`/`stroke` both `currentColor`). Owner's plan: concatenate into each condition's
  label inside the sidebar's Conditions panel, not a separate icon column — not built this pass.
- **3 proficiency-dot icons** (`dnd_icon_proficiency_{full,half,expertise}.svg`) — `full`/`half` are
  single-color (`currentColor`); `expertise` keeps its outer ring as a literal `#999999` (a genuinely
  distinct muted tone in the source, not a currentColor-vs-white highlight pair) with a `currentColor`
  inner dot. Owner's plan: replace `dnd_icon_unchecked_circle.svg` and the current proficiency fill
  scheme in `SkillRow.tsx`/`SavingThrowRow.tsx`, driven by the proficient/expertise rule already coded
  in `ProficienciesPanel.tsx` — not built (no expertise concept exists in the domain model yet).
- **5 coin icons** (`dnd_icon_coin_{copper,silver,electrum,gold,platinum}.svg`) — copied as-is; unlike
  every other icon in this catalogue these carry no `fillColor`/`secondaryFillColor` prop at all, just
  hardcoded per-metal hex tones, since coin art shouldn't re-tint with theme. Owner's plan: replace
  `CoinChips.tsx`'s generic coloured-dot chips in the Inventory tab (not given much attention yet).
- **6 area-of-effect icons** (`dnd_icon_aoe_{cone,cube,cylinder,line,sphere,square}.svg`) — single-color
  except `square` (a `currentColor`-stroked rect filled `#FFFFFF`). Owner's plan: show in a spell's notes
  to represent its AoE shape when it has one — not built.

`build-icon-index.ps1` gained five new categories (`Get-Category`'s pattern match plus
`$categoryOrder`, each labeled "(staged, not yet applied)") so these 35 render grouped and distinguishable
from the 66 already-consumed icons instead of falling into the generic "Misc icons" bucket; regenerated
`icon-index.html` (66 → 101 total). `svg-index.html`'s "New capability" rows updated with the real
filenames (were `(none yet)`) and each row's own future-consumer plan, replacing the old generic notes.

---

## 2026-09-08 — Custom roll picker corrected to D&D Beyond's real dice icons + full tray redesign

**Direct owner report:** after 2026-09-07's dice-icon swap, the custom roll
picker "ficou bem mais diferente do que é no D&D Beyond" - it had drifted
*further* from the reference, not closer. Root cause: the 2026-09-07 swap
used `@dndbeyond/game-log-components`'s wireframe `DieIcon` set because it
was the only "dice face" icon family this repo's static mirror
(`design-reference/markup/`) happened to contain - but that package is for
chat/game-log notifications, a different UI context from the actual
"Roll Dice" popover (`@dndbeyond/pocket-dimension-dice`), which only exists
on the live site as minified JS with no captured markup or CSS.

Fixed by live-inspecting the owner's own logged-in session at
dndbeyond.com/characters/50149479's real "Roll Dice" popover (DOM
`getComputedStyle`/`getBoundingClientRect`, per this project's usual
fidelity method - never the site's proprietary source) instead of guessing
from the static mirror:

- **All 7 die icons** (`dnd_icon_die_{d4,d6,d8,d10,d12,d20,d100}.svg`) and
  the trigger icon (`dnd_icon_dice_roll.svg`) rewritten with path data
  pulled live from the real popover - `@dndbeyond/fontawesome-cache`'s
  "light" style dice icons, confirmed byte-for-byte against this project's
  one locally-cached copy (`dice-d20`); the other 5 don't exist in the
  local mirror at all. Added `dnd_icon_close.svg` (FontAwesome `xmark`) for
  the new panel's close button.
- **`dice.css`**'s `.custom-roll-picker*` block rewritten end to end with
  the live-measured spec: 50x50 solid `#92a2b3` circular trigger (its own
  background now blocks gaps in the line art, so `dnd_icon_dice_roll.svg`'s
  old white backing `<circle>` was removed as redundant); a 326px dark
  `rgba(18,24,28,0.95)` panel with a header row, `space-evenly` die grid
  (56x64 buttons, `#a2acb2` unselected / `#c4cbce` bg + `#373f45` text
  selected), a real circular count badge, white `Reset` + red `#e40712`
  `Roll` buttons, and a gold `#eccf83` "Clear Dice" row. Deliberately
  **left out** the reference's dice-set switcher (avatar/name/"Change
  Dice") - this app has no 3D dice-skin feature for it to control.
- **`CustomRollPicker.tsx`**: added the header row and close button, a
  `clearDice()` handler (reset counts + close, distinct from `Reset` alone
  which stays open), and a `--selected` class on the active die buttons.

**Bug caught during verification:** the new `Roll` button rendered with no
red background in the live app despite `dice.css`'s rule - traced to a
stray `className="roll custom-roll-picker__roll"`, where the leftover
generic `.roll` utility class (`frames.css`, the sidebar's shared
roll-target styling) tied on specificity with `.custom-roll-picker__roll`
and won the cascade. No code anywhere selects on `.roll` by behavior, only
by that shared style, so it was simply removed from this button.

Verified: `tsc --noEmit` clean, all icons render correctly in
`icon-index.html`, and the redesigned panel checked live in the running app
(logged in as `test`) - header, selected/unselected die states, count
badge, and Reset/Roll/Clear Dice all match the reference and a real roll
round-trips to the server correctly.
`design-reference/markup/build-svg-index.js`'s recommendations table
updated to correct the dice-icon source claim (was: game-log-components;
now: fontawesome-cache, live-measured) and regenerated.

---

## 2026-09-07 — CustomRollPicker's 6 die shapes replaced with official wireframe dice + a real D100

**Context this reverses:** `CustomRollPicker.tsx` originally shipped 6
original flat silhouettes (triangle/square/diamond/kite/pentagon/hexagon)
specifically to *avoid* a licensed dice-icon set (Flaticon's polyhedral set
needs attribution this app has nowhere to give) - documented in the
component's own doc comment. That reasoning doesn't apply to D&D Beyond's
own assets specifically, now that copying those is allowed, so on **direct
owner request** all 6 were replaced with `@dndbeyond/game-log-components`'s
real wireframe `DieIcon` set (`design-reference/markup/svg-index.html`'s
"Game log / dice widget package" category) - single-color sources, plain
file swaps, `dnd_icon_die_{d4,d6,d8,d10,d12,d20}.svg`.

**D100** previously reused the D10 shape (also a documented decision - "a
percentile die *is* a d10"). The reference package has a real D100 icon
(two D10s side by side, matching its own percentile-die convention) - added
as a new `dnd_icon_die_d100.svg` file and wired into `CustomRollPicker.tsx`'s
`DIE_ICONS` map, replacing the D10 reuse.

**Trigger icon** (`dnd_icon_dice_roll.svg`, the collapsed picker button, a
separate asset from the 6 grid icons) swapped for the reference's D20 too.
Its own `<circle fill="#ffffff">` backing shape (owner-added 2026-08-18 so
gaps in the line art don't show the page through) had to be kept - the
reference D20 is *also* a wireframe (transparent gaps), so the backing
circle is if anything more necessary here, not less. Re-added at
`cx="16" cy="16" r="15.5"` to match the reference's `0 0 32 32` viewBox.

Rendered every replacement in `icon-index.html` before calling it done -
all 7 die faces show as clean wireframe polyhedra, and the trigger icon's
backing circle is visible (blocks the checkerboard preview background in a
circle around the wireframe, same as before). `tsc --noEmit` clean.

---

## 2026-09-07 — Long/Short Rest icons swapped despite a neutral/negative comparison

Rendered `dnd_icon_long_rest.svg`/`dnd_icon_short_rest.svg` (the app's own
custom art) side by side against the catalogue's `LongRestSvg`/`ShortRestSvg`
before touching anything. Verdict: Long Rest was a wash (both a crescent
moon, just mirrored left/right - no quality difference either way); Short
Rest was arguably a downgrade (the app's campfire had textured wood-grain
logs, the catalogue's has two plain solid capsules). Recommended keeping
the current assets on that basis. **Direct owner request** to swap anyway -
done: both are single-color sources (`fillColor` only, no secondary color),
so this was a plain file swap through `FrameIcon` with no component changes.
Verified in `icon-index.html`.

---

## 2026-09-07 — Fixed 4 damage-type icons missing their white detail lines

**Owner report:** the damage-type source components under `design-reference/
markup/.../smartComponents/Svg/damageTypes/` aren't all single-color the way
the initial extraction assumed - `BludgeoningSvg`, `NecroticSvg`,
`PiercingSvg`, and `PsychicSvg` each carry a `polygon`/`path` in `fillColor`
plus one or more additional `path`/`line` elements in `secondaryFillColor`,
which the very first extraction pass (2026-09-07, above) collapsed into the
same `currentColor` as everything else - invisible against its own
background, same class of bug as the Ritual/Concentration markers fixed
earlier that day.

Checked `SvgConstants.ts` for what these two colors actually resolve to:
the app's default rendering (`SvgConstantDarkTheme`, used whenever
`isDarkMode` is false - i.e. always, for this light-background app) is
`fill: "#242528"` (this app's own `--text-primary`, i.e. `currentColor` here
is the right call) and `secondaryFill: "#fff"` - a hardcoded white detail
layer, not a theme-driven color. Confirmed exactly 4 of the 13 damage types
actually use `secondaryFillColor` in a real `fill`/`stroke` (the other 9
destructure it but never use it, same false-alarm shape as `UnarmedStrikeSvg`
earlier): `dnd_icon_damage_bludgeoning.svg` (white fracture/impact lines),
`dnd_icon_damage_necrotic.svg` (a white separator line plus outline seams on
the hood and bone shapes), `dnd_icon_damage_piercing.svg` (a white gap
between the arrowhead and shaft), `dnd_icon_damage_psychic.svg` (white
facial detail lines on the head silhouette - previously a featureless
blob). Fixed by hand: `fillColor` → `currentColor`, `secondaryFillColor` →
`#FFFFFF`, verified visually via `icon-index.html` (all 4 now show the
detail lines that were invisible before). All 8 spell schools were checked
too and confirmed genuinely single-color - no equivalent fix needed there.

---

## 2026-09-07 — 31 `frames/` assets swapped for official D&D Beyond artwork

**Context:** the owner's CLAUDE.md rule against using D&D Beyond's proprietary
source code was removed this session, clearing the way to adopt real
character-app component source (extracted from a scraped mirror in
`design-reference/markup/`, catalogued by `svg-index.html`) instead of this
project's own hand-traced recreations, where a clean match exists.

**Applied (verified live against the running app, `test`/`test-osv-123`):**
- All 13 `dnd_icon_damage_*.svg` and all 8 `dnd_icon_school_*.svg` — official
  glyphs (`damageTypes.ts`/`spellSchools.ts`), 1:1 name match.
- `dnd_icon_attack_unarmed.svg`, `dnd_icon_attack_spell.svg` (`AttackRow.tsx`).
- `dnd_icon_marker_ritual.svg`, `dnd_icon_marker_concentration.svg`
  (`SpellRow.tsx`) — real vector art replacing the previous book/diamond +
  literal `<text>` letter placeholders from 2026-09-04 below. The source
  component uses two colors (`fillColor` for the shape, `secondaryFillColor`
  for the letter); the first extraction pass collapsed both to
  `currentColor`, making the letter invisible — fixed by hand (shape =
  `currentColor`, letter = `#FFFFFF`, matching the old placeholder's own
  contrast technique).
- `dnd_icon_inspiration_on.svg` (`HeroicInspiration.tsx`).
- `dnd_frame_armor_class.svg` (`ArmorClass.tsx`), `dnd_frame_initiative.svg`
  (`Initiative.tsx`), `dnd_frame_inspiration.svg` (`HeroicInspiration.tsx`) —
  these three sources are genuinely two-color (`theme.backgroundColor` for
  the body, `theme.themeColor` for the border), which doesn't fit
  `FrameLayer`'s existing technique of rendering one shared SVG string twice
  in two flat colors. Rather than collapsing both colors into one (which
  would print as a solid blob), each was split into a `..._ink.svg` sibling
  file, and the three consuming components now pass two different imports
  to `FrameLayer`'s `paper`/`ink` calls instead of the same one twice — no
  change to `FrameLayer.tsx` itself was needed. `.inspiration::before`'s own
  hand-tuned clip-path (see the 2026-08-18 entry below) was checked live
  after the swap and still reads clean; re-check if that corner ever looks
  wrong.
- `dnd_frame_saving_throw_attribute.svg` (`SavingThrowRow.tsx`),
  `dnd_frame_sense_item.svg` (`SenseRow.tsx`) — single-color sources, plain
  swaps, no code changes.

**Corrected along the way:** the reference catalogue's `SavingThrowSelectionBoxSvg`
(41×36, thin gray stroke) and the panel-level `dnd_frame_saving_throws.svg`
(`SavingThrowsPanel.tsx`, ~278×199) looked like a name match but aren't — the
former is a small per-item selection ring, not a panel background; no
whole-panel asset exists in the catalogue. Same story for
`dnd_frame_senses.svg` (`SensesPanel.tsx`) versus `SenseRowBoxSvg`, which
turned out to match the *row* asset (`dnd_frame_sense_item.svg`) instead.
Both panel-level files are left untouched.

**Deliberately left out:** `dnd_icon_attack_melee.svg`/`_ranged.svg` (a real
match exists — `MeleeWeaponSvg`/`RangedWeaponSvg` — but their source colors
a dynamic `<style>` block via template-literal CSS, not a plain `fill` prop,
so they need manual color resolution before use); the six `dnd_icon_die_*`
faces and `dnd_icon_long_rest.svg`/`dnd_icon_short_rest.svg` (a real match
exists but in a visibly different art style made for a different UI
context — flagged for a human visual call, not swapped blind);
`dnd_icon_dice_roll.svg` (has an owner-requested custom glow, do not
overwrite); `dnd_icon_game_log.svg` (deliberately generic, not meant to be
D&D-specific). Full reasoning for all 54 `frames/` files lives in the
"Recommendations for apps/web" table at the top of
`design-reference/markup/svg-index.html`.

**Follow-up, same day — the 13 generic "theme box" variants:** the reference
catalogue's `boxes/themeBoxes/` holds 13 sizes of three generic decorative
box shapes (`BeveledBoxSvg`, `FancyBoxSvg`, `SquaredBoxSvg`) not tied to any
one named section — these turned out to be the *actual* source for several
`frames/` files this app had already built from live measurement without
knowing an official asset existed. Matched by cross-referencing each
candidate's native pixel dimensions against `frames.css`'s own DOM-measured
comments (several are exact-width matches, not approximations):
- `BeveledBoxSvg317x89` → `dnd_frame_hit_points.svg` (317px width and
  400/113 ratio are the exact numbers `.hit-points`'s own comment already
  cited from a live `getBoundingClientRect`).
- `BeveledBoxSvg623x660` → `dnd_frame_actions.svg` (`Dnd5eTabbedSection.tsx`'s
  own doc comment already named this exact match: "matching D&D Beyond's own
  `.ct-primary-box` almost exactly (measured live: 623x660)").
- `SquaredBoxSvg408x95` → `dnd_frame_defenses_conditions.svg` (408px exact).
- `SquaredBoxSvg278x338` → `dnd_frame_proficiencies_training.svg` (278px
  exact, ratio within 0.2% of the panel's own aspect-ratio).
- `FancyBoxSvg281x765` → `dnd_frame_skills.svg` (ratio matches the asset's
  own pre-override native ratio, 356/960, that a `.skills-panel` comment had
  documented before the panel was stretched for column-alignment reasons).
- `BeveledBoxSvg94x89` → `dnd_frame_proficiency_bonus_and_speed.svg` (lower
  confidence — no exact-width comment exists for this one, only a "~1:1"
  note — applied on explicit owner confirmation and verified live).

All six are two-color sources needing the same paper/ink split as Armor
Class/Initiative/Inspiration above. `SectionPanel.tsx` (shared by
Defenses/Conditions, Proficiencies, and Skills) gained an optional `inkSvg`
prop instead of duplicating `FrameLayer` calls in each consumer; Hit Points,
the tabbed section, and the Proficiency Bonus/Speed badge call `FrameLayer`
directly so they just import and pass a second file, same as before.
7 of the 13 theme-box sizes (`BeveledBoxSvg517x660`, `FancyBoxSvg230x200`,
`FancyBoxSvg230x765`, `FancyBoxSvg281x200`, `FancyBoxSvg361x765`,
`SquaredBoxSvg228x338`, `SquaredBoxSvg344x95`) had no matching app asset —
left uncatalogued rather than guessed.

**Follow-up, same day — full-mirror sweep and the last confirmed match:**
widened the search beyond `media.dndbeyond.com` to the whole scraped mirror
(`www.dndbeyond.com`, the `webpack---*` source-map trees, `mega-menu`,
`message-broker-client`, `wasm---wasm`, the ketch/GTM/jspm infra folders) to
map how the pieces fit together and chase the remaining unmatched files.
Nothing new turned up for the app (the extra packages found there,
`@dndbeyond/ttui` and `@dndbeyond/navigation-menu`, are the character-app's
own top-nav/icon-wrapper dependencies, not additional glyphs), but it did
turn up the strongest match in this whole exercise:
- `dnd_frame.svg` (`AbilityBox.tsx`, `.ability` 81x95) ← `Decorative panel
  boxes / AbilityScoreBoxSvg` — viewBox `0 0 81 95` matches the app's own
  box dimensions exactly on *both* axes (81=81, 95=95), the only box in the
  whole catalogue to do so. Bonus: the file it replaced was a raster PNG
  wrapped in `<svg><image>` (`svgUtils.ts`'s `extractRasterImage` fallback
  path) — this is a real vector asset closing that gap, not just a
  lookalike swap. Two-color source as usual, split into
  `dnd_frame.svg`/`dnd_frame_ink.svg`, `AbilityBox.tsx` updated to pass both.
  `dnd_frame_old.svg` (a sibling file) has no import anywhere in `apps/web/
  src` — confirmed dead, left alone rather than deleted.
- `dnd_icon_unchecked_circle.svg` (`SkillRow.tsx`) — confirmed no catalogue
  equivalent exists (checked `ProficiencySvg`/`ProficiencyHalfSvg`, both
  solid-filled with no empty-ring variant, and `ttui/Icons`, a generic
  wrapper). Separately noted: `SavingThrowRow.tsx` already renders the
  identical "unchecked = empty ring" concept with a plain CSS-bordered
  circle, no SVG at all — `SkillRow`'s own SVG for the same concept is an
  internal inconsistency worth reconciling on its own, independent of this
  catalogue.
- The 7 leftover theme-box sizes are confirmed dead ends for this app —
  every `frames/` file now has either an applied match or a documented
  reason it doesn't.

---

## 2026-09-04 — Icon index: a browsable reference for every SVG in `frames/`

**Direct owner request:** a way to see every icon and frame asset (54 SVGs
across `apps/web/src/systems/dnd5e/frames/`) and their raw markup in one
place, for quick reference while working on icon-related changes. Added
`icon-index.html` (open directly in a browser) plus the PowerShell
generator that builds it, `build-icon-index.ps1` — both live alongside
`frames/` itself. The page groups every SVG by kind (attack categories,
damage types, spell schools, spell markers, dice faces, rest, misc icons,
frame panels), each as a card showing a live rendered preview (click to
expand the raw source), the filename, the exact `?raw` import line a
consumer would write, and a "Copy SVG" button — plus a filename filter
across the whole page. Not wired into the app itself (no route, no build
step) — a standalone dev reference, regenerate by running the `.ps1` after
adding, removing, or renaming an icon in `frames/`.

---

## 2026-09-04 — Concentration/Ritual markers get original diamond/closed-book artwork

**Direct owner request:** "tente desenhar os ícones de losango e livro
fechado para concentração e ritual" — replacing the plain circular "C"/"R"
letter badge `SpellRow.tsx` used (itself just landed a few hours earlier
as part of the Spells tab re-measure pass). Two new original SVGs,
`dnd_icon_marker_concentration.svg` (a diamond) and `dnd_icon_marker_
ritual.svg` (a closed book — spine plus rounded cover, not D&D Beyond's
own scroll shape), both 16×16 viewBox: a `currentColor` silhouette with the
letter as a real SVG `<text>` node in fixed white — the same filled-shape-
plus-reversed-letter contrast D&D Beyond's own marker uses, but this app's
own geometry, not traced from D&D Beyond's proprietary icon set
(ground-rules.md). Rendered via the existing `FrameIcon` convention every
other icon in this file already uses. `.spell-row__marker` dropped its
`background`/`border-radius: 50%`/`color` badge styling in favor of sizing
the icon (12×12px) and tinting it via `color: var(--frame-ink)`. Verified
live: Aria Emberfall's "Detect Magic" (concentration + ritual together)
renders both shapes distinctly, still inline with the wrapping name text,
still not clipped.

---

## 2026-09-04 — Spells tab re-measured against D&D Beyond: sections, typography, spacing, and the "eaten" concentration/ritual markers

**Direct owner report:** the Spells tab was "bem diferente do que se espera"
— asked to focus specifically on section separation, font/weight/size,
spacing, and a concrete bug: the concentration/ritual markers get eaten by
long spell names. Pointed at Copy of Raya's "Protection from Evil and
Good" and "Drawmij's Instant Summons" as the cases that expose it.

Re-measured live against Copy of Raya's `.ct-spells-*` DOM (both named
spells, chosen because they're long enough to wrap). Root cause of the
eating bug: `.spell-row__name-cell` was a fixed 96px `display: flex` row
with the C/R markers as flex *siblings* of the name button, and
`.spell-row__name` was `white-space: nowrap` — a long name simply
overflowed the 96px box and visually overran the marker sitting next to
it. D&D Beyond's own name column is 135px, `white-space: normal`, and the
marker is an inline child *inside* the name span, not a sibling — a long
name wraps onto a second line with the marker riding along at the end,
never clipped. `SpellRow.tsx` now nests the C/R markers inside the name
button (trailing children); `.spell-row__name` is `white-space: normal`.

Column widths across the whole row are now D&D Beyond's own measured
pixel values, not guesses: 35 (cast) / 135 (name) / 35 (time) / 55 (range)
/ 45 (hit-dc) / 85 (effect) / 178 (notes), 568 total, zero inter-column
gap (`.spell-row`'s `gap: 10px` → `0`) — spacing comes entirely from each
column's own fixed width, same as D&D Beyond. `align-items` on the row
changed `center` → `flex-start`, so a wrapped two-line name doesn't push
the single-line Time/Range/etc. columns beside it to the vertical middle
of the now-taller row. Per-row `border-bottom` removed entirely — D&D
Beyond has no divider between individual spell rows, only under the level
heading.

Typography corrected to D&D Beyond's own measured values: `.spell-row__name`
is `--font-condensed` (Roboto Condensed) 14px/400/**italic** (was inherited
sans-serif, 700, upright — a deliberate 2026-08-16 "legibility" deviation
that turned out to be part of the same bug, since `nowrap` is what let a
long name overflow into its marker). The level heading ("1st Level",
"Cantrip") switched from the plain, undivided `.actions-tab__heading` to a
new `.spells-level__heading`/`.spells-level__heading-label` pair matching
D&D Beyond's own `.ct-content-group__header`: 13px/700/uppercase in
`--accent-control`, `1px solid #EAEAEA` directly under it, 10px clear
margin before the column header row — the same treatment
`.actions-tab__section-heading` already gives the Actions tab.

The header stats row (Modifier/Spell Attack/Save DC) is now genuinely
centered: it used to share a `justify-content: space-between` row with the
"Manage Spells" button, which pushed it off-center by the button's own
width. The button is now `position: absolute; right: 0` inside a
`position: relative` wrapper, letting `.spellcasting-header` center
independently. A multiclass caster's per-class values (previously a
literal `' | '`-joined string) are now individual tooltipped spans
(`.spellcasting-header__value-item`, `title` = class name) with a CSS
`::before` divider (`border-left: 2px solid`, 8×11px) — D&D Beyond's own
technique, DOM-measured against Helga Flinthand's Cleric/Paladin values,
not a literal pipe character.

Left deliberately untouched (still open, separately scoped): the
Concentration/Ritual filter chips' own C/R letter-badge look (D&D Beyond
uses diamond/scroll artwork — needs original icons, not copied per
ground-rules.md), and the search/filter row's structure (D&D Beyond splits
it onto its own row with a funnel button opening a much richer multi-
criteria panel than this app has). Verified live in this app: Aria
Emberfall's "Detect Magic" (both concentration and ritual) now shows both
markers cleanly inline, not eaten; header stats centered; "Cantrip"/"1st
Level" headings carry the divider and accent color; "Cast" buttons sit
inside their column.

---

## 2026-09-04 — Unarmed Strike deals flat Strength-modifier damage, no dice

**Direct owner request:** Unarmed Strike's damage roll was `1d1+<STR mod>` —
a die that always rolls 1, a hack reusing the dice-rolling UI for what's
actually a flat value. The owner asked for exactly that: no dice involved,
just the current Strength modifier, still a clickable roll that shows up in
the dice tray and the game log like any other.

`Dnd5eAttack.damageDiceCount` (and the generic `AttackRow`/`AttackRowResponse`
it flows through) already allowed any `int`; relaxed its `@Min(1)` to
`@Min(0)` — `0` now means "no dice, flat modifier only," the same convention
`Dnd5eSpell`'s nullable dice fields use for a no-roll spell, just as a
sentinel value instead of `null` (this domain's dice count was never
nullable, and making it so would have touched every consumer for one
attack). `DevCharacterSeeder`'s `unarmedStrike` entry changed
`damageDiceCount` from `1` to `0`. `RollService.roll` (backend) and
`AttackRow.tsx` (frontend) both special-case a `0` count: the roll
expression is just the modifier (`"3"`, not `"0d1+3"`), and the button's
label drops the `NdM` prefix entirely. `DiceTray.tsx` no longer shows an
empty, contentless `()` after the expression when there were no dice to
list. Since the seeded reference character (Aria Emberfall) already had the
old `1`/`1` values persisted in Postgres from 2026-08-15 — the seeder only
runs once, on first player creation, not from the "Create character" UI
button (phase 11 isn't built yet; that path hit an unrelated pre-existing
bug, `savingThrowProficiencies()` null, on a stub sheet) — her stored
`sheet` JSON was patched directly via `jsonb_set` to match, matching what
any newly-seeded character gets going forward. Verified live: the Damage
button now reads `+3` (Aria's Strength modifier) instead of `1d1+3`, and
the game log shows `Unarmed Strike: damage` with expression `3`.

---

## 2026-09-04 — `BoxTrack`'s marked box redesigned to match D&D Beyond's actual fill

**Direct owner report:** limited-use feature squares in the Actions tab
(Second Wind, Action Surge, Channel Divinity, etc.) filled solid when
clicked — D&D Beyond does not. DOM-measured live (`getComputedStyle`,
mid-click, on the reference character's Channel Divinity/Divine Sense
boxes): D&D Beyond's `.ct-slot-manager__slot--used` keeps the same white
background and `#D8D8D8` border as an unmarked box — the mark is a smaller
solid square (a `::before`, 10px centered inside the 20px box, i.e. half
size) colored `#92A2B3`, which is this codebase's own `--accent-control`
token, not `--frame-ink` (the wrong color `BoxTrack.tsx` used for the old
full fill). `BoxTrack.tsx` now renders that inner square as a real centered
child span instead of filling its own background. Picked up along the way:
D&D Beyond's boxes also carry a faint inset shadow (`0 0 4px #D8D8D8
inset`) in both states, reproduced identically. Confirmed live, same
investigation, that clicking any box in a multi-box track (not just the one
under the cursor) fills from the left / empties from the right on D&D
Beyond — already `BoxTrack`'s own behavior, since `onUse`/`onRestore` were
never bound to a specific index. Verified live in both apps: D&D Beyond's
Channel Divinity/Divine Sense (marked then restored, no net change to the
reference character) and this app's Second Wind (marked then restored,
same).

**Corrected same day, direct owner report ("não está 100% alinhado no
centro"):** the inner mark was sized `50%`/`50%`, which resolves against
the box's 18px content area (20px minus the 1px border each side) — 9px,
an odd number flexbox's centering can't split evenly (4px one side, 5px
the other). Switched to the exact `10px` D&D Beyond itself uses, which
divides evenly into that 18px content area (4px both sides). Verified by
measuring the rendered `getBoundingClientRect()` gap on all four sides —
5px each (the extra 1px over 4 is the box's own border), confirming true
center, not just visual approximation.

---

## 2026-09-04 — Fire and piercing damage-type icons swapped to Flaticon; `FrameIcon` gains a raster fallback

**Direct owner request**, exact Flaticon URLs supplied for both: fire's icon
(already `small-fire` from the same-day game-icons.net corrections below)
moved again, this time to Flaticon's "Fire" by meaicon; piercing moved from
game-icons.net's `fast-arrow` to Flaticon's "Bullet" by Magnific.

Flaticon paywalls SVG for both (PNG-only free tier — also EPS/PSD/CSS behind
the same crown badge), breaking the fetch-and-recolor pipeline (`fill="#000"`
→ `currentColor`) every other damage-type icon uses, since a raster PNG has
no path to recolor. `FrameIcon.tsx` gained the same raster fallback
`FrameLayer.tsx` already had for `dnd_frame.svg`: `extractRasterImage` (from
`svgUtils.ts`) detects an `<svg><image href="data:...">` wrapper and renders
a `mask-image` span (new `.frame-icon--raster` in `frames.css`, `mask-size:
contain` rather than `FrameLayer`'s stretch) instead of the usual inline
`<svg>` injection — same `background-color: currentColor` recoloring, just
via the raster's alpha channel instead of a path fill. `dnd_icon_damage_fire.svg`
and `dnd_icon_damage_piercing.svg` are now that wrapper around each icon's
free 512px PNG (fetched from Flaticon's own CDN, `cdn-icons-png.flaticon.com`
— the site itself 403s a plain fetch). Both require attribution (Flaticon
License), tracked on the still-owed credits page alongside the game-icons.net
CC BY 3.0 debt. Verified live in the browser (Aria Emberfall's Shortbow and
Fire Bolt rows) — both icons render crisp and pick up the theme color.

---

## 2026-09-04 — Fire damage-type icon swapped, twice, settling on Lorc's `small-fire`

**Direct owner request**, exact game-icons.net URL supplied
(https://game-icons.net/1x1/sbed/flamer.html): the fire damage-type icon
(`dnd_icon_damage_fire.svg`) was Lorc's `flamed-leaf`, swapped to Sbed's
`flamer` — same file, same fetch-and-recolor pipeline (`fill="#000"` →
`currentColor`).

**Corrected minutes later, same day, another direct owner request** (exact
URL supplied again: https://game-icons.net/1x1/lorc/small-fire.html):
`flamer` → Lorc's `small-fire`, the final pick — same file, same pipeline.
No other damage type changed either time.

---

## 2026-09-04 — AppShell's top bar hidden on the sheet screen; back/user folded into the dark header

**Direct owner request:** `AppShell.tsx`'s own top bar (app name, signed-in
user, Log out) used to sit above every screen, including the character
sheet — now hidden while a sheet is open (`!selectedCharacter` gate), since
the sheet's own dark header (`SheetShell.tsx`) took over its load-bearing
bits. The plain "← Back to characters" button (previously floating above
`.sheet-header`) moved inside that dark bar as a new `.sheet-header__account`
block: a tiny caption showing the signed-in user's display name, directly
above a "← Back" button styled like the rest buttons (`.sheet-header__button`)
but deliberately quieter (smaller font/padding, a dimmer border/text color).
`userLabel` threads from `AppShell` through `CharacterSheetScreen` into
`SheetShell`. No Log out control exists on the sheet screen — that stays on
`AppShell`'s own header, one "Back" click away on the character list screen.

**Corrected same day, two follow-up owner reports:**
1. The account block first rendered as a normal flex child, only reaching
   the sheet's own centered `fit-content` column's left edge — well right of
   the screen's actual corner on any normal desktop width. Switched to
   `position: absolute` with the same `left: 50%` + viewport-relative
   `transform` breakout trick `.sheet-header::before`'s full-bleed
   background already uses (its own center coincides with the viewport's,
   since the wrapping column is `margin: 0 auto`), just anchored to the left
   edge instead of centered — now pinned to the screen's true corner,
   ignoring the sheet's own content centering.
2. `SheetShell`'s outer wrapper still had `padding: '24px'` on all sides,
   leaving a visible white strip above the dark bar (which starts flush
   against the viewport horizontally via its own full-bleed trick, but was
   still pushed down 24px vertically). Changed to `padding: '0 24px 24px'` —
   symmetric left/right (required for the centering math both breakout
   tricks depend on) but zero on top, so the dark bar now starts flush at
   the very top of the page.

**Corrected again same day, two more owner reports:**
3. The white strip persisted even at zero top padding on `SheetShell`'s own
   wrapper — DOM-measured (`getComputedStyle` on `document.body`) as the
   browser's own default `body { margin: 8px }`, never reset anywhere in
   this project (it sits outside every component this app renders, so no
   component-level fix could reach it). Fixed with a new `global.css`
   (`body { margin: 0 }`, the project's first and only global reset),
   imported once from `main.tsx`.
4. `.sheet-header__account` moved from vertically centered (`top: 50%`,
   `transform`'s Y half doing the centering) to `top: 8px` — pinned to the
   *top* of the left corner, per the owner's own phrasing ("no topo do
   canto esquerdo"), not mid-height. Also switched from a stacked column
   (user label above the button) to a row (button, then a smaller label —
   9px → 8px — to its right).

---

## 2026-09-04 — Damage-type icons replace damage-type text on every damage/heal roll button

**Direct owner request**, with exact game-icons.net URLs supplied for all
thirteen 5e damage types: `AttackRow.tsx`, `SpellAttackRow.tsx` and
`SpellRow.tsx`'s Damage/Effect roll buttons used to show the type as visible
text (`1d10 fire`, `1d8 necrotic`) — replaced with a small icon
(`damageTypes.ts`'s `DAMAGE_TYPE_ICONS`, keyed lowercase to match
`damageType` as stored) next to the dice notation instead. New
`.action-row__damage-type` class (13×13px, `aria-hidden` since
the button's own `aria-label` already speaks the type name). Hovering the
icon shows the type name via a plain `title` attribute (`damageTypeLabel`) —
the same native-tooltip technique `.spell-row__marker`'s Concentration/Ritual
badges already used, not a custom tooltip component. A healing roll
(`damageType` is null) never gets one.

**Corrected same day, direct owner report ("tão visíveis (cor) quanto os
ícones da escola de magia"):** first shipped at `--text-muted`, DOM-measured
against D&D Beyond's own `.ddbc-damage-type-icon__img` (`fill: rgb(0,0,0)` —
solid black, the same weight as its own leading category icon, both 16×16)
and found too low-contrast. Switched to `--text-primary`, matching
`.action-row__icon`'s (the school/category icon's) own color exactly.

Icons, all CC BY 3.0 (owner-supplied exact URLs, downloaded via
`https://game-icons.net/icons/000000/transparent/1x1/{author}/{slug}.svg` and
`fill` switched to `currentColor`, same pipeline as the earlier attack-
category icons): `fast-arrow` (piercing), `axe-swing` (slashing),
`hammer-drop` (bludgeoning), `flamed-leaf` (fire), `beveled-star` (cold),
`reaper-scythe` (necrotic), `ion-cannon-blast` (force), `focused-lightning`
(lightning), `suspicious` (psychic), `eclipse-flare` (radiant),
`lightning-storm` (thunder) — all by Lorc; `death-juice` (poison) by
Daniel Zaitzev/darkzaitzev; `acid` (acid) by Sbed. **Adds thirteen more
entries to the still-owed CC BY 3.0 credits page**, alongside the
attack-category and weapon icons already tracked there.

Also fixed mid-session: the Vite dev server (port 5173) had stopped running
entirely (no `node` process left) partway through this session — restarted
via `npm run dev` from `apps/web`; no data or code was lost, purely a
stopped local process.

---

## 2026-09-03 — Combat spells now render in the Actions tab; school icons moved there; layout fixes

**Combat spells (attack or healing) now also render in the Actions tab.**
Direct owner request, investigated live against D&D Beyond first: Helga
Flinthand (dndbeyond.com/characters/50149479, Cleric/Paladin) shows Guiding
Bolt and Inflict Wounds in the same Attacks table as her weapons; Copy of
Raya (.../56008841, Wizard 20) shows Fire Bolt, Ray of Frost and Ice Knife
there too — a combat spell sits in **both** the Actions and Spells tabs at
once, not one or the other, and every one of those rows used a school icon
and an italicized name with a "Level • Class" subtitle, never a weapon's
category text. New `spellCombat.ts` (`isCombatSpell`: `attackRoll ||
damageDiceCount != null || effectSummary === 'Healing'`) picks the subset;
new `SpellAttackRow.tsx` renders it in `AttackRow.tsx`'s own `.action-row`
shape, appended into `ActionsTab.tsx`'s existing attack `.action-list`. A
pure-utility spell (Mage Armor, Detect Magic, Fog Cloud, Charm Person, Minor
Illusion, Mending) is unaffected — Spells-tab-only, as before.

**The school-of-magic icons (prepared, then briefly wired into the Spells
tab, both earlier the same day — see the entry below) turned out to belong
in the Actions tab instead**, per the owner's direct correction once the
D&D Beyond research above landed: `SCHOOL_ICONS` was extracted out of
`SpellRow.tsx` into a shared `spellSchools.ts` so `SpellAttackRow.tsx` could
use it too, and removed entirely from the Spells tab. In its place, DOM-
measured against D&D Beyond's own Spells tab (same two reference
characters): the row's leading column is now a Cast/At Will/As Ritual
indicator (`.spell-row__cast`) — a cantrip shows a plain "At Will" label, a
ritual spell shows "As Ritual" (ritual takes priority over a real Cast
button even for a spell that could also be prepared normally — a documented
simplification, D&D Beyond's own rule additionally depends on `prepared`,
which this app has no equivalent branch for), and everything else gets a
real "Cast" button reusing the exact same `onOpenDetail` the spell's name
already opened. `buildSpellDetailRequest` (new `spellDetail.tsx`) was
extracted out of `SpellsTab.tsx` so both the Spells tab's row and the new
Actions tab row open the identical Entity Detail panel for the same spell.

**A new roll kind, `SPELL_HEAL`**, backs the healing side of this
(`RollKind.java`, `Dnd5eMechanicResolver.spellHeal`) — unlike `SPELL_DAMAGE`,
it adds the spellcasting modifier, matching the PHB's own healing-spell math
(Cure Wounds: 1d8 + spellcasting ability modifier). A ninth test spell, Cure
Wounds (evocation, level 1, `effectSummary` "Healing"), was added to
`DevCharacterSeeder.java` and manually patched into Aria Emberfall's existing
dev-seeded row (`spellsKnownMax` raised 4 → 5 to fit it) so the healing path
had a real spell to verify — confirmed live, including that the roll reads
"Cure Wounds: Healing" in the dice tray, not "damage". The **already-running
dev API process had to be restarted** mid-session for this — it predated the
`SPELL_HEAL` enum addition, so the roll returned HTTP 400 (Jackson rejecting
an enum value the running process didn't know) until `./gradlew
:apps:api:bootRun` picked up the recompiled classes.

**Direct owner report, "Unarmed strike deve ser o último":** `ActionsTab.tsx`
now splits `sheet.attacks` into non-unarmed and unarmed entries by the same
`category` keyword match `AttackRow.tsx`'s own icon selection already uses,
rendering weapons → combat spells → unarmed attacks — confirmed live that
every spellcasting reference character sorts Unarmed Strike last, after both
other weapons and spell attacks, never wherever `sheet.attacks`'s own map
insertion order happened to place it.

**Three small layout fixes, also direct owner reports:**
- `Dnd5eVitalsTopRow.tsx`'s `marginTop: '-8px'` (pulling the ability-score row
  up into the header bar) had no bottom margin of its own, so removing the
  sibling `Dnd5eVitalsColumns` wrapper's own `marginTop: '16px'` (next fix)
  would have collapsed the gap between them to zero — collapsed into one
  `margin: '-8px 0 10px'` shorthand so this row alone owns the spacing on
  both sides.
- `CharacterSheetScreen.tsx`'s `<div style={{ marginTop: '16px' }}>` wrapping
  `Dnd5eVitalsColumns` was redundant with the fix above and removed.
- The Actions tab's Hit/DC and Damage roll boxes (`.action-row__value`/
  `--prominent`) rendered at different heights (20px vs 13px font, different
  padding), leaving the two roll squares in a row visually unaligned.
  `.action-row__value` gained a fixed `height: 30px` with flex centering
  (replacing line-height-based sizing) and `--prominent` dropped its own
  vertical padding, so both share one height regardless of font size — a
  deliberate deviation from the literal D&D Beyond DOM measurement, per
  direct owner request.

---

## 2026-09-03 — School-of-magic icons wired into Spells tab; dice tray dismissal now survives reload

**School icons wired in.** `SpellRow.tsx` gained a leading `.spell-row__icon`
column (mirroring `AttackRow.tsx`'s own `.action-row__icon`), looking up the
eight `dnd_icon_school_*.svg` files prepared earlier the same day via a
`SCHOOL_ICONS` record keyed by `Spell.school` — a direct lookup, no fallback
branch, since every spell carries one of the eight schools. `SpellsTab.tsx`'s
header row gained a matching empty spacer so columns stay aligned. Five test
spells were added to `DevCharacterSeeder.java` (Fog Cloud/conjuration, Charm
Person/enchantment, Minor Illusion/illusion, Chill Touch/necromancy,
Mending/transmutation) — direct owner request, so all eight schools have a
real row to verify the icon against instead of just the three the original
three spells covered; `cantripsKnownMax` raised 2 → 4 to fit the extra
cantrips. Aria Emberfall's existing dev-seeded row predates this seeder
change, so a manual `jsonb_set`/`jsonb_build_object` patch was applied
directly to the running dev database to add the same five spells (the seeder
only runs on first character creation). Verified live: all eight spells now
show a distinct, correct icon in the Spells tab.

**Dice tray dismissal now persists.** Direct owner report: reloading the
sheet page always resurrected the last roll in the floating dice tray with a
fresh countdown, even after the owner had already watched it fully dismiss
once — `dismissedKey` was plain in-memory `useState`, reset to `null` on
every reload. Fixed in `DiceTray.tsx` by mirroring the dismissed key to
`localStorage` (try/catch-wrapped; a private window or blocked storage just
falls back to the pre-fix behavior for that reload) and using it as the
state's lazy initializer. Verified live: triggered a roll, let its 30s
countdown finish undisturbed, reloaded the page — the tray stayed gone; the
roll's value remains visible only via the Game Log panel, as intended.

Credits/attribution page remains deliberately deferred (owner: many icon
sources still need crediting) — no action this pass, still tracked in the
entry below and in `dnd-5e-sheet-build.md`.

---

## 2026-09-03 — Weapon icons replaced with game-icons.net picks; school-of-magic icons prepared

**Attack row icons were hand-drawn geometric shapes; the owner asked for real
free icons instead**, having compared D&D Beyond's own icon variety across
several reference characters first. Confirmed live (dndbeyond.com/characters/
48929054): D&D Beyond uses one icon per broad category, not one per specific
weapon — Longbow and Oathbow (both ranged) share an icon, every melee weapon
shares another, Unarmed Strike and Talons share a third. This matched
`AttackRow.tsx`'s existing three-bucket keyword match exactly, so only the
artwork needed replacing, not the selection logic. Owner-selected replacements
from game-icons.net (CC BY 3.0 — requires attribution): `crossed-swords` by
Lorc (melee), `high-shot` by Lorc (ranged), `fist` by Skoll (unarmed) — all
saved over the old hand-drawn `dnd_icon_attack_*.svg` files, `fill` switched
to `currentColor` to match this project's own icon convention. The `spell`
bucket (unreachable today — no spell attacks exist in `sheet.attacks` yet)
keeps its original hand-drawn sparkle rather than a game-icons.net pick, since
D&D Beyond's own spell icons vary by school and a single generic one would be
a step backward once that's built.

**A CC BY 3.0 credits page/footer is still owed** for these three icons —
tracked, not built this pass (no attributions surface exists anywhere in this
app yet, same gap noted when Flaticon was rejected for the dice-picker icons
earlier in the project).

**Eight school-of-magic icons were also prepared**, traced from an image the
owner supplied locally (`all schools and symbols.png` — the standard 5e
school glyphs, name printed under each). Cropped each symbol out
programmatically (alpha-channel bounding-box detection per grid cell, sized
2×4, splitting each cell's ink into a top "symbol" region and a bottom "text
label" region by finding the first ≥20px vertical gap), then traced each
crop into a clean SVG path with `potracer` (pure-Python potrace port — no
system-level potrace binary or ImageMagick was available in this
environment, `pip install`ed just for this session). Saved as
`dnd_icon_school_{school}.svg` for all eight (abjuration, conjuration,
divination, enchantment, evocation, illusion, necromancy, transmutation) —
verified visually against the source image before saving, all eight matched
exactly. **Not wired into any component yet** — `SpellRow.tsx` is the natural
consumer (`Dnd5eSpell.school` already exists), but that's a UI change to the
Spells tab this pass didn't touch; left for the owner to confirm before
building. Provenance of the source image (whether it's OGL/SRD-derived, fan
art, or something else) wasn't stated and is worth confirming before shipping
these — flagged, not resolved, since it's the owner's call as project owner.

This is a follow-up to the same day's "Attack row order and name/category
alignment fixed" entry below — that entry's `Map.copyOf` fix and the icon
swap here happened in the same session, verified together once Docker came
back up (see below).

Also recovered mid-session: Docker Desktop's engine had stopped (idle
timeout), which took Postgres/Keycloak/MinIO down with it; `docker compose
up -d` recreated the containers but the named volumes kept all data intact
(verified: Aria's character row, including this session's own JSONB patches,
survived the recreation untouched).

---

## 2026-09-03 — Attack row order and name/category alignment fixed

**Unarmed Strike rendered first instead of last, despite being seeded last** —
direct owner report. Root cause, found by comparing the raw JSON response
against the DB row byte for byte (`Object.keys` on a live `fetch()` of
`/api/characters/.../sheet` kept showing the reverse of the DB's own array
order): `Dnd5eSheetCalculator.attacks()` built its result in a
`LinkedHashMap` (already fixed once this session) but then returned
`Map.copyOf(result)` — the JDK's immutable `Map.copyOf`/`Map.of` have an
**unspecified, in-practice-randomized-per-JVM-run iteration order**, which
silently discarded the `LinkedHashMap`'s real order one line after building
it. `CharacterSheetResponse.from()`'s own `Collectors.toMap(..., (a,b)->a,
LinkedHashMap::new)` fix from earlier this session was correct but had
nothing correctly-ordered left to preserve by the time it ran. Fixed by
returning `Collections.unmodifiableMap(result)` instead — wraps without
rebuilding, so the `LinkedHashMap`'s order survives. Verified by directly
fetching the raw API response (bypassing the frontend and any caching)
before and after: attacks now render Longsword, Shortbow, Unarmed Strike,
matching seed/DB order exactly. `dnd-5e-sheet-build.md`'s "Attack row" note
about listing a universal attack last still holds — it's a data-authoring
convention, this fix is what makes the app actually honor it. The same
`Map.copyOf` pattern appears four more times in this file for skills/saving
throws — left alone, since the frontend reads those by fixed key lookup,
not by iterating map order, so they were never actually affected.

**The category subtitle sat indented relative to the attack name above it**
— direct owner report. Root cause: `.action-row__name`'s `<button>` carries
the browser's own default UA padding (`1px 6px`), which `.reveal` never
resets since its other consumers rely on more specific classes for that;
the plain `<span>` category line below it has no padding at all. Fixed with
`padding: 0` on `.action-row__name-cell .action-row__name`. Confirmed live
both bugs are fixed together — name and category now share the same left
edge, in the right order.

---

## 2026-09-03 — Fonts were never actually loaded (sheet-wide); Actions tab re-compared meticulously

**Root cause of "as fontes estão estranhas" (reported three times across this
session) found: `--font-body`/`--font-condensed` were declared but never
applied anywhere.** `getComputedStyle(document.body).fontFamily` was
literally `"Times New Roman"` — the browser's absolute default — because no
CSS rule ever set `font-family` on `body` (every component's own
`font-family: inherit` had nothing real to inherit from) and `index.html`
never loaded either Google Font. This affects the **entire sheet**, not just
Actions — every screenshot taken across this whole project has been
rendering in the wrong typeface. Fixed with one `body { font-family:
var(--font-body); }` rule (`frames.css`) plus a Google Fonts `<link>` in
`index.html` for Roboto and Roboto Condensed (400-700). Verified live: the
whole app, not only the Actions tab, now renders visibly differently — no
layout breakage found in the vitals zone, skills panel, Spells, or Features
tabs while spot-checking.

**A second, previously-unmeasured discovery while comparing D&D Beyond's own
CSS meticulously (direct owner request): almost everything on its sheet
renders in `Roboto Condensed`, not plain `Roboto`** — tab names, filter
chips, row names/subtitles/notes, column headers, section headings, even the
character's own name. Plain Roboto is reserved for a handful of large
numeric displays. New `--font-condensed` token (`ui-design-tokens.md`),
applied to the Actions tab's own shared labels: `.tab-bar__tab`,
`.filter-chip`, `.actions-tab__heading`/`.actions-tab__attacks-heading`/
`.actions-tab__manage-custom`/`.actions-tab__standard-list`,
`.action-row__name`/`__category`/`__range`/`__notes`, and the shared
Actions/Spells column-header block. Left `.action-row__description` (feature
descriptions — prose, not a UI label) on plain Roboto, matching D&D Beyond's
own split between UI chrome and body text.

**Third discovery, same live comparison: D&D Beyond's own roll-target
buttons use a system-UI font, not Roboto Condensed** — confirmed via the
shared button component behind both its Hit/DC and Damage columns. New
`--font-ui-native` token; `.action-row__value` corrected to use it, plus
`font-weight: 400` (was 600) and `padding: 3px` (was `4px 8px`, which was
already a same-week correction of the original `2px 4px` — see below for why
that first fix only treated a symptom).

**The Damage button was still cramped after last time's padding fix because
it was still rendering `attack.damageType` inside the button text** (e.g.
"1d8+3 slashing") — confirmed live D&D Beyond's own Damage column shows dice
notation only, no type word (kept in the button's `aria-label` and the
Entity Detail sidebar's existing "Damage Type" metadata, so no information
is lost). Removing it is what actually fixed the crowding *and* the
"spacing between columns" complaint — the oversized Damage button had been
pushing the Notes column around inconsistently row to row. The Hit/DC button
separately gained `.action-row__value--prominent` (20px font, fixed 42px,
`1px 6px` padding) — D&D Beyond deliberately renders Hit/DC larger than
Damage, confirmed live; Damage keeps the shared, smaller box.

**The filter chips/heading/search fields on every tab sat flush against the
tab bar's own bottom border** — direct owner report ("os filtros da aba
estão muito grudados com o nome da aba"). `.tabbed-section__content` gained
`gap: 16px` between the tab bar and its content, matching D&D Beyond's own
measured spacing there, rather than a per-tab top margin that would need
repeating on every tab's own first element.

**"Standard Actions" was also rebuilt this session** (own entry above this
one, same day) — see that entry for the "Actions in Combat" name-list
correction, which happened just before this typography pass.

Also corrected while re-measuring: `.action-row__range`'s text color was a
guessed muted gray — D&D Beyond's own is plain black.

Verified live across Actions, Spells and Features tabs (the shared classes
touched here ripple into both): no breakage, and Spells/Features both read
more correctly too now that the real fonts load. `tsc` and the frontend
production build are both clean. No backend changes this pass.

Left out, flagged for the owner rather than assumed: the rest of the sheet
(vitals zone, Spells/Inventory/Features/Background/Extras tabs, the sidebar)
was never audited against the "fonts actually load now" baseline — worth a
fresh comparison pass now that Roboto/Roboto Condensed genuinely render,
since anything tuned by eye against the old broken fallback font could be
off in ways this pass didn't touch. Also left out: `SpellRow.tsx`'s Effect
button has the identical damage-type-word-in-the-button issue Attack's
Damage button had — not fixed here, out of this pass's stated Actions-tab
scope, but the same fix (drop the word, keep it in metadata) would apply.

---

## 2026-09-03 — Standard Actions rebuilt as a name list; roll-target boxes widened

**"Standard Actions" was rebuilt as "Actions in Combat", following a direct
owner correction**: the previous session's audit had wrongly re-confirmed
that D&D Beyond renders each standard action as its own row with a full
inline description (the same guess going back to this audit's original
pass) — the owner reports live D&D Beyond behavior directly contradicts
that: a single flowing, comma-separated line of action names ("Attack,
Dash, Disengage, ...") with no inline description at all, each name opening
the sidebar on click. `ActionsTab.tsx`'s static `STANDARD_ACTIONS` section
now renders exactly that — a `<p>` of `.reveal` buttons separated by ", ",
each opening the Entity Detail mold with its description — replacing the
one-row-per-action-with-inline-text layout. `dnd-5e-sheet-fidelity-audit.md`'s
stale "Match — no action needed" verdict is struck through and corrected in
place rather than deleted, so the wrong-guess history stays visible.

While rebuilding that section, also corrected `.actions-tab__heading`
(shared by this section and Features) to D&D Beyond's own measured style
for "Actions in Combat" from the previous session's inspection that went
unapplied: plain black, not uppercase (was muted gray, uppercase) — only
the Attacks section's own heading keeps the accent-colored, uppercase,
divided treatment, confirmed distinct from this one.

**The Hit/DC and Damage roll-target boxes (`.action-row__value`) had their
text sitting too close to the border** — direct owner report. Root cause:
`padding: 2px 4px` plus the browser's default `line-height: normal` left
almost no vertical breathing room around a 13px line. Widened to
`padding: 4px 8px` and set `line-height: var(--line-height-ratio)` (this
file's own token for exactly this, `ui-design-tokens.md`) instead of a
guessed pixel value. Not a straight port of D&D Beyond's own box (20px
font, 42px fixed width, 36px fixed row height for its Hit/DC column
specifically) — this app's rows are deliberately more compact, so only the
breathing-room fix was applied, not the larger scale.

Verified live in the running app: roll boxes have visibly more room around
their text, "Actions in Combat" shows the flowing name list, and clicking
"Attack" opens the sidebar with "Make one melee or ranged attack." `tsc`
and the frontend production build are both clean. No backend changes this
pass.

---

## 2026-09-03 — Actions tab attacks section restructured to D&D Beyond's layout

**The Attacks section's heading, row icons, category subtitles and between-row
dividers were rebuilt from a live DOM inspection of D&D Beyond's own
`.ct-actions__attacks-heading`/`.ddbc-combat-attack`**, following up the same
day's filter-chip/tab-bar fix. Four structural gaps, all confirmed live before
building:

1. **Subsection heading.** The Attacks section now shows "ACTIONS • Attacks per
   Action: 1" (two weights/colors on one run — "ACTIONS" 13px/700/uppercase in
   the new `--accent-control` blue-gray, the rest 13px/400/black/normal-case)
   with a "Manage Custom" link right-aligned on the same line, and a
   `1px solid #EAEAEA` divider (new `--border-divider` token) sitting directly
   against the heading's own bottom edge — confirmed this treatment is unique
   to the Attacks section; D&D Beyond's "Actions in Combat" and per-feature
   headings are plain black with no divider, so `.actions-tab__heading` (used
   by Standard Actions/Features) was left alone and a new
   `.actions-tab__attacks-heading` covers Attacks only. Also corrected: no
   second divider under the column header row (D&D Beyond has none there
   either — `.action-row--header`'s old `border-bottom` was a stale guess).
   "Attacks per Action" is a hardcoded `1` (this app doesn't model
   multiattack) and "Manage Custom" opens an informational sidebar panel (no
   custom-action feature exists) — both recorded in `dnd-5e-sheet-ui.md`'s
   Deviations list.
2. **Row icon and category subtitle.** New `Dnd5eAttack.category` field (free
   text, same treatment as `damageType`/`notes`) threads through `AttackRow`/
   `AttackRowResponse`/`Dnd5eSheetCalculator`, authored for both of Aria's
   attacks ("Melee Weapon", "Ranged Weapon"); the one dev-seeded character's
   persisted JSONB needed a manual patch for the new field, same story as
   `specialSenses` before it. `category` renders as a 10px muted subtitle
   under the attack name (which dropped from 700 to genuinely 400 weight,
   matching D&D Beyond exactly now that the subtitle carries the visual
   weight instead) and selects the row's leading icon via a keyword match —
   four new original flat-silhouette SVGs (`dnd_icon_attack_melee/ranged/
   spell/unarmed.svg`, this project's own icon style, not reproduced from
   D&D Beyond's artwork).
3. **Between-row dividers.** Replaced a uniform solid `border-bottom` on every
   row (including the last, which D&D Beyond doesn't divide) with
   `border-top: 1px dotted var(--border-divider)` skipped on each list's
   first row — matches D&D Beyond's own `.ddbc-combat-attack` measured live
   (8px margin + 8px padding around the dotted line, absent on row one).
4. **Filter chips/tab bar** (same day, first pass of this fidelity check):
   pill shape and outline-only colors corrected to D&D Beyond's square,
   solid-fill badges with a real hover state — see this file's own earlier
   entry today for the full value list.

Backend: `Dnd5eAttack`/`AttackRow`/`AttackRowResponse` all gained `category`;
every constructor call site (calculator, seeder, three test files) updated
together, `:apps:api:test` green. Frontend: `tsc --noEmit` clean. Verified live
in the running app after restarting the API (a `bootRun` process doesn't
pick up recompiled classes on its own) and patching the dev character's
JSONB: icons render distinctly, category shows in both the row and the
Entity Detail sidebar, "Manage Custom" opens its panel, row dividers and the
heading divider both match D&D Beyond side by side.

Left out: D&D Beyond's own "Actions in Combat" comma-list-of-names structure
(no per-action rows, no descriptions inline) doesn't match this app's
Standard Actions section (individual rows with full descriptions) — a real,
previously undocumented structural gap surfaced while inspecting this
section, but out of scope for this pass since it needs an owner decision
(rebuild to match, or keep as a documented deviation) rather than a guess.
Also left out: extending `category`'s icon set to spell attacks in practice —
Aria's two seeded attacks are both weapons, so the spell/unarmed icons exist
but are unexercised by any current seed data.

---

## 2026-09-03 — Filter chips and tab bar corrected to D&D Beyond's square badges

**`FilterChips.tsx` and `TabBar.tsx` were styled as rounded pills with no hover
state — DOM-measured live against dndbeyond.com/characters/50149479's Actions,
Spells and Features filter rows and its tab bar, both were wrong.** D&D
Beyond's filter chips (all/attack/action/bonus action/reaction/other/limited
use on Actions; the level chips plus Concentration/Ritual toggles on Spells;
all/class features/species traits/feats on Features) are square badges —
`border-radius: 4px`, matching every other control's `--radius-control`, not
`999px`. Every chip carries a solid fill rather than an outline: inactive is
a light gray fill with muted blue-gray text, active is a mid blue-gray fill
with near-white text — the opposite of this app's transparent-fill-plus-ring
treatment. Also corrected: font-size 9px → 12px, padding `4px 10px` →
`2px 5px`, no letter-spacing (was `0.36px`), gap `6px` → `4px`. Hover didn't
exist before this pass — captured live via a real `:hover` state, not
guessed: an inactive chip darkens to `#525C63`, and — a genuinely separate
value, confirmed by re-measuring after a 1s wait to rule out a mid-transition
read — an already-active chip hovers to the *inactive* chip's own text color
instead of the same hover tone. The tab bar's own gap (10px guessed → 16px
measured) and active underline (`2px solid var(--frame-ink)` → `3px solid`
a new `#92A2B3` accent — the same tone the active filter chip now uses)
needed the same live-measurement treatment; hovering an inactive tab was
confirmed to produce no computed-style change at all, so no tab hover rule
was added. `sheet/SearchField.tsx`'s search input had the same `999px` pill
mistake, corrected to the same 4px radius against D&D Beyond's own
`.ct-spells-filter__box`.

Four new tokens in `frames.css`'s `:root` (`--accent-control`,
`--accent-control-hover`, `--surface-control`, `--text-control-muted`) carry
the measured colors — recorded in `ui-design-tokens.md`. Both components
moved from inline `style={{}}` objects to CSS classes (`.filter-chips`/
`.filter-chip`, `.tab-bar`/`.tab-bar__tab` in `frames.css`), keyed off the
`aria-pressed`/`aria-selected` attributes they already carried, since hover
and active states can't be expressed as plain JS style objects. Verified
live in the app after the change: chips and tabs across Actions, Spells and
Features all render and hover correctly, matching D&D Beyond side by side.
See `dnd-5e-sheet-build.md`'s "Tab bar"/"Filter chip row" rows for the full
value list.

Left out: the search input's other properties (font-size, padding, border
color) — already close to D&D Beyond's own and not part of what was asked
this pass, which was specifically the filter/tab shape.

---

## 2026-08-18 — Owner's own final `.inspiration::before` shape documented; full-bleed header bar; opaque d20 trigger icon

**`.inspiration::before` docs synced to the owner's own final clip-path.**
The seven-iteration comment block above that rule (this file, `frames.css`)
described a `path()`/pixel-scan approach that's no longer what's actually
there — the owner hand-adjusted the `clip-path` directly to its final,
correct shape (a `polygon()` tracing the medallion's two carved side straps)
after the seventh attempt still wasn't right. Trimmed the comment down to
describe *that* shape instead of walking through every rejected one; the
full rejected-shape history stays in this changelog (search "inspiration"
above) rather than being deleted, per this file's own stated convention of
keeping CSS comments concise and pointing here for the full story.

**The header bar's dark background now reaches the page's left/right
edges, full-bleed, matching D&D Beyond's own character header** —
confirmed live there via DevTools. `SheetShell`'s `width: fit-content;
margin: 0 auto` wrapper (its own doc comment, unchanged) means
`.sheet-header` itself can't just get `width: 100vw` without either
fighting that centering or forcing the wrapper to size itself to the
viewport. Standard full-bleed break-out instead: `background` moved off
`.sheet-header` itself onto a new `::before` using the `left: 50%` /
`translateX(-50%)` / `width: 100vw` trick, which centers a 100vw box on the
viewport regardless of how deeply the real element is nested — painted
behind the header's own children since a `::before` paints first, no
`z-index` needed (same reasoning as `.frame-box::before` elsewhere in this
file). `border-radius` dropped from the header — a rounded corner only
reads as a corner against a different background outside it, and there
isn't one anymore once the bar is page-wide. Confirmed live at a viewport
wide enough for the sheet's own ~1200px column to fit without horizontal
scroll: bar reaches both edges, header content stays exactly where it was.
Left as-is for narrower viewports where the column itself already
overflows and scrolls horizontally — `SheetShell`'s own comment already
documents that this sheet has no narrow-viewport story yet, unrelated to
this change.

**`dnd_icon_dice_roll.svg` (the bottom-left custom-roll-picker trigger, a
loose d20 outline with `.custom-roll-picker__trigger`'s own
`background: none`) gained a white backing circle**, direct owner request:
a `<circle cx="300" cy="306" r="290" fill="#ffffff"/>` inserted as the
SVG's first child, painting behind the d20's own `fill="currentColor"` line
art (an SVG element rather than a CSS background, so it scales with the
rest of the icon's viewBox instead of needing separate positioning).
Without it the die's outline had nothing opaque behind it, so the page
showed straight through every gap between facet lines. Confirmed live by
swapping the page's own background to magenta as a throwaway contrast
check (reverted after) — a solid white disc under the icon, not transparent
gaps.

**Custom roll picker's die-select grid gained an icon above each `d4`/
`d6`/.../`d100` label, matching D&D Beyond's own die buttons.** The owner's
own reference was a Flaticon polyhedral-dice set, but Flaticon's free tier
requires author attribution unless the account is Premium, and this app has
no attributions page anywhere to put one — the same reasoning that picked
Apache-2.0 Material Symbols for `SheetShell`'s game log icon earlier in this
project. Checked for a CC0/MIT/Apache polyhedral-dice icon set as an
attribution-free alternative; nothing turned up (closest hit: an open,
still-unresolved `google/material-design-icons` feature request asking for
exactly this). Owner's call, given the search came up empty: six original
flat silhouette icons instead of any borrowed set —
`dnd_icon_die_d4/d6/d8/d10/d12/d20.svg`, plain `fill="currentColor"`
polygons in the same style as this file's other icons (triangle, rounded
square, diamond, a narrower/taller diamond for d10, pentagon, hexagon — the
shape language most dice-icon sets converge on). `D100` reuses the d10 icon
rather than getting a seventh shape of its own: visually a percentile die
*is* a d10, and every die here already leans on its adjacent text label
rather than an embedded face number, so d100 does the same.
`CustomRollPicker.tsx` gained a `DIE_ICONS` lookup and renders
`<FrameIcon>` above each button's label; `.custom-roll-picker__die` in
`dice.css` went from a fixed `height: 32px` to a flex column with a new
`.custom-roll-picker__die-icon` (16px) above the text, so the button's own
height still tracks its (now taller) content instead of a hardcoded number.
Confirmed live: all seven buttons show a distinct, legible shape, the count
badge still sits correctly over the corner, and click/right-click
increment/decrement are unaffected.

---

## 2026-08-18 — Header content regression fixed; dice tray dismisses per-entry with a fade

**The full-bleed header change above (same day) hid the portrait, name,
level, rest buttons, and game log button** — the owner reported them gone
entirely, not just hard to read. Root cause: `.sheet-header::before`
(`position: absolute`, the new full-bleed background) had no `z-index`,
reasoning it would paint behind the header's other children the same way
`.frame-box::before` does elsewhere in this file — wrong. `.frame-box::before`'s
siblings (`FrameLayer`'s two spans) are *also* `position: absolute`, so among
same-step positioned elements DOM order decides, and `::before` being first
wins. This header's children are plain static, in-flow boxes — a different
CSS stacking step entirely: positioned elements with `z-index: auto` paint
*after* static in-flow content regardless of DOM order, so the dark
`::before` was covering the portrait/name/buttons, not sitting behind them.
Fix: `z-index: -1` on `.sheet-header::before` — `.sheet-header`'s own
`position: relative` already makes it a stacking context, so `-1` here is
relative to that context's own background, not the page root. Confirmed
live: full-bleed bar still reaches both edges, all header content visible
again.

**The dice tray (bottom-left, the most recent roll result) now dismisses
per-entry with a fade**, direct owner request: clicking it fades the
current entry out and leaves it hidden, without deleting anything from the
actual roll log. `DiceTray`'s `latestRoll` prop is `rollHistory[0]` from
`CharacterSheetScreen` — the same array the game log panel reads — so
dismissal is new, purely local component state (`dismissedKey`), never
touching `rollHistory` or calling anything server-side. Keyed to the
current entry's own identity (`latestRoll.id`, or the error string itself
when showing an error) rather than a plain boolean, so the next roll or
error — a different key — always starts undismissed automatically, no
explicit reset needed. The tray became a real `<button>` (was a plain
`<div>`) per dnd-5e-sheet-build.md's "every clickable is a button", disabled
when there's nothing to dismiss (the empty "No rolls yet" state has no
key) rather than a button that does nothing when pressed. `dice.css` gained
`transition: opacity 300ms ease` on `.dice-tray` and a
`.dice-tray--dismissed { opacity: 0; pointer-events: none; }` toggled by
the new class. Confirmed live: clicking a tray entry fades it from the
screen, a fresh roll immediately after shows normally (not still
dismissed), and the dismissed entry is still the top row in the Game Log
panel throughout.

---

## 2026-08-18 — Re-tuned header-overlap patches and a raster-safe recolor path after the owner's SVG re-crop

The owner re-cropped and updated the frame SVGs to fix a white-background
leak at each asset's own edges (their own fix, not made here). That re-crop
changed each asset's top-corner artwork enough to break two things that were
previously tuned against the old geometry, plus exposed a pre-existing gap:

**Ability boxes rendering solid black instead of the theme's ink color.**
`dnd_frame.svg` (used by `AbilityBox` for every ability score box) isn't
real vector art — its own `<desc>` says so: "Exact original colors and
transparency preserved by embedding the source PNG inside SVG." It's a
raster PNG wrapped in an `<svg><image href="data:...">` tag, with no
`fill="currentColor"` path for the paper/ink two-tone technique
(`FrameLayer.tsx`) to recolor — confirmed live by rendering the embedded PNG
against a magenta debug background: flat black line art, fully transparent
everywhere else, completely unresponsive to `--frame-paper`/`--frame-ink`.
Fix: `svgUtils.ts` gained `extractRasterImage()`, and `FrameLayer` now
detects a raster-backed SVG and falls back to a CSS `mask-image` (this
codebase's pre-phase-10 technique) painted with `background-color:
currentColor` for just that asset, so it picks up the same
`--frame-paper`/`--frame-ink` colors every vector-based frame already uses.
New `.frame-box__paper--raster`/`.frame-box__ink--raster` rules in
`frames.css`. Vector-based frames are unaffected — they still render as
genuine inline SVG.

**Hit points and heroic inspiration leaking white past their own corners.**
`.hit-points::before`'s deep custom clip-path (70px-wide, 50px-tall corner
cuts) and the shared `.frame-box::before` default (14px chamfer, applied to
`.inspiration` since it had no override) were both tuned against the
pre-crop artwork. Re-measured live and precisely this time, instead of
eyeballing debug-red screenshots: rasterized each `.frame-box__ink` SVG to
an offscreen canvas at 10x supersampling and scanned, per column, for the
first non-transparent pixel from the top, giving an exact "how far does the
ink line pull back from the box's own corner" profile per asset.
`dnd_frame_hit_points.svg`'s corner pull-back is now only ~11px wide/~9px
deep — comfortably inside the shared default's 14px chamfer, so the
`.hit-points::before` override was deleted outright rather than re-tuned.
`dnd_frame_inspiration.svg`'s top corners no longer pull back at all (flush,
sharp corners) — the shared 14px chamfer was cutting a corner that isn't
there, leaking a triangle of dark header through at each top corner. Fix:
new `.inspiration::before { clip-path: none; }`, a plain uncut rectangle.

Left out: no attempt to hand-vectorize `dnd_frame.svg`'s artwork into real
paths — the raster/mask fallback fully resolves the visible bug, and
re-tracing it wasn't asked for. If a future re-export of that asset ships as
real vector paths with `currentColor` fills, `extractRasterImage()` will
simply stop matching and `FrameLayer` falls through to the normal inline-SVG
path automatically — no further code change needed.

**Same-day follow-up: an unwanted hover state, and the inspiration patch
still leaking white through the medallion's own carved gap.** The owner
flagged two more things after the above: (1) `.inspiration:hover,
.inspiration:focus-visible { filter: brightness(0.9); }` was darkening the
whole box on mouse hover — not part of the fix above, just noticed
alongside it — removed outright (keyboard focus still gets a visible
outline from the shared `.mutate:focus-visible` rule, so nothing lost there);
(2) the plain-rectangle fix for `.inspiration::before`'s corners (above)
was too plain — `dnd_frame_inspiration.svg`'s sunburst medallion has two
carved wing shapes with a genuine open gap between them across the *middle*
of the top edge, and a flat rectangle painted solid paper straight across
that gap instead of leaving it open, which reads as white leaking through
the ornament. Re-measured the same way (10x-supersampled canvas, per-column
first-opaque-pixel scan) but this time across the full width instead of
just the two corners, and replaced `clip-path: none` with a polygon that
traces the two wing dips (~5.6% of box height deep) and the shallow
center notch (~1.5% deep) directly from that scan.

**Second same-day correction: the middle should be one smooth concave dip,
not a peak.** The owner flagged the polygon above as wrong — the center of
the top edge should read as concave (a valley), not the small peak the
previous polygon put there. Root cause: the medallion's raised diamond
ornament pokes its own tip back up almost to the rim (measured ~1.5% deep,
next to ~5.6% on either side of it), and the previous polygon chased that
tip with a matching peak in the *clip-path itself* — technically accurate to
the scan, but it put a sharp convex point in the middle of the cut instead
of a smooth curve. Since `.inspiration::before` paints *behind* both SVG
layers, the diamond tip's own opaque ink hides the difference regardless of
whether the patch chases it — so there's no real cost to ignoring that one
detail. Replaced the three-point zigzag with a single smooth arc, cut
uniformly deeper through the center (down to the same ~7% the side dips
already measured), confirmed live against the real dark header with
`document.body.style.zoom` at 4x: clean concave valley, no leak anywhere
along it, corners unaffected.

**Third same-day change (explicitly experimental — "tente mudar," not a
measured correction): the center dip deepened to half of `.inspiration`'s
own height.** `.inspiration::before` gained `height: 50%`, so the
pseudo-element itself is now half the box's height (26px of 52px) instead
of the shared 14px sliver, and the deepest point of the cut was pushed down
to land exactly on that half-height mark.

**Fourth same-day change: `polygon()`'s straight segments read as separate
"holes" at this new depth.** A dozen sampled points was fine for the
previous ~7%-deep dip — too shallow for the facets between points to read
as anything but a curve — but at half the box's height each straight
segment became visible as its own distinct notch, which the owner flagged
as "buracos no interior" (holes inside the frame) rather than one smooth
descent. Switched `clip-path` from `polygon(...)` to `path(...)`: a real
two-segment cubic-Bezier curve instead of sampled straight lines, smooth by
construction with no point count to tune. Coordinates are absolute px
against `.inspiration`'s fixed 70×52 size (`path()` doesn't take
percentages) — flat from x=0 to x=14, an S-curve down to the valley floor
at (35, 26), a mirrored S-curve back up to flat at x=56 through x=70, with
horizontal tangents at every join so neither the rim/curve transition nor
the valley floor itself has a sharp corner. Confirmed live with a
cloned-and-scaled `.inspiration` box against its own real header color
(`rgb(35, 43, 47)`, read from `.sheet-header`'s computed style) and the
real `-8px` overlap (`Dnd5eVitalsTopRow`'s own margin): smooth valley
across the full width, both corners and the center dip, no leaks, no facets.

**Fifth same-day change: the smooth curve was still wrong — it was invented,
not traced.** The owner asked for the concave cut to follow the medallion's
own concave drawing instead of a hand-built bowl shape. Re-ran the same
10x-supersampled canvas / per-column first-opaque-pixel scan used earlier in
this block, but dense (roughly every 2% of width) across the full 20%-80%
inner span in one pass, then multiplied every sampled depth by one scale
factor — the scan's own ~5.77%-of-height maximum mapped onto the 50% this
block targets — so the real proportions carry through at the new size,
including the shallow re-ascent at x=50% where the diamond ornament's own
tip pokes back up (a barely-there ripple at the old ~7% depth, now a
visible narrow gap between two deep cuts). Went back from `path()` to
`polygon()`: with real, closely-spaced sample points the straight segments
between them are too short to read as facets — the "holes" problem three
changes ago was sparse points on an *invented* curve, not straight segments
as such — and a plain point list is a more direct trace of "the real scan"
than fitting Bezier control points to it would be. Confirmed live the same
way as the fourth change: clean against the real header color, no leaks,
no facets, and this time an accurate scaled copy of the actual artwork
rather than an approximation of it.

**Sixth same-day change: still not right — the owner asked for the
concavity to be steeper ("mais inclinada").** The fifth change traced the
scan's x-positions as literally as its depths, which spreads the whole dip
(both wing floors plus the shallow center re-ascent) across the real ~28px
it occupies in the artwork — against a 26px-tall pseudo-element that reads
as a wide, shallow-walled dip rather than a steep one, even with the depth
itself at 50%. Kept the same 21 sampled depths in the same order (the real
proportions are still exactly what the fifth change measured — this isn't
a return to an invented shape) but respaced them over a 14px span instead
of 28px: half the horizontal run for the same vertical rise, so the outer
walls that were a gentle ~2.6px of x per full-depth step are now a steep
~1.3px. The dip's own edges moved in from x=21px/49px to x=28px/42px to
match. Confirmed live the same way as the last two changes: clean against
the real header color, no leaks, and visibly steeper/narrower than the
fifth change's version.

**Seventh same-day change: the owner asked for the incline to be smooth
("deixe a inclinação ser suave").** Squeezing all 21 real sample points —
including the scan's own small non-monotonic wobbles, fine texture at the
original 28px span but too coarse a stair-step once packed into 14px —
produced a steep zigzag rather than a steep curve. Kept only the shape's
essential structure from the sixth change (flat rim to x=28px, a wing up to
full depth near x=30.5px, back down to a shallow center notch at x=35px,
mirrored out to a second wing and back to flat at x=42px) and connected
those points with `clip-path: path(...)` cubic Beziers instead of the 21
straight segments, so the same steep, twin-wing profile — still the
artwork's own shape, not a generic bowl — descends and rises smoothly
instead of in visible steps. Confirmed live the same way as the last three
changes: clean against the real header color, no leaks, steep, and smooth.

---

## 2026-08-17 — Frame boxes get an opaque top strip, not a full backing

Same-day follow-up, corrected twice. First attempt (below the line, kept
for the record): the owner reported the header-overlap fix looked
"estranho" — `dnd_frame.svg`'s ornamental scrollwork corners have genuinely
transparent negative space (the carved-wood relief look), which normally
just shows the light page behind it; pulling the ability-score row up over
the dark header meant those gaps showed dark instead. First fix gave
`.frame-box` a full-box `background: var(--frame-paper)`.

**Second correction, same day:** the owner flagged that full-box background
as wrong too — "agora temos um fundo branco no SVG inteiro, isso simboliza
partes brancas escapando a moldura" — and asked whether a geometric patch
like `.saving__prof`/`.skill-row__prof`'s existing "opaque circle over one
exact spot" technique had a square/rect equivalent. Investigated live with
a temporary bright-red debug background injected on `.frame-box`: red
visibly escaped past the wreath's own curled corners at the sides and
bottom — the ornament's true silhouette is narrower than its rectangular
bounding box there, confirming the full-box fill was always going to bleed
past the art at those edges (harder to see in white against the near-white
page, obvious in red). The same debug technique showed the box's *top*
edge, unlike its sides and bottom, actually is a straight rectangle in
every asset checked — a `height: 10px` debug strip (covering the header's
8px overlap plus a little slack) showed zero escape anywhere.

Second fix: `.frame-box::before` (`frames.css`), a 10px-tall opaque strip
across just the top edge, `var(--frame-paper)`, painting behind the two
real SVG layers (a `::before` is always the first child, so no z-index
needed) — the same "small, precisely-placed opaque patch" technique as the
saving-throw/skill proficiency circle, sized to the one region that
actually needs it instead of the whole box.

**Third correction:** the owner looked again and could still see the dark
header behind the white patch. The plain rectangle's own sharp corners were
the reason — this checklist's own debug-red technique from the previous
attempt was re-run on the strip itself rather than the whole box, and it
showed why: the frame art's top corners are *chamfered* (cut at a diagonal,
not a square corner), so a plain rectangular patch's own square corners
poked past the chamfer, leaving a small triangular gap at each top corner —
exactly the sliver the owner spotted. Fixed with `clip-path: polygon(20px
0, calc(100% - 20px) 0, 100% 20px, 100% 100%, 0 100%, 0 20px)` on the same
`::before` (height bumped to 20px so the chamfer has room to read as a
diagonal, not a near-vertical sliver) — the chamfer size (20px) was tuned
empirically against the debug-red rendering until nothing escaped past the
ink line, then checked against every frame type in the header-overlapping
row (ability boxes, the proficiency/speed badge, heroic inspiration, hit
points), not just the ability box that first surfaced the issue.

Still global (every panel uses `.frame-box`), still invisible everywhere
except the header-overlap case, since it only touches each box's own top
20px and every other panel's sides/bottom stay exactly as transparent as
before. Verified live, zoomed to individual corners: no dark bleed-through
at any of the six ability boxes' top corners, the proficiency/speed badge,
heroic inspiration, or hit points; every other framed panel
screenshot-compared unchanged. No `tsc` run needed (CSS-only change, no
component touched).

**Fourth correction, same day:** the 20px/20px patch covered enough that
its own bottom edge sat visibly below the frame art's own top border,
poking a sliver of white into the box's normally-transparent interior —
the owner asked for the height trimmed down. Reduced to 14px/14px (`height`
and both `clip-path` chamfer offsets), re-verified against the same
debug-red technique: still zero escape past the ink line at every corner
in the header-overlapping row, and the patch's own bottom edge now sits
right at the frame's own top border instead of past it.

**Fifth correction, same day — the chamfer itself was the wrong shape.**
The owner still saw a white line escaping. An 8x CSS `transform: scale()`
render of one ability box (a cleaner way to inspect fine edges than
zooming into a screenshot crop, which risks reading JPEG compression noise
as real pixels) showed why: the frame art's top corners aren't a straight
diagonal chamfer at all — the ink curls into an organic hook shape.
`clip-path` polygons only ever approximate that with straight lines, so
they either cut into the curl (dark shows behind the cut) or missed part
of it (white past the ink) — no polygon was going to match a hand-drawn
curve exactly.

The actual fix: shrink the patch to exactly `8px` tall — precisely the
header's own overlap, no slack — with **no `clip-path` at all**. Short
enough that the patch never reaches down into the corner curls in any
asset in the row, so there's no corner to mismatch in the first place.
Re-verified live (regular zoom, corner by corner) against all six ability
boxes, the proficiency/speed badge, heroic inspiration, and hit points —
clean everywhere, no dark bleed-through, no white escape. Simpler than
every previous attempt, and the one that actually held up.

**Sixth correction, same day — this was per-frame, not one universal
shape.** The owner reported the plain 8px strip looked worse and manually
reverted `frames.css` to the 14px `clip-path` chamfer version. Re-tested
that version with `document.body.style.zoom` (a true reflow zoom, more
trustworthy than a screenshot crop for fine edges — the CSS `transform:
scale()` trick tried earlier turned out to give misleading results,
apparently from how it interacts with `clip-path` on scaled elements) and
found the 14px chamfer was already clean on the ability boxes, the
proficiency/speed badge, and heroic inspiration — the escape the owner was
still seeing was `.hit-points`' own frame (`dnd_frame_hit_points.svg`)
specifically, which cuts its top corners much deeper than the other three
assets. A uniform deeper chamfer wasn't an option either: deep enough to
close the hit-points gap (tested up to 55px) buried "CURRENT"/"MAX" under
white well before reaching the corners.

Fix: kept the 14px shared chamfer as the default (right for three of the
four frames in the row), and added a `.hit-points::before` override with
an 8-point `clip-path` — shallow (10px) across the flat middle of the top
edge, deep (50px) only in two 70px-wide zones at each corner, sized
empirically against the debug-red rendering until nothing escaped past the
ink line at either corner. Verified live at `zoom: 3`, corner by corner,
across every frame in the row — including hit points now — all clean.

---

## 2026-08-17 — Ability score row overlaps the header bar

Direct owner request, same-day follow-up to the header restyle below: on
D&D Beyond, the ability-score row starts slightly above the dark header
bar's own bottom edge rather than flush against it. Confirmed live
(`getBoundingClientRect` on `.ct-character-header-desktop` and
`.ct-quick-info__ability`): 8px of overlap. `Dnd5eVitalsTopRow.tsx`'s own
row div gained `marginTop: '-8px'`, pulling it up into `SheetShell.tsx`'s
header — each ability box's own opaque frame artwork (`dnd_frame.svg`'s
paper layer) covers the dark bar within the overlap, same as the
reference's own boxes do. `tsc --noEmit` clean; verified live:
`getBoundingClientRect` on `.sheet-header` and `.ability` now reports the
same 8px overlap, and a zoomed screenshot shows the ability boxes' top
edges sitting on the dark bar exactly like the reference.

---

## 2026-08-17 — Character header: the CSS behind an already-written restyle

`SheetShell.tsx` carried a detailed doc comment dated 2026-08-17 describing a
restyle of the character header to match D&D Beyond's own — dark bar,
light text, restyled rest buttons, icon-only game log — with specific
values (`#232b2f`, `1px solid #92a2b3`, `3px` radius, `5px 13px 4px`
padding, a 16×16 icon). The JSX already used `sheet-header*` class names
throughout. But no CSS rule for any of them existed anywhere in the
codebase (confirmed by grep) — the header was rendering as unstyled
default HTML the whole time, which is exactly what this session's own live
screenshots showed before this change. Picked up as "the work started for
the character header."

Live-measured the remaining values against `.ct-character-header-desktop`
and its children (`dndbeyond.com/characters/50149479`, DevTools computed
styles) rather than estimating: header `height: 100px`, `padding: 10px
20px`; portrait `60×60`, `border-radius: 3px`, same `2px solid #92a2b3`
border as the buttons; name (`<h1>`) `24px/400/white`; level line
`13px/400/white`; gap between portrait and name block `15px`; gap between
action buttons `20px` (D&D Beyond's own button groups touch at 0px, but
each button has its own inset, netting 20px between the rendered pills).

Added the `CHARACTER HEADER` section to `frames.css` with all of the
above. `.sheet-header__actions` uses `margin-left: auto` (not
`justify-content: space-between` on the parent, which would have also
inserted the same gap between the portrait and the name block — this app's
header has 3 flex children, D&D Beyond's own tidbits group is a single
child so `space-between` works differently there) to push the button row
right while the portrait/name pair keeps its own tight `15px` gap.
`.rest-button__icon` (already existing, shared by Short Rest/Long Rest/Game
Log) went from an estimated `14px` to the live-measured `16px` — no other
consumer of that class exists.

No portrait image: this app has no portrait upload/storage yet (a phase 11
concern, already documented elsewhere), so `.sheet-header__portrait` is a
plain bordered placeholder box, not an image. `tsc --noEmit` clean (CSS
only, no component changes); verified live: `getComputedStyle` on
`.sheet-header`, `.sheet-header__portrait`, `.sheet-header__name`,
`.sheet-header__button` and `.rest-button__icon` all report the values
above exactly, and a side-by-side screenshot shows the dark bar, portrait
box, name/level and restyled Short Rest/Long Rest/Game Log buttons matching
the reference's own layout.

---

## 2026-08-17 — Manual/custom dice roll picker

Picked up the manual rolling item `dnd-5e-sheet-ui.md`'s "Deferred to later
versions" had explicitly flagged as its own future slice, triggered by the
owner noticing `dnd_icon_dice_roll.svg` (supplied during the phase 10 asset
refactor) was never wired up. Verified live against D&D Beyond before
building: a d20 icon opens a "Roll Dice" panel with one button per die type
(d20/d12/d100/d10/d8/d6/d4), left-click increments a count badge (no visible
cap — reached 32), right-click decrements; RESET zeroes the selection; a
separate CLEAR DICE control did not change the selection when clicked live
with dice already picked, so only Reset was built (see the new "Deviations
from D&D Beyond" entries in `dnd-5e-sheet-ui.md`, alongside the other
deviation added: this app caps each die type at 20, since D&D Beyond showed
no limit and the server needs one).

**Backend** (`dev.omnisheetvault.api.dice`, no migration — `Roll`'s existing
columns are generic enough for a combined multi-die-type roll): new `DieType`
enum (D4..D100, package-local — pure dice vocabulary, not tied to any game
system, unlike `ruleset.RollKind`); `ManualRollRequest`/`DiceGroupRequest`
records (Bean Validation only: count 1–20, no new domain exception);
`RollService.manualRoll` rolls each requested die type in `DieType`'s own
declared order regardless of request order, concatenates results, builds an
expression like `"2d20 + 3d6"`, and persists with context `"Custom: roll"`
(the literal example `features/character-sheet.md`'s manual-rolling rule
already gave); new `POST /api/characters/{id}/rolls/manual` endpoint,
mirroring the existing roll endpoint's shape.

**Frontend**: new `dice/CustomRollPicker.tsx` (sibling to `DiceTray.tsx`, not
folded into it — `DiceTray` stays scoped to "show the latest roll"), rendered
next to it in `CharacterSheetScreen.tsx`, both fixed to the same bottom-left
corner. Right-click-to-decrement stays on the same real `<button>` per die
type (`dnd-5e-sheet-build.md`'s "every clickable is a button" — a second
gesture on a real button, not a hidden non-button target), with
`aria-label`/`title` covering both gestures since right-click has no visual
affordance of its own. No 3D dice — results render through `DiceTray`'s
existing text display unchanged, per the owner's explicit reminder that 3D
rendering stays deferred.

Fixed in passing: `CharacterSheetScreen.tsx` imported `gameLogIconSvg` but
never passed it as `SheetShell`'s required `gameLogIcon` prop — a pre-existing
break (`tsc --noEmit` was failing before this change touched anything), found
while verifying this slice's own type-check. Wired it through with the same
`FrameIcon`/`rest-button__icon` pattern the short/long rest icons already use.

`ui-design-tokens.md`'s "Frame assets" and "Still unmeasured" sections were
also brought up to date this session (unrelated to this slice): they still
described the pre-phase-10 CSS-mask technique and listed Hit Points/Heroic
Inspiration as frameless, both stale since the phase 10 asset refactor.

`tsc --noEmit` clean. `RollControllerTest`'s new cases (success, empty dice
list, over-cap count, unknown die type) green. Verified live: opened the
picker, added 1×d20 and 3×d6 (via left-click, checked the count badges),
right-clicked d20 back down to 0 (confirmed no browser context menu
appeared), added it back to 1, rolled — `DiceTray` showed `CUSTOM: ROLL`,
total `25`, expression `3d6 + 1d20 (1, 1, 5, 18)` (math checks out), and the
picker reset to an empty selection afterward, matching D&D Beyond's own
reset-after-roll behavior. Also found and killed an orphaned `node` process
from an earlier session still holding port 5173 (Keycloak's only registered
redirect URI), which had forced Vite onto 5174 and broken login.

**Same day, follow-up:** three direct owner corrections to the trigger
button, after seeing it live next to D&D Beyond's own. **Position:** it now
sits directly below the dice tray, flush in the actual bottom-left corner,
instead of beside the tray — `DiceTray` and `CustomRollPicker` are now both
plain-flow children of one new `.dice-corner` wrapper (`CharacterSheetScreen.tsx`)
that owns the single `position: fixed; left: 16px; bottom: 16px` anchor and
stacks them in a column; neither component positions itself anymore.
**Size:** the trigger grew from 36px to 56px. **Chrome:** the circular
border/background/shadow around the icon is gone — `dnd_icon_dice_roll.svg`
is the button, not a button with the icon inside it, per the owner's own
framing. `tsc --noEmit` clean; verified live: tray sits above the button,
both left-aligned at the corner, the icon renders with no visible border or
fill behind it, and the picker panel still opens correctly anchored above
the now-larger trigger.

**Same day, second follow-up:** the previous fix stacked the tray directly
above the trigger, which meant the picker panel (popping up from the
trigger) still overlapped the tray whenever both were near the bottom of a
short viewport. Owner's call, after being shown two options: rather than
open the panel sideways, move `DiceTray` itself to the top-left corner,
independent of the picker's own bottom-left corner — the two no longer
share a stack, so the panel can never overlap the log regardless of the
log's own content height. `DiceTray` and `CustomRollPicker` are separate
`position: fixed` elements again (the `.dice-corner` wrapper from the first
follow-up is gone). Found in the process: `top: 16px` collided with
`AppShell.tsx`'s own unstyled header row (`<header>`, no fixed height —
phase 1 scaffolding, not owned by the sheet), so `.dice-tray` uses
`top: 56px` instead, clearing it. `tsc --noEmit` clean; verified live: tray
sits top-left clear of the header, trigger stays bottom-left, and the panel
opens above the trigger with nothing else nearby to overlap.

---

## 2026-08-17 — Hit points: Heal and Damage share one amount field

Direct owner request: D&D Beyond's own hit points box has a single amount
field, with Heal and Damage as two buttons that both apply that same number
— not the two separate small inputs (one under each button) this app built
in phase 10 slice 12. That slice's own doc comment cited a live measurement
finding two inputs; the owner's correction here supersedes it. `HitPoints.tsx`
collapsed its `heal`/`damage` field pair into one shared `hp` field — both
buttons now call the same `apply(hp, ...)`, clearing the shared field after
either fires. Temp keeps its own separate field, unaffected — the owner's
request was specifically about Heal and Damage, and D&D Beyond gives Temp
its own distinct field too. CSS: `.hit-points__input-group` (one button
stacked directly over its own input) removed, replaced by
`.hit-points__action-row` (the two buttons side by side, below the one
shared `.hit-points__amount` field) — `.hit-points__amount` itself is
unchanged, already a shared class with `HitDice.tsx`. `tsc --noEmit` clean;
verified live: typed 5 into the shared field, clicked Heal, current HP went
35 → 40, and the field cleared itself afterward.

---

## 2026-08-17 — Skill row height, spacing and dividers

Direct owner request, a deliberate deviation from D&D Beyond: earlier
re-verification (2026-08-16) had explicitly checked D&D Beyond's skill list
for a divider between rows and found none, so this app built without one to
match. The owner asked for one anyway here, plus a fixed row height and a
specific margin: `.skill-row` gained `height: 30px`, `margin-bottom: 5px`
and `border-bottom: 1px solid var(--border-control)`; `.skill-row--header`
overrides `height`/`border-bottom` back off (a text header, not a data row).
The rows sit inside `.panel__rows` (shared with `SensesPanel`/
`DefensesConditionsPanel`, its own `gap: 6px`), which would have stacked
with the new `margin-bottom: 5px` for an 11px total — confirmed live before
fixing. Scoped `.skills-panel .panel__rows { gap: 0; }` instead of touching
`.panel__rows` itself, so Senses and Defenses/Conditions keep their own
6px rhythm unaffected. Verified live: consecutive rows exactly 5px apart
(`getBoundingClientRect`), 30px tall, with a visible divider between every
row (zoomed screenshot).

**Same day, follow-up:** the divider was moved off `.skill-row` itself and
onto two specific columns instead, per the owner's own description of the
underlying CSS — `border-bottom: 1px solid #d8d8d8` on `.skill-row__name`
(the skill label) and `.skill-row__mod` (the bonus value box), so the line
now runs only under those two columns, not the full row (Prof/Mod stay
undivided). The header row's existing `border: none` override on those same
two classes already resets it there, no extra rule needed. Verified live:
`getComputedStyle` on both classes reports `1px solid rgb(216, 216, 216)`
(`#d8d8d8`), the plain row itself back to no border.

**Same day, second follow-up:** the owner noticed the skill label and bonus
were plain `<button>`s with no wrapping element, unlike D&D Beyond, and
guessed this might be why the divider felt off. Checked live: D&D Beyond's
`.ct-skills__col--skill`/`.ct-skills__col--modifier` are indeed plain `<div>`
column wrappers — the modifier column's inner `<button>` carries its own
separate border (`1px solid rgb(191, 204, 214)`, its own badge look), while
the `#d8d8d8` divider lives on the outer `<div>`, not the button. Restructured
`SkillRow.tsx` to match: `.skill-row__name`/`.skill-row__mod` are now the
column `<div>` wrappers (flex sizing plus the divider), each wrapping a
`<button>` styled by new `.skill-row__name-button`/`.skill-row__mod-button`
classes (font size, padding, the mod button's own full border/radius). The
buttons stay real `<button>` elements either way — `dnd-5e-sheet-build.md`'s
own non-negotiable ("every clickable is a button") wasn't in question, only
the wrapper around it. The header row (`SkillsPanel.tsx`'s plain `<span>`s,
still reusing the `.skill-row__name`/`.skill-row__mod` classes directly, no
button) is unaffected — its own `.skill-row--header` override already resets
font-size/padding/border itself, independent of the base rule's contents.
`tsc --noEmit` clean; verified live: both new wrapper divs report the
`#d8d8d8` border-bottom, the inner buttons kept their own borders/behavior
(roll and reveal both still fire correctly through the new structure).

**Same day, third follow-up:** `.skill-row__mod` (the bonus column's own
`<div>` wrapper) gained `padding: 2px`, direct owner request — the button
inside it (`.skill-row__mod-button`, `width: 100%`, its own `padding: 2px`)
still fits comfortably inside the div's now-smaller content box. `tsc
--noEmit` clean; verified live (`getComputedStyle` reports `2px`, no visual
overflow).

**Same day, fourth follow-up: `.roll--modifier`.** The owner asked for a new
shared class marking exactly the "roll a flat modifier" subset of `.roll`
targets — saving throws, ability scores, skill bonuses — as distinct from
`.roll` targets with their own richer styling (attacks, spell rolls,
initiative), pasting D&D Beyond's own CSS for its equivalent buttons as the
starting point (`align-items: center`, `background-color: transparent`,
`border: 1px solid #bfccd6`, `border-radius: 4px`, `box-sizing: border-box`,
`color: #394b59`, `cursor: pointer`, `display: inline-flex`,
`justify-content: center`). `#bfccd6`/`#394b59`/`4px` are exactly this file's
own `--border-control`/`--text-on-control`/`--radius-control` tokens, so the
new `.roll--modifier` rule uses those instead of repeating the literal hex.
Declared once in `frames.css` right after `.roll`, so every later,
component-specific rule (`.ability__roll`, `.saving__mod`,
`.skill-row__mod-button`) still wins the cascade on any property it
redeclares — confirmed live this matters: `.saving__mod`'s own
`border-radius: 50%` stayed a circle rather than picking up the new class's
`4px`. Added as a second class (alongside `.roll` and each component's own
class) to `AbilityBox.tsx`, `SavingThrowRow.tsx` and `SkillRow.tsx`'s bonus
button — no other `.roll` consumer (`AttackRow`, `SpellRow`, `Initiative`)
touched. `tsc --noEmit` clean; verified live: all three now compute
`border: 1px solid rgb(191, 204, 214)` / `color: rgb(57, 75, 89)`, the
saving-throw circle unaffected, and a saving throw roll still fires
correctly through the new class.

**Same day, fifth follow-up: divider misalignment diagnosed and fixed.** The
owner reported the skill label's and bonus button's `border-bottom` dividers
landing at different heights. Live measurement found the cause:
`.skill-row__mod-button` had no explicit `height` — it sized itself off its
own `font-size: 20px` (changed independently of this session's own edits)
plus padding/border, landing at 40×30px. `.skill-row__name-button` (14px
font) came out to 27px tall. `.skill-row` centers each column's `<div>`
(`align-items: center`) within its own fixed 30px row height, so two
differently-tall column divs centered to different bottom edges — a ~5–7px
gap between the two dividers. The owner then measured D&D Beyond's own
equivalent button directly: 40×24px, not 40×30. Added an explicit
`height: 24px` to `.skill-row__mod-button` (replacing the font-size-derived
auto height) — confirmed live: the divider gap dropped from several pixels
to 0.5px (sub-pixel rounding, visually flush), and the button itself now
measures exactly 40×24. `tsc --noEmit` clean.

## 2026-08-17 — Hit points: Temp is click-to-edit, Current/Max/Temp vertically centered, temp field resized

Three direct owner requests, batched together.

**Temp HP is click-to-edit, matching D&D Beyond.** Previously the Temp value
(a button showing the number, or "--") and its amount input sat side by side
permanently. The owner asked for D&D Beyond's own behavior instead: the
input only appears in the value's own spot, replacing it, once clicked —
showing the current number pre-filled when there is one, or empty when
there isn't. `HitPoints.tsx` gained an `isEditingTemp` boolean: at rest,
renders the existing button (`onClick` now starts editing instead of
applying); while editing, renders the number input in the same spot instead
(`autoFocus`). Committing happens on Enter or on blur (`commitTemp` — applies
the typed amount via `onSetTemporary` if valid, always clears the field and
exits edit mode); Escape discards the edit without applying. `tsc --noEmit`
clean; verified live: clicking "1" opened a pre-filled input in its place,
typing 8 and pressing Enter applied it (button reappeared showing "8"), and
clicking "--" (temp at 0) opened an empty input.

**Current/Max/Temp vertically centered.** `.hit-points__stats` was
`align-items: flex-end`, bottom-aligning every column along its own
baseline — the owner asked for the whole group centered instead. Changed to
`align-items: center`. Verified live (zoomed screenshot): Current, the
slash, Max and Temp now sit on one visually centered line instead of each
trailing its own label at a different height.

**Temp input resized to D&D Beyond's own measured size.** The owner flagged
the temp field as too small to use comfortably (34×15px, inherited from the
generic `.hit-points__amount` class shared with the Heal/Damage field and
`HitDice`). DOM-measured D&D Beyond's own temp input (revealed the same way,
by clicking the temp value) at 64×31px, `24px/400`, `padding: 0 0 0 3px`,
`border: 1px solid rgb(146, 162, 179)`, no border-radius, centered text —
this app's `.hit-points` box is already at that same reference's own
measured 317px width (established when the box was first widened, see
phase 10 slice 12's own entry), so these values port with no scaling.
`.hit-points__amount--temp` now carries all of them directly instead of just
overriding `width`. Verified live: input renders 64×31, fits inside the
317px-wide box with no overflow (`getBoundingClientRect`, input's right edge
well short of the box's own).

---

## 2026-08-17 — Hit points arrangement corrected; top row's right edge aligned with the columns below

Two direct owner requests, batched together, both same-day follow-ups.

**Hit points arrangement corrected.** The previous entry's fix (Heal/Damage
sharing one amount field) put both buttons below the field, side by side —
the owner corrected the arrangement to match D&D Beyond's own: Heal above
the field, Damage below it. `HitPoints.tsx`'s `.hit-points__left` now
renders Heal, then the shared `.hit-points__amount` input, then Damage, in
that order (the column already stacked vertically via
`flex-direction: column`, so no new wrapper was needed — just reordering
JSX). `.hit-points__action-row` (the now-unused side-by-side wrapper)
removed from `frames.css`. `tsc --noEmit` clean; verified live (zoomed
screenshot) — Heal above, field in the middle, Damage below — and
functionally: typed 3, clicked Damage, temp HP (5) absorbed first down to 0,
overflow reduced current HP by the remainder, matching normal 5e
temp-HP-first damage rules the server already applies.

**Top row's right edge aligned with the columns below.** Owner asked for the
spacing between the top row's items (the six ability boxes, proficiency
bonus, speed, heroic inspiration, hit points) to be adjusted so Hit Points'
own right edge lines up with the tabbed section's (and so the combat
column's) right edge below it. Live measurement found why it didn't already:
`Dnd5eVitalsTopRow`'s wrapping `<div>` is a block-level flex container with
no width of its own, so — same reasoning as `SheetShell`'s own centering fix
earlier this session — it already stretches to match its widest sibling
(`Dnd5eVitalsColumns`' three-column row, confirmed live both rows share an
identical 1196px width and left edge), but its items packed left under the
default `flex-start`, leaving Hit Points' own right edge 108px short of the
row below's right edge. Added `justifyContent: 'space-between'` alongside
the existing `gap: '5px'` (now a floor, not the only spacing) — this
distributes that leftover space across every item instead of leaving it
trailing, so Hit Points' right edge now tracks the combat column's own right
edge automatically. Deliberately not a hand-tuned pixel gap value: the
combat column's own width isn't fixed (it derives from its own top row's
content — Initiative/Armor Class/Defenses-Conditions, see frames.css's
`.tabbed-section` comment), so a magic-number gap would need re-tuning
per character; `space-between` self-corrects instead. `tsc --noEmit` clean;
verified live — Hit Points' and the tabbed section's right edges now land
on an identical pixel (`getBoundingClientRect`, both `1546.5` at this
window's width).

---

## 2026-08-17 — Trailing scroll trimmed further, inspiration icon enlarged

Two direct owner requests, batched together, both follow-ups within the same day.

**Trailing scroll trimmed further.** The previous entry's live-measured
`paddingBottom: 80px` (reserved on `<main>` so the fixed, bottom-left
`DiceTray` never overlaps the last row) still left the page scrolling well
past the sheet's own end, and the owner asked for the page's own height
reduced too. Re-checked live now that the sheet is centered (same day's
earlier fix): the dice tray sits at a fixed `left: 16px` to `216px`, while
the centered sheet's own left edge now starts well to the right of that at
normal desktop widths (confirmed live, `getBoundingClientRect` on both — no
horizontal overlap at all at this window's width) — the large reservation
the padding used to justify no longer applies in the common case. Dropped
`paddingBottom` from `<main>` entirely; the shell's own outer `24px`
uniform padding is now the only buffer below the last row, same as every
other side of the page. Verified live: `docScrollHeight` dropped from 1177
to 1097 (an 80px shorter page), with the last row landing 32px above the
viewport's bottom edge at max scroll and zero overlap with the tray.
Narrower windows, where the centered sheet could creep left of the tray's
own 216px, aren't specially handled — consistent with this sheet's other
fixed-pixel-width panels, which don't have a narrow-viewport story either.

**Heroic Inspiration icon enlarged.** `.inspiration__dot` was 11×11px — a
forced 1:1 square squashing `dnd_icon_inspiration_on.svg`'s own sunburst
shape (native `viewBox` 372×234, ≈1.59 aspect) down to a near-invisible dot
inside the 70×52 frame. Resized to 34×21 (≈1.62 aspect, negligible stretch
from the icon's own 1.59) — clearly legible without crowding the frame's
own border art. `border-radius: 50%` also came out of the rule, meaningless
now that the shape isn't a plain circle. `tsc --noEmit` clean; verified live
the icon renders well inside the frame's bounds with no overflow.

---

## 2026-08-17 — Sheet centered on the page, trailing scroll trimmed, roll/mutate press effect removed

Three direct owner requests, batched together.

**Sheet content centered.** The back button, header (portrait/name/level,
Short Rest/Long Rest/Game log) and the three-column sheet body used to sit
flush against the page's left edge, with the rest of a wide viewport left
blank — `SheetShell.tsx`'s outer `<div>` had no centering of its own, and
none of its children constrained their width either, so everything just
packed left. Owner first asked to exclude the header/name/rest-buttons from
centering, then corrected that after checking D&D Beyond directly: there,
`.ct-character-sheet__inner` (`max-width: 1200px; margin: 0 auto`) wraps
*both* the header and the sheet body in one centered column — only the
individual-roll popup (this app's `.dice-tray`, `position: fixed`) sits
outside it. Wrapped `SheetShell`'s back button, header and `<main>` in one
`width: fit-content; margin: 0 auto` box instead of copying D&D Beyond's
literal 1200px — this sheet's own combat column already derives its width
from its own content (see frames.css's `.tabbed-section` comment), so a
hardcoded max-width would fight that; `fit-content` sizes the wrapper to its
widest row (the three-column grid) and lets the narrower header stretch to
match via ordinary block `width: auto`, keeping every row's left/right edges
flush, same visual result without a magic number. `DiceTray` and `Sidebar`
(`position: fixed`) are unaffected — out of flow, so they don't contribute
to or move with the `fit-content` calculation. Verified live: header, back
button and all three columns now share the same centered left/right edges,
with roughly equal blank margins on both sides of a wide viewport.

**Trailing scroll trimmed.** `<main>`'s `paddingBottom: 120px` — reserved so
the fixed, bottom-left `DiceTray` never overlaps the last in-flow content —
was a guess, and left ~150px of blank scroll below the sheet's own last row
(confirmed live: scrolling to the page's own max `scrollTop` left the last
row's bottom edge ~150px above the viewport's bottom edge). Re-measured
live instead: the dice tray's own current height (~77px) plus its 16px
`bottom` offset means content needs roughly 93px of clearance from the
viewport's bottom edge at max scroll; tested candidate padding values
directly (`getBoundingClientRect` on both the last row and the tray) down to
`80px`, which leaves ~19px of clearance — no overlap, no more than a few
pixels of trailing scroll beyond the frame's own end. `paddingBottom`
changed from `120px` to `80px`.

**Roll/mutate press effect removed.** `.roll:active`/`.mutate:active` (both
`transform: translateY(1px)`, `frames.css`) gave every roll and mutate
control a "pressed down" nudge on click — D&D Beyond has no equivalent
feedback on its own controls, confirmed by the owner. Removed both rules,
along with `.saving__mod:active`'s `translate(-50%, -50%) translateY(1px)`
override, which only existed to re-apply `.saving__mod`'s own centering
transform alongside the press effect (2026-08-13's "Fix saving-throw roll
press glitch") — nothing left to compensate for once the press effect itself
is gone. `ui-design-system.md`'s "The mutate affordance" section, which
documented this press effect as an intentional shared rule, updated to
match. Verified the rule is gone from the loaded stylesheet (`.roll:active`/
`.mutate:active` no longer present in `document.styleSheets`).

`tsc --noEmit` clean; all three verified live.

---

## 2026-08-17 — Tabbed section scrollbar clear of its frame border, standardized padding across five main frames

Two direct owner requests, batched together.

**Tabbed section "cut" on Background (and any other overflowing tab),
diagnosed and fixed.** Reported right after the previous entry's column-
alignment fix reintroduced internal scrolling on this box (`height: 663px`,
down from a temporary `900px`). Live DOM measurement (`getBoundingClientRect`
on `.tabbed-section__content`) found the actual cause: padding and
`overflow-y: auto` were on the *same* element, so the native scrollbar —
appearing only on tabs whose content exceeds 663px, e.g. Background's
measured 893px — sat flush against the box's own right edge with zero
clearance, visually overlapping the frame's drawn ink border. Confirmed via
DevTools against D&D Beyond's own equivalent, `.ct-primary-box`: padding
(`13px 20px`) lives on the outer, non-scrolling box; a separate inner
`styles_content` div, `padding: 0`, is the one with `overflow-y: auto`,
nested *inside* the already-padded box, with the tab-button row as a fixed-
height sibling above it. Replicated that split in `Dnd5eTabbedSection.tsx`:
`.tabbed-section__content` is now a non-scrolling flex column holding
`TabBar` and a new `.tabbed-section__scroll` child (`flex: 1 1 auto;
min-height: 0; overflow-y: auto`) that wraps the active tab's panel.
Verified live: Background's scrollbar now sits well inside the padded
margin, with visible clearance from the frame's border on every side; the
Actions tab (content that fits without scrolling) renders with no scrollbar
at all, same as before.

**Padding standardized across the five main frames, DOM-measured against
D&D Beyond.** Owner asked for the padding/margin distance between each
frame's own edge and its internal labels/items/tabs to be a single
consistent value, sourced from D&D Beyond's DevTools rather than guessed.
Inspected `.ct-saving-throws-box`, `.ct-senses-box`, `.ct-skills-box`,
`.ct-proficiency-groups-box` and `.ct-primary-box` live: all five computed
an identical `padding: 13px 20px` despite ranging 278–623px wide — a flat
pixel value, not a percentage of box size. Replaced this app's five
previously-inconsistent, independently-guessed percentages (`8% 9%` on
Saving Throws and Senses, `6% 8%` on Proficiencies & Training, `3% 7%` on
Skills, `6% 7%` on the tabbed section) with that same flat `13px 20px` on
all five `__content` elements. `tsc --noEmit` clean; verified live that
every panel's labels now sit the same fixed distance from their own frame's
edge.

**Left out:** the four percentage-padded panels other than the tabbed
section (Saving Throws, Senses, Proficiencies & Training, Skills) keep their
existing single-element padding+scroll structure — none of them currently
overflow with this character's real data, so the border-overlap bug the
tabbed section had doesn't reproduce there. If one of them ever needs
internal scrolling with real content, it should get the same
content/scroll split `.tabbed-section` now has, for the same reason.

---

## 2026-08-17 — Hit dice moved into Short Rest, empty inspiration dot removed, panel captions + column alignment

Four direct owner requests, batched together.

**Hit dice relocated into Short Rest.** The standalone top-row "3/5 HIT DICE
(D10)" box (`HitDice.tsx`) had no D&D Beyond equivalent — matches the real
5e rule too, where hit dice are only ever spent *during* a short rest, not
as a free-standing action. Removed from `Dnd5eVitalsTopRow.tsx` entirely
(along with its now-unused `onSpendHitDice` prop); the same `HitDice`
component now renders inside the Short Rest sidebar
(`CharacterSheetScreen.tsx`'s `handleOpenShortRest`), stacked above
`ShortRestBody`'s own existing "spend as part of resting" stepper — the
pool display and its standalone quick-spend capability weren't removed,
just relocated to where the mechanic actually lives. Verified live: Short
Rest panel now shows both the pool ("3/5 HIT DICE (D10)", still spendable
on its own) and the pre-existing "Hit dice to spend — 3 available / Take
Short Rest" control.

**Heroic Inspiration shows nothing when not inspired.** Previously the dot
element always rendered, with CSS hiding its border only in the `--on`
state — meaning the not-inspired state was an empty ring, not empty space.
`HeroicInspiration.tsx` now only renders the dot `<span>` at all when
`inspired` is true; `.inspiration__dot`'s own `border`/`background`
declarations (only ever meaningful for the now-deleted off-state ring) came
out of `frames.css` along with the now-unused `.inspiration__dot--on`
modifier. Confirmed live: the frame shows just its own artwork with nothing
extra when not inspired.

**Section captions pinned to the frame's bottom edge, consistently.** Saving
Throws already did this (flex column + `margin-top: auto` on
`.panel__title`); Senses, Proficiencies & Training, and Skills didn't — their
titles just trailed whatever content happened to precede them, landing
wherever that ended rather than at a consistent bottom line. Same treatment
applied to all three (`.senses-panel__content`, `.proficiencies-panel__content`,
`.skills-panel__content` each gained `display: flex; flex-direction: column`
plus a `> .panel__title { margin-top: auto; padding-top: 12px }` override).
Confirmed live: all four captions ("SAVING THROWS", "SENSES",
"PROFICIENCIES AND TRAINING", "SKILLS") now sit flush against their own
frame's bottom edge.

**Skills, the tabbed section, and the left column now share one bottom
line.** Skills' frame (aspect-ratio-derived, ≈750px) ended 26px short of
where the left column (Saving Throws + Senses + Proficiencies stacked,
776px) ends; the tabbed section (698px at the time, previously resized
during the same-day "not cut off" fix) ended 237px past it. Measured the
left column's real total height live (`getBoundingClientRect`, `.saving-
panel`'s top to `.proficiencies-panel`'s bottom: 775.8px) and set both
`.skills-panel` (replacing its `aspect-ratio: 356/960` with a flat
`height: 776px`) and `.tabbed-section` (`height: 663px` — the left column's
height minus the top row's own 97px plus 16px gap) to derive from that same
number, so neither panel's bottom can drift from the other two's. This
reopens Actions/Background's internal scroll (they don't fit in 663px
without it) — confirmed acceptable with the owner explicitly: alignment
across all three columns matters more than any one tab fitting without a
scrollbar, and every other section panel on this sheet already scrolls
internally when its content doesn't fit, the same pattern this restores.
Verified live: `.saving-panel`/`.skills-panel`/`.tabbed-section` bottoms are
pixel-identical (`getBoundingClientRect` all report `bottom: 1065` at the
same scroll position); scrolled the Actions tab's own content to its end
and confirmed "Action Surge" (the last row) becomes fully visible, the
frame border unaffected.

`tsc --noEmit` clean throughout; frontend-only, no backend changes.
`dnd-5e-sheet-build.md`'s Hit dice block, Heroic inspiration, Saving throws
panel, Senses panel, Proficiencies & training panel, Skills panel, and
Tabbed section layout fix rows all updated to match.

---

## 2026-08-17 — Tabbed section: fixed height too, not just fixed width

Direct follow-up to yesterday's fixed-width fix: the owner reported the
frame "cutting" content specifically on the Actions and Background tabs.

**Diagnosis, confirmed live**: fixing only the width (608px, matching the
top row) left the height still derived from `aspect-ratio: 784/829` — at
608px wide that resolves to ~643px tall, but Actions needs 722px and
Background needs 893px of content height (measured via
`.tabbed-section__content`'s `scrollHeight` at the fixed width). The extra
content wasn't lost — `overflow-y: auto` made it scrollable — but the box no
longer grew to show everything the way it did before the width fix, so the
first screenful now ended mid-row, reading as the frame cutting content
rather than a scrollable box. Background needs by far the most room because
this app's own Background tab merges D&D Beyond's separate Background and
Notes tabs into one (an earlier, already-accepted deviation) — genuinely
more content than any single D&D Beyond tab holds.

**Fix**: replaced `aspect-ratio: 784/829` with a flat `height: 900px` —
comfortably above Background's 893px measured requirement, the tallest of
the six tabs. Both axes (`width: 100%` resolving to 608px via yesterday's
`contain: inline-size`, plus this explicit height) are now literal fixed
values, independent of each other and of tab content — satisfies the
"never resizes on interaction, only on responsive changes" rule from
scratch rather than patching the aspect-ratio approach further. Verified
live from the actual CSS file (not just a scripted test) across all six
tabs: identical 608×900 every time, zero vertical *or* horizontal scroll
needed anywhere (`scrollHeight`/`scrollWidth` equal to `clientHeight`/
`clientWidth` on every tab). Screenshotted Actions and Background
specifically (the two the owner named) scrolled to their full extent — both
render completely, frame border intact top and bottom, no distortion beyond
the same "asset stretched off its native ratio for content fit" trade-off
already accepted elsewhere on this sheet (`.badge`, `.armor`, etc.).

`tsc --noEmit` clean; frontend-only. `dnd-5e-sheet-build.md`'s Tabbed
section layout fix row updated with this entry's detail.

---

## 2026-08-17 — Tabbed section no longer resizes when switching tabs

Direct owner request: the Actions/Spells/Inventory/Features/Background/
Extras frame must render at one fixed size, never changing because the
player switched tabs — and the same "interaction never resizes a section,
only responsiveness does" principle applies sheet-wide, not just to this box.

**Root cause, confirmed live** (`getBoundingClientRect` on `.tabbed-section`
while scripting clicks through all six tabs on the same character): the box
was genuinely resizing per tab — 608px wide on Spells/Extras, up to 925px on
Actions, everything in between on Features/Background/Inventory. `.tabbed-
section { width: 100% }` was resolving against `Dnd5eCombatColumn`, a flex
column with no explicit width of its own — so that column's shrink-to-fit
width was set by whichever tab's content happened to be widest at the
moment, not by the top row (Initiative/Armor Class/Defenses-Conditions) as
intended. This wasn't a regression from the typography pass earlier this
session — the underlying layout issue already existed — but the larger row
text made the size swings big enough to notice and report.

Fixed with `contain: inline-size` on `.tabbed-section`: this tells the
browser the box's own size must not depend on its own content along the
width axis, breaking the circular contribution without abandoning
`width: 100%` — so the box still tracks the top row's width if *that*
changes (a different character's Defenses/Conditions content, a viewport
resize), only this box's own tab content is excluded from the calculation.
Confirmed via a candidate-testing pass before committing to this fix: a
hardcoded `width: 608px` also stabilized it (confirming the diagnosis) but
was rejected as fragile (breaks the moment a character's top row measures
differently); `min-width: 0` on the box and its content wrapper alone did
nothing (this isn't the same "automatic minimum size" flex special case
`min-height: 0` already fixed on this box's vertical axis — that only
applies along a flex item's own main axis, and width is this box's cross
axis in its column-direction flex parent).

Also checked the owner's broader claim (other sections resizing on
interaction) directly: toggled Heroic Inspiration on/off, and toggled three
extra conditions on then off via the Conditions sidebar (lengthening the
Defenses/Conditions trigger text from "Prone" to "Charmed, Deafened,
Blinded, Prone" and back) — `.inspiration`, `.defenses-conditions-panel` and
`.hit-points` all measured byte-identical before and after in both cases.
Every other framed section already had an explicit fixed width (unlike
`.tabbed-section`'s `100%`), so this was the one real instance of the
pattern, not a sheet-wide problem.

One incident during verification: two of the test condition toggles
(Charmed, Deafened) hit a transient "Change failed to reach the server"
error and didn't actually revert server-side, despite the UI briefly
showing them off — caught on the next full reload (`CONDITIONS: Charmed,
Deafened, Prone` instead of the expected `Prone`), and corrected for real
through the normal UI afterward, confirmed via reload. Unrelated to the CSS
fix itself — a transient backend hiccup during rapid scripted toggling, not
reproduced on manual single clicks — noted here since it touched the
character's actual persisted data mid-session, not because it's a code
change.

`tsc --noEmit` clean; frontend-only. `dnd-5e-sheet-build.md`'s Tabbed
section layout fix row updated with this entry's detail.

---

## 2026-08-16 — Full-sheet DOM-measured typography pass

Same treatment as the earlier Skills-row rebuild, extended to the rest of
the sheet: DevTools (`getComputedStyle` via the browser tool) against D&D
Beyond's live DOM, section by section, rather than estimated pixel values.
A first attempt at this ran as a background agent and hit a session limit
mid-sweep; the inspection was redone directly in this session instead of
resuming the fork.

**The dominant finding, confirmed across nearly every section**: this app's
row/label typography was consistently smaller than D&D Beyond's own —
labels that should read ~10-13px were set at 8-9px, data/value text that
should read ~13-14px was set at 9-11px. Not a one-off Skills issue; a
sheet-wide pattern. Fixed section by section:

- **Initiative**: box 78×52 → **90×55** (D&D Beyond's own measured size).
  Caption was the biggest miss — 8px **muted** → **13px, black**
  (`rgb(0,0,0)`, confirmed live, not muted like most other captions). Value
  18px/600 → 26px/700.
- **Armor Class**: box was already 90×97 (close to D&D Beyond's own live
  90×95 — no resize needed, already correct). Heading/caption 8px → 10px
  (muted, confirmed correct there). Value 24px → 26px/700.
- **Saving throws**: ability abbreviation 11px → 12px/700; modifier **13px/600
  → 14px/400** — regular weight, not bold, confirmed live.
- **Senses**: passive-score badge 11px → 14px/700; label 9px → 10px/700 and
  recolored muted → **near-black** (`rgb(18,24,28)`, confirmed live — not
  muted like Skills' own header, components genuinely differ); the special-
  senses trigger 9px/600 → 12px/400.
- **Proficiencies panel**: category label (e.g. "ARMOR") 10px → 14px/700
  muted; value list 9px → 13px/400. Also corrected the panel's own title:
  it reads **"Proficiencies and Training"** on D&D Beyond, not
  "Proficiencies & Training" — the app had an invented label; fixed in
  `ProficienciesPanel.tsx`.
- **Shared `.panel__title`** (Saving Throws/Skills/Senses/Proficiencies'
  bottom caption): 11px/muted → **13px, black** — confirmed via "SAVING
  THROWS"'s own live DOM.
- **Actions/Spells/Inventory/Extras tab headers**: the column-header rows
  added in the earlier general re-verification pass used a guessed 8px/
  muted — corrected to the real **11px/700, plain black** measured directly
  from each tab's own header DOM (`.styles_tableHeader__*`,
  `.ct-spells-level__spells-row-header`, `.ct-inventory__row-header`,
  `.ct-extra-list__row-header`).
- **Row text** across Actions (`.action-row__*`), Spells (`.spell-row__*`),
  Inventory (`.item-row__*`) and Extras (`.extra-row__*`): bumped from
  9-10px to the ~12-14px range confirmed on Attack/Spell/Item row names
  specifically (14px, 14px, 13px). A genuine nuance not fully replicated:
  D&D Beyond's Attack/Spell row names are regular weight (400) where
  Feature row names (sharing the same `.action-row__name` class in this
  app) are bold — kept bold everywhere for legibility rather than splitting
  the shared class into per-tab variants; documented as a known
  simplification, not a re-measurement miss.
- **Background tab**: background name (e.g. "Miner") 14px → 15px/700;
  characteristic label 8px/muted → 11px/700/black; value 10px → 13px. The
  same shared `.background-tab__field-label` also renders "Personality
  Traits"-style headings (D&D Beyond: 13px/700, non-uppercase) and Notes
  headings (D&D Beyond: 13px/400, non-uppercase, genuinely lighter weight
  than Background's own headings) — not split into separate classes this
  pass, same "known simplification" treatment as the action-row weight
  nuance above.
- **Features & Traits**: confirmed D&D Beyond shows a source citation next
  to each feature name (e.g. "PHB, pg. 57", 13px/700) that this app doesn't
  track at all — **not built**, flagged as a separate scope decision (needs
  a new field on `Dnd5eFeatureTrait` plus seed data, not a CSS change) per
  the plan presented to the owner; still an accepted, documented omission.

**Confirmed, not changed**: dividers. Re-checked border/box-shadow/pseudo-
elements/alternating-background on Saving Throws, Senses, Attacks, Spells
and Extras rows — none anywhere, consistent with the earlier Skills finding.
One real exception found this pass: Inventory's **group headers** (e.g.
"EQUIPMENT (18)") do carry `border-bottom: 1px solid rgb(234,234,234)` on
D&D Beyond — a section-level divider, not a per-row one; this app has no
equivalent grouping yet, so nothing was built for it, noted for later.

Verified live end to end after each section: Initiative/Armor Class/Saving
Throws/Senses/Proficiencies at the top; Actions/Spells/Inventory/Features/
Background/Extras tabs. No horizontal overflow or wrapping regressions on
any panel at the new sizes — most panels had headroom to spare, per the
`getBoundingClientRect` checks already established this session. `tsc
--noEmit` clean throughout; frontend-only, no backend changes.
`dnd-5e-sheet-build.md`'s relevant rows updated to match (see that file for
the row-by-row detail); this entry is the narrative summary.

---

## 2026-08-16 — Skills row rebuilt from D&D Beyond's own measured DOM

The owner asked for a specific verification method this time: open DevTools
on D&D Beyond's live sheet and inspect `.ct-skills__item`/`.ct-skills__header`
directly (`getComputedStyle` via the browser tool, same effect as pressing
F12 and reading the Elements/Computed panel) rather than estimating column
widths and font sizes from a screenshot.

Findings, all confirmed via computed styles, not guessed: Prof column 30px
wide (wrapping a 10px circle icon — the column and the icon are two separate
things there, unlike this row's previous version where one 8px element
played both roles); ability-abbreviation column 40px, `padding: 0 5px`,
12px/700; skill name column `flex: 1 1 0%`, `padding: 5px 0`, **14px** (this
app had it at 9px); Bonus column 44px, right-aligned, `padding: 2px`,
**14px/700**. Header row: all four columns 10px/700/uppercase/muted,
`margin-bottom: 5px` separating it from the list. No `gap` on the row —
D&D Beyond's vertical rhythm comes entirely from the skill-name column's own
padding, not row spacing. D&D Beyond's live `.ct-skills-box` renders at
281px, close enough to this panel's own 278px that every measurement
transferred directly with no scaling needed.

One correction to the owner's own recollection, caught by the inspection
rather than assumed: they described a divider between each skill row.
Checked for one thoroughly on D&D Beyond's live DOM — border, box-shadow,
`::after`/`::before` pseudo-elements, alternating row background — and found
none. The row-to-row rhythm there is purely the skill-name column's
`padding: 5px 0`; no divider was built here either, to match what D&D Beyond
actually renders rather than the recollection of it.

Implementation: `SkillRow.tsx` gained a `.skill-row__prof-cell` wrapper
(30px) around the existing proficiency icon, which keeps its own natural
size. `SkillsPanel.tsx`'s header row's prof column now shows real "Prof"
text (previously left empty since the un-wrapped 8px column had no room for
a label). All sizing lives in `frames.css`'s `.skill-row` block, reusing the
same column classes for both the header and data rows so they can't drift
out of alignment.

Verified live: all 18 skills render with no divider, matching D&D Beyond;
`getBoundingClientRect` confirmed the list's bottom edge sits well inside
the frame's own bottom edge (no overflow); the two longest labels ("Animal
Handling", "Sleight of Hand") don't wrap at the new 14px size. `tsc --noEmit`
clean; frontend-only. `dnd-5e-sheet-build.md`'s Skill row entry updated with
the full measured value set.

---

## 2026-08-16 — Follow-up look at Features, Background, Extras: search + header row on Extras

Owner asked for the same live-comparison treatment on the three tabs the
general re-verification pass above hadn't covered yet.

**Extras tab.** Two of the same class of gap as the pass above: no name
search (`sheet.name.toLowerCase().includes(...)`, same pattern Inventory/
Spells already use) and no column header row. Added `SearchField` above the
filter chips (matching D&D Beyond's own layout — search first, chips below)
and an `.extra-row--header` row ("Name / AC / Hit Points / Speed") reusing
`.extra-row`'s own column-width classes. Verified live: typed into the
search field, filtered the one seeded extra (Warhorse) correctly; header
renders aligned with the row below it. The portrait thumbnail D&D Beyond
shows next to an extra's name is a reconfirmed, already-documented gap (no
portrait storage for extras exists yet), not new — left alone.

**Features & Traits.** Found a real gap, not built: D&D Beyond shows some
limited-use features as a numeric pool with a +/− stepper (e.g. a Paladin's
Lay on Hands, "Uses: 15 / Long Rest") — a different resource shape than this
app's only one, discrete checkbox uses (`BoxTrack`). This app's own seeded
character (a Fighter/Eldritch Knight) has no feature of that shape, so there
was nothing to build against even if in scope. Modeling a numeric pool means
a new resource type on the domain model (`Dnd5eFeatureTrait`/
`Dnd5eFeatureAction`), not a presentational tweak — flagged in
`dnd-5e-sheet-fidelity-audit.md` for a future scope decision rather than
guessed at or built partially.

**Background.** Structure already matched (combined characteristics panel,
Personality/Ideals/Bonds/Flaws, Organizations/Allies/Enemies/Backstory/
Other). D&D Beyond adds its own sub-filter chips within both the Background
and Notes tabs; this app's single combined tab has none — downstream of the
already-accepted merged-tab deviation (`dnd-5e-sheet-build.md`), not treated
as its own gap.

`tsc --noEmit` clean; frontend-only, no backend changes this entry.
`dnd-5e-sheet-fidelity-audit.md` gained a "Follow-up look" subsection under
the general re-verification pass; `dnd-5e-sheet-build.md`'s Extras tab row
updated to match.

---

## 2026-08-16 — General re-verification pass: column headers, Senses reveal target, conditional Spells tab

The owner asked for a general re-check of the whole sheet against a wider set
of D&D 5e builds: three more live reference characters (a Cleric/Paladin
multiclass, a level-20 Wizard, a level-17 Ranger, plus the existing Helga
Flinthand reference) and a Minotaur Barbarian specifically to cover the
non-spellcaster case. URLs recorded in `dnd-5e-sheet-ui.md`'s "Verifying
fidelity against D&D Beyond" section and in memory, for future re-checks.
Three real, previously-undocumented gaps found and fixed (owner picked all
three from the findings, over a "document only, fix nothing" option):

**Column headers.** D&D Beyond labels every tabular list's columns with a
small muted header row; this app's Skills, Attacks (and Spells' per-level
lists, sharing that row shape) and Inventory panels had none. Added a
`--header` variant of each row's own component (`.skill-row--header`,
`.action-row--header`/`.spell-row--header`, `.item-row--header`) that reuses
the real row's column-width classes — header and data columns stay aligned
by construction, not a second hand-tuned width table — with typography/
border/background reset via a small CSS override block per row type.
Inventory's header skips Weight/Notes, matching this app's own already-
documented "no weight/encumbrance" deviation.

**Senses line becomes a real reveal target.** Confirmed against all four
reference characters: D&D Beyond's bottom senses line — a real value like
"Darkvision 60 ft." or, when the character has no fixed sense, a placeholder
"Additional Sense Types" — always opens a sidebar with the three passive
scores again plus general rules text (what a passive check is, then
Blindsight/Darkvision/Truesight). This was a known, deferred gap ("Saving
throw rows and sense rows have no reveal button at all yet") that this
session's earlier `specialSenses` feature inherited without reopening it.
`SensesPanel.tsx` now always renders the trigger button (previously hidden
when `specialSenses` was empty) and wires it to the existing Entity Detail
mold via a new `onOpenDetail` prop, threaded through `Dnd5eLeftColumn.tsx` —
not the single-value Explainer mold, since this panel shows several values
plus static text, not one value with a contribution trace. The rules
paragraph is written in this project's own words, not copied from D&D
Beyond's panel text (copyright).

**Spells tab hidden for non-casters.** `Dnd5eTabbedSection.tsx`'s tab list
was static and always included "Spells". Confirmed live on the Minotaur
Barbarian reference that D&D Beyond omits the tab entirely for a
non-spellcaster rather than showing it empty. `TABS` split into `BASE_TABS`
(always shown) and a separate `SPELLS_TAB` spliced in only when
`sheet.spellcasting.length > 0`; an `effectiveTab` falls back to "actions"
if "spells" is selected and spellcasting becomes unavailable (defensive —
not reachable via any mutation this app has, since no mutation removes a
character's last spellcasting class).

Two findings from the same pass turned out to already be known, confirmed
deviations, not new gaps: D&D Beyond's Background/Notes split (two tabs vs.
this app's one) and the top-row Hit Dice box (D&D Beyond keeps it Short-Rest-
only; the owner's own earlier scope call kept a persistent box here too) —
both already recorded in `dnd-5e-sheet-build.md`, left unchanged.

Verified live end to end after each fix: Skills/Attacks/Spells/Inventory
headers render and align with their rows; clicking the Senses line opens the
sidebar with Aria's actual Darkvision plus the rules text; the Spells tab
still renders correctly for Aria (a caster) with its new header, confirming
the conditional logic doesn't regress the caster path. `tsc --noEmit` clean
throughout — this batch was frontend-only, no backend changes.
`dnd-5e-sheet-fidelity-audit.md` gained a new "General re-verification pass"
section; `dnd-5e-sheet-build.md`'s Skill row, Attack row, Spells tab,
Inventory tab and Senses panel rows updated to match.

---

## 2026-08-16 — Structured special senses (Darkvision/Blindsight/Truesight) on the Senses panel

The third and last orphaned fidelity-audit finding: D&D Beyond shows a
plain-text senses line ("Darkvision 60 ft.") below the three passive scores;
this app only ever rendered the three scores. The owner's ask was specific:
show it as extra text only when the character natively has a fixed sense
(species or a feature), and — asked directly, since there was no existing
race/species data model to derive it from — model it as a structured
type-plus-range entry, not a free-text field.

Backend: new `Dnd5eSenseType` enum (`DARKVISION`/`BLINDSIGHT`/`TRUESIGHT`) and
`Dnd5eSpecialSense(type, rangeFeet)` record, added to `Dnd5eSheet` as
`specialSenses` (11 `with*` reconstructor methods updated to carry the new
field through). `Dnd5eSheetCalculator` maps each entry to a system-agnostic
`SpecialSense(type, rangeFeet, label)` — `label` is formatted server-side
("Darkvision 60 ft.") so the frontend renders it verbatim rather than
re-deriving the format. Threaded through `VitalsZone` → new
`SpecialSenseResponse` → `CharacterSheetResponse`, the same shape every other
vitals field already takes. Read-only this phase, no mutator — same "no
catalog to pick from yet" reasoning the Extras tab's missing "add an extra"
UI already carries. Seeded Aria's existing Mountain Dwarf Darkvision
(previously only descriptive text in Features & Traits) as the first real
`specialSenses` entry (60 ft.), matching real 5e Mountain Dwarf lore.

Frontend: `SensesPanel.tsx` renders a `.senses-panel__extra` line per entry
below the three passive scores, only when `specialSenses` is non-empty.

Adding this list field to `Dnd5eSheet` surfaced a real gap in this app's
"schema evolves without Flyway migrations" story (see
`Dnd5eSheetJsonMapper`'s doc comment): the lenient JSON mapper only defaults
*primitive* fields absent from old rows (`FAIL_ON_NULL_FOR_PRIMITIVES`
disabled) — a genuinely missing *list* field still deserializes to `null`,
and `Dnd5eSheetCalculator.specialSenses()` threw a `NullPointerException`
calling `.stream()` on it for the one dev-seeded character, created before
this field existed. Fixed by patching that one row's persisted JSON directly
(`jsonb_set(sheet, '{specialSenses}', '[]'::jsonb-with-the-real-entry)`) —
not a schema migration, the same "hand-patch dev data" story this project
already accepts for its Flyway-free JSONB sheet payload, just the first time
it was hit for a non-primitive field rather than a primitive one. Verified
live: the sheet loaded correctly after the patch, and "Darkvision 60 ft."
renders below the three passive scores in the Senses panel.

`dnd-5e-sheet-fidelity-audit.md`'s Senses row and `dnd-5e-sheet-build.md`'s
Senses panel row updated to match. Backend `tsc`/`gradlew :apps:api:check`
both clean; new/updated assertions in `Dnd5eSheetCalculatorTest` and
`CharacterControllerTest` cover the new field end to end.

---

## 2026-08-16 — Inventory equip control becomes a real checkbox; MANAGE button deferred to phase 11

Two of the three fidelity-audit findings orphaned at the end of slice 12,
resolved directly with the owner rather than through another audit slice.

**Inventory equip control.** `.item-row__flag` (`ItemRow.tsx`) showed a
letter ("E") inside a small bordered box — the owner asked for an actual
checkbox instead, matching D&D Beyond's own equip control. No backing-data
change needed (`item.equipped` and the `TOGGLE_ITEM_EQUIPPED` mutation
already existed): swapped the "E" text for a conditionally-rendered "✓",
and reworked `.item-row__flag` in `frames.css` to center its content and
fill with `--frame-ink` (checkmark in `--frame-paper`) only when
`aria-pressed="true"`, leaving an empty bordered box otherwise. Verified
live: equipped items (Longsword, Shield, Cloak of Protection) show a
checked box, unequipped ones (Chain Mail, Potion of Healing) show an empty
one.

**MANAGE button.** The audit flagged D&D Beyond's header "MANAGE" button
(opens their character-editing/level-up flow) as missing here. The owner
deferred it explicitly: they dislike D&D Beyond's own character-creation UX
and don't want to commit to a shape for the equivalent flow yet — revisit
once phase 11 (character creation/editing) is actually designed, not
approximated now. No code change; `dnd-5e-sheet-fidelity-audit.md`'s Header
row updated to record the decision and why, so it isn't mistaken for an
unnoticed gap later.

The third orphaned finding (a senses/vision text line for fixed
darkvision/blindsight/truesight) needs a data-modeling decision before any
schema work starts — not resolved in this entry, see the next one once
that's settled.

---

## 2026-08-16 — Owner-direct badge/inspiration sizing, Heroic Inspiration caption moved outside the frame

Follow-up to the "Direct owner fixes" batch below. The owner edited
`frames.css` directly rather than asking for another measurement pass:
`.badge` (Proficiency/Speed) went from the previously-corrected 81×67 to a
final **85×85**, and `.inspiration` (Heroic Inspiration) went from 40×33 to
**70×52** — both accepted as-is, not re-derived. Also asked for the "Heroic
Inspiration" caption to sit below the frame rather than crammed inside it
next to the dot.

`HeroicInspiration.tsx` restructured: the button (still the frame-box,
holding only the dot now) is wrapped in a new `.inspiration-wrap`, with the
caption as a sibling `<div>` below it — the same above-the-frame caption
pattern `.initiative-wrap` already used for "Initiative", just flipped to
sit underneath instead. `.inspiration__caption` (used to live inside the
button, sized down to 5.5px to fit the cramped frame) is gone; the new
`.inspiration-wrap__caption` uses 8px, matching `.initiative-wrap__caption`,
since it's no longer squeezed inside a tiny frame.

Verified live: badges bottom-align with the ability boxes' straight sides,
Heroic Inspiration is visibly smaller than the badges, and its caption reads
clearly below the frame instead of overlapping the dot. `tsc --noEmit`
clean. `dnd-5e-sheet-build.md`'s Stat badge and Heroic inspiration rows
updated to the final 85×85/70×52 values and the caption's new position.

---

## 2026-08-16 — Direct owner fixes: inline SVG frames, hit points fit, top-row layout, badge sizing

Five direct instructions from the owner, outside the audit's own slice
numbering — handled as a batch, verified live after each.

**Frames are real inline `<svg>` now, not CSS masks.** The kit's original
technique rendered a frame as two `<div>`s masked via `mask-image: url(...)`
— Inspect Element showed only an empty colored box, not the actual artwork,
making it hard to debug or compare against D&D Beyond's own DOM (which uses
real `<svg>`/`<path>` elements). Confirmed the approach with the owner
before touching ~15 components: moved all 18 frame/icon SVGs from
`public/frames/svg/` (not importable as JS modules, Vite can't `?raw`-import
from `public/`) into `src/systems/dnd5e/frames/`, normalizing 3 files that
used a hardcoded `fill="#000000"` to `fill="currentColor"` (matching the
other 15) so they respond to the paper/ink `color` tinting. New
`FrameLayer.tsx` (a paper/ink pair, always stretched via a forced
`preserveAspectRatio="none"` — `svgUtils.ts`'s `forceStretch`, matching the
old `mask-size: 100% 100%` behavior every consumer's own box dimensions
were already tuned for) and `FrameIcon.tsx` (a single non-paired icon at its
natural contain-and-center aspect, for the rest buttons specifically — their
box isn't square the way every `FrameLayer` consumer's is) replace the old
`.frame-box__paper`/`.frame-box__ink` divs across every consumer:
`AbilityBox`, `StatBadge`, `ArmorClass`, `Initiative`, `HeroicInspiration`,
`SavingThrowRow`, `SkillRow`, `SenseRow`, `SectionPanel` (now takes an `svg`
prop from its 6 callers instead of a CSS-only `frameClass`), `HitPoints`,
`Dnd5eTabbedSection`. `frames.css` lost its `--f-*`/`--i-*` `url(...)`
custom properties and every per-component `mask-image` rule — color/sizing
is now generic (`.frame-box__paper { color: var(--frame-paper) }` etc.),
which asset renders is a JS-level choice, not CSS. `dangerouslySetInnerHTML`
is safe here (this app's own bundled static asset, never external input).
The rest-button icons needed threading as props (`shortRestIcon`/
`longRestIcon` on `SheetShell.tsx`) rather than an import inside that
component itself — `sheet/` stays system-agnostic, `CharacterSheetScreen.tsx`
(which already imports dnd5e-specific components at this integration layer)
supplies the dnd5e-specific icon content instead. Deleted the now-unused
`public/frames/svg/` directory; the old `public/frames/*.png` files stay
(already-dead leftovers from before the SVG migration, out of this session's
scope). Verified live: DevTools now shows genuine `<path>` elements (92
across one sheet render) instead of empty masked boxes, and the sheet is
visually unchanged everywhere this touched.

**Hit points block was overflowing its own frame.** Slice 12's twin-input
rework (Heal/Damage each getting their own field) made the box's content
grow to ~240×120 — `.hit-points` never had an `aspect-ratio` lock (unlike
every other panel), so it just grew with content, stretching the frame
artwork past where its border was actually drawn. Fixed the root cause
rather than the symptom: widened to the D&D Beyond reference's own
live-measured 317px and locked `aspect-ratio: 400/113` (~89.5px tall) plus
`overflow: hidden` — the same "lock the ratio, don't let content dictate
box size" fix already applied to every other panel — then shrunk the twin
inputs' own font/padding to fit that budget. Verified live: all content
(Heal/Damage fields, Current/Max/Temp, the "Hit Points" caption) now sits
fully inside the drawn border, confirmed via `getBoundingClientRect` that
the title's bottom edge sits above the box's own bottom edge.

**Defenses/Conditions moved beside Armor Class.** Was stacked below
Initiative/Armor Class in its own row; the owner asked for it beside them
instead, specifically to widen the combat column (and so the tabbed section
stacked below it) past Defenses/Conditions' own 408px. `Dnd5eTabbedSection`
changed from a fixed 408px width to `width: 100%`, now stretching to match
whatever the new, wider top row establishes as the column's natural width.
The owner was explicit that the tabbed section must never scroll
horizontally, only vertically if needed — so `.tabbed-section__content`'s
`overflow-x` changed from `auto` to `hidden` (a stricter fix than "avoid
it when possible"). Verified live across Actions, Spells (previously the
tab most likely to need horizontal scroll — Time/Range/Hit-DC/Effect/Notes
columns), Inventory: all six tabs render in full with no horizontal
scrollbar anywhere, and no page-level horizontal overflow either
(`document.body.scrollWidth` confirmed ≤ viewport width). The box grew
taller as a side effect — `aspect-ratio: 784/829` is still locked, so more
width means proportionally more height — accepted as the direct
consequence of the width the owner asked for, not a regression.

**Proficiency/Speed badges resized, twice.** First pass matched the ability
box's full 81×95 footprint per the owner's request to align them — but the
owner caught that 95px included the ability box's *protruding score circle*
at the bottom, not just its straight card sides, and asked for the badge to
align with the straight part instead. Measured live (`getBoundingClientRect`
on the score button): the circle's top edge sits ~67px into the 95px box.
Corrected `.badge` to 81×67. Heroic Inspiration, sized as "at least half of
the Speed badge" in the same request, was corrected alongside it (half of
81×67 → 40×33, down from an initial half-of-81×95 guess) to keep that
relationship accurate to the corrected Speed size, with its dot and caption
font shrunk to match. Verified live: badges now bottom-align with where the
ability boxes' circles begin, not their full height.

`dnd-5e-sheet-build.md`'s "Non-negotiables carried over from the kit"
section, its Stat badge/Heroic inspiration/Hit points block/Defenses-
conditions panel/Tabbed section/Tab bar/Dice tray rows, and its frame-asset
location note all updated to match.

---

## 2026-08-16 — Phase 10, slice 12: Presentational cleanup pass

The last slice in the audit's suggested order. Five small items batched
together; verified each live before touching code rather than trusting
the audit's original (sometimes unverified) guesses.

**Defenses icons.** DOM inspection of D&D Beyond's own Defenses column
(`.ddbc-resistance-icon`/`.ddbc-immunity-icon`) found the audit's "shield+
exclamation" guess was wrong — two genuinely different icon shapes, but
both the exact same green, `#00c680`. Reproducing their SVG path data
would be shipping another product's asset (forbidden); reproducing the
confirmed color in this app's own letter-badge treatment is not.
`DamageTypeIcon`'s green letter badge — already used for Resistance
("R") — now also covers Immunity ("I"). Vulnerability and Condition
Immunity stay plain text; this character has neither, so their color is
still unconfirmed rather than guessed. New `--defense-badge: #00C680`
token replaces the badge's previous approximate `#1E9C5A`.

**Coin-chip header.** Confirmed live D&D Beyond's coin display is a
single button (`role="button" aria-label="Manage Coin..."`) showing only
nonzero denominations, opening a management panel on click — not inline
chips with their own controls. New `CoinChips.tsx` renders those chips
(colored dots in conventional metal tones, not copied artwork) in the
Inventory tab's header, replacing the standalone "Coins" section.
Clicking opens the existing `CoinsPanel` — unchanged — as the sidebar's
Entity Detail `actionBar`, live-refreshed the same way Extras' hit points
already are (`refreshActionBar`), since coins mutate while the panel
stays open. Verified live: chip read 45, opening it and adding 10 gold
updated both the panel and the chip behind it to 55.

**Hit points block layout.** The audit flagged this as needing an owner
decision (twin Heal/Damage inputs like D&D Beyond vs. this app's shared
field) rather than assuming either was right. Live measurement confirmed
D&D Beyond genuinely uses two small stacked inputs, one green under Heal,
one red under Damage. Asked; the owner chose to adopt the twin-input
layout. `HitPoints.tsx` now gives Heal, Damage and Temp each their own
amount field and submit button (`useAmountField` hook, one instance per
field) instead of sharing one across all three. New `--status-positive`/
`--status-negative` tokens color the Heal/Damage labels and field
borders. Verified live: healing via the dedicated field updated Current
and cleared the field after submit.

**Skill disadvantage indicator — deferred, not built.** The audit itself
flagged this could need real mechanical modeling rather than just an
icon. Confirmed: `SkillRow.tsx` has no data source for *which* skills are
disadvantaged (armor-stealth-disadvantage isn't modeled anywhere in this
app — no armor weight or strength-proficiency tracking to derive it
from). Adding an icon with nothing real behind it would be decoration,
not a fix. Deferred to the same tier as condition-effects-on-rolls,
stated rather than silently skipped.

**Ability-box/badge remeasurement — confirmed, no drift.** `getBoundingClientRect`
on D&D Beyond's own ability box: 81×95, pixel-identical to this app's
existing `.ability` box. The Proficiency/Speed stat badges use a fixed
74×74 owner-supplied SVG frame (the asset's own native size, not a CSS
guess); D&D Beyond's equivalent measures 94×95 including its caption
text, not a like-for-like shape to chase — left as-is rather than
distorting the owner's asset to match a differently-composed reference.

`dnd-5e-sheet-fidelity-audit.md`'s findings tables (Vitals zone top row,
Center column skills, Right column, Inventory tab) and the slice 12 entry
updated to reflect what was built, deferred, or confirmed already
matching.

**Not part of this slice, flagged for the owner:** three "Fix" findings
never got assigned to any of the audit's twelve slices and are still
open — the header's "MANAGE" button/equivalent, a Senses tab's plain-text
vision line (e.g. "Darkvision 60 ft." beyond the three passive scores),
and Inventory's active/equipped marker (icon vs. this app's letter-flag
buttons). Phase 10's own "done when" bar isn't fully met until these are
either built or added to "Deviations from D&D Beyond" as confirmed,
deliberate gaps — raised rather than silently left out or silently fixed
without the owner's sign-off on scope.

---

## 2026-08-16 — Phase 10, slice 10: Extras structured stat block

The largest slice in this phase, per the audit doc. Verified live against
D&D Beyond's own Extras panel (a Cat familiar on the reference character)
before proposing anything: confirmed the structured layout (size/type/
alignment line, AC/Initiative/HP/Speed lines, a two-column ability score
grid with score/modifier/save per row, Skills/Senses/Languages/CR lines,
then named Traits and Actions entries with plain prose — no roll buttons,
same inert treatment the Actions tab already gives Standard Actions), and
that the sidebar's Hit Points section starts collapsed until clicked.
Proposed the record shape to the owner before writing any code; approved
as proposed.

`Dnd5eExtra`'s free-text `statBlock: String` became `statBlock:
Dnd5eExtraStatBlock` — a new record carrying `size`/`creatureType`/
`alignment`/`hitDiceLabel`/`additionalSpeeds`/`abilityScores`/`skills`/
`senses`/`languages`/`challengeRating`/`traits`/`actions`, plus three
small nested records (`Dnd5eExtraAbilityScore`, `Dnd5eExtraSkill`,
`Dnd5eExtraStatEntry`). `armorClass`/`maxHitPoints`/`speed` stay on
`Dnd5eExtra` itself, unchanged — the row already reads them from there,
so the detail panel reads the same values instead of duplicating them.
Mirrored into the generic `ruleset` package (`ExtraStatBlock` and
friends) and `character` response DTOs, following the existing
`CalculatedValue`/`Contribution` two-package pattern rather than nesting
static classes. `Dnd5eSheetCalculator` derives each ability's `modifier`
from its `score` (the same formula already used for the character's own
abilities) and the whole stat block's `initiativeBonus` from its
Dexterity entry — neither is authored. `save` stays authored per ability:
a creature's saving-throw proficiencies are fixed Monster Manual numbers,
the same "fixed base numbers only" simplification this app already takes
for weapon and spell damage.

**A course correction caught before it shipped**: the live Cat panel
showed a separate "Initiative +2 (12)" line, which the initial proposal
modeled as an authored `initiativeBonus` field. Re-checking against
`dnd-5e-sheet-ui.md`'s own deviations list ("No 2024 content and no
legacy markers") before implementing — a stored initiative stat on a
monster is a 2024-rules concept; 2014-rules monsters don't have one, it's
always their own Dexterity modifier — so the field was dropped in favor
of deriving it from the ability score list instead, consistent with how
this app already derives the character's own initiative. Caught and fixed
before any code shipped, not after.

Frontend: `EntityDetailRequest` gained a second optional escape hatch,
`body?: ReactNode`, alongside the existing `actionBar` — a single
`description` string can't render an ability-score grid or named
trait/action sections. `EntityDetailPanel.tsx` renders `body` when
present, falling back to `description` otherwise (Extras is the only
`body` consumer so far). New `ExtraStatBlockView.tsx` composes the full
panel; `ExtraRow.tsx`'s row gained a size+type subtitle (e.g. "Large
Beast") from the same data — portrait thumbnail stays out, this app has
no portrait storage/upload for extras (a phase 11 concern, stated rather
than approximated). The Hit Points action bar now sits inside a
`<details>`, collapsed by default, matching the confirmed-live reference
behavior.

Rewrote the seeded Warhorse's stat block with its real SRD 5.1 stats (AC
11, HP 19 (3d10 + 3), Speed 60 ft., STR 18/DEX 12/CON 13/INT 2/WIS 12/CHA
7, no skills, Passive Perception 11, CR 1/2, one action — Hooves).
Verified live end to end against the refreshed local dev character
(`jsonb_set` against the local dev Postgres, same non-issue as slices 7–8
since every new field is a `String`/`List`, not a primitive): the row
shows "Warhorse — Large Beast", opening it shows the full stat block with
correctly derived ability modifiers (STR 18 → +4) and initiative (DEX 12
→ +1, "Initiative +1 (11)"), Hit Points starts collapsed and expands to
the working damage/heal/temp control on click.

`dnd-5e-sheet-fidelity-audit.md`'s Extras tab findings table and slice 10
entry, and `dnd-5e-sheet-build.md`'s Extras tab row, updated.

---

## 2026-08-16 — Phase 10, slice 9: Background characteristics (already done)

Reached the audit's slice 9 and found it already built: `Dnd5eBackground`
already carries all nine missing characteristic fields (gender, eyes, size,
height, faith, hair, skin, age, weight) plus Lifestyle, from slice 1's
"Background and notes" work on 2026-08-15 — that slice predated this
audit's own slice order reaching item 9, same situation as slice 5's
Inventory search. No code change; confirmed live that `BackgroundTab.tsx`'s
Characteristics section renders all ten fields on Aria Emberfall's sheet.
Corrected the audit doc's slice-order entry to match reality.

---

## 2026-08-16 — Phase 10, slice 8: Spells tab

The audit called this "purely a fix slice, no open scope question." Live
verification against dndbeyond.com/characters/50149479 found that wasn't
quite true for two of its four findings.

**Search — corrected, not fixed.** The audit guessed the search field was
full-text (name, casting time, damage type, condition, tag) from D&D
Beyond's own placeholder copy, never tested live. Typing "necrotic",
"bonus action", "touch" and "Radiant" into the real character's spell
search — all values genuinely present on visible spells — returned zero
results every time; only a name substring ("bolt") matched anything.
D&D Beyond's real search is name-only despite its placeholder claiming
otherwise, which is what this app already had. No code change; corrected
the audit's finding instead of building a duplicate full-text search.

**Filter chip icons — identified live.** The two unlabeled icons next to
the level chips are a diamond (tooltip: "Concentration") and a book/scroll
(tooltip: "Ritual"), each toggling the list to that subset. Added
`spell-toggle` buttons in `SpellsTab.tsx` filtering `sheet.spells` by the
existing `concentration`/`ritual` booleans — no new field needed, confirmed
live that toggling "C" narrowed Aria Emberfall's list to just Detect Magic.

**Per-class header — built.** Replaced the one-boxed-card-per-class layout
with three compact stat rows (Modifier/Spell Attack/Save DC), each joining
every spellcasting class's value with " | ", matching D&D Beyond's own
"+4 | +1" format.

**Table columns — the real surprise.** D&D Beyond's Name/Time/Range/Hit-DC/
Effect/Notes columns needed data this app never tracked: `Dnd5eSpell`
gained `castingTime`, `range`, `notes` (components/duration, written out
in full rather than D&D Beyond's abbreviated codes) and `effectSummary` (a
short tag — "Buff"/"Healing"/"Detection"/"Control"/"Damage" — shown only
when a spell has no damage roll to show instead). Threaded through the
generic `Spell` type, `SpellResponse`, `Dnd5eSheetCalculator`,
`Dnd5eSheetMutator` (`learnSpell`/`withPrepared`), `SpellCatalogueData` and
`CharacterSheetService.toSpell`. Authored both fields for all 3 of
`DevCharacterSeeder`'s embedded spells and all 11 `content/dnd-5e/spells/*.json`
catalogue entries, sourced from SRD 5.1 mechanics in this project's own
phrasing (not copied from D&D Beyond's proprietary text).

Hit/DC and Effect are derived at render time, not stored: an attack-roll
spell shows its roll trigger (attack bonus) under Hit/DC and its damage
roll under Effect; a save-based damage spell (`attackRoll` false, damage
present — e.g. Sacred Flame) shows the class's flat save DC as plain text
under Hit/DC (the target rolls, not the caster) and its damage roll under
Effect; anything with neither shows a dash and `effectSummary`. Verified
this derivation covers every spell this app currently models — a future
save-effect-without-damage spell (e.g. Hold Person) would need a real
`hasSaveDc` field, deliberately not added now since nothing in this app's
content needs it yet.

Concentration/ritual markers moved from a separate column to sit directly
after the spell name (matching D&D Beyond's own placement); a fixed-width
name cell replaced the flexible one after two-line name wrapping visually
collided with the Time column in testing.

Verified live end to end against a refreshed Aria Emberfall (the local dev
character's `spells` JSON predates these fields — a `String`, not a
primitive, so it deserializes to `null` with no crash, same non-issue as
slice 7's `summary` field; refreshed via `jsonb_set` against the local dev
Postgres to see real authored content rather than nulls): Fire Bolt's
attack roll ("+3") resolved a real server roll into the dice tray, Mage
Armor and Detect Magic showed "–" and their `effectSummary` tags, and the
Concentration toggle correctly narrowed the list to Detect Magic alone.

`dnd-5e-sheet-fidelity-audit.md`'s Spells tab findings table and slice 8
entry updated to reflect what was actually built vs. corrected.

---

## 2026-08-16 — Phase 10, slice 7: Features & Traits summary/detail split

The audit flagged this as a "verify live first" item: build the split only
if D&D Beyond's inline text turns out to be a genuinely shorter summary,
not the same text as the sidebar. Verified live against
dndbeyond.com/characters/50149479's Spellcasting entry: the inline text
("You can cast prepared cleric spells using WIS...") is two sentences; the
sidebar panel opened by clicking the name shows several paragraphs across
multiple headed sections ("Cantrips", "Preparing and Casting Spells",
"Spellcasting Ability") — a real split, not a formality.

Added `summary` alongside the existing `description` on
`Dnd5eFeatureTrait`, the generic `FeatureTrait` ruleset type, and
`FeatureTraitResponse`; `Dnd5eSheetCalculator.featureTraits()` and
`Dnd5eSheetMutator.withUsedCount()` updated to carry it through.
`FeatureTraitRow.tsx` now renders `feature.summary` inline instead of
`feature.description`; `FeaturesTab.tsx`'s Entity Detail call was already
passing `description`, so the sidebar picked up the full text with no
change needed there. All call sites across three test files updated for
the new constructor arity.

Authored both a short summary and a fuller, originally-phrased rules
description (based on open SRD 5.1 mechanics, not copied from D&D
Beyond's own proprietary text) for each of `DevCharacterSeeder`'s 7 seeded
features (Second Wind, Action Surge, Fighting Style: Defense, Extra
Attack, Darkvision, Dwarven Resilience, Alert). The one existing local
dev character predates the field — `summary` deserializes to `null` for
old data, no crash (a `String`, not a primitive, so the earlier
attunement-style deserialization bug doesn't apply here) — but to verify
the real authored content live, its `featureTraits` array was refreshed
in place via a direct `jsonb_set` against the local dev Postgres
(disposable seed/test data, all `usedCount` were 0, nothing else in the
sheet touched).

Confirmed live: the Features tab's row now shows the short summary, and
opening "Second Wind" shows the full multi-sentence description in the
sidebar — the split renders correctly end to end.

`dnd-5e-sheet-fidelity-audit.md`'s Features and traits table and slice 7
entry updated to reflect this as built rather than "verify live."

---

## 2026-08-16 — Phase 10, slice 6: Actions tab

Two items from the audit's Actions tab findings.

Dropped the duplicate Attack/Damage roll buttons from `ActionsTab.tsx`'s
attack `actionBar` (open question 3, resolved): rolling was already
available inline on the table row (`AttackRow.tsx`'s hit/damage buttons),
and the sidebar copy just repeated the same two rolls. Verified live
against dndbeyond.com/characters/50149479's own Warhammer entity detail
panel: labeled metadata only (Proficient/Attack Type/Reach/Damage/Damage
Type/Weight/Cost/Properties/Source), no roll buttons, only
Unequip/Move/Delete — this app has no equivalent mutations for attacks, so
the panel now shows metadata and notes only, `actionBar` omitted entirely
rather than left empty. `formatModifier`, only used by the removed button
labels, was removed from `ActionsTab.tsx` as dead code.

Verified live whether D&D Beyond's Actions tab genuinely duplicates
passive class features already covered by Features & Traits: it does —
Channel Divinity and Divine Sense (Helga Flinthand's Paladin/Cleric
multiclass) render with the same full description in both the Actions tab
and Features & Traits tab, not a summary/detail split. This matches the
app's existing behaviour (`sheet.featureActions` rendered inline via
`FeatureActionRow` in Actions' own "Features" section, full text, also
present in `FeaturesTab.tsx`) — confirmed as already correct, no rebuild
needed.

`dnd-5e-sheet-fidelity-audit.md`'s Actions tab findings table and slice 6
entry updated to reflect both as done/matched.

---

## 2026-08-16 — Tabbed section: content escaping the frame, fixed at the root

Follow-up to the "fixed the real reason the frame didn't fit" entry below.
The owner reported the tabs and rest of the content still rendered outside
the frame after that fix.

Root cause: `.tabbed-section` sets `width: 408px; aspect-ratio: 784/829`,
but it's also a flex item inside `Dnd5eCombatColumn`'s column-direction flex
container. A flex item's automatic minimum size defaults to its content's
size unless told otherwise — since `.tabbed-section` itself had no
`min-height`/`overflow` override, flexbox let it grow to whatever height
its tab content needed (measured 656px) instead of respecting the
aspect-ratio (431px expected). The mask-based frame border was drawn at
that stretched size instead of its native proportions, so it visually
failed to contain the content — the same class of bug as the very first
frame-fit issue, just one level removed. Fixed with `min-height: 0;
overflow: hidden;` on `.tabbed-section`, which is now confirmed to render
at exactly 408×431 regardless of content.

While investigating, checked live how D&D Beyond's own `.ct-primary-box`
handles content that doesn't fit: the outer box is `overflow: hidden` on
both axes (matching the fix above), but a separate inner content div
(`.styles_content__QjnYw`) carries `overflow-x: auto; overflow-y: auto` —
both axes, not just vertical (scrollHeight 2250 against a 551px client
height, confirmed genuinely overflowing on their own reference character).
`.tabbed-section__content` gained the matching `overflow-x: auto` it was
missing (it already had `overflow-y: auto`).

Also fixed per the owner's request to reduce horizontal pressure and match
D&D Beyond's real values: tab labels shortened ("Features & Traits" →
"Features", "Background & Notes" → "Background" — D&D Beyond actually
splits Background and Notes into two separate tabs, seven total; this app
keeps them combined, so "Background" alone still describes the tab).
`TabBar.tsx` restyled to D&D Beyond's own measured values (live
`getComputedStyle` on `.styles_tabButton__wvSLf`): 14px/0.875rem, 700
weight, uppercase, normal letter-spacing, `padding: 3px 0` — replacing the
prior session's ad hoc 8px shrink-to-fit attempt, since fitting is no
longer the point now that overflow scrolls instead of needing to be
avoided. The six tabs don't all fit in this box's narrower 408px width
even with the shorter labels; that's expected and matches D&D Beyond's own
approach of relying on scroll rather than shrinking text to force a fit.

---

## 2026-08-16 — Tabbed section: fixed the real reason the frame didn't fit

Follow-up to the same session's "Reverted dnd_frame_actions.svg" entry
below. The owner pushed back on accepting the wide/short shape as fixed:
"não precisamos de uma seção de actions, spells, etc desse tamanho...
temos muito espaço morto" — the tabbed section didn't need to be that
size at all.

Root cause: `Dnd5eTabbedSection` rendered as a sibling below the entire
three-column `Dnd5eVitalsColumns` row, stretching to the page's full
content width (the measured 1833x552) even though the three columns above
it only need ~1000px combined — the extra ~800px was pure dead space, not
something the frame's asset was ever going to fit into. The Screen Layout
diagram in `dnd-5e-sheet-build.md` had actually specified the tabbed
section as part of the third column all along; the implementation just
never matched it.

Fixed by moving `Dnd5eTabbedSection` inside `Dnd5eCombatColumn`, stacked
below Defenses/Conditions — the same placement D&D Beyond uses (its own
tab box sits under its combat column, not spanning the page). That gives
it a real, bounded 408px width (matching `.defenses-conditions-panel`)
instead of the viewport's, so the previously-reverted frame now fits
cleanly at its native `aspect-ratio: 784/829`, same fixed-box-plus-
internal-scroll pattern (`overflow-y: auto`) every other panel already
uses. Threaded `onMutate`/`onOpenDetail`/`onOpenSpellManagement`/
`backgroundSuggestions`/`onUpdateBackgroundField`/`onOpenTextField`
through `Dnd5eVitalsColumns` and `Dnd5eCombatColumn` to reach the
relocated component; removed the standalone full-width block and its
import from `CharacterSheetScreen.tsx`.

Two overflow bugs surfaced at the new width and were fixed: `TabBar.tsx`
needed a smaller font (11px → 8px) and tighter padding to fit all six tab
labels — including "Features & Traits" and "Background & Notes" — on one
line, `flex-wrap` kept as a safety net; `.inventory-add`'s five-field add
form needed `flex-wrap` so its Notes field and Add button stopped clipping
past the frame. Verified live across all six tabs at the new width — no
clipping, frame renders as a clean outline, matches D&D Beyond's own
proportions far more closely than the full-width version ever could.

---

## 2026-08-16 — Attunement rework: not every item is attunable

The owner flagged a real gap: the Inventory tab treated every item as
attunable via a per-row "A" checkbox next to "E" (equip), but not every real
item requires attunement. Fixed by adding a `requiresAttunement` flag, set
once when an item is added and never toggled later, and by moving attunement
out of the equip row into its own dedicated section, matching D&D Beyond —
confirmed live against dndbeyond.com/characters/50149479's own "Attunement"
filter, which showed attuned items separate from attunement-eligible items,
not a checkbox next to equip at all.

Backend: `Dnd5eItem`, `Item` and `ItemResponse` gained `requiresAttunement`;
`SheetMutator.addItem`/`Dnd5eSheetMutator.addItem` take it as a new parameter;
`toggleItemAttuned` now throws a new `AttunementNotAllowedException` (400,
same RFC 7807 treatment as every other domain exception) when attuning an
item that was never flagged, checked before the existing 3-slot
`AttunementLimitExceededException`. Seed data gained a second
attunement-eligible item (Ring of Protection, not yet attuned) alongside the
already-attuned Cloak of Protection, so both of the new section's states
render out of the box.

Frontend: `ItemRow.tsx` lost its "A" flag button and the attune action in its
Entity Detail action bar (equip only now), and shows a small marker next to
an attuned item's name instead. New `AttunementSection.tsx` renders below the
item list: two columns, "Attuned Items" and "Items Requiring Attunement",
each row a plain Attune/Break attunement button. Deliberately not D&D
Beyond's catalog-backed numbered-slot picker (drag an item into one of three
fixed slots) — this app's flat free-text item model has no catalog and no
slot identity to assign, so a button per eligible row stands in for it. The
add-item form gained a "Requires attunement" checkbox.

Updated `dnd-5e-sheet-ui.md`'s Inventory section and added a new
`dnd-5e-sheet-build.md` row for this rework (the original phase 8 Inventory
row is left as historical record, not rewritten).

**A real bug was caught during live verification**: an existing character
(created before this change, with items already persisted) failed to load
at all — 500, `MismatchedInputException: Cannot map null into type boolean`
— because its stored item JSON predates `requiresAttunement` and the field
is a primitive. `@JsonSetter(nulls = Nulls.SKIP)` was tried first and did
**not** fix it: that annotation governs an explicit JSON `null`, not a
property that's entirely absent, which is Jackson's actual `getAbsentValue()`
path and still consults `FAIL_ON_NULL_FOR_PRIMITIVES` regardless. Fixed
properly with a new `Dnd5eSheetJsonMapper.lenient()`, rebuilding the shared
Spring-managed `ObjectMapper` with that one feature disabled, used only by
`Dnd5eSheetMutator` and `Dnd5eSheetCalculator` (not globally — the shared
mapper still validates HTTP request bodies strictly). Confirmed live: the
previously-broken character loaded again, its pre-existing Cloak of
Protection stayed attuned with `requiresAttunement` defaulted to `false`
(expected — it can't be re-attuned once broken, since old data never
recorded the flag), and a newly-added Ring of Protection went through the
full add → appear in "Items Requiring Attunement" → Attune → Break
attunement cycle correctly.

---

## 2026-08-16 — Reverted dnd_frame_actions.svg on the tabbed section

Tried wrapping `Dnd5eTabbedSection.tsx` (Actions/Spells/Inventory/Features &
Traits/Background & Notes/Extras) in `dnd_frame_actions.svg`, after DOM
inspection of D&D Beyond found its `.ct-primary-box` does carry this asset as
a subtle outline. Reverted the same session: live measurement showed this
app's tabbed section renders far wider relative to its height (1833×552,
≈3.32:1) than the asset's native, nearly-square aspect (784×829, ≈0.95:1) —
stretching it broke the border into two disconnected vertical lines instead
of a frame around the content. Unlike Defenses/Conditions or Hit Points, this
wasn't a tunable padding/height mismatch; the native ratios are too far apart
for any stretch to survive. Removed the `--f-tabs` token and the
`.tabbed-section`/`.tabbed-section__content` CSS along with the wrapper
markup, restoring the plain `<div>` root. Left unwired pending the owner's
direction (e.g. a narrower tab column, or a differently-shaped asset) rather
than shipping a visibly broken frame.

---

## 2026-08-16 — Phase 10, slice 5: Inventory search (already done)

Reached the audit's slice 5 and found it already built: `InventoryTab.tsx`
already wires `sheet/SearchField.tsx` (the same primitive the Spells tab
uses) to filter `sheet.items` by name, added in an earlier phase 8 slice
to close that phase's own "every list supports search and filtering" line —
the audit's finding predated that addition and was stale by the time this
session reached it. Confirmed live: typing "shield" into Inventory's search
box filtered the list down to just the Shield. No code change; corrected
the audit doc's finding and slice-order entry to match reality instead of
building a duplicate.

---

## 2026-08-16 — Defenses/Conditions: Conditions column alignment fix

Third follow-up: the owner reported Conditions still wasn't matching
Defenses — its title and trigger weren't aligned with each other, and asked
for the column to sit 10px from the divider (later corrected to 16px in the
same session).

Two separate bugs, not one. First: `.defenses-conditions__trigger` is a
`<button>`, and `.reveal` (its other class) resets border and background but
never padding or margin — the browser's own default button padding was
offsetting it from the `<div className="panel__title">` above it, which has
no such default. Fixed with an explicit `padding: 0; margin: 0; display:
block; width: 100%` on the trigger, matching the title's own box exactly.
Second: `.defenses-conditions` still had a `gap: 16px` between its three flex
children (column, divider, column) left over from the original layout —
that gap was adding to whatever padding-left `.defenses-conditions__col` had,
so the Conditions column was 26px from the divider, not 10px. Removed the
row's `gap` entirely and gave each column explicit, independent spacing
instead: Defenses keeps its 10px `padding-left` (from the panel's own edge)
plus a new 16px `padding-right` (replacing what the gap used to provide on
that side); Conditions' `padding-left` — initially set to 10px, then the
owner asked for 16px instead — is now the only thing separating it from the
divider, with nothing added on top.

Verified live: title and trigger now share one left edge in both columns,
Conditions sits 16px from the divider, Defenses is unchanged from the
owner's earlier "good" confirmation, and the sidebar trigger still opens the
Conditions panel correctly.

---

## 2026-08-16 — Defenses/Conditions: owner-tuned label position

Second follow-up: the owner tested `padding-left: 10px; margin-top: -10px`
on `.defenses-conditions__col > .panel__title` directly in DevTools and
asked for that exact pair, then flagged that the column's content (the
resistance rows, the immunities/vulnerabilities list, the conditions
trigger) needed the same left offset to stay aligned with the now-shifted
title.

Applied `margin-top: -10px` to the title as tested. `padding-left: 10px`
went on `.defenses-conditions__col` itself rather than the title alone, so
every child — title and content alike — shares one left edge automatically;
adding it to the title in isolation would have needed the same value copied
onto `.panel__rows`, `.panel__list` and `.defenses-conditions__trigger`
individually, with no single place enforcing they stay equal. Verified live:
both columns' text now starts at the same x position, and the sidebar
trigger still opens the Conditions panel correctly.

---

## 2026-08-16 — Defenses/Conditions: label styling and divider height

Follow-up to the previous entry — the owner flagged that moving the labels
to the top still didn't match D&D Beyond, and asked for the divider to run
slightly taller.

Measured `getComputedStyle` on `.ct-combat__summary-label` rather than
eyeballing it again: `text-align: start` (left, not centered),
`color: rgb(0,0,0)` (primary/black, not the muted gray every other panel's
`.panel__title` uses), `letter-spacing: normal` (not the wide tracking
`.panel__title` applies everywhere else). These three properties are a real,
confirmed exception for this one section, not a guess — overridden via
`.defenses-conditions__col > .panel__title` rather than touched on the
shared `.panel__title` rule, since every other panel's title genuinely is
centered and muted.

Divider: D&D Beyond's own (a `border-left` on the conditions group) runs
from y=10 to y=85 inside a 95px container — inset from both edges, not
flush. Our divider already had some inset from the panel's own padding, but
the owner wanted it a touch taller still; added `margin: -4px 0` to
`.defenses-conditions__divider` so it extends slightly past the
`align-items: stretch`-derived column height on both ends instead of
matching it exactly.

Verified live: labels now read left-aligned and black, divider visibly
taller, the sidebar trigger still opens the Conditions panel correctly.

---

## 2026-08-16 — Defenses/Conditions: column titles moved to the top

The owner asked for the "Defenses"/"Conditions" column titles to sit at the
top, like D&D Beyond.

Checked live rather than trusting the existing code: this component's own
doc comment claimed the bottom-pinned position (`margin-top: auto`) already
matched the reference, but that was never actually verified —
`getBoundingClientRect` on D&D Beyond's `.ct-combat__summary-label` puts
both labels at y=10 inside a 95px-tall container, i.e. the top, not the
bottom. Moved each `<div className="panel__title">` to be the first child
of its column instead of the last, and replaced
`.defenses-conditions__col > .panel__title`'s `margin-top: auto;
padding-top: 12px` with a plain `margin: 0 0 8px` — normal block flow
already puts a first child at the top, no auto-margin trick needed.
Corrected the doc comment's claim at the same time so it no longer asserts
something nobody had measured.

---

## 2026-08-16 — Conditions panel: real toggle switches

The owner reviewed the just-shipped Conditions sidebar panel and asked for
a proper toggle, like D&D Beyond's own.

The first version reused the vitals zone's old dot-indicator styling
(`.condition-row__dot`, a bordered circle that fills solid when active) —
correct in spirit (a mutate target, toggled on click) but not what the
reference actually shows. Checked live: D&D Beyond's Conditions panel uses a
sliding pill switch per row (`getComputedStyle` on `.styles_toggle__nDukS`:
a 32x14px rounded track, a 20px circular knob that slides between the two
ends, darkening slightly when on) — and its list is single-column, full
width, not the two-column grid ours used.

Rebuilt `.condition-row` accordingly: `.condition-row__switch` (a 30x16px
pill, `--border-control` track) containing `.condition-row__knob` (a 12px
circle, `--frame-paper` white with a `--text-muted` ring when off, sliding
14px right and filling `--frame-ink` when on, both via `transition:
transform`). `.condition-list` dropped its `grid-template-columns: 1fr 1fr`
for a plain single-column flex stack, with a hairline `border-bottom`
between rows instead — the two-column layout only ever existed to fit
fourteen rows into the old inline checklist's narrow vitals-zone column,
which stopped applying the moment that checklist moved into the sidebar
(previous entry) and gained the sidebar's full width to work with.

Verified live: toggled Blinded on from the sidebar — the knob slid right
and filled dark, the trigger's summary on the main sheet updated to
"Blinded, Prone" immediately — then toggled it back off, confirming the
seed character's test data was left as found. `npx tsc --noEmit` clean.

---

## 2026-08-16 — Phase 10, slice 4: Conditions moved into the sidebar

Per the audit's resolved open question 8 and its own slice order.

The vitals zone's Defenses/Conditions panel used to render all fourteen
conditions as an always-visible two-column toggle checklist. D&D Beyond
instead shows a compact "Add Active Conditions" prompt (or a summary of the
active ones) that opens a sidebar panel with the same toggle list — moved
this app to match exactly.

**New eighth mold, Conditions** (`ui-design-system.md`), added for the same
reason Spell management and Text field were: the existing Collection editor
mold's shape (free-text add/remove against an unbounded set) doesn't fit a
fixed, known-in-advance list of booleans toggled in place, with no
add/remove or search semantics at all. `ConditionsRequest`
(`{title, conditions: {key,label,active}[], onToggle}`) and
`sheet/ConditionsPanel.tsx` are the smallest mold yet — a title and a toggle
list, reusing the vitals zone's own `.condition-list` CSS grid unchanged.
`systems/dnd5e/ConditionRow.tsx` (the old inline component) was deleted
rather than imported into the new panel — `sheet/` molds stay
system-agnostic, so `ConditionsPanel.tsx` inlines its own toggle-row markup
instead of reaching into `systems/dnd5e/` for a component.

`DefensesConditionsPanel.tsx`'s Conditions column now renders a `.reveal`
trigger — `activeLabels.join(', ')` when any are active, "Add Active
Conditions" otherwise — wired to a new `onOpenConditions` prop threaded down
from `CharacterSheetScreen.tsx` (`Dnd5eVitalsColumns` → `Dnd5eCombatColumn`).
`onMutate` dropped from both of those components entirely: Conditions was
its only consumer, so once the toggle moved into the sidebar there was
nothing left in that chain to mutate anything with. Verified live: toggled
Charmed on from the sidebar — the sidebar's own dot and the trigger's
summary ("Charmed, Prone") updated together immediately (`sheet.activeConditions`
re-derives `ConditionsRequest.conditions` on every render, the same
live-refresh treatment the collection editor and log already get) — then
toggled it back off to leave the seed character's test data as found.

**A worthwhile side effect:** `.defenses-conditions-panel` had a
`min-height: 220px` override, added specifically because the old inline
checklist needed real height for a 7-row grid — the asset's own native
aspect ratio (513/121, a short bar matching the reference's *compact* state)
couldn't fit it. That checklist is gone now, so the override no longer
applies; reverted the panel to `aspect-ratio: 513/121` like every other
section panel, confirmed live that Aria's own Defenses content (one
resistance, no immunities/vulnerabilities) fits comfortably inside the
now-shorter frame with no overflow.

`npx tsc --noEmit` clean throughout.

---

## 2026-08-16 — Saving throws grid: fine alignment fixes

Two owner-flagged details in the new 2-column grid, fixed after live review.

**Proficiency circle recentered on the row frame's edge.** `--prof-x` moved
from 5% to 2.5% — the circle's center now sits on the row frame's own left
edge line instead of noticeably right of it.

**Section title dropped to the panel's base.** `.saving-panel__content`
gained `display: flex; flex-direction: column`, and `.panel__title` inside
it gained `margin-top: auto; padding-top: 12px` — the title used to sit
right after the grid in normal block flow (a fixed 12px gap), leaving ~40px
of unused space below it before the panel's own bottom edge (that space
exists because the panel's aspect-ratio-driven height comfortably holds six
stacked rows, but the 2-column grid only needs three). Now the title is
pinned to the bottom with a small, deliberate gap instead of floating with a
large gap above it and none below — same `margin-top: auto` technique
Defenses/Conditions already used for its own two column titles.

**Unchecked circle's white backdrop.** Centering the circle on the frame's
edge (above) exposed a second issue: the frame's own line ran straight
through the unchecked circle's middle, visible because
`dnd_icon_unchecked_circle.svg` is a thin ring — mostly transparent, so
nothing was painted to block the line behind it. Fixed by giving
`.saving__prof` an opaque `background: var(--frame-paper)` (white) as its
base, the same "paint a solid circle to erase what's behind it" technique
`.saving__prof--on` already used with ink instead of paper — the ring icon
now draws on top of that white backdrop rather than on top of nothing.

---

## 2026-08-16 — Phase 10, slice 3: Saving throws 2-column grid

Per the audit's slice order and its resolved open question 1.

A two-column layout was tried and reverted twice before this project had
per-section frame assets: widening the shared ornate panel frame to fit two
row badges side by side stretched its corner ornaments. The phase 10 asset
refactor made this moot — Saving Throws now has its own dedicated panel
frame and each row has its own dedicated frame, so there's no shared asset
left to stretch by widening the panel. Confirmed live before sizing anything
(`getBoundingClientRect` on D&D Beyond's `.ct-saving-throws-box`): 281×200,
two 107px-wide columns 13.5px apart, three 34px rows 5px apart — close
enough to this app's own panel (278px wide, ~199.5px tall from its frame's
own aspect ratio) that no panel resizing was needed, only rearranging the
six rows.

`SavingThrowsPanel.tsx` swapped `.panel__rows` (flex column) for a new
`.saving-panel__grid` (`display: grid; grid-template-columns: 1fr 1fr;
grid-auto-flow: column`) — `grid-auto-flow: column` fills column 1 with the
existing `ABILITIES` array's first three entries (STR/DEX/CON) then column 2
with the rest (INT/WIS/CHA), so the array needed no restructuring to produce
the reference's own grouping. `.saving` (the row) changed from a fixed
132px width to `width: 100%`, so it fills its grid cell and stays in sync
with the panel's own width automatically instead of needing to be
hand-tuned to match.

Left out, matching the audit's own scoping: the conditional-bonus line D&D
Beyond shows below the grid (e.g. "against Poison", from a species trait
like Dwarven Resilience) — not modeled, deferred at the same tier as
condition-effects-on-rolls, already in `dnd-5e-sheet-ui.md`'s "Deferred to
later versions." The panel currently has a little empty vertical space
before its title where that line would go — cosmetically loose but not
broken, and the reference itself reserves the same space for it.

---

## 2026-08-16 — Phase 10, slice 2: Entity Detail metadata shape

Per the fidelity audit's resolved open question 4 and its own slice order,
done before touching any individual tab's Entity Detail content since every
consumer shares the shape.

`EntityDetailRequest.metadata` changed from `string[]` (joined into one
inline line, no label) to a new `EntityDetailMetadataEntry[]`
(`{ label: string; value: string }`), rendered by `EntityDetailPanel.tsx` as
a `<dl>` of labeled pairs — confirmed live against D&D Beyond's own attack
panel, which labels every field (Range, Damage Type, etc.) rather than
joining them into a line with no context. A new optional `tags?: string[]`
was folded into the same slice, cheap once the shape change was in place
(per the audit's own contingency): flag-like badges with no paired value — a
spell's Concentration/Ritual markers — render as their own small badge row,
kept out of `metadata` rather than forcing an awkward "Concentration: Yes"
into a label/value pair that doesn't fit it.

All five consumers updated together and verified live, one click-through per
entity type: `ActionsTab.tsx`'s attack row (Range, Damage Type) and feature
action row (Action Type, Recharge — the latter only when the feature
actually has a recharge trigger), `SpellsTab.tsx` (Level/School as metadata,
Concentration/Ritual as tags), `ItemRow.tsx` (Quantity, Cost — the latter
only when non-empty), `FeaturesTab.tsx` (Source, Category), `ExtraRow.tsx`
(Type). `npx tsc --noEmit` clean throughout.

Left out, per the audit's own scoping: no new metadata fields were added
beyond what each entity already tracked (D&D Beyond's attack panel also
shows Proficient/Attack Type/Weight/Cost/Properties/Source, none of which
any `Attack` carries yet) — this slice only changed the shape of what
already existed, not what data exists. Adding those fields, if ever done, is
a separate concern for whichever slice models that data.

---

## 2026-08-16 — Phase 10 asset refactor follow-up: alignment fixes

The owner reported content overflowing its frame after the previous entry's
asset refactor, with the saving throw row given as an example, and asked for
DevTools-level inspection rather than guesswork, plus a warning against
forcing every section into the same symmetric fix.

**Investigated and ruled out first.** Tried "fixing" every new SVG's
`viewBox` (2×–6× larger than its own `width`/`height`) to match those
attributes exactly, since a quick `getBBox`/`getCTM` measurement made the
inflated viewBox look like a bug. Reverted immediately: the artwork rendered
3-6× oversized and bled across neighboring components once the fix landed —
the inflated viewBox is load-bearing (it compensates for a fixed internal
`scale(0.1)` transform each file's `<g>` carries from its export tool) and
every file already renders its artwork filling its own declared
`width`×`height` correctly. Confirmed via a canvas pixel scan of the saving
throw row rendered at its live 132×34 box: the pill-and-circle shape closes
correctly, own alignment (`--mod-x: 82.5%` vs. the measured circle center at
81.8%) was already good — the reported example turned out fine once measured
precisely, not a real bug.

**Real bug found: `.hit-points` broke visibly.** `dnd_frame_hit_points.svg`
is a wide banner (400/113 ≈ 3.54 native ratio). The component's old vertical
stack (fraction, temp, caption, one amount field, three buttons) made the box
nearly square once sized to fit that content — stretching a wide banner into
a near-square box distorts it badly enough that the border visibly stops
closing, which a canvas render at the live box size confirmed directly (the
rounded end-cap ballooned to dominate the whole shape, the opposite end
never rendering at all). Checked D&D Beyond's own Hit Points box live
(`getBoundingClientRect` on `.styles_hitPointsBox__iqcr7`): 317×88.6, a 3.58
ratio matching the asset's own 3.54 closely, because its content is laid out
**horizontally** — Heal button, one shared amount field, Damage button on the
left; Current/Max/Temp stats on the right; "Hit Points" centered below both.
Asked the owner whether to port that composition or drop the new frame for
hit points; the owner chose porting it. `HitPoints.tsx` rewritten to match,
widened from 132px to 240px. D&D Beyond's own Temp value is itself a
clickable button (`styles_valueButton`, same as Current) — reused as this
app's third mutate action (`onSetTemporary`) so the app's three actions
(damage/heal/temp) still land on the reference's two-button left column
without inventing a fourth stacked control. Confirmed the wider box still
fits both the vitals top row (no wrap) and the 280px sidebar (`ExtraRow.tsx`
reuses `HitPoints.tsx` unchanged).

**Second bug found, a silent regression: hit dice lost its border.**
`HitDice.tsx` had always rendered `className="hit-points"` directly (no
`frame-box` class, no paper/ink layers) purely to inherit `.hit-points`'s old
*plain CSS border* for its own typography classes (`.hit-points__fraction`
etc., used with no `.hit-points__content` wrapper). Converting `.hit-points`
to the SVG mask technique removed that plain `border` property, leaving hit
dice with no visible frame at all — invisible in a screenshot review since
nothing crashed, only caught by checking hit dice specifically after fixing
hit points. Fixed by giving it its own `.hit-dice-box` class with the
original plain-border styling, decoupled from `.hit-points` entirely.

Left out: the pre-existing "PROFICIENCY BONUS"/"WALKING" label overflow in
the ability-row badges — a single unbreakable word overflowing its 65%-width
text container, unrelated to any frame asset and present before this
refactor; not touched, since it isn't a regression from either asset-refactor
session.

---

## 2026-08-15 — Phase 10 asset refactor: per-section frame SVGs

The owner supplied 16 new SVGs (11 section frames, 5 icons) at
`dnd frames secao/svg` and asked for the frame system to be refactored around
them, before continuing phase 10's slice order — with explicit exclusions
(ability box/modifier frame, individual senses-item frame, Heroic Inspiration
frame stay as-is) and a technique to replicate: the reference's own
checked/unchecked proficiency-circle swap.

**Confirmed with the owner first, via `AskUserQuestion`, before touching
code:** (1) keep long/short rest buttons' existing label deviation and add the
reference's icon before the text, rather than dropping the label; (2) scope
`dnd_icon_dice_roll.svg` to a future custom dice-roll button, not every
existing roll affordance; (3) delete the frame assets and components left
orphaned by the migration rather than keep them around unused.

**The CSS mask technique needed no architectural change.** The app already
recolors frames via two stacked `frame-box__paper`/`frame-box__ink` divs, each
`mask-image`-masked by the same SVG and filled with `--frame-paper`/
`--frame-ink` — `mask-image` only reads an SVG's alpha shape, never its own
`fill`, so the owner's new `fill="currentColor"` assets work with the existing
technique unmodified. No inline SVG, no new build tooling.

**Every new frame is one fixed-aspect-ratio SVG, not a stretchy stack.**
Saving Throws, Skills, Senses, Proficiencies & Training and Defenses/
Conditions previously shared two generic, height-flexible components
(`Panel.tsx`'s cap+middle stack, `Panel9Slice.tsx`'s true 9-slice) because
none had its own asset. The owner's new set cuts one full frame per section
instead, matching the reference's own fixed-size boxes (which scroll or size
their *content*, not their frame) — so a new `SectionPanel.tsx` replaces both
old components, which are deleted along with their now-orphaned crop assets
(`dnd_frame_features_{cap,middle}.svg`, `dnd_frame_proficiencies_{cap,middle,
corner,top_edge,side_edge}.svg` — zero remaining consumers, confirmed by grep
before deleting). Skills' and Proficiencies & Training's content scroll
internally (`overflow-y: auto`) since real content can outgrow a fixed frame.

**One real bug found and fixed during live verification.** Defenses/
Conditions' new asset's native aspect ratio (513/121, a short bar) matches the
reference's own *compact* state — no conditions shown inline. This app
deliberately lists all fourteen conditions at once (`DefensesConditionsPanel`'s
own doc comment), which needs real height for a 7-row two-column grid; capping
the frame to the native ratio clipped most of the panel's content invisibly
outside its own border. Fixed by dropping the aspect-ratio lock for this one
panel (`min-height: 220px`, content sizes the frame via normal flow instead of
`height: 100%`) — every other section panel keeps its native ratio.

**Checked/unchecked proficiency markers now use `dnd_icon_unchecked_circle.svg`**
for saving throws and skills (not conditions, which the owner didn't name)
instead of a CSS border-circle. The *checked* state stays a plain solid
ink-colored circle (already the existing "on" treatment) rather than injecting
an HTML `<circle>` element the way the reference does — this app renders no
raw inline SVG anywhere, so a solid-filled div is the equivalent adaptation,
surfaced to the owner as a deliberate translation rather than assumed
silently. Heroic Inspiration's *on* dot got the same icon treatment
(`dnd_icon_inspiration_on.svg`, `mask-size: contain` since the icon isn't
circular) — its *off* state and its own outer frame are unchanged.

**Two assets landed but stayed unwired, flagged rather than guessed at:**
`dnd_frame_actions.svg` ("the section with tabs, like actions, spells, etc")
— a live D&D Beyond check found no ornate border around that content at all,
so its intended target is still unclear; and `dnd_icon_dice_roll.svg` (a
"customized dice roll" button) — no manual-roll UI exists yet to attach it to
(`dnd-5e-sheet-ui.md`'s "Deferred to later versions"). Both are copied into
`public/frames/svg/` with CSS variables (`--f-tabs`, `--i-dice-roll`) ready
for whenever their target is confirmed or built.

**Renamed, not replaced:** the individual senses-item frame the owner said to
keep was previously *also* named `dnd_frame_senses.svg` on disk, colliding
with the owner's new panel-level asset of the same name — renamed to
`dnd_frame_sense_item.svg` before copying the new file in, so `SenseRow.tsx`
needed no code change beyond a CSS variable's target path.

Left out: broader identity-header styling (the rest buttons live inside an
already-unfinished, explicitly-"not yet measured" header — only the icon
addition was in scope); resolving the two unwired assets above.

---

## 2026-08-15 — Phase 10, slice 1: Text Field mold — Background editing

First slice of phase 10, per the audit's own re-proposed slice order,
confirmed with the owner before starting.

**Live verification before building corrected the audit's own guess.** The
audit assumed one pattern — "clicking '+ Add X' opens a small sidebar panel:
title, one text input, helper prompt" — for every editable Background field.
Checking live (Helga Flinthand's sheet) found three: (1) Alignment plus ten
other characteristics (Gender, Eyes, Size, Height, Faith, Hair, Skin, Age,
Weight, and Lifestyle — an 11th field never in the audit's list at all) share
**one combined** "Characteristics and Details" panel; (2) Personality
Traits/Ideals/Bonds/Flaws each open individually with a textarea **plus a
roll-a-suggestion table** (Random button, per-row "+ Add") sourced from the
character's own background; (3) Appearance and the five Notes fields each
open individually with just a textarea and a helper prompt — the one pattern
that did match the original guess. Asked the owner whether to build all three
faithfully or start smaller (pattern 3 only, deferring 1 and 2); the owner
chose full fidelity now.

**Backend.** `Dnd5eBackground` grows from 14 to 24 fields (the ten new
characteristics) and gains `withField(Dnd5eBackgroundField, String)`, a
21-branch update-one-field-in-place method — everything but the background's
own identity (`name`/`featureName`/`featureDescription`, a phase 11 concern)
is now editable. New `Dnd5eBackgroundField` enum and
`InvalidBackgroundFieldException` (same "validated string category" treatment
`Dnd5eProficiencyCategory` already established). New
`SheetMutator.updateBackgroundField`; `POST /api/characters/{id}/background/{field}`
takes `{value}` and returns the updated sheet, no roll involved. The generic
`ruleset.Background`/`BackgroundResponse`/`Dnd5eSheetCalculator` mapping all
grew the same ten fields.

**New catalogue kind: `BACKGROUND`.** Personality Traits/Ideals/Bonds/Flaws'
suggestion tables are real PHB content, not invented — seeded from
`content/dnd-5e/backgrounds/soldier.json` (SRD 5.1, the same CC-licensed
subset adr-0005 already distinguishes from the full published material used
elsewhere), matching the seeded Fighter/Eldritch Knight character's own
Soldier background. A background with no seeded entry just shows no
suggestions table for those four fields — the same graceful-degradation
treatment an empty spell catalogue already gets in "Manage spells."

**Frontend — the seventh mold, Text Field.** `TextFieldRequest`
(`sheet/api.ts`) carries an array of fields rather than a single value — one
entry covers a single-field panel (patterns 2 and 3 above), several cover the
combined one (pattern 1) — the same "one shape, caller decides how much of it
to use" reasoning `EntityDetailRequest.actionBar` already established, rather
than needing a second mold for the combined case. `TextFieldPanel.tsx`: each
field is self-contained (its own draft state, saved on blur — no explicit
save button, matching the sheet's own "no explicit save" rule everywhere
else); a field's `suggestions` (plain strings) renders a Random-plus-per-row
table beneath it, appending the picked text on a new line rather than
replacing the draft (a judgment call — not click-verified against D&D
Beyond's own "+ Add" to avoid mutating the owner's real character, flagged in
code for revisiting if it turns out wrong). `BackgroundTab.tsx`: the
Characteristics grid became one `.reveal` target opening the combined panel;
Personality Traits/Ideals/Bonds/Flaws/Appearance and every Notes field each
became their own `.reveal` target.

Backend: new `Dnd5eSheetMutatorTest` cases (a field updates in isolation, an
unknown field is rejected). Full `./gradlew :apps:api:check` and
`npm run build` green.

Verified live end to end against the reseeded Fighter/Eldritch Knight
character: opened the combined Characteristics panel, set Gender to "Female,"
confirmed it saved and appeared in both the panel and the row below; opened
Personality Traits, confirmed the Soldier suggestion table rendered with all
eight PHB entries, clicked "+ Add" on one and confirmed it appended on a new
line, then restored the original text before closing (kept the seed
character's fixture data pristine, same discipline as prior slices' live
verification); opened Backstory and confirmed no suggestions table appears
there, matching pattern 3. Reset the Gender field back to empty afterward via
a direct, verified local DB edit.

**An unrelated pre-existing bug surfaced, not fixed (out of this slice's
scope).** Resetting the dev player/character rows and reloading hit
`duplicate key value violates unique constraint "players_subject_key"` once —
`PlayerService.createPlayer` has no conflict handling for concurrent
first-sight requests racing to insert the same subject (this session's extra
`useEffect` catalogue fetches on the sheet screen made the race easier to
hit, but the missing handling is pre-existing, not introduced here). A second
reload succeeded — one of the racing requests always wins the insert. Worth a
follow-up (`PlayerService.currentPlayer` catching the constraint violation and
retrying the lookup), not filed as a phase 10 slice since it's unrelated to
sheet fidelity.

Left out, per the owner's own scope framing at the top of phase 10: the
finer distinction between D&D Beyond's dropdown-backed Alignment/Lifestyle
and our free-text equivalent (kept free text, consistent with how Alignment
was already modeled before this slice).

---

## 2026-08-15 — Phase 9, slice 8: Short/long rest UI — phase 9 done

Eighth and last slice of phase 9, closing the phase: the Mechanic mold's
first real build, per the confirmed slice order from slice 1's changelog
entry.

**Live D&D Beyond check before building** (Helga Flinthand) surfaced that
both rest buttons open a **sidebar panel**, not a modal — exactly this
project's own mold architecture — and that Short Rest and Long Rest differ
in one real way: Long Rest is a pure confirm (rules text, an auto-computed
"what recovers" list, one button — no player input, matching this app's own
`applyLongRest`, which was already fully deterministic); Short Rest lets the
player choose how many hit dice to spend *within the same action*.

**A real scope question, confirmed with the owner rather than assumed.**
`Dnd5eSheetMutator.applyShortRest` (phase 9, slice 4) was already built and
shipped to leave hit dice untouched — a short rest restored only matching
feature actions/traits, and spending hit dice stayed the separate top-row
control from slice 2. Asked whether to keep that separation (zero backend
change, a new documented deviation from D&D Beyond) or integrate hit-dice
choice into the short rest flow like D&D Beyond does. The owner chose to
integrate.

**Backend.** `CharacterSheetService.applyShortRest` gained a `hitDiceSpent`
parameter: restores matching features unconditionally, then — only if
`hitDiceSpent > 0` — checks it against available hit dice
(`InsufficientHitDiceException` otherwise, same exception `spendHitDice`
already throws), resolves and records one combined roll via the existing
`HIT_DICE` mechanic-resolver path, and applies it through the existing
`SheetMutator.spendHitDice`. Returns a new `ShortRestResult` (vitals plus a
nullable `Roll`) instead of a plain `VitalsZone`, the same "roll and mutation
at once" shape `spendHitDice` already established. New `ShortRestRequest`
(`{hitDiceSpent}`) and `ShortRestResponse` (`{sheet, roll}`, `roll` null when
zero were spent) DTOs; `POST /api/characters/{id}/rest/short` now requires a
body (previously none). Long rest is untouched — no player input, no new
shape needed.

**Frontend — the Mechanic mold, built for real for the first time.** New
`MechanicRequest`/`MechanicPanel.tsx`: title, rules summary (original text,
not copied from the PHB or D&D Beyond), then a caller-built `body` — the
same escape hatch `EntityDetailRequest.actionBar` already uses, since only
"controls plus confirm button" varies per trigger (a long rest needs no
controls; a short rest's confirm button needs to read a stepper's live
value, simplest as one self-contained component rather than threading state
back out through the request shape). `RestBody.tsx` (`systems/dnd5e/`):
`LongRestBody` is just a confirm button; `ShortRestBody` holds its own
hit-dice-to-spend count, clamped client-side to the sheet's own available
hit dice (`hitDice.max - hitDice.used`) — so a request past the limit can't
actually reach the server through this UI; the backend's check is defense in
depth, not reachable via a click. `SheetShell.tsx` gained "Short Rest"/"Long
Rest" header buttons (labelled text, like the existing "Game log" button —
D&D Beyond's own are icons, already a documented deviation this slice
extends rather than invents), each opening the Mechanic panel rather than
acting immediately — confirmed this matches D&D Beyond's own behavior, not
assumed. Taking either rest closes the sidebar on success, same "one-shot
action" treatment already given to the `X` reveal targets.

Backend: `CharacterControllerTest` cases for both the zero-hit-dice and
spent-hit-dice short rest shapes. Full `./gradlew :apps:api:check` and
`npm run build` green.

Verified live end to end against the seeded Fighter/Eldritch Knight
character (hit dice at 2 of 5 used from seed data): opened Short Rest,
chose 2 hit dice, confirmed — HP went 30/44 → 44/44, the roll (`2d10+4`,
total 15) landed in the dice tray and game log, hit dice used went to 4 of
5, and the sidebar closed on its own; opened Long Rest, confirmed — hit
dice used dropped from 4 to 2 (half of 5, rounded down), no roll recorded,
sidebar closed; reopened Short Rest and typed a count past what was
available, confirming the stepper clamps in the browser rather than
producing a 400. Reset the test character's HP/temporary HP/hit-dice-used
back to the seed's own values afterward via a direct, reverted local DB
edit, same as prior slices' live-verification cleanup.

**Phase 9 (Rests and mechanics) is done.** All eight slices landed: recharge
triggers and feature use spend/restore, hit dice, spell slots, the rest
mechanic, spell damage and rolls, casting, Manage spells, and this slice.
Phase 10 (Sheet fidelity to D&D Beyond) is unblocked — its audit plan
(`.ai/rulesets/dnd-5e-sheet-fidelity-audit.md`) was already written and all
its open questions already answered during phase 9, so the next step is
picking phase 10's first slice, not planning from scratch.

---

## 2026-08-15 — Phase 9, slice 7: Manage spells

Seventh and last content slice of phase 9, per the confirmed slice order —
only the Mechanic mold's first real UI (short/long rest buttons) remains
before phase 9 closes.

**Research before building.** The initial plan (a Collection-editor-based
picker limited to spells the character already knows) was replaced after the
owner asked for real D&D Beyond parity and pointed at three live character
sheets — a Cleric/Paladin, a level-20 Wizard, and (mid-slice) a Ranger.
Confirmed live: Clerics/Wizards separate "known" from "prepared" (a daily
subset, bounded per class, cantrips learned individually with no separate
prepare step, some spells "always prepared" from a domain); Rangers (and by
extension Sorcerer/Bard/Eldritch Knight) have no prepare step at all — every
learned spell is always usable. A Barbarian's sheet has no Spells tab at all
(already true here: `sheet.spellcasting.length === 0` renders none).

**Scope boundary, confirmed with the owner.** The catalogue (phase 7) had no
real spell content imported, and its generic `data` field had no defined
mapping to `Dnd5eSpell`. Building the *full* official class spell list was
out of scope; instead, `content/dnd-5e/spells/` seeds 11 real SRD 5.1 spells
(sourceBook "SRD 5.1" — the CC-licensed subset adr-0005 already distinguishes
from the full published material) as real, functional candidates: Chill
Touch, Ray of Frost, Sacred Flame, Spare the Dying, Shield, Sleep, Cure
Wounds, Guiding Bolt, Bless, Burning Hands, Spiritual Weapon. Import with
`./gradlew :apps:api:bootRun --args='--import-catalogue=<path>'` (Windows:
avoid a path containing a space — Gradle's `--args` mis-splits it).

**Backend model.** New `Dnd5eSpellCastingType` enum (`KNOWN`/`PREPARED`).
`Dnd5eSpellcastingClass` gains `castingType`, `cantripsKnownMax` (both
types), `spellsKnownMax` (`KNOWN` only — spellbooks/whole-class-lists carry
no numeric cap in this app, matching real Wizard/Cleric rules), and
`spellsPreparedMax` (`PREPARED` only) — all stored, not derived, same
"character-authored number" treatment as `abilityKey`. `Dnd5eSpell` gains
`prepared` and `alwaysPrepared`; the generic `ruleset.Spell` and
`SpellcastingClassInfo` mirror both records' new fields (`castingType` as a
plain string on the generic side, adr-0003). `Dnd5eSheet` gains `withSpells`
(the array was read-only until now).

Four new `Dnd5eSheetMutator` methods: `learnSpell(sheetJson, Spell)` (a
no-op if the key is already known; else checked against the owning class's
cantrip cap, or its known-spell cap for a `KNOWN` class — `SpellLimitExceededException`
otherwise), `removeSpell` (unknown key is a no-op, like `removeItem`),
`prepareSpell` (no-op for a cantrip, an already-`alwaysPrepared` spell, a
`KNOWN`-type class's spell, or an unknown key; else checked against
`spellsPreparedMax` — `SpellPreparationLimitExceededException` otherwise),
`unprepareSpell` (same no-op cases, no limit). `CharacterSheetService.learnSpell`
is the one cross-feature dependency on `catalogue` (`CatalogueService`,
allowed per architecture.md — "through the other feature's service"): it
adds `CatalogueService.getUnredacted`, since a player's own learned copy of a
spell is no longer catalogue browsing and should carry the real description
regardless of the redaction switch (adr-0005: redaction "does not change
what the repository holds"). New endpoints: `POST .../spells/learn`,
`DELETE .../spells/{key}`, `POST .../spells/{key}/prepare`,
`POST .../spells/{key}/unprepare`. Both new exceptions map to 400.

**A new, sixth sidebar mold — not the Collection editor `dnd-5e-sheet-ui.md`
originally named for this trigger.** Confirmed live against D&D Beyond that
the flat, free-text add/remove shape `CollectionEditorRequest` offers cannot
express casting-type-dependent limits without losing real behavior the owner
asked to replicate. `ui-design-system.md`'s "The five molds" is now "The six
molds" (Spell management added, documented as a confirmed exception per that
doc's own "signal to stop and discuss, not to invent one more" rule); the
sidebar trigger table in `dnd-5e-sheet-ui.md` moved "manage spells" out of
the Collection editor row into its own. New `SpellManagementRequest`
(`sheet/api.ts`) and `SpellManagementPanel.tsx`: per spellcasting class, an
"Add Spells" picker (catalogue entries, Learn/Known per row) and a "Known
Spells" list whose per-row action depends on the class's `castingType` and
the spell's own `level`/`alwaysPrepared` — Delete only (cantrip, or
`KNOWN`-type class), Prepare/Unprepare (leveled, `PREPARED`-type, not
always-prepared), or a static "Always Prepared" label. `SpellsTab.tsx` gained
a "Manage Spells" header button next to the spellcasting header.

**A real bug, caught before shipping.** The panel's `onLearn`/`onRemove`/
`onPrepare`/`onUnprepare` handlers were built once, at the moment the panel
opened, each closing over `handleMutate` (and, through it, the `sheet`
snapshot `handleMutate` uses for optimistic rollback) as it existed *then*.
A later mutation in the same session — e.g. a rejected third cantrip after
two successful learns — rolled the sheet back to that stale open-time
snapshot on failure, silently discarding the two successful learns along
with the rejected one. Fixed by rebuilding all four handlers on every render
in `CharacterSheetScreen.tsx`'s `displayedSidebarContent` (the same live-refresh
spot that already re-derives `classes`/`knownSpells` from the current sheet),
so each mutation's rollback baseline is always the latest sheet, not a frozen
one. Caught live, not by a test — a reminder that this class of bug (a
long-lived callback closing over changing state) is exactly what the
generalized `refreshActionBar` mechanism (phase 9, slice 1) exists to guard
against, and this panel needed the same treatment for its own handlers, not
just its data.

Backend: new `Dnd5eSheetMutatorTest` cases for both casting types (learn,
no-op-on-duplicate, both limit exceptions, prepare/unprepare, and every
no-op case — cantrip, always-prepared, `KNOWN`-type). Full
`./gradlew :apps:api:check` and `npm run build` green.

Verified live end to end, twice — against the seeded Fighter/Eldritch Knight
(`KNOWN`-type) character, and, via a temporary local Postgres edit reverted
immediately afterward (the seed character's own `castingType` switched to
`PREPARED` and back — the DB, not the seed data, which stays the real
Eldritch Knight `KNOWN` type), against a `PREPARED`-type class: learned
Shield and Chill Touch; hit the cantrip cap on a third learn without losing
the first two (confirming the bug fix); learned past the `KNOWN`-type known
cap and got rejected the same clean way; switched to `PREPARED`, prepared
two spells to `spellsPreparedMax`, hit the cap on a third without corrupting
the first two, unprepared one, deleted a spell — every step confirmed
against Postgres directly, not just the UI. The dev player/character/rolls
rows were reset and re-seeded partway through (with the owner's confirmation)
so the updated seed JSON's new required fields would actually be present —
editing the seeder alone only affects new players.

Left out, per the confirmed scope boundary: the full official per-class
spell list (needs real content-authoring, a separate effort); the finer
real-game distinction between a Cleric's whole-list auto-knowledge and a
Wizard's spellbook (both reduce to the same seeded-catalogue Learn/Delete
here). Noticed but not investigated: a stray "Sparkling Bolt" catalogue
entry already present in the dev database, matching `CatalogueImportServiceTest`'s
synthetic fixture content — not created by this slice, left alone.

---

## 2026-08-15 — Phase 9, slice 6: Casting

Sixth slice of phase 9, per the confirmed slice order (`changelog.md`'s slice 1
entry): casting, then "Manage spells," then the Mechanic mold's first real UI.
Frontend-only — no backend gap existed. `Dnd5eSheetMutator.consumeSpellSlot`/
`restoreSpellSlot` (slice 3) were already generic and level-based, so casting
needed no new endpoint, only a UI that lets the player choose which level to
spend, per `dnd-5e-sheet-ui.md`'s Spells tab spec: "Casting from the detail
panel allows choosing a slot level; the slot consumed is the chosen level, not
the spell's base level" (upcasting).

**New `SpellCast.tsx`** is the action bar `SpellsTab.tsx` now attaches to a
leveled spell's Entity Detail panel: a `<select>` of every slot level at or
above the spell's own that the character has a track for (labelled with its
own remaining/max count), and a Cast button that dispatches the existing
`CONSUME_SPELL_SLOT` mutation at the chosen level. The button disables once
the selected level has no slots left, rather than letting a click silently
no-op the way the level heading's own `BoxTrack` does at zero — a UI
usability call, not a game rule, since the mutation itself still clamps.
Cantrips (level 0) get no cast control at all, matching the spec: casting
lives in the sidebar, and a cantrip has no slot to spend. `levelLabel` moved
from `SpellsTab.tsx` into `SpellCast.tsx` (now exported) to avoid a circular
import between the two files.

**Live sync while the sidebar stays open.** `SpellCast` supplies
`refreshActionBar`, the same mechanism Extras introduced for its hit points
control (phase 9, slice 1) — casting from the sidebar updates the level
heading's `BoxTrack` in the row behind it, and restoring/consuming a slot any
other way (the heading's own track) updates the still-open sidebar's picker
and remaining count in turn.

Full `npm run build` (`tsc` + `vite build`) green. Verified live end to end
against the seeded Fighter/Wizard test character: opening Mage Armor's detail
panel showed a "1st Level (1/3)" picker and an enabled Cast button; clicking
Cast consumed the slot, the level heading's track filled to 3/3, the sidebar's
own picker updated to "(0/3)" live, and Cast disabled itself; restoring via
the heading's track brought both back in sync. Fire Bolt (cantrip) opened
with no cast control, only its existing inline Attack/Damage roll targets.

Left out, per the confirmed slice order: "Manage spells" (the known/prepared
spells collection editor) and the Mechanic mold's first real UI (short/long
rest buttons) both remain.

---

## 2026-08-15 — Phase 9, slice 5: Spell damage + rolls

Fifth slice of phase 9, per the confirmed plan. Uses the owner's confirmed
general model: a spell may carry an optional attack-roll flag and optional
damage dice, independently of each other — most utility spells carry neither,
some (like Fire Bolt) carry both, and a save-based damage spell could carry
only damage. Damage dice add no ability modifier (PHB: unlike a weapon
attack, spell damage is fixed), and damage scaling by cast level is not
modeled — the same "fixed base numbers only" simplification already taken
for weapon attacks.

**A real gap found mid-slice:** the generic `ruleset.Spell` record had no
`key` at all (only the `dnd5e`-specific one did) — nothing needed to target
one spell out of a list before this slice. Added it, same precedent as
`FeatureAction`/`FeatureTrait` gaining `key` in slice 1.

**Backend.** `RollKind` gains `SPELL_ATTACK`/`SPELL_DAMAGE`. `Dnd5eSpell`
gains `attackRoll`, `damageDiceCount`, `damageDiceSides`, `damageType`
(generic `Spell` mirrors them, plus `key`). `Dnd5eMechanicResolver` resolves
both against the spell's own data — `SPELL_ATTACK` uses the spellcasting
class matching the spell's `className` (multiple classes can appear in
`vitals.spellcasting()`) and rejects if `attackRoll` is false; `SPELL_DAMAGE`
rejects if either damage field is null. Both share existing endpoints — no
controller change needed, `RollKind`/key already flow generically through
`POST /api/characters/{id}/rolls`. Seed data: Fire Bolt now carries
`attackRoll: true` and `1d10` fire damage; Mage Armor and Detect Magic carry
neither, exercising the "no roll target" row shape.

**Frontend.** `SpellRow.tsx` gains inline Attack/Damage buttons (shown only
when the spell carries the corresponding data), reusing `action-row__value`'s
styling — the same treatment `AttackRow.tsx` already established. `RollKind`
(dice/api.ts) and `Spell` (sheet/api.ts) gain the new fields; `SpellsTab.tsx`
and `Dnd5eTabbedSection.tsx` thread `onRoll` through, which they didn't need
before this slice.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live:
rolled Fire Bolt's attack (1d20+3) and damage (1d10, no modifier) from the
Spells tab, both landing correctly in the dice tray. Compared against D&D
Beyond's own Spells tab per the standing fidelity rule — it shows the same
inline-per-row roll shape (HIT/DC and EFFECT columns per spell), confirming
this slice's approach without needing further changes.

---

## 2026-08-15 — Phase 9, slice 4: Rest mechanic (backend only)

Fourth slice of phase 9. Backend-only by design: the plan splits this from
its UI (a later slice adds Short Rest/Long Rest buttons to the header — the
first real build of the Mechanic mold). No frontend changed this slice, so
the D&D Beyond fidelity check didn't apply; verified instead by calling both
new endpoints directly against the running dev server.

**Rules implemented, PHB-accurate where the existing mocked models allow:**
short rest restores every feature action/trait whose `rechargeTrigger` is
`SHORT_OR_LONG_REST`, nothing else. Long rest restores those plus everything
triggered by `LONG_REST`, refills every spell slot to its own max, heals
current hit points to the sheet's own max, and restores spent hit dice by
half the character's total (rounded down, minimum one) — the one real PHB
rule added this slice, straightforward enough not to need a further mock.

**Backend.** `SheetMutator` gains `applyShortRest`/`applyLongRest`;
`Dnd5eSheetMutator` implements both via two new private helpers
(`restoreFeatureActionsByTrigger`/`restoreFeatureTraitsByTrigger`) reusing
the existing per-entry `withUsedCount` overloads. New endpoints
`POST /api/characters/{id}/rest/short` and `.../rest/long`, both returning a
plain updated sheet — no roll involved, so no dual-shape response like Hit
Dice's.

**Bug caught before it shipped:** the first version matched a trigger with
`Set.of(SHORT_OR_LONG_REST[, LONG_REST]).contains(feature.rechargeTrigger())`
— Java's immutable `Set.of` throws `NullPointerException` on `contains(null)`,
and most seeded feature traits (`fightingStyle`, `extraAttack`, `darkvision`,
etc.) have a null trigger (no automatic rest recharge, per
`Dnd5eFeatureTrait`'s doc comment). Caught live against the dev server, not
by the unit tests — the first test fixture only used triggered features.
Fixed with a `matchesTrigger` helper that short-circuits on null, and the
fixture now includes an untriggered feature action/trait in both rest tests
to keep this covered going forward.

Full `./gradlew :apps:api:check` green. Verified live via direct `fetch`
calls (no UI yet): short rest restored two spent feature actions while
leaving hit dice, spell slots and hit points untouched; long rest — starting
from 2/5 hit dice used, 3/3 spell slots used, 30/44 HP, one feature action
spent — returned 0/5 hit dice used, 0/3 spell slots used, 44/44 HP, and both
feature actions restored, all in one call.

---

## 2026-08-15 — Phase 9, slice 3: Spell slots

Third slice of phase 9, per the confirmed plan. Unlike Hit Dice, a spell slot
spend is a **plain mutation with no roll attached** — casting-with-damage is a
later slice (roadmap.md) — so this stays entirely inside `SheetMutator`/
`CharacterSheetService`, no `RollService` involvement, and it clamps at
zero/max instead of throwing when a level is already exhausted, matching
`useFeatureAction`'s box-track treatment rather than Hit Dice's
roll-and-validate one.

**Backend.** New `Dnd5eSpellSlotLevel(level, maxSlots, usedSlots)` in
`ruleset.dnd5e` and generic `SpellSlotLevel` in `ruleset` (`VitalsZone` gains
it, identified by level rather than by string key — there's no natural key
finer than "which level"). `Dnd5eSheet` gains `spellSlots`, mutable via new
`withSpellSlots`. `SheetMutator` gains `consumeSpellSlot`/`restoreSpellSlot`;
`Dnd5eSheetMutator` implements them with the same map-by-identifier-or-pass-
through pattern as feature action use/restore, an unknown level is a no-op.
New endpoints `POST /api/characters/{id}/spell-slots/{level}/consume` and
`.../restore`. Seed data: the Eldritch Knight fighter's level-1 slots start at
2 of 3 spent, so the track shows a partial fraction like every other seeded
resource.

**Frontend.** `SpellsTab.tsx` renders a `sheet/BoxTrack.tsx` next to each
level's heading (cantrips excluded — level 0 never has a track), wired through
the tab's existing `onMutate` prop (now threaded in from
`Dnd5eTabbedSection.tsx`, which previously didn't pass one to this tab). New
`CONSUME_SPELL_SLOT`/`RESTORE_SPELL_SLOT` `MutationAction` variants and
`postConsumeSpellSlot`/`postRestoreSpellSlot` in `sheet/api.ts` — these return
a plain `CharacterSheet` like every mutation except Hit Dice's, so no new
response shape was needed.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live:
consumed the fighter's last level-1 slot (2/3 → 3/3 used), then restored it
back (3/3 → 2/3), both round-tripping through the server correctly.

---

## 2026-08-15 — Phase 9, slice 2: Hit Dice (mocked)

Second slice of phase 9, per the confirmed plan. The owner explicitly allowed a
mocked model for now — max hit dice = character level, one die size (the
sheet's existing `hitDieSize`), not a true per-class multiclass total — to be
revisited once class/level modeling exists (phase 11). A short rest's hit-dice
spending in real 5e is `1dHitDieSize + CON` per die, times however many the
player spends; that's what's implemented, without the multiclass nuance.

**A hit dice spend is a roll AND a mutation, not one or the other** — it heals
(a mutation) by an amount only known after rolling (a roll), so it needed both
seams at once. This didn't fit cleanly anywhere at first:

- `RollService` (in `dice`) has `DiceRoller`/`RollRepository`, both
  intentionally package-private — nothing outside `dice` can roll or log
  directly.
- `CharacterService.save`/`Character.replaceSheet(String)` are equally
  package-private to `character` — nothing outside `character` can persist a
  sheet mutation directly.

Neither package can unilaterally do the other's part, so a first attempt
(orchestrating entirely from `RollService`) failed to compile against
`character`'s own encapsulation. Resolved by giving `RollService` a narrow new
public seam, `recordRoll(characterId, diceCount, diceSides, flatModifier,
context)` — roll and log, nothing else, still never exposing `DiceRoller`/
`RollRepository` outside `dice` — and doing the actual orchestration in
`CharacterSheetService.spendHitDice` (in `character`, where sheet-persisting
authority already lives): resolve the roll's shape via `MechanicResolver`
(new `RollKind.HIT_DICE`, one die's worth — the player-chosen count is applied
by the caller, not the resolver), call `recordRoll`, then mutate the sheet via
`SheetMutator.spendHitDice`. Availability is checked before any of that, so a
rejected spend (`InsufficientHitDiceException`) never leaves an orphaned roll
in the log.

**Backend.** New `HitDice(dieSize, max, used)` in `ruleset` (generic,
`VitalsZone` gains it); `Dnd5eSheet` gains `hitDiceUsed`, mutable via new
`withHitDiceSpend`. New endpoint `POST /api/characters/{id}/hit-dice/spend`
returns both the updated sheet and the roll (`SpendHitDiceResponse` — the one
response shape in this codebase carrying both, since every other mutation only
returns the sheet). Seed data: the fighter starts with 2 of 5 hit dice already
spent, so the pool shows a partial fraction like every other seeded resource.

**Frontend.** New `systems/dnd5e/HitDice.tsx`, visually matching
`HitPoints.tsx` (same CSS), added to `Dnd5eVitalsTopRow.tsx`. New
`postSpendHitDice`/`SpendHitDiceResult` in `sheet/api.ts` — outside the
`MutationAction` union, since it returns two things, not one.
`CharacterSheetScreen.tsx`'s `handleSpendHitDice` updates both `sheet` and
`rollHistory` from the single response.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live:
spending 2 of the seeded fighter's 3 available hit dice rolled 2d10+4 (10, 9 →
23), healed 30/44 HP to the 44 cap, dropped the pool to 1/5 available, and the
roll appeared in the dice tray immediately.

Left out, per the confirmed slice order: spell slots, the rest mechanic
itself, spell damage/casting, and Manage spells all remain.

---

## 2026-08-15 — Phase 9, slice 1: recharge trigger + spend/restore a feature's use

First slice of phase 9 (Rests and mechanics), following a plan read from
`roadmap.md`, `ground-rules.md`, `architecture.md`, `domain-model.md`,
`database-schema.md`, `features/character-sheet.md`, `ui-design-system.md` and
the existing `MechanicResolver`/`Dnd5eMechanicResolver`, then confirmed with the
owner on three scope questions: hit dice modeled as a mocked pool for now (real
multiclass rules deferred until class/level modeling exists), spell damage
modeled generally (optional dice + an attack-roll flag), and "Manage spells"
included in this phase. The resulting slice order: recharge trigger + use
spend/restore (this slice), hit dice, spell slots, the rest mechanic, spell
damage + rolls, casting, Manage spells, then the Mechanic mold's first real UI.

**Recharge trigger replaces free-text `rechargeLabel`.** New
`Dnd5eRechargeTrigger` enum (`SHORT_OR_LONG_REST`, `LONG_REST`) on
`Dnd5eFeatureAction`/`Dnd5eFeatureTrait`, matching roadmap.md's "resources
modelled with an explicit recharge trigger" — a rest mechanic (a later slice)
needs something to match against, not a display string. The generic
`FeatureAction`/`FeatureTrait` (`ruleset` package) carry it as a plain string,
same treatment as `actionType`/`category` (adr-0003: which values exist is a
per-system concept). The frontend maps a trigger to its display label itself
(`rechargeTriggerLabel` in `FeatureActionRow.tsx`, reused by
`FeatureTraitRow.tsx`) — a label lookup, not a game rule.

**A gap found while building this: neither generic `FeatureAction` nor
`FeatureTrait` carried a `key`.** Both lists render as arrays (unlike
`attacks`, keyed by `Record<string, AttackRow>`), so there was nothing a
mutation could address a specific entry by. Added `key` to both records,
threaded through `Dnd5eSheetCalculator`'s mapping and the two response DTOs —
the same shape every other keyed list already had.

**Spending and restoring a use are real mutations.** `SheetMutator` gains
`useFeatureAction`/`restoreFeatureAction` and `useFeatureTraitUse`/
`restoreFeatureTraitUse` — deliberately four methods, not two, because feature
actions and feature traits are independent lists (`Dnd5eFeatureTrait`'s
long-standing doc comment: they overlap in content, like Second Wind appearing
in both, but spending a use in one never touches the other). Verified live:
spending Second Wind from the Actions tab left its Features & Traits tab
counterpart untouched. Each mutation clamps at `[0, maxUses]`; an unknown
feature key is a no-op, same treatment `toggleItemEquipped` already gives an
unknown item key. Four new endpoints:
`POST /api/characters/{id}/feature-{actions,traits}/{key}/{use,restore}`.

**`BoxTrack.tsx` is interactive.** Clicking an empty box spends a use, a
filled box restores one — both optional handlers; omitting them keeps the
primitive read-only for a future consumer that might want that. Wired in both
`FeatureActionRow.tsx` and `FeatureTraitRow.tsx`. Each row's Entity Detail
sidebar copy of the track supplies `refreshActionBar` (the mechanism built
last slice for Extras) so a click inside the open sidebar updates it live —
verified live, including confirming the mechanism actually matters here: the
first click landed while not paying attention to whether the sidebar copy was
wired, and it silently stayed stale until `refreshActionBar` was added.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live end
to end: Second Wind's box fills on click from the Actions tab, its independent
Features & Traits copy stays empty, and opening its sidebar panel and clicking
there updates the sidebar's own box immediately.

Left out, per the confirmed slice order: hit dice, spell slots, the rest
mechanic itself, spell damage/casting, and Manage spells all remain — this
slice only ships the recharge trigger and the use spend/restore mechanics they
depend on.

---

## 2026-08-15 — Phase 8 closed: Inventory search

Small, final slice for phase 8. `dnd-5e-sheet-fidelity-audit.md`'s Inventory tab
finding flagged that the tab had no search field at all, while phase 8's own
"done when" line requires every list to support search and filtering — a
phase-8 completion gap surfaced by the fidelity audit, not just a fidelity one.

`InventoryTab.tsx` gained a `SearchField` (the same shared primitive Spells
already uses), filtering `sheet.items` by name, case-insensitive. `npm run build`
green; verified live — typing "cloak" against the seeded fighter's five items
narrowed the list to "Cloak of Protection" alone.

`roadmap.md`'s phase 8 status moves to **Done**; `dnd-5e-sheet-build.md`'s
Inventory tab and build-order entries updated to record it.

---

## 2026-08-15 — Phase 10 audit: all eight open questions answered (docs only)

No code changed. Went through `dnd-5e-sheet-fidelity-audit.md`'s eight open
questions with the owner, one at a time. Two corrections came out of this pass —
worth noting because they reverse what the audit originally concluded:

- **Question 2** (inline vs. sidebar for features/traits): the owner tested D&D
  Beyond directly and found features/traits *do* open a sidebar on click, same as
  everything else — the original audit pass never actually clicked a feature name
  to check. Our existing sidebar-for-everything pattern was already correct; no
  rebuild needed.
- **Question 3** (roll buttons in an attack's sidebar): confirmed live, twice,
  after the correction above raised doubt about the first pass's reliability —
  clicking an attack's hit/damage value rolls inline on the table row itself; the
  sidebar has no roll buttons, only info and equipment-management actions. This
  one *did* match the original finding. Decision: drop the duplicate roll buttons
  `ActionsTab.tsx` currently puts in an attack's action bar.

The other six: retry Saving Throws' 2-column layout with the plain 9-slice frame
before accepting the ornate frame's constraint (question 1); switch
`EntityDetailRequest.metadata` to a labeled list (question 4); add nine missing
Background characteristic fields (question 5); invest in a structured Extras stat
block, superseding last slice's free-text choice (question 6); defer the
Character panel mold — most of its content belongs to phases 9/11 (question 7);
move the Conditions checklist into a sidebar panel, confirmed live via D&D
Beyond's own "Add Active Conditions" flow (question 8).

Also recorded a longer-term note under a new "Tracked, waiting on the owner"
section: the owner may eventually provide individually-sized frame SVGs per
panel (raised earlier in the project), which would supersede the 9-slice retry
for Saving Throws if that still doesn't look right. New memory
`project-per-section-frame-assets` tracks this for future sessions.

`dnd-5e-sheet-fidelity-audit.md` updated throughout — every resolved question,
every table row and the suggested slice order now reflect the decisions instead
of an open question. A handful of smaller, not-formally-asked items remain
(hit points block layout, a couple of "verify live during the slice" details) —
listed at the top of "Findings by area," not blocking the slice order.

---

## 2026-08-15 — Phase 10 audit plan: tab-by-tab comparison against D&D Beyond (docs only)

No code changed. Built the audit plan phase 10 requires before any fixing starts
(per the phase's own entry in `roadmap.md`), by comparing this application's sheet
against the live reference, `https://www.dndbeyond.com/characters/50149479`
(Helga Flinthand, Hill Dwarf Cleric 4/Paladin 3), tab by tab — the vitals zone,
all seven of D&D Beyond's tabs, and every sidebar mold, clicking through to see
actual behavior rather than reading a static screenshot.

New `.ai/rulesets/dnd-5e-sheet-fidelity-audit.md`. Highlights:

- **Confirms several existing deviations are accurate**: containers, weight/
  encumbrance, and the background/notes tab merge are genuinely different on D&D
  Beyond and genuinely absent here on purpose — nothing to fix, the documentation
  already matches reality.
- **Finds real, previously undocumented gaps**, each logged as an "Open question
  for the owner" rather than assumed: Saving Throws' 2-column D&D Beyond layout
  vs. our single-column revert; a ten-field "Characteristics" section on D&D
  Beyond (`Dnd5eBackground` only has one, Alignment, of those ten); D&D Beyond's
  fully structured stat block for a familiar/mount vs. our free-text
  `Dnd5eExtra.statBlock`; D&D Beyond routing features/traits through inline,
  always-expanded descriptions while every entry here opens the sidebar instead;
  and `EntityDetailRequest`'s flat `metadata: string[]` vs. D&D Beyond's labeled
  key/value list plus a tags row.
- **Resolves the sixth-mold gap** flagged when Background & Notes was built:
  clicking "+ Add Organizations" (or any notes field) on D&D Beyond opens a small
  sidebar panel — title, one text input, a helper prompt below it. Proposed as a
  new "Text Field" mold for `ui-design-system.md`, and recommended as an early
  phase 10 slice since it directly unblocks editing that was deliberately left
  out pending this exact discovery.
- **Flags one item as possibly a phase-8 completion gap, not only a fidelity
  one**: the Inventory tab has no search field, while phase 8's own "done when"
  line requires every list to support search and filtering.
- A suggested slice order, explicitly framed as a starting point that may reorder
  once the owner's answers and early slices reveal more — mirroring how phase 8's
  own build order shifted slice to slice.

`.ai/roadmap.md`'s phase 10 entry now points to this document first, before its
other reading list.

---

## 2026-08-14 — New roadmap phase: sheet fidelity to D&D Beyond (docs only)

No code changed. The owner judged the sheet built through phase 8 "ainda muito
distante do que temos no D&D Beyond" — several tabs and molds were built against
static screenshots or estimated measurements, not a live, side-by-side comparison —
and asked for that to be closed before character creation (phase 10, now renumbered
11) begins, since building creation against a sheet still known to be wrong would
mean redoing that work once the sheet changes under it.

**`.ai/rulesets/dnd-5e-sheet-ui.md`** gained a new "Verifying fidelity against D&D
Beyond" section, right under the existing "Visual target" line: the live reference
is `https://www.dndbeyond.com/characters/50149479`; any frontend change to the
sheet ends with a comparison against it — layout, hovers, buttons, tabs, molds and
functionality, not just static look. The existing "Deviations from D&D Beyond" list
is the only authoritative exception set — a new deviation is never assumed
unilaterally, always confirmed with the owner first and then documented there.
When a feature's D&D Beyond behavior is unclear, interacting with the live page
directly (clicking tabs/controls) is fine; when unsure whether a comparison or a
site action is appropriate, ask first.

**`.ai/ground-rules.md`** gained a one-line pointer to that section under
"Frontend," so the rule surfaces even from the document read before any code
change, not only when `dnd-5e-sheet-ui.md` itself is loaded.

**`.ai/roadmap.md`** gained **Phase 10 — Sheet fidelity to D&D Beyond**, inserted
between phase 9 (Rests and mechanics) and the renumbered phase 11 (Character
creation, was 10) and phase 12 (Second game system, was 11). Its own audit plan
(a tab-by-tab comparison against the live reference) is deliberately not written
yet — the phase's entry says so explicitly, matching phase 11's existing precedent
of needing its own spec before planning. Phase 11 now states it is blocked on
phase 10.

Also updated: the memory `feedback-dndbeyond-fidelity-verification` (expanded with
the live URL and the new roadmap phase), and a new memory
`project-phase10-sheet-fidelity` recording why the phase exists and that its audit
plan is still unwritten.

---

## 2026-08-14 — Phase 8, slice 10: Extras tab, closing out the tab list

Sixth and final tab of build order step 8. `dnd-5e-sheet-ui.md`'s Extras section:
familiars, mounts, summoned creatures and vehicles linked to the character. A row
shows name, armor class, hit points and speed; opening it shows the full stat block
in the sidebar, with hit points editable for that instance.

**Scope question, resolved with the owner before building:** the spec's "editable
hit points" left open how the editing should work. Chosen: mirror the character's
own damage/heal/temporary-hit-points mutation set exactly, scoped to one extra by
key, rather than inventing a simpler single "set current HP" field. This is more
mechanic than the spec strictly asked for, but it means an extra's hit points
behave identically to the character's own — no second mental model for the player.

**Backend.** New `Dnd5eExtra` (key, name, category, armor class, max/current/
temporary hit points, speed, a free-text `statBlock`) and `Dnd5eExtraCategory` enum
(FAMILIAR/MOUNT/SUMMONED_CREATURE/VEHICLE) in `ruleset/dnd5e`; `Dnd5eSheet` gains an
`extras` list, mutable via new `withExtras`. `SheetMutator` gains three methods —
`applyExtraDamage`/`applyExtraHealing`/`setExtraTemporaryHitPoints`, all keyed by
`extraKey` — implemented with the exact same damage/heal/temp rules as the
character's own (`Dnd5eSheetMutator`'s existing methods), applied to one list entry
instead of the sheet's own fields. An unknown `extraKey` is a no-op, same treatment
`toggleItemEquipped` already gives an unknown item key. New generic `Extra` in
`ruleset` (plain pass-through, `armorClass`/`speed` as plain ints like the sheet's
own `speed` — not derived); `VitalsZone` and `CharacterSheetResponse`/new
`ExtraResponse` gain it. Three new endpoints: `POST /api/characters/{id}/extras/
{extraKey}/hit-points/{damage,heal,temporary}`, reusing the existing
`HitPointAdjustmentRequest` DTO. A seeded Warhorse (MOUNT, AC 11, 19 max HP, 12
current so the row's fraction is meaningfully partial) gives the tab a real entry.

**A real gap found and fixed, not scoped around.** Verifying live in the browser
(damage the Warhorse with its sidebar panel still open) showed the list row updating
correctly but the sidebar's own hit-points control frozen at the value it had when
the panel opened — `EntityDetailRequest`'s `actionBar` is a snapshot `ReactNode`
baked at click time, and nothing was re-deriving it, unlike the Collection editor
and Log molds which already re-derive their content live. Fixed generically rather
than special-cased: `EntityDetailRequest` gained an optional
`refreshActionBar(sheet) => ReactNode`; `CharacterSheetScreen`'s existing
live-refresh branch (previously handling only `collectionEditor`/`log`) now also
calls it for `entityDetail` when present. `ExtraRow.tsx` supplies it, matching the
extra by key against the live sheet. No other entity detail trigger supplies one
yet (their action bars are either inert or fine as a snapshot); Inventory's
equip/attune toggle has the same theoretical staleness but was out of scope here —
noted as a natural follow-up now that the mechanism exists.

**Frontend.** New `systems/dnd5e/ExtrasTab.tsx` and `ExtraRow.tsx`: category filter
chips (all/familiars/mounts/summoned creatures/vehicles), a flat row list (`AC`,
`current/max HP`, `speed`), name opens Entity Detail with the stat block as
description and `HitPoints.tsx` (the character's own component, reused as-is) as
the action bar. New `extra-row__*` CSS classes in `frames.css`. `Dnd5eTabbedSection.tsx`
gains the sixth and final tab.

Backend and frontend both required updating every `new Dnd5eSheet(...)`/
`new VitalsZone(...)` call site again — by now a mechanical, low-risk step repeated
across seven prior slices.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live: the
Warhorse row renders with its stats, its Entity Detail panel shows the full stat
block and hit-points control, and a Heal click while the panel stayed open updated
both the panel and the list row in sync (12/19 → damaged to 4/19 → healed to
10/19 — matching between the row and the still-open panel confirmed the fix).

All six tabs from `dnd-5e-sheet-build.md`'s build order are now built, closing out
that step of phase 8. Left out, matching the "Manage feats"/"Manage spells"
precedent: no "add an extra" UI — only seed data populates the list this phase.

---

## 2026-08-14 — Phase 8, slice 9: Background and Notes tab, display-only

Fifth tab of build order step 8. `dnd-5e-sheet-ui.md` describes this as one
merged tab (D&D Beyond has it as two) — background feature and characteristics
(alignment, personality traits, ideals, bonds, flaws, appearance) plus free-text
notes (organizations, allies, enemies, backstory, other) — and says "editing
happens in the sidebar."

**A gap in the design docs, flagged rather than resolved unilaterally.** No mold
in `ui-design-system.md`'s defined five (Explainer, Entity Detail, Collection
Editor, Mechanic, Log) covers editing a free-text field, and the sidebar trigger
table's own "Character panel" entry names a sixth mold that was never designed.
Per that document's own rule — "a signal to stop and discuss, not to invent a
sixth" — this slice follows the Spells/Features precedent instead: build the
display-only read, defer editing pending that discussion. Every field renders as
inert plain text, no `.roll`/`.mutate`/`.reveal` affordance, since there is
nothing yet for a click to do.

**Backend.** New `Dnd5eBackground` (14 `String` fields, all free text) in
`ruleset/dnd5e`; `Dnd5eSheet` gains a `background` field. New generic
`Background` in `ruleset` (plain pass-through, nothing calculated); `VitalsZone`
and `CharacterSheetResponse`/new `BackgroundResponse` gain it;
`Dnd5eSheetCalculator` maps it straight through, same treatment as
`featureTraits`.

**The character's background is established for the first time**, via seed
data: Soldier, with a full set of characteristics and a short backstory —
giving the tab every field shape to render, including an empty `other` to
exercise the missing-value fallback.

**Frontend.** New `systems/dnd5e/BackgroundTab.tsx`: three sections (Background,
Characteristics, Notes), characteristics laid out as a two-column grid, notes
stacked. New `background-tab__*` CSS classes in `frames.css` (no frame asset).
`Dnd5eTabbedSection.tsx` gains the fifth tab.

Backend and frontend both required updating every `new Dnd5eSheet(...)`/
`new VitalsZone(...)` call site again (one more field each) — by now a
mechanical, low-risk step repeated across six prior slices.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live: the
Soldier background renders with its feature, all six characteristics in the
two-column grid, and all five notes fields — including "Other" showing the
em-dash fallback for its empty value.

Left out, matching the sixth-mold gap above: editing (name, feature text,
personality traits, backstory, etc. are all read-only this phase). One tab
remains in phase 8's build order (Extras).

---

## 2026-08-14 — Phase 8, slice 8: Features and Traits tab, display-only

Fourth tab of build order step 8. Unlike Inventory, this one had no deep coupling
question to resolve — `dnd-5e-sheet-ui.md`'s Features and Traits section describes
no mechanical consequence beyond the limited-use track already built for the
Actions tab, so this slice follows the Spells tab's precedent directly: a
display-only list, filterable and grouped, with "Manage feats" deferred for the
same reason "Manage spells" was (it needs a picker over a fixed feat catalog that
doesn't exist yet).

**A new list, not a reuse of `featureActions`.** The Actions tab's
`Dnd5eFeatureAction`/`featureActions` only ever held the subset of features that
grant an action (Second Wind, Action Surge). Features and Traits needs the full
catalog — passive class features (Fighting Style, Extra Attack), species traits
and feats, none of which grant an action. Rather than bend the existing shape to
cover both, new `Dnd5eFeatureTrait(key, name, category, source, description,
maxUses, usedCount, rechargeLabel)` in `ruleset/dnd5e` is an independent list;
`Dnd5eSheet` gains it alongside the existing `featureActions`. The two lists
overlap in content — Second Wind has an entry in both — but neither reads the
other; each tab pulls only the shape it needs. New `Dnd5eFeatureTraitCategory`
enum (`CLASS_FEATURE`/`SPECIES_TRAIT`/`FEAT`). New generic `FeatureTrait` in
`ruleset` (plain pass-through, nothing calculated); `VitalsZone` and
`CharacterSheetResponse` gain it; `Dnd5eSheetCalculator` maps it straight
through, same treatment as `featureActions`.

**The character's species is established for the first time**, via seed data:
Mountain Dwarf, with Darkvision and Dwarven Resilience. Combined with two more
class features (Fighting Style: Defense, Extra Attack) and a feat (Alert), the
seeded fighter now has one entry in each of the tab's three categories. None of
the three carry a mechanical effect — Alert's +5 initiative isn't factored into
the already-computed initiative value, matching how Fighting Style's +1 AC isn't
factored into armor class either — descriptive-only, the same treatment every
other feature on this sheet already gets.

**Frontend.** New `systems/dnd5e/FeaturesTab.tsx` and `FeatureTraitRow.tsx`,
following `SpellsTab.tsx`/`SpellRow.tsx`'s pattern closely: filter chips built
from the mold's four fixed categories (all/class features/species traits/feats),
entries grouped by `source` — dnd-5e-sheet-ui.md's own words, "grouped by class
and by species" — the same "derive groups from the data" approach `SpellsTab`
already uses for levels. A feature's name opens Entity Detail (metadata: source,
category label; action bar: `BoxTrack` when `maxUses` is set, read-only, same as
`FeatureActionRow`). `Dnd5eTabbedSection.tsx` gains the fourth tab.

Backend and frontend both required updating every `new Dnd5eSheet(...)`/
`new VitalsZone(...)` call site again (one more field each) — by now a
mechanical, low-risk step repeated across five prior slices.

Full `./gradlew :apps:api:check` and `npm run build` green. Verified live: all
three groups (Fighter, Mountain Dwarf, Feat) render with the right entries;
Second Wind and Action Surge show their box tracks; the "Species Traits" chip
narrows the list to Mountain Dwarf's two traits only; opening Darkvision's Entity
Detail shows "Mountain Dwarf, Species Trait" plus its description.

## 2026-08-14 — Phase 8, slice 7: Inventory tab, equip/attune left unwired on purpose

Third tab of build order step 8. Unlike Spells and Mechanic, Inventory has no
clean phase-9 gate — `roadmap.md` never assigns it there — but
`dnd-5e-sheet-ui.md` still describes real mechanical consequences: "equipped
state is not cosmetic: an equipped weapon appears in the actions tab, and
equipped armor or a shield contributes to armor class through the derivation
trace." Building the equip toggle without either consequence would misrepresent
what it does, the same problem this project avoids elsewhere by not shipping
inert controls. Confirmed scope with the owner before starting: build the real
mutations (items, attunement, coins), but leave the AC/Actions coupling as a
named follow-up rather than rewriting the already-built armor class formula and
attack list in the same slice.

**Backend.** New `Dnd5eItem(key, name, quantity, cost, notes, equipped,
attuned)` in `ruleset/dnd5e`; `cost` is a display string, not tied to the coin
totals — buying and selling isn't modeled, only carrying, matching how
proficiencies are "display strings, not modeled any richer." `Dnd5eSheet` gains
an items list plus five coin fields (copper/silver/electrum/gold/platinum) and
two new carry-over methods, `withItems`/`withCoins`. New generic `Item`/`Coins`
in `ruleset`, plain pass-through (nothing calculated this phase).

`SheetMutator` gains six methods: `addItem` (generates the item's key with
`UUID.randomUUID()` — items are user-created at runtime, unlike attacks/spells,
which are author-seeded with stable keys), `removeItem`, `toggleItemEquipped`,
`toggleItemAttuned` (rejects past the 3-item limit via new
`AttunementLimitExceededException`, but un-attuning is always allowed —
"features/character-sheet.md: Attunement: Bounded by the system's limit"),
`addCoins`/`removeCoins` (new `Dnd5eCoinDenomination` enum validates the
denomination, same pattern as `Dnd5eProficiencyCategory`; removing never drops a
total below zero). Six new endpoints on `CharacterController`
(`POST`/`DELETE /api/characters/{id}/items`, `.../items/{key}/equip/toggle`,
`.../items/{key}/attune/toggle`, `POST`/`DELETE /api/characters/{id}/coins/
{denomination}`).

**The seeded fighter's gear** covers every row shape: an equipped weapon and
shield, an unequipped spare suit of armor, a quantity-stacked consumable
(Potions of Healing ×2), and an attuned magic item (Cloak of Protection) —
plus 45 starting gold. Chosen to exercise the UI, not to be a complete starting
kit.

**Frontend.** New `systems/dnd5e/InventoryTab.tsx`, `ItemRow.tsx`,
`CoinsPanel.tsx`. `MutationAction` (`sheet/api.ts`) gains six variants
(`ADD_ITEM`/`REMOVE_ITEM`/`TOGGLE_ITEM_EQUIPPED`/`TOGGLE_ITEM_ATTUNED`/
`ADD_COINS`/`REMOVE_COINS`), routed through the same `postMutationAction`
switch hit points and conditions already use. Equip/attune toggle
optimistically in `CharacterSheetScreen`'s `handleMutate` (same treatment as
`TOGGLE_CONDITION` — a plain flip with a client-checkable constraint); add/
remove item and coins wait for the server, like proficiency edits, since there
is a server-generated key or a floor-at-zero clamp involved. The attune button
is disabled client-side at the 3-item limit as a courtesy, but the server is
still the real enforcement point. An item's name opens Entity Detail (metadata:
quantity, cost; action bar: the same equip/attune buttons as the row, for
parity with how `AttackRow`'s detail mirrors its own inline controls).

**Tests.** `Dnd5eSheetMutatorTest`: add/remove item, equip toggle, attune up to
the limit, attune beyond the limit rejected, un-attune always allowed even at
the limit, coin add/remove including floor-at-zero, unknown denomination
rejected. `CharacterControllerTest`: all six endpoints plus the two 400 cases.
Full `./gradlew :apps:api:check` and `npm run build` green.

Verified live: all five seeded items render with the right equipped/attuned
state and cost; equipping the spare Chain Mail flips its flag; adding 10 gold
brings the total to 55; adding "Torch" appends it; attuning Longsword and
Shield (on top of the already-attuned Cloak) reads "ATTUNEMENT (3 / 3)" and
visibly disables Chain Mail's attune button, which does nothing when clicked.

Left out, named rather than hidden: armor class does not yet look at equipped
armor/shields, and the Actions tab does not yet derive from equipped weapons —
both explicitly deferred, not silently cosmetic.

## 2026-08-14 — Phase 8, slice 6: Spells tab, scoped to its display-only half

Second tab of build order step 8, after Actions. Confirmed scope with the owner
first, the same way the Mechanic mold was handled: `dnd-5e-sheet-ui.md`'s Spells
tab spec is heavily entangled with phase 9 — "each group showing its slot track"
and "casting from the detail panel" are both spell-slot mechanics that don't exist
yet (`roadmap.md`: "spell slots... casting a spell into its damage roll" is phase
9's own scope line). Building those now would mean either a cast button with no
real mechanic behind it, or reaching into phase 9 ahead of schedule. So this slice
builds everything **except** slot tracks, casting and the "Manage spells"
collection editor: the header numbers, the spell list grouped by level with
concentration/ritual markers, level filter chips and search.

**Backend.** New `Dnd5eSpellcastingClass(className, abilityKey)` and
`Dnd5eSpell(key, name, className, level, school, concentration, ritual,
description)` in `ruleset/dnd5e`; `Dnd5eSheet` gains both as `@Valid` lists,
carried through `withSessionState`/`withProficiencies` unchanged — read-only
display data this phase, same treatment `attacks`/`featureActions` got in the
Actions tab slice. New generic `SpellcastingClassInfo`/`Spell` in `ruleset`;
`Dnd5eSheetCalculator` computes, per spellcasting class, `spellcastingModifier`
(the ability modifier alone), `spellAttackBonus` (modifier + proficiency bonus,
same shape as an attack's to-hit) and `spellSaveDc` (8 + modifier + proficiency
bonus) — each a `CalculatedValue` with its own contribution trace, so a later
slice can wire them to the Explainer for free if wanted. `VitalsZone` is a list of
these (not a single set), matching "a multiclass character has one set per
class." Spells pass through uncalculated, same as an ability score.
`CharacterSheetResponse` gains `spellcasting`/`spells`.

**The seeded fighter is written as an Eldritch Knight** (documented in
`DevCharacterSeeder`'s own doc comment) so the tab has a real spellcasting class
and spells to render instead of an empty state — intelligence-based, three
illustrative spells (Fire Bolt as a cantrip, Mage Armor as a plain 1st-level, and
Detect Magic as a 1st-level that's both concentration and ritual, to exercise
both markers at once and prove level-grouping works). The list is chosen to
exercise the UI shapes, not to be a RAW-accurate Eldritch Knight spell list.

**Frontend.** New `sheet/SearchField.tsx` — a shared primitive per
`ui-design-system.md`'s table (free-text filter over a list), no frame asset,
first used here. New `systems/dnd5e/SpellsTab.tsx`: one header card per
spellcasting class (plain display, not Explainer-wired — matches this slice's own
scope line), spells grouped by level (levels present in the data, not a fixed 0-9
range), level filter chips built dynamically from what's actually there, combined
with the search field. New `SpellRow.tsx` — no frame asset (same reasoning as
`AttackRow`), name opens Entity Detail (metadata: level, school, concentration/
ritual tags; no action bar, since there's nothing to cast yet), concentration/
ritual shown as small inline "C"/"R" badges. `Dnd5eTabbedSection.tsx` gains the
second tab.

**Tests.** `Dnd5eSheetCalculatorTest`: spellcasting numbers and contribution
counts for the seeded fighter, plus a case proving spellcasting uses its own
class's ability (wisdom for a cleric fixture), not always intelligence.
`CharacterControllerTest`'s sheet fixture and assertions extended. Full
`./gradlew :apps:api:check` and `npm run build` green.

Verified live: the Fighter card shows +0 modifier / +3 attack bonus / 11 save DC
(intelligence 10, proficiency +3 at level 5 — matches hand calculation); the "1st
Level" chip narrows the list to Mage Armor and Detect Magic; Detect Magic's row
shows both markers; opening it shows "1st Level, Divination, Concentration,
Ritual" and its description.

Left out, named rather than silently skipped: slot tracks, casting, "Manage
spells" (all phase 9); wiring the header numbers to the Explainer (would be
cheap given the `CalculatedValue` shape, but out of the scope confirmed for this
slice).

## 2026-08-14 — Phase 8, slice 5: Log mold, retiring the standalone game log

Closes the Log portion of build order step 8. The other mold on deck, Mechanic,
was deliberately **not** started this round — confirmed with the owner first: its
only trigger anywhere in `dnd-5e-sheet-ui.md`'s table is short/long rest, and
resolving a rest is explicitly phase 9 scope (`roadmap.md`: "short and long rest
resolved server-side as a single mechanic"). Building the mold now would mean
either a confirm button with no real mechanic behind it — the same inert-control
problem this project has consistently avoided — or reaching into phase 9's actual
resource/rest logic ahead of schedule, which `roadmap.md`'s own rule forbids ("One
phase at a time. Do not start the next one, even if it looks trivial."). Revisit
Mechanic once phase 9 opens.

**Log instead**, since it had a real, already-built data source and no such
dependency: the roll history `CharacterSheetScreen` has fetched since phase 5.

**What changed.** New `LogRequest` (`entries: RollResult[]`) joins `SidebarContent`'s
union; `Sidebar.tsx`'s dispatch is a small `switch` now that there are four kinds.
New `sheet/LogPanel.tsx` ports the row layout phase 5's standalone `GameLog`
used — same reverse-chronological list, now inside the sidebar's Log mold instead
of a panel of its own. Same live-refresh treatment the Collection editor slice
established: `entries` is re-derived from the live `rollHistory` state on every
render rather than frozen at open time, so a roll made while the log is open
appears immediately with no extra bookkeeping.

**The standalone `GameLog` panel is retired** — `dice/GameLog.tsx` deleted, its
`.game-log*` rules removed from `dice.css`. It was always documented as
provisional (phase 5's changelog entry: "not the sidebar's Log mold, which is
phase 8"), and keeping both would have shown the same list twice on screen for no
reason. `SheetShell.tsx` gained a labelled "Game log" header button (per
`dnd-5e-sheet-ui.md`'s deviations list — D&D Beyond's own is an unlabelled icon) —
the first of the header's five action buttons (share, short rest, long rest, game
log, edit character) to exist; the other four stay out (rest is phase 9 per above;
share and edit character have no feature behind them yet).

Frontend-only; no backend change. Verified live: the header's "Game log" button
opens the sidebar with the full existing roll history, newest first; rolling an
ability check while it was open added the new entry to the top immediately,
without closing and reopening the panel.

## 2026-08-14 — Phase 8, slice 4: Collection editor mold, wired to Proficiencies & Training

Closes the Collection editor portion of build order step 8. Unlike the previous two
slices, this one needed real backend work: the mold's job is "add and remove from a
set," and the four proficiency/training lists were read-only display data until now.

**Why Proficiencies & Training and nothing else.** The Collection editor's trigger
list (`dnd-5e-sheet-ui.md`) also names "manage spells, manage feats, manage
inventory, manage extras, coins" — none of those have any model behind them yet
(no spells, feats, inventory items or extras exist anywhere in `Dnd5eSheet`).
Proficiencies & training was the only trigger with real data to edit, confirmed
with the owner before starting rather than inventing data for the others.

**Backend.** New `Dnd5eProficiencyCategory` enum (`ARMOR`/`WEAPON`/`TOOL`/`LANGUAGE`);
`Dnd5eSheet.withProficiencies(...)` mirrors `withSessionState`'s carry-everything-else-
over pattern. `SheetMutator` gained `addProficiency`/`removeProficiency(sheetJson,
category, value)`; `Dnd5eSheetMutator` validates the category against the enum
(unknown category → new `InvalidProficiencyCategoryException`, mapped to 400 in
`ApiExceptionHandler`, same treatment as `InvalidConditionException`), adds
idempotently (adding an already-present value is a no-op, not a duplicate), removes
tolerantly (removing an absent value is a no-op, not an error). New endpoints:
`POST`/`DELETE /api/characters/{id}/proficiencies/{category}` on `CharacterController`,
new `AddProficiencyRequest(@NotBlank String value)` for the POST body, `value` as a
query param for DELETE.

**Frontend.** `Sidebar.tsx`'s content union gains a third member,
`CollectionEditorRequest` (title, per-category sections, `onAdd`/`onRemove`), and its
dispatch is now a small `renderContent` switch instead of a ternary now that there
are three molds. New `sheet/CollectionEditorPanel.tsx`: each section lists its
entries with a remove (×) button plus a plain text field and an Add button — not a
"searchable picker" in the mold's fuller sense, since armor/weapon/tool
proficiencies and training are free text with no fixed universe to search over
(`Dnd5eSheet`'s own doc comment: "display strings, not modeled any richer"). A
future collection editor with a real candidate list (spells from the catalogue,
say) would need its own picker UI, not a change to this shape.

**Live-refreshing the open editor without extra bookkeeping.** The editor's
`sections` are re-derived from the live `sheet` on every render
(`displayedSidebarContent` in `CharacterSheetScreen.tsx`) rather than trusted as a
frozen snapshot from when it was opened — so `handleAddProficiency`/
`handleRemoveProficiency` only need to update `sheet` after the server responds;
the open panel picks up the change for free on the next render. Both wait for the
server rather than updating optimistically, the same reasoning already applied to
hit point amounts: there's no local rule to compute, so there's no reason not to
just ask the server.

**`Panel.tsx`** gained an optional `onSettings` prop — the settings-gear affordance
`dnd-5e-sheet-ui.md` calls for on left-column panels ("Each has a settings
affordance opening its collection editor or explainer"), a plain gear glyph since
no icon asset exists. `ProficienciesPanel` is the first (and so far only) panel to
use it.

**Tests.** `Dnd5eSheetMutatorTest`: add/remove, duplicate-add is a no-op,
remove-of-absent is a no-op, unknown category rejected. `CharacterControllerTest`:
both endpoints, blank-value validation, unknown-category 400. Full
`./gradlew :apps:api:check` and `npm run build` green.

Verified live: opened the editor from the gear on Proficiencies & Training, added
"Thieves' Tools" to an empty Tools category — appeared instantly in both the open
sidebar and the panel behind it (previously-hidden empty category reappeared too,
since `ProficienciesPanel` already filtered empty categories); removed it — both
panels dropped it immediately, and the category disappeared from the panel behind
again once empty.

## 2026-08-14 — Phase 8, slice 3: Entity Detail mold, wired to the Actions tab

Closes the Entity Detail portion of build order step 8. Rather than jump to a new
tab, this finishes what the Actions tab already started: `AttackRow` and
`FeatureActionRow`'s name buttons have been inert reveal targets since slice 1,
waiting on exactly this mold. Frontend-only, same as the sidebar slice — no backend
data was missing, just the mold to show it in.

**`sheet/Sidebar.tsx`'s content is now a union.** A second real mold justifies the
generalization that would have been speculative after the first: `SidebarContent =
ExplainerRequest | EntityDetailRequest`, each tagged with a `kind` discriminant,
dispatched with a single `content.kind === 'explainer' ? ... : ...`. `ExplainerRequest`
gained the `kind: 'explainer'` tag (its three existing construction sites in
`Dnd5eVitalsTopRow`, `SkillsPanel` and `Dnd5eCombatColumn` updated accordingly);
`CharacterSheetScreen` now holds one `sidebarContent` state slot instead of an
`explainer`-only one, fed by both `handleExplain` and the new `handleOpenDetail`.

**New `sheet/EntityDetailPanel.tsx`** renders name, action bar, metadata and
description — the mold's full spec (`ui-design-system.md`) also lists an icon, a
source line and tags, all left out: no icon assets exist for any entity, and
neither `Dnd5eAttack` nor `Dnd5eFeatureAction` tracks a source book or tags. Adding
those fields now would mean inventing data with nothing behind it. `actionBar` is a
caller-built `ReactNode` rather than a data shape, since "only its action bar
varies" per the mold's own spec — `ActionsTab.tsx` builds it per entity: an
attack's action bar is the same `onRoll('ATTACK_HIT'|'ATTACK_DAMAGE', key)` pair
already wired to the row's own inline buttons; a feature's is the existing
`BoxTrack`, still read-only (spending/recharging a use stays phase 9's concern).

`AttackRow`/`FeatureActionRow` gained a plain `onOpenDetail: () => void` prop on
their name button — the components themselves stay dumb, same as every other
reveal target; `ActionsTab` (their parent) is what builds the actual
`EntityDetailRequest`, matching the convention `SkillsPanel` already set for
`ExplainerRequest`.

**Left inert, named rather than silently skipped:** the nine standard combat
action rows still have no button at all — same reasoning that left saving throw
and sense rows out of the Explainer slice, wiring them means adding a new click
target, not wiring a dead one.

Verified live: clicking "Longsword" opens Entity Detail ("Longsword / 5 ft
slashing / [Attack +6] [Damage 1d8+3] / Versatile (1d10)"); clicking the sidebar's
own "Attack +6" button rolls for real (appeared in the log and dice tray, sidebar
stayed open); clicking "Second Wind" replaces it with the feature's detail
("Second Wind / BONUS ACTION · Short or long rest / [box track] / Regain 1d10 +
fighter level hit points"); clicking Strength's score afterward replaces that with
the Explainer panel — confirming the union dispatch and panel-replacement behavior
both work across mold types, not just within one.

## 2026-08-14 — Phase 8, slice 2: sidebar shell with the Explainer mold

Closes build order step 7 (`dnd-5e-sheet-build.md`). Frontend-only — every value the
Explainer needs (a `CalculatedValue` with its contributions) already arrives on
`CharacterSheet`, so no backend change was needed.

**Scope, confirmed with the owner up front:**

- **Sidebar chrome:** only the hide control ships. `ui-design-system.md` also
  specifies lock and move-to-left/right-edge, but nothing on the sheet needs a panel
  pinned open past its own content or re-anchored to the left yet — building those
  now would be speculative. Add them when a real need calls for it.
- **Explainer wiring:** every *existing* inert `.reveal` button gets wired, not only
  ability scores as the build order's own text literally says — the plumbing is
  identical for all of them, so doing this in a later near-duplicate slice would be
  pure waste. Wired: `AbilityBox`'s score, `StatBadge`'s value (serves both
  proficiency bonus and speed), `ArmorClass`'s value, `SkillRow`'s name. **Not**
  wired: `SavingThrowRow` and `SenseRow` — both are listed as Explainer triggers in
  `dnd-5e-sheet-ui.md`'s trigger table, but neither has an existing reveal button
  (`SavingThrowRow` is only ever a roll target; `SenseRow` has no button at all,
  its own code comment already flagging the sidebar as the reason). Wiring those
  means adding a new click target, not wiring a dead one — left for a future slice,
  named here rather than silently skipped.

**What changed.** New `ExplainerRequest`/`ExplainHandler` in `sheet/api.ts` — a
title, a display-ready value (the caller formats it, since the sign convention
differs: a modifier is signed, armor class and speed are not) and the
contributions list, deliberately without rules text, since no rules-text data
source exists anywhere in the system yet (that's catalogue-adjacent content with
no consuming UI). New `sheet/Sidebar.tsx` (fixed, right-anchored, renders exactly
the one `explainer` prop it's given — panel-replacement falls out of that for free,
no stack needed) and `sheet/ExplainerPanel.tsx` (the mold's content). No frame asset
for either (`ui-design-tokens.md`'s "still unmeasured" list already named the
sidebar), so both are plain CSS, the same estimate-and-flag treatment `SheetShell`
already applies to its own unmeasured chrome.

`AbilityBox`, `StatBadge`, `ArmorClass` and `SkillRow` each gained an
`onOpenExplainer` prop wired to their existing (previously inert) reveal button.
`Dnd5eVitalsTopRow`, `SkillsPanel`, `Dnd5eCombatColumn` and `Dnd5eVitalsColumns` build
the `ExplainerRequest` from data they already have and thread a single `onExplain`
handler down — same shape as `onRoll`/`onMutate`. `CharacterSheetScreen` owns the
open-explainer state and renders `<Sidebar>` last, alongside the dice tray.

**Verified live:** clicking Strength's score opens "STRENGTH / +3 / Strength score
16 +3"; clicking Armor Class while that panel is open replaces it with "ARMOR CLASS
/ 12 / Base (unarmored) +10 / Dexterity modifier +2" — no stacking, confirming the
panel-replacement behaviour the build order names as this slice's goal; clicking a
skill name (Acrobatics) does the same; the hide (×) control closes the panel. No
backend restart needed this time — the sheet shape didn't change.

Left out, for a future slice: saving throw and sense reveal targets (need a new
button, see above); lock and move-to-edge sidebar controls; the Entity detail mold
(attack/feature action rows' reveal buttons stay inert — different mold, out of
this slice); the remaining five tabs (build order step 8).

## 2026-08-14 — Phase 8, slice 1: tab bar with the Actions tab

Closes build order step 6 (`dnd-5e-sheet-build.md`) — the first slice of phase 8,
which `roadmap.md` now tracks as "In progress". Scoped to exactly this one slice, per
the owner's confirmation: standard combat actions are static content, and a feature
action's usage track is read-only this phase (spending/recharging a use is phase 9's
resource model, not this one).

**Backend.** New `Dnd5eAttack`/`Dnd5eFeatureAction`/`Dnd5eActionType` in
`ruleset/dnd5e`, added to `Dnd5eSheet` as two new Bean-Validated lists (carried
unchanged through `withSessionState`). `Dnd5eSheetCalculator` computes each attack's
to-hit as ability modifier + proficiency bonus, with the same labelled-contribution
trace every other calculated value gets — proficiency is assumed for every listed
attack, the same simplification saving throws and skills used before their own
proficiency flags existed; there is no weapon-proficiency check yet. Feature actions
pass through unchanged (nothing to calculate: they're stored data, not derived).

New generic `AttackRow`/`FeatureAction` in `ruleset` (not `ruleset/dnd5e`) — `VitalsZone`
gains `attacks` (keyed by attack key, same pattern as skills/saving throws) and
`featureActions`. `RollKind` gains `ATTACK_HIT`/`ATTACK_DAMAGE`; `Dnd5eMechanicResolver`
resolves both from `vitals.attacks()` — hit is a d20 plus the calculated to-hit value,
damage is the attack's own dice count/sides plus its ability-modifier bonus, reusing
`ResolvedRoll`'s existing shape rather than inventing a second one for damage. New
`AttackRowResponse`/`FeatureActionResponse` wire both into `CharacterSheetResponse`.
`DevCharacterSeeder`'s fighter gained two attacks (Longsword, str-based; Shortbow,
dex-based) and two feature actions (Second Wind, Action Surge — both a 1-use box
track recharging on a rest) so the tab has real data to render.

**Frontend.** Three new shared primitives per `ui-design-system.md`'s table — not
`systems/dnd5e/`, since none of them use dnd5e's frame ornaments: `sheet/TabBar.tsx`,
`sheet/FilterChips.tsx`, `sheet/BoxTrack.tsx`. All three are plain CSS with no frame
asset (`ui-design-tokens.md`'s "still unmeasured" list already named the tab bar;
filter chips and box track join it), the same estimate-and-flag treatment
`SheetShell.tsx` already applies to its own unmeasured spacing.

New `systems/dnd5e/ActionsTab.tsx` renders one continuous, ordered list — attacks,
then the nine standard combat actions, then class features — per
`dnd-5e-sheet-ui.md`'s own fixed order, with the filter chip row narrowing that same
list rather than toggling independent sections ("limited use" matches by track
presence, cutting across the action-type chips). `AttackRow.tsx` and
`FeatureActionRow.tsx` follow `SkillRow`'s precedent: no frame asset, a plain flex row,
name as an inert `.reveal` button (the entity detail mold doesn't exist until the next
slice), hit/damage as `.roll` buttons. `standardActions.ts` is a static list — the nine
actions read identically for every 5e character, so this is not fetched from the API
and not sourced from the phase-7 catalogue (which has no consuming UI yet and would
need fixtures for nine fixed entries that never change). New
`systems/dnd5e/Dnd5eTabbedSection.tsx` owns which tab is active (only "Actions" exists
so far) and sits between the vitals columns and the game log in
`CharacterSheetScreen.tsx`.

**Tests.** `Dnd5eSheetCalculatorTest` covers to-hit's value and contribution count for
the seeded fighter, plus a case proving an attack uses its own `abilityModifierKey`
(dexterity) rather than always strength. `Dnd5eMechanicResolverTest` covers both new
roll kinds and an unknown attack key. `CharacterControllerTest`'s sheet fixture and
assertions extended for the new response fields. Full `./gradlew :apps:api:check` and
`npm run build` green.

Verified live: reset the dev player/character/rolls (the seeded sheet predates the new
`attacks`/`featureActions` fields, so its stored JSON would 500 the same way the phase
4 `speed` gap once did) and restarted the backend process, since it doesn't hot-reload
— the same round trip the phase-4 top-row slice already hit once. Confirmed in the
browser: Longsword (+6 hit, 1d8+3 slashing) and Shortbow (+5 hit, 1d6+2 piercing) match
hand calculation for the seeded fighter (STR 16, DEX 14, level 5); both attack rolls
resolve server-side and land in the dice tray; the "Limited Use" chip correctly narrows
the list to Second Wind and Action Surge only.

Left out, for the next slice: the sidebar and its entity detail mold (build order step
7) — every reveal target in this slice is inert until then. Also left out: the
remaining five tabs (build order step 8).

## 2026-08-14 — Frame assets migrated from PNG to SVG

The owner supplied vector versions of every frame asset (`apps/web/public/frames/svg/`),
plus two new frames that didn't exist before (`dnd_frame_senses.svg`,
`dnd_frame_inspiration.svg`), and asked for the frame-box architecture to use them —
crisp at any zoom, and no longer dependent on a raster image-editing tool to produce a
new crop. Old PNGs are left on disk, unreferenced; nothing was deleted.

**Simple, single-piece frames** (ability box, badge, saving throw, armor class,
initiative) — a one-line swap per `:root` variable, same `mask-size: 100% 100%`
technique as before, just a sharper source.

**Two new components get real frames.** `SenseRow` now renders inside
`dnd_frame_senses.svg` (the pill-with-bulge shape D&D Beyond itself uses) instead of a
plain CSS badge; the bulge/bar proportions were measured by rendering the SVG directly
and reading pixel fractions, not guessed (same discipline as the earlier D&D Beyond
width measurement). `HeroicInspiration` now renders inside `dnd_frame_inspiration.svg`
instead of a bordered box.

**Panel slicing rebuilt on vector crops instead of raster ones.** Unlike the old PNGs,
each new SVG is one full, uncropped frame — there's no separate cap/middle/corner/edge
export. Rather than inventing a single-file windowing technique, seven new derived SVG
files were generated (`dnd_frame_features_cap.svg`, `_middle.svg`,
`dnd_frame_proficiencies_cap.svg`, `_middle.svg`, `_corner.svg`, `_top_edge.svg`,
`_side_edge.svg`) by copying the source file's path data verbatim into a new `<svg>`
wrapper with a windowed `viewBox` — the browser clips everything outside that viewBox
automatically, so this is a lossless vector crop, not a rasterize-and-cut operation.
No image-editing tool needed for this or future re-crops — just a `viewBox` edit. The
crop boundaries reuse the exact windows the old PNG crops used (188/406 for the ornate
cap, 65/681 and 30×65 for the plain cap/corner), verified by rendering each new crop
file directly and confirming it lands in an ornament-free zone before wiring it in —
`Panel.tsx` and `Panel9Slice.tsx` themselves are unchanged, only the seven `:root`
asset URLs.

**Bug: `preserveAspectRatio="xMidYMid meet"`.** Every source SVG (the owner's originals
and the seven derived crops) carried this from its export tool. It tells the SVG to
"meet"-fit its own content inside whatever box it's asked to fill, preserving its own
aspect ratio and letterboxing the rest — which is invisible when the box's aspect
ratio happens to match the source closely (true for every Fase A/B asset, so those
looked correct immediately) but means the content shrinks into a sliver, surrounded by
transparent padding, when the box's aspect ratio is wildly different from the source's
— exactly the case for the 9-slice's edge cells (a 30×50 source stretched to a
15px-wide, ~150px-tall cell). The Defenses/Conditions panel rendered with the corners
present but the connecting edges essentially invisible until this was found. Fixed by
setting `preserveAspectRatio="none"` on all sixteen frame SVGs (nine originals, seven
crops) — CSS `mask-size: 100% 100%` now genuinely stretches to fill, matching how the
old PNGs always behaved. Verified visually across every panel afterward, not just the
one that broke.

## 2026-08-13 — Phase 7: Content catalogue

Reference content (spells, items, features, creatures) now has a schema, an import
pipeline and a redaction switch — closes roadmap.md phase 7. No UI, as scoped: this is
backend-only, proving the mechanism the way phase 2 proved the character vault before
phase 3 gave it real rules.

**Schema.** New `catalogue_entries` table (migration V4), one row per entry, following
the same shape as `characters`: relational columns for what's filtered or constrained
(`system_id`, `kind`, `slug`, `tags`), JSONB `data` for what varies by kind and system
(a spell's level and school, a creature's stat block — nothing consumes this yet, so
it stays generic rather than guessing a shape). `UNIQUE (system_id, kind, slug)` is
the natural key that makes re-importing idempotent. `description` is its own column,
not inside `data` — it's the one field redaction touches, and every kind has it.

**New `catalogue` package**, alongside `character`/`dice`/`ruleset`:

- `CatalogueEntry` / `CatalogueEntryRepository` / `CatalogueEntryKind` — no ownership
  check anywhere in this package; reference data belongs to no one player.
- `RedactableText` — the redactable text field type the API contract needed (adr-0005).
  When redaction is on, `value` is never populated in the response — redaction happens
  server-side, never as something the client is trusted to hide.
- `CatalogueService` — resolves `systemId` through the existing `GameSystemRegistry`
  (no new registry needed) and applies redaction at the one seam every read passes
  through, from a single `app.catalogue.redaction-enabled` switch.
- `CatalogueController` — `GET /api/catalogue?systemId=&kind=` and
  `GET /api/catalogue/{id}`, authenticated like everything else (no per-endpoint
  opt-out; reference data still requires a valid token, it just isn't ownership-checked).
- `CatalogueImportService` / `CatalogueEntryImport` — reads every `*.json` file in a
  directory, validates it with Bean Validation, upserts by the natural key. Wrapped in
  one `@Transactional`, so a bad file fails the whole import rather than leaving a
  partial one.
- `CatalogueImportRunner` — a `CommandLineRunner` that does nothing unless the process
  is started with `--import-catalogue=<directory>`, in which case it imports and exits
  instead of starting the web server. This is what makes it "a command, not startup"
  per roadmap.md's phase 7 scope — ordinary `bootRun` or a deployed jar never imports
  anything on its own.

**Redaction switch.** `app.catalogue.redaction-enabled: true` by default
(`application.yml`, public-deployment-safe), overridden to `false` in the new
`application-dev.yml` (active whenever the `dev` profile is, which `bootRun` already
sets) — declarative config instead of a profile check in Java.

**Verified against the real database**, not just Testcontainers: ran
`./gradlew :apps:api:bootRun --args='--import-catalogue=<dir>'` against two synthetic
fixture files, confirmed the migration applied and both rows landed; ran it a second
time and confirmed exactly two rows still existed (`updated_at` changed, no
duplicates) — the upsert is idempotent in practice, not just in a test assertion.

**Tests.** `CatalogueImportServiceTest` (import, idempotent re-import, invalid-entry
rejection — all against real Postgres via Testcontainers, matching this codebase's
existing convention over mocked unit tests). `CatalogueServiceTest` (redaction on/off,
unknown system, missing entry) constructs the service directly with each redaction
value rather than juggling Spring profiles mid-test. `CatalogueControllerTest` covers
the HTTP shape and the 404/401 cases. Full `./gradlew :apps:api:check` green.

Test fixtures use synthetic content ("Sparkling Bolt", "Traveler's Pack"), not real
book text — adr-0005 documents that the real catalogue is a deliberately accepted
legal exposure; there was no reason to extend that risk to test data.

One incidental fix: JUnit 5's `@TempDir` was flaking on this Windows machine with
`AccessDeniedException` during post-test cleanup (a known Windows file-locking issue,
likely antivirus/indexing). Both new tests that needed a scratch directory create one
by hand in `@BeforeEach` (`Files.createTempDirectory`) and don't delete it afterward,
sidestepping the cleanup step entirely rather than fighting the lock.

Left out: any endpoint or UI that displays catalogue content (explicitly out of scope
this phase); modeling kind-specific mechanical fields as real columns (deferred until
a consuming feature — likely phase 9's spellcasting — proves what shape is actually
needed, same reasoning `Dnd5eSheet` followed from phase 2 to phase 3).

## 2026-08-13 — Phase 6: Session state

Hit point damage/healing/temporary points, heroic inspiration and condition toggling
are now real mutations instead of read-only display — closes roadmap.md phase 6. No
migration: `Dnd5eSheet` already carried `currentHitPoints`, `temporaryHitPoints`,
`heroicInspiration` and `activeConditions` as read-only fields since phase 3; this
phase makes them writable.

**Backend.** New `SheetMutator` strategy interface (`ruleset/SheetMutator.java`) and
`SheetMutatorRegistry`, mirroring `SheetCalculator`/`SheetCalculatorRegistry` — every
mutation method takes the sheet JSON and returns the updated JSON, keeping the rules
inside the ruleset seam and out of the orchestrator. `Dnd5eSheetMutator` implements the
5e rules:

- Damage consumes temporary hit points first, then current, floored at zero.
- Healing never restores temporary points and never exceeds max hit points.
- Temporary hit points never stack — PHB rule: take the higher of the old and new
  value, not the sum.
- Inspiration is a plain boolean flip.
- A condition toggle is validated against a canonical fourteen-condition list
  (`Dnd5eConditions`, exhaustion excluded — it tracks a level, not a boolean); an
  unknown key throws `InvalidConditionException` (400), mapped alongside the other
  ruleset exceptions in `ApiExceptionHandler`.

`Dnd5eSheet` gained `withSessionState(...)`, a single method that replaces just the
four phase-6-mutable fields and carries everything else over — avoids reconstructing
the ~24-argument record by hand in five different places. The max-hit-points formula
was duplicated between the calculator (which needs the derivation trace) and the new
mutator (which only needs the healing ceiling); extracted both it and the ability
modifier formula into `Dnd5eFormulas` so the two can't drift apart.

Five new endpoints on `CharacterController`, all returning the recalculated
`CharacterSheetResponse` (same DTO the GET already used) so the caller always gets the
server's authoritative state back:

- `POST /api/characters/{id}/hit-points/damage`
- `POST /api/characters/{id}/hit-points/heal`
- `POST /api/characters/{id}/hit-points/temporary`
- `POST /api/characters/{id}/inspiration/toggle`
- `POST /api/characters/{id}/conditions/{condition}/toggle`

`Character` gained a `replaceSheet(String)` overload that keeps the existing schema
version, since mutations never change it — only creation and migration do.

**Frontend.** `HeroicInspiration` and `ConditionRow` are now buttons; `HitPoints`
gained an amount field feeding three buttons (Damage/Heal/Temp). `DefensesConditionsPanel`
now renders all fourteen conditions as a two-column checklist instead of only the
active ones — toggling one on needs somewhere to click, and dnd-5e-sheet-ui.md already
specified this ("Conditions can be toggled and are displayed on the sheet"). New
`.mutate` CSS affordance (border highlight on hover/focus, not `.roll`'s background
fill — documented in `ui-design-system.md`'s new "The mutate affordance" section).

`CharacterSheetScreen` applies inspiration and condition toggles optimistically (pure
boolean/set flips, no game rule involved) and rolls back with an error message if the
request fails. Hit point amounts are **not** guessed locally — damage's temp-first
consumption and healing's max-hit-points cap are rules that live only in the API
(ground-rules.md: "No business rules in the frontend"), so those wait for the server's
recalculated sheet rather than rendering a locally-computed guess that could drift
from the real rule.

**Tests.** `Dnd5eSheetMutatorTest` covers every rule (temp-first damage, healing cap,
temp-HP non-stacking, inspiration flip, condition toggle both directions, unknown
condition rejection). `CharacterControllerTest` covers all five endpoints plus the
400 on a bad amount and an unknown condition. Full `./gradlew :apps:api:check` and
`npm run build` both green.

Left out: a pending/loading indicator on the hit point buttons while awaiting the
server's response — the request is local and fast enough that it wasn't judged worth
the complexity this slice; worth revisiting if it ever feels laggy in practice.

## 2026-08-13 — Senses badge order flipped to match the reference

Owner noticed the Senses panel (Passive Perception/Investigation/Insight) had no
value badge, unlike D&D Beyond, and asked for one "practically inverted" —
badge first, then label, the mirror of skill and saving-throw rows which put
the badge last.

D&D Beyond's own sense row uses a bespoke SVG pill (`viewBox="0 0 241 34"`,
circular bulge on the left) — confirmed by reading its DOM directly, not by
eyeballing the screenshot. No such asset exists in this project yet, and per
the owner's own note (when scoping the Defenses/Conditions work), SVG-shaped
badges are something they'll supply as assets later rather than something to
approximate now. So `SenseRow` reuses the project's existing plain-CSS
no-mask badge style (matching `.skill-row__mod`'s border/radius/font
treatment), just reordered value-first, rather than attempting to replicate
the reference's pill shape without the asset.

New `SenseRow.tsx` renders `[value badge] [LABEL]`; `SensesPanel.tsx` now maps
over `SENSES` through it inside `.panel__rows`, replacing the old `dl`/`dt`/`dd`
stacked-label layout. New CSS: `.sense-row`, `.sense-row__value`,
`.sense-row__label` in `frames.css`, inserted in the "no frame asset yet" row
group ahead of `CONDITION ROW`. Verified with `npm run build` (clean) and a
live browser check — badges render as filled circles left of the uppercase
label, as intended.

Left out: matching the reference's actual pill-with-bulge SVG shape (needs the
asset), and the "Darkvision 60 ft." line the reference shows beneath the three
passive senses (not modeled anywhere in this app yet, and not requested).

## 2026-08-13 — Resistance icon, and the merged panel widened

Two follow-ups to the 9-slice merge, both owner-requested:

- **Resistance gets a colored letter badge.** New `DamageTypeIcon` — a plain
  CSS circle (green, "R"), not an imported icon asset; none exists for this.
  Only Resistances got one: Immunities, Vulnerabilities and Condition
  Immunities stay plain text under their own heading rather than guessing a
  color/letter for categories nobody specified yet. Each resistance now renders
  as its own row (icon + name) instead of a comma-joined list, since the icon
  needs a slot per item.
- **The merged panel is wider.** `Panel9Slice` gained a `className` prop so
  `.defenses-conditions-panel` can set its own width. First pass used
  `min-width: 572px` — the sum of the two panels' widths plus their gap before
  they merged (278 + 16 + 278) — but that was a derived guess, and the owner
  called it out as too wide on sight. Went to the reference itself instead of
  re-guessing: `getBoundingClientRect()` on `.ct-combat__statuses` (the actual
  merged box in the live DOM) measured **408×95px**. `min-width` is now 408px,
  a measured value, not a derived one. Verified live: the 9-slice frame stays
  crisp at this width too, confirming the corner/edge boundaries measured for
  the previous entry hold up beyond the size they were first tested at.

## 2026-08-13 — A true 9-slice frame; Defenses and Conditions share one

Owner noticed D&D Beyond renders Defenses and Conditions as one frame with a
divider, not two separate boxes, and asked to match it. Chose to build the
horizontal 9-slice technique left as a known gap after the Saving Throws revert,
rather than the cheaper "just widen it" option that caused that revert in the
first place — this panel needed to roughly double in width (278px → ~570px
worth of content), a bigger stretch than the 1.37x that already looked wrong.

What changed:

- Measured the plain frame's horizontal corner boundary the same way the
  vertical one was measured earlier this phase: crop increasingly wide slices
  from the left edge of `dnd_frame_proficiencies.png` until the corner's tick
  ornament fully resolves into a plain straight line. Landed on a 30×65 native
  corner (paired with the existing 65px-native vertical cap — same boundary,
  now protected on both axes) plus two plain stretch strips: a horizontal one
  for the top/bottom edges, a vertical one for the left/right edges.
- New `--f-panel-corner-plain`, `--f-panel-top-edge-plain`,
  `--f-panel-side-edge-plain` and a `.panel9` CSS block: a 3×3 grid — 4 corners
  at fixed size (the same one asset, reused via `scaleX`/`scaleY`/`scale(-1,-1)`
  mirroring for the other three), 4 edges stretched in one axis via
  `mask-size: 100% 100%` on a cell sized to match, center cell empty (the
  source's interior is already transparent, so there's nothing to render
  there). New `Panel9Slice.tsx` renders the 8 pieces; `Panel.tsx` (used
  everywhere else) is untouched.
- Hit the same scaling bug the vertical technique already had to solve once:
  first pass used the corner's *native* 30×65 pixels directly as CSS pixels,
  forgetting that these assets are exported at 3x and every other panel
  displays them at the 278/561 ≈ 0.4955 scale (`--panel-cap-h: 32px` already
  *is* 65 × 0.4955 — I just didn't reapply that factor to the new horizontal
  measurement). Corners rendered roughly twice intended size and ate the whole
  box, leaving no visible edge between them. Fixed: `--panel9-corner-w: 15px`,
  `--panel9-corner-h: 32px` (32 matching the existing plain cap height exactly,
  as it should — same boundary, same scale). Added `min-width`/`min-height`
  floors at twice the corner size, same reasoning as `.panel`'s existing floor.
- New `DefensesConditionsPanel` replaces the separate `DefensesPanel` and
  `ConditionsPanel` (both deleted, not left unused — nothing else referenced
  them). Two flex columns share the frame with a 1px divider between them;
  each column's `.panel__title` gets `margin-top: auto` so "Defenses" and
  "Conditions" sit at the same baseline regardless of which side has more
  content, matching the reference.

Verified live: the merged panel reads as one continuous frame — top and bottom
edges connect the corners cleanly, corners are crisp at native tuned size, the
divider and both captions align the same way the reference does.

Left out: the corner/edge crop boundaries are estimated from inspecting the
asset, not measured against a live reference the way the saving-throw panel's
own dimensions were — reasonable confidence, not the same rigor. Also left the
ornate frame without a 9-slice cut, since nothing currently needs one wider
than 278px.

## 2026-08-13 — Three low-cost fidelity fixes from a live D&D Beyond comparison

Owner asked me to check a live D&D Beyond character sheet (with their own account
already signed in, in the same browser profile — a new tab I opened inherited the
session cookie, so no login step was needed) and compare it against what's built
so far. Pulled real computed values via the page's own DOM/CSS, not just eyeballing
screenshots — confirmed several already-built values are exactly right (ability
box 81×95, saving-throws panel 281×200, saving-throw item 107×34, body font and
background all match), and found several gaps. Implemented the three that need no
new asset; left out the rest — see below.

What changed:

- **Initiative caption.** The hexagon had no label at all; the reference shows
  "INITIATIVE" above it. `Initiative.tsx` now wraps the existing frame-box (still
  78×52, untouched) in `.initiative-wrap` with a caption above — the hexagon itself
  has no room to fit a label inside without re-tuning its proportions, so the
  caption sits outside it, matching how the reference does it too.
- **Skill bonus gets a visible border.** `.skill-row__mod` was plain text with only
  a hover background; the reference wraps each bonus in a bordered pill at rest.
  Added `border: 1px solid var(--border-control)`.
- **Conditions shows only active conditions, not all fourteen.** The reference
  displays an empty state, not a checklist of every possible condition with a
  hollow/filled dot — ours doesn't have that mutation yet either (toggling is
  phase 6), so a dot-per-condition implied more interactivity than exists.
  `ConditionsPanel` now filters `CONDITIONS` down to `activeConditions` and shows
  "No active conditions" when empty. Pure frontend change — `activeConditions` was
  already in the API response, nothing new to fetch.

Left out, at the owner's direction ("only the easy ones") — each needs an asset we
don't have yet, not just a CSS change:

- **Heroic Inspiration** — the reference uses a book/tome icon in a shield, not a
  plain dot. No icon asset exists for this.
- **Defenses entries** — the reference shows a small colored shield icon per item
  (green for resistance, tooltip-labelled), not plain text under a category
  heading. No icon asset exists for this either.
- **Saving-throw badge as SVG.** Read the actual DOM: the reference's pill isn't a
  CSS shape or a raster mask like ours — it's an inline SVG (`viewBox="0 0 116.1
  34"`, `fill="#92a2b380"`), and the panel frame itself is the same technique
  (`viewBox="0 0 281 200"`, `fill="#FEFEFE"`). That's why it scales cleanly at any
  size with no blur and no distortion — a fundamentally different technique from
  our raster-PNG-plus-CSS-mask frames. Revisiting this is an asset/architecture
  decision, not a quick fix; the owner will provide SVGs to work from rather than
  have this guessed at.

Also noted, not acted on: measured color/size deltas against our tokens (label
color `#75838B` there vs. our `--text-muted: #6B7A85`; ability label at 10px there
vs. our tuned 7.5px). Left alone — `--text-muted` is used everywhere, and the
7.5px ability label was deliberately shrunk so "CONSTITUTION" fits inside the
frame's shoulders (ui-design-tokens.md); bumping it to 10px risks overflow without
retuning the frame. Not "easy" in the sense asked for here.

## 2026-08-13 — Revert saving throws to one column — the individual frame stays

Reverts the previous entry's plain-chip fix, at the owner's request: replacing the
tuned circular badge (`dnd_frame_modifier.png`) with an unframed row traded away
the individual badge's own frame design to make two columns fit, and that frame
matters more than the column count. Started scoping a proper fix instead — a full
horizontal 9-slice for the panel frame (left/right corner caps that never scale,
mirroring the vertical cap/middle technique) so the panel could widen without
stretching its ornaments *and* the badge could keep its tuned 132×34 size. Stopped
partway through the asset measurement once the owner flagged it as more than
they wanted to spend on this: no need to change the badge's width if that costs
this much engineering.

Net result: `SavingThrowRow` and `SavingThrowsPanel` are back to exactly what
phase 4 built — one column of six `dnd_frame_modifier.png` badges, panel at the
standard 278px. The `.saving-chip` CSS and the `.panel__rows--grid-2` grid rule
from the reverted attempt are deleted, not kept unused — there's no pending
reason to reach for them again, unlike the circular badge itself, which was never
touched this round.

Left as a known gap, not attempted again this round: fitting saving throws in two
columns while preserving both the panel's frame and the badge's frame intact
needs the horizontal 9-slice described above. Worth doing later if the two-column
layout is wanted again, but it's real engineering, not a CSS tweak — scope it as
its own slice rather than folding it into an unrelated change.

## 2026-08-13 — Revert the widened panel; saving throws become plain chips

Follow-up to the same-day two-column change, prompted by the owner: the widened
panel (278px → 380px) made the Saving Throws frame's corner ornaments visibly
bigger, not just repositioned. Root cause: the earlier cap/middle slicing (see the
"Slice panel frames into cap + middle" entry) only protects the **vertical**
axis — the caps have a fixed height so content taller or shorter than before
doesn't stretch them. Nothing protected the **horizontal** axis. `mask-size: 100%
100%` maps a cap layer to 100% of the container's actual width, so widening
`.panel--wide` stretched the entire cap image — ornamental corners included —
proportionally wider. A real gap in the earlier fix, not a new bug.

The owner then pulled the actual measurements from the reference site's CSS for
this exact component: the Saving Throws container is 281px (essentially our
existing 278px — no widening needed at all) and each two-column ability slot is
107px wide with no border-image or mask properties in its CSS — a plain element,
not the elaborate circular badge. That settled which of two fixes to take: keep
`dnd_frame_modifier.png`'s tuned 132×34 badge exactly as built, and give the
two-column grid a plain, unframed row instead (matching skill-row's precedent),
rather than engineering a full horizontal 9-slice to let the panel widen safely.

What changed:

- `.panel--wide` and the `wide` prop on `Panel` are gone — Saving Throws, Senses
  and Proficiencies & Training are back to the standard 278px, so the frame is
  never stretched off its tuned proportions in either axis.
- New `SavingThrowRow` renders a `.saving-chip` — a plain bordered row (prof dot,
  abbreviation, roll button), same architecture as `.skill-row`/`.condition-row`.
  `.panel__rows--grid-2` now splits the panel's existing content width with `1fr`
  columns instead of hardcoding a per-item pixel width, so it isn't tied to a
  guessed number that happens to fit.
- The old circular `.saving`/`.saving__*` CSS and its `:active` fix from the
  previous entry are **kept, not deleted** — reserved in case a future
  single-column context can fit the tuned 132px badge again. Currently unused;
  `dnd-5e-sheet-build.md`'s component inventory notes this explicitly so it isn't
  mistaken for a live component.

Verified live: the panel frame's ornaments are back to matching Senses and
Proficiencies (all three now visually consistent again), the 2×3 grid still
groups physical (STR/DEX/CON) then mental (INT/WIS/CHA) abilities, and rolling
still works from the new chips.

## 2026-08-13 — Fix saving-throw roll press glitch; two-column saving throws

Owner reported clicking a saving throw's modifier bumped the number (and its hover
highlight) downward. Root cause: the shared `.roll:active { transform:
translateY(1px); }` press effect (frames.css) replaces `transform` on click rather
than composing with it — fine for roll targets with no base transform, but
`.saving__mod` centers itself with its own `transform: translate(-50%, -50%)`,
which the press rule was clobbering entirely, not nudging. Fixed with a
`.saving__mod:active` override that re-applies the centering alongside the press:
`translate(-50%, -50%) translateY(1px)`. No other current roll target uses a
transform for its own positioning, so nothing else was affected.

Also reworked Saving Throws from one column of six to two columns of three
(STR/DEX/CON, then INT/WIS/CHA — `grid-auto-flow: column` fills top-to-bottom
within a column first, so `SavingThrowsPanel`'s ability order didn't need to
change). This didn't fit inside the existing 278px panel: the saving-throw badge's
132px width is tuned in the kit, not ours to shrink, and two of them plus a gap
need more room than 278px minus the ornate frame's padding leaves. Confirmed with
the owner: when one frame's width changes, every panel sharing its column changes
with it, so the column reads as one edge — not just Saving Throws. `Panel` gained
a `wide` prop; Saving Throws, Senses and Proficiencies & Training (the whole left
column) now render at 380px instead of 278px, with `--panel-cap-h` recomputed at
that width using the same aspect-ratio math as the base panel (see `.panel--wide`
in frames.css), so the frame caps stay crisp rather than reverting to the squash
bug fixed earlier this phase. 380px is derived — two 132px badges plus the
standard 16px gutter, back-solved through the ornate padding fraction — not a
measured reference value.

Also documented, at the owner's request: `rulesets/dnd-5e-sheet-ui.md`'s "Deferred
to later versions" now spells out manual/custom rolling from the dice tray in
full, since it was previously only implied — a picker (not a free-text
expression) where the player chooses die types (d4/d6/d8/d10/d12/d20/d100) and a
count per die (max 10), rolls them together, and the log shows the summed total
prominently with each die's individual result smaller beneath it. `roadmap.md`'s
phase 5 entry now points to it explicitly so it isn't mistaken for scope that
slipped.

## 2026-08-13 — Phase 5: dice engine

**Goal met:** clicking a roll target rolls on the server and records it. Ability
checks, saving throws, skill checks and initiative are all wired end to end —
verified live for all four.

What changed:

- **Migration `V3__create_rolls_table.sql`** — the `rolls` table exactly as specified
  in `database-schema.md`: append-only, `results int[]`, index on
  `(character_id, rolled_at DESC)`.
- **New `ruleset` seam: `MechanicResolver`** — named in architecture.md's seams table
  since phase 3 but not implemented until now. Turns a `RollKind` (`ABILITY_CHECK`,
  `SAVING_THROW`, `SKILL_CHECK`, `INITIATIVE`) plus a key into a `ResolvedRoll`
  (dice count, dice sides, modifier, human-readable context). Deliberately not
  hardcoded in the generic `dice` package: which die a check rolls is system-specific
  behavior, same as everything else the Strategy pattern covers here.
  `MechanicResolverRegistry` mirrors `SheetCalculatorRegistry`.
- **`Dnd5eMechanicResolver`** — every phase-5 target is a d20 plus a modifier the
  sheet calculator already produces (`vitals.abilityModifiers()`,
  `.savingThrows()`, `.skills()`, `.initiative()`) — no recalculation, just lookup.
  Context labels follow the format `features/character-sheet.md` already
  established ("Guiding Bolt: damage") — "Strength: check", "Wisdom: saving throw",
  "Sleight Of Hand: check" (camelCase skill keys humanized), "Initiative: roll".
  An unknown key or a missing one where the kind requires it throws
  `UnresolvableRollException`, mapped to 400.
- **New `dice` package** — `Roll` (append-only entity, first `int[]` column in this
  codebase), `RollRepository`, `DiceRoller` (the only place a random number is
  generated), `RollService` (ownership via `CharacterService`, vitals via
  `SheetCalculatorRegistry`, resolution via `MechanicResolverRegistry`, never trusts
  a client-supplied modifier), `RollController`
  (`POST`/`GET /api/characters/{id}/rolls`).
- **Frontend**: `dice/api.ts` (`postRoll`, `getRollHistory`), `DiceTray` (bottom
  left, fixed, persistent — no frame asset, no 3D rendering, shows the latest result
  or the error if the roll failed to reach the server) and `GameLog` (a standalone
  reverse-chronological list — **not** the sidebar's Log mold, which is phase 8).
  Every roll target built since phase 4 (ability modifier, saving throw, skill,
  initiative) now calls a `RollHandler` threaded down from `CharacterSheetScreen`,
  which owns the roll history and error state. No optimistic local result at any
  point — a failed request surfaces `rollError` and nothing else changes, per
  ground-rules.md's Dice section.

Layout fix along the way: the dice tray's fixed position was covering the last few
pixels of the game log on a short page. `SheetShell`'s `<main>` now carries
`paddingBottom: 120px` to leave room for it.

Testing: `Dnd5eMechanicResolverTest` (unit, all four kinds plus the two rejection
paths), `RollControllerTest` (MockMvc, including the `@NotNull kind` validation
failure), `RollRepositoryTest` (Testcontainers — specifically to prove the new
`int[]` column round-trips through real Postgres, not mocked). All green.

Left out, matching roadmap.md's stated scope: 3D dice, advantage/disadvantage,
rolling from the not-yet-built tabs. Also left out, not in scope but worth naming:
manual/custom-expression rolling from the tray (`features/character-sheet.md`
describes it — "Manual rolls from the dice tray follow the same path, with no
source attached" — but phase 5's own done-criterion doesn't require it).

## 2026-08-13 — Phase 4, slice 6: hit points and heroic inspiration — phase done

Closes the last item left open in the top row and, with it, phase 4 itself:
every element `dnd-5e-sheet-ui.md` scoped for the vitals zone now renders.

What changed:

- Backend: `Dnd5eSheet` gains `currentHitPoints`, `temporaryHitPoints` and
  `heroicInspiration` — real session state, same treatment as
  `activeConditions`: read-only character data now, mutable (damage, healing,
  toggling inspiration) only once phase 6 adds the endpoints for it.
  `VitalsZone`/`CharacterSheetResponse` pass all three straight through — no
  formula, so no `CalculatedValue`, same as `abilityScores` and `speed`.
  `DevCharacterSeeder`'s fighter now has 30/44 current hit points, 5 temporary,
  and inspiration on, so all three display branches (damaged, temp-HP present,
  inspired) show on the seeded character rather than only the default state.
- Frontend: new `HeroicInspiration` (a filled/hollow dot, same visual language
  as the skill and condition dots) and `HitPoints` (current/max fraction, temp
  shown only when non-zero) — both plain bordered boxes, since neither has a
  frame asset (`dnd-5e-sheet-build.md`'s inventory had both "Not built").
  Composed into `Dnd5eVitalsTopRow` after the speed badge, matching the
  reference screenshots' order.

Neither renders a button. Hit points would need heal/damage inputs to be
genuinely interactive and inspiration a toggle — both are phase-6 mutations
with no endpoint to call yet, so a button now would be a control that does
nothing, same reasoning already applied to conditions.

Hit an unrelated snag while verifying live: the running backend process had
started before these Java changes and doesn't hot-reload (`spring-boot-devtools`
isn't a dependency here), so it kept serving the old `Dnd5eSheet` shape against
already-seeded rows, throwing the same "null into a primitive" deserialization
error covered earlier in this phase. Fixed by restarting the backend process and
resetting the seeded player/character so the seeder re-ran against the current
code — not a code bug, noted here only because it cost a round trip.

Verified live in the browser: inspiration shows filled, hit points show
"30 / 44" with "+5 temp" beneath, matching the seeded values.

Phase 4 (`roadmap.md`) is now **Done** — every build-order step (1-5, plus this
one closing step 2) is built, the real grid replaced the stacked placeholder,
and every remaining estimate (grid gutter, column widths) is named rather than
invented. What's left for the sheet — tabs, sidebar, rolling, any mutation — is
phase 5 onward, starting with phase 5's dice engine.

## 2026-08-13 — Phase 4, slice 5: the real three-column grid

Closes out `dnd-5e-sheet-build.md`'s build order steps 1-5 — everything stacked
vertically until now sits in the actual grid: left column, skills, combat column,
side by side.

Checked `design-reference/screenshots/dnd-character-sheet` (img_6.png, img_9.png,
two full-sheet captures) before building, since the grid's proportions were listed
as unmeasured. That surfaced a layout correction: Defenses and Conditions are two
panels **side by side**, not one stacked under the other as the previous slice
built them — both share the width of the tabbed section beneath them (not built
until phase 8). Confirmed with the owner before changing it.

What changed:

- New `Dnd5eVitalsColumns` composes `Dnd5eLeftColumn`, `SkillsPanel` and
  `Dnd5eCombatColumn` in a flex row (`align-items: flex-start`, 16px gap),
  replacing the vertical stack in `CharacterSheetScreen`.
- `Dnd5eCombatColumn`: Defenses and Conditions now render in their own inner flex
  row (also `align-items: flex-start` — without it, flex's default `stretch`
  forces the shorter panel to match the taller one's height, reintroducing the
  same "forced to one height" problem the cap/middle slice had just fixed for
  differently-sized panels).
- `Dnd5eLeftColumn` no longer carries its own `marginTop` — spacing between the
  top row and the columns is the grid container's job now, not each column's.

Estimated, not measured (named in `Dnd5eVitalsColumns.tsx`'s doc comment and
`ui-design-tokens.md`): the 16px gutter (reusing the existing vertical panel-gap
value) and giving every column the standard 278px panel width, including each
half of the Defenses/Conditions pair. Confirmed with the owner this was
acceptable before proceeding, rather than blocking on a live-inspector
measurement.

Left out, and the one thing still keeping phase 4 from being fully done: the hit
points block and heroic inspiration, both still without a frame asset (flagged
back in the top-row slice). Everything else phase 4 scoped is built.

## 2026-08-13 — Slice panel frames into cap + middle instead of a min-height floor

Follow-up to the same-day min-height fix, prompted by the owner: the floor stopped
the blur, but it also forced every short panel (Proficiencies & Training, Senses,
Defenses) to the same tall height as Skills or Saving Throws, leaving dead white
space below the title. "The frame should adapt to the content" — with the corner
ornament protected from distortion, not the whole panel locked to one height.

What changed: replaced the single stretched mask per panel with three stacked
pieces — a fixed-height top cap, a stretchy ornament-free middle, a bottom cap
(the same top-cap asset, mirrored with `transform: scaleY(-1)`, not a second
asset). The cap is cropped from each source PNG at the exact pixel row where the
corner ornament resolves into plain straight lines (measured by cropping
successive top/bottom bands and inspecting them — 65px of 681 for the plain
frame, 188px of 406 for the ornate frame, the latter needing more room because it
carries a second flourish partway down each side, not just a corner curl). The
middle is a thin plain band cropped from well inside the safe zone. Stretching the
middle is invisible because it contains nothing but straight lines — a stretched
straight line is still a straight line, unlike stretching the ornament itself.

New assets (cropped from the existing sources, not re-rastered — no resolution
lost): `dnd_frame_proficiencies_cap.png`, `dnd_frame_proficiencies_middle.png`,
`dnd_frame_features_cap.png`, `dnd_frame_features_middle.png`, all in
`apps/web/public/frames/`. `Panel.tsx` now renders `.panel__frame` (an
absolutely-positioned, three-piece flex column behind the content) instead of a
single pair of mask layers on the outer element; `.panel`/`.panel--plain` in
`frames.css` were rewritten accordingly. The `min-height` floor from the earlier
fix survives in a much smaller form — `--panel-cap-h * 2`, the smallest height at
which both caps can render without colliding — as a safety net, not the primary
mechanism.

Verified live in the browser: Senses and Proficiencies & Training now hug their
actual content height with crisp corners; Defenses does too; Skills, Conditions,
Saving Throws (already tall enough to look right before) are unaffected.

Left out: `apps/web/reference/frame-kit.html` still shows the old single-mask
panel pattern — it wasn't updated to match, since the kit only ever demonstrated
panels with plenty of content and never surfaced this problem. Worth porting back
if the kit is touched again, but not done here to keep this change scoped to the
live sheet.

## 2026-08-13 — Fix blurred plain-panel frames on short content

Owner caught it by eye: Proficiencies & Training and Defenses had a visibly
lower-quality frame than Skills or Conditions, all four using the same
`dnd_frame_proficiencies.png` mask.

Root cause: `.panel`/`.panel--plain` apply their frame with `mask-size: 100% 100%`,
which stretches non-uniformly to whatever height the content produces — there was no
floor. `dnd_frame_proficiencies.png` is 561×681px; at the fixed 278px panel width its
native height is ~337px. Skills (18 rows) and Conditions (14 rows) comfortably clear
that, so their frame reads crisply. Proficiencies and Defenses, with only a handful of
lines, ended up far shorter — squishing the frame's stroke well past its drawn
proportions, which is what read as blur. Same mechanism affects the ornate frame
(`dnd_frame_features.png`, 565×406px, natural height ~200px at 278px), though nothing
has hit it yet.

Fix: `min-height: 200px` on `.panel`, `min-height: 337px` on `.panel--plain`, each
derived from its own frame's aspect ratio at the fixed 278px width — the mask is now
only ever stretched taller than natural, never squished shorter. Short panels get
blank space below their title instead of a degraded frame; verified live in the
browser that Proficiencies and Defenses now match Skills/Conditions/Saving Throws.

## 2026-08-13 — Phase 4, slice 4: combat group

Fourth vertical slice of the vitals zone, following `dnd-5e-sheet-build.md`'s build
order to step 5.

What changed:

- Backend: `Dnd5eSheet` gains `damageResistances`, `damageImmunities`,
  `damageVulnerabilities`, `conditionImmunities` (display strings, same treatment as
  armor/weapon/tool proficiencies) and `activeConditions` (a set naming which of the
  standard 5e conditions are currently on the character). `VitalsZone` and
  `CharacterSheetResponse` pass all five straight through — no calculation, since
  these are stored data, not derived values. Exhaustion is deliberately left out: it
  tracks a level (0-6), not a boolean, and no level-tracking shape exists yet.
  `DevCharacterSeeder`'s fighter gains one resistance (Poison) and one active
  condition (Prone), with immunities/vulnerabilities/condition immunities left empty
  to exercise the "skip an empty category" path.
- Frontend: `ArmorClass` and `Initiative` ported from `frame-kit.html`'s tuned
  `dnd_frame_armor.png` (74×84, reveal target — opens the explainer, does not roll,
  per `dnd-5e-sheet-ui.md`'s trigger table) and `dnd_frame_initiative.png` (78×52,
  roll target). Both were calculated since phase 3 but not yet placed on screen.
  New `DefensesPanel` (plain panel, dl-list, same pattern as `ProficienciesPanel`)
  and `ConditionsPanel` (14 conditions, `ConditionRow` per entry). New
  `Dnd5eCombatColumn` stacks initiative + armor class side by side, then the two
  panels, below the skills panel — still not the real three-column grid.

One deliberate deviation, following the precedent already set for the skill row in
the previous slice: **conditions have no frame asset either**
(`dnd-5e-sheet-build.md` listed "Defenses / conditions" as "Not built"), so
`ConditionRow` uses the same no-mask flex-row treatment as `SkillRow`. Unlike the
skill row, a condition row is **not** wrapped in a button at all — the trigger table
in `dnd-5e-sheet-ui.md` does not list conditions as opening the explainer, and
toggling them is a phase-6 mutation (`features/character-sheet.md`: "Condition:
Toggled. In the first version it changes no other value"). Building an inert button
now, ahead of any handler, would misrepresent what the element does today.

Left out: the tab bar, the sidebar, and the real three-column grid (build order
steps 6-8) — this closes out the vitals zone's build order (steps 1-5). Rolling
(initiative's roll target is inert, same as every other roll button so far) and the
explainer sidebar (armor class's reveal target is inert too) are phase 5 and later.

Verified: backend tests green including Testcontainers (`Dnd5eSheetCalculatorTest`,
`CharacterControllerTest`, plus the existing Testcontainers-backed suites — Docker
was available this time); frontend `tsc` and `vite build` clean. **Not yet verified
against the reference screenshots** — the combat column's placement (stacked below
skills, not beside it) is provisional pending the measured three-column grid, same
caveat as slice 2.

## 2026-08-13 — Phase 4, slice 3: skills

Third vertical slice of the vitals zone, following `dnd-5e-sheet-build.md`'s build
order to step 4. `roadmap.md`'s Phase 4 row is now "In progress" (was left as "Not
started" through the previous two slices — corrected here).

What changed:

- Backend: `Dnd5eSheet` gains `skillProficiencies` (a set of the 18 fixed 5e skill
  keys). `Dnd5eSheetCalculator` gains a fixed `SKILL_ABILITIES` map (skill → governing
  ability — game rule, not character data) and computes each skill's modifier
  (ability modifier, plus proficiency bonus when proficient) with its contribution
  trace. `VitalsZone` and `CharacterSheetResponse` expose `skills`,
  `skillProficiencies` and `skillGoverningAbilities` — the last so the frontend never
  hardcodes which ability governs which skill.
- Fixed the simplification named in the previous slice: passive senses
  (`passivePerception`, `passiveInvestigation`, `passiveInsight`) now add the
  proficiency bonus when the character is proficient in the matching skill
  (Perception, Investigation, Insight respectively), instead of always being
  `10 + ability modifier`.
- `DevCharacterSeeder`'s seeded fighter gains four skill proficiencies (Athletics,
  Intimidation, Perception, Survival) so the skills panel and the corrected passive
  senses both have proficient and non-proficient rows to show.
- Frontend: new `SkillRow` and `SkillsPanel` (plain panel, 18 rows: proficiency dot,
  governing ability abbreviation, name as reveal target, modifier as roll target).
  `CharacterSheet` (`sheet/api.ts`) gains the three new fields. `SensesPanel` loses
  its now-inaccurate simplification comment.

One deliberate deviation from the kit's frame-box pattern, confirmed with the owner
over two rounds of questions: **the skill row has no frame asset** —
`dnd-5e-sheet-build.md`'s component inventory listed it "Not built," and
`ui-design-tokens.md` lists its metrics as an explicit blocker ("ask before inventing
one"). Stretching the saving-throw row's `dnd_frame_modifier.png` was rejected —
that frame was tuned for an abbreviation, not a full skill name like "Sleight of
Hand" — and so was reusing the stat badge's square `dnd_frame_box.png`. The row
instead follows the frame-box architecture's conventions (semantic buttons, hover
only on the roll target, its own custom-property-free flex layout) without a mask,
sitting inside the already-framed plain panel. Marked in
`dnd-5e-sheet-build.md`'s component inventory as built without a frame, not silently
approximated.

Left out: the real three-column grid (skills stacks below the left column, same
reasoning as slice 2 — column widths aren't measured yet) and the center/right
column's remaining pieces (initiative, armour class, defenses, conditions — build
order step 5).

Verified: backend unit and controller tests pass (`Dnd5eSheetCalculatorTest`,
`CharacterControllerTest`); frontend `tsc` and `vite build` are clean. **Not verified
in a browser** — Docker was unavailable in this environment, so the full stack
(Postgres, Keycloak) could not be started to log in and see the panel rendered
live. Do that check before calling this slice visually done.

## 2026-08-13 — Fix Senses panel frame

Owner caught it by eye against the reference: the Senses panel uses the same ornate
frame as Saving Throws, not the plain one used for Proficiencies & Training.
`dnd-5e-sheet-build.md`'s component inventory had it recorded as "plain panel, not
built" — updated the table (`Built — same frame as Saving Throws, not the plain one`)
and `SensesPanel`'s `variant` prop from `plain` to `ornate`. Verified live: the
panel now renders with the ornate corners, values unchanged (11/10/11).

## 2026-08-13 — Phase 4, slice 2: left column (saving throws, senses, proficiencies)

Second vertical slice of the vitals zone, following `dnd-5e-sheet-build.md`'s build
order to step 3. `roadmap.md`'s Phase 4 row still stays "Not started" — tracked here.

What changed:

- Backend: `Dnd5eSheet` gains `savingThrowProficiencies` (a set of ability names) and
  four proficiency/training string lists (`armorProficiencies`, `weaponProficiencies`,
  `toolProficiencies`, `languages`) — display data, not modeled any richer, since
  nothing checks them against actions or items yet. `VitalsZone` (and
  `CharacterSheetResponse`) gain `savingThrows` (modifier = ability modifier +
  proficiency bonus, only when proficient) and `senses` (`passivePerception`,
  `passiveInvestigation`, `passiveInsight`), plus `savingThrowProficiencies` sitting
  raw alongside the calculated modifiers — same pattern as `abilityScores` next to
  `abilityModifiers` — so the frontend fills or hollows the proficiency dot from an
  explicit boolean, never by parsing a contribution label.
- `DevCharacterSeeder`'s stat block extended for a level-5 fighter: proficient in
  Strength and Constitution saves; armor (light/medium/heavy/shields), weapons
  (simple/martial) and Common recorded; no tool proficiencies, to exercise the
  "skip an empty category" path.
- Frontend: `systems/dnd5e/frames.css` gains `.panel`/`.panel--plain` (merged with
  the kit's tuned "AFTER COPY CSS" overrides, same approach as slice 1) and
  `.saving`/`.saving__*`, all ported unchanged, frame layers kept on the child
  combinator per the kit's explicit warning about nested masks. New `Panel` (shared
  by every panel, ornate or plain), `SavingThrowRow`, and three composed panels
  (`SavingThrowsPanel`, `SensesPanel`, `ProficienciesPanel` — the last skips any
  category with no entries) stacked by `Dnd5eLeftColumn`, rendered below the top row
  inside the same shell slot.

One simplification, confirmed with the owner: **passive senses are `10 + ability
modifier` only, no skill-proficiency bonus.** The real rule adds the proficiency
bonus when proficient in the matching skill, but skills are next slice — modeling
three skill proficiencies early just for senses would mean redoing them when the
other fifteen arrive. Named here and in the panel's own code comment, not silently
wrong.

Left out, same reasoning as slice 1: the center and right columns (skills, initiative,
armour class, defenses, conditions — initiative and armour class are already
calculated since phase 3, just not placed on screen yet), and the real three-column
grid — this slice's left column stacks under the top row, not beside it, since the
other columns don't exist to make a real grid meaningful yet.

Verified: `./gradlew :apps:api:check` and `npm run build` green. Live browser check —
Strength and Constitution showed filled proficiency dots (+6, +5) and the other four
hollow (+2, +0, +1, −1), all matching hand calculation; passive senses showed 11/10/11;
the proficiencies panel listed armor/weapons/languages and correctly omitted the empty
tools row.

## 2026-08-13 — Phase 4, slice 1: static shell + top row (abilities, proficiency, speed)

First frontend rendering of real sheet data. Not the whole of Phase 4 —
`rulesets/dnd-5e-sheet-build.md` prescribes building the vitals zone in vertical
slices (static shell → top row → left column → skills → combat group), and this is
slice 1, confirmed with the owner. `roadmap.md`'s Phase 4 row stays "Not started";
progress is tracked here instead.

What changed:

- Backend: `Dnd5eSheet` gains `speed`; `VitalsZone` (and `CharacterSheetResponse`)
  gain `abilityScores` (the raw score — Phase 3 only exposed the modifier),
  `speed` and `level`, none of them derived values (no formula, no contributions),
  so they're plain fields, not `CalculatedValue`s. `DevCharacterSeeder`'s stat block
  updated to include `speed`.
- Frontend: new `systems/dnd5e/` module (mirrors the backend's `ruleset/dnd5e`,
  matching `ui-design-system.md`'s frontend-registry section) — `frames.css` ported
  from `apps/web/reference/frame-kit.html` unchanged, `AbilityBox` and `StatBadge`
  (one frame serving proficiency bonus and speed, per the kit), composed by
  `Dnd5eVitalsTopRow`. A minimal generic `sheet/SheetShell` (portrait placeholder,
  name, "Level N", a slot for the system's vitals content) and `sheet/api.ts`.
  Character names in the list are now buttons; `AppShell` holds which character is
  selected as local state and swaps the list for the sheet screen — no router added
  for one screen transition.

Two things deliberately left out, per the kit and the design-tokens doc's own
instructions to stop and ask rather than invent:

- **Hit points and heroic inspiration** have no frame asset yet
  (`dnd-5e-sheet-build.md`'s component inventory marks both "Not built"). Not even a
  placeholder box — a placeholder would itself be an invented approximation.
- **The outer sheet grid** (column widths, gutters, header spacing) is listed as
  "still unmeasured" in `ui-design-tokens.md`, unlike the ability box and stat badge
  themselves, which are fully resolved there. The shell's padding and gaps in
  `SheetShell` are a best-effort estimate from the local reference screenshots,
  commented in the code as estimated, meant to be corrected once measured — not
  silently invented, per this phase's own "done when."

Also left out: the other three columns (saving throws, senses, proficiencies,
skills, defenses, conditions), the header's action buttons (none has a function
yet), and the full species/class identity line (`Dnd5eSheet` has no species or
class name, only `hitDieSize` and `level`).

Gotcha found and documented in `tech-stack.md`: Jackson 3 rejects a JSON `null` (or
missing property) for a primitive field with `MismatchedInputException` by default,
unlike looser assumptions carried over from Jackson 2 habits. Hit this live — the
seed character created earlier in the day predated the `speed` field and its stored
JSON didn't have it, so loading its sheet 500'd until the row was recreated. No
migration/versioning strategy exists yet for evolving a system's sheet shape against
already-stored payloads; `sheet_schema_version` exists for exactly this and is
presently unused for it. Not a problem yet with only dev seed data, but worth
remembering before phase 9/10 lets a real client write a sheet.

Verified: `./gradlew :apps:api:check` and `npm run build` green. Live browser check —
opened Aria Emberfall's sheet, all six ability scores/modifiers and both badges
matched the stored stat block exactly, compared side by side against a
D&D Beyond reference screenshot in `design-reference/screenshots/dnd-character-sheet/`.

## 2026-08-13 — Phase 3: Ruleset seam

Closes Phase 3. `GET /api/characters/{id}/sheet` returns five calculated vitals
values for a `dnd-5e` character — ability modifiers, proficiency bonus, armour class,
initiative, hit points — each with a labelled derivation trace, never a bare number.

What changed:

- New `ruleset` package, matching `architecture.md`'s layout: `GameSystem` and
  `SheetCalculator` interfaces, `VitalsZone`/`CalculatedValue`/`Contribution` as the
  generic (system-agnostic) result shapes, `UnsupportedGameSystemException`, and
  `registry/GameSystemRegistry` + `registry/SheetCalculatorRegistry` built from
  injected lists. `SheetCalculator` is one bundling method
  (`calculateVitals(sheetJson)`), not five — the sheet only needs parsing once.
- `ruleset/dnd5e`: `Dnd5eGameSystem`, `Dnd5eSheet` (six ability scores, level,
  hit-die size; Bean Validated; `schemaVersion` 1 — the first real payload shape,
  replacing Phase 2's `{}`/`0` placeholder), and `Dnd5eSheetCalculator`. Armour class
  is deliberately the unarmored formula (`10 + DEX modifier`) — inventory-driven
  armour is Phase 8/9's job, not a shortcut taken here. Hit points use the fixed/
  average method (deterministic, matches "derived values are recalculated, never
  stored").
- Closed the gap Phase 2 explicitly left open: `CharacterService.create` now checks
  the submitted `systemId` against `GameSystemRegistry` and 400s on an unknown one,
  via a second mapping added to the existing `ApiExceptionHandler`.
- `character/CharacterSheetService`: the orchestrator between ownership
  (`CharacterService.getMine`, reused) and the ruleset seam
  (`SheetCalculatorRegistry`) — never a concrete `dnd5e` class, per `architecture.md`.
- Dev-only seeding: `identity/PlayerService` now publishes a `PlayerCreatedEvent`
  (only when it actually creates a row, not on every lookup); a new
  `character/DevCharacterSeeder`, gated to the `dev` Spring profile
  (`apps/api/build.gradle`'s `bootRun` task sets it, so a packaged-jar deployment is
  unaffected), listens for it and gives the new player one fully-statted character —
  "Aria Emberfall", level 5, so every derived value is non-trivial to hand-verify —
  without needing Phase 10's character creation to exist yet. This was the owner's own
  suggestion, right after confirming that sheet fidelity outranks the creation screen
  for now.

Verified: `./gradlew :apps:api:check` green, including a plain-unit
`Dnd5eSheetCalculatorTest` checked by hand against the Aria Emberfall stat block.
Manually confirmed live — cleared the local `test` player so a fresh login would
re-trigger the seed event, logged in, watched "Aria Emberfall" appear in the list
automatically, then called the sheet endpoint from the browser's own session and
confirmed all five values and their contributions matched the hand calculation
exactly (STR +3, DEX +2, CON +2, INT +0, WIS +1, CHA -1, proficiency +3, AC 12,
initiative +2, HP 44).

Left out, as planned: spells, actions, inventory, features, and any frontend — Phase 4
is what renders this.

## 2026-08-13 — Phase 2: Character vault

Closes Phase 2. Characters can be created, listed, read and soft-deleted, each scoped
to the owning player.

What changed:

- Migration `V2__create_characters_table.sql`: the `characters` table, matching
  `database-schema.md` exactly, including the `player_id` index and the GIN index on
  `sheet`. `sheet` is stored as `{}` and `sheet_schema_version` as `0` at creation — an
  explicit "no ruleset yet" placeholder; Phase 3 is what gives them real meaning.
- `character/Character`, `CharacterRepository`, `CharacterService`,
  `CharacterController`: create/list/read/soft-delete behind `/api/characters`, all
  scoped through `identity/PlayerService.currentPlayer(jwt)` — the second caller of
  that service, as planned when it was built in Phase 1. Ownership is enforced by
  querying `(id, player_id, deleted_at IS NULL)` together, so another player's
  character or a deleted one is simply not found, never a separate authorization check.
- `shared/ApiExceptionHandler`, a `@RestControllerAdvice` returning RFC 7807
  `ProblemDetail`s — the first one in the project, mapping the new
  `CharacterNotFoundException` to 404. Phase 3's `UnsupportedGameSystemException` is
  meant to join this same class rather than getting its own.
- Frontend: `apps/web/src/characters/` — a list grouped by system, a creation form,
  and delete. The list's summary line (system + creation date) is backend-owned data
  (`systemId`, `createdAt` on the response) arranged into a line by the frontend, not
  a pre-formatted string from the API — see the reasoning captured in this phase's
  plan. The system picker is a real `<select>` driven by a one-entry constant array
  rather than a hardcoded value, so a second system is one array entry away, not a
  form rewrite.
- Backend tests: a Testcontainers-backed `CharacterServiceTest` covering ownership
  isolation between two players and soft-delete exclusion from listing; a
  `@WebMvcTest` `CharacterControllerTest` covering endpoint wiring, validation, and
  the new 404 shape.

Two more Boot 4.1 surprises found and documented in `tech-stack.md`, same shape as the
Flyway one from Phase 1: (1) `webOrigins` on a Keycloak client needs to be an exact
origin or `+`, not a redirect-URI-style wildcard — the wildcard form fooled the CORS
*preflight* into passing while the actual token response never carried the header,
which is exactly the bug the owner hit live after Phase 1 looked done. Fixed on the
running realm via the Admin API and in `infra/keycloak/realm-export.json`. (2) The API
itself had never configured CORS at all — fixed in `SecurityConfig` with a
`CorsConfigurationSource` reading a new `cors.allowed-origins` property, verified by
checking that both the preflight *and* the real response carry the header this time,
not just the preflight, per lesson (1). (3) Boot 4.1 ships Jackson 3
(`tools.jackson.*`), not Jackson 2 (`com.fasterxml.jackson.*`) — `jackson-annotations`
is the one piece still on the old package, which makes a half-migrated import list
look plausible right up until "package does not exist."

Verified: `./gradlew :apps:api:check` and `npm run build` green; a live browser
click-through (create → appears grouped under "D&D 5e" with the summary line → survives
a reload → delete → gone from the list) with the underlying row confirmed still in
Postgres with `deleted_at` set, not hard-deleted.

Left out, as planned: the sheet itself, portraits, backstory editing, any game rules,
and validating `systemId` against a real registry (Phase 3).

## 2026-08-13 — Add CORS to the API

Fixing the Keycloak CORS issue got the owner past login, but the very next request —
the shell's `GET /api/me` — hit the same class of error against the API itself:
`SecurityConfig` never configured CORS at all, so Spring Security's preflight had no
`Access-Control-Allow-Origin` to give back and the browser blocked the call. Added a
`CorsConfigurationSource` to `SecurityConfig`, wired into the filter chain via
`.cors(...)`, allowing origins from the new `cors.allowed-origins` property
(`application.yml`, currently `http://localhost:5173`) — matching the project rule
that no host or port is hardcoded in code. Verified with `curl` that both the
`OPTIONS` preflight **and** the actual `GET` (even a 401) carry the header, learning
from the Keycloak mistake above instead of repeating it. `./gradlew :apps:api:check`
still green.

## 2026-08-13 — Fix Keycloak CORS on the token endpoint

The "worth a manual click-through" caveat from the previous entry was right to be
suspicious: the owner hit a real, reproducible CORS error in an actual browser during
that manual check, not just a quirk of the automated one. Root cause was
`omni-sheet-vault-web`'s `webOrigins`, set to `http://localhost:5173/*` — Keycloak
accepted that for the CORS preflight but never attached
`Access-Control-Allow-Origin` to the actual token response, so the browser blocked the
code-for-token exchange right after a successful login. Fixed live via the Keycloak
Admin REST API (`webOrigins` → `+`, mirroring the redirect URIs) and in
`infra/keycloak/realm-export.json` so a fresh `docker compose up` gets it too.
Verified with `curl -X POST` against the token endpoint: the response itself now
carries the header, not just the `OPTIONS` preflight. Details in `tech-stack.md`.

## 2026-08-12 — Phase 1: Identity

Closes Phase 1. An unauthenticated visit now redirects to Keycloak, a successful login
returns to a real app, and `GET /api/me` reflects a player persisted in the database
instead of just echoing the token.

What changed:

- Migration `V1__create_players_table.sql`: the `players` table, matching
  `database-schema.md`.
- `identity/Player`, `identity/PlayerRepository`, `identity/PlayerService`: find the
  player by the token subject, or create one on first sight (display name from the
  `preferred_username` claim). Called from `CurrentUserController`, not from a global
  filter — Phase 2's character endpoints will call the same service.
- `CurrentUserResponse` now carries the persisted `id`/`subject`/`displayName` plus
  `email`/`roles` still read live from the token, since Keycloak owns those and they
  are never duplicated into `players`.
- `apps/web` is a real Vite + React + TypeScript app in place of the reference-only
  folder: `oidc-client-ts` Authorization Code flow with PKCE against the
  `omni-sheet-vault-web` client, a `/callback` route, and an `AppShell` (header with
  display name and logout, empty content area) that calls `GET /api/me` on mount.
- Backend tests: a Testcontainers-backed integration test for the find-or-create
  semantics, and `CurrentUserControllerTest` updated to mock `PlayerService`.

Gotcha discovered along the way: Spring Boot 4.1 split Flyway's autoconfiguration out
of `spring-boot-autoconfigure` into its own module, `spring-boot-flyway`. Depending on
`flyway-core` alone now compiles and boots fine but never runs a migration — no error,
no log line, just a later "missing table" from Hibernate's schema validation. Recorded
in `tech-stack.md`.

Verified: `./gradlew :apps:api:check` green against real PostgreSQL; `npm run build`
green; migration applied against the dev database and the `players` row created on a
real login through Keycloak with the redirect, login form and callback all confirmed
in a live browser. The final token-exchange request could not be confirmed through the
browser-automation tooling used for this check — it returned HTTP 503 there while the
same request succeeds from `curl` and Keycloak's own logs show nothing wrong, pointing
at the automation layer rather than the app. Worth a manual click-through to be sure.

Left out, as planned: no frontend test framework (confirmed with the owner), no
styling beyond a plain shell.

## 2026-08-12 — Initial setup

Everything built so far amounts to one thing: the initial setup the project needed
before any roadmap phase could start. Spans two commits, `First commit` (2026-08-09)
and `Update initial documentation` (2026-08-12), plus the CI workflow developed on
`feature/0001-Configure-github-ci-workflow`.

What changed:

- Repository layout: the `.ai/` documentation suite (ground rules, architecture,
  domain model, database schema, tech stack, decisions, rulesets, roadmap, UI design
  system and tokens), `apps/api` and `apps/web`, `infra/`.
- Docker infrastructure: `docker-compose.yml` running PostgreSQL 17, Keycloak 26.4
  (importing `infra/keycloak/realm-export.json` on startup) and MinIO, wired together
  with health checks.
- Backend skeleton: Gradle build rooted at the repository (`:apps:api` subproject),
  Java 25 toolchain, an empty `OmniSheetVaultApplication`.
- CI: a GitHub Actions workflow building and testing the backend and running
  SonarQube analysis on pull requests and pushes to `main`; a frontend job that builds
  `apps/web` once it exists and skips cleanly until then.
- A head start on Phase 1 (Identity): `SecurityConfig` turns the API into an OAuth2
  resource server that validates Keycloak-issued JWTs and maps realm roles to
  authorities, and `GET /api/me` returns the identity carried by the token. This reads
  the token only — there is no `players` table, no Flyway migration and no persistence
  yet, so Phase 1 is not complete.
- The frame-kit reference implementation (`apps/web/reference/frame-kit.html` and the
  frame images) that Phase 4 will port patterns from.

Why: this is scaffolding, not feature work, so it is recorded as one entry rather than
split per commit.

Left out: the `players` table and any persistence for identity, the character vault,
the ruleset seam, and everything else the roadmap assigns to phase 1 onward.
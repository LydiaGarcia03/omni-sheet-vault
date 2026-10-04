# Feature — Experience, level up, sheet themes, home dark mode, death saves (D&D 5e)

Status: **approved 2026-09-27, all nine slices; all done (slice 9, desktop
0.2.0, on 2026-10-04).**

## Where it stopped (2026-09-27)

1. **Slice 9:** raise `appVersion` to 0.2.0, run `packageDesktop`, and test
   an update over 0.1.0 data. Also check the home dark theme in the desktop
   edition. Include a level up on a 0.1.0 spellcaster: it exercises the
   legacy spell id folding (`Dnd5eLegacyChoiceIds`).
2. **Not yet compared with D&D Beyond's own level up flow:** that would mean
   leveling a reference character there.
3. **Suggested commits** (the assistant doesn't run Git), one per changelog
   entry dated 2026-09-27.

## What the owner asked for

1. **XP and level up**
   - An XP system that follows the 5e table of XP per level.
   - Leveling up opens a **level up page**, not the character builder.
     - It offers two choices: add a level to an existing class, or start a
       new class (multiclass).
     - It shows what that class level grants, and asks for its choices.
     - A **Finish leveling up** button returns to the sheet.
   - A **character sidebar**, opened by clicking the portrait or the name and
     level, laid out like D&D Beyond's. It offers:
     - **Manage experience**: add or remove XP, only for XP builds;
     - **Level up**: always for milestone builds, and for XP builds once one
       is available;
     - **Change sheet appearance**.
   - A **thin XP bar** next to the name and level, only for XP builds.
2. **Themes**
   - **The D&D 5e default theme is DDB Red.**
   - Every place that uses today's blue-gray (Cleric Silver) turns red: frames
     and borders, buttons, subsection titles. That covers the sheet, the
     **character builder** and the new level up page.
   - The owner's reference for "where the color goes" is Seyrie (DDB Red)
     compared with Helga (Cleric Silver).
   - Players can choose another theme per character from the sidebar.
3. **Dark theme for the home:** the landing page and the character list get a
   dark theme, switched on from the **user menu** (the button with the
   player's name).
4. **Death saving throws**, as on D&D Beyond: at 0 hit points or less, the hit
   points box turns into Death Saves, with successes and failures.
5. **Desktop edition:** ship everything there at the end.

## Owner decisions (2026-09-27)

- **Default theme for D&D 5e: DDB Red**, for new *and* existing characters,
  and for the builder and the level up page too. The blue-gray becomes one
  theme among the others ("Cleric Silver").
- **After adding XP that crosses a threshold:**
  - the sidebar shows a **Level up** button right under the Manage experience
    section;
  - if the player closes the sidebar without leveling up, a **golden
    up-arrow** appears next to the sheet's portrait, and stays until the
    level up is done.
  - There is no automatic redirect.

## What D&D Beyond does (observed 2026-09-27)

Observed on `characters/48929054` (Seyrie: XP advancement, DDB Red, at 0 HP)
and `characters/50149479` (the reference: milestone, **Cleric Silver**).
Nothing was changed on either.

### Where the theme color is applied

Seyrie's sheet was scanned for every element whose computed color is DDB
Red (`#C53131`), on the Actions tab. The same selectors carry `#92a2b3` on
the Cleric Silver sheet.

| Element | Property |
| --- | --- |
| Section frames (the SVG box drawings: abilities, skills, saves, senses, proficiencies, tabs area) | path `fill` |
| Proficiency / ability ellipse accents | `stroke` |
| Character portrait (header) | border |
| Header buttons (Short Rest, Long Rest, game log) | border |
| XP bar progress | background |
| Outlined theme buttons ("Change sheet appearance") | border |
| Active tab ("Actions") | underline border |
| Active filter chip ("All") | background and border |
| Links such as "Manage Custom" | text color |
| Subsection headings ("Actions", "Bonus Actions", "Reactions", "Other") | text color and underline |
| Attacks heading | text color |

**Scan of the other tabs, done in slice 1:**
- **Every tab:** content-group headings, class-detail names and outlined
  theme buttons.
- **Spells:** the filled Cast buttons.
- **Inventory:** the slot managers (charges, attunement) and the container
  actions.
- **Other elements in the theme color:** the temp HP input, the dice button,
  the header button icons, and the saving-throw row boxes (at 50% alpha).

Colors that stay the same under every theme:
- `#525C63`, an inactive filter chip on hover;
- `#E40712`, the dice tray's Roll button (D&D Beyond's brand red). The
  owner's earlier decision is that our Roll button follows the theme.
- **Outlined theme buttons don't change color on hover** on D&D Beyond.
  Since slice 1 ours don't either: Manage Feats, Manage Spells, Manage
  Inventory and the sidebar action buttons.
- Neither sheet uses `#92A2B3` or `#4A5D6B` anywhere outside the theme. Our
  `#4A5D6B` frame ink was never a D&D Beyond color: D&D Beyond's frames are
  the theme color itself.

**D&D Beyond's builder is not themed.** It keeps its own blue (`#1C9AEF`) and
green (`#96BF6B`), even for a DDB Red character. Ours follows the owner's
decision and uses DDB Red.

### Theme colors

Read from D&D Beyond's theme picker. Each theme is one accent color on a
`#FEFEFE` background.

| Theme | Color | Theme | Color |
| --- | --- | --- | --- |
| **DDB Red (default)** | `#C53131` | Paladin Gold | `#b59e54` |
| Barbarian Fire | `#e5623e` | Ranger Emerald | `#4f7e61` |
| Bard Rouge | `#aa6dab` | Rogue Ash | `#555752` |
| Cleric Silver (our look until now) | `#92a2b3` | Sorcerer Blood | `#972e2e` |
| Druid Moss | `#79853c` | Warlock Iris | `#8137af` |
| Fighter Rust | `#7e4f3d` | Wizard Cobalt | `#0045b7` |
| Monk Sky | `#53a5c5` | Artificer Copper | `#D59139` |

- Campaign themes are left out.
- **Our frames can be recolored:** `frames.css` draws them with
  `currentColor` from `--frame-ink`. Today `--accent-control` is `#92A2B3`
  (Cleric Silver), and `builder.css` repeats the same blue-grays in its own
  `--builder-*` tokens.

### XP bar, character sidebar and Manage XP

- **Header:** under "Wood Elf Ranger 17" there is a thin bar, `LVL 17` on the
  left and `LVL 18` on the right. The filled part is in the theme color, the
  rest gray, with `257,246 / 265,000 XP` centered below in small type.
- **Character sidebar** (dark), top to bottom:
  1. The portrait in a theme-colored frame.
  2. The name, with a pencil.
  3. The species.
  4. The XP bar.
  5. The classes: an icon with a level badge, then the class name and the
     subclass.
  6. **CHANGE SHEET APPEARANCE**, outlined in the theme color.
  7. **MY CHARACTER**: Manage Character & Levels, Manage Experience ("XP"
     glyph), Character Settings.
  8. **PLAY**: Game Log, Short Rest, Long Rest, Find a Group.
  9. **SHARE**: Export to PDF.
- **Manage XP panel:**
  - "Current XP Total: … (Level N)";
  - the bar with a marker, each level's threshold under its end;
  - **Set level** and **Set XP**;
  - **Add XP** and **Remove XP** tabs, with a value field;
  - "New XP Total: … (Level N)" as a live preview.
- **Change Sheet Appearance panel:** "Current Decorations" (Portrait, Frame,
  Theme, Backdrop); an Underdark toggle; "Browse Decorations" accordions.
  Each theme tile is a sample ability box ("DEXTERITY +5 / 20") in that
  theme's color, with the name below.

### Death saves (Seyrie at 0 HP)

The hit points box, top right of the sheet, is replaced by a **Death Saves**
box of the same size, in the theme frame.

- **Left:** a skull icon with ✕ eyes.
- **Middle:** two rows.
  - **FAILURE** (Roboto 15px, bold, black): three 15px circles. A marked
    failure is red, `rgb(210, 64, 64)`; an empty one is dark.
  - **SUCCESS**, with the same circles. A marked success is green; an empty
    one is dark.
- **Below:** the label **DEATH SAVES** (Roboto Condensed 13px, bold).

The exact sizes, colors and click behavior are measured on the live page
during implementation.

## The rules (2014 PHB)

- **XP per level** ("Character Advancement"):

  | Level | XP | Level | XP | Level | XP | Level | XP |
  | --- | --- | --- | --- | --- | --- | --- | --- |
  | 1 | 0 | 6 | 14,000 | 11 | 85,000 | 16 | 195,000 |
  | 2 | 300 | 7 | 23,000 | 12 | 100,000 | 17 | 225,000 |
  | 3 | 900 | 8 | 34,000 | 13 | 120,000 | 18 | 265,000 |
  | 4 | 2,700 | 9 | 48,000 | 14 | 140,000 | 19 | 305,000 |
  | 5 | 6,500 | 10 | 64,000 | 15 | 165,000 | 20 | 355,000 |

  The character level is the sum of the class levels. XP only says how many
  levels the character *may* have. A level up is available when XP reaches
  the next threshold and the level is below 20.
- **Multiclass prerequisites** ("Multiclassing"): a score of 13 in the key
  abilities of the new class *and* of every current class. They are checked
  only while the build's `multiclassPrerequisites` preference is on, as in
  the builder.
- **Death saving throws** ("Dropping to 0 Hit Points"):
  - At 0 HP an unconscious character rolls a d20 at the start of each turn:
    10 or higher is a success, below 10 a failure.
  - **A natural 20** brings the character back with 1 HP.
  - **A natural 1** counts as two failures.
  - Three successes: stable. Three failures: dead.
  - Taking damage at 0 HP is one failure, or two on a critical hit.
  - **Any healing** resets both counts and brings the character back.
  - Stable characters and those at 1 HP or more have no counts.

## Design

### Data (sheet payload, D&D 5e; play state, untouched by re-materializing)

- `experiencePoints`: an int, default 0.
- `appearance.theme`: a theme id, default `ddb-red`. An existing sheet without
  it reads as `ddb-red`, so every character turns red, as decided.
- `deathSaves`: `{ successes 0–3, failures 0–3 }`. Healing above 0 resets it.
- **New column `characters.level_up_draft jsonb`** (migration V9): the build
  with the pending level while the player chooses; null otherwise. Leaving
  the page keeps it, "Cancel" deletes it, and the sheet only changes on
  "Finish".

### Backend (inside the D&D 5e strategies; no `if (dnd5e)`)

- **`Dnd5eExperience`:** the XP table, the level for a total, the thresholds,
  and whether a level up is available. It gets tests from the PHB table.
- **The sheet view** gains:
  - `experience`: `points`, `levelFromXp`, `currentLevelAt`, `nextLevelAt`
    and `levelUpAvailable`, plus the build's `advancement`;
  - `deathSaves`, and `dying` (HP at 0 or less, not yet stable or dead).
- **`POST /api/characters/{id}/experience`** takes `{add}`, `{remove}` or
  `{set}`, and never lets XP fall below 0.
- **`PUT /api/characters/{id}/appearance`** takes `{theme}`, validated against
  the theme list.
- **Death saves:**
  - `PUT /api/characters/{id}/death-saves` with `{successes, failures}` sets
    the counts by hand, as clicking the circles does. *(Built as PUT with
    counts instead of the planned `POST {result}`, so a click can also clear
    a circle.)*
  - `DELETE /api/characters/{id}/death-saves` clears them.
  - **"Roll death save"** goes through the existing dice engine, so the roll
    is persisted like any other. It applies the rules: 10 or more is a
    success, below 10 a failure, a natural 1 is two failures, and a natural
    20 brings the character back at 1 HP.
  - Damage at 0 HP adds a failure, two for a critical when the damage
    endpoint says so. Healing resets the counts.
- **Level up:**
  - `POST /api/characters/{id}/level-up` with `{classSlug}` starts it: +1 to
    an existing class, or a new class at level 1. It checks the cap of 20 and
    the prerequisites.
  - `GET`, `PUT` and `DELETE` on the same path read, save and cancel the
    draft.
  - `POST …/level-up/finish` validates the choices and re-materializes the
    sheet (play state kept). Current HP rises by the max HP gained.
  - The page is driven by the existing `CharacterCreationFlow` plan, limited
    to the choices the new level introduces. Their ids already carry class
    and level (`class:fighter:4:…`).
  - The choices left pending when the level up starts (the grown spell
    pools, rolled HP, the level's own choices) are stored as
    `openedChoiceIds`, and stay on the page once answered.
  - **Hit points follow the build's method** (owner decision):
    - **Fixed:** the class average, with nothing asked.
    - **Rolled:** the new level gets an empty slot at its own place in
      `rolledHitPoints`, so no roll moves to another class's level. The page
      asks for it with the builder's roll field, limited to the leveled
      class.
  - Builds saved before the spell pools (per-level ids
    `class:<c>:<n>:cantrips…`) are folded into the pooled ids when read.
- **XP never goes below where the current level starts** (owner decision,
  2026-09-27): a level 6 character has at least 14,000 XP, and its next
  level up is at 23,000.
  - The sheet reads stored XP as `max(stored, threshold(level))`, so
    characters created at a higher level, or saved before this, show it at
    once.
  - Setting or removing XP stops at that floor, and levels are never
    removed. In the panel, the preview stops there too, and "Set level"
    disables the levels below the current one.

### Themes (web)

- **Tokens:**
  - A theme is a small set of CSS custom properties: `--theme-accent` (the
    DDB color), `--frame-ink` (frames), `--accent-control` (chips, tab
    underline), heading and link colors, and outlined-button borders.
  - Hover and pressed shades come from the accent, measured per role from
    D&D Beyond.
  - They are set by `data-theme="<id>"` on the sheet root.
- **Theme files:** `themes.css` holds one block per theme (DDB Red plus the
  13 class themes), and `sheetThemes.ts` holds the list, for the picker and
  the API validation.
- **Sheet:** every hard-coded use of the blue-gray (`#92A2B3`, and the
  frame-ink blue `#4A5D6B` where D&D Beyond uses the theme color) moves to
  the theme tokens, following the scan table above.
- **Builder and level up page:** always DDB Red, the D&D 5e system default,
  whatever theme the character later picks. `builder.css`'s `--builder-*`
  blue-grays (`--builder-ink`, `--builder-chip-on`, frame borders, buttons,
  step headings) switch to the same theme tokens with `ddb-red`. The builder
  gets its own scan against D&D Beyond's builder, which already appears in
  `dndbeyond-builder-walkthrough.md`.
- **Dice tray and roll popups:** they follow the sheet's theme where D&D
  Beyond's do. That is checked in the scan.

### Dark theme for the home

- **Scope:** the landing page, the character list and the credits page share
  the `--app-*` tokens (`theme/theme.css`), so a dark theme is one `:root`
  override: `[data-app-theme="dark"]` sets dark surface, ink, line and muted
  colors. The gold accent stays.
- **Switch:** a **"Dark theme"** toggle in the user menu (next to "My
  characters"), available in both editions.
- **Storage:** the choice is kept in the browser (`localStorage`), and it
  follows the system's dark mode until the player picks one. It is a
  per-viewer convenience, so it needs no server change, and it works in the
  desktop edition as it is.
- **Out of it:** the sheet and the builder keep their own themes. D&D
  Beyond's "Underdark mode" for the sheet stays out of scope.

### Character sidebar, XP bar and golden arrow (web)

- **Header XP bar:** thin, next to the name and level, XP builds only, in the
  theme color, measured against D&D Beyond.
- **Character sidebar:** a new sidebar kind, opened from the portrait or the
  name and level, in D&D Beyond's order:
  - portrait, name, species, XP bar, class rows with level badges,
    **Change sheet appearance**;
  - **MY CHARACTER**:
    - **Manage experience** (XP builds). **Right under it**, a **Level up**
      button whenever a level up is available;
    - **Level up** (milestone builds);
  - **PLAY**: Game log, Short rest, Long rest.
- **Manage experience panel:** the current total, the bar, Set level, Set XP,
  Add/Remove tabs, the live "New XP Total (Level N)", and **Apply**. After
  applying, the character sidebar shows the Level up button if one became
  available.
- **Golden arrow:** while `levelUpAvailable` is true and the sidebar is
  closed, a small golden up-arrow sits beside the header portrait, with the
  tooltip "Level up available". Clicking it opens the character sidebar. It
  disappears once the level up is finished.
- **Change sheet appearance panel:** the current theme, plus a grid of theme
  tiles (a sample ability box in each color). Picking one applies it at once.

### Death saves (web)

- **When the box shows:** while `dying` is true, the hit points box is
  replaced, in place and at the same size, by the **Death Saves** box: skull,
  FAILURE and SUCCESS rows of three circles, and the "DEATH SAVES" label, in
  the theme frame.
- **Clicking a circle** fills up to it. Clicking the last filled circle
  empties it.
- **Roll:** clicking the skull rolls the save. The roll goes to the dice tray
  and the game log. *(Built this way: D&D Beyond's box has no roll control.)*
- **Three failures / three successes:** as built, the box only shows the
  three filled circles, and the skull can no longer roll. D&D Beyond's own
  dead and stable states weren't measured: that would mean changing Seyrie on
  D&D Beyond. HP management still works in both.
- **Healing** (the existing HP panel) resets the counts and brings the hit
  points box back.

### Desktop edition

- V9 and the new sheet fields migrate on the embedded database like any
  others. Nothing is desktop-specific; the dark home theme works as it is.
- At the end: raise `appVersion` to `0.2.0`, run `packageDesktop`, and test an
  **update** over a data folder made by 0.1.0. Characters must survive.

## Slices

1. **Themes, part 1: DDB Red everywhere.** ✓ Built 2026-09-27.
   - Scan D&D Beyond's other sheet tabs, sidebar panels and builder.
   - Add the theme tokens and `themes.css` with DDB Red.
   - Move every hard-coded blue-gray in the sheet, the builder and the dice
     tray to the tokens.
   - Compare live with Seyrie, and with D&D Beyond's builder.
2. ✓ Built 2026-09-27 (desktop check moved to slice 9). **Home dark theme:** the tokens, the user-menu toggle, and remembering the
   choice. Checked live in both editions.
3. ✓ Built 2026-09-27. **Death saves:** backend (the rules, the endpoints, the roll through the
   dice engine) and the box in the web app, compared live with Seyrie's.
4. ✓ Built 2026-09-27. **XP backend:** `Dnd5eExperience`, `experiencePoints`, the view's
   `experience`, and the experience endpoint.
5. ✓ Built 2026-09-27. **Character sidebar, XP bar, Manage experience panel and golden arrow**,
   compared live with D&D Beyond.
6. ✓ Built 2026-09-27. **Themes, part 2:** the other 13 themes, `appearance.theme`, its endpoint,
   and the Change sheet appearance panel.
7. ✓ Built 2026-09-27. **Level up backend:** V9; start, save, cancel and finish; the plan limited
   to the new level; re-materialization; the HP raise; prerequisites. Tests
   cover a single-class level, a new multiclass, a subclass level, an ASI
   level and a spellcaster level.
8. ✓ Built and checked live 2026-09-27 (Vex milestone/fixed, Mez test XP/rolled). **Level up page:** choose a class, see what the level grants, make the
   choices, set HP, "Finish leveling up". Wired to the sidebar buttons and
   the golden arrow. Checked live on a milestone and an XP character.
9. ✓ Done 2026-10-04. **Desktop 0.2.0:** `appVersion` raised and packaged
   (`OmniSheetVault-0.2.0-windows.zip`). Tested over a copy of the owner's
   0.1.0 data folder (`LOCALAPPDATA` pointed at the copy): Flyway applied V8
   and V9, the 0.1.0 character (Mez, Warlock 3 / Fighter 3) opens, the home
   dark theme toggles, and Manage XP unlocks Level up. The owner closed the
   test there (the level up itself on the 0.1.0 spellcaster was not run).

Each slice ends with its tests, the changelog and the docs, as usual.

## Out of scope

- Removing levels.
- Campaign themes, backdrops, frames, the sheet's Underdark mode.
- Renaming from the sidebar.
- "Character Settings", "Find a Group" and "Export to PDF".
- A dark theme for the sheet or the builder.

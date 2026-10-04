# Feature — Sidebar fidelity and customization (D&D 5e sheet)

Status: **approved 2026-09-27.** Comes before the desktop 0.2.0 package
(slice 9 of `leveling-and-appearance.md`).

## Owner decisions (2026-09-27)

1. **Rules text: yes.**
   - It comes from 5etools' descriptions (2014 sources), formatted the way
     D&D Beyond formats them. For example, a spell's "At Higher Levels." text
     is split into its own paragraph with the lead-in words in bold.
   - The formatting rules are worked out from the reference characters and
     applied to every rules text.
   - **Changed 2026-09-28:** the panes' explanatory text (abilities, saves,
     skills, senses, rests…) is written by the owner by hand, not ingested.
     The slots and their styling are built; see `rules-text-slots.md`.
     Copying D&D Beyond's page text into the code isn't done (it's the
     PHB's copyrighted text).
2. **Sidebar controls:** hide and lock only, with the icons from
   `svg-index.html` (`SidebarLeftSvg` / `SidebarRightSvg`, `LockSvg` /
   `UnlockSvg`). No overlay / fixed, no left / right.
3. **HP:** switch to D&D Beyond's **Max HP Modifier** and **Override Max
   HP**, replacing the "manual adjustment".
4. **Defenses stay display only.** Resistances, immunities and
   vulnerabilities only matter once the damage taken is known, and that
   happens at the table.

A throwaway D&D Beyond character for observing changes (such as adding and
removing a defense): `characters/171527207`.

## What the owner asked for

1. Rebuild every sheet sidebar to D&D Beyond's structure and styling. "Almost
   every sidebar is styled differently from Beyond."
2. More customization, as D&D Beyond has:
   - rename the character from the pencil beside the name in the character
     sidebar;
   - D&D Beyond's "Customize" sections in the sidebars;
   - a gear on Senses, Saving Throws and Skills, not only on Proficiencies &
     Training;
   - add defenses by hand.

This reverses a documented deviation: "No value overrides in the first
version — the 'customize' collapsible is absent from every panel"
(`systems/dnd-5e/sheet-ui.md`).

## What D&D Beyond does (observed 2026-09-27, `characters/50149479`)

Nothing was changed on the reference character. Customize sections were
opened and closed without typing.

### The sidebar shell (every panel)

- **Width** 340px. The content has a `#FEFEFE` background, padding
  `0 10px 16px`, and uses Roboto 13px / 18.2px in `rgb(18,24,28)`.
- **Frame:** an SVG border drawn at the top and bottom of the pane, a gap, then
  the content. It comes in light and dark variants; the character panel is
  dark.
- **Control bar above the pane:**
  - Hide sidebar (»);
  - Lock / Unlocked;
  - Set to overlay position / Set to fixed position;
  - Align Left / Align Right.
  Ours has only a "×".
- **Header** (`ct-sidebar__header`):
  - an optional small **parent** line above ("Saving Throws", "Skills",
    "Cleric");
  - an optional **preview icon** (32px);
  - the heading in Roboto Condensed 700 18px, with the value in a smaller
    trailing span.
- **PREV / NEXT** sit on the top border when the panel belongs to a list,
  e.g. a single skill; they step through its siblings.
- **Customize:**
  - a `<details>` with 1px top and bottom borders in the theme color at 40%
    alpha;
  - the summary "Customize" is Roboto 600 13px with `10px 0` padding and a
    chevron;
  - inside it sits an **editor box** (`#F9F9F9` background, 1px `#EAEAEA`
    border, 10px padding) of **value editor rows**. Each row has a label
    (11px), a value field (72×25) and a "Enter Source Notes..." field (215×25).
    Both fields are white with a 1px `#DCDFE1` border and 4px radius.
- **Subheadings** (`ct-sidebar__subheading`): condensed caps, e.g. "OVERRIDE
  SPEEDS", "SAVING THROW MODIFIERS", "ADD NEW PROFICIENCIES".
- **Rules text** (`ddbc-html-content`) closes most panels: the PHB text for
  abilities, speed, initiative, AC, saves, senses and skills.

### Shell measurements (slice 1, measured on 171527207, DDB Red)

- **Position:** the sidebar is `position: absolute`, 340px wide. The control
  bar sits 8px below its top, is 23px tall, and has a 16px left margin; the
  pane starts 33px below the top.
- **Frame:**
  - top and bottom SVGs (`viewBox 0 0 340 18`, 340×18); the bottom one is the
    top one rotated 180°;
  - `borderBg` fill: `#FEFEFE` light, `#12181C` dark;
  - `borderStroke` and `borderFill`: the theme color;
  - then a 10px gap, and content with 3px side borders in the theme color;
  - the content scrolls (`overflow: auto`).
- **Controls:**
  - 18×18 icons, `#F9FAFA` at 60% opacity, 12px apart;
  - open: "Hide sidebar" (`SidebarRightSvg`) and "Unlocked" (`UnlockSvg`);
  - hidden: the sidebar shrinks to a 25px strip with only `SidebarLeftSvg`,
    and reopening brings back the last panel;
  - locked: only `LockSvg` shows, and the hide control disappears.
- **Behavior:**
  - unlocked, Escape and a click outside the sidebar hide it;
  - locked, neither does;
  - a locked sidebar still switches panels when something else on the sheet
    is clicked.
- **Header:**
  - `margin-bottom: 10px`;
  - the optional parent line is Roboto 13px and clickable (it opens the
    parent panel, e.g. Saving Throws);
  - the preview is a 37×37 box holding a 32px icon;
  - the heading is Roboto Condensed 700 18px. Its value follows 5px later,
    with the sign in 12px at 60% alpha and the number in 18px.

### Panel by panel

| Trigger | D&D Beyond's panel | Customize |
| --- | --- | --- |
| Portrait / name | Dark panel: portrait, **name with a pencil**, species, level, class rows, Change Sheet Appearance, My Character / Play / Share menus | The pencil swaps the name for an inline field (dark `#2B2C30` bg, 1px `#75838B` border, 4px radius, Roboto 16px, max 128 chars). Escape cancels. |
| Ability score | Icon + "Strength 13 (+1)"; table: Total Score, Modifier, Base Score, Bonus, Set Score, Stacking Bonus; then the rules text | Always visible, not collapsed: **Other Modifier**, **Override Score** |
| Proficiency bonus | Heading + rules text | — |
| Speed | "Walking 25 ft." rows; rules text | **Override Speeds** (Burrowing, Climbing, Flying, Swimming, Walking: speed + source/notes); **Set Movement Display** (select) |
| Initiative | "Initiative (+4)"; Initiative Score 14, With Advantage (+5) 19, With Disadvantage (−5) 9; rules text | — |
| Armor class | "Armor Class: 18"; contribution rows (14 Armor (Scale Mail), +1 Dexterity Bonus (Max 2)…); rules text | **Override AC**, **Override Base Armor + DEX**, **Additional Magic Bonus**, **Additional Misc Bonus** (each with source notes) |
| Defenses | "Defenses"; RESISTANCES / IMMUNITIES (/ VULNERABILITIES) with icon and source ("Poison (Dwarven Resilience)") | **Defense Type** (Resistance / Immunity / Vulnerability) → **Defense Sub-Type** ("Choose a Resistance") adds one |
| Conditions | Icon + name + toggle + chevron per condition; Exhaustion with levels 1–6 | — |
| Saving Throws (gear) | The saves grid; SAVING THROW MODIFIERS ("against Poison"); rules text | — |
| A single save (click its label) | Parent "Saving Throws"; icon + "Strength Saving Throw +1"; rules text | **Saving Throw Override**, **Magic Bonus**, **Misc Bonus**, **Proficiency Level** (select) |
| Senses (gear) | Passive Perception / Investigation / Insight, then Darkvision 60 ft.; rules text | **Override Passive** ×3; **Blindsight / Darkvision / Tremorsense / Truesight** (distance + source/notes) |
| Skills (gear on the "SKILLS" label) | "All Skills": collapsible **Skills** (the list) and **Custom Skills** ("+ Add Custom Skill") | — |
| A single skill | Parent "Skills"; proficiency dot, ability, "Acrobatics +1"; PREV / NEXT; rules text | **Skill Override**, **Magic Bonus**, **Misc Bonus**, **Proficiency Level** (select), **Stat Override** (select) |
| Proficiencies & Training (gear) | ADD NEW PROFICIENCIES: **Proficiency Type** select; then Armor / Weapons / Tools / Languages groups | — |
| Hit points | HP Management: Current / Max / Temp; Healing / Damage with ± and New HP | **Max HP Modifier**, **Override Max HP** (plain fields) |
| Item (e.g. Warhammer) | Icon, **name with a pencil**, "Weapon, common", "In Equipment"; properties; description; TAGS; Unequip / Move / Delete | **To Hit Override, To Hit Bonus, Damage Bonus, Cost Override, Weight Override**, Silvered, Adamantine, **Display As Attack**, **Name**, **Notes**, a reset button ("No customizations") |
| Spell (e.g. Guiding Bolt) | Parent class, icon, **name with a pencil**, level/school, Cast + level stepper, damage; properties; text; TAGS | **To Hit Override, To Hit Bonus, Damage Bonus, DC Override, DC Bonus**, Display As Attack, Name, Notes, reset |
| Class feature | Parent class, icon, name, text | — |

**Hand-added defenses** (observed on the throwaway character 171527207; the
test resistance was removed afterwards):
- **Sub-type list:** searchable. For Resistance and Vulnerability it holds the
  13 damage types plus variants ("Bludgeoning from nonmagical attacks",
  "Damage from Spells"…). For Immunity it is grouped into **Damage** (the 13
  types plus a few variants) and **Conditions** (the 15 conditions).
- **Choosing a sub-type adds it at once.** The sub-type then resets to "Choose
  a Resistance".
- **In the list at the top:** "Cold\* (Custom)", with the damage icon, sorted
  alphabetically among the granted ones. The asterisk marks it as manual. The
  source in parentheses is gray `#75838B`.
- **Inside Customize:** a section per type ("RESISTANCES") listing the manual
  ones. Each row has a **DELETE** button, the name, and an "Enter Source
  Note..." field.
  - The button is a theme outline button: Roboto Condensed 8px uppercase in
    the theme color, 1px border, 3px radius, 37×22.
  - Deleting is immediate, with no confirmation.
- **On the sheet:** the Defenses box shows "Cold\*".
- **Owner decision (2026-09-27): only the 13 damage types**, plus D&D
  Beyond's 15 conditions for immunity (Blinded, Charmed, Deafened, Exhaustion,
  Frightened, Grappled, Incapacitated, Invisible, Paralyzed, Petrified,
  Poisoned, Prone, Restrained, Stunned, Unconscious).
  - Counted on 171527207: D&D Beyond offers 26 resistances, 16 damage
    immunities plus 15 conditions, and 15 vulnerabilities. Its variants are a
    curated list ("Damage Dealt By Traps", "Lightning (granted by darksteel
    greataxe)"…).
  - 5etools' monster data would give 49 resistances and 46 immunities, a
    larger list that still doesn't match. So neither list is reproduced.

## What we have today

- **One `Sidebar` shell:** an `aside` with a "×", and no frame, control bar,
  PREV / NEXT or light / dark panes beyond `sidebar--dark`.
- **One generic Explainer** serves ability, proficiency bonus, speed, AC,
  initiative, skill, save, senses and defenses: a title with a value and a
  contributions list. It has no table, no rules text and no Customize.
- **No Customize anywhere, and no stored overrides.** The only related data are
  the build's `abilityScoreAdjustments` and the HP "manual adjustment" (a
  documented replacement for D&D Beyond's Max HP Modifier / Override pair,
  replaced by that pair in slice 9a).
- **No rename endpoint:** `characters.name` is set at creation only.
- **Gears:** Proficiencies & Training has one; Saving Throws, Senses and Skills
  don't.

## Design

### Data (sheet payload, play state that survives re-materializing)

- A new `customizations` block in `Dnd5eSheet`, null on older sheets:
  - `abilities`: `{ability → {otherModifier, overrideScore}}`;
  - `savingThrows`: `{ability → {override, magicBonus, miscBonus,
    proficiencyLevel, notes…}}`;
  - `skills`: `{skill → {override, magicBonus, miscBonus, proficiencyLevel,
    statOverride, notes…}}`, plus `customSkills`
    `[{key, name, ability, proficiencyLevel, …}]`;
  - `senses`: passive overrides, and sense distances with notes;
  - `speeds`: overrides with notes, plus `movementDisplay`;
  - `armorClass`: override, base armor + DEX override, magic bonus, misc
    bonus, each with notes;
  - `defenses`: `[{type: RESISTANCE|IMMUNITY|VULNERABILITY, subtype}]`;
  - `hitPoints`: `maxModifier` and `overrideMax` (slice 9a);
  - `proficiencies`: `[{key, type: ARMOR|WEAPON|TOOL|LANGUAGE, name, custom,
    notes}]` (slice 9b);
  - `items` and `spells`: `{key → {toHitOverride, toHitBonus, damageBonus, …,
    name, notes}}`.
- Every value is optional; an empty value means "not customized". Every block
  goes through Bean Validation, like the rest of the payload.
- **The calculator applies them** in one place per stat, and each override
  shows up in the derivation trace, so the Explainer tables stay honest.

### API

- `PUT /api/characters/{id}/name`: generic, on the relational column,
  1–128 characters.
- `PUT /api/characters/{id}/customizations/{group}/{target}`: the D&D 5e mutator
  validates the group and target (strategy, no `if (dnd5e)`).
- `POST` / `DELETE /api/characters/{id}/defenses` for hand-added defenses.
- `POST` / `DELETE /api/characters/{id}/custom-skills`.

### Web

- **Shell primitives** (in `ui-design-system.md`):
  - `SidebarFrame` (the SVG borders, light / dark);
  - `SidebarControls`: hide, lock, overlay / fixed, left / right. The choices
    are kept per viewer in `localStorage`, like the home theme;
  - `SidebarHeader` (parent, preview, heading, value);
  - `SidebarPager` (PREV / NEXT);
  - `Customize` (the details) with `EditorBox` and `ValueEditorRow` (value +
    source notes);
  - `SidebarSubheading`.
- **Explainer replaced by dedicated D&D 5e panes:** Ability, Speed,
  Initiative, Armor Class, Defenses, Saving Throws, Saving Throw, Senses,
  All Skills and Skill. Proficiency Bonus keeps a simple pane.
- **Gears** on Saving Throws, Senses and Skills, and "Additional Skills" under
  the list, drawn like the existing Proficiencies gear.

## Slices

1. ✓ Built 2026-09-27. **Sidebar shell:** the frame, the control bar (hide
   and lock), the header, the Customize primitives, and the tokens. Every
   existing panel moves into the new shell unchanged. The PREV / NEXT pager
   moves to slice 5, where it is first used.
2. ✓ Built 2026-09-27. **Character name:** the endpoint, and the pencil with
   its inline field in the character sidebar. The header and the character
   list update.
   - D&D Beyond's behavior, observed on 171527207 (renamed, then restored):
     - a click on the name or the pencil opens the field, focused, with the
       cursor at the end;
     - **leaving the field saves**, and the header updates at once;
     - **Enter doesn't save**;
     - a blank name isn't saved;
     - Escape closes the sidebar without saving.
   - Name: Roboto Condensed 24px / 33.6px, white. Pencil (`PencilSvg`):
     15×14, 8px from the name. Row: centered, `padding: 0 25px`, 8px below.
   - Field: 269×32, `#2B2C30` background, 1px `#75838B` border, 4px radius,
     Roboto 16px, 128 characters at most.
3. ✓ Built 2026-09-27. **Ability pane:** the table plus Other Modifier and
   Override Score, stored and applied.
   - D&D Beyond's table (measured on 171527207's Strength):
     - 1px `#EDEAE8` borders and 29px rows, label cells padded `5px 10px`
       (500 13px);
     - value cells 70px wide and centered, with a 2px `#D0CAC5` left border;
     - bonus sources ("Mountain Dwarf (+2)") under Bonus, in 12px `#75838B`
       indented 18px;
     - Other Modifier and Override Score are 70×27 fields with a "--"
       placeholder and a 1px `#A2ACB2` border;
     - the heading is "Strength 17" with "(+3)" in 14px.
   - Our table is 314px wide against D&D Beyond's 299. That is D&D Beyond's
     15px scrollbar: its panes scroll because of the rules text. Ours will
     match once the rules text arrives (slice 10); it isn't a styling
     difference.
4. ✓ Built 2026-09-27. **Saving throws:** the gear, the Saving Throws pane,
   and the per-save pane with its Customize.
   - Proficiency Level options (observed): -- / Not Proficient / Half
     Proficient / Proficient / Expertise.
   - Subheading measured: Roboto Condensed 700 19.5px / 27.3px, uppercase,
     `margin: 10px 0`.
   - A save's pane opens from its abbreviation (on the sheet and in the pane's
     grid); the bonus still rolls.
5. ✓ Built and checked live 2026-09-27. **Skills:** the gear, All Skills
   (Skills / Custom Skills with add), and the per-skill pane with PREV / NEXT
   and its Customize.
   - The passive senses follow a customized Perception, Investigation or
     Insight since slice 6.
   - Observed on 171527207 (a custom skill was added, then removed):
     - **All Skills:** opened by both the "SKILLS" gear and "Additional
       Skills". Two collapsibles, "Skills" (open) and "Custom Skills"
       (closed). Each header has a `#F1F1F1` background, 10px padding, a 3px
       theme-colored left border and a 700 13px heading. The rows are the
       sheet's own skill rows.
     - **"+ Add Custom Skill"** at once adds "Custom Skill 1": no ability
       ("--"), proficient, +0. It appears in the list and at the end of the
       sheet's Skills box.
     - **Custom skill pane:** "Skills" parent, a collapsible **"Edit"** (not
       "Customize"), and a REMOVE button that deletes at once. Edit holds
       Override, Magic Bonus, Misc Bonus, Stat (select), Proficiency Level
       (select, default Proficient), Name, Notes and Description (textarea),
       in the item-customize grid.
     - **PREV / NEXT:** a `nav` absolutely positioned 5.2px from the pane's
       top, centered, 48px apart. The buttons are 61×26, Roboto Condensed 500
       10px, uppercase, 0.4px letter spacing, with a 14px Font Awesome chevron
       in the theme color ("‹ PREV", "NEXT ›"). At an end, the button is at
       50% opacity with a default cursor.
6. ✓ Built and checked live 2026-09-27. **Senses:** the gear, and the pane
   with its Customize.
   - Measured on 171527207:
     - list lines are 20px, with the label in 13px and the value in Roboto
       Condensed 700 14px;
     - the distance table has an 80px label column, "Distance (ft.)" and
       "Source/Notes" headers in bold 13px, 25px rows, and 85×25 distance
       fields.
   - The passive scores are now 10 + the skill's own bonus (PHB 2014), so
     they follow its customization; an override replaces the result.
   - A distance set for a sense the character lacks adds that sense.
   - The rules text is still our own summary until slice 10.
7. ✓ Built and checked live 2026-09-28. **Speed, initiative, armor class,
   defenses:** the panes, their Customize, and hand-added defenses with
   remove.
   - Only walking speed is calculated. Other movements exist once overridden.
     The top-row badge shows the Movement Display choice when it has a speed,
     otherwise walking.
   - Defense sources come from `DEFENSE` grants (species), the item name, or
     "Custom". Sheets materialized before 2026-09-28 show no species source
     until the build is re-applied.
   - **Sidebar position (owner report, 2026-09-28):** D&D Beyond's
     `ct-sidebar` is `position: absolute` (top 72px), 966px tall on a 1044px
     page. It scrolls away with the sheet instead of staying on screen. Ours
     now does the same, anchored to `SheetShell`'s wrapper below the app
     header.
   - **Rules text (owner, 2026-09-28):** the owner asked for D&D Beyond's
     wording. D&D Beyond shows the PHB 2014 paragraphs, and 5etools' PHB data
     holds the same text. Slice 10 therefore takes it from 5etools (the agreed
     source, ADR-0006), not by copying it off the D&D Beyond page.
8. ✓ Built and checked live (DOM) 2026-09-28; the owner approved the look
   the same day. **Items and spells:** their Customize
   and rename, applied to attacks, DCs, cost and weight.
   - Observed on 171527207 (a To Hit Bonus was set on the Longsword, then
     removed):
     - Customize is a minimal collapsible right under the header ("Weapon,
       common / In Equipment"), above the properties.
     - **The pencil beside the name toggles Customize and focuses Name.**
       There is no separate inline rename field.
     - Layout: a `ct-editor-box` (10px padding, `#F9F9F9`, 1px `#EAEAEA`).
       Three columns of 97px cells: a 45×23 number field (10px text) with a
       10px / 13px label to its right, or a 20px checkbox with its label.
       Name and Notes are full-width rows with a 250×23 field.
     - A weapon has To Hit Override, To Hit Bonus, Damage Bonus, Cost
       Override, Weight Override, Silvered, Adamantine, Display As Attack,
       Name and Notes. Any other item (Chain Mail) has only Cost Override,
       Weight Override, Name and Notes.
     - Once customized, the name and the section read "Longsword\*" and
       "Customize\*". The 110×27 button turns from "No Customizations"
       (disabled) into "Remove Customizations". A click shows "Confirm (3)"
       counting down; a second click resets.
   - **Our choices:**
     - Weight Override is entered in kg, like the rest of the sheet, and
       stored in lb.
     - Silvered and Adamantine show among the item's properties.
     - Display As Attack lists a weapon among the attacks even when not
       equipped. For a spell, it lists the spell in the Actions tab.
     - The spell's Customize sits in the same place as the item's. That spot
       is assumed, not observed: the test character has no spells.
     - Weapon proficiency also matches the catalogue slug, so a renamed
       weapon keeps it.
9. **Every other panel restyled to D&D Beyond,** each compared live. Split into
   sub-slices, one panel group each:
   - 9a. ✓ Built 2026-09-28. **HP Management**, with Max HP Modifier and
     Override Max HP (owner decision 3).
     - Observed on 171527207 (a modifier of 3, then an override of 10, both
       cleared afterwards):
       - no Customize collapsible: three sections, each closed by a 1px
         bottom border in the theme color at 40% alpha with 20px padding
         below;
       - Current / Max / Temp: condensed 700 13px `#75838B` uppercase labels;
         Current and Temp are 64×31 fields (Roboto 24px, 1px theme border);
         the slash and Max are Roboto 500 26px;
       - Healing and Damage are 88px boxes (1px green / red border, 4px
         radius, a 10px label and a 28px condensed field); each has a 36×36
         theme-filled "+" / "−" button ("Increase / Decrease Hit Points")
         that **steps its field up by 1, without applying**; New HP (28px
         condensed) sits between them;
       - while healing or damage is pending, APPLY CHANGES (100×30, theme
         fill) and CANCEL (65×30, theme outline) show 24px under Damage,
         8px apart, Roboto Condensed 500 10px uppercase (corrected
         2026-09-28: the first build applied on "+" / "−");
       - a dying character's pane ends with a "Death Saving Throws Rules"
         subheading and rules text;
       - Max HP Modifier and Override Max HP: two centered columns, a
         label over a 50×30 field with a "--" placeholder, saved on blur;
       - a modifier makes Max `16` in green with `(13)` (17px) beside it; an
         override replaces the maximum and ignores the modifier (`10` in red,
         below the calculated 13);
       - **current hit points move with the maximum** (13 → 16 → 10 → 13):
         D&D Beyond keeps the damage taken;
       - the top-row box shows the plain number.
     - **Our choices:**
       - both values are the `hitPoints` customizations (`maxModifier`,
         `overrideMax`); a stored legacy `maxHitPointsAdjustment` reads as the
         Max HP Modifier, and `POST /hit-points/max-adjustment` is gone;
       - any customization that changes the maximum (a Constitution override
         too) moves current hit points by the same amount; a character at 0
         stays at 0;
       - exhaustion 4 still halves an overridden maximum;
       - the "Max HP Breakdown" stays under the fields (not on D&D Beyond),
         drawn as a Customize collapsible holding the ability pane's table
         and a "Max Hit Points" total row.
   - 9b. ✓ Built 2026-09-28. **Proficiencies & Training** ("Add New
     Proficiencies").
     - Observed on 171527207 (Elvish and a custom language were added, then
       removed):
       - "Add New Proficiencies" subheading; a "Proficiency Type" label
         (Roboto Condensed 700 16px, `10px 0 2px`) over a 314×33 select
         (1px `#DCDFE1`) grouped **Existing** (Armor, Weapon, Tool, Language)
         and **Custom** (Tool, Language);
       - an existing type shows "Available Proficiencies", indented 10px:
         D&D Beyond's catalogue grouped by source (weapons by Simple /
         Martial). Picking one **adds it at once**;
       - a custom type adds an entry at once, with an "Enter Name" field
         (214×25) instead of the name;
       - hand-added entries are listed first, under a subheading per type
         ("Languages"), indented 10px: the name (Roboto Condensed 700 13px,
         214px), a "Remove Proficiency" button (85×22, theme outline, 8px
         uppercase; removes at once), and an "Enter Source Note..." field
         (304×25) below;
       - the pane ends with the full lists: Armor / Weapons / Tools /
         Languages headings (Roboto Condensed 700 14px uppercase `#75838B`)
         over comma-separated names (Roboto Condensed 13px), groups split by
         1px `#D8D8D8` with 10px above and below;
       - no rules text.
     - **Our choices:**
       - hand-added proficiencies are the `proficiencies` customizations
         (`Dnd5eCustomProficiency`: type, name, custom, notes), so they
         survive re-applying the build, and only they can be removed. The
         calculator joins their names to the build's lists (weapon attacks
         count them too);
       - the old `POST` / `DELETE /api/characters/{id}/proficiencies/{category}`
         (free text straight into the build's lists) and the Collection
         editor panel are gone. Entries a sheet already got that way stay in
         its lists until the build is re-applied;
       - options come from our catalogue: languages grouped by source; mundane
         armor and shields by armor type; mundane weapons by Simple /
         Martial; tools (Tool, Artisan's Tools, Instrument, Gaming Set) by
         source. Each option shows its source. Native selects with
         `<optgroup>`, as in the Defenses pane.
   - 9c. ✓ Built and checked live 2026-10-03. **Conditions.** Measured on
     Helga (read only) and 171527207 (Blinded and Exhaustion 2 set, then
     cleared):
     - the standard sidebar header, then the active effects (this app's own),
       then the list;
     - 45px rows, padded `10px 5px`, with no dividers;
     - 20px icon in a 25px box with 10px after it; name Roboto 14px `#12181C`,
       bold while active;
     - a 27px chevron column;
     - toggle: a 32×14 track, `#C7CDD1` off and theme 50% over white on. Its
       20px knob sits 3px proud: `#A2ACB2` off; theme color and 12px to the
       right on. Shadow `0 1px 1px rgba(0,0,0,.3)`, 0.3s transitions;
     - Exhaustion is its own group: 10px margin, 1px `#EAEAEA` top border,
       10px padding;
       - "Level --" / "Level N" (13px, bold from level 1);
       - a centered level bar "-- 1 2 3 4 5 6": 4px gap, 12px radius, 19px
         buttons padded `2px 12px`, Roboto 700 13px, `#F9FAFA` / `#75838B`;
       - the current level is in the theme color, and from level 1 the
         levels below it and "--" are theme 60% over white, all with white
         text.
     - The sheet's summary now reads "Exhaustion (Level 2)", as on D&D Beyond.
   - 9d. ✓ Built and checked live 2026-10-03. **Entity detail (features,
     actions, extras).** Measured on Helga (read only: Divine Sense, her Cat):
     - **Feature action** (`featureDetail.tsx`): the granting feature as the
       parent line, a link that opens its pane (`FeatureAction.parentName` /
       `parentKey`, from the calculator); then "Limited Use" (13px bold, 5px
       after) with the boxes and no recharge text, in its own separated
       section (`actionsPosition: 'section'`); then "Action Type: 1 Bonus
       Action"; then the text. The old "Recharge" property is gone.
     - **Feature** (Features tab and the parent link): the source as the
       parent line, no properties; text 12px / 16.8px with `5px 0`
       paragraphs; each action it grants in an extra block (3px `#D8D8D8`
       left border, padding `5px 0 5px 8px`, margin `5px 0 5px 4px`) with
       "Name: 1 Action" and "Uses" + boxes + "/ Short or long rest". A
       trait-linked action spends its trait, so the feature's own track only
       shows when no linked action has uses.
     - **Extra**: category as the parent line ("Familiar"); the collapsed Hit
       Points block right under the header (separator, 15px padding; summary
       `#F1F1F1`, padding 10px, 13px); the stat block as D&D Beyond's creature
       block (1px `#AFA47A` border, `#F5F3EE`, shadow `0 0 5px #A2ACB2`,
       padding `15px 10px`, Scala Sans 15px `#5B160C`; name 20px bold with a
       rule; italic gray meta; bold attribute labels; two STR–CON / INT–CHA
       tables 14px with 11px uppercase MOD / SAVE heads; Traits / Actions
       18px headings with a rule, bold-italic entry names); Delete last in
       its own separated section.
     - Not reproduced: the extra's source line ("Monster Manual"), its
       Customize and its creature art — we hold none of them.
   - 9e. ✓ Built and checked live 2026-10-03. **The character menu, the
     game log, the rests.** Measured on Helga (rests, read only: panes
     opened, never taken) and 171527207 (game log: four test rolls; menu):
     - **Short rest:** standard header; intro 13px `#75838B`; RECOVER
       (margin 25px 0; heading Roboto Condensed 16px bold uppercase; sources
       13px, margin 10px 0 — `restRecovery`: Pact Magic and the features that
       recharge on a short rest); one block per hit die pool (margin 25px 0,
       15px between): "CLASS (Hit Die: 1d8+2 • Total: 4)" (class 16px bold
       uppercase, 5px after, rest 14px condensed), then the pool's 20px boxes
       centered, 2px margins, 10px below the heading — spent dice locked,
       marking the next ones selects them; then TAKE SHORT REST (theme fill,
       10px uppercase, padding `9px 15px`) and RESET (outline, 10px apart),
       centered. `HitDice.Pool` now carries `classNames` for the heading.
     - **Long rest:** same header and RECOVER ("Up to N Hit Dice, N Spell
       Slots, …"); TAKE LONG REST / RESET; the closing rules text after a
       separator (margin and padding 16px). The intro stays dark there. The
       hit dice choice (C2b) uses the same box rows.
     - **Game log:** chat bubbles, newest at the bottom: sender 12.8px bold
       `#738694`, 36px left indent; bubble `#182026`, 1px `#1B9AF0` border,
       radius `12px 12px 0 12px`; "ACTION: TYPE" 13px bold uppercase
       `#A7B6C2` with the type colored (check `#B55DFF`, save `#6CBF5B`, to
       hit `#1B9AF0`, damage `#DF7B7B`); the newest expanded (die icon +
       "8 + 3" 20px, notation 14px `#5C7080`, "=" between rules, total 32px),
       the rest collapsed (padding `8px 10px`, ruled total 22.4px) until
       clicked; "2 MINS AGO" 11.2px bold `#BFCCD6` on the right.
     - **Character menu:** a milestone build shows "Level N" (10px) under the
       species, 15px above the classes; group rows padded `10px 40px 10px
       15px` (40px tall).
     - **Deviations:** no "Reset Maximum HP" / "Automatically apply healing"
       checkboxes (healing is always applied; no temporary max HP changes
       exist), no 2024 "Long Rest Rules" radio (PHB 2014 only), no class art
       beside the class rows, no dice-set preview in the log, and the log
       isn't pinned to the pane bottom (our sidebar is taller than the
       window, so it opens scrolled to the newest instead). The old "Spend
       Hit Dice" control (`HitDice.tsx`) is gone: dice are spent through the
       short rest, as on D&D Beyond.
   - 9f. ✓ Built and checked live 2026-10-03. **Manage Custom, Manage
     Inventory / Spells / Feats.** Measured on 171527207 (a custom action
     created, measured, then removed) and Helga (Manage Spells opened, read
     only):
     - **Manage Custom Actions:** groups per template (19.5px bold uppercase
       subheading, margin 10px 0); rows with the name and REMOVE ACTION (8px
       outline, padding 5px, radius 3px); "Add New Actions" + a select whose
       choice creates "Custom Action N" at once, with no activation type
       (shown nowhere on the Actions tab until one is set — the API now
       accepts a null `activationType`). Clicking a name opens the action's
       pane: name, an "Edit" section with D&D Beyond's field grid (selects
       44×33, numbers 45×23, checkboxes, then Name / Snippet / Description),
       saved field by field through the new `PUT
       /api/characters/{id}/custom-actions/{actionKey}`, then Range/Area,
       Activation Type, Damage, Save and the text. The Actions tab opens the
       same pane.
     - **Manage Inventory:** "Add Items" collapsible (open) with Filter
       (37px search, icon at 12px) and "Filter By Type" chips (Armor, Potion,
       Ring, Rod, Scroll, Staff, Wand, Weapon, Wondrous, Other Gear: 8px
       outline, 55px minimum, 5px gaps, matched on the catalogue's
       `itemKind` / `typeLabel`); result rows (name colored by rarity, 11px
       type line, ADD 8.8px outline, 1px `#EAEAEA` separators); "Add Custom
       Item" between rules (the Customize field grid); "MY INVENTORY" (16px
       bold uppercase) with one grey collapsible per location, its weight on
       the right (the server's figure), and rows of equip box, name (opens
       the item's pane, where Delete lives) and MOVE.
     - **Manage Spells:** a "Spell Slots" collapsible with the per-level
       "1st / 3" callout; per class a 15px bold uppercase heading, then
       "Known Spells" (counts in Roboto Condensed 14px, Filter, "Filter By
       Spell Level" chips, every catalogue spell with LEARN outline or DELETE
       filled, names italic with a 60% "(1st)") and "Prepared Spells (N)"
       (cantrips with DELETE, UNPREPARE, ALWAYS PREPARED; below a rule, the
       known but unprepared spells with PREPARE).
     - **Manage Feats:** every catalogue feat grouped by source book (19.5px
       bold heading with a chevron, open; 1px gray bottom rule, 20px after);
       rows of name 13px and source 11px italic gray, ADD outline or REMOVE
       filled; separators in the theme color at 40%.
     - **Deviations:** no "Add ▾" destination menu (items go to the default
       location), no Proficient / Common / Magical / Container checkboxes or
       source-category chips, no inline item or feat details, no class
       portrait, no slot editing in Spell Slots (pact slots aren't listed
       there), no Settings button on Manage Feats.
   - 9g. ✓ Built and checked live 2026-10-04. **The text fields, Manage XP
     and Change Appearance.** Measured on 171527207 (panes opened only) and
     Seyrie, 48929054 (the XP reference, read only):
     - **Single text field** (Personality Traits, Ideals, Bonds, Flaws,
       Appearance, the Notes): header, then a growing textarea (min 42px,
       padding `11px 10px`, 1px `#EAEAEA`, radius 3px, 13px / 18px, "Enter
       any … here!"); with suggestions, a SUGGESTIONS subheading (19.5px
       bold uppercase), the prompt, and the table (die column, the entries,
       RANDOM; header Roboto Condensed 14px bold with a 3px `#D0CAC5` rule;
       rows striped `#FAF8F7`, 1px `#EDEAE8` rules; "+ ADD" 8px filled theme
       buttons). A picked suggestion is appended and saved at once.
     - **Combined fields:** bold 13px labels over 25px inputs (1px `#DCDFE1`,
       radius 4px), 10px apart; Alignment and Lifestyle are 33px selects with
       D&D Beyond's choices. **Owner decision (2026-10-04):** Alignment,
       Faith and Lifestyle stay in the Characteristics panel, and the
       physical details (Hair, Skin, Eyes, Height, Weight, Age, Gender, Size)
       in their own Appearance Details panel opened from the Appearance
       section — D&D Beyond joins them in one "Characteristics and Details"
       panel.
     - **Manage XP:** standard header; totals Roboto Condensed 15px bold
       (`12px 0 20px`; the new total after a rule); controls after a theme
       40% rule with 20px margin and padding; SET LEVEL (90×33 select) and
       SET XP (90×27 input) on one row; ADD XP / REMOVE XP tabs (12px bold
       uppercase, 2px underline in the theme color, active text `#374045`);
       a full-width 27px value field.
     - **Change Sheet Appearance:** "Current Decorations" with Portrait (130px)
       and Theme (the sample and its name) in 157×160 cells, labels in
       `#75838B` above, a `#EAEAEA` rule; "BROWSE DECORATIONS" (19.5px bold
       uppercase) with a collapsed "Themes" section holding the tiles.
     - **Deviations:** no Frame, Backdrop or Portrait browsing, no Underdark
       preference (the app has its own dark theme), no theme groups (DEFAULT /
       class) inside Themes.
10. ✓ Done 2026-10-04. **Docs:** the deviations list and the trigger table in
    `systems/dnd-5e/sheet-ui.md`, and the molds in `ui-design-system.md`, now
    describe the panes as built.
    - ✓ Built early, 2026-09-28: the **rules text slots**. `RulesText` draws
      D&D Beyond's `ddbc-html-content` at the end of the Ability, Proficiency
      Bonus, Speed, Initiative, Armor Class, Saving Throws, Saving Throw,
      Senses and Skill panes, and above and below the rest controls. The
      owner fills `rulesText.ts` (`rules-text-slots.md`).

Each slice ends with its tests, a live comparison with D&D Beyond, the
changelog and the docs.

## Questions for the owner

All answered on 2026-09-27; see "Owner decisions" at the top. The plan is
complete (slices 1–10).

1. **Rules text** (the PHB paragraphs D&D Beyond shows in most panes): include
   it? It would come from 5etools' 2014 PHB book data (a new ingestion); it
   can't be hand-written.
2. **Sidebar controls** (lock, overlay / fixed, left / right): build all four,
   or only hide?
3. **HP:** keep our "manual adjustment" (a documented deviation), or switch to
   D&D Beyond's Max HP Modifier + Override Max HP?
4. **Do defenses have an effect?** Should hand-added (and granted) resistances
   halve damage in the damage panel, or stay display only as today?

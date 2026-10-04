# UI design system

What is shared across every game system. Read this before building any sheet UI.
For one system's concrete composition, read `systems/<system-id>/sheet-ui.md`.

## The sheet shell

Every supported system renders inside the same shell, in the same vertical order:

1. **Identity header** — portrait, name, system-specific summary line, action buttons
2. **Vitals zone** — the numbers a player looks at mid-roll; composed per system
3. **Tabbed detail section** — tab set defined per system
4. **Dice tray** — anchored bottom left, persistent, dice model per system

Plus one **contextual sidebar**, anchored right.

A new system composes zones 2, 3 and 4 and supplies a theme. It does not redesign the
shell. If a system seems to need a different shell, raise it — do not fork the shell.

## The contextual sidebar

One surface, not many. Rules:

- Opening a panel while another is open **replaces** the content. There is no stack,
  no back button, no history.
- **Controls at the top: hide and lock** (owner decision, 2026-09-27; no move to
  left or right, no fixed / overlay modes). They use D&D Beyond's icons
  (`sheet/sidebarIcons.tsx`).
  - Hidden, the sidebar shrinks to a 25px strip with only the show control, and
    showing it brings back the last panel. The sheet opens with it hidden; with
    no panel yet it shows "Select elements on the character sheet to display
    more information about".
  - Unlocked, Escape and a click outside hide it. Locked, it stays open and the
    hide control disappears. Opening another panel still replaces the content.
  - The lock is remembered per viewer (`localStorage`).
- **Frame:** D&D Beyond's border SVG at the top and bottom, a 10px gap, and 3px
  side borders, all in the sheet theme's `--theme-accent`. It has a light
  (`#FEFEFE`) and a dark (`#12181C`, character panel) variant. Measurements
  are in `systems/dnd-5e/features/sidebar-fidelity-and-customization.md`.
- **Shared panel parts** (`sheet/sidebarParts.tsx`):
  - `SidebarHeader`: an optional clickable parent line, a 37px preview, and the
    heading with a signed value;
  - `Customize`: the themed collapsible;
  - `EditorBox` and `ValueEditorRow`: a value and "Enter Source Notes...",
    saved on blur;
  - `RulesText` (`sheet/RulesText.tsx`): the rules text closing a pane (or,
    `placement="intro"`, above its controls), drawn like D&D Beyond's
    `ddbc-html-content`. The D&D 5e texts are hand-written in
    `systems/dnd5e/rulesText.ts` (`systems/dnd-5e/features/rules-text-slots.md`).
  Every panel is built from these.
- The sidebar is the only place that explains a number. The sheet shows values; the
  sidebar shows where they came from.

### The molds

Every panel is one of these. A new panel that fits none of them is a signal to stop
and discuss, not to invent one more. Each starts with `SidebarHeader` and follows
D&D Beyond's own pane for the same trigger; the measurements per pane are in
`systems/dnd-5e/features/sidebar-fidelity-and-customization.md` (slices 1–9).

| Mold | Purpose | Structure |
| --- | --- | --- |
| System pane (`pane` kind) | A game system's own pane drawn from the live sheet | D&D 5e: ability, proficiency bonus, speed, initiative, armor class, saving throws (and one save), senses, all skills (and one skill or custom skill), defenses, proficiencies & training. Header, values, Customize, rules text. |
| Entity detail | One item, spell, action, feature or extra | Optional parent line (a link with `onOpenParent`), icon, name with a pencil, subtitle; the action bar on top (a spell's Cast), under the header with a separator (`actionsPosition: 'section'`: a feature action's Limited Use, an extra's Hit Points) or last (default); Customize (its label can be "Edit"); properties; description or a caller-built body; tags. |
| Management | Add, remove and organize a collection | Manage Spells (Spell Slots, per class Known / Prepared Spells with filter and level chips), Manage Feats (grouped by source, Add / Remove), Manage Inventory (Add Items with type chips, Add Custom Item, My Inventory per location), Manage Custom Actions (groups by template, a select that creates "Custom Action N", each opening its Entity detail with Edit). |
| Text field | Edit free-text fields | One field: a growing textarea, then SUGGESTIONS with D&D Beyond's table. Several: labeled inputs, selects for fixed choices. |
| Mechanic | Execute something | Header, intro rules text, caller-built body (the rests: RECOVER, hit dice box rows, Take / Reset), closing rules text after a rule. |
| Log | The roll history | Chat bubbles, newest at the bottom and expanded, the rest collapsed until clicked. |
| Conditions | Toggle the fixed condition list | Active effects, then rows with icon, name and switch; Exhaustion with its level bar. |
| HP Management | Edit and heal / damage hit points | Current / Max / Temp, Healing and Damage with the New HP and Apply / Cancel, then Max HP Modifier and Override Max HP. |
| Character panel | Who the character is | Dark panel: portrait, name with pencil, species and level or XP bar, classes, Change Sheet Appearance, My Character / Play menus. Opens Manage XP and Change Sheet Appearance. |

The Collection editor mold was retired on 2026-09-28; the Explainer became the
system panes.

## The roll affordance

**Any element that triggers a roll changes background color on hover. Nothing else
does.** This is the only reliable signal, since rollable and non-rollable elements
share shapes — a saving throw circle rolls, a passive sense circle does not.

Rollable elements must also be keyboard-focusable and expose an accessible label
describing the roll.

**Direct owner request, 2026-08-17:** a second, narrower class — `.roll--modifier`
— marks the subset of roll targets that roll a flat modifier and nothing else
(saving throws, ability scores, skill bonuses), always alongside `.roll` rather
than replacing it. Ported directly from D&D Beyond's own CSS for its equivalent
buttons (`frames.css`'s own `--border-control`/`--text-on-control`/
`--radius-control` tokens already carry the exact hex values it uses). Roll
targets with their own richer per-row styling — attacks, spell rolls, initiative
— stay on plain `.roll`, not this modifier.

## The mutate affordance

Elements that change and persist character state — hit point damage/heal/temporary
buttons, heroic inspiration, a condition toggle — use a **border** highlight on hover
and focus, never the roll background. Sharing the roll's background would blur the
one signal the roll affordance depends on. Like roll targets, mutate targets are
keyboard-focusable with an accessible label.

**Direct owner request, 2026-08-17:** neither roll nor mutate targets get a
press/"pushed down" effect on `:active` — D&D Beyond has no such feedback on
its own equivalent controls, and this app dropped its `translateY(1px)`
`:active` effect to match.

## Derived values carry their provenance

No derived value is rendered as a bare number. Every one arrives from the API with the
list of contributions that produced it, each with a label and a source.

Armor class is not `18`; it is 14 from scale mail, +1 from dexterity capped at 2, +1
from a fighting style, +2 from a shield. Wisdom is not `18`; it is a base score plus
named bonuses.

This is a contract with the backend, not a UI nicety: it is what makes a wrong
calculation diagnosable from the screen instead of from a debugger.

## Shared primitives

The full visual vocabulary. Systems restyle these; they do not replace them.

| Primitive | Used for |
| --- | --- |
| Dot rating | Filled or empty dots for a trait level |
| Circle rating | Same, hollow circles |
| Stat badge | A framed number with a label |
| Box track | N boxes, marked or unmarked, with an optional recharge label |
| Numeric pool | A number with increment and decrement, with a recharge label |
| Panel | A titled, framed container with an optional settings affordance |
| Tab bar | The detail section's tabs |
| Filter chip row | Mutually exclusive or additive filters above a list |
| Search field | Free-text filter over a list |
| Entity row | One line in a list, clickable into the entity detail mold |
| Derivation trace | Label, value and source, stacked |
| Portrait | Character or creature image |
| Source select | A single pick from catalogue entries: `builder/SourceSelect.tsx` (see below) |

### Source select

Every builder choice (`ChoiceField`) and the class picker use `SourceSelect`,
not a native `<select>` or a row of chip buttons. Spells keep their own
`SpellPicker`. A choice with `count > 1` uses its multiple mode:
- the popup stays open while picking;
- the trigger lists the picks and an `n/max` counter (blue, green when full);
- a status line reads "n of max chosen";
- once full, unpicked options are `aria-disabled` and dimmed, and clicking a
  picked one removes it.

Lists where no option has a book (skills, tools, abilities) show no group
headings and no code badges. In multiple mode, "Clear" sits in the status line,
never as a list row, so rows don't shift under the pointer while picking.

**Picks show at once.** Choices on screen come from the saved plan. The builder
overlays the local build's answers (`BuilderDefinition.answerOf`,
`withLocalAnswers`), so a pick is marked immediately instead of after the
autosave and the plan round trip.

**Book order:** `BuilderDefinition.leadingSources` go first (D&D 5e: the
Player's Handbook). The rest follow, largest group first.

**Progress checklist** (`BuilderChecklist`): one line per step, and under a step
with several kinds of choice, one line per kind (e.g. Classes → Class,
Subclass, Ability increases & feats, Proficiencies, Spells). Each line shows ✓
or "n left".
- It never lists individual picks or the values chosen. The step pages show
  those.
- The kinds come from `BuilderDefinition.checklistGroupOf` / `checklistGroupRank`.
- The column grows with the page instead of scrolling; only the summary column
  is sticky.

**Portrait picker** (`PortraitPicker`, Basics): the current portrait, Upload
image, "Pick a default portrait" and Remove.
- "Pick a default portrait" opens a separate section with the system's preset
  grid; picking one closes it.
- `CharacterPortrait` renders a portrait anywhere (summary, sheet header,
  character list) in the caller's placeholder box.

**Starting equipment** (`Dnd5eEquipmentStep`) doesn't use selects for its own
decisions:
- equipment or gold is a `Segmented`;
- each A-or-B pick is one checkbox-style line per option, and the unpicked line
  dims;
- only item picks ("a martial weapon") use a `SourceSelect`, inline under the
  chosen line, one per unit.

**ASI or feat** (`Dnd5eAsiOrFeatField`): a segmented "Ability Score Improvement |
Feat" first.
- **ASI:** a second segmented, "+2 to one ability | +1 to two abilities", then a
  single or a multiple select. +2 is stored as the ability twice.
- **Feat:** a feat select, with the feat's own picks nested under it.

Background on the component: The native one can't style its options,
lay them out in two columns, or show a tooltip. The owner chose this design on
2026-09-25 (variant A of `design-reference/mockups/d2-builder/source-select/`):
- **Names:** Roboto Condensed, uppercase, tracked `.04em`, 14px. The selected
  entry is bold with a ✓.
- **Book code** on the right: condensed 11px/700 in a `--surface` badge. It turns
  `--builder-chip-on` (the theme accent) with white text on the highlighted row.
- **Full book name** in a tooltip on hover (tap on touch screens). The tooltip
  is portalled to `<body>` with fixed positioning, so the scrolling list never
  clips it, and it flips below the code near the top of the viewport.
- **Grouping:** options are grouped under a book heading. Leading books come
  first (see below), then the largest group; entries with no book go last
  under "Other".
- **Filter box** above 8 options. It matches the name, the book name or the
  exact book code.
- **Keyboard:** arrows, Home/End, Enter, Esc. Type-to-jump works when there is
  no filter box.
- **Accessibility:** combobox + listbox roles. Each option's accessible name
  includes its book ("Aasimar (Dungeon Master's Guide)").

Codes come from `GET /api/catalogue/sources` (`sourceCode`), loaded once per
system by `SourceCodesProvider` (`catalogue/SourceCodes.tsx`). The frontend
never derives an abbreviation itself.

## Theming

A system supplies tokens only: color ramp, typography, frame ornaments, background
treatment. Tokens never change layout, spacing rules or component behavior.

Background art chosen by the player is **character data**, not a theme token.

The page backdrop is a system's background treatment: the sheet shell and the
builder take a `pageClassName` from the system. D&D 5e uses `parchment-page`
(a textured off-white paper; sections keep white paper over it).

### App theme (landing, home, shell)

`apps/web/src/theme/theme.css` defines the app's own tokens on `:root`. This is
direction B of `design-reference/mockups/home`, chosen on 2026-09-25:
- parchment `--app-bg #F3F3F1`, white surfaces, charcoal header `#1F2327`;
- gold accent `--app-accent #D9A441`, with charcoal text on it;
- Roboto Condensed uppercase headings.

**Dark theme** (direction A, "Arcane Vault"):
- `[data-app-theme='dark']` redefines the same variables: background
  `#15191C`, surfaces `#1F2529` and `#273036`, ink `#E8ECEF`, lines
  `#34404A`, header `#0E1113`. The gold accent stays.
- `AppPage` (`theme/AppPage.tsx`) is the root of every page drawn with the
  app theme: landing, home, credits, and "Creating your character…". It sets
  the attribute. The sheet and the builder don't use it and keep their own
  tokens.
- `AppThemeProvider` (`theme/appTheme.tsx`) holds the choice:
  - the "Dark theme" switch in the user menu (`menuitemcheckbox`) sets it;
  - it's kept in `localStorage` (`osv.app-theme`), a per-browser
    convenience;
  - until the player picks, it follows `prefers-color-scheme`.
- The landing is signed out and has no user menu, so it always follows the
  system (or a choice made earlier in that browser).

The Keycloak login theme repeats these values in
`infra/keycloak-themes/omni-sheet-vault/login/resources/css/omni-sheet-vault.css`.

**App header** (`apps/web/src/shell/AppHeader.tsx`), on every page, full width
and at least 96 px tall: the 48 px logo and the name (a link to `/`) in the
left corner, and the player's menu (or "Log in" when signed out) in the right
corner.
- On the builder, the system picker and the sheet it is `compact` (58 px, 32 px
  logo). It adds a "← Your characters" link at the right edge of the page's
  centred column (`backToListWithin`): the builder's 1440 px column, or the
  sheet's measured column.
- The builder puts its "Draft" badge and save state in the header's `children`.
- On a character's pages (builder, sheet, level up) it takes the `systemId`
  and shows that system's mark after the app's name, behind a thin divider.
  Each system styles its own mark (`shell/systemMarks.ts` → the system's
  component): D&D 5e is the ampersand logo (a PNG used as a CSS mask) and
  "Dungeons & Dragons" in Modesto Bold Condensed (not bundled: commercial, so
  it falls back to the heading font where it isn't installed), both in the
  muted brand red `--dnd5e-brand-red-muted` (`#C53131`, the sheet's DDB Red), at 15 px so it stays smaller than the app's name;
  "5th edition · 2014" muted, centred on the name.
- **Rule (owner, 2026-10-04):** the bar is the same for every system: its
  black background, the app's logo and name, their font and size, the back
  link and the user pill. A system only adds its mark after the divider
  (logo, name and edition in its own theme), never restyles the bar. The
  mark stays smaller than the app's name. Below 1280 px the edition hides; below 640 px the whole mark does.
- On the sheet it sits above the character header, the way D&D Beyond's site
  bar sits above its own. The sheet header no longer carries a back button or
  the user name.

**App footer** (`apps/web/src/shell/AppFooter.tsx`), on every page (landing,
home, create-character, system picker, builder, sheet, credits): the Wizards of
the Coast fan content notice, a one-line summary of the content and art
sources, and a link to `/credits`. Its styles live in `theme/theme.css`
(`.app-footer`). On an `.app-page` it rests at the bottom of short pages.

**Credits page** (`apps/web/src/shell/CreditsPage.tsx`, public route
`/credits`): mirrors `NOTICE.md`. When a credit changes, update both.

**Home and landing** (`apps/web/src/home/`):
- **Landing:** hero, "What you can do" and "Pick your game".
- **Home:** "Start something new", a filter of the systems that have characters
  (`?system=`), and banners grouped by system.
- **Banner:** portrait, name, then the facts the backend composed
  (`CharacterResponse.summary`, the first one emphasised). It has a 5 px left
  bar in the system's colour (`supportedSystems.ts`, D&D 5e `#C2412F`), and
  drafts carry a "Draft · continue creating" badge.
- **Systems:** those not supported yet show only a "Coming soon" badge.
- **Deleting** always asks first (`ConfirmDialog`).

## Redactable content

Rules text sourced from published books — spell, feature, item and trait descriptions
— is **redactable**. When redaction is enabled, the API omits the text and the client
renders a placeholder in its place. Everything else keeps working: the spell is still
castable, the feature still tracks its uses, the item still contributes to armor
class. Only the prose disappears.

Redaction is a server-side switch, configured per environment. **It is not a CSS
blur**: text sent to the browser is retrievable regardless of how it is styled, so
hiding it client-side would be decoration, not behavior.

Implementation rules:

- Redactable text is a distinct field type in the API contract, not a special case
  per endpoint. A redacted field arrives as a marker, never as text.
- **The client never receives the prose it hides.** There is no blur filter applied to
  real text — the component renders a placeholder because there is nothing to render.
  An implementation that styles received text is wrong, however similar it looks.
- The placeholder reads as deliberate, not broken: **gray skeleton blocks of varied
  width**, plus a short label saying the description is unavailable in this
  environment. Blocks are chosen over a blurred-text look because they need no fake
  text to exist and cannot be mistaken for a rendering fault.

  *Recorded alternative, not currently implemented:* a blurred-text appearance. It
  would need the backend to send the original character count so the placeholder
  matches the real length. Rejected for now because it reads as a broken render to
  anyone who does not know what it is. Revisit only if the block treatment looks wrong
  in practice — and note the backend contract changes if so.
- Name, mechanical values, tags and source attribution are never redacted — only
  descriptive prose.
- This applies to every game system, not just one.

## Frontend registry

Mirrors the backend strategy registry. A system module provides its theme, sheet
composition, dice tray and character-list card, registered under the same system
identifier the backend uses.

Adding a system means adding a module. It must not require editing the shell, the
sidebar, the character list, or another system's module.

## Rules that do not bend

- No game rules in the frontend. If the UI needs a modifier, the API returns it.
- The character list is fully generic: it renders a portrait, a name and a summary
  line that the backend composed. It knows no system by name.
- Every list of entities supports search and filtering through the shared primitives.

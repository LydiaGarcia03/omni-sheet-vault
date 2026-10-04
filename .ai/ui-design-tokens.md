# UI design tokens

The resolved values behind the interface. Read this before writing any CSS.

Fidelity comes from measured values, not from estimating against a screenshot. An
agent told "make it look like the reference" approximates twenty times and lands
somewhere else; an agent given `81px` applies `81px`.

## Where values actually live

`apps/web/reference/frame-kit.html` is the **reference implementation** — a working,
hand-tuned build of every framed component. Its numbers were dialled in against the
live reference interface and are authoritative for anything they cover.

This document records the values that are global or shared. Anything about where
content sits inside a specific frame's artwork lives in the kit, because those numbers
only mean something next to the art they were tuned against.

**Open the kit before building a component. Port it; do not re-derive it.**

Reference screenshots are in `design-reference/screenshots/`, which is not version
controlled. Open the relevant one when building or reviewing a component; if it is
missing, say so rather than working from memory.

---

## Global tokens · resolved

### Typography

| Token | Value |
| --- | --- |
| `--font-body` | `Roboto, Helvetica, sans-serif` |
| `--font-condensed` | `'Roboto Condensed', Roboto, Helvetica, sans-serif` — the dominant UI font on D&D Beyond's own sheet (tab names, filter chips, row names/labels, headings); plain `--font-body` is reserved for a handful of large numeric displays |
| `--font-ui-native` | `system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif` — roll-target boxes only, matching D&D Beyond's own native-OS-font treatment for its dice buttons specifically |
| `--line-height-ratio` | `1.4` |

**Both font families were declared from day one but never actually loaded** —
found 2026-09-03 chasing an owner report that the sheet's fonts "look
strange": `document.body`'s computed `font-family` was literally the
browser's absolute default, "Times New Roman", because nothing had ever set
`font-family` on `body` (every component's own `font-family: inherit` had
nothing real to inherit) and no `<link>`/`@font-face` loaded either webfont.
Fixed with a `body { font-family: var(--font-body); }` rule (`frames.css`)
plus a Google Fonts `<link>` in `index.html` for both Roboto and Roboto
Condensed. This affects the whole sheet, not one tab — most visible text
outside the Actions tab was never audited against this baseline and may be
worth a fresh look now that the actual typeface renders correctly.

Line height is a ratio, not a set of pixel values. Every measured pair confirmed it:
13/18.2, 16/22.4, 26/36.4.

| Role | Size | Weight |
| --- | --- | --- |
| Ability label | `7.5px` | 700 |
| Small caption, panel category | `8px` | 700 |
| Panel entry | `9px` | 400 |
| Panel title | `11px` | 700 |
| Saving throw abbreviation | `11px` | 700 |
| Saving throw modifier | `13px` | 600 |
| Ability score | `16px` | 700 |
| Initiative value | `18px` | 600 |
| Stat badge value | `22px` | 600 |
| Armour class value | `24px` | 700 |
| Ability modifier | `26px` | 500 |

Uppercase labels are tracked between `.06em` and `.1em`. The ability label is the
exception at `.01em`, because CONSTITUTION and INTELLIGENCE must fit inside the frame's
shoulders.

**The modifier is the primary value, not the score.** 26px against 16px. The number
used at the table gets the emphasis — keep that hierarchy.

### Colour

| Token | Value |
| --- | --- |
| `--text-primary` | `#242528` |
| `--text-on-control` | `#394B59` |
| `--text-muted` | `#6B7A85` |
| `--border-control` | `#BFCCD6` |
| `--surface-page` | `#F9F9F9` |

### Theme

| Token | Value | Purpose |
| --- | --- | --- |
| `--theme-accent` | `#C53131` (DDB Red) | The D&D 5e theme color, in `systems/dnd5e/themes.css` |
| `--theme-accent-dark` | accent 79% + black (`#9C2727`) | Filled theme button on hover (Cast) |
| `--theme-accent-darker` | accent 58.5% + black (`#731D1D`) | Inset shadow of a hovered filled theme button |
| `--theme-accent-strong` | `var(--theme-accent)`; `#2B69AB` under Cleric Silver | Solid fills (Roll, remaining slots) through `--accent-control-saturated` |

**Per-character themes:**
- `themes.css` holds one `:root[data-sheet-theme='<id>']` block per D&D
  Beyond theme. Each sets `--theme-accent`, and Cleric Silver also sets
  `--theme-accent-strong`.
- `useSheetTheme` sets the attribute on the root element while a sheet is
  open, and clears it on leaving, so the builder is always DDB Red.
- The list of theme ids lives in `systems/dnd5e/sheetThemes.ts` for the web,
  and in `Dnd5eAppearance.THEMES` for the API.
| `--frame-ink` | `var(--theme-accent)` | Frame outline |
| `--frame-paper` | `#FFFFFF` | Fill behind the frame |
| `--ink-control` | `#4A5D6B` | Dark control ink that D&D Beyond does not theme: the condition toggle, spell and attunement markers, hover borders of the HP buttons. Proficiency dots (saves and skills) are always `#383838`, under every theme |

`--theme-accent` is the single color a theme changes. D&D Beyond applies its
theme color to:
- frames;
- the active tab underline and the active filter chip;
- subsection headings and links;
- outlined and filled theme buttons;
- the portrait border;
- the header buttons and their icons;
- the temp HP box;
- use and slot marks;
- the dice button.

The sheet maps each of these to `--theme-accent`, directly or through
`--frame-ink` and `--accent-control`. The builder's `--builder-ink`,
`--builder-chip-on` and `--builder-pending` use it as well
(`systems/dnd-5e/features/leveling-and-appearance.md`).

### Shape and interaction

| Token | Value |
| --- | --- |
| `--radius-control` | `4px` |
| Control border | `1px solid var(--border-control)` |
| `--roll-bg-default` | `transparent` |
| `--roll-bg-hover` | `rgba(57, 75, 89, 0.10)` |
| Hover transition | `background-color 120ms ease` |
| `--accent-control` | `var(--theme-accent)` — active filter chip fill, active tab underline, headings, links, outlined buttons |
| `--accent-control-hover` | `#525C63` — inactive filter chip on hover; the same under every theme (measured on DDB Red and Cleric Silver). Outlined "Manage" buttons have no hover color change |
| `--accent-control-saturated` | `var(--theme-accent)` — solid button fill (Roll, remaining-slot badge). A light theme such as Cleric Silver needs a darker value here (`#2B69AB`, the same hue at 60%/42%), because its accent is too low-contrast to read as a filled button |
| `--surface-control` | `#ECEDEE` — inactive filter chip fill (also its hover text color) |
| `--text-control-muted` | `#75838B` — inactive filter chip text (also an active chip's hover fill) |
| `--border-divider` | `#EAEAEA` — subsection-heading and between-row dividers (much lighter than `--border-control`, which stays reserved for solid control outlines) |

The four accent/surface tokens above were DOM-measured 2026-09-03 against D&D
Beyond's own Actions/Spells/Features filter chips and its tab bar's active
underline — one recurring blue-gray accent shared by both controls, not the
pill-shaped, outline-only guess `FilterChips.tsx`/`TabBar.tsx` originally shipped
with. `--border-divider` was measured the same day against the Attacks section's
own heading divider and its between-row dividers. See `systems/dnd-5e/sheet-build.md`'s
"Tab bar"/"Filter chip row"/"Attack row"/"Attacks section heading" rows for the
full corrected value lists (padding, font-size, gap, hover behavior).

---

## Component metrics · resolved

Outer dimensions. Content placement inside each frame is in the kit.

| Component | Size |
| --- | --- |
| Ability box | `81 x 95` |
| Armour class | `74 x 84` |
| Stat badge | `74 x 74` |
| Initiative | `78 x 52` |
| Saving throw row | `132 x 34` |
| Panel | `278` wide, height follows content |

| Spacing | Value |
| --- | --- |
| Between ability boxes | `5px` |
| Between saving throw rows | `6px` |

**The stat badge is one component.** Proficiency bonus and speed share its frame,
layout and type scale.

**Panels come in two frames.** Ornate for framed groups, plain for text-heavy content
like proficiencies. Each has its own padding, since the plain frame's border is much
thinner.

**Ornate panel content padding is `13px 20px`**, shared by Saving Throws, Senses,
Proficiencies, Skills and the tabbed section — a flat pixel value, not proportional to
box size, matching D&D Beyond's own equivalent boxes despite their own widths ranging
278–623px.

### Character header (`SheetShell.tsx`)

DOM-measured against D&D Beyond's own character header.

| Property | Value |
| --- | --- |
| Background | `#232B2F` |
| Button border | `1px solid var(--accent-control)` |
| Button radius | `3px` |
| Button padding | `5px 13px 4px` |
| Button icon | `16 x 16` |
| Button label | `13px / 700 / uppercase`, white |

---

## Frame assets

**Phase 10 asset refactor:** one dedicated SVG per section, replacing the earlier CSS
`mask-image` div-pair technique (a masked div shows nothing inspectable in DevTools —
just a colored rectangle, not the artwork). `FrameLayer.tsx` renders the real, imported
SVG markup (`import x from '...svg?raw'`) for two stacked layers, paper and ink;
`SectionPanel.tsx` is the generic wrapper every section's own component supplies its
own `?raw`-imported asset to. Each SVG is stretched to fill its box exactly
(`forceStretch`, `svgUtils.ts`), same as the old mask's `mask-size: 100% 100%`
behaviour — every consumer's own CSS dimensions were already tuned for that.

Assets live in `apps/web/src/systems/dnd5e/frames/` (`.svg`, imported with `?raw`), one
file per section — e.g. `dnd_frame_saving_throws.svg`, `dnd_frame_hit_points.svg`. A
few components legitimately share one asset because every instance is the same size
and shape (the six ability boxes all use `dnd_frame.svg`) — that is reuse, not the old
stretch-and-compromise pattern. `apps/web/public/frames/` (`.png`) is the pre-refactor
location and should no longer gain new files.

Icons (`dnd_icon_*.svg`, e.g. rest buttons, the inspiration dot, the unchecked-circle
proficiency marker) live in the same folder, rendered by `FrameIcon.tsx` instead of
`FrameLayer.tsx` — no paper/ink layering, just the raw SVG.

---

## Still unmeasured

Blockers, not suggestions. Ask before inventing one.

- Sheet grid: gutter estimated at 16px (reusing the vertical panel-gap value) and
  every column at the standard 278px panel width, from
  `design-reference/screenshots/dnd-character-sheet`, not measured live — see
  `Dnd5eVitalsColumns.tsx`. The top row's internal spacing is still unmeasured.
- The tab bar's own strip has no dedicated frame (it sits inside the already-framed
  `Dnd5eTabbedSection` box, `dnd_frame_actions.svg`), the sidebar, and the dice tray —
  all three are still plain bordered boxes, no frame asset built for any of them.
  Hit points and heroic inspiration are no longer on this list — both got dedicated
  frames in the phase 10 asset refactor (`dnd_frame_hit_points.svg`,
  `dnd_frame_inspiration.svg`).
- The label's exact colour — currently inheriting `--text-primary`

Measure these in the browser and tune them in the kit, which carries a slider panel
for exactly this. Promote anything global back into this document.

---

## Capturing a value

Select the element in the inspector and run:

```js
const s = getComputedStyle($0);
console.log(JSON.stringify(Object.fromEntries(
  ['width','height','padding','margin','background-color','background-image',
   'border','border-radius','box-shadow','position','display','gap',
   'font-family','font-size','font-weight','line-height','letter-spacing',
   'text-transform','color'].map(p => [p, s.getPropertyValue(p)])
), null, 2));
```

Keep the browser at 100% zoom. Measure in layers — a component is a container plus its
inner parts, and the parts carry the typography. For interaction states, use the
inspector's force-state control; the delta between default and hover is the affordance
token, and it cannot be recovered from a screenshot.
# D&D 5e — building the sheet screen

How to implement the sheet. What it must *do* is in `features/character-sheet.md`;
what it must *contain* is in `rulesets/dnd-5e-sheet-ui.md`; the shared vocabulary is in
`ui-design-system.md`. This document is about turning those into code.

## Start here: the reference implementation

`apps/web/reference/frame-kit.html` is a working, tuned, static implementation of the
sheet's framed components. **Open it before writing any component.** It is not a
mockup — it is the pattern to port.

It establishes, already solved:

- how ornamental frames are applied and recoloured
- the alignment values for every framed component, tuned by hand against the reference
- how roll targets differ from reveal targets
- how framed components nest without breaking

Do not reinvent any of that. Port it.

Frame assets live in `apps/web/public/frames/`.

### Non-negotiables carried over from the kit

**Frames are CSS masks, never `<img>`.** Two stacked layers — a paper layer filled with
`--frame-paper` and an ink layer filled with `--frame-ink` — both masked by the same
asset. This is what makes one black PNG serve every colour and state.

**Frame layers are selected with the child combinator.** `.panel > .frame-box__ink`,
never `.panel .frame-box__ink`. With a descendant selector, a framed component placed
inside another framed component inherits the outer mask. This bug already happened once
with saving-throw rows inside a panel.

**Every clickable is a `<button>`.** Both roll targets and reveal targets. Keyboard
focus and assistive semantics come free; a div with a click handler loses both.

**Only roll targets change background on hover.** That is the single affordance
distinguishing "this rolls dice" from "this opens the sidebar", since the two often
share a shape.

**Content position is per-component custom properties.** Each component declares its
own `--*` variables for placing content inside its artwork. Adjust those; never edit
layout rules to fix alignment.

## Component inventory

| Component | Frame asset | Status |
| --- | --- | --- |
| Ability box | `dnd_frame.png` | Tuned in the kit |
| Armour class | `dnd_frame_armor.png` | Tuned in the kit |
| Saving throw row | `dnd_frame_modifier.png` | Tuned in the kit |
| Initiative | `dnd_frame_initiative.png` | Tuned in the kit |
| Stat badge | `dnd_frame_box.png` | Tuned — serves proficiency bonus *and* speed |
| Panel, ornate | `dnd_frame_features.png` | Tuned in the kit |
| Panel, plain | `dnd_frame_proficiencies.png` | Tuned — used for text-heavy panels |
| Hit points block | — | Not built |
| Heroic inspiration | — | Not built. Boolean, not a counter |
| Skill row | — | Not built |
| Senses panel | plain panel | Not built |
| Defenses / conditions | — | Not built |
| Tab bar | — | Not built |
| Sidebar | — | Not built |
| Dice tray | — | Not built |

A component marked "not built" still follows the kit's patterns. If it needs a frame
that does not exist yet, stop and ask — do not approximate one in CSS.

## Screen layout

```
┌─────────────────────────────────────────────────────────────────┐
│ HEADER  portrait · name · species/class/level · action buttons  │
├─────────────────────────────────────────────────────────────────┤
│ ability ×6 │ proficiency │ speed │ inspiration │ hit points     │
├──────────────────┬────────────────────┬─────────────────────────┤
│ saving throws    │                    │ initiative │ armour     │
│ ─────────────    │      skills        ├─────────────────────────┤
│ senses           │                    │ defenses  │ conditions  │
│ ─────────────    │                    ├─────────────────────────┤
│ proficiencies    │                    │ tabbed detail section   │
└──────────────────┴────────────────────┴─────────────────────────┘
```

Three columns below the top row. Left is a stack of panels, centre is the skills panel,
right holds combat stats above the tabbed section.

The exact column widths, gaps and the top row's spacing are **not yet measured**. Take
them from `ui-design-tokens.md` once recorded; do not invent them. The left column has
been measured at roughly 278px — the panel width already used in the kit.

## Build order

Vertical slices, each ending in something visible and reviewable.

1. **Static shell.** The grid above, with placeholder blocks. Confirms the layout
   before any component detail exists.
2. **Top row.** Port the ability box and stat badge from the kit; build hit points and
   inspiration. Six abilities plus four boxes.
3. **Left column.** Saving throw rows inside a panel; senses and proficiencies as plain
   panels. This is where nesting is exercised — watch the child combinator.
4. **Skills.** The longest list on the sheet and the most reused row pattern.
5. **Combat group.** Initiative, armour class, defenses, conditions.
6. **Tab bar with one tab.** Actions only. Proves the tab pattern.
7. **Sidebar shell with one mold.** The explainer, wired to ability scores. Proves the
   panel-replacement behaviour.
8. **Remaining tabs and molds.**

Do not build tabs before the vitals zone is right. The vitals zone establishes every
pattern the tabs reuse.

## Data, not presentation

The sheet renders what the API returns. It does not compute.

Derived values arrive with their contributions — armour class is never the number 18,
it is 18 plus the list that produced it. The sheet shows the number; the sidebar shows
the breakdown. A component that calculates a modifier from a score has put game rules
in the frontend, which `ground-rules.md` forbids.

Description prose may arrive redacted. Render the placeholder treatment; everything
else on the component still works.

## Definition of done for a component

- Ported from the kit's pattern, or following it if new
- Content positioned through its own custom properties
- Roll targets are buttons with the hover affordance; reveal targets are buttons
  without it
- Compared side by side against its reference screenshot at the same zoom
- Values taken from `ui-design-tokens.md`, with anything still estimated flagged in the
  summary rather than silently invented
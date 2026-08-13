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
| `--line-height-ratio` | `1.4` |

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
| `--frame-ink` | `#4A5D6B` | Frame outline |
| `--frame-paper` | `#FFFFFF` | Fill behind the frame |

These two restyle every frame at once. That is the whole theming mechanism — a system
supplies its own pair and its own artwork, and nothing else changes.

### Shape and interaction

| Token | Value |
| --- | --- |
| `--radius-control` | `4px` |
| Control border | `1px solid var(--border-control)` |
| `--roll-bg-default` | `transparent` |
| `--roll-bg-hover` | `rgba(57, 75, 89, 0.10)` |
| Hover transition | `background-color 120ms ease` |

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

---

## Frame assets

Every frame is applied as a CSS mask, never as an `<img>`. Two stacked layers, paper
and ink, share one mask; colour comes from the two theme variables. One black asset
serves every colour and state.

Three asset requirements. All three break the component without breaking the code, and
all three are checkable before testing:

1. **Transparent interior, opaque strokes.** The mask reads the alpha channel, so an
   opaque white interior renders as a solid filled block. This has already happened
   once.
2. **Cropped tight to the drawing.** Empty canvas becomes padding and shrinks the
   frame inside its box.
3. **Exported at 3x.** These components are small; thin strokes render soft at nominal
   size.

Assets live in `apps/web/public/frames/`.

---

## Still unmeasured

Blockers, not suggestions. Ask before inventing one.

- Sheet grid: column widths, gutters, the top row's internal spacing
- Hit points block, heroic inspiration, skill row, tab bar, sidebar, dice tray
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
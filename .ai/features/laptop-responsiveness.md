# Laptop responsiveness

Make the existing screens work on slightly smaller screens (laptops), and keep
every future system working there from its first slice.

Status: **done** (2026-10-11). Owner approved scaling for the D&D sheet and a
1280px minimum. R3 follows the owner's choice: scale only while the sidebar is
locked, floor 0.85.

## Targets

| Width | Typical device |
| --- | --- |
| 1280 x 720 | Small laptop; the **minimum supported width** |
| 1366 x 768 | The most common budget laptop |
| 1440 x 900 | 14" laptops |
| 1536 x 864 | 1920 x 1080 at 125% Windows scaling |

- Height matters as much as width: 720–768px leaves about 600px under the app
  header.
- Below 1280px nothing has to look designed; it only has to stay usable. No
  horizontal page scroll is acceptable at 1280px or above.
- Phones and tablets are out of scope.

## Approach per screen

| Screen | Approach |
| --- | --- |
| Character list (home) | Fluid grid. Check the breakpoints in `home.css` / `theme.css` at the target widths |
| D&D builder | `builder-layout` side columns narrow at 1280–1440. Under ~1200 the summary column drops below the step |
| VtM builder | Same treatment as the D&D builder for its flex layout |
| D&D sheet | **Proportional scaling.** The sheet is a pixel replica of D&D Beyond with px-sized SVG frames, so it isn't reflowed. The whole sheet scales with CSS `zoom` to the width left beside the sidebar, floored at 0.85 |
| VtM sheet | Real reflow: its columns drop to fewer columns below its 1220px design width |

### D&D sheet scaling, details

- A small hook (`useFitScale(designWidth, floor)`) measures the available width
  with a `ResizeObserver` and sets `zoom` on the sheet root. CSS alone can't
  divide a length by a length, so a pure-CSS `clamp()` isn't possible.
- The contextual sidebar, the dice tray and the app header stay **outside** the
  zoomed element: they keep their real size.
- Things to re-verify under zoom (they use `getBoundingClientRect` or fixed
  positioning): `usePopupDirection` popups, `CheckListSelect`, the creation
  tutorial's spotlight, context menus, and the tooltips.
- At 1280 with the sidebar open, the sheet sits at about 0.85. If text below
  that becomes unreadable, the fallback is the sidebar overlaying the sheet
  instead of pushing it. Decide this in R3 from screenshots.
- The D&D Beyond fidelity comparison is still done at 100% (≥1440 wide).

## R0 findings (2026-10-11)

Measured in same-origin iframes sized to each target and checked for overflow.
That gives exact viewport sizes without resizing the browser window.

| Screen | 1280 x 720 | Notes |
| --- | --- | --- |
| Home | OK | The system cards row overflows by design: it's a carousel with an arrow |
| D&D builder | OK, no page overflow | The middle column is 595px (770 at 1536). On the Classes step, the spell tables scroll sideways (704 in 497) |
| VtM builder | **Broken**: the right column and header were cut off, with no scrollbar | Fixed in R2 (below) |
| D&D sheet, sidebar closed | OK | The sheet is 1191px wide and fits |
| D&D sheet, sidebar open | **Covers the combat column** | See the table below |
| VtM sheet, panel closed | OK | |
| VtM sheet, panel open | Covers the right of the tabs (~330px) | The panel overlays; the sheet can reflow beside it |
| Height 720 | Pages scroll | The D&D tabbed section is a fixed 672px starting at y = 359, so its list scrolls inside a page that also scrolls |

D&D sheet with the sidebar open (an absolute overlay, 340px):

| Viewport | Sheet covered by | Scale needed to fit beside it |
| --- | --- | --- |
| 1280 | 303px | 0.76 |
| 1366 | 260px | 0.83 |
| 1440 | 223px | 0.89 |
| 1536 | 175px | 0.97 |
| 1920 | fits | 1 |

At 1280 and 1366, the approved 0.85 floor doesn't remove the overlap. The
owner decided (2026-10-11):
- the sheet scales only while the sidebar is locked;
- an unlocked sidebar overlays, as on D&D Beyond;
- the floor stays 0.85, and the leftover overlap at 1280–1366 is accepted.

## Slices

| Slice | Content | Status |
| --- | --- | --- |
| R0 | Measure: screenshot every screen at the four target sizes, with the sidebar closed and open, and list what breaks | done |
| R1 | Shell and home: app header, character list, global breakpoints in `theme.css` | done (no change needed) |
| R2 | Builders: D&D and VtM layouts at 1280–1440 and under 1200 | done |
| R3 | D&D sheet: `useFitScale` with zoom, sidebar outside it, popups re-verified | done |
| R4 | VtM sheet: reflow below 1220 | done |
| R5 | Height: scroll caps on middle-column frames follow `100vh` instead of fixed px, checked at 720 and 768 high | done (already viewport-based) |

### What was built

- **D&D builder spell tables:** they follow their own width through a
  container query on `.builder-spells__list`, not the viewport's.
  - 720px or less: Effect is hidden.
  - 560px or less: Range is hidden too.
  - 440px or less: Casting time goes as well.
  - They no longer scroll sideways at any width; before, they did even at
    1920.
- **D&D sheet (R3):**
  - `SheetShell` takes `reserveRight` and `overlay`.
  - With the sidebar locked and shown, the screen reserves 352px (340 + a
    12px gap) as right padding. The column then scales with `zoom` to the
    remaining width, from 1 down to 0.85, in 0.01 steps.
  - The sidebar, the dice tray, the custom roll picker and the roll mode menu
    render through `overlay`, outside the zoomed column, at real size.
  - `Sidebar` reports its lock through `onLockedChange`.
  - Measured with the sidebar locked:

    | Viewport | Zoom | Overlap |
    | --- | --- | --- |
    | 1280 | 0.85 | 111px |
    | 1366 | 0.85 | 25px |
    | 1440 | 0.88 | 0 |
    | 1536 | 0.96 | 0 |
    | 1920 | 1 | 0 |

  - The roll mode menu still opens at the click point under zoom. The
    builder-only popups (`usePopupDirection`) are never inside the zoomed
    column.
- **VtM sheet (R4):**
  - It gets `side-open` while its detail panel is open, which keeps 360px
    free on the right.
  - The sheet is a size container (`vtm5-sheet`). At 1060px or less, the
    vitals drop under the attributes, and the main grid becomes two equal
    columns with the tabs spanning below them.
  - With the panel open, nothing is covered at 1280, 1366, 1536 or 1920.
- **Height (R5):**
  - The builder frames already cap at `100vh - 96px`.
  - The D&D tabbed section (672px) fits a 720px viewport with page scroll.
  - The VtM panel and dock are viewport-anchored.

VtM builder fix:
- `.vtm5-page { min-width: fit-content }` made the page as wide as its 1500px
  layout, and the global `overflow-x: hidden` then cut it off. That rule is
  removed.
- The step column shrinks from its natural width (`flex: 0 1 auto`).
- At 1500px and below, the progress column narrows to 260px and the
  explanation column to 300px.
- At 1536 and wider nothing changed (317 / 796 / 315).

Each slice ends with screenshots at 1280 x 720 and 1366 x 768, plus a check
that 1920 x 1080 didn't change.

## Rule for future systems

Written into `ui-design-system.md` ("Screen sizes") and `ground-rules.md`
(Frontend):

- **Daggerheart, and any later system:** its sheet and builder must work at 1280
  x 720 from the first slice. They should not be retrofitted later.
- **Fluid sheet:** prefer a layout that reflows.
- **Pixel replica of an external reference (like D&D):** it scales with
  `useFitScale`.

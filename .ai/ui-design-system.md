# UI design system

What is shared across every game system. Read this before building any sheet UI.
For one system's concrete composition, read `rulesets/<system>-sheet-ui.md`.

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
- Controls at the top: hide, lock, and move to left or right edge. The D&D Beyond
  "fixed" and "overlay" modes are not implemented.
- The sidebar is the only place that explains a number. The sheet shows values; the
  sidebar shows where they came from.

### The five molds

Every panel is one of these. A new panel that fits none of them is a signal to stop
and discuss, not to invent a sixth.

| Mold | Purpose | Structure |
| --- | --- | --- |
| Explainer | Justify a derived value | Value, derivation trace, rules text |
| Entity detail | Show one item, spell, action or feature | Icon, name, source line, action bar, metadata, description, tags |
| Collection editor | Add and remove from a set | Current entries with remove, plus a searchable picker |
| Mechanic | Execute something | Rules summary, controls, confirm button |
| Log | Chronological record | Reverse-chronological entries |

The entity detail mold is one parameterized component. Only its action bar varies:
equip and move for items, cast with a level stepper for spells, limited-use boxes for
features.

## The roll affordance

**Any element that triggers a roll changes background color on hover. Nothing else
does.** This is the only reliable signal, since rollable and non-rollable elements
share shapes — a saving throw circle rolls, a passive sense circle does not.

Rollable elements must also be keyboard-focusable and expose an accessible label
describing the roll.

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

## Theming

A system supplies tokens only: color ramp, typography, frame ornaments, background
treatment. Tokens never change layout, spacing rules or component behavior.

Background art chosen by the player is **character data**, not a theme token.

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
# Rules text slots (D&D 5e sidebar panes)

Where to write the explanatory text that closes the sidebar panes, as D&D Beyond
does. The owner writes it by hand; the code only draws it.

## The one file to edit

**`apps/web/src/systems/dnd5e/rulesText.ts`**

Every text lives there, as a template string between backticks. An empty string
(two backticks with nothing between them) shows nothing, so a pane without text
looks finished.

No other file needs to change to add or edit text.

## Which constant feeds which pane

Each pane was checked on D&D Beyond (`characters/171527207`, 2026-09-28). The
last column counts D&D Beyond's paragraphs, only as a size reference.

| Constant | Pane | How to open it on our sheet | D&D Beyond |
| --- | --- | --- | --- |
| `ABILITY_RULES_TEXT.strength` … `.charisma` | Ability pane, one text per ability | Click an ability box's name or score | 12 paragraphs (Strength) |
| `PROFICIENCY_BONUS_RULES_TEXT` | Proficiency Bonus | Click the Proficiency box | 5 paragraphs |
| `SPEED_RULES_TEXT` | Speed | Click the Speed box | 8 paragraphs |
| `INITIATIVE_RULES_TEXT` | Initiative | Click the Initiative box | 5 paragraphs |
| `ARMOR_CLASS_RULES_TEXT` | Armor Class | Click the Armor Class shield | 1 block |
| `SAVING_THROWS_RULES_TEXT` | Saving Throws **and** each single save (the same text in both, as on D&D Beyond) | The gear on "Saving Throws", or a save's abbreviation | 6 paragraphs |
| `SENSES_RULES_TEXT` | Senses | The gear on "Senses" | 9 paragraphs; ours holds a placeholder summary written in our own words, replace it freely |
| `SKILL_RULES_TEXT.acrobatics` … `.survival` | A single skill, one text per skill | Click a skill's name | 1 paragraph (Acrobatics) |
| `SHORT_REST_INTRO_TEXT` | Short Rest, **above** the controls | The Short Rest button | intro paragraph; ours holds our own summary |
| `SHORT_REST_RULES_TEXT` | Short Rest, at the end | The Short Rest button | benefits and rules |
| `LONG_REST_INTRO_TEXT` | Long Rest, **above** the controls | The Long Rest button | intro paragraphs; ours holds our own summary |
| `LONG_REST_RULES_TEXT` | Long Rest, at the end | The Long Rest button | "Benefits of the Rest.", "Interrupting the Rest." with bullets |
| `CONDITION_RULES_TEXT.blinded` … `.unconscious` | Conditions pane: a condition's rules, under its row | Open Conditions, then the chevron at the right of the row (it appears once the text exists) | 3 paragraphs, a rule, a heading and 2 bullets (Blinded) |
| `EXHAUSTION_RULES_TEXT` | Conditions pane: Exhaustion's rules, under its row | Same, on the Exhaustion row | — |
| `DEATH_SAVES_RULES_TEXT` | HP Management, at the end, under the subheading "Death Saving Throws Rules" | Only while the character is dying (0 HP, not stable): click the Hit Points / Death Saves box | 6 paragraphs, with a rule |

Checked on D&D Beyond and **without** explanatory text, so no slot: HP
Management (while not dying), Defenses, Proficiencies & Training, All Skills,
custom skills, Heroic Inspiration (a click only toggles it, no pane), the
character menu, Change Sheet Appearance. Items, spells and features already
show their own descriptions from 5etools.

Not checked yet: Manage XP (the test character has no XP to manage), the
inventory panes (Manage Inventory, coins, encumbrance), Manage Spells / Feats,
Manage Custom, the game log. They are looked at in slices 9e–9g; any text found
there gets a slot and a row here.

## Format

Write inside the backticks:

```ts
export const INITIATIVE_RULES_TEXT = `First paragraph.

Second paragraph, with **bold words** and *italic words*.
A single line break stays a line break.

- a bullet
- another **bullet**`;
```

The text is Markdown, read line by line:
- A **blank line** starts a new paragraph; a **single line break** stays a line
  break inside the paragraph.
- `#` to `######` start a heading; the paragraph can follow on the very next
  line. Every level is drawn the same (D&D Beyond's condition heading); `#`
  and `##` both become `<h2>`, since the pane's own title is the `<h1>`.
- Lines starting with `- `, `* ` or `+ ` make a bullet list; `1. ` a numbered
  list.
- A line holding only `---` (or `***`) is a horizontal rule.
- Pipe tables (`| a | b |`, then `|---|---|`, then rows). A cell holding only
  `checkbox` draws a checkbox, display only for now (Exhaustion's "Applied"
  column; slice 9c ties it to the exhaustion level).
- `***text***` is bold italic, `**text**` bold (D&D Beyond's lead-ins, e.g.
  "At Higher Levels."), `*text*` italic.
- A line holding only `blockquote` opens D&D Beyond's boxed aside (the PHB
  sidebars such as "Hiding"), and a line holding only `fim-blockquote` closes
  it. Everything between them is Markdown as above.
- Don't use a backtick (`` ` ``) inside the text, and write `\${` if the text
  ever needs a literal `${`.
- UI copy is American English (CLAUDE.md's language policy).

## Styling (already done)

`RulesText` (`apps/web/src/sheet/RulesText.tsx`) draws the text; its CSS is
`.rules-text*` in `apps/web/src/systems/dnd5e/frames.css`. Measured on D&D
Beyond's `ddbc-html-content`:
- closing text: 10px margin and 10px padding above, a 1px `#EAEAEA` top border;
- Roboto 13px / 18.2px, `#12181C`;
- paragraphs 15px apart; lists 13px above and below, 30px indent; bold is 700;
- headings Roboto Condensed 700: `#`–`###` 15.21px / 21.294px with 15.21px
  above and below; `####` 14px / 19.6px with 18.62px; `#####` and `######`
  14px / 19.6px with 23.38px;
- the boxed quote: `#FAFAFB`, 4px `rgba(138,146,153,.45)` bands above and
  below, 1px `#F4F2F2` sides, 12px 14px 2px padding, 8px 2px 4px margin;
  rules 6.5px above and below, 1px gray;
- the intro variant (rests) has no border and 10px below it;
- the plain variant (an expanded condition, 10px padding; the death saves text
  under its subheading) has no spacing of its own.

## After writing

Nothing to rebuild on the backend. With `npm run dev` running, the sheet updates
as soon as the file is saved. `npm test` still passes: the tests use their own
texts.

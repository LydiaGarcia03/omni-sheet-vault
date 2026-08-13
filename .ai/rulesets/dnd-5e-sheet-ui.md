# D&D 5e — character sheet UI

Concrete composition for the `dnd-5e` system. Read `ui-design-system.md` first: the
shell, the sidebar molds, the roll affordance and the shared primitives are defined
there and are not repeated here.

**Visual target: extreme fidelity to D&D Beyond's character sheet.** Where this
document departs from it, the departure is listed explicitly in "Deviations" below.
Anything not listed there should match.

Only the 2014 fifth edition is supported. The system is presented to the player as
"D&D 5e". There is no 2024 content and therefore no "legacy" marker anywhere.

## Identity header

Portrait, character name, species and class line with levels, character level.
Clicking the portrait or the name opens the character panel in the sidebar.

Buttons: share, short rest, long rest, game log, edit character.

- "Find a group" is not implemented — this application manages sheets, not sessions.
- The game log button carries a visible label, using the space freed by the removal
  above. In D&D Beyond it is an unlabelled icon.

## Vitals zone

**Top row.** Six ability boxes, each with modifier and score; proficiency bonus;
walking speed; heroic inspiration; hit points block with current, maximum and
temporary, plus heal and damage inputs.

Each ability box has two click targets: the score opens the explainer, the modifier
rolls. Heroic inspiration is a boolean — the character is inspired or is not.

**Left column.** Saving throws panel, passive senses panel, proficiencies and
training panel. Each has a settings affordance opening its collection editor or
explainer.

**Center column.** Skills list: proficiency marker, governing ability, name, bonus.
Clicking the name opens that skill's explainer; clicking the bonus rolls.

**Right column.** Initiative, armor class, defenses, conditions.

Conditions can be toggled and are displayed on the sheet. **In the first version they
have no effect on rolls or derived values.**

## Tabs

Six tabs: actions, spells, inventory, features and traits, background and notes,
extras.

D&D Beyond has seven — background and notes are merged here into one.

### Actions

Filter chips: all, attack, action, bonus action, reaction, other, limited use.

Attack rows show name, range, hit or DC, damage, notes. The hit and damage values are
roll triggers. Below the attacks come the standard combat actions, then class
features that grant actions, grouped by action type. Features with limited uses carry
a box track and a recharge label.

Every row opens the entity detail mold.

### Spells

Header shows spellcasting modifier, spell attack bonus and save DC **per spellcasting
class** — a multiclass character has one set per class. These three are display only.

Filter chips by base spell level, plus search. Filtering uses the spell's own level,
not the level it might be cast at.

Spells are grouped by level, each group showing its slot track. Concentration and
ritual are marked on the row. Casting from the detail panel allows choosing a slot
level; the slot consumed is the chosen level, not the spell's base level.

"Manage spells" opens a collection editor with known spells and prepared spells,
split per class, showing cantrips known and preparation limits, and marking spells
that are always prepared.

**Managing prepared spells belongs on the sheet, not in character creation** —
several classes change preparations at every rest.

### Inventory

Item rows show equipped state, name, quantity, cost, notes. Equipped state is not
cosmetic: an equipped weapon appears in the actions tab, and equipped armor or a
shield contributes to armor class through the derivation trace.

Attunement section with three slots.

Coin management panel: totals per denomination, plus add and remove.

### Features and traits

Filter chips: all, class features, species traits, feats. Entries are grouped by
class and by species, each showing its source, description, and — where applicable —
its limited-use track and recharge label. "Manage feats" opens a collection editor.

### Background and notes

Merged tab. Background feature and characteristics — alignment, personality traits,
ideals, bonds, flaws, appearance — plus free-text notes for organizations, allies,
enemies, backstory and other. Editing happens in the sidebar.

### Extras

Familiars, mounts, summoned creatures and vehicles linked to the character. A row
shows name, armor class, hit points and speed; opening it shows the full stat block
in the sidebar, with editable hit points for that instance.

## Sidebar panels

| Trigger | Mold |
| --- | --- |
| Ability score, proficiency bonus, speed, armour class, initiative, defenses, a skill name, saving throws, senses | Explainer |
| Any item, spell, action, feature, extra | Entity detail |
| Proficiencies and training, manage spells, manage feats, manage inventory, manage extras, coins | Collection editor |
| Short rest, long rest | Mechanic |
| Game log | Log |
| Portrait or name | Character panel |

## Deviations from D&D Beyond

Deliberate. Do not "fix" these back toward the original.

- **No containers.** The backpack and container model is removed; every item lives in
  one flat equipment list.
- **No weight or encumbrance.**
- **Packs and kits stay whole.** An adventuring pack or a tool kit is a single item
  whose contents are listed in its detail panel. D&D Beyond explodes them into loose
  items, which hides what the character actually owns.
- **Tools describe their uses.** A tool's detail panel documents the actions it
  enables and their DCs.
- **Background and notes are one tab.**
- **No "find a group".**
- **Game log button is labeled.**
- **No coin settings.** Lifestyle and expenses are cosmetic and may be omitted.
- **No dice skins.**
- **No 2024 content and no legacy markers.**
- **No value overrides in the first version** — the "customize" collapsible is absent
  from every panel.

## Deferred to later versions

- Conditions affecting rolls and derived values
- Value overrides
- Player-selectable background art
- 3D dice rendering. The dice tray ships with a result log first; the renderer sits
  behind an interface so a 3D one can replace it without touching the sheet.

## Content catalogue

The catalogue covers the **full published 2014 fifth edition**, not only the SRD. See
`decisions/adr-0005-content-catalogue-and-redaction.md` for that decision and its
consequences.

Descriptive prose from the books is redactable, per `ui-design-system.md`. Redaction
is enabled in the public deployment and disabled locally. Spell mechanics, feature
tracking and item effects are unaffected either way — only descriptions are withheld.
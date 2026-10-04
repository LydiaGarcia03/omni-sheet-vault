# D&D 5e — character sheet UI

Concrete composition for the `dnd-5e` system. Read `ui-design-system.md` first: the
shell, the sidebar molds, the roll affordance and the shared primitives are defined
there and are not repeated here.

**Visual target: extreme fidelity to D&D Beyond's character sheet.** Where this
document departs from it, the departure is listed explicitly in "Deviations" below.
Anything not listed there should match.

Only the 2014 fifth edition is supported. The system is presented to the player as
"D&D 5e". There is no 2024 content and therefore no "legacy" marker anywhere.

## Verifying fidelity against D&D Beyond

The live reference is a real D&D Beyond character sheet:
**https://www.dndbeyond.com/characters/50149479**. It is the source of truth for
this document, not the other way around — when this document is silent or unclear
on a visual or functional detail, check the live page rather than guessing.

**Additional reference characters (owner-supplied, 2026-08-16)**, for broader
verification across a wider variety of D&D 5e builds than #50149479 alone covers
(different class/subclass mixes, spellcasting shapes, inventory sizes) — useful
for a general re-check pass, not just single-feature slices:
- https://www.dndbeyond.com/characters/56008841
- https://www.dndbeyond.com/characters/48929054
- https://www.dndbeyond.com/characters/48959782

**At the end of any frontend change to the character sheet**, compare the result
side by side against that page: layout, spacing, colors, typography, every hover
state, every button, every tab, every sidebar mold, and the underlying
functionality (what a click does, what opens where, what a control's states are).
If it doesn't look and behave the same, treat it as not done yet — not as
"close enough."

- This applies to hovers, buttons, screens, functionality and visual design alike.
  Not just static appearance — click through the same tabs and controls on both
  sides and compare the behavior, not only a screenshot.
- **"Anything not listed [in Deviations] should match"** is the standing default —
  the deviations list exists precisely so exceptions are explicit and reviewed, not
  silently assumed. Do not add a new deviation on your own judgment: if a detail
  seems like it should differ from D&D Beyond, confirm with the owner before
  building it that way, and if confirmed, add it to "Deviations" so it stays a
  documented exception rather than a silent one.
- When unsure how a feature behaves on D&D Beyond (what a control does, what a
  panel shows, how a list filters), it is fine to open the live page and interact
  with it directly — click tabs, open panels, trigger hovers — to understand the
  structure and dynamics before building the equivalent here.
- When unsure whether a comparison is the right one to make, or whether some
  action on the live site is safe or appropriate to perform, ask the owner first
  rather than guessing or proceeding.
- See `feedback-dndbeyond-fidelity-verification` in memory for the same rule as it
  was first established: measure the live DOM (`getBoundingClientRect()`, computed
  styles) rather than eyeballing a screenshot, whenever a measurement is needed.

## Identity header

Portrait, character name, species and class line with levels, character level.
Clicking the portrait or the name opens the character panel in the sidebar.

Buttons: share, short rest, long rest, game log, edit character.

- "Find a group" is not implemented — this application manages sheets, not sessions.
- The game log button carries a visible label, using the space freed by the removal
  above. In D&D Beyond it is an unlabelled icon.
- Short rest and long rest are built (phase 9): each opens the Mechanic mold in the
  sidebar rather than acting immediately, confirmed live that D&D Beyond does the
  same. Their buttons are labelled text too, same reasoning as the game log button.
  Share and edit character remain unbuilt — neither has a feature behind it yet.

## Vitals zone

**Top row.** Six ability boxes, each with modifier and score; proficiency bonus;
walking speed; heroic inspiration; hit points block with current, maximum and
temporary, plus heal and damage inputs. Clicking anywhere inside the hit points
block except its numbers and its own inline controls opens the sidebar's HP
Management mold (`ui-design-system.md`'s ninth) — confirmed live against D&D
Beyond, which opens the same kind of panel from its own Hit Points box. The panel
ends with D&D Beyond's Max HP Modifier and Override Max HP (the `hitPoints`
customizations); a customized maximum shows in the panel in green or red, with the
calculated one beside it in parentheses. The top-row box shows the plain number.

Each ability box has two click targets: the score opens the explainer, the modifier
rolls. Heroic inspiration is a boolean — the character is inspired or is not.

**Left column.** Saving throws panel, passive senses panel, proficiencies and
training panel. Each has a settings affordance opening its collection editor or
explainer.

**Center column.** Skills list: proficiency marker, governing ability, name, bonus.
Clicking the name opens that skill's explainer; clicking the bonus rolls.

**Right column.** Initiative, armor class, defenses, conditions.

Conditions can be toggled and are displayed on the sheet. **In the first version they
have no effect on rolls or derived values.** Toggling happens in the sidebar's
Conditions mold (`ui-design-system.md`'s eighth, phase 10 slice 4), not inline —
confirmed live against D&D Beyond, which opens the same sidebar panel from "Add
Active Conditions." The Defenses/Conditions panel's own Conditions column shows a
compact trigger instead of the fourteen-item checklist: "Add Active Conditions" when
none are active, or a comma-joined summary of the active ones.

## Tabs

Six tabs: actions, spells, inventory, features and traits, background and notes,
extras.

D&D Beyond has seven — background and notes are merged here into one.

### Actions

Filter chips: all, attack, action, bonus action, reaction, other, limited use.

The Attacks section carries its own heading, distinct from every other section on
this tab: "Actions • Attacks per Action: `N`" (`N` is `VitalsZone.attacksPerAction`,
1 + the highest Extra Attack modifier, since C3a) with a "Manage Custom" control on the
same line —
every section's own "Manage Custom" opens the same shared sidebar panel with the
character's complete list of player-authored custom actions, grouped by template
(General/Spell/Weapon), confirmed live to match D&D Beyond's own real behavior
(punch list item 7, `systems/dnd-5e/references/sheet-fidelity-audit.md`). A `displayAsAttack` custom
action that resolves to a to-hit roll renders here, in this same table, exactly
like a weapon row; one that doesn't (a save-based effect, or `displayAsAttack`
unset) renders instead as its own row in whichever of this tab's four sections its
activation type buckets into. Attack rows show a leading icon (selected from the attack's
`category`, e.g. "Ranged Weapon"/"Melee Weapon" — free text, not an enum, rendered
as a subtitle under the name too), name, range, hit or DC, damage, notes. The hit
and damage values are roll triggers. **2026-09-03, direct owner request:** any
spell that deals damage or heals also renders in this same table, after the
weapon rows — an italic name with a "Level • Class" subtitle in place of the
category one, a school-of-magic icon in place of the weapon-category one (the
only place a school icon appears — the Spells tab uses a different leading
column, see below), and a heal roll in the Damage column for a healing spell
instead of a damage one. A weapon or natural attack whose `category` names it
as an unarmed attack (e.g. Unarmed Strike) always sorts last in this table,
after both other weapons and any spell rows — confirmed live against every
spellcasting reference character. Below the attacks come "Actions in Combat" — a
flowing, comma-separated line of standard action names, each opening the entity
detail mold with its description on click (no inline description, unlike
Features and Traits' own summary-plus-sidebar split) — then class features that
grant actions, grouped by action type. Features with limited uses carry a box
track and a recharge label.

Every row opens the entity detail mold.

### Spells

Header shows spellcasting modifier, spell attack bonus and save DC **per distinct
value across the spellcasting classes**, as D&D Beyond's `generateSpellCasterInfo`
does (owner decision, 2026-09-24).
- A Bard/Paladin with the same CHA shows one "+4", with both classes in its
  tooltip; classes with different values show one value each.
- Non-class casters (a species' granted spells) are left out, unless they are
  the only casters.

These three are display only.

Search (with "Manage Spells") and level filtering each get their own row, never
sharing one — search sits directly below the header, the level filter row below
that. Filter chips are keyed by the spell's own level, not the level it might be
cast at. Concentration and Ritual are icon-only chips in that same level-filter
row, not a separate control. The search row's own filter (funnel) button opens
an inline advanced-filter panel that replaces the level-filter row and the spell
list — never a sidebar — with checkbox-style chip groups for whichever
dimensions this app tracks real data for (Casting Time, Saving Throw, Damage
Type; Tags/Conditions/Attack Type are out until those fields exist).

Spells are grouped by level, each group showing its slot track. Concentration and
ritual are marked on the row. The whole row opens the Entity Detail panel (not
just the name or Cast button) — the Cast button and the inline roll targets stop
propagation so they still fire their own action first. Casting from the detail
panel allows choosing a slot level; the slot consumed is the chosen level, not
the spell's base level. **2026-09-17, direct owner request:** unlike D&D Beyond's
own Cast button (DOM-confirmed live: it only logs which level was cast at, never
rolls anything, and its damage/healing preview is inert), this app's Cast also
rolls — the spell's attack roll if it has one, else its damage/healing roll,
already scaled to the chosen level via the spell's own upcasting data. See
`SpellCast.tsx`'s own doc comment for the full design and `changelog.md`'s same-day
entry for the verification. Every spell the character has — attack, healing and
utility alike — is listed here, whether or not it also appears in the Actions
tab (see above). **2026-09-03, direct owner correction:** the row's leading
column is a Cast/At Will/As Ritual indicator, not a school icon — a cantrip
shows a plain "At Will" label, a ritual spell shows "As Ritual", everything else
gets a real "Cast" button that opens the same slot-level picker as the row's own
detail panel.

A spell's save DC **ability label** (e.g. "DEX" on Acid Splash) is a fixed
property of the spell itself — the ability its target rolls against — never the
caster's own spellcasting ability, which only sets the DC's numeric value. The
two are unrelated: a Fighter/Eldritch Knight's spellcasting ability is
Intelligence, but Acid Splash is always a Dexterity save regardless of who casts
it. The detail panel shows Level/School as its own italic line under the spell's
name (D&D Beyond's own `.ct-spell-detail__level-school` — "Evocation Cantrip"
for a cantrip, school first; "1st Level Evocation" otherwise, level first),
then a stacked properties list — Casting Time, Range, Components (V/S/M,
material text as a hover tooltip), Duration and, when the spell has one,
Attack or Save — each its own row (label, then value), not a wrapped
label:value paragraph. This is the shared Entity Detail mold's own metadata
rendering (`EntityDetailPanel.tsx`), matching D&D Beyond's `InfoItem`/
`sidebarSeparator` pattern used across its spell/action/item detail panels
alike, not a spell-specific layout.

"Manage spells" opens the spell management mold (not the Collection editor — see
`ui-design-system.md`'s "The six molds") with known spells and prepared spells,
split per class, showing cantrips known and preparation limits, and marking spells
that are always prepared. Each class carries a casting type: `KNOWN` (Ranger,
Sorcerer, Bard, Eldritch Knight — every learned spell is always usable, no separate
preparation step) or `PREPARED` (Cleric, Wizard — preparing/unpreparing a known
leveled spell within the class's own limit; cantrips are learned individually
under both types, never prepared). Confirmed live against D&D Beyond across all
four classes before building. Learning a new spell picks from a seeded catalogue
subset (`content/dnd-5e/spells/`), not the full official class spell list — a
deliberate phase 9 scope boundary, not a gap: the full official list needs a much
larger content-authoring effort the owner has not yet committed to.

**Managing prepared spells belongs on the sheet, not in character creation** —
several classes change preparations at every rest.

### Inventory

Item rows show equipped state, name, quantity, cost, notes. Equipped state is not
cosmetic: an equipped weapon appears in the actions tab, and equipped armor or a
shield contributes to armor class through the derivation trace.

Not every item can be attuned: `requiresAttunement` is set once when an item is
added (a checkbox on the add-item form) and is never toggled later — only
flagged items are ever attunable. Attunement itself is not a per-row checkbox
next to equip in the main item list; it lives in its own section below the
list, styled to the same `inventory-section-heading` tier as Equipment/
Backpack/Bag of Holding/Other Possessions — confirmed live against D&D Beyond
(dndbeyond.com/characters/50149479's own "Attunement" filter): "Attuned
Items" on the left (three fixed slots, each showing a generic equipment
glyph rather than the item's own portrait art, which this app has no catalog
identity for) and "Items Requiring Attunement" on the right — **every** item
flagged `requiresAttunement`, whether or not it is currently attuned, not
just the still-eligible ones (D&D Beyond lists an already-attuned item on
the right too, its slot picker just reflects the attuned state). The
right-hand list's own row is a checkbox before the name — the same
`MarkBox`/equip-flag control and name styling the main item list's own rows
use — matching D&D Beyond's real slot-manager checkbox exactly, unlike the
left-hand slots (still a plain display, no interaction). An item's own
Entity Detail panel (opened from its row in the main list, not from the
Attunement section) carries the rest, DOM-matched to D&D Beyond's own item
sidebar section by section: a header icon (`ItemPreviewIcon.tsx`) colored by
`itemKind` alone, independent of rarity — red for WEAPON, blue for
ARMOR/SHIELD, green for anything else (GEAR, the same catch-all D&D Beyond's
own `potion.jpg` fallback covers) — then Weight/Cost/Source properties (Source
only for a catalogued item; a freeform one has none to show), then the
description, then the action bar *last*: Equip/Unequip, Attune/Unattune (only
for a `requiresAttunement` item, in place of the quantity stepper a
non-attunable item shows instead), Move (a `buttonWithMenu`-style popover
of the other storage locations) and Delete.

Other Possessions shows its usual grid only once it holds at least one item;
while empty it shows a plain prompt instead — "+ Add other possessions,
treasure, or holdings for your character in this section." — confirmed live
against D&D Beyond's own empty-state text for the same section. Clicking it
opens a sidebar text field to name a new item, added to that storage
location on Enter/blur; D&D Beyond's own equivalent panel is one freeform
textarea rather than an item-adding field, since it has no per-item weight
tracking to speak of there — this app models Other Possessions as real items
(weight/encumbrance initiative), so the field creates one discrete item per
entry instead of appending to a paragraph.

Coin management panel: totals per denomination, plus add and remove.

### Features and traits

Filter chips: all, class features, species traits, feats. Entries are grouped by
class and by species, each showing its source, description, and — where applicable —
its limited-use track and recharge label. "Manage feats" opens a real catalogue
picker over 5etools' own feat list (`FeatManagementPanel.tsx`, 2026-09-21) — not
the Collection editor mold, same "a plain free-text field can't express a
catalogue picker" reasoning "Manage spells" already established.

### Background and notes

Merged tab. Background feature (read-only — choosing a different background is a
phase 11 concern) and characteristics — alignment, personality traits, ideals,
bonds, flaws, appearance, plus ten more (gender, eyes, size, height, faith, hair,
skin, age, weight, lifestyle) confirmed live against D&D Beyond — plus free-text
notes for organizations, allies, enemies, backstory and other. Editing happens in
the sidebar, via the Text Field mold (`ui-design-system.md`'s seventh) —
confirmed live to be three sub-patterns sharing that one mold: alignment plus the
other characteristics share one combined panel; personality traits, ideals,
bonds and flaws each open individually with a roll-a-suggestion table sourced
from the character's own background (a seeded catalogue subset — see
`content/dnd-5e/backgrounds/`, currently just Soldier); appearance and every
notes field each open individually with no suggestions.

### Extras

Familiars, mounts, summoned creatures and vehicles linked to the character. A row
shows name, armor class, hit points and speed; opening it shows the full stat block
in the sidebar, with editable hit points for that instance and a Delete button —
D&D Beyond's own Extras sidebar has no action beyond hit points and removal, so
that's the entire action bar here too.

## Sidebar panels

| Trigger | Mold |
| --- | --- |
| Ability score, proficiency bonus, speed, initiative, armour class, defenses, saving throws (gear or one save), senses (gear), skills (gear: All Skills; one skill or custom skill) | System pane, with Customize where D&D Beyond has one |
| Proficiencies and training (gear) | System pane (`Dnd5eProficienciesPane`; hand-added entries are the `proficiencies` customizations) |
| Any item, spell, feature action (parent link to its feature), feature, custom action (Edit), extra | Entity detail |
| Manage Spells, Manage Feats, Manage Inventory, Manage Custom | Management |
| Characteristics (Alignment, Faith, Lifestyle), Appearance details, a single Background/Notes field (Personality Traits, Ideals, Bonds, Flaws, Appearance, Organizations, Allies, Enemies, Backstory, Other) | Text field |
| Short rest, long rest | Mechanic |
| Game log | Log |
| The Conditions column's trigger ("Add Active Conditions" or a summary) | Conditions |
| Hit points box | HP Management |
| Portrait or name | Character panel (then Manage XP, Change Sheet Appearance, Level up) |
| Coins | Their own panel |

## Deviations from D&D Beyond

Deliberate. Do not "fix" these back toward the original.

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
- ~~No value overrides in the first version~~ — **reversed 2026-09-27** by the owner:
  D&D Beyond's "Customize" sections are being built
  (`systems/dnd-5e/features/sidebar-fidelity-and-customization.md`).
- **The sidebar has only hide and lock** (owner decision, 2026-09-27). D&D Beyond's
  overlay / fixed and left / right controls are omitted.
- **Manual dice roll picker caps each die type at 20.** D&D Beyond showed no limit
  live (reached 32 with no cap hit) — 20 is this app's own DoS-safe ceiling, not a
  measured value.
- **No "Clear Dice" control on the manual roll picker.** D&D Beyond has one alongside
  Reset, but clicking it live (with dice already selected) didn't change the
  selection — its actual purpose wasn't confirmed (likely tied to the 3D dice history,
  which this app doesn't have yet), so only Reset was built.
- **The advantage/disadvantage popover has no "SEND TO: Everyone/Self" section.**
  D&D Beyond's own right-click menu carries one above "ROLL WITH" — omitted, this
  app has no chat/whisper concept to send a roll to. Its selected-row checkmark
  icon is a plain highlight here instead of a fourth extracted icon.
- **A save-based `displayAsAttack` custom action never folds into the Attack
  table.** D&D Beyond's own to-hit-or-DC display for a custom action wasn't fully
  confirmed live; this app's `AttackRow` shape has no DC slot at all (only a
  weapon-style to-hit), so only a custom action with a `stat` set (a real to-hit
  roll) folds in — one with only `saveType` set stays in its Actions-tab section
  list instead, a deliberate limitation rather than a guess at extending the
  shared attack-row shape from one unconfirmed case (see `Dnd5eCustomAction`'s
  own doc comment).
- **A custom action's "Snippet" field is captured but never displayed.**
  Confirmed live it doesn't render in D&D Beyond's own read-only sidebar summary;
  where else (if anywhere) it might show wasn't fully explored, so it stays
  stored-but-unsurfaced rather than a guess.
- **Characteristics and Appearance Details are two panels** (owner decision,
  2026-10-04). D&D Beyond joins them in one "Characteristics and Details"
  panel.
- **Sidebar panes leave out what the app has no data or concept for**
  (slices 9d–9g, each listed in
  `systems/dnd-5e/features/sidebar-fidelity-and-customization.md`):
  - an extra's source line, Customize and art; class art beside the
    character panel's classes;
  - the rest checkboxes ("Reset Maximum HP changes", "Automatically apply
    healing") and the 2024 "Long Rest Rules" choice;
  - the game log's dice-set preview, and pinning it to the pane bottom (our
    sidebar is taller than the window, so it opens scrolled to the newest);
  - the "Add ▾" destination menu, the Proficient / Common / Magical /
    Container checkboxes and source chips in Manage Inventory; inline item and
    feat details; the class portrait and slot editing in Manage Spells; the
    Settings button in Manage Feats;
  - Frame, Backdrop and Portrait browsing, the Underdark preference and the
    theme groups in Change Sheet Appearance.
- **No separate "Spend Hit Dice" control.** Dice are spent through the short
  rest's box rows, as on D&D Beyond.

## Deferred to later versions

- Conditions affecting rolls and derived values
- Player-selectable background art
- 3D dice rendering. The dice tray ships with a result log first; the renderer sits
  behind an interface so a 3D one can replace it without touching the sheet. Manual
  rolling (below) still renders through the same text-based tray, not a 3D animation.
- **Advanced item/spell filtering by type, rarity or tag.** D&D Beyond's funnel icon
  (`FilterSvg`, confirmed live 2026-09-10 next to the Spells and Inventory search
  boxes) opens this. This app's `SearchField.tsx` (Spells/Inventory/Extras tabs) is
  name-only. The catalogue side has it: Manage Inventory filters by type and
  Manage Spells by level (slice 9f).

## Content catalogue

The catalogue covers the **full published 2014 fifth edition**, not only the SRD. See
`decisions/adr-0005-content-catalogue-and-redaction.md` for that decision and its
consequences.

Descriptive prose from the books is redactable, per `ui-design-system.md`. Redaction
is enabled in the public deployment and disabled locally. Spell mechanics, feature
tracking and item effects are unaffected either way — only descriptions are withheld.
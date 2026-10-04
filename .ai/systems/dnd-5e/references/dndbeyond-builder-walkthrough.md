# D&D Beyond character builder — walkthrough and lessons for D2

Research input for Stage D2 of `systems/dnd-5e/features/character-creation.md` (the guided creation
flow, roadmap phase 11). Recorded 2026-09-24 from a live walk through
dndbeyond.com's builder (character-app 1.71.1), creating a character named "test"
from the listing page up to the "View Character Sheet" button.

The goal is **not** to clone this builder. It is to know what a player used to
D&D Beyond expects, and which of its patterns are worth keeping or avoiding.

Character built during the walk: Fighter 1 (Basic Rules 2014) · Folk Hero (5e
Core) · Mountain Dwarf (Legacy, PHB 2014) · Standard Array (15/13/14/8/10/12 base,
17/13/16/8/10/12 total) · starting equipment package, not gold.

---

## 1. Flow structure

| Step | URL segment | What it holds |
| --- | --- | --- |
| Listing | `/characters` | Cards (portrait, name, "Level \| Species \| Class"), View/Edit/Copy/Delete, search, sort, slot count, "Create a Character" |
| Method | `/characters/builder` | Standard / Quickbuilder (level 1 only) / Premade |
| Home | `builder/home/basic` | Name, portrait, **all** character preferences (see §2.1) |
| 1. Class | `builder/class/...` | Choose class → manage class (level, features, choices) |
| 2. Background | `builder/background/...` | Choose background → its proficiencies, feature, characteristics, details |
| 3. Species | `builder/species/...` | Choose species/subspecies → traits and trait choices |
| 4. Abilities | `builder/ability-scores/manage` | Generation method + score calculations |
| 5. Equipment | `builder/equipment/manage` | Starting equipment or gold, inventory, add items, currency |
| What's Next | `builder/whats-next` | View Character Sheet · Export to PDF · campaign links |

- **The character exists from the first click.** Choosing "Standard" creates it
  immediately (id in the URL). Every change autosaves; there is no final "Create".
- Navigation: a tab bar with every step always clickable (no forced order), plus
  large prev/next arrows either side of the content. The name and portrait header
  repeats on every step.
- Step order is **Class → Background → Species** (the 2024 PHB order), even for
  2014 content. Our D2 draft said species → class → background.
- Deep-linking to a step URL without going through the app redirected to Home.

## 2. Step by step

### 2.1 Home (preferences)

Name field with "Show Suggestions" (five names flavoured by the chosen species, plus
a shuffle button) and the portrait slot. Below them are all the preferences: source
toggles (Homebrew, 5.5e Core, 5.5e Expanded, Drops, 5e Core, 5e Expanded,
Legacy/Noncore, Partnered), dice rolling, optional features (Optional Class
Features, Customize Your Origin), advancement (milestone/XP), HP type
(fixed/rolled), prerequisites (feats, multiclass), level-scaled spells, encumbrance,
coin weight, ability display (modifier or score on top), privacy.

### 2.2 Class

- A searchable list grouped by source, with collapsible groups, each row showing
  an icon and the book.
- Picking a class opens a **"Confirm Add Class" modal**: hit die, primary ability,
  saves, and every feature by level. You commit only after you've seen what you get.
- After adding: level dropdown, remove button, HP summary with "Manage HP", then a
  feature accordion. A feature with an open choice shows a **blue "!" badge and "N
  Choices"**, and the badge clears when the choice is made.
- Skill picks are native selects. An option picked in one select disappears from
  the others.
- Fighting Style uses a searchable, source-grouped dropdown. The chosen option's
  description shows inline under it.
- "Available at Higher Levels (N)" previews future features. There is also "+ Add
  Another Class".

### 2.3 Background

- A searchable, source-grouped dropdown (with a Custom Background).
- On selection: description, skill/tool proficiencies, an inline tool choice, and
  the feature in an accordion.
- The d8 "Suggested Characteristics" tables have RANDOM and "+ADD" buttons to fill
  personality traits, ideals, bonds and flaws.
- Then Character Details, Physical Characteristics, Personal Characteristics and
  Notes: the backstory lives here, not on a separate step.

### 2.4 Species

- Source filter, a "Show Legacy Content" toggle, and search.
- Subspecies are grouped under a parent row ("DWARF (2)" → Hill / Mountain), each
  labelled "Legacy".
- A **"Confirm Species" modal** mirrors the class one: portrait art, description,
  trait list, a details-page link, and the traits accordion.
- The manage view is the same accordion pattern. "Tool Proficiency — 1 Choice"
  holds an inline select with the "!" badge.
- The species' two ASI traits both show as "Ability Score Increase" (one from the
  race, one from the subrace), which is confusing.

### 2.5 Abilities (the owner's favourite)

- **Generation method** select: Standard Array, Manual/Rolled, Point Buy.
- **Point Buy:**
  - a big "POINTS REMAINING 27/27" counter;
  - one select per ability, whose options carry their cost ("15 (-9 Points)");
  - **options you can't afford are removed**, so the budget can't be overspent;
  - under each select, **"TOTAL: N" already includes species bonuses** (STR 8 →
    TOTAL 10 as soon as Mountain Dwarf is chosen).
- **Standard Array:** one select per ability. A value used in one select disappears
  from the others, and TOTAL updates live.
- The species ASI traits are repeated here as accordions: "Ability Score Increase
  (Mountain Dwarf)" → "Your Constitution score increases by 2."
- **Score Calculations**: one card per ability with:
  - Total Score and Modifier, highlighted;
  - Base Score;
  - Bonus, **itemised by source** ("Mountain Dwarf (+2)");
  - Set Score;
  - Stacking Bonus;
  - editable Other Modifier and Override Score.

  This is the "visibility of racial points" the owner likes. It is the same
  contributions idea as our C2a "Total Score" explainer, but shown up front while
  building instead of behind a click.

### 2.6 Equipment

- "Choose EQUIPMENT or GOLD".
- Equipment shows each source's package ("Fighter Starting Equipment", "Folk Hero
  Starting Equipment") as **A-or-B checkbox pairs**:
  - the unpicked alternative greys out;
  - options that need a pick ("a martial weapon", "artisan's tools") open an
    inline searchable, source-grouped select;
  - picking a pack shows its contents.
- **Nothing reaches the inventory until "ADD STARTING EQUIPMENT"** is pressed. The
  inventory stays "(0)" until then, and a toast confirms the add.
- After adding:
  - packs are split into their items (17 items, 94 lb, 10 gp to currency);
  - armour and weapons arrive **unequipped** with WEAR/WIELD buttons, the same as
    our F1 decision;
  - the Starting Equipment section collapses.
- Then Other Possessions, Add Items (catalogue search) and Currency.

### 2.7 Portrait

- The portrait slot on every step opens "Manage Portrait":
  - upload (recommended 150×150, 3 MB max);
  - a grid of **default portraits for the chosen species** ("Mountain Dwarf
    Portraits", ~40);
  - then "Other Portraits" (hundreds, no search or filter).
- The selection is outlined, and Apply updates the builder header and its small
  thumbnail.

### 2.8 What's Next

- One line of text, then VIEW CHARACTER SHEET, EXPORT TO PDF and VIEW ALL MY
  CHARACTERS.
- A "Come Together" blurb with Find a Group and Start a New Campaign.
- **No summary and no list of unmade choices** at the end.

### 2.9 Landing on the sheet

- "View Character Sheet" opens `/characters/<id>`, the regular sheet.
- A "MANAGE" button next to the name returns to the builder. Builder and sheet
  are two views of one always-saved character.
- The new sheet shows the unequipped-gear consequence right away: **AC 11**
  (chain mail not worn yet) and only Unarmed Strike under Actions, with no hint
  telling the player to equip what they just received.
- "Actions in Combat" lists 2024 actions (Influence, Magic, Study, Utilize) on a
  2014 character, another case of edition mixing.

## 3. What works well

1. **Preview before commit.** The confirm modals for class and species show every
   consequence (features by level, traits) before the choice lands. This is exactly
   `project-purpose.md`'s "explain the consequences of each choice before it is
   made".
2. **Pending-choice badges.** A "!" plus "N Choices" on each accordion makes
   open decisions visible without a wizard forcing an order.
3. **Constraints enforced by the options themselves.** Point-buy options you can't
   afford disappear, and used values and skills disappear from sibling selects.
   The user can't build an illegal state.
4. **Live, sourced totals on Abilities.** Base, bonus and total are visible at once,
   with each bonus labelled by where it comes from.
5. **Free navigation plus autosave.** Any step can be revisited, and nothing is lost.
6. **Inline sub-choices.** A choice nested in a choice (the martial weapon inside
   the equipment option, the tool inside the trait) opens right where it's needed.
7. **Flavour helpers.** Name suggestions per species, species-filtered default
   portraits, and random characteristics lower the blank-page cost.
8. **Unequipped starting gear**, which matches our own F1 decision.

## 4. What works poorly

1. **Home is a wall of settings.** A first-time player gets ~15 preferences before
   picking a class, most of them irrelevant at that point.
2. **The character exists before it's valid.** Leaving midway leaves a
   half-character in the listing, using a slot.
3. **No final review or validation.** What's Next doesn't list unresolved "!"
   choices; you can leave with an incomplete sheet.
4. **Silent destructive resets.** Changing the generation method wiped every score
   without confirmation.
5. **UI-only constraints.** Clicking fast in Standard Array produced a duplicate 13
   (the filter only ran after the re-render), and nothing flagged it afterwards.
6. **Edition mixing.** A 2014 Fighter's weapon list starts with "5.5e Core Rules"
   and offers only the 5.5e Longsword (plus a Vampire: The Masquerade variant).
   Species rows say "Legacy", which reads as "deprecated" to a newcomer.
7. **Duplicate labels.** Two "Ability Score Increase" traits with the same name.
8. **Truncated selects.** "15 (-9 …" is cut off in the point-buy select after
   choosing.
9. **An easy-to-miss commit step.** Starting equipment does nothing until an
   explicit button is pressed, and the inventory stays at 0 until then.
10. **An unbrowsable portrait gallery.** Hundreds of "Other Portraits" with no
    search or tags.
11. **Native selects everywhere** for skill choices. They're functional but plain,
    and they show nothing about the option until it's chosen.
12. **A first sheet with no guidance.** It opens at AC 11 with no weapon attacks,
    and nothing says "equip your armour and weapons".

## 5. Takeaways for D2 (proposals, not decisions)

| Keep | Adapt | Avoid |
| --- | --- | --- |
| Confirm/preview modal per major pick | Home: only name + portrait + sources; move the other preferences to the sheet's settings | Creating the character before it is valid |
| "!" + "N Choices" badge per feature | Abilities: keep the sourced breakdown, rendered with our C2a contributions shape | Silent resets: confirm before changing the generation method |
| Constraints through options (point-buy budget, used values removed) | Also validate server-side (the planner already knows pending choices) | Edition mixing: our catalogue is 2014-only per the 5etools memory |
| Live TOTAL including species bonuses | A final **Review** step listing unresolved choices from `planBuild` | A hidden commit button for equipment: apply the package on selection or make it the step's primary action |
| Species-filtered default portraits | A portrait gallery tagged by species/class, with search | Duplicate trait names: label ASIs with their source |
| Inline sub-choice selects | Custom pickers showing option descriptions before selection | |
| Unequipped starting gear (F1) | A first-visit hint on the sheet: "equip your starting gear" | |

## 6. Owner decisions (2026-09-24)

1. **Step order:** free navigation, like Beyond, with the steps laid out as
   Species → Class → Background (then Abilities, Equipment, Review).
2. **Persistence:** a **draft with autosave**. It is created on the first step,
   saved on every change, and shown as a draft in the listing until the final
   review completes it.
3. **Portraits:** upload (MinIO), plus a **placeholder per species** until an
   image is uploaded. No default gallery, since Beyond's art can't be reused.
4. **Preferences in D2:** content sources (adr-0006 toggles), fixed or rolled HP,
   optional class features (Tasha's), and prerequisites (feats/multiclass). The
   owner expects to discuss further points Beyond doesn't cover, and asked for
   ideas.
5. **Background characteristics (d8 tables):** **not** in D2. The owner doesn't
   like the tables. Personality is written freely by the user on the sheet after
   creation.

## 6b. Layout feedback on the mockups (2026-09-24)

These are round-1 sneak peeks, in `design-reference/mockups/d2-builder/`.

**Liked:**
- the live character summary beside the form (A);
- the sidebar checklist with per-step detail and a progress bar (B);
- the plain, sectioned forms (D), split into one page per step;
- A's prev/next buttons at the bottom.

**Disliked:**
- A's cramped ability grid;
- A's top tabs, which carry no information;
- B's option cards for class choices (ugly and confusing);
- the whole of C (squeezed, confusing);
- D's single page, and its top summary bar;
- E's one-question flow (abstract, the character's state isn't visible, and it
  doesn't scale to level 20).

Round 2 (`round-2/`) combines the liked parts into three shells: three columns, a
rich step bar, and a single sidebar. It also puts abilities in rows (base → sourced
bonuses → total → modifier).

**Round-2 decision (owner, 2026-09-24): D2 uses suggestion 1's structure**
(`round-2/suggestion-1-three-columns.html`), with adjustments to come.

- **Left:** the step checklist with sub-choices and progress.
- **Middle:** one form page per step, with prev/next at the bottom.
- **Right:** the live character summary.

The owner chose it because it shows everything needed at once, and noted it
resembles Baldur's Gate 3's character creation menu, which they count in its
favour.

Suggestion 2's step cards were liked too, but their hover detail was clipped
inside the header (a mockup bug: the card row's horizontal scroll clips the
popover). Its informative step cards remain an idea for narrow screens.

**Round-3 adjustments (owner, 2026-09-24)**, in `round-3/index.html`:

- **Look:** the sheet's look. Sections use the Proficiencies & Training frame
  (`dnd_frame_proficiencies_training_ink.svg`), stretched with a 14px
  `border-image` 9-slice. Section titles are uppercase condensed, and the palette
  and chips are the sheet's own (`--frame-ink`, `filter-chip`).
- **Frame weight:** the SVG needs an intrinsic size (`width`/`height`), or its
  thickness varies with each box's size.
  - The side columns (checklist, summary) keep the native 14px weight, so they
    stand out.
  - Every middle section uses a thin 8px weight.
- **Summary abilities:** a compact six-column strip instead of big boxes.
- **Basics:**
  - content sources are a multi-select dropdown, with the 2014 Player's Handbook
    and 2014 Dungeon Master's Guide always checked and read-only;
  - partnered content has an "enable all" switch plus a per-publisher selection;
  - new rules: advancement (milestone or XP), encumbrance, and ignore coin weight.
- **Classes:**
  - multiclass and a starting level above 1;
  - the "+ Add another class" list shows each class's prerequisite status;
  - a level-order strip shows the HP each level adds;
  - one tab per class, with a per-level accordion of choices and features;
  - a "Coming next" table previews the next levels' features and choices.
- **Abilities:** the Beyond layout. A method select, the points-remaining counter,
  one select per ability with its cost and a total, and species ASIs labelled by
  source. Then Score Calculations cards (total, modifier, base, bonus per source,
  set score, stacking bonus), with **Other Modifier** and **Override Score**
  inputs that update the card live.
- **Equipment:** the Beyond layout.
  - a starting-equipment accordion: equipment or gold, A-or-B picks, "Add starting
    equipment" and "Clear all";
  - the inventory list, with **Wear/Wield toggles** that make an item start
    equipped (the live summary's AC and attacks follow them);
  - add items, other possessions, currency.
- **Review:**
  - a list of the missing choices, with "Choose →" links;
  - one card per step (key/value lines and Edit);
  - a Finish button that unlocks when nothing is missing.

## 7. Not observed in this walk

- The Manual/Rolled generation method (not opened, to avoid another reset).
- The GOLD alternative in Equipment.
- Level-up flow and multiclass (only "+ Add Another Class" seen).
- Quickbuilder and Premade creation methods.
- Whether the sheet warns about unmade choices after "View Character Sheet".

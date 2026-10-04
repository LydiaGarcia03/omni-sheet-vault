# Feature — Inventory equipment mechanics

**Status: done — all 7 slices complete.**

Slice 1 (`ItemConverter`) is built and verified against the owner's real 5etools data —
see `changelog.md`'s 2026-09-20 "Inventory equipment mechanics, slice 1" entry for the
two real bugs it found and fixed (`FiveEToolsSourceClassifier` was missing `XDMG`/`XMM`,
and one item's rollable charge count). `content/dnd-5e/items/` now holds 1,931 real
converted items, not yet imported into the running local dev catalogue (left for the
owner — see that changelog entry for why).

Slice 2 is built: `Dnd5eItem`/`ruleset.Item` can carry a full catalogue item's
weapon/armour/charge/granted-spell data, added through a new
`POST /api/characters/{id}/items/from-catalogue` endpoint — see `changelog.md`'s slice
2 entry for the field list, the backward-compatible constructor approach, and the
`withEquipped`/`withAttuned`/`withQuantity` field-loss bug it fixed before it ever
shipped. Backend-only, same as slice 1 — no Inventory tab UI calls the new endpoint
yet, left for a later slice.

Slice 3 is built: an equipped weapon-shaped item folds into the Actions tab's attack
table, proficiency-checked against `sheet.weaponProficiencies()`, ability computed
(ranged→dexterity, melee→strength unless finesse→the better one) rather than stored —
see `changelog.md`'s slice 3 entry for the full mapping and its six new tests.

Slice 4 is built: `armorClass()` now looks at equipped `ARMOR`/`SHIELD`-kind items
instead of a flat unarmored formula — dexterity capped per `armorCategory`, a shield's
flat bonus stacking independently, a magic item's own `armorClassBonus` added on top.
See `changelog.md`'s slice 4 entry for its six new tests.

Slice 6 turned out to already be built — `AttunementSection.tsx` (two columns, a
3-limit, no per-row checkbox) predates this initiative, from an earlier phase; the
plan's own text above was stale. The owner instead asked for real visual parity
with D&D Beyond's own attuned-slot artwork; done — three fixed slots using the
real `AttunementSlotBoxSvg.tsx` asset. See `changelog.md`'s slice 6 entry for what
that asset actually turned out to be (a frame outline, not a fill — only found by
rendering it).

**Slice 5 — corrected same day, after slice 6 surfaced a real duplication.**
The version below (attack-roll spells folded into `Dnd5eSheetCalculator.attacks()`)
was **reverted, not kept**: `ActionsTab.tsx` already had a complete, independent
frontend mechanism for spell → Actions-tab rows (`isCombatSpell`/`SpellAttackRow.tsx`,
built 2026-09-03 through 09-15, before this initiative existed), which this slice
should have found before writing backend code. That mechanism already covers
attack-roll **and** save-based spells (a flat DC under Hit/DC) — the "`AttackRow`
has no DC slot" limitation cited below was real for the *backend's* map, but never
actually blocking, since the frontend never routed spells through it. The backend
addition duplicated the attack-roll subset both mechanisms rendered (Fire Bolt,
Chill Touch). See `changelog.md`'s correction entry for the full story, including a
real, independently-confirmed bug fixed in the older mechanism at the same time (it
wrongly included healing spells — Cure Wounds — which real D&D Beyond never shows
in Actions).

**Current, correct state**: `Dnd5eSheetCalculator.attacks()` does not fold spells in
at all — it never needed to. Spells reach the Actions tab entirely through
`ActionsTab.tsx`'s pre-existing `combatSpells`/`SpellAttackRow`, now excluding
healing correctly. Decision 2 below (attack-roll or save-based spell → Actions,
never healing) still holds; only *which layer* implements it changed.

Slice 7 is built: an item's own `grantedSpells` (5etools' `attachedSpells.charges`,
extracted since slice 1 but unused until now) are real, playable mechanics — casting
one spends the granting item's charges instead of a spell slot. `Spell`/`Dnd5eSpell`
gained `grantedByItemKey`/`chargeCost`/`fixedSaveDc` so the entire existing
spell-rendering/rolling pipeline (`SpellRow`, `SpellAttackRow`, `spellDetail.tsx`) is
reused rather than duplicated; a granted spell is filtered out of `sheet.spells()`
unless its owning item is currently equipped (and attuned, if required). A real bug
surfaced only during live verification, not caught by any test: `SpellResponse`/
`ItemResponse` (the actual HTTP DTOs) had never been updated to carry the new
fields, so the domain layer computed everything correctly but the browser never saw
it — Fireball's "DEX 15" showed as "–" until both response records were fixed. See
`changelog.md`'s slice 7 entry for the full verification trail (cast → charge
decrement → long rest → charge restore, all confirmed live against Aria Emberfall).

## Why this exists

By the end of phase 10, the Inventory tab already renders in the app's standard list
grid (`ListRow`/`.action-list`, the same pattern `ActionsTab`/`SpellsTab` use) and
`equipped`/`attuned` are real persisted booleans — but both are cosmetic today.
`InventoryTab.tsx` and `ItemRow.tsx` document this gap outright: neither flag feeds
armour class or the Actions tab, and `systems/dnd-5e/sheet-ui.md` already names the
target ("equipped weapon appears in the actions tab, equipped armour or a shield
contributes to armour class") without it being built. Separately, `Dnd5eItem` has no
weapon/armour shape at all — items are freeform gear, so there is no data path from an
item to an `AttackRow`, and no item ever came from a real content source (5etools is
this project's absolute D&D 5e source of truth per `adr-0006`, but items were never
ingested — only spells were, see `5etools-ingestion.md`).

This is not phase 11 (character creation) and phase 10 is already closed, so it is
tracked here as its own initiative rather than a numbered roadmap phase.

## Decisions made (owner, 2026-09-20)

1. **Attunement cap stays a hardcoded `3`.** Matches the 2014 rules directly ("a
   character can be attuned to no more than three magic items at a time") and matches
   D&D Beyond's own default — confirmed live against Helga Flinthand's sheet
   (dndbeyond.com/characters/50149479). No feature in this app's domain model grants
   extra slots, so making the cap data-driven (as D&D Beyond's own engine allows via a
   `SetAttunementSlotsModifier`) would be speculative generality. `Dnd5eSheetMutator`'s
   existing `ATTUNEMENT_LIMIT = 3` and its attune/unattune rules (attuning doesn't
   require equipping; unattuning is always allowed) already match D&D Beyond's real
   behaviour and don't change.
2. **Actions tab gets exactly what D&D Beyond's own Actions tab shows — not the
   broader "damage or healing" rule first proposed.** Confirmed live: an equipped
   weapon-shaped item, and a known/prepared spell with an attack roll or a saving
   throw, both fold into the ATTACK table (identical row shape). A spell that only
   heals or buffs — Cure Wounds, Shield of Faith — **never appears in Actions**, even
   filtering by the ACTION tab specifically; it stays Spells-tab-only. This app follows
   that exactly: no new "healing dice" field on the spell catalogue, because nothing
   downstream would use it.
3. **Item weapon/armour data gets its own precise shape, sourced 1:1 from 5etools —
   not `Dnd5eCustomAction`'s coarser hand-authored enum shape
   (`Dnd5eWeaponAttackType`/`dualWield`/`silvered`/`rangeCategory`).** That shape
   exists for a human picking options on a form; 5etools gives exact damage dice,
   versatile alternate damage, precise property lists and exact range numbers, and
   collapsing that into the coarser shape would lose precision. The two paths (custom
   action vs. item) converge only at the output (`AttackRow`), never at the input.
4. **Equipped armour/shield contributing to armour class is bundled into this
   initiative**, closing `systems/dnd-5e/sheet-ui.md`'s named gap rather than leaving it for a
   separate pass.
5. **Item-granted spells by charge (the Wand of Fireballs mechanic) reuse this app's
   existing "resource with a recharge trigger" concept from phase 9**, rather than
   being built as a new mechanic from scratch. Confirmed live: the wand shows up only
   in the Spells tab (3rd Level section), tagged with the wand's own name instead of a
   class, a "1C" charge-cost badge instead of a slot-level badge, and a
   "1 Charge (7/7)" note — never in Actions. 5etools models the same shape
   (`charges`, `recharge`, `rechargeAmount`, `attachedSpells.charges` mapping a charge
   cost to a spell slug) that this app's resource/recharge-trigger domain concept
   already covers.

## Facts confirmed live (dndbeyond.com/characters/50149479, 2026-09-20)

- Equip and attune are independent toggles. Attuning does not require equipping.
  Unequipping an attuned item does not unattune it — but an attuned-but-unequipped
  item's modifiers stop applying (equip **and** attune, when attunement is required,
  are both needed for an item to contribute anything).
- The Attunement section is its own panel (3 fixed slots + a list of items requiring
  attunement), not a per-row checkbox in the main equipment list.
- An item's detail sidebar shows, for a weapon: Proficient/Attack Type/Range/Damage/
  Damage Type/Weight/Cost/Properties/Source, plus UNEQUIP/MOVE/DELETE buttons; for an
  attunement item, an extra UNATTUNE button, independent of UNEQUIP.
- Wand of Fireballs: not in the equip-checkbox column at all (it's a held item, not
  armour/a weapon slot); appears only in Spells, 3rd Level, as `Fireball` with a "1C"
  badge and "Wand of Fireballs" as its source line instead of a class.

## Field mapping (5etools item → this project's schema)

Source files: `items-base.json`'s `baseitem` array (mundane equipment) and
`items.json`'s `item` array (magic items) — both single files directly under the data
root, not split per sourcebook like spells. Both share one converter and one output
`data` shape; magic items simply populate more of it.

| 5etools field | This project's field | Notes |
| --- | --- | --- |
| `name` | `name` | |
| `source`, `page` | `sourceBook`, `sourcePage` | Same book-code lookup as spells |
| `type` (before any `\|SOURCE` suffix), `wondrous` | `data.itemKind`, `data.typeLabel` | `itemKind` ∈ `WEAPON`/`ARMOR`/`SHIELD`/`GEAR`, derived from `weapon`/`armor` flags and the `M`/`R`/`LA`/`MA`/`HA`/`S` type codes; `typeLabel` is a confident-subset-plus-fallback decode (same pattern as spell school names), `"Wondrous Item"` when `wondrous` is true and no type code exists |
| `rarity` | `data.rarity` | Already a plain word (`"none"` for mundane) |
| `reqAttune` | `data.requiresAttunement`, `data.attunementRequirement` | Boolean → `true`/`null`; string (e.g. `"by a spellcaster"`) → `true`/that string |
| `weight` | `data.weightLb` | |
| `value` (copper) | `data.costGp` | Divided by 100 |
| `weaponCategory` | `data.weaponCategory` | `SIMPLE`/`MARTIAL`, weapon only |
| `type` (`M`/`R`) | `data.attackType` | `MELEE`/`RANGED`, weapon only |
| `dmg1`, `dmg2` | `data.damageDiceCount/Sides`, `data.versatileDamageDiceCount/Sides` | Parsed `NdM`, weapon only |
| `dmgType` | `data.damageType` | `B`/`P`/`S` → `bludgeoning`/`piercing`/`slashing`, same lowercase convention as spell damage types |
| `property` | `data.properties`, `data.finesse` | Confident-subset decode (`V`/`A`/`L`/`LD`/`2H`/`H`/`F`/`T`/`R`/`S`); `finesse` is `true` when `F` is present |
| `range` | `data.normalRange`, `data.longRange` | `"80/320"` → `80`/`320`; applies to ranged weapons and thrown melee weapons alike |
| `type` (`LA`/`MA`/`HA`) | `data.armorCategory` | `LIGHT`/`MEDIUM`/`HEAVY`, armour only — the dexterity-modifier cap per category is a standard rule applied at calculation time (slice 4), not stored here |
| `ac` | `data.baseArmorClass` | Armour/shield |
| `stealth` | `data.stealthDisadvantage` | Armour only |
| `strength` | `data.strengthRequirement` | Armour only |
| `bonusWeapon` | `data.weaponAttackBonus`, `data.weaponDamageBonus` | Combined bonus applies to both |
| `bonusWeaponAttack`, `bonusWeaponDamage` | `data.weaponAttackBonus`, `data.weaponDamageBonus` | Split bonus, rarer |
| `bonusAc` | `data.armorClassBonus` | |
| `charges` | `data.charges` | |
| `recharge`, `rechargeAmount` | `data.rechargeTrigger`, `data.rechargeFormula` | e.g. `"dawn"`, `"1d6 + 1"` (tag-stripped) |
| `attachedSpells.charges` | `data.grantedSpells[]` (`spellSlug`, `chargeCost`, `fixedSaveDc`) | Maps a charge cost to the spell(s) it casts — the Wand of Fireballs mechanic; `fixedSaveDc` is extracted from the item's own flavor text (`{@dc N}`) since a wand's save DC is fixed by the item, not the wielder's spellcasting ability |
| `entries` | `description` | Same flat-string-array join + tag-strip as spells; nested `{"type":"entries",...}` sub-blocks are not flattened (none of this pass's 2014-sourced test items need it — named, not silently dropped) |

## Out of scope for this pass (slice 1)

- **Generic magic variants** (`magicvariants.json`, e.g. "+1 Longsword" applied to any
  base weapon) — confirmed literal `+N` weapons are *not* items.json entries, they're
  generated from a base item plus a variant template by a different, more complex
  mechanism. A real, named "+1 Longsword" with a literal `bonusWeapon` field (e.g.
  named artifacts) still imports fine; the generic on-the-fly variant system does not.
- Ammo consumption tracking (`ammoType`, `packContents`).
- Weapon mastery (2024-only, and this app excludes `XPHB`/`XDMG` already).
- Passive, non-combat item effects (e.g. Cloak of Elvenkind's Stealth/Perception
  swap) — nothing in this app automates a passive modifier like that for any item
  today; only combat-facing dice/AC/spell data is.
  - **Since C3e (2026-09-24):** structured magic-item fields become
    `data.mechanics`. `ItemMechanicsMapper` handles non-armor `bonusAc`,
    `bonusSavingThrow`, `bonusSpellAttack`, `bonusSpellSaveDc`, `ability.static`,
    `resist`, `immune`, `vulnerable` and `conditionImmune`.
  - These apply while the item is active: equipped, and attuned when required.
  - See `systems/dnd-5e/features/character-creation.md`'s "C3e" for what was left out.
- Item-level `tags` (D&D Beyond's own Damage/Combat/Outerwear-style badges) — no
  confident source field identified yet; left empty rather than guessed, same
  precedent as the spells doc's own tags question.
- **`AttackRow` (the backend map) has no DC slot** — real, but not a spell-facing gap:
  the frontend's own `SpellAttackRow` already renders a save-based spell's DC without
  ever needing this map (see slice 5's correction above). Still a real limitation for
  a **save-based custom action** specifically (`Dnd5eCustomAction`'s own doc comment),
  which has no frontend equivalent of its own — that stays list-only, genuinely
  unresolved, not part of any numbered slice below.

## Slice plan

1. **5etools item ingestion** (`ItemConverter`, mirrors `SpellConverter`) — backend
   only. Done when every in-scope `items-base.json`/`items.json` entry converts
   without a validation failure, a re-run is byte-identical, and Cloak of Elvenkind /
   Wand of Fireballs / Warhammer / Light Crossbow / Scale Mail are spot-checked against
   this doc's field mapping.
2. **Sheet item references the catalogue** — `Dnd5eItem` can reference a real 5etools
   item by slug (inheriting its weapon/armour/spell-grant data), the same
   `CatalogueService.findBySlug` pattern spells already use, while still allowing a
   freeform/homebrew item with no catalogue link.
3. **Equipped weapon → Actions** — `Dnd5eSheetCalculator.attacks()` folds an equipped
   weapon-shaped item into an `AttackRow`, checked against the character's weapon
   proficiencies.
4. **Equipped armour/shield → armour class** — armour class's derivation trace gains
   the equipped item's contribution.
5. **Attack-roll/save-based spell → Actions** — **reverted from `attacks()`**; already
   fully covered by the pre-existing frontend mechanism (`ActionsTab.tsx`'s
   `combatSpells`/`SpellAttackRow`), corrected the same day to exclude healing. See
   this doc's own slice 5 correction note above and `changelog.md`'s correction entry.
6. **Dedicated Attunement panel** — replaces the per-row checkbox with the real
   section (3 slots + "items requiring attunement" list); backend rules already match
   D&D Beyond and don't change.
7. **Item-granted spells by charge** — the Wand of Fireballs mechanic, built on the
   phase 9 resource/recharge-trigger concept; surfaces only in Spells, with a
   charge-cost badge instead of a slot-level badge. Done.

Each slice ends with a changelog entry and this doc updated before the next one starts.
All 7 are complete; this initiative is closed.

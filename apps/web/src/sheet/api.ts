import type { ReactNode } from 'react';
import { apiFetch } from '../api/client';
import type { CatalogueEntry } from '../catalogue/api';
import type { RollResult } from '../dice/api';

export type Contribution = {
  source: string;
  amount: number;
};

export type CalculatedValue = {
  value: number;
  contributions: Contribution[];
};

/** How much of the proficiency bonus a skill or save adds: none, half, full, or double (expertise). */
export type ProficiencyLevel = 'NONE' | 'HALF' | 'FULL' | 'EXPERT';

/**
 * What the Explainer mold needs (ui-design-system.md) — a title, a display-ready
 * value and its derivation trace. The headline value arrives pre-formatted because
 * its sign convention varies by what it is (a modifier is signed, armor class and
 * speed are not) and the caller already knows which; the panel itself stays dumb.
 * `rulesText` closes the panel (see `RulesText`); empty or absent shows nothing.
 */
export type ExplainerRequest = {
  kind: 'explainer';
  title: string;
  icon?: ReactNode;
  formattedValue: string;
  contributions: Contribution[];
  /** A small heading above the contributions, e.g. `Total Score 20` for an ability's breakdown. */
  contributionsHeading?: string;
  rulesText?: string;
};

/** Bound by CharacterSheetScreen to the sidebar's open-panel state — leaf reveal targets just call it. */
export type ExplainHandler = (request: ExplainerRequest) => void;

/**
 * One labeled fact in the Entity Detail mold's metadata row — e.g. `{ label:
 * 'Range', value: '5 ft.' }`. Phase 10 slice 2: this replaced a plain
 * `string[]` (joined lines with no label) so metadata reads as D&D Beyond's
 * own labeled fields do, confirmed live against the reference's attack panel
 * (Proficient/Attack Type/Reach/Damage/Damage Type/Weight/Cost/Properties/
 * Source) — we only label the subset of that list our own data already
 * carries, not the fields no entity tracks yet (source/book stays out, same
 * reasoning as this type's own doc comment below).
 */
export type EntityDetailMetadataEntry = { label: string; value: ReactNode };

/**
 * What the Entity Detail mold needs (ui-design-system.md) — name, action bar,
 * metadata, tags and description. `icon`/`parent` cover D&D Beyond's shared
 * sidebar `Header` (a `parent` line above the name plus a `preview` icon
 * beside it); source line is still left out, since no entity tracks a
 * source/book yet. Every trigger without icon/class data (an attack, a
 * feature action) simply omits them, same precedent as ExplainerRequest's
 * rules text. `actionBar` is a caller-built node (roll buttons for an
 * attack, a BoxTrack for a limited-use feature) rather than a data shape,
 * since "only its action bar varies" per the mold's own spec. `tags` is for
 * flag-like badges with no paired value (a spell's Concentration/Ritual
 * markers) — kept separate from `metadata`'s label/value pairs rather than
 * forcing an awkward "Concentration: Yes". `body` is a second caller-built
 * escape hatch, same reasoning as `actionBar`: an extra's structured stat
 * block (phase 10 slice 10) needs a real layout — an ability score grid,
 * skills/senses/languages/CR lines, named trait/action entries — that a
 * single `description` string cannot render. Renders instead of
 * `description` when present; every other trigger keeps using plain
 * `description` text.
 */
export type EntityDetailRequest = {
  kind: 'entityDetail';
  name: string;
  /**
   * Optional line above the name — matches D&D Beyond's own shared `Header`
   * `parent` slot (the class, item or feature that grants this entity, e.g.
   * a spell's `Spell.className`). Every entity type without a grantor omits
   * it.
   */
  parent?: string;
  /** Makes the parent line a link that opens the granting entity's own detail. */
  onOpenParent?: () => void;
  /**
   * Optional icon beside the name — D&D Beyond's own `Header` `preview` slot
   * (a spell's 32x32 school-of-magic glyph). Every entity type without an
   * icon asset omits it.
   */
  icon?: ReactNode;
  /**
   * Optional italic line directly under the name — matches D&D Beyond's own
   * `.ct-spell-detail__level-school` ("1st-level Evocation"), the one entity
   * type (so far) with a short classifying line that isn't a label/value pair
   * and doesn't belong in `metadata`. Every other entity type omits it.
   */
  subtitle?: ReactNode;
  metadata: EntityDetailMetadataEntry[];
  tags?: string[];
  description: string;
  body?: ReactNode;
  actionBar?: ReactNode;
  /**
   * Where `actionBar` sits relative to properties/description/tags —
   * DOM-confirmed live D&D Beyond puts it last for most entities (an item's
   * own Quantity/Move/Delete row trails its Weight/Cost/Source and
   * description), 'bottom' is the default. Spells are the one confirmed
   * exception (`buildSpellDetailRequest`): their Cast control renders
   * *before* properties, so that's the only caller passing `'top'`.
   * `'section'` puts it right under the header with its own separator — a
   * feature action's Limited Use boxes.
   */
  actionsPosition?: 'top' | 'section' | 'bottom';
  /**
   * Optional: rebuild the action bar from live sheet state. Most triggers omit
   * this — their action bar is either inert or fine as the snapshot taken when
   * the panel opened. A trigger whose action bar shows values that can change
   * while the panel stays open (e.g. an Extra's hit points, mutated via its own
   * action bar) supplies this so CharacterSheetScreen can keep it in sync, the
   * same live-refresh treatment already given to the Collection editor and Log
   * molds.
   */
  refreshActionBar?: (sheet: CharacterSheet) => ReactNode;
  /** Identifies the entity shown, so opening another one starts the panel afresh while a refresh keeps its state. */
  entityKey?: string;
  /** Rebuilds the whole panel from live sheet state (a rename or Customize change); null keeps the snapshot. */
  refresh?: (sheet: CharacterSheet) => EntityDetailRequest | null;
  /**
   * D&D Beyond's Customize under the header: the editor, and whether anything is customized (the name and the
   * section then read "*"). The pencil beside the name opens it and focuses the editor's `[data-customize-name]` field.
   */
  customize?: { editor: ReactNode; customized: boolean; label?: string };
};

/** Bound by CharacterSheetScreen to the sidebar's open-panel state — leaf reveal targets just call it. */
export type EntityDetailHandler = (request: EntityDetailRequest) => void;

/**
 * What the Log mold needs (ui-design-system.md) — reverse-chronological entries.
 * `entries` already arrives newest-first from the API (see dice/api.ts).
 */
export type LogRequest = {
  kind: 'log';
  entries: RollResult[];
  /** The name over each entry — the character's. */
  senderName?: string;
};

/**
 * One condition row inside a `ConditionsRequest` panel.
 */
export type ConditionEntry = { key: string; label: string; active: boolean; icon?: ReactNode; rulesText?: string };

/**
 * What the Conditions panel needs (phase 10 slice 4) — not the Collection
 * editor mold: that mold's shape is free-text add/remove against an
 * unbounded set, but conditions are a fixed, known-in-advance list of
 * fourteen booleans toggled in place, with no add/remove or search
 * semantics at all. Confirmed live against D&D Beyond: clicking "Add Active
 * Conditions" (or a summary of the active ones) opens a sidebar panel
 * titled "Conditions" listing all fourteen, each with a toggle — this
 * mirrors that exactly, moved out of `DefensesConditionsPanel`'s
 * always-visible grid into the sidebar to match.
 */
export type ConditionsRequest = {
  kind: 'conditions';
  title: string;
  conditions: ConditionEntry[];
  onToggle: (key: string) => void;
  exhaustionLevel: number;
  exhaustionIcon?: ReactNode;
  exhaustionRulesText?: string;
  onExhaustionChange: (level: number) => void;
  activeEffects: ActiveEffect[];
  onEndEffect: (effectKey: string) => void;
};

/**
 * What the Spell management panel needs (phase 9's "Manage spells") — not the
 * Collection editor mold: that mold's shape is a flat free-text list per category
 * (`CollectionEditorRequest`), which doesn't fit "known spells and prepared
 * spells, split per class, showing cantrips known and preparation limits, and
 * marking spells that are always prepared" (systems/dnd-5e/sheet-ui.md's Spells
 * tab spec) — a genuinely different shape, not a missing feature of the existing
 * mold. `classes` carries each spellcasting class's own limits (see
 * `SpellcastingClassInfo`); `knownSpells` is the character's own `spells` list;
 * `catalogue` is the seeded subset of learnable spells (phase 9's scope boundary,
 * confirmed with the owner: not the full official class spell list, which needs a
 * much larger content-authoring effort).
 */
export type SpellManagementRequest = {
  kind: 'spellManagement';
  classes: SpellcastingClassInfo[];
  knownSpells: Spell[];
  catalogue: CatalogueEntry[];
  /** The slots summarized at the top of the pane. */
  slots?: SpellSlotLevel[];
  onLearn: (catalogueEntryId: string, className: string) => void;
  onRemove: (spellKey: string) => void;
  onPrepare: (spellKey: string) => void;
  onUnprepare: (spellKey: string) => void;
};

/**
 * The Features & Traits tab's "Manage Feats" trigger — a flat catalogue picker,
 * unlike `SpellManagementRequest`'s per-class sections, since a feat isn't
 * scoped to a class and carries no known/prepared caps to track. `knownFeats`
 * is `sheet.featureTraits` filtered to `category === 'FEAT'`; `catalogue` is
 * every real 5etools feat imported into the catalogue.
 */
export type FeatManagementRequest = {
  kind: 'featManagement';
  knownFeats: FeatureTrait[];
  catalogue: CatalogueEntry[];
  onLearn: (catalogueEntryId: string) => void;
  onRemove: (featureKey: string) => void;
};

/**
 * What the Mechanic mold needs (ui-design-system.md: "Rules summary, controls,
 * confirm button") — phase 9's short/long rest are its first real triggers.
 * `body` is a caller-built node covering both "controls" and "confirm button",
 * same escape hatch `EntityDetailRequest.actionBar` already uses: a long rest
 * needs no player input at all (just a confirm button), a short rest needs a
 * hit-dice-to-spend control the confirm button must read the current value of
 * — a single self-contained component is simpler than threading that value
 * back out through this request shape.
 */
export type MechanicRequest = {
  kind: 'mechanic';
  title: string;
  /** Rules text above the controls (see `RulesText`). */
  summary: string;
  /** Draws the intro gray, as D&D Beyond's short rest pane does. */
  mutedSummary?: boolean;
  body: ReactNode;
  /** Rules text closing the panel. */
  rulesText?: string;
};

/**
 * One editable free-text field within a `TextFieldRequest` panel — see that
 * type's doc comment for why a panel carries an array rather than one value.
 * `suggestions`, when present, renders a "roll a suggestion" table below the
 * field (phase 10, confirmed live against D&D Beyond's Personality
 * Traits/Ideals/Bonds/Flaws panels) — a plain client-side random pick, not a
 * server roll: it's flavor text, not a mechanic.
 */
export type TextFieldEntry = {
  label: string;
  value: string;
  multiline?: boolean;
  helperPrompt?: string;
  onSave: (value: string) => void;
  suggestions?: string[];
  /** Turns the field into a select with these choices (plus "--"). */
  options?: string[];
  /** The die the suggestions table is headed with, e.g. "d8". */
  suggestionDie?: string;
};

/**
 * What the Text Field mold needs (ui-design-system.md's seventh mold, phase
 * 10) — confirmed live against D&D Beyond's Background tab: most fields
 * (Backstory, Organizations, Personality Traits, ...) each open their own
 * single-field panel, but Alignment plus the character's other
 * characteristics (Gender, Eyes, Size, Height, Faith, Hair, Skin, Age,
 * Weight, Lifestyle) share one combined "Characteristics and Details" panel.
 * `fields` covers both shapes with one array — one entry for a single-field
 * panel, several for a combined one — rather than needing a second mold or an
 * awkward single-vs-many union.
 */
export type TextFieldRequest = {
  kind: 'textField';
  title: string;
  fields: TextFieldEntry[];
};

/**
 * What the HP Management mold needs — D&D Beyond's own sidebar panel opened by
 * clicking anywhere inside the top-row Hit Points box except its numbers and
 * its own inline Heal/Damage/Temp controls (see `HitPoints.tsx`'s
 * `onOpenManagement`). `current`/`max`/`temporary` mirror the box's own
 * values so the two stay in sync while both are visible. `calculatedMax` is the
 * maximum before `maxModifier` or `maxOverride` (null: not set).
 */
export type HpManagementRequest = {
  kind: 'hpManagement';
  current: number;
  max: number;
  temporary: number;
  calculatedMax: number;
  maxModifier: number | null;
  maxOverride: number | null;
  /** How the maximum is built (each level's die, Constitution, feature bonuses). */
  maxContributions: Contribution[];
  onDamage: (amount: number) => void;
  onHeal: (amount: number) => void;
  onSetTemporary: (amount: number) => void;
  onSetMaxModifier: (value: number | null) => void;
  onSetMaxOverride: (value: number | null) => void;
  /** At 0 hit points and not stable: the pane ends with `deathSavesRulesText` (see `RulesText`). */
  dying?: boolean;
  deathSavesRulesText?: string;
};

/**
 * What the "Manage Custom" panel needs (punch list item 7,
 * systems/dnd-5e/references/sheet-fidelity-audit.md) — not the Collection editor mold: a
 * custom action has a multi-field, template-dependent shape (dropdowns,
 * checkboxes, numbers), the same reasoning `SpellManagementRequest`'s doc
 * comment already gives for spells. All four of the Actions tab's "Manage
 * Custom" triggers open this same panel with the character's complete list —
 * confirmed live against D&D Beyond: one flat list grouped by template, not
 * scoped per section.
 */
export type ManageCustomActionsRequest = {
  kind: 'manageCustomActions';
  customActions: CustomAction[];
  onAdd: (action: NewCustomAction) => void;
  onRemove: (actionKey: string) => void;
  /** Opens the action's own pane, where its Edit section changes it. */
  onOpenAction: (action: CustomAction) => void;
};

/** A freeform/homebrew item's own fields — same shape ADD_ITEM already takes, minus the discriminant. */
export type NewItemDraft = { name: string; quantity: number; cost: string; notes: string; requiresAttunement: boolean };

/**
 * The Inventory tab's "Manage Inventory" trigger (matching "Manage Spells"/"Manage
 * Custom") — relocates the add-item form that used to sit inline at the bottom of
 * the tab into this same sidebar surface, alongside the full item list for removal.
 * `catalogue`/`onAddFromCatalogue` are the weight/encumbrance initiative's own
 * addition: a real 5etools-backed picker (same "Learn"-style pattern
 * `FeatManagementRequest` already uses), matching D&D Beyond's own "Add Items"
 * panel — `onAdd` (freeform/homebrew) stays alongside it, same as Beyond's own
 * separate "Add Custom Item". `onMove` reassigns an item's storage location
 * (Equipment/Backpack/Bag of Holding/Other Possessions).
 */
export type ManageInventoryRequest = {
  kind: 'manageInventory';
  items: Item[];
  catalogue: CatalogueEntry[];
  onAdd: (draft: NewItemDraft) => void;
  onAddFromCatalogue: (catalogueEntryId: string) => void;
  onRemove: (itemKey: string) => void;
  onMove: (itemKey: string, storageLocation: string) => void;
  onToggleEquip?: (itemKey: string) => void;
  /** Opens the item's own pane (Customize, Delete). */
  onOpenItem?: (item: Item) => void;
  /** The server's weight per storage location, shown on each group. */
  sections?: StorageSection[];
};

/** Everything the one sidebar surface can show — see Sidebar.tsx. */
/** The character sidebar, opened from the portrait or the name: who the character is, and what to do with it. */
export type CharacterMenuRequest = {
  kind: 'characterMenu';
  name: string;
  portrait: ReactNode;
  /** The character's summary facts after the first (D&D 5e: the species). */
  subtitle: string | null;
  experience: Experience | null;
  onManageExperience: () => void;
  onLevelUp: () => void;
  onOpenLog: () => void;
  onOpenShortRest: () => void;
  onOpenLongRest: () => void;
  onChangeAppearance?: () => void;
  /** Renames the character; without it the name isn't editable. */
  onRename?: (name: string) => void;
  icons: { shortRest: ReactNode; longRest: ReactNode; gameLog: ReactNode };
};

/** Manage experience: set a level or a total, or add or remove points, then apply the new total. */
export type ManageExperienceRequest = {
  kind: 'manageExperience';
  experience: Experience;
  onApply: (points: number) => void;
};

/** Change sheet appearance: the system's themes as tiles, each drawn by `renderSample`. */
export type ChangeAppearanceRequest = {
  kind: 'changeAppearance';
  themes: { id: string; label: string; color: string }[];
  current: string | null;
  onSelect: (themeId: string) => void;
  renderSample: (theme: { id: string; label: string; color: string }) => ReactNode;
  /** The character's portrait, shown under Current Decorations. */
  portrait?: ReactNode;
};

/**
 * A game system's own sidebar panel (e.g. D&D 5e's ability pane): drawn from the live sheet on every render, so it
 * shows a mutation's result while it stays open. `content` is filled in by the sheet screen from `render`.
 */
export type PaneRequest = {
  kind: 'pane';
  render: (sheet: CharacterSheet) => ReactNode;
  content?: ReactNode;
};

/** Bound by CharacterSheetScreen to the sidebar's open-panel state. */
export type PaneHandler = (request: PaneRequest) => void;

export type SidebarContent =
  | PaneRequest
  | CharacterMenuRequest
  | ChangeAppearanceRequest
  | ManageExperienceRequest
  | ExplainerRequest
  | EntityDetailRequest
  | LogRequest
  | SpellManagementRequest
  | MechanicRequest
  | TextFieldRequest
  | ConditionsRequest
  | HpManagementRequest
  | ManageCustomActionsRequest
  | ManageInventoryRequest
  | FeatManagementRequest;

export type AttackRow = {
  name: string;
  range: string;
  toHit: CalculatedValue;
  damageDiceCount: number;
  damageDiceSides: number;
  damageModifier: number;
  damageType: string;
  category: string;
  notes: string;
  /** 'ACTION' | 'BONUS_ACTION' | 'REACTION' | 'OTHER' — always 'ACTION' for a catalog weapon or equipped item; only a custom action folded in here can carry a different one. */
  actionType: string;
  /** A versatile weapon's two-handed dice (same modifier); null or absent otherwise. */
  versatileDiceCount?: number | null;
  versatileDiceSides?: number | null;
};

export type FeatureAction = {
  key: string;
  name: string;
  actionType: string;
  description: string;
  maxUses: number | null;
  usedCount: number;
  rechargeTrigger: string | null;
  /** The feature that grants this action; null when it isn't linked to one. */
  parentName?: string | null;
  parentKey?: string | null;
};

export type SpellcastingClassInfo = {
  className: string;
  spellcastingAbility: string;
  spellcastingModifier: CalculatedValue;
  spellAttackBonus: CalculatedValue;
  spellSaveDc: CalculatedValue;
  castingType: string;
  cantripsKnownMax: number;
  spellsKnownMax: number | null;
  spellsPreparedMax: number | null;
  classCaster: boolean;
};

export type Spell = {
  key: string;
  name: string;
  className: string;
  level: number;
  school: string;
  castingTime: string;
  range: string;
  concentration: boolean;
  ritual: boolean;
  attackRoll: boolean;
  damageDiceCount: number | null;
  damageDiceSides: number | null;
  damageType: string | null;
  notes: string;
  effectSummary: string;
  prepared: boolean;
  alwaysPrepared: boolean;
  description: string;
  saveAbility: string | null;
  components: string;
  materialComponent: string | null;
  duration: string;
  /** 5etools' own "At Higher Levels" scaling text — null for a spell with none (most cantrips, and some leveled spells). */
  higherLevelsDescription: string | null;
  /** The same scaling, structured: dice added to damageDiceCount/damageDiceSides per slot level cast above `level`. Null together when the spell doesn't scale this way. */
  higherLevelsDamageDiceCount: number | null;
  higherLevelsDamageDiceSides: number | null;
  /**
   * The Wand of Fireballs mechanic (systems/dnd-5e/features/inventory-equipment-mechanics.md,
   * slice 7) — all three null together for a normal class-known spell.
   * `grantedByItemKey` names the `Item` (see its own `charges`/`chargesUsed`)
   * this spell is cast from instead of a spell slot; `chargeCost` is how many
   * of that item's charges casting it spends; `fixedSaveDc` is the item's own
   * flat save DC, used instead of a spellcasting class's computed one (which a
   * granted spell has none of).
   */
  grantedByItemKey: string | null;
  chargeCost: number | null;
  fixedSaveDc: number | null;
  /** The player's Customize values over the class's attack bonus and save DC; absent on an older API. */
  adjustments?: SpellAdjustments;
  /** How a feature-granted spell is cast without a slot; null or absent for a spell cast with slots. */
  usage?: SpellUsage | null;
};

/** A feature-granted spell cast at will, or a number of times per rest; `castLevel` null casts it at its own level. */
export type SpellUsage = {
  mode: 'AT_WILL' | 'LIMITED';
  maxUses: number;
  usedUses: number;
  recharge: 'SHORT_OR_LONG_REST' | 'LONG_REST' | null;
  castLevel: number | null;
  selfOnly: boolean;
};

/** An attack bonus override (null: none) or bonus, a damage bonus, a save DC override or bonus, and Display As Attack. */
export type SpellAdjustments = {
  attackOverride: number | null;
  attackBonus: number;
  damageBonus: number;
  saveDcOverride: number | null;
  saveDcBonus: number;
  displayAsAttack: boolean;
};

/** The spell's attack bonus once adjusted: the override, or the class's bonus plus the player's. */
export function adjustedSpellAttack(spell: Spell, classAttackBonus: number): number {
  const adjustments = spell.adjustments;
  if (!adjustments) {
    return classAttackBonus;
  }
  return adjustments.attackOverride ?? classAttackBonus + adjustments.attackBonus;
}

/** The spell's save DC once adjusted: the override, or the calculated DC plus the player's bonus; null without a DC. */
export function adjustedSpellSaveDc(spell: Spell, calculatedDc: number | null): number | null {
  const adjustments = spell.adjustments;
  if (adjustments?.saveDcOverride != null) {
    return adjustments.saveDcOverride;
  }
  return calculatedDc == null ? null : calculatedDc + (adjustments?.saveDcBonus ?? 0);
}

/**
 * A player-authored custom action — punch list item 7
 * (systems/dnd-5e/references/sheet-fidelity-audit.md). Every field beyond `template`/
 * `name`/`snippet`/`description`/`activationType` is nullable: almost every
 * field on D&D Beyond's own form is optional, and this app's mirrors that —
 * see `Dnd5eCustomAction`'s own doc comment for the confirmed-live semantics
 * (to-hit/damage math, which template uses which extra field). A `displayAsAttack`
 * entry that resolves to a real to-hit roll (a `stat` set) also appears in
 * `attacks`, computed the same way a catalog attack is — see that record's
 * `attacks` field.
 */
export type NewCustomAction = {
  template: string;
  name: string;
  snippet: string;
  description: string;
  rangeCategory: string | null;
  rangeFeet: number | null;
  stat: string | null;
  diceCount: number | null;
  dieType: number | null;
  fixedValue: number | null;
  damageType: string | null;
  saveType: string | null;
  fixedSaveDc: number | null;
  spellRangeType: string | null;
  aoeType: string | null;
  aoeSize: number | null;
  /** Null until the player picks one (D&D Beyond's "--"); such an action shows nowhere on the Actions tab. */
  activationType: string | null;
  activationTime: number | null;
  affectedByMartialArts: boolean;
  proficient: boolean;
  displayAsAttack: boolean;
  weaponAttackType: string | null;
  longRange: number | null;
  dualWield: boolean;
  silvered: boolean;
};

export type CustomAction = NewCustomAction & { key: string };

export type FeatureTrait = {
  key: string;
  name: string;
  category: string;
  source: string;
  summary: string;
  description: string;
  maxUses: number | null;
  usedCount: number;
  rechargeTrigger: string | null;
  /** What the player picked for this feature (the subclass under "Martial Archetype"); null on older sheets. */
  choices?: string[] | null;
};

/**
 * The backend's own `Item` shape grew far past this (weapon/armour/charge data,
 * slices 2-4 of systems/dnd-5e/features/inventory-equipment-mechanics.md) without this type
 * being updated to match — a pre-existing gap, not introduced here. `charges`/
 * `chargesUsed` are added now because slice 7 (item-granted spells by charge)
 * needs them to show remaining charges; the rest of that catalogue-sourced data
 * stays untyped/inaccessible here until something actually needs it.
 */
export type Item = {
  key: string;
  name: string;
  quantity: number;
  cost: string;
  notes: string;
  equipped: boolean;
  attuned: boolean;
  requiresAttunement: boolean;
  charges: number | null;
  chargesUsed: number;
  itemKind: string;
  rarity: string | null;
  storageLocation: string;
  weightKg: number | null;
  /** Pre-formatted "book, page" string, copied once from a catalogue entry — `null` for a freeform item. */
  source: string | null;
  /** The weapon's properties, plus Silvered / Adamantine when customized; absent on an older API. */
  properties?: string[];
};

/** One of a character's storage buckets — see `Encumbrance`. */
export type StorageSection = {
  location: string;
  itemCount: number;
  weightKg: number;
  capacityKg: number | null;
};

/**
 * Weight/encumbrance initiative: carried weight and capacity, already converted to
 * kilograms server-side (ground-rules.md: "No business rules in the frontend") —
 * `overloaded` is always `false` while `trackWeight` is off, even if the raw
 * numbers would otherwise exceed `capacityKg`.
 */
export type Encumbrance = {
  trackWeight: boolean;
  carriedWeightKg: number;
  capacityKg: number;
  overloaded: boolean;
  sections: StorageSection[];
};

export type Coins = {
  copper: number;
  silver: number;
  electrum: number;
  gold: number;
  platinum: number;
};

export type ExtraAbilityScore = {
  abilityKey: string;
  score: number;
  modifier: number;
  save: number;
};

export type ExtraSkill = {
  name: string;
  bonus: number;
};

export type ExtraStatEntry = {
  name: string;
  description: string;
};

export type ExtraStatBlock = {
  size: string;
  creatureType: string;
  alignment: string;
  initiativeBonus: number;
  hitDiceLabel: string;
  additionalSpeeds: string | null;
  abilityScores: ExtraAbilityScore[];
  skills: ExtraSkill[];
  senses: string;
  languages: string;
  challengeRating: string;
  traits: ExtraStatEntry[];
  actions: ExtraStatEntry[];
};

export type Extra = {
  key: string;
  name: string;
  category: string;
  armorClass: number;
  maxHitPoints: number;
  currentHitPoints: number;
  temporaryHitPoints: number;
  speed: number;
  statBlock: ExtraStatBlock;
};

/** One pool per die size (largest first); dieSize/max/used summarise them. longRestRecoveryMax is how many spent dice a long rest returns. */
export type HitDice = {
  dieSize: number;
  max: number;
  used: number;
  pools: HitDicePool[];
  longRestRecoveryMax: number;
};

export type HitDicePool = {
  dieSize: number;
  max: number;
  used: number;
  /** The classes that bring dice of this size. */
  classNames?: string[];
};

/** Die size → number of dice. */
export type HitDiceBySize = Record<number, number>;

/** One slot pool; `pact` is the Warlock's Pact Magic pool, apart from the regular slots and regained on a short rest. */
export type SpellSlotLevel = {
  level: number;
  maxSlots: number;
  usedSlots: number;
  pact?: boolean;
  /** The class a Pact Magic pool belongs to; null for a regular pool. */
  className?: string | null;
};

export type SpecialSense = {
  type: string;
  rangeFeet: number;
  label: string;
};

export type Background = {
  name: string;
  featureName: string;
  featureDescription: string;
  alignment: string;
  personalityTraits: string;
  ideals: string;
  bonds: string;
  flaws: string;
  appearance: string;
  organizations: string;
  allies: string;
  enemies: string;
  backstory: string;
  other: string;
  gender: string;
  eyes: string;
  size: string;
  height: string;
  faith: string;
  hair: string;
  skin: string;
  age: string;
  weight: string;
  lifestyle: string;
};

/** The field names `postUpdateBackgroundField` accepts — must match `Dnd5eBackgroundField` exactly. */
export type BackgroundField =
  | 'ALIGNMENT'
  | 'PERSONALITY_TRAITS'
  | 'IDEALS'
  | 'BONDS'
  | 'FLAWS'
  | 'APPEARANCE'
  | 'ORGANIZATIONS'
  | 'ALLIES'
  | 'ENEMIES'
  | 'BACKSTORY'
  | 'OTHER'
  | 'GENDER'
  | 'EYES'
  | 'SIZE'
  | 'HEIGHT'
  | 'FAITH'
  | 'HAIR'
  | 'SKIN'
  | 'AGE'
  | 'WEIGHT'
  | 'LIFESTYLE';

/** A lasting effect from a cast spell; endsOnRests empty means only the player ends it, onSelf false means it is on an ally and changes none of the character's values. */
export type ActiveEffect = {
  key: string;
  name: string;
  castAtLevel: number | null;
  concentration: boolean;
  endsOnRests: string[];
  durationText: string | null;
  onSelf: boolean;
};

/** Advantage or disadvantage the sheet forces on a roll, with the sources of each; both present cancel to NORMAL. */
export type ForcedRollMode = {
  mode: 'ADVANTAGE' | 'DISADVANTAGE' | 'NORMAL';
  advantageSources: string[];
  disadvantageSources: string[];
};

export type CharacterSheet = {
  abilityScores: Record<string, number>;
  abilityModifiers: Record<string, CalculatedValue>;
  proficiencyBonus: CalculatedValue;
  armorClass: CalculatedValue;
  initiative: CalculatedValue;
  hitPoints: CalculatedValue;
  speed: number;
  level: number;
  savingThrows: Record<string, CalculatedValue>;
  savingThrowProficiencies: Record<string, ProficiencyLevel>;
  senses: Record<string, CalculatedValue>;
  armorProficiencies: string[];
  weaponProficiencies: string[];
  toolProficiencies: string[];
  languages: string[];
  skills: Record<string, CalculatedValue>;
  skillProficiencies: Record<string, ProficiencyLevel>;
  skillGoverningAbilities: Record<string, string>;
  damageResistances: string[];
  damageImmunities: string[];
  damageVulnerabilities: string[];
  conditionImmunities: string[];
  activeConditions: string[];
  exhaustionLevel: number;
  currentHitPoints: number;
  temporaryHitPoints: number;
  /** The hit point maximum before the player's Max HP Modifier or Override Max HP. */
  calculatedMaxHitPoints: number;
  heroicInspiration: boolean;
  attacks: Record<string, AttackRow>;
  featureActions: FeatureAction[];
  spellcasting: SpellcastingClassInfo[];
  spells: Spell[];
  items: Item[];
  coins: Coins;
  featureTraits: FeatureTrait[];
  background: Background;
  extras: Extra[];
  hitDice: HitDice;
  spellSlots: SpellSlotLevel[];
  specialSenses: SpecialSense[];
  customActions: CustomAction[];
  encumbrance: Encumbrance;
  attacksPerAction: number;
  /** Keyed ATTACK, INITIATIVE, ABILITY_CHECK:<ability>, SAVING_THROW:<ability>, SKILL_CHECK:<skill>; absent when no mode is forced. */
  rollModes: Record<string, ForcedRollMode>;
  activeEffects: ActiveEffect[];
  /** Where build-derived values come from; null when the system has none. */
  provenance: Provenance | null;
  /** Skills the player added, with their calculated bonus. */
  customSkills?: CustomSkill[];
  /** Every defense with its source; absent on responses from an older API. */
  defenses?: DefenseEntry[];
  /** Situational roll modifiers ("advantage against poison"), shown but never applied. */
  rollNotes: RollNote[];
  /** Absent on responses from an API older than death saves. */
  deathSaves?: DeathSaves;
  /** The character's advancement; null for a system without one. */
  experience?: Experience | null;
  /** The theme the sheet is drawn in; absent means the system default. */
  sheetTheme?: string | null;
};

export type DefenseType = 'RESISTANCE' | 'IMMUNITY' | 'VULNERABILITY' | 'CONDITION_IMMUNITY';

/** One defense: what grants it (`source`, null when unknown); a `custom` one was added by the player and has a `key`. */
export type DefenseEntry = {
  key: string | null;
  type: DefenseType;
  name: string;
  source: string | null;
  custom: boolean;
  notes: string | null;
};

/**
 * The character's advancement: `currentLevelAt`/`nextLevelAt` bound the progress bar (`nextLevelAt`
 * is null at the top level); `levelUpAvailable` means the points reached a level not yet taken
 * (experience builds only); `canLevelUp` means a level up may be started at all.
 */
export type Experience = {
  advancement: 'XP' | 'MILESTONE';
  points: number;
  level: number;
  levelFromPoints: number;
  currentLevelAt: number;
  nextLevelAt: number | null;
  levelUpAvailable: boolean;
  canLevelUp: boolean;
  /** The points each level starts at, level 1 first. */
  thresholds: number[];
  classes: { name: string; subclass: string | null; level: number }[];
};

/** Death saving throws: `dying` while the sheet shows them in place of the hit points (at 0 hit points). */
export type DeathSaves = {
  successes: number;
  failures: number;
  dying: boolean;
  stable: boolean;
  dead: boolean;
};

export type RollNote = {
  mode: 'ADVANTAGE' | 'DISADVANTAGE' | string;
  /** The rolls it concerns, e.g. SAVING_THROWS or DEXTERITY_SAVING_THROWS. */
  target: string;
  restriction: string;
  source: string;
};

export type Provenance = {
  abilityScores: Record<string, CalculatedValue>;
  speed: CalculatedValue;
  proficiencySources: { kind: string; label: string; source: string }[];
  /** Each ability as D&D Beyond's ability pane lays it out. */
  abilityBreakdowns: Record<string, AbilityBreakdown>;
  /** The player's customizations, in the game system's own shape (read by its panes). */
  customizations?: unknown;
};

export type CustomSkill = {
  key: string;
  name: string;
  /** Null when the skill uses no ability. */
  abilityKey: string | null;
  proficiencyLevel: ProficiencyLevel;
  value: CalculatedValue;
  notes: string | null;
  description: string | null;
};

export type AbilityBreakdown = {
  total: number;
  modifier: number;
  base: number;
  bonus: number;
  bonusSources: Contribution[];
  setScore: number;
  stackingBonus: number;
  otherModifier: number | null;
  overrideScore: number | null;
};

export type MutationAction =
  | { type: 'DAMAGE'; amount: number }
  | { type: 'HEAL'; amount: number }
  | { type: 'SET_DEATH_SAVES'; successes: number; failures: number }
  | { type: 'SET_EXPERIENCE'; points: number }
  | { type: 'SET_SHEET_THEME'; theme: string }
  /** A hand-set value over a calculated one; `value` is the game system's own shape for `group`. */
  | { type: 'CUSTOMIZE'; group: string; target: string; value: Record<string, unknown> }
  /** Clears a customization, or removes an entry the player added (a custom skill). */
  | { type: 'REMOVE_CUSTOMIZATION'; group: string; target: string }
  | { type: 'TEMPORARY_HIT_POINTS'; amount: number }
  | { type: 'TOGGLE_INSPIRATION' }
  | { type: 'TOGGLE_CONDITION'; condition: string }
  | { type: 'SET_EXHAUSTION_LEVEL'; level: number }
  | { type: 'ADD_ITEM'; name: string; quantity: number; cost: string; notes: string; requiresAttunement: boolean; storageLocation?: string }
  | { type: 'REMOVE_ITEM'; itemKey: string }
  | { type: 'TOGGLE_ITEM_EQUIPPED'; itemKey: string }
  | { type: 'TOGGLE_ITEM_ATTUNED'; itemKey: string }
  | { type: 'SET_ITEM_QUANTITY'; itemKey: string; quantity: number }
  | { type: 'ADD_CATALOGUE_ITEM'; catalogueEntryId: string }
  | { type: 'MOVE_ITEM'; itemKey: string; storageLocation: string }
  | { type: 'SET_TRACK_ENCUMBRANCE'; trackEncumbrance: boolean }
  | { type: 'ADD_COINS'; denomination: string; amount: number }
  | { type: 'REMOVE_COINS'; denomination: string; amount: number }
  | { type: 'EXTRA_DAMAGE'; extraKey: string; amount: number }
  | { type: 'EXTRA_HEAL'; extraKey: string; amount: number }
  | { type: 'EXTRA_TEMPORARY_HIT_POINTS'; extraKey: string; amount: number }
  | { type: 'REMOVE_EXTRA'; extraKey: string }
  | { type: 'USE_FEATURE_ACTION'; featureKey: string }
  | { type: 'RESTORE_FEATURE_ACTION'; featureKey: string }
  | { type: 'USE_FEATURE_TRAIT'; featureKey: string }
  | { type: 'RESTORE_FEATURE_TRAIT'; featureKey: string }
  | { type: 'CONSUME_SPELL_SLOT'; level: number; pact?: boolean }
  | { type: 'RESTORE_SPELL_SLOT'; level: number; pact?: boolean }
  | { type: 'CAST_ITEM_GRANTED_SPELL'; spellKey: string }
  | { type: 'CAST_SPELL'; spellKey: string; slotLevel: number; onSelf: boolean; pact?: boolean }
  | { type: 'END_ACTIVE_EFFECT'; effectKey: string }
  | { type: 'LEARN_SPELL'; catalogueEntryId: string; className: string }
  | { type: 'REMOVE_SPELL'; spellKey: string }
  | { type: 'PREPARE_SPELL'; spellKey: string }
  | { type: 'UNPREPARE_SPELL'; spellKey: string }
  | { type: 'ADD_CUSTOM_ACTION'; action: NewCustomAction }
  | { type: 'UPDATE_CUSTOM_ACTION'; actionKey: string; action: NewCustomAction }
  | { type: 'REMOVE_CUSTOM_ACTION'; actionKey: string }
  | { type: 'LEARN_FEAT'; catalogueEntryId: string }
  | { type: 'REMOVE_FEAT'; featureKey: string };

/** Bound by CharacterSheetScreen to the optimistic-update/rollback flow — leaf mutate targets just call it. */
export type MutationHandler = (action: MutationAction) => void;

export function postMutationAction(characterId: string, action: MutationAction): Promise<CharacterSheet> {
  switch (action.type) {
    case 'DAMAGE':
      return postDamage(characterId, action.amount);
    case 'HEAL':
      return postHealing(characterId, action.amount);
    case 'SET_DEATH_SAVES':
      return putDeathSaves(characterId, action.successes, action.failures);
    case 'SET_EXPERIENCE':
      return postJson(characterId, '/experience', { set: action.points });
    case 'SET_SHEET_THEME':
      return putSheetTheme(characterId, action.theme);
    case 'CUSTOMIZE':
      return putCustomization(characterId, action.group, action.target, action.value);
    case 'REMOVE_CUSTOMIZATION':
      return deleteCustomization(characterId, action.group, action.target);
    case 'TEMPORARY_HIT_POINTS':
      return postTemporaryHitPoints(characterId, action.amount);
    case 'TOGGLE_INSPIRATION':
      return postInspirationToggle(characterId);
    case 'TOGGLE_CONDITION':
      return postConditionToggle(characterId, action.condition);
    case 'SET_EXHAUSTION_LEVEL':
      return postSetExhaustionLevel(characterId, action.level);
    case 'ADD_ITEM':
      return postAddItem(
        characterId, action.name, action.quantity, action.cost, action.notes, action.requiresAttunement,
        action.storageLocation,
      );
    case 'REMOVE_ITEM':
      return deleteItem(characterId, action.itemKey);
    case 'TOGGLE_ITEM_EQUIPPED':
      return postToggleItemEquipped(characterId, action.itemKey);
    case 'TOGGLE_ITEM_ATTUNED':
      return postToggleItemAttuned(characterId, action.itemKey);
    case 'SET_ITEM_QUANTITY':
      return postSetItemQuantity(characterId, action.itemKey, action.quantity);
    case 'ADD_CATALOGUE_ITEM':
      return postAddCatalogueItem(characterId, action.catalogueEntryId);
    case 'MOVE_ITEM':
      return postMoveItem(characterId, action.itemKey, action.storageLocation);
    case 'SET_TRACK_ENCUMBRANCE':
      return postSetTrackEncumbrance(characterId, action.trackEncumbrance);
    case 'ADD_COINS':
      return postAddCoins(characterId, action.denomination, action.amount);
    case 'REMOVE_COINS':
      return deleteCoins(characterId, action.denomination, action.amount);
    case 'EXTRA_DAMAGE':
      return postExtraDamage(characterId, action.extraKey, action.amount);
    case 'EXTRA_HEAL':
      return postExtraHealing(characterId, action.extraKey, action.amount);
    case 'EXTRA_TEMPORARY_HIT_POINTS':
      return postExtraTemporaryHitPoints(characterId, action.extraKey, action.amount);
    case 'REMOVE_EXTRA':
      return deleteExtra(characterId, action.extraKey);
    case 'USE_FEATURE_ACTION':
      return postUseFeatureAction(characterId, action.featureKey);
    case 'RESTORE_FEATURE_ACTION':
      return postRestoreFeatureAction(characterId, action.featureKey);
    case 'USE_FEATURE_TRAIT':
      return postUseFeatureTraitUse(characterId, action.featureKey);
    case 'RESTORE_FEATURE_TRAIT':
      return postRestoreFeatureTraitUse(characterId, action.featureKey);
    case 'CONSUME_SPELL_SLOT':
      return postConsumeSpellSlot(characterId, action.level, action.pact ?? false);
    case 'RESTORE_SPELL_SLOT':
      return postRestoreSpellSlot(characterId, action.level, action.pact ?? false);
    case 'CAST_ITEM_GRANTED_SPELL':
      return postCastItemGrantedSpell(characterId, action.spellKey);
    case 'CAST_SPELL':
      return postCastSpell(characterId, action.spellKey, action.slotLevel, action.onSelf, action.pact ?? false);
    case 'END_ACTIVE_EFFECT':
      return postEndActiveEffect(characterId, action.effectKey);
    case 'LEARN_SPELL':
      return postLearnSpell(characterId, action.catalogueEntryId, action.className);
    case 'REMOVE_SPELL':
      return deleteSpell(characterId, action.spellKey);
    case 'PREPARE_SPELL':
      return postPrepareSpell(characterId, action.spellKey);
    case 'UNPREPARE_SPELL':
      return postUnprepareSpell(characterId, action.spellKey);
    case 'ADD_CUSTOM_ACTION':
      return postAddCustomAction(characterId, action.action);
    case 'UPDATE_CUSTOM_ACTION':
      return putCustomAction(characterId, action.actionKey, action.action);
    case 'REMOVE_CUSTOM_ACTION':
      return deleteCustomAction(characterId, action.actionKey);
    case 'LEARN_FEAT':
      return postLearnFeat(characterId, action.catalogueEntryId);
    case 'REMOVE_FEAT':
      return deleteFeat(characterId, action.featureKey);
  }
}

export async function getCharacterSheet(characterId: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/sheet`);
  if (!response.ok) {
    throw new Error(`Failed to load character sheet: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

async function postMutation(characterId: string, path: string, amount?: number): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}${path}`, {
    method: 'POST',
    ...(amount !== undefined && {
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount }),
    }),
  });
  if (!response.ok) {
    throw new Error(`Mutation failed to reach the server: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export function postDamage(characterId: string, amount: number): Promise<CharacterSheet> {
  return postMutation(characterId, '/hit-points/damage', amount);
}

export function postHealing(characterId: string, amount: number): Promise<CharacterSheet> {
  return postMutation(characterId, '/hit-points/heal', amount);
}

export function postTemporaryHitPoints(characterId: string, amount: number): Promise<CharacterSheet> {
  return postMutation(characterId, '/hit-points/temporary', amount);
}

export function postInspirationToggle(characterId: string): Promise<CharacterSheet> {
  return postMutation(characterId, '/inspiration/toggle');
}

export function postConditionToggle(characterId: string, condition: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/conditions/${condition}/toggle`);
}

export function postSetExhaustionLevel(characterId: string, level: number): Promise<CharacterSheet> {
  return postJson(characterId, '/exhaustion', { level });
}

async function postJson(characterId: string, path: string, body: unknown): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(`Mutation failed to reach the server: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export function postAddItem(
  characterId: string, name: string, quantity: number, cost: string, notes: string, requiresAttunement: boolean,
  storageLocation?: string,
): Promise<CharacterSheet> {
  return postJson(characterId, '/items', { name, quantity, cost, notes, requiresAttunement, storageLocation });
}

export async function deleteItem(characterId: string, itemKey: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/items/${encodeURIComponent(itemKey)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to remove item: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export async function deleteExtra(characterId: string, extraKey: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/extras/${encodeURIComponent(extraKey)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to remove extra: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export function postToggleItemEquipped(characterId: string, itemKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/items/${encodeURIComponent(itemKey)}/equip/toggle`);
}

export function postToggleItemAttuned(characterId: string, itemKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/items/${encodeURIComponent(itemKey)}/attune/toggle`);
}

export function postSetItemQuantity(characterId: string, itemKey: string, quantity: number): Promise<CharacterSheet> {
  return postJson(characterId, `/items/${encodeURIComponent(itemKey)}/quantity`, { quantity });
}

export function postAddCatalogueItem(characterId: string, catalogueEntryId: string): Promise<CharacterSheet> {
  return postJson(characterId, '/items/from-catalogue', { catalogueEntryId });
}

export function postMoveItem(characterId: string, itemKey: string, storageLocation: string): Promise<CharacterSheet> {
  return postJson(characterId, `/items/${encodeURIComponent(itemKey)}/move`, { storageLocation });
}

export function postSetTrackEncumbrance(characterId: string, trackEncumbrance: boolean): Promise<CharacterSheet> {
  return postJson(characterId, '/encumbrance/tracking', { trackEncumbrance });
}

export function postAddCoins(characterId: string, denomination: string, amount: number): Promise<CharacterSheet> {
  return postJson(characterId, `/coins/${denomination}`, { amount });
}

export async function deleteCoins(characterId: string, denomination: string, amount: number): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/coins/${denomination}?amount=${amount}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to remove coins: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export function postExtraDamage(characterId: string, extraKey: string, amount: number): Promise<CharacterSheet> {
  return postMutation(characterId, `/extras/${encodeURIComponent(extraKey)}/hit-points/damage`, amount);
}

export function postExtraHealing(characterId: string, extraKey: string, amount: number): Promise<CharacterSheet> {
  return postMutation(characterId, `/extras/${encodeURIComponent(extraKey)}/hit-points/heal`, amount);
}

export function postExtraTemporaryHitPoints(characterId: string, extraKey: string, amount: number): Promise<CharacterSheet> {
  return postMutation(characterId, `/extras/${encodeURIComponent(extraKey)}/hit-points/temporary`, amount);
}

export function postUseFeatureAction(characterId: string, featureKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/feature-actions/${encodeURIComponent(featureKey)}/use`);
}

export function postRestoreFeatureAction(characterId: string, featureKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/feature-actions/${encodeURIComponent(featureKey)}/restore`);
}

export function postUseFeatureTraitUse(characterId: string, featureKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/feature-traits/${encodeURIComponent(featureKey)}/use`);
}

export function postRestoreFeatureTraitUse(characterId: string, featureKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/feature-traits/${encodeURIComponent(featureKey)}/restore`);
}

export function postConsumeSpellSlot(characterId: string, level: number, pact = false): Promise<CharacterSheet> {
  return postMutation(characterId, `/spell-slots/${level}/consume${pact ? '?pact=true' : ''}`);
}

export function postRestoreSpellSlot(characterId: string, level: number, pact = false): Promise<CharacterSheet> {
  return postMutation(characterId, `/spell-slots/${level}/restore${pact ? '?pact=true' : ''}`);
}

export function postCastItemGrantedSpell(characterId: string, spellKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/spells/${spellKey}/cast-from-item`);
}

export function postCastSpell(characterId: string, spellKey: string, slotLevel: number, onSelf: boolean, pact = false): Promise<CharacterSheet> {
  return postJson(characterId, `/spells/${encodeURIComponent(spellKey)}/cast`, { slotLevel, onSelf, pact });
}

export function postEndActiveEffect(characterId: string, effectKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/active-effects/${encodeURIComponent(effectKey)}/end`);
}

export function postLearnSpell(characterId: string, catalogueEntryId: string, className: string): Promise<CharacterSheet> {
  return postJson(characterId, '/spells/learn', { catalogueEntryId, className });
}

export async function deleteSpell(characterId: string, spellKey: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/spells/${encodeURIComponent(spellKey)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to remove spell: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export function postLearnFeat(characterId: string, catalogueEntryId: string): Promise<CharacterSheet> {
  return postJson(characterId, '/feats/learn', { catalogueEntryId });
}

export async function deleteFeat(characterId: string, featureKey: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/feats/${encodeURIComponent(featureKey)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to remove feat: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export function postPrepareSpell(characterId: string, spellKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/spells/${encodeURIComponent(spellKey)}/prepare`);
}

export function postUnprepareSpell(characterId: string, spellKey: string): Promise<CharacterSheet> {
  return postMutation(characterId, `/spells/${encodeURIComponent(spellKey)}/unprepare`);
}

export function postAddCustomAction(characterId: string, action: NewCustomAction): Promise<CharacterSheet> {
  return postJson(characterId, '/custom-actions', action);
}

export async function putCustomAction(characterId: string, actionKey: string, action: NewCustomAction): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/custom-actions/${encodeURIComponent(actionKey)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(action),
  });
  if (!response.ok) {
    throw new Error(`Failed to update custom action: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

export async function deleteCustomAction(characterId: string, actionKey: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/custom-actions/${encodeURIComponent(actionKey)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to remove custom action: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

/**
 * A hit dice spend is both a roll and a mutation at once (see
 * `Dnd5eSheetMutator.spendHitDice`'s doc comment) — the response carries both,
 * unlike every other mutation here, which only returns the sheet.
 */
export type SpendHitDiceResult = {
  sheet: CharacterSheet;
  roll: RollResult;
};

async function putSheetTheme(characterId: string, theme: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/appearance`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ theme }),
  });
  if (!response.ok) {
    throw new Error(`Failed to change the sheet theme: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

async function putCustomization(
  characterId: string, group: string, target: string, value: Record<string, unknown>,
): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/customizations/${group}/${target}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(value),
  });
  if (!response.ok) {
    throw new Error(`Failed to save the customization: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

async function deleteCustomization(characterId: string, group: string, target: string): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/customizations/${group}/${target}`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error(`Failed to remove the customization: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

async function putDeathSaves(characterId: string, successes: number, failures: number): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/death-saves`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ successes, failures }),
  });
  if (!response.ok) {
    throw new Error(`Failed to set death saves: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

/** Rolls a death saving throw server-side; the roll and the updated sheet come back together, like a hit dice spend. */
export async function postDeathSaveRoll(characterId: string): Promise<SpendHitDiceResult> {
  const response = await apiFetch(`/api/characters/${characterId}/death-saves/roll`, { method: 'POST' });
  if (!response.ok) {
    throw new Error(`Failed to roll a death save: ${response.status}`);
  }
  return response.json() as Promise<SpendHitDiceResult>;
}

export async function postSpendHitDice(characterId: string, count: number, dieSize?: number): Promise<SpendHitDiceResult> {
  const response = await apiFetch(`/api/characters/${characterId}/hit-dice/spend`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ count, dieSize }),
  });
  if (!response.ok) {
    throw new Error(`Failed to spend hit dice: ${response.status}`);
  }
  return response.json() as Promise<SpendHitDiceResult>;
}

/**
 * A short rest optionally spends hit dice in the same operation (matching
 * D&D Beyond's own short rest panel) — `roll` is null when `hitDiceSpent` is
 * zero, not a zero-dice roll record.
 */
export type ShortRestResult = {
  sheet: CharacterSheet;
  roll: RollResult | null;
  /** One roll per die size spent. */
  rolls: RollResult[];
};

export async function postShortRest(characterId: string, hitDiceBySize: HitDiceBySize): Promise<ShortRestResult> {
  const response = await apiFetch(`/api/characters/${characterId}/rest/short`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ hitDiceSpent: 0, hitDiceBySize }),
  });
  if (!response.ok) {
    throw new Error(`Failed to take a short rest: ${response.status}`);
  }
  return response.json() as Promise<ShortRestResult>;
}

export function postUpdateBackgroundField(characterId: string, field: BackgroundField, value: string): Promise<CharacterSheet> {
  return postJson(characterId, `/background/${field}`, { value });
}

/** A long rest needs no player input — every consequence is automatic, so it returns just the updated sheet. */
/** hitDiceRecovered is the player's choice of spent dice to recover; omitted, the server chooses. */
export async function postLongRest(characterId: string, hitDiceRecovered?: HitDiceBySize): Promise<CharacterSheet> {
  const response = await apiFetch(`/api/characters/${characterId}/rest/long`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ hitDiceRecovered: hitDiceRecovered ?? null }),
  });
  if (!response.ok) {
    throw new Error(`Failed to take a long rest: ${response.status}`);
  }
  return response.json() as Promise<CharacterSheet>;
}

import type { CharacterSheet, ProficiencyLevel } from '../../sheet/api';

/** A saving throw's or a skill's Customize values, as the D&D 5e sheet stores them (null: not set). */
export type CheckCustomization = {
  override: number | null;
  overrideNotes: string | null;
  magicBonus: number | null;
  magicBonusNotes: string | null;
  miscBonus: number | null;
  miscBonusNotes: string | null;
  proficiencyLevel: ProficiencyLevel | null;
  proficiencyLevelNotes: string | null;
  statOverride: string | null;
  statOverrideNotes: string | null;
};

/** A custom skill as stored (the calculated bonus comes with the sheet's `customSkills`). */
export type StoredCustomSkill = {
  key: string;
  name: string;
  statOverride: string | null;
  proficiencyLevel: ProficiencyLevel;
  override: number | null;
  magicBonus: number | null;
  miscBonus: number | null;
  notes: string | null;
  description: string | null;
};

/** A hand-set number with its source notes (a passive override, a sense's distance). */
export type NotedValue = { value: number | null; notes: string | null };

/** A defense the player added: a damage type, or a condition for an immunity. */
export type StoredCustomDefense = {
  key: string;
  type: 'RESISTANCE' | 'IMMUNITY' | 'VULNERABILITY';
  subtype: string;
  notes: string | null;
};

export type ProficiencyType = 'ARMOR' | 'WEAPON' | 'TOOL' | 'LANGUAGE';

/** A proficiency the player added: an existing one picked by name, or a custom one named by hand ('' until named). */
export type StoredCustomProficiency = {
  key: string;
  type: ProficiencyType;
  name: string;
  custom: boolean;
  notes: string | null;
};

/** An item's Customize values as stored: cost in gp, weight in lb; null or false means not set. */
export type ItemCustomization = {
  toHitOverride: number | null;
  toHitBonus: number | null;
  damageBonus: number | null;
  costOverride: number | null;
  weightOverride: number | null;
  silvered: boolean;
  adamantine: boolean;
  displayAsAttack: boolean;
  name: string | null;
  notes: string | null;
};

/** A spell's Customize values as stored; null or false means not set. */
export type SpellCustomization = {
  toHitOverride: number | null;
  toHitBonus: number | null;
  damageBonus: number | null;
  dcOverride: number | null;
  dcBonus: number | null;
  displayAsAttack: boolean;
  name: string | null;
  notes: string | null;
};

export const EMPTY_ITEM_CUSTOMIZATION: ItemCustomization = {
  toHitOverride: null,
  toHitBonus: null,
  damageBonus: null,
  costOverride: null,
  weightOverride: null,
  silvered: false,
  adamantine: false,
  displayAsAttack: false,
  name: null,
  notes: null,
};

export const EMPTY_SPELL_CUSTOMIZATION: SpellCustomization = {
  toHitOverride: null,
  toHitBonus: null,
  damageBonus: null,
  dcOverride: null,
  dcBonus: null,
  displayAsAttack: false,
  name: null,
  notes: null,
};

export type Dnd5eCustomizations = {
  savingThrows: Record<string, CheckCustomization>;
  skills: Record<string, CheckCustomization>;
  customSkills: StoredCustomSkill[];
  passives: Record<string, NotedValue>;
  senses: Record<string, NotedValue>;
  speeds: Record<string, NotedValue>;
  movementDisplay: string | null;
  armorClass: Record<string, NotedValue>;
  defenses: StoredCustomDefense[];
  items: Record<string, ItemCustomization>;
  spells: Record<string, SpellCustomization>;
  hitPoints: Record<string, NotedValue>;
  proficiencies: StoredCustomProficiency[];
};

export const EMPTY_CHECK: CheckCustomization = {
  override: null,
  overrideNotes: null,
  magicBonus: null,
  magicBonusNotes: null,
  miscBonus: null,
  miscBonusNotes: null,
  proficiencyLevel: null,
  proficiencyLevelNotes: null,
  statOverride: null,
  statOverrideNotes: null,
};

/** The D&D 5e customizations the API sends with the sheet's provenance. */
export function customizationsOf(sheet: CharacterSheet): Dnd5eCustomizations {
  const stored = sheet.provenance?.customizations as Partial<Dnd5eCustomizations> | undefined;
  return {
    savingThrows: stored?.savingThrows ?? {},
    skills: stored?.skills ?? {},
    customSkills: stored?.customSkills ?? [],
    passives: stored?.passives ?? {},
    senses: stored?.senses ?? {},
    speeds: stored?.speeds ?? {},
    movementDisplay: stored?.movementDisplay ?? null,
    armorClass: stored?.armorClass ?? {},
    defenses: stored?.defenses ?? [],
    items: stored?.items ?? {},
    spells: stored?.spells ?? {},
    hitPoints: stored?.hitPoints ?? {},
    proficiencies: stored?.proficiencies ?? [],
  };
}

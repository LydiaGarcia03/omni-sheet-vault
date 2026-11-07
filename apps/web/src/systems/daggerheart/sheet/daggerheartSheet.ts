import { TRAITS, type DaggerheartBuild, type TraitId } from '../builder/daggerheartBuild';

export type Contribution = { source: string; amount: number };
export type Calculated = { value: number; contributions: Contribution[] };
export type Reference = { slug: string; name: string };
export type SheetFeature = { source: string; origin: string; name: string; text: string; modifiers?: { target: string; value: number }[] };
export type SheetDomainCard = { slug: string; name: string; domain: string; level: number; type: string; recallCost: number; text: string; inLoadout: boolean };
export type SheetWeapon = {
  slug: string;
  name: string;
  slot: string;
  tier: number;
  trait: string;
  range: string;
  damage: { die: string; bonus: number; type: string };
  burden: number;
  feature: { name: string; text: string } | null;
};
export type SheetArmor = { slug: string; name: string; tier: number; baseThresholds: { major: number; severe: number }; baseScore: number; feature: { name: string; text: string } | null };
export type Counter = { name: string; value: number; max: number };
export type Attack = { name: string; slot: string; trait: string; modifier: number; range: string; damage: string; damageType: string; burden: number };

/** The stored sheet plus the values the API derives from it (`GET /api/daggerheart/characters/{id}/sheet`). */
export type DaggerheartSheet = {
  level: number;
  proficiency: number;
  identity: {
    class: Reference | null;
    subclass: (Reference & { spellcastTrait: string | null }) | null;
    domains: Reference[];
    ancestry: { slug?: string; name: string; mixed?: boolean; from?: Reference[] } | null;
    community: Reference | null;
    transformation: Reference | null;
  };
  traits: Partial<Record<TraitId, number>>;
  features: SheetFeature[];
  experiences: { name: string; bonus: number }[];
  domainCards: SheetDomainCard[];
  weapons: { primary: SheetWeapon | null; secondary: SheetWeapon | null };
  armor: SheetArmor | null;
  inventory: { slug?: string; name: string; quantity: number }[];
  gold: { handfuls: number; bags: number; chests: number };
  trackers: { hitPointsMarked: number; stressMarked: number; armorSlotsMarked: number; hope: number };
  conditions: string[];
  notes: string;
  counters?: Counter[];
  lastDamage?: { amount: number; hitPoints: number; armorUsed: boolean; fell: boolean };
  lastRest?: { kind: 'short' | 'long'; moves: { move: string; forAlly: boolean; rolled?: number; tier?: number; cleared?: number; gained?: number }[] };
  shortRestsInARow?: number;
  build: DaggerheartBuild;
  derived: {
    traits: Record<TraitId, Calculated>;
    evasion: Calculated;
    armorScore: Calculated;
    majorThreshold: Calculated;
    severeThreshold: Calculated;
    hitPointSlots: Calculated;
    stressSlots: Calculated;
    maxHope: number;
    proficiency: number;
    spellcastTrait: string;
    attacks: Attack[];
  };
};

/** How many domain cards the loadout holds; the rest wait in the vault (SRD p. 6). */
export const LOADOUT_SIZE = 5;
/** Every Hope feature costs 3 Hope. */
export const HOPE_FEATURE_COST = 3;
export const MAX_PROFICIENCY = 6;

export const FEATURE_SOURCES: Record<string, string> = {
  hope: 'Hope feature',
  class: 'Class feature',
  subclass: 'Subclass · Foundation',
  specialization: 'Subclass · Specialization',
  mastery: 'Subclass · Mastery',
  multiclass: 'Multiclass',
  ancestry: 'Ancestry',
  community: 'Community',
  transformation: 'Transformation',
};

/** "Slyborne Fungril Ranger", with "(Wayfinder)" as the emphasised part. */
export function heritageLine(sheet: DaggerheartSheet): { lead: string; subclass: string } {
  const { community, ancestry, class: playerClass, subclass } = sheet.identity;
  return {
    lead: [community?.name, ancestry?.name, playerClass?.name].filter(Boolean).join(' '),
    subclass: subclass?.name ?? '',
  };
}

export function domainCaption(sheet: DaggerheartSheet): string {
  return sheet.identity.domains.map((domain) => domain.name).join(' & ');
}

export function loadout(sheet: DaggerheartSheet): SheetDomainCard[] {
  return sheet.domainCards.filter((card) => card.inLoadout);
}

export function vault(sheet: DaggerheartSheet): SheetDomainCard[] {
  return sheet.domainCards.filter((card) => !card.inLoadout);
}

/** The class and subclass features first (Hope feature leading), then heritage. */
export function featuresBy(sheet: DaggerheartSheet, sources: string[]): SheetFeature[] {
  return sources.flatMap((source) => sheet.features.filter((feature) => feature.source === source));
}

export function traitName(id: string): string {
  return TRAITS.find((trait) => trait.id === id.toLowerCase())?.name ?? id;
}

/** The weapon's line under its name: "Agility · Far range · d6+3 physical damage". */
export function weaponLine(weapon: SheetWeapon, attack: Attack | undefined): string {
  const damage = attack?.damage ?? `${weapon.damage.die}${weapon.damage.bonus > 0 ? `+${weapon.damage.bonus}` : ''}`;
  return `${attack?.trait || weapon.trait} · ${weapon.range} range · ${damage} ${weapon.damage.type} damage`;
}

export function burdenLabel(burden: number): string {
  return burden >= 2 ? 'Two-handed' : 'One-handed';
}

export const STANDARD_CONDITIONS = ['Hidden', 'Restrained', 'Vulnerable'];
export const SHORT_RESTS_BEFORE_LONG = 3;

export type RestMove = { id: string; name: string; text: string; ally?: boolean; party?: boolean };

/** The downtime moves of each rest (SRD p. 52); each rest takes two, the same one allowed twice. */
export const REST_MOVES: Record<'short' | 'long', RestMove[]> = {
  short: [
    { id: 'tendToWounds', name: 'Tend to Wounds', text: 'Clear 1d4 + Tier Hit Points for yourself or an ally.', ally: true },
    { id: 'clearStress', name: 'Clear Stress', text: 'Clear 1d4 + Tier Stress.' },
    { id: 'repairArmor', name: 'Repair Armor', text: "Clear 1d4 + Tier Armor Slots from your or an ally's armor.", ally: true },
    { id: 'prepare', name: 'Prepare', text: 'Gain a Hope, or 2 Hope each if you prepare with party members.', party: true },
  ],
  long: [
    { id: 'tendToAllWounds', name: 'Tend to All Wounds', text: 'Clear all Hit Points for yourself or an ally.', ally: true },
    { id: 'clearAllStress', name: 'Clear All Stress', text: 'Clear all Stress.' },
    { id: 'repairAllArmor', name: 'Repair All Armor', text: "Clear all Armor Slots from your or an ally's armor.", ally: true },
    { id: 'prepare', name: 'Prepare', text: 'Gain a Hope, or 2 Hope each if you prepare with party members.', party: true },
    { id: 'workOnAProject', name: 'Work on a Project', text: "With the GM's approval, advance a long-term project's countdown." },
  ],
};

export function restMoveName(id: string): string {
  return [...REST_MOVES.short, ...REST_MOVES.long].find((move) => move.id === id)?.name ?? id;
}

/** The change a click on slot `index` makes: a marked slot clears from there on, an empty one marks up to it. */
export function slotDelta(index: number, marked: number): number {
  return (index < marked ? index : index + 1) - marked;
}

export function hasStory(build: DaggerheartBuild): boolean {
  const story = build.story ?? {};
  return Boolean(build.pronouns || story.description || story.background || story.connections);
}

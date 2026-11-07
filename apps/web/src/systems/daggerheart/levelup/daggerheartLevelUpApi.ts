import { apiFetch } from '../../../api/client';

export type Advancement = {
  option: string;
  tier: number;
  traits?: string[];
  experiences?: number[];
  card?: string;
  class?: string;
  domain?: string;
  subclass?: string;
};

/** The next level's entry, as the build's `levels` keeps it. */
export type LevelEntry = { experience?: string; domainCard?: string; advancements?: Advancement[] };

export type GridOption = { id: string; label: string; slots: number; marked: number; picked: number; takesBothPicks: boolean; crossedOut: boolean };
export type CardOption = { slug: string; name: string; domain: string; level: number; type: string; recallCost: number; text: string };
export type MulticlassOption = {
  slug: string;
  name: string;
  domains: string[];
  features: { name: string; text: string }[];
  subclasses: { slug: string; name: string; foundation: { name: string; text: string }[] }[];
};
export type LevelSummary = {
  level: number;
  proficiency: number;
  evasion: number;
  armorScore: number;
  majorThreshold: number;
  severeThreshold: number;
  hitPointSlots: number;
  stressSlots: number;
  traits: Record<string, number>;
  experiences: { name: string; bonus: number }[];
  domainCards: string[];
};

/** `GET /api/daggerheart/characters/{id}/level-up`: the next level, its grid and options, and the sheet before and after. */
export type LevelUpView = {
  level: number;
  nextLevel: number;
  tier: number;
  atMaxLevel: boolean;
  draft: LevelEntry;
  achievement: { experience: boolean; proficiency: boolean; clearsTraitMarks: boolean };
  grid: { tier: number; open: boolean; options: GridOption[] }[];
  markedTraits: string[];
  experiences: { name: string; bonus: number }[];
  cardOptions: CardOption[];
  multiclassOptions: MulticlassOption[];
  subclassNext: { card: string; subclass?: string; features?: { name: string; text: string }[] };
  problems: string[];
  missing: string[];
  before: LevelSummary;
  after: LevelSummary;
  newFeatures: { source: string; origin: string; name: string; text: string }[];
};

const path = (characterId: string) => `/api/daggerheart/characters/${characterId}/level-up`;

async function read<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new Error(problem?.detail ?? 'The level up could not be saved.');
  }
  return response.json();
}

export async function getLevelUp(characterId: string): Promise<LevelUpView> {
  return read(await apiFetch(path(characterId)));
}

export async function saveLevelUp(characterId: string, entry: LevelEntry): Promise<LevelUpView> {
  return read(await apiFetch(path(characterId), { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(entry) }));
}

export async function cancelLevelUp(characterId: string): Promise<void> {
  await apiFetch(path(characterId), { method: 'DELETE' });
}

export async function finishLevelUp(characterId: string): Promise<void> {
  await read(await apiFetch(`${path(characterId)}/finish`, { method: 'POST' }));
}

/** How many of a level's two picks the entry uses; Proficiency and multiclassing take both. */
export function picksUsed(entry: LevelEntry, grid: LevelUpView['grid']): number {
  const both = new Set(grid.flatMap((tier) => tier.options.filter((option) => option.takesBothPicks).map((option) => option.id)));
  return (entry.advancements ?? []).reduce((total, advancement) => total + (both.has(advancement.option) ? 2 : 1), 0);
}

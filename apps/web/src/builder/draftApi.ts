import { apiFetch } from '../api/client';
import type { Character } from '../characters/api';
import type { Portrait } from '../characters/portrait';

export type ChoiceOption = {
  key: string;
  label: string;
  sourceBook: string | null;
  summary: string | null;
  /** The system's machine-readable rules for this option, when a UI needs them (e.g. a point-buy cost table). */
  data?: unknown;
};

export type CreationChoice = {
  id: string;
  type: string;
  parentChoiceId: string | null;
  prompt: string;
  sourceLabel: string;
  count: number;
  optional: boolean;
  pending: boolean;
  options: ChoiceOption[];
  selected: string[];
  /** The class (by key) and level the choice arises at; a null level belongs to the class as a whole. Missing on older payloads. */
  group?: string | null;
  level?: number | null;
};

export type BuildPlan = {
  choices: CreationChoice[];
  problems: string[];
  pendingCount: number;
};

export type Contribution = { source: string; amount: number };
export type CalculatedValue = { value: number; contributions: Contribution[] };

export type AbilityPreview = { ability: string; score: number; modifier: number; contributions: Contribution[] };

export type VitalsPreview = {
  level: number;
  hitPoints: CalculatedValue;
  hitDice: string;
  armorClass: CalculatedValue;
  speed: number;
  initiative: number;
  proficiencyBonus: number;
  savingThrows: { name: string; value: number }[];
  passivePerception: number;
  attacks: { name: string; toHit: number; damage: string }[];
  features: string[];
  skills: string[];
  armor: string[];
  weapons: string[];
  tools: string[];
  languages: string[];
  resistances: string[];
  senses: string[];
};

/** The live summary: ability scores always, `vitals` once the draft can form a sheet (a class and every base score). */
/**
 * A class's level-by-level table: one row per level from 1, one cell per column; `key` matches its choices' `group`,
 * and `currentLevel` is the level reached.
 */
export type ProgressionTable = { key: string; name: string; currentLevel: number; columns: string[]; rows: string[][] };

/** What a chosen option gives: short facts ("Speed" · "25 ft") and titled grants, whose text is null when prose is redacted. */
export type SelectionDetail = {
  name: string;
  sourceBook: string | null;
  facts: { label: string; text: string }[];
  grants: { label: string; text: string | null }[];
};

export type DraftPreview = {
  abilities: AbilityPreview[];
  vitals: VitalsPreview | null;
  /** Missing on payloads from before class progressions existed. */
  progressions?: ProgressionTable[];
  /** Detail cards by step key ("species", "background"); missing on older payloads. */
  selections?: Record<string, SelectionDetail>;
  /** What the character starts carrying; missing on older payloads. */
  startingEquipment?: StartingEquipment;
};

/**
 * A starting-item line: `key` names this line for equipping (null for a free-text item), `equipAction` says whether
 * it can start worn or wielded (null when it can't).
 */
export type StartingInventoryItem = { key: string | null; name: string; quantity: number; equipAction: 'WEAR' | 'WIELD' | null; equipped: boolean };

export type StartingEquipment = { inventory: StartingInventoryItem[]; gold: number; silver: number; copper: number };

/** A creation draft: `build` is the game system's own build document, opaque to the generic builder. */
export type Draft = {
  id: string;
  name: string;
  systemId: string;
  build: unknown;
  plan: BuildPlan;
  preview: DraftPreview;
  portrait: Portrait | null;
};

async function readJson<T>(response: Response, action: string): Promise<T> {
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null;
    throw new Error(problem?.detail ?? `Failed to ${action}: ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export async function createDraft(name: string, systemId: string): Promise<Draft> {
  const response = await apiFetch('/api/characters/drafts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, systemId }),
  });
  return readJson<Draft>(response, 'create the character');
}

export async function getDraft(id: string): Promise<Draft> {
  return readJson<Draft>(await apiFetch(`/api/characters/${id}/draft`), 'load the draft');
}

export async function saveDraft(id: string, name: string, build: unknown): Promise<Draft> {
  const response = await apiFetch(`/api/characters/${id}/draft`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, build }),
  });
  return readJson<Draft>(response, 'save the draft');
}

export async function finishDraft(id: string): Promise<Character> {
  return readJson<Character>(await apiFetch(`/api/characters/${id}/draft/finish`, { method: 'POST' }), 'finish the character');
}

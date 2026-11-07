import { apiFetch } from '../../../api/client';
import type { DaggerheartSheet } from './daggerheartSheet';

/** One roll as the API's history shows it; the Duality fields are zero for damage and rest rolls. */
export type DaggerheartRoll = {
  id: string;
  kind: 'duality' | 'reaction' | 'damage' | 'custom' | 'rest' | 'other';
  label: string;
  dice: number[];
  /** The die of each entry in dice. */
  sides: number[];
  hope: number;
  fear: number;
  advantageDie: number;
  modifier: number;
  total: number;
  difficulty: number;
  critical: boolean;
  withHope: boolean;
  success: boolean | null;
  outcome: string;
  rolledAt: string;
};

export type Advantage = 'none' | 'advantage' | 'disadvantage';

export type DualityRequest = {
  trait: string;
  experiences: number[];
  advantage: Advantage;
  modifier: number;
  difficulty: number;
  reaction: boolean;
  label?: string;
  /** Dice added to the pool from the tray, rolled into the total. */
  dice?: { count: number; sides: number }[];
};

export type DamageRequest = { weapon?: string; count?: number; sides?: number; bonus?: number; critical: boolean; label?: string };

async function post<T>(path: string, body: unknown): Promise<T> {
  const response = await apiFetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new Error(problem?.detail ?? 'The roll failed.');
  }
  return response.json();
}

export function rollDuality(characterId: string, request: DualityRequest): Promise<{ roll: DaggerheartRoll; sheet: DaggerheartSheet }> {
  return post(`/api/daggerheart/characters/${characterId}/rolls/duality`, request);
}

export function rollDamage(characterId: string, request: DamageRequest): Promise<DaggerheartRoll> {
  return post(`/api/daggerheart/characters/${characterId}/rolls/damage`, request);
}

export function rollCustom(characterId: string, dice: { count: number; sides: number }[], bonus: number, critical = false, label?: string): Promise<DaggerheartRoll> {
  return post(`/api/daggerheart/characters/${characterId}/rolls/custom`, { dice, bonus, critical, label });
}

export async function rollHistory(characterId: string): Promise<DaggerheartRoll[]> {
  const response = await apiFetch(`/api/daggerheart/characters/${characterId}/rolls`);
  const rolls: unknown = response.ok ? await response.json().catch(() => []) : [];
  return Array.isArray(rolls) ? rolls : [];
}

/** What a Duality result did to the sheet, in the SRD's words. */
export function outcomeNote(roll: DaggerheartRoll): string {
  if (roll.kind === 'reaction') {
    return roll.critical ? 'Critical on a reaction: ignore what a success would have spared you. No Hope, no Stress cleared.' : 'Reaction rolls make no Hope or Fear.';
  }
  if (roll.critical) {
    return 'You gain a Hope and clear a Stress. On an attack, roll critical damage.';
  }
  return roll.withHope ? 'You gain a Hope.' : 'The GM gains a Fear.';
}

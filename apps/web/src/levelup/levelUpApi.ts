import { apiFetch } from '../api/client';
import type { BuildPlan, DraftPreview } from '../builder/draftApi';

/** A level up in progress: the class leveled, the level it reaches, and the choices that level asks for. */
export type LevelUp = {
  id: string;
  name: string;
  systemId: string;
  classSlug: string;
  classLevel: number;
  /** The game system's own build document with the new level, opaque here. */
  build: unknown;
  plan: BuildPlan;
  preview: DraftPreview;
  maxHitPointsBefore: number;
  /** Null until the build can form a sheet. */
  maxHitPointsAfter: number | null;
};

async function readJson<T>(response: Response, action: string): Promise<T> {
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null;
    throw new Error(problem?.detail ?? `Failed to ${action}: ${response.status}`);
  }
  return response.json() as Promise<T>;
}

/** The level up in progress, or null when none is. */
export async function getLevelUp(characterId: string): Promise<LevelUp | null> {
  const response = await apiFetch(`/api/characters/${characterId}/level-up`);
  if (response.status === 409) {
    return null;
  }
  return readJson<LevelUp>(response, 'load the level up');
}

export async function startLevelUp(characterId: string, classSlug: string): Promise<LevelUp> {
  const response = await apiFetch(`/api/characters/${characterId}/level-up`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ classSlug }),
  });
  return readJson<LevelUp>(response, 'start the level up');
}

export async function saveLevelUp(characterId: string, build: unknown): Promise<LevelUp> {
  const response = await apiFetch(`/api/characters/${characterId}/level-up`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ build }),
  });
  return readJson<LevelUp>(response, 'save the level up');
}

export async function cancelLevelUp(characterId: string): Promise<void> {
  const response = await apiFetch(`/api/characters/${characterId}/level-up`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error(`Failed to cancel the level up: ${response.status}`);
  }
}

export async function finishLevelUp(characterId: string): Promise<void> {
  await readJson<unknown>(await apiFetch(`/api/characters/${characterId}/level-up/finish`, { method: 'POST' }), 'finish leveling up');
}

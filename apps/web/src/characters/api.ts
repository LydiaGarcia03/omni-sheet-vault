import { apiFetch } from '../api/client';
import type { Portrait } from './portrait';

export type CharacterStatus = 'DRAFT' | 'ACTIVE';

export type Character = {
  id: string;
  name: string;
  systemId: string;
  status: CharacterStatus;
  createdAt: string;
  portrait: Portrait | null;
  /** The card's facts, composed by the character's game system on the API, most important first. */
  summary: SummaryFact[];
};

export type SummaryFact = { label: string; value: string };

export async function listCharacters(): Promise<Character[]> {
  const response = await apiFetch('/api/characters');
  if (!response.ok) {
    throw new Error(`Failed to list characters: ${response.status}`);
  }
  return response.json() as Promise<Character[]>;
}

export async function getCharacter(id: string): Promise<Character> {
  const response = await apiFetch(`/api/characters/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to load character: ${response.status}`);
  }
  return response.json() as Promise<Character>;
}

export async function renameCharacter(id: string, name: string): Promise<Character> {
  const response = await apiFetch(`/api/characters/${id}/name`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  });
  if (!response.ok) {
    throw new Error(`Failed to rename character: ${response.status}`);
  }
  return response.json() as Promise<Character>;
}

export async function deleteCharacter(id: string): Promise<void> {
  const response = await apiFetch(`/api/characters/${id}`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error(`Failed to delete character: ${response.status}`);
  }
}

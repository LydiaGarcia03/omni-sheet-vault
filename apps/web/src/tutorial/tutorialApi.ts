import { apiFetch } from '../api/client';

/** Ids of the tutorials the player already finished or skipped; empty when the list can't be read. */
export async function getSeenTutorials(): Promise<string[]> {
  const response = await apiFetch('/api/me/tutorials');
  if (!response.ok) {
    throw new Error('Could not load the tutorial state.');
  }
  const body = (await response.json()) as { seen: string[] };
  return body.seen;
}

export async function markTutorialSeen(tutorialId: string): Promise<void> {
  await apiFetch(`/api/me/tutorials/${encodeURIComponent(tutorialId)}`, { method: 'PUT' });
}

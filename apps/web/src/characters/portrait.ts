import { apiFetch } from '../api/client';

/** A character's portrait as the API describes it: a preset the web app bundles, or an upload behind a short-lived URL. */
export type Portrait = { kind: 'PRESET'; presetId: string; url: null } | { kind: 'UPLOAD'; presetId: null; url: string };

export const PORTRAIT_MAX_BYTES = 3 * 1024 * 1024;
export const PORTRAIT_ACCEPTED_TYPES = ['image/png', 'image/jpeg'];

/** Why a file can't be uploaded as a portrait, checked before sending; null when it can. */
export function portraitFileProblem(file: File): string | null {
  if (!PORTRAIT_ACCEPTED_TYPES.includes(file.type)) {
    return 'Choose a PNG or JPEG image.';
  }
  if (file.size > PORTRAIT_MAX_BYTES) {
    return 'The image is too large: a portrait can be at most 3 MB.';
  }
  return null;
}

async function portraitOrError(response: Response, action: string): Promise<Portrait> {
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null;
    throw new Error(problem?.detail ?? `Failed to ${action}: ${response.status}`);
  }
  return response.json() as Promise<Portrait>;
}

export async function choosePresetPortrait(characterId: string, presetId: string): Promise<Portrait> {
  const response = await apiFetch(`/api/characters/${characterId}/portrait`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ presetId }),
  });
  return portraitOrError(response, 'choose the portrait');
}

export async function uploadPortrait(characterId: string, file: File): Promise<Portrait> {
  const body = new FormData();
  body.append('file', file);
  const response = await apiFetch(`/api/characters/${characterId}/portrait`, { method: 'POST', body });
  return portraitOrError(response, 'upload the portrait');
}

export async function removePortrait(characterId: string): Promise<void> {
  const response = await apiFetch(`/api/characters/${characterId}/portrait`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error(`Failed to remove the portrait: ${response.status}`);
  }
}

import { apiFetch } from '../api/client';
import type { CurrentUser } from '../auth/AuthProvider';

async function userOrError(response: Response, action: string): Promise<CurrentUser> {
  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { detail?: string } | null;
    throw new Error(problem?.detail ?? `Failed to ${action}: ${response.status}`);
  }
  return response.json() as Promise<CurrentUser>;
}

/** Saves the name shown in the app; an empty one goes back to the username. */
export async function saveDisplayName(displayName: string): Promise<CurrentUser> {
  const response = await apiFetch('/api/me/profile', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ displayName }),
  });
  return userOrError(response, 'save the name');
}

export async function uploadAvatar(file: File): Promise<CurrentUser> {
  const body = new FormData();
  body.append('file', file);
  return userOrError(await apiFetch('/api/me/avatar', { method: 'POST', body }), 'upload the picture');
}

export async function removeAvatar(): Promise<CurrentUser> {
  return userOrError(await apiFetch('/api/me/avatar', { method: 'DELETE' }), 'remove the picture');
}

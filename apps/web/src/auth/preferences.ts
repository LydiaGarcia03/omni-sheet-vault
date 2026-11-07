import { apiFetch } from '../api/client';

export type PreferencesUpdate = { locale?: string; theme?: string };

/** Saves the signed-in player's interface preferences; a failure only means the choice isn't kept across devices. */
export async function savePreferences(update: PreferencesUpdate): Promise<void> {
  try {
    await apiFetch('/api/me/preferences', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(update),
    });
  } catch {
    // The browser still keeps the choice.
  }
}

/**
 * Cookies the Keycloak login pages read too (same host, any port): the interface language, the theme, and a marker
 * the login page sets when the player picks a language there.
 */
export const PREFERENCE_COOKIES = { locale: 'osv_locale', theme: 'osv_theme', localePicked: 'osv_locale_picked' } as const;

const ONE_YEAR = 60 * 60 * 24 * 365;

export function readPreferenceCookie(name: string): string | null {
  const entry = document.cookie.split('; ').find((candidate) => candidate.startsWith(`${name}=`));
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : null;
}

export function writePreferenceCookie(name: string, value: string): void {
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${ONE_YEAR}; samesite=lax`;
}

export function clearPreferenceCookie(name: string): void {
  document.cookie = `${name}=; path=/; max-age=0; samesite=lax`;
}

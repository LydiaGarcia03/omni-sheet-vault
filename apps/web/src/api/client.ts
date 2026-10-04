import { csrfToken, redirectToLogin } from '../auth/session';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

/**
 * Calls the API on the same origin with the session cookie; writes carry the CSRF token. The API renews the session's
 * tokens itself, so a 401 means the session is gone: the browser goes back to the login page.
 */
export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers);
  const token = csrfToken();
  if (token && !SAFE_METHODS.has((init.method ?? 'GET').toUpperCase())) {
    headers.set('X-XSRF-TOKEN', token);
  }
  const response = await fetch(path, { ...init, headers });
  if (response.status === 401) {
    redirectToLogin();
  }
  return response;
}

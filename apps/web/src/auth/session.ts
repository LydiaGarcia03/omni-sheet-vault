/** Where the browser returns after login when nothing else was asked for. */
export const DEFAULT_RETURN_PATH = '/';

/** The API's own Keycloak login (adr-0008); the browser only ever holds its session cookie. */
const LOGIN_PATH = '/oauth2/authorization/keycloak';
const LOGOUT_PATH = '/logout';
const CSRF_COOKIE = 'XSRF-TOKEN';

/** Sends the browser to the login page; after signing in it comes back to `returnTo` (an in-app path). */
export function redirectToLogin(returnTo: string = window.location.pathname + window.location.search): void {
  window.location.assign(loginUrl(returnTo));
}

/** Sends the browser to Keycloak's registration page; the new account comes back signed in to `returnTo`. */
export function redirectToRegistration(returnTo: string = DEFAULT_RETURN_PATH): void {
  window.location.assign(`${loginUrl(returnTo)}&signup`);
}

/** Ends the API session and the Keycloak session, then lands on the home page: a form post, so the browser follows the redirects. */
export function signOutOfSession(): void {
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = LOGOUT_PATH;
  const token = document.createElement('input');
  token.type = 'hidden';
  token.name = '_csrf';
  token.value = csrfToken() ?? '';
  form.appendChild(token);
  document.body.appendChild(form);
  form.submit();
}

/** The CSRF token the API sets as a readable cookie; writes send it back in the `X-XSRF-TOKEN` header. */
export function csrfToken(): string | null {
  const cookie = document.cookie.split('; ').find((entry) => entry.startsWith(`${CSRF_COOKIE}=`));
  return cookie ? decodeURIComponent(cookie.slice(CSRF_COOKIE.length + 1)) : null;
}

export function loginUrl(returnTo: string): string {
  return `${LOGIN_PATH}?returnTo=${encodeURIComponent(safeReturnPath(returnTo))}`;
}

/** Only same-app paths: a return target can never send the browser to another site. */
export function safeReturnPath(path: unknown): string {
  return typeof path === 'string' && path.startsWith('/') && !path.startsWith('//') && !path.startsWith('/\\') ? path : DEFAULT_RETURN_PATH;
}

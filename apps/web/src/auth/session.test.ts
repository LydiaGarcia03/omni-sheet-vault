import { afterEach, describe, expect, it } from 'vitest';
import { csrfToken, loginUrl, safeReturnPath } from './session';

describe('safeReturnPath', () => {
  it('keeps an in-app path with its query', () => {
    expect(safeReturnPath('/characters/new/dnd-5e')).toBe('/characters/new/dnd-5e');
    expect(safeReturnPath('/?system=dnd-5e')).toBe('/?system=dnd-5e');
  });

  it('never leaves the app', () => {
    expect(safeReturnPath('https://evil.example/')).toBe('/');
    expect(safeReturnPath('//evil.example/')).toBe('/');
    expect(safeReturnPath('/\\evil.example/')).toBe('/');
    expect(safeReturnPath(undefined)).toBe('/');
    expect(safeReturnPath(42)).toBe('/');
  });
});

describe('loginUrl', () => {
  it("starts the API's login and carries the return path", () => {
    expect(loginUrl('/characters/new/dnd-5e')).toBe('/oauth2/authorization/keycloak?returnTo=%2Fcharacters%2Fnew%2Fdnd-5e');
    expect(loginUrl('https://evil.example/')).toBe('/oauth2/authorization/keycloak?returnTo=%2F');
  });
});

describe('csrfToken', () => {
  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('reads the token the API set as a cookie', () => {
    document.cookie = 'XSRF-TOKEN=abc-123';

    expect(csrfToken()).toBe('abc-123');
  });

  it('is null before the API has set it', () => {
    expect(csrfToken()).toBeNull();
  });
});

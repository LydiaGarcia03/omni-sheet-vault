import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const session = vi.hoisted(() => ({
  csrfToken: vi.fn(),
  redirectToLogin: vi.fn(),
}));
vi.mock('../auth/session', () => session);

import { apiFetch } from './client';

const fetchMock = vi.fn();

function headersOf(call: number): Headers {
  return (fetchMock.mock.calls[call][1] as RequestInit & { headers: Headers }).headers;
}

describe('apiFetch', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock);
    session.csrfToken.mockReturnValue('csrf-1');
    fetchMock.mockResolvedValue(new Response('{}', { status: 200 }));
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.unstubAllGlobals();
  });

  it('calls the API on the same origin and never sends a token of its own', async () => {
    await apiFetch('/api/characters');

    expect(fetchMock.mock.calls[0][0]).toBe('/api/characters');
    expect(headersOf(0).get('Authorization')).toBeNull();
    expect(headersOf(0).get('X-XSRF-TOKEN')).toBeNull();
  });

  it('sends the CSRF token with every write', async () => {
    await apiFetch('/api/characters', { method: 'POST', body: '{"a":1}', headers: { 'Content-Type': 'application/json' } });

    expect(headersOf(0).get('X-XSRF-TOKEN')).toBe('csrf-1');
    expect(headersOf(0).get('Content-Type')).toBe('application/json');
    expect(fetchMock.mock.calls[0][1].body).toBe('{"a":1}');
  });

  it('goes back to the login page when the session is gone', async () => {
    fetchMock.mockResolvedValue(new Response('', { status: 401 }));

    const response = await apiFetch('/api/characters');

    expect(response.status).toBe(401);
    expect(session.redirectToLogin).toHaveBeenCalled();
  });
});

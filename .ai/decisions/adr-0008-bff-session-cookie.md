# ADR-0008 — The API becomes the web app's backend-for-frontend (session cookie)

**Status:** Accepted and implemented (2026-09-26), as slice 3, stages 1–4, of
`features/private-sharing.md`.

## Context

The web app runs the OIDC login itself (`oidc-client-ts`, `auth/oidcConfig.ts`).
It keeps the access, refresh and id tokens in `sessionStorage` and sends
`Authorization: Bearer` to the API. Any XSS flaw could read and steal them.

The owner wants to show the app to a friend over a tunnel or a small VM, and
decided (2026-09-25, `open-items.md`) that tokens must leave the browser first.

This supersedes two current rules:
- `ground-rules.md`, Security: "The API is a resource server. It validates JWTs
  and nothing else."
- `ground-rules.md`, Frontend: "Tokens are handled by the OIDC library."

## Decision

The Spring API runs the Keycloak login (authorization code flow with PKCE, as a
confidential client) and keeps the tokens server-side. The browser only holds a
session cookie.
- **Login:** `GET /oauth2/authorization/keycloak` starts it, and Keycloak
  returns to `/login/oauth2/code/keycloak`. Spring Security's
  `oauth2-client` handles both.
- **Session:** stored in Postgres (Spring Session JDBC, one Flyway migration),
  so a restart doesn't log everyone out. The cookie is
  `HttpOnly; Secure; SameSite=Lax`; `Secure` is relaxed only for local
  `http://localhost`.
- **Tokens:** kept in the session's `OAuth2AuthorizedClient` and refreshed
  server-side before they expire. The browser never sees them.
- **Controllers:** unchanged. A filter turns the session's access token into
  the same `JwtAuthenticationToken` the controllers already receive, so
  `@AuthenticationPrincipal Jwt`, ownership checks and every test keep working.
- **CSRF:** state-changing requests carry the `XSRF-TOKEN` cookie value back
  in an `X-XSRF-TOKEN` header (Spring's cookie CSRF repository). GET requests
  are unaffected.
- **Same origin:** the web app and the API share an origin. Locally, Vite
  proxies `/api`, `/oauth2`, `/login` and `/logout` to 8090. A deployment puts
  a reverse proxy in front of both.
- **Logout:** `POST /logout` ends the session and the Keycloak session (RP-initiated
  logout).
- **Web:** `oidc-client-ts` is removed. The app asks `GET /api/me`; a 401 sends
  the browser to `/oauth2/authorization/keycloak`.
- **Keycloak:** the API logs in with a new confidential client,
  `omni-sheet-vault-bff`. Its secret lives in `.env`
  (`KEYCLOAK_BFF_CLIENT_SECRET`), never in the committed realm export.
  - The public `omni-sheet-vault-web` client keeps the SPA working until
    stage 4, then is removed.
  - This amends the first draft, which converted `omni-sheet-vault-web`
    itself: converting it would have broken the SPA's login mid-migration.
- **Endpoints:** listed in `application.yml` instead of discovered, so the API
  starts without Keycloak. The logout (stage 2) therefore builds Keycloak's
  end-session URL itself.
- **CSRF during the migration:** only requests that carry a session need the
  token. Stage 4 makes it apply to every write.
- **Bearer after stage 4:** not accepted anywhere, and there is no CORS
  configuration. The `oauth2-resource-server` starter stays only for the
  `JwtDecoder` and the JWT types.

## Consequences

- New dependencies, to record in `tech-stack.md`:
  `spring-boot-starter-oauth2-client` and `spring-session-jdbc`.
- One migration adds the Spring Session tables.
- The portrait signed URLs, the login theme and the ownership checks are
  unaffected.
- `ground-rules.md` and `architecture.md` change to say: "The API authenticates
  browsers by session and still validates tokens internally; the browser never
  holds a token".

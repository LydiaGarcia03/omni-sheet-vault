# Feature — Private sharing (license, sign-up, session cookie)

Status: **plan approved 2026-09-26. Slices 1, 2 and 3 done (2026-09-26). Next:
putting it online, planned separately.**

The owner picked the PolyForm Noncommercial License 1.0.0. Sign-up runs on
Keycloak's own registration page, with a "New in the vault? Create an account"
link and protection against mass sign-ups by bots.

The owner wants to show the app to a friend. They won't share or advertise it.
Owner decisions so far (2026-09-26):
- the project is a non-commercial fan project, under a license that says so;
- technical, graphic and content credits go in the footer;
- tokens leave the browser (ADR-0008) before anyone else uses the app;
- there must be a way to create an account.

## Slice 1 — License and credits (done 2026-09-26)

Built as `LICENSE`, `NOTICE.md`, `shell/AppFooter.tsx` and the public
`/credits` page (`shell/CreditsPage.tsx`). The rest of this section is the
original plan.

- A `LICENSE` file at the repository root with the license the owner picks.
  The project's own code and art go under it; third-party material keeps its
  owners' terms.
- A footer on every page (landing, home, builder, sheet) with:
  - the fan content notice required by Wizards of the Coast's Fan Content
    Policy: unofficial fan content, not approved or endorsed by Wizards, and
    portions of the materials are property of Wizards of the Coast;
  - content: rules text and data from 5etools (the 2014 books);
  - graphics: the default portraits and item icons are D&D Beyond / Wizards of
    the Coast art;
  - technical: the main open-source pieces (Spring Boot, React, Keycloak,
    PostgreSQL, MinIO), each with its license.
- A small "Credits" page linked from the footer, if the list is too long for
  the footer itself.

## Slice 2 — Create an account (done 2026-09-26)

What was built:
- **Realm:** `registrationAllowed: true`.
  - Password policy: `length(8) and maxLength(128) and notUsername and
    notEmail`.
  - `bruteForceProtected: true`, with `failureFactor: 10`: temporary lockouts
    after 10 failed logins, waits growing from 60 s to 15 min.
  - `verifyEmail` stays `false`.
  - These settings were also applied to the running realm with `kcadm.sh`,
    because `--import-realm` skips an existing realm.
- **Theme messages:**
  - `noAccount`: "New in the vault?"
  - `doRegister`: "Create an account"
  - `registerTitle`: "Create your vault account"
  - `backToLogin`: "« Back to sign in"
- **Web:** `redirectToRegistration` (in `auth/session.ts`) sends the OIDC
  request with `prompt=create`, which opens Keycloak's registration page
  directly. `useAuth().signUp` exposes it, and the landing's "Create account"
  button calls it.
- **Against bulk sign-ups:** see `open-items.md` → Accounts. It lists the
  options (a private URL, a proxy rate limit, reCAPTCHA with owner keys).

The original plan follows.

- Keycloak: `registrationAllowed: true` in the realm export.
- There is no SMTP server, so `verifyEmail` and "forgot password" stay off.
- A password policy (minimum length 8).
- The login theme shows the "Create an account" link. The landing page's "Create
  character" and a new "Create account" button send people to Keycloak's
  registration page.
- A new account gets its player row on first request (`PlayerService`).
- Open sign-up means anyone with the URL can register. That fits "not shared or
  advertised", but the URL must stay private.

## Slice 3 — Session cookie (ADR-0008)

**Stage 1, done 2026-09-26:**
- **Keycloak:** a new confidential client, `omni-sheet-vault-bff`, in the
  realm export without its secret. It was created in the running realm with
  the secret from `.env`.
- **API:** `oauth2Login` with PKCE; migration V7 (Spring Session tables); and
  `SessionTokenAuthenticationFilter`.
- **CSRF:** a cookie token, required when a request carries a session.
- **Errors:** a 401 entry point.
- **Vite:** proxies to 8090, done early because the login needs the same
  origin.
- **Checked live:**
  - logging in at `localhost:5173/oauth2/authorization/keycloak` comes back to
    `/`;
  - `/api/me` answers 200 with only the cookie;
  - the cookie is invisible to JavaScript;
  - a POST without the CSRF token gets 403;
  - the session row is in `spring_session`.
**Stage 2, done 2026-09-26:**
- `CsrfCookieFilter` sends the `XSRF-TOKEN` cookie on every response.
- `BrowserLoginRequestResolver` does three things:
  - it keeps PKCE on;
  - it remembers `?returnTo=` (`LoginReturnPath`, same-app paths only; the
    login success handler redirects there);
  - `?signup` adds `prompt=create`.
- `POST /logout`: `KeycloakLogoutSuccessHandler` redirects to Keycloak's end
  session (`app.auth.end-session-uri`) with `id_token_hint` and
  `post_logout_redirect_uri` set to the app's `/`.
- **Checked live:**
  - login with `returnTo=/credits` lands on `/credits`;
  - the `XSRF-TOKEN` cookie arrives;
  - a POST with the header passes CSRF, without it gets 403;
  - a form logout returns to `/`, and `/api/me` then gets 401;
  - the next login asks for the password again (the Keycloak session ended);
  - `?signup` opens the registration page.

**Stage 3, done 2026-09-26:**
- **Removed:** `oidc-client-ts`, `auth/oidcConfig.ts`, `auth/Callback.tsx` and
  the `/callback` route.
- **`auth/session.ts`:** `redirectToLogin` and `redirectToRegistration` go to
  the API's login URL. `signOutOfSession` is a form POST to `/logout` with
  `_csrf`. `csrfToken` reads the `XSRF-TOKEN` cookie.
- **`AuthProvider`:** asks `/api/me` and exposes that player as `user`.
  `AppShell` no longer fetches `/api/me` itself.
- **`apiFetch`:** same-origin paths, with `X-XSRF-TOKEN` on writes. A 401
  goes to the login; there is no client-side renewal.
- **Account link:** `VITE_KEYCLOAK_ACCOUNT_URL`, which defaults to the local
  account page.
- **Checked live:**
  - landing, then "I already have an account", then Keycloak, back to home as
    "test";
  - "Create character" created a draft and the builder loaded;
  - the draft was deleted through the API with the CSRF header;
  - "Log out" returned to the landing, and `/api/me` then got 401;
  - `sessionStorage` holds nothing from the app.
- **Note for the owner:** a browser that used the old version still has stale
  `oidc.*` keys and tokens in `localStorage` for `localhost:5173`. Nothing
  reads them anymore; clearing the site data removes them.

**Stage 4, done 2026-09-26:**
- **`SecurityConfig`:**
  - no `oauth2ResourceServer`, so no Bearer header is read;
  - no CORS;
  - the default CSRF matcher, so every write needs the token.
- `cors.allowed-origins` was removed from `application.yml`.
- **Keycloak:** the public `omni-sheet-vault-web` client was removed from the
  realm export and from the running realm.
- **Tests:**
  - `SessionAuthenticationTest`: a Bearer header gives 401; a write without
    CSRF gives 403, even with no session cookie;
  - `CharacterPortraitControllerTest.requiresASignedInPlayer` sends the CSRF
    token, so it still checks the 401.
  - spring-security-test's `jwt()` skips CSRF by itself, so the other
    controller tests stay unchanged.
- **Checked live:**
  - a Bearer header gets 401;
  - a POST from another site gets 401;
  - login, then home as "test" with 5 characters;
  - a write without CSRF gets 403, and with CSRF it passes.

Staged, each stage keeping the app working:
1. Backend login and session: add `oauth2-client` and Spring Session JDBC (a
   Flyway migration); make the Keycloak client confidential, with its secret in
   `.env`; add the filter that feeds controllers the same `Jwt` they get today.
   Bearer tokens still work during this stage.
2. CSRF cookie and header; a Vite proxy for same-origin; `GET /api/me`; `POST
   /logout`.
3. Web: remove `oidc-client-ts`, and switch `apiFetch`, the auth provider,
   login, logout and the callback to the session.
4. Remove Bearer from the browser path. Update `ground-rules.md`,
   `architecture.md`, `tech-stack.md` and the tests.

## Later — putting it online

Once the three slices are done, a Cloudflare Tunnel from the owner's machine, or
an Oracle Cloud "Always Free" VM running the `docker compose` stack. This is
planned separately.

# Feature — Home page, public landing and login theme

Status: **done** (2026-09-25). H1–H6 are complete. What's left is the later
dark theme, and the Keycloak items in `open-items.md`.

## Goal

- A public landing page that sells the app.
- A Keycloak login restyled to match.
- A signed-in home that lists the player's characters as banners grouped by game
  system, with a system filter.

Mock: `design-reference/mockups/home/index.html`, direction **B · Clean
Parchment**. Serve it from the repository root (it loads the bundled portraits),
e.g. `python -m http.server 5191` and
`http://127.0.0.1:5191/design-reference/mockups/home/index.html`.

## Decisions (owner, 2026-09-25)

- **Direction B is the app's look.** Direction A (Arcane Vault) becomes a later
  "Dark theme", switched from the user menu. The theme tokens must make that a
  second set of values, not a rewrite.
- **Colours:** the brand and primary-button colour is gold `#D9A441`, with
  charcoal text on it (`#1F2327`).
- **Logo:** `apps/web/src/assets/brand/omni-logo.svg` (dark shield, for light
  backgrounds) and `omni-logo-light.svg` (parchment shield, for the charcoal
  header). The name next to it is live text: Roboto Condensed, uppercase,
  `.08em` tracking.
- **Banner:**
  - portrait, then the name;
  - then the system's facts. For D&D 5e that is the classes, each with its own
    level ("Rogue 4 / Sorcerer 3", never a total level), and the species on the
    line below;
  - a 5 px left bar in the system's colour (D&D 5e `#C2412F`);
  - drafts carry a "Draft · continue creating" badge.
- **Systems:**
  - the filter lists only systems that have characters;
  - the "Start something new" and "Pick your game" cards show every system;
    only D&D 5e can be created, and the others (Vampire, Daggerheart,
    Pathfinder 2e, Call of Cthulhu, Tormenta20) show only a "Coming soon"
    badge, with no "Notify me" button.
- **Signed out:** `/` shows the landing page.
  - "Log in" and "Create character" go to Keycloak;
  - after login the player returns to where they were going: the character list,
    or creating a D&D 5e character.
- **Keycloak login:** restyled to direction B. "Forgot password?" stays off (no
  SMTP; see `open-items.md`). Account registration has been on since 2026-09-26:
  the login shows "New in the vault? Create an account", and the landing has a
  "Create account" button (`features/private-sharing.md`, slice 2).
- **Delete:** deleting a character asks for confirmation; today it is immediate.

## Constraints

- **The character list is generic** (`ui-design-system.md`, "Rules that do not
  bend"). The backend composes each banner's facts per system through a strategy
  selected by system id; the web app never names a system in list code.
- **Out of scope:**
  - the sheet keeps its D&D Beyond fidelity and its blue;
  - the character builder keeps its own tokens for now;
  - the dark theme is prepared but not built.

## Plan and progress

- [x] **H1 — Theme foundation.** *Done:* `apps/web/src/theme/theme.css` (imported
  in `main.tsx`) holds the `--app-*` tokens and the `.app-page`, `.app-wrap`,
  `.app-heading`, `.app-btn`, `.app-chip` primitives. App-wide CSS custom properties for direction B:
  background, surface, text, muted, line, accent, accent ink, header, radius,
  fonts. Defined on `:root` so a `[data-theme="dark"]` set can come later. Not
  applied to the sheet or the builder.
- [x] **H2 — Per-system summary in the API.** *Done:*
  - `ruleset/CharacterSummarizer`, `SummaryFact`, `registry/CharacterSummarizerRegistry`;
  - `dnd5e/Dnd5eCharacterSummarizer` reads only the JSON nodes it needs:
    `classLevels` and `build`, or the draft's `classes`/`speciesSlug`. Names come
    from the catalogue, falling back to words from the slug. The species label
    reuses `Dnd5eBuildPlanner.speciesLabel(name, subspecies)`;
  - `character/CharacterSummaryService` returns an empty list on any failure, so
    the list never breaks;
  - `CharacterResponse.summary` (`[{label, value}]`), filled by
    `CharacterController` and `CharacterDraftController.finish`;
  - tests: `Dnd5eCharacterSummarizerTest` (4); `CharacterControllerTest` mocks
    the new service.

  Original plan for this step:
  - A `CharacterSummarizer` strategy per system, found through the registry.
  - The D&D 5e implementation returns labelled facts: classes with their levels,
    and species. It works from the sheet for active characters and from the
    build for drafts.
  - `CharacterResponse` gains `summary`.
  - Tests.
- [x] **H3 — Public landing and login flow.** *Done:*
  - **Auth:**
    - `AuthProvider` no longer forces login. It exposes
      `status: loading | signed-in | signed-out`, `signIn(returnTo)` and
      `signOut`;
    - `session.redirectToLogin(returnTo)` puts `{returnTo}` in the OIDC state,
      and `Callback` returns there;
    - `safeReturnPath` accepts in-app paths only, never `//…` or `/callback`.
    - *Since 2026-09-26 (ADR-0008, stage 3):* `redirectToLogin` passes
      `?returnTo=` to the API's login instead, and the API redirects back
      there. `Callback` no longer exists.
  - **Routes (`AppShell`):**
    - `/` → `LandingPage` when signed out, `HomePage` when signed in;
    - every other route is wrapped in `RequireSession`, which logs in and comes
      back to the same address;
    - new `/characters/new/:systemId`: `SystemPicker` creates that system's draft
      at once, with a ref guard against StrictMode.
  - **Systems:** `supportedSystems.ts` gained `tagline`, `color`,
    `cardBackground`, the `UPCOMING_SYSTEMS` marketing list, and
    `supportedSystem`, `systemLabel`, `systemColor`.
  - **`home/`:** `LandingPage`, `AppHeader`, `SystemCards`, `home.css`.

  Original plan for this step:
  - The app no longer forces login on load; `/` shows the landing page when
    signed out.
  - The builder and the sheet still require a session.
  - "Log in" / "Create character" start the OIDC redirect with a return path.
  - A web system registry for the landing and home cards: available systems
    come from `SUPPORTED_SYSTEMS`, and the upcoming ones are a static
    marketing list.
- [x] **H4 — Signed-in home.** *Done (code and unit tests; live check in H6):*
  - `home/HomePage`:
    - "Start something new" cards;
    - a filter that lists systems with characters and keeps `?system=` in the
      URL;
    - groups by system;
    - an empty state;
    - Delete behind `ConfirmDialog`.
  - `home/CharacterBanner`:
    - the system colour as `--system-color`;
    - a whole-card link, "Open Vex" or "Continue creating …";
    - the facts from `character.summary`, the first one emphasised;
    - the draft badge;
    - the "⋯" menu.
  - The old `characters/CharacterList.tsx` was removed.
  - Tests: `HomePage.test.tsx` (5), `LandingPage.test.tsx` (3),
    `session.test.ts` (2), `Callback.test.tsx` (3).

  Original plan for this step:
  - Header: logo, and a user menu with My characters, Account (Keycloak account
    console) and Log out.
  - "Start something new" cards, the system filter (kept in the URL), banners
    grouped by system, and the "⋯" menu with Delete behind a confirmation.
  - Empty states: no characters yet, and none in the filtered system.
  - Tests.
- [x] **H5 — Keycloak login theme.** *Done and checked in the browser:*
  - **Theme:** `infra/keycloak-themes/omni-sheet-vault/login/`:
    - `theme.properties`: `parent=keycloak.v2`, the extra stylesheet, and
      `darkMode=false`, since B is light;
    - `messages_en.properties`: "Sign in to your vault", "Username or email";
    - `resources/css/omni-sheet-vault.css`: overrides of PatternFly 5 classes and
      variables;
    - `resources/img/`: both logos, and `party.png`, three round portraits
      composed once with System.Drawing.
  - **Mount:** a separate folder, because `infra/keycloak` is the realm-import
    folder. `docker-compose.yml` mounts it at
    `/opt/keycloak/themes/omni-sheet-vault`.
  - **Realm:** `realm-export.json` sets `"loginTheme": "omni-sheet-vault"` for
    fresh installs. `--import-realm` skips an existing realm, so the running
    realm was switched with `kcadm.sh update realms/omni-sheet-vault -s
    loginTheme=omni-sheet-vault`.
  - **Reset password** stays off, so Keycloak hides its link. Registration was
    turned on later (2026-09-26, `features/private-sharing.md`).

  Original plan for this step:
  - `infra/keycloak/themes/omni-sheet-vault/login/`, extending `keycloak.v2` with
    CSS, the logo and messages ("Sign in to your vault").
  - Mounted into the container through `docker-compose.yml`; the realm's
    `loginTheme` set to it.
  - Registration and reset password stay off.
- [x] **H6 — Wrap-up.** *Done:*
  - **Live check (2026-09-25), all passed:**
    - signed-out `/` shows the landing page;
    - "Log in" goes through Keycloak (the browser's SSO session signed in
      silently) back to `/`, where the home shows the real summaries (Vex
      "Rogue 4 / Sorcerer 3 · Changeling", Liriel "Wizard 5 · High Elf", Aria
      "Fighter 20 · Mountain Dwarf", the draft "Bard 3 / Wizard 4 ·
      Aarakocra");
    - the D&D card's "Create character" creates exactly one draft and opens its
      builder;
    - "⋯ → Delete" opens the confirmation, and confirming removes that draft
      only;
    - the filter sets `?system=dnd-5e`;
    - the user menu opens;
    - the Keycloak theme was checked on its own login page.
  - **Not tested live:** Log out, which would have ended the owner's Keycloak
    session (the assistant never types passwords).
  - **Checks:** `:apps:api:check` green; web type-check and 97 tests green.
  - **Docs:** `architecture.md` (request flow), `ui-design-system.md` (app theme,
    home and landing), `open-items.md` (registration, forgot password),
    changelog.

  Original plan for this step:
  - Live check: signed out → landing → login → home; filter; create; delete
    with confirmation; logout.
  - `ui-design-system.md`, `architecture.md`, changelog.

## Where it stopped

Finished: H1–H6 are done, with nothing pending in this plan. The dark theme
(direction A) shipped on 2026-09-27 as slice 2 of
`systems/dnd-5e/features/leveling-and-appearance.md`; see `ui-design-system.md`, "App
theme". Possible next steps:
- the builder in the gold accent;
- grouping default portraits by species.

# Ground rules

Read this before writing or editing any code.

## Language

Code, comments, identifiers, documentation, file names and commit messages are in
English. Conversation with the owner is in Brazilian Portuguese. These never mix.

## Design principles

- **SOLID, with emphasis on Open/Closed.** Adding a game system means adding classes.
  If supporting a new system requires editing an existing class, the abstraction is
  wrong — stop and discuss it instead of adding a branch.
- **Clean Code.** Small functions, intention-revealing names, no comments explaining
  code that could be clearer instead.
- **Strategy for anything system-specific.** System-specific behavior lives in a
  strategy implementation selected by system identifier through a registry. The
  orchestrating service delegates; it never decides.
- **No conditional on system identifier.** `if (systemId.equals(...))`, a `switch` on
  the system, or an `instanceof` chain over sheet types is forbidden outside the
  registry itself. Finding one means the strategy seam is missing or in the wrong
  place — fix the seam, do not add a branch.
- **No fallback system.** An unknown system identifier throws
  `UnsupportedGameSystemException` and becomes HTTP 400. Never default to D&D.
- **No speculative generality.** Do not abstract for a second game system until a
  second game system exists. One implementation plus a clear seam beats a framework
  built on guesses.

## Comments

- A comment names what the code **is or does**, briefly enough to orient a reader
  in a few seconds — e.g. `// Skills section CSS`. It is never a record of what
  the owner asked for, when, or the back-and-forth that produced the change —
  that history belongs in `changelog.md`, one entry per change, not scattered
  through the codebase as narrative comments.
- Only write a comment when the WHY is genuinely non-obvious (a hidden
  constraint, a workaround for a specific bug, behavior that would surprise a
  reader) — see "Clean Code" above. Keep it about the code, not about the request
  that produced it.
- Existing narrative comments (dated, quoting the owner, walking through what
  changed and why) predate this rule. Leave them as they are unless a dedicated
  cleanup pass is asked for — don't fold that cleanup into an unrelated change.

## Java conventions

- Java 25. Use records for immutable data, sealed interfaces where the set of
  implementations is closed, pattern matching over instanceof chains.
- Constructor injection only. No field injection, no `@Autowired` on fields.
- Package by feature, not by layer: `character`, `dice`, `ruleset`, `identity`.
  Controllers, services and repositories live inside their feature package.
- Domain types never leak out of the API. Controllers speak in request/response
  records, mapped explicitly.
- Bean Validation on every inbound record. The database cannot validate the JSONB
  payload, so Java is the only gate.
- Exceptions are handled in one `@RestControllerAdvice` and returned as RFC 7807
  problem details. Never return raw stack traces.

## Persistence

- Every schema change is a Flyway migration under `db/migration`, named
  `V{n}__{description}.sql`. Migrations are append-only — never edit an applied one.
- `ddl-auto` stays `validate`. Hibernate never alters the schema.
- System-specific sheet data goes in a JSONB column, mapped from a typed Java record.
  Every stored payload carries a `schemaVersion` field.
- Relational columns for anything queried, joined or constrained. JSONB only for the
  system-specific shape.

## Catalogue content

- **Same name, different source, different entity.** When two sources publish an
  entity with the same name (e.g. *Aberrant Dragonmark* in ERLW and EFA), both are
  kept as separate catalogue entries. Never pick one.
  - Ingestion gives each one a source-suffixed slug (`aberrant-dragonmark-erlw`,
    `aberrant-dragonmark-efa`).
  - Two entries with the same name *and* the same source fail ingestion.
- **Every catalogue listing shows each entry's source** next to its name, so
  same-named entries are always distinguishable. Use `CatalogueSourceLabel` in the
  web app.

## Security

- The API validates Keycloak JWTs, and it also runs the browser's login
  (adr-0008).
  - The tokens stay server-side in the session. The browser holds only an
    `HttpOnly` session cookie.
  - Controllers always receive a validated `Jwt`, taken from the session.
  - No Bearer header is accepted. Every write needs the CSRF token.
- Passwords, registration, password reset and MFA belong to Keycloak. No credential
  handling in application code, ever.
- Every endpoint is authenticated by default. Public endpoints are opt-in and explicit.
- Ownership is enforced in the service layer: a user can only read or modify their
  own characters, checked against the token subject, never against a request parameter.

## Dice

- Dice rolls are resolved server-side. The client never reports a result.
- Every roll is persisted as an immutable event: who, when, expression, context,
  individual die results, total. Roll records are never updated or deleted.

## Frontend

- TypeScript, functional components, hooks.
- No business rules in the frontend. If the UI needs to know what a modifier is, the
  API returns it.
- The web app never holds a token (adr-0008).
  - It relies on the API's `HttpOnly` session cookie, and calls the API only
    through `apiFetch`, on its own origin, which adds the CSRF header to
    writes.
  - Never add an OIDC library or store credentials in browser storage.
- **Any change to the D&D 5e character sheet ends with a live comparison against
  D&D Beyond's own sheet** — see `systems/dnd-5e/sheet-ui.md`'s "Verifying
  fidelity against D&D Beyond" for the reference URL and how to apply it. Do not
  call sheet UI work done without it.

## Testing

- Tests ship with the code that they cover.
- Persistence and integration tests run against a real PostgreSQL via Testcontainers.
  H2 is forbidden — it does not support JSONB and would hide real failures.
- Every game system implementation gets tests derived from its published rules.

## Workflow

- Work in vertical slices that cross the whole stack, not in horizontal layers.
- Propose a plan before any change larger than one slice.
- After finishing, update `changelog.md` and any `.ai/` document the change
  invalidated.
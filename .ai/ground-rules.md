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

## Security

- The API is a resource server. It validates JWTs and nothing else.
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
- Tokens are handled by the OIDC library. Never store tokens in `localStorage` by hand.

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
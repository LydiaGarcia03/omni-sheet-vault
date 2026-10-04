# Architecture

## Shape

A monorepo holding two independent applications and the infrastructure that supports
them.

The Gradle build is rooted at the **repository root**, which owns the wrapper and the
`settings.gradle` declaring `include 'apps:api'`. The backend is therefore the
subproject `:apps:api`, and every Gradle command runs from the root:
`./gradlew :apps:api:bootRun`.

The frontend is an npm project rooted at `apps/web` and is deliberately outside the
Gradle build. Keeping it out avoids Gradle scanning `node_modules` and keeps the two
toolchains independent.

```
omni-sheet-vault/
├── settings.gradle              # include 'apps:api'
├── gradlew / gradlew.bat        # wrapper lives here, not in apps/api
├── .ai/
├── apps/
│   ├── api/
│   │   ├── src/main/java/dev/omnisheetvault/api/
│   │   │   ├── character/       # Character vault: CRUD, ownership
│   │   │   ├── dice/            # Roll engine and roll history
│   │   │   ├── ruleset/         # Game system implementations
│   │   │   ├── identity/        # Token to application user mapping
│   │   │   ├── storage/         # Portrait upload, S3-compatible
│   │   │   └── shared/          # Cross-cutting: error handling, config
│   │   └── src/main/resources/db/migration/
│   └── web/
├── infra/
│   ├── keycloak/
│   └── postgres/init/
└── docker-compose.yml
```

## Package by feature

Each package under `api/` owns its controller, service, repository and domain types.
A package may depend on `shared`, and on the domain types of another feature, but
never on another feature's repository. Cross-feature work goes through the other
feature's service.

`ruleset` is the exception in one direction: `character` and `dice` both depend on the
interfaces `ruleset` exposes, never on a concrete system implementation.

## Runtime pieces

| Piece | Responsibility |
| --- | --- |
| React SPA | Rendering, OIDC login flow, no business rules |
| Spring Boot API | All rules, all validation, all persistence |
| PostgreSQL | Relational spine plus JSONB sheet payloads |
| Keycloak | Identity: credentials, sessions, tokens |
| MinIO (S3 API) | Character portraits |

## Request flow

Since 2026-09-26 (adr-0008):
1. On load, the SPA asks `GET /api/me` (`AuthProvider`). A 401 shows the
   public landing page at `/`.
2. "Log in", "Create character", or any route wrapped in `RequireSession`
   sends the browser to `/oauth2/authorization/keycloak?returnTo=<in-app
   path>`. "Create account" adds `&signup`.
3. Keycloak authenticates the user on the `omni-sheet-vault` login theme
   (`infra/keycloak-themes/`) and sends the code back to the API.
4. The API exchanges the code (confidential client, PKCE) and keeps the tokens
   in the session. It sets an `HttpOnly` session cookie and redirects to the
   return path.
5. The SPA calls the API on its own origin with that cookie (`apiFetch`).
   Writes carry the CSRF token, and a 401 sends the browser back to the login.
6. The API renews the session's access token when needed, and validates it
   against Keycloak's public keys (cached from JWKS).
7. The service layer checks that the token subject owns the requested resource.

The API never sees a password, and the browser never sees a token.

**How the session login works (adr-0008; see
`features/private-sharing.md`):**
- `/oauth2/authorization/keycloak` starts the login and
  `/login/oauth2/code/keycloak` receives the code. Both use the confidential
  `omni-sheet-vault-bff` client with PKCE.
- The tokens stay in the session, stored in Postgres (Spring Session JDBC). The
  browser only gets an `HttpOnly` `SESSION` cookie.
- `SessionTokenAuthenticationFilter` renews the session's access token when
  needed. It decodes it (the JWKS-backed `JwtDecoder` that Boot builds from
  `spring.security.oauth2.resourceserver.jwt.issuer-uri`) and gives
  controllers a `JwtAuthenticationToken`.
- **No Bearer header is read.** The resource-server starter stays on the
  classpath only for that decoder and the JWT types.
- Every write needs the `XSRF-TOKEN` cookie value in an `X-XSRF-TOKEN` header,
  or an `_csrf` form field. `CsrfCookieFilter` issues the cookie on every
  response.
- There is no CORS configuration: the web app and the API share an origin.
- `/oauth2/authorization/keycloak?returnTo=/path` comes back to that in-app
  path after the login (`LoginReturnPath`, same-app paths only). Adding
  `&signup` opens Keycloak's registration page (`prompt=create`).
- `POST /logout` (a form post with `_csrf`) ends the API session, then the
  Keycloak session (`KeycloakLogoutSuccessHandler`, RP-initiated logout), and
  returns to `/`.
- Locally, Vite proxies `/api`, `/oauth2`, `/login/oauth2` and `/logout` to
  8090, keeping the `localhost:5173` Host header.
- An unauthenticated request gets a 401, never a login redirect. That includes
  an anonymous write without CSRF: its 403 goes through the `/error`
  dispatch, which also needs a login.
- A deployment must put a reverse proxy in front of the web app and the API,
  on one origin, forwarding the same paths as the Vite proxy.

## Multi-system design — Strategy pattern

A game system is an implementation, not data. System-specific behavior is expressed
with the **Strategy pattern**: a family of algorithms, one implementation per system,
interchangeable at runtime and selected by system identifier.

### Request flow

```
React  ──▶  Controller  ──▶  Orchestrator  ──▶  Registry  ──▶  System strategy
            (HTTP only)      (no game rules)     (lookup)       (all the rules)
```

The orchestrator receives a request carrying a system identifier, asks the registry
for the strategy registered under it, delegates, and handles the surrounding concerns:
validation, ownership, persistence, roll recording. **It contains no game rules and
names no system.**

### The seams

| Interface | Responsibility |
| --- | --- |
| `GameSystem` | Identity and metadata of a system |
| `CharacterCreationFlow` | Ordered choices and what each one grants |
| `SheetCalculator` | All derived values: hit points, armour class, modifiers |
| `MechanicResolver` | Turns an action ("cast fireball") into dice expressions |

Every strategy interface exposes `String systemId()`. That is what makes registration
automatic.

### The registry

The registry is built by injecting every implementation and indexing it:

```java
@Component
public class SheetCalculatorRegistry {

   private final Map<String, SheetCalculator> bySystem;

   public SheetCalculatorRegistry(List<SheetCalculator> calculators) {
      this.bySystem = calculators.stream()
              .collect(toMap(SheetCalculator::systemId, identity()));
   }

   public SheetCalculator forSystem(String systemId) {
      SheetCalculator calculator = bySystem.get(systemId);
      if (calculator == null) {
         throw new UnsupportedGameSystemException(systemId);
      }
      return calculator;
   }
}
```

Spring injects every `@Component` implementing the interface, so adding a system means
adding classes and editing nothing. An unknown identifier fails explicitly and maps to
HTTP 400 — never to a silent default and never to a fallback system.

### Granularity

Group calculations that always change together into one interface instead of creating
a strategy per calculation. `SheetCalculator` with several methods beats
`HitPointsStrategy`, `ArmorClassStrategy` and `InitiativeStrategy`, which would mean a
class and a registration per formula per system.

Split into separate strategy interfaces only where the families are genuinely
independent — creation, calculation and mechanic resolution are; individual formulas
inside one system are not.

### Package layout

```
ruleset/
├── GameSystem.java              # interfaces only
├── SheetCalculator.java
├── CharacterCreationFlow.java
├── MechanicResolver.java
├── registry/                    # lookup, no rules
└── dnd5e/                       # one subpackage per system
    ├── Dnd5eGameSystem.java
    ├── Dnd5eSheetCalculator.java
    └── ...
```

`character` and `dice` depend on the interfaces and the registries. Nothing outside
`ruleset` may import a class from a system subpackage.

**Catalogue access from rules code** (2026-09-23). `ruleset.CatalogueLookup` is a
read-only port that rules code uses to read catalogue entries by kind name and
slug. The `catalogue` package implements it; the dependency points from
`catalogue` to `ruleset`, never back. `ContentDirectoryCatalogue` reads the ingested
`content/<system>/` files (used by the `planBuild` tool). A Postgres-backed
implementation arrives when the running API first needs one (D1's seeder).

`CharacterCreationFlowRegistry.standalone(...)` builds the registry without Spring,
for command-line tools. It is the one place that lists flows by hand.

Persistence is deliberately generic even though the logic is not: a character row
stores its system identifier plus a JSONB payload. This means adding a system needs no
migration, and it keeps the door open to declarative rule packs later without a
rewrite.

## Storage boundary

Portraits go through a `PortraitStorage` interface implemented over the S3 API.
MinIO locally and in the single-VM deployment; any S3-compatible provider elsewhere.
The application never writes to the local filesystem.

Built in D2j (2026-09-25):
- **Storage:** `storage/S3PortraitStorage` builds its clients on first use, so
  contexts that never touch portraits need neither MinIO nor credentials. It
  creates the bucket on the first upload.
- **Reading:** the bucket is private. Browsers load uploads through signed GET
  URLs (`storage.url-ttl`, 1 h), signed against `storage.public-endpoint`
  (defaults to `storage.endpoint`) so they work when the API reaches the storage
  under another host name.
- **Validation:** `character/PortraitImage` validates every upload (PNG or JPEG,
  at most 3 MB and 4096 px a side) and re-encodes it as a centre-cropped PNG of
  at most 512 px.
- **Presets:** a character can also use one of the web app's bundled presets
  instead of an upload. Presets never touch storage.
- **Local credentials:** the dev profile imports the repository's `.env`
  (`spring.config.import`), so `bootRun` gets the MinIO credentials.

## Deployment

Everything runs from `docker-compose.yml`, on the owner's machine during development
and on a single free-tier VM when a public instance is wanted. Local and deployed
environments differ only by environment variables. The target VM is ARM, so every
image must have an `arm64` build, including the API image.

Nothing in the code may assume a host name, a port or a credential. All of it comes
from configuration.

Local ports: API `8090`, Keycloak `8081`, PostgreSQL `5432`, MinIO `9000` and `9001`,
Vite `5173`. The API avoids `8080` because that port is taken on the development
machine.

## Editions

The same code runs in two editions, chosen by Spring profile. Each edition adds
its own classes behind existing seams; no code branches on the edition.

| Seam | Server edition (default) | Desktop edition (`desktop` profile) |
| --- | --- | --- |
| Security | `SecurityConfig`: Keycloak login, session cookie (adr-0008) | `DesktopSecurityConfig`: one local player, no login, `127.0.0.1` and local Host only |
| Database | PostgreSQL from `docker-compose.yml` | `EmbeddedDatabaseConfig`: PostgreSQL binaries started by the app |
| `PortraitStorage` | `S3PortraitStorage` (MinIO) | `FileSystemPortraitStorage` plus `LocalPortraitFileController` |
| Catalogue import | `--import-catalogue` by hand | `DesktopCatalogueBootstrap`, on start, when the content changed |

The desktop edition exists so friends can run the vault on their own Windows
machines without Docker or hosting; see `features/desktop-edition.md`.
- **Launcher and packaging:** the separate Gradle subproject `:apps:desktop`
  (`DesktopLauncher` starts `OmniSheetVaultApplication` in the `desktop`
  profile).
- **User guide:** `apps/desktop/README.md`. Its
data lives in `%LOCALAPPDATA%\OmniSheetVault\`. The `desktop` package holds its
settings (`DesktopProperties`) and its database. The other classes live in the
feature package whose seam they fill.
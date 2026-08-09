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

1. The SPA finds no valid token and redirects the browser to Keycloak.
2. Keycloak authenticates the user and returns an authorization code.
3. The SPA exchanges the code for tokens using PKCE.
4. The SPA calls the API with `Authorization: Bearer <access token>`.
5. The API validates the signature against Keycloak's public keys, cached from JWKS.
   No network call per request.
6. The service layer checks that the token subject owns the requested resource.

The API never sees a password and holds no session.

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

Persistence is deliberately generic even though the logic is not: a character row
stores its system identifier plus a JSONB payload. This means adding a system needs no
migration, and it keeps the door open to declarative rule packs later without a
rewrite.

## Storage boundary

Portraits go through a `PortraitStorage` interface implemented over the S3 API.
MinIO locally and in the single-VM deployment; any S3-compatible provider elsewhere.
The application never writes to the local filesystem.

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
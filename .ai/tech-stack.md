# Tech stack

Record every dependency and version change here.

## Backend

| Item | Version | Notes |
| --- | --- | --- |
| Java | 25 | Toolchain-pinned in `build.gradle.kts` |
| Spring Boot | 4.1.x | Runs on Spring Framework 7 and Spring Security 7 |
| Gradle | 9.x | Build rooted at the repository; backend is `:apps:api` |
| PostgreSQL | 17 | JSONB for system-specific sheet payloads |
| Flyway | Managed by Boot | Needs `flyway-database-postgresql` alongside `flyway-core`, **and** `spring-boot-flyway` — see constraint below |
| Testcontainers | Managed by Boot | Real PostgreSQL in tests; H2 is forbidden. The `minio` module (test only, 2026-09-25) runs a real MinIO for the portrait storage tests. Since 2026-10-04 on `cgr.dev/chainguard/minio` pinned by digest (`MinioTestImage`): MinIO stopped publishing public images, so `minio/minio` no longer pulls on CI |
| `jpackage` (JDK 25) | JDK tool | The desktop edition's Windows package (`:apps:desktop:packageDesktop`), as an `--type app-image` zipped for sharing. No new dependency. An `.exe` or `.msi` installer would also need the WiX toolset, which isn't installed |
| `io.zonky.test:embedded-postgres` | 2.2.2, binaries BOM `17.11.0` | Approved by the owner 2026-09-27. The desktop edition's database (`features/desktop-edition.md`): real PostgreSQL 17 binaries, started by the app. Despite "test" in the name it runs a real server. It brings binaries for Windows, macOS, Linux and Alpine; the installer can drop the unused ones later |
| AWS SDK for Java v2, `s3` | BOM `2.55.5` | Portrait storage over the S3 API (`S3PortraitStorage`): MinIO locally, any S3-compatible provider elsewhere; signed GET URLs via `S3Presigner`. Agreed with the owner 2026-09-25 (D2j) |

Starters in use: `web`, `validation`, `data-jpa`, `security`,
`oauth2-resource-server`, `oauth2-client`, `session-jdbc`, `actuator`, plus
`docker-compose` as `developmentOnly`.

`oauth2-client` (Spring Security 7.1) and `session-jdbc` (Spring Session 4.1)
were added 2026-09-26 for ADR-0008, which the owner approved.
- `oauth2-client`: the API runs the browser's Keycloak login with the
  confidential `omni-sheet-vault-bff` client.
- `session-jdbc`: keeps browser sessions in Postgres (`spring_session`,
  migration V7).
- The Keycloak endpoints are listed in `application.yml` instead of discovered
  from the issuer, so the API and its tests start without Keycloak.

**Warning for anyone searching the web:** most Spring Boot material online targets
Boot 3. Boot 4 changed parts of the security configuration API. Verify against the
Boot 4 documentation before copying a snippet.

#### Constraint: Boot 4.1 pulls in Jackson 3, not Jackson 2

`ObjectMapper` and the rest of the databind API live at `tools.jackson.databind.*`
now, not `com.fasterxml.jackson.databind.*` — Jackson 3 moved its group id to
`tools.jackson` and its core packages with it. `jackson-annotations` is the one piece
still published under `com.fasterxml.jackson.core` (2.21), so an import list can look
half-migrated and still be correct. `import com.fasterxml.jackson.databind.ObjectMapper`
fails to compile with "package does not exist" — the fix is `import
tools.jackson.databind.ObjectMapper`, not adding a dependency.

Jackson 3 is also stricter deserializing into a primitive field: a JSON `null` or a
missing property throws `MismatchedInputException: Cannot map 'null' into type 'int'`
by default, rather than silently defaulting to `0`. Hit this for real in phase 4 —
`Dnd5eSheet` gained a field after some `characters.sheet` rows were already stored,
and reading one of the old rows 500'd until it was recreated. There is no
migration/versioning strategy yet for evolving a system's sheet shape against
already-stored JSONB payloads — `sheet_schema_version` exists for exactly this and is
currently unused for it. Fine while only dev seed data exists; revisit before phase
9/10 lets a real client write a sheet.

#### Constraint: `flyway-core` alone does not enable Flyway on Boot 4

Boot 4 split `spring-boot-autoconfigure` into many small per-feature modules
(`spring-boot-hibernate`, `spring-boot-jdbc`, and so on). Flyway's autoconfiguration
moved out too, into `org.springframework.boot:spring-boot-flyway` — a Boot-managed
module, not a Flyway one. Depending only on `org.flywaydb:flyway-core` (which was
sufficient through Boot 3) now compiles fine, boots fine, and silently never runs a
migration: no error, no log line mentioning Flyway, Hibernate's `ddl-auto: validate`
just fails later with "missing table". **`spring-boot-flyway` must be an explicit
`implementation` dependency alongside `flyway-core` and `flyway-database-postgresql`.**

Deliberately absent: Lombok (records cover the need), Hypersistence Utils (Hibernate
maps JSONB natively via `@JdbcTypeCode(SqlTypes.JSON)`).

### Code quality and CI

| Item | Version | Notes |
| --- | --- | --- |
| SonarQube Cloud | — | Free for public projects; analysis runs from CI, not automatic |
| SonarScanner for Gradle (`org.sonarqube`) | 7.x | **Minimum 7.0.0.6105** — see constraint below |
| JaCoCo | Managed by Gradle | XML report must stay enabled; Sonar reads only the XML |

#### Constraint: Sonar plugin must be 7.x

Gradle 9 removed the `Convention` API. Scanner versions below 7.0.0.6105 still call
it, so the build fails at the `sonar` task with:

    'org.gradle.api.plugins.Convention org.gradle.api.internal.plugins.DslObject.getConvention()'

The message names no plugin, so it reads like a Gradle problem. It is a version
mismatch. **Never downgrade the Sonar plugin below 7.0.0.6105 while the build runs on
Gradle 9.**

The plugin is applied to the **root** project, not to `:apps:api` — the scanner
analyzes a project hierarchy from its root. JaCoCo stays in `:apps:api`, where the
tests are.

## Frontend

| Item | Version | Notes |
| --- | --- | --- |
| React | 19.2.x | Scaffolded with Vite, functional components + hooks only |
| TypeScript | 5.9.x | `strict: true` |
| Vite | 6.4.x | Dev server on 5173 |
| @vitejs/plugin-react | 4.7.x | |
| react-router | 7.x | Approved by the owner 2026-09-24 (D2). `BrowserRouter` in `App.tsx`; routes `/`, `/credits`, `/characters/:id`, `/characters/:id/build/:step?` in `AppShell.tsx`. |

`oidc-client-ts` was removed on 2026-09-26 (ADR-0008, stage 3). The API runs
the login now, and the web app only holds a session cookie. The only web env
variable left is the optional `VITE_KEYCLOAK_ACCOUNT_URL`, for the "Account"
link.
| Vitest | 3.2.x | Dev dependency, approved by the owner 2026-09-24. `npm test` runs `vitest run`. Configured in `vite.config.ts` (`test` block, jsdom). |
| @testing-library/react | 16.3.x | Dev dependency; component tests render real components into jsdom |
| @testing-library/jest-dom | 7.0.x | Dev dependency; DOM matchers, loaded by `src/test/setup.ts` |
| jsdom | 30.1.x | Dev dependency; Vitest's test environment |

Test files sit next to the code they test (`*.test.ts` / `*.test.tsx`). `npm install`
reported 2 moderate-severity advisories in the dev tree on 2026-09-24. `npm audit fix
--force` would pull breaking upgrades, so it wasn't run.

No SSR framework: the application lives entirely behind a login, so server rendering
and SEO would add complexity with no benefit.

## Infrastructure

| Service | Image | Ports |
| --- | --- | --- |
| PostgreSQL | `postgres:17-alpine` | 5432 |
| Keycloak | `quay.io/keycloak/keycloak` | 8081 |
| MinIO | `minio/minio` (works only where already pulled: the image is no longer public; a fresh machine needs another source, e.g. `cgr.dev/chainguard/minio`) | 9000 API, 9001 console |

Keycloak keeps its own database inside the same PostgreSQL container, created by a
first-boot script in `infra/postgres/init/`.

#### Constraint: a client's `webOrigins` entry must be an origin, not a redirect pattern

(The public `omni-sheet-vault-web` client was removed on 2026-09-26 by
ADR-0008, stage 4. The browser no longer calls Keycloak's token endpoint. The
lesson still applies to any client that does.)

`omni-sheet-vault-web` originally had `webOrigins: ["http://localhost:5173/*"]`,
copying the `redirectUris` pattern. Keycloak's token endpoint accepted that for the
CORS **preflight** (`OPTIONS`) — the `Access-Control-Allow-Origin` header showed up
there — but never added it to the actual `POST` response, so the browser blocked the
token exchange with a CORS error right after a successful login. `curl` against the
same endpoint looked fine, which hid the bug during backend-only testing.

**`webOrigins` must be either an exact origin (`http://localhost:5173`, no path) or
`+`** (mirror the registered redirect URIs). The realm now uses `+`. Verify by
checking that a `curl -X POST` to `.../protocol/openid-connect/token` with an
`Origin` header echoes back `Access-Control-Allow-Origin` on the response itself, not
just on an `OPTIONS` preflight.

## Deployment constraint

The public instance targets a free-tier ARM VM. **Every image must have an `arm64`
build, including the API image.** Build with buildx or on the target architecture.
Discovering this on deployment day is avoidable — treat it as a hard requirement.

## Before exposing anything publicly

None of the items below apply to local development. They become mandatory the moment
a service is reachable from outside the development machine.

### Keycloak

The admin created from `KC_BOOTSTRAP_ADMIN_USERNAME` and `KC_BOOTSTRAP_ADMIN_PASSWORD`
is a **temporary bootstrap account**. Keycloak flags it on every login. It is fine
locally — the credentials live in `.env` and the service only listens on localhost.

Before the first public deployment:

- Create a permanent admin account with a password that does not come from an
  environment variable, and delete the bootstrap admin.
- Do not expose the admin console publicly unless there is a reason to.
- Serve Keycloak over HTTPS. The realm currently has `sslRequired: external`, which
  only holds if a TLS terminator is actually in front of it.

Note: accounts in the `master` realm are not part of the exported realm file. A
permanent admin created locally would be lost on `docker compose down -v`, which is
why this step is deliberately deferred to deployment rather than done now.

## Toolchain and JAVA_HOME

`JAVA_HOME` on the development machine points at Java 21, used by unrelated projects.
It must not be changed. The `toolchain` block pins this project to Java 25
independently of `JAVA_HOME`, which is the whole reason it exists.

## Local-only data

| Folder | Populated from | Notes |
| --- | --- | --- |
| `tools/5etools-data/` | Manually, by the owner, extracting a [5etools-mirror-3/5etools-src](https://github.com/5etools-mirror-3/5etools-src) release zip's full contents here (the whole repo checkout, not just `data/` — its own `data/` subdirectory lands at `tools/5etools-data/data/`) | Gitignored, matching `.env` — never committed. The ingestion tool's default `--data` path is `tools/5etools-data/data`, not this folder's own root — confirmed 2026-09-17 against a real extraction. Not fetched from GitHub by the tool itself. |

## Versioning policy

Pin image tags explicitly. Never use `latest` in a file that will run on the VM.
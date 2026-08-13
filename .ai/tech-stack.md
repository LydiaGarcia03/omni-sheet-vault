# Tech stack

Record every dependency and version change here.

## Backend

| Item | Version | Notes |
| --- | --- | --- |
| Java | 25 | Toolchain-pinned in `build.gradle.kts` |
| Spring Boot | 4.1.x | Runs on Spring Framework 7 and Spring Security 7 |
| Gradle | 9.x | Build rooted at the repository; backend is `:apps:api` |
| PostgreSQL | 17 | JSONB for system-specific sheet payloads |
| Flyway | Managed by Boot | Needs `flyway-database-postgresql` alongside `flyway-core` |
| Testcontainers | Managed by Boot | Real PostgreSQL in tests; H2 is forbidden |

Starters in use: `web`, `validation`, `data-jpa`, `security`,
`oauth2-resource-server`, `actuator`, plus `docker-compose` as `developmentOnly`.

**Warning for anyone searching the web:** most Spring Boot material online targets
Boot 3. Boot 4 changed parts of the security configuration API. Verify against the
Boot 4 documentation before copying a snippet.

Deliberately absent: Lombok (records cover the need), Hypersistence Utils (Hibernate
maps JSONB natively via `@JdbcTypeCode(SqlTypes.JSON)`).

<<<<<<< Updated upstream
=======
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

>>>>>>> Stashed changes
## Frontend

| Item | Notes |
| --- | --- |
| React + TypeScript | Scaffolded with Vite |
| Vite | Dev server on 5173 |
| oidc-client-ts | Authorization Code flow with PKCE |

No SSR framework: the application lives entirely behind a login, so server rendering
and SEO would add complexity with no benefit.

## Infrastructure

| Service | Image | Ports |
| --- | --- | --- |
| PostgreSQL | `postgres:17-alpine` | 5432 |
| Keycloak | `quay.io/keycloak/keycloak` | 8081 |
| MinIO | `minio/minio` | 9000 API, 9001 console |

Keycloak keeps its own database inside the same PostgreSQL container, created by a
first-boot script in `infra/postgres/init/`.

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

## Versioning policy

Pin image tags explicitly. Never use `latest` in a file that will run on the VM.
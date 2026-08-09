# CLAUDE.md — omni-sheet-vault

Entry point for any AI agent working on this repository.
Read this file **first**, then load only the `.ai/` documents relevant to the task.

---

## 1. Project snapshot

**omni-sheet-vault** is a web application for creating, storing and playing with
tabletop RPG character sheets across **multiple game systems**.

It exists because single-system tools (D&D Beyond) do not cover other systems, and
generic tools (Roll20) treat sheets as dumb forms with no mechanics behind them.
This project aims for both: system-aware mechanics *and* system plurality.

Core capabilities:

- Authenticated, per-user character vault
- Guided character creation that explains the consequences of each choice before it is made
- Server-side dice engine with a permanent, auditable roll history
- Active mechanics (cast a spell, then roll its damage — not just static numbers)
- Character portraits and long-form backstory

**Stack**: Java 25 + Spring Boot (Gradle) · React + Vite · PostgreSQL · Keycloak · MinIO · Docker

---

## 2. Language policy (non-negotiable)

| Context | Language |
| --- | --- |
| Conversation with the user | Brazilian Portuguese |
| Code, identifiers, comments | English |
| Documentation content and file names | English |
| Commit messages, branch names | English |
| End-user UI copy | English (i18n-ready from day one) |

Never write documentation or code in Portuguese, even when the request that triggered
it was written in Portuguese.

---

## 3. The `.ai/` index

Every document below lives in `.ai/`. Read them on demand — do not load all of them
for every task.

| Document | Read it when | Write to it when |
| --- | --- | --- |
| `project-purpose.md` | Starting a session, or when a request seems to conflict with the product vision | The scope or motivation of the product changes |
| `ground-rules.md` | **Always, before writing or editing any code** | The user establishes a new convention |
| `architecture.md` | Adding a module, a layer, a dependency, or moving files around | An architectural boundary changes |
| `tech-stack.md` | Adding or upgrading a dependency, or setting up tooling | A version or tool changes |
| `domain-model.md` | Touching entities, persistence, or anything named after a domain concept | A domain concept is added, renamed or removed |
| `database-schema.md` | **Before writing any migration, entity or query** | A table, column, index or constraint changes |
| `features/*.md` | Implementing or changing that specific feature | That feature's behaviour changes |
| `rulesets/*.md` | Implementing or changing support for that specific game system | That system's rules coverage changes |
| `decisions/*.md` | A choice looks arbitrary and you need the reasoning behind it | A decision with long-term consequences is made |
| `ui-design-system.md` | Building or restyling any UI | Design tokens or component patterns change |
| `glossary.md` | You hit an unfamiliar RPG or project term | A new term enters the codebase |
| `changelog.md` | Rarely — for recent history | **After every completed change** |

### Fixed reading order for a coding task

1. `ground-rules.md`
2. `architecture.md`
3. The relevant `features/*.md` and/or `rulesets/*.md`
4. `domain-model.md` and `database-schema.md` if the change touches persistence
5. `decisions/` only when a constraint looks arbitrary

`features/`, `rulesets/`, `ui-design-system.md` and `glossary.md` are created as the
corresponding work starts. A missing file is not an error — say so and move on.

---

## 4. Repository layout

```
omni-sheet-vault/
├── .ai/                  # AI-facing documentation (see index above)
├── apps/
│   ├── api/              # Java 25 + Spring Boot backend — Gradle subproject :apps:api
│   │   ├── build.gradle
│   │   └── src/
│   └── web/              # React + Vite frontend — independent npm project
├── infra/
│   ├── keycloak/         # Exported realm, imported on container startup
│   └── postgres/init/    # First-boot SQL scripts
├── gradle/               # Gradle wrapper — the repository root owns the build
├── gradlew
├── gradlew.bat
├── settings.gradle       # Declares include 'apps:api'
├── docker-compose.yml    # Local infrastructure
├── .env                  # Local credentials — never committed
├── .env.example          # Template for .env — committed
├── .gitignore
├── CLAUDE.md
└── README.md
```

**The Gradle build is rooted at the repository, not at `apps/api`.** The backend is
the subproject `:apps:api`. There is no wrapper and no `settings.gradle` inside
`apps/api` — if one appears, it is stale and must be deleted.

The frontend stays outside the Gradle build. Never add `apps/web` as a subproject.

---

## 5. Commands

All Gradle commands run **from the repository root**, targeting the subproject.
The development machine is Windows: `.\gradlew.bat` locally, `./gradlew` elsewhere.

```bash
# Infrastructure — repository root
docker compose up -d
docker compose ps
docker compose logs -f keycloak
docker compose down

# Backend — repository root, targeting the subproject
./gradlew :apps:api:bootRun
./gradlew :apps:api:test
./gradlew :apps:api:check

# Frontend — run from apps/web
npm run dev
npm test
```

Local service map: API `8090` · Keycloak `8081` · Postgres `5432` ·
MinIO API `9000` · MinIO console `9001` · Vite `5173`.

The API runs on `8090` because `8080` is occupied by another process on the
development machine. Do not assume `8080` in code, config or docs.

`JAVA_HOME` on the development machine points at a Java 21 installation used by other
projects. That is intentional and must not be changed: the `toolchain` block in
`apps/api/build.gradle` pins this project to Java 25 regardless of `JAVA_HOME`.

---

## 6. Working agreement

**Before coding**

- Read the documents listed in section 3 for the task type.
- If a request contradicts `architecture.md` or `ground-rules.md`, stop and say so
  instead of silently deviating.
- If the task is larger than a single vertical slice, propose a plan first and wait
  for approval.

**While coding**

- Follow SOLID and Clean Code. In particular: a new game system must be added by
  writing new classes, never by editing existing ones (Open/Closed).
- Every schema change is a Flyway migration. Never rely on Hibernate to alter schema.
- Prefer small, intention-revealing methods over comments that explain bad code.
- Tests come with the code, not after it.

**After coding**

- Append an entry to `.ai/changelog.md`.
- Update any `.ai/` document the change made inaccurate.
- Summarize what changed and what was deliberately left out.

**Never**

- Commit secrets, `.env` contents, or the exported realm's client secrets.
- Add a dependency without recording it in `.ai/tech-stack.md`.
- Introduce a framework or library that was not agreed with the user.
- Handle passwords in application code — Keycloak owns credentials, the API only
  validates tokens.
- Ship UI assets copied from another product; visual references guide our own assets.

---

## 7. Definition of done

A change is done when it compiles, its tests pass, `./gradlew :apps:api:check` is green,
the `.ai/` docs reflect reality, and the changelog has an entry.
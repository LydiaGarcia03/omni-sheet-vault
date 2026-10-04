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

| Context | Language                                   |
| --- |--------------------------------------------|
| Conversation with the user | Brazilian Portuguese                       |
| Code, identifiers, comments | American English                           |
| Documentation content and file names | American English                           |
| Commit messages, branch names | American English                           |
| End-user UI copy | American English (i18n-ready from day one) |

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
| `features/*.md` | Implementing or changing a **platform** feature, one that works the same for every game system (shell, home, editions, sharing, the generic sheet behaviour) | That feature's behaviour changes |
| `systems/README.md` | Any task that names a game system, or before touching `ruleset.<system>` / `apps/web/src/systems/<system>` | A system is added, or its status changes |
| `systems/<system-id>/*.md` | Implementing or changing that game system: its rules, sheet layout and sheet build | That system's rules coverage or sheet changes |
| `systems/<system-id>/features/*.md` | Implementing or changing a feature that exists only for that system (e.g. D&D's 5etools ingestion, spellcasting pools) | That feature's behaviour changes |
| `systems/<system-id>/references/*.md` | Checking fidelity against the system's reference site or source (walkthroughs, audits) | A new walkthrough or audit is done |
| `decisions/*.md` | A choice looks arbitrary and you need the reasoning behind it | A decision with long-term consequences is made |
| `ui-design-system.md` | Building or restyling any UI | Design tokens or component patterns change |
| `glossary.md` | You hit an unfamiliar RPG or project term | A new term enters the codebase |
| `developer-guide.md` | Updating the editions (server, desktop), or looking for where a process lives in `apps/api` / `apps/web` | A key class moves, a process changes, a release step changes, or a game system is added |
| `changelog.md` | Rarely — for recent history | **After every completed change** |
| `roadmap.md` | **When asked to plan work or start a phase** | A phase is finished, or the plan changes |
| `open-items.md` | Starting a session, or when the owner asks what's pending | A decision is deferred to the owner, or one is answered |

### Fixed reading order for a coding task

1. `ground-rules.md`
2. `architecture.md`
3. The relevant `features/*.md` (platform), and for system work
   `systems/README.md` followed by the documents of that system's folder
4. `domain-model.md` and `database-schema.md` if the change touches persistence
5. `decisions/` only when a constraint looks arbitrary

### Where a new document goes

- Works the same for every game system → `features/`.
- Belongs to one game system → `systems/<system-id>/`. Use the root for
  system-wide specs, `features/` for one feature's plan, and `references/` for
  comparisons with an external source.
- `<system-id>` matches the system identifier in code (`dnd-5e`, `vtm-v5`).
  A new system gets a new folder and a row in `systems/README.md`, never edits
  to another system's folder.
- `changelog.md` keeps the paths that were valid when each entry was written.
  Older entries may name `rulesets/…` or `features/…` paths that moved on
  2026-10-03; `systems/README.md` maps the old names to the new ones.

`features/`, `systems/`, `ui-design-system.md` and `glossary.md` are created as the
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
│   ├── desktop/          # Desktop edition: launcher window + Windows package — Gradle subproject :apps:desktop
│   │   ├── build.gradle
│   │   ├── README.md     # How to build, share, install and use the desktop edition
│   │   └── src/
│   └── web/              # React + Vite frontend — independent npm project
├── infra/
│   ├── keycloak/         # Exported realm, imported on container startup
│   └── postgres/init/    # First-boot SQL scripts
├── gradle/               # Gradle wrapper — the repository root owns the build
├── gradlew
├── gradlew.bat
├── settings.gradle       # Declares include 'apps:api' and 'apps:desktop'
├── docker-compose.yml    # Local infrastructure
├── .env                  # Local credentials — never committed
├── .env.example          # Template for .env — committed
├── .gitignore
├── CLAUDE.md
└── README.md
```

**The Gradle build is rooted at the repository, not at `apps/api`.** The backend is
the subproject `:apps:api`. The desktop edition's launcher and packaging are the
subproject `:apps:desktop`, which depends on `:apps:api`. There is no wrapper and no `settings.gradle` inside
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

# Desktop edition — repository root, Windows only (see apps/desktop/README.md)
./gradlew :apps:desktop:packageDesktop   # → apps/desktop/build/distributions/OmniSheetVault-<version>-windows.zip
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
- Run commits or any Git command. Instead, let the user know what should be done.
- Add comments regarding what was asked for by the user in code classes. Add those type of comments in specific Markdown files, like `.ai/chagelogs.md`. Code classes should only have comments summarizing in one line maximum what that part of the code does. 
---

## 7. Definition of done

A change is done when it compiles, its tests pass, `./gradlew :apps:api:check` is green,
the `.ai/` docs reflect reality, and the changelog has an entry.
# Delivery roadmap

The order in which omni-sheet-vault gets built. Each phase is a vertical slice that
ends in something demonstrable.

**Read this when asked to plan work or to start a phase.** Read the phase's own entry
plus the documents it lists before writing code.

## Rules for every phase

- One phase at a time. Do not start the next one, even if it looks trivial.
- Propose a plan and wait for approval before writing code.
- Tests ship with the code. Persistence tests run against real PostgreSQL via
  Testcontainers.
- Every schema change is a Flyway migration.
- The phase ends with a changelog entry and any `.ai/` document the work invalidated
  brought up to date.
- Anything listed as out of scope stays out, even when it is one line away. Say what
  you left out.

## Status

| Phase | Name | State |
| --- | --- | --- |
| 0 | Foundations | Done |
| 1 | Identity | Not started |
| 2 | Character vault | Not started |
| 3 | Ruleset seam | Not started |
| 4 | Sheet — vitals zone | Not started |
| 5 | Dice engine | Not started |
| 6 | Session state | Not started |
| 7 | Content catalogue | Not started |
| 8 | Sheet — tabs and sidebar | Not started |
| 9 | Rests and mechanics | Not started |
| 10 | Character creation | Not started |
| 11 | Second game system | Not started |

---

## Phase 0 — Foundations · done

Repository layout, `.ai/` documentation, Docker infrastructure (PostgreSQL, Keycloak,
MinIO), Gradle build on Java 25, empty Spring Boot application, GitHub Actions with
SonarQube, branch protection, and the frame kit reference implementation.

---

## Phase 1 — Identity

**Goal:** a person logs in and reaches an authenticated empty screen.

Read: `decisions/adr-0002-keycloak-for-identity.md`, `architecture.md`,
`database-schema.md`.

In scope:

- Spring Security as an OAuth2 resource server validating Keycloak tokens
- First Flyway migration: the `players` table
- On first authenticated request, create the local player row from the token subject
- `GET /api/me` returning the current player
- React app scaffolded with Vite, OIDC login with PKCE via `oidc-client-ts`
- Authenticated shell: header, logout, empty content area

Out of scope: characters, the sheet, any styling beyond a plain shell.

**Done when** an unauthenticated visit redirects to Keycloak, a successful login
returns to the app, and `/api/me` responds with the player. An unauthenticated request
to a protected endpoint returns 401.

---

## Phase 2 — Character vault

**Goal:** characters can be created, listed and deleted.

Read: `database-schema.md`, `domain-model.md`, `features/character-sheet.md`.

In scope:

- Migration: the `characters` table, including the JSONB payload column
- Create, list, read and soft-delete endpoints
- Ownership enforced from the token subject in the service layer, never from a
  client-supplied identifier
- Character list screen, grouped by game system
- The summary line under each character name comes from the backend, not the frontend

Out of scope: the sheet itself, portraits, any game rules. A created character is an
empty shell with a name and a system identifier.

**Done when** a player creates a character, sees it in the list, and cannot read or
delete another player's character.

---

## Phase 3 — Ruleset seam

**Goal:** the strategy pattern exists and D&D 5e is registered behind it.

Read: `decisions/adr-0003-code-per-game-system.md`,
`decisions/adr-0004-strategy-with-registry.md`, `architecture.md`.

In scope:

- The `ruleset` package: `GameSystem`, `SheetCalculator`, and their registries
- Registries assembled by injecting implementations, keyed by `systemId`
- An unknown identifier throws `UnsupportedGameSystemException`, mapped to HTTP 400
- `Dnd5eSheet` as a typed record with Bean Validation and a schema version
- `Dnd5eSheetCalculator` producing ability modifiers, proficiency bonus, armour class,
  initiative and hit points — **each with its derivation trace**
- `GET /api/characters/{id}/sheet` returning calculated values with contributions
- A seeded test character so the sheet can be built before creation exists

Out of scope: spells, actions, inventory, features. Only the values the vitals zone
needs.

**Done when** the endpoint returns armour class as a value plus the labelled
contributions that produced it, and the calculator is unit-tested against the rules.

---

## Phase 4 — Sheet, vitals zone

**Goal:** the top of the sheet renders real data.

Read: `rulesets/dnd-5e-sheet-build.md` first, then `ui-design-system.md`,
`ui-design-tokens.md`, `rulesets/dnd-5e-sheet-ui.md`.

**Open `apps/web/reference/frame-kit.html` before writing any component.** Port its
patterns; do not reinvent them.

In scope: the screen grid, the header, the top row (abilities, proficiency, speed,
inspiration, hit points), and the three columns (saving throws, senses, proficiencies,
skills, initiative, armour class, defenses, conditions).

Read-only. Nothing is clickable yet.

Out of scope: tabs, the sidebar, rolling, any mutation.

**Done when** the vitals zone matches its reference screenshots side by side, and
values that are still estimated are named rather than silently invented.

---

## Phase 5 — Dice engine

**Goal:** clicking a roll target rolls on the server and records it.

Read: `features/character-sheet.md`, `database-schema.md`.

In scope:

- Migration: the `rolls` table, append-only
- Server-side resolution: the client sends what is being rolled, never a result
- Every roll persisted with expression, context, individual results and total
- The dice tray, with a result log — no 3D rendering
- The game log panel
- Roll targets in the vitals zone wired up, with the hover affordance

Out of scope: 3D dice, advantage and disadvantage, rolls from tabs.

**Done when** an ability modifier click produces a server-resolved result that appears
in the tray and the game log, and a failed request produces no fabricated local result.

---

## Phase 6 — Session state

**Goal:** the sheet is usable at the table.

In scope: hit point damage, healing and temporary points; heroic inspiration; toggling
conditions (display only, no rule effects); optimistic updates that roll back on
failure.

Out of scope: resources with recharge triggers, which arrive with rests in phase 9.

**Done when** damage and healing persist and survive a reload, and a failed write
restores the previous value with an error surfaced.

---

## Phase 7 — Content catalogue

**Goal:** spells, items, features and creatures exist as reference data.

Read: `decisions/adr-0005-content-catalogue-and-redaction.md`.

In scope:

- Catalogue schema, separate from character tables — reference data, loaded by import
- An import pipeline, run as a command rather than at application startup
- The redactable text field type in the API contract
- Server-side redaction switch, enabled by configuration

Out of scope: the UI that displays any of it.

**Done when** the catalogue imports reproducibly, and with redaction enabled the API
returns markers instead of prose while names, numbers and tags still arrive.

---

## Phase 8 — Sheet, tabs and sidebar

**Goal:** the rest of the sheet.

In scope: the tab bar; actions, spells, inventory, features and traits, background and
notes, extras; the sidebar with all five molds; the redacted-text placeholder.

Build the tab bar and one tab first, then the sidebar shell with the explainer mold,
then the rest. Do not build all six tabs before the first one is reviewed.

**Done when** every tab renders, the sidebar replaces its content rather than stacking,
and every list supports search and filtering.

---

## Phase 9 — Rests and mechanics

**Goal:** spending and recovering resources.

In scope: resources modelled with an explicit recharge trigger; spell slots; short and
long rest resolved server-side as a single mechanic; casting a spell into its damage
roll; rest dice recorded in the log.

**Done when** a short rest restores exactly the resources whose trigger matches, in one
server operation, and its dice appear in the log.

---

## Phase 10 — Character creation

**Goal:** characters are built in the application rather than seeded.

In scope: the guided flow, showing the consequences of each choice before it is
committed; levelling up; portrait upload to storage.

This phase needs its own specification first. Write
`features/character-creation.md` before planning it.

---

## Phase 11 — Second game system

**Goal:** prove the architecture.

The real test of every decision made so far. Adding it must require **no edits** to the
shell, the character list, the sidebar, the dice engine, or D&D's own code. If it does,
the seam is in the wrong place — stop and discuss rather than working around it.

Extract shared abstractions here, from two real implementations. Not before.

---

## Asking for work

To plan: *"Read `.ai/roadmap.md` and propose a plan for phase N."*

To build: *"Start phase N."* The agent reads this file, the documents that phase lists,
proposes a plan, and waits.

Mid-phase: *"Continue phase N"* — the changelog says where the last one stopped.
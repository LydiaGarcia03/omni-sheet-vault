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
| 1 | Identity | Done |
| 2 | Character vault | Done |
| 3 | Ruleset seam | Done |
| 4 | Sheet — vitals zone | Done |
| 5 | Dice engine | Done |
| 6 | Session state | Done |
| 7 | Content catalogue | Done |
| 8 | Sheet — tabs and sidebar | Done |
| 9 | Rests and mechanics | Done |
| 10 | Sheet fidelity to D&D Beyond | Done |
| 11 | Character creation | Not started |
| 12 | Second game system | Not started |

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

Read: `systems/dnd-5e/sheet-build.md` first, then `ui-design-system.md`,
`ui-design-tokens.md`, `systems/dnd-5e/sheet-ui.md`.

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

Out of scope at the time: 3D dice, advantage and disadvantage (built in phase 10,
see `changelog.md`'s 2026-09-14 entry), rolls from tabs, manual/custom rolls typed
into the dice tray (see `systems/dnd-5e/sheet-ui.md`'s "Deferred to later
versions" for what's still deferred).

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

## Phase 8 — Sheet, tabs and sidebar · done

**Goal:** the rest of the sheet.

In scope: the tab bar; actions, spells, inventory, features and traits, background and
notes, extras; the sidebar with all five molds; the redacted-text placeholder.

Build the tab bar and one tab first, then the sidebar shell with the explainer mold,
then the rest. Do not build all six tabs before the first one is reviewed.

**Done when** every tab renders, the sidebar replaces its content rather than stacking,
and every list supports search and filtering. Inventory was the last list missing
search; closed 2026-08-15 (see `systems/dnd-5e/sheet-build.md`'s component inventory).

---

## Phase 9 — Rests and mechanics

**Goal:** spending and recovering resources.

In scope: resources modelled with an explicit recharge trigger; spell slots; short and
long rest resolved server-side as a single mechanic; casting a spell into its damage
roll; rest dice recorded in the log.

**Done when** a short rest restores exactly the resources whose trigger matches, in one
server operation, and its dice appear in the log.

---

## Phase 10 — Sheet fidelity to D&D Beyond · done

**Goal:** close the gap between this application's D&D 5e sheet and D&D Beyond's own
— visually and functionally, not just approximately.

Read: `systems/dnd-5e/references/sheet-fidelity-audit.md` first — the audit pass is already
done, this phase is the fixing. Then `systems/dnd-5e/sheet-ui.md` (especially
"Visual target" and "Verifying fidelity against D&D Beyond"), `ui-design-system.md`,
`ui-design-tokens.md`, `systems/dnd-5e/sheet-build.md`.

By the end of phase 8, every tab and mold exists, but several were built against
screenshots, estimated measurements, or a best guess where no clearer spec existed
— not against the live reference, side by side, control by control. This phase is
that audit-and-fix pass: go through the vitals zone, all six tabs and every sidebar
mold against **https://www.dndbeyond.com/characters/50149479**, and correct
whatever doesn't match — layout, spacing, hover states, button behavior, panel
contents, everything — except what "Deviations from D&D Beyond" already lists as
deliberate.

`systems/dnd-5e/references/sheet-fidelity-audit.md` is that plan: a tab-by-tab comparison
against the live reference, findings classified as a straightforward fix or a
scope decision needing the owner's input, plus a suggested slice order. Its "Open
questions for the owner" must be answered — at least the ones blocking the first
few slices — before fixing starts; the suggested order itself is a starting point,
not fixed, and may reorder as slices reveal more.

Every mismatch found that isn't an oversight — i.e., looks like it might be a
deliberate choice already made and simply undocumented — gets confirmed with the
owner and, if confirmed, added to "Deviations from D&D Beyond" rather than silently
changed either way.

**Done when** every screen, tab and mold has been compared live against the
reference character sheet and matches it, or the mismatch is a confirmed,
documented entry in "Deviations from D&D Beyond."

---

## Phase 11 — Character creation

**Goal:** characters are built in the application rather than seeded.

In scope: the guided flow, showing the consequences of each choice before it is
committed; levelling up; portrait upload to storage.

**Was blocked on phase 10 (visual fidelity), unblocked 2026-09-20 — now blocked
again on a stricter condition, direct owner decision 2026-09-22: the guided flow
does not start until the sheet it would build is verified 100% correct against
*two* standards at once — D&D Beyond's own live sheet (visual/behavioral
fidelity, phase 10's own standard) *and* real 5etools data (mechanical
correctness — no hand-typed or mocked fact anywhere the sheet currently shows a
value).** This phase's own specification, `systems/dnd-5e/features/character-creation.md`,
sequences that prerequisite work (its own Stages A and C) ahead of the creation
flow itself (Stage D2) rather than treating them as already covered by phase 10's
"done" status — phase 10 checked layout and behavior against D&D Beyond, never
whether the underlying values came from real game data.

This phase's specification is written: `systems/dnd-5e/features/character-creation.md`. Read it
before planning further — it also carries the still-open
`decisions/adr-0006-5etools-as-content-source.md` question (excluding non-core/
collab sources at character-creation time) forward into its own Stage D2, where
that toggle now belongs.

**Target outcome, owner statement 2026-09-23:** a conversational rebuild in which
the owner names class, level, species and background, and the assistant asks each
implied choice from real 5etools data. The resulting sheet is living: equipment,
active spells and conditions change AC, speed and roll modes. That rebuild is
Stage D1, and it is not gated, because it produces the characters the Stage C
audit checks.

**Stage C gate passed (2026-09-24).** The data-fidelity audit
(`systems/dnd-5e/references/data-fidelity-audit.md`) closed on Aria, Liriel and Vex:
every finding is fixed or accepted by the owner, and the visible changes were
checked live against D&D Beyond. Stage D2 (the in-app creation flow) is
unblocked.

**Next up in D2 (approved 2026-09-25):** `systems/dnd-5e/features/builder-refinements.md`,
in four slices (all done 2026-09-26):
1. dropups, book order, collapsible spell filters, and the playtest limiter;
2. virtual 4d6-drop-lowest ability rolls and the rolled hit points UI;
3. technical summaries on options, where the effect replaces the book name;
4. a collapsed class-progression preview (the book's class table) on the
   Classes step.

---

## Phase 12 — Second game system

**Goal:** prove the architecture.

The real test of every decision made so far. Adding it must require **no edits** to the
shell, the character list, the sidebar, the dice engine, or D&D's own code. If it does,
the seam is in the wrong place — stop and discuss rather than working around it.

Extract shared abstractions here, from two real implementations. Not before.

**Candidate system: Vampire: The Masquerade 5e** (2026-10-03). The plan,
the Demiplane walkthrough and the seams it breaks are in
`systems/vtm-v5/features/implementation-plan.md`; it waits on the owner's answers there.

---

## Asking for work

To plan: *"Read `.ai/roadmap.md` and propose a plan for phase N."*

To build: *"Start phase N."* The agent reads this file, the documents that phase lists,
proposes a plan, and waits.

Mid-phase: *"Continue phase N"* — the changelog says where the last one stopped.
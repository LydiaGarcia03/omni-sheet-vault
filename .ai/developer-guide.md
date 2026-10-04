# Developer guide — updating the vault and finding your way in the code

A practical map for the owner: how to bring every edition up to date after a
change, and where the code that does each thing lives, in the API and in the
web app. Game-system code has its own section on each side, so a new system
(Vampire, Daggerheart, Pathfinder 2e…) gets a sibling section next to D&D 5e.

- [Part 1 — Updating everything](#part-1--updating-everything)
- [Part 2 — API code map (`apps/api`)](#part-2--api-code-map-appsapi)
- [Part 3 — Web code map (`apps/web`)](#part-3--web-code-map-appsweb)
- [Part 4 — Adding a new game system](#part-4--adding-a-new-game-system)

Line numbers in `file:line` references drift as the code changes. When one is
off, search for the method name next to it.

Related documents: `architecture.md` (the why), `apps/desktop/README.md` (the
desktop guide for players), `features/desktop-edition.md`,
`systems/dnd-5e/features/5etools-ingestion.md`.

---

## Part 1 — Updating everything

### 1.1 What to run after a change

All commands run from the repository root unless stated otherwise. The
development machine is Windows, so they use `.\gradlew.bat`.

| What changed | Local server edition | Desktop edition |
| --- | --- | --- |
| Java code in `apps/api` | Restart `bootRun` | Rebuild the package (1.4) |
| A new Flyway migration | Restart `bootRun`; Flyway applies it on start | Rebuild; it is applied on the player's first start |
| Web code in `apps/web` | Nothing: Vite reloads on save | Rebuild the package (1.4) |
| `apps/web/package.json` | `npm install` in `apps/web` | `npm install`, then rebuild |
| `content/` (catalogue JSON) | Re-import the catalogue (1.3) | Rebuild; the new content is imported on the player's first start |
| `apps/desktop` (launcher) | — | Rebuild the package (1.4) |
| `docker-compose.yml`, `infra/` | `docker compose up -d` | — (the desktop edition uses neither) |

### 1.2 Server edition (local development)

```powershell
docker compose up -d                       # Postgres, Keycloak, MinIO
.\gradlew.bat :apps:api:bootRun            # API on 8090, dev profile, migrations applied on start
cd apps\web; npm install; npm run dev      # Web on 5173; Vite proxies /api, /oauth2, /login/oauth2, /logout to 8090
```

- `bootRun` sets the `dev` profile (`apps/api/build.gradle`), which imports
  the repository's `.env`.
- Spring Boot's Docker Compose support (`application.yml`,
  `spring.docker.compose.file`) also starts the containers if they are down.
- Open `http://localhost:5173` and log in with the local dev user.

### 1.3 Refreshing the catalogue (`content/`)

The catalogue is generated, never hand-written (adr-0006):

1. **Regenerate the JSON from 5etools** (only when 5etools data or a
   converter changed). Put a 5etools release in `tools/5etools-data/`, then:

   ```powershell
   .\gradlew.bat :apps:api:ingest5etools                       # every kind
   .\gradlew.bat :apps:api:ingest5etools --args="--kind=spell" # one kind
   ```

   Output: `content/dnd-5e/<kind>/<slug>.json`.

2. **Import it into the server's database.** `--import-catalogue` takes one
   kind directory per run and exits when done. To import every kind:

   ```powershell
   Get-ChildItem content\dnd-5e -Directory |
     Where-Object { $_.Name -notin 'builds', 'mechanics' } |
     ForEach-Object { .\gradlew.bat :apps:api:bootRun --args="--import-catalogue=$($_.FullName)" }
   ```

   `builds/` holds character build files and `mechanics/` the adr-0007
   overlay, which is read during ingestion; neither is a catalogue kind.

The desktop edition needs neither step by hand: it imports `content/` on its
own whenever the content's fingerprint changes (2.3, "Catalogue import").

### 1.4 Desktop edition — building a new version

The `.exe` is a snapshot of the code at the moment it was built. Every change
since then reaches the players only through a new package.

**1. Check the code is green.**

```powershell
.\gradlew.bat :apps:api:check :apps:desktop:test
cd apps\web; npm test; cd ..\..
```

**2. Raise the version** in `apps/desktop/build.gradle`:

```groovy
def appVersion = '0.2.0'   // was 0.1.0
```

It names the zip and the `.exe`'s version, so players can tell builds apart.
Use `MAJOR.MINOR.PATCH` with numbers only: `jpackage` refuses anything else.

**3. Build the package.**

```powershell
.\gradlew.bat :apps:desktop:packageDesktop
```

It runs four tasks in order; each one can be run on its own when debugging:

| Task | What it does |
| --- | --- |
| `buildDesktopWebApp` | `npm run build:desktop` → `apps/web/dist-desktop` |
| `stageDesktopApp` | Copies the launcher jar, every runtime jar (API included), `dist-desktop` as `web/`, and `content/` |
| `desktopAppImage` | `jpackage --type app-image` → `apps/desktop/build/jpackage/image/OmniSheetVault/` |
| `packageDesktop` | Zips it → `apps/desktop/build/distributions/OmniSheetVault-<version>-windows.zip` |

Old output is replaced on each run, so nothing from the previous build
lingers.

**4. Test the update the way a player will get it.** Your own
`%LOCALAPPDATA%\OmniSheetVault` still has the data of the previous version, so
it is the best test of the upgrade path:

1. Back up `%LOCALAPPDATA%\OmniSheetVault` (copy the folder).
2. Close any running copy of the vault (its window's **Quit**).
3. Extract the new zip **outside the repository** and open
   `OmniSheetVault.exe`.
4. Check that the old characters open, and that the new changes are there.
   The first start after an update is slower: Flyway migrates the database
   and the catalogue is re-imported if `content/` changed.
5. If it fails, read `%LOCALAPPDATA%\OmniSheetVault\logs\omni-sheet-vault.log`.

**5. Share it.** Upload the zip (about 150 MB) to Google Drive, OneDrive or
WeTransfer. Players follow "Updating to a new version" in
`apps/desktop/README.md`: quit, delete the old program folder, extract the new
one. Their characters live in `%LOCALAPPDATA%\OmniSheetVault`, which is never
touched.

**6. Record it.** A changelog entry naming the version and what it brings.

**Pitfalls**

- **A migration must be safe on real data.** Players skip versions, so the
  migrations from their version to the newest one all run in one go on their
  data. Never edit an applied migration; add a new one.
- **The sheet JSON must stay readable.** Sheets are JSONB; a field renamed or
  removed in `Dnd5eSheet` without a fallback breaks old characters on a
  player's machine, where you can't fix them by hand. Keep old fields
  readable, or write a migration that rewrites the JSON (V8 is an example).
- **A running Vite dev server is fine:** it ignores `dist-desktop`.
- **No JDK 25?** The toolchain finds it on its own; `JAVA_HOME` can stay on
  Java 21. It must be a full JDK, with `jpackage`.

### 1.5 Release checklist

- [ ] `:apps:api:check`, `:apps:desktop:test` and `npm test` green
- [ ] `appVersion` raised
- [ ] `packageDesktop` built
- [ ] Upgrade tested over the previous version's data (1.4, step 4)
- [ ] `changelog.md` entry
- [ ] Zip shared together with `apps/desktop/README.md` Part 2

---

## Part 2 — API code map (`apps/api`)

Root package: `dev.omnisheetvault.api` (`apps/api/src/main/java/…`).
Resources: `apps/api/src/main/resources/` (`application*.yml`,
`db/migration/V*.sql`).

### 2.1 Packages at a glance

| Package | Owns | System-specific? |
| --- | --- | --- |
| `character` | Character vault: CRUD, ownership, drafts, level up, the sheet's HTTP surface, portraits | No |
| `dice` | Server-side rolls and the immutable roll history | No |
| `ruleset` | The strategy interfaces and the shared value types every system returns | Interfaces: no |
| `ruleset.registry` | Lookup of a strategy by system id; no rules | No |
| `ruleset.<system>` | **All rules of one game system** (`ruleset.dnd5e` today) | **Yes** |
| `catalogue` | Catalogue storage, import, HTTP; the 5etools ingestion tool | Ingestion: D&D 5e |
| `identity` | Maps the token's subject to a `Player`; `/api/me` | No |
| `storage` | Portrait files: S3 (server) or file system (desktop) | No |
| `desktop` | Desktop edition settings, embedded Postgres, serving the web build | No |
| `shared` | Security, CSRF, error handling | No |

Rule of thumb: **if the change is a game rule, it belongs in `ruleset.<system>`.**
Everything else is the platform and must not name a system.

### 2.2 Platform classes (system-agnostic)

#### `character`

| Class | Role |
| --- | --- |
| `Character` | JPA entity: id, owner, `systemId`, status (`DRAFT`/`ACTIVE`), `sheet` (JSONB text), creation draft, level-up draft, portrait |
| `CharacterRepository` | Spring Data repository; only `CharacterService` uses it |
| `CharacterService` | Ownership gate: `getMine`, `getMineActive`, `getMineDraft` check the token subject; create, rename, delete, `save` |
| `CharacterController` | Every sheet endpoint under `/api/characters/{id}/…` (HP, conditions, items, spells, rests, coins, customizations…) |
| `CharacterSheetService` | Orchestrator of play-time mutations: load → `SheetMutator` → save → `SheetCalculator` |
| `CharacterSheetResponse` | The sheet sent to the web app, built from `VitalsZone` (`from(VitalsZone)`) |
| `CharacterDraftController` / `CharacterDraftService` | Character creation: draft, autosave, finish |
| `CharacterLevelUpController` / `CharacterLevelUpService` | Level up: start, save, cancel, finish |
| `CharacterPortraitController` / `CharacterPortraitService` / `PortraitImage` | Portrait upload, presets, validation and re-encoding |
| `CharacterSummaryService` | The facts on each character card of the home page |
| `CharacterBuildRunner` (`--apply-builds`) / `CharacterRematerializeRunner` (`--rematerialize`) / `CharacterSheetPrintRunner` (`--print-sheets`) | Dev command-line tools: recreate characters from build files (resets play state); re-apply each character's own stored build keeping play state (use after a rules or data change); print sheets as a backup |
| `*Request` / `*Response` records | HTTP shapes; domain types never leave the API |

#### `dice`

| Class | Role |
| --- | --- |
| `RollController` | `POST /rolls` (sheet roll), `/rolls/manual` (dice tray), `/rolls/creation` (ability score rolls), `GET /rolls` (history) |
| `RollService` | Resolves the expression through `MechanicResolver`, rolls, applies advantage/disadvantage, persists |
| `DiceRoller` | The random source (swap it in tests) |
| `Roll` / `RollRepository` | The immutable roll event and its repository; never updated or deleted |
| `RollMode`, `KeptDice`, `DieType` | Advantage/disadvantage and dice types |

#### `ruleset` — the seams

| Interface | A system implements it to… |
| --- | --- |
| `GameSystem` | Declare its id and display name |
| `CharacterCreationFlow` | Start a draft, plan the build's choices, preview it, `materialize` it into a sheet, level up |
| `SheetCalculator` | Turn the stored sheet JSON into `VitalsZone`, every derived value |
| `SheetMutator` | Apply play-time changes (damage, items, spells, rests…) to the sheet JSON |
| `MechanicResolver` | Turn "roll this skill / attack / spell" into a dice expression |
| `CharacterSummarizer` | Give the home page's card facts |
| `CatalogueLookup` (port) | Read catalogue entries; implemented by `catalogue`, used by rules code |

`ruleset.registry.*Registry` index every implementation by `systemId()` and
throw `UnsupportedGameSystemException` (HTTP 400) for an unknown id.

Shared value types (`VitalsZone`, `CalculatedValue`, `Contribution`, `Item`,
`Spell`, `AttackRow`, `BuildPlan`, `MaterializedSheet`…) are what strategies
return; they carry no system name. `VitalsZone` is the whole computed sheet.

#### `catalogue`

| Class | Role |
| --- | --- |
| `CatalogueEntry` / `CatalogueEntryRepository` / `CatalogueEntryKind` | Catalogue rows (spells, items, classes…), one table for every system |
| `CatalogueService` / `CatalogueController` | Listing, sources, partners, one entry; redaction (adr-0005) |
| `CatalogueImportService` | Upserts one directory of JSON files; removes rows whose file is gone |
| `CatalogueImportRunner` | `--import-catalogue=<dir>` on the command line |
| `DesktopCatalogueBootstrap` | Desktop: imports all of `content/` on start when its SHA-256 changed |
| `DatabaseCatalogueLookup` / `ContentDirectoryCatalogue` | The `CatalogueLookup` port over Postgres / over `content/` files |
| `Ingest5eToolsMain` / `Ingest5eToolsRunner` + `*Converter` + `FiveETools*` | The 5etools → `content/` tool (`ingest5etools` Gradle task). D&D 5e only |
| `PlanBuildMain` | `planBuild` Gradle task: prints a build file's choices |

#### `identity`, `storage`, `desktop`, `shared`

| Class | Role |
| --- | --- |
| `identity.PlayerService` | `currentPlayer(jwt)`: finds or creates the `Player` for the token subject |
| `identity.CurrentUserController` | `GET /api/me` |
| `storage.PortraitStorage` | Port; `S3PortraitStorage` (server) and `FileSystemPortraitStorage` + `LocalPortraitFileController` (desktop) |
| `desktop.DesktopProperties` | `app.desktop.*`: data, content, runtime and web folders |
| `desktop.EmbeddedDatabaseConfig` | Starts the bundled PostgreSQL in the desktop profile |
| `desktop.DesktopWebAppConfig` | Serves the web build from the API in the desktop profile |
| `shared.SecurityConfig` | Server security: Keycloak login, session, CSRF (adr-0008) |
| `shared.SessionTokenAuthenticationFilter` | Renews and decodes the session's token, gives controllers a `Jwt` |
| `shared.DesktopSecurityConfig` + `LocalPlayerSecurityContextRepository` + `LocalHostOnlyFilter` | Desktop: one local player, no login, localhost only |
| `shared.ApiExceptionHandler` | Every exception → RFC 7807 problem detail |

### 2.3 Processes — where each one happens

#### A sheet change (e.g. "take 7 damage")

1. `CharacterController` — `POST /api/characters/{id}/hit-points/damage`.
2. `CharacterSheetService.applyDamage` (`CharacterSheetService.java:92`) calls
   the private `mutate` (`:520`):
   - `CharacterService.getMineActive` — ownership check;
   - `SheetMutatorRegistry.forSystem(systemId)` → `Dnd5eSheetMutator.applyDamage`;
   - `Character.replaceSheet` + `CharacterService.save`;
   - `SheetCalculatorRegistry.forSystem(systemId).calculateVitals` on the new JSON.
3. The controller returns `CharacterSheetResponse.from(vitals)`.

Almost every sheet endpoint follows this shape. **To add one:** a method on
`SheetMutator` (default implementation if only some systems support it), its
system implementation, a `CharacterSheetService` method using `mutate`, an
endpoint in `CharacterController`, and the web side (3.3).

#### Reading the sheet

`GET /api/characters/{id}/sheet` → `CharacterController.getSheet`
(`CharacterController.java:63`) → `CharacterSheetService.getVitals` →
`SheetCalculator.calculateVitals`. Nothing derived is stored: AC, modifiers,
attacks and so on are recalculated on every read.

#### A roll

`RollController` → `RollService.roll` (`RollService.java:51`):
`SheetCalculator` builds the vitals, `MechanicResolver.resolve(vitals, kind,
key, castAtLevel)` gives the dice expression, `DiceRoller` rolls, the mode
(advantage/disadvantage) is applied, and a `Roll` is saved. The dice tray's
free-form rolls go through `RollService.manualRoll` (`:115`).

#### Creating a character (draft → playable)

1. `POST /api/characters/drafts` → `CharacterDraftService.create`, with
   `CharacterCreationFlow.emptyDraft()`.
2. Every builder autosave → `PUT /draft` → `CharacterDraftService.save`
   (`:65`): `plan` (choices and problems) and `preview` (live summary).
3. `POST /draft/finish` → `CharacterDraftService.finish` (`:74`):
   `CharacterCreationFlow.materialize` turns the build into the sheet JSON,
   starting equipment is added, and the status becomes `ACTIVE`.

#### Level up

`CharacterLevelUpService.start` (`:84`) → `CharacterCreationFlow.withLevelUp`;
saves keep a level-up draft on the character; `finish` (`:126`) materializes
the build again over the current sheet, keeping play state.

#### Catalogue import

`CatalogueImportService.importFrom(dir)` (`:43`) reads every `*.json`,
validates it (`CatalogueEntryImport`), upserts by `(systemId, kind, slug)`,
then deletes rows of those kinds with no file left. Called by
`CatalogueImportRunner` (server, by hand) and `DesktopCatalogueBootstrap.run`
(desktop, on start).

#### Login and the current player

Server: `SecurityConfig` + `SessionTokenAuthenticationFilter` (adr-0008), then
`PlayerService.currentPlayer(jwt)` (`:35`). Desktop:
`LocalPlayerSecurityContextRepository` hands every request the `local-player`
subject; the rest of the code can't tell the difference.

#### Errors

Throw a specific exception (e.g. `SpellLimitExceededException`) and map it in
`ApiExceptionHandler`. Never return a stack trace.

#### Database changes

New file `db/migration/V{n}__{description}.sql` (next free number: see the
folder; V9 is the latest today). Append-only. Read `database-schema.md` first
and update it after.

### 2.4 Game systems — API

Each system is one subpackage of `ruleset`, plus its catalogue content in
`content/<system-id>/`. Nothing outside `ruleset` imports from a system
subpackage.

#### 2.4.1 D&D 5e (`dnd-5e`) — `ruleset.dnd5e`

**Strategy implementations (the entry points):**

| Class | Implements | Size |
| --- | --- | --- |
| `Dnd5eGameSystem` | `GameSystem` | small |
| `Dnd5eCharacterCreationFlow` | `CharacterCreationFlow` | ~220 lines |
| `Dnd5eSheetCalculator` | `SheetCalculator` | ~1,240 lines |
| `Dnd5eSheetMutator` | `SheetMutator` | ~1,050 lines |
| `Dnd5eMechanicResolver` | `MechanicResolver` | ~210 lines |
| `Dnd5eCharacterSummarizer` | `CharacterSummarizer` | small |

**Stored data (the JSONB shape):**

| Class | Role |
| --- | --- |
| `Dnd5eSheet` | The stored sheet record: abilities, HP, items, spells, features, customizations… |
| `Dnd5eSheetJsonMapper` | Reads and writes `Dnd5eSheet` ↔ JSON |
| `Dnd5eCharacterBuild` | The build document (choices made in the builder) |
| `Dnd5eItem`, `Dnd5eSpell`, `Dnd5eFeatureTrait`, `Dnd5eFeatureAction`, `Dnd5eExtra`, `Dnd5eCustomAction`, `Dnd5eBackground`… | Parts of the sheet |
| `Dnd5eModifier` / `Dnd5eModifiers` / `Dnd5eModifierTarget` / `Dnd5eModifierType` | Bonuses from items, feats, conditions (adr-0007) |
| `Dnd5eCustomizations` / `Dnd5eCustomizationEditor` | D&D Beyond's "Customize" overrides |

**Character creation and level up:**

| Class | Role |
| --- | --- |
| `Dnd5eBuildPlanner` | Which choices the build offers, which are pending, and their problems (~1,230 lines) |
| `Dnd5eSpellPlanner` / `Dnd5eSpellLists` | Spell choices per class and subclass |
| `Dnd5eChoiceOptions` / `Dnd5eOptionSummaries` / `Dnd5ePrerequisites` | The options offered for each choice |
| `Dnd5eBuildMaterializer` | Build → sheet: class levels, proficiencies, features, spells, slots, starting equipment |
| `Dnd5eClassProgression` / `Dnd5eHitDicePools` | Per-class progression tables and hit dice |
| `Dnd5eSourceFilteredCatalogue` | Hides sources the player didn't enable |

**Where a D&D rule lives:**

| Rule | Place |
| --- | --- |
| Ability modifier, max HP | `Dnd5eFormulas.modifier`, `maxHitPoints` |
| Armor class | `Dnd5eSheetCalculator.armorClass` / `calculatedArmorClass` (`:823`, `:847`) |
| Hit points | `Dnd5eSheetCalculator.hitPoints` (`:980`) |
| Saving throws, skills, senses | `Dnd5eSheetCalculator.savingThrows` (`:1033`), `skills` (`:1165`), `senses` (`:1119`) |
| Attacks table | `Dnd5eSheetCalculator.attacks` (`:584`), `itemAttackRow`, `unarmedStrike` |
| Spell slots, spellcasting info | `Dnd5eSheetCalculator.spellSlots`, `spellcasting`; pools built in `Dnd5eBuildMaterializer.spellSlots` / `pactSlots` |
| Encumbrance | `Dnd5eSheetCalculator.encumbrance` (`:921`) |
| Damage, healing, temp HP, death saves | `Dnd5eSheetMutator.applyDamage` (`:64`), `applyHealing`, `applyDeathSaveRoll` |
| Casting a spell, active effects | `Dnd5eSheetMutator.castSpell` (`:729`), `endActiveEffect` |
| Short and long rests | `Dnd5eSheetMutator.applyShortRest` (`:665`), `applyLongRest` (`:687`) |
| Conditions and exhaustion | `Dnd5eSheetMutator.toggleCondition`, `setExhaustionLevel`; list in `Dnd5eConditions` |
| Roll expressions (skills, attacks, spell damage, upcasting) | `Dnd5eMechanicResolver.resolve` (`:34`) and its private helpers |

**Content:** `content/dnd-5e/<kind>/`, generated by the `catalogue` ingestion
tool from 5etools (2014 sources only). Never hand-write a rules fact.

**System docs:** `systems/dnd-5e/` (start with `systems/README.md`).

#### 2.4.2 Next system — template

Copy this heading for each new system (e.g. `2.4.2 Daggerheart
(daggerheart) — ruleset.daggerheart`) and fill the same three tables:
strategy implementations, stored data, and where each rule lives.

---

## Part 3 — Web code map (`apps/web`)

React 19 + TypeScript + Vite, React Router 7. Source in `apps/web/src/`. Tests
sit next to the code (`*.test.tsx`), run with `npm test` (Vitest).

### 3.1 Folders at a glance

| Folder | Owns | System-specific? |
| --- | --- | --- |
| `main.tsx`, `App.tsx` | Bootstrapping: theme, auth, router | No |
| `shell/` | Routes (`AppShell`), header, footer, credits | No |
| `auth/` | `AuthProvider` (asks `/api/me`), login redirect, CSRF cookie | No |
| `api/client.ts` | `apiFetch`: the only way to call the API | No |
| `home/` | Landing page and the character list | No |
| `characters/` | Character API, portraits, **the list of supported systems** | Registries |
| `builder/` | The generic character builder frame (steps, checklist, summary, autosave) | No |
| `levelup/` | Level-up page lookup per system | Registry |
| `sheet/` | The sheet screen, sidebar panels, sheet API client | Mostly no (see 3.4.2) |
| `dice/` | Dice tray, roll mode menu, roll API | No (uses D&D icons) |
| `catalogue/` | Catalogue API and source codes | No |
| `theme/`, `global.css` | App-wide theme | No |
| `systems/<system>/` | **Everything one game system draws** | **Yes** |
| `edition.ts` | `isDesktopEdition`, from Vite's build mode | No |

### 3.2 Platform pieces

| File | Role |
| --- | --- |
| `shell/AppShell.tsx` | All routes: `/`, `/characters/new[/:systemId]`, `/characters/:id`, `/characters/:id/build/:step?`, `/characters/:id/level-up` |
| `auth/AuthProvider.tsx` | Session state (`loading` / `signed-in` / `signed-out`) and `signIn` |
| `api/client.ts` | `apiFetch`: same origin, adds `X-XSRF-TOKEN` on writes, 401 → login |
| `characters/supportedSystems.ts` | `SUPPORTED_SYSTEMS` (creatable) and `UPCOMING_SYSTEMS` ("Coming soon" cards) |
| `characters/portraitPresets.ts` | Default portraits per system |
| `shell/systemMarks.ts` | The system's styled name in the app header, per system id |
| `builder/builderDefinitions.ts` | Builder per system id |
| `builder/builderDefinition.ts` | The `BuilderDefinition` contract a system implements |
| `builder/CharacterBuilder.tsx` | The builder frame: loads the draft, autosaves 600 ms after a change, finishes |
| `builder/draftApi.ts` | Draft endpoints |
| `levelup/levelUpPages.ts` | Level-up page per system id |
| `sheet/CharacterSheetScreen.tsx` | The sheet screen: loads the sheet, owns sidebar, rolls and mutations (~690 lines) |
| `sheet/api.ts` | `CharacterSheet` type (`:775`), `MutationAction` (`:920`), `postMutationAction` (`:969`), one function per endpoint |
| `sheet/Sidebar.tsx`, `sheet/*Panel.tsx` | The right-hand sidebar and its panels (HP, inventory, spells, conditions…) |
| `dice/api.ts` | `postRoll` (`:62`), `postManualRoll` (`:86`), history |
| `dice/DiceTray.tsx`, `CustomRollPicker.tsx`, `RollModeMenu.tsx` | Roll result tray, free dice, advantage menu |

### 3.3 Processes — where each one happens

#### Opening a character

`AppShell` → `SheetPage` loads `getCharacter(id)`. A `DRAFT` goes to the
builder; an `ACTIVE` one renders `CharacterSheetScreen`, which loads
`getCharacterSheet` and the roll history.

#### A sheet change (e.g. "take 7 damage")

1. A component calls the screen's `onMutate({ type: 'DAMAGE', amount: 7 })`.
2. `CharacterSheetScreen.handleMutate`:
   - pure toggles (inspiration, condition, equip, attune) flip locally at once;
   - `postMutationAction` (`sheet/api.ts:969`) maps the action to its endpoint;
   - the server's recalculated sheet replaces the local one; on failure the
     old sheet comes back with an error message.
3. Never compute a rule locally: wait for the server's sheet.

**To add a mutation:** a new member of `MutationAction`, its case in
`postMutationAction` (plus an exported `post…` function if needed), and the
component that dispatches it. API side: 2.3.

#### A roll

A clickable value calls `onRoll(kind, key)` → `CharacterSheetScreen.handleRoll`
→ `postRoll` → the result is pushed to the roll history and shown in
`DiceTray`. The client never decides a result.

#### Creating a character

`/characters/new` → `SystemPicker` → `/characters/new/:systemId` →
`CreateCharacterPage` creates a draft (the home and landing pages skip the
picker while only one system exists: `onlySystemId()`) → `CharacterBuilder` with
`builderFor(systemId)`. The system's `StepPage` renders each step; the frame
autosaves and calls `finishDraft` (`CharacterBuilder.tsx:121`) at the end.

#### Desktop edition differences

`npm run build:desktop` sets `isDesktopEdition` (`edition.ts`). The header
hides "Account" and "Log out"; `/api/me` always answers with the local player,
so the landing page never shows. Nothing else differs.

### 3.4 Game systems — web

Each system lives in `src/systems/<system>/` and is plugged in through the
registries in 3.2.

#### 3.4.1 D&D 5e (`dnd-5e`) — `src/systems/dnd5e/`

**Sheet layout (D&D Beyond's layout):**

| File | Area |
| --- | --- |
| `Dnd5eVitalsTopRow.tsx` | Top row: abilities, proficiency bonus and speed badges, inspiration, HP |
| `Dnd5eVitalsColumns.tsx` | The columns below: `Dnd5eLeftColumn` and `Dnd5eCombatColumn` (which holds `Dnd5eTabbedSection`) |
| `AbilityBox.tsx`, `HitPoints.tsx`, `ArmorClass.tsx`, `Initiative.tsx`, `HeroicInspiration.tsx`, `DeathSavesBox.tsx` | The boxes of the top row |
| `SavingThrowsPanel.tsx`, `SensesPanel.tsx`, `ProficienciesPanel.tsx`, `SkillsPanel.tsx` (+ `*Row.tsx`) | Left column |
| `DefensesConditionsPanel.tsx` | Defenses and conditions (also exports `CONDITIONS`) |
| `Dnd5eTabbedSection.tsx` | The tabs: `ActionsTab`, `SpellsTab`, `InventoryTab`, `FeaturesTab`, `BackgroundTab`, `ExtrasTab` |
| `Dnd5e*Pane(s).tsx` | Sidebar panes opened from the sheet (ability, skill, saving throw, senses, proficiencies, combat) |
| `frames.css`, `frames/*.svg`, `FrameLayer.tsx`, `FrameIcon.tsx` | D&D Beyond-style frames and icons (~3,700 lines of CSS) |
| `themes.css`, `sheetThemes.ts`, `ThemeSample.tsx` | Sheet colour themes |

**Tabs and rows:**

| File | Content |
| --- | --- |
| `ActionsTab.tsx`, `AttackRow.tsx`, `SpellAttackRow.tsx`, `FeatureActionRow.tsx`, `standardActions.ts` | Actions and attacks |
| `SpellsTab.tsx`, `SpellRow.tsx`, `SpellCast.tsx`, `spellDetail.tsx`, `spellCombat.ts`, `spellcastingHeader.ts` | Spells, casting, slot levels |
| `InventoryTab.tsx`, `ItemRow.tsx`, `AttunementSection.tsx`, `CoinsPanel.tsx` | Inventory and coins |
| `FeaturesTab.tsx`, `FeatureTraitRow.tsx` | Features and traits |
| `BackgroundTab.tsx` | Background and personality |
| `ExtrasTab.tsx`, `ExtraRow.tsx`, `ExtraStatBlockView.tsx` | Companions and summons |
| `RestBody.tsx` | Short and long rest panels: RECOVER line (`restRecovery`), hit dice boxes per pool, Take / Reset |
| `featureDetail.tsx` | Feature and feature-action detail panes (Actions and Features tabs) |
| `customizations.ts`, `CheckCustomize.tsx`, `EntityCustomize.tsx` | The "Customize" overrides |
| `rulesText.ts` | Rules text shown in panels (conditions, rests, death saves) |

**Builder and level up:**

| File | Role |
| --- | --- |
| `builder/dnd5eBuilderDefinition.ts` | The `BuilderDefinition` registered for `dnd-5e` |
| `builder/dnd5eBuild.ts` | Steps, build shape, which choice goes on which step |
| `builder/Dnd5eStepPage.tsx` | Renders one step |
| `builder/Dnd5eBasicsStep.tsx`, `Dnd5eAbilitiesStep.tsx`, `Dnd5eEquipmentStep.tsx`, `Dnd5eClassLevels.tsx`, `SpellPicker.tsx`, `Dnd5eReviewCards.tsx` | Individual steps and pickers |
| `levelup/Dnd5eLevelUpPage.tsx` | The level-up page |
| `portraits/` | Default portraits and their manifest |

#### 3.4.2 Known couplings to undo before a second system

These spots import from `systems/dnd5e` directly instead of going through a
registry. They work while D&D 5e is the only system, and are the first things
to generalize when a second one arrives (ground-rules: no speculative
generality until then):

- `sheet/CharacterSheetScreen.tsx` renders `Dnd5eVitalsTopRow`,
  `Dnd5eVitalsColumns`, `HitDice`, rest bodies, D&D conditions and themes
  directly. It needs a per-system "sheet layout" registry, like
  `builderDefinitions.ts`.
- `sheet/api.ts`'s `CharacterSheet` type mirrors `CharacterSheetResponse`,
  whose fields (spell slots, heroic inspiration, death saves…) follow D&D 5e.
- `dice/` uses D&D icons from `systems/dnd5e/frames/`.

#### 3.4.3 Next system — template

Copy this heading for each new system (e.g. `3.4.3 Daggerheart
(daggerheart) — src/systems/daggerheart/`) with the same tables: sheet layout,
tabs and rows, builder and level up.

---

## Part 4 — Adding a new game system

A system is added by writing new classes; editing an existing class to add a
branch for it is forbidden (`ground-rules.md`). Plan it first
(`roadmap.md`), and create `systems/<system-id>/` with a row in
`systems/README.md`.

**API (`ruleset.<system>`):**

1. `<System>GameSystem implements GameSystem` with the new system id.
2. `<System>CharacterCreationFlow`, `<System>SheetCalculator`,
   `<System>SheetMutator`, `<System>MechanicResolver`,
   `<System>CharacterSummarizer`, all `@Component`. The registries find them
   on their own.
3. Its stored sheet record and JSON mapper, with a `schemaVersion`.
4. Its catalogue content in `content/<system-id>/<kind>/`; a new
   `CatalogueEntryKind` only if a kind doesn't exist yet.
5. Tests derived from the published rules.
6. No migration: the character row stores the system id plus JSONB.

**Web (`src/systems/<system>/`):**

1. An entry in `SUPPORTED_SYSTEMS` (and remove it from `UPCOMING_SYSTEMS`).
2. A `BuilderDefinition`, registered in `builder/builderDefinitions.ts`.
3. A level-up page in `levelup/levelUpPages.ts`, if the system has levels.
4. Portrait presets in `characters/portraitPresets.ts`, if any.
5. Its header mark (the system's styled name and edition, in its own CSS),
   registered in `shell/systemMarks.ts`. Put the edition in a span with
   `app-system__secondary` so narrow screens can drop it.
6. The sheet: first undo the couplings in 3.4.2, then register its layout.

**Docs:** a new section 2.4.x and 3.4.x in this guide, `glossary.md` terms,
and a changelog entry.

**Desktop:** nothing extra. The next package includes the new system and its
`content/` folder.

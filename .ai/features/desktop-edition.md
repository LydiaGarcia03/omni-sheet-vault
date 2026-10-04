# Feature — Desktop edition (runs on a friend's Windows notebook, no Docker)

Status: **approved 2026-09-27, embedded PostgreSQL included. Slices 1–3 and
the guide (slice 5) done. What's left of slice 4: a run on a truly clean
Windows machine or user.** Replaces `oracle-deployment.md`, which the owner
dropped.

User guide (building, sharing, installing, using, backing up, updating,
troubleshooting): **`apps/desktop/README.md`**.

## Slice 3 — done 2026-09-27

- **New Gradle subproject `:apps:desktop`**
  (`dev.omnisheetvault.desktop`). It depends on `:apps:api`, the Boot BOM and
  `spring-boot`.
  - `DesktopLauncher` (main): shows the window, starts
    `OmniSheetVaultApplication` in the `desktop` profile, records the port,
    and opens the browser. "Quit", or closing the window, calls
    `SpringApplication.exit`, which closes PostgreSQL, then releases the lock.
  - **The folders and port go in as command-line arguments.** First they
    were Spring default properties, which `application-desktop.yml`
    outranked. The relative `content` and `web` then resolved against the
    working folder, and the live test imported the repository's `content/`
    instead of the bundled one.
  - `LauncherWindow` (Swing): "Starting… / running at … / stopping… / could
    not start (see log)", with "Open the vault" and "Quit".
  - `SingleInstance`: a file lock plus a port file in the data folder. A
    second launch opens the running vault instead of starting a second
    database on the same files.
  - `PortChooser`: 8095 when it's free, any free loopback port otherwise.
  - `AppFolders`: the web app and content next to the launcher jar, and the
    data in `%LOCALAPPDATA%\OmniSheetVault`.
  - Tests: `DesktopLauncherTest`, `SingleInstanceTest`, `PortChooserTest` and
    `AppFoldersTest`.
- **Packaging.** `packageDesktop` runs `buildDesktopWebApp`, then
  `stageDesktopApp`, then `desktopAppImage` (`jpackage --type app-image`),
  then zips the result.
  - Only the Windows PostgreSQL binaries are kept.
  - `-Xmx768m`, and the runtime is stripped with `--strip-debug`,
    `--no-man-pages` and `--no-header-files`.
  - Result: a 235 MB folder (120 MB of it the Java runtime), or a 147 MB zip.
- **An app image instead of an installer.** An `.exe` or `.msi` installer
  needs the WiX toolset, which isn't on the development machine. The portable
  folder needs no admin rights and no extra tool. An installer can be added
  later with WiX.
- **Desktop profile:** the log goes to
  `<data>/logs/omni-sheet-vault.log`.
- **Checked live**, simulating a friend's machine: the zip extracted to a
  separate folder, `LOCALAPPDATA` pointed at an empty folder, and the program
  started from another working folder.
  - `OmniSheetVault.exe` was ready in about 24 s on the first start. That
    includes importing 3,101 entries from the bundled `app\content`.
  - The browser showed the empty vault as the Windows user.
  - A second launch exited on its own and left the first one running.
  - Closing the window (`CloseMainWindow`) stopped the vault gracefully: no
    orphan `postgres.exe`, and no leftover `postmaster.pid` or `app.port`.
  - The Swing window itself couldn't be inspected visually from the
    assistant's tools. Its text is in `LauncherWindow`.
- **No custom icon yet:** the `.exe` has the default Java icon. `jpackage
  --icon` needs an `.ico`, and the logo is only SVG.
- **CI:** `.github/workflows/ci.yml` also runs `:apps:desktop:test`. The
  package itself is Windows-only and isn't built in CI.

## Slice 2 — done 2026-09-27

- **Web:**
  - `src/edition.ts` sets `isDesktopEdition` from Vite's build mode.
  - `npm run build:desktop` (`vite build --mode desktop --outDir
    dist-desktop`) builds the desktop web app; `apps/web/dist-desktop/` is
    gitignored.
  - In that build, the user menu keeps only "My characters". "Account" and
    "Log out" are hidden (`AppHeader.test.tsx`).
  - The landing page never shows, because `/api/me` always answers with the
    local player.
- **API:** `desktop.DesktopWebAppConfig` serves the build from
  `app.desktop.web-directory` (default `web`), on the same origin as the API.
  - `/assets/**` is cached for a year (Vite hashes those names);
    `index.html` gets `no-cache`.
  - `/` is forwarded to `index.html`: the resource handler never sees the
    empty path.
  - Any other path the router owns gets `index.html`, so a reload on
    `/characters/…/build/species` works. An unknown `/api/…` path stays a
    404.
  - Tests: 3 more cases in `DesktopEditionTest`, 11 in total.
- **Checked live with a single
  `java -jar api.jar --spring.profiles.active=desktop`:**
  - it starts in about 7 s, straight to the home as the Windows user
    ("Lydia"), with no login;
  - the user menu shows only "My characters";
  - "Create character" creates a draft and opens the builder;
  - an uploaded portrait is stored on disk and shown;
  - a reload on a builder step opens that step;
  - choosing Elf autosaves, which proves the CSRF token holds across
    requests;
  - the catalogue shows the full text (Fireball, `redacted: false`).
- **Jar size:** 188 MB, because embedded-postgres brings binaries for four
  platforms. Slice 3's Windows installer should keep only
  `windows-amd64`.

## Slice 1 — done 2026-09-27

All of it is behind the `desktop` Spring profile (`application-desktop.yml`).

- **`desktop.DesktopProperties`** (`app.desktop.*`): `data-directory`
  (default `%LOCALAPPDATA%\OmniSheetVault`), `content-directory` (default
  `content`) and `runtime-directory` (default `<data>/runtime`).
  - They are bound as text and turned into paths with `Path.of`: Spring's own
    conversion reads a relative path as a resource, and `../../content`
    failed.
- **`desktop.EmbeddedDatabaseConfig`:** embedded-postgres 2.2.2 with
  PostgreSQL 17.11.
  - The data lives in `<data>/db` and the binaries are unpacked in
    `<data>/runtime`.
  - It listens on a random free port and serves a Hikari pool of 5.
  - **A server left running by a killed app** (found live: stopping the JVM
    by force leaves `postgres.exe` running) is stopped with the bundled
    `pg_ctl` before the next start.
- **`shared.DesktopSecurityConfig`** (the server's `SecurityConfig` is now
  `@Profile("!desktop")`):
  - The local player comes from `LocalPlayerSecurityContextRepository`: a
    `JwtAuthenticationToken` with subject `local-player`, the Windows user
    name as display name, and role `player`.
  - **Why a repository and not a filter:** a filter made every request look
    like a new login, and Spring then rotated the CSRF token each time. The
    cookie was deleted from the second request on, so the web app's writes
    would fail. Seen live and covered by a test.
  - `LocalHostOnlyFilter` answers only when the Host is `localhost`,
    `127.0.0.1` or `[::1]`, against DNS rebinding. `server.address` is
    `127.0.0.1`.
  - CSRF is the same cookie and header as the server edition. Sessions are
    stateless.
- **`storage.FileSystemPortraitStorage`** and
  **`storage.LocalPortraitFileController`** (`S3PortraitStorage` is now
  `@Profile("!desktop")`):
  - Files are stored at `<data>/portraits/<key>` and served at
    `/api/portrait-files/uploads/<character>/<file>.png`.
  - A key can never leave the portrait folder.
- **`catalogue.DesktopCatalogueBootstrap`:** imports every kind directory of
  every system under the content folder. A SHA-256 of the content, kept in
  `<data>/catalogue.fingerprint`, skips the import when nothing changed.
- **Redaction** is off in this profile (`app.catalogue.redaction-enabled:
  false`), as the owner decided.
- **Measured on the development machine, with the full content (3,101
  entries):**
  - first start: about 18 s to start and migrate, plus about 10 s for the
    import;
  - later starts: about 5 s, with no import.
- **Tests:**
  - `DesktopEditionTest` (8 cases): embedded DB in the data folder, the local
    player, another host refused, CSRF needed, the CSRF token kept across
    requests together with a real create and delete, catalogue imported,
    portraits on disk served back, other file names refused;
  - `EmbeddedDatabaseRestartTest`: data survives a restart, and an orphan
    server started with `pg_ctl` is stopped;
  - `FileSystemPortraitStorageTest`.
- **Test JVM:** `java.io.tmpdir` now points to `build/tmp/test`. On the
  development machine the test JVM's temp folder was `C:\WINDOWS\TEMP`, where
  JUnit's `@TempDir` gets "access denied".
- **Still open, for slice 3:** a graceful "Quit". Closing the app normally runs
  the shutdown hooks and stops PostgreSQL; the leftover cleanup above covers
  the rest.

## Goal

A friend installs one Windows program, opens it, and gets the vault in her
browser:
- no Docker, no Java install and no account setup;
- her characters and portraits stay on her own machine;
- the same code base as the server version: no fork that drifts apart.

## What changes, and what doesn't

| Piece | Server version (today) | Desktop edition |
| --- | --- | --- |
| Web app | Vite dev server / proxy | Built once, served by the API itself (same origin) |
| Database | PostgreSQL in Docker | **Real PostgreSQL binaries started by the app** (embedded-postgres). The same Flyway migrations, JSONB and queries; H2 stays forbidden |
| Login | Keycloak + session cookie | **No login.** One local player per machine; the server only listens on `127.0.0.1`, so nobody else on the network can reach it |
| Portraits | MinIO (S3) | **Files in the user's data folder** (`FileSystemPortraitStorage`, a new `PortraitStorage`) |
| Catalogue | `--import-catalogue` by hand | Imported automatically on first start, and again after an update |
| Java | Installed JDK 25 | A trimmed Java runtime bundled inside the program (`jpackage`) |

Data folder: `%LOCALAPPDATA%\OmniSheetVault\`, holding `db/`, `portraits/`
and `backups/`. It survives updates: installing a new version keeps the
characters, and Flyway migrates them.

## Design

Everything the desktop edition needs is **new classes behind the existing
seams**, switched on by a `desktop` Spring profile. No `if (desktop)` anywhere
(Open/Closed, `ground-rules.md`).

- **`LocalIdentityFilter`** (in `identity`, `desktop` profile): every request
  is authenticated as the machine's local player. It is a
  `JwtAuthenticationToken` with a fixed subject, so controllers,
  `PlayerService` and ownership checks don't change.
  - Keycloak isn't used, so no password exists anywhere. The rule
    "Keycloak owns credentials" still holds.
  - CSRF stays on.
- **`DesktopSecurityConfig`** (`desktop` profile): used instead of the server's
  `SecurityConfig`, which gets `@Profile("!desktop")`.
- **`FileSystemPortraitStorage`**: stores files under
  `portraits/<character>`. It returns a short-lived signed local URL, served by
  a small controller.
- **`EmbeddedPostgresDataSource`** (`desktop` profile): starts PostgreSQL from
  the bundled binaries in the data folder on a free local port, and stops it
  on exit.
- **`DesktopLauncher`**: picks a free port on `127.0.0.1`, starts the app,
  opens the default browser, and shows a small window with "Open the vault"
  and "Quit".
- **Web app:**
  - a build flag hides the login, sign-up and logout buttons, and the
    "Account" link;
  - `AuthProvider` still asks `/api/me`, which now always answers with the
    local player.
- **Packaging** (new Gradle subproject `:apps:desktop`):
  1. builds the web app (`npm run build`);
  2. builds the API jar with the web app and `content/` inside;
  3. runs `jpackage` on Windows to produce an installer (`.msi` or `.exe`) with
     its own Java runtime, a Start menu entry and a desktop shortcut.

## Owner-facing notes

- **Size:** the installer is about 150–250 MB: the Java runtime, the
  PostgreSQL binaries, the app and the content.
- **Memory:** about 1 GB while open (API plus PostgreSQL).
- **Windows SmartScreen:** it warns the first time ("Windows protected your
  PC"), because the program is not signed; signing costs money every year. The
  friend clicks "More info", then "Run anyway". The instructions will say so.
- **Updates:** the owner builds a new installer and sends it. Installing over
  the old one keeps the data.
- **Backups:** the data folder can be copied as it is. A later slice can add
  "Export character" and "Import character" as a JSON file, which also lets
  friends swap characters.
- **Only Windows for now:** `jpackage` builds for the system it runs on, so a
  macOS build would need a Mac.
- **Default portraits:** they are loaded from D&D Beyond's servers, so they
  need internet. Everything else works offline. To be confirmed while
  building.

## Slices

1. **`desktop` profile, backend:**
   - embedded PostgreSQL;
   - `LocalIdentityFilter` and `DesktopSecurityConfig`;
   - `FileSystemPortraitStorage`;
   - the automatic catalogue import;
   - tests for each.
2. **Web in desktop mode:** the build flag, served by the API. Checked with
   `java -jar` on the development machine.
3. **Launcher and packaging:** `:apps:desktop`, `jpackage`, the installer, and
   the small "Open / Quit" window.
4. **Test on a clean Windows machine**, or a fresh Windows user account:
   install, create a character, upload a portrait, roll, close, reopen, and
   check the data is still there.
5. **Docs:**
   - `architecture.md`, with the new subproject and the profiles;
   - `tech-stack.md`, with the new dependencies below;
   - a short guide for friends.

## New dependencies (owner approval needed)

- `io.zonky.test:embedded-postgres` (Apache 2.0): the PostgreSQL binaries per
  operating system, plus the code that starts them. Despite the "test" in its
  name it runs a real PostgreSQL server.
- `jpackage`: part of the JDK, not a new dependency.

## Out of scope

- Syncing characters between friends, or with a server.
- The macOS and Linux installers.
- Code signing.

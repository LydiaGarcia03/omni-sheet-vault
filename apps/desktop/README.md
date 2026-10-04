# Omni Sheet Vault — Desktop edition guide

The desktop edition is the whole vault in one Windows program. It keeps every
character on the computer it runs on and needs no account, no Docker and no Java
install. This guide has two parts:

- [Part 1 — For the owner: building and sharing the package](#part-1--for-the-owner-building-and-sharing-the-package)
- [Part 2 — For players: installing and using the vault](#part-2--for-players-installing-and-using-the-vault)

Technical design: `.ai/features/desktop-edition.md`.

---

## Part 1 — For the owner: building and sharing the package

### What you need on the development machine

- **JDK 25**, found by Gradle's toolchain; `JAVA_HOME` can stay on Java 21. It
  must be a full JDK, because the build uses its `jpackage` tool.
- **Node.js and npm**, with `npm install` run once in `apps/web`.
- **Windows.** `jpackage` builds for the system it runs on, so this package
  only runs on Windows.

### Build the package

From the repository root:

```powershell
.\gradlew.bat :apps:desktop:packageDesktop
```

It takes about a minute. It does the following:

1. Builds the web app in desktop mode (`npm run build:desktop`, into
   `apps/web/dist-desktop`).
2. Gathers the API, its libraries (with the Windows PostgreSQL binaries
   only), the web app and the `content/` catalogue.
3. Runs `jpackage`, which adds a trimmed Java runtime and creates
   `OmniSheetVault.exe`.
4. Zips the result.

The file to share is:

```
apps\desktop\build\distributions\OmniSheetVault-0.2.0-windows.zip   (about 150 MB)
```

### Try it yourself before sending it

Extract the zip somewhere outside the repository and double-click
`OmniSheetVault.exe`. It uses your own
`%LOCALAPPDATA%\OmniSheetVault` folder, separate from the Docker development
setup.

### Share it

The zip is too big for most email and chat apps. Upload it to Google Drive,
OneDrive or WeTransfer and send the link, together with Part 2 of this guide.

### Release a new version

The full checklist, including how to test the upgrade over your own data, is
in `.ai/developer-guide.md`, section 1.4.

1. Raise `appVersion` in `apps/desktop/build.gradle` (for example `0.1.0` to
   `0.2.0`), so everyone can tell the versions apart.
2. Build the package again and share the new zip.
3. Players follow "Updating to a new version" below. **Their characters are
   kept.** The data folder is separate from the program, and database changes
   are migrated automatically on the first start.

---

## Part 2 — For players: installing and using the vault

### What your computer needs

- Windows 10 or 11, 64-bit.
- About 250 MB of disk for the program, plus a little more for your
  characters.
- About 1 GB of free memory while the vault is open.
- **No internet**, except for the default character portraits and the fonts.
  Everything else works offline.

### Installing (once)

1. Download `OmniSheetVault-<version>-windows.zip`.
2. **Extract it; don't run it from inside the zip.** Right-click the zip,
   choose **Extract All…**, and pick a folder you'll keep, for example
   `Documents\OmniSheetVault`.
3. Open the extracted `OmniSheetVault` folder and double-click
   **`OmniSheetVault.exe`**.
4. **"Windows protected your PC"?** That's expected: the program isn't
   signed, because signing costs money every year, and it isn't a virus.
   Click **More info**, then **Run anyway**. Windows only asks once.
5. Optional: to get a desktop shortcut, right-click `OmniSheetVault.exe`,
   choose **Send to**, then **Desktop (create shortcut)**. On Windows 11 you
   may need **Show more options** first.

### Opening the vault

1. Double-click `OmniSheetVault.exe`, or your shortcut.
2. A small **Omni Sheet Vault** window appears and says **"Starting the
   vault…"**.
   - The **first start takes about half a minute to a minute**, because it
     prepares the database and loads the books.
   - Later starts take a few seconds.
3. When it's ready, the vault opens in your default browser at
   `http://localhost:8095/`. The address can be different if that port was
   busy; the window shows the right one.
4. You're in. There is no login: the vault on your computer belongs to your
   Windows user, whose name appears at the top right.

The small window has two buttons:

| Button | What it does |
| --- | --- |
| **Open the vault** | Opens the vault in the browser again, e.g. after closing the tab |
| **Quit** | Saves everything, closes the database and stops the vault |

**Closing the small window also stops the vault.** Closing only the browser
tab doesn't: the vault keeps running until you close the window.

Opening `OmniSheetVault.exe` while the vault is already running doesn't start
a second copy. It opens the running one in the browser.

### Where your characters are kept

Everything you create lives in your own Windows profile, not in the program
folder:

```
%LOCALAPPDATA%\OmniSheetVault
```

To open it, press **Win + R**, type `%LOCALAPPDATA%\OmniSheetVault` and press
Enter. It contains:

| Folder or file | What it is |
| --- | --- |
| `db\` | The database with your characters, dice rolls and the catalogue |
| `portraits\` | The portraits you uploaded |
| `logs\` | A diary of what the vault did, useful when something goes wrong |
| `runtime\` | The database program, unpacked on the first start |

### Backing up your characters

1. **Quit the vault** (the **Quit** button, or close the small window).
2. Copy the whole `%LOCALAPPDATA%\OmniSheetVault` folder somewhere safe: a
   USB stick, a cloud drive or another disk.

To restore a backup, quit the vault, replace that folder with your copy, and
open the vault again.

### Updating to a new version

1. Quit the vault.
2. Delete the old **program** folder (the one with `OmniSheetVault.exe`).
   Don't touch `%LOCALAPPDATA%\OmniSheetVault`: that's where your characters
   are.
3. Extract the new zip in the same place and open `OmniSheetVault.exe`.
4. The first start after an update can take a bit longer while it updates
   the database and the books. Your characters are still there.

### Uninstalling

- **To remove the program:** delete its folder, and the shortcut if you made
  one.
- **To also erase every character, for good:** delete
  `%LOCALAPPDATA%\OmniSheetVault`.

### Troubleshooting

| What you see | What to do |
| --- | --- |
| "Windows protected your PC" | **More info**, then **Run anyway** (see Installing) |
| The window says **"The vault could not start"** | Quit, then open it again. If it happens again, send the owner the file `%LOCALAPPDATA%\OmniSheetVault\logs\omni-sheet-vault.log` |
| The browser didn't open | Click **Open the vault**, or type the address the window shows into your browser |
| The page says the site can't be reached | The vault isn't running: open `OmniSheetVault.exe` |
| A Windows Firewall prompt about Java or PostgreSQL | Click **Cancel**. The vault only talks to your own computer and needs no network access |
| The computer crashed or lost power with the vault open | Just open it again. It notices the database was left open and recovers on its own |
| Your antivirus complains | The program isn't signed. If you trust the person who sent it, allow it; otherwise ask them |

### Good to know

- **Your characters stay on your computer.** Nothing is uploaded, and friends
  can't see each other's characters.
- The vault only answers your own computer, not other devices on your Wi-Fi.
- It only runs on Windows for now.

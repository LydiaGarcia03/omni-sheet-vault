# Open items waiting on the owner

Decisions and follow-ups the owner deferred for later. Each item says what's needed
and what the assistant recommends. Remove an item once it's answered, and record the
answer in the relevant `.ai/` document and in `changelog.md`.

## Decisions

- **"At Will" in the Cast control (spellcasting P2, 2026-10-03).** D&D Beyond
  shows plain text. Here it is a button that casts the spell, so a lasting
  effect (Armor of Shadows' Mage Armor → AC) can start without a slot.
  Recommendation: keep the button and list it under "Deviations from D&D
  Beyond" in `systems/dnd-5e/sheet-ui.md`.

- **Vampire: The Masquerade 5e (phase 12), 2026-10-03.** Questions 1–2
  (content source, books) are answered. Still open in `systems/vtm-v5/features/implementation-plan.md`
  §6: timing against the pending D&D work, approving the V1 seam changes,
  and where XP is spent. Also confirm Ruins of Carthage's dot layout on
  Anarch p. 196 (`systems/vtm-v5/character-rules.md` §12).

## Known gaps, not yet scheduled

- **`POST /api/characters` creates a sheet that can't render.** Found
  2026-09-27 while testing the desktop edition, but the server edition has it
  too.
  - A character created directly through that endpoint gets a D&D 5e sheet
    with `items` null. Opening it throws a `NullPointerException` in
    `Dnd5eSheet.items()`, and the web app shows "Could not load the character
    sheet".
  - The web app never calls that endpoint (it creates drafts through
    `POST /api/characters/drafts`), so players can't reach this.
  - Recommendation: default the empty lists when a sheet is created or read,
    or retire the endpoint if nothing needs it.
- **`{#itemEntry …}` templates.** 15 generic items (Absorbing Tattoos, Grenades,
  Grenade Launcher) start with a reference to a shared text template in
  `items.json`, and `ItemConverter` doesn't resolve those templates yet.
- **Tables inside feature and spell text** (e.g. Wild Magic Surge) are not
  rendered into descriptions.
- **Grants written only in prose.** The Rogue's Thieves' Cant language, and a
  species' own tongue that 5etools marks as `other`, aren't in the materialized
  `languages`. Both need adr-0007 overlay data.
- **Feature uses and recharge** (Second Wind, Action Surge, Sorcery Points) are
  C3b. Since C3a, a feature's HP, AC and extra-attack effects apply once its
  adr-0007 overlay file exists (Tough, Defense, Extra Attack and Draconic
  Resilience have one; Dwarven Toughness and Unarmored Defense get one when a
  character needs them).
- **Pact slots and slotless casting.** Resolved: P1 on 2026-09-28, P2 on
  2026-10-03 (`systems/dnd-5e/features/spellcasting-pools.md`).
- **A draft that was never finished** ("New character" for the local `test`
  player) has an empty sheet. The dev runners skip drafts since 2026-10-03
  (`--print-sheets` used to crash on it).
- **Eldritch Knight / Arcane Trickster school limits.** Since 2026-09-24,
  5etools' subclass spell lists feed the options. Only the "any school" levels
  are still unfiltered, and the rule for them is in prose, so it needs adr-0007
  overlay data.
- **Spells from optional features with no ability** (e.g. invocations granting a
  spell) get no caster entry, so their attack bonus and DC don't show.
- **Per-size hit dice.** A multiclass sheet records every class's die in
  `classLevels`, but the hit-dice pool UI shows only the starting class's die
  (C2).

## Deferred to phase 11 (D2)

State on 2026-09-25: D2a, D2b, D2c, D2g and the D2e spell picker are built. See
`systems/dnd-5e/features/character-creation.md`, "Built 2026-09-25".

Still open:
- **Classes step (D2e):** built on 2026-09-26: the per-level accordion with
  class tabs, the "starting class" marker, and the class progression panel.
  - Still open: the owner decides whether the progression panel covers the
    mock's "Coming next" table, which also listed "You'll choose" per level.
  - The mock's hit points per level in each level summary aren't shown yet.
- **Species, Background and Equipment designs (D2d, D2f, D2h):** built on
  2026-09-26.
  - Still open: "Add items" from the catalogue and "Other possessions" at
    creation (the owner chose later).
  - Wear/Wield was checked live on 2026-09-26.
- **Flaky `S3PortraitStorageTest` (2026-09-26):**
  - It failed twice inside the full `:apps:api:check` with MinIO answering 503
    on bucket creation, and passed alone. The machine was low on memory and
    disk (C: 32 GB free).
  - The next full run passed.
  - If it keeps recurring, look at the MinIO test container's setup.
- **Checklist:** it shows ✓ on a step whose only issue is a rule problem (e.g. a
  point-buy overspend).
- **Default portraits by species.** D2j shows all 51 presets together.
  `portraits.json` keeps Beyond's `raceId` only; mapping it to our species would
  let the picker show the chosen species' portraits first, as Beyond does.
  - **Proprietary art:** D&D Beyond/Wizards of the Coast, like the
    `dnd_item_kind_*.jpg` icons. The owner keeps it local for now and hasn't
    pushed it anywhere; revisit before any public deployment.
- **Uploaded portraits of deleted characters.** Deleting a character only sets
  `deleted_at`, so its uploaded portrait stays in MinIO. A cleanup is needed if
  hard deletion or a retention policy arrives.
- **Stored but not yet applied on the sheet:** advancement (XP), ignore coin
  weight. Variant encumbrance isn't supported.
- **Third-party partnered books:** since 2026-09-26 the Partnered row groups the
  licensed books we import by brand (Critical Role, Rick and Morty).
  - Books from partner publishers are still missing (Tal'Dorei Reborn,
    Humblewood, Grim Hollow, Drakkenheim, Kobold Press…). 5etools' main data
    doesn't carry them; its homebrew repository might.
  - Using that repository would be a new content source, outside adr-0006. The
    owner hasn't chosen it.
  - See `systems/dnd-5e/features/builder-refinements.md`.
- **Name suggestions:** no name data imported.
- **Checking the planner against D&D Beyond's builder:** continues as each step
  is built.

## Security — required before any public deployment

- **Move tokens out of the browser: a BFF with an `HttpOnly; Secure` session
  cookie.** The owner decided this on 2026-09-25 and approved ADR-0008 on
  2026-09-26.
  - **Done on 2026-09-26 (stages 1–4):** the web app holds only the session
    cookie, and the API runs the login and keeps the tokens. Bearer headers
    and the public web client are gone.
  - **Still worth adding before going public:**
    - a strict CSP;
    - a reverse proxy that serves the web app and the API on one origin, over
      HTTPS: the session cookie is `Secure` outside the dev profile.

## Accounts (Keycloak)

- **Forgot password.** Wanted, but not now: Keycloak's reset-password flow needs
  an SMTP server, which the project doesn't have yet. `resetPasswordAllowed`
  stays `false` and the login theme doesn't show the link.
- **Account registration: on since 2026-09-26** (`features/private-sharing.md`,
  slice 2). Keycloak's own registration page, with a password policy and
  brute-force protection on logins. Still open:
  - **Email verification** needs SMTP, so `verifyEmail` stays `false`. Anyone can
    register with any address.
  - **Bots creating accounts in bulk.** Keycloak has no rate limit on
    registration. Options, in order:
    1. keep the URL private (the owner's plan: shared only with a friend);
    2. when it goes online, a rate-limiting rule on
       `/realms/omni-sheet-vault/login-actions/registration` at the proxy or in
       Cloudflare;
    3. Keycloak's built-in reCAPTCHA on the registration flow. The owner must
       create Google reCAPTCHA keys first; they go in the "Recaptcha" step of the
       "registration" flow, never in the committed realm export.
  - **Owner to decide:** whether to turn registration off again once the
    friend has an account (`registrationAllowed: false`, or an invite-only
    setup).

## Git

The assistant doesn't run Git. As of 2026-09-25 the working tree holds all
uncommitted work since the last commit. `changelog.md`'s 2026-09-23 to 2026-09-25
entries list it, together with a suggested commit message per change.

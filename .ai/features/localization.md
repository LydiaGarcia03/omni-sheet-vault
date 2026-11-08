# Localization

The whole app (web, API messages, catalogue content and the Keycloak login) in
**English (default)** and **Brazilian Portuguese**. Only these two languages for
now; adding a third is adding files, never editing code.

Planned with the owner on 2026-10-14. Nothing is built yet.

## Decisions

- **Default is English.** The player's choice is saved in the `players` table and
  follows them across devices. Logged out, it lives in the browser. A saved
  server value wins; while it is unset, the browser value is used.
- **Theme moves to the server too.** The dark theme was browser-only
  (`localStorage`); it moves next to the language in `players`.
- **One file per content kind per language, English included.** For example,
  `spells.en.json` and `spells.pt-BR.json`. English files are the existing
  sources *renamed*, never duplicated, so there is still a single English
  source. Locale codes follow BCP 47 (`en`, `pt-BR`).
- **Folders are named `locales`**, never `i18n`. Exception: Keycloak requires its
  theme's `messages/` folder.
- **Missing translation falls back to English**, field by field. An empty value
  counts as missing.
- **Rules always run on the English canonical data.** Identifiers (slugs, VtM's
  English keys such as `"Strength"`) never change; translation only changes what
  is displayed.
- **Translation split:**
  - The owner translates short terms by hand from the official Portuguese books:
    sheet section names, labels, and names of spells, items, clans, disciplines…
  - The assistant translates the long texts by AI, using the owner's terms for
    consistency.
- **AI notice:** in the builder and on the sheet, when the language isn't English,
  a fine-print notice says the rules text was translated by AI from the English
  original and may differ from the official Portuguese books.
- **Units:** in pt-BR, feet become metres (5 ft = 1.5 m), formatted by the API.
  The rules never convert. Weight stays in kg.
- **No i18n library.** A small in-house provider: `useT()`, `{name}`
  interpolation, `Intl.PluralRules`. Non-English dictionaries load on demand, so
  English users download nothing extra.
- **Language button:**
  - In the header, next to the user menu (or next to "Log in").
  - Material Symbols `language` icon from Google Fonts (subset with
    `icon_names=language`, `display=block`) plus the code: `EN` / `PT-BR`.
  - Clicking opens a dropdown with the current language checked.
  - On the Keycloak login, the selector sits below the form card.
- **Loading overlay:**
  - Covers the language switch and other long actions: the current page stays
    visible behind a blur and a light veil.
  - The spinner is a gold ring over a dark track, recreated as inline SVG from
    D&D Beyond's `AnimatedLoadingRingSvg` (`stroke-dasharray` animation).
  - Under `prefers-reduced-motion` the ring pulses instead of spinning.
  - The existing plain "Loading…" texts switch to the same spinner, without the
    overlay.
- **Tests come last.** New unit tests are written in the final slice. Existing
  tests must stay green along the way.

## File layout

| What | English | Portuguese |
| --- | --- | --- |
| Platform UI | `apps/web/src/locales/<area>.en.json` | `apps/web/src/locales/<area>.pt-BR.json` |
| A system's UI | `apps/web/src/systems/<system>/locales/<system>.en.json` (VtM: `vtm5.en.json`, game-term labels under `terms`) | `…/<system>.pt-BR.json` (`terms` come from the owner's worksheet) |
| API messages | `apps/api/src/main/resources/locales/messages_en.properties` | `messages_pt_BR.properties` |
| A system's API messages | `apps/api/src/main/resources/locales/<system>-messages_en.properties` (picked up by name, no config change; `vtm5-`, `dnd5e-`) | `<system>-messages_pt_BR.properties` |
| VtM content texts | `ruleset/vtm5/vtm5-content.en.json` (renamed in L4) | `ruleset/vtm5/locales/vtm5-content.pt-BR.json` (overlay, assistant) |
| VtM terms | the English names themselves (they are identifiers) | `ruleset/vtm5/locales/vtm5-terms.pt-BR.json` (flat English → Portuguese map, from the owner's worksheet) |
| D&D terms the API writes | in code, as each lookup's fallback | `ruleset/dnd5e/locales/dnd5e-terms.pt-BR.json` (`terms.<group>.<key>`, the same paths as the web's `dnd5e.pt-BR.json`; from the owner's worksheet 01) |
| D&D / Daggerheart content | `content/<system>/<kind>/<kind>.en.json` | `content/<system>/<kind>/<kind>.pt-BR.json` |
| Login | `infra/keycloak-themes/omni-sheet-vault/login/messages/messages_en.properties` | `messages_pt_BR.properties` |

D&D content is one file per kind since D2 (2026-10-15). Daggerheart is still one
file per entry until L5; the readers take both (`CatalogueEntryFiles`).

## Owner's translation worksheet (temporary)

- **Files:** a folder per system, `translation-worksheets/<system-id>/`, at the
  repository root. It holds numbered batches (`01-content-terms.pt-BR.json`,
  `02-sheet-labels.pt-BR.json`…), so a new batch never touches a file the owner
  is editing. The app never reads them.
- **Shape:** the assistant generates each batch with every term to fill,
  grouped by where it will end up. `_howTo` holds the instructions.
  - `flat: true` means the key is used as is (a term map keyed by the English
    term).
  - Otherwise the key is a dotted path into the target JSON.
  - `target` is one file or a list of files; a list writes each translation
    into all of them (the D&D terms both the web and the API use).

```json
{
  "_howTo": ["Fill only \"pt\" …"],
  "clans": {
    "target": "apps/api/src/main/resources/ruleset/vtm5/locales/vtm5-terms.pt-BR.json",
    "flat": true,
    "entries": {
      "Brujah": { "en": "Brujah", "pt": "" },
      "Toreador": { "en": "Toreador", "pt": "" }
    }
  }
}
```

- **Past worksheets** (all merged and removed on 2026-11-07; new batches
  follow the same shape):
  - `translation-worksheets/vtm-v5/01-content-terms.pt-BR.json`, with 568
    VtM terms from `vtm5-content.en.json`: attributes, dot scale, skills,
    specialties, disciplines and powers, clans, banes and compulsions,
    predator types, ages, advantages, trackers, resonances, identity fields,
    distributions, rituals, thin-blood, loresheets, coterie types and domain.
  - Phrases written by us, such as the clans' nicknames, are left to the
    assistant.
  - `translation-worksheets/vtm-v5/02-sheet-labels.pt-BR.json`, with the 55
    game-term labels of the VtM builder, sheet and dice (section titles,
    trackers, tabs). They merge into `vtm5.pt-BR.json` under `terms`.
  - `translation-worksheets/dnd-5e/01-sheet-labels.pt-BR.json`, with the 339
    game terms of the D&D sheet, builder and level up, one group per
    `terms` section of `dnd5e.en.json` (abilities, skills, conditions, damage
    types, labels, builder steps…), plus `shared-sheet-terms` for the shared
    sheet's `terms` in `sheet.pt-BR.json`. Since 2026-10-19, the groups the
    API also reads (`abilityAbbreviations`, `skills`, `damageTypes`,
    `movements`, `senses`, `labels`) target both `dnd5e.pt-BR.json` and the
    API's `dnd5e-terms.pt-BR.json`, and six API-only groups were added
    (`sizes`, `creatureTypes`, `armorCategories`, `weaponCategories`,
    `toolCategories`, `vehicles`; 23 terms).
- **Owner:** fills only `pt`. `en` is a reading and search aid.
- **Merge:** `node tools/translation-worksheet/merge.mjs <system-id>` writes the
  filled `pt` values into their `target` locale files and removes them from the
  worksheet. Empty ones stay for later. It runs as often as the owner likes;
  terms show up in the app only after a merge.
- **Done:** once a worksheet is empty, it is deleted.

## Slices

| Slice | Delivers |
| --- | --- |
| L1 | Web i18n provider, language button and dropdown, language and theme saved in `players` (migration), `LoadingOverlay` and spinner, platform pages translated (header, home, footer, credits) |
| L2 | Keycloak login in pt-BR; selector below the form; the app passes `ui_locales` so the login opens in the chosen language |
| L3 | API locale (`Accept-Language`, only `en`/`pt-BR`), `MessageSource`, errors and card summaries; content overlay loading with fallback; worksheet merge script; AI notice component |
| L4 | **VtM:** rename to `.en.json`, UI dictionaries, long texts by AI, worksheet for the owner |
| L5 | Daggerheart, same pattern; content moves to one file per kind |
| L6+ | D&D in layers: UI and ft→m; then classes, species, backgrounds, feats; spells; conditions and mechanics; items; creatures |
| Last | Unit tests for everything above (missing-key check, fallback, merge script, units) |

Every slice ends with a 1280x720 check in Portuguese, which runs 20–30% longer.

## Code

| Piece | Where |
| --- | --- |
| Preferences API | `identity/PreferencesController` (`PUT /api/me/preferences`, `{ locale?, theme? }`, a null field keeps its value); `GET /api/me` returns `preferences: { locale, theme }`; migration `V11__add_player_preferences.sql` |
| Provider and bundles | `apps/web/src/language/language.tsx` |
| Language button | `language/LanguageMenu.tsx` + `language.css`, rendered by `AppHeader` |
| Saving | `auth/preferences.ts` (`savePreferences`); the theme provider uses it too |
| Platform bundles | `locales/platformMessages.ts`: `useShellText` (header, footer, loading), `useHomeText` (landing, home, cards, banners, picker, create page), `useCreditsText` |
| Loading | `loading/Spinner.tsx`, `loading/LoadingOverlay.tsx`, `loading.css` |

### Provider and bundles (`language/language.tsx`)

- Provider order in `main.tsx`: `AuthProvider` → `AppThemeProvider` →
  `LanguageProvider` → `App`.
- **Choosing the language:**
  - On start, the provider uses the browser's choice (`osv.locale`).
  - Once `/api/me` answers, a saved `preferences.locale` replaces it.
  - When the player picks a language, it is saved in the browser and, if
    signed in, on the server.
  - `<html lang>` follows the language.
- **Loading:**
  - A non-English start waits behind the overlay until its bundles load, so
    English never flashes first.
  - A switch shows the overlay for at least 450 ms.
- **Bundles:** `defineMessages(englishJson, { 'pt-BR': () => import('…pt-BR.json') })`
  registers a bundle and returns its hook.
  - The English JSON is bundled with the app; other languages are fetched on
    demand.
  - Keys are typed from the English JSON (`t('home.title')`).
  - `{name}` placeholders take parameters.
  - An object `{ "one", "other" }` is a plural, picked with
    `Intl.PluralRules` from `params.count`.
  - `t.dynamic(key, fallback)` serves keys built at runtime, e.g. system
    taglines by id.
- **Fallback:** a missing or empty message falls back to English.
- **No provider** (isolated tests): `useLanguage()` answers English.
- **Legal notices stay in English** (the Wizards fan content notice, and later
  Paradox's and Darrington's). The text around them is translated.

### API language (L3)

- **Request language:**
  - `apiFetch` sends `Accept-Language` with the current language
    (`language/currentLocale.ts`).
  - `InterfaceLocaleConfig`'s `LocaleResolver` accepts `en` and `pt-BR`,
    English otherwise.
  - Code reads the language from `LocaleContextHolder`.
- **API messages:**
  - `InterfaceLocaleConfig.messageSource()` reads `locales/messages`
    (`messages_en.properties`, `messages_pt_BR.properties`).
  - It is declared explicitly because Spring Boot's auto-configuration only
    starts when an unsuffixed `messages.properties` exists, and the files
    follow the `.en` naming rule.
  - An exception the player can see implements `shared/LocalizedProblem`
    (`messageCode()`, `messageArguments()`). `ApiExceptionHandler` resolves
    it in the request's language and falls back to the English message.
  - Converted: character not found / is draft / not draft, not dying, level
    up not started, attunement limit, spell limit, preparation limit,
    insufficient hit dice, and the portrait size limit.
  - Programmer-facing errors stay English: unknown condition, coin, theme,
    field, tutorial id, system, roll.
  - Free-text rule errors (`InvalidBuildException`, `FeatNotAllowedException`,
    `LevelUpNotAllowedException`, …) need keys at each throw site, done in
    each system's slice.
- **Content overlay:** `shared/TranslationOverlay`.
  - `localize(english, "ruleset/vtm5/locales/vtm5-content", locale)` reads
    `<base>.<tag>.json` from the classpath once, and lays it over the English
    tree.
  - Only non-empty strings replace English text.
  - Inside arrays, elements are matched by `name` / `id`, or else by index.
- **AI notice:** `AppFooter rulesText` adds the fine-print line when the
  language isn't English. It's set on the builder, the sheets
  (`SheetShell`, `Vtm5Footer`, `DaggerheartFooter`) and the level-up pages.
- **Card summaries** stay English until each system's slice: they are built
  from content names that the system's terms translate.

### Login pages (Keycloak)

The login is Keycloak on another origin (`localhost:8081`), so it can't read the
app's `localStorage`. It shares **cookies** instead, which belong to the host,
not the port.

| Cookie | Written by | Read by |
| --- | --- | --- |
| `osv_locale` (`en` / `pt-BR`) | The app's language button and the login page's language menu | The app on start, and the API's `BrowserLoginRequestResolver` (via `InterfaceLocaleCookie`), which sends it as `ui_locales`, so the login opens in that language |
| `osv_theme` (`light` / `dark` / `system`) | `AppThemeProvider`, whenever the choice changes | `login/resources/js/osv-preferences.js`, before the page paints. `system` (or no cookie) follows `prefers-color-scheme`, like the app |
| `osv_locale_picked` | The login page's language menu | The app after the login. It means the player picked a language on the login page, so that choice beats the saved preference: the app keeps it, saves it to the server and clears the marker |

- **Realm:**
  - `internationalizationEnabled`, `supportedLocales` `en` / `pt-BR`, and
    `defaultLocale` `en` are set in `realm-export.json`.
  - The running local realm was updated with `kcadm`, because
    `--import-realm` skips a realm that already exists.
- **Template:**
  - `login/template.ftl` is a copy of Keycloak 26.4's `keycloak.v2`
    template.
  - It drops the built-in language `<select>` from the card's header.
  - It adds `nav.osv-language` after `</main>`: the same pill and dropdown
    as the app, centred below the card, with each language in its own name
    (`locale_en`, `locale_pt-BR` in both message files).
  - On a Keycloak upgrade, compare it with the new upstream template.
- **Messages:**
  - `messages_pt_BR.properties` translates the theme's own keys.
  - Keycloak's built-in Portuguese covers the rest.
- **Dark theme:** `html[data-osv-theme='dark']` in `omni-sheet-vault.css`
  re-tints the card to the app's "Arcane Vault" palette. The script also adds
  PatternFly's `pf-v5-theme-dark` class.
- **Not done (production):** the cookies are host-only. When Keycloak gets its
  own hostname (`auth.<domain>`, see `deployment-oracle-guide.md`), both sides
  must write them with `Domain=<domain>`, or the login falls back to English
  and the system theme.

## Where it stopped

- **L1 done (2026-10-14):**
  - header, user menu, language button, footer, landing, home, system cards,
    banners, delete dialog, system picker, create page and credits in both
    languages;
  - language and theme saved in `players`;
  - spinner and overlay; page-level "Loading…" texts replaced by the spinner
    (AppShell, builder, D&D level up, VtM step).
- **L2 done (2026-10-14):**
  - login and registration open in the app's language and theme;
  - the language button sits below the card;
  - a pick made there carries back into the app.
  - See "Login pages (Keycloak)".
- **L3 done (2026-10-14):**
  - API language and messages, content overlay, merge script and AI notice;
    see "API language (L3)";
  - first owner worksheet: `translation-worksheets/vtm-v5/01-content-terms.pt-BR.json`.
- **L4 done (2026-10-14), VtM:** see "VtM (L4)".
- **Now: D&D (L6+)**, moved ahead of Daggerheart by the owner on 2026-10-14.
  Daggerheart (L5) follows it. See "D&D (L6)".
- **Worksheets merged (2026-11-07):** all four (D&D 01 and 02, VtM 01 and 02)
  were filled and merged, so `translation-worksheets/` is empty. See "Names
  from the Brazilian books" below.

### Names from the Brazilian books

- **Sources:** the owner's PT-BR PDFs.
  - D&D: Livro do Jogador, Guia do Mestre, Manual dos Monstros, Xanathar,
    Volo, Costa da Espada, Compêndio Elemental.
  - VtM: the V5 Livro Básico.
- **Method:** each name was checked against the books' text; when missing,
  the book's headings and tables gave the official one. 1,176 of the 2,110
  D&D names come from those books. The rest (Tasha, Eberron, Ravnica,
  Wildemount…) follow the same terminology.
- **Choices:**
  - The PHB PDF is a community translation: it says "Chi" and carries the UA
    revised ranger. The app keeps "Ki", as the later books and the long texts
    do, and "Arquétipo de Patrulheiro".
  - Book typos are fixed ("Bênção", "Redenção", "Fênix").
- **VtM:**
  - Book terms used: Checagem de Sangue (Rouse Check), Pilar (Touchstone),
    Qualidade (Merit), Ficha de Conhecimento (Loresheet).
  - Clan bane names, specialties and supplement-only loresheets are not in
    the core book and were translated in the same style.

### VtM (L4)

- **Content:**
  - `vtm5-content.en.json` is the English source, renamed.
  - `Vtm5Content.localized(locale)` lays `locales/vtm5-content.pt-BR.json`
    (texts, translated by the assistant) over it and adds `terms` from
    `locales/vtm5-terms.pt-BR.json` (empty until the owner's first merge).
  - `term(name, locale)` falls back to English.
  - The rules keep reading `all()`, in English.
- **API texts:** `Vtm5Messages` reads `locales/vtm5-messages_{en,pt_BR}.properties`
  for the creation checklist prompts and problems, the sheet action errors,
  the XP log labels and the starting-XP note.
  - Those labels are stored in the language they were written in, like any
    other log text.
- **Rolls:** stored contexts stay English, because the code tells roll kinds
  apart by prefix. `Vtm5RollController.view` translates the label, the trait
  names, the rouse/frenzy/remorse notes and the outcome tag when serving.
- **Card summary:** `Vtm5CharacterSummarizer` shows clan, age and predator
  through `term`.
- **Web:**
  - `useVtm5Content` caches per language and refetches on a switch;
    `termOf` / `poolOf` show names.
  - `useVtm5Text` covers the builder frame and steps, the explanation column,
    the sheet, the dice dock and the footer sources.
  - The sheet, roll log and builder plan reload on a language switch.
  - `useStepLabel` and `useGeneration` (`13ª geração`) live in `vtm5Build.ts`.
- **Shared builder:** its save labels and draft badge moved to
  `locales/builder.{en,pt-BR}.json` (`useBuilderText`). It also refetches the
  draft's plan when the language changes. The rest of its chrome waits for
  the D&D slice, its only user.
- **Not done:**
  - Book names (Core, Camarilla, Anarch) and the roll label "Distill" stay
    English.
  - Stored player text (notes, item names, specialties typed in) is never
    translated.
- Small inline loading texts inside system panels ("Loading creatures…",
  "Loading the SRD…") wait for their system's slice.

### D&D (L6)

Layers approved by the owner on 2026-10-14, each closed and checked before the
next:

| Layer | Delivers | Owner's worksheet |
| --- | --- | --- |
| D1 | Interface: sheet, builder, level up, the shared builder and sheet chrome, ft → m; then the API's plan prompts and problems | Game-term labels |
| D2 | Content moved to one file per kind (`classes.en.json`…); classes, subclasses, species, backgrounds, feats, optional features | Their names |
| D3 | Spells | Spell names, schools, components |
| D4 | Conditions, mechanics, languages | Names |
| D5 | Items (long texts may stay English, decided then) | Names, properties |
| D6 | Creatures (same) | Names, types, sizes |

- **D1, web side, done (2026-10-15):**
  - `systems/dnd5e/locales/dnd5e.{en,pt-BR}.json` (`useDnd5eText`,
    `useDnd5eTerms` in `dnd5eMessages.ts`) covers the sheet's panes and
    tabs, the builder steps, level up and the builder tour. Game terms live
    under `terms` and come from the owner's worksheet; everything else was
    translated by the assistant.
  - `useDnd5eTerms()` adds lookups by key (`ability`, `skill`,
    `damageType`, `school`, `condition`…) and distances: `feet(30)` reads
    "30 ft." or "9 m". Inputs that store feet (sense overrides, custom
    action range) keep a "(pés)" label in Portuguese.
  - Roll contexts stay English (`ENGLISH_TERMS`), like VtM's.
  - Shared builder: `locales/builder.{en,pt-BR}.json` now covers the
    checklist, summary, portrait picker, selects, progression and pending
    mark. A definition's optional `useLabels()` hook (`BuilderLabels`) names
    its steps, checklist lines and the summary's game terms; D&D's is
    `useDnd5eBuilderLabels`.
  - Tours: `TutorialStep.title` is a `ReactNode`, so a step can render
    translated text. The D&D tour's text lives under `tutorial` in the D&D
    bundle; the tour chrome (Skip, Back, Next) is in `shell.*.json`.
  - `language/format.ts` gained `formatFixed` ("5.0 kg" / "5,0 kg").
- **D1, API side, done (2026-10-15):**
  - `Dnd5eText` words D&D's player-facing API texts from
    `locales/dnd5e-messages_{en,pt_BR}.properties` (codes without the
    `dnd5e.` prefix). `Dnd5eText.ENGLISH` serves tests and command-line
    tools; the `Dnd5eMessages` bean follows the request's language.
  - `Dnd5eCharacterCreationFlow` passes it to `Dnd5eBuildPlanner`,
    `Dnd5eSpellPlanner` and `Dnd5eClassProgression`. Their old constructors
    and `table(…)` overload keep English.
  - Translated: choice prompts, the ability-score method names, the
    starting-gold and ASI option labels, the equipment summaries' words,
    the prerequisite notes, the rule problems a player can fix (multiclass
    minimums, standard array, point buy, rolled hit points, ability
    maximum, feat prerequisites), the level 20 limit, and the progression
    headers and ordinals.
  - Kept English, because they are stored or read by code: grant sources
    ("Fighter 1", "Other modifier", "Override", "Base"), roll labels,
    spell usage notes ("At will"), and the problems that name internal ids.
  - The web reads the progression's features as its third column
    (`PROGRESSION_FEATURES_COLUMN`) instead of by the English header, and
    the Abilities step's problem filter also knows the Portuguese words.
  - The level up page refetches on a language switch, like the builder.
  - Messages without arguments are read as they are, so they write a
    single `'`; messages with arguments follow `MessageFormat` (`''`).
- **Left for D2 (content):** every content name (the owner's worksheet 02).
- **Class progression tables (D2 slice 3c, 2026-10-19):**
  `Dnd5eClassProgression` reads the class's and subclass's column labels,
  feature names and the table's title through the catalogue's translation
  (`CatalogueRecord.texts` / `localizedName`). The 18 column labels ("Rages",
  "Sneak Attack", "Cantrips Known"…) are the worksheet 02 group
  `class-progression-columns`, with `"field": "label"`: the merge matches
  them by `label` instead of `name` and writes them by index
  (`data.tableColumns.<n>.label`) into `classes.pt-BR.json` and
  `subclasses.pt-BR.json`. Reimport both kinds after a merge.
- **Subspecies "Standard" (D2 slice 3b, 2026-10-19):** the unnamed default
  subspecies keeps the key `""` (stored in `subspeciesName`); its option label
  is `plan.standardSubspecies` ("Standard" / "Padrão"). The web's
  `describeBuild` leaves it out by its empty key, not by its label.
- **Option summaries and card values (D2 slice 3a, 2026-10-19):**
  - `Dnd5eOptionSummaries` and `Dnd5eSelectionDetails` write through
    `Dnd5eText`: sentence words from `dnd5e-messages` (`summary.*`,
    `unit.distance*`), game terms through `Dnd5eText.term(group, key,
    english)`, which `Dnd5eMessages` reads from `dnd5e-terms.<tag>.json`
    (English, or an empty term, keeps the code's English).
  - `Dnd5eText.distance(feet)` writes "30 ft" or "9 m" (factor 0.3, one
    decimal), like the web's `formatFeet`.
  - Feature names inside summaries (background features, a subclass's first
    features) and feat / optional-feature prose come from the catalogue's
    translation.
  - The card labels stay English keys, shown through `terms.selectionLabels`.
  - Kept English until their layer: spell and language summaries (D3, D4),
    language names, item names (D5), known species spells.
- **D1 1280x720 check in Portuguese done (2026-10-19):** sheet (all tabs,
  spell panel), builder (all seven steps), level up start page and the
  builder tour. Fixed on the way: the header's back link under the language
  button (the centred column now keeps 260 px clear of each edge), the spell
  rows' "Conjurar" / "À vontade" column (wider under `:lang(pt-BR)`), the
  Basics name field squeezed by the portrait column (`minmax(220px, 1fr)`),
  and tour bubbles placed off screen (now measured). Level-up steps after
  choosing a class were not opened, since that starts a level up. The
  Daggerheart tour stays English until L5.

#### D2 (character options)

- **Done (2026-10-15):**
  - **One file per kind:** `content/dnd-5e/<kind>/<kind>.en.json` (a JSON
    array in slug order). The 3,551 entry files were merged, nothing else
    changed. `CatalogueEntryFiles` reads an array file as many entries, an
    object file as one, and skips translation files. The importer,
    `ContentDirectoryCatalogue` and the 5etools ingestion use it; ingestion
    writes the English file whole and keeps translation files.
  - **Translations in the database:** `<kind>.pt-BR.json` maps each slug to
    its translated text, mirroring the entry (`name`, `description`, `data`;
    arrays addressed by the English element `name`, every same-named element
    at once). The import stores them in `catalogue_entries.translations`
    (V12), so every edition serves them without the content folder.
  - **Display only:** `CatalogueRecord.translations` with `localizedName` /
    `localizedDescription`. Catalogue browsing (`list`, `get`) lays the
    translation over name, description and data. The builder's option
    labels use the localized name. Rules, materialized sheets and
    `getUnredacted` (which copies into a sheet) stay English.
  - **Owner's names worksheet:**
    `translation-worksheets/dnd-5e/02-character-option-names.pt-BR.json`,
    2,092 unique names of classes, subclasses, species, backgrounds, feats and
    optional features with their features, traits, subspecies and variants.
    Its groups carry `names` (a list of kinds) instead of a key path: the
    merge script puts each name into every entry of those kinds that
    carries it, in `<kind>.pt-BR.json`. An optional `field` matches another
    text field than `name` (the progression columns use `label`). After a
    merge, reimport the kinds.
- **Long texts, by source book** (owner's choice, 2026-10-15), about 1.45
  million characters in all:

  | Stage | Books | Characters |
  | --- | --- | --- |
  | 1 | Player's Handbook | 473k (396k distinct) |
  | 2 | Xanathar's, Tasha's, Dungeon Master's Guide | 298k |
  | 3 | Monsters of the Multiverse, Volo's, Fizban's, Tome of Foes, Elemental Evil | 205k |
  | 4 | Setting books (Ravnica, Avernus, Sword Coast, Eberron, Wildemount, Ravenloft, Theros, Strixhaven, Dragonlance, Spelljammer, Witchlight, Bigby, Sigil, Book of Many Things, Saltmarsh, Acq. Inc., Tomb of Annihilation) | ~330k |
  | 5 | Plane Shift, small packages, Unearthed Arcana | ~140k |

  - **Stage 1 done (2026-10-15):** every PHB class, subclass, species,
    background, feat and optional feature: descriptions, feature and trait
    texts, and the backgrounds' personality tables. 205 entries carry a
    translation in the catalogue.
  - **Stage 2 done (2026-10-15):** Xanathar's, Tasha's and the DMG: the
    artificer, their subclasses, feats, optional features (infusions, runes,
    maneuvers, invocations, Arcane Shot options) and species (DMG monster
    races, Custom Lineage). 186 more entries carry a translation. Working
    files `xge-*`, `tce-*`, `dmg-*` in `_ai/`; DMG species used
    `atoms`/`compose`.
  - **Stage 3, Monsters of the Multiverse done (2026-10-15):** all 30
    species (`motm-species*`, via `atoms`/`compose`).
  - **Stage 3, Elemental Evil, Tome of Foes and Fizban's done
    (2026-10-15):** aarakocra and genasi, gith, the three dragonborn of
    Fizban's (chromatic, gem, metallic), Way of the Ascendant Dragon,
    Drakewarden and the four feats (`eepc-*`, `mtf-*`, `ftd-*`). Applied to
    the content files but not reimported or checked on screen, at the
    owner's request; reimport `species`, `subclasses` and `feats` to see
    them.
  - **Stage 3, Volo's done (2026-10-15):** all 13 species (`vgm-species*`,
    via `atoms`/`compose`), applied without reimport or tests at the owner's
    request. Stage 3 is complete.
  - **Stage 4 done (2026-10-18):** all 17 setting books (Ravnica, Avernus,
    Sword Coast, Eberron, Wildemount, Ravenloft, Theros, Strixhaven,
    Dragonlance, Spelljammer, Witchlight, Bigby, Sigil, Book of Many Things,
    Saltmarsh, Acquisitions Inc., Tomb of Annihilation): their subclasses,
    species, backgrounds and feats. Working files `<book>-<kind>` in `_ai/`
    (`ggr`, `bgdia`, `scag`, `erlw`, `egw`, `vrgr`, `mot`, `scc`, `dsotdq`,
    `aag`, `wbtw`, `bgg`, `sato`, `bmt`, `gos`, `ai`, `toa`). Avernus restates
    the PHB backgrounds, so its PHB texts were reused from `phb-backgrounds`.
    Applied and `:apps:api:check` green, but not reimported: Docker was down.
  - **Stage 5 done (2026-10-18):** the six Plane Shift books, the Tortle
    Package, Locathah Rising, One Grung Above, Adventure with Muk and UA: The
    Mystic Class (class and subclasses). Working files `ps<a|d|i|x|k|z>-*`,
    `tttp-*`, `lr-*`, `oga-*`, `awm-*`, `uamy-*`. Every class, subclass,
    species, background, feat and optional feature in the catalogue now has
    its long texts in Brazilian Portuguese. Not reimported (Docker down).
  - **Tool:** `tools/translation-worksheet/content-texts.mjs`.
    - `extract` writes a book's distinct English texts of one kind.
    - The translation goes to a file with the same ids.
    - `apply` places each one at every path that holds its English text, in
      `<kind>.pt-BR.json`, keeping the owner's names.
    - Where texts restate each other (species variants), `atoms` keeps only
      the base texts plus the leftover labels and sentences, and `compose`
      rebuilds the rest from their translations.
    - The working files stay in `translation-worksheets/dnd-5e/_ai/`
      (`phb-<kind>.en.json` / `.pt-BR.json`), so a re-ingest can apply them
      again. The merge script skips that folder.
  - **Conventions in the texts:** distances in metres (5 ft = 1.5 m, 1 mile =
    1.5 km), weights in kg, alignments as (Leal), (Bom)… Spell, feature and
    trait names inside the prose use the assistant's best match for the
    Brazilian books; the owner's worksheet sets the names shown as titles.
  - After applying, reimport the kinds (see `developer-guide.md`).
- **Display pass (2026-10-15):**
  - `CatalogueTextMemory` maps every English catalogue text to its
    translation (rebuilt when the catalogue changes).
  - `CharacterSheetLocalizationAdvice` (a `ResponseBodyAdvice`) applies it to
    every `CharacterSheetResponse`: feature and trait names, sources,
    summaries and descriptions, feature actions, and the background's name
    and feature. `SheetTextLocalizer` handles composed names
    ("Fighting Style: Archery", "Fighter 3") and rebuilds row summaries from
    the translated description. The stored sheet and the player's own text
    are untouched.
  - The builder's species and background cards translate trait and feature
    texts through `CatalogueRecord.texts(locale)`. Their labels stay English
    keys and the web shows them through `terms.selectionLabels`.
- **Reimport done (2026-10-19):** `classes`, `species`, `subclasses`,
  `feats` and `backgrounds` were reimported, and every entry of those kinds
  (and of `optional-features`) carries a translation. Checked in pt-BR:
  catalogue descriptions of Fairy, Tortle, Grung, Locathah, Echo Knight, the
  Mystic subclasses and the Strixhaven feats; on Vex's sheet, the Changeling
  traits and the Shapechanger action.
- **Names merged (2026-11-07):** worksheets 01 and 02 were merged and the six
  kinds reimported. Checked in pt-BR on Mez's sheet: abilities, skills, metres
  and the warlock's features (Magia de Pacto, Patrono Transcendental).
  Proficiency lists, languages, senses and damage types still show English;
  they come with D4 and D5.
- **Next:**
  - Spells (D3), then conditions, mechanics and languages (D4) and items
    (D5).

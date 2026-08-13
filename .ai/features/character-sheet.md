# Feature — character sheet

How the sheet behaves. Layout and styling live in `ui-design-system.md`,
`ui-design-tokens.md` and `rulesets/<system>-sheet-ui.md`; this document is about what
happens when the player interacts with it.

## Scope

Viewing and playing an existing character: reading values, rolling, and changing the
state that changes during a session.

Out of scope: character creation and levelling up, which alter what the character *is*
rather than how it currently stands.

Managing prepared spells **is** in scope despite looking like character construction —
several classes re-prepare at every rest, so it is session state.

## The three outcomes rule

Every interactive element on the sheet does exactly one of three things:

| Outcome | Effect | Example |
| --- | --- | --- |
| Explain | Opens a sidebar panel | Clicking an ability score |
| Roll | Resolves a roll on the server and records it | Clicking an ability modifier |
| Mutate | Changes character state and persists it | Marking a resource as used |

An element that would do two of these is a design error. Where one visual component
needs two outcomes, it has two distinct click targets — the ability box is one
component with a score target that explains and a modifier target that rolls.

Only roll targets change background colour on hover. See `ui-design-system.md`.

## Rolling

The client never computes a result. It sends **what is being rolled**, not the dice:
the character, the source of the roll, and any player-supplied modifiers such as
advantage or a chosen spell level.

The server resolves the expression, generates the result, persists it as an immutable
roll record, and returns it. The result appears in the dice tray and is appended to
the game log.

Consequences:

- A roll that fails to reach the server is not a roll. Never fabricate a local result
  as a fallback.
- Roll records are never edited or deleted, including when a character is deleted.
- The roll's context is captured at roll time — "Guiding Bolt: damage", "History:
  check", "Custom: roll" — because the log is read long after the fact.

Manual rolls from the dice tray follow the same path, with no source attached.

## Mutations

Each mutation persists immediately. There is no explicit save.

| Mutation | Notes |
| --- | --- |
| Hit points | Damage, healing, temporary hit points. Damage consumes temporary first |
| Resource use | Marking uses spent or restored |
| Resource spend with amount | The player chooses how much, not just one unit |
| Spell slot | Consumed at the level chosen when casting, not the spell's base level |
| Prepared spells | Preparing and unpreparing, within the limits of each class |
| Condition | Toggled. In the first version it changes no other value |
| Inspiration | Boolean toggle |
| Equipped state | Feeds the actions list and derived values — not cosmetic |
| Attunement | Bounded by the system's limit |
| Inventory, coins, proficiencies, feats, extras | Add and remove |
| Background and notes | Free text, edited in the sidebar |
| Rest | Compound operation, below |

The UI updates optimistically and rolls back on failure, surfacing the error rather
than leaving the screen showing a change that did not persist.

## Rests

A rest is a mechanic resolved by the system's strategy, not a sequence of individual
mutations issued by the client. The client sends the intent and any player input; the
server applies every consequence and returns the resulting state.

Rests restore resources by matching the rest type against each resource's recharge
trigger. This is why resources are modelled with an explicit trigger rather than each
feature carrying bespoke reset logic.

Rests that roll dice record those rolls in the log like any other roll.

## Derived values

Every derived value arrives with its contributions, each labelled with its source. The
sheet renders the value; the sidebar renders the breakdown.

A derived value is never stored — it is recalculated from stored state on every read.
Storing it would create two sources of truth that drift.

## Redacted descriptions

When redaction is enabled, description text arrives as a marker and the client renders
a placeholder. Everything else behaves normally: spells cast, resources track, items
contribute. See `decisions/adr-0005-content-catalogue-and-redaction.md`.

## Ownership

Every read and write resolves the owner from the token subject. A character identifier
supplied by the client is never trusted as proof of ownership.

## Open questions

- Does the sidebar stay open when the player switches tabs, or close?
- What happens when the same character is open in two places at once — last write
  wins, or a conflict warning?
- Is a rest undoable? It changes many values at once and a misclick is plausible.
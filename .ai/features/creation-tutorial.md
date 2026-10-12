# Creation tutorial

A guided tour of the character builder, shown on its own the first time a player
opens a system's builder, and reopened from a floating **Tutorial** button in the
page's bottom-right corner. Every system has its own steps, text and look; the
mechanics are shared.

Approved by the owner on 2026-10-11 (plan in `open-items.md`, then moved here).

## Behaviour

- Opens automatically when the player's `seen_tutorials` (see
  `database-schema.md`, `players`) doesn't hold the system's tutorial id. Once per
  system: `builder:dnd-5e` and `builder:vtm-v5` are separate.
- Finish, **Skip tutorial** or Escape closes it and marks it seen
  (`PUT /api/me/tutorials/{id}`). Finish also returns the builder to its first
  step. Reopening from the button never marks it again.
- Each step dims the page except one part (the spotlight) and explains it in a
  bubble beside it, with "N of M", Back, Next/Finish and Skip. Arrow keys move,
  Escape closes. A step can name a builder step to open first; one whose target
  isn't on the page shows its bubble centred.
- The first step carries the disclaimer: the tool does the arithmetic and lays the
  choices out; it doesn't replace the books, the DM's rulings or a printed sheet.
- Rules text is short, original English, never copied from a book or D&D Beyond.

## Code

| Piece | Where |
| --- | --- |
| Seen list API | `identity/TutorialController` (`GET /api/me/tutorials`, `PUT /api/me/tutorials/{id}`), `PlayerService.markTutorialSeen`, migration `V10__add_player_seen_tutorials.sql` |
| Tour types | `apps/web/src/tutorial/tutorialDefinition.ts`: `TutorialDefinition { id, className, steps }`, `TutorialStep { title, body, target?, builderStep? }` |
| Overlay | `tutorial/Tutorial.tsx` + `tutorial.css`: four shade panels and a ring placed with transforms, measured at most once a frame |
| Launcher | `tutorial/TutorialLauncher.tsx` (fixed bottom-right) |
| Seen state | `tutorial/useTutorial.ts` |
| Wiring | `BuilderDefinition.tutorial`; `CharacterBuilder` renders the launcher and the tour for both the shared layout and a system's own `Frame` |
| Shared targets | `data-tutorial="checklist" / "step" / "stepnav" / "summary" / "save-state" / "help"` in `CharacterBuilder` and the launcher |

### Adding a system's tour

Write a `TutorialDefinition` in the system's builder folder with its own id
(`builder:<system-id>`), its steps and a theme class that sets the
`--tutorial-*` tokens (accent, surface, text, fonts, radius), then set
`tutorial` on the system's `BuilderDefinition`. A system with its own `Frame`
adds `data-tutorial` attributes to its own parts. No shared file changes.

## Slices

| Slice | Status |
| --- | --- |
| 0. Middle-column frames capped at one screen | Done 2026-10-11 |
| 1. Shared overlay + D&D 5e tour | Done 2026-10-11 (`systems/dnd5e/builder/dnd5eTutorial.tsx`, 16 steps: Basics in four stops, Ability scores in two, every other step on its first frame) |
| 2. VtM V5 tour | Next |
| 3. Daggerheart tour | When its builder exists |

export type StandardAction = { key: StandardActionKey };

export type StandardActionKey =
  | 'attack'
  | 'dash'
  | 'disengage'
  | 'dodge'
  | 'help'
  | 'hide'
  | 'ready'
  | 'search'
  | 'useAnObject'
  | 'twoWeaponFighting'
  | 'opportunityAttack'
  | 'interactWithAnObject';

/**
 * The nine standard 5e combat actions — identical text for every character, not
 * character data, so this is a static frontend list rather than something fetched
 * from the API. Not sourced from the content catalogue (phase 7): catalogue
 * content has no consuming UI yet, and importing fixtures for exactly nine fixed,
 * unchanging entries would be a bigger lift than this slice's scope.
 */
export const STANDARD_ACTIONS: StandardAction[] = [
  { key: 'attack' },
  { key: 'dash' },
  { key: 'disengage' },
  { key: 'dodge' },
  { key: 'help' },
  { key: 'hide' },
  { key: 'ready' },
  { key: 'search' },
  { key: 'useAnObject' },
];

/**
 * Every character can fight with two light weapons at once — confirmed live
 * against a second reference character (a Fighter, dndbeyond.com/characters/
 * 51556097, picked specifically because Aria's own seeded attacks never
 * exercise the Bonus Action/Reaction/Other filters): its own "Bonus Actions"
 * section shows this same universal entry under its own "Actions in Combat"
 * list, same shape as `STANDARD_ACTIONS` above.
 */
export const BONUS_ACTIONS: StandardAction[] = [
  { key: 'twoWeaponFighting' },
];

/**
 * Every character can take an opportunity attack — same reference character
 * as above, confirmed live its "Reactions" section shows only this one
 * universal entry (the owner's own character has no class-granted reactions
 * to add beside it).
 */
export const REACTIONS: StandardAction[] = [
  { key: 'opportunityAttack' },
];

/**
 * Every character can interact with one object for free during their turn —
 * same reference character, confirmed live its "Other" section shows only
 * this one universal entry.
 */
export const OTHER_ACTIONS: StandardAction[] = [
  { key: 'interactWithAnObject' },
];

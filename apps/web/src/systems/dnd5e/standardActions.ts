export type StandardAction = { name: string; description: string };

/**
 * The nine standard 5e combat actions — identical text for every character, not
 * character data, so this is a static frontend list rather than something fetched
 * from the API. Not sourced from the content catalogue (phase 7): catalogue
 * content has no consuming UI yet, and importing fixtures for exactly nine fixed,
 * unchanging entries would be a bigger lift than this slice's scope.
 */
export const STANDARD_ACTIONS: StandardAction[] = [
  { name: 'Attack', description: 'Make one melee or ranged attack.' },
  { name: 'Dash', description: 'Gain extra movement equal to your speed.' },
  { name: 'Disengage', description: 'Your movement does not provoke opportunity attacks for the rest of the turn.' },
  {
    name: 'Dodge',
    description:
      'Attacks against you have disadvantage, and you have advantage on Dexterity saving throws, as long as you can see the attacker and are not incapacitated.',
  },
  {
    name: 'Help',
    description: "Aid another creature's ability check, or aid an ally's attack against a creature within 5 feet of you.",
  },
  { name: 'Hide', description: 'Make a Dexterity (Stealth) check to try to hide.' },
  { name: 'Ready', description: 'Prepare to act in response to a trigger you define, using your reaction when it occurs.' },
  { name: 'Search', description: 'Devote your attention to finding something.' },
  { name: 'Use an Object', description: 'Interact with a second object, or use an object that requires your action.' },
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
  {
    name: 'Two-Weapon Fighting',
    description:
      "When you take the Attack action and attack with a light melee weapon in one hand, use a bonus action to attack with a different light melee weapon in your other hand. Don't add your ability modifier to that second attack's damage, unless the modifier is negative.",
  },
];

/**
 * Every character can take an opportunity attack — same reference character
 * as above, confirmed live its "Reactions" section shows only this one
 * universal entry (the owner's own character has no class-granted reactions
 * to add beside it).
 */
export const REACTIONS: StandardAction[] = [
  {
    name: 'Opportunity Attack',
    description:
      'When a hostile creature you can see moves out of your reach, use your reaction to make one melee attack against it, resolved right before it leaves.',
  },
];

/**
 * Every character can interact with one object for free during their turn —
 * same reference character, confirmed live its "Other" section shows only
 * this one universal entry.
 */
export const OTHER_ACTIONS: StandardAction[] = [
  {
    name: 'Interact with an Object',
    description: 'As a free part of your turn, interact with one object or feature of the environment — open a door, draw a weapon, pick something up.',
  },
];

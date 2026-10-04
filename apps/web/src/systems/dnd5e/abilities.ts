export type Ability = { key: string; abbreviation: string; label: string };

export const ABILITIES: Ability[] = [
  { key: 'strength', abbreviation: 'STR', label: 'Strength' },
  { key: 'dexterity', abbreviation: 'DEX', label: 'Dexterity' },
  { key: 'constitution', abbreviation: 'CON', label: 'Constitution' },
  { key: 'intelligence', abbreviation: 'INT', label: 'Intelligence' },
  { key: 'wisdom', abbreviation: 'WIS', label: 'Wisdom' },
  { key: 'charisma', abbreviation: 'CHA', label: 'Charisma' },
];

export function abilityAbbreviation(key: string): string {
  return ABILITIES.find((ability) => ability.key === key)?.abbreviation ?? key.slice(0, 3).toUpperCase();
}

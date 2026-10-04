/** D&D Beyond's sheet themes (campaign themes left out), as the API accepts them; DDB Red is the default. */
export type SheetTheme = { id: string; label: string; color: string };

export const DEFAULT_SHEET_THEME = 'ddb-red';

export const SHEET_THEMES: SheetTheme[] = [
  { id: 'ddb-red', label: 'DDB Red', color: '#C53131' },
  { id: 'barbarian-fire', label: 'Barbarian Fire', color: '#E5623E' },
  { id: 'bard-rouge', label: 'Bard Rouge', color: '#AA6DAB' },
  { id: 'cleric-silver', label: 'Cleric Silver', color: '#92A2B3' },
  { id: 'druid-moss', label: 'Druid Moss', color: '#79853C' },
  { id: 'fighter-rust', label: 'Fighter Rust', color: '#7E4F3D' },
  { id: 'monk-sky', label: 'Monk Sky', color: '#53A5C5' },
  { id: 'paladin-gold', label: 'Paladin Gold', color: '#B59E54' },
  { id: 'ranger-emerald', label: 'Ranger Emerald', color: '#4F7E61' },
  { id: 'rogue-ash', label: 'Rogue Ash', color: '#555752' },
  { id: 'sorcerer-blood', label: 'Sorcerer Blood', color: '#972E2E' },
  { id: 'warlock-iris', label: 'Warlock Iris', color: '#8137AF' },
  { id: 'wizard-cobalt', label: 'Wizard Cobalt', color: '#0045B7' },
  { id: 'artificer-copper', label: 'Artificer Copper', color: '#D59139' },
];

export function sheetThemeLabel(id: string | null | undefined): string {
  return SHEET_THEMES.find((theme) => theme.id === id)?.label ?? SHEET_THEMES[0].label;
}

const RARITY_CLASS_SUFFIXES: Record<string, string> = {
  uncommon: 'uncommon',
  rare: 'rare',
  'very rare': 'very-rare',
  legendary: 'legendary',
  artifact: 'artifact',
};

/** `null` for a common/mundane item — no color override, same as D&D Beyond's own plain-ink item names. */
export function rarityClassSuffix(rarity: string | null): string | null {
  return rarity ? RARITY_CLASS_SUFFIXES[rarity.toLowerCase()] ?? null : null;
}

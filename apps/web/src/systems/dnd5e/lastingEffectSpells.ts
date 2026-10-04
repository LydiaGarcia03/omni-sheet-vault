import { createContext, useContext } from 'react';
import type { CatalogueEntry } from '../../catalogue/api';

/** Slugs of the catalogue spells whose data carries a lasting effect (Mage Armor). */
export const LastingEffectSpellsContext = createContext<ReadonlySet<string>>(new Set());

export function lastingEffectSpellSlugs(catalogueSpells: CatalogueEntry[]): ReadonlySet<string> {
  return new Set(
    catalogueSpells
      .filter((entry) => (entry.data as { effect?: unknown } | null)?.effect != null)
      .map((entry) => entry.slug),
  );
}

export function useHasLastingEffect(spellKey: string): boolean {
  return useContext(LastingEffectSpellsContext).has(spellKey);
}

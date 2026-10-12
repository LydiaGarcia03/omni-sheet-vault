import { useEffect, useState } from 'react';
import { getCatalogue } from '../../../catalogue/api';

/** A spell's rules text from the catalogue; `text` is null when the server redacts prose. */
export type SpellDescription = { text: string | null; higherLevels: string | null; redacted: boolean };

let loading: Promise<Map<string, SpellDescription>> | null = null;

function load(): Promise<Map<string, SpellDescription>> {
  loading ??= getCatalogue('dnd-5e', 'SPELL').then(
    (entries) =>
      new Map(
        entries.map((entry) => {
          const data = entry.data as { higherLevelsDescription?: string | null } | null;
          return [entry.slug, { text: entry.description.value, higherLevels: data?.higherLevelsDescription ?? null, redacted: entry.description.redacted }];
        }),
      ),
    (error: unknown) => {
      loading = null;
      throw error;
    },
  );
  return loading;
}

/** Every spell's description by slug, fetched once per page load and shared by all spell pickers; null until it arrives. */
export function useSpellDescriptions(enabled: boolean): Map<string, SpellDescription> | null {
  const [descriptions, setDescriptions] = useState<Map<string, SpellDescription> | null>(null);
  useEffect(() => {
    if (!enabled || descriptions) {
      return;
    }
    let active = true;
    load().then(
      (loaded) => active && setDescriptions(loaded),
      () => undefined,
    );
    return () => {
      active = false;
    };
  }, [enabled, descriptions]);
  return descriptions;
}

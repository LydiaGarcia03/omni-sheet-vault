import type { SpellcastingClassInfo } from '../../sheet/api';

export type HeaderValue = { value: number; sources: string[] };

/**
 * One header value per distinct number, listing every class that shares it, as D&D
 * Beyond's own spellcasting header does. Only class casters count; a sheet whose
 * only casters are granted sources (a species' spells) shows those instead.
 */
export function groupHeaderValues(
  casters: SpellcastingClassInfo[],
  pick: (info: SpellcastingClassInfo) => number,
): HeaderValue[] {
  const classCasters = casters.filter((info) => info.classCaster);
  const shown = classCasters.length > 0 ? classCasters : casters;
  const grouped: HeaderValue[] = [];
  for (const info of shown) {
    const value = pick(info);
    const existing = grouped.find((entry) => entry.value === value);
    if (existing) {
      existing.sources.push(info.className);
    } else {
      grouped.push({ value, sources: [info.className] });
    }
  }
  return grouped;
}

import abjurationIcon from './frames/dnd_icon_school_abjuration.svg?raw';
import conjurationIcon from './frames/dnd_icon_school_conjuration.svg?raw';
import divinationIcon from './frames/dnd_icon_school_divination.svg?raw';
import enchantmentIcon from './frames/dnd_icon_school_enchantment.svg?raw';
import evocationIcon from './frames/dnd_icon_school_evocation.svg?raw';
import illusionIcon from './frames/dnd_icon_school_illusion.svg?raw';
import necromancyIcon from './frames/dnd_icon_school_necromancy.svg?raw';
import transmutationIcon from './frames/dnd_icon_school_transmutation.svg?raw';

export function schoolLabel(school: string): string {
  return school.charAt(0).toUpperCase() + school.slice(1);
}

/**
 * One icon per school of magic, keyed off `Spell.school` — the owner's own
 * traced symbols (2026-09-03, see `systems/dnd-5e/sheet-build.md`), not
 * game-icons.net picks like `AttackRow.tsx`'s icons, so no attribution entry
 * is needed for these eight. Direct lookup, no fallback branch, since every
 * spell in the domain model carries one of these eight schools.
 *
 * **Moved out of `SpellRow.tsx` same day, direct owner correction:** the
 * icon's actual place is the Actions tab (`SpellAttackRow.tsx`), not the
 * Spells tab — confirmed live against D&D Beyond (Helga Flinthand's Guiding
 * Bolt/Inflict Wounds, Copy of Raya's Fire Bolt/Ray of Frost/Ice Knife) that
 * the school icon only ever appears in the Actions tab's attack table; the
 * Spells tab's own leading column is a Cast/At Will/As Ritual indicator
 * instead (`SpellRow.tsx`'s `.spell-row__cast`). Extracted to this shared
 * module so both rows import the same eight assets rather than duplicating
 * the map.
 */
export const SCHOOL_ICONS: Record<string, string> = {
  abjuration: abjurationIcon,
  conjuration: conjurationIcon,
  divination: divinationIcon,
  enchantment: enchantmentIcon,
  evocation: evocationIcon,
  illusion: illusionIcon,
  necromancy: necromancyIcon,
  transmutation: transmutationIcon,
};

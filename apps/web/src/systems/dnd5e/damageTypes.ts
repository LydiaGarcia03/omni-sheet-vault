import acidIcon from './frames/dnd_icon_damage_acid.svg?raw';
import bludgeoningIcon from './frames/dnd_icon_damage_bludgeoning.svg?raw';
import coldIcon from './frames/dnd_icon_damage_cold.svg?raw';
import fireIcon from './frames/dnd_icon_damage_fire.svg?raw';
import forceIcon from './frames/dnd_icon_damage_force.svg?raw';
import lightningIcon from './frames/dnd_icon_damage_lightning.svg?raw';
import necroticIcon from './frames/dnd_icon_damage_necrotic.svg?raw';
import piercingIcon from './frames/dnd_icon_damage_piercing.svg?raw';
import poisonIcon from './frames/dnd_icon_damage_poison.svg?raw';
import psychicIcon from './frames/dnd_icon_damage_psychic.svg?raw';
import radiantIcon from './frames/dnd_icon_damage_radiant.svg?raw';
import slashingIcon from './frames/dnd_icon_damage_slashing.svg?raw';
import thunderIcon from './frames/dnd_icon_damage_thunder.svg?raw';

export function damageTypeLabel(damageType: string): string {
  return damageType.charAt(0).toUpperCase() + damageType.slice(1);
}

/**
 * One icon per 5e damage type, keyed lowercase (matching `AttackRow.damageType`/
 * `Spell.damageType` as stored) — the owner's own picks from game-icons.net
 * (CC BY 3.0 — requires attribution, tracked on the still-owed credits page
 * alongside the attack-category icons): `fast-arrow`/`axe-swing`/
 * `hammer-drop`/`beveled-star`/`reaper-scythe`/`ion-cannon-blast`/
 * `focused-lightning`/`suspicious`/`eclipse-flare`/`lightning-storm` (Lorc)
 * for nine of the thirteen types, `death-juice` (Daniel Zaitzev/darkzaitzev)
 * and `acid` (Sbed) for two more. Direct lookup, no fallback — a row only
 * renders this icon when `damageType` is present and non-null, which every
 * attack and non-healing damage spell carries.
 *
 * **Fire and piercing, direct owner request, 2026-09-04:** replaced with
 * Flaticon picks — "Fire" by meaicon (Shapes And Symbol pack, Meaicon Black
 * Fill style) and "Bullet" by Magnific (Law And Justice pack, Basic Rounded
 * Filled style) — both attribution-required (Flaticon License, also
 * still-owed on the credits page). Flaticon's SVG format is paywalled for
 * these two (PNG is the only free format), so unlike the other eleven icons
 * they aren't a `currentColor`-fillable path: each is the free 512px PNG
 * wrapped in an `<svg><image href="data:...">`, same raster-fallback
 * technique `dnd_frame.svg` already used (see `FrameIcon.tsx`/
 * `svgUtils.ts`'s `extractRasterImage`). Fire itself went through two
 * earlier corrections before this one, also direct owner requests, same
 * day: Lorc's `flamed-leaf` → Sbed's `flamer` → Lorc's `small-fire`
 * (game-icons.net, real vector) — this Flaticon swap is the final pick.
 */
export const DAMAGE_TYPE_ICONS: Record<string, string> = {
  piercing: piercingIcon,
  slashing: slashingIcon,
  bludgeoning: bludgeoningIcon,
  fire: fireIcon,
  cold: coldIcon,
  poison: poisonIcon,
  acid: acidIcon,
  necrotic: necroticIcon,
  force: forceIcon,
  lightning: lightningIcon,
  psychic: psychicIcon,
  radiant: radiantIcon,
  thunder: thunderIcon,
};

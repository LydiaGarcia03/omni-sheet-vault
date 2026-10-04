import armorIcon from './frames/dnd_item_kind_armor.jpg';
import gearIcon from './frames/dnd_item_kind_gear.jpg';
import weaponIcon from './frames/dnd_item_kind_weapon.jpg';

const ITEM_KIND_ICONS: Record<string, string> = {
  WEAPON: weaponIcon,
  ARMOR: armorIcon,
  SHIELD: armorIcon,
  GEAR: gearIcon,
};

type ItemPreviewIconProps = {
  itemKind: string;
};

/**
 * The sidebar's `ct-sidebar__header-preview-image` slot for an item without
 * its own unique artwork — D&D Beyond falls back to one plain silhouette per
 * broad category (fetched live from its own CDN: `weapon.jpg`/`armor.jpg`,
 * and `potion.jpg` reused as the catch-all for everything else, confirmed
 * against Bedroll/Amulet/Crossbow Bolts alike) instead of a blank box. Keyed
 * off `Dnd5eItemKind` directly — `SHIELD` shares `armor.jpg` with `ARMOR`,
 * matching D&D Beyond's own Shield/Scale Mail rows. A cataloged item's real
 * unique illustration (e.g. Cloak of Elvenkind) is out of scope — this app
 * has no per-item art catalog, only these three generic fallbacks.
 */
export function ItemPreviewIcon({ itemKind }: ItemPreviewIconProps) {
  const src = ITEM_KIND_ICONS[itemKind] ?? gearIcon;
  return <img className="item-preview-icon" src={src} alt="" aria-hidden="true" />;
}

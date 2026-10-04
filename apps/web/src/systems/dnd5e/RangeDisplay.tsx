import { FrameIcon } from './FrameIcon';
import coneIcon from './frames/dnd_icon_aoe_cone.svg?raw';
import cubeIcon from './frames/dnd_icon_aoe_cube.svg?raw';
import cylinderIcon from './frames/dnd_icon_aoe_cylinder.svg?raw';
import lineIcon from './frames/dnd_icon_aoe_line.svg?raw';
import sphereIcon from './frames/dnd_icon_aoe_sphere.svg?raw';
import squareIcon from './frames/dnd_icon_aoe_square.svg?raw';

type ParsedRange = {
  primary: string;
  unit?: string;
  longRange?: string;
  areaDetail?: string;
};

/**
 * DOM-measured against D&D Beyond's own `.ddbc-combat-attack__range`
 * (Warhammer "5 ft." + "Reach", Crossbow "80 (320)") and
 * `.ddbc-combat-attack__spell-range` (Guiding Bolt "120 ft.", Inflict Wounds
 * "Touch"), 2026-09-10, direct owner report ("o número deve ser tamanho
 * normal e negrito e feet... devem ser menores"): the value is a bold number
 * plus a smaller unit, not one plain run. Every `range` string this app
 * actually stores fits one of four shapes — dual (`"80/320 ft."`), simple
 * (`"120 ft."`/`"5 ft."`), a bare word (`"Touch"`/`"Self"`), or `"Self"`/
 * `"Touch"` with a parenthetical area detail (`"Self (15-foot cube)"`) — so
 * this parses the existing string rather than adding a new structured field.
 * `unit` is always rendered as the literal `"ft."` regardless of what the
 * source matched — this app's own data is meant to be authored that way
 * (**2026-09-10, direct owner correction, "usar 'ft.' ao invés de 'feet'"**;
 * `feet` still matches so older/incoming data doesn't silently break) —
 * D&D Beyond's own unit label is always `"ft."` too, confirmed live.
 */
export function parseRange(range: string): ParsedRange {
  const dual = range.match(/^(\d+)\/(\d+)\s*(?:ft\.?|feet)$/i);
  if (dual) {
    return { primary: dual[1], longRange: `/${dual[2]}` };
  }
  const simple = range.match(/^(\d+)\s*(?:ft\.?|feet)$/i);
  if (simple) {
    return { primary: simple[1], unit: 'ft.' };
  }
  const withDetail = range.match(/^(Self|Touch)\s*\((.+)\)$/i);
  if (withDetail) {
    return { primary: withDetail[1], areaDetail: withDetail[2] };
  }
  return { primary: range };
}

type RangeDisplayProps = {
  range: string;
};

/**
 * The Range column only. **2026-09-10, direct owner correction:** an area
 * spell's `"(15-foot cube)"` detail used to render here, under the primary
 * value — moved out entirely, per the owner's live-confirmed observation
 * (Copy of Raya, dndbeyond.com/characters/56008841: Lightning Bolt's Range
 * column shows only "Self", its "100 ft." + line icon sit in the Notes
 * column instead, via `.ddbc-note-components`). See `SpellAreaTag` below for
 * where that detail actually goes now.
 */
export function RangeDisplay({ range }: RangeDisplayProps) {
  const parsed = parseRange(range);
  return (
    <>
      <span className="range-display__primary">{parsed.primary}</span>
      {parsed.unit && <span className="range-display__suffix"> {parsed.unit}</span>}
      {parsed.longRange && <span className="range-display__suffix">{parsed.longRange}</span>}
    </>
  );
}

const AOE_SHAPE_ICONS: Record<string, string> = {
  cone: coneIcon,
  cube: cubeIcon,
  cylinder: cylinderIcon,
  line: lineIcon,
  sphere: sphereIcon,
  square: squareIcon,
};

/**
 * DOM-measured against D&D Beyond's own `.ddbc-note-components` (Copy of
 * Raya's Ice Knife "5 ft." + sphere icon, Lightning Bolt "100 ft." + line
 * icon, Green-Flame Blade "5 ft." + sphere icon — all three confirmed live,
 * 2026-09-10): every one of these is a **secondary** area riding along with
 * the spell's own notes, in the Notes column, never the Range column, which
 * stays the plain origin point ("Self"/"Touch"/a number) — see
 * `RangeDisplay`'s own doc comment. The distance here is *not* bold (12px/
 * 400, `rgb(18,24,28)`) unlike the Range column's own bold primary — DOM-
 * confirmed a deliberately quieter treatment for a Notes-column value.
 * `dnd_icon_aoe_*.svg` (six shapes) were staged in a prior session
 * (`frames/`, `build-icon-index.ps1`'s own "staged, not yet applied"
 * category) and are wired here for the first time. Shape is read straight
 * out of `range`'s own parenthetical (e.g. "cube" from "15-foot cube") via
 * `parseRange` — no new field, same free-text-keyword-selects-an-icon
 * convention `AttackRow.tsx`'s `attackIcon()` already uses. Only the one
 * shape word this app's data currently uses ("cube", Thunderwave) is
 * exercised live; the other five are wired and ready the moment a spell
 * with that shape exists.
 */
export function SpellAreaTag({ range }: { range: string }) {
  const { areaDetail } = parseRange(range);
  if (!areaDetail) {
    return null;
  }
  const match = areaDetail.match(/^(\d+)-foot\s+(\w+)$/i);
  if (!match) {
    return null;
  }
  const [, distance, shape] = match;
  const icon = AOE_SHAPE_ICONS[shape.toLowerCase()];
  return (
    <span className="spell-area-tag">
      <span className="spell-area-tag__distance">{distance}</span>
      <span className="spell-area-tag__unit"> ft.</span>
      {icon && <FrameIcon className="spell-area-tag__icon" svg={icon} title={shape} aria-hidden />}
    </span>
  );
}

import type { AttackRow as AttackRowData } from '../../sheet/api';
import { ListRow } from '../../sheet/ListRow';
import { DAMAGE_TYPE_ICONS, damageTypeLabel } from './damageTypes';
import { FrameIcon } from './FrameIcon';
import meleeIcon from './frames/dnd_icon_attack_melee.svg?raw';
import rangedIcon from './frames/dnd_icon_attack_ranged.svg?raw';
import spellIcon from './frames/dnd_icon_attack_spell.svg?raw';
import unarmedIcon from './frames/dnd_icon_attack_unarmed.svg?raw';
import { RangeDisplay } from './RangeDisplay';

type AttackRowProps = {
  attack: AttackRowData;
  unarmed?: boolean;
  onRollHit: () => void;
  onRollHitContextMenu: (x: number, y: number) => void;
  onRollDamage: () => void;
  /** Rolls a versatile weapon's two-handed damage. */
  onRollVersatileDamage?: () => void;
  onOpenDetail: () => void;
};

function diceLabel(diceCount: number, diceSides: number, modifier: number): string {
  return `${diceCount}d${diceSides}${modifier !== 0 ? formatModifier(modifier) : ''}`;
}

function formatModifier(modifier: number): string {
  return modifier >= 0 ? `+${modifier}` : `−${Math.abs(modifier)}`;
}

/** Icon by attack category (falls back to melee). game-icons.net art, CC BY 3.0 — attribution tracked in systems/dnd-5e/sheet-build.md's "Attack row" entry. */
function attackIcon(category: string, unarmed: boolean): string {
  const normalized = category.toLowerCase();
  if (unarmed || normalized.includes('unarmed')) {
    return unarmedIcon;
  }
  if (normalized.includes('spell')) {
    return spellIcon;
  }
  if (normalized.includes('ranged')) {
    return rangedIcon;
  }
  return meleeIcon;
}

/**
 * No frame asset — plain flex row (see SkillRow.tsx). The whole row opens
 * Entity Detail (`ListRow`); Hit and Damage are roll targets that stop
 * propagation so they don't also trigger it. A versatile weapon stacks a smaller two-handed damage button under the
 * one-handed one, as D&D Beyond does.
 */
export function AttackRow({
  attack,
  unarmed = false,
  onRollHit,
  onRollHitContextMenu,
  onRollDamage,
  onRollVersatileDamage,
  onOpenDetail,
}: AttackRowProps) {
  const hitLabel = formatModifier(attack.toHit.value);
  // 0 dice (e.g. Unarmed Strike): flat modifier only, not "0d1+3".
  const damageLabel =
    attack.damageDiceCount > 0
      ? diceLabel(attack.damageDiceCount, attack.damageDiceSides, attack.damageModifier)
      : formatModifier(attack.damageModifier);
  const versatileLabel =
    attack.versatileDiceCount != null && attack.versatileDiceSides != null
      ? diceLabel(attack.versatileDiceCount, attack.versatileDiceSides, attack.damageModifier)
      : null;

  return (
    <ListRow className="action-row reveal" ariaLabel={`Open ${attack.name} details`} onOpen={onOpenDetail}>
      <FrameIcon className="action-row__icon" svg={attackIcon(attack.category, unarmed)} aria-hidden />
      <div className="action-row__name-cell">
        <span className="reveal action-row__name">{attack.name}</span>
        <div className="action-row__category">{attack.category}</div>
      </div>
      <div className="action-row__range">
        <RangeDisplay range={attack.range} />
      </div>
      <div className="action-row__value-cell action-row__value-cell--prominent">
        <button
          type="button"
          className="roll action-row__value action-row__value--prominent"
          aria-label={`Roll ${attack.name} attack, ${hitLabel}`}
          onClick={(event) => {
            event.stopPropagation();
            onRollHit();
          }}
          onContextMenu={(event) => {
            event.preventDefault();
            event.stopPropagation();
            onRollHitContextMenu(event.clientX, event.clientY);
          }}
        >
          {hitLabel}
        </button>
      </div>
      <div className={`action-row__value-cell action-row__value-cell--damage${versatileLabel ? ' action-row__value-cell--versatile' : ''}`}>
        <button
          type="button"
          className="roll action-row__value action-row__value--damage"
          aria-label={`Roll ${attack.name} damage, ${damageLabel} ${attack.damageType}`}
          onClick={(event) => {
            event.stopPropagation();
            onRollDamage();
          }}
        >
          <span className="action-row__damage-value">{damageLabel}</span>
          {DAMAGE_TYPE_ICONS[attack.damageType] && (
            <FrameIcon
              className="action-row__damage-type"
              svg={DAMAGE_TYPE_ICONS[attack.damageType]}
              title={damageTypeLabel(attack.damageType)}
              aria-hidden
            />
          )}
        </button>
        {versatileLabel && onRollVersatileDamage && (
          <button
            type="button"
            className="roll action-row__value action-row__value--damage action-row__value--versatile"
            aria-label={`Roll ${attack.name} two-handed damage, ${versatileLabel} ${attack.damageType}`}
            title="Two-handed"
            onClick={(event) => {
              event.stopPropagation();
              onRollVersatileDamage();
            }}
          >
            <span className="action-row__damage-value">{versatileLabel}</span>
          </button>
        )}
      </div>
      {attack.notes && <div className="action-row__notes">{attack.notes}</div>}
    </ListRow>
  );
}

import type { RollHandler } from '../../dice/api';
import type { CharacterSheet, MutationHandler, Spell } from '../../sheet/api';
import { DAMAGE_TYPE_ICONS, damageTypeLabel } from './damageTypes';
import { FrameIcon } from './FrameIcon';

type ItemSpellCastProps = {
  spell: Spell;
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onRoll: RollHandler;
};

/**
 * The Wand of Fireballs mechanic's own Entity Detail action bar — systems/dnd-5e/features/
 * inventory-equipment-mechanics.md's slice 7. Sibling to `SpellCast.tsx` (a
 * normal leveled spell's own slot-based cast control), reusing its exact
 * `.spell-cast` layout classes: no new CSS needed, just a charge cost/remaining
 * pair in place of the level stepper, since upcasting by spending extra
 * charges (the real PHB rule) is not modeled — a named simplification, see
 * `Dnd5eSpell`'s own doc comment. Never rendered for a normal class-known
 * spell (`spell.grantedByItemKey == null`) — `spellDetail.tsx`'s
 * `castActionBar` picks this component over `SpellCast` based on exactly
 * that field.
 */
export function ItemSpellCast({ spell, sheet, onMutate, onRoll }: ItemSpellCastProps) {
  const item = sheet.items.find((candidate) => candidate.key === spell.grantedByItemKey);
  if (!item || spell.chargeCost == null) {
    return null;
  }

  const remaining = (item.charges ?? 0) - item.chargesUsed;
  const damageLabel = spell.damageDiceCount != null && spell.damageDiceSides != null
    ? `${spell.damageDiceCount}d${spell.damageDiceSides}`
    : null;

  const handleCast = () => {
    onMutate({ type: 'CAST_ITEM_GRANTED_SPELL', spellKey: spell.key });
    if (spell.attackRoll) {
      onRoll('SPELL_ATTACK', spell.key);
    } else if (damageLabel) {
      onRoll('SPELL_DAMAGE', spell.key);
    }
  };

  return (
    <div className="spell-cast">
      <div className="spell-cast__top-row">
        <button
          type="button"
          className="mutate spell-cast__button"
          disabled={remaining < spell.chargeCost}
          aria-label={`Cast ${spell.name} using ${spell.chargeCost} charge${spell.chargeCost === 1 ? '' : 's'}`}
          onClick={handleCast}
        >
          <span className="spell-cast__remaining">{remaining}</span>
          Cast
        </button>
        <div className="spell-cast__level">
          <span className="spell-cast__level-label">Cost</span>
          <span className="spell-cast__level-current">
            {spell.chargeCost} Charge{spell.chargeCost === 1 ? '' : 's'}
          </span>
        </div>
      </div>
      {damageLabel && (
        <div className="spell-cast__preview">
          <span className="spell-cast__preview-amount">{damageLabel}</span>
          {spell.damageType && DAMAGE_TYPE_ICONS[spell.damageType] && (
            <FrameIcon
              className="spell-cast__preview-icon"
              svg={DAMAGE_TYPE_ICONS[spell.damageType]}
              title={damageTypeLabel(spell.damageType)}
              aria-hidden
            />
          )}
          <span className="spell-cast__preview-label">Damage</span>
        </div>
      )}
    </div>
  );
}

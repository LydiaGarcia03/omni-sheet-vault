import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import { ListRow } from '../../sheet/ListRow';
import {
  adjustedSpellAttack,
  adjustedSpellSaveDc,
  type CharacterSheet,
  type EntityDetailHandler,
  type MutationHandler,
  type Spell,
} from '../../sheet/api';
import { ABILITIES, abilityAbbreviation } from './abilities';
import { DAMAGE_TYPE_ICONS, damageTypeLabel } from './damageTypes';
import { FrameIcon } from './FrameIcon';
import { RangeDisplay, SpellAreaTag } from './RangeDisplay';
import { levelLabel } from './SpellCast';
import { spellDamageLabel } from './spellCombat';
import { buildSpellDetailRequest } from './spellDetail';
import { SCHOOL_ICONS } from './spellSchools';

type SpellAttackRowProps = {
  spell: Spell;
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
};

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/**
 * A spell that deals damage (`isCombatSpell`, `spellCombat.ts`), rendered in
 * the very same `.action-row` shape `AttackRow.tsx` uses for weapons — direct
 * owner request, 2026-09-03, confirmed live against D&D Beyond: Helga
 * Flinthand's (dndbeyond.com/characters/50149479) Guiding Bolt and Inflict
 * Wounds, and Copy of Raya's (.../characters/56008841) Fire Bolt, Ray of
 * Frost and Ice Knife all sit in the same attack table as the weapon rows
 * below/above them, not a separate list — same Attack/Range/Hit-DC/Damage/
 * Notes columns, just a school icon (`SCHOOL_ICONS`, keyed off `Spell.school`)
 * instead of a weapon-category one, and an italic name with a "Level • Class"
 * subtitle (`.action-row__category`) instead of "Melee/Ranged Weapon" —
 * confirmed live D&D Beyond italicizes every spell name in this table, never
 * a weapon's. Only this app's own two combat shapes are handled: an
 * attack-roll spell shows its roll trigger under Hit/DC and damage under
 * Damage; a save-based damage spell (`attackRoll` false, damage present)
 * shows the class's flat save DC under Hit/DC (plain text — the target rolls
 * the save, not the caster) and its damage roll under Damage. The name
 * reopens the same Entity Detail panel (`buildSpellDetailRequest`) the Spells
 * tab's own row opens, so casting a leveled spell works identically from
 * either tab.
 *
 * **2026-09-04, direct owner request, same pass as `AttackRow.tsx`:** a
 * damage roll also shows a small `DAMAGE_TYPE_ICONS` glyph next to its dice
 * (`damageTypes.ts`), `title`-tooltipped with the type name on hover.
 *
 * **Corrected 2026-09-20**: `isCombatSpell` no longer includes healing (see
 * its own doc comment — D&D Beyond's real Actions tab never shows one), so
 * this row can no longer receive a healing spell; the healing-specific
 * rendering branch this comment used to describe (a 2026-09-10 addition,
 * `dnd_icon_healing.svg`, `SPELL_HEAL`) was dead code once that fix landed
 * and has been removed. A healing spell's own cast/roll still works from the
 * Spells tab (`SpellRow.tsx`), unaffected — that path never depended on this
 * row.
 *
 * **Same day, direct owner report ("todos os elementos devem ser
 * encapsulados por uma div"):** the Hit/DC and Damage cells (button or
 * static value, either way) now sit inside a `.action-row__value-cell` div
 * that owns the column's width — see `AttackRow.tsx`'s own doc comment,
 * same fix, same reasoning. **Corrected 2026-09-15:** that div was actually
 * still a `<span>` until this date — flexbox's own blockification of span
 * children made the mistake invisible, since a `<span>` and a `<div>` render
 * identically once forced block-level as a flex item. Every non-interactive
 * column wrapper in this row (icon, category, range, both value-cells,
 * notes) is a real `<div>` now, matching D&D Beyond's own
 * `.ddbc-combat-attack__*` column markup; only genuine inline text runs
 * inside a button (`.action-row__damage-value`/`__damage-type`) stay `<span>`.
 */
export function SpellAttackRow({ spell, sheet, onRoll, onRollContextMenu, onMutate, onOpenDetail }: SpellAttackRowProps) {
  const spellcasting = sheet.spellcasting.find((info) => info.className === spell.className);
  const diceLabel = spellDamageLabel(spell);
  const hasDice = diceLabel != null;
  const isSaveBasedDamage = !spell.attackRoll && hasDice;
  const saveDc = adjustedSpellSaveDc(spell, spellcasting ? spellcasting.spellSaveDc.value : spell.fixedSaveDc);
  const attackBonus = spellcasting ? adjustedSpellAttack(spell, spellcasting.spellAttackBonus.value) : null;

  return (
    <ListRow
      className="action-row reveal"
      ariaLabel={`Open ${spell.name} details`}
      onOpen={() => onOpenDetail(buildSpellDetailRequest(spell, sheet, onMutate, onRoll))}
    >
      <FrameIcon className="action-row__icon" svg={SCHOOL_ICONS[spell.school]} aria-hidden />
      <div className="action-row__name-cell">
        <span className="reveal action-row__name action-row__name--spell">{spell.name}</span>
        <div className="action-row__category">
          {levelLabel(spell.level)} • {spell.className}
        </div>
      </div>
      <div className="action-row__range">
        <RangeDisplay range={spell.range} />
      </div>
      <div className="action-row__value-cell action-row__value-cell--prominent">
        {spell.attackRoll && attackBonus != null ? (
          <button
            type="button"
            className="roll action-row__value action-row__value--prominent"
            aria-label={`Roll ${spell.name} attack, ${formatSigned(attackBonus)}`}
            onClick={(event) => {
              event.stopPropagation();
              onRoll('SPELL_ATTACK', spell.key);
            }}
            onContextMenu={(event) => {
              event.preventDefault();
              event.stopPropagation();
              onRollContextMenu(event.clientX, event.clientY, 'SPELL_ATTACK', spell.key, formatSigned(attackBonus));
            }}
          >
            {formatSigned(attackBonus)}
          </button>
        ) : isSaveBasedDamage && saveDc != null ? (
          <div
            className="action-row__value action-row__value--prominent action-row__value--static action-row__value--dc"
            title={`Save DC, ${ABILITIES.find((ability) => ability.key === spell.saveAbility)?.label ?? spell.saveAbility ?? spellcasting?.spellcastingAbility}`}
          >
            <span className="action-row__dc-value">{saveDc}</span>
            <span className="action-row__dc-label">
              {abilityAbbreviation(spell.saveAbility ?? spellcasting?.spellcastingAbility ?? '')}
            </span>
          </div>
        ) : (
          <div className="action-row__value action-row__value--prominent action-row__value--static">–</div>
        )}
      </div>
      <div className="action-row__value-cell action-row__value-cell--damage">
        {hasDice ? (
          <button
            type="button"
            className="roll action-row__value action-row__value--damage"
            aria-label={`Roll ${spell.name} damage, ${diceLabel} ${spell.damageType}`}
            onClick={(event) => {
              event.stopPropagation();
              onRoll('SPELL_DAMAGE', spell.key);
            }}
          >
            <span className="action-row__damage-value">{diceLabel}</span>
            {spell.damageType && DAMAGE_TYPE_ICONS[spell.damageType] && (
              <FrameIcon
                className="action-row__damage-type"
                svg={DAMAGE_TYPE_ICONS[spell.damageType]}
                title={damageTypeLabel(spell.damageType)}
                aria-hidden
              />
            )}
          </button>
        ) : (
          <div className="action-row__value action-row__value--damage action-row__value--static">{spell.effectSummary}</div>
        )}
      </div>
      {spell.notes && (
        <div className="action-row__notes">
          <SpellAreaTag range={spell.range} />
          {spell.notes}
        </div>
      )}
    </ListRow>
  );
}

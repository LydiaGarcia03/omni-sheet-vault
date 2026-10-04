import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import { ListRow } from '../../sheet/ListRow';
import { adjustedSpellAttack, adjustedSpellSaveDc, type Spell, type SpellcastingClassInfo } from '../../sheet/api';
import { abilityAbbreviation } from './abilities';
import { DAMAGE_TYPE_ICONS, damageTypeLabel } from './damageTypes';
import { FrameIcon } from './FrameIcon';
import healingIcon from './frames/dnd_icon_healing.svg?raw';
import concentrationIcon from './frames/dnd_icon_marker_concentration.svg?raw';
import ritualIcon from './frames/dnd_icon_marker_ritual.svg?raw';
import { RangeDisplay, SpellAreaTag } from './RangeDisplay';
import { isHealingSpell, spellDamageLabel } from './spellCombat';

export { schoolLabel } from './spellSchools';

type SpellRowProps = {
  spell: Spell;
  spellcasting: SpellcastingClassInfo | undefined;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onOpenDetail: () => void;
  /** A higher level the spell is listed at (a Pact Magic level): its effect is scaled and its own level marked. */
  castLevel?: number;
};

const ORDINAL_SUFFIXES: Record<number, string> = { 1: 'st', 2: 'nd', 3: 'rd' };

/** D&D Beyond's blue badge with the spell's own level, on the Cast button of a row listed at a higher level. */
function ScaledLevelBadge({ level }: { level: number }) {
  return (
    <span className="spell-row__scaled" aria-label={`Level ${level} spell`}>
      <span className="spell-row__scaled-number">{level}</span>
      <span className="spell-row__scaled-ordinal">{ORDINAL_SUFFIXES[level] ?? 'th'}</span>
    </span>
  );
}

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/**
 * D&D Beyond's own Time column shows a compact abbreviation ("1A", "1m"), not
 * the full casting time text — DOM-confirmed against dndbeyond.com/characters/
 * 50149479 (`ActivationUtils.renderCastingTimeAbbreviation` in its own source).
 * `Dnd5eSpell.castingTime` stores the full text ("1 Action"); this is a
 * display-only shortening, not a new stored field.
 */
function castingTimeAbbreviation(castingTime: string): string {
  const match = castingTime.match(/^(\d+)\s+(.+)$/);
  if (!match) {
    return castingTime;
  }
  const [, count, unit] = match;
  const normalized = unit.toLowerCase();
  if (normalized.startsWith('bonus action')) return `${count}BA`;
  if (normalized.startsWith('action')) return `${count}A`;
  if (normalized.startsWith('reaction')) return `${count}R`;
  if (normalized.startsWith('minute')) return `${count}m`;
  if (normalized.startsWith('hour')) return `${count}h`;
  return castingTime;
}

/**
 * No frame asset — same reasoning as AttackRow. The name opens the Entity Detail
 * mold; concentration and ritual are marked right after the name, per phase 10's
 * live verification against D&D Beyond (slice 8) — its own row places these
 * markers there too, not as a separate column. Time/Range/Notes are plain text
 * (`Dnd5eSpell`'s own doc comment explains the split from `description`). Hit/DC
 * and Effect are derived, not stored fields — confirmed live this app's current
 * spell set never needs a save DC without also having a damage roll: an attack-
 * roll spell shows its roll trigger under Hit/DC and its damage under Effect; a
 * save-based damage spell (`attackRoll` false, damage present, e.g. Sacred Flame)
 * shows the class's flat save DC under Hit/DC (plain text — the target rolls the
 * save, not the caster) and its damage roll under Effect; anything with neither
 * shows a dash under Hit/DC and `effectSummary` under Effect. A healing spell
 * (`effectSummary === 'Healing'`) never shows a save DC either — healing has no
 * save — and rolls via `SPELL_HEAL`, not `SPELL_DAMAGE`.
 *
 * **Corrected 2026-09-03, direct owner report:** the leading column briefly
 * carried the school-of-magic icon (same-day earlier commit) — the owner
 * clarified that icon belongs only to the Actions tab's `SpellAttackRow`.
 * This tab's own leading column (`.spell-row__cast`) is a Cast/At Will/As
 * Ritual indicator instead, DOM-measured against D&D Beyond's own Spells tab
 * (Helga Flinthand, Copy of Raya): a cantrip (`level === 0`) shows a plain
 * "AT WILL" label, a ritual spell shows "AS RITUAL", and everything else
 * gets a real "CAST" button. Simplified relative to D&D Beyond, which shows
 * "AS RITUAL" only for a ritual spell that also isn't currently prepared
 * (letting a prepared ritual spell still show "CAST") — this app has no
 * per-spell casting-method choice, so ritual takes priority outright rather
 * than depending on `prepared`, a documented simplification. A feature-granted
 * spell cast without a slot reads "AT WILL" or gets a "USE" button instead. The Cast button
 * reuses the same `onOpenDetail` the name already opens (same `SpellCast`
 * action bar), rather than duplicating the slot-level picker inline.
 *
 * **2026-09-04, direct owner request, same pass as `AttackRow.tsx`/
 * `SpellAttackRow.tsx`:** a damage roll's visible `spell.damageType` text was
 * replaced with a `DAMAGE_TYPE_ICONS` glyph (`damageTypes.ts`), `title`-
 * tooltipped with the type name on hover.
 *
 * **2026-09-10, direct owner report:** a healing roll used to get no glyph at
 * all (`damageType` is null for healing, so it fell through the damage-type
 * lookup). D&D Beyond's own Spells tab renders a heart in the exact same
 * Effect-column slot for a healing spell — DOM-confirmed live against
 * dndbeyond.com/characters/50149479's Cure Wounds row (`ddbc-healing-icon__icon`,
 * fill `#242528`, the same solid tone `.action-row__damage-type` already
 * applies via `--text-primary`). `dnd_icon_healing.svg` is that glyph's own
 * extracted path, same precedent as `dnd_icon_attunement.svg`/
 * `dnd_icon_settings.svg` (changelog.md, 2026-09-10) — a generic UI glyph,
 * not the game-content artwork ground-rules.md's "own assets" stance guards
 * (spell markers, damage types, dice faces).
 *
 * **2026-09-04, direct owner report ("os símbolos de concentração e ritual
 * não são respeitados e são comidos"):** the C/R markers used to be flex
 * siblings of the name button inside a fixed-width, non-wrapping row —
 * exactly the D&D Beyond bug fix a general re-verification pass had
 * confirmed as a mismatch: D&D Beyond's own name column (135px,
 * `white-space: normal`, DOM-measured against Copy of Raya's "Protection
 * from Evil and Good" and "Drawmij's Instant Summons", both long enough to
 * wrap) wraps a long name onto multiple lines with the marker riding along
 * inline at the end of the text, never clipped. Moving the markers *inside*
 * the name button (trailing children, not siblings) gets the same result:
 * `white-space: normal` on `.spell-row__name` now wraps button text and
 * marker together as one inline run.
 *
 * **2026-09-04, direct owner request ("tente desenhar os ícones de losango
 * e livro fechado"):** the C/R markers were a plain circular letter badge;
 * replaced with the owner's own asked-for shapes — a diamond for
 * Concentration, a closed book (spine + cover, not D&D Beyond's scroll) for
 * Ritual — as this app's own original artwork (`dnd_icon_marker_
 * concentration.svg`/`dnd_icon_marker_ritual.svg`), not traced from D&D
 * Beyond's own proprietary icon set (ground-rules.md: visual references
 * guide our own assets, never copied outright). Each is a `currentColor`
 * silhouette (tints with `.spell-row__marker`'s own `color`, same
 * `FrameIcon` convention every other icon in this file uses) with the
 * letter rendered as a real SVG `<text>` node in a fixed white, the same
 * filled-shape-plus-reversed-letter contrast D&D Beyond's own marker uses,
 * just this app's own geometry.
 */
export function SpellRow({ spell, spellcasting, onRoll, onRollContextMenu, onOpenDetail, castLevel }: SpellRowProps) {
  const scaled = castLevel != null && castLevel > spell.level;
  const damageLabel = spellDamageLabel(spell, scaled ? castLevel : undefined);
  const isHealing = isHealingSpell(spell);
  const isSaveBasedDamage = !spell.attackRoll && !isHealing && damageLabel != null;
  const saveDc = adjustedSpellSaveDc(spell, spellcasting ? spellcasting.spellSaveDc.value : spell.fixedSaveDc);
  const attackBonus = spellcasting ? adjustedSpellAttack(spell, spellcasting.spellAttackBonus.value) : null;

  return (
    <ListRow className="spell-row reveal" ariaLabel={`Open ${spell.name} details`} onOpen={onOpenDetail}>
      <div className="spell-row__cast">
        {spell.usage?.mode === 'AT_WILL' ? (
          <span className="spell-row__cast-label">
            At
            <br />
            Will
          </span>
        ) : spell.usage?.mode === 'LIMITED' ? (
          <button
            type="button"
            className="mutate spell-row__cast-button"
            onClick={(event) => {
              event.stopPropagation();
              onOpenDetail();
            }}
          >
            Use
          </button>
        ) : spell.ritual ? (
          <span className="spell-row__cast-label">
            As
            <br />
            Ritual
          </span>
        ) : spell.level === 0 ? (
          <span className="spell-row__cast-label">
            At
            <br />
            Will
          </span>
        ) : (
          <button
            type="button"
            className="mutate spell-row__cast-button"
            onClick={(event) => {
              event.stopPropagation();
              onOpenDetail();
            }}
          >
            {scaled && <ScaledLevelBadge level={spell.level} />}
            Cast
          </button>
        )}
      </div>
      <div className="spell-row__name-cell">
        <span className="spell-row__name">
          {spell.name}
          {spell.concentration && (
            <FrameIcon className="spell-row__marker" svg={concentrationIcon} aria-label="Concentration" title="Concentration" />
          )}
          {spell.ritual && <FrameIcon className="spell-row__marker" svg={ritualIcon} aria-label="Ritual" title="Ritual" />}
        </span>
      </div>
      <div className="spell-row__time" title={spell.castingTime}>
        {castingTimeAbbreviation(spell.castingTime)}
      </div>
      <div className="spell-row__range">
        <RangeDisplay range={spell.range} />
      </div>
      <div className="spell-row__hit-dc">
        {spell.attackRoll && attackBonus != null && (
          <button
            type="button"
            className="roll action-row__value spell-row__value--tohit"
            aria-label={`Roll ${spell.name} spell attack`}
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
        )}
        {!spell.attackRoll && isSaveBasedDamage && saveDc != null && (
          <div className="spell-row__save">
            <span className="spell-row__save-label">
              {abilityAbbreviation(spell.saveAbility ?? spellcasting?.spellcastingAbility ?? '')}
            </span>
            <span className="spell-row__save-value">{saveDc}</span>
          </div>
        )}
        {!spell.attackRoll && (!isSaveBasedDamage || saveDc == null) && '–'}
      </div>
      <div className="spell-row__effect">
        {damageLabel ? (
          <button
            type="button"
            className="roll action-row__value spell-row__value--effect"
            aria-label={isHealing ? `Roll ${spell.name} healing, ${damageLabel}` : `Roll ${spell.name} damage, ${damageLabel} ${spell.damageType}`}
            onClick={(event) => {
              event.stopPropagation();
              onRoll(isHealing ? 'SPELL_HEAL' : 'SPELL_DAMAGE', spell.key, scaled ? castLevel : undefined);
            }}
          >
            <span className="action-row__damage-value">{damageLabel}</span>
            {isHealing ? (
              <FrameIcon className="action-row__damage-type" svg={healingIcon} title="Healing" aria-hidden />
            ) : (
              spell.damageType &&
              DAMAGE_TYPE_ICONS[spell.damageType] && (
                <FrameIcon
                  className="action-row__damage-type"
                  svg={DAMAGE_TYPE_ICONS[spell.damageType]}
                  title={damageTypeLabel(spell.damageType)}
                  aria-hidden
                />
              )
            )}
          </button>
        ) : (
          spell.effectSummary
        )}
      </div>
      <div className="spell-row__notes">
        <SpellAreaTag range={spell.range} />
        {spell.notes}
      </div>
    </ListRow>
  );
}

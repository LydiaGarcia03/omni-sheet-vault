import { useState } from 'react';
import type { RollHandler } from '../../dice/api';
import type { MutationHandler, Spell, SpellSlotLevel, SpellUsage } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { DAMAGE_TYPE_ICONS } from './damageTypes';
import { FrameIcon } from './FrameIcon';
import healingIcon from './frames/dnd_icon_healing.svg?raw';
import { useHasLastingEffect } from './lastingEffectSpells';
import { ENGLISH_TERMS, useDnd5eTerms, type Dnd5eTerms } from './locales/dnd5eMessages';
import { isHealingSpell, scaleDiceCount } from './spellCombat';

type SpellCastProps = {
  spell: Spell;
  spellSlots: SpellSlotLevel[];
  onMutate: MutationHandler;
  onRoll: RollHandler;
};

export function levelLabel(level: number, terms: Dnd5eTerms = ENGLISH_TERMS): string {
  return level === 0 ? terms.t('terms.labels.cantrip') : terms.t('spell.levelLabel', { ordinal: terms.ordinal(level) });
}

/**
 * Short form for the level filter chip — D&D Beyond's own `TabFilter` labels
 * each level tab with just an abbreviation, keeping `levelLabel`'s full text
 * for the section heading above the level's own spell list instead.
 */
export function levelAbbreviation(level: number, terms: Dnd5eTerms = ENGLISH_TERMS): string {
  return level === 0 ? terms.t('terms.labels.cantrip') : terms.ordinal(level);
}

/** A cast button with its remaining-uses badge, as D&D Beyond's ct-theme-button with its count. */
function CastButton({ label, remaining, ariaLabel, onCast }: { label: string; remaining: number; ariaLabel: string; onCast: () => void }) {
  return (
    <button type="button" className="mutate spell-cast__button" disabled={remaining <= 0} aria-label={ariaLabel} onClick={onCast}>
      <span className="spell-cast__remaining">{remaining}</span>
      {label}
    </button>
  );
}

/**
 * No frame asset (component inventory: "Not built" until this slice). Entity
 * Detail action bar for a leveled spell — systems/dnd-5e/sheet-ui.md's Spells tab spec:
 * "Casting from the detail panel allows choosing a slot level; the slot
 * consumed is the chosen level, not the spell's base level" (upcasting).
 * Cantrips never reach this component — SpellsTab only builds it for
 * `spell.level > 0`. Casting spends the chosen slot and, for a spell with a
 * lasting effect (Mage Armor), starts it as an active effect on the server; such
 * a spell also gets a Self/Ally target choice, since only a self-cast effect
 * changes the character's own values.
 *
 * Layout matches D&D Beyond's own `.ct-spell-caster`: a `−`/`+` stepper for
 * the level (not a `<select>`) beside a Cast button carrying the
 * remaining-slots count as its own small badge, plus a live damage/healing
 * preview below that recomputes as the level changes.
 *
 * Deliberately goes further than D&D Beyond: their own Cast button only
 * logs "cast at level N" and leaves the preview number inert — the player
 * has to work out upcast damage by hand. This app's whole premise is active
 * mechanics, not static numbers — so Cast here also rolls: the spell's
 * attack roll if it has one (damage is a separate, deliberate follow-up
 * once a hit is confirmed, same as a weapon attack), else its
 * damage/healing roll, already scaled to the chosen level. The row's own
 * quick-roll target is untouched — always the base level, a manual
 * shortcut that needs no slot.
 *
 * As on D&D Beyond, the level stepper steps through every level with a pool, the
 * Pact Magic one included; at a level the button reads "Spell Slot" or "Pact Slot"
 * after the pool it spends (both when a regular and the pact pool share it).
 */

/** D&D Beyond's limited-use line under the Cast control, e.g. "Once per Long Rest". */
export function limitedUseLabel(usage: SpellUsage, terms: Dnd5eTerms = ENGLISH_TERMS): string {
  const { t } = terms;
  const count = usage.maxUses === 1 ? t('spell.once') : usage.maxUses === 2 ? t('spell.twice') : t('spell.times', { count: usage.maxUses });
  const rest =
    usage.recharge === 'LONG_REST' ? t('terms.labels.longRest') : usage.recharge === 'SHORT_OR_LONG_REST' ? t('terms.labels.shortRest') : null;
  return rest ? t('spell.perRest', { count, rest }) : count;
}

export function SpellCast(props: SpellCastProps) {
  return props.spell.usage ? <SlotlessSpellCast {...props} usage={props.spell.usage} /> : <SlotSpellCast {...props} />;
}

/**
 * A feature-granted spell cast without a slot (D&D Beyond's `.ct-spell-caster` for an innate spell): "At Will", or a
 * "Use" button while uses remain, at a fixed level with no stepper, and for a limited spell its frequency below.
 */
function SlotlessSpellCast({ spell, onMutate, onRoll, usage }: SpellCastProps & { usage: SpellUsage }) {
  const [target, setTarget] = useState<'self' | 'ally'>('self');
  const hasLastingEffect = useHasLastingEffect(spell.key);
  const terms = useDnd5eTerms();
  const { t } = terms;
  const castLevel = usage.castLevel ?? spell.level;
  const remaining = usage.maxUses - usage.usedUses;

  const cast = () => {
    onMutate({ type: 'CAST_SPELL', spellKey: spell.key, slotLevel: 0, onSelf: usage.selfOnly || target === 'self', pact: false });
    rollOnCast(spell, castLevel, onRoll);
  };

  return (
    <div className="spell-cast">
      <div className="spell-cast__top-row">
        <span className="spell-cast__label">{t('spell.cast')}</span>
        {usage.mode === 'AT_WILL' ? (
          <button type="button" className="mutate spell-cast__button" aria-label={t('spell.castAtWill', { name: spell.name })} onClick={cast}>
            {t('spell.atWill')}
          </button>
        ) : (
          <button
            type="button"
            className="mutate spell-cast__button spell-cast__button--limited"
            disabled={remaining <= 0}
            aria-label={t('spell.useAria', { name: spell.name, remaining, max: usage.maxUses })}
            onClick={cast}
          >
            {t('spell.use')}
          </button>
        )}
        <div className="spell-cast__level">
          <span className="spell-cast__label">{t('spell.level')}</span>
          <span className="spell-cast__level-current spell-cast__level-current--fixed">{levelAbbreviation(castLevel, terms)}</span>
        </div>
      </div>
      {usage.mode === 'LIMITED' && <div className="spell-cast__limited">{limitedUseLabel(usage, terms)}</div>}
      {hasLastingEffect && !usage.selfOnly && <TargetChoice target={target} onSelect={setTarget} />}
      <CastPreview spell={spell} level={castLevel} />
    </div>
  );
}

function rollOnCast(spell: Spell, level: number, onRoll: RollHandler) {
  if (spell.attackRoll) {
    onRoll('SPELL_ATTACK', spell.key);
  } else if (scaleDiceCount(spell, level) != null && spell.damageDiceSides != null) {
    onRoll(isHealingSpell(spell) ? 'SPELL_HEAL' : 'SPELL_DAMAGE', spell.key, level);
  }
}

function TargetChoice({ target, onSelect }: { target: 'self' | 'ally'; onSelect: (target: 'self' | 'ally') => void }) {
  const { t } = useDnd5eTerms();
  return (
    <div className="spell-cast__target">
      <span className="spell-cast__label">{t('spell.target')}</span>
      <FilterChips
        chips={[
          { id: 'self', label: t('spell.self'), ariaLabel: t('spell.castOnSelf') },
          { id: 'ally', label: t('spell.ally'), ariaLabel: t('spell.castOnAlly') },
        ]}
        activeChip={target}
        onSelect={(id) => onSelect(id as 'self' | 'ally')}
      />
    </div>
  );
}

function CastPreview({ spell, level }: { spell: Spell; level: number }) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const isHealing = isHealingSpell(spell);
  const scaledDiceCount = scaleDiceCount(spell, level);
  if (scaledDiceCount == null || spell.damageDiceSides == null) {
    return null;
  }
  return (
    <div className="spell-cast__preview">
      <span className="spell-cast__preview-amount">{`${scaledDiceCount}d${spell.damageDiceSides}`}</span>
      {isHealing ? (
        <FrameIcon className="spell-cast__preview-icon" svg={healingIcon} title={t('spell.healing')} aria-hidden />
      ) : (
        spell.damageType &&
        DAMAGE_TYPE_ICONS[spell.damageType] && (
          <FrameIcon
            className="spell-cast__preview-icon"
            svg={DAMAGE_TYPE_ICONS[spell.damageType]}
            title={terms.damageType(spell.damageType)}
            aria-hidden
          />
        )
      )}
      <span className="spell-cast__preview-label">{isHealing ? t('spell.healing') : t('spell.damage')}</span>
    </div>
  );
}

function SlotSpellCast({ spell, spellSlots, onMutate, onRoll }: SpellCastProps) {
  const eligible = spellSlots.filter((slot) => slot.level >= spell.level);
  const levels = [...new Set(eligible.map((slot) => slot.level))].sort((a, b) => a - b);
  const [selectedLevel, setSelectedLevel] = useState(levels[0] ?? spell.level);
  const [target, setTarget] = useState<'self' | 'ally'>('self');
  const hasLastingEffect = useHasLastingEffect(spell.key);
  const terms = useDnd5eTerms();
  const { t } = terms;

  if (levels.length === 0) {
    return null;
  }

  const displayedLevel = levels.includes(selectedLevel) ? selectedLevel : levels[0];
  const levelIndex = levels.indexOf(displayedLevel);
  const selectedSlot = eligible.find((slot) => !slot.pact && slot.level === displayedLevel);
  const pactSlot = eligible.find((slot) => slot.pact && slot.level === displayedLevel);

  const cast = (slotLevel: number, pact: boolean) => {
    onMutate({ type: 'CAST_SPELL', spellKey: spell.key, slotLevel, onSelf: target === 'self', pact });
    rollOnCast(spell, slotLevel, onRoll);
  };

  return (
    <div className="spell-cast">
      <div className="spell-cast__top-row">
        <span className="spell-cast__label">{t('spell.cast')}</span>
        {selectedSlot && (
          <CastButton
            label={t('terms.labels.spellSlot')}
            remaining={selectedSlot.maxSlots - selectedSlot.usedSlots}
            ariaLabel={t('spell.castSlot', { name: spell.name, level: levelLabel(selectedSlot.level, terms) })}
            onCast={() => cast(selectedSlot.level, false)}
          />
        )}
        {pactSlot && (
          <CastButton
            label={t('terms.labels.pactSlot')}
            remaining={pactSlot.maxSlots - pactSlot.usedSlots}
            ariaLabel={t('spell.castPact', { name: spell.name, level: levelLabel(pactSlot.level, terms) })}
            onCast={() => cast(pactSlot.level, true)}
          />
        )}
        <div className="spell-cast__level">
          <span className="spell-cast__label">{t('spell.level')}</span>
          <button
            type="button"
            className="spell-cast__level-step spell-cast__level-step--decrease"
            aria-label={t('spell.decreaseLevel')}
            disabled={levelIndex <= 0}
            onClick={() => setSelectedLevel(levels[levelIndex - 1])}
          >
            −
          </button>
          <span className="spell-cast__level-current">{levelAbbreviation(displayedLevel, terms)}</span>
          <button
            type="button"
            className="spell-cast__level-step spell-cast__level-step--increase"
            aria-label={t('spell.increaseLevel')}
            disabled={levelIndex >= levels.length - 1}
            onClick={() => setSelectedLevel(levels[levelIndex + 1])}
          >
            +
          </button>
        </div>
      </div>
      {hasLastingEffect && <TargetChoice target={target} onSelect={setTarget} />}
      <CastPreview spell={spell} level={displayedLevel} />
    </div>
  );
}

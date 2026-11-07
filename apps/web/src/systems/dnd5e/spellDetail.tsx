import type { RollHandler } from '../../dice/api';
import {
  adjustedSpellAttack,
  adjustedSpellSaveDc,
  type CharacterSheet,
  type EntityDetailMetadataEntry,
  type EntityDetailRequest,
  type MutationHandler,
  type Spell,
} from '../../sheet/api';
import { ENGLISH_TERMS, type Dnd5eTerms } from './locales/dnd5eMessages';
import { customizationsOf, EMPTY_SPELL_CUSTOMIZATION, type SpellCustomization } from './customizations';
import { EntityCustomizeEditor, type CustomizeField } from './EntityCustomize';
import { FrameIcon } from './FrameIcon';
import { ItemSpellCast } from './ItemSpellCast';
import { levelLabel, SpellCast } from './SpellCast';
import { SCHOOL_ICONS } from './spellSchools';

/**
 * D&D Beyond's own `.ct-spell-detail__level-school` swaps the order for a
 * cantrip ("Evocation Cantrip", school first) versus a leveled spell ("1st
 * Level Evocation", level first) — DOM-measured live, not a guess.
 */
function levelSchoolSubtitle(spell: Spell, terms: Dnd5eTerms): string {
  const school = terms.school(spell.school);
  return spell.level === 0
    ? terms.t('spell.subtitleCantrip', { school, cantrip: terms.t('terms.labels.cantrip') })
    : terms.t('spell.subtitleLeveled', { level: levelLabel(spell.level, terms), school });
}

/**
 * Cantrips (level 0) consume no slot, so they never get a cast action bar —
 * only the inline attack/damage/heal roll targets `SpellRow`/`SpellAttackRow`
 * already show. An item-granted spell (slice 7, `ItemSpellCast`) spends
 * charges instead of a slot regardless of its own level, cantrip or not — a
 * wand's own granted spell is never itself a cantrip in practice, but the
 * charge-cost gate applies uniformly either way.
 */
function castActionBar(spell: Spell, sheet: CharacterSheet, onMutate: MutationHandler, onRoll: RollHandler) {
  if (spell.grantedByItemKey != null) {
    return <ItemSpellCast spell={spell} sheet={sheet} onMutate={onMutate} onRoll={onRoll} />;
  }
  return spell.level > 0 ? (
    <SpellCast spell={spell} spellSlots={sheet.spellSlots} onMutate={onMutate} onRoll={onRoll} />
  ) : undefined;
}

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/**
 * 5etools' own "At Higher Levels" scaling text, appended after the base description
 * in the same paragraph flow — matches D&D Beyond's own spell detail rendering.
 * `undefined` (not an empty string) when the spell has none, so `EntityDetailPanel`
 * falls back to its own plain `description` rendering instead of this `body`.
 */
function descriptionBody(spell: Spell, terms: Dnd5eTerms) {
  if (!spell.higherLevelsDescription) {
    return undefined;
  }
  return (
    <p className="entity-detail__description">
      {spell.description} <strong>{terms.t('terms.labels.atHigherLevels')}</strong> {spell.higherLevelsDescription}
    </p>
  );
}

/**
 * D&D Beyond's own sidebar (`SpellDetail.tsx`'s `.ct-spell-detail__properties`)
 * shows Casting Time/Range/Components/Duration/Attack-or-Save — Level/School
 * live outside this list, in `levelSchoolSubtitle`'s own line, same split
 * D&D Beyond itself makes. Components shows "V, S, M" with the material text
 * as a hover tooltip, matching D&D Beyond's own `Tooltip` on its component
 * short codes, rather than spelling the material out inline the way the
 * table's own `notes` column does.
 */
function propertyMetadata(spell: Spell, sheet: CharacterSheet, terms: Dnd5eTerms): EntityDetailMetadataEntry[] {
  const { t } = terms;
  const spellcasting = sheet.spellcasting.find((info) => info.className === spell.className);
  const saveDc = adjustedSpellSaveDc(spell, spellcasting ? spellcasting.spellSaveDc.value : spell.fixedSaveDc);
  const attackOrSave: EntityDetailMetadataEntry[] = [];
  if (spell.attackRoll && spellcasting) {
    attackOrSave.push({ label: t('spell.attack'), value: formatSigned(adjustedSpellAttack(spell, spellcasting.spellAttackBonus.value)) });
  } else if (spell.saveAbility && saveDc != null) {
    attackOrSave.push({ label: t('spell.save'), value: t('spell.saveDc', { ability: terms.abilityAbbreviation(spell.saveAbility), dc: saveDc }) });
  }
  return [
    { label: t('terms.labels.castingTime'), value: spell.castingTime },
    { label: t('terms.labels.range'), value: spell.range },
    {
      label: t('terms.labels.components'),
      value: spell.materialComponent ? <span title={spell.materialComponent}>{spell.components}</span> : spell.components,
    },
    { label: t('terms.labels.duration'), value: spell.duration },
    ...attackOrSave,
  ];
}

/**
 * Extracted from `SpellsTab.tsx` (2026-09-03) so `SpellAttackRow.tsx` (the
 * Actions tab's spell rows) can open the exact same Entity Detail panel —
 * including the same live-refreshing `SpellCast` action bar — as clicking a
 * spell's name in the Spells tab, rather than duplicating this shape.
 */
function spellFields({ t }: Dnd5eTerms): CustomizeField[] {
  return [
    { key: 'toHitOverride', label: t('item.toHitOverride'), kind: 'number' },
    { key: 'toHitBonus', label: t('item.toHitBonus'), kind: 'number' },
    { key: 'damageBonus', label: t('item.damageBonus'), kind: 'number' },
    { key: 'dcOverride', label: t('spell.dcOverride'), kind: 'number' },
    { key: 'dcBonus', label: t('spell.dcBonus'), kind: 'number' },
    { key: 'displayAsAttack', label: t('item.displayAsAttack'), kind: 'checkbox' },
    { key: 'name', label: t('item.name'), kind: 'text', name: true },
    { key: 'notes', label: t('item.notes'), kind: 'text' },
  ];
}

function spellCustomize(spell: Spell, customization: SpellCustomization | undefined, onMutate: MutationHandler, terms: Dnd5eTerms) {
  return {
    customized: customization != null,
    editor: (
      <EntityCustomizeEditor
        fields={spellFields(terms)}
        values={customization ?? EMPTY_SPELL_CUSTOMIZATION}
        customized={customization != null}
        onSave={(next) => onMutate({ type: 'CUSTOMIZE', group: 'spells', target: spell.key, value: next })}
        onReset={() => onMutate({ type: 'REMOVE_CUSTOMIZATION', group: 'spells', target: spell.key })}
      />
    ),
  };
}

export function buildSpellDetailRequest(
  spell: Spell,
  sheet: CharacterSheet,
  onMutate: MutationHandler,
  onRoll: RollHandler,
  terms: Dnd5eTerms = ENGLISH_TERMS,
): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `spell:${spell.key}`,
    customize: spellCustomize(spell, customizationsOf(sheet).spells[spell.key], onMutate, terms),
    refresh: (latestSheet) => {
      const latestSpell = latestSheet.spells.find((candidate) => candidate.key === spell.key);
      return latestSpell ? buildSpellDetailRequest(latestSpell, latestSheet, onMutate, onRoll, terms) : null;
    },
    name: spell.name,
    parent: spell.className,
    icon: <FrameIcon className="entity-detail__icon" svg={SCHOOL_ICONS[spell.school]} aria-hidden />,
    subtitle: levelSchoolSubtitle(spell, terms),
    metadata: propertyMetadata(spell, sheet, terms),
    tags: [
      ...(spell.concentration ? [terms.t('terms.labels.concentration')] : []),
      ...(spell.ritual ? [terms.t('terms.labels.ritual')] : []),
    ],
    description: spell.description,
    body: descriptionBody(spell, terms),
    actionBar: castActionBar(spell, sheet, onMutate, onRoll),
    actionsPosition: 'top',
  };
}

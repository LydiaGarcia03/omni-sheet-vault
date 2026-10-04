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
import { abilityAbbreviation } from './abilities';
import { customizationsOf, EMPTY_SPELL_CUSTOMIZATION, type SpellCustomization } from './customizations';
import { EntityCustomizeEditor, type CustomizeField } from './EntityCustomize';
import { FrameIcon } from './FrameIcon';
import { ItemSpellCast } from './ItemSpellCast';
import { levelLabel, SpellCast } from './SpellCast';
import { SCHOOL_ICONS, schoolLabel } from './spellSchools';

/**
 * D&D Beyond's own `.ct-spell-detail__level-school` swaps the order for a
 * cantrip ("Evocation Cantrip", school first) versus a leveled spell ("1st
 * Level Evocation", level first) — DOM-measured live, not a guess.
 */
function levelSchoolSubtitle(spell: Spell): string {
  const level = levelLabel(spell.level);
  const school = schoolLabel(spell.school);
  return spell.level === 0 ? `${school} ${level}` : `${level} ${school}`;
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
function descriptionBody(spell: Spell) {
  if (!spell.higherLevelsDescription) {
    return undefined;
  }
  return (
    <p className="entity-detail__description">
      {spell.description} <strong>At Higher Levels.</strong> {spell.higherLevelsDescription}
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
function propertyMetadata(spell: Spell, sheet: CharacterSheet): EntityDetailMetadataEntry[] {
  const spellcasting = sheet.spellcasting.find((info) => info.className === spell.className);
  const saveDc = adjustedSpellSaveDc(spell, spellcasting ? spellcasting.spellSaveDc.value : spell.fixedSaveDc);
  const attackOrSave: EntityDetailMetadataEntry[] = [];
  if (spell.attackRoll && spellcasting) {
    attackOrSave.push({ label: 'Attack', value: formatSigned(adjustedSpellAttack(spell, spellcasting.spellAttackBonus.value)) });
  } else if (spell.saveAbility && saveDc != null) {
    attackOrSave.push({ label: 'Save', value: `${abilityAbbreviation(spell.saveAbility)} DC ${saveDc}` });
  }
  return [
    { label: 'Casting Time', value: spell.castingTime },
    { label: 'Range', value: spell.range },
    {
      label: 'Components',
      value: spell.materialComponent ? <span title={spell.materialComponent}>{spell.components}</span> : spell.components,
    },
    { label: 'Duration', value: spell.duration },
    ...attackOrSave,
  ];
}

/**
 * Extracted from `SpellsTab.tsx` (2026-09-03) so `SpellAttackRow.tsx` (the
 * Actions tab's spell rows) can open the exact same Entity Detail panel —
 * including the same live-refreshing `SpellCast` action bar — as clicking a
 * spell's name in the Spells tab, rather than duplicating this shape.
 */
const SPELL_FIELDS: CustomizeField[] = [
  { key: 'toHitOverride', label: 'To Hit Override', kind: 'number' },
  { key: 'toHitBonus', label: 'To Hit Bonus', kind: 'number' },
  { key: 'damageBonus', label: 'Damage Bonus', kind: 'number' },
  { key: 'dcOverride', label: 'DC Override', kind: 'number' },
  { key: 'dcBonus', label: 'DC Bonus', kind: 'number' },
  { key: 'displayAsAttack', label: 'Display As Attack', kind: 'checkbox' },
  { key: 'name', label: 'Name', kind: 'text', name: true },
  { key: 'notes', label: 'Notes', kind: 'text' },
];

function spellCustomize(spell: Spell, customization: SpellCustomization | undefined, onMutate: MutationHandler) {
  return {
    customized: customization != null,
    editor: (
      <EntityCustomizeEditor
        fields={SPELL_FIELDS}
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
): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `spell:${spell.key}`,
    customize: spellCustomize(spell, customizationsOf(sheet).spells[spell.key], onMutate),
    refresh: (latestSheet) => {
      const latestSpell = latestSheet.spells.find((candidate) => candidate.key === spell.key);
      return latestSpell ? buildSpellDetailRequest(latestSpell, latestSheet, onMutate, onRoll) : null;
    },
    name: spell.name,
    parent: spell.className,
    icon: <FrameIcon className="entity-detail__icon" svg={SCHOOL_ICONS[spell.school]} aria-hidden />,
    subtitle: levelSchoolSubtitle(spell),
    metadata: propertyMetadata(spell, sheet),
    tags: [...(spell.concentration ? ['Concentration'] : []), ...(spell.ritual ? ['Ritual'] : [])],
    description: spell.description,
    body: descriptionBody(spell),
    actionBar: castActionBar(spell, sheet, onMutate, onRoll),
    actionsPosition: 'top',
  };
}

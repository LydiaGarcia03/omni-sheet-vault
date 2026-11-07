import { useState } from 'react';
import type { CharacterSheet, DefenseEntry, DefenseType, MutationHandler } from '../../sheet/api';
import { RulesText } from '../../sheet/RulesText';
import { Customize, EditorBox, SidebarHeader, ValueEditorRow } from '../../sheet/sidebarParts';
import { customizationsOf, type NotedValue, type StoredCustomDefense } from './customizations';
import { DamageTypeIcon } from './DamageTypeIcon';
import immunitySvg from './frames/dnd_icon_immunity.svg?raw';
import resistanceSvg from './frames/dnd_icon_resistance.svg?raw';
import vulnerabilitySvg from './frames/dnd_icon_vulnerability.svg?raw';
import { ENGLISH_TERMS, useDnd5eTerms, type Dnd5eTerms } from './locales/dnd5eMessages';
import { ARMOR_CLASS_RULES_TEXT, INITIATIVE_RULES_TEXT, SPEED_RULES_TEXT } from './rulesText';

export const MOVEMENT_KEYS = ['burrowing', 'climbing', 'flying', 'swimming', 'walking'];

/** The movement modes, named in the player's language. */
export function movementsOf(terms: Dnd5eTerms): { key: string; label: string }[] {
  return MOVEMENT_KEYS.map((key) => ({ key, label: terms.t.dynamic(`terms.movements.${key}`, key) }));
}

/** PHB 2014 damage types, the same list the API accepts. */
export const DAMAGE_TYPES = [
  'acid', 'bludgeoning', 'cold', 'fire', 'force', 'lightning', 'necrotic', 'piercing', 'poison', 'psychic', 'radiant',
  'slashing', 'thunder',
];

/** The conditions D&D Beyond offers as immunities, the same list the API accepts. */
const IMMUNE_CONDITIONS = [
  'blinded', 'charmed', 'deafened', 'exhaustion', 'frightened', 'grappled', 'incapacitated', 'invisible', 'paralyzed',
  'petrified', 'poisoned', 'prone', 'restrained', 'stunned', 'unconscious',
];

const CUSTOM_DEFENSE_TYPES = [
  { key: 'RESISTANCE', label: 'terms.defenses.resistance', plural: 'terms.defenses.resistances' },
  { key: 'IMMUNITY', label: 'terms.defenses.immunity', plural: 'terms.defenses.immunities' },
  { key: 'VULNERABILITY', label: 'terms.defenses.vulnerability', plural: 'terms.defenses.vulnerabilities' },
] as const;

/** A custom defense's subtype: a damage type, or a condition for a condition immunity. */
function subtypeName(subtype: string, terms: Dnd5eTerms): string {
  return DAMAGE_TYPES.includes(subtype) ? terms.damageType(subtype) : terms.condition(subtype);
}

function signed(amount: number): string {
  return amount < 0 ? `−${Math.abs(amount)}` : `+${amount}`;
}

/** The movement the sheet shows and its speed: the chosen display when it has a speed, otherwise walking. */
export function displayedMovement(sheet: CharacterSheet, terms: Dnd5eTerms = ENGLISH_TERMS): { label: string; speed: number } {
  const { movementDisplay, speeds } = customizationsOf(sheet);
  const movements = movementsOf(terms);
  const chosen = movements.find((movement) => movement.key === movementDisplay);
  const chosenSpeed = chosen && chosen.key !== 'walking' ? speeds[chosen.key]?.value : null;
  return chosen && chosenSpeed != null ? { label: chosen.label, speed: chosenSpeed } : { label: movements[movements.length - 1].label, speed: sheet.speed };
}

type PaneProps = { sheet: CharacterSheet; onMutate: MutationHandler };

/** D&D Beyond's Speed pane: each movement with a speed, then Override Speeds and Set Movement Display, then the rules text. */
export function Dnd5eSpeedPane({ sheet, onMutate }: PaneProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const movements = movementsOf(terms);
  const { speeds, movementDisplay } = customizationsOf(sheet);
  const rows = movements.filter(({ key }) => key !== 'walking' && speeds[key]?.value != null);
  const save = (key: string, next: NotedValue) => onMutate({ type: 'CUSTOMIZE', group: 'speeds', target: key, value: next });
  const setDisplay = (key: string) =>
    key === 'walking'
      ? onMutate({ type: 'REMOVE_CUSTOMIZATION', group: 'movementDisplay', target: 'walking' })
      : onMutate({ type: 'CUSTOMIZE', group: 'movementDisplay', target: key, value: {} });

  return (
    <div className="combat-pane">
      <SidebarHeader title={t('terms.labels.speed')} />
      <div className="combat-pane__list">
        <div className="combat-pane__line">
          <span className="combat-pane__line-label">{movements[movements.length - 1].label}</span> <span className="combat-pane__line-value">{terms.feet(sheet.speed)}</span>
        </div>
        {rows.map(({ key, label }) => (
          <div key={key} className="combat-pane__line">
            <span className="combat-pane__line-label">{label}</span> <span className="combat-pane__line-value">{terms.feet(speeds[key].value ?? 0)}</span>
          </div>
        ))}
      </div>
      <Customize>
        <div className="combat-pane__editor-heading">{t('combat.overrideSpeeds')}</div>
        <EditorBox>
          {movements.map(({ key, label }) => (
            <ValueEditorRow
              key={key}
              label={label}
              value={speeds[key]?.value ?? null}
              notes={speeds[key]?.notes ?? ''}
              onCommit={(value, notes) => save(key, { value, notes: notes || null })}
            />
          ))}
        </EditorBox>
        <div className="combat-pane__editor-heading">{t('combat.setMovementDisplay')}</div>
        <select
          className="sidebar-value-editor__input combat-pane__select"
          aria-label={t('combat.setMovementDisplay')}
          value={movementDisplay ?? 'walking'}
          onChange={(event) => setDisplay(event.target.value)}
        >
          {movements.map(({ key, label }) => (
            <option key={key} value={key}>
              {label}
            </option>
          ))}
        </select>
      </Customize>
      <RulesText text={SPEED_RULES_TEXT} />
    </div>
  );
}

/** D&D Beyond's Initiative pane: the initiative score, the passive score with advantage or disadvantage, the rules text. */
export function Dnd5eInitiativePane({ sheet }: { sheet: CharacterSheet }) {
  const { t } = useDnd5eTerms();
  const score = 10 + sheet.initiative.value;
  const rows = [
    { label: t('combat.initiativeScore'), value: score },
    { label: t('combat.withAdvantage'), value: score + 5 },
    { label: t('combat.withDisadvantage'), value: score - 5 },
  ];
  return (
    <div className="combat-pane">
      <SidebarHeader title={t('terms.labels.initiative')} modifier={sheet.initiative.value} modifierFormat="parens" />
      <div className="combat-pane__list">
        {rows.map(({ label, value }) => (
          <div key={label} className="combat-pane__line">
            <span className="combat-pane__line-label">{label}:</span> <span className="combat-pane__line-value">{value}</span>
          </div>
        ))}
      </div>
      <RulesText text={INITIATIVE_RULES_TEXT} />
    </div>
  );
}

const ARMOR_CLASS_FIELDS = [
  { key: 'override', label: 'combat.overrideAc' },
  { key: 'baseArmorDex', label: 'combat.overrideBaseArmorDex' },
  { key: 'magicBonus', label: 'combat.magicBonus' },
  { key: 'miscBonus', label: 'combat.miscBonus' },
] as const;

/** D&D Beyond's Armor Class pane: each contribution to the total, its four-field Customize, then the rules text. */
export function Dnd5eArmorClassPane({ sheet, onMutate }: PaneProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const { armorClass } = customizationsOf(sheet);
  return (
    <div className="combat-pane">
      <SidebarHeader title={`${t('terms.labels.armorClass')}: ${sheet.armorClass.value}`} />
      <div className="combat-pane__list">
        {sheet.armorClass.contributions.map((contribution, index) => (
          <div key={`${contribution.source}-${index}`} className="combat-pane__contribution">
            <span className="combat-pane__contribution-amount">{index === 0 ? contribution.amount : signed(contribution.amount)}</span>
            <span>{contribution.source}</span>
          </div>
        ))}
      </div>
      <Customize>
        <EditorBox>
          {ARMOR_CLASS_FIELDS.map(({ key, label }) => (
            <ValueEditorRow
              key={key}
              label={t(label, { dex: terms.abilityAbbreviation('dexterity') })}
              value={armorClass[key]?.value ?? null}
              notes={armorClass[key]?.notes ?? ''}
              onCommit={(value, notes) =>
                onMutate({ type: 'CUSTOMIZE', group: 'armorClass', target: key, value: { value, notes: notes || null } })
              }
            />
          ))}
        </EditorBox>
      </Customize>
      <RulesText text={ARMOR_CLASS_RULES_TEXT} />
    </div>
  );
}

const DEFENSE_ICONS: Record<DefenseType, { svg: string; variant: 'resistance' | 'immunity' | 'vulnerability' }> = {
  RESISTANCE: { svg: resistanceSvg, variant: 'resistance' },
  IMMUNITY: { svg: immunitySvg, variant: 'immunity' },
  CONDITION_IMMUNITY: { svg: immunitySvg, variant: 'immunity' },
  VULNERABILITY: { svg: vulnerabilitySvg, variant: 'vulnerability' },
};

const DEFENSE_GROUPS: { heading: 'terms.defenses.resistances' | 'terms.defenses.immunities' | 'terms.defenses.vulnerabilities'; types: DefenseType[] }[] = [
  { heading: 'terms.defenses.resistances', types: ['RESISTANCE'] },
  { heading: 'terms.defenses.immunities', types: ['IMMUNITY', 'CONDITION_IMMUNITY'] },
  { heading: 'terms.defenses.vulnerabilities', types: ['VULNERABILITY'] },
];

/** The sheet's defenses of one type as the Defenses box names them; a hand-added one is marked "*". */
export function defenseLabels(sheet: CharacterSheet, type: DefenseType, fallback: string[]): string[] {
  if (!sheet.defenses) {
    return fallback;
  }
  const granted = new Set(sheet.defenses.filter((defense) => defense.type === type && !defense.custom).map((defense) => defense.name.toLowerCase()));
  const labels = new Map<string, string>();
  for (const defense of sheet.defenses.filter((entry) => entry.type === type)) {
    const name = defense.name.toLowerCase();
    if (!labels.has(name)) {
      labels.set(name, granted.has(name) ? defense.name : `${defense.name}*`);
    }
  }
  return [...labels.values()];
}

function DefenseLine({ defense }: { defense: DefenseEntry }) {
  const { t } = useDnd5eTerms();
  const icon = DEFENSE_ICONS[defense.type];
  const source = defense.custom ? t('combat.custom') : defense.source;
  return (
    <div className="defenses-pane__defense">
      <DamageTypeIcon svg={icon.svg} variant={icon.variant} label={t(`terms.defenses.${icon.variant}`)} />
      <span>
        {defense.name}
        {defense.custom && '*'}
        {source && <span className="defenses-pane__source"> ({source})</span>}
      </span>
    </div>
  );
}

function CustomDefenseRow({ defense, onMutate }: { defense: StoredCustomDefense; onMutate: MutationHandler }) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const subtype = subtypeName(defense.subtype, terms);
  const [notes, setNotes] = useState(defense.notes ?? '');
  const commit = () => {
    const next = notes.trim() === '' ? null : notes;
    if (next !== defense.notes) {
      onMutate({ type: 'CUSTOMIZE', group: 'defenses', target: defense.key, value: { value: null, notes: next } });
    }
  };
  return (
    <div className="defenses-pane__custom-row">
      <button
        type="button"
        className="sidebar-remove-button"
        onClick={() => onMutate({ type: 'REMOVE_CUSTOMIZATION', group: 'defenses', target: defense.key })}
      >
        {t('combat.delete')}
      </button>
      <span className="defenses-pane__custom-name">{subtype}</span>
      <input
        className="sidebar-value-editor__input defenses-pane__custom-notes"
        placeholder={t('combat.sourceNote')}
        aria-label={t('combat.sourceNoteOf', { name: subtype })}
        value={notes}
        onChange={(event) => setNotes(event.target.value)}
        onBlur={commit}
      />
    </div>
  );
}

/** D&D Beyond's Defenses pane: every defense grouped by type with its source, and the Customize that adds more. */
export function Dnd5eDefensesPane({ sheet, onMutate }: PaneProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const { defenses: custom } = customizationsOf(sheet);
  const [type, setType] = useState<StoredCustomDefense['type']>('RESISTANCE');
  const typeEntry = CUSTOM_DEFENSE_TYPES.find((entry) => entry.key === type);
  const typeLabel = typeEntry ? t(typeEntry.label) : '';
  const all = sheet.defenses ?? [];

  const add = (subtype: string) => {
    if (subtype !== '') {
      onMutate({ type: 'CUSTOMIZE', group: 'defenses', target: 'new', value: { type, subtype } });
    }
  };

  return (
    <div className="combat-pane">
      <SidebarHeader title={t('terms.defenses.defenses')} />
      {DEFENSE_GROUPS.map(({ heading, types }) => {
        const entries = all.filter((defense) => types.includes(defense.type)).sort((a, b) => a.name.localeCompare(b.name));
        return entries.length === 0 ? null : (
          <section key={heading} className="defenses-pane__group">
            <h2 className="defenses-pane__heading">{t(heading)}</h2>
            {entries.map((defense, index) => (
              <DefenseLine key={`${defense.key ?? defense.name}-${defense.source}-${index}`} defense={defense} />
            ))}
          </section>
        );
      })}
      <Customize>
        <div className="defenses-pane__pickers">
          <label className="defenses-pane__picker">
            <span className="sidebar-value-editor__label">{t('combat.defenseType')}</span>
            <select className="sidebar-value-editor__input" value={type} onChange={(event) => setType(event.target.value as StoredCustomDefense['type'])}>
              {CUSTOM_DEFENSE_TYPES.map(({ key, label }) => (
                <option key={key} value={key}>
                  {t(label)}
                </option>
              ))}
            </select>
          </label>
          <label className="defenses-pane__picker">
            <span className="sidebar-value-editor__label">{t('combat.defenseSubType')}</span>
            <select className="sidebar-value-editor__input" value="" onChange={(event) => add(event.target.value)}>
              <option value="">{t('combat.chooseA', { type: typeLabel })}</option>
              {type === 'IMMUNITY' ? (
                <>
                  <optgroup label={t('combat.damageGroup')}>
                    {DAMAGE_TYPES.map((damage) => (
                      <option key={damage} value={damage}>
                        {terms.damageType(damage)}
                      </option>
                    ))}
                  </optgroup>
                  <optgroup label={t('combat.conditionsGroup')}>
                    {IMMUNE_CONDITIONS.map((condition) => (
                      <option key={condition} value={condition}>
                        {terms.condition(condition)}
                      </option>
                    ))}
                  </optgroup>
                </>
              ) : (
                DAMAGE_TYPES.map((damage) => (
                  <option key={damage} value={damage}>
                    {terms.damageType(damage)}
                  </option>
                ))
              )}
            </select>
          </label>
        </div>
        {CUSTOM_DEFENSE_TYPES.map(({ key, plural }) => {
          const entries = custom.filter((defense) => defense.type === key);
          return entries.length === 0 ? null : (
            <section key={key} className="defenses-pane__custom-group">
              <h3 className="defenses-pane__custom-heading">{t(plural)}</h3>
              {entries.map((defense) => (
                <CustomDefenseRow key={defense.key} defense={defense} onMutate={onMutate} />
              ))}
            </section>
          );
        })}
      </Customize>
    </div>
  );
}

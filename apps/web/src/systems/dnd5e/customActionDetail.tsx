import type { CustomAction, EntityDetailRequest, NewCustomAction } from '../../sheet/api';
import { ABILITIES } from './abilities';
import { DAMAGE_TYPES } from './Dnd5eCombatPanes';
import { EntityCustomizeEditor, type CustomizeField, type CustomizeValue } from './EntityCustomize';
import { ENGLISH_TERMS, type Dnd5eTerms } from './locales/dnd5eMessages';

const capitalize = (text: string) => text.charAt(0).toUpperCase() + text.slice(1);

const ACTIVATION_TYPES = ['ACTION', 'NO_ACTION', 'BONUS_ACTION', 'REACTION', 'MINUTE', 'HOUR', 'SPECIAL'];

const AOE_TYPES = ['CONE', 'CUBE', 'CYLINDER', 'LINE', 'SPHERE', 'SQUARE', 'SQUARE_FEET', 'EMANATION'];

const DIE_OPTIONS = [4, 6, 8, 10, 12, 20].map((sides) => ({ value: String(sides), label: `d${sides}` }));

/** Options named by their `terms.<group>.<VALUE>` message. */
function termOptions(terms: Dnd5eTerms, group: string, values: string[]) {
  return values.map((value) => ({ value, label: terms.t.dynamic(`terms.${group}.${value}`, value) }));
}

function abilityOptions(terms: Dnd5eTerms) {
  return ABILITIES.map((ability) => ({ value: ability.key, label: terms.ability(ability.key) }));
}

function damageOptions(current: string | null, terms: Dnd5eTerms) {
  const options = DAMAGE_TYPES.map((damage) => ({ value: capitalize(damage), label: terms.damageType(damage) }));
  return current && !options.some((option) => option.value === current) ? [{ value: current, label: current }, ...options] : options;
}

/** The Edit section's fields in D&D Beyond's order; the Spell and Weapon templates add their own before the checkboxes. */
function editFields(action: CustomAction, terms: Dnd5eTerms): CustomizeField[] {
  const { t } = terms;
  const rangeOptions = termOptions(terms, 'ranges', ['MELEE', 'RANGED']);
  const abilities = abilityOptions(terms);
  const templateFields: CustomizeField[] =
    action.template === 'SPELL'
      ? [{ key: 'spellRangeType', label: t('customAction.spellRange'), kind: 'select', options: [...rangeOptions, ...termOptions(terms, 'ranges', ['SELF'])] }]
      : action.template === 'WEAPON'
        ? [
            { key: 'weaponAttackType', label: t('customAction.attackType'), kind: 'select', options: termOptions(terms, 'weaponAttackTypes', ['NATURAL', 'UNARMED_STRIKE']) },
            { key: 'longRange', label: t('customAction.longRange'), kind: 'number' },
            { key: 'dualWield', label: t('customAction.dualWield'), kind: 'checkbox' },
            { key: 'silvered', label: t('item.silvered'), kind: 'checkbox' },
          ]
        : [];
  return [
    { key: 'rangeCategory', label: t('customAction.range'), kind: 'select', options: rangeOptions },
    { key: 'stat', label: t('customAction.stat'), kind: 'select', options: abilities },
    { key: 'diceCount', label: t('customAction.diceCount'), kind: 'number' },
    { key: 'dieType', label: t('customAction.dieType'), kind: 'select', options: DIE_OPTIONS },
    { key: 'fixedValue', label: t('customAction.fixedValue'), kind: 'number' },
    { key: 'damageType', label: t('customAction.damageType'), kind: 'select', options: damageOptions(action.damageType, terms) },
    { key: 'saveType', label: t('customAction.saveType'), kind: 'select', options: abilities },
    { key: 'fixedSaveDc', label: t('customAction.fixedSaveDc'), kind: 'number' },
    { key: 'rangeFeet', label: t('customAction.rangeFeet'), kind: 'number' },
    { key: 'aoeType', label: t('customAction.aoeType'), kind: 'select', options: termOptions(terms, 'areas', AOE_TYPES) },
    { key: 'aoeSize', label: t('customAction.aoeSize'), kind: 'number' },
    { key: 'activationType', label: t('customAction.activationType'), kind: 'select', options: termOptions(terms, 'activations', ACTIVATION_TYPES) },
    { key: 'activationTime', label: t('customAction.activationTime'), kind: 'number' },
    ...templateFields,
    { key: 'affectedByMartialArts', label: t('customAction.affectedByMartialArts'), kind: 'checkbox' },
    { key: 'proficient', label: t('customAction.proficient'), kind: 'checkbox' },
    { key: 'displayAsAttack', label: t('item.displayAsAttack'), kind: 'checkbox' },
    { key: 'name', label: t('item.name'), kind: 'text', name: true },
    { key: 'snippet', label: t('customAction.snippet'), kind: 'textarea' },
    { key: 'description', label: t('customAction.description'), kind: 'textarea' },
  ];
}

function editValues(action: CustomAction): Record<string, CustomizeValue> {
  const { key: _key, ...fields } = action;
  return { ...fields, dieType: action.dieType == null ? null : String(action.dieType) };
}

/** Back to the API's shape: a blank name keeps the old one, text fields are never null, the die is a number. */
function toAction(action: CustomAction, values: Record<string, CustomizeValue>): NewCustomAction {
  const { key: _key, ...current } = action;
  return {
    ...current,
    ...(values as Partial<NewCustomAction>),
    name: typeof values.name === 'string' && values.name.trim() !== '' ? values.name.trim() : action.name,
    snippet: typeof values.snippet === 'string' ? values.snippet : '',
    description: typeof values.description === 'string' ? values.description : '',
    dieType: values.dieType == null ? null : Number(values.dieType),
  };
}

function distance(feet: number | null, terms: Dnd5eTerms): string {
  return feet == null ? '--' : terms.feet(feet);
}

function rangeArea(action: CustomAction, terms: Dnd5eTerms): string {
  const { t } = terms;
  const reach = action.rangeCategory === 'MELEE' || action.rangeCategory == null;
  const range = reach ? t('customAction.reach', { distance: distance(action.rangeFeet, terms) }) : distance(action.rangeFeet, terms);
  const area = action.aoeType ? ` (${distance(action.aoeSize, terms)} ${t.dynamic(`terms.areas.${action.aoeType}`, action.aoeType)})` : '';
  return `${range}${area}`;
}

function properties(action: CustomAction, terms: Dnd5eTerms) {
  const { t } = terms;
  const activation = action.activationType ? t.dynamic(`terms.activations.${action.activationType}`, action.activationType) : null;
  const damageType = action.damageType ? ` ${terms.damageType(action.damageType)}` : '';
  const damage = action.diceCount && action.dieType ? `${action.diceCount}d${action.dieType}${damageType}` : null;
  return [
    { label: t('customAction.rangeArea'), value: rangeArea(action, terms) },
    ...(activation ? [{ label: t('customAction.activationType'), value: activation }] : []),
    ...(damage ? [{ label: t('customAction.damage'), value: damage }] : []),
    ...(action.saveType
      ? [{ label: t('customAction.save'), value: action.fixedSaveDc ? t('customAction.saveDc', { ability: terms.ability(action.saveType), dc: action.fixedSaveDc }) : terms.ability(action.saveType) }]
      : []),
  ];
}

/** A custom action's pane: its name, the Edit section (every field, saved as it changes), then its properties and text. */
export function customActionDetailRequest(
  action: CustomAction,
  onUpdate: (actionKey: string, action: NewCustomAction) => void,
  terms: Dnd5eTerms = ENGLISH_TERMS,
): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `custom-action:${action.key}`,
    name: action.name,
    metadata: properties(action, terms),
    description: action.description,
    customize: {
      label: terms.t('customAction.edit'),
      customized: false,
      editor: (
        <EntityCustomizeEditor
          fields={editFields(action, terms)}
          values={editValues(action)}
          customized={false}
          onSave={(values) => onUpdate(action.key, toAction(action, values))}
        />
      ),
    },
    refresh: (sheet) => {
      const latest = sheet.customActions.find((candidate) => candidate.key === action.key);
      return latest ? customActionDetailRequest(latest, onUpdate, terms) : null;
    },
  };
}

import type { CustomAction, EntityDetailRequest, NewCustomAction } from '../../sheet/api';
import { ABILITIES } from './abilities';
import { DAMAGE_TYPES } from './Dnd5eCombatPanes';
import { EntityCustomizeEditor, type CustomizeField, type CustomizeValue } from './EntityCustomize';

const capitalize = (text: string) => text.charAt(0).toUpperCase() + text.slice(1);

const ACTIVATION_TYPES = [
  { value: 'ACTION', label: 'Action' },
  { value: 'NO_ACTION', label: 'No Action' },
  { value: 'BONUS_ACTION', label: 'Bonus Action' },
  { value: 'REACTION', label: 'Reaction' },
  { value: 'MINUTE', label: 'Minute' },
  { value: 'HOUR', label: 'Hour' },
  { value: 'SPECIAL', label: 'Special' },
];

const AOE_TYPES = [
  { value: 'CONE', label: 'Cone' },
  { value: 'CUBE', label: 'Cube' },
  { value: 'CYLINDER', label: 'Cylinder' },
  { value: 'LINE', label: 'Line' },
  { value: 'SPHERE', label: 'Sphere' },
  { value: 'SQUARE', label: 'Square' },
  { value: 'SQUARE_FEET', label: 'Square Feet' },
  { value: 'EMANATION', label: 'Emanation' },
];

const ABILITY_OPTIONS = ABILITIES.map((ability) => ({ value: ability.key, label: ability.label }));
const DIE_OPTIONS = [4, 6, 8, 10, 12, 20].map((sides) => ({ value: String(sides), label: `d${sides}` }));
const RANGE_OPTIONS = [
  { value: 'MELEE', label: 'Melee' },
  { value: 'RANGED', label: 'Ranged' },
];

function damageOptions(current: string | null) {
  const options = DAMAGE_TYPES.map((damage) => ({ value: capitalize(damage), label: capitalize(damage) }));
  return current && !options.some((option) => option.value === current) ? [{ value: current, label: current }, ...options] : options;
}

/** The Edit section's fields in D&D Beyond's order; the Spell and Weapon templates add their own before the checkboxes. */
function editFields(action: CustomAction): CustomizeField[] {
  const templateFields: CustomizeField[] =
    action.template === 'SPELL'
      ? [{ key: 'spellRangeType', label: 'Spell Range', kind: 'select', options: [...RANGE_OPTIONS, { value: 'SELF', label: 'Self' }] }]
      : action.template === 'WEAPON'
        ? [
            { key: 'weaponAttackType', label: 'Attack Type', kind: 'select', options: [{ value: 'NATURAL', label: 'Natural' }, { value: 'UNARMED_STRIKE', label: 'Unarmed Strike' }] },
            { key: 'longRange', label: 'Long Range', kind: 'number' },
            { key: 'dualWield', label: 'Dual Wield', kind: 'checkbox' },
            { key: 'silvered', label: 'Silvered', kind: 'checkbox' },
          ]
        : [];
  return [
    { key: 'rangeCategory', label: 'Range', kind: 'select', options: RANGE_OPTIONS },
    { key: 'stat', label: 'Stat', kind: 'select', options: ABILITY_OPTIONS },
    { key: 'diceCount', label: 'Dice Count', kind: 'number' },
    { key: 'dieType', label: 'Die Type', kind: 'select', options: DIE_OPTIONS },
    { key: 'fixedValue', label: 'Fixed Value', kind: 'number' },
    { key: 'damageType', label: 'Damage Type', kind: 'select', options: damageOptions(action.damageType) },
    { key: 'saveType', label: 'Save Type', kind: 'select', options: ABILITY_OPTIONS },
    { key: 'fixedSaveDc', label: 'Fixed Save DC', kind: 'number' },
    { key: 'rangeFeet', label: 'Range', kind: 'number' },
    { key: 'aoeType', label: 'AoE Type', kind: 'select', options: AOE_TYPES },
    { key: 'aoeSize', label: 'AoE Size', kind: 'number' },
    { key: 'activationType', label: 'Activation Type', kind: 'select', options: ACTIVATION_TYPES },
    { key: 'activationTime', label: 'Activation Time', kind: 'number' },
    ...templateFields,
    { key: 'affectedByMartialArts', label: 'Affected by Martial Arts', kind: 'checkbox' },
    { key: 'proficient', label: 'Proficient', kind: 'checkbox' },
    { key: 'displayAsAttack', label: 'Display as Attack', kind: 'checkbox' },
    { key: 'name', label: 'Name', kind: 'text', name: true },
    { key: 'snippet', label: 'Snippet', kind: 'textarea' },
    { key: 'description', label: 'Description', kind: 'textarea' },
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

function rangeArea(action: CustomAction): string {
  const range = `${action.rangeFeet ?? '--'}ft.${action.rangeCategory === 'MELEE' || action.rangeCategory == null ? ' Reach' : ''}`;
  const area = action.aoeType ? ` (${action.aoeSize ?? '--'}ft. ${AOE_TYPES.find((type) => type.value === action.aoeType)?.label ?? ''})` : '';
  return `${range}${area}`;
}

function properties(action: CustomAction) {
  const activation = ACTIVATION_TYPES.find((type) => type.value === action.activationType)?.label;
  const damage = action.diceCount && action.dieType ? `${action.diceCount}d${action.dieType}${action.damageType ? ` ${action.damageType}` : ''}` : null;
  return [
    { label: 'Range/Area', value: rangeArea(action) },
    ...(activation ? [{ label: 'Activation Type', value: activation }] : []),
    ...(damage ? [{ label: 'Damage', value: damage }] : []),
    ...(action.saveType ? [{ label: 'Save', value: `${capitalize(action.saveType)}${action.fixedSaveDc ? ` DC ${action.fixedSaveDc}` : ''}` }] : []),
  ];
}

/** A custom action's pane: its name, the Edit section (every field, saved as it changes), then its properties and text. */
export function customActionDetailRequest(action: CustomAction, onUpdate: (actionKey: string, action: NewCustomAction) => void): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `custom-action:${action.key}`,
    name: action.name,
    metadata: properties(action),
    description: action.description,
    customize: {
      label: 'Edit',
      customized: false,
      editor: (
        <EntityCustomizeEditor
          fields={editFields(action)}
          values={editValues(action)}
          customized={false}
          onSave={(values) => onUpdate(action.key, toAction(action, values))}
        />
      ),
    },
    refresh: (sheet) => {
      const latest = sheet.customActions.find((candidate) => candidate.key === action.key);
      return latest ? customActionDetailRequest(latest, onUpdate) : null;
    },
  };
}

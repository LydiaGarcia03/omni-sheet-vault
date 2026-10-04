import type { CustomAction, ManageCustomActionsRequest, NewCustomAction } from './api';
import { SidebarHeader } from './sidebarParts';

const TEMPLATES = [
  { value: 'GENERAL', label: 'General' },
  { value: 'SPELL', label: 'Spell' },
  { value: 'WEAPON', label: 'Weapon' },
];

/** A blank action of the template, named like D&D Beyond's: "Custom Action N". */
export function blankCustomAction(template: string, existing: CustomAction[]): NewCustomAction {
  return {
    template,
    name: `Custom Action ${existing.length + 1}`,
    snippet: '',
    description: '',
    rangeCategory: null,
    rangeFeet: null,
    stat: null,
    diceCount: null,
    dieType: null,
    fixedValue: null,
    damageType: null,
    saveType: null,
    fixedSaveDc: null,
    spellRangeType: null,
    aoeType: null,
    aoeSize: null,
    activationType: null,
    activationTime: null,
    affectedByMartialArts: false,
    proficient: false,
    displayAsAttack: false,
    weaponAttackType: null,
    longRange: null,
    dualWield: false,
    silvered: false,
  };
}

/**
 * D&D Beyond's Manage Custom Actions pane: the actions grouped by template, each opening its own pane (where Edit
 * changes it) with a Remove Action button, then "Add New Actions", whose select creates a blank action at once.
 */
export function ManageCustomActionsPanel({ customActions, onAdd, onRemove, onOpenAction }: ManageCustomActionsRequest) {
  const groups = TEMPLATES.map((template) => ({
    ...template,
    actions: customActions.filter((action) => action.template === template.value),
  })).filter((group) => group.actions.length > 0);

  return (
    <div className="custom-actions-pane">
      <SidebarHeader title="Manage Custom Actions" />
      {groups.map((group) => (
        <section key={group.value}>
          <h2 className="custom-actions-pane__subheading">{group.label}</h2>
          <ul className="custom-actions-pane__actions">
            {group.actions.map((action) => (
              <li key={action.key} className="custom-actions-pane__summary">
                <button type="button" className="custom-actions-pane__name" onClick={() => onOpenAction(action)}>
                  {action.name}
                </button>
                <button
                  type="button"
                  className="mutate custom-actions-pane__remove"
                  aria-label={`Remove ${action.name}`}
                  onClick={() => onRemove(action.key)}
                >
                  Remove Action
                </button>
              </li>
            ))}
          </ul>
        </section>
      ))}
      <section>
        <h2 className="custom-actions-pane__subheading">Add New Actions</h2>
        <select
          className="custom-actions-pane__select"
          aria-label="Add a custom action"
          value=""
          onChange={(event) => event.target.value && onAdd(blankCustomAction(event.target.value, customActions))}
        >
          <option value="">-- Choose an Option --</option>
          {TEMPLATES.map((template) => (
            <option key={template.value} value={template.value}>
              {template.label}
            </option>
          ))}
        </select>
      </section>
    </div>
  );
}

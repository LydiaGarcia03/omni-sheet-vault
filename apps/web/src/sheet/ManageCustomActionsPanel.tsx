import type { CustomAction, ManageCustomActionsRequest, NewCustomAction } from './api';
import { useSheetText, type SheetText } from '../locales/platformMessages';
import { SidebarHeader } from './sidebarParts';

const TEMPLATES = [
  { value: 'GENERAL', label: 'customActions.general' },
  { value: 'SPELL', label: 'terms.spell' },
  { value: 'WEAPON', label: 'terms.weapon' },
] as const;

/** A blank action of the template, named like D&D Beyond's: "Custom Action N". */
export function blankCustomAction(template: string, existing: CustomAction[], t: SheetText = useSheetText.english): NewCustomAction {
  return {
    template,
    name: t('customActions.defaultName', { number: existing.length + 1 }),
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
  const t = useSheetText();
  const groups = TEMPLATES.map((template) => ({
    ...template,
    actions: customActions.filter((action) => action.template === template.value),
  })).filter((group) => group.actions.length > 0);

  return (
    <div className="custom-actions-pane">
      <SidebarHeader title={t('customActions.title')} />
      {groups.map((group) => (
        <section key={group.value}>
          <h2 className="custom-actions-pane__subheading">{t(group.label)}</h2>
          <ul className="custom-actions-pane__actions">
            {group.actions.map((action) => (
              <li key={action.key} className="custom-actions-pane__summary">
                <button type="button" className="custom-actions-pane__name" onClick={() => onOpenAction(action)}>
                  {action.name}
                </button>
                <button
                  type="button"
                  className="mutate custom-actions-pane__remove"
                  aria-label={t('common.removeName', { name: action.name })}
                  onClick={() => onRemove(action.key)}
                >
                  {t('customActions.removeAction')}
                </button>
              </li>
            ))}
          </ul>
        </section>
      ))}
      <section>
        <h2 className="custom-actions-pane__subheading">{t('customActions.addNew')}</h2>
        <select
          className="custom-actions-pane__select"
          aria-label={t('customActions.addLabel')}
          value=""
          onChange={(event) => event.target.value && onAdd(blankCustomAction(event.target.value, customActions, t))}
        >
          <option value="">{t('common.chooseOption')}</option>
          {TEMPLATES.map((template) => (
            <option key={template.value} value={template.value}>
              {t(template.label)}
            </option>
          ))}
        </select>
      </section>
    </div>
  );
}

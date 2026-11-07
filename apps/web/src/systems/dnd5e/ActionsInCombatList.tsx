import type { EntityDetailHandler } from '../../sheet/api';
import { useDnd5eTerms } from './locales/dnd5eMessages';
import type { StandardAction } from './standardActions';

type ActionsInCombatListProps = {
  actions: StandardAction[];
  onOpenDetail: EntityDetailHandler;
};

/**
 * The flowing, comma-separated name list D&D Beyond shows under every
 * action-type section's own "Actions in Combat" sub-heading — not just the
 * Action section (Attack/Dash/...), every section gets one: Bonus Action
 * shows Two-Weapon Fighting, Reaction shows Opportunity Attack, Other shows
 * Interact with an Object. Confirmed live against a second reference
 * character (a Fighter) specifically because Aria's own build never
 * exercises those three categories. Each name opens the Entity Detail mold;
 * no inline description, same correction already applied to the Action
 * section's own list.
 */
export function ActionsInCombatList({ actions, onOpenDetail }: ActionsInCombatListProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const named = actions.map((action) => ({
    key: action.key,
    name: t(`terms.actions.${action.key}`),
    description: t(`standardActions.${action.key}`, { distance: terms.feet(5) }),
  }));
  return (
    <>
      <h3 className="actions-tab__heading">{t('actionsTab.actionsInCombat')}</h3>
      <p className="actions-tab__standard-list">
        {named.map((action, index) => (
          <span key={action.key}>
            <button
              type="button"
              className="reveal"
              onClick={() =>
                onOpenDetail({ kind: 'entityDetail', name: action.name, metadata: [], description: action.description })
              }
            >
              {action.name}
            </button>
            {index < actions.length - 1 ? ', ' : ''}
          </span>
        ))}
      </p>
    </>
  );
}

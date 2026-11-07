import type { CharacterSheet, EntityDetailHandler, MutationHandler } from '../../sheet/api';
import { SidebarCollapsible, SidebarHeader } from '../../sheet/sidebarParts';
import { AddExtraSection } from './AddExtraSection';
import { extraCategoriesOf, extraDetailRequest, sizeAndType } from './ExtraRow';
import { useDnd5eTerms } from './locales/dnd5eMessages';

type ManageExtrasPanelProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
};

/**
 * D&D Beyond's Manage Extras pane: Add an Extra from the catalogue's creatures, then the character's extras grouped by
 * category, each with its size and type, its name opening its own pane, and Delete.
 */
export function ManageExtrasPanel({ sheet, onMutate, onOpenDetail }: ManageExtrasPanelProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const groups = extraCategoriesOf(terms).map((category) => ({
    ...category,
    extras: sheet.extras.filter((extra) => extra.category === category.id),
  })).filter((group) => group.extras.length > 0);

  return (
    <div className="extras-manage">
      <SidebarHeader title={t('extras.manage')} />
      <SidebarCollapsible heading={t('extras.addAnExtra')} defaultOpen>
        <AddExtraSection onMutate={onMutate} />
      </SidebarCollapsible>
      <SidebarCollapsible heading={t('extras.current')} defaultOpen>
        {groups.length === 0 && <p className="extras-manage__empty">{t('extras.none')}</p>}
        {groups.map((group) => (
          <section key={group.id} className="extras-manage__group">
            <h3 className="extras-manage__group-heading">{group.label}</h3>
            <ul className="extras-manage__list">
              {group.extras.map((extra) => (
                <li key={extra.key} className="extras-manage__item">
                  <button type="button" className="extras-manage__item-text" onClick={() => onOpenDetail(extraDetailRequest(extra, onMutate, terms))}>
                    <span className="extras-manage__name">{extra.name}</span>
                    <span className="extras-manage__meta">{sizeAndType(extra)}</span>
                  </button>
                  <button
                    type="button"
                    className="mutate extras-manage__delete"
                    aria-label={t('extras.deleteName', { name: extra.name })}
                    onClick={() => onMutate({ type: 'REMOVE_EXTRA', extraKey: extra.key })}
                  >
                    {t('combat.delete')}
                  </button>
                </li>
              ))}
            </ul>
          </section>
        ))}
      </SidebarCollapsible>
    </div>
  );
}

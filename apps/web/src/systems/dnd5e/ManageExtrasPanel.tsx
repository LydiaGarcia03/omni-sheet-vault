import type { CharacterSheet, EntityDetailHandler, MutationHandler } from '../../sheet/api';
import { SidebarCollapsible, SidebarHeader } from '../../sheet/sidebarParts';
import { AddExtraSection } from './AddExtraSection';
import { EXTRA_CATEGORIES, extraDetailRequest, sizeAndType } from './ExtraRow';

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
  const groups = EXTRA_CATEGORIES.map((category) => ({
    ...category,
    extras: sheet.extras.filter((extra) => extra.category === category.id),
  })).filter((group) => group.extras.length > 0);

  return (
    <div className="extras-manage">
      <SidebarHeader title="Manage Extras" />
      <SidebarCollapsible heading="Add an Extra" defaultOpen>
        <AddExtraSection onMutate={onMutate} />
      </SidebarCollapsible>
      <SidebarCollapsible heading="Current Extras" defaultOpen>
        {groups.length === 0 && <p className="extras-manage__empty">No extras yet.</p>}
        {groups.map((group) => (
          <section key={group.id} className="extras-manage__group">
            <h3 className="extras-manage__group-heading">{group.label}</h3>
            <ul className="extras-manage__list">
              {group.extras.map((extra) => (
                <li key={extra.key} className="extras-manage__item">
                  <button type="button" className="extras-manage__item-text" onClick={() => onOpenDetail(extraDetailRequest(extra, onMutate))}>
                    <span className="extras-manage__name">{extra.name}</span>
                    <span className="extras-manage__meta">{sizeAndType(extra)}</span>
                  </button>
                  <button
                    type="button"
                    className="mutate extras-manage__delete"
                    aria-label={`Delete ${extra.name}`}
                    onClick={() => onMutate({ type: 'REMOVE_EXTRA', extraKey: extra.key })}
                  >
                    Delete
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

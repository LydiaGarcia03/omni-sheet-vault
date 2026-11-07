import { useState } from 'react';
import type { CharacterSheet, EntityDetailHandler, MutationHandler, PaneHandler } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { GridHeaderRow, GridScroll } from '../../sheet/ListGrid';
import { SearchField } from '../../sheet/SearchField';
import { categoryLabel, extraCategoriesOf, ExtraRow, sizeAndType } from './ExtraRow';
import { useDnd5eTerms, type Dnd5eText } from './locales/dnd5eMessages';
import { ManageExtrasPanel } from './ManageExtrasPanel';

type ExtrasTabProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  onOpenPane: PaneHandler;
};

function columns(t: Dnd5eText) {
  return [
    { key: 'name', className: 'extra-row__name-cell grid-header-cell', content: t('spellsTab.nameColumn') },
    { key: 'ac', className: 'extra-row__ac grid-header-cell', content: t('terms.labels.armorClassShort') },
    { key: 'hp', className: 'extra-row__hp grid-header-cell', content: t('terms.labels.hitPoints') },
    { key: 'speed', className: 'extra-row__speed grid-header-cell', content: t('terms.labels.speed') },
    { key: 'notes', className: 'extra-row__notes grid-header-cell', content: t('actionsTab.notesColumn') },
  ];
}

/**
 * D&D Beyond's Extras tab: search by name, type or category, Manage Extras, a chip per category the character has
 * (singular names), then one section per category with Name, AC, Hit Points, Speed and Notes columns.
 */
export function ExtrasTab({ sheet, onMutate, onOpenDetail, onOpenPane }: ExtrasTabProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const [activeCategory, setActiveCategory] = useState('all');
  const [search, setSearch] = useState('');

  const present = extraCategoriesOf(terms).filter((category) => sheet.extras.some((extra) => extra.category === category.id));
  const chips = [{ id: 'all', label: t('actionsTab.all') }, ...present];
  const needle = search.trim().toLowerCase();
  const visible = sheet.extras
    .filter((extra) => activeCategory === 'all' || extra.category === activeCategory)
    .filter((extra) => !needle || [extra.name, sizeAndType(extra), categoryLabel(extra.category, terms)].some((text) => text.toLowerCase().includes(needle)));
  const sections = present.filter((category) => visible.some((extra) => extra.category === category.id));

  const openManageExtras = () =>
    onOpenPane({ kind: 'pane', render: (latest) => <ManageExtrasPanel sheet={latest} onMutate={onMutate} onOpenDetail={onOpenDetail} /> });

  return (
    <div className="extras-tab">
      <div className="tab-actions-row extras-actions-row">
        <SearchField value={search} onChange={setSearch} placeholder={t('extras.search')} />
        <button type="button" className="mutate inventory-manage-button" onClick={openManageExtras}>
          {t('extras.manage')}
        </button>
      </div>

      {sheet.extras.length > 0 ? (
        <>
          <div className="grid-filter-row">
            <FilterChips chips={chips} activeChip={activeCategory} onSelect={setActiveCategory} />
          </div>
          <GridScroll className="extras-tab__list">
            {sections.map(({ id: category, label }) => (
              <section key={category}>
                <div className="extras-section-heading">
                  <div className="extras-section-heading__label">{label}</div>
                </div>
                <GridHeaderRow rowClassName="extra-row" columns={columns(t)} />
                <div className="action-list">
                  {visible
                    .filter((extra) => extra.category === category)
                    .map((extra) => (
                      <ExtraRow key={extra.key} extra={extra} onMutate={onMutate} onOpenDetail={onOpenDetail} />
                    ))}
                </div>
              </section>
            ))}
          </GridScroll>
        </>
      ) : (
        <p className="extras-tab__empty">{t('extras.empty')}</p>
      )}
    </div>
  );
}

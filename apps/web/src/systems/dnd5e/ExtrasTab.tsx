import { useState } from 'react';
import type { CharacterSheet, EntityDetailHandler, MutationHandler } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { GridHeaderRow, GridScroll } from '../../sheet/ListGrid';
import { SearchField } from '../../sheet/SearchField';
import { ExtraRow } from './ExtraRow';

const CHIPS = [
  { id: 'all', label: 'All' },
  { id: 'FAMILIAR', label: 'Familiars' },
  { id: 'MOUNT', label: 'Mounts' },
  { id: 'SUMMONED_CREATURE', label: 'Summoned Creatures' },
  { id: 'VEHICLE', label: 'Vehicles' },
];

type ExtrasTabProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
};

/**
 * Build order step 8's final tab (systems/dnd-5e/sheet-build.md): familiars, mounts,
 * summoned creatures and vehicles, per systems/dnd-5e/sheet-ui.md's Extras section. A
 * row shows name, armor class, hit points and speed; opening it shows the full
 * stat block (free text) in the sidebar via Entity Detail, with the extra's
 * hit points editable in the action bar (see ExtraRow.tsx). No "add an extra"
 * UI this phase — same reasoning as "Manage feats"/"Manage spells": there is
 * no creature catalog to pick from yet, only seed data.
 *
 * **Restructured 2026-09-21** to the same grid conventions Actions/Spells/
 * Inventory/Features already use: `.tab-actions-row` for the search field
 * (was a bespoke inline-styled flex row), `.grid-filter-row` for the
 * category chips, and one section per category present
 * (`.extras-section-heading`, same treatment as
 * `.inventory-section-heading`) with its own repeated `GridHeaderRow` —
 * was a single flat list with one header row regardless of category, so a
 * mixed Familiars/Mounts list read as one undifferentiated group. `GridScroll`
 * keeps the header/chips fixed while only the row list scrolls, matching
 * every other tab's own `.<tab>-tab`/`.<tab>-tab__list` split.
 */
export function ExtrasTab({ sheet, onMutate, onOpenDetail }: ExtrasTabProps) {
  const [activeCategory, setActiveCategory] = useState('all');
  const [search, setSearch] = useState('');

  const visible = sheet.extras
    .filter((extra) => activeCategory === 'all' || extra.category === activeCategory)
    .filter((extra) => extra.name.toLowerCase().includes(search.toLowerCase()));

  const categories = CHIPS.filter((chip) => chip.id !== 'all' && visible.some((extra) => extra.category === chip.id));

  return (
    <div className="extras-tab">
      <div className="tab-actions-row extras-actions-row">
        <SearchField value={search} onChange={setSearch} placeholder="Search extras" />
      </div>

      {sheet.extras.length > 0 && (
        <>
          <div className="grid-filter-row">
            <FilterChips chips={CHIPS} activeChip={activeCategory} onSelect={setActiveCategory} />
          </div>

          <GridScroll className="extras-tab__list">
            {categories.map(({ id: category, label }) => (
              <section key={category}>
                <div className="extras-section-heading">
                  <div className="extras-section-heading__label">{label}</div>
                </div>
                <GridHeaderRow
                  rowClassName="extra-row"
                  columns={[
                    { key: 'name', className: 'extra-row__name-cell grid-header-cell', content: 'Name' },
                    { key: 'ac', className: 'extra-row__stat grid-header-cell', content: 'AC' },
                    { key: 'hp', className: 'extra-row__stat grid-header-cell', content: 'Hit Points' },
                    { key: 'speed', className: 'extra-row__stat grid-header-cell', content: 'Speed' },
                  ]}
                />
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
      )}

      {sheet.extras.length === 0 && (
        <p style={{ fontSize: '11px', color: 'var(--text-muted, #6B7A85)' }}>No familiars, mounts, summoned creatures or vehicles yet.</p>
      )}
    </div>
  );
}

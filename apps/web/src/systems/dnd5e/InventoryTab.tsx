import { useState } from 'react';
import type { CharacterSheet, EntityDetailHandler, Item, MutationHandler, StorageSection } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { GridHeaderRow, GridScroll } from '../../sheet/ListGrid';
import { SearchField } from '../../sheet/SearchField';
import { AttunementSection } from './AttunementSection';
import { CoinChips } from './CoinChips';
import { CoinsPanel } from './CoinsPanel';
import { FrameIcon } from './FrameIcon';
import filterIcon from './frames/dnd_icon_filter.svg?raw';
import { rarityClassSuffix } from './itemRarity';
import { ItemRow } from './ItemRow';
import { EMPTY_ITEM_ADVANCED_FILTERS, hasActiveItemAdvancedFilters, ItemsAdvancedFilters, matchesItemAdvancedFilters } from './ItemsAdvancedFilters';
import { OtherPossessionsQuickAdd } from './OtherPossessionsQuickAdd';

const SECTION_LABELS: Record<string, string> = {
  EQUIPMENT: 'Equipment',
  BACKPACK: 'Backpack',
  BAG_OF_HOLDING: 'Bag of Holding',
  OTHER_POSSESSIONS: 'Other Possessions',
};

const GRID_COLUMNS = [
  { key: 'flag', className: 'item-row__flag' },
  { key: 'name', className: 'item-row__name grid-header-cell', content: 'Name' },
  { key: 'weight', className: 'item-row__weight grid-header-cell', content: 'Weight' },
  { key: 'quantity', className: 'item-row__quantity grid-header-cell', content: 'Qty' },
  { key: 'cost', className: 'item-row__cost grid-header-cell', content: 'Cost (gp)' },
  { key: 'notes', className: 'item-row__notes grid-header-cell', content: 'Notes' },
];

function sectionLabel(location: string): string {
  return SECTION_LABELS[location] ?? location;
}

/** Gated on equipped, not attuned — the real Bag of Holding item never requires attunement. */
function bagOfHoldingRaritySuffix(items: Item[]): string | null {
  const bag = items.find((item) => item.name === 'Bag of Holding' && item.equipped);
  return bag ? rarityClassSuffix(bag.rarity) : null;
}

type InventoryTabProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  onOpenManageInventory: () => void;
};

/**
 * Build order step 8 (systems/dnd-5e/sheet-build.md), scoped per the owner's confirmed
 * decision: item rows, attunement (bounded at 3) and coins are all real,
 * persisted mutations.
 *
 * **Weight/encumbrance initiative** restructured grouping from item kind
 * (Weapons/Armor/Shields/Gear) to storage location — Equipment/Backpack/
 * [Bag of Holding]/Attunement/Other Possessions, matching D&D Beyond's own
 * Inventory tab (confirmed live), simplified to a single Backpack and a
 * conditional Bag of Holding rather than Beyond's arbitrary-many-named-
 * containers system. Each storage section shows its own
 * item count and weight (kg, converted server-side — ground-rules.md's "no
 * business rules in the frontend"), plus a capacity for Backpack/Bag of
 * Holding only (Equipment has no maximum, Other Possessions is never
 * physically carried at all — same treatment D&D Beyond gives both). The
 * header's weight bar and Overloaded state only render while
 * `sheet.encumbrance.trackWeight` is on, the per-character setting toggled
 * from Manage Inventory. Bag of Holding's own section heading is colored by
 * the bag's rarity, matching D&D Beyond's own item-name-by-rarity convention
 * (`itemRarity.ts`).
 *
 * `.inventory-tab`/`.inventory-tab__list` fill `.tabbed-section__scroll`'s own
 * height so only the row list scrolls, same fix `.spells-tab` already has.
 */
export function InventoryTab({ sheet, onMutate, onOpenDetail, onOpenManageInventory }: InventoryTabProps) {
  const [activeSection, setActiveSection] = useState('all');
  const [search, setSearch] = useState('');
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  const [advancedFilters, setAdvancedFilters] = useState(EMPTY_ITEM_ADVANCED_FILTERS);

  const { encumbrance } = sheet;
  const sectionByLocation = new Map<string, StorageSection>(encumbrance.sections.map((section) => [section.location, section]));
  const hasBagOfHolding = sectionByLocation.has('BAG_OF_HOLDING');

  const chips = [
    { id: 'all', label: 'All' },
    { id: 'EQUIPMENT', label: 'Equipment' },
    { id: 'BACKPACK', label: 'Backpack' },
    ...(hasBagOfHolding ? [{ id: 'BAG_OF_HOLDING', label: 'Bag of Holding' }] : []),
    { id: 'ATTUNEMENT', label: 'Attunement' },
    { id: 'OTHER_POSSESSIONS', label: 'Other Possessions' },
  ];

  const visibleItems = sheet.items.filter((item) => {
    if (!matchesItemAdvancedFilters(item, advancedFilters)) {
      return false;
    }
    return item.name.toLowerCase().includes(search.toLowerCase());
  });

  const showSection = (location: string) => activeSection === 'all' || activeSection === location;

  const openOtherPossessions = () =>
    onOpenDetail({
      kind: 'entityDetail',
      name: 'Other Possessions',
      metadata: [],
      description: '',
      actionBar: <OtherPossessionsQuickAdd onMutate={onMutate} />,
    });

  const renderSection = (location: string) => {
    const section = sectionByLocation.get(location);
    const items = visibleItems.filter((item) => item.storageLocation === location);
    const raritySuffix = location === 'BAG_OF_HOLDING' ? bagOfHoldingRaritySuffix(sheet.items) : null;
    // Other Possessions is never physically carried — no weight/capacity stat, matching D&D Beyond exactly.
    const showWeight = section && location !== 'OTHER_POSSESSIONS';
    const isOtherPossessions = location === 'OTHER_POSSESSIONS';
    // Gated on the section's own total, not the filtered `items` list, so an active search doesn't mistake
    // "no matches" for "genuinely empty" and show the add prompt over real, just-hidden items.
    const hasOtherPossessionsItems = isOtherPossessions && (section?.itemCount ?? 0) > 0;
    return (
      <section key={location}>
        <div className="inventory-section-heading">
          <h3 className={`inventory-section-heading__label${raritySuffix ? ` inventory-section-heading__label--${raritySuffix}` : ''}`}>
            {sectionLabel(location)} ({section?.itemCount ?? 0})
          </h3>
          {showWeight && (
            <span className="inventory-section-heading__weight">
              {section.weightKg.toFixed(1)} kg{section.capacityKg != null ? ` (${section.capacityKg.toFixed(1)} kg max)` : ''}
            </span>
          )}
        </div>
        {!isOtherPossessions || hasOtherPossessionsItems ? (
          <>
            <GridHeaderRow rowClassName="item-row" columns={GRID_COLUMNS} />
            <div className="action-list">
              {items.map((item) => (
                <ItemRow key={item.key} item={item} onMutate={onMutate} onOpenDetail={onOpenDetail} hasBagOfHolding={hasBagOfHolding} />
              ))}
            </div>
          </>
        ) : null}
        {isOtherPossessions && (
          <button type="button" className="reveal inventory-empty-prompt" onClick={openOtherPossessions}>
            + Add other possessions, treasure, or holdings for your character in this section.
          </button>
        )}
      </section>
    );
  };

  const openCoins = () =>
    onOpenDetail({
      kind: 'entityDetail',
      name: 'Coins',
      metadata: [],
      description: '',
      actionBar: <CoinsPanel coins={sheet.coins} onMutate={onMutate} />,
      refreshActionBar: (latestSheet) => <CoinsPanel coins={latestSheet.coins} onMutate={onMutate} />,
    });

  return (
    <div className="inventory-tab">
      <div className="inventory-overview">
        {encumbrance.trackWeight && (
          <div className={`inventory-weight-bar${encumbrance.overloaded ? ' inventory-weight-bar--overloaded' : ''}`}>
            <span className="inventory-weight-bar__label">Weight Carried:</span>
            <span className="inventory-weight-bar__value">
              {encumbrance.carriedWeightKg.toFixed(1)} / {encumbrance.capacityKg.toFixed(1)} kg
            </span>
            {encumbrance.overloaded && <span className="inventory-weight-bar__status">Overloaded</span>}
          </div>
        )}
        <CoinChips coins={sheet.coins} onOpenCoins={openCoins} />
      </div>

      <div className="tab-actions-row inventory-actions-row">
        <SearchField value={search} onChange={setSearch} placeholder="Search items" />
        <button
          type="button"
          className={`mutate inventory-filter-button${hasActiveItemAdvancedFilters(advancedFilters) ? ' inventory-filter-button--active' : ''}`}
          aria-label="Advanced filters"
          aria-pressed={showAdvancedFilters}
          onClick={() => setShowAdvancedFilters((current) => !current)}
        >
          <FrameIcon className="inventory-filter-button__icon" svg={filterIcon} aria-hidden />
        </button>
        <button type="button" className="mutate inventory-manage-button" onClick={onOpenManageInventory}>
          Manage Inventory
        </button>
      </div>

      {showAdvancedFilters ? (
        <GridScroll className="inventory-tab__list">
          <ItemsAdvancedFilters filters={advancedFilters} onChange={setAdvancedFilters} />
        </GridScroll>
      ) : (
        <>
          <div className="grid-filter-row">
            <FilterChips chips={chips} activeChip={activeSection} onSelect={setActiveSection} />
          </div>

          <GridScroll className="inventory-tab__list">
            {showSection('EQUIPMENT') && renderSection('EQUIPMENT')}
            {showSection('BACKPACK') && renderSection('BACKPACK')}
            {hasBagOfHolding && showSection('BAG_OF_HOLDING') && renderSection('BAG_OF_HOLDING')}
            {showSection('ATTUNEMENT') && (
              <section>
                <div className="inventory-section-heading">
                  <h3 className="inventory-section-heading__label">Attunement</h3>
                </div>
                <AttunementSection items={sheet.items} onMutate={onMutate} />
              </section>
            )}
            {showSection('OTHER_POSSESSIONS') && renderSection('OTHER_POSSESSIONS')}
          </GridScroll>
        </>
      )}
    </div>
  );
}

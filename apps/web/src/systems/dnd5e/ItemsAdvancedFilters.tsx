import type { Item } from '../../sheet/api';

export type ItemAdvancedFilterState = {
  equippedOnly: boolean;
  attunedOnly: boolean;
  requiresAttunementOnly: boolean;
};

export const EMPTY_ITEM_ADVANCED_FILTERS: ItemAdvancedFilterState = {
  equippedOnly: false,
  attunedOnly: false,
  requiresAttunementOnly: false,
};

export function hasActiveItemAdvancedFilters(filters: ItemAdvancedFilterState): boolean {
  return filters.equippedOnly || filters.attunedOnly || filters.requiresAttunementOnly;
}

export function matchesItemAdvancedFilters(item: Item, filters: ItemAdvancedFilterState): boolean {
  if (filters.equippedOnly && !item.equipped) {
    return false;
  }
  if (filters.attunedOnly && !item.attuned) {
    return false;
  }
  if (filters.requiresAttunementOnly && !item.requiresAttunement) {
    return false;
  }
  return true;
}

type ItemsAdvancedFiltersProps = {
  filters: ItemAdvancedFilterState;
  onChange: (filters: ItemAdvancedFilterState) => void;
};

/**
 * Same treatment as `SpellsAdvancedFilters` — replaces the kind chips and item
 * list while open, not a sidebar. Only three boolean dimensions exist on
 * `Item` today (equipped/attuned/requiresAttunement); D&D Beyond's own
 * inventory filter also has type/tag/rarity/attack-range dimensions, but
 * none of those are exposed on this app's `Item` yet (same documented gap as
 * `SpellsAdvancedFilters`'s own doc comment for Tags/Conditions/Attack Type) —
 * left out rather than guessed.
 */
export function ItemsAdvancedFilters({ filters, onChange }: ItemsAdvancedFiltersProps) {
  return (
    <div className="inventory-advanced-filters">
      <div className="inventory-advanced-filters__group">
        <h4 className="inventory-advanced-filters__label">Show Only</h4>
        <div className="filter-chips">
          <button
            type="button"
            className="filter-chip"
            aria-pressed={filters.equippedOnly}
            onClick={() => onChange({ ...filters, equippedOnly: !filters.equippedOnly })}
          >
            Equipped
          </button>
          <button
            type="button"
            className="filter-chip"
            aria-pressed={filters.attunedOnly}
            onClick={() => onChange({ ...filters, attunedOnly: !filters.attunedOnly })}
          >
            Attuned
          </button>
          <button
            type="button"
            className="filter-chip"
            aria-pressed={filters.requiresAttunementOnly}
            onClick={() => onChange({ ...filters, requiresAttunementOnly: !filters.requiresAttunementOnly })}
          >
            Requires Attunement
          </button>
        </div>
      </div>
      {hasActiveItemAdvancedFilters(filters) && (
        <button type="button" className="reveal inventory-advanced-filters__clear" onClick={() => onChange(EMPTY_ITEM_ADVANCED_FILTERS)}>
          Clear Filters
        </button>
      )}
    </div>
  );
}

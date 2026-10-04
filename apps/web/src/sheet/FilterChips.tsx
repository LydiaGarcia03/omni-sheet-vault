import type { ReactNode } from 'react';

type Chip = { id: string; label: ReactNode; ariaLabel?: string };

type FilterChipsProps = {
  chips: Chip[];
  activeChip: string;
  onSelect: (id: string) => void;
};

/**
 * Shared primitive (ui-design-system.md) — a mutually exclusive filter row above a
 * list. Styled via `.filter-chips`/`.filter-chip` (frames.css) — square badges with
 * a hover and active state, DOM-measured against D&D Beyond's own Actions/Spells/
 * Features filter row, not the pill shape this used to guess at. First consumer is
 * the Actions tab's all/attack/action/bonus action/reaction/other/limited-use row.
 * `label` accepts a `ReactNode` (not just text) and `ariaLabel` names an icon-only
 * chip — D&D Beyond's own equivalent (`TabFilter`) takes an icon component as a
 * filter's label the same way, for the Spells tab's Concentration/Ritual chips.
 */
export function FilterChips({ chips, activeChip, onSelect }: FilterChipsProps) {
  return (
    <div className="filter-chips">
      {chips.map((chip) => {
        const isActive = chip.id === activeChip;
        return (
          <button
            key={chip.id}
            type="button"
            className="filter-chip"
            aria-pressed={isActive}
            aria-label={chip.ariaLabel}
            onClick={() => onSelect(chip.id)}
          >
            {chip.label}
          </button>
        );
      })}
    </div>
  );
}

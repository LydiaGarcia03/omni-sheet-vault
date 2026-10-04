import type { Spell } from '../../sheet/api';
import { abilityAbbreviation } from './abilities';
import { damageTypeLabel } from './damageTypes';

export type SpellAdvancedFilterState = {
  castingTimes: string[];
  saveAbilities: string[];
  damageTypes: string[];
};

export const EMPTY_ADVANCED_FILTERS: SpellAdvancedFilterState = { castingTimes: [], saveAbilities: [], damageTypes: [] };

export function hasActiveAdvancedFilters(filters: SpellAdvancedFilterState): boolean {
  return filters.castingTimes.length > 0 || filters.saveAbilities.length > 0 || filters.damageTypes.length > 0;
}

export function matchesAdvancedFilters(spell: Spell, filters: SpellAdvancedFilterState): boolean {
  if (filters.castingTimes.length > 0 && !filters.castingTimes.includes(spell.castingTime)) {
    return false;
  }
  if (filters.saveAbilities.length > 0 && (!spell.saveAbility || !filters.saveAbilities.includes(spell.saveAbility))) {
    return false;
  }
  if (filters.damageTypes.length > 0 && (!spell.damageType || !filters.damageTypes.includes(spell.damageType))) {
    return false;
  }
  return true;
}

function toggle(list: string[], value: string): string[] {
  return list.includes(value) ? list.filter((entry) => entry !== value) : [...list, value];
}

type SpellsAdvancedFiltersProps = {
  spells: Spell[];
  filters: SpellAdvancedFilterState;
  onChange: (filters: SpellAdvancedFilterState) => void;
};

type FilterGroupProps = {
  label: string;
  options: string[];
  selected: string[];
  formatOption: (value: string) => string;
  onToggle: (value: string) => void;
};

function FilterGroup({ label, options, selected, formatOption, onToggle }: FilterGroupProps) {
  if (options.length === 0) {
    return null;
  }
  return (
    <div className="spells-advanced-filters__group">
      <h4 className="spells-advanced-filters__label">{label}</h4>
      <div className="filter-chips">
        {options.map((option) => (
          <button
            key={option}
            type="button"
            className="filter-chip"
            aria-pressed={selected.includes(option)}
            onClick={() => onToggle(option)}
          >
            {formatOption(option)}
          </button>
        ))}
      </div>
    </div>
  );
}

/**
 * D&D Beyond's own advanced filter (`SpellsFilter.tsx`'s `renderAdvancedFilters`)
 * replaces the level tabs and spell list with a set of checkbox-array filters
 * (Tags/Conditions/Casting Time/Saving Throw/Damage Types/Attack Type) — not a
 * sidebar. This app only tracks enough data for three of those dimensions
 * (Tags/Conditions/Attack Type aren't modeled on `Spell` yet, same documented
 * gap as `systems/dnd-5e/references/sheet-fidelity-audit.md`'s "Tracked, waiting on the owner").
 * Options are derived from the character's own spells, not a static list, so a
 * character with no save-DC spells simply never shows a "Saving Throw" group.
 */
export function SpellsAdvancedFilters({ spells, filters, onChange }: SpellsAdvancedFiltersProps) {
  const castingTimes = Array.from(new Set(spells.map((spell) => spell.castingTime))).sort();
  const saveAbilities = Array.from(
    new Set(spells.map((spell) => spell.saveAbility).filter((ability): ability is string => ability != null)),
  ).sort();
  const damageTypes = Array.from(
    new Set(spells.map((spell) => spell.damageType).filter((type): type is string => type != null)),
  ).sort();

  return (
    <div className="spells-advanced-filters">
      <FilterGroup
        label="Casting Time"
        options={castingTimes}
        selected={filters.castingTimes}
        formatOption={(value) => value}
        onToggle={(value) => onChange({ ...filters, castingTimes: toggle(filters.castingTimes, value) })}
      />
      <FilterGroup
        label="Saving Throw"
        options={saveAbilities}
        selected={filters.saveAbilities}
        formatOption={abilityAbbreviation}
        onToggle={(value) => onChange({ ...filters, saveAbilities: toggle(filters.saveAbilities, value) })}
      />
      <FilterGroup
        label="Damage Type"
        options={damageTypes}
        selected={filters.damageTypes}
        formatOption={damageTypeLabel}
        onToggle={(value) => onChange({ ...filters, damageTypes: toggle(filters.damageTypes, value) })}
      />
      {hasActiveAdvancedFilters(filters) && (
        <button type="button" className="reveal spells-advanced-filters__clear" onClick={() => onChange(EMPTY_ADVANCED_FILTERS)}>
          Clear Filters
        </button>
      )}
    </div>
  );
}

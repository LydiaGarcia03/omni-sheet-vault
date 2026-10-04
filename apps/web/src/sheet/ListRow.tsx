import type { KeyboardEvent, ReactNode } from 'react';

type ListRowProps = {
  className: string;
  ariaLabel: string;
  onOpen: () => void;
  children: ReactNode;
};

/**
 * Shared sheet primitive — a list row that opens its Entity Detail on a click
 * anywhere in the row, not just its name cell (D&D Beyond's own behavior).
 * Every real interactive child (roll targets, Cast/equip/remove controls, box
 * tracks) must call `event.stopPropagation()` in its own click handler so it
 * doesn't also trigger `onOpen`. Deliberately does not own divider styling —
 * `AttackRow`/`SpellAttackRow`/`SpellRow`, `ItemRow`/`ExtraRow`, and
 * `FeatureActionRow`/`FeatureTraitRow` each use a genuinely different divider
 * treatment (dotted top skipping the first row, solid bottom on every row,
 * none at all), so each keeps its own row class for that.
 */
export function ListRow({ className, ariaLabel, onOpen, children }: ListRowProps) {
  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onOpen();
    }
  };

  return (
    <div className={className} role="button" tabIndex={0} aria-label={ariaLabel} onClick={onOpen} onKeyDown={handleKeyDown}>
      {children}
    </div>
  );
}

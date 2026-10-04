import type { ReactNode } from 'react';

export type GridColumn = {
  key: string;
  className: string;
  content?: ReactNode;
};

type GridHeaderRowProps = {
  rowClassName: string;
  columns: GridColumn[];
};

/**
 * Shared primitive (ui-design-system.md) — the column-labeled header row above
 * a grid list (Actions/Spells/Inventory tabs' own `.action-list`). Each column
 * keeps its own row-type width class (the data shape genuinely differs per
 * tab) and supplies its own already-styled content; this component only owns
 * the row wrapper and the `aria-hidden` semantics — the header is decorative,
 * screen readers get column context from each row's own labelled controls.
 * Replaces three independently hand-written header rows whose shared cell
 * styling (`.grid-header-cell`, frames.css) had drifted out of sync with each
 * other rather than three genuinely different treatments.
 */
export function GridHeaderRow({ rowClassName, columns }: GridHeaderRowProps) {
  return (
    <div className={`${rowClassName} grid-row--header`} aria-hidden="true">
      {columns.map((column) => (
        <div key={column.key} className={column.className}>
          {column.content}
        </div>
      ))}
    </div>
  );
}

type GridScrollProps = {
  className?: string;
  children: ReactNode;
};

/**
 * Shared primitive — the single scrolling region a tab's grid list lives in
 * (`.grid-scroll`, frames.css), keeping header/filter controls above it fixed
 * while only the row list scrolls — matching D&D Beyond's own `.ct-primary-box`
 * structure (padding on a non-scrolling outer box, `overflow-y: auto` on a
 * separate inner one). `className` adds a tab-specific modifier on top
 * (e.g. Inventory's own extra top margin) without duplicating the scroll rule.
 */
export function GridScroll({ className, children }: GridScrollProps) {
  return <div className={className ? `grid-scroll ${className}` : 'grid-scroll'}>{children}</div>;
}

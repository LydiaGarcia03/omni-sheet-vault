import type { ReactNode } from 'react';
import type { ProgressionTable } from './draftApi';

type ProgressionPanelProps = {
  table: ProgressionTable;
  /** Starts opened instead of collapsed. */
  defaultOpen?: boolean;
  /** A line above the table, e.g. the level a level up reaches. */
  caption?: ReactNode;
};

/** A class's table, collapsed until opened: the current level is marked, the levels after it preview what comes next. */
export function ProgressionPanel({ table, defaultOpen = false, caption }: ProgressionPanelProps) {
  return (
    <details className="builder-frame builder-progression" open={defaultOpen || undefined}>
      <summary className="builder-frame__title builder-progression__summary">Class progression · {table.name}</summary>
      {caption && <p className="builder-progression__caption">{caption}</p>}
      <div className="builder-progression__scroll">
        <table className="builder-progression__table">
          <thead>
            <tr>
              {table.columns.map((column) => (
                <th key={column} scope="col">
                  {column}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {table.rows.map((row, index) => {
              const level = index + 1;
              const state = level === table.currentLevel ? ' is-current' : level < table.currentLevel ? ' is-reached' : '';
              return (
                <tr key={level} className={`builder-progression__row${state}`} aria-current={level === table.currentLevel ? 'true' : undefined}>
                  {row.map((cell, column) => (
                    <td key={column}>{cell}</td>
                  ))}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </details>
  );
}

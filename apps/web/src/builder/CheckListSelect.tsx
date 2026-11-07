import { useEffect, useId, useRef, useState, type ReactNode } from 'react';
import { usePopupDirection } from './usePopupDirection';

export type CheckListOption = { key: string; label: string; note?: string; checked: boolean; locked?: boolean };
export type CheckListGroup = { label: string; options: CheckListOption[] };

type CheckListSelectProps = {
  ariaLabel: string;
  /** What the closed trigger shows, e.g. the chosen books as tags. */
  summary: ReactNode;
  groups: CheckListGroup[];
  onToggle: (key: string) => void;
  /** Shown in the popup instead of the list, e.g. while loading. */
  message?: string;
};

/** A select of checkboxes in the source select's look; the popup is fixed to the trigger, so a scrolling frame can't clip it. */
export function CheckListSelect({ ariaLabel, summary, groups, onToggle, message }: CheckListSelectProps) {
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const popup = useRef<HTMLDivElement>(null);
  const { direction, style } = usePopupDirection(open, trigger, popup);
  const listId = `${useId()}-list`;

  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (event: MouseEvent) => {
      if (!root.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    const escape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false);
        trigger.current?.focus();
      }
    };
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', escape);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', escape);
    };
  }, [open]);

  return (
    <div className={`source-select check-list-select${open ? ' is-open' : ''}`} ref={root}>
      <button
        ref={trigger}
        type="button"
        className="source-select__trigger check-list-select__trigger"
        aria-label={ariaLabel}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={listId}
        onClick={() => setOpen(!open)}
      >
        <span className="check-list-select__summary">{summary}</span>
        <svg className="source-select__caret" viewBox="0 0 10 10" aria-hidden="true">
          <path d="M1 3l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.6" />
        </svg>
      </button>
      {open && (
        <div ref={popup} className={`source-select__popup${direction === 'up' ? ' is-up' : ''}`} style={style}>
          <ul id={listId} className="source-select__list" role="listbox" aria-label={ariaLabel} aria-multiselectable>
            {message && <li className="source-select__empty">{message}</li>}
            {groups.map((group) => (
              <CheckListGroupRows key={group.label} group={group} onToggle={onToggle} />
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

function CheckListGroupRows({ group, onToggle }: { group: CheckListGroup; onToggle: (key: string) => void }) {
  return (
    <>
      <li className="source-select__group" role="presentation">
        {group.label}
      </li>
      {group.options.map((option) => (
        <li
          key={option.key}
          role="option"
          aria-selected={option.checked}
          aria-disabled={option.locked || undefined}
          className={`source-select__option check-list-select__option${option.locked ? ' is-blocked' : ''}`}
          onClick={() => !option.locked && onToggle(option.key)}
        >
          <span className="check-list-select__box" aria-hidden="true" />
          <span className="source-select__name">
            {option.label}
            {option.note && <span className="source-select__summary">{option.note}</span>}
          </span>
        </li>
      ))}
    </>
  );
}

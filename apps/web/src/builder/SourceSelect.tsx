import { useEffect, useId, useMemo, useRef, useState, type KeyboardEvent, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { useLeadingSources, useSourceCode } from '../catalogue/SourceCodes';
import { catalogueEntryLabel } from '../sheet/CatalogueSourceLabel';
import { usePopupDirection } from './usePopupDirection';

/** Above this many options the popup gets a filter box. */
const SEARCH_THRESHOLD = 8;
const OTHER_GROUP = 'Other';

/** `summary` says what the option gives; it shows under the name and is searchable. */
export type SourceSelectOption = { key: string; label: string; sourceBook: string | null; summary?: string | null };

type CommonProps = {
  options: SourceSelectOption[];
  placeholder: string;
  ariaLabel: string;
  disabled?: boolean;
};

type SingleProps = CommonProps & {
  multiple?: false;
  value: string | null;
  onChange: (key: string | null) => void;
  clearable?: boolean;
};

type MultipleProps = CommonProps & {
  multiple: true;
  values: string[];
  max: number;
  onChange: (keys: string[]) => void;
};

type SourceSelectProps = SingleProps | MultipleProps;

type Row = { option: SourceSelectOption; index: number };
type Group = { book: string; rows: Row[] };

/**
 * A styleable select for catalogue entries: options grouped by source book, each showing
 * the book's code with its full name on hover. Single pick, or several up to a maximum.
 */
export function SourceSelect(props: SourceSelectProps) {
  const { options, placeholder, ariaLabel, disabled = false } = props;
  const id = useId();
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [active, setActive] = useState(0);
  const root = useRef<HTMLDivElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const search = useRef<HTMLInputElement>(null);
  const list = useRef<HTMLUListElement>(null);
  const popup = useRef<HTMLDivElement>(null);
  const direction = usePopupDirection(open, trigger, popup);
  const codeOf = useSourceCode();
  const leading = useLeadingSources();
  const searchable = options.length > SEARCH_THRESHOLD;
  const selectedKeys = props.multiple ? props.values : props.value ? [props.value] : [];
  const selected = selectedKeys.flatMap((key) => options.filter((option) => option.key === key));
  const full = props.multiple && props.values.length >= props.max;
  const grouped = options.some((option) => option.sourceBook);

  const groups = useMemo(() => groupByBook(options, query, codeOf, leading), [options, query, codeOf, leading]);
  const visible = useMemo(() => groups.flatMap((group) => group.rows.map((row) => row.option)), [groups]);

  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (event: MouseEvent) => {
      if (!root.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  useEffect(() => {
    if (open) {
      (searchable ? search.current : trigger.current)?.focus();
    }
  }, [open, searchable]);

  useEffect(() => {
    list.current?.querySelector('.is-active')?.scrollIntoView?.({ block: 'nearest' });
  }, [active, open]);

  const show = () => {
    setQuery('');
    const firstSelected = groupByBook(options, '', codeOf, leading)
      .flatMap((group) => group.rows)
      .findIndex((row) => selectedKeys.includes(row.option.key));
    setActive(Math.max(0, firstSelected));
    setOpen(true);
  };
  const hide = (refocus: boolean) => {
    setOpen(false);
    if (refocus) {
      trigger.current?.focus();
    }
  };
  const isBlocked = (key: string) => Boolean(full) && !selectedKeys.includes(key);
  const pick = (key: string) => {
    if (!props.multiple) {
      props.onChange(key);
      hide(true);
    } else if (props.values.includes(key)) {
      props.onChange(props.values.filter((value) => value !== key));
    } else if (!isBlocked(key)) {
      props.onChange([...props.values, key]);
    }
  };
  const clear = () => {
    if (props.multiple) {
      props.onChange([]);
    } else {
      props.onChange(null);
      hide(true);
    }
  };

  const onKeyDown = (event: KeyboardEvent) => {
    if (!open) {
      if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(event.key)) {
        event.preventDefault();
        show();
      }
      return;
    }
    const last = visible.length - 1;
    const moves: Record<string, number> = { ArrowDown: Math.min(last, active + 1), ArrowUp: Math.max(0, active - 1), Home: 0, End: last };
    if (event.key in moves) {
      event.preventDefault();
      setActive(moves[event.key]);
    } else if (event.key === 'Enter') {
      event.preventDefault();
      if (visible[active]) {
        pick(visible[active].key);
      }
    } else if (event.key === 'Escape') {
      event.preventDefault();
      hide(true);
    } else if (event.key === 'Tab') {
      hide(false);
    } else if (!searchable && event.key.length === 1) {
      const match = visible.findIndex((option) => option.label.toLowerCase().startsWith(event.key.toLowerCase()));
      if (match >= 0) {
        setActive(match);
      }
    }
  };

  const listId = `${id}-list`;
  const optionId = (index: number) => `${id}-option-${index}`;
  const activeDescendant = open && visible[active] ? optionId(active) : undefined;
  const clearRow = !props.multiple && props.clearable && selected.length > 0 && !query;

  return (
    <div className={`source-select${open ? ' is-open' : ''}`} ref={root}>
      <button
        ref={trigger}
        type="button"
        className="source-select__trigger"
        role="combobox"
        aria-label={ariaLabel}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={listId}
        aria-activedescendant={searchable ? undefined : activeDescendant}
        disabled={disabled}
        onClick={() => (open ? hide(true) : show())}
        onKeyDown={onKeyDown}
      >
        <TriggerValue selected={selected} placeholder={placeholder} />
        {props.multiple && (
          <span className={`source-select__count${full ? ' is-full' : ''}`}>
            {props.values.length}/{props.max}
          </span>
        )}
        <svg className="source-select__caret" viewBox="0 0 10 10" aria-hidden="true">
          <path d="M1 3l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.6" />
        </svg>
      </button>
      {open && (
        <div ref={popup} className={`source-select__popup${direction === 'up' ? ' is-up' : ''}`}>
          {searchable && (
            <div className="source-select__search">
              <input
                ref={search}
                type="text"
                placeholder="Filter…"
                aria-label={`Filter ${ariaLabel}`}
                aria-controls={listId}
                aria-activedescendant={activeDescendant}
                autoComplete="off"
                value={query}
                onChange={(event) => {
                  setQuery(event.target.value);
                  setActive(0);
                }}
                onKeyDown={onKeyDown}
              />
            </div>
          )}
          {props.multiple && (
            <div className="source-select__status">
              <span role="status">
                {props.values.length} of {props.max} chosen{full && ' · remove one to pick another'}
              </span>
              {selected.length > 0 && (
                <button type="button" className="source-select__status-clear" onMouseDown={(event) => event.preventDefault()} onClick={clear}>
                  Clear
                </button>
              )}
            </div>
          )}
          <ul
            ref={list}
            id={listId}
            className="source-select__list"
            role="listbox"
            aria-label={ariaLabel}
            aria-multiselectable={props.multiple || undefined}
            onMouseDown={(event) => event.preventDefault()}
          >
            {clearRow && (
              <li className="source-select__clear" role="option" aria-selected={false} onClick={clear}>
                Clear selection
              </li>
            )}
            {groups.map((group) => (
              <GroupRows key={group.book} label={grouped ? group.book : null}>
                {group.rows.map(({ option, index }) => (
                  <li
                    key={option.key}
                    id={optionId(index)}
                    role="option"
                    aria-selected={selectedKeys.includes(option.key)}
                    aria-disabled={isBlocked(option.key) || undefined}
                    aria-label={catalogueEntryLabel(option.label, option.sourceBook)}
                    className={`source-select__option${index === active ? ' is-active' : ''}${isBlocked(option.key) ? ' is-blocked' : ''}`}
                    onMouseMove={() => index !== active && setActive(index)}
                    onClick={() => pick(option.key)}
                  >
                    <span className="source-select__check" aria-hidden="true">
                      ✓
                    </span>
                    <span className="source-select__name">
                      <Highlighted text={option.label} query={query} />
                      {option.summary && <span className="source-select__summary">{option.summary}</span>}
                    </span>
                    <SourceBadge sourceBook={option.sourceBook} />
                  </li>
                ))}
              </GroupRows>
            ))}
            {visible.length === 0 && <li className="source-select__empty">No match for “{query}”</li>}
          </ul>
        </div>
      )}
    </div>
  );
}

function TriggerValue({ selected, placeholder }: { selected: SourceSelectOption[]; placeholder: string }) {
  if (selected.length === 0) {
    return <span className="source-select__value source-select__placeholder">{placeholder}</span>;
  }
  if (selected.length === 1) {
    return (
      <>
        <span className="source-select__value">{selected[0].label}</span>
        <SourceBadge sourceBook={selected[0].sourceBook} />
      </>
    );
  }
  return <span className="source-select__value">{selected.map((option) => option.label).join(', ')}</span>;
}

function GroupRows({ label, children }: { label: string | null; children: ReactNode }) {
  return (
    <>
      {label && (
        <li className="source-select__group" role="presentation">
          {label}
        </li>
      )}
      {children}
    </>
  );
}

/** Groups matching options by book: leading books first, then the largest group, keeping each group's own order. */
function groupByBook(options: SourceSelectOption[], query: string, codeOf: (book: string) => string | null, leading: string[]): Group[] {
  const needle = query.trim().toLowerCase();
  const byBook = new Map<string, SourceSelectOption[]>();
  for (const option of options) {
    const book = option.sourceBook ?? OTHER_GROUP;
    const code = option.sourceBook ? codeOf(option.sourceBook) : null;
    const matches =
      !needle ||
      option.label.toLowerCase().includes(needle) ||
      (option.sourceBook?.toLowerCase().includes(needle) ?? false) ||
      (option.summary?.toLowerCase().includes(needle) ?? false) ||
      code?.toLowerCase() === needle;
    if (matches) {
      byBook.set(book, [...(byBook.get(book) ?? []), option]);
    }
  }
  const rank = (book: string) => {
    const at = leading.indexOf(book);
    return at >= 0 ? at : leading.length;
  };
  const ordered = [...byBook.entries()].sort(
    ([bookA, rowsA], [bookB, rowsB]) =>
      rank(bookA) - rank(bookB) ||
      Number(bookA === OTHER_GROUP) - Number(bookB === OTHER_GROUP) ||
      rowsB.length - rowsA.length ||
      bookA.localeCompare(bookB),
  );
  let index = 0;
  return ordered.map(([book, rows]) => ({ book, rows: rows.map((option) => ({ option, index: index++ })) }));
}

function Highlighted({ text, query }: { text: string; query: string }) {
  const needle = query.trim().toLowerCase();
  const at = needle ? text.toLowerCase().indexOf(needle) : -1;
  if (at < 0) {
    return <>{text}</>;
  }
  const end = at + needle.length;
  return (
    <>
      {text.slice(0, at)}
      <mark>{text.slice(at, end)}</mark>
      {text.slice(end)}
    </>
  );
}

/** The book's code as a small badge; hovering or tapping it shows the full book name in a floating tooltip. */
export function SourceBadge({ sourceBook }: { sourceBook: string | null }) {
  const codeOf = useSourceCode();
  const badge = useRef<HTMLSpanElement>(null);
  const tapped = useRef(false);
  const [tip, setTip] = useState<{ top: number; left: number; below: boolean } | null>(null);

  useEffect(() => {
    if (!tip) {
      return;
    }
    const hide = () => setTip(null);
    window.addEventListener('scroll', hide, true);
    return () => window.removeEventListener('scroll', hide, true);
  }, [tip]);

  if (!sourceBook) {
    return null;
  }
  const place = () => {
    const rect = badge.current?.getBoundingClientRect();
    if (rect) {
      const below = rect.top < 40;
      setTip({ top: below ? rect.bottom + 6 : rect.top - 6, left: rect.right, below });
    }
  };

  return (
    <span
      ref={badge}
      className="source-select__src"
      onMouseEnter={place}
      onMouseLeave={() => setTip(null)}
      onTouchStart={() => {
        tapped.current = true;
        if (tip) {
          setTip(null);
        } else {
          place();
        }
      }}
      onClick={(event) => {
        if (tapped.current) {
          tapped.current = false;
          event.stopPropagation();
        }
      }}
    >
      <span className="source-select__code">{codeOf(sourceBook) ?? sourceBook}</span>
      {tip &&
        createPortal(
          <div role="tooltip" className={`source-select-tip${tip.below ? ' is-below' : ''}`} style={{ top: tip.top, left: tip.left }}>
            {sourceBook}
          </div>,
          document.body,
        )}
    </span>
  );
}

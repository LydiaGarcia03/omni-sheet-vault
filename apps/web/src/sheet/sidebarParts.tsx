import { useEffect, useState, type ReactNode } from 'react';

type SidebarHeaderProps = {
  title: ReactNode;
  /** A signed value drawn after the title, e.g. a saving throw's +5. */
  modifier?: number;
  /** `parens` draws the value smaller and in parentheses, as the ability pane does: "Strength 17 (+3)". */
  modifierFormat?: 'plain' | 'parens';
  /** The small line above the heading naming the panel this one belongs to. */
  parent?: string;
  onOpenParent?: () => void;
  icon?: ReactNode;
};

/** D&D Beyond's ct-sidebar__header: an optional parent line, a 32px preview icon, and the heading with its value. */
export function SidebarHeader({ title, modifier, modifierFormat = 'plain', parent, onOpenParent, icon }: SidebarHeaderProps) {
  return (
    <header className="sidebar-header">
      {parent &&
        (onOpenParent ? (
          <button type="button" className="sidebar-header__parent" onClick={onOpenParent}>
            {parent}
          </button>
        ) : (
          <div className="sidebar-header__parent">{parent}</div>
        ))}
      <div className={icon ? 'sidebar-header__primary' : 'sidebar-header__primary sidebar-header__primary--plain'}>
        {icon && <div className="sidebar-header__preview">{icon}</div>}
        <h1 className="sidebar-header__heading">
          {title}
          {modifier !== undefined &&
            (modifierFormat === 'parens' ? (
              <span className="sidebar-header__modifier sidebar-header__modifier--parens">
                ({modifier < 0 ? '−' : '+'}
                {Math.abs(modifier)})
              </span>
            ) : (
              <span className="sidebar-header__modifier">
                <span className="sidebar-header__sign">{modifier < 0 ? '−' : '+'}</span>
                {Math.abs(modifier)}
              </span>
            ))}
        </h1>
      </div>
    </header>
  );
}

export function ChevronDown() {
  return (
    <svg className="sidebar-customize__chevron" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" aria-hidden="true">
      <path d="M233.4 406.6c12.5 12.5 32.8 12.5 45.3 0l192-192c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L256 338.7 86.6 169.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l192 192z" />
    </svg>
  );
}

type CustomizeProps = {
  children: ReactNode;
  label?: string;
  /** Controls the section from outside, e.g. a pencil that opens it; uncontrolled when absent. */
  open?: boolean;
  onToggle?: (open: boolean) => void;
};

/** D&D Beyond's collapsed "Customize" section (a custom skill's reads "Edit"): a themed details with its editors inside. */
export function Customize({ children, label = 'Customize', open, onToggle }: CustomizeProps) {
  return (
    <details
      className="sidebar-customize"
      open={open}
      onToggle={onToggle ? (event) => onToggle((event.currentTarget as HTMLDetailsElement).open) : undefined}
    >
      <summary className="sidebar-customize__summary">
        {label}
        <ChevronDown />
      </summary>
      <div className="sidebar-customize__content">{children}</div>
    </details>
  );
}

type SidebarCollapsibleProps = { heading: string; defaultOpen?: boolean; callout?: ReactNode; children: ReactNode };

/** D&D Beyond's ddbc-collapsible: a grey header (with an optional right-hand callout) and a theme-colored left edge. */
export function SidebarCollapsible({ heading, defaultOpen = false, callout, children }: SidebarCollapsibleProps) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <section className={open ? 'sidebar-collapsible is-open' : 'sidebar-collapsible'}>
      <button type="button" className="sidebar-collapsible__header" aria-expanded={open} onClick={() => setOpen(!open)}>
        <span className="sidebar-collapsible__heading">{heading}</span>
        {callout != null && <span className="sidebar-collapsible__callout">{callout}</span>}
        <ChevronDown />
      </button>
      {open && <div className="sidebar-collapsible__content">{children}</div>}
    </section>
  );
}

function PagerChevron({ direction }: { direction: 'previous' | 'next' }) {
  return (
    <svg className="sidebar-pager__chevron" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 512" aria-hidden="true">
      <path
        d={
          direction === 'previous'
            ? 'M9.4 233.4c-12.5 12.5-12.5 32.8 0 45.3l192 192c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L77.3 256 246.6 86.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0l-192 192z'
            : 'M310.6 233.4c12.5 12.5 12.5 32.8 0 45.3l-192 192c-12.5 12.5-32.8 12.5-45.3 0s-12.5-32.8 0-45.3L242.7 256 73.4 86.6c-12.5-12.5-12.5-32.8 0-45.3s32.8-12.5 45.3 0l192 192z'
        }
      />
    </svg>
  );
}

/** D&D Beyond's PREV / NEXT on the pane's top edge, stepping through a panel's siblings; null at an end. */
export function SidebarPager({ onPrevious, onNext }: { onPrevious: (() => void) | null; onNext: (() => void) | null }) {
  return (
    <nav className="sidebar-pager" aria-label="Previous and next">
      <button type="button" className="sidebar-pager__button" disabled={!onPrevious} onClick={onPrevious ?? undefined}>
        <PagerChevron direction="previous" />
        <span>Prev</span>
      </button>
      <button type="button" className="sidebar-pager__button" disabled={!onNext} onClick={onNext ?? undefined}>
        <span>Next</span>
        <PagerChevron direction="next" />
      </button>
    </nav>
  );
}

/** The grey box that groups a panel's editors (ct-editor-box). */
export function EditorBox({ children }: { children: ReactNode }) {
  return <div className="sidebar-editor-box">{children}</div>;
}

function parseValue(text: string): number | null {
  if (text.trim() === '') {
    return null;
  }
  const parsed = Number(text);
  return Number.isInteger(parsed) ? parsed : null;
}

type ValueEditorRowProps = {
  label: string;
  value: number | null;
  notes: string;
  /** Called on blur when the value or the notes changed; an empty value is null. */
  onCommit: (value: number | null, notes: string) => void;
  /** Replaces the number field, e.g. a proficiency level select. */
  control?: ReactNode;
};

/** One ct-value-editor row: a label, a value field and a source-notes field, saved when a field loses focus. */
export function ValueEditorRow({ label, value, notes, onCommit, control }: ValueEditorRowProps) {
  const [valueText, setValueText] = useState(value === null ? '' : String(value));
  const [notesText, setNotesText] = useState(notes);
  useEffect(() => setValueText(value === null ? '' : String(value)), [value]);
  useEffect(() => setNotesText(notes), [notes]);

  const commit = () => {
    const nextValue = control ? value : parseValue(valueText);
    if (nextValue !== value || notesText !== notes) {
      onCommit(nextValue, notesText);
    }
  };

  return (
    <div className="sidebar-value-editor">
      <div className="sidebar-value-editor__label">{label}</div>
      <div className="sidebar-value-editor__fields">
        {control ?? (
          <input
            className="sidebar-value-editor__input sidebar-value-editor__input--value"
            type="number"
            aria-label={label}
            value={valueText}
            onChange={(event) => setValueText(event.target.value)}
            onBlur={commit}
          />
        )}
        <input
          className="sidebar-value-editor__input sidebar-value-editor__input--notes"
          type="text"
          placeholder="Enter Source Notes..."
          aria-label={`${label} source notes`}
          value={notesText}
          onChange={(event) => setNotesText(event.target.value)}
          onBlur={commit}
        />
      </div>
    </div>
  );
}

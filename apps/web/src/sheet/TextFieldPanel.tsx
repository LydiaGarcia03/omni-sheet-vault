import { useLayoutEffect, useRef, useState } from 'react';
import type { TextFieldEntry, TextFieldRequest } from './api';
import { SidebarHeader } from './sidebarParts';

type SuggestionsTableProps = {
  title: string;
  die: string;
  entries: string[];
  onPick: (text: string) => void;
};

/** D&D Beyond's suggestions table: a die column, the entries striped, a Random pick and a "+ Add" per row. */
function SuggestionsTable({ title, die, entries, onPick }: SuggestionsTableProps) {
  return (
    <table className="text-field-pane__table">
      <thead>
        <tr>
          <th className="text-field-pane__die">{die}</th>
          <th>{title}</th>
          <th className="text-field-pane__action">
            <button type="button" className="text-field-pane__button" onClick={() => onPick(entries[Math.floor(Math.random() * entries.length)])}>
              Random
            </button>
          </th>
        </tr>
      </thead>
      <tbody>
        {entries.map((entry, index) => (
          <tr key={index}>
            <td className="text-field-pane__die">{index + 1}</td>
            <td>{entry}</td>
            <td className="text-field-pane__action">
              <button type="button" className="text-field-pane__button" aria-label={`Add suggestion ${index + 1}`} onClick={() => onPick(entry)}>
                + Add
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

/** A textarea that grows with its text, as D&D Beyond's does. */
function GrowingTextarea({ value, placeholder, onChange, onBlur }: { value: string; placeholder: string; onChange: (text: string) => void; onBlur: () => void }) {
  const ref = useRef<HTMLTextAreaElement>(null);
  useLayoutEffect(() => {
    const element = ref.current;
    if (element) {
      element.style.height = 'auto';
      element.style.height = `${Math.max(42, element.scrollHeight + 2)}px`;
    }
  }, [value]);
  return (
    <textarea
      ref={ref}
      className="text-field-pane__textarea"
      value={value}
      placeholder={placeholder}
      onChange={(event) => onChange(event.target.value)}
      onBlur={onBlur}
    />
  );
}

/**
 * A single-field pane's editor, saved on blur. A suggestion is appended on a new line, and saved at once since
 * the textarea never had focus.
 */
function SingleField({ label, value, helperPrompt, onSave, suggestions, suggestionDie }: TextFieldEntry) {
  const [draft, setDraft] = useState(value);
  const commit = (next: string) => next !== value && onSave(next);
  const append = (text: string) => {
    const next = draft.trim() ? `${draft}\n${text}` : text;
    setDraft(next);
    commit(next);
  };
  const hasSuggestions = suggestions != null && suggestions.length > 0;

  return (
    <>
      <GrowingTextarea value={draft} placeholder={`Enter any ${label.toLowerCase()} here!`} onChange={setDraft} onBlur={() => commit(draft)} />
      {!hasSuggestions && helperPrompt && <p className="text-field-pane__prompt">{helperPrompt}</p>}
      {hasSuggestions && (
        <section className="text-field-pane__suggestions">
          <h2 className="text-field-pane__subheading">Suggestions</h2>
          {helperPrompt && <p className="text-field-pane__prompt">{helperPrompt}</p>}
          <SuggestionsTable title={label} die={suggestionDie ?? `d${suggestions.length}`} entries={suggestions} onPick={append} />
        </section>
      )}
    </>
  );
}

/** One row of a combined pane: a bold label over an input, or a select when the field has fixed choices. */
function DetailField({ label, value, onSave, options }: TextFieldEntry) {
  const [draft, setDraft] = useState(value);
  const choices = options && value && !options.includes(value) ? [value, ...options] : options;
  return (
    <label className="text-field-pane__entry">
      <span className="text-field-pane__label">{label}</span>
      {choices ? (
        <select className="text-field-pane__select" value={value} onChange={(event) => onSave(event.target.value)}>
          <option value="">--</option>
          {choices.map((choice) => (
            <option key={choice} value={choice}>
              {choice}
            </option>
          ))}
        </select>
      ) : (
        <input
          className="text-field-pane__input"
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          onBlur={() => draft !== value && onSave(draft)}
        />
      )}
    </label>
  );
}

/**
 * The Text Field mold (ui-design-system.md's seventh) as D&D Beyond draws it: a single field is a growing textarea
 * with its suggestions table; several fields are D&D Beyond's "Characteristics and Details" list of labeled inputs.
 */
export function TextFieldPanel({ title, fields }: TextFieldRequest) {
  return (
    <div className="text-field-pane">
      <SidebarHeader title={title} />
      {fields.length === 1 ? (
        <SingleField {...fields[0]} />
      ) : (
        <div className="text-field-pane__entries">
          {fields.map((field) => (
            <DetailField key={field.label} {...field} />
          ))}
        </div>
      )}
    </div>
  );
}

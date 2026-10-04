import { useEffect, useState } from 'react';

export type CustomizeValue = number | boolean | string | null;

/**
 * One Customize field: a number (`decimal` allows fractions; `scale` converts the stored value for display, e.g.
 * pounds shown as kilograms), a checkbox, or a text field; `name` marks the field the pencil focuses.
 */
export type CustomizeField = {
  key: string;
  label: string;
  kind: 'number' | 'checkbox' | 'text' | 'textarea' | 'select';
  decimal?: boolean;
  scale?: number;
  name?: boolean;
  /** A select's choices; the empty value is D&D Beyond's "--". */
  options?: { value: string; label: string }[];
};

type EntityCustomizeEditorProps = {
  fields: CustomizeField[];
  values: Record<string, CustomizeValue>;
  customized: boolean;
  /** Called with every field's value once one changes. */
  onSave: (values: Record<string, CustomizeValue>) => void;
  /** The "Remove Customizations" button; left out where there is nothing to reset to. */
  onReset?: () => void;
};

const CONFIRM_SECONDS = 3;

function displayOf(field: CustomizeField, value: CustomizeValue): string {
  if (value == null || typeof value === 'boolean') {
    return '';
  }
  if (typeof value === 'number' && field.scale) {
    return String(Math.round(value * field.scale * 100) / 100);
  }
  return String(value);
}

function parseNumber(field: CustomizeField, text: string): number | null {
  if (text.trim() === '') {
    return null;
  }
  const parsed = Number(text);
  if (!Number.isFinite(parsed) || (!field.decimal && !Number.isInteger(parsed))) {
    return null;
  }
  return field.scale ? parsed / field.scale : parsed;
}

function TextualField({ field, value, onCommit }: { field: CustomizeField; value: CustomizeValue; onCommit: (next: CustomizeValue) => void }) {
  const display = displayOf(field, value);
  const [text, setText] = useState(display);
  useEffect(() => setText(display), [display]);
  const commit = () => {
    const next = field.kind === 'number' ? parseNumber(field, text) : text.trim() === '' ? null : text;
    if (next !== value) {
      onCommit(next);
    } else {
      setText(display);
    }
  };
  return (
    <label className={`entity-customize__property entity-customize__property--${field.kind}`}>
      {field.kind === 'textarea' ? (
        <textarea
          className="entity-customize__input entity-customize__textarea"
          value={text}
          onChange={(event) => setText(event.target.value)}
          onBlur={commit}
        />
      ) : (
        <input
          className="entity-customize__input"
          type={field.kind === 'number' ? 'number' : 'text'}
          step={field.decimal ? 'any' : 1}
          value={text}
          data-customize-name={field.name ? '' : undefined}
          onChange={(event) => setText(event.target.value)}
          onBlur={commit}
        />
      )}
      <span className="entity-customize__label">{field.label}</span>
    </label>
  );
}

function SelectField({ field, value, onCommit }: { field: CustomizeField; value: CustomizeValue; onCommit: (next: CustomizeValue) => void }) {
  return (
    <label className="entity-customize__property entity-customize__property--select">
      <select
        className="entity-customize__select"
        value={typeof value === 'string' ? value : ''}
        onChange={(event) => onCommit(event.target.value === '' ? null : event.target.value)}
      >
        <option value="">--</option>
        {(field.options ?? []).map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      <span className="entity-customize__label">{field.label}</span>
    </label>
  );
}

/** The "Remove Customizations" button: a first click asks to confirm for three seconds, a second one resets. */
function ResetButton({ customized, onReset }: { customized: boolean; onReset: () => void }) {
  const [secondsLeft, setSecondsLeft] = useState(0);
  useEffect(() => {
    if (secondsLeft === 0) {
      return undefined;
    }
    const timer = window.setTimeout(() => setSecondsLeft(secondsLeft - 1), 1000);
    return () => window.clearTimeout(timer);
  }, [secondsLeft]);

  const confirming = secondsLeft > 0;
  return (
    <button
      type="button"
      className="entity-customize__reset"
      disabled={!customized}
      onClick={() => {
        if (confirming) {
          setSecondsLeft(0);
          onReset();
        } else {
          setSecondsLeft(CONFIRM_SECONDS);
        }
      }}
    >
      {!customized ? 'No Customizations' : confirming ? `Confirm (${secondsLeft})` : 'Remove Customizations'}
    </button>
  );
}

/** D&D Beyond's item and spell Customize: a three-column grid of numbers and checkboxes, full-width text fields, and a reset. */
export function EntityCustomizeEditor({ fields, values, customized, onSave, onReset }: EntityCustomizeEditorProps) {
  const save = (key: string, next: CustomizeValue) => onSave({ ...values, [key]: next });
  return (
    <div className="entity-customize">
      <div className="entity-customize__properties">
        {fields.map((field) =>
          field.kind === 'checkbox' ? (
            <label key={field.key} className="entity-customize__property entity-customize__property--checkbox">
              <input
                className="entity-customize__checkbox"
                type="checkbox"
                checked={values[field.key] === true}
                onChange={(event) => save(field.key, event.target.checked)}
              />
              <span className="entity-customize__label">{field.label}</span>
            </label>
          ) : field.kind === 'select' ? (
            <SelectField key={field.key} field={field} value={values[field.key] ?? null} onCommit={(next) => save(field.key, next)} />
          ) : (
            <TextualField key={field.key} field={field} value={values[field.key] ?? null} onCommit={(next) => save(field.key, next)} />
          ),
        )}
      </div>
      {onReset && (
        <div className="entity-customize__actions">
          <ResetButton customized={customized} onReset={onReset} />
        </div>
      )}
    </div>
  );
}

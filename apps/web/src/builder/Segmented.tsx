type SegmentedProps<T extends string> = {
  value: T | null;
  options: [T, string][];
  onChange: (value: T) => void;
  ariaLabel?: string;
};

/** A row of mutually exclusive buttons; `value` null leaves every one unpressed. */
export function Segmented<T extends string>({ value, options, onChange, ariaLabel }: SegmentedProps<T>) {
  return (
    <div className="builder-seg" role="radiogroup" aria-label={ariaLabel}>
      {options.map(([key, label]) => (
        <button key={key} type="button" role="radio" aria-checked={value === key} className={value === key ? 'is-on' : ''} onClick={() => onChange(key)}>
          {label}
        </button>
      ))}
    </div>
  );
}

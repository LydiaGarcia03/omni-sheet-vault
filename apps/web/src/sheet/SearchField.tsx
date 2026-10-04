type SearchFieldProps = {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
};

/**
 * Shared primitive (ui-design-system.md) — a free-text filter over a list. No
 * frame asset exists for it, so this is a plain text input. Styled via
 * `.search-field` (frames.css) rather than inline styles so it can carry a
 * `flex`/`min-width` floor — an inline-styled input with no sizing rule could
 * shrink to invisible next to wider flex siblings (the Spells tab's filter
 * row). Border-radius DOM-measured against D&D Beyond's own
 * `.ct-spells-filter__box` (2026-09-03): 4px, same square corner as every
 * other control — not the pill this used to guess at.
 */
export function SearchField({ value, onChange, placeholder }: SearchFieldProps) {
  return (
    <input
      type="search"
      className="search-field"
      value={value}
      onChange={(event) => onChange(event.target.value)}
      placeholder={placeholder ?? 'Search'}
      aria-label={placeholder ?? 'Search'}
    />
  );
}

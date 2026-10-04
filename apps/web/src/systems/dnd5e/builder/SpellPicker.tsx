import { useId, useMemo, useState } from 'react';
import type { ChoiceOption, CreationChoice } from '../../../builder/draftApi';
import { SourceBadge } from '../../../builder/SourceSelect';

/** The spell mechanics the API attaches to each spell option. */
type SpellData = {
  level: number;
  school: string | null;
  castingTime: string | null;
  range: string | null;
  duration: string | null;
  components: string | null;
  concentration: boolean;
  ritual: boolean;
  attackRoll: boolean | null;
  saveAbility: string | null;
  damageType: string | null;
  damageDiceCount: number | null;
  damageDiceSides: number | null;
  effectSummary: string | null;
};

const SPELL_CHOICE_TYPES = new Set(['CANTRIP', 'SPELL', 'SPELLBOOK', 'PREPARED_SPELL']);

export function isSpellChoice(choice: CreationChoice): boolean {
  return SPELL_CHOICE_TYPES.has(choice.type) && choice.options.some((option) => spellData(option) !== null);
}

function spellData(option: ChoiceOption): SpellData | null {
  const data = option.data as SpellData | undefined;
  return data && typeof data.level === 'number' ? data : null;
}

const capitalize = (text: string) => text.charAt(0).toUpperCase() + text.slice(1);
const levelLabel = (level: number) => (level === 0 ? 'Cantrip' : `${level}${['st', 'nd', 'rd'][level - 1] ?? 'th'}`);
const distinct = (values: (string | null)[]) => [...new Set(values.filter((value): value is string => Boolean(value)))].sort();

type Filters = {
  search: string;
  level: number | null;
  school: string;
  damageType: string;
  castingTime: string;
  source: string;
  concentration: boolean;
  ritual: boolean;
  onlyChosen: boolean;
};

const NO_FILTERS: Filters = {
  search: '',
  level: null,
  school: '',
  damageType: '',
  castingTime: '',
  source: '',
  concentration: false,
  ritual: false,
  onlyChosen: false,
};

type SpellPickerProps = {
  choice: CreationChoice;
  onChange: (selected: string[]) => void;
};

/** A spell or cantrip choice as a filterable list: search, level, school, damage, casting time, source, concentration and ritual. */
export function SpellPicker({ choice, onChange }: SpellPickerProps) {
  const [filters, setFilters] = useState<Filters>(NO_FILTERS);
  const [open, setOpen] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const filtersId = useId();
  const listId = useId();
  const [showList, setShowList] = useState(() => choice.selected.length < choice.count);
  const spells = useMemo(
    () => choice.options.flatMap((option) => {
      const data = spellData(option);
      return data ? [{ option, data }] : [];
    }),
    [choice.options],
  );
  const levels = [...new Set(spells.map((spell) => spell.data.level))].sort((a, b) => a - b);
  const schools = distinct(spells.map((spell) => spell.data.school));
  const damageTypes = distinct(spells.map((spell) => spell.data.damageType));
  const castingTimes = distinct(spells.map((spell) => spell.data.castingTime));
  const sources = distinct(spells.map((spell) => spell.option.sourceBook));
  const full = choice.selected.length >= choice.count;
  const activeFilters = activeFilterCount(filters);

  const shown = spells.filter(({ option, data }) => {
    const search = filters.search.trim().toLowerCase();
    return (
      (!search || option.label.toLowerCase().includes(search)) &&
      (filters.level === null || data.level === filters.level) &&
      (!filters.school || data.school === filters.school) &&
      (!filters.damageType || data.damageType === filters.damageType) &&
      (!filters.castingTime || data.castingTime === filters.castingTime) &&
      (!filters.source || option.sourceBook === filters.source) &&
      (!filters.concentration || data.concentration) &&
      (!filters.ritual || data.ritual) &&
      (!filters.onlyChosen || choice.selected.includes(option.key))
    );
  });

  const toggle = (key: string) => {
    if (choice.selected.includes(key)) {
      onChange(choice.selected.filter((selected) => selected !== key));
    } else if (choice.count === 1) {
      onChange([key]);
    } else if (!full) {
      onChange([...choice.selected, key]);
    }
  };
  const set = <K extends keyof Filters>(key: K, value: Filters[K]) => setFilters((current) => ({ ...current, [key]: value }));
  const labelOf = (key: string) => choice.options.find((option) => option.key === key)?.label ?? key;
  const summaryOf = (key: string) => choice.options.find((option) => option.key === key)?.summary ?? null;

  return (
    <div className={`builder-spells${choice.pending ? ' is-pending' : ''}`}>
      <div className="builder-spells__head">
        <div className="builder-grow">
          <div className="builder-choice__label">{choice.prompt}</div>
          <div className="builder-choice__from">{choice.sourceLabel}</div>
        </div>
        <span className={`builder-badge ${full ? 'builder-badge--done' : 'builder-badge--pending'}`}>
          {choice.selected.length} of {choice.count} chosen
        </span>
        <button type="button" className="builder-btn" aria-expanded={showList} aria-controls={listId} onClick={() => setShowList(!showList)}>
          {showList ? 'Hide spells ▴' : 'Show spells ▾'}
        </button>
      </div>

      {choice.selected.length > 0 && (
        <div className="builder-spells__chosen">
          {choice.selected.map((key) => (
            <button key={key} type="button" className="builder-chip" aria-pressed="true" onClick={() => toggle(key)} title={summaryOf(key) ? `${summaryOf(key)} · Remove` : 'Remove'}>
              {labelOf(key)} ✕
            </button>
          ))}
        </div>
      )}

      {showList && (
        <div id={listId} className="builder-spells__body">
          <div className="builder-spells__filters">
            <div className="builder-spells__bar">
              <input
                className="builder-input builder-spells__search"
                placeholder="Search spells"
                aria-label="Search spells"
                value={filters.search}
                onChange={(event) => set('search', event.target.value)}
              />
              <button type="button" className="builder-btn" aria-expanded={showFilters} aria-controls={filtersId} onClick={() => setShowFilters(!showFilters)}>
                Filters{activeFilters > 0 && ` · ${activeFilters}`} {showFilters ? '▴' : '▾'}
              </button>
              {JSON.stringify(filters) !== JSON.stringify(NO_FILTERS) && (
                <button type="button" className="builder-btn" onClick={() => setFilters(NO_FILTERS)}>
                  Clear filters
                </button>
              )}
            </div>
            {showFilters && (
              <div id={filtersId} className="builder-spells__panel">
                {levels.length > 1 && (
                  <div className="builder-chips" role="group" aria-label="Spell level">
                    <button type="button" className="builder-chip" aria-pressed={filters.level === null} onClick={() => set('level', null)}>
                      All
                    </button>
                    {levels.map((level) => (
                      <button key={level} type="button" className="builder-chip" aria-pressed={filters.level === level} onClick={() => set('level', level)}>
                        {levelLabel(level)}
                      </button>
                    ))}
                  </div>
                )}
                <FilterSelect label="School" value={filters.school} values={schools} onChange={(value) => set('school', value)} />
                <FilterSelect label="Damage type" value={filters.damageType} values={damageTypes} onChange={(value) => set('damageType', value)} />
                <FilterSelect label="Casting time" value={filters.castingTime} values={castingTimes} onChange={(value) => set('castingTime', value)} />
                <FilterSelect label="Source" value={filters.source} values={sources} onChange={(value) => set('source', value)} />
                <div className="builder-chips">
                  <button type="button" className="builder-chip" aria-pressed={filters.concentration} onClick={() => set('concentration', !filters.concentration)}>
                    Concentration
                  </button>
                  <button type="button" className="builder-chip" aria-pressed={filters.ritual} onClick={() => set('ritual', !filters.ritual)}>
                    Ritual
                  </button>
                  <button type="button" className="builder-chip" aria-pressed={filters.onlyChosen} onClick={() => set('onlyChosen', !filters.onlyChosen)}>
                    Chosen only
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="builder-spells__count builder-muted">
            {shown.length} of {spells.length} spells
          </div>
          <div className="builder-spells__list" role="list">
            <div className="builder-spells__row is-header" aria-hidden>
              <span />
              <span>Name</span>
              <span>Level</span>
              <span>School</span>
              <span>Casting time</span>
              <span>Range</span>
              <span>Effect</span>
            </div>
            {shown.map(({ option, data }) => {
              const chosen = choice.selected.includes(option.key);
              const expanded = open === option.key;
              return (
                <div key={option.key} role="listitem" className={`builder-spells__item${chosen ? ' is-chosen' : ''}`}>
                  <div className="builder-spells__row">
                    <button
                      type="button"
                      className={`builder-btn${chosen ? ' builder-btn--primary' : ''}`}
                      disabled={!chosen && full && choice.count > 1}
                      aria-label={`${chosen ? 'Remove' : 'Choose'} ${option.label}`}
                      onClick={() => toggle(option.key)}
                    >
                      {chosen ? '✓' : '+'}
                    </button>
                    <button type="button" className="builder-spells__name" aria-expanded={expanded} onClick={() => setOpen(expanded ? null : option.key)}>
                      {option.label}
                      <SourceBadge sourceBook={option.sourceBook} />
                      {data.concentration && <span className="builder-spells__tag" title="Concentration">C</span>}
                      {data.ritual && <span className="builder-spells__tag" title="Ritual">R</span>}
                    </button>
                    <span>{levelLabel(data.level)}</span>
                    <span>{data.school ? capitalize(data.school) : '—'}</span>
                    <span>{data.castingTime ?? '—'}</span>
                    <span>{data.range ?? '—'}</span>
                    <span>{effectOf(data)}</span>
                  </div>
                  {expanded && (
                    <div className="builder-spells__detail">
                      <span>
                        <b>Duration</b> {data.duration ?? '—'}
                      </span>
                      <span>
                        <b>Components</b> {data.components ?? '—'}
                      </span>
                      {data.saveAbility && (
                        <span>
                          <b>Save</b> {capitalize(data.saveAbility)}
                        </span>
                      )}
                      {data.attackRoll && <span>Spell attack</span>}
                      {data.effectSummary && <span>{data.effectSummary}</span>}
                    </div>
                  )}
                </div>
              );
            })}
            {shown.length === 0 && <p className="builder-muted">No spell matches these filters.</p>}
          </div>
        </div>
      )}
    </div>
  );
}

/** How many filters in the collapsible panel are set; the search box isn't counted. */
function activeFilterCount(filters: Filters): number {
  const { search: _search, ...panel } = filters;
  return Object.entries(panel).filter(([key, value]) => value !== NO_FILTERS[key as keyof Filters]).length;
}

function effectOf(data: SpellData): string {
  if (data.damageDiceCount && data.damageDiceSides) {
    return `${data.damageDiceCount}d${data.damageDiceSides}${data.damageType ? ` ${data.damageType}` : ''}`;
  }
  return data.effectSummary ?? '—';
}

function FilterSelect({ label, value, values, onChange }: { label: string; value: string; values: string[]; onChange: (value: string) => void }) {
  if (values.length < 2) {
    return null;
  }
  return (
    <select className="builder-input builder-spells__select" aria-label={label} value={value} onChange={(event) => onChange(event.target.value)}>
      <option value="">{label}: any</option>
      {values.map((entry) => (
        <option key={entry} value={entry}>
          {capitalize(entry)}
        </option>
      ))}
    </select>
  );
}

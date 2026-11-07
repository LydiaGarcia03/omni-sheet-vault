import { useId, useMemo, useState } from 'react';
import type { ChoiceOption, CreationChoice } from '../../../builder/draftApi';
import { PendingMark } from '../../../builder/PendingMark';
import { SourceBadge } from '../../../builder/SourceSelect';
import { useDnd5eTerms, type Dnd5eTerms } from '../locales/dnd5eMessages';
import { useSpellDescriptions, type SpellDescription } from './useSpellDescriptions';

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
const levelLabel = (level: number, terms: Dnd5eTerms) => (level === 0 ? terms.t('terms.labels.cantrip') : terms.ordinal(level));
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
  const terms = useDnd5eTerms();
  const { t } = terms;
  const [filters, setFilters] = useState<Filters>(NO_FILTERS);
  const [open, setOpen] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const filtersId = useId();
  const listId = useId();
  const [showList, setShowList] = useState(() => choice.selected.length < choice.count);
  const descriptions = useSpellDescriptions(open !== null);
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
    <details
      className={`builder-level builder-spells${choice.pending ? ' is-pending' : ''}`}
      open={showList}
      onToggle={(event) => setShowList(event.currentTarget.open)}
    >
      <summary className="builder-level__summary" aria-controls={listId}>
        <span className="builder-spells__title">
          <span className="builder-choice__label">
            {choice.prompt}
            {choice.pending && <PendingMark />}
          </span>
          <span className="builder-choice__from">{choice.sourceLabel}</span>
        </span>
        <span className={`builder-badge ${full ? 'builder-badge--done' : 'builder-badge--pending'}`}>
          {t('builder.spells.chosen', { count: choice.selected.length, total: choice.count })}
        </span>
        {choice.selected.length > 0 && <span className="builder-level__features">{choice.selected.map(labelOf).join(', ')}</span>}
      </summary>

      {showList && (
        <div id={listId} className="builder-level__body builder-spells__body">
          {choice.selected.length > 0 && (
            <div className="builder-spells__chosen">
              {choice.selected.map((key) => (
                <button key={key} type="button" className="builder-chip" aria-pressed="true" onClick={() => toggle(key)} title={summaryOf(key) ? t('builder.spells.removeWith', { summary: summaryOf(key) ?? '' }) : t('builder.spells.remove')}>
                  {labelOf(key)} ✕
                </button>
              ))}
            </div>
          )}
          <div className="builder-spells__filters">
            <div className="builder-spells__bar">
              <input
                className="builder-input builder-spells__search"
                placeholder={t('builder.spells.search')}
                aria-label={t('builder.spells.search')}
                value={filters.search}
                onChange={(event) => set('search', event.target.value)}
              />
              <button type="button" className="builder-btn" aria-expanded={showFilters} aria-controls={filtersId} onClick={() => setShowFilters(!showFilters)}>
                {t('builder.spells.filters')}
                {activeFilters > 0 && ` · ${activeFilters}`} {showFilters ? '▴' : '▾'}
              </button>
              {JSON.stringify(filters) !== JSON.stringify(NO_FILTERS) && (
                <button type="button" className="builder-btn" onClick={() => setFilters(NO_FILTERS)}>
                  {t('builder.spells.clearFilters')}
                </button>
              )}
            </div>
            {showFilters && (
              <div id={filtersId} className="builder-spells__panel">
                {levels.length > 1 && (
                  <div className="builder-chips" role="group" aria-label={t('builder.spells.levelAria')}>
                    <button type="button" className="builder-chip" aria-pressed={filters.level === null} onClick={() => set('level', null)}>
                      {t('builder.spells.all')}
                    </button>
                    {levels.map((level) => (
                      <button key={level} type="button" className="builder-chip" aria-pressed={filters.level === level} onClick={() => set('level', level)}>
                        {levelLabel(level, terms)}
                      </button>
                    ))}
                  </div>
                )}
                <FilterSelect label={t('terms.builder.school')} value={filters.school} values={schools} format={terms.school} onChange={(value) => set('school', value)} />
                <FilterSelect label={t('terms.builder.damageType')} value={filters.damageType} values={damageTypes} format={terms.damageType} onChange={(value) => set('damageType', value)} />
                <FilterSelect label={t('terms.builder.castingTime')} value={filters.castingTime} values={castingTimes} onChange={(value) => set('castingTime', value)} />
                <FilterSelect label={t('item.source')} value={filters.source} values={sources} onChange={(value) => set('source', value)} />
                <div className="builder-chips">
                  <button type="button" className="builder-chip" aria-pressed={filters.concentration} onClick={() => set('concentration', !filters.concentration)}>
                    {t('terms.labels.concentration')}
                  </button>
                  <button type="button" className="builder-chip" aria-pressed={filters.ritual} onClick={() => set('ritual', !filters.ritual)}>
                    {t('terms.labels.ritual')}
                  </button>
                  <button type="button" className="builder-chip" aria-pressed={filters.onlyChosen} onClick={() => set('onlyChosen', !filters.onlyChosen)}>
                    {t('builder.spells.chosenOnly')}
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="builder-spells__count builder-muted">
            {t('builder.spells.count', { shown: shown.length, total: spells.length })}
          </div>
          <div className="builder-spells__list" role="list">
            <div className="builder-spells__row is-header" aria-hidden>
              <span />
              <span>{t('terms.builder.name')}</span>
              <span>{t('terms.builder.level')}</span>
              <span>{t('terms.builder.school')}</span>
              <span>{t('terms.builder.castingTime')}</span>
              <span>{t('builder.spells.range')}</span>
              <span>{t('terms.builder.effect')}</span>
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
                      aria-label={t(chosen ? 'builder.spells.removeSpell' : 'builder.spells.chooseSpell', { name: option.label })}
                      onClick={() => toggle(option.key)}
                    >
                      {chosen ? '✓' : '+'}
                    </button>
                    <button type="button" className="builder-spells__name" aria-expanded={expanded} onClick={() => setOpen(expanded ? null : option.key)}>
                      {option.label}
                      <SourceBadge sourceBook={option.sourceBook} />
                      {data.concentration && <span className="builder-spells__tag" title={t('terms.labels.concentration')}>C</span>}
                      {data.ritual && <span className="builder-spells__tag" title={t('terms.labels.ritual')}>R</span>}
                    </button>
                    <span>{levelLabel(data.level, terms)}</span>
                    <span>{data.school ? terms.school(data.school) : '—'}</span>
                    <span>{data.castingTime ?? '—'}</span>
                    <span>{data.range ?? '—'}</span>
                    <span>{effectOf(data, terms)}</span>
                  </div>
                  {expanded && (
                    <div className="builder-spells__detail">
                      <dl className="builder-spells__facts">
                        <SpellFact label={t('terms.labels.duration')} value={durationOf(data, terms)} />
                        <SpellFact label={t('terms.labels.components')} value={data.components ?? '—'} />
                        <SpellFact label={t('terms.builder.attackSave')} value={attackOrSaveOf(data, terms)} />
                        <SpellFact label={t('terms.builder.damageEffect')} value={effectOf(data, terms)} />
                        {data.ritual && <SpellFact label={t('terms.labels.ritual')} value={t('builder.spells.yes')} />}
                      </dl>
                      <SpellText description={descriptions?.get(option.key)} loading={descriptions === null} />
                    </div>
                  )}
                </div>
              );
            })}
            {shown.length === 0 && <p className="builder-muted">{t('builder.spells.noMatch')}</p>}
          </div>
        </div>
      )}
    </details>
  );
}

/** The spell's rules text under its facts, with "At Higher Levels." as its own paragraph. */
function SpellText({ description, loading }: { description: SpellDescription | undefined; loading: boolean }) {
  const { t } = useDnd5eTerms();
  if (loading) {
    return <p className="builder-spells__text builder-muted">{t('builder.spells.loadingDescription')}</p>;
  }
  if (!description || description.redacted || !description.text) {
    return <p className="builder-spells__text builder-muted">{description?.redacted ? t('builder.spells.hidden') : t('builder.spells.noDescription')}</p>;
  }
  return (
    <div className="builder-spells__text">
      {description.text.split(/\n+/).map((paragraph, index) => (
        <p key={index}>{paragraph}</p>
      ))}
      {description.higherLevels && (
        <p>
          <b>{t('terms.labels.atHigherLevels')}</b> {description.higherLevels}
        </p>
      )}
    </div>
  );
}

function SpellFact({ label, value }: { label: string; value: string }) {
  return (
    <div className="builder-spells__fact">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

function durationOf(data: SpellData, terms: Dnd5eTerms): string {
  const duration = data.duration ?? '—';
  return data.concentration && !/concentration/i.test(duration)
    ? terms.t('builder.spells.concentrationDuration', { concentration: terms.t('terms.labels.concentration'), duration })
    : duration;
}

function attackOrSaveOf(data: SpellData, terms: Dnd5eTerms): string {
  if (data.saveAbility) {
    return terms.t('terms.builder.save', { ability: terms.ability(data.saveAbility.toLowerCase()) });
  }
  return data.attackRoll ? terms.t('terms.builder.spellAttack') : terms.t('builder.spells.none');
}

/** How many filters in the collapsible panel are set; the search box isn't counted. */
function activeFilterCount(filters: Filters): number {
  const { search: _search, ...panel } = filters;
  return Object.entries(panel).filter(([key, value]) => value !== NO_FILTERS[key as keyof Filters]).length;
}

function effectOf(data: SpellData, terms: Dnd5eTerms): string {
  if (data.damageDiceCount && data.damageDiceSides) {
    return `${data.damageDiceCount}d${data.damageDiceSides}${data.damageType ? ` ${terms.damageType(data.damageType)}` : ''}`;
  }
  return data.effectSummary ?? '—';
}

type FilterSelectProps = { label: string; value: string; values: string[]; format?: (value: string) => string; onChange: (value: string) => void };

function FilterSelect({ label, value, values, format = capitalize, onChange }: FilterSelectProps) {
  const { t } = useDnd5eTerms();
  if (values.length < 2) {
    return null;
  }
  return (
    <select className="builder-input builder-spells__select" aria-label={label} value={value} onChange={(event) => onChange(event.target.value)}>
      <option value="">{t('builder.spells.any', { label })}</option>
      {values.map((entry) => (
        <option key={entry} value={entry}>
          {format(entry)}
        </option>
      ))}
    </select>
  );
}

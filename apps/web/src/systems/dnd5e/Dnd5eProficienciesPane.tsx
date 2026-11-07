import { useEffect, useState } from 'react';
import { getCatalogue, type CatalogueEntry } from '../../catalogue/api';
import type { CharacterSheet, MutationHandler } from '../../sheet/api';
import { catalogueEntryLabel } from '../../sheet/CatalogueSourceLabel';
import { SidebarHeader } from '../../sheet/sidebarParts';
import { customizationsOf, type ProficiencyType, type StoredCustomProficiency } from './customizations';
import { useDnd5eTerms } from './locales/dnd5eMessages';

const TYPES = [
  { key: 'ARMOR', label: 'terms.proficiencyTypes.ARMOR', plural: 'terms.proficiencyGroups.ARMOR' },
  { key: 'WEAPON', label: 'terms.proficiencyTypes.WEAPON', plural: 'terms.proficiencyGroups.WEAPON' },
  { key: 'TOOL', label: 'terms.proficiencyTypes.TOOL', plural: 'terms.proficiencyGroups.TOOL' },
  { key: 'LANGUAGE', label: 'terms.proficiencyTypes.LANGUAGE', plural: 'terms.proficiencyGroups.LANGUAGE' },
] as const;

/** D&D Beyond offers custom entries for tools and languages only. */
const CUSTOM_TYPES: ProficiencyType[] = ['TOOL', 'LANGUAGE'];

const TOOL_TYPE_LABELS = ["Tool", "Artisan's Tools", 'Instrument', 'Gaming Set'];
const ARMOR_GROUPS = ['Light Armor', 'Medium Armor', 'Heavy Armor', 'Shield'];
/** D&D Beyond lists the core rules first. */
const SOURCE_ORDER = ["Player's Handbook"];

type ItemData = { itemKind?: string; typeLabel?: string; rarity?: string; weaponCategory?: string | null };

export type ProficiencyOptionGroup = { label: string; options: { value: string; label: string }[] };

function itemData(entry: CatalogueEntry): ItemData {
  return (entry.data ?? {}) as ItemData;
}

function byName(a: CatalogueEntry, b: CatalogueEntry): number {
  return a.name.localeCompare(b.name);
}

/** Entries grouped under `groupOf`, in `order` first, then any other group alphabetically; each shows its source. */
function grouped(entries: CatalogueEntry[], groupOf: (entry: CatalogueEntry) => string, order: string[] = []): ProficiencyOptionGroup[] {
  const groups = new Map<string, CatalogueEntry[]>();
  for (const entry of [...entries].sort(byName)) {
    const group = groupOf(entry);
    groups.set(group, [...(groups.get(group) ?? []), entry]);
  }
  const labels = [...groups.keys()].sort((a, b) => {
    const rank = (label: string) => (order.includes(label) ? order.indexOf(label) : order.length);
    return rank(a) - rank(b) || a.localeCompare(b);
  });
  return labels.map((label) => ({
    label,
    options: (groups.get(label) ?? []).map((entry) => ({
      value: entry.name,
      label: catalogueEntryLabel(entry.name, entry.sourceBook),
    })),
  }));
}

/** The catalogue entries offered for an existing proficiency of `type`: mundane armor, weapons and tools, and languages. */
export function proficiencyOptions(type: ProficiencyType, languages: CatalogueEntry[], items: CatalogueEntry[]): ProficiencyOptionGroup[] {
  const mundane = items.filter((entry) => (itemData(entry).rarity ?? 'none') === 'none');
  switch (type) {
    case 'LANGUAGE':
      return grouped(languages, (entry) => entry.sourceBook ?? 'Other', SOURCE_ORDER);
    case 'ARMOR':
      return grouped(
        mundane.filter((entry) => ['ARMOR', 'SHIELD'].includes(itemData(entry).itemKind ?? '')),
        (entry) => itemData(entry).typeLabel ?? 'Armor',
        ARMOR_GROUPS,
      );
    case 'WEAPON':
      return grouped(
        mundane.filter((entry) => itemData(entry).itemKind === 'WEAPON'),
        (entry) => itemData(entry).weaponCategory ?? 'Other',
        ['Simple', 'Martial'],
      );
    case 'TOOL':
      return grouped(
        mundane.filter((entry) => TOOL_TYPE_LABELS.includes(itemData(entry).typeLabel ?? '')),
        (entry) => entry.sourceBook ?? 'Other',
        SOURCE_ORDER,
      );
  }
}

/** A text field saved when it loses focus. */
function BlurField({ value, placeholder, label, className, onCommit }: {
  value: string;
  placeholder: string;
  label: string;
  className: string;
  onCommit: (value: string) => void;
}) {
  const [text, setText] = useState(value);
  useEffect(() => setText(value), [value]);
  return (
    <input
      className={`sidebar-value-editor__input ${className}`}
      placeholder={placeholder}
      aria-label={label}
      value={text}
      onChange={(event) => setText(event.target.value)}
      onBlur={() => text.trim() !== value && onCommit(text.trim())}
    />
  );
}

function AddedProficiency({ proficiency, onMutate }: { proficiency: StoredCustomProficiency; onMutate: MutationHandler }) {
  const { t } = useDnd5eTerms();
  const save = (changes: { name?: string; notes?: string | null }) =>
    onMutate({
      type: 'CUSTOMIZE',
      group: 'proficiencies',
      target: proficiency.key,
      value: { name: changes.name ?? proficiency.name, notes: changes.notes !== undefined ? changes.notes : proficiency.notes },
    });
  const displayName = proficiency.name || t('proficiencies.customProficiency');
  return (
    <div className="proficiencies-pane__added">
      <div className="proficiencies-pane__added-row">
        {proficiency.custom ? (
          <BlurField
            className="proficiencies-pane__added-name-input"
            placeholder={t('proficiencies.enterName')}
            label={t('proficiencies.customNameLabel')}
            value={proficiency.name}
            onCommit={(name) => save({ name })}
          />
        ) : (
          <div className="proficiencies-pane__added-name">{proficiency.name}</div>
        )}
        <button
          type="button"
          className="sidebar-remove-button proficiencies-pane__remove"
          aria-label={t('proficiencies.removeName', { name: displayName })}
          onClick={() => onMutate({ type: 'REMOVE_CUSTOMIZATION', group: 'proficiencies', target: proficiency.key })}
        >
          {t('proficiencies.removeProficiency')}
        </button>
      </div>
      <BlurField
        className="proficiencies-pane__added-notes"
        placeholder={t('combat.sourceNote')}
        label={t('combat.sourceNoteOf', { name: displayName })}
        value={proficiency.notes ?? ''}
        onCommit={(notes) => save({ notes: notes || null })}
      />
    </div>
  );
}

function useProficiencyCatalogues(): { languages: CatalogueEntry[]; items: CatalogueEntry[] } {
  const [languages, setLanguages] = useState<CatalogueEntry[]>([]);
  const [items, setItems] = useState<CatalogueEntry[]>([]);
  useEffect(() => {
    let active = true;
    getCatalogue('dnd-5e', 'LANGUAGE').then((entries) => active && setLanguages(entries), () => undefined);
    getCatalogue('dnd-5e', 'ITEM').then((entries) => active && setItems(entries), () => undefined);
    return () => {
      active = false;
    };
  }, []);
  return { languages, items };
}

type Dnd5eProficienciesPaneProps = { sheet: CharacterSheet; onMutate: MutationHandler };

/**
 * D&D Beyond's Proficiencies & Training pane: the proficiencies added by hand, grouped by type, each with Remove and a
 * source note; then Add New Proficiencies (an existing one picked from the catalogue, added at once, or a custom tool
 * or language named afterwards); then every proficiency the character has.
 */
export function Dnd5eProficienciesPane({ sheet, onMutate }: Dnd5eProficienciesPaneProps) {
  const { t } = useDnd5eTerms();
  const added = customizationsOf(sheet).proficiencies;
  const { languages, items } = useProficiencyCatalogues();
  const [choice, setChoice] = useState('');
  const [kind, type] = choice.split(':') as ['existing' | 'custom' | '', ProficiencyType];

  const chooseType = (next: string) => {
    const [nextKind, nextType] = next.split(':');
    if (nextKind === 'custom') {
      onMutate({ type: 'CUSTOMIZE', group: 'proficiencies', target: 'new', value: { type: nextType, custom: true } });
      setChoice('');
    } else {
      setChoice(next);
    }
  };
  const addExisting = (name: string) =>
    name !== '' && onMutate({ type: 'CUSTOMIZE', group: 'proficiencies', target: 'new', value: { type, name } });

  const lists: Record<ProficiencyType, string[]> = {
    ARMOR: sheet.armorProficiencies,
    WEAPON: sheet.weaponProficiencies,
    TOOL: sheet.toolProficiencies,
    LANGUAGE: sheet.languages,
  };

  return (
    <div className="proficiencies-pane">
      <SidebarHeader title={t('terms.labels.proficienciesAndTraining')} />
      {TYPES.map(({ key, plural }) => {
        const entries = added.filter((proficiency) => proficiency.type === key);
        return entries.length === 0 ? null : (
          <section key={key}>
            <h2 className="sidebar-subheading">{t(plural)}</h2>
            {entries.map((proficiency) => (
              <AddedProficiency key={proficiency.key} proficiency={proficiency} onMutate={onMutate} />
            ))}
          </section>
        );
      })}
      <h2 className="sidebar-subheading">{t('proficiencies.addNew')}</h2>
      <label className="proficiencies-pane__field">
        <span className="proficiencies-pane__field-label">{t('proficiencies.type')}</span>
        <select className="proficiencies-pane__select" value={choice} onChange={(event) => chooseType(event.target.value)}>
          <option value="">{t('proficiencies.chooseOption')}</option>
          <optgroup label={t('proficiencies.existing')}>
            {TYPES.map(({ key, label }) => (
              <option key={key} value={`existing:${key}`}>
                {t(label)}
              </option>
            ))}
          </optgroup>
          <optgroup label={t('combat.custom')}>
            {TYPES.filter(({ key }) => CUSTOM_TYPES.includes(key as ProficiencyType)).map(({ key, label }) => (
              <option key={key} value={`custom:${key}`}>
                {t(label)}
              </option>
            ))}
          </optgroup>
        </select>
      </label>
      {kind === 'existing' && (
        <label className="proficiencies-pane__field proficiencies-pane__field--available">
          <span className="proficiencies-pane__field-label">{t('proficiencies.available')}</span>
          <select className="proficiencies-pane__select" value="" onChange={(event) => addExisting(event.target.value)}>
            <option value="">{t('proficiencies.chooseOption')}</option>
            {proficiencyOptions(type, languages, items).map((group) => (
              <optgroup key={group.label} label={group.label}>
                {group.options.map((option) => (
                  <option key={option.label} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </optgroup>
            ))}
          </select>
        </label>
      )}
      <div className="proficiencies-pane__groups">
        {TYPES.map(({ key, plural }) =>
          lists[key].length === 0 ? null : (
            <div key={key} className="proficiencies-pane__group">
              <div className="proficiencies-pane__group-label">{t(plural)}</div>
              <div className="proficiencies-pane__group-value">
                {lists[key].map((name, index) => (
                  <span key={name}>
                    {name}
                    {index < lists[key].length - 1 && ', '}
                  </span>
                ))}
              </div>
            </div>
          ),
        )}
      </div>
    </div>
  );
}

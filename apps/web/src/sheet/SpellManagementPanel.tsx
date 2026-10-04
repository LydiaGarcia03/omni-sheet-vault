import { useState } from 'react';
import type { CatalogueEntry, SpellCatalogueEntryData } from '../catalogue/api';
import type { Spell, SpellcastingClassInfo, SpellManagementRequest, SpellSlotLevel } from './api';
import { catalogueEntryLabel } from './CatalogueSourceLabel';
import { SidebarCollapsible, SidebarHeader } from './sidebarParts';

function ordinal(level: number): string {
  if (level === 1) return '1st';
  if (level === 2) return '2nd';
  if (level === 3) return '3rd';
  return `${level}th`;
}

const levelLabel = (level: number) => (level === 0 ? 'Cantrip' : ordinal(level));

function SpellName({ name, level }: { name: string; level: number }) {
  return (
    <span className="spell-manage__spell-name">
      {name}
      <span className="spell-manage__spell-level">({levelLabel(level)})</span>
    </span>
  );
}

/** The Spell Slots header: D&D Beyond's per-level "1st / 4" callout, the slots themselves inside. */
function SpellSlotsSection({ slots }: { slots: SpellSlotLevel[] }) {
  const regular = slots.filter((slot) => !slot.pact && slot.maxSlots > 0);
  if (regular.length === 0) {
    return null;
  }
  const callout = (
    <span className="spell-manage__slot-summary">
      {regular.map((slot) => (
        <span key={slot.level} className="spell-manage__slot-level">
          <span className="spell-manage__slot-level-name">{ordinal(slot.level)}</span>
          <span className="spell-manage__slot-level-available">{slot.maxSlots - slot.usedSlots}</span>
        </span>
      ))}
    </span>
  );
  return (
    <div className="spell-manage__slots">
      <SidebarCollapsible heading="Spell Slots" callout={callout}>
        <ul className="spell-manage__slot-list">
          {regular.map((slot) => (
            <li key={slot.level}>
              {ordinal(slot.level)} Level: {slot.maxSlots - slot.usedSlots} of {slot.maxSlots} available
            </li>
          ))}
        </ul>
      </SidebarCollapsible>
    </div>
  );
}

type ClassSectionProps = {
  classInfo: SpellcastingClassInfo;
  knownSpells: Spell[];
  catalogue: CatalogueEntry[];
  onLearn: (catalogueEntryId: string, className: string) => void;
  onRemove: (spellKey: string) => void;
  onPrepare: (spellKey: string) => void;
  onUnprepare: (spellKey: string) => void;
};

/**
 * One class: its heading, then Known Spells (the counts, a name filter, level chips, and every catalogue spell
 * with Learn or Delete) and Prepared Spells (the known ones, with Prepare / Unprepare, Always Prepared or Delete).
 */
function ClassSection({ classInfo, knownSpells, catalogue, onLearn, onRemove, onPrepare, onUnprepare }: ClassSectionProps) {
  const [search, setSearch] = useState('');
  const [levels, setLevels] = useState<number[]>([]);
  const classSpells = knownSpells.filter((spell) => spell.className === classInfo.className);
  const cantripsKnown = classSpells.filter((spell) => spell.level === 0).length;
  const leveledKnown = classSpells.filter((spell) => spell.level > 0).length;
  const prepared = classSpells.filter((spell) => spell.level > 0 && spell.prepared).length;
  const knownBySlug = new Map(classSpells.map((spell) => [spell.key, spell]));
  const preparedCaster = classInfo.castingType === 'PREPARED';

  const levelOf = (entry: CatalogueEntry) => (entry.data as SpellCatalogueEntryData).level;
  const offeredLevels = [...new Set(catalogue.map(levelOf))].sort((a, b) => a - b);
  const toggleLevel = (level: number) => setLevels(levels.includes(level) ? levels.filter((value) => value !== level) : [...levels, level]);
  const visible = catalogue
    .filter((entry) => levels.length === 0 || levels.includes(levelOf(entry)))
    .filter((entry) => entry.name.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => levelOf(a) - levelOf(b) || a.name.localeCompare(b.name));

  const exceeded = (count: number, max: number | null) => (max != null && max > 0 && count > max ? ' is-exceeded' : '');

  return (
    <section className="spell-manage__class">
      <h2 className="spell-manage__class-heading">{classInfo.className}</h2>
      <SidebarCollapsible heading="Known Spells">
        <div className="spell-manage__info">
          {classInfo.cantripsKnownMax > 0 && (
            <div className={`spell-manage__info-entry${exceeded(cantripsKnown, classInfo.cantripsKnownMax)}`}>
              Cantrips: {cantripsKnown}/{classInfo.cantripsKnownMax}
            </div>
          )}
          {preparedCaster ? (
            <div className={`spell-manage__info-entry${exceeded(prepared, classInfo.spellsPreparedMax)}`}>
              Prepared Spells: {prepared}
              {classInfo.spellsPreparedMax != null ? `/${classInfo.spellsPreparedMax}` : ''}
              <span className="spell-manage__info-extra">({leveledKnown} Known)</span>
            </div>
          ) : (
            <div className={`spell-manage__info-entry${exceeded(leveledKnown, classInfo.spellsKnownMax)}`}>
              Spells Known: {leveledKnown}
              {classInfo.spellsKnownMax != null ? `/${classInfo.spellsKnownMax}` : ''}
            </div>
          )}
        </div>
        <div className="inventory-manage__filter-group">
          <div className="inventory-manage__filter-label">Filter</div>
          <input
            className="inventory-manage__search"
            type="search"
            aria-label={`Filter ${classInfo.className} spells`}
            placeholder="Enter Spell Name"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>
        <div className="inventory-manage__filter-group">
          <div className="inventory-manage__filter-label">Filter By Spell Level</div>
          <div className="inventory-manage__chips">
            {offeredLevels.map((level) => (
              <button
                key={level}
                type="button"
                className={levels.includes(level) ? 'inventory-manage__chip is-active' : 'inventory-manage__chip'}
                aria-pressed={levels.includes(level)}
                aria-label={level === 0 ? 'Cantrips' : `${ordinal(level)} level`}
                onClick={() => toggleLevel(level)}
              >
                {level === 0 ? '- 0 -' : ordinal(level)}
              </button>
            ))}
          </div>
        </div>
        <ul className="spell-manage__spells">
          {visible.map((entry) => {
            const known = knownBySlug.get(entry.slug);
            const label = catalogueEntryLabel(entry.name, entry.sourceBook);
            return (
              <li key={entry.id} className="spell-manage__spell">
                <SpellName name={entry.name} level={levelOf(entry)} />
                {known ? (
                  <button type="button" className="mutate spell-manage__button spell-manage__button--filled" aria-label={`Remove ${known.name}`} onClick={() => onRemove(known.key)}>
                    Delete
                  </button>
                ) : (
                  <button type="button" className="mutate spell-manage__button" aria-label={`Learn ${label}`} onClick={() => onLearn(entry.id, classInfo.className)}>
                    Learn
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      </SidebarCollapsible>
      <SidebarCollapsible heading={`${preparedCaster ? 'Prepared Spells' : 'Spells Known'} (${preparedCaster ? cantripsKnown + prepared : classSpells.length})`}>
        <ul className="spell-manage__spells">
          {classSpells
            .filter((spell) => !preparedCaster || spell.level === 0 || spell.prepared || spell.alwaysPrepared)
            .sort((a, b) => a.level - b.level || a.name.localeCompare(b.name))
            .map((spell) => (
              <li key={spell.key} className="spell-manage__spell">
                <SpellName name={spell.name} level={spell.level} />
                {spell.alwaysPrepared ? (
                  <span className="spell-manage__always">Always Prepared</span>
                ) : preparedCaster && spell.level > 0 ? (
                  <button
                    type="button"
                    className="mutate spell-manage__button"
                    aria-label={spell.prepared ? `Unprepare ${spell.name}` : `Prepare ${spell.name}`}
                    onClick={() => (spell.prepared ? onUnprepare(spell.key) : onPrepare(spell.key))}
                  >
                    {spell.prepared ? 'Unprepare' : 'Prepare'}
                  </button>
                ) : (
                  <button type="button" className="mutate spell-manage__button spell-manage__button--filled" aria-label={`Remove ${spell.name}`} onClick={() => onRemove(spell.key)}>
                    Delete
                  </button>
                )}
              </li>
            ))}
        </ul>
        {preparedCaster && (
          <ul className="spell-manage__spells spell-manage__spells--unprepared">
            {classSpells
              .filter((spell) => spell.level > 0 && !spell.prepared && !spell.alwaysPrepared)
              .sort((a, b) => a.level - b.level || a.name.localeCompare(b.name))
              .map((spell) => (
                <li key={spell.key} className="spell-manage__spell">
                  <SpellName name={spell.name} level={spell.level} />
                  <button type="button" className="mutate spell-manage__button" aria-label={`Prepare ${spell.name}`} onClick={() => onPrepare(spell.key)}>
                    Prepare
                  </button>
                </li>
              ))}
          </ul>
        )}
      </SidebarCollapsible>
    </section>
  );
}

/**
 * The Spells tab's Manage Spells pane as D&D Beyond lays it out: the Spell Slots summary, then one section per
 * spellcasting class (see SpellManagementRequest for why this is not the Collection editor mold).
 */
export function SpellManagementPanel({ classes, knownSpells, catalogue, slots = [], onLearn, onRemove, onPrepare, onUnprepare }: SpellManagementRequest) {
  return (
    <div className="spell-manage">
      <SidebarHeader title="Manage Spells" />
      <SpellSlotsSection slots={slots} />
      {classes.map((classInfo) => (
        <ClassSection
          key={classInfo.className}
          classInfo={classInfo}
          knownSpells={knownSpells}
          catalogue={catalogue}
          onLearn={onLearn}
          onRemove={onRemove}
          onPrepare={onPrepare}
          onUnprepare={onUnprepare}
        />
      ))}
    </div>
  );
}

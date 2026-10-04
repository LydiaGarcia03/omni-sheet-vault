import { useState } from 'react';
import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, EntityDetailHandler, MutationHandler, Spell } from '../../sheet/api';
import { BoxTrack } from '../../sheet/BoxTrack';
import { FilterChips } from '../../sheet/FilterChips';
import { GridHeaderRow, GridScroll } from '../../sheet/ListGrid';
import { SearchField } from '../../sheet/SearchField';
import { FrameIcon } from './FrameIcon';
import filterIcon from './frames/dnd_icon_filter.svg?raw';
import concentrationIcon from './frames/dnd_icon_marker_concentration.svg?raw';
import ritualIcon from './frames/dnd_icon_marker_ritual.svg?raw';
import { groupHeaderValues, type HeaderValue } from './spellcastingHeader';
import { levelAbbreviation, levelLabel } from './SpellCast';
import { SpellRow } from './SpellRow';
import { buildSpellDetailRequest } from './spellDetail';
import { EMPTY_ADVANCED_FILTERS, hasActiveAdvancedFilters, matchesAdvancedFilters, SpellsAdvancedFilters } from './SpellsAdvancedFilters';

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/** One spellcasting header stat; each value's tooltip names the classes it comes from. */
function HeaderStat({ label, values, signed = false }: { label: string; values: HeaderValue[]; signed?: boolean }) {
  return (
    <div className="spellcasting-header__stat">
      <div className="spellcasting-header__value">
        {values.map((entry) => (
          <span key={entry.value} className="spellcasting-header__value-item" title={entry.sources.join(', ')}>
            {signed ? formatSigned(entry.value) : entry.value}
          </span>
        ))}
      </div>
      <div className="spellcasting-header__label">{label}</div>
    </div>
  );
}

type SpellsTabProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  onOpenSpellManagement: () => void;
};

/**
 * Build order step 8 (systems/dnd-5e/sheet-build.md), scoped to the display-only part of
 * systems/dnd-5e/sheet-ui.md's Spells tab, confirmed with the owner: header numbers per
 * spellcasting class, the spell list grouped by level, concentration/ritual
 * markers, level filter chips and search. Header numbers are plain display, not
 * Explainer triggers, same reasoning as before. Header layout and the
 * Concentration/Ritual toggle buttons were reworked in phase 10 slice 8 — see
 * `SpellRow.tsx`'s doc comment for the table-column rework that came with it.
 * No "Manage spells" collection
 * editor yet — that is phase 9's next slice (roadmap.md). The slot track per
 * level is real as of phase 9's spell slot slice
 * (`Dnd5eSheetMutator.consumeSpellSlot`/`restoreSpellSlot`): it renders next to
 * each level's heading, the same BoxTrack primitive feature actions use,
 * matching this tab's own spec ("each group showing its slot track"). Cantrips
 * (level 0) never have a slot track. Casting (phase 9) opens from a leveled
 * spell's Entity Detail panel, per this tab's own spec ("Casting from the
 * detail panel allows choosing a slot level; the slot consumed is the chosen
 * level, not the spell's base level") — `SpellCast.tsx` builds that action bar,
 * reusing the same generic `consumeSpellSlot` mutation the level heading's
 * BoxTrack already spends. "Manage spells" (phase 9's next slice) opens
 * `SpellManagementPanel` — not the Collection editor mold this tab's own spec
 * originally named, since that mold's flat free-text-list shape cannot express
 * "known spells and prepared spells, split per class, showing cantrips known and
 * preparation limits" — confirmed a genuine gap live against D&D Beyond (Cleric,
 * Wizard, Ranger), not an oversight; see `SpellManagementRequest`'s doc comment.
 *
 * **2026-09-03:** this tab still lists every spell the character has
 * (attack, healing and utility alike, unchanged) — a damage-dealing or
 * healing spell now *also* renders in the Actions tab (`ActionsTab.tsx`'s
 * `SpellAttackRow`, `spellCombat.ts`'s `isCombatSpell`), confirmed live
 * against D&D Beyond that a combat spell sits in both tabs at once, not one
 * or the other. `buildSpellDetailRequest` (`spellDetail.tsx`) was extracted
 * out of this file's own `onOpenDetail` so both rows open the identical
 * Entity Detail panel for the same spell.
 */
export function SpellsTab({ sheet, onRoll, onRollContextMenu, onMutate, onOpenDetail, onOpenSpellManagement }: SpellsTabProps) {
  const [activeFilter, setActiveFilter] = useState('all');
  const [search, setSearch] = useState('');
  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  const [advancedFilters, setAdvancedFilters] = useState(EMPTY_ADVANCED_FILTERS);

  const pactPool = sheet.spellSlots.find((slot) => slot.pact);
  /** As on D&D Beyond, the Pact Magic level also lists the pact class's lower-level spells, cast at that level. */
  const castAtPactLevel = (spell: Spell) =>
    pactPool != null && spell.className === pactPool.className && spell.level > 0 && spell.level < pactPool.level;
  const pactLevels = pactPool && sheet.spells.some(castAtPactLevel) ? [pactPool.level] : [];
  const levels = Array.from(new Set([...sheet.spells.map((spell) => spell.level), ...pactLevels])).sort((a, b) => a - b);
  const hasConcentrationSpells = sheet.spells.some((spell) => spell.concentration);
  const hasRitualSpells = sheet.spells.some((spell) => spell.ritual);
  const chips = [
    { id: 'all', label: 'All' },
    ...levels.map((level) => ({ id: String(level), label: levelAbbreviation(level) })),
    ...(hasConcentrationSpells
      ? [{ id: 'concentration', label: <FrameIcon className="filter-chip__icon" svg={concentrationIcon} aria-hidden />, ariaLabel: 'Concentration' }]
      : []),
    ...(hasRitualSpells
      ? [{ id: 'ritual', label: <FrameIcon className="filter-chip__icon" svg={ritualIcon} aria-hidden />, ariaLabel: 'Ritual' }]
      : []),
  ];

  const visibleSpells = sheet.spells.filter((spell) => {
    if (activeFilter === 'concentration') {
      if (!spell.concentration) return false;
    } else if (activeFilter === 'ritual') {
      if (!spell.ritual) return false;
    } else if (
      activeFilter !== 'all' &&
      spell.level !== Number(activeFilter) &&
      !(castAtPactLevel(spell) && pactPool?.level === Number(activeFilter))
    ) {
      return false;
    }
    if (!matchesAdvancedFilters(spell, advancedFilters)) {
      return false;
    }
    return spell.name.toLowerCase().includes(search.toLowerCase());
  });
  const spellsAtLevel = (level: number) => {
    const own = visibleSpells.filter((spell) => spell.level === level);
    if (pactPool?.level !== level) {
      return own;
    }
    return [...own, ...visibleSpells.filter(castAtPactLevel)].sort((a, b) => a.name.localeCompare(b.name));
  };
  const spellsByLevel = levels
    .map((level) => ({ level, spells: spellsAtLevel(level) }))
    .filter(({ spells }) => spells.length > 0);

  return (
    <div className="spells-tab">
      {sheet.spellcasting.length === 0 ? (
        <p style={{ fontSize: '11px', color: 'var(--text-muted, #6B7A85)' }}>This character has no spellcasting.</p>
      ) : (
        <>
          <div className="spellcasting-header">
            <HeaderStat label="Modifier" values={groupHeaderValues(sheet.spellcasting, (info) => info.spellcastingModifier.value)} signed />
            <HeaderStat label="Spell Attack" values={groupHeaderValues(sheet.spellcasting, (info) => info.spellAttackBonus.value)} signed />
            <HeaderStat label="Save DC" values={groupHeaderValues(sheet.spellcasting, (info) => info.spellSaveDc.value)} />
          </div>
          <div className="tab-actions-row spells-actions-row">
            <SearchField value={search} onChange={setSearch} placeholder="Search spells" />
            <button
              type="button"
              className={`mutate spells-filter-button${hasActiveAdvancedFilters(advancedFilters) ? ' spells-filter-button--active' : ''}`}
              aria-label="Advanced filters"
              aria-pressed={showAdvancedFilters}
              onClick={() => setShowAdvancedFilters((current) => !current)}
            >
              <FrameIcon className="spells-filter-button__icon" svg={filterIcon} aria-hidden />
            </button>
            <button type="button" className="mutate spells-manage-button" onClick={onOpenSpellManagement}>
              Manage Spells
            </button>
          </div>
        </>
      )}

      {sheet.spells.length > 0 && showAdvancedFilters && (
        <GridScroll>
          <SpellsAdvancedFilters spells={sheet.spells} filters={advancedFilters} onChange={setAdvancedFilters} />
        </GridScroll>
      )}

      {sheet.spells.length > 0 && !showAdvancedFilters && (
        <>
          <div className="grid-filter-row">
            <FilterChips chips={chips} activeChip={activeFilter} onSelect={setActiveFilter} />
          </div>

          <GridScroll>
            {spellsByLevel.map(({ level, spells }) => {
              const pools = sheet.spellSlots.filter((candidate) => candidate.level === level).sort((a, b) => Number(a.pact) - Number(b.pact));
              return (
              <section key={level}>
                <div className="spells-level__heading">
                  <div className="spells-level__heading-label">{levelLabel(level)}</div>
                  {pools.length > 0 && (
                    <div className="spells-level__slot-groups">
                      {pools.map((slot) => (
                        <div key={slot.pact ? 'pact' : 'slots'} className="spells-level__slot-group">
                          <BoxTrack
                            maxUses={slot.maxSlots}
                            usedCount={slot.usedSlots}
                            onUse={() => onMutate({ type: 'CONSUME_SPELL_SLOT', level, pact: slot.pact })}
                            onRestore={() => onMutate({ type: 'RESTORE_SPELL_SLOT', level, pact: slot.pact })}
                          />
                          <span className="spells-level__slot-label">{slot.pact ? 'Pact' : 'Slots'}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                <GridHeaderRow
                  rowClassName="spell-row"
                  columns={[
                    { key: 'cast', className: 'spell-row__cast' },
                    { key: 'name', className: 'spell-row__name-cell grid-header-cell', content: 'Name' },
                    { key: 'time', className: 'spell-row__time grid-header-cell', content: 'Time' },
                    { key: 'range', className: 'spell-row__range grid-header-cell', content: 'Range' },
                    { key: 'hit-dc', className: 'spell-row__hit-dc grid-header-cell', content: 'Hit / DC' },
                    { key: 'effect', className: 'spell-row__effect grid-header-cell', content: 'Effect' },
                    { key: 'notes', className: 'spell-row__notes grid-header-cell', content: 'Notes' },
                  ]}
                />
                <div className="action-list">
                  {spells.map((spell) => (
                    <SpellRow
                      key={spell.key}
                      castLevel={spell.level < level ? level : undefined}
                      spell={spell}
                      spellcasting={sheet.spellcasting.find((info) => info.className === spell.className)}
                      onRoll={onRoll}
                      onRollContextMenu={onRollContextMenu}
                      onOpenDetail={() => onOpenDetail(buildSpellDetailRequest(spell, sheet, onMutate, onRoll))}
                    />
                  ))}
                </div>
              </section>
              );
            })}
          </GridScroll>
        </>
      )}
    </div>
  );
}

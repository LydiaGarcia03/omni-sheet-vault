import type { ReactNode } from 'react';
import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, ForcedRollMode, MutationHandler, PaneHandler, RollNote } from '../../sheet/api';
import { ABILITIES } from './abilities';
import { SavingThrowPane, SavingThrowsPane } from './Dnd5eSavingThrowPanes';
import frameSvg from './frames/dnd_frame_saving_throws.svg?raw';
import { RollModeMarker } from './RollModeMarker';
import { SectionPanel } from './SectionPanel';
import { SavingThrowRow } from './SavingThrowRow';

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/** The saving throws a roll note concerns: every save, or one ability's ("DEXTERITY_SAVING_THROWS"). */
function noteAbility(target: string): string | null | undefined {
  if (target === 'SAVING_THROWS') {
    return null;
  }
  const match = /^([A-Z]+)_SAVING_THROWS$/.exec(target);
  return match ? ABILITIES.find(({ key }) => key === match[1].toLowerCase())?.label : undefined;
}

/**
 * The lines below the grid, as D&D Beyond's dice adjustment summary: one "on <Ability> (sources)"
 * line per save the sheet forces a mode on, then each situational note ("against poison"; the
 * source is in the icon's tooltip, as on D&D Beyond), which is shown but never applied.
 */
export function SavingThrowModifiers({ sheet, heading }: { sheet: CharacterSheet; heading?: ReactNode }) {
  const forced = ABILITIES.flatMap(({ key, label }) => {
    const rollMode = sheet.rollModes[`SAVING_THROW:${key}`];
    return rollMode && rollMode.mode !== 'NORMAL' ? [{ key, label, rollMode }] : [];
  });
  const notes = (sheet.rollNotes ?? []).flatMap((note) => {
    const ability = noteAbility(note.target);
    return ability === undefined ? [] : [{ note, ability }];
  });
  if (forced.length === 0 && notes.length === 0) {
    return null;
  }
  return (
    <>
      {heading}
      <div className="saving-panel__modifiers">
        {forced.map(({ key, label, rollMode }) => (
          <div key={key} className="saving-panel__modifier">
            <RollModeMarker rollMode={rollMode} className="saving-panel__modifier-icon" />
            on <span className="saving-panel__modifier-ability">{label}</span>{' '}
            ({(rollMode.mode === 'ADVANTAGE' ? rollMode.advantageSources : rollMode.disadvantageSources).join(', ')})
          </div>
        ))}
        {notes.map(({ note, ability }) => (
          <div key={`${note.source}:${note.restriction}`} className="saving-panel__modifier">
            <RollModeMarker rollMode={noteAsRollMode(note)} className="saving-panel__modifier-icon" />
            {ability && (
              <>
                on <span className="saving-panel__modifier-ability">{ability}</span>{' '}
              </>
            )}
            {note.restriction}
          </div>
        ))}
      </div>
    </>
  );
}

function noteAsRollMode(note: RollNote): ForcedRollMode {
  const advantage = note.mode === 'ADVANTAGE';
  return {
    mode: advantage ? 'ADVANTAGE' : 'DISADVANTAGE',
    advantageSources: advantage ? [`${note.source}, ${note.restriction}`] : [],
    disadvantageSources: advantage ? [] : [`${note.source}, ${note.restriction}`],
  };
}

type SavingThrowsGridProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onOpenSave?: (ability: string) => void;
};

/**
 * 2-column x 3-row grid (STR/DEX/CON left, INT/WIS/CHA right), confirmed live against D&D Beyond
 * (.ct-saving-throws-box: two 107px-wide columns 13.5px apart, three 34px rows 5px apart);
 * `grid-auto-flow: column` fills column 1 with ABILITIES' first three entries.
 */
export function SavingThrowsGrid({ sheet, onRoll, onRollContextMenu, onOpenSave }: SavingThrowsGridProps) {
  return (
    <div className="saving-panel__grid">
      {ABILITIES.map(({ key, abbreviation, label }) => (
        <SavingThrowRow
          key={key}
          abbreviation={abbreviation}
          fullName={label}
          proficiency={sheet.savingThrowProficiencies[key] ?? 'NONE'}
          modifier={sheet.savingThrows[key].value}
          onRoll={() => onRoll('SAVING_THROW', key)}
          onRollContextMenu={(x, y) => onRollContextMenu(x, y, 'SAVING_THROW', key, formatSigned(sheet.savingThrows[key].value))}
          onOpenDetail={onOpenSave ? () => onOpenSave(key) : undefined}
        />
      ))}
    </div>
  );
}

type SavingThrowsPanelProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onOpenPane: PaneHandler;
};

/** The sheet's saving throws box: the grid, the modifiers, and a gear opening D&D Beyond's Saving Throws pane. */
export function SavingThrowsPanel({ sheet, onRoll, onRollContextMenu, onMutate, onOpenPane }: SavingThrowsPanelProps) {
  const openSavingThrows = () =>
    onOpenPane({
      kind: 'pane',
      render: (liveSheet) => (
        <SavingThrowsPane sheet={liveSheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenSave={openSave} />
      ),
    });
  const openSave = (ability: string) =>
    onOpenPane({
      kind: 'pane',
      render: (liveSheet) => (
        <SavingThrowPane sheet={liveSheet} ability={ability} onMutate={onMutate} onOpenSavingThrows={openSavingThrows} />
      ),
    });

  return (
    <SectionPanel frameClass="saving-panel" svg={frameSvg} title="Saving Throws" onSettings={openSavingThrows}>
      <SavingThrowsGrid sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenSave={openSave} />
      <SavingThrowModifiers sheet={sheet} />
    </SectionPanel>
  );
}

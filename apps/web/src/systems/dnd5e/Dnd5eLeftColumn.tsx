import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, MutationHandler, PaneHandler } from '../../sheet/api';
import { ProficienciesPanel } from './ProficienciesPanel';
import { SavingThrowsPanel } from './SavingThrowsPanel';
import { SensesPanel } from './SensesPanel';

type Dnd5eLeftColumnProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onOpenPane: PaneHandler;
};

export function Dnd5eLeftColumn({ sheet, onRoll, onRollContextMenu, onMutate, onOpenPane }: Dnd5eLeftColumnProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <SavingThrowsPanel sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onMutate={onMutate} onOpenPane={onOpenPane} />
      <SensesPanel sheet={sheet} onMutate={onMutate} onOpenPane={onOpenPane} />
      <ProficienciesPanel sheet={sheet} onMutate={onMutate} onOpenPane={onOpenPane} />
    </div>
  );
}

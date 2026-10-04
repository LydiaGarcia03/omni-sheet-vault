import type { BackgroundCatalogueEntryData } from '../../catalogue/api';
import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type {
  BackgroundField,
  CharacterSheet,
  EntityDetailHandler,
  MutationHandler,
  PaneHandler,
  TextFieldRequest,
} from '../../sheet/api';
import { ArmorClass } from './ArmorClass';
import { DefensesConditionsPanel } from './DefensesConditionsPanel';
import { Dnd5eArmorClassPane, Dnd5eDefensesPane, Dnd5eInitiativePane } from './Dnd5eCombatPanes';
import { Dnd5eTabbedSection } from './Dnd5eTabbedSection';
import { Initiative } from './Initiative';

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

type Dnd5eCombatColumnProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onOpenPane: PaneHandler;
  onOpenConditions: () => void;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  onOpenSpellManagement: () => void;
  onOpenManageCustomActions: () => void;
  onOpenManageInventory: () => void;
  onOpenManageFeats: () => void;
  backgroundSuggestions: BackgroundCatalogueEntryData | null;
  onUpdateBackgroundField: (field: BackgroundField, value: string) => void;
  onOpenTextField: (request: TextFieldRequest) => void;
};

/**
 * The tabbed section (Actions/Spells/Inventory/etc.) stacks below the top row
 * here rather than spanning the whole page below all three vitals columns —
 * see Dnd5eTabbedSection.tsx's doc comment for why this placement, not a
 * full-width one, is what lets its frame fit.
 *
 * Defenses/Conditions sits beside Initiative/Armor Class (not stacked below
 * them) per the owner's direct request: that widens the column's own natural
 * width past Defenses/Conditions' fixed 408px, which the tabbed section below
 * now stretches to match (`.tabbed-section { width: 100% }`) — enough room
 * for all six tabs to render without the horizontal scroll a narrower box
 * needed; only vertical scroll remains, same as before.
 */
export function Dnd5eCombatColumn({
  sheet,
  onRoll,
  onRollContextMenu,
  onOpenPane,
  onOpenConditions,
  onMutate,
  onOpenDetail,
  onOpenSpellManagement,
  onOpenManageCustomActions,
  onOpenManageInventory,
  onOpenManageFeats,
  backgroundSuggestions,
  onUpdateBackgroundField,
  onOpenTextField,
}: Dnd5eCombatColumnProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-start' }}>
        <Initiative
          modifier={sheet.initiative.value}
          rollMode={sheet.rollModes.INITIATIVE}
          onRoll={() => onRoll('INITIATIVE', null)}
          onRollContextMenu={(x, y) => onRollContextMenu(x, y, 'INITIATIVE', null, formatSigned(sheet.initiative.value))}
          onOpenPane={() => onOpenPane({ kind: 'pane', render: (liveSheet) => <Dnd5eInitiativePane sheet={liveSheet} /> })}
        />
        <ArmorClass
          value={sheet.armorClass.value}
          onOpenExplainer={() =>
            onOpenPane({ kind: 'pane', render: (liveSheet) => <Dnd5eArmorClassPane sheet={liveSheet} onMutate={onMutate} /> })
          }
        />
        <DefensesConditionsPanel
          sheet={sheet}
          onOpenConditions={onOpenConditions}
          onOpenDefenses={() =>
            onOpenPane({ kind: 'pane', render: (liveSheet) => <Dnd5eDefensesPane sheet={liveSheet} onMutate={onMutate} /> })
          }
        />
      </div>
      <Dnd5eTabbedSection
        sheet={sheet}
        onRoll={onRoll}
        onRollContextMenu={onRollContextMenu}
        onMutate={onMutate}
        onOpenDetail={onOpenDetail}
        onOpenSpellManagement={onOpenSpellManagement}
        onOpenManageCustomActions={onOpenManageCustomActions}
        onOpenManageInventory={onOpenManageInventory}
        onOpenManageFeats={onOpenManageFeats}
        backgroundSuggestions={backgroundSuggestions}
        onUpdateBackgroundField={onUpdateBackgroundField}
        onOpenTextField={onOpenTextField}
      />
    </div>
  );
}

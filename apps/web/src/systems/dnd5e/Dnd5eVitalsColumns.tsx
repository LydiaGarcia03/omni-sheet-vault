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
import { Dnd5eCombatColumn } from './Dnd5eCombatColumn';
import { Dnd5eLeftColumn } from './Dnd5eLeftColumn';
import { SkillsPanel } from './SkillsPanel';

type Dnd5eVitalsColumnsProps = {
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
 * The real three-column grid: saving throws/senses/proficiencies, skills, then
 * initiative/armor class/defenses/conditions/tabbed section. Column widths and
 * the gutter between them aren't measured anywhere (ui-design-tokens.md's
 * "still unmeasured" list) — the 16px gutter reuses the value already used for
 * vertical panel spacing, and every column keeps the same fixed 278px panel
 * width rather than inventing per-column widths. Estimated from
 * design-reference/screenshots/dnd-character-sheet, not a measured value.
 * The third column's tabbed section (Actions/Spells/Inventory/etc.) is
 * threaded through here rather than rendered as a sibling below this whole
 * row — see Dnd5eCombatColumn.tsx's doc comment for why.
 */
export function Dnd5eVitalsColumns({
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
}: Dnd5eVitalsColumnsProps) {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '16px' }}>
      <Dnd5eLeftColumn
        sheet={sheet}
        onRoll={onRoll}
        onRollContextMenu={onRollContextMenu}
        onMutate={onMutate}
        onOpenPane={onOpenPane}
      />
      <SkillsPanel sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onMutate={onMutate} onOpenPane={onOpenPane} />
      <Dnd5eCombatColumn
        sheet={sheet}
        onRoll={onRoll}
        onRollContextMenu={onRollContextMenu}
        onOpenPane={onOpenPane}
        onOpenConditions={onOpenConditions}
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

import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, MutationHandler } from '../../sheet/api';
import { RulesText } from '../../sheet/RulesText';
import { SidebarHeader } from '../../sheet/sidebarParts';
import { ABILITIES } from './abilities';
import { ABILITY_ICONS } from './abilityIcons';
import { CheckCustomize } from './CheckCustomize';
import { customizationsOf, EMPTY_CHECK } from './customizations';
import { FrameIcon } from './FrameIcon';
import { SAVING_THROWS_RULES_TEXT } from './rulesText';
import { SavingThrowModifiers, SavingThrowsGrid } from './SavingThrowsPanel';

type SavingThrowsPaneProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onOpenSave: (ability: string) => void;
};

/** D&D Beyond's Saving Throws pane: the saves grid, the saving throw modifiers, then the rules text. */
export function SavingThrowsPane({ sheet, onRoll, onRollContextMenu, onOpenSave }: SavingThrowsPaneProps) {
  return (
    <div className="saving-throws-pane">
      <SidebarHeader title="Saving Throws" />
      <SavingThrowsGrid sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenSave={onOpenSave} />
      <SavingThrowModifiers sheet={sheet} heading={<h2 className="sidebar-subheading">Saving Throw Modifiers</h2>} />
      <RulesText text={SAVING_THROWS_RULES_TEXT} />
    </div>
  );
}

type SavingThrowPaneProps = {
  sheet: CharacterSheet;
  ability: string;
  onMutate: MutationHandler;
  onOpenSavingThrows: () => void;
};

/** D&D Beyond's pane for one saving throw: its value, the Customize that overrides it, then the rules text. */
export function SavingThrowPane({ sheet, ability, onMutate, onOpenSavingThrows }: SavingThrowPaneProps) {
  const label = ABILITIES.find((entry) => entry.key === ability)?.label ?? ability;
  const value = customizationsOf(sheet).savingThrows[ability] ?? EMPTY_CHECK;
  return (
    <div className="saving-throw-pane">
      <SidebarHeader
        parent="Saving Throws"
        onOpenParent={onOpenSavingThrows}
        icon={<FrameIcon svg={ABILITY_ICONS[ability]} className="explainer-icon" aria-hidden />}
        title={`${label} Saving Throw`}
        modifier={sheet.savingThrows[ability].value}
      />
      <CheckCustomize
        label="Saving Throw"
        value={value}
        onChange={(next) => onMutate({ type: 'CUSTOMIZE', group: 'savingThrows', target: ability, value: next })}
      />
      <RulesText text={SAVING_THROWS_RULES_TEXT} />
    </div>
  );
}

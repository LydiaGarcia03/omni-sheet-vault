import { useState } from 'react';
import type { BackgroundCatalogueEntryData } from '../../catalogue/api';
import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { BackgroundField, CharacterSheet, EntityDetailHandler, MutationHandler, TextFieldRequest } from '../../sheet/api';
import { TabBar } from '../../sheet/TabBar';
import { ActionsTab } from './ActionsTab';
import { BackgroundTab } from './BackgroundTab';
import { ExtrasTab } from './ExtrasTab';
import { FeaturesTab } from './FeaturesTab';
import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame_actions.svg?raw';
import inkSvg from './frames/dnd_frame_actions_ink.svg?raw';
import { InventoryTab } from './InventoryTab';
import { SpellsTab } from './SpellsTab';

/**
 * Labels shortened from D&D Beyond's own ("Features & Traits", "Background &
 * Notes") to fit six tabs across this box's own, narrower width — confirmed
 * live D&D Beyond actually splits "Background" and "Notes" into two separate
 * tabs (7 total), which this app doesn't (one combined tab, per phase 8's
 * scope); "Background" alone still reads correctly for the combined tab.
 * "Spells" is the one entry left out here — it's conditional, see below.
 */
const BASE_TABS = [
  { id: 'actions', label: 'Actions' },
  { id: 'inventory', label: 'Inventory' },
  { id: 'features', label: 'Features' },
  { id: 'background', label: 'Background' },
  { id: 'extras', label: 'Extras' },
];
const SPELLS_TAB = { id: 'spells', label: 'Spells' };

type Dnd5eTabbedSectionProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
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
 * Build order step 6/8 (systems/dnd-5e/sheet-build.md): all six tabs are built,
 * closing out phase 8's tab list. **General re-verification, 2026-08-16:**
 * Spells is now conditional on `sheet.spellcasting.length > 0` — confirmed
 * live against a non-caster reference (a Minotaur Barbarian) that D&D Beyond
 * omits the Spells tab entirely rather than showing it empty. If the active
 * tab is "spells" and spellcasting becomes unavailable, `effectiveTab` falls
 * back to "actions" rather than rendering nothing.
 *
 * `dnd_frame_actions.svg` was tried here and reverted once, then re-applied
 * in the same session once its actual problem was found: this component
 * used to render as a full-width sibling below the three vitals columns
 * (`Dnd5eVitalsColumns`), stretching to the page's full content width
 * (measured 1833x552, a 3.32 ratio, all "dead space" — the three columns
 * above it only need ~1000px combined) — nothing to do with the frame
 * asset itself, whose native ratio (784x829, ≈0.946) is nearly square,
 * matching D&D Beyond's own `.ct-primary-box` almost exactly (measured
 * live: 623x660, ≈0.944). Fixed by moving this component to live *inside*
 * `Dnd5eCombatColumn`, stacked below Defenses/Conditions instead of below
 * the whole three-column row — the same placement D&D Beyond uses (the tab
 * box sits under its own right-hand combat column, not spanning the page).
 * That gives it the combat column's own fixed 408px width (matching
 * `.defenses-conditions-panel`) rather than the viewport's, so a locked
 * `aspect-ratio: 784/829` frame (same fixed-box-plus-internal-scroll
 * pattern every other panel already uses — Saving Throws, Skills, Senses,
 * Proficiencies, Defenses/Conditions, Hit Points) fits it cleanly instead
 * of distorting. Tab content overflow now scrolls inside the box
 * (`.tabbed-section__content`) rather than growing it, same tradeoff those
 * other panels already accepted.
 *
 * **Direct owner request, 2026-08-17:** the tab buttons and the active tab's
 * content are split into two children — `TabBar` and a new
 * `.tabbed-section__scroll` wrapper — instead of one `overflow-y: auto`
 * div holding both, per D&D Beyond's own `.ct-primary-box` (confirmed live:
 * padding sits on the outer, non-scrolling box; a separate inner
 * `styles_content` div, `padding: 0`, is the one with `overflow-y: auto`,
 * nested *inside* the already-padded box — `styles_tabList`, a flex column,
 * holds the fixed-height tab row and that scrollable content div as
 * siblings). Our own previous single-div version put the padding and the
 * scrolling on the same element, so the native scrollbar — appearing only
 * on tabs whose content overflows 663px, e.g. Background's 893px — sat
 * flush against the box's own right edge with zero clearance from the
 * padding, visually overlapping the frame's drawn ink border and reading
 * as the frame "cutting" the content (owner report, 2026-08-17, after the
 * box's height was fixed at 663px for column alignment — see frames.css).
 * Splitting the scroll onto its own child, inset by the outer wrapper's own
 * padding, keeps the scrollbar clear of the border, same as the reference.
 */
export function Dnd5eTabbedSection({
  sheet,
  onRoll,
  onRollContextMenu,
  onMutate,
  onOpenDetail,
  onOpenSpellManagement,
  onOpenManageCustomActions,
  onOpenManageInventory,
  onOpenManageFeats,
  backgroundSuggestions,
  onUpdateBackgroundField,
  onOpenTextField,
}: Dnd5eTabbedSectionProps) {
  const [activeTab, setActiveTab] = useState('actions');
  const hasSpellcasting = sheet.spellcasting.length > 0;
  const tabs = hasSpellcasting
    ? [BASE_TABS[0], SPELLS_TAB, ...BASE_TABS.slice(1)]
    : BASE_TABS;
  const effectiveTab = activeTab === 'spells' && !hasSpellcasting ? 'actions' : activeTab;

  return (
    <div className="frame-box tabbed-section">
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className="tabbed-section__content">
        <TabBar tabs={tabs} activeTab={effectiveTab} onSelect={setActiveTab} />
        <div className="tabbed-section__scroll">
          {effectiveTab === 'actions' && (
            <ActionsTab
              sheet={sheet}
              onRoll={onRoll}
              onRollContextMenu={onRollContextMenu}
              onMutate={onMutate}
              onOpenDetail={onOpenDetail}
              onOpenManageCustomActions={onOpenManageCustomActions}
            />
          )}
          {effectiveTab === 'spells' && (
            <SpellsTab
              sheet={sheet}
              onRoll={onRoll}
              onRollContextMenu={onRollContextMenu}
              onMutate={onMutate}
              onOpenDetail={onOpenDetail}
              onOpenSpellManagement={onOpenSpellManagement}
            />
          )}
          {effectiveTab === 'inventory' && (
            <InventoryTab
              sheet={sheet}
              onMutate={onMutate}
              onOpenDetail={onOpenDetail}
              onOpenManageInventory={onOpenManageInventory}
            />
          )}
          {effectiveTab === 'features' && (
            <FeaturesTab sheet={sheet} onMutate={onMutate} onOpenDetail={onOpenDetail} onOpenManageFeats={onOpenManageFeats} />
          )}
          {effectiveTab === 'background' && (
            <BackgroundTab
              sheet={sheet}
              suggestions={backgroundSuggestions}
              onUpdateField={onUpdateBackgroundField}
              onOpenTextField={onOpenTextField}
            />
          )}
          {effectiveTab === 'extras' && <ExtrasTab sheet={sheet} onMutate={onMutate} onOpenDetail={onOpenDetail} />}
        </div>
      </div>
    </div>
  );
}

import type { CharacterSheet, MutationHandler, PaneHandler } from '../../sheet/api';
import { Dnd5eProficienciesPane } from './Dnd5eProficienciesPane';
import frameSvg from './frames/dnd_frame_proficiencies_training.svg?raw';
import frameInkSvg from './frames/dnd_frame_proficiencies_training_ink.svg?raw';
import { SectionPanel } from './SectionPanel';

type ProficienciesPanelProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenPane: PaneHandler;
};

/** Each entry's source from the build ("Rogue 1"), shown as its tooltip; entries added by hand have none. */
function sourcesByLabel(sheet: CharacterSheet): Map<string, string> {
  return new Map((sheet.provenance?.proficiencySources ?? []).map((entry) => [entry.label, entry.source]));
}

/** The sheet's proficiencies box; its gear opens D&D Beyond's Proficiencies & Training pane. */
export function ProficienciesPanel({ sheet, onMutate, onOpenPane }: ProficienciesPanelProps) {
  const sources = sourcesByLabel(sheet);
  const openPane = () =>
    onOpenPane({ kind: 'pane', render: (liveSheet) => <Dnd5eProficienciesPane sheet={liveSheet} onMutate={onMutate} /> });
  const categories = [
    { label: 'Armor', items: sheet.armorProficiencies },
    { label: 'Weapons', items: sheet.weaponProficiencies },
    { label: 'Tools', items: sheet.toolProficiencies },
    { label: 'Languages', items: sheet.languages },
  ].filter((category) => category.items.length > 0);

  return (
    <SectionPanel frameClass="proficiencies-panel" svg={frameSvg} inkSvg={frameInkSvg} title="Proficiencies and Training" onSettings={openPane}>
      {/* Plain divs, not a <dl> — the browser's own default margin on <dl>
          isn't cleared anywhere in frames.css. */}
      <div className="panel__list" role="list">
        {categories.map(({ label, items }) => (
          <div key={label} className="panel__list-item" role="listitem">
            <div className="panel__list-label">{label}</div>
            <div className="panel__list-value">
              {items.map((item, index) => (
                <span key={item} title={sources.has(item) ? `From ${sources.get(item)}` : undefined}>
                  {item}
                  {index < items.length - 1 && ', '}
                </span>
              ))}
            </div>
          </div>
        ))}
      </div>
    </SectionPanel>
  );
}

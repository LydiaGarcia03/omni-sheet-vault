import type { CharacterSheet, MutationHandler, PaneHandler } from '../../sheet/api';
import { Dnd5eSensesPane, PASSIVES } from './Dnd5eSensesPane';
import { SENSES_RULES_TEXT } from './rulesText';
import frameSvg from './frames/dnd_frame_senses.svg?raw';
import { SectionPanel } from './SectionPanel';
import { SenseRow } from './SenseRow';

type SensesPanelProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenPane: PaneHandler;
};

/**
 * The three passive scores, then the special senses line ("Additional Sense Types" without any), which, like the
 * gear on the title, opens D&D Beyond's Senses pane.
 */
export function SensesPanel({ sheet, onMutate, onOpenPane }: SensesPanelProps) {
  const openSenses = () =>
    onOpenPane({
      kind: 'pane',
      render: (liveSheet) => <Dnd5eSensesPane sheet={liveSheet} onMutate={onMutate} rulesText={SENSES_RULES_TEXT} />,
    });

  return (
    <SectionPanel frameClass="senses-panel" svg={frameSvg} title="Senses" onSettings={openSenses}>
      <div className="panel__rows">
        {PASSIVES.map(({ key, label }) => (
          <SenseRow key={key} value={sheet.senses[key].value} label={label} />
        ))}
      </div>
      <button type="button" className="reveal senses-panel__trigger" onClick={openSenses}>
        {sheet.specialSenses.length > 0 ? sheet.specialSenses.map((sense) => sense.label).join(', ') : 'Additional Sense Types'}
      </button>
    </SectionPanel>
  );
}

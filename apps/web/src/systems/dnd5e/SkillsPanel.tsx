import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, MutationHandler, PaneHandler } from '../../sheet/api';
import { AllSkillsPane, CustomSkillPane, CustomSkillRows, SkillPane, SkillRows, type SkillEntry, type SkillPaneNavigation } from './Dnd5eSkillPanes';
import frameSvg from './frames/dnd_frame_skills.svg?raw';
import frameInkSvg from './frames/dnd_frame_skills_ink.svg?raw';
import { SectionPanel } from './SectionPanel';

type SkillsPanelProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onOpenPane: PaneHandler;
};

/** The sheet's skills box: the eighteen skills, the custom ones, "Additional Skills" and the gear opening All Skills. */
export function SkillsPanel({ sheet, onRoll, onRollContextMenu, onMutate, onOpenPane }: SkillsPanelProps) {
  const navigation: SkillPaneNavigation = {
    openAllSkills: () =>
      onOpenPane({
        kind: 'pane',
        render: (liveSheet) => (
          <AllSkillsPane sheet={liveSheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenEntry={openEntry} onMutate={onMutate} />
        ),
      }),
    openEntry: (entry) => openEntry(entry),
  };
  function openEntry(entry: SkillEntry) {
    onOpenPane({
      kind: 'pane',
      render: (liveSheet) =>
        entry.kind === 'skill' ? (
          <SkillPane sheet={liveSheet} skill={entry.key} onMutate={onMutate} navigation={navigation} />
        ) : (
          <CustomSkillPane sheet={liveSheet} skillKey={entry.key} onMutate={onMutate} navigation={navigation} />
        ),
    });
  }

  return (
    <SectionPanel frameClass="skills-panel" svg={frameSvg} inkSvg={frameInkSvg} title="Skills" onSettings={navigation.openAllSkills}>
      <div className="skill-row skill-row--header" aria-hidden="true">
        <div className="skill-row__prof-cell">Prof</div>
        <div className="skill-row__ability">Mod</div>
        <div className="skill-row__name">Skill</div>
        <div className="skill-row__mod">Bonus</div>
      </div>
      <div className="panel__rows">
        <SkillRows sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenEntry={openEntry} />
        <CustomSkillRows sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenEntry={openEntry} />
      </div>
      <button type="button" className="reveal skills-panel__additional" onClick={navigation.openAllSkills}>
        Additional Skills
      </button>
    </SectionPanel>
  );
}

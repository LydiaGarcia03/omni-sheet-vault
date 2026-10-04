import { useEffect, useState } from 'react';
import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type { CharacterSheet, CustomSkill, MutationHandler, ProficiencyLevel } from '../../sheet/api';
import { RulesText } from '../../sheet/RulesText';
import { Customize, SidebarCollapsible, SidebarHeader, SidebarPager } from '../../sheet/sidebarParts';
import { ABILITIES, abilityAbbreviation } from './abilities';
import { CheckCustomize } from './CheckCustomize';
import { customizationsOf, EMPTY_CHECK, type StoredCustomSkill } from './customizations';
import { ProficiencyDot } from './ProficiencyDot';
import { SKILL_RULES_TEXT } from './rulesText';
import { SkillRow } from './SkillRow';
import { SKILLS } from './skills';

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/** A skill or a custom skill, in the order the panes step through them. */
export type SkillEntry = { kind: 'skill' | 'custom'; key: string };

export function skillEntries(sheet: CharacterSheet): SkillEntry[] {
  return [
    ...SKILLS.map(({ key }) => ({ kind: 'skill' as const, key })),
    ...(sheet.customSkills ?? []).map(({ key }) => ({ kind: 'custom' as const, key })),
  ];
}

/** How the skill panes open each other: the list, a skill, or a custom skill. */
export type SkillPaneNavigation = {
  openAllSkills: () => void;
  openEntry: (entry: SkillEntry) => void;
};

type SkillRowsProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onOpenEntry: (entry: SkillEntry) => void;
};

export function SkillRows({ sheet, onRoll, onRollContextMenu, onOpenEntry }: SkillRowsProps) {
  return (
    <>
      {SKILLS.map(({ key, label }) => (
        <SkillRow
          key={key}
          name={label}
          abilityAbbreviation={abilityAbbreviation(sheet.skillGoverningAbilities[key])}
          proficiency={sheet.skillProficiencies[key] ?? 'NONE'}
          modifier={sheet.skills[key].value}
          rollMode={sheet.rollModes[`SKILL_CHECK:${key}`]}
          onRoll={() => onRoll('SKILL_CHECK', key)}
          onRollContextMenu={(x, y) => onRollContextMenu(x, y, 'SKILL_CHECK', key, formatSigned(sheet.skills[key].value))}
          onOpenExplainer={() => onOpenEntry({ kind: 'skill', key })}
        />
      ))}
    </>
  );
}

export function CustomSkillRows({ sheet, onRoll, onRollContextMenu, onOpenEntry }: SkillRowsProps) {
  return (
    <>
      {(sheet.customSkills ?? []).map((skill) => (
        <SkillRow
          key={skill.key}
          name={skill.name}
          abilityAbbreviation={skill.abilityKey ? abilityAbbreviation(skill.abilityKey) : '--'}
          proficiency={skill.proficiencyLevel}
          modifier={skill.value.value}
          onRoll={() => onRoll('SKILL_CHECK', skill.key)}
          onRollContextMenu={(x, y) => onRollContextMenu(x, y, 'SKILL_CHECK', skill.key, formatSigned(skill.value.value))}
          onOpenExplainer={() => onOpenEntry({ kind: 'custom', key: skill.key })}
        />
      ))}
    </>
  );
}

type AllSkillsPaneProps = SkillRowsProps & { onMutate: MutationHandler };

/** D&D Beyond's All Skills pane: the Skills list, then the Custom Skills with "+ Add Custom Skill". */
export function AllSkillsPane({ sheet, onRoll, onRollContextMenu, onOpenEntry, onMutate }: AllSkillsPaneProps) {
  return (
    <div className="all-skills-pane">
      <SidebarHeader title="All Skills" />
      <SidebarCollapsible heading="Skills" defaultOpen>
        <SkillRows sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenEntry={onOpenEntry} />
      </SidebarCollapsible>
      <SidebarCollapsible heading="Custom Skills">
        <CustomSkillRows sheet={sheet} onRoll={onRoll} onRollContextMenu={onRollContextMenu} onOpenEntry={onOpenEntry} />
        <button
          type="button"
          className="sidebar-collapsible__add"
          onClick={() => onMutate({ type: 'CUSTOMIZE', group: 'customSkills', target: 'new', value: {} })}
        >
          + Add Custom Skill
        </button>
      </SidebarCollapsible>
    </div>
  );
}

/** PREV / NEXT through the skills, then the custom skills. */
function Pager({ sheet, entry, navigation }: { sheet: CharacterSheet; entry: SkillEntry; navigation: SkillPaneNavigation }) {
  const entries = skillEntries(sheet);
  const index = entries.findIndex((candidate) => candidate.kind === entry.kind && candidate.key === entry.key);
  const previous = index > 0 ? entries[index - 1] : null;
  const next = index >= 0 && index < entries.length - 1 ? entries[index + 1] : null;
  return (
    <SidebarPager
      onPrevious={previous ? () => navigation.openEntry(previous) : null}
      onNext={next ? () => navigation.openEntry(next) : null}
    />
  );
}

function SkillHeading({ proficiency, ability, name }: { proficiency: ProficiencyLevel; ability: string; name: string }) {
  return (
    <span className="skill-pane__heading">
      <ProficiencyDot level={proficiency} className="skill-pane__prof" />
      <span className="skill-pane__ability">{ability}</span>
      {name}
    </span>
  );
}

type SkillPaneProps = { sheet: CharacterSheet; skill: string; onMutate: MutationHandler; navigation: SkillPaneNavigation };

/** D&D Beyond's pane for one skill: its value, PREV / NEXT, the Customize that overrides it, then the rules text. */
export function SkillPane({ sheet, skill, onMutate, navigation }: SkillPaneProps) {
  const label = SKILLS.find((entry) => entry.key === skill)?.label ?? skill;
  const value = customizationsOf(sheet).skills[skill] ?? EMPTY_CHECK;
  return (
    <div className="skill-pane">
      <Pager sheet={sheet} entry={{ kind: 'skill', key: skill }} navigation={navigation} />
      <SidebarHeader
        parent="Skills"
        onOpenParent={navigation.openAllSkills}
        title={
          <SkillHeading
            proficiency={sheet.skillProficiencies[skill] ?? 'NONE'}
            ability={abilityAbbreviation(sheet.skillGoverningAbilities[skill])}
            name={label}
          />
        }
        modifier={sheet.skills[skill].value}
      />
      <CheckCustomize
        label="Skill"
        withStatOverride
        value={value}
        onChange={(next) => onMutate({ type: 'CUSTOMIZE', group: 'skills', target: skill, value: next })}
      />
      <RulesText text={SKILL_RULES_TEXT[skill] ?? ''} />
    </div>
  );
}

function parseOrNull(text: string): number | null {
  if (text.trim() === '') {
    return null;
  }
  const parsed = Number(text);
  return Number.isInteger(parsed) ? parsed : null;
}

/** A field of the custom skill's Edit grid, saved when it loses focus. */
function EditField({ label, value, onCommit, wide = false }: { label: string; value: string; onCommit: (text: string) => void; wide?: boolean }) {
  const [text, setText] = useState(value);
  useEffect(() => setText(value), [value]);
  return (
    <label className={wide ? 'custom-skill-edit__field custom-skill-edit__field--wide' : 'custom-skill-edit__field'}>
      <input
        className="sidebar-value-editor__input"
        value={text}
        onChange={(event) => setText(event.target.value)}
        onBlur={() => text !== value && onCommit(text)}
      />
      <span className="custom-skill-edit__label">{label}</span>
    </label>
  );
}

type CustomSkillPaneProps = { sheet: CharacterSheet; skillKey: string; onMutate: MutationHandler; navigation: SkillPaneNavigation };

/** D&D Beyond's pane for a custom skill: PREV / NEXT, its Edit fields, and Remove. */
export function CustomSkillPane({ sheet, skillKey, onMutate, navigation }: CustomSkillPaneProps) {
  const calculated: CustomSkill | undefined = sheet.customSkills?.find((skill) => skill.key === skillKey);
  const stored: StoredCustomSkill | undefined = customizationsOf(sheet).customSkills.find((skill) => skill.key === skillKey);
  const [description, setDescription] = useState(stored?.description ?? '');
  useEffect(() => setDescription(stored?.description ?? ''), [stored?.description]);
  if (!calculated || !stored) {
    return null;
  }
  const save = (changes: Partial<StoredCustomSkill>) =>
    onMutate({ type: 'CUSTOMIZE', group: 'customSkills', target: skillKey, value: { ...stored, ...changes } });
  const numberField = (field: 'override' | 'magicBonus' | 'miscBonus', label: string) => (
    <EditField label={label} value={stored[field] === null ? '' : String(stored[field])} onCommit={(text) => save({ [field]: parseOrNull(text) })} />
  );

  return (
    <div className="skill-pane">
      <Pager sheet={sheet} entry={{ kind: 'custom', key: skillKey }} navigation={navigation} />
      <SidebarHeader
        parent="Skills"
        onOpenParent={navigation.openAllSkills}
        title={
          <SkillHeading
            proficiency={calculated.proficiencyLevel}
            ability={calculated.abilityKey ? abilityAbbreviation(calculated.abilityKey) : '--'}
            name={calculated.name}
          />
        }
        modifier={calculated.value.value}
      />
      <Customize label="Edit">
        <div className="custom-skill-edit">
          {numberField('override', 'Override')}
          {numberField('magicBonus', 'Magic Bonus')}
          {numberField('miscBonus', 'Misc Bonus')}
          <label className="custom-skill-edit__field">
            <select
              className="sidebar-value-editor__input"
              aria-label="Stat"
              value={stored.statOverride ?? ''}
              onChange={(event) => save({ statOverride: event.target.value || null })}
            >
              <option value="">--</option>
              {ABILITIES.map((ability) => (
                <option key={ability.key} value={ability.key}>
                  {ability.abbreviation}
                </option>
              ))}
            </select>
            <span className="custom-skill-edit__label">Stat</span>
          </label>
          <label className="custom-skill-edit__field">
            <select
              className="sidebar-value-editor__input"
              aria-label="Proficiency Level"
              value={stored.proficiencyLevel}
              onChange={(event) => save({ proficiencyLevel: event.target.value as ProficiencyLevel })}
            >
              <option value="NONE">Not Proficient</option>
              <option value="HALF">Half Proficient</option>
              <option value="FULL">Proficient</option>
              <option value="EXPERT">Expertise</option>
            </select>
            <span className="custom-skill-edit__label">Proficiency Level</span>
          </label>
          <EditField wide label="Name" value={stored.name} onCommit={(text) => text.trim() !== '' && save({ name: text.trim() })} />
          <EditField wide label="Notes" value={stored.notes ?? ''} onCommit={(text) => save({ notes: text || null })} />
          <label className="custom-skill-edit__field custom-skill-edit__field--wide">
            <textarea
              className="sidebar-value-editor__input custom-skill-edit__description"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              onBlur={() => description !== (stored.description ?? '') && save({ description: description || null })}
            />
            <span className="custom-skill-edit__label">Description</span>
          </label>
        </div>
      </Customize>
      <div className="skill-pane__actions">
        <button
          type="button"
          className="sidebar-remove-button"
          onClick={() => {
            onMutate({ type: 'REMOVE_CUSTOMIZATION', group: 'customSkills', target: skillKey });
            navigation.openAllSkills();
          }}
        >
          Remove
        </button>
      </div>
    </div>
  );
}

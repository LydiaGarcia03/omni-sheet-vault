import type { ProficiencyLevel } from '../../sheet/api';
import { Customize, EditorBox, ValueEditorRow } from '../../sheet/sidebarParts';
import { ABILITIES } from './abilities';
import type { CheckCustomization } from './customizations';

const PROFICIENCY_OPTIONS: [ProficiencyLevel, string][] = [
  ['NONE', 'Not Proficient'],
  ['HALF', 'Half Proficient'],
  ['FULL', 'Proficient'],
  ['EXPERT', 'Expertise'],
];

type NumberField = 'override' | 'magicBonus' | 'miscBonus';

type CheckCustomizeProps = {
  /** "Saving Throw" or "Skill": the rows read "Saving Throw Override", "Skill Magic Bonus"… */
  label: string;
  value: CheckCustomization;
  /** Skills only: the ability the check uses. */
  withStatOverride?: boolean;
  onChange: (next: CheckCustomization) => void;
};

/** D&D Beyond's Customize for a saving throw or a skill: override, magic and misc bonuses, proficiency level, stat. */
export function CheckCustomize({ label, value, withStatOverride = false, onChange }: CheckCustomizeProps) {
  const numberRow = (field: NumberField, name: string) => (
    <ValueEditorRow
      label={`${label} ${name}`}
      value={value[field]}
      notes={value[`${field}Notes`] ?? ''}
      onCommit={(next, notes) => onChange({ ...value, [field]: next, [`${field}Notes`]: notes || null })}
    />
  );

  return (
    <Customize>
      <EditorBox>
        {numberRow('override', 'Override')}
        {numberRow('magicBonus', 'Magic Bonus')}
        {numberRow('miscBonus', 'Misc Bonus')}
        <ValueEditorRow
          label={`${label} Proficiency Level`}
          value={null}
          notes={value.proficiencyLevelNotes ?? ''}
          onCommit={(_, notes) => onChange({ ...value, proficiencyLevelNotes: notes || null })}
          control={
            <select
              className="sidebar-value-editor__input sidebar-value-editor__input--value"
              aria-label={`${label} Proficiency Level`}
              value={value.proficiencyLevel ?? ''}
              onChange={(event) => onChange({ ...value, proficiencyLevel: (event.target.value || null) as ProficiencyLevel | null })}
            >
              <option value="">--</option>
              {PROFICIENCY_OPTIONS.map(([level, name]) => (
                <option key={level} value={level}>
                  {name}
                </option>
              ))}
            </select>
          }
        />
        {withStatOverride && (
          <ValueEditorRow
            label={`${label} Stat Override`}
            value={null}
            notes={value.statOverrideNotes ?? ''}
            onCommit={(_, notes) => onChange({ ...value, statOverrideNotes: notes || null })}
            control={
              <select
                className="sidebar-value-editor__input sidebar-value-editor__input--value"
                aria-label={`${label} Stat Override`}
                value={value.statOverride ?? ''}
                onChange={(event) => onChange({ ...value, statOverride: event.target.value || null })}
              >
                <option value="">--</option>
                {ABILITIES.map((ability) => (
                  <option key={ability.key} value={ability.key}>
                    {ability.label}
                  </option>
                ))}
              </select>
            }
          />
        )}
      </EditorBox>
    </Customize>
  );
}

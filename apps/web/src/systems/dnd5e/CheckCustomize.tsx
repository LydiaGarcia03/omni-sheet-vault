import type { ProficiencyLevel } from '../../sheet/api';
import { Customize, EditorBox, ValueEditorRow } from '../../sheet/sidebarParts';
import { ABILITIES } from './abilities';
import type { CheckCustomization } from './customizations';
import { useDnd5eTerms } from './locales/dnd5eMessages';

const PROFICIENCY_OPTIONS = [
  ['NONE', 'terms.proficiencyLevels.none'],
  ['HALF', 'terms.proficiencyLevels.half'],
  ['FULL', 'terms.proficiencyLevels.full'],
  ['EXPERT', 'terms.proficiencyLevels.expert'],
] as const;

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
  const terms = useDnd5eTerms();
  const { t } = terms;
  const numberRow = (field: NumberField, name: 'check.override' | 'check.magicBonus' | 'check.miscBonus') => (
    <ValueEditorRow
      label={t(name, { label })}
      value={value[field]}
      notes={value[`${field}Notes`] ?? ''}
      onCommit={(next, notes) => onChange({ ...value, [field]: next, [`${field}Notes`]: notes || null })}
    />
  );

  return (
    <Customize>
      <EditorBox>
        {numberRow('override', 'check.override')}
        {numberRow('magicBonus', 'check.magicBonus')}
        {numberRow('miscBonus', 'check.miscBonus')}
        <ValueEditorRow
          label={t('check.proficiencyLevel', { label })}
          value={null}
          notes={value.proficiencyLevelNotes ?? ''}
          onCommit={(_, notes) => onChange({ ...value, proficiencyLevelNotes: notes || null })}
          control={
            <select
              className="sidebar-value-editor__input sidebar-value-editor__input--value"
              aria-label={t('check.proficiencyLevel', { label })}
              value={value.proficiencyLevel ?? ''}
              onChange={(event) => onChange({ ...value, proficiencyLevel: (event.target.value || null) as ProficiencyLevel | null })}
            >
              <option value="">--</option>
              {PROFICIENCY_OPTIONS.map(([level, name]) => (
                <option key={level} value={level}>
                  {t(name)}
                </option>
              ))}
            </select>
          }
        />
        {withStatOverride && (
          <ValueEditorRow
            label={t('check.statOverride', { label })}
            value={null}
            notes={value.statOverrideNotes ?? ''}
            onCommit={(_, notes) => onChange({ ...value, statOverrideNotes: notes || null })}
            control={
              <select
                className="sidebar-value-editor__input sidebar-value-editor__input--value"
                aria-label={t('check.statOverride', { label })}
                value={value.statOverride ?? ''}
                onChange={(event) => onChange({ ...value, statOverride: event.target.value || null })}
              >
                <option value="">--</option>
                {ABILITIES.map((ability) => (
                  <option key={ability.key} value={ability.key}>
                    {terms.ability(ability.key)}
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

import { useState } from 'react';
import type { BackgroundCatalogueEntryData } from '../../catalogue/api';
import type { BackgroundField, CharacterSheet, TextFieldRequest } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { useDnd5eTerms } from './locales/dnd5eMessages';

const CHIPS = [
  { id: 'all', label: 'actionsTab.all' },
  { id: 'BACKGROUND', label: 'terms.labels.background' },
  { id: 'CHARACTERISTICS', label: 'backgroundTab.characteristics' },
  { id: 'APPEARANCE', label: 'backgroundTab.appearance' },
  { id: 'NOTES', label: 'backgroundTab.notes' },
] as const;

const ALIGNMENTS = [
  'Chaotic Evil', 'Chaotic Good', 'Chaotic Neutral', 'Lawful Evil', 'Lawful Good', 'Lawful Neutral', 'Neutral', 'Neutral Evil', 'Neutral Good',
];

const LIFESTYLES = ['Wretched', 'Squalid', 'Poor', 'Modest', 'Comfortable', 'Wealthy', 'Aristocratic'];

type BackgroundTabProps = {
  sheet: CharacterSheet;
  suggestions: BackgroundCatalogueEntryData | null;
  onUpdateField: (field: BackgroundField, value: string) => void;
  onOpenTextField: (request: TextFieldRequest) => void;
};

type Field = {
  label: string;
  value: string;
};

function FieldBlock({ label, value }: Field) {
  return (
    <div className="background-tab__field">
      <div className="background-tab__field-label">{label}</div>
      <p className="background-tab__field-value">{value || '—'}</p>
    </div>
  );
}

type ClickableFieldProps = Field & { onClick: () => void };

/** Same `.reveal` affordance every other clickable field name on the sheet uses. */
function ClickableFieldBlock({ label, value, onClick }: ClickableFieldProps) {
  const { t } = useDnd5eTerms();
  return (
    <button type="button" className="reveal background-tab__field" onClick={onClick} aria-label={t('backgroundTab.edit', { label })}>
      <div className="background-tab__field-label">{label}</div>
      <p className="background-tab__field-value">{value || '—'}</p>
    </button>
  );
}

/** Same treatment as the Features tab's category headings (`.actions-tab__section-heading`). */
function SectionHeading({ title }: { title: string }) {
  return (
    <div className="actions-tab__section-heading">
      <span>{title}</span>
    </div>
  );
}

/**
 * Build order step 8 (systems/dnd-5e/sheet-build.md) built this display-only; phase
 * 10 adds editing, confirmed live against D&D Beyond. Alignment, Faith and
 * Lifestyle (the first and last as selects) open the Characteristics panel;
 * the physical details open their own Appearance Details panel from the
 * Appearance section. Personality Traits/Ideals/
 * Bonds/Flaws each open individually with a roll-a-suggestion table sourced
 * from the background's own catalogue entry (`suggestions`, null if the
 * background has no seeded content — the field still opens, just without a
 * suggestions table, same treatment `SpellManagementRequest.catalogue`
 * degrading gracefully already established), and every Notes field
 * (Organizations/Allies/Enemies/Backstory/Other) plus the free-text
 * Appearance field each open individually with no suggestions. Every panel
 * is the Text Field mold (`ui-design-system.md`'s seventh) — see
 * `TextFieldRequest`'s doc comment for why one shape covers both the
 * single-field and combined-field cases. The background's own identity
 * (`name`/`featureName`/`featureDescription`) stays inert — choosing a
 * different background is a phase 11 concern.
 *
 * Filterable by subsection (`CHIPS`) same as the other tabs; four sections
 * — Background, Characteristics, Appearance, Notes — each headed with
 * `.actions-tab__section-heading`, the same treatment as the Features tab's
 * category headings. The background's own name (e.g. "Soldier") reuses
 * `.features-tab__source-heading`, matching the class/species source
 * heading nested under a Features tab category (e.g. "Fighter" under
 * "Class Features").
 */
export function BackgroundTab({ sheet, suggestions, onUpdateField, onOpenTextField }: BackgroundTabProps) {
  const { t } = useDnd5eTerms();
  const [activeSection, setActiveSection] = useState('all');
  const background = sheet.background;
  const showSection = (id: string) => activeSection === 'all' || activeSection === id;

  const openSingleField = (field: BackgroundField, label: string, value: string, helperPrompt?: string, fieldSuggestions?: string[]) =>
    onOpenTextField({
      kind: 'textField',
      title: label,
      fields: [
        {
          label,
          value,
          multiline: true,
          helperPrompt,
          onSave: (next) => onUpdateField(field, next),
          suggestions: fieldSuggestions,
        },
      ],
    });

  const openCharacteristics = () =>
    onOpenTextField({
      kind: 'textField',
      title: t('backgroundTab.characteristics'),
      fields: [
        {
          label: t('terms.labels.alignment'),
          value: background.alignment,
          options: ALIGNMENTS,
          optionLabel: (option) => t.dynamic(`terms.alignments.${option}`, option),
          onSave: (v) => onUpdateField('ALIGNMENT', v),
        },
        { label: t('backgroundTab.faith'), value: background.faith, onSave: (v) => onUpdateField('FAITH', v) },
        {
          label: t('terms.labels.lifestyle'),
          value: background.lifestyle,
          options: LIFESTYLES,
          optionLabel: (option) => t.dynamic(`terms.lifestyles.${option}`, option),
          onSave: (v) => onUpdateField('LIFESTYLE', v),
        },
      ],
    });

  const openAppearanceDetails = () =>
    onOpenTextField({
      kind: 'textField',
      title: t('backgroundTab.appearanceDetails'),
      fields: [
        { label: t('backgroundTab.hair'), value: background.hair, onSave: (v) => onUpdateField('HAIR', v) },
        { label: t('backgroundTab.skin'), value: background.skin, onSave: (v) => onUpdateField('SKIN', v) },
        { label: t('backgroundTab.eyes'), value: background.eyes, onSave: (v) => onUpdateField('EYES', v) },
        { label: t('backgroundTab.height'), value: background.height, onSave: (v) => onUpdateField('HEIGHT', v) },
        { label: t('backgroundTab.weight'), value: background.weight, onSave: (v) => onUpdateField('WEIGHT', v) },
        { label: t('backgroundTab.age'), value: background.age, onSave: (v) => onUpdateField('AGE', v) },
        { label: t('backgroundTab.gender'), value: background.gender, onSave: (v) => onUpdateField('GENDER', v) },
        { label: t('terms.labels.size'), value: background.size, onSave: (v) => onUpdateField('SIZE', v) },
      ],
    });

  return (
    <div>
      <FilterChips chips={CHIPS.map(({ id, label }) => ({ id, label: t(label) }))} activeChip={activeSection} onSelect={setActiveSection} />

      {showSection('BACKGROUND') && (
        <section>
          <SectionHeading title={t('terms.labels.background')} />
          <div className="background-tab__feature">
            <div className="features-tab__source-heading">{background.name}</div>
            {background.featureName && (
              <>
                <div className="background-tab__field-label">{background.featureName}</div>
                <p className="background-tab__field-value">{background.featureDescription || '—'}</p>
              </>
            )}
          </div>
        </section>
      )}

      {showSection('CHARACTERISTICS') && (
        <section>
          <SectionHeading title={t('backgroundTab.characteristics')} />
          <button type="button" className="reveal background-tab__grid" onClick={openCharacteristics} aria-label={t('backgroundTab.editCharacteristics')}>
            <FieldBlock label={t('terms.labels.alignment')} value={background.alignment ? t.dynamic(`terms.alignments.${background.alignment}`, background.alignment) : ''} />
            <FieldBlock label={t('backgroundTab.faith')} value={background.faith} />
          </button>

          <div className="background-tab__grid">
            <ClickableFieldBlock
              label={t('terms.labels.personalityTraits')}
              value={background.personalityTraits}
              onClick={() =>
                openSingleField('PERSONALITY_TRAITS', t('terms.labels.personalityTraits'), background.personalityTraits, undefined, suggestions?.personalityTraits)
              }
            />
            <ClickableFieldBlock
              label={t('terms.labels.ideals')}
              value={background.ideals}
              onClick={() => openSingleField('IDEALS', t('terms.labels.ideals'), background.ideals, undefined, suggestions?.ideals)}
            />
            <ClickableFieldBlock
              label={t('terms.labels.bonds')}
              value={background.bonds}
              onClick={() => openSingleField('BONDS', t('terms.labels.bonds'), background.bonds, undefined, suggestions?.bonds)}
            />
            <ClickableFieldBlock
              label={t('terms.labels.flaws')}
              value={background.flaws}
              onClick={() => openSingleField('FLAWS', t('terms.labels.flaws'), background.flaws, undefined, suggestions?.flaws)}
            />
          </div>
        </section>
      )}

      {showSection('APPEARANCE') && (
        <section>
          <SectionHeading title={t('backgroundTab.appearance')} />
          <button type="button" className="reveal background-tab__grid" onClick={openAppearanceDetails} aria-label={t('backgroundTab.editAppearance')}>
            <FieldBlock label={t('backgroundTab.gender')} value={background.gender} />
            <FieldBlock label={t('backgroundTab.eyes')} value={background.eyes} />
            <FieldBlock label={t('terms.labels.size')} value={background.size} />
            <FieldBlock label={t('backgroundTab.height')} value={background.height} />
            <FieldBlock label={t('backgroundTab.hair')} value={background.hair} />
            <FieldBlock label={t('backgroundTab.skin')} value={background.skin} />
            <FieldBlock label={t('backgroundTab.age')} value={background.age} />
            <FieldBlock label={t('backgroundTab.weight')} value={background.weight} />
          </button>
          <ClickableFieldBlock
            label={t('backgroundTab.appearance')}
            value={background.appearance}
            onClick={() =>
              openSingleField('APPEARANCE', t('backgroundTab.appearance'), background.appearance, t('backgroundTab.appearancePrompt'))
            }
          />
        </section>
      )}

      {showSection('NOTES') && (
        <section>
          <SectionHeading title={t('backgroundTab.notes')} />
          <div className="background-tab__notes">
            <ClickableFieldBlock
              label={t('backgroundTab.organizations')}
              value={background.organizations}
              onClick={() =>
                openSingleField(
                  'ORGANIZATIONS',
                  t('backgroundTab.organizations'),
                  background.organizations,
                  t('backgroundTab.organizationsPrompt'),
                )
              }
            />
            <ClickableFieldBlock
              label={t('backgroundTab.allies')}
              value={background.allies}
              onClick={() => openSingleField('ALLIES', t('backgroundTab.allies'), background.allies, t('backgroundTab.alliesPrompt'))}
            />
            <ClickableFieldBlock
              label={t('backgroundTab.enemies')}
              value={background.enemies}
              onClick={() => openSingleField('ENEMIES', t('backgroundTab.enemies'), background.enemies, t('backgroundTab.enemiesPrompt'))}
            />
            <ClickableFieldBlock
              label={t('backgroundTab.backstory')}
              value={background.backstory}
              onClick={() =>
                openSingleField(
                  'BACKSTORY',
                  t('backgroundTab.backstory'),
                  background.backstory,
                  t('backgroundTab.backstoryPrompt'),
                )
              }
            />
            <ClickableFieldBlock
              label={t('backgroundTab.other')}
              value={background.other}
              onClick={() => openSingleField('OTHER', t('backgroundTab.other'), background.other, t('backgroundTab.otherPrompt'))}
            />
          </div>
        </section>
      )}
    </div>
  );
}

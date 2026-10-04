import { useState } from 'react';
import type { BackgroundCatalogueEntryData } from '../../catalogue/api';
import type { BackgroundField, CharacterSheet, TextFieldRequest } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';

const CHIPS = [
  { id: 'all', label: 'All' },
  { id: 'BACKGROUND', label: 'Background' },
  { id: 'CHARACTERISTICS', label: 'Characteristics' },
  { id: 'APPEARANCE', label: 'Appearance' },
  { id: 'NOTES', label: 'Notes' },
];

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
  return (
    <button type="button" className="reveal background-tab__field" onClick={onClick} aria-label={`Edit ${label}`}>
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
      title: 'Characteristics',
      fields: [
        { label: 'Alignment', value: background.alignment, options: ALIGNMENTS, onSave: (v) => onUpdateField('ALIGNMENT', v) },
        { label: 'Faith', value: background.faith, onSave: (v) => onUpdateField('FAITH', v) },
        { label: 'Lifestyle', value: background.lifestyle, options: LIFESTYLES, onSave: (v) => onUpdateField('LIFESTYLE', v) },
      ],
    });

  const openAppearanceDetails = () =>
    onOpenTextField({
      kind: 'textField',
      title: 'Appearance Details',
      fields: [
        { label: 'Hair', value: background.hair, onSave: (v) => onUpdateField('HAIR', v) },
        { label: 'Skin', value: background.skin, onSave: (v) => onUpdateField('SKIN', v) },
        { label: 'Eyes', value: background.eyes, onSave: (v) => onUpdateField('EYES', v) },
        { label: 'Height', value: background.height, onSave: (v) => onUpdateField('HEIGHT', v) },
        { label: 'Weight', value: background.weight, onSave: (v) => onUpdateField('WEIGHT', v) },
        { label: 'Age', value: background.age, onSave: (v) => onUpdateField('AGE', v) },
        { label: 'Gender', value: background.gender, onSave: (v) => onUpdateField('GENDER', v) },
        { label: 'Size', value: background.size, onSave: (v) => onUpdateField('SIZE', v) },
      ],
    });

  return (
    <div>
      <FilterChips chips={CHIPS} activeChip={activeSection} onSelect={setActiveSection} />

      {showSection('BACKGROUND') && (
        <section>
          <SectionHeading title="Background" />
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
          <SectionHeading title="Characteristics" />
          <button type="button" className="reveal background-tab__grid" onClick={openCharacteristics} aria-label="Edit characteristics">
            <FieldBlock label="Alignment" value={background.alignment} />
            <FieldBlock label="Faith" value={background.faith} />
          </button>

          <div className="background-tab__grid">
            <ClickableFieldBlock
              label="Personality Traits"
              value={background.personalityTraits}
              onClick={() =>
                openSingleField('PERSONALITY_TRAITS', 'Personality Traits', background.personalityTraits, undefined, suggestions?.personalityTraits)
              }
            />
            <ClickableFieldBlock
              label="Ideals"
              value={background.ideals}
              onClick={() => openSingleField('IDEALS', 'Ideals', background.ideals, undefined, suggestions?.ideals)}
            />
            <ClickableFieldBlock
              label="Bonds"
              value={background.bonds}
              onClick={() => openSingleField('BONDS', 'Bonds', background.bonds, undefined, suggestions?.bonds)}
            />
            <ClickableFieldBlock
              label="Flaws"
              value={background.flaws}
              onClick={() => openSingleField('FLAWS', 'Flaws', background.flaws, undefined, suggestions?.flaws)}
            />
          </div>
        </section>
      )}

      {showSection('APPEARANCE') && (
        <section>
          <SectionHeading title="Appearance" />
          <button type="button" className="reveal background-tab__grid" onClick={openAppearanceDetails} aria-label="Edit appearance details">
            <FieldBlock label="Gender" value={background.gender} />
            <FieldBlock label="Eyes" value={background.eyes} />
            <FieldBlock label="Size" value={background.size} />
            <FieldBlock label="Height" value={background.height} />
            <FieldBlock label="Hair" value={background.hair} />
            <FieldBlock label="Skin" value={background.skin} />
            <FieldBlock label="Age" value={background.age} />
            <FieldBlock label="Weight" value={background.weight} />
          </button>
          <ClickableFieldBlock
            label="Appearance"
            value={background.appearance}
            onClick={() =>
              openSingleField('APPEARANCE', 'Appearance', background.appearance, 'Describe your character’s physical appearance.')
            }
          />
        </section>
      )}

      {showSection('NOTES') && (
        <section>
          <SectionHeading title="Notes" />
          <div className="background-tab__notes">
            <ClickableFieldBlock
              label="Organizations"
              value={background.organizations}
              onClick={() =>
                openSingleField(
                  'ORGANIZATIONS',
                  'Organizations',
                  background.organizations,
                  'What organizations, factions, or groups is your character affiliated with?',
                )
              }
            />
            <ClickableFieldBlock
              label="Allies"
              value={background.allies}
              onClick={() => openSingleField('ALLIES', 'Allies', background.allies, "Who are your character's allies and companions?")}
            />
            <ClickableFieldBlock
              label="Enemies"
              value={background.enemies}
              onClick={() => openSingleField('ENEMIES', 'Enemies', background.enemies, "Who are your character's enemies and rivals?")}
            />
            <ClickableFieldBlock
              label="Backstory"
              value={background.backstory}
              onClick={() =>
                openSingleField(
                  'BACKSTORY',
                  'Backstory',
                  background.backstory,
                  'Talk about your character’s origins. Where are they from? How did they end up adventuring?',
                )
              }
            />
            <ClickableFieldBlock
              label="Other"
              value={background.other}
              onClick={() => openSingleField('OTHER', 'Other', background.other, 'Anything else worth noting about your character.')}
            />
          </div>
        </section>
      )}
    </div>
  );
}

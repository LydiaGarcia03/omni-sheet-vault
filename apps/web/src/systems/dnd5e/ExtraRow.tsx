import { ListRow } from '../../sheet/ListRow';
import type { EntityDetailHandler, EntityDetailRequest, Extra, MutationHandler } from '../../sheet/api';
import { EntityCustomizeEditor, type CustomizeField } from './EntityCustomize';
import { ExtraStatBlockView } from './ExtraStatBlockView';
import { HitPoints } from './HitPoints';
import { ENGLISH_TERMS, useDnd5eTerms, type Dnd5eTerms } from './locales/dnd5eMessages';

type ExtraRowProps = {
  extra: Extra;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
};

/** D&D Beyond's singular category names, in its own order, each with a one-line hint shown when adding. */
export const EXTRA_CATEGORIES = [
  { id: 'BATTLE_SMITH_DEFENDER', creatures: true },
  { id: 'BEAST_COMPANION', creatures: true },
  { id: 'FAMILIAR', creatures: true },
  { id: 'SIDEKICK', creatures: true },
  { id: 'WILD_SHAPE', creatures: true },
  { id: 'MOUNT', creatures: true },
  { id: 'PET', creatures: true },
  { id: 'SUMMONED_CREATURE', creatures: true },
  { id: 'MISC', creatures: true },
  { id: 'VEHICLE', creatures: false },
];

/** The categories with their names and hints in the player's language. */
export function extraCategoriesOf({ t }: Dnd5eTerms): { id: string; label: string; hint: string; creatures: boolean }[] {
  return EXTRA_CATEGORIES.map((category) => ({
    ...category,
    label: t.dynamic(`terms.extraCategories.${category.id}`, category.id),
    hint: t.dynamic(`extras.hints.${category.id}`, ''),
  }));
}

export function categoryLabel(category: string, { t }: Dnd5eTerms = ENGLISH_TERMS): string {
  return t.dynamic(`terms.extraCategories.${category}`, category);
}

const MOVEMENT_WORDS: Record<string, string> = { climb: 'climbing', fly: 'flying', swim: 'swimming', burrow: 'burrowing' };

/** "Climb 30 ft., Fly 60 ft. (hover)" → "Climbing, Flying": the other ways the creature moves, under its speed. */
export function movementCallout(additionalSpeeds: string | null, { t }: Dnd5eTerms = ENGLISH_TERMS): string {
  if (!additionalSpeeds) {
    return '';
  }
  return additionalSpeeds
    .split(',')
    .map((part) => MOVEMENT_WORDS[part.trim().split(' ')[0].toLowerCase()])
    .filter(Boolean)
    .map((movement) => t.dynamic(`terms.movements.${movement}`, movement))
    .join(', ');
}

/** "Tiny Beast": size before type; a type already in parentheses ("(Fey, Fiend, or Celestial)") reads as written. */
export function sizeAndType(extra: Extra): string {
  return `${extra.statBlock.size} ${extra.statBlock.creatureType}`.trim();
}

function extraHitPoints(extra: Extra, onMutate: MutationHandler, terms: Dnd5eTerms) {
  return (
    <details className="extra-row__hp-details">
      <summary>
        <span>{terms.t('terms.labels.hitPoints')}</span>
        <span className="extra-row__hp-summary">
          {extra.currentHitPoints}/{extra.maxHitPoints}
          <svg className="extra-row__hp-chevron" viewBox="0 0 10 10" width="10" height="10" aria-hidden="true">
            <path d="M1 3l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.6" />
          </svg>
        </span>
      </summary>
      <HitPoints
        current={extra.currentHitPoints}
        max={extra.maxHitPoints}
        temporary={extra.temporaryHitPoints}
        onDamage={(amount) => onMutate({ type: 'EXTRA_DAMAGE', extraKey: extra.key, amount })}
        onHeal={(amount) => onMutate({ type: 'EXTRA_HEAL', extraKey: extra.key, amount })}
        onSetTemporary={(amount) => onMutate({ type: 'EXTRA_TEMPORARY_HIT_POINTS', extraKey: extra.key, amount })}
        framed={false}
      />
    </details>
  );
}

/** Under Customize, as on Beyond: the collapsed Hit Points, then the stat block. */
function extraBody(extra: Extra, onMutate: MutationHandler, terms: Dnd5eTerms) {
  return (
    <>
      {extraHitPoints(extra, onMutate, terms)}
      <div className="extra-detail__stat-block">
        <ExtraStatBlockView name={extra.name} statBlock={extra.statBlock} armorClass={extra.armorClass} maxHitPoints={extra.maxHitPoints} speed={extra.speed} />
      </div>
    </>
  );
}

function customizeFields({ t }: Dnd5eTerms): CustomizeField[] {
  return [
    { key: 'name', label: t('item.name'), kind: 'text', name: true },
    { key: 'notes', label: t('item.notes'), kind: 'text' },
  ];
}

function extraCustomize(extra: Extra, onMutate: MutationHandler, terms: Dnd5eTerms) {
  const save = (name: string, notes: string) => onMutate({ type: 'UPDATE_EXTRA', extraKey: extra.key, name, notes });
  return {
    customized: Boolean(extra.notes),
    editor: (
      <EntityCustomizeEditor
        fields={customizeFields(terms)}
        values={{ name: extra.name, notes: extra.notes ?? '' }}
        customized={Boolean(extra.notes)}
        onSave={(values) => save(String(values.name ?? extra.name), String(values.notes ?? ''))}
        onReset={() => save(extra.name, '')}
      />
    ),
  };
}

/**
 * The extra's Entity Detail in D&D Beyond's order, also opened from Manage Extras: the category over the name (the
 * pencil opens Customize: name and notes), the book it came from, Customize, the collapsed Hit Points, the stat block.
 * Delete lives in Manage Extras, as on Beyond. Rebuilt from the live sheet so hit points, a rename or notes show at once.
 */
export function extraDetailRequest(extra: Extra, onMutate: MutationHandler, terms: Dnd5eTerms = ENGLISH_TERMS): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `extra:${extra.key}`,
    name: extra.name,
    parent: categoryLabel(extra.category, terms),
    subtitle: extra.source ?? undefined,
    metadata: [],
    description: '',
    body: extraBody(extra, onMutate, terms),
    customize: extraCustomize(extra, onMutate, terms),
    refresh: (sheet) => {
      const latest = sheet.extras.find((candidate) => candidate.key === extra.key);
      return latest ? extraDetailRequest(latest, onMutate, terms) : null;
    },
  };
}

export function ExtraRow({ extra, onMutate, onOpenDetail }: ExtraRowProps) {
  const terms = useDnd5eTerms();
  const callout = movementCallout(extra.statBlock.additionalSpeeds, terms);
  return (
    <ListRow className="extra-row reveal" ariaLabel={terms.t('actionsTab.openDetails', { name: extra.name })} onOpen={() => onOpenDetail(extraDetailRequest(extra, onMutate, terms))}>
      <div className="extra-row__name-cell">
        <span className="reveal extra-row__name">{extra.name}</span>
        <span className="extra-row__subtitle">{sizeAndType(extra)}</span>
      </div>
      <div className="extra-row__ac">{extra.armorClass}</div>
      <div className="extra-row__hp">
        <span className="extra-row__hp-value">{extra.currentHitPoints}</span>
        <span className="extra-row__hp-sep">/</span>
        <span className="extra-row__hp-value extra-row__hp-value--max">{extra.maxHitPoints}</span>
      </div>
      <div className="extra-row__speed">
        <span className="range-display__primary">{terms.distanceValue(extra.speed)}</span>
        <span className="range-display__suffix">{terms.distanceUnit}</span>
        {callout && <span className="extra-row__speed-callout">{callout}</span>}
      </div>
      <div className="extra-row__notes">{extra.notes}</div>
    </ListRow>
  );
}

import { ListRow } from '../../sheet/ListRow';
import type { EntityDetailHandler, EntityDetailRequest, Extra, MutationHandler } from '../../sheet/api';
import { EntityCustomizeEditor, type CustomizeField } from './EntityCustomize';
import { ExtraStatBlockView } from './ExtraStatBlockView';
import { HitPoints } from './HitPoints';

type ExtraRowProps = {
  extra: Extra;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
};

/** D&D Beyond's singular category names, in its own order, each with a one-line hint shown when adding. */
export const EXTRA_CATEGORIES = [
  { id: 'BATTLE_SMITH_DEFENDER', label: 'Battle Smith Defender', hint: 'The steel defender a Battle Smith artificer builds.', creatures: true },
  { id: 'BEAST_COMPANION', label: 'Beast Companion', hint: "A beast that fights alongside a Beast Master ranger.", creatures: true },
  { id: 'FAMILIAR', label: 'Familiar', hint: 'A spirit that serves you in an animal form, through Find Familiar or a similar feature.', creatures: true },
  { id: 'SIDEKICK', label: 'Sidekick', hint: 'A companion creature that travels and fights with the party.', creatures: true },
  { id: 'WILD_SHAPE', label: 'Wild Shape', hint: 'A beast form a druid can take.', creatures: true },
  { id: 'MOUNT', label: 'Mount', hint: 'A creature you ride, such as a warhorse or a Find Steed spirit.', creatures: true },
  { id: 'PET', label: 'Pet', hint: 'An animal kept for company rather than combat.', creatures: true },
  { id: 'SUMMONED_CREATURE', label: 'Summoned', hint: 'A creature called by a spell or feature for a while.', creatures: true },
  { id: 'MISC', label: 'Misc', hint: 'Any other creature linked to the character.', creatures: true },
  { id: 'VEHICLE', label: 'Vehicle', hint: 'A ship, cart or other vehicle. None are in the catalogue yet.', creatures: false },
];

export function categoryLabel(category: string): string {
  return EXTRA_CATEGORIES.find((entry) => entry.id === category)?.label ?? category;
}

const MOVEMENT_WORDS: Record<string, string> = { climb: 'Climbing', fly: 'Flying', swim: 'Swimming', burrow: 'Burrowing' };

/** "Climb 30 ft., Fly 60 ft. (hover)" → "Climbing, Flying": the other ways the creature moves, under its speed. */
export function movementCallout(additionalSpeeds: string | null): string {
  if (!additionalSpeeds) {
    return '';
  }
  return additionalSpeeds
    .split(',')
    .map((part) => MOVEMENT_WORDS[part.trim().split(' ')[0].toLowerCase()])
    .filter(Boolean)
    .join(', ');
}

/** "Tiny Beast": size before type; a type already in parentheses ("(Fey, Fiend, or Celestial)") reads as written. */
export function sizeAndType(extra: Extra): string {
  return `${extra.statBlock.size} ${extra.statBlock.creatureType}`.trim();
}

function extraHitPoints(extra: Extra, onMutate: MutationHandler) {
  return (
    <details className="extra-row__hp-details">
      <summary>
        <span>Hit Points</span>
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
function extraBody(extra: Extra, onMutate: MutationHandler) {
  return (
    <>
      {extraHitPoints(extra, onMutate)}
      <div className="extra-detail__stat-block">
        <ExtraStatBlockView name={extra.name} statBlock={extra.statBlock} armorClass={extra.armorClass} maxHitPoints={extra.maxHitPoints} speed={extra.speed} />
      </div>
    </>
  );
}

const CUSTOMIZE_FIELDS: CustomizeField[] = [
  { key: 'name', label: 'Name', kind: 'text', name: true },
  { key: 'notes', label: 'Notes', kind: 'text' },
];

function extraCustomize(extra: Extra, onMutate: MutationHandler) {
  const save = (name: string, notes: string) => onMutate({ type: 'UPDATE_EXTRA', extraKey: extra.key, name, notes });
  return {
    customized: Boolean(extra.notes),
    editor: (
      <EntityCustomizeEditor
        fields={CUSTOMIZE_FIELDS}
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
export function extraDetailRequest(extra: Extra, onMutate: MutationHandler): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `extra:${extra.key}`,
    name: extra.name,
    parent: categoryLabel(extra.category),
    subtitle: extra.source ?? undefined,
    metadata: [],
    description: '',
    body: extraBody(extra, onMutate),
    customize: extraCustomize(extra, onMutate),
    refresh: (sheet) => {
      const latest = sheet.extras.find((candidate) => candidate.key === extra.key);
      return latest ? extraDetailRequest(latest, onMutate) : null;
    },
  };
}

export function ExtraRow({ extra, onMutate, onOpenDetail }: ExtraRowProps) {
  const callout = movementCallout(extra.statBlock.additionalSpeeds);
  return (
    <ListRow className="extra-row reveal" ariaLabel={`Open ${extra.name} details`} onOpen={() => onOpenDetail(extraDetailRequest(extra, onMutate))}>
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
        <span className="range-display__primary">{extra.speed}</span>
        <span className="range-display__suffix">ft.</span>
        {callout && <span className="extra-row__speed-callout">{callout}</span>}
      </div>
      <div className="extra-row__notes">{extra.notes}</div>
    </ListRow>
  );
}

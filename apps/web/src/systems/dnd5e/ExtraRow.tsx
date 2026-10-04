import { ListRow } from '../../sheet/ListRow';
import type { EntityDetailHandler, Extra, MutationHandler } from '../../sheet/api';
import { ExtraStatBlockView } from './ExtraStatBlockView';
import { HitPoints } from './HitPoints';

type ExtraRowProps = {
  extra: Extra;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
};

function categoryLabel(category: string): string {
  switch (category) {
    case 'FAMILIAR':
      return 'Familiar';
    case 'MOUNT':
      return 'Mount';
    case 'SUMMONED_CREATURE':
      return 'Summoned Creature';
    case 'VEHICLE':
      return 'Vehicle';
    default:
      return category;
  }
}

function extraActionBar(extra: Extra, onMutate: MutationHandler) {
  return (
    <details className="extra-row__hp-details">
      <summary>
        Hit Points {extra.currentHitPoints}/{extra.maxHitPoints}
      </summary>
      <HitPoints
        current={extra.currentHitPoints}
        max={extra.maxHitPoints}
        temporary={extra.temporaryHitPoints}
        onDamage={(amount) => onMutate({ type: 'EXTRA_DAMAGE', extraKey: extra.key, amount })}
        onHeal={(amount) => onMutate({ type: 'EXTRA_HEAL', extraKey: extra.key, amount })}
        onSetTemporary={(amount) => onMutate({ type: 'EXTRA_TEMPORARY_HIT_POINTS', extraKey: extra.key, amount })}
      />
    </details>
  );
}

function extraBody(extra: Extra, onMutate: MutationHandler) {
  return (
    <>
      <div className="extra-detail__stat-block">
        <ExtraStatBlockView name={extra.name} statBlock={extra.statBlock} armorClass={extra.armorClass} maxHitPoints={extra.maxHitPoints} speed={extra.speed} />
      </div>
      <div className="entity-detail__actions item-action-bar">
        <button type="button" className="mutate sidebar-action-button" onClick={() => onMutate({ type: 'REMOVE_EXTRA', extraKey: extra.key })}>
          Delete
        </button>
      </div>
    </>
  );
}

/**
 * No frame asset — same pattern as ItemRow. The whole row opens Entity Detail in D&D Beyond's extra order: the
 * category as the parent line, the collapsed Hit Points control under the header (`refreshActionBar` keeps it live),
 * the stat block (`ExtraStatBlockView`), then Delete.
 */
export function ExtraRow({ extra, onMutate, onOpenDetail }: ExtraRowProps) {
  const openDetail = () =>
    onOpenDetail({
      kind: 'entityDetail',
      name: extra.name,
      parent: categoryLabel(extra.category),
      metadata: [],
      description: '',
      body: extraBody(extra, onMutate),
      actionBar: extraActionBar(extra, onMutate),
      actionsPosition: 'section',
      refreshActionBar: (sheet) => {
        const latest = sheet.extras.find((candidate) => candidate.key === extra.key);
        return latest ? extraActionBar(latest, onMutate) : undefined;
      },
    });

  return (
    <ListRow className="extra-row reveal" ariaLabel={`Open ${extra.name} details`} onOpen={openDetail}>
      <div className="extra-row__name-cell">
        <span className="reveal extra-row__name">{extra.name}</span>
        <span className="extra-row__subtitle">
          {extra.statBlock.size} {extra.statBlock.creatureType}
        </span>
      </div>
      <div className="extra-row__stat">{extra.armorClass}</div>
      <div className="extra-row__stat">
        {extra.currentHitPoints}/{extra.maxHitPoints}
      </div>
      <div className="extra-row__stat">{extra.speed} ft.</div>
    </ListRow>
  );
}

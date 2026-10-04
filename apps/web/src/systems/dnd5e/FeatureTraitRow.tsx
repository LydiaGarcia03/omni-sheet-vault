import { BoxTrack } from '../../sheet/BoxTrack';
import { ListRow } from '../../sheet/ListRow';
import type { FeatureTrait, MutationHandler } from '../../sheet/api';
import { UsePoolStepper } from '../../sheet/UsePoolStepper';
import { LARGE_POOL_THRESHOLD, rechargeTriggerLabel } from './FeatureActionRow';
import { FrameIcon } from './FrameIcon';
import minusIconSvg from './frames/dnd_icon_minus.svg?raw';
import plusIconSvg from './frames/dnd_icon_plus.svg?raw';

type FeatureTraitRowProps = {
  feature: FeatureTrait;
  onMutate: MutationHandler;
  onOpenDetail: () => void;
};

/**
 * No frame asset — same reasoning as AttackRow/FeatureActionRow. The whole row
 * opens the Entity Detail mold (`ListRow`), which shows the full `description`;
 * the row itself shows only `summary` — confirmed live against D&D Beyond's own
 * Features & Traits tab (phase 10, slice 7) that the two are genuinely different
 * lengths, not the same text twice. The box track spends/restores a use through
 * `onMutate` (phase 9) — independently from `FeatureActionRow`'s own track, per
 * `Dnd5eFeatureTrait`'s doc comment: the two lists never affect each other.
 */
export function FeatureTraitRow({ feature, onMutate, onOpenDetail }: FeatureTraitRowProps) {
  return (
    <ListRow className="action-feature reveal" ariaLabel={`Open ${feature.name} details`} onOpen={onOpenDetail}>
      <div className="action-feature__body">
        <h3 className="reveal actions-tab__heading">{feature.name}</h3>
        <p className="action-feature__description">{feature.summary}</p>
      </div>
      {feature.choices != null && feature.choices.length > 0 && (
        <div className="feature-snippet__extra feature-snippet__choices">
          {feature.choices.map((choice) => (
            <div key={choice} className="feature-snippet__choice">
              {choice}
            </div>
          ))}
        </div>
      )}
      {feature.maxUses != null && (
        <div className="feature-snippet__extra">
          {feature.maxUses >= LARGE_POOL_THRESHOLD ? (
            <UsePoolStepper
              maxUses={feature.maxUses}
              usedCount={feature.usedCount}
              rechargeLabel={rechargeTriggerLabel(feature.rechargeTrigger)}
              onUse={() => onMutate({ type: 'USE_FEATURE_TRAIT', featureKey: feature.key })}
              onRestore={() => onMutate({ type: 'RESTORE_FEATURE_TRAIT', featureKey: feature.key })}
              useIcon={<FrameIcon svg={minusIconSvg} className="stepper-icon" aria-hidden />}
              restoreIcon={<FrameIcon svg={plusIconSvg} className="stepper-icon" aria-hidden />}
            />
          ) : (
            <BoxTrack
              maxUses={feature.maxUses}
              usedCount={feature.usedCount}
              rechargeLabel={rechargeTriggerLabel(feature.rechargeTrigger)}
              onUse={() => onMutate({ type: 'USE_FEATURE_TRAIT', featureKey: feature.key })}
              onRestore={() => onMutate({ type: 'RESTORE_FEATURE_TRAIT', featureKey: feature.key })}
            />
          )}
        </div>
      )}
    </ListRow>
  );
}

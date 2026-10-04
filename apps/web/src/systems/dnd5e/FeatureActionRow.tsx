import { BoxTrack } from '../../sheet/BoxTrack';
import { ListRow } from '../../sheet/ListRow';
import type { FeatureAction, MutationHandler } from '../../sheet/api';
import { UsePoolStepper } from '../../sheet/UsePoolStepper';
import { FrameIcon } from './FrameIcon';
import minusIconSvg from './frames/dnd_icon_minus.svg?raw';
import plusIconSvg from './frames/dnd_icon_plus.svg?raw';

type FeatureActionRowProps = {
  feature: FeatureAction;
  onMutate: MutationHandler;
  onOpenDetail: () => void;
};

// D&D Beyond's own inline-row threshold (FeatureSnippetLimitedUse.tsx's largeLimitedUseAmount) for BoxTrack vs UsePoolStepper.
export const LARGE_POOL_THRESHOLD = 8;

export function rechargeTriggerLabel(trigger: string | null): string | null {
  switch (trigger) {
    case 'SHORT_OR_LONG_REST':
      return 'Short or long rest';
    case 'LONG_REST':
      return 'Long rest';
    default:
      return null;
  }
}

/**
 * No frame asset — same reasoning as AttackRow. The whole row opens the
 * Entity Detail mold (`ListRow`). The box track spends/restores a use through
 * `onMutate` (phase 9) — clicking an empty box spends one, clicking a filled
 * box restores one; `BoxTrack`/`UsePoolStepper` stop propagation on their own
 * buttons so a use click doesn't also open the detail panel.
 * **Restructured 2026-09-03**, DOM-measured against D&D Beyond's own named
 * features (Second Wind, Action Surge): name, description and the box track
 * stack vertically — a block per feature, not one flex row with the track
 * squeezed in beside a truncated description. The track sitting below the
 * description, not beside the name, is what the owner meant by "o quadrado
 * é maior e embaixo da ação."
 */
export function FeatureActionRow({ feature, onMutate, onOpenDetail }: FeatureActionRowProps) {
  return (
    <ListRow className="action-feature reveal" ariaLabel={`Open ${feature.name} details`} onOpen={onOpenDetail}>
      <div className="action-feature__body">
        <h3 className="reveal actions-tab__heading">{feature.name}</h3>
        <p className="action-feature__description">{feature.description}</p>
      </div>
      {feature.maxUses != null && (
        <div className="feature-snippet__extra">
          {feature.maxUses >= LARGE_POOL_THRESHOLD ? (
            <UsePoolStepper
              maxUses={feature.maxUses}
              usedCount={feature.usedCount}
              rechargeLabel={rechargeTriggerLabel(feature.rechargeTrigger)}
              onUse={() => onMutate({ type: 'USE_FEATURE_ACTION', featureKey: feature.key })}
              onRestore={() => onMutate({ type: 'RESTORE_FEATURE_ACTION', featureKey: feature.key })}
              useIcon={<FrameIcon svg={minusIconSvg} className="stepper-icon" aria-hidden />}
              restoreIcon={<FrameIcon svg={plusIconSvg} className="stepper-icon" aria-hidden />}
            />
          ) : (
            <BoxTrack
              maxUses={feature.maxUses}
              usedCount={feature.usedCount}
              rechargeLabel={rechargeTriggerLabel(feature.rechargeTrigger)}
              onUse={() => onMutate({ type: 'USE_FEATURE_ACTION', featureKey: feature.key })}
              onRestore={() => onMutate({ type: 'RESTORE_FEATURE_ACTION', featureKey: feature.key })}
            />
          )}
        </div>
      )}
    </ListRow>
  );
}

import type {
  CharacterSheet,
  EntityDetailHandler,
  EntityDetailRequest,
  FeatureAction,
  FeatureTrait,
  MutationHandler,
} from '../../sheet/api';
import { BoxTrack } from '../../sheet/BoxTrack';
import { rechargeTriggerLabel } from './FeatureActionRow';

/** "1 Action" / "1 Bonus Action" / "1 Reaction" — D&D Beyond's activation wording. */
export function actionTypeLabel(actionType: string): string {
  const words = actionType
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
  return `1 ${words}`;
}

type LimitedUseProps = {
  label: string;
  maxUses: number;
  usedCount: number;
  rechargeLabel?: string | null;
  onUse: () => void;
  onRestore: () => void;
};

/** A bold label followed by the use boxes (D&D Beyond's ct-feature-snippet__limited-use). */
function LimitedUse({ label, maxUses, usedCount, rechargeLabel, onUse, onRestore }: LimitedUseProps) {
  return (
    <div className="feature-detail__limited-use">
      <span className="feature-detail__limited-use-label">{label}</span>
      <BoxTrack maxUses={maxUses} usedCount={usedCount} rechargeLabel={rechargeLabel} onUse={onUse} onRestore={onRestore} />
    </div>
  );
}

function actionLimitedUse(feature: FeatureAction, onMutate: MutationHandler) {
  if (feature.maxUses == null) {
    return undefined;
  }
  return (
    <LimitedUse
      label="Limited Use"
      maxUses={feature.maxUses}
      usedCount={feature.usedCount}
      onUse={() => onMutate({ type: 'USE_FEATURE_ACTION', featureKey: feature.key })}
      onRestore={() => onMutate({ type: 'RESTORE_FEATURE_ACTION', featureKey: feature.key })}
    />
  );
}

/** The detail pane for an action granted by a feature: parent link, Limited Use, Action Type, description. */
export function featureActionDetailRequest(
  feature: FeatureAction,
  sheet: CharacterSheet,
  onMutate: MutationHandler,
  onOpenDetail: EntityDetailHandler,
): EntityDetailRequest {
  const parentTrait = sheet.featureTraits.find((trait) => trait.key === feature.parentKey);
  return {
    kind: 'entityDetail',
    entityKey: `feature-action:${feature.key}`,
    name: feature.name,
    parent: feature.parentName ?? undefined,
    onOpenParent: parentTrait ? () => onOpenDetail(featureTraitDetailRequest(parentTrait, sheet, onMutate, onOpenDetail)) : undefined,
    metadata: [{ label: 'Action Type', value: actionTypeLabel(feature.actionType) }],
    description: feature.description,
    actionBar: actionLimitedUse(feature, onMutate),
    actionsPosition: 'section',
    refreshActionBar: (latestSheet) => {
      const latest = latestSheet.featureActions.find((candidate) => candidate.key === feature.key);
      return latest ? actionLimitedUse(latest, onMutate) : undefined;
    },
  };
}

function traitBody(trait: FeatureTrait, sheet: CharacterSheet, onMutate: MutationHandler) {
  const actions = sheet.featureActions.filter((action) => action.parentKey === trait.key);
  return (
    <div className="feature-detail__body">
      {trait.description
        .split(/\n+/)
        .filter((paragraph) => paragraph.trim() !== '')
        .map((paragraph, index) => (
          <p key={index}>{paragraph}</p>
        ))}
      {actions.map((action) => (
        <div key={action.key} className="feature-detail__extra">
          <p className="feature-detail__extra-line">
            <span className="feature-detail__extra-name">{action.name}:</span> {actionTypeLabel(action.actionType)}
          </p>
          {action.maxUses != null && (
            <LimitedUse
              label="Uses"
              maxUses={action.maxUses}
              usedCount={action.usedCount}
              rechargeLabel={rechargeTriggerLabel(action.rechargeTrigger)}
              onUse={() => onMutate({ type: 'USE_FEATURE_ACTION', featureKey: action.key })}
              onRestore={() => onMutate({ type: 'RESTORE_FEATURE_ACTION', featureKey: action.key })}
            />
          )}
        </div>
      ))}
      {trait.maxUses != null && !actions.some((action) => action.maxUses != null) && (
        <div className="feature-detail__extra">
          <LimitedUse
            label="Uses"
            maxUses={trait.maxUses}
            usedCount={trait.usedCount}
            rechargeLabel={rechargeTriggerLabel(trait.rechargeTrigger)}
            onUse={() => onMutate({ type: 'USE_FEATURE_TRAIT', featureKey: trait.key })}
            onRestore={() => onMutate({ type: 'RESTORE_FEATURE_TRAIT', featureKey: trait.key })}
          />
        </div>
      )}
    </div>
  );
}

/** The detail pane for a class feature, species trait or feat: source line, text, then its actions and uses. */
export function featureTraitDetailRequest(
  trait: FeatureTrait,
  sheet: CharacterSheet,
  onMutate: MutationHandler,
  onOpenDetail: EntityDetailHandler,
): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `feature-trait:${trait.key}`,
    name: trait.name,
    parent: trait.source,
    metadata: [],
    description: trait.description,
    body: traitBody(trait, sheet, onMutate),
    refresh: (latestSheet) => {
      const latest = latestSheet.featureTraits.find((candidate) => candidate.key === trait.key);
      return latest ? featureTraitDetailRequest(latest, latestSheet, onMutate, onOpenDetail) : null;
    },
  };
}

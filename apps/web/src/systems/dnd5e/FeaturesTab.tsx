import { useState } from 'react';
import type { CharacterSheet, EntityDetailHandler, FeatureTrait, MutationHandler } from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { featureTraitDetailRequest } from './featureDetail';
import { FeatureTraitRow } from './FeatureTraitRow';

const CHIPS = [
  { id: 'all', label: 'All' },
  { id: 'CLASS_FEATURE', label: 'Class Features' },
  { id: 'SPECIES_TRAIT', label: 'Species Traits' },
  { id: 'FEAT', label: 'Feats' },
];

type CategoryHeadingProps = {
  title: string;
  onManageFeats?: () => void;
};

function CategoryHeading({ title, onManageFeats }: CategoryHeadingProps) {
  return (
    <>
      <div className="actions-tab__section-heading">
        <span>{title}</span>
      </div>
      {onManageFeats && (
        <div className="features-tab__manage-feats-row">
          <button type="button" className="features-tab__manage-feats-button" onClick={onManageFeats}>
            Manage Feats
          </button>
        </div>
      )}
    </>
  );
}

type FeaturesTabProps = {
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  onOpenManageFeats: () => void;
};

/**
 * Build order step 8 (systems/dnd-5e/sheet-build.md), scoped consistently with the
 * Spells tab: filterable and grouped by source ("grouped by class and by
 * species" per systems/dnd-5e/sheet-ui.md). Limited-use tracks spend/restore for real
 * as of phase 9 (`FeatureTraitRow.tsx`,
 * `Dnd5eSheetMutator.useFeatureTraitUse`/`restoreFeatureTraitUse`) —
 * independently from the Actions tab's own tracks, per `Dnd5eFeatureTrait`'s
 * doc comment. "Manage Feats" opens `FeatManagementPanel.tsx`, a catalogue
 * picker over the real 5etools feat list — only shown next to the "Feats"
 * category heading, since class features/species traits aren't a player
 * choice the way a feat is. Distinct from `featureActions` (the Actions
 * tab's own list): this is the
 * full catalog, including passive features and species traits/feats that
 * grant no action.
 *
 * Grouped two levels deep: the category (`.actions-tab__section-heading`,
 * reusing the Actions tab's own top-level section treatment, labelled from
 * `CHIPS`) containing each of that category's own sources
 * (`.features-tab__source-heading`, frames.css) — a class/species/feat name
 * alone, with no category label above it, was otherwise indistinguishable
 * from its own category.
 */
export function FeaturesTab({ sheet, onMutate, onOpenDetail, onOpenManageFeats }: FeaturesTabProps) {
  const [activeCategory, setActiveCategory] = useState('all');

  const visible = sheet.featureTraits.filter(
    (feature) => activeCategory === 'all' || feature.category === activeCategory,
  );
  // CHIPS' own order/labels ("Class Features"/"Species Traits"/"Feats") double as the
  // category heading above each source's own group — only categories the current
  // filter actually left something in get a heading, so selecting a single chip
  // still shows just that one category's own name rather than three empty headings.
  // Feats always shows under All/Feats: it holds the only "Manage Feats" button.
  const categories = CHIPS.filter(
    (chip) =>
      chip.id !== 'all' &&
      (visible.some((feature) => feature.category === chip.id) ||
        (chip.id === 'FEAT' && (activeCategory === 'all' || activeCategory === 'FEAT'))),
  );

  const renderFeature = (feature: FeatureTrait) => (
    <FeatureTraitRow
      key={feature.key}
      feature={feature}
      onMutate={onMutate}
      onOpenDetail={() => onOpenDetail(featureTraitDetailRequest(feature, sheet, onMutate, onOpenDetail))}
    />
  );

  return (
    <div>
      <FilterChips chips={CHIPS} activeChip={activeCategory} onSelect={setActiveCategory} />
      {categories.map(({ id: category, label: categoryTitle }) => {
        const inCategory = visible.filter((feature) => feature.category === category);
        const sources = Array.from(new Set(inCategory.map((feature) => feature.source)));
        return (
          <section key={category}>
            <CategoryHeading title={categoryTitle} onManageFeats={category === 'FEAT' ? onOpenManageFeats : undefined} />
            {category === 'FEAT' ? (
              <div className="action-list">{inCategory.map(renderFeature)}</div>
            ) : (
              sources.map((source) => (
                <section key={source}>
                  <h3 className="features-tab__source-heading">{source}</h3>
                  <div className="action-list">{inCategory.filter((feature) => feature.source === source).map(renderFeature)}</div>
                </section>
              ))
            )}
          </section>
        );
      })}
    </div>
  );
}

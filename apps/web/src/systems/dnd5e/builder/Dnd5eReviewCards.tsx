import type { ReactNode } from 'react';
import { Link } from 'react-router';
import { selectedLabels } from '../../../builder/builderDefinition';
import type { BuildPlan, CreationChoice, DraftPreview } from '../../../builder/draftApi';
import { useDnd5eTerms, type Dnd5eText } from '../locales/dnd5eMessages';
import { dnd5eStepOf, preferencesOf, type Dnd5eBuild } from './dnd5eBuild';

type Dnd5eReviewCardsProps = {
  characterId: string;
  name: string;
  build: Dnd5eBuild;
  plan: BuildPlan;
  preview: DraftPreview;
};

/** How the hit points were set: fixed, rolled, or still averaged over the levels not rolled yet. */
function hitPointNote(build: Dnd5eBuild, rolled: CreationChoice | undefined, t: Dnd5eText): string {
  if (build.hitPointMethod !== 'ROLLED') {
    return t('builder.review.hitPointNoteFixed');
  }
  const left = rolled ? rolled.count - rolled.selected.length : 0;
  return left > 0 ? t('builder.review.hitPointNoteAverage', { left, total: rolled?.count ?? 0 }) : t('builder.review.hitPointNoteRolled');
}

/** One card per step with what the draft holds there, a badge for what's left, and a link back to edit it. */
export function Dnd5eReviewCards({ characterId, name, build, plan, preview }: Dnd5eReviewCardsProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const choice = (id: string) => plan.choices.find((candidate) => candidate.id === id);
  const pendingIn = (step: string) => plan.choices.filter((candidate) => candidate.pending && dnd5eStepOf(candidate) === step).length;
  const preferences = preferencesOf(build);
  const species = preview.selections?.species;
  const background = preview.selections?.background;
  const grant = (label: string) => background?.grants.find((entry) => entry.label === label)?.text ?? null;
  const classLabel = (slug: string) => choice('build.classes')?.options.find((option) => option.key === slug)?.label ?? slug;
  const method = choice('build.abilityScores');
  const equipment = preview.startingEquipment;
  const equipped = (equipment?.inventory ?? []).filter((item) => item.equipped).map((item) => item.name);
  const coins = equipment
    ? [
        [equipment.gold, t('terms.coins.gold').toLowerCase()],
        [equipment.silver, t('terms.coins.silver').toLowerCase()],
        [equipment.copper, t('terms.coins.copper').toLowerCase()],
      ]
        .filter(([amount]) => Number(amount) > 0)
        .map(([amount, unit]) => `${amount} ${unit}`)
        .join(' · ')
    : '';

  const card = (step: string, title: string, rows: [string, ReactNode][]) => (
    <section key={step} className="builder-frame builder-review-card">
      <div className="builder-frame__title">
        <span className="builder-grow">{title}</span>
        {pendingIn(step) > 0 && <span className="builder-badge builder-badge--pending">{t('builder.left', { count: pendingIn(step) })}</span>}
        <Link className="builder-btn" to={`/characters/${characterId}/build/${step}`}>
          {t('builder.review.edit')}
        </Link>
      </div>
      <dl className="builder-kv">
        {rows.map(([label, value]) => (
          <div key={label} className="builder-kv__row">
            <dt>{label}</dt>
            <dd>{value ?? <span className="builder-muted">—</span>}</dd>
          </div>
        ))}
      </dl>
    </section>
  );

  return (
    <div className="builder-review-grid">
      {card('basics', t('terms.builderSteps.basics'), [
        [t('builder.review.name'), name || null],
        [t('builder.review.sources'), preferences.sources === null ? t('builder.review.everyBook') : t('builder.review.coreAndBooks', { count: preferences.sources.length })],
        [
          t('builder.review.rules'),
          [
            preferences.advancement === 'XP' ? t('terms.builder.experiencePoints') : t('terms.builder.milestone'),
            build.hitPointMethod === 'ROLLED' ? t('builder.review.rolledHp') : t('builder.review.fixedHp'),
            preferences.encumbrance === 'STANDARD' ? t('builder.review.standardEncumbrance') : t('builder.review.noEncumbrance'),
          ].join(' · '),
        ],
      ])}
      {card('species', t('terms.builderSteps.species'), [
        [t('terms.builderSteps.species'), species?.name ?? null],
        [t('builder.review.bonuses'), species?.facts.find((fact) => fact.label === 'Ability bonuses')?.text ?? null],
      ])}
      {card('classes', t('terms.builderSteps.classes'), [
        ...build.classes.map((entry): [string, ReactNode] => {
          const subclass = choice(`build.class.${entry.classSlug}.subclass`);
          return [`${classLabel(entry.classSlug)} ${entry.level}`, subclass && subclass.selected.length > 0 ? selectedLabels(subclass)[0] : null];
        }),
        [t('terms.labels.hitPoints'), preview.vitals ? `${preview.vitals.hitPoints.value} (${hitPointNote(build, choice('build.rolledHitPoints'), t)})` : null],
      ])}
      {card('background', t('terms.builderSteps.background'), [
        [t('terms.builderSteps.background'), background?.name ?? null],
        [t('builder.review.skills'), grant('Skill proficiencies')],
        [t('builder.review.tools'), grant('Tool proficiencies')],
      ])}
      {card('abilities', t('terms.builderSteps.abilities'), [
        [t('builder.review.method'), method && method.selected.length > 0 ? selectedLabels(method)[0] : null],
        [
          t('builder.review.totals'),
          Object.keys(build.baseAbilityScores).length === 0
            ? null
            : preview.abilities.map((ability) => `${terms.abilityAbbreviation(ability.ability)} ${ability.score}`).join(' · '),
        ],
      ])}
      {card('equipment', t('terms.builderSteps.equipment'), [
        [t('builder.review.inventory'), equipment && equipment.inventory.length > 0 ? t('builder.review.items', { count: equipment.inventory.length }) : null],
        [t('builder.review.equipped'), equipped.length > 0 ? equipped.join(', ') : t('builder.review.nothing')],
        [t('terms.builder.currency'), coins || null],
      ])}
    </div>
  );
}

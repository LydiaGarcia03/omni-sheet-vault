import type { ReactNode } from 'react';
import { Link } from 'react-router';
import { selectedLabels } from '../../../builder/builderDefinition';
import type { BuildPlan, CreationChoice, DraftPreview } from '../../../builder/draftApi';
import { dnd5eStepOf, preferencesOf, type Dnd5eBuild } from './dnd5eBuild';

type Dnd5eReviewCardsProps = {
  characterId: string;
  name: string;
  build: Dnd5eBuild;
  plan: BuildPlan;
  preview: DraftPreview;
};

const ABBREVIATIONS: Record<string, string> = {
  strength: 'STR',
  dexterity: 'DEX',
  constitution: 'CON',
  intelligence: 'INT',
  wisdom: 'WIS',
  charisma: 'CHA',
};

/** How the hit points were set: fixed, rolled, or still averaged over the levels not rolled yet. */
function hitPointNote(build: Dnd5eBuild, rolled: CreationChoice | undefined): string {
  if (build.hitPointMethod !== 'ROLLED') {
    return 'fixed';
  }
  const left = rolled ? rolled.count - rolled.selected.length : 0;
  return left > 0 ? `average for ${left} of ${rolled?.count} levels not rolled yet` : 'rolled';
}

/** One card per step with what the draft holds there, a badge for what's left, and a link back to edit it. */
export function Dnd5eReviewCards({ characterId, name, build, plan, preview }: Dnd5eReviewCardsProps) {
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
        [equipment.gold, 'gp'],
        [equipment.silver, 'sp'],
        [equipment.copper, 'cp'],
      ]
        .filter(([amount]) => Number(amount) > 0)
        .map(([amount, unit]) => `${amount} ${unit}`)
        .join(' · ')
    : '';

  const card = (step: string, title: string, rows: [string, ReactNode][]) => (
    <section key={step} className="builder-frame builder-review-card">
      <div className="builder-frame__title">
        <span className="builder-grow">{title}</span>
        {pendingIn(step) > 0 && <span className="builder-badge builder-badge--pending">{pendingIn(step)} left</span>}
        <Link className="builder-btn" to={`/characters/${characterId}/build/${step}`}>
          Edit
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
      {card('basics', 'Basics', [
        ['Name', name || null],
        ['Sources', preferences.sources === null ? 'Every book' : `Core + ${preferences.sources.length} other books`],
        [
          'Rules',
          [
            preferences.advancement === 'XP' ? 'XP' : 'Milestone',
            build.hitPointMethod === 'ROLLED' ? 'rolled HP' : 'fixed HP',
            preferences.encumbrance === 'STANDARD' ? 'standard encumbrance' : 'no encumbrance',
          ].join(' · '),
        ],
      ])}
      {card('species', 'Species', [
        ['Species', species?.name ?? null],
        ['Bonuses', species?.facts.find((fact) => fact.label === 'Ability bonuses')?.text ?? null],
      ])}
      {card('classes', 'Classes', [
        ...build.classes.map((entry): [string, ReactNode] => {
          const subclass = choice(`build.class.${entry.classSlug}.subclass`);
          return [`${classLabel(entry.classSlug)} ${entry.level}`, subclass && subclass.selected.length > 0 ? selectedLabels(subclass)[0] : null];
        }),
        ['Hit points', preview.vitals ? `${preview.vitals.hitPoints.value} (${hitPointNote(build, choice('build.rolledHitPoints'))})` : null],
      ])}
      {card('background', 'Background', [
        ['Background', background?.name ?? null],
        ['Skills', grant('Skill proficiencies')],
        ['Tools', grant('Tool proficiencies')],
      ])}
      {card('abilities', 'Abilities', [
        ['Method', method && method.selected.length > 0 ? selectedLabels(method)[0] : null],
        [
          'Totals',
          Object.keys(build.baseAbilityScores).length === 0
            ? null
            : preview.abilities.map((ability) => `${ABBREVIATIONS[ability.ability] ?? ability.ability} ${ability.score}`).join(' · '),
        ],
      ])}
      {card('equipment', 'Equipment', [
        ['Inventory', equipment && equipment.inventory.length > 0 ? `${equipment.inventory.length} items` : null],
        ['Equipped', equipped.length > 0 ? equipped.join(', ') : 'Nothing'],
        ['Currency', coins || null],
      ])}
    </div>
  );
}

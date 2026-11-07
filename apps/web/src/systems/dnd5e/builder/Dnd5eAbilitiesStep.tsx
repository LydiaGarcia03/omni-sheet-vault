import { useState } from 'react';
import { RolledDice } from '../../../builder/RolledDice';
import type { AbilityPreview, CreationChoice, DraftPreview } from '../../../builder/draftApi';
import { postCreationRoll, type RollResult } from '../../../dice/api';
import { ENGLISH_TERMS, useDnd5eTerms } from '../locales/dnd5eMessages';
import {
  ABILITIES,
  withAbilityAdjustment,
  withAbilityScore,
  withAbilityScoreMethod,
  type Dnd5eAbilityScoreMethod,
  type Dnd5eBuild,
} from './dnd5eBuild';

type MethodRules = {
  values?: number[];
  budget?: number;
  costs?: Record<string, number>;
  min?: number;
  max?: number;
  roll?: { count: number; sides: number; keepHighest: number; label: string };
};

type Dnd5eAbilitiesStepProps = {
  characterId: string;
  build: Dnd5eBuild;
  choices: CreationChoice[];
  problems: string[];
  preview: DraftPreview;
  onChange: (build: Dnd5eBuild) => void;
};

/** Contributions the player doesn't type: species, feats and other grants, not the base score or manual corrections. */
const MANUAL_SOURCES = new Set(['Base', 'Other modifier', 'Override']);

/** The API's ability-score problems, in English or Portuguese. */
const SCORE_PROBLEM = /score|point buy|standard array|valores|compra de pontos|sequência padrão/i;

const signed = (value: number) => (value >= 0 ? `+${value}` : `−${Math.abs(value)}`);

/** Score calculation cards are narrow: "Ability Score Improvement (Bard 4)" reads as "ASI (Bard 4)". */
const abbreviated = (source: string) => source.replace(/^Ability Score Improvement\b/, 'ASI');

/** D&D Beyond's Abilities step: a generation method, the base scores it allows, and every number behind each total. */
export function Dnd5eAbilitiesStep({ characterId, build, choices, problems, preview, onChange }: Dnd5eAbilitiesStepProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const [pendingMethod, setPendingMethod] = useState<Dnd5eAbilityScoreMethod | null>(null);
  const [lastRolls, setLastRolls] = useState<Record<string, RollResult>>({});
  const [rolling, setRolling] = useState<string | null>(null);
  const [rollError, setRollError] = useState<string | null>(null);
  const methodChoice = choices.find((choice) => choice.id === 'build.abilityScores');
  const methods = methodChoice?.options ?? [];
  const rulesOf = (method: string | null) => (methods.find((option) => option.key === method)?.data ?? {}) as MethodRules;
  const rules = rulesOf(build.abilityScoreMethod);
  const hasScores = Object.keys(build.baseAbilityScores).length > 0;
  const byAbility = new Map(preview.abilities.map((ability) => [ability.ability, ability]));

  const switchTo = (method: Dnd5eAbilityScoreMethod | null) => {
    const costs = rulesOf(method).costs;
    const cheapest = costs ? Math.min(...Object.keys(costs).map(Number)) : null;
    onChange(withAbilityScoreMethod(build, method, cheapest));
    setPendingMethod(null);
  };
  const requestMethod = (method: Dnd5eAbilityScoreMethod | null) => (hasScores ? setPendingMethod(method) : switchTo(method));

  const rollAbility = async (ability: string) => {
    const dice = rules.roll;
    if (!dice) {
      return;
    }
    setRolling(ability);
    setRollError(null);
    try {
      const roll = await postCreationRoll(characterId, {
        count: dice.count,
        sides: dice.sides,
        keepHighest: dice.keepHighest,
        context: `${ENGLISH_TERMS.ability(ability)} (${dice.label})`,
      });
      setLastRolls((current) => ({ ...current, [ability]: roll }));
      onChange(withAbilityScore(build, ability, roll.total));
    } catch {
      setRollError(t('builder.rollFailed'));
    } finally {
      setRolling(null);
    }
  };

  const scoreProblems = problems.filter((problem) => SCORE_PROBLEM.test(problem));
  const bonuses = speciesBonuses(preview.abilities, terms.ability);

  return (
    <>
      <section className="builder-frame" data-tutorial="ability-method">
        <div className="builder-frame__title">
          <span className="builder-grow">{t('builder.abilities.method')}</span>
          <select
            className="builder-input"
            style={{ width: 200 }}
            aria-label={t('builder.abilities.method')}
            value={build.abilityScoreMethod ?? ''}
            onChange={(event) => requestMethod((event.target.value || null) as Dnd5eAbilityScoreMethod | null)}
          >
            <option value="">{t('builder.abilities.chooseMethod')}</option>
            {methods.map((option) => (
              <option key={option.key} value={option.key}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {pendingMethod !== null && (
          <div className="builder-confirm" role="alertdialog" aria-label={t('builder.abilities.changeMethod')}>
            <span className="builder-grow">{t('builder.abilities.switchWarning')}</span>
            <button type="button" className="builder-btn builder-btn--primary" onClick={() => switchTo(pendingMethod)}>
              {t('builder.abilities.switch')}
            </button>
            <button type="button" className="builder-btn" onClick={() => setPendingMethod(null)}>
              {t('builder.abilities.cancel')}
            </button>
          </div>
        )}

        {build.abilityScoreMethod === null ? (
          <p className="builder-muted">{t('builder.abilities.pickMethod')}</p>
        ) : (
          <>
            {build.abilityScoreMethod === 'POINT_BUY' && <PointBudget build={build} rules={rules} />}
            <div className="builder-ability-grid">
              {ABILITIES.map((ability) => (
                <div key={ability} className="builder-ability-cell">
                  <label className="builder-label" htmlFor={`base-${ability}`}>
                    {terms.abilityAbbreviation(ability)}
                  </label>
                  <BaseScoreInput id={`base-${ability}`} build={build} ability={ability} rules={rules} onChange={onChange} />
                  {build.abilityScoreMethod === 'MANUAL' && rules.roll && (
                    <div className="builder-ability-cell__roll">
                      <button
                        type="button"
                        className="builder-btn"
                        aria-label={t('builder.abilities.rollAbility', { ability: terms.ability(ability) })}
                        title={rules.roll.label}
                        disabled={rolling !== null}
                        onClick={() => rollAbility(ability)}
                      >
                        {rolling === ability ? t('builder.rolling') : t('builder.roll')}
                      </button>
                      {lastRolls[ability] && <RolledDice roll={lastRolls[ability]} />}
                    </div>
                  )}
                  <div className="builder-ability-cell__total">
                    {t('builder.abilities.total', { value: build.baseAbilityScores[ability] === undefined ? '—' : (byAbility.get(ability)?.score ?? '—') })}
                    {sourcedBonus(byAbility.get(ability)) !== 0 && (
                      <span className="builder-ability-cell__bonus">{t('builder.abilities.including', { bonus: signed(sourcedBonus(byAbility.get(ability))) })}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {bonuses.map((bonus) => (
          <div key={bonus.source} className="builder-asi">
            <span>
              <b>{t('terms.builder.abilityScoreIncrease')}</b> · {bonus.source}
            </span>
            <span className="builder-positive">{bonus.parts.join(', ')}</span>
          </div>
        ))}
        {rollError && <p className="builder-problem">{rollError}</p>}
        {scoreProblems.map((problem) => (
          <p key={problem} className="builder-problem">
            {problem}
          </p>
        ))}
      </section>

      <section className="builder-frame" data-tutorial="ability-scores">
        <div className="builder-frame__title">{t('builder.abilities.calculations')}</div>
        <p className="builder-muted" style={{ marginTop: 0 }}>
          {t('builder.abilities.calculationsLead')}
        </p>
        <div className="builder-calc-grid">
          {ABILITIES.map((ability) => (
            <ScoreCard key={ability} ability={ability} build={build} preview={byAbility.get(ability)} onChange={onChange} />
          ))}
        </div>
      </section>
    </>
  );
}

function PointBudget({ build, rules }: { build: Dnd5eBuild; rules: MethodRules }) {
  const { t } = useDnd5eTerms();
  const spent = ABILITIES.reduce((total, ability) => total + (rules.costs?.[String(build.baseAbilityScores[ability])] ?? 0), 0);
  const budget = rules.budget ?? 0;
  return (
    <div className="builder-budget">
      <span className="builder-label" style={{ margin: 0 }}>
        {t('builder.abilities.pointsRemaining')}
      </span>
      <span className={`builder-budget__value${spent > budget ? ' is-over' : ''}`}>
        {budget - spent} / {budget}
      </span>
    </div>
  );
}

type BaseScoreInputProps = {
  id: string;
  build: Dnd5eBuild;
  ability: string;
  rules: MethodRules;
  onChange: (build: Dnd5eBuild) => void;
};

/** Point buy offers only affordable scores, the standard array only values no other ability took, manual any number in range. */
function BaseScoreInput({ id, build, ability, rules, onChange }: BaseScoreInputProps) {
  const { t } = useDnd5eTerms();
  const current = build.baseAbilityScores[ability];
  const set = (value: string) => onChange(withAbilityScore(build, ability, value === '' ? null : Number(value)));

  if (build.abilityScoreMethod === 'POINT_BUY' && rules.costs) {
    const costs = rules.costs;
    const spentElsewhere = ABILITIES.filter((other) => other !== ability).reduce(
      (total, other) => total + (costs[String(build.baseAbilityScores[other])] ?? 0),
      0,
    );
    const affordable = Object.entries(costs).filter(
      ([score, cost]) => spentElsewhere + cost <= (rules.budget ?? 0) || Number(score) === current,
    );
    return (
      <select id={id} className="builder-input" value={current ?? ''} onChange={(event) => set(event.target.value)}>
        {affordable.map(([score, cost]) => (
          <option key={score} value={score}>
            {t('builder.abilities.points', { score, count: cost })}
          </option>
        ))}
      </select>
    );
  }
  if (build.abilityScoreMethod === 'STANDARD_ARRAY' && rules.values) {
    const taken = ABILITIES.filter((other) => other !== ability).map((other) => build.baseAbilityScores[other]);
    const remaining = [...rules.values];
    for (const value of taken) {
      const index = remaining.indexOf(value);
      if (index >= 0) {
        remaining.splice(index, 1);
      }
    }
    return (
      <select id={id} className="builder-input" value={current ?? ''} onChange={(event) => set(event.target.value)}>
        <option value="">—</option>
        {[...new Set(remaining)].map((value) => (
          <option key={value} value={value}>
            {value}
          </option>
        ))}
      </select>
    );
  }
  return (
    <input
      id={id}
      className="builder-input"
      type="number"
      min={rules.min}
      max={rules.max}
      value={current ?? ''}
      onChange={(event) => set(event.target.value)}
    />
  );
}

type ScoreCardProps = {
  ability: string;
  build: Dnd5eBuild;
  preview: AbilityPreview | undefined;
  onChange: (build: Dnd5eBuild) => void;
};

function ScoreCard({ ability, build, preview, onChange }: ScoreCardProps) {
  const terms = useDnd5eTerms();
  const { t } = terms;
  const adjustment = build.abilityScoreAdjustments?.[ability] ?? { otherModifier: null, overrideScore: null };
  const base = build.baseAbilityScores[ability];
  const granted = (preview?.contributions ?? []).filter((part) => !MANUAL_SOURCES.has(part.source));
  const bonus = granted.reduce((total, part) => total + part.amount, 0);
  const scored = base !== undefined;
  const update = (field: 'otherModifier' | 'overrideScore', value: string) =>
    onChange(withAbilityAdjustment(build, ability, { ...adjustment, [field]: value === '' ? null : Number(value) }));

  return (
    <div className="builder-calc">
      <div className="builder-calc__head">{terms.ability(ability)}</div>
      <div className="builder-calc__row is-highlight">
        <span>{t('terms.builder.totalScore')}</span>
        <b>{scored && preview ? preview.score : '—'}</b>
      </div>
      <div className="builder-calc__row is-highlight">
        <span>{t('terms.builder.modifier')}</span>
        <b>{scored && preview ? signed(preview.modifier) : '—'}</b>
      </div>
      <div className="builder-calc__row">
        <span>{t('terms.builder.baseScore')}</span>
        <span>{base ?? '—'}</span>
      </div>
      <div className="builder-calc__row">
        <span>{t('terms.builder.bonus')}</span>
        <span>{signed(bonus)}</span>
      </div>
      {granted.map((part) => (
        <div key={part.source} className="builder-calc__row is-source">
          <span>{abbreviated(part.source)}</span>
          <span>({signed(part.amount)})</span>
        </div>
      ))}
      <label className="builder-calc__row is-edit">
        <span>{t('terms.builder.otherModifier')}</span>
        <input
          className="builder-input builder-input--number"
          type="number"
          min={-20}
          max={20}
          placeholder="—"
          value={adjustment.otherModifier ?? ''}
          onChange={(event) => update('otherModifier', event.target.value)}
        />
      </label>
      <label className="builder-calc__row is-edit">
        <span>{t('terms.builder.overrideScore')}</span>
        <input
          className="builder-input builder-input--number"
          type="number"
          min={1}
          max={30}
          placeholder="—"
          value={adjustment.overrideScore ?? ''}
          onChange={(event) => update('overrideScore', event.target.value)}
        />
      </label>
    </div>
  );
}

function sourcedBonus(ability: AbilityPreview | undefined): number {
  return (ability?.contributions ?? []).filter((part) => !MANUAL_SOURCES.has(part.source)).reduce((total, part) => total + part.amount, 0);
}

/** The species' and other grants' increases, grouped by source: "Mountain Dwarf: Strength +2". */
function speciesBonuses(abilities: AbilityPreview[], nameOf: (ability: string) => string): { source: string; parts: string[] }[] {
  const bySource = new Map<string, string[]>();
  for (const ability of abilities) {
    for (const part of ability.contributions) {
      if (!MANUAL_SOURCES.has(part.source)) {
        bySource.set(part.source, [...(bySource.get(part.source) ?? []), `${nameOf(ability.ability)} ${signed(part.amount)}`]);
      }
    }
  }
  return [...bySource].map(([source, parts]) => ({ source, parts }));
}

import { useState } from 'react';
import { Link } from 'react-router';
import type { CreationChoice } from '../../../builder/draftApi';
import { PendingMark } from '../../../builder/PendingMark';
import { postCreationRoll } from '../../../dice/api';
import { withRolledHitPoint, type Dnd5eBuild } from './dnd5eBuild';

/** `group` is the class a row belongs to and `classLevel` its level in that class, so rows can be shown per class or per level. */
type LevelRules = { sides: number; average: number; rollLabel: string; group?: string; classLevel?: number };

const FIXED_HIT_POINTS = 'FIXED_HIT_POINTS';

type Dnd5eRolledHitPointsFieldProps = {
  characterId: string;
  choice: CreationChoice;
  build: Dnd5eBuild;
  onChange: (build: Dnd5eBuild) => void;
  /** Shows only this class's rows; every row when unset. */
  group?: string;
  /** With `group`, shows only this level's row (the level being gained). */
  classLevel?: number;
  /** Points to the Basics setting that switches between fixed and rolled; off where the draft's settings can't be reached. */
  showMethodHint?: boolean;
};

/** The rows of a class (or every row) that still have no value; fixed hit points never leave one. */
export function unrolledHitPointLevels(choice: CreationChoice, build: Dnd5eBuild, group?: string): number {
  if (choice.type === FIXED_HIT_POINTS) {
    return 0;
  }
  return choice.options.filter(
    (option, index) => (group === undefined || (option.data as LevelRules).group === group) && (build.rolledHitPoints[index] ?? null) === null,
  ).length;
}

/** One row per level after the first: rolled hit points take a typed or rolled die, fixed ones show the class's fixed value. */
export function Dnd5eRolledHitPointsField({ characterId, choice, build, onChange, group, classLevel, showMethodHint = true }: Dnd5eRolledHitPointsFieldProps) {
  const [rolling, setRolling] = useState<number | null>(null);
  const [rollError, setRollError] = useState<string | null>(null);
  const fixed = choice.type === FIXED_HIT_POINTS;
  const levels = choice.options.map((option, index) => ({ index, label: option.label, ...(option.data as LevelRules) }));
  const shown = levels.filter(
    (level) => (group === undefined || level.group === group) && (classLevel === undefined || level.classLevel === classLevel),
  );
  const valueAt = (index: number) => build.rolledHitPoints[index] ?? null;
  const set = (index: number, value: number | null) => onChange(withRolledHitPoint(build, index, value, levels.length));
  const rolledTotal = shown.reduce((total, level) => total + (valueAt(level.index) ?? 0), 0);
  const averageTotal = shown.reduce((total, level) => total + level.average, 0);

  if (shown.length === 0) {
    return null;
  }

  const roll = async (index: number) => {
    const level = levels[index];
    setRolling(index);
    setRollError(null);
    try {
      const result = await postCreationRoll(characterId, { sides: level.sides, count: 1, keepHighest: null, context: level.rollLabel });
      set(index, result.total);
    } catch {
      setRollError('The roll failed. Try again.');
    } finally {
      setRolling(null);
    }
  };

  const basics = <Link to={`/characters/${characterId}/build/basics`}>Basics</Link>;

  return (
    <div className="builder-hp">
      <p className="builder-hp__subtitle">
        {choice.prompt}
        {!fixed && shown.some((level) => valueAt(level.index) === null) && <PendingMark />}
      </p>
      <div className="builder-hp__rows">
        {shown.map((level) => (
          <div key={level.label} className="builder-hp-roll">
            <span>
              <b>{level.label}</b> <span className="builder-muted">(d{level.sides})</span>
            </span>
            {fixed ? (
              <span className="builder-hp-fixed" aria-label={`Hit points, ${level.label}`}>
                {level.average}
              </span>
            ) : (
              <>
                <input
                  className="builder-input builder-input--number"
                  type="text"
                  inputMode="numeric"
                  maxLength={2}
                  aria-label={`Hit points, ${level.label}`}
                  value={valueAt(level.index) ?? ''}
                  onChange={(event) => {
                    const digits = event.target.value.replace(/\D/g, '');
                    set(level.index, digits === '' ? null : Number(digits));
                  }}
                />
                <button type="button" className="builder-btn" disabled={rolling !== null} onClick={() => roll(level.index)}>
                  {rolling === level.index ? 'Rolling…' : `Roll d${level.sides}`}
                </button>
              </>
            )}
          </div>
        ))}
        <div className="builder-hp-total">
          {fixed ? (
            <span>Fixed total {averageTotal}</span>
          ) : (
            <>
              <span>Rolled total {rolledTotal}</span>
              <span className="builder-muted">Average would be {averageTotal}</span>
            </>
          )}
        </div>
        {showMethodHint && (
          <p className="builder-hp-hint builder-muted">
            {fixed ? (
              <>Each level takes the class's fixed value: the die's average, rounded up. To roll instead, set Hit points on level up to Rolled in {basics}.</>
            ) : (
              <>To take each level's fixed value (the die's average, rounded up) instead, set Hit points on level up to Fixed in {basics}.</>
            )}
          </p>
        )}
        {rollError && <p className="builder-problem">{rollError}</p>}
      </div>
    </div>
  );
}

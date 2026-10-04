import { useState } from 'react';
import type { CreationChoice } from '../../../builder/draftApi';
import { postCreationRoll } from '../../../dice/api';
import { withRolledHitPoint, type Dnd5eBuild } from './dnd5eBuild';

/** `group` is the class a row belongs to and `classLevel` its level in that class, so rows can be shown per class or per level. */
type LevelRules = { sides: number; average: number; rollLabel: string; group?: string; classLevel?: number };

type Dnd5eRolledHitPointsFieldProps = {
  characterId: string;
  choice: CreationChoice;
  build: Dnd5eBuild;
  onChange: (build: Dnd5eBuild) => void;
  /** Shows only this class's rows; every row when unset. */
  group?: string;
  /** With `group`, shows only this level's row (the level being gained). */
  classLevel?: number;
};

/** The rows of a class (or every row) that still have no value. */
export function unrolledHitPointLevels(choice: CreationChoice, build: Dnd5eBuild, group?: string): number {
  return choice.options.filter(
    (option, index) => (group === undefined || (option.data as LevelRules).group === group) && (build.rolledHitPoints[index] ?? null) === null,
  ).length;
}

/** One row per level after the first: type the die you rolled at the table, roll it here, or take the fixed value. */
export function Dnd5eRolledHitPointsField({ characterId, choice, build, onChange, group, classLevel }: Dnd5eRolledHitPointsFieldProps) {
  const [rolling, setRolling] = useState<number | null>(null);
  const [rollError, setRollError] = useState<string | null>(null);
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

  const useFixed = (index: number) => set(index, levels[index].average);

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

  return (
    <div className="builder-choice">
      <div className="builder-choice__label">{choice.prompt}</div>
      <div>
        {shown.map((level) => (
          <div key={level.label} className="builder-hp-roll">
            <span>
              <b>{level.label}</b> <span className="builder-muted">d{level.sides}</span>
            </span>
            <input
              className="builder-input builder-input--number"
              type="number"
              min={1}
              max={level.sides}
              aria-label={`Hit points, ${level.label}`}
              value={valueAt(level.index) ?? ''}
              onChange={(event) => set(level.index, event.target.value === '' ? null : Number(event.target.value))}
            />
            <button type="button" className="builder-btn" disabled={rolling !== null} onClick={() => roll(level.index)}>
              {rolling === level.index ? 'Rolling…' : `Roll d${level.sides}`}
            </button>
            <button
              type="button"
              className="builder-btn"
              aria-label={`Use fixed value for ${level.label}`}
              title="The class's fixed value: the die's average, rounded up"
              onClick={() => useFixed(level.index)}
            >
              Use fixed ({level.average})
            </button>
          </div>
        ))}
        <div className="builder-hp-total">
          <span>Rolled total {rolledTotal}</span>
          <span className="builder-muted">Average would be {averageTotal}</span>
        </div>
        {rollError && <p className="builder-problem">{rollError}</p>}
      </div>
    </div>
  );
}

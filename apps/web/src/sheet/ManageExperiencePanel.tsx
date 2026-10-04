import { useState } from 'react';
import type { ManageExperienceRequest } from './api';
import { SidebarHeader } from './sidebarParts';
import { formatPoints, levelForPoints, XpBar } from './XpBar';

type Change = 'add' | 'remove';

function wholePoints(text: string): number | null {
  const parsed = Number(text);
  return text.trim() !== '' && Number.isInteger(parsed) && parsed >= 0 ? parsed : null;
}

/**
 * D&D Beyond's Manage XP panel: the current total and bar, Set level and Set XP, Add/Remove
 * tabs, and a live "New XP Total". Every control edits one pending total, applied at once.
 */
export function ManageExperiencePanel({ experience, onApply }: ManageExperienceRequest) {
  const [pending, setPending] = useState<number | null>(null);
  const [change, setChange] = useState<Change>('add');
  const [amount, setAmount] = useState('');
  const [setXp, setSetXp] = useState('');

  const typedAmount = wholePoints(amount);
  const newTotal =
    typedAmount !== null
      ? Math.max(experience.currentLevelAt, (pending ?? experience.points) + (change === 'add' ? typedAmount : -typedAmount))
      : Math.max(experience.currentLevelAt, pending ?? experience.points);
  const newLevel = levelForPoints(newTotal, experience.thresholds);
  const hasChange = newTotal !== experience.points;

  function apply() {
    onApply(newTotal);
    setPending(null);
    setAmount('');
    setSetXp('');
  }

  function reset() {
    setPending(null);
    setAmount('');
    setSetXp('');
  }

  return (
    <div className="manage-xp">
      <SidebarHeader title="Manage XP" />
      <h2 className="manage-xp__total">
        Current XP Total: {formatPoints(experience.points)} (Level {experience.levelFromPoints})
      </h2>
      <XpBar experience={experience} variant="panel" />
      <div className="manage-xp__controls">
        <div className="manage-xp__set">
          <label className="manage-xp__label">
            Set level
            <select
              className="manage-xp__select"
              value=""
              onChange={(event) => {
                const level = Number(event.target.value);
                setPending(experience.thresholds[level - 1]);
                setAmount('');
              }}
            >
              <option value="">--</option>
              {experience.thresholds.map((threshold, index) => (
                <option key={threshold} value={index + 1} disabled={index + 1 < experience.level}>
                  {index + 1}
                </option>
              ))}
            </select>
          </label>
          <label className="manage-xp__label">
            Set XP
            <input
              type="number"
              min={0}
              className="manage-xp__input manage-xp__input--short"
              value={setXp}
              onChange={(event) => {
                setSetXp(event.target.value);
                const points = wholePoints(event.target.value);
                setPending(points);
                setAmount('');
              }}
            />
          </label>
        </div>
        <div className="manage-xp__adjust">
          <div className="manage-xp__tabs" role="tablist">
            {(['add', 'remove'] as const).map((tab) => (
              <button key={tab} type="button" role="tab" aria-selected={change === tab} className="manage-xp__tab" onClick={() => setChange(tab)}>
                {tab === 'add' ? 'Add XP' : 'Remove XP'}
              </button>
            ))}
          </div>
          <input
            type="number"
            min={0}
            className="manage-xp__input"
            placeholder="Type XP Value"
            aria-label={change === 'add' ? 'XP to add' : 'XP to remove'}
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
          />
        </div>
      </div>
      <h2 className="manage-xp__total manage-xp__total--new">
        New XP Total: {formatPoints(newTotal)} (Level {newLevel})
      </h2>
      {hasChange && (
        <div className="manage-xp__actions">
          <button type="button" className="manage-xp__apply" onClick={apply}>
            Apply
          </button>
          <button type="button" className="manage-xp__cancel" onClick={reset}>
            Cancel
          </button>
        </div>
      )}
    </div>
  );
}

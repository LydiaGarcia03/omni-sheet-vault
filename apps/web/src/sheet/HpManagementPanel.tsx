import { useEffect, useState } from 'react';
import type { HpManagementRequest } from './api';
import { RulesText } from './RulesText';
import { Customize, SidebarHeader } from './sidebarParts';

/** A signed amount with its sign drawn apart, as in the ability pane's table. */
function Signed({ value }: { value: number }) {
  return (
    <>
      <span className="hp-breakdown__sign">{value < 0 ? '−' : '+'}</span>
      {Math.abs(value)}
    </>
  );
}

function positiveAmount(text: string): number {
  const parsed = Number(text);
  return text.trim() !== '' && Number.isInteger(parsed) && parsed > 0 ? parsed : 0;
}

/** A blank field clears the value; anything else must be a whole number. */
function parsedOptional(text: string): number | null | undefined {
  if (text.trim() === '') {
    return null;
  }
  const parsed = Number(text);
  return Number.isInteger(parsed) ? parsed : undefined;
}

function PlusIcon() {
  return (
    <svg viewBox="0 0 16 16" aria-hidden className="hp-management__button-icon">
      <path d="M6.5 1h3v5.5H15v3H9.5V15h-3V9.5H1v-3h5.5z" />
    </svg>
  );
}

function MinusIcon() {
  return (
    <svg viewBox="0 0 16 16" aria-hidden className="hp-management__button-icon">
      <path d="M1 6.5h14v3H1z" />
    </svg>
  );
}

/** A number field saved on blur (Enter blurs); an unreadable entry reverts to the stored value. */
function OptionalNumberField({
  label,
  value,
  onSave,
}: {
  label: string;
  value: number | null;
  onSave: (value: number | null) => void;
}) {
  const [draft, setDraft] = useState(value === null ? '' : String(value));
  useEffect(() => setDraft(value === null ? '' : String(value)), [value]);

  function commit() {
    const parsed = parsedOptional(draft);
    if (parsed === undefined) {
      setDraft(value === null ? '' : String(value));
    } else if (parsed !== value) {
      onSave(parsed);
    }
  }

  return (
    <label className="hp-management__override">
      <span className="hp-management__label">{label}</span>
      <input
        type="number"
        className="hp-management__override-input"
        placeholder="--"
        value={draft}
        onChange={(event) => setDraft(event.target.value)}
        onBlur={commit}
        onKeyDown={(event) => event.key === 'Enter' && event.currentTarget.blur()}
      />
    </label>
  );
}

/**
 * D&D Beyond's HP Management pane: Current / Max / Temp; Healing and Damage, which +/− step up, with the New HP between
 * them and Apply Changes / Cancel while one is pending; then Max HP Modifier and Override Max HP. A customized maximum
 * shows the calculated one beside it. A dying character's pane ends with the death saving throw rules.
 */
export function HpManagementPanel({
  current,
  max,
  temporary,
  calculatedMax,
  maxModifier,
  maxOverride,
  maxContributions,
  dying = false,
  deathSavesRulesText = '',
  onDamage,
  onHeal,
  onSetTemporary,
  onSetMaxModifier,
  onSetMaxOverride,
}: HpManagementRequest) {
  const [currentDraft, setCurrentDraft] = useState(String(current));
  const [tempDraft, setTempDraft] = useState(String(temporary));
  const [healingDraft, setHealingDraft] = useState('');
  const [damageDraft, setDamageDraft] = useState('');

  useEffect(() => setCurrentDraft(String(current)), [current]);
  useEffect(() => setTempDraft(String(temporary)), [temporary]);

  function commitCurrent() {
    const parsed = Number(currentDraft);
    if (currentDraft.trim() === '' || !Number.isInteger(parsed)) {
      setCurrentDraft(String(current));
      return;
    }
    const delta = parsed - current;
    if (delta > 0) {
      onHeal(delta);
    } else if (delta < 0) {
      onDamage(-delta);
    }
  }

  function commitTemp() {
    const parsed = Number(tempDraft);
    if (tempDraft.trim() === '' || !Number.isInteger(parsed) || parsed < 0) {
      setTempDraft(String(temporary));
      return;
    }
    if (parsed !== temporary) {
      onSetTemporary(parsed);
    }
  }

  const healingAmount = positiveAmount(healingDraft);
  const damageAmount = positiveAmount(damageDraft);
  const hasPendingChanges = healingAmount > 0 || damageAmount > 0;
  const newHp = Math.min(max, Math.max(0, current - damageAmount) + healingAmount);

  function applyChanges() {
    if (damageAmount > 0) {
      onDamage(damageAmount);
    }
    if (healingAmount > 0) {
      onHeal(healingAmount);
    }
    cancelChanges();
  }

  function cancelChanges() {
    setHealingDraft('');
    setDamageDraft('');
  }

  const maxClass =
    max > calculatedMax ? ' hp-management__number--positive' : max < calculatedMax ? ' hp-management__number--negative' : '';

  return (
    <div className="hp-management">
      <SidebarHeader title="HP Management" />

      <div className="hp-management__section hp-management__stats">
        <label className="hp-management__stat">
          <span className="hp-management__label">Current</span>
          <input
            type="number"
            className="hp-management__stat-input"
            value={currentDraft}
            onChange={(event) => setCurrentDraft(event.target.value)}
            onBlur={commitCurrent}
            onKeyDown={(event) => event.key === 'Enter' && event.currentTarget.blur()}
            aria-label="Current hit points"
          />
        </label>
        <span className="hp-management__slash">/</span>
        <div className="hp-management__stat">
          <span className="hp-management__label">Max</span>
          <span className="hp-management__max">
            <span className={`hp-management__number${maxClass}`}>{max}</span>
            {max !== calculatedMax && <span className="hp-management__original-max">({calculatedMax})</span>}
          </span>
        </div>
        <label className="hp-management__stat">
          <span className="hp-management__label">Temp</span>
          <input
            type="number"
            min={0}
            className="hp-management__stat-input"
            value={tempDraft}
            onChange={(event) => setTempDraft(event.target.value)}
            onBlur={commitTemp}
            onKeyDown={(event) => event.key === 'Enter' && event.currentTarget.blur()}
            aria-label="Temporary hit points"
          />
        </label>
      </div>

      <div className="hp-management__section hp-management__adjust">
        <div className="hp-management__adjust-row hp-management__adjust-row--healing">
          <label className="hp-management__amount hp-management__amount--healing">
            <span className="hp-management__amount-label">Healing</span>
            <input
              type="number"
              min={0}
              className="hp-management__amount-input"
              placeholder="0"
              value={healingDraft}
              onChange={(event) => setHealingDraft(event.target.value)}
              aria-label="Healing amount"
            />
          </label>
          <button
            type="button"
            className="hp-management__button"
            aria-label="Increase Hit Points"
            onClick={() => setHealingDraft(String(healingAmount + 1))}
          >
            <PlusIcon />
          </button>
        </div>
        <div className="hp-management__new-hp">
          <span className="hp-management__label">New HP</span>
          <span className="hp-management__new-hp-value">{newHp}</span>
        </div>
        <div className="hp-management__adjust-row hp-management__adjust-row--damage">
          <label className="hp-management__amount hp-management__amount--damage">
            <span className="hp-management__amount-label">Damage</span>
            <input
              type="number"
              min={0}
              className="hp-management__amount-input"
              placeholder="0"
              value={damageDraft}
              onChange={(event) => setDamageDraft(event.target.value)}
              aria-label="Damage amount"
            />
          </label>
          <button
            type="button"
            className="hp-management__button"
            aria-label="Decrease Hit Points"
            onClick={() => setDamageDraft(String(damageAmount + 1))}
          >
            <MinusIcon />
          </button>
        </div>
        {hasPendingChanges && (
          <div className="hp-management__apply">
            <button type="button" className="mutate hp-management__apply-button" onClick={applyChanges}>
              Apply Changes
            </button>
            <button type="button" className="hp-management__cancel-button" onClick={cancelChanges}>
              Cancel
            </button>
          </div>
        )}
      </div>

      <div className="hp-management__section hp-management__overrides">
        <OptionalNumberField label="Max HP Modifier" value={maxModifier} onSave={onSetMaxModifier} />
        <OptionalNumberField label="Override Max HP" value={maxOverride} onSave={onSetMaxOverride} />
      </div>

      {maxContributions.length > 0 && (
        <div className="hp-management__breakdown">
          <Customize label="Max HP Breakdown">
            <div className="hp-breakdown" role="table" aria-label="Max HP breakdown">
              {maxContributions.map((contribution, index) => (
                <div key={index} className="hp-breakdown__row" role="row">
                  <div className="hp-breakdown__label" role="rowheader">
                    {contribution.source}
                  </div>
                  <div className="hp-breakdown__value" role="cell">
                    {index === 0 ? contribution.amount : <Signed value={contribution.amount} />}
                  </div>
                </div>
              ))}
              <div className="hp-breakdown__row hp-breakdown__row--total" role="row">
                <div className="hp-breakdown__label" role="rowheader">
                  Max Hit Points
                </div>
                <div className="hp-breakdown__value" role="cell">
                  {max}
                </div>
              </div>
            </div>
          </Customize>
        </div>
      )}

      {dying && deathSavesRulesText.trim() !== '' && (
        <section className="hp-management__death-saves-rules">
          <h2 className="sidebar-subheading">Death Saving Throws Rules</h2>
          <RulesText text={deathSavesRulesText} placement="plain" />
        </section>
      )}
    </div>
  );
}

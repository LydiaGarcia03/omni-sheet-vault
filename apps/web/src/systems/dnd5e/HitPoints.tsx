import { useState, type KeyboardEvent, type MouseEvent } from 'react';
import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame_hit_points.svg?raw';
import inkSvg from './frames/dnd_frame_hit_points_ink.svg?raw';

type HitPointsProps = {
  current: number;
  max: number;
  temporary: number;
  onDamage: (amount: number) => void;
  onHeal: (amount: number) => void;
  onSetTemporary: (amount: number) => void;
  /** Omitted where there is nothing to open (an Extra's hit points, already inside its own Entity Detail body). */
  onOpenManagement?: () => void;
};

function useAmountField() {
  const [amount, setAmount] = useState('');
  const parsedAmount = Number(amount);
  const isValid = amount.trim() !== '' && Number.isInteger(parsedAmount) && parsedAmount > 0;
  return { amount, setAmount, parsedAmount, isValid };
}

/**
 * frames/svg/dnd_frame_hit_points.svg (phase 10 asset refactor). Current/Max/
 * Temp on the right, "Hit Points" centered below both.
 *
 * **Direct owner request, 2026-08-17:** Heal and Damage share one amount
 * field instead of each carrying its own — D&D Beyond's own hit points box
 * has a single input, with Heal and Damage as two buttons that both apply
 * *that* number, correcting phase 10 slice 12's own live measurement (which
 * had found two separate inputs and built for that instead). Temp keeps its
 * own dedicated field — the owner's correction was specifically about Heal
 * and Damage, and Temp is still a distinct destination D&D Beyond gives its
 * own field. **Same day, follow-up:** the first pass put both buttons below
 * the field, side by side — the owner corrected the arrangement to match
 * D&D Beyond's own: Heal above the field, Damage below it, the field
 * sandwiched between the two rather than trailing both. The amount itself
 * is never validated against a game rule here: how damage and healing
 * actually apply (temporary points consumed first, healing capped at max)
 * is computed server-side and comes back in the next sheet — this component
 * only rejects a non-positive amount before sending it at all.
 *
 * **Direct owner request, 2026-08-17:** Temp is a click-to-edit field, not a
 * value-plus-input pair always shown side by side — matching D&D Beyond's
 * own behavior there. At rest it's just the number (or "--" when 0/unset);
 * clicking it swaps that same spot for a number input, pre-filled with the
 * current value when there is one. Committing (Enter, or blurring the
 * field) applies the typed amount and swaps back to plain text; Escape
 * discards the edit instead of applying it.
 *
 * DOM-confirmed live against D&D Beyond: clicking anywhere inside the box
 * opens a fuller "HP Management" sidebar panel (see `HpManagementPanel.tsx`),
 * except the numbers and this box's own Heal/Damage/Temp controls, which
 * stop propagation so they keep firing their own action first — same
 * "whole card opens the sidebar, its live controls opt out" pattern as
 * `AbilityBox`. `onOpenManagement` is optional since an Extra's own hit
 * points (`ExtraRow.tsx`) reuse this component from inside an Entity Detail
 * body that is already a sidebar panel — opening a second one from within
 * the first has no real target.
 */
export function HitPoints({
  current,
  max,
  temporary,
  onDamage,
  onHeal,
  onSetTemporary,
  onOpenManagement,
}: HitPointsProps) {
  const hp = useAmountField();
  const temp = useAmountField();
  const [isEditingTemp, setIsEditingTemp] = useState(false);

  function apply(field: ReturnType<typeof useAmountField>, action: (amount: number) => void) {
    if (!field.isValid) {
      return;
    }
    action(field.parsedAmount);
    field.setAmount('');
  }

  function stopPropagation(event: MouseEvent) {
    event.stopPropagation();
  }

  function startEditingTemp() {
    temp.setAmount(temporary > 0 ? String(temporary) : '');
    setIsEditingTemp(true);
  }

  function commitTemp() {
    if (temp.isValid) {
      onSetTemporary(temp.parsedAmount);
    }
    temp.setAmount('');
    setIsEditingTemp(false);
  }

  function handleTempKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Enter') {
      commitTemp();
    } else if (event.key === 'Escape') {
      temp.setAmount('');
      setIsEditingTemp(false);
    }
  }

  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (onOpenManagement && (event.key === 'Enter' || event.key === ' ')) {
      event.preventDefault();
      onOpenManagement();
    }
  };

  return (
    <div
      className="frame-box hit-points"
      role={onOpenManagement ? 'button' : undefined}
      tabIndex={onOpenManagement ? 0 : undefined}
      onClick={onOpenManagement}
      onKeyDown={onOpenManagement ? handleKeyDown : undefined}
      aria-label={`Hit points ${current} of ${max}${temporary > 0 ? `, plus ${temporary} temporary` : ''}${onOpenManagement ? ', open HP management' : ''}`}
    >
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className="hit-points__content">
        <div className="hit-points__row">
          <div className="hit-points__left" onClick={stopPropagation}>
            <button
              type="button"
              className="mutate hit-points__action hit-points__action--heal"
              disabled={!hp.isValid}
              onClick={(event) => {
                stopPropagation(event);
                apply(hp, onHeal);
              }}
            >
              Heal
            </button>
            <input
              type="number"
              min={1}
              className="hit-points__amount"
              value={hp.amount}
              onChange={(event) => hp.setAmount(event.target.value)}
              onClick={stopPropagation}
              aria-label="Heal or damage amount"
            />
            <button
              type="button"
              className="mutate hit-points__action hit-points__action--damage"
              disabled={!hp.isValid}
              onClick={(event) => {
                stopPropagation(event);
                apply(hp, onDamage);
              }}
            >
              Damage
            </button>
          </div>
          <div className="hit-points__stats">
            <div className="hit-points__stat">
              <div className="hit-points__stat-label">Current</div>
              <div className="hit-points__stat-value" onClick={stopPropagation}>
                {current}
              </div>
            </div>
            <div className="hit-points__stat-slash">/</div>
            <div className="hit-points__stat">
              <div className="hit-points__stat-label">Max</div>
              <div className="hit-points__stat-value" onClick={stopPropagation}>
                {max}
              </div>
            </div>
            <div className="hit-points__stat">
              <div className="hit-points__stat-label">Temp</div>
              {isEditingTemp ? (
                <input
                  type="number"
                  min={1}
                  className="hit-points__amount hit-points__amount--temp"
                  value={temp.amount}
                  onChange={(event) => temp.setAmount(event.target.value)}
                  onBlur={commitTemp}
                  onKeyDown={handleTempKeyDown}
                  onClick={stopPropagation}
                  aria-label="Temporary hit points amount"
                  autoFocus
                />
              ) : (
                <button
                  type="button"
                  className="mutate hit-points__stat-value"
                  onClick={(event) => {
                    stopPropagation(event);
                    startEditingTemp();
                  }}
                  aria-label="Set temporary hit points"
                >
                  {temporary > 0 ? temporary : '--'}
                </button>
              )}
            </div>
          </div>
        </div>
        <div className="hit-points__title">Hit Points</div>
      </div>
    </div>
  );
}

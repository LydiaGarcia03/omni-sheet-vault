import { useCallback, useEffect, useState } from 'react';
import { apiFetch } from '../../../api/client';
import diceIcon from '../vtm5-dice-icon.webp';

export type Vtm5Roll = {
  id: string;
  kind: 'pool' | 'rouse' | 'reroll' | 'frenzy' | 'remorse';
  label: string;
  faces: number[];
  hunger: number;
  successes: number;
  difficulty: number;
  margin: number;
  win: boolean;
  critical: boolean;
  messy: boolean;
  bestial: boolean;
  outcome: string;
  rerolled: boolean;
  rolledAt: string;
};

export type DicePool = {
  traits: Record<string, number>;
  toggle: (trait: string, value: number) => void;
  rollNow: (traits: string[], label: string) => Promise<void>;
  rouse: (reason?: string) => Promise<void>;
  frenzy: (difficulty: number) => Promise<void>;
  remorse: () => Promise<void>;
};

/** The dice pool and roll log state; every roll is made and stored by the API. */
export function useVtm5Dice(characterId: string, onSheetChanged: () => void) {
  const [traits, setTraits] = useState<Record<string, number>>({});
  const [bonus, setBonus] = useState(0);
  const [difficulty, setDifficulty] = useState(0);
  const [rolls, setRolls] = useState<Vtm5Roll[]>([]);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(() => {
    apiFetch(`/api/vtm5/characters/${characterId}/rolls`).then((r) => (r.ok ? r.json() : [])).then(setRolls, () => undefined);
  }, [characterId]);
  useEffect(refresh, [refresh]);

  const post = async (path: string, body?: unknown) => {
    const response = await apiFetch(`/api/vtm5/characters/${characterId}/rolls${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body ?? {}),
    });
    if (!response.ok) {
      const problem = await response.json().catch(() => null);
      setError(problem?.detail ?? 'The roll failed.');
      return;
    }
    setError(null);
    setOpen(true);
    refresh();
    onSheetChanged();
  };

  const pool: DicePool = {
    traits,
    toggle: (trait, value) => {
      setOpen(true);
      setTraits((current) => {
        const next = { ...current };
        if (trait in next) delete next[trait];
        else next[trait] = value;
        return next;
      });
    },
    rollNow: (names, label) => post('', { traits: names, bonus: 0, label }),
    rouse: (reason) => post('/rouse', { reason: reason ?? '' }),
    frenzy: (difficulty) => post('/frenzy', { difficulty }),
    remorse: () => post('/remorse'),
  };

  const dock = (hunger: number) => (
    <Vtm5DiceDock
      open={open}
      onToggle={() => setOpen(!open)}
      traits={traits}
      bonus={bonus}
      difficulty={difficulty}
      onDifficulty={(delta) => setDifficulty(Math.max(0, difficulty + delta))}
      hunger={hunger}
      rolls={rolls}
      error={error}
      onBonus={(delta) => setBonus(bonus + delta)}
      onRemove={(trait) => pool.toggle(trait, 0)}
      onClear={() => {
        setTraits({});
        setBonus(0);
      }}
      onRoll={() => post('', { traits: Object.keys(traits), bonus, difficulty })}
      onReroll={(rollId) => post(`/${rollId}/reroll`)}
    />
  );
  return { pool, dock };
}

type DockProps = {
  open: boolean;
  onToggle: () => void;
  traits: Record<string, number>;
  bonus: number;
  difficulty: number;
  onDifficulty: (delta: number) => void;
  hunger: number;
  rolls: Vtm5Roll[];
  error: string | null;
  onBonus: (delta: number) => void;
  onRemove: (trait: string) => void;
  onClear: () => void;
  onRoll: () => void;
  onReroll: (rollId: string) => void;
};

function Vtm5DiceDock({ open, onToggle, traits, bonus, difficulty, onDifficulty, hunger, rolls, error, onBonus, onRemove, onClear, onRoll, onReroll }: DockProps) {
  const size = Math.max(0, Object.values(traits).reduce((a, b) => a + b, 0) + bonus);
  return (
    <>
      {open && (
        <aside className="dock open">
          <section className="frame">
            <div className="frame__title">Dice Pool</div>
            <div className="pool">
              {Object.keys(traits).length === 0 && <div className="pool__empty">Click a rating on the sheet to add it.</div>}
              {Object.entries(traits).map(([trait, value]) => (
                <div key={trait} className="pool__row">
                  <span>{trait}</span>
                  <button type="button" onClick={() => onRemove(trait)} title="Remove">
                    <span className="diamonds">{'◆'.repeat(value)}</span> ✕
                  </button>
                </div>
              ))}
              <div className="pool__row">
                <span>Bonus dice</span>
                <span>
                  <button type="button" className="step" onClick={() => onBonus(-1)}>−</button> <b>{bonus > 0 ? `+${bonus}` : bonus}</b>{' '}
                  <button type="button" className="step" onClick={() => onBonus(1)}>+</button>
                </span>
              </div>
              <div className="pool__row">
                <span>Difficulty</span>
                <span>
                  <button type="button" className="step" onClick={() => onDifficulty(-1)}>−</button> <b>{difficulty || '0'}</b>{' '}
                  <button type="button" className="step" onClick={() => onDifficulty(1)}>+</button>
                </span>
              </div>
              <div className="pool__total">
                <span>Pool <b>{size}</b></span>
                <span>Hunger dice <b className="red">{Math.min(hunger, Math.max(size, 1))}</b></span>
              </div>
              <div className="pool__buttons">
                <button type="button" className="btn btn--ghost" onClick={onClear}>Clear</button>
                <button type="button" className="btn" disabled={size === 0 && Object.keys(traits).length === 0} onClick={onRoll}>Roll</button>
              </div>
              {error && <p className="vtm-problem">{error}</p>}
            </div>
          </section>
          <section className="frame">
            <div className="frame__title">Roll Log</div>
            {rolls.length === 0 && <div className="roll__empty">No rolls yet.</div>}
            {rolls.slice(0, 20).map((roll) => (
              <div key={roll.id} className="roll">
                <div className="roll__head">
                  <span>{roll.kind === 'reroll' ? `↻ ${roll.label}` : roll.label}</span>
                  <span className="muted">{new Date(roll.rolledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
                {roll.kind === 'rouse' ? (
                  <div className="roll__result">{roll.successes ? 'Passed' : 'Failed'}</div>
                ) : (
                  <div className="roll__result">
                    {roll.successes} success{roll.successes === 1 ? '' : 'es'}
                    {roll.difficulty > 0 && <span className="roll__vs"> vs {roll.difficulty} · margin {roll.margin}</span>} <small>{roll.outcome}</small>
                  </div>
                )}
                <div className="faces">
                  {roll.faces.map((face, i) => (
                    <i key={i} className={`face${face >= 6 ? ' ok' : ''}${roll.kind === 'rouse' || i >= roll.faces.length - roll.hunger ? ' hunger' : ''}`}>{face}</i>
                  ))}
                </div>
                {roll.kind !== 'rouse' && roll.kind !== 'frenzy' && roll.kind !== 'remorse' && (
                  <div className="roll__actions">
                    <span>{roll.faces.length - roll.hunger} regular · {roll.hunger} hunger</span>
                    {roll.rerolled || roll.kind === 'reroll' ? <span className="used">Re-rolled</span> : <button type="button" className="red" onClick={() => onReroll(roll.id)}>↻ Re-roll · 1 WP</button>}
                  </div>
                )}
              </div>
            ))}
          </section>
        </aside>
      )}
      <button type="button" className="dicefab" aria-label={open ? 'Hide the dice' : 'Show the dice'} aria-expanded={open} onClick={onToggle}>
        <img src={diceIcon} alt="" />
        {size > 0 && <b className="dicefab__count">{size}</b>}
      </button>
    </>
  );
}

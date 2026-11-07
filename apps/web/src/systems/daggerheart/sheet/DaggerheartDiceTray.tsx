import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { FrameIcon } from '../../dnd5e/FrameIcon';
import d4Icon from '../../dnd5e/frames/dnd_icon_die_d4.svg?raw';
import d6Icon from '../../dnd5e/frames/dnd_icon_die_d6.svg?raw';
import d8Icon from '../../dnd5e/frames/dnd_icon_die_d8.svg?raw';
import d10Icon from '../../dnd5e/frames/dnd_icon_die_d10.svg?raw';
import d12Icon from '../../dnd5e/frames/dnd_icon_die_d12.svg?raw';
import d20Icon from '../../dnd5e/frames/dnd_icon_die_d20.svg?raw';
import diceIcon from '../assets/dice-open.webp';
import { signed } from '../builder/daggerheartBuild';
import type { DaggerheartSheet } from './daggerheartSheet';
import { rollCustom, rollDuality, rollHistory, type Advantage, type DaggerheartRoll } from './daggerheartRolls';

const ICONS: Record<number, string> = { 4: d4Icon, 6: d6Icon, 8: d8Icon, 10: d10Icon, 12: d12Icon, 20: d20Icon };
const EXTRA_DICE = [4, 6, 8, 10, 12, 20];
const SHOWN_HISTORY = 3;
/** The history label joins the pool's source and kind ("Agility — Trait Roll"). */
const LABEL_JOIN = ' — ';

/** What the tray will roll: Duality dice or plain dice, with the modifiers and dice the player adds. */
type Pool = {
  source: string;
  label: string;
  duality: boolean;
  trait: string;
  /** The trait's value, shown in the modifier; the API adds it itself. */
  traitValue: number;
  experiences: number[];
  dice: number[];
  advantage: Advantage;
  reaction: boolean;
  /** The player's own ± on top of the trait and Experiences (or a weapon's damage bonus). */
  modifier: number;
  critical: boolean;
  weapon?: string;
};

export type WeaponDice = { name: string; slot: string; count: number; sides: number; bonus: number };

/** How the sheet starts a roll: every one opens a pool in the tray, which the Roll button rolls (as on Demiplane). */
export type DicePool = {
  experiences: number[];
  /** Every roll, newest first, for the game log. */
  history: DaggerheartRoll[];
  trait: (traitId: string, name: string, value: number, reaction?: boolean) => void;
  attack: (weapon: { name: string; slot: string; trait: string; modifier: number }) => void;
  damage: (weapon: WeaponDice) => void;
  spellcast: (source: string, value: number, reaction?: boolean) => void;
  experience: (index: number) => void;
};

const duality = (source: string, label: string, extra: Partial<Pool> = {}): Pool => ({
  source, label, duality: true, trait: '', traitValue: 0, experiences: [], dice: [], advantage: 'none', reaction: false, modifier: 0, critical: false, ...extra,
});

/**
 * Daggerheart's dice tray, modelled on Demiplane's: a round button in the bottom-left corner that opens Duality and
 * the » mini tray of dice. Every roll first builds a pool (from the tray or the sheet's traits, weapons and Experiences),
 * which the player adjusts (advantage, reaction, ±, dice) and rolls. Results stack above, the newest expanded.
 */
export function useDaggerheartDice(characterId: string, sheet: DaggerheartSheet | null, onSheet: (sheet: DaggerheartSheet) => void): { pool: DicePool; tray: ReactNode } {
  const [open, setOpen] = useState(false);
  const [extras, setExtras] = useState(false);
  const [pool, setPool] = useState<Pool | null>(null);
  const [history, setHistory] = useState<DaggerheartRoll[]>([]);
  const [expanded, setExpanded] = useState<string | null>(null);
  /** Rolls made in this session, newest first; the tray shows them until each is closed (the game log keeps them all). */
  const [recent, setRecent] = useState<DaggerheartRoll[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [rolling, setRolling] = useState(false);
  const critical = useRef<string | null>(null);
  const requests = useRef(new Map<string, Pool>());

  useEffect(() => {
    let active = true;
    rollHistory(characterId).then((rolls) => active && setHistory(rolls));
    return () => {
      active = false;
    };
  }, [characterId]);

  const start = useCallback((next: Pool) => {
    setPool(next);
    setOpen(true);
    setError(null);
  }, []);

  const api: DicePool = {
    experiences: pool?.experiences ?? [],
    history,
    trait: (traitId, name, value, reaction = false) => start(duality(name, 'Trait Roll', { trait: traitId, traitValue: value, reaction })),
    attack: (weapon) => start(duality(weapon.name, 'Attack Roll', { trait: weapon.trait, traitValue: weapon.modifier, weapon: weapon.slot })),
    damage: (weapon) =>
      start({
        ...duality(weapon.name, 'Damage Roll'),
        duality: false,
        dice: Array.from({ length: weapon.count }, () => weapon.sides),
        modifier: weapon.bonus,
        critical: critical.current === weapon.slot,
        weapon: weapon.slot,
      }),
    spellcast: (source, value, reaction = false) => start(duality(source, 'Spellcast Roll', { trait: 'spellcast', traitValue: value, reaction })),
    experience: (index) => {
      const current = pool?.duality ? pool : duality('Custom Roll', 'Duality Roll');
      const experiences = current.experiences.includes(index) ? current.experiences.filter((i) => i !== index) : [...current.experiences, index];
      start({ ...current, experiences });
    },
  };

  const addDie = (sides: number) => start({ ...(pool ?? { ...duality('Custom Roll', 'Custom Roll'), duality: false }), dice: [...(pool?.dice ?? []), sides] });

  const roll = async (request: Pool) => {
    setRolling(true);
    setError(null);
    try {
      const groups = EXTRA_DICE.map((sides) => ({ sides, count: request.dice.filter((die) => die === sides).length })).filter((group) => group.count > 0);
      const label = request.source + LABEL_JOIN + request.label;
      let result: DaggerheartRoll;
      if (request.duality) {
        const { roll: rolled, sheet: changed } = await rollDuality(characterId, {
          trait: request.trait,
          experiences: request.experiences,
          advantage: request.advantage,
          modifier: request.modifier,
          difficulty: 0,
          reaction: request.reaction,
          label,
          dice: groups,
        });
        result = rolled;
        onSheet(changed);
        critical.current = request.weapon && rolled.critical && !request.reaction ? request.weapon : null;
      } else {
        if (groups.length === 0) throw new Error('Add a die to roll.');
        result = await rollCustom(characterId, groups, request.modifier, request.critical, label);
        if (request.critical) critical.current = null;
      }
      requests.current.set(result.id, { ...request, experiences: [] });
      setHistory((previous) => [result, ...previous]);
      setRecent((previous) => [result, ...previous]);
      setExpanded(result.id);
      setPool(null);
      setExtras(false);
    } catch (cause) {
      setError((cause as Error).message);
    } finally {
      setRolling(false);
    }
  };

  const hope = sheet?.trackers.hope ?? 0;
  const shown = recent.slice(0, SHOWN_HISTORY).reverse();
  const newest = recent[0]?.id;

  const tray = sheet && (
    <div className={`dh-tray${open ? ' is-open' : ''}`}>
      {open && shown.length > 0 && (
        <div className="dh-tray__history">
          {shown.map((item) => (
            <HistoryItem
              key={item.id}
              roll={item}
              expanded={(expanded ?? newest) === item.id}
              onExpand={() => setExpanded(item.id)}
              onReroll={requests.current.has(item.id) ? () => void roll(requests.current.get(item.id) as Pool) : undefined}
              onClose={() => setRecent((previous) => previous.filter((other) => other.id !== item.id))}
              rolling={rolling}
            />
          ))}
        </div>
      )}
      {open && pool && (
        <div className="dh-tray__pool">
          <div className="dh-tray__pool-head">
            <div>
              {pool.source !== pool.label && <small>{pool.source}</small>}
              <b>{pool.reaction ? pool.label.replace('Roll', 'Reaction') : pool.label}</b>
            </div>
            <button type="button" className="dh-tray__clear" onClick={() => setPool(null)}>
              Clear
            </button>
          </div>
          <div className="dh-tray__pool-dice">
            {pool.duality && (
              <>
                <Die sides={12} tone="hope" title="Hope die" />
                <Die sides={12} tone="fear" title="Fear die" />
                {pool.advantage !== 'none' && <Die sides={6} tone={pool.advantage === 'advantage' ? 'adv' : 'dis'} title={pool.advantage === 'advantage' ? 'Advantage die' : 'Disadvantage die'} />}
              </>
            )}
            {pool.dice.map((sides, index) => (
              <button key={index} type="button" className="dh-tray__pool-die" title={`Remove the d${sides}`} onClick={() => setPool({ ...pool, dice: pool.dice.filter((_, i) => i !== index) })}>
                <Die sides={sides} />
              </button>
            ))}
            {pool.critical && <span className="dh-tray__tag crit">Critical damage</span>}
          </div>
          {pool.experiences.length > 0 && (
            <div className="dh-tray__exps">
              {pool.experiences.map((index) => (
                <button key={index} type="button" title="Take it off this roll" onClick={() => setPool({ ...pool, experiences: pool.experiences.filter((i) => i !== index) })}>
                  {sheet.experiences[index]?.name} {signed(sheet.experiences[index]?.bonus ?? 0)} ✕
                </button>
              ))}
              <small className={pool.experiences.length > hope ? 'short' : undefined}>
                {pool.experiences.length} Hope · you have {hope}
              </small>
            </div>
          )}
          <div className="dh-tray__pool-row">
            {pool.duality ? (
              <span className="dh-tray__toggles">
                <button type="button" className="adv" aria-pressed={pool.advantage === 'advantage'} onClick={() => setPool({ ...pool, advantage: pool.advantage === 'advantage' ? 'none' : 'advantage' })}>
                  +adv
                </button>
                <button type="button" className="dis" aria-pressed={pool.advantage === 'disadvantage'} onClick={() => setPool({ ...pool, advantage: pool.advantage === 'disadvantage' ? 'none' : 'disadvantage' })}>
                  -disadv
                </button>
                <button type="button" className="react" aria-pressed={pool.reaction} title="A reaction roll makes no Hope or Fear" onClick={() => setPool({ ...pool, reaction: !pool.reaction })}>
                  reaction
                </button>
              </span>
            ) : (
              <span />
            )}
            <Modifier pool={pool} experienceBonus={pool.experiences.reduce((total, index) => total + (sheet.experiences[index]?.bonus ?? 0), 0)} onChange={(modifier) => setPool({ ...pool, modifier })} />
          </div>
          <button type="button" className="dh-tray__roll-button" disabled={rolling || (!pool.duality && pool.dice.length === 0)} onClick={() => void roll(pool)}>
            {rolling ? 'Rolling…' : 'Roll'}
          </button>
        </div>
      )}
      {open && error && (
        <div className="dh-tray__error" role="alert">
          {error}
          <button type="button" aria-label="Dismiss" onClick={() => setError(null)}>
            ✕
          </button>
        </div>
      )}
      <div className="dh-tray__controls">
        <button type="button" className="dh-tray__toggle" aria-label={open ? 'Close the dice tray' : 'Open the dice tray'} aria-expanded={open} onClick={() => setOpen(!open)}>
          {open ? <span className="dh-tray__chevron" aria-hidden="true" /> : <img src={diceIcon} alt="" />}
        </button>
        {open && (
          <>
            {!pool && (
              <button type="button" className="dh-tray__duality" onClick={() => start(duality('Custom Roll', 'Duality Roll'))}>
                Duality
              </button>
            )}
            <button type="button" className="dh-tray__more" aria-expanded={extras} aria-label={extras ? 'Hide the extra dice' : 'Show the extra dice'} onClick={() => setExtras(!extras)}>
              {extras ? '<<' : '>>'}
            </button>
            {extras && (
              <div className="dh-tray__extra">
                {EXTRA_DICE.map((sides) => (
                  <button key={sides} type="button" className="dh-tray__extra-die" aria-label={`Add a d${sides}`} onClick={() => addDie(sides)}>
                    <FrameIcon svg={ICONS[sides]} className="dh-tray__extra-icon" />
                    <span>+d{sides}</span>
                  </button>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );

  return { pool: api, tray };
}

function Die({ sides, tone, value, title }: { sides: number; tone?: string; value?: number; title?: string }) {
  return (
    <span className={`dh-die${tone ? ` ${tone}` : ''}`} title={title ?? `d${sides}`}>
      <FrameIcon svg={ICONS[sides]} className="dh-die__icon" />
      {value !== undefined && <b>{value}</b>}
    </span>
  );
}

function Modifier({ pool, experienceBonus, onChange }: { pool: Pool; experienceBonus: number; onChange: (modifier: number) => void }) {
  const fixed = pool.traitValue + experienceBonus;
  const total = fixed + pool.modifier;
  return (
    <span className="dh-tray__modifier">
      <button type="button" aria-label="Lower the modifier" onClick={() => onChange(pool.modifier - 1)}>
        -
      </button>
      <input type="text" inputMode="numeric" aria-label="Modifier" value={total === 0 ? '' : signed(total)} placeholder="+0" onChange={(event) => onChange((Number(event.target.value.replace(/[^\d-]/g, '')) || 0) - fixed)} />
      <button type="button" aria-label="Raise the modifier" onClick={() => onChange(pool.modifier + 1)}>
        +
      </button>
    </span>
  );
}

function HistoryItem({ roll, expanded, onExpand, onReroll, onClose, rolling }: { roll: DaggerheartRoll; expanded: boolean; onExpand: () => void; onReroll?: () => void; onClose: () => void; rolling: boolean }) {
  const [source, label] = roll.label.includes(LABEL_JOIN) ? roll.label.split(LABEL_JOIN) : ['Roll', roll.label];
  const isDuality = roll.kind === 'duality' || roll.kind === 'reaction';
  const side = !isDuality ? 'plain' : roll.critical ? 'crit' : roll.withHope ? 'hope' : 'fear';
  const skip = isDuality ? (roll.advantageDie ? 3 : 2) : 0;
  const extras = roll.dice.slice(skip);
  const sides = (roll.sides ?? []).slice(skip);
  return (
    <div className={`dh-tray__item ${side}${expanded ? ' expanded' : ''}`}>
      <button type="button" className="dh-tray__item-head" onClick={onExpand} aria-expanded={expanded}>
        <span className="dh-tray__names">
          <small>{source}</small>
          <b>{label}</b>
        </span>
      </button>
      {expanded && onReroll && (
        <button type="button" className="dh-tray__reroll" disabled={rolling} onClick={onReroll}>
          Reroll
        </button>
      )}
      <span className="dh-tray__total">{roll.total}</span>
      <button type="button" className="dh-tray__close" aria-label="Close this result" title="Close (it stays in the game log)" onClick={onClose}>
        ✕
      </button>
      {expanded && (
        <div className="dh-tray__faces">
          {isDuality && (
            <>
              <Die sides={12} tone="hope" value={roll.hope} title="Hope die" />
              <Die sides={12} tone="fear" value={roll.fear} title="Fear die" />
              {roll.advantageDie !== 0 && <Die sides={6} tone={roll.advantageDie > 0 ? 'adv' : 'dis'} value={Math.abs(roll.advantageDie)} />}
            </>
          )}
          {extras.map((face, index) => (
            <Die key={index} sides={sides[index] ?? 6} value={face} />
          ))}
          <span className="dh-tray__mod">{signed(roll.modifier)}</span>
          {isDuality && <span className={`dh-tray__tag ${side}`}>{roll.critical ? 'Critical' : roll.kind === 'reaction' ? 'Reaction' : roll.withHope ? 'With Hope' : 'With Fear'}</span>}
          {roll.kind === 'custom' && roll.critical && <span className="dh-tray__tag crit">Critical</span>}
        </div>
      )}
    </div>
  );
}

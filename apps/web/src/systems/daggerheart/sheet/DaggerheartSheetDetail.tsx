import { useEffect, useState, type ReactNode } from 'react';
import { Link } from 'react-router';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { renameCharacter } from '../../../characters/api';
import type { Portrait } from '../../../characters/portrait';
import { signed, TRAITS, type TraitId } from '../builder/daggerheartBuild';
import type { DicePool } from './DaggerheartDiceTray';
import type { DaggerheartRoll } from './daggerheartRolls';
import { weaponDice, type Act } from './DaggerheartSheetScreen';
import {
  burdenLabel,
  FEATURE_SOURCES,
  featuresBy,
  LOADOUT_SIZE,
  loadout,
  REST_MOVES,
  restMoveName,
  SHORT_RESTS_BEFORE_LONG,
  STANDARD_CONDITIONS,
  vault,
  weaponLine,
  type Calculated,
  type DaggerheartSheet,
  type SheetDomainCard,
} from './daggerheartSheet';

export type Detail = { kind: string; key: string };

type CharacterInfo = { id: string; systemId: string; name: string; portrait: Portrait | null };
type Context = {
  act: Act;
  character: CharacterInfo;
  open: (detail: Detail) => void;
  pool: DicePool;
  onSheet: (sheet: DaggerheartSheet) => void;
  onRenamed: (name: string) => void;
  onPortrait: (portrait: Portrait | null) => void;
  onError: (message: string) => void;
};

/** The sidebar: one element's full text, the contributions behind its number, and what can be done with it. */
export function DaggerheartSheetDetail({ sheet, detail, onClose, ...context }: { sheet: DaggerheartSheet; detail: Detail; onClose: () => void } & Context) {
  useEffect(() => {
    const close = (event: KeyboardEvent) => event.key === 'Escape' && onClose();
    window.addEventListener('keydown', close);
    return () => window.removeEventListener('keydown', close);
  }, [onClose]);
  const view = describe(sheet, detail, context);
  if (!view) {
    return null;
  }
  return (
    <aside className="side" aria-label={view.title}>
      <div className="side__head">
        <small>{view.kind}</small>
        <h2>{view.title}</h2>
        <button type="button" className="side__close" aria-label="Close" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="side__body">{view.body}</div>
    </aside>
  );
}

type View = { kind: string; title: string; body: ReactNode };

function describe(sheet: DaggerheartSheet, { kind, key }: Detail, context: Context): View | null {
  const { derived } = sheet;
  const { act, pool } = context;
  const rollButton = (text: string, start: (reaction: boolean) => void) => (
    <div className="acts">
      <button type="button" className="dh-btn gold" onClick={() => start(false)}>
        {text}
      </button>
      <button type="button" className="dh-btn light" onClick={() => start(true)}>
        Reaction roll
      </button>
    </div>
  );
  switch (kind) {
    case 'log':
      return {
        kind: 'Dice',
        title: 'Game log',
        body: pool.history.length === 0 ? <p>No rolls yet.</p> : <GameLog rolls={pool.history} />,
      };
    case 'character':
      return { kind: 'Character', title: context.character.name, body: <CharacterPanel {...context} /> };
    case 'level':
      return {
        kind: `Tier ${tierOf(sheet.level)}`,
        title: `Level ${sheet.level}`,
        body: (
          <>
            <p>
              The party levels up together when the GM says a milestone is reached. Each level takes the tier achievement, two advancements, +1 to the
              damage thresholds and a new domain card.
            </p>
            {sheet.level < 10 ? (
              <div className="acts">
                <Link className="dh-btn gold" to={`/characters/${context.character.id}/level-up`}>
                  Level up to {sheet.level + 1}
                </Link>
              </div>
            ) : (
              <p className="side__note">Level 10 is the highest level.</p>
            )}
          </>
        ),
      };
    case 'rest':
      return {
        kind: 'Downtime',
        title: key === 'long' ? 'Long rest' : 'Short rest',
        body: <RestPanel key={key} sheet={sheet} kind={key === 'long' ? 'long' : 'short'} act={act} />,
      };
    case 'death':
      return {
        kind: 'Death move',
        title: 'You fell',
        body: (
          <>
            <p>When you mark your last Hit Point, choose a death move:</p>
            <Feature name="Blaze of Glory" text="Take one final action. It critically succeeds (with GM approval), then you cross through the veil of death." />
            <Feature
              name="Avoid Death"
              text="You drop unconscious until an ally clears a Hit Point or the party finishes a long rest. Roll your Hope Die: if it's at or under your level, you gain a scar and cross out a Hope slot."
            />
            <Feature
              name="Risk It All"
              text="Roll your Duality Dice. Hope higher: stay up and clear HP or Stress up to the Hope Die. Fear higher: you die. Matching: stay up and clear all HP and Stress."
            />
          </>
        ),
      };
    case 'trait': {
      const trait = TRAITS.find((candidate) => candidate.id === key);
      if (!trait) return null;
      const value = derived.traits[trait.id];
      return {
        kind: 'Trait',
        title: `${trait.name} ${signed(value?.value ?? 0)}`,
        body: (
          <>
            <p>Used to {trait.verbs.toLowerCase().replace(/, ([^,]*)$/, ' or $1')}. Add it to the Duality Dice when an action calls for {trait.name}.</p>
            {derived.spellcastTrait.toLowerCase() === trait.id && <p className="side__note">Your Spellcast trait ({sheet.identity.subclass?.name}).</p>}
            {rollButton(`Roll ${trait.name}`, (reaction) => pool.trait(trait.id, trait.name, value?.value ?? 0, reaction))}
            {value && <Breakdown value={value} />}
          </>
        ),
      };
    }
    case 'evasion':
      return {
        kind: 'Defense',
        title: `Evasion ${derived.evasion.value}`,
        body: (
          <>
            <p>An attack against you hits when its roll meets or beats your Evasion.</p>
            <Breakdown value={derived.evasion} />
          </>
        ),
      };
    case 'armor':
      return {
        kind: 'Armor',
        title: sheet.armor?.name ?? 'Unarmored',
        body: (
          <>
            <p>When you take damage, you can mark an Armor Slot to reduce its severity by one threshold: Severe to Major, Major to Minor, Minor to nothing.</p>
            <div className="acts">
              <button type="button" className="dh-btn sm" disabled={sheet.trackers.armorSlotsMarked >= derived.armorScore.value} onClick={() => void act({ type: 'track', track: 'armorSlots', delta: 1 })}>
                Mark a slot
              </button>
              <button type="button" className="dh-btn sm light" disabled={sheet.trackers.armorSlotsMarked === 0} onClick={() => void act({ type: 'track', track: 'armorSlots', delta: -1 })}>
                Clear one
              </button>
              <span className="acts__note">
                {sheet.trackers.armorSlotsMarked} / {derived.armorScore.value} marked
              </span>
            </div>
            <h4>Armor Score</h4>
            <Breakdown value={derived.armorScore} />
            {sheet.armor && (
              <dl className="meta">
                <dt>Tier</dt>
                <dd>{sheet.armor.tier}</dd>
                <dt>Base thresholds</dt>
                <dd>
                  {sheet.armor.baseThresholds.major} / {sheet.armor.baseThresholds.severe}
                </dd>
                <dt>At level {sheet.level}</dt>
                <dd>
                  {derived.majorThreshold.value} / {derived.severeThreshold.value}
                </dd>
              </dl>
            )}
            {sheet.armor?.feature && <Feature name={sheet.armor.feature.name} text={sheet.armor.feature.text} />}
          </>
        ),
      };
    case 'thresholds':
      return {
        kind: 'Damage',
        title: `Thresholds ${derived.majorThreshold.value} / ${derived.severeThreshold.value}`,
        body: (
          <>
            <p>
              Damage below your Major threshold marks 1 Hit Point. Damage at or above it marks 2, and at or above your Severe threshold, 3.
            </p>
            <h4>Major</h4>
            <Breakdown value={derived.majorThreshold} />
            <h4>Severe</h4>
            <Breakdown value={derived.severeThreshold} />
          </>
        ),
      };
    case 'hp':
      return {
        kind: 'Tracker',
        title: `Hit Points · ${derived.hitPointSlots.value} slots`,
        body: (
          <>
            <p>Mark Hit Points when you take damage. When you mark your last one, you make a death move.</p>
            <Breakdown value={derived.hitPointSlots} />
          </>
        ),
      };
    case 'stress':
      return {
        kind: 'Tracker',
        title: `Stress · ${derived.stressSlots.value} slots`,
        body: (
          <>
            <p>
              Some features cost Stress, and the GM can make you mark it. Marking your last Stress makes you Vulnerable until you clear at least one; when you must mark
              Stress but can't, mark 1 Hit Point instead.
            </p>
            <Breakdown value={derived.stressSlots} />
          </>
        ),
      };
    case 'hope': {
      const hopeFeature = featuresBy(sheet, ['hope'])[0];
      return {
        kind: 'Tracker',
        title: `Hope ${sheet.trackers.hope} / ${derived.maxHope}`,
        body: (
          <>
            <p>You gain Hope when the Hope die beats the Fear die. Spend it to add an Experience to a roll, to help an ally, or on features.</p>
            {hopeFeature && <Feature name={`${hopeFeature.name} · Hope feature`} text={hopeFeature.text} />}
          </>
        ),
      };
    }
    case 'proficiency':
      return {
        kind: 'Combat',
        title: `Proficiency ${derived.proficiency}`,
        body: <p>How many damage dice your weapons roll. It grows as you level up.</p>,
      };
    case 'experience': {
      const experience = sheet.experiences[Number(key)];
      if (!experience) return null;
      return {
        kind: 'Experience',
        title: `${experience.name} ${signed(experience.bonus)}`,
        body: (
          <>
            <p>When it applies, spend a Hope to add {signed(experience.bonus)} to an action or reaction roll.</p>
            <p className="side__note">Click it on the sheet to add it to your next roll.</p>
          </>
        ),
      };
    }
    case 'weapon': {
      const weapon = key === 'secondary' ? sheet.weapons.secondary : sheet.weapons.primary;
      if (!weapon) return null;
      const attack = derived.attacks.find((candidate) => candidate.slot === key);
      return {
        kind: `${key} weapon · tier ${weapon.tier}`,
        title: weapon.name,
        body: (
          <>
            <p>{weaponLine(weapon, attack)}.</p>
            <div className="acts"><button type="button" className="dh-btn gold" onClick={() => pool.attack({ name: weapon.name, slot: key, trait: (attack?.trait ?? weapon.trait).toLowerCase(), modifier: attack?.modifier ?? 0 })}>Attack roll</button><button type="button" className="dh-btn" onClick={() => pool.damage(weaponDice(sheet, weapon, key))}>Damage roll</button></div>
            <dl className="meta">
              <dt>Attack</dt>
              <dd>
                {attack?.trait || weapon.trait} {signed(attack?.modifier ?? 0)}
              </dd>
              <dt>Damage</dt>
              <dd>
                {attack?.damage ?? weapon.damage.die} {weapon.damage.type} (Proficiency {derived.proficiency})
              </dd>
              <dt>Range</dt>
              <dd>{weapon.range}</dd>
              <dt>Burden</dt>
              <dd>{burdenLabel(weapon.burden)}</dd>
            </dl>
            {weapon.feature && <Feature name={weapon.feature.name} text={weapon.feature.text} />}
          </>
        ),
      };
    }
    case 'card': {
      const card = sheet.domainCards.find((candidate) => candidate.slug === key);
      if (!card) return null;
      return {
        kind: `${card.domain} · Level ${card.level} · ${card.type}`,
        title: card.name,
        body: (
          <>
            <p className="side__text">{card.text}</p>
            <dl className="meta">
              <dt>Recall Cost</dt>
              <dd>{card.recallCost} Stress</dd>
              <dt>Location</dt>
              <dd>{card.inLoadout ? 'Loadout' : 'Vault'}</dd>
            </dl>
            <CardMoves sheet={sheet} card={card} act={act} />
          </>
        ),
      };
    }
    case 'feature': {
      const feature = sheet.features[Number(key)];
      if (!feature) return null;
      return {
        kind: `${FEATURE_SOURCES[feature.source] ?? feature.source} · ${feature.origin}`,
        title: feature.name,
        body: (
          <>
            <p className="side__text">{feature.text}</p>
            {feature.text.includes('Spellcast Roll') && derived.spellcastTrait && rollButton(`Spellcast Roll (${derived.spellcastTrait})`, (reaction) => pool.spellcast(feature.name, derived.traits[derived.spellcastTrait.toLowerCase() as TraitId]?.value ?? 0, reaction))}
          </>
        ),
      };
    }
    case 'vault': {
      const stored = vault(sheet);
      return {
        kind: 'Domain cards',
        title: `Vault · ${stored.length} card${stored.length === 1 ? '' : 's'}`,
        body: (
          <>
            <p>
              Your loadout holds up to {LOADOUT_SIZE} domain cards ({loadout(sheet).length} now); the rest wait here. Moving a card into the loadout costs its Recall
              Cost in Stress, unless you do it at the start of a rest.
            </p>
            {stored.length === 0 && <p className="side__note">No cards in the vault yet. Cards from leveling up beyond five land here.</p>}
            {stored.map((card) => (
              <div key={card.slug}>
                <Feature name={`${card.name} · ${card.domain} ${card.level}`} text={card.text} />
                <CardMoves sheet={sheet} card={card} act={act} />
              </div>
            ))}
          </>
        ),
      };
    }
    case 'conditions':
      return {
        kind: 'Conditions',
        title: sheet.conditions.length ? sheet.conditions.join(', ') : 'No conditions',
        body: <ConditionsPanel sheet={sheet} act={act} />,
      };
    default:
      return null;
  }
}

/** Loadout → vault is free; vault → loadout costs the Recall Cost in Stress, or nothing during a rest. */
function CardMoves({ sheet, card, act }: { sheet: DaggerheartSheet; card: SheetDomainCard; act: Act }) {
  if (card.inLoadout) {
    return (
      <div className="acts">
        <button type="button" className="dh-btn sm light" onClick={() => void act({ type: 'moveCard', slug: card.slug, to: 'vault' })}>
          Move to vault
        </button>
      </div>
    );
  }
  const full = loadout(sheet).length >= LOADOUT_SIZE;
  return (
    <div className="acts">
      <button type="button" className="dh-btn sm" disabled={full} onClick={() => void act({ type: 'moveCard', slug: card.slug, to: 'loadout' })}>
        Recall · {card.recallCost} Stress
      </button>
      <button type="button" className="dh-btn sm light" disabled={full} onClick={() => void act({ type: 'moveCard', slug: card.slug, to: 'loadout', free: true })}>
        Swap in during a rest
      </button>
      {full && <span className="acts__note">The loadout is full: move a card to the vault first.</span>}
    </div>
  );
}

function ConditionsPanel({ sheet, act }: { sheet: DaggerheartSheet; act: Act }) {
  const [custom, setCustom] = useState('');
  const set = (conditions: string[]) => act({ type: 'conditions', value: conditions });
  const toggle = (name: string) => set(sheet.conditions.includes(name) ? sheet.conditions.filter((c) => c !== name) : [...sheet.conditions, name]);
  const others = sheet.conditions.filter((c) => !STANDARD_CONDITIONS.includes(c));
  return (
    <>
      <p>Hidden: rolls against you have disadvantage. Restrained: you can't move. Vulnerable: rolls against you have advantage. Marking your last Stress makes you Vulnerable until you clear one.</p>
      <div className="toggles">
        {[...STANDARD_CONDITIONS, ...others].map((name) => (
          <label key={name} className="toggle-row">
            <input type="checkbox" checked={sheet.conditions.includes(name)} onChange={() => void toggle(name)} /> {name}
          </label>
        ))}
      </div>
      <h4>Another condition</h4>
      <div className="add-row">
        <input type="text" className="field" value={custom} placeholder="From a feature, e.g. Asleep" aria-label="Another condition" onChange={(event) => setCustom(event.target.value)} />
        <button type="button" className="dh-btn sm" disabled={!custom.trim()} onClick={() => void set([...sheet.conditions, custom.trim()]).then((ok) => ok && setCustom(''))}>
          Add
        </button>
      </div>
    </>
  );
}

type PickedMove = { move: string; forAlly: boolean; withParty: boolean };

/** Two downtime moves, the same one allowed twice; the server rolls the 1d4 + Tier. */
function RestPanel({ sheet, kind, act }: { sheet: DaggerheartSheet; kind: 'short' | 'long'; act: Act }) {
  const moves = REST_MOVES[kind];
  const [picked, setPicked] = useState<PickedMove[]>([
    { move: moves[0].id, forAlly: false, withParty: false },
    { move: moves[1].id, forAlly: false, withParty: false },
  ]);
  const [done, setDone] = useState(false);
  const shortRests = sheet.shortRestsInARow ?? 0;
  const mustRestLong = kind === 'short' && shortRests >= SHORT_RESTS_BEFORE_LONG;
  const update = (index: number, patch: Partial<PickedMove>) => setPicked(picked.map((move, i) => (i === index ? { ...move, ...patch } : move)));
  const last = sheet.lastRest;
  return (
    <>
      <p>
        {kind === 'short'
          ? 'About an hour to catch your breath. Choose two downtime moves; you can pick the same one twice.'
          : 'Several hours of camp or sleep. Choose two downtime moves; you can pick the same one twice.'}{' '}
        Before them, you can move domain cards between loadout and vault for free.
      </p>
      {mustRestLong && <p className="side__note">After three short rests in a row, the next rest must be a long rest.</p>}
      {kind === 'short' && !mustRestLong && shortRests > 0 && <p className="acts__note">{shortRests} short rest{shortRests === 1 ? '' : 's'} in a row so far.</p>}
      {picked.map((choice, index) => {
        const move = moves.find((candidate) => candidate.id === choice.move) ?? moves[0];
        return (
          <div key={index} className="rest-move">
            <label>
              <span>Move {index + 1}</span>
              <select className="field" value={choice.move} onChange={(event) => update(index, { move: event.target.value, forAlly: false, withParty: false })}>
                {moves.map((candidate) => (
                  <option key={candidate.id} value={candidate.id}>
                    {candidate.name}
                  </option>
                ))}
              </select>
            </label>
            <small>{move.text}</small>
            {move.ally && (
              <label className="toggle-row">
                <input type="checkbox" checked={choice.forAlly} onChange={(event) => update(index, { forAlly: event.target.checked })} /> For an ally
              </label>
            )}
            {move.party && (
              <label className="toggle-row">
                <input type="checkbox" checked={choice.withParty} onChange={(event) => update(index, { withParty: event.target.checked })} /> With party members
              </label>
            )}
          </div>
        );
      })}
      <div className="acts">
        <button type="button" className="dh-btn" disabled={mustRestLong} onClick={() => void act({ type: 'rest', kind, moves: picked }).then((ok) => ok && setDone(true))}>
          Take the {kind} rest
        </button>
      </div>
      {done && last && (
        <div className="rest-result" role="status">
          <h4>{last.kind === 'long' ? 'Long' : 'Short'} rest taken</h4>
          {last.moves.map((move, index) => (
            <div key={index}>
              <b>{restMoveName(move.move)}</b>
              {move.forAlly ? ' for an ally' : ''}
              {move.rolled !== undefined && ` · rolled ${move.rolled} + Tier ${move.tier}`}
              {move.cleared !== undefined && move.cleared >= 0 && ` · cleared ${move.cleared}`}
              {move.cleared === -1 && ' · clears all'}
              {move.gained !== undefined && ` · +${move.gained} Hope`}
            </div>
          ))}
          <p className="acts__note">Features with uses per rest refresh now; effects that last until your next rest end.</p>
        </div>
      )}
    </>
  );
}

function CharacterPanel({ character, onRenamed, onPortrait, onError }: Omit<Context, 'act'>) {
  const [name, setName] = useState(character.name);
  const save = async () => {
    const trimmed = name.trim();
    if (!trimmed || trimmed === character.name) return;
    const renamed = await renameCharacter(character.id, trimmed).catch(() => null);
    if (renamed) onRenamed(renamed.name);
    else onError('Could not rename the character.');
  };
  return (
    <>
      <h4>Name</h4>
      <input type="text" className="field" aria-label="Character name" value={name} onChange={(event) => setName(event.target.value)} onBlur={() => void save()} />
      <div className="dh-portrait-picker">
        <PortraitPicker characterId={character.id} systemId={character.systemId} portrait={character.portrait} onChange={onPortrait} />
      </div>
    </>
  );
}

/** Tier 1 is level 1; tiers 2, 3 and 4 start at levels 2, 5 and 8. */
function tierOf(level: number): number {
  return level <= 1 ? 1 : level <= 4 ? 2 : level <= 7 ? 3 : 4;
}

/** Every roll, newest first: what was rolled, the dice, and how it came out. */
function GameLog({ rolls }: { rolls: DaggerheartRoll[] }) {
  return (
    <div className="game-log">
      {rolls.map((roll) => {
        const [source, label] = roll.label.includes(' — ') ? roll.label.split(' — ') : ['', roll.label];
        const duality = roll.kind === 'duality' || roll.kind === 'reaction';
        return (
          <div key={roll.id} className={`game-log__row${duality ? (roll.critical || roll.withHope ? ' hope' : ' fear') : ''}`}>
            <span>
              {source && <small>{source}</small>}
              <b>{label}</b>
              <small>
                {duality ? `Hope ${roll.hope} · Fear ${roll.fear}` : roll.dice.join(' + ')}
                {roll.modifier ? ` ${signed(roll.modifier)}` : ''}
                {duality ? ` · ${roll.critical ? 'Critical' : roll.kind === 'reaction' ? 'Reaction' : roll.withHope ? 'with Hope' : 'with Fear'}` : ''}
                {' · '}
                {new Date(roll.rolledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </small>
            </span>
            <b className="game-log__total">{roll.total}</b>
          </div>
        );
      })}
    </div>
  );
}

function Breakdown({ value }: { value: Calculated }) {
  return (
    <dl className="meta breakdown">
      {value.contributions.map((part, index) => (
        <div key={`${part.source}-${index}`} className="breakdown__row">
          <dt>{part.source}</dt>
          <dd>{index === 0 ? part.amount : signed(part.amount)}</dd>
        </div>
      ))}
      <div className="breakdown__row total">
        <dt>Total</dt>
        <dd>{value.value}</dd>
      </div>
    </dl>
  );
}

function Feature({ name, text }: { name: string; text: string }) {
  return (
    <div className="side__feature">
      <b>{name}</b>
      <p className="side__text">{text}</p>
    </div>
  );
}

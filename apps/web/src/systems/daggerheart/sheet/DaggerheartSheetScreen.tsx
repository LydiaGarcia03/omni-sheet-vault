import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { apiFetch } from '../../../api/client';
import { getCatalogue } from '../../../catalogue/api';
import { CharacterPortrait } from '../../../characters/CharacterPortrait';
import type { Portrait } from '../../../characters/portrait';
import type { SheetLayoutProps } from '../../../sheet/sheetLayouts';
import { AppHeader } from '../../../shell/AppHeader';
import { DaggerheartFooter } from '../DaggerheartFooter';
import { classArt } from '../daggerheartArt';
import { DAGGERHEART, type ClassData } from '../daggerheartCatalogue';
import { signed, TRAITS } from '../builder/daggerheartBuild';
import { DomainIcon, domainColor } from '../builder/DaggerheartParts';
import { FrameIcon } from '../../dnd5e/FrameIcon';
import gameLogIcon from '../../dnd5e/frames/dnd_icon_game_log.svg?raw';
import { useDaggerheartDice, type DicePool, type WeaponDice } from './DaggerheartDiceTray';
import { DaggerheartSheetDetail, type Detail } from './DaggerheartSheetDetail';
import {
  burdenLabel,
  domainCaption,
  FEATURE_SOURCES,
  featuresBy,
  HOPE_FEATURE_COST,
  heritageLine,
  LOADOUT_SIZE,
  loadout,
  MAX_PROFICIENCY,
  slotDelta,
  traitName,
  vault,
  weaponLine,
  type DaggerheartSheet,
  type SheetDomainCard,
  type SheetFeature,
  type SheetWeapon,
} from './daggerheartSheet';
import '../dh-theme.css';
import '../dh-sheet.css';

const TABS = ['Cards & Features', 'Equipment', 'Details'] as const;
type Tab = (typeof TABS)[number];
type Open = (detail: Detail) => void;
export type Act = (action: Record<string, unknown>) => Promise<boolean>;

/** The Daggerheart sheet (sheet v3): play state on the page, every element's full text and actions in the sidebar. */
export function DaggerheartSheetScreen({ character, userLabel }: SheetLayoutProps) {
  const [sheet, setSheet] = useState<DaggerheartSheet | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<Tab>('Cards & Features');
  const [detail, setDetail] = useState<Detail | null>(null);
  const [name, setName] = useState(character.name);
  const [portrait, setPortrait] = useState<Portrait | null>(character.portrait ?? null);

  useEffect(() => {
    let active = true;
    apiFetch(`/api/daggerheart/characters/${character.id}/sheet`)
      .then((response) => (response.ok ? response.json() : Promise.reject(new Error('Could not load the sheet.'))))
      .then((loaded: DaggerheartSheet) => active && setSheet(loaded), (cause: Error) => active && setError(cause.message));
    return () => {
      active = false;
    };
  }, [character.id]);

  const act: Act = useCallback(
    async (action) => {
      const response = await apiFetch(`/api/daggerheart/characters/${character.id}/actions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(action),
      });
      if (!response.ok) {
        const problem = await response.json().catch(() => null);
        setError(problem?.detail ?? 'That change could not be saved.');
        return false;
      }
      setError(null);
      setSheet(await response.json());
      return true;
    },
    [character.id],
  );

  const { pool, tray } = useDaggerheartDice(character.id, sheet, setSheet);
  const open = (next: Detail) => setDetail(next);
  const isOpen = (next: Detail) => detail?.kind === next.kind && detail.key === next.key;
  const closeDetail = useCallback(() => setDetail(null), []);

  return (
    <div className="dh-page">
      <AppHeader userName={userLabel} backToListWithin={1180} compact systemId={character.systemId} />
      {!sheet && error && (
        <p role="alert" className="problem dh-sheet-problem">
          {error}
        </p>
      )}
      {sheet && (
        <main className="dh-sheet">
          <Hero sheet={sheet} name={name} portrait={<CharacterPortrait portrait={portrait} systemId={character.systemId} className="dh-portrait" />} open={open} />
          {error && (
            <p role="alert" className="problem">
              {error}
            </p>
          )}
          <StatsRow sheet={sheet} open={open} act={act} pool={pool} />
          <section className="vitals">
            <HitPointsPanel sheet={sheet} open={open} act={act} />
            <WeaponsPanel sheet={sheet} open={open} pool={pool} />
            <HopePanel sheet={sheet} open={open} act={act} pool={pool} />
          </section>
          <nav className="dh-tabs" aria-label="Sheet sections">
            {TABS.map((tabName) => (
              <button key={tabName} type="button" className={`dh-tab${tab === tabName ? ' on' : ''}`} aria-pressed={tab === tabName} onClick={() => setTab(tabName)}>
                {tabName}
              </button>
            ))}
          </nav>
          <section className="dh-frame paper tabpanel">
            {tab === 'Cards & Features' && <CardsTab sheet={sheet} open={open} isOpen={isOpen} act={act} />}
            {tab === 'Equipment' && <EquipmentTab sheet={sheet} open={open} act={act} pool={pool} />}
            {tab === 'Details' && <DetailsTab sheet={sheet} act={act} />}
          </section>
        </main>
      )}
      {tray}
      {sheet && detail && (
        <DaggerheartSheetDetail
          sheet={sheet}
          detail={detail}
          act={act}
          character={{ id: character.id, systemId: character.systemId, name, portrait }}
          open={open}
          pool={pool}
          onSheet={setSheet}
          onRenamed={setName}
          onPortrait={setPortrait}
          onError={setError}
          onClose={closeDetail}
        />
      )}
      <DaggerheartFooter />
    </div>
  );
}

function Hero({ sheet, name, portrait, open }: { sheet: DaggerheartSheet; name: string; portrait: ReactNode; open: Open }) {
  const { lead, subclass } = heritageLine(sheet);
  const banner = sheet.identity.class ? classArt(sheet.identity.class.slug).banner : undefined;
  return (
    <section className="dh-frame navy hero">
      <button type="button" className="portrait-button" title="Change portrait or name" onClick={() => open({ kind: 'character', key: '' })}>
        {portrait}
      </button>
      <div className="hero__who">
        <button type="button" className="hero__name" onClick={() => open({ kind: 'character', key: '' })}>
          <h1>{name}</h1>
        </button>
        <div className="hero__line">
          {lead} {subclass && <b>({subclass})</b>}
          {sheet.identity.domains.length > 0 && <> · {domainCaption(sheet)}</>}
        </div>
      </div>
      <div className="hero__banner" title={domainCaption(sheet)}>
        {banner && <div className="hero__banner-art" style={{ backgroundImage: `url(${banner})` }} />}
      </div>
      <div className="hero__actions">
        <button type="button" className="dh-btn ghost hero__log" aria-label="Game log" title="Game log" onClick={() => open({ kind: 'log', key: '' })}>
          <FrameIcon svg={gameLogIcon} className="hero__log-icon" />
        </button>
        <button type="button" className="dh-btn ghost" onClick={() => open({ kind: 'rest', key: 'short' })}>
          Short rest
        </button>
        <button type="button" className="dh-btn ghost" onClick={() => open({ kind: 'rest', key: 'long' })}>
          Long rest
        </button>
        <button type="button" className="cond-box" onClick={() => open({ kind: 'conditions', key: '' })}>
          <b>Conditions</b>
          {sheet.conditions.length ? sheet.conditions.join(', ') : 'none'}
        </button>
      </div>
      <button type="button" className="hero__level" onClick={() => open({ kind: 'level', key: '' })}>
        <span className="stat level">{sheet.level}</span>
        <span className="plate">Level</span>
      </button>
    </section>
  );
}

function StatsRow({ sheet, open, act, pool }: { sheet: DaggerheartSheet; open: Open; act: Act; pool: DicePool }) {
  const { derived, trackers } = sheet;
  const armorSlots = derived.armorScore.value;
  return (
    <section className="dh-frame navy stats">
      <div className="defs">
        <button type="button" className="def" onClick={() => open({ kind: 'evasion', key: '' })}>
          <span className="stat evasion">{derived.evasion.value}</span>
          <span className="plate">Evasion</span>
        </button>
        <button type="button" className="def" onClick={() => open({ kind: 'armor', key: '' })}>
          <span className="stat armor">{armorSlots}</span>
          <span className="plate">Armor</span>
        </button>
      </div>
      <div className="aslots">
        <button type="button" className="aslots__title" onClick={() => open({ kind: 'armor', key: '' })}>
          Armor slots <em>{trackers.armorSlotsMarked}/{armorSlots}</em>
        </button>
        <div className="aslots__row">
          <button type="button" className="pm minus" aria-label="Clear an Armor Slot" disabled={trackers.armorSlotsMarked === 0} onClick={() => void act({ type: 'track', track: 'armorSlots', delta: -1 })}>
            −
          </button>
          <Slots kind="armor" label="Armor Slot" total={armorSlots} marked={trackers.armorSlotsMarked} onDelta={(delta) => act({ type: 'track', track: 'armorSlots', delta })} />
          <button type="button" className="pm" aria-label="Mark an Armor Slot" disabled={trackers.armorSlotsMarked >= armorSlots} onClick={() => void act({ type: 'track', track: 'armorSlots', delta: 1 })}>
            +
          </button>
        </div>
        <button type="button" className="aslots__armor" onClick={() => open({ kind: 'armor', key: '' })}>
          {sheet.armor ? (
            <>
              <b>{sheet.armor.name}</b>
              {sheet.armor.feature && <span> ({sheet.armor.feature.name}: {sheet.armor.feature.text})</span>}
            </>
          ) : (
            <b>Unarmored</b>
          )}
        </button>
      </div>
      <div className="traits">
        {TRAITS.map((trait) => (
          <div key={trait.id} className="trait">
            <button type="button" className="plate" title={`About ${trait.name}`} onClick={() => open({ kind: 'trait', key: trait.id })}>
              {trait.name}
            </button>
            <button type="button" className="stat trait__roll" title={`Roll ${trait.name}`} aria-label={`Roll ${trait.name}`} onClick={() => pool.trait(trait.id, trait.name, derived.traits[trait.id]?.value ?? 0)}>
              {signed(derived.traits[trait.id]?.value ?? 0)}
            </button>
            <span className="verbs">
              {trait.verbs.split(', ').map((verb) => (
                <span key={verb}>{verb}</span>
              ))}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

/** A row of slots: a click marks up to that slot or clears from it. */
function Slots({ kind, label, total, marked, onDelta }: { kind: string; label: string; total: number; marked: number; onDelta: (delta: number) => void }) {
  return (
    <span className="slots">
      {Array.from({ length: total }, (_, index) => (
        <button
          key={index}
          type="button"
          className={`slot ${kind}${index < marked ? ' on' : ''}`}
          aria-label={`${label} ${index + 1}${index < marked ? ', marked' : ''}`}
          onClick={() => onDelta(slotDelta(index, marked))}
        />
      ))}
    </span>
  );
}

function HitPointsPanel({ sheet, open, act }: { sheet: DaggerheartSheet; open: Open; act: Act }) {
  const { derived, trackers } = sheet;
  const fallen = derived.hitPointSlots.value > 0 && trackers.hitPointsMarked >= derived.hitPointSlots.value;
  return (
    <div className="dh-frame paper vit">
      <div className="dh-title">
        <span>Hit Points &amp; Stress</span>
      </div>
      <button type="button" className="thr" onClick={() => open({ kind: 'thresholds', key: '' })} aria-label="Damage thresholds">
        <span>
          Minor<small>mark 1 HP</small>
        </span>
        <b>{derived.majorThreshold.value}</b>
        <span>
          Major<small>mark 2 HP</small>
        </span>
        <b>{derived.severeThreshold.value}</b>
        <span>
          Severe<small>mark 3 HP</small>
        </span>
      </button>
      <Track label="Hit Points" kind="hp" track="hitPoints" total={derived.hitPointSlots.value} marked={trackers.hitPointsMarked} onOpen={() => open({ kind: 'hp', key: '' })} act={act} />
      <Track label="Stress" kind="stress" track="stress" total={derived.stressSlots.value} marked={trackers.stressMarked} onOpen={() => open({ kind: 'stress', key: '' })} act={act} />
      <DamageRow sheet={sheet} act={act} />
      {fallen && (
        <button type="button" className="fallen" onClick={() => open({ kind: 'death', key: '' })}>
          You marked your last Hit Point: make a death move ›
        </button>
      )}
    </div>
  );
}

function Track({ label, kind, track, total, marked, onOpen, act }: { label: string; kind: string; track: string; total: number; marked: number; onOpen: () => void; act: Act }) {
  return (
    <div className="track">
      <div className="track-head">
        <button type="button" onClick={onOpen}>
          <h4>{label}</h4>
        </button>
        <span>
          {marked} / {total} marked
        </span>
      </div>
      <div className="track-row">
        <button type="button" className="pm minus" aria-label={`Clear one ${label}`} disabled={marked === 0} onClick={() => void act({ type: 'track', track, delta: -1 })}>
          −
        </button>
        <Slots kind={kind} label={label} total={total} marked={marked} onDelta={(delta) => void act({ type: 'track', track, delta })} />
        <button type="button" className="pm" aria-label={`Mark one ${label}`} onClick={() => void act({ type: 'track', track, delta: 1 })}>
          +
        </button>
      </div>
    </div>
  );
}

function DamageRow({ sheet, act }: { sheet: DaggerheartSheet; act: Act }) {
  const [amount, setAmount] = useState('');
  const [useArmor, setUseArmor] = useState(false);
  const value = Number(amount);
  const { majorThreshold, severeThreshold } = sheet.derived;
  const severity = !(value > 0) ? '' : value >= severeThreshold.value ? 'Severe · mark 3 HP' : value >= majorThreshold.value ? 'Major · mark 2 HP' : 'Minor · mark 1 HP';
  const armorLeft = sheet.derived.armorScore.value - sheet.trackers.armorSlotsMarked;
  const last = sheet.lastDamage;
  return (
    <div className="dmg">
      <div className="dmg__row">
        <b className="dmg__label">Damage</b>
        <input type="text" inputMode="numeric" aria-label="Damage taken" value={amount} placeholder="0" onChange={(event) => setAmount(event.target.value.replace(/\D/g, ''))} />
        <span className="dmg__severity">{severity}</span>
      </div>
      <div className="dmg__row">
        <label className="dmg__armor">
          <input type="checkbox" checked={useArmor && armorLeft > 0} disabled={armorLeft === 0} onChange={(event) => setUseArmor(event.target.checked)} /> Mark an Armor Slot
        </label>
        <button
          type="button"
          className="dh-btn sm"
          disabled={!(value > 0)}
          onClick={() => void act({ type: 'damage', amount: value, useArmor: useArmor && armorLeft > 0 }).then((ok) => ok && (setAmount(''), setUseArmor(false)))}
        >
          Apply
        </button>
      </div>
      {last && (
        <div className="dmg__last" role="status">
          Last: {last.amount} damage → {last.hitPoints} HP marked{last.armorUsed ? ' (an Armor Slot absorbed one threshold)' : ''}
        </div>
      )}
    </div>
  );
}

function WeaponsPanel({ sheet, open, pool }: { sheet: DaggerheartSheet; open: Open; pool: DicePool }) {
  const { primary, secondary } = sheet.weapons;
  return (
    <div className="dh-frame paper vit">
      <div className="dh-title">
        <span>Active weapons</span>
      </div>
      <button type="button" className="prof" onClick={() => open({ kind: 'proficiency', key: '' })}>
        Proficiency
        {Array.from({ length: MAX_PROFICIENCY }, (_, index) => (
          <i key={index} className={index < sheet.derived.proficiency ? 'on' : undefined} />
        ))}
      </button>
      {primary ? <WeaponRow sheet={sheet} weapon={primary} slot="primary" open={open} pool={pool} /> : <EmptyWeapon label="Primary" text="None — fighting unarmed" />}
      {secondary ? (
        <WeaponRow sheet={sheet} weapon={secondary} slot="secondary" open={open} pool={pool} />
      ) : (
        <EmptyWeapon label="Secondary" text={primary && primary.burden >= 2 ? `None — the ${primary.name} needs both hands` : 'None'} />
      )}
      <div className="weapon unarmed">
        <span className="weapon__text">
          <span className="k">Unarmed</span>
          <small>Strength or Finesse (GM's choice) · Melee range · {sheet.derived.proficiency}d4 physical damage</small>
        </span>
        <span className="wbtns">
          {(['strength', 'finesse'] as const).map((trait) => (
            <button key={trait} type="button" className="cost" title={`Unarmed attack with ${traitName(trait)}`} onClick={() => pool.attack({ name: 'Unarmed', slot: 'unarmed', trait, modifier: sheet.derived.traits[trait]?.value ?? 0 })}>
              {signed(sheet.derived.traits[trait]?.value ?? 0)}
              <small>{trait === 'strength' ? 'str' : 'fin'}</small>
            </button>
          ))}
          <button type="button" className="cost" title="Roll unarmed damage" onClick={() => pool.damage({ name: 'Unarmed', slot: 'unarmed', count: sheet.derived.proficiency, sides: 4, bonus: 0 })}>
            {sheet.derived.proficiency}d4
            <small>damage</small>
          </button>
        </span>
      </div>
    </div>
  );
}

/** A weapon: its name opens the details; the attack and damage chips roll straight into the dice tray. */
function WeaponRow({ sheet, weapon, slot, open, pool }: { sheet: DaggerheartSheet; weapon: SheetWeapon; slot: string; open: Open; pool?: DicePool }) {
  const attack = sheet.derived.attacks.find((candidate) => candidate.slot === slot);
  return (
    <div className="weapon">
      <button type="button" className="weapon__text" onClick={() => open({ kind: 'weapon', key: slot })}>
        <span className="k">
          {slot} · {burdenLabel(weapon.burden)}
        </span>
        <b>{weapon.name}</b>
        <small>{weaponLine(weapon, attack)}</small>
        <small className="wfeat">{weapon.feature ? `${weapon.feature.name}: ${weapon.feature.text}` : 'No feature'}</small>
      </button>
      {pool && attack && (
        <span className="wbtns">
          <button type="button" className="cost" title={`${weapon.name} attack roll`} onClick={() => pool.attack({ name: weapon.name, slot, trait: attack.trait.toLowerCase(), modifier: attack.modifier })}>
            {signed(attack.modifier)}
            <small>attack</small>
          </button>
          <button type="button" className="cost" title={`${weapon.name} damage roll`} onClick={() => pool.damage(weaponDice(sheet, weapon, slot))}>
            {attack.damage}
            <small>damage</small>
          </button>
        </span>
      )}
    </div>
  );
}

/** A weapon's damage dice for the tray: [Proficiency] dice of its die, plus its bonus. */
export function weaponDice(sheet: DaggerheartSheet, weapon: SheetWeapon, slot: string): WeaponDice {
  return { name: weapon.name, slot, count: sheet.derived.proficiency, sides: Number(weapon.damage.die.replace('d', '')) || 6, bonus: weapon.damage.bonus };
}

function EmptyWeapon({ label, text }: { label: string; text: string }) {
  return (
    <div className="weapon off">
      <span className="weapon__text">
        <span className="k">{label}</span>
        <small>{text}</small>
      </span>
    </div>
  );
}

function HopePanel({ sheet, open, act, pool }: { sheet: DaggerheartSheet; open: Open; act: Act; pool: DicePool }) {
  const hopeFeature = featuresBy(sheet, ['hope'])[0];
  const hope = sheet.trackers.hope;
  return (
    <div className="dh-frame paper vit">
      <div className="dh-title">
        <button type="button" onClick={() => open({ kind: 'hope', key: '' })}>
          <span>
            Hope <em>{hope}/{sheet.derived.maxHope}</em>
          </span>
        </button>
      </div>
      <div className="hope-row">
        <button type="button" className="pm minus" aria-label="Spend a Hope" disabled={hope === 0} onClick={() => void act({ type: 'track', track: 'hope', delta: -1 })}>
          −
        </button>
        {Array.from({ length: sheet.derived.maxHope }, (_, index) => (
          <button
            key={index}
            type="button"
            className={`diamond${index < hope ? ' on' : ''}`}
            aria-label={`Hope ${index + 1}${index < hope ? ', held' : ''}`}
            onClick={() => void act({ type: 'track', track: 'hope', delta: slotDelta(index, hope) })}
          />
        ))}
        <button type="button" className="pm" aria-label="Gain a Hope" disabled={hope >= sheet.derived.maxHope} onClick={() => void act({ type: 'track', track: 'hope', delta: 1 })}>
          +
        </button>
      </div>
      <div className="hope-note">
        Spend 1 to add an Experience{hopeFeature && ` · ${HOPE_FEATURE_COST} for ${hopeFeature.name}`}
      </div>
      <div className="dh-title">
        <span>Experience</span>
      </div>
      {sheet.experiences.map((experience, index) => {
        const picked = pool.experiences.includes(index);
        const unaffordable = !picked && pool.experiences.length >= hope;
        return (
          <button
            key={experience.name}
            type="button"
            className={`exp${picked ? ' on' : ''}`}
            aria-pressed={picked}
            disabled={unaffordable}
            title={picked ? 'Added to your next roll; click to take it off' : unaffordable ? 'Each Experience costs a Hope' : 'Add to your next roll for 1 Hope'}
            onClick={() => pool.experience(index)}
          >
            <span>{experience.name}</span>
            <b>{signed(experience.bonus)}</b>
          </button>
        );
      })}
    </div>
  );
}

function CardsTab({ sheet, open, isOpen, act }: { sheet: DaggerheartSheet; open: Open; isOpen: (detail: Detail) => boolean; act: Act }) {
  const hand = loadout(sheet);
  const stored = vault(sheet);
  const featureCard = (feature: SheetFeature) => {
    const key = String(sheet.features.indexOf(feature));
    return <FeatureCard key={key} feature={feature} selected={isOpen({ kind: 'feature', key })} onOpen={() => open({ kind: 'feature', key })} />;
  };
  return (
    <>
      <div className="dh-title">
        <span>
          Loadout <em>{hand.length} / {LOADOUT_SIZE}</em>
        </span>
      </div>
      <div className="hand">
        {hand.map((card) => (
          <DomainCard key={card.slug} card={card} selected={isOpen({ kind: 'card', key: card.slug })} onOpen={() => open({ kind: 'card', key: card.slug })} />
        ))}
        {Array.from({ length: Math.max(0, LOADOUT_SIZE - hand.length) }, (_, index) => (
          <div key={index} className="dcard is-empty">
            Empty slot
          </div>
        ))}
        <button type="button" className="dcard vault" onClick={() => open({ kind: 'vault', key: '' })}>
          <b>Vault</b>
          <small>
            {stored.length} card{stored.length === 1 ? '' : 's'}
          </small>
        </button>
      </div>
      <div className="feat-cols">
        <div>
          <div className="dh-title">
            <span>Class &amp; Subclass</span>
          </div>
          <div className="feat-grid">{featuresBy(sheet, ['hope', 'class', 'subclass', 'specialization', 'mastery', 'multiclass']).map(featureCard)}</div>
        </div>
        <div>
          <div className="dh-title">
            <span>Heritage</span>
          </div>
          <div className="feat-grid">{featuresBy(sheet, ['ancestry', 'community', 'transformation']).map(featureCard)}</div>
        </div>
      </div>
      <Counters sheet={sheet} act={act} />
    </>
  );
}

/** Class resources and anything else the player counts (Prayer Dice, Favor, a project's countdown). */
function Counters({ sheet, act }: { sheet: DaggerheartSheet; act: Act }) {
  const [name, setName] = useState('');
  const counters = sheet.counters ?? [];
  return (
    <div className="counters">
      <div className="dh-title spaced">
        <span>Counters</span>
      </div>
      {counters.length === 0 && <p className="muted-ink">Track class resources like Prayer Dice or Favor, or a project's countdown.</p>}
      {counters.map((counter, index) => (
        <div key={`${counter.name}-${index}`} className="counter">
          <b>{counter.name}</b>
          <Stepper label={counter.name} value={counter.value} min={0} max={counter.max || undefined} onChange={(value) => act({ type: 'counter', index, value })} />
          <label className="counter__max">
            max
            <input
              type="text"
              inputMode="numeric"
              aria-label={`${counter.name} maximum`}
              defaultValue={counter.max || ''}
              placeholder="—"
              onBlur={(event) => Number(event.target.value || 0) !== counter.max && void act({ type: 'counter', index, max: Number(event.target.value || 0) })}
            />
          </label>
          <button type="button" className="link-btn" onClick={() => void act({ type: 'counter', index, remove: true })}>
            remove
          </button>
        </div>
      ))}
      <div className="add-row">
        <input type="text" className="field" value={name} placeholder="e.g. Prayer Dice" aria-label="New counter" onChange={(event) => setName(event.target.value)} />
        <button type="button" className="dh-btn sm" disabled={!name.trim()} onClick={() => void act({ type: 'counter', name }).then((ok) => ok && setName(''))}>
          Add counter
        </button>
      </div>
    </div>
  );
}

export function Stepper({ label, value, min, max, onChange }: { label: string; value: number; min: number; max?: number; onChange: (value: number) => Promise<boolean> }) {
  return (
    <span className="stepper">
      <button type="button" className="pm minus" aria-label={`Less ${label}`} disabled={value <= min} onClick={() => void onChange(value - 1)}>
        −
      </button>
      <b>{max ? `${value} / ${max}` : value}</b>
      <button type="button" className="pm" aria-label={`More ${label}`} disabled={max !== undefined && value >= max} onClick={() => void onChange(value + 1)}>
        +
      </button>
    </span>
  );
}

function DomainCard({ card, selected, onOpen }: { card: SheetDomainCard; selected: boolean; onOpen: () => void }) {
  return (
    <button type="button" className={`dcard${selected ? ' is-selected' : ''}`} onClick={onOpen}>
      <span className="dcard__head" style={{ background: domainColor(card.domain) }}>
        <DomainIcon domain={card.domain} /> {card.domain} · Lv {card.level}
        <span className="recall" title="Recall Cost">
          ⟲ {card.recallCost}
        </span>
      </span>
      <span className="dcard__name">{card.name}</span>
      <span className="dcard__type">{card.type}</span>
      <span className="dcard__text">{card.text}</span>
    </button>
  );
}

function FeatureCard({ feature, selected, onOpen }: { feature: SheetFeature; selected: boolean; onOpen: () => void }) {
  return (
    <button type="button" className={`dcard feature${selected ? ' is-selected' : ''}`} onClick={onOpen}>
      <span className="dcard__head">
        {FEATURE_SOURCES[feature.source] ?? feature.source} · {feature.origin}
      </span>
      <span className="dcard__name">{feature.name}</span>
      <span className="dcard__text">{feature.text}</span>
    </button>
  );
}

function EquipmentTab({ sheet, open, act, pool }: { sheet: DaggerheartSheet; open: Open; act: Act; pool: DicePool }) {
  const { primary, secondary } = sheet.weapons;
  const [item, setItem] = useState('');
  const gold = sheet.gold;
  const setGold = (patch: Partial<typeof gold>) => act({ type: 'gold', ...gold, ...patch });
  return (
    <div className="equip">
      <div>
        <div className="dh-title">
          <span>Armor</span>
        </div>
        {sheet.armor ? (
          <button type="button" className="weapon" onClick={() => open({ kind: 'armor', key: '' })}>
            <span className="weapon__text">
              <span className="k">Tier {sheet.armor.tier}</span>
              <b>{sheet.armor.name}</b>
              <small>
                Armor Score {sheet.armor.baseScore} · base thresholds {sheet.armor.baseThresholds.major} / {sheet.armor.baseThresholds.severe}
              </small>
              <small className="wfeat">{sheet.armor.feature ? `${sheet.armor.feature.name}: ${sheet.armor.feature.text}` : 'No feature'}</small>
            </span>
          </button>
        ) : (
          <EmptyWeapon label="Armor" text="None — unarmored" />
        )}
        <div className="dh-title spaced">
          <span>Weapons</span>
        </div>
        {primary && <WeaponRow sheet={sheet} weapon={primary} slot="primary" open={open} pool={pool} />}
        {secondary && <WeaponRow sheet={sheet} weapon={secondary} slot="secondary" open={open} pool={pool} />}
        {!primary && !secondary && <EmptyWeapon label="Weapons" text="None" />}
      </div>
      <div>
        <div className="dh-title">
          <span>Inventory</span>
        </div>
        <ul className="inventory">
          {sheet.inventory.map((entry, index) => (
            <li key={`${entry.name}-${index}`}>
              <span>{entry.name}</span>
              <span className="inventory__actions">
                <Stepper label={entry.name} value={entry.quantity} min={1} onChange={(quantity) => act({ type: 'updateItem', index, quantity })} />
                <button type="button" className="link-btn" onClick={() => void act({ type: 'removeItem', index })}>
                  remove
                </button>
              </span>
            </li>
          ))}
        </ul>
        <div className="add-row">
          <input type="text" className="field" value={item} placeholder="e.g. Grappling hook" aria-label="New item" onChange={(event) => setItem(event.target.value)} />
          <button type="button" className="dh-btn sm" disabled={!item.trim()} onClick={() => void act({ type: 'addItem', name: item, quantity: 1 }).then((ok) => ok && setItem(''))}>
            Add item
          </button>
        </div>
        <div className="dh-title spaced">
          <span>Gold</span>
        </div>
        <div className="gold">
          <Gold label="Handfuls" value={gold.handfuls} onChange={(handfuls) => setGold({ handfuls })} />
          <Gold label="Bags" value={gold.bags} onChange={(bags) => setGold({ bags })} />
          <Gold label="Chests" value={gold.chests} onChange={(chests) => setGold({ chests })} />
        </div>
        <p className="gold-note">Ten handfuls make a bag; ten bags make a chest. The sheet carries them over.</p>
      </div>
    </div>
  );
}

function Gold({ label, value, onChange }: { label: string; value: number; onChange: (value: number) => Promise<boolean> }) {
  return (
    <div className="gold__box">
      <b>{value}</b>
      <span>{label}</span>
      <Stepper label={label} value={value} min={0} onChange={onChange} />
    </div>
  );
}

function DetailsTab({ sheet, act }: { sheet: DaggerheartSheet; act: Act }) {
  const questions = useClassQuestions(sheet.identity.class?.slug);
  const story = sheet.build.story ?? {};
  return (
    <div className="details">
      <Story title="Pronouns" text={sheet.build.pronouns} />
      <Story title="Description" text={story.description} />
      <Story title="Background" text={story.background} prompts={questions?.backgroundQuestions} />
      <Story title="Connections" text={story.connections} prompts={questions?.connectionQuestions} />
      <div className="story notes">
        <div className="dh-title">
          <span>Notes</span>
        </div>
        <SavedTextarea value={sheet.notes} placeholder="Session notes, names, clues…" onSave={(value) => act({ type: 'notes', value })} />
      </div>
    </div>
  );
}

function SavedTextarea({ value, placeholder, onSave }: { value: string; placeholder: string; onSave: (value: string) => Promise<boolean> }) {
  const [draft, setDraft] = useState(value);
  useEffect(() => setDraft(value), [value]);
  return <textarea className="field" rows={6} aria-label="Notes" value={draft} placeholder={placeholder} onChange={(event) => setDraft(event.target.value)} onBlur={() => draft !== value && void onSave(draft)} />;
}

function Story({ title, text, prompts }: { title: string; text?: string; prompts?: string[] }) {
  return (
    <div className="story">
      <div className="dh-title">
        <span>{title}</span>
      </div>
      {prompts && prompts.length > 0 && (
        <ul className="story__prompts">
          {prompts.map((prompt) => (
            <li key={prompt}>{prompt}</li>
          ))}
        </ul>
      )}
      <p className={text ? 'story__text' : 'story__text empty'}>{text || 'Not written yet.'}</p>
    </div>
  );
}

/** The class's background and connection questions, from the catalogue. */
function useClassQuestions(classSlug: string | undefined): Pick<ClassData, 'backgroundQuestions' | 'connectionQuestions'> | null {
  const [questions, setQuestions] = useState<Pick<ClassData, 'backgroundQuestions' | 'connectionQuestions'> | null>(null);
  useEffect(() => {
    if (!classSlug) {
      return;
    }
    let active = true;
    getCatalogue(DAGGERHEART, 'CLASS')
      .then((classes) => {
        const found = classes.find((entry) => entry.slug === classSlug)?.data as ClassData | undefined;
        if (active && found) {
          setQuestions({ backgroundQuestions: found.backgroundQuestions ?? [], connectionQuestions: found.connectionQuestions ?? [] });
        }
      })
      .catch(() => undefined);
    return () => {
      active = false;
    };
  }, [classSlug]);
  return questions;
}

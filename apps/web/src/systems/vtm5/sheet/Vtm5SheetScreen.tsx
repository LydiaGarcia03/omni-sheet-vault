import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { apiFetch } from '../../../api/client';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { renameCharacter } from '../../../characters/api';
import { CharacterPortrait } from '../../../characters/CharacterPortrait';
import type { Portrait } from '../../../characters/portrait';
import type { SheetLayoutProps } from '../../../sheet/sheetLayouts';
import { AppHeader } from '../../../shell/AppHeader';
import { Vtm5Footer } from '../Vtm5Footer';
import { GROUPS, traitsOfGroup, useVtm5Content, type Clan, type Vtm5Content } from '../content';
import { useVtm5Dice, type DicePool } from './Vtm5DiceDock';
import bloodIcon from '../vtm5-blood.png';
import '../vtm5-sheet.css';

type Track = { superficial: number; aggravated: number };
type XpEntry = { kind: 'award' | 'spend'; amount?: number; note?: string; target?: string; key?: string; label?: string; cost?: number; undone?: boolean; at?: string };
type NextCost = { attribute: Record<string, number>; skill: Record<string, number>; discipline: Record<string, number>; advantage: Record<string, number>; ritual: Record<string, number>; loresheet: Record<string, number[]>; bloodPotency: number; specialty: number };
export type Vtm5Sheet = {
  identity: Record<string, string>;
  attributes: Record<string, number>;
  skills: Record<string, number>;
  specialties: Record<string, string[]>;
  disciplines: Record<string, { dots: number; powers: string[] }>;
  rituals?: string[];
  loresheets?: Record<string, number[]>;
  thinBlood?: { merits: string[]; flaws: string[]; alchemyMethod?: string };
  inventory?: { name: string; quantity: number; notes: string }[];
  coterie?: {
    name?: string; type?: string; city?: string; tenets?: string; goals?: string; boons?: string; domainDescription?: string; domainResonance?: string;
    members: string[]; contribution: number; othersDonated: number; domain: Record<string, number>; backgrounds: { name: string; dots: number }[];
  };
  advantages: { name: string; dots: number; kind: string }[];
  predatorExtras: string;
  convictions: { conviction: string; touchstone: string }[];
  humanity: number;
  stains: number;
  bloodPotency: number;
  hunger: number;
  health: Track;
  willpower: Track;
  resonance: string | null;
  notes: string;
  xpLog: XpEntry[];
  derived: {
    healthMax: number;
    willpowerMax: number;
    bloodSurge: string;
    mend: string;
    baneSeverity: string;
    xpEarned: number;
    xpSpent: number;
    xpAvailable: number;
    clan?: Clan;
    nextCost: NextCost;
    coteriePool: number;
    coterieSpent: number;
  };
};
type Detail = { kind: string; key: string } | null;
type Act = (action: Record<string, unknown>) => Promise<void>;

const TABS = ['Disciplines', 'Merits & Flaws', 'Coterie', 'Inventory', 'Lore & Identity', 'Notes'] as const;
const IDENTITY = ['concept', 'chronicle', 'clan', 'predator', 'ambition', 'generation', 'sire', 'desire', 'age'];
const EDITABLE_IDENTITY = ['concept', 'chronicle', 'sire', 'ambition', 'desire'];

/** The VtM 5e sheet: sheet 2's layout, identity in its own tab, a detail sidebar that manages each trait. */
export function Vtm5SheetScreen({ character, userLabel }: SheetLayoutProps) {
  const content = useVtm5Content();
  const [sheet, setSheet] = useState<Vtm5Sheet | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<(typeof TABS)[number]>('Disciplines');
  const [detail, setDetail] = useState<Detail>(null);
  const [portrait, setPortrait] = useState<Portrait | null>(character.portrait ?? null);
  const [name, setName] = useState(character.name);

  const load = useCallback(() => {
    apiFetch(`/api/vtm5/characters/${character.id}/sheet`)
      .then((response) => (response.ok ? response.json() : Promise.reject(new Error('Could not load the sheet.'))))
      .then(setSheet, (cause: Error) => setError(cause.message));
  }, [character.id]);
  useEffect(load, [load]);
  const { pool, dock } = useVtm5Dice(character.id, load);

  const act: Act = async (action) => {
    const response = await apiFetch(`/api/vtm5/characters/${character.id}/actions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(action),
    });
    if (!response.ok) {
      const problem = await response.json().catch(() => null);
      setError(problem?.detail ?? 'That change could not be saved.');
      return;
    }
    setError(null);
    setSheet(await response.json());
  };

  const open = (kind: string, key: string) => setDetail({ kind, key });
  return (
    <div className="vtm5-page">
      <AppHeader userName={userLabel} backToListWithin={1220} compact systemId={character.systemId} />
      {sheet && content && (
        <div className="vtm5-sheet">
          <header className="head">
            <button type="button" className="portrait-button" title="Change portrait or name" onClick={() => open('character', '')}>
              <CharacterPortrait portrait={portrait} systemId={character.systemId} className="portrait" />
            </button>
            <div>
              <button type="button" className="head__name click" onClick={() => open('character', '')}>{name}</button>
              <div className="head__line">
                <b>{sheet.identity.clan}</b> · {sheet.identity.age} · {sheet.identity.predator} · {sheet.identity.generation} generation
              </div>
            </div>
            <div className="head__actions">
              <button type="button" className="btn" onClick={() => void pool.rouse()}>Rouse Check</button>
              <button type="button" className="btn btn--ghost" onClick={() => open('frenzy', '')}>Frenzy</button>
              <button type="button" className="xp-chip" onClick={() => open('xp', '')}>
                XP <b>{sheet.derived.xpAvailable}</b>/{sheet.derived.xpEarned}
              </button>
            </div>
          </header>
          {error && <p role="alert" className="vtm-problem">{error}</p>}

          <section className="top">
            {GROUPS.map((group) => (
              <div key={group} className="dbox">
                <div className="dbox__title">{group}</div>
                <div className="attrs">
                  {traitsOfGroup(content.attributes, group).map((name) => (
                    <div key={name} className={`abox${name in pool.traits ? ' in-pool' : ''}`}>
                      <button type="button" className="abox__name click" onClick={() => open('attr', name)}>{name}</button>
                      <button type="button" className="abox__value" title="Add to dice pool" onClick={() => pool.toggle(name, sheet.attributes[name] ?? 0)}>{sheet.attributes[name] ?? 0}</button>
                      <Dots value={sheet.attributes[name] ?? 0} />
                    </div>
                  ))}
                </div>
              </div>
            ))}
            <div className="vitals">
              <Vital title="Health" onClick={() => open('blood', 'Health')} big={`${sheet.derived.healthMax - sheet.health.superficial - sheet.health.aggravated}`} max={sheet.derived.healthMax}>
                <Boxes total={sheet.derived.healthMax} track={sheet.health} />
              </Vital>
              <Vital title="Willpower" onClick={() => open('blood', 'Willpower')} big={`${sheet.derived.willpowerMax - sheet.willpower.superficial - sheet.willpower.aggravated}`} max={sheet.derived.willpowerMax}>
                <Boxes total={sheet.derived.willpowerMax} track={sheet.willpower} />
              </Vital>
              <Vital title="Hunger" onClick={() => open('blood', 'Hunger')}>
                <div className={`hunger-blood${sheet.hunger ? '' : ' zero'}`}><img src={bloodIcon} alt="" /><span>{sheet.hunger}</span></div>
              </Vital>
              <Vital title="Humanity" onClick={() => open('blood', 'Humanity')} big={`${sheet.humanity}`}>
                <HumanityBoxes humanity={sheet.humanity} stains={sheet.stains} />
              </Vital>
            </div>
          </section>

          <section className="main">
            <div className="col">
              <div className="dbox">
                <div className="dbox__title">The Blood</div>
                <Row label="Blood Potency" value={sheet.bloodPotency} onClick={() => open('blood', 'Blood Potency')} />
                <Row label="Blood Surge" value={sheet.derived.bloodSurge} onClick={() => open('blood', 'Blood Potency')} />
                <Row label="Mend" value={`${sheet.derived.mend} superficial`} onClick={() => open('blood', 'Blood Potency')} />
                <Row label="Bane Severity" value={sheet.derived.baneSeverity} onClick={() => open('blood', 'Blood Potency')} />
                <Row label="Resonance" value={sheet.resonance || '—'} onClick={() => open('blood', 'Resonance')} />
              </div>
              {sheet.derived.clan && (
                <div className="dbox">
                  <div className="dbox__title">Bane &amp; Compulsion</div>
                  <div className="text">
                    <div className="entry"><b>{sheet.derived.clan.bane.name}</b>{sheet.derived.clan.bane.text}</div>
                    <div className="entry"><b>{sheet.derived.clan.compulsion.name}</b>{sheet.derived.clan.compulsion.text}</div>
                  </div>
                </div>
              )}
              <div className="dbox">
                <div className="dbox__title">Convictions &amp; Touchstones</div>
                <div className="text">
                  {sheet.convictions.map((c, i) => (
                    <button key={i} type="button" className="entry" onClick={() => open('conv', String(i))}>
                      <b>Conviction {i + 1}</b>
                      {c.conviction || '—'} <span className="vtm-muted">→ {c.touchstone || '—'}</span>
                    </button>
                  ))}
                  {sheet.convictions.length < 3 && (
                    <button type="button" className="entry vtm-muted" onClick={() => void act({ type: 'conviction', index: -1 }).then(() => open('conv', String(sheet.convictions.length)))}>
                      + Add a conviction
                    </button>
                  )}
                </div>
              </div>
            </div>

            <div className="dbox skills">
              <div className="dbox__title">Skills</div>
              {GROUPS.map((group) => (
                <div key={group}>
                  <div className="skills__group">{group}</div>
                  {traitsOfGroup(content.skills, group).map((name) => {
                    const value = sheet.skills[name] ?? 0;
                    return (
                      <div key={name} className={`skill${name in pool.traits ? ' in-pool' : ''}`}>
                        <Dots value={value} />
                        <button type="button" className="click" onClick={() => open('skill', name)}>
                          {name}
                          {sheet.specialties[name]?.length ? <small>{sheet.specialties[name].join(', ')}</small> : null}
                        </button>
                        <button type="button" className={`skill__rating${value ? '' : ' zero'}`} title="Add to dice pool" onClick={() => value && pool.toggle(name, value)}>{value || '–'}</button>
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>

            <div className="dbox tabbed">
              <nav className="tabs">
                {TABS.map((name) => (
                  <button key={name} type="button" className={`tab${tab === name ? ' on' : ''}`} onClick={() => setTab(name)}>
                    {name}
                  </button>
                ))}
              </nav>
              <div style={{ maxHeight: '787px', overflowY: 'auto', paddingRight: '7px', height: 'stretch' }} >
              {tab === 'Disciplines' && <DisciplinesTab sheet={sheet} content={content} open={open} pool={pool} />}
              {tab === 'Merits & Flaws' && (
                <>
                  {sheet.advantages.map((a) => (
                    <button key={a.name} type="button" className="mrow" onClick={() => open('adv', a.name)}>
                      <span><b>{a.name}</b><small>{a.kind}</small></span>
                      <Dots value={a.dots} />
                    </button>
                  ))}
                  <button type="button" className="mrow add-advantage" onClick={() => open('advAdd', '')}>
                    <span><b>+ Add a merit or background</b><small>3 XP per dot</small></span>
                  </button>
                  <div className="section">Loresheets</div>
                  {Object.entries(sheet.loresheets ?? {}).map(([name, levels]) => {
                    const loresheet = content.loresheets.find((l) => l.name === name);
                    return (
                      <button key={name} type="button" className="mrow" onClick={() => open('lore', name)}>
                        <span><b>{name}</b><small>{levels.slice().sort().map((l) => loresheet?.levels[l - 1] ?? `Level ${l}`).join(' · ')}</small></span>
                        <span className="dots">{[1, 2, 3, 4, 5].map((dot) => <i key={dot} className={`dot${levels.includes(dot) ? ' on' : ''}`} />)}</span>
                      </button>
                    );
                  })}
                  <button type="button" className="mrow add-advantage" onClick={() => open('loreAdd', '')}>
                    <span><b>+ Add a loresheet entry</b><small>its level × 3 XP</small></span>
                  </button>
                  {sheet.thinBlood && (
                    <>
                      <div className="section">Thin-blood</div>
                      {sheet.thinBlood.merits.map((m) => (
                        <div key={m} className="mrow"><span><b>{m}</b><small>{content.thinBloodMerits.find((x) => x.name === m)?.short} · merit</small></span><span /></div>
                      ))}
                      {sheet.thinBlood.flaws.map((f) => (
                        <div key={f} className="mrow"><span><b>{f}</b><small>{content.thinBloodFlaws.find((x) => x.name === f)?.short} · flaw</small></span><span /></div>
                      ))}
                    </>
                  )}
                  <div className="section">From the predator type</div>
                  <p className="tab-text">{sheet.predatorExtras}</p>
                </>
              )}
              {tab === 'Coterie' && <CoterieTab sheet={sheet} content={content} act={act} />}
              {tab === 'Inventory' && <InventoryTab sheet={sheet} act={act} />}
              {tab === 'Lore & Identity' && (
                <>
                  <div className="section">Identity</div>
                  <div className="ident-grid">
                    {IDENTITY.map((field) => (
                      <button key={field} type="button" className="ifield" onClick={() => open('ident', field)}>
                        <span>{field}</span>
                        <b>{sheet.identity[field] || '—'}</b>
                      </button>
                    ))}
                  </div>
                </>
              )}
              {tab === 'Notes' && (
                <div className="tab-text">
                  <SavedTextarea value={sheet.notes} placeholder="Session notes…" onSave={(value) => act({ type: 'notes', value })} />
                </div>
              )}
              </div>
            </div>
          </section>

          {detail && (
            <aside className="side open">
              <div className="side__nav">
                <span />
                <button type="button" onClick={() => setDetail(null)} style={{ color: 'var(--red-dark)', fontWeight: '700', fontSize: '12px' }}>✕</button>
              </div>
              <div className="side__body">
                {detail.kind === 'character' ? (
                  <>
                    <div className="side__type">Character</div>
                    <div className="side__name">{name}</div>
                    <h4>Name</h4>
                    <SavedInput
                      value={name}
                      onSave={async (value) => {
                        if (!value.trim()) return;
                        const renamed = await renameCharacter(character.id, value.trim()).catch(() => null);
                        if (renamed) setName(renamed.name);
                        else setError('Could not rename the character.');
                      }}
                    />
                    <h4>Portrait</h4>
                    <div className="vtm5-portrait">
                      <PortraitPicker characterId={character.id} systemId={character.systemId} portrait={portrait} onChange={setPortrait} />
                    </div>
                  </>
                ) : (
                  <DetailBody detail={detail} sheet={sheet} content={content} act={act} open={open} rouse={() => pool.rouse()} dice={pool} />
                )}
              </div>
            </aside>
          )}
        </div>
      )}
      {!sheet && error && <p role="alert" className="vtm-problem">{error}</p>}
      {sheet && <div className="vtm5-sheet vtm5-dock-host">{dock(sheet.hunger)}</div>}
      <Vtm5Footer />
    </div>
  );
}

function DisciplinesTab({ sheet, content, open, pool }: { sheet: Vtm5Sheet; content: Vtm5Content; open: (kind: string, key: string) => void; pool: DicePool }) {
  return (
    <>
      {Object.entries(sheet.disciplines).map(([name, discipline]) => (
        <div key={name}>
          <div className="section">
            <button type="button" className="click" onClick={() => open('disc', name)}>{name}</button>
            <Dots value={discipline.dots} />
            <button type="button" className="click vtm-muted" style={{ marginLeft: 'auto', textTransform: 'uppercase', fontSize: '12px' }} onClick={() => pool.toggle(name, discipline.dots)}>add {discipline.dots} to pool</button>
          </div>
          <div className="table-head"><span>Power</span><span>Cost</span><span>Pool</span><span>Lvl</span></div>
          {(content.disciplines[name]?.powers ?? []).filter((p) => discipline.powers.includes(p.name)).map((p) => (
            <div key={p.name} className="prow">
              <button type="button" className="click" onClick={() => open('power', `${name}|${p.name}`)}><b>{p.name}</b><small>{p.summary}</small></button>
              <span>{p.cost}</span>
              {p.pool === '—' ? <span className="vtm-muted">—</span> : <button type="button" className="pool-btn" onClick={() => void pool.rollNow(p.pool.split(' + '), p.name)}>{p.pool}</button>}
              <span>{p.level}</span>
            </div>
          ))}
        </div>
      ))}
      {(sheet.rituals?.length ?? 0) > 0 && (
        <>
          <div className="section">Rituals</div>
          {content.rituals.filter((r) => sheet.rituals?.includes(r.name)).map((r) => (
            <div key={r.name} className="prow">
              <span><b>{r.name}</b><small>{r.summary}</small></span>
              <span>One Rouse Check</span>
              <span className="red">Intelligence + Blood Sorcery</span>
              <span>{r.level}</span>
            </div>
          ))}
        </>
      )}
      <p className="vtm-muted">Open a discipline to raise it with XP or learn a power for a new dot.</p>
    </>
  );
}

function DetailBody({ detail, sheet, content, act, open, rouse, dice }: { detail: NonNullable<Detail>; sheet: Vtm5Sheet; content: Vtm5Content; act: Act; open: (kind: string, key: string) => void; rouse: () => Promise<void>; dice: DicePool }) {
  const { kind, key } = detail;
  const available = sheet.derived.xpAvailable;
  const head = (type: string, name: string, lead: string) => (
    <>
      <div className="side__type">{type}</div>
      <div className="side__name">{name}</div>
      <p className="side__lead">{lead}</p>
    </>
  );
  const raise = (target: string, traitKey: string, value: number, cost: number) => (
    <>
      <div className="rating">
        <Dots value={value} />
        <button type="button" className="step" disabled={!cost || cost > available} onClick={() => void act({ type: 'spendXp', target, key: traitKey })}>+</button>
        <span className="rating__value">{value || '--'}</span>
      </div>
      <div className="cost">
        {cost ? <>Next dot: <b>{cost} XP</b> · you have {available}</> : 'Maximum rating'} ·{' '}
        <button type="button" className="red" onClick={() => open('xp', '')}>undo in XP history</button>
      </div>
    </>
  );

  if (kind === 'attr' || kind === 'skill') {
    const isAttribute = kind === 'attr';
    const trait = (isAttribute ? content.attributes : content.skills)[key];
    const value = (isAttribute ? sheet.attributes : sheet.skills)[key] ?? 0;
    const scale = isAttribute ? content.attributeScale : content.skillScale;
    const cost = (isAttribute ? sheet.derived.nextCost.attribute : sheet.derived.nextCost.skill)[key] ?? 0;
    return (
      <>
        {head(`${trait.group} ${isAttribute ? 'attribute' : 'skill'}`, key, trait.short)}
        {raise(isAttribute ? 'attribute' : 'skill', key, value, cost)}
        {trait.uses && (<><h4>Used for</h4><p>{trait.uses}</p></>)}
        {!isAttribute && <Specialties skill={key} sheet={sheet} ideas={trait.specialties ?? []} act={act} />}
        <h4>What each dot means</h4>
        <table className="levels"><tbody>{scale.map((text, i) => <tr key={text} className={i + 1 === value ? 'current' : undefined}><td>{'●'.repeat(i + 1)}</td><td>{text}</td></tr>)}</tbody></table>
      </>
    );
  }
  if (kind === 'disc' || kind === 'power') {
    const [name, powerName] = key.split('|');
    const discipline = content.disciplines[name];
    const power = discipline.powers.find((p) => p.name === powerName);
    if (power) {
      return (
        <>
          {head(`${name} ${'●'.repeat(power.level)}`, power.name, power.summary)}
          <table className="levels"><tbody><tr><td>Cost</td><td>{power.cost}</td></tr><tr><td>Pool</td><td>{power.pool}</td></tr></tbody></table>
        </>
      );
    }
    const owned = sheet.disciplines[name] ?? { dots: 0, powers: [] };
    const inClan = sheet.derived.clan?.disciplines.includes(name);
    return (
      <>
        {head(`${discipline.type} discipline · ${inClan ? 'in-clan' : 'out-of-clan'}`, name, discipline.short)}
        {raise('discipline', name, owned.dots, sheet.derived.nextCost.discipline[name] ?? 0)}
        {!inClan && <p className="warn-note">Out-of-clan: learning it calls for a taste of the blood of someone who has it. The Storyteller decides.</p>}
        <p className={`warn-note${sheet.resonance === discipline.resonance ? ' met' : ''}`}>
          Raising it calls for drinking {discipline.resonance} blood{sheet.resonance === discipline.resonance ? ' — your last feeding matches' : ''}. A reminder, not a block.
        </p>
        {name === 'Thin-Blood Alchemy' && sheet.thinBlood?.alchemyMethod && (
          <>
            <h4>Distill</h4>
            <p>Method: <b>{sheet.thinBlood.alchemyMethod}</b>. One Rouse Check, then roll; 2, 4 or 6 successes brew a weak, normal or strong dose.</p>
            <div className="side-actions">
              <button type="button" className="btn" onClick={() => {
                const attribute = content.alchemyMethods.find((m) => m.name === sheet.thinBlood?.alchemyMethod)?.attribute ?? 'Intelligence';
                void dice.rollNow([attribute, 'Thin-Blood Alchemy'], 'Distill');
              }}>Roll distillation</button>
              <button type="button" className="btn btn--ghost" onClick={() => void rouse()}>Rouse Check</button>
            </div>
          </>
        )}
        {name === 'Blood Sorcery' && owned.dots > 0 && (
          <>
            <h4>Rituals</h4>
            {content.rituals.map((r) => {
              const known = sheet.rituals?.includes(r.name);
              const ritualCost = sheet.derived.nextCost.ritual?.[r.name] ?? 0;
              return (
                <div key={r.name} className="spec-row">
                  <span>{'●'.repeat(r.level)} {r.name} <span className="vtm-muted">— {r.summary}</span></span>
                  {known ? <span className="red">known</span> : ritualCost ? (
                    <button type="button" className="red" disabled={ritualCost > available} onClick={() => void act({ type: 'spendXp', target: 'ritual', key: r.name })}>learn · {ritualCost} XP</button>
                  ) : <span>—</span>}
                </div>
              );
            })}
          </>
        )}
        <h4>Powers · {owned.powers.length}/{owned.dots}</h4>
        {discipline.powers.map((p) => {
          const known = owned.powers.includes(p.name);
          const learnable = !known && p.level <= owned.dots && owned.powers.length < owned.dots;
          return (
            <div key={p.name} className="spec-row">
              <span>{'●'.repeat(p.level)} {p.name} <span className="vtm-muted">— {p.summary}</span></span>
              {known ? <span className="red">known</span> : learnable ? <button type="button" className="red" onClick={() => void act({ type: 'learnPower', discipline: name, power: p.name })}>learn</button> : <span>—</span>}
            </div>
          );
        })}
      </>
    );
  }
  if (kind === 'blood') {
    return <TrackerDetail name={key} sheet={sheet} content={content} act={act} head={head} rouse={rouse} remorse={dice.remorse} />;
  }
  if (kind === 'adv') {
    const advantage = content.advantages.find((a) => a.name === key);
    const owned = sheet.advantages.find((a) => a.name === key)?.dots ?? 0;
    const cost = sheet.derived.nextCost.advantage?.[key] ?? 0;
    return (
      <>
        {head(advantage?.kind ?? 'Advantage', key, advantage?.short ?? 'From the predator type.')}
        {advantage && advantage.kind !== 'Flaw' ? (
          raise('advantage', key, owned, cost)
        ) : (
          <div className="rating"><Dots value={owned} /><span className="rating__value">{owned}</span></div>
        )}
        {advantage?.kind === 'Flaw' && <div className="hint">Flaws come from creation and the Storyteller, not from XP</div>}
      </>
    );
  }
  if (kind === 'advAdd') {
    const buyable = content.advantages.filter((a) => a.kind !== 'Flaw' && !sheet.advantages.some((owned) => owned.name === a.name));
    return (
      <>
        {head('Advantages', 'Add a merit or background', 'Backgrounds are what you have; merits are traits. Buying the first dot spends XP right away.')}
        {buyable.map((a) => {
          const cost = sheet.derived.nextCost.advantage?.[a.name] ?? 0;
          return (
            <div key={a.name} className="spec-row">
              <span><b>{a.name}</b> <span className="vtm-muted">— {a.kind} · {a.short}</span></span>
              <button type="button" className="red" disabled={!cost || cost > available} onClick={() => void act({ type: 'spendXp', target: 'advantage', key: a.name }).then(() => open('adv', a.name))}>
                buy · {cost} XP
              </button>
            </div>
          );
        })}
      </>
    );
  }
  if (kind === 'ident') {
    const label = key.charAt(0).toUpperCase() + key.slice(1);
    return (
      <>
        {head('Identity', label, content.identity[label] ?? '')}
        {EDITABLE_IDENTITY.includes(key) ? (
          <SavedInput value={sheet.identity[key] ?? ''} onSave={(value) => act({ type: 'identity', field: key, value })} />
        ) : (
          <>
            <p>{sheet.identity[key] || '—'}</p>
            <div className="hint">Set at creation</div>
          </>
        )}
      </>
    );
  }
  if (kind === 'conv') {
    const index = Number(key);
    const conviction = sheet.convictions[index] ?? { conviction: '', touchstone: '' };
    return (
      <>
        {head('Conviction', `Conviction ${index + 1}`, content.convictionHelp)}
        <h4>Conviction</h4>
        <SavedInput value={conviction.conviction} onSave={(value) => act({ type: 'conviction', index, conviction: value })} />
        <h4>Touchstone</h4>
        <SavedInput value={conviction.touchstone} onSave={(value) => act({ type: 'conviction', index, touchstone: value })} />
        {sheet.convictions.length > 1 && (
          <div className="side-actions">
            <button type="button" className="btn btn--ghost" onClick={() => void act({ type: 'conviction', index, remove: true })}>Remove</button>
          </div>
        )}
      </>
    );
  }
  if (kind === 'lore' || kind === 'loreAdd') {
    const shown = kind === 'lore' ? content.loresheets.filter((l) => l.name === key) : content.loresheets.filter((l) => (sheet.derived.nextCost.loresheet?.[l.name] ?? []).some((c) => c > 0));
    return (
      <>
        {head('Loresheet', kind === 'lore' ? key : 'Add a loresheet entry', 'Each dot is its own entry, bought separately: it doesn\'t give the lower ones. The Storyteller decides which loresheets the chronicle allows.')}
        {shown.map((l) => (
          <div key={l.name}>
            {kind === 'loreAdd' && <h4>{l.name}{l.restriction ? ` · ${l.restriction}` : ''}</h4>}
            {l.levels.map((entry, index) => {
              const owned = sheet.loresheets?.[l.name]?.includes(index + 1);
              const cost = sheet.derived.nextCost.loresheet?.[l.name]?.[index] ?? 0;
              return (
                <div key={entry} className="spec-row">
                  <span>{'●'.repeat(index + 1)} {entry}</span>
                  {owned ? <span className="red">owned</span> : cost ? (
                    <button type="button" className="red" disabled={cost > available} onClick={() => void act({ type: 'spendXp', target: 'loresheet', key: l.name, level: index + 1 })}>buy · {cost} XP</button>
                  ) : <span>—</span>}
                </div>
              );
            })}
          </div>
        ))}
      </>
    );
  }
  if (kind === 'frenzy') {
    return <FrenzyPanel sheet={sheet} dice={dice} head={head} />;
  }
  if (kind === 'xp') {
    return <XpPanel sheet={sheet} act={act} head={head} />;
  }
  return null;
}

function CoterieTab({ sheet, content, act }: { sheet: Vtm5Sheet; content: Vtm5Content; act: Act }) {
  const coterie = sheet.coterie ?? { members: [], contribution: 0, othersDonated: 0, domain: {}, backgrounds: [] };
  const [member, setMember] = useState('');
  const save = (patch: Record<string, unknown>) => act({ type: 'coterie', ...patch });
  const left = sheet.derived.coteriePool - sheet.derived.coterieSpent;
  const text = (field: 'name' | 'city' | 'tenets' | 'goals' | 'boons' | 'domainDescription' | 'domainResonance', label: string, hint: string) => (
    <div className="coterie-field">
      <span>{label}</span>
      <SavedInput value={coterie[field] ?? ''} onSave={(value) => save({ [field]: value })} />
      <small>{hint}</small>
    </div>
  );
  const backgroundNames = content.advantages.filter((a) => a.kind === 'Background').map((a) => a.name);
  const setBackground = (name: string, dots: number) => {
    const others = coterie.backgrounds.filter((b) => b.name !== name);
    return save({ backgrounds: dots > 0 ? [...others, { name, dots }] : others });
  };
  return (
    <>
      <div className="section">Coterie</div>
      <div className="coterie-grid">
        {text('name', 'Name', 'What the coterie calls itself.')}
        <div className="coterie-field">
          <span>Type</span>
          <select value={coterie.type ?? ''} onChange={(event) => void save({ type: event.target.value })}>
            <option value="">—</option>
            {content.coterieTypes.map((type) => <option key={type}>{type}</option>)}
          </select>
          <small>Its role in the city.</small>
        </div>
        {text('city', 'City', 'Where it hunts and schemes.')}
        {text('tenets', 'Tenets', 'The rules everyone in it keeps.')}
        {text('goals', 'Goals', 'What it is working toward.')}
        {text('boons', 'Boons & debts', 'Who owes the coterie, and whom it owes.')}
      </div>

      <div className="section">Members <span className="vtm-muted" style={{ fontSize: '11px' }}>· each gives one dot</span></div>
      {coterie.members.map((name, index) => (
        <div key={name + index} className="spec-row">
          <span>{name}</span>
          <button type="button" className="red" onClick={() => void save({ members: coterie.members.filter((_, i) => i !== index) })}>remove</button>
        </div>
      ))}
      <div className="add-row">
        <input type="text" value={member} placeholder="Another member's character" onChange={(event) => setMember(event.target.value)} />
        <button type="button" className="btn" disabled={!member.trim()} onClick={() => void save({ members: [...coterie.members, member] }).then(() => setMember(''))}>Add member</button>
      </div>

      <div className="section">Pool · {sheet.derived.coterieSpent}/{sheet.derived.coteriePool} dots</div>
      <p className="tab-text vtm-muted">
        One dot from {sheet.coterie ? 'each of the ' + (coterie.members.length + 1) : 'every'} member{coterie.members.length ? 's' : ''}, {coterie.contribution} you gave at creation, and what the others gave:
      </p>
      <Stepper value={coterie.othersDonated} min={0} max={99} onChange={(value) => save({ othersDonated: value })} />

      <div className="section">Domain</div>
      {Object.entries(content.domain).map(([trait, short]) => (
        <div key={trait} className="mrow">
          <span><b>{trait}</b><small>{short}</small></span>
          <span className="dots pick">
            {[1, 2, 3, 4, 5].map((dot) => {
              const value = coterie.domain[trait] ?? 0;
              const next = value === dot ? dot - 1 : dot;
              return <i key={dot} className={`dot${dot <= value ? ' on' : ''}${next - value > left ? ' locked' : ''}`} onClick={() => next - value <= left && void save({ domain: { [trait]: next } })} />;
            })}
          </span>
        </div>
      ))}
      {text('domainDescription', 'Description', 'The streets, the buildings, the feeding grounds.')}
      {text('domainResonance', 'Resonance', 'The emotion most blood there carries.')}

      <div className="section">Coterie backgrounds</div>
      {backgroundNames.map((name) => {
        const value = coterie.backgrounds.find((b) => b.name === name)?.dots ?? 0;
        return (
          <div key={name} className="mrow">
            <span><b>{name}</b><small>{content.advantages.find((a) => a.name === name)?.short}</small></span>
            <span className="dots pick">
              {[1, 2, 3, 4, 5].map((dot) => {
                const next = value === dot ? dot - 1 : dot;
                return <i key={dot} className={`dot${dot <= value ? ' on' : ''}${next - value > left ? ' locked' : ''}`} onClick={() => next - value <= left && void setBackground(name, next)} />;
              })}
            </span>
          </div>
        );
      })}
    </>
  );
}

function InventoryTab({ sheet, act }: { sheet: Vtm5Sheet; act: Act }) {
  const [name, setName] = useState('');
  const items = sheet.inventory ?? [];
  return (
    <>
      {items.length === 0 && <p className="tab-text vtm-muted">Nothing carried yet.</p>}
      {items.map((item, index) => (
        <div key={index} className="mrow inventory-row">
          <span>
            <b>{item.name}</b>
            <SavedInput value={item.notes} onSave={(notes) => act({ type: 'updateItem', index, notes })} />
          </span>
          <span className="inventory-row__actions">
            <button type="button" className="step" disabled={item.quantity <= 1} onClick={() => void act({ type: 'updateItem', index, quantity: item.quantity - 1 })}>−</button>
            <b>{item.quantity}</b>
            <button type="button" className="step" onClick={() => void act({ type: 'updateItem', index, quantity: item.quantity + 1 })}>+</button>
            <button type="button" className="red" onClick={() => void act({ type: 'removeItem', index })}>remove</button>
          </span>
        </div>
      ))}
      <div className="add-row spaced">
        <input type="text" value={name} placeholder="e.g. Restoration kit" onChange={(event) => setName(event.target.value)} />
        <button type="button" className="btn" disabled={!name.trim()} onClick={() => void act({ type: 'addItem', name, quantity: 1 }).then(() => setName(''))}>Add item</button>
      </div>
    </>
  );
}

function FrenzyPanel({ sheet, dice, head }: { sheet: Vtm5Sheet; dice: DicePool; head: (type: string, name: string, lead: string) => ReactNode }) {
  const [difficulty, setDifficulty] = useState(3);
  const willpower = sheet.derived.willpowerMax - sheet.willpower.superficial - sheet.willpower.aggravated;
  return (
    <>
      {head('The Beast', 'Frenzy check', 'When the Beast pushes to take over: roll current Willpower plus a third of Humanity, no Hunger dice. Fail and you frenzy.')}
      <div className="cost">Pool: <b>{Math.max(1, willpower + Math.floor(sheet.humanity / 3))} dice</b> · Willpower {willpower} + Humanity {sheet.humanity} ÷ 3</div>
      <h4>Difficulty</h4>
      <div className="rating">
        <button type="button" className="step" disabled={difficulty <= 1} onClick={() => setDifficulty(difficulty - 1)}>−</button>
        <span className="rating__value" style={{ margin: '0 auto' }}>{difficulty}</span>
        <button type="button" className="step" onClick={() => setDifficulty(difficulty + 1)}>+</button>
      </div>
      <div className="side-actions"><button type="button" className="btn" onClick={() => void dice.frenzy(difficulty)}>Roll frenzy check</button></div>
      <div className="hint">The result goes to the roll log</div>
    </>
  );
}

function TrackerDetail({ name, sheet, content, act, head, rouse, remorse }: { name: string; sheet: Vtm5Sheet; content: Vtm5Content; act: Act; head: (type: string, name: string, lead: string) => ReactNode; rouse: () => Promise<void>; remorse: () => Promise<void> }) {
  const lead = content.blood[name] ?? '';
  if (name === 'Health' || name === 'Willpower') {
    const track = name.toLowerCase();
    const values = name === 'Health' ? sheet.health : sheet.willpower;
    const max = name === 'Health' ? sheet.derived.healthMax : sheet.derived.willpowerMax;
    const damage = (kind: string, amount: number) => void act({ type: 'damage', track, kind, amount });
    return (
      <>
        {head('Tracker', name, lead)}
        <div className="track-edit"><Boxes total={max} track={values} /></div>
        <div className="side-actions">
          <button type="button" className="btn btn--ghost" onClick={() => damage('superficial', 1)}>+ Superficial /</button>
          <button type="button" className="btn btn--ghost" onClick={() => damage('aggravated', 1)}>+ Aggravated X</button>
          <button type="button" className="btn btn--ghost" onClick={() => damage('superficial', -1)}>− /</button>
          <button type="button" className="btn btn--ghost" onClick={() => damage('aggravated', -1)}>− X</button>
        </div>
        <div className="hint">When the track is full, new Superficial damage becomes Aggravated</div>
      </>
    );
  }
  if (name === 'Hunger') {
    return (
      <>
        {head('Tracker', name, lead)}
        <Stepper value={sheet.hunger} min={0} max={5} onChange={(value) => act({ type: 'hunger', value })} />
        <div className="side-actions"><button type="button" className="btn" onClick={() => void rouse()}>Rouse Check</button></div>
        <div className="hint">6 or more passes; anything else raises Hunger by one</div>
      </>
    );
  }
  if (name === 'Humanity') {
    return (
      <>
        {head('Tracker', name, lead)}
        <Stepper value={sheet.humanity} min={0} max={10} onChange={(value) => act({ type: 'humanity', value })} />
        <h4>Stains</h4>
        <Stepper value={sheet.stains} min={0} max={10 - sheet.humanity} onChange={(value) => act({ type: 'stains', value })} />
        <div className="side-actions">
          <button type="button" className="btn" disabled={sheet.stains === 0} onClick={() => void remorse()}>Roll Remorse</button>
        </div>
        <div className="hint">End of session: one die per empty, unstained box. Any success keeps Humanity; none loses a dot. Stains clear.</div>
      </>
    );
  }
  if (name === 'Resonance') {
    return (
      <>
        {head('The Blood', name, lead)}
        <select value={sheet.resonance ?? ''} onChange={(event) => void act({ type: 'resonance', value: event.target.value })}>
          <option value="">—</option>
          {Object.keys(content.resonances).map((resonance) => <option key={resonance}>{resonance}</option>)}
        </select>
        {sheet.resonance && <p>Boosts {content.resonances[sheet.resonance]?.join(' and ')} when Intense or Acute.</p>}
      </>
    );
  }
  const cost = sheet.derived.nextCost.bloodPotency;
  return (
    <>
      {head('The Blood', 'Blood Potency', lead)}
      <div className="rating">
        <Dots value={sheet.bloodPotency} />
        <button type="button" className="step" disabled={!cost || cost > sheet.derived.xpAvailable} onClick={() => void act({ type: 'spendXp', target: 'bloodPotency', key: 'Blood Potency' })}>+</button>
        <span className="rating__value">{sheet.bloodPotency}</span>
      </div>
      <div className="cost">{cost ? <>Next dot: <b>{cost} XP</b></> : 'Maximum'}</div>
      <table className="levels">
        <tbody>
          <tr><th>BP</th><th>Surge</th><th>Mend</th><th>Bane</th></tr>
          {content.bpTable.slice(0, 6).map((row, i) => (
            <tr key={i} className={i === sheet.bloodPotency ? 'current' : undefined}><td>{i}</td><td>{row[0]}</td><td>{row[1]}</td><td>{row[4]}</td></tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

function Specialties({ skill, sheet, ideas, act }: { skill: string; sheet: Vtm5Sheet; ideas: string[]; act: Act }) {
  const [name, setName] = useState('');
  const owned = sheet.specialties[skill] ?? [];
  const canAdd = (sheet.skills[skill] ?? 0) > owned.length && sheet.derived.xpAvailable >= sheet.derived.nextCost.specialty;
  return (
    <>
      <h4>Specialties</h4>
      {owned.length ? owned.map((s) => <div key={s} className="spec-row"><span>{s} <span className="vtm-muted">+1 die when it applies</span></span></div>) : <p className="vtm-muted">None yet.</p>}
      {(sheet.skills[skill] ?? 0) > 0 && (
        <>
          <div className="add-row">
            <input type="text" value={name} placeholder={ideas[0] ? `e.g. ${ideas[0]}` : 'Specialty'} onChange={(event) => setName(event.target.value)} />
            <button type="button" className="btn" disabled={!canAdd || !name.trim()} onClick={() => void act({ type: 'spendXp', target: 'specialty', key: skill, specialty: name }).then(() => setName(''))}>
              Add · {sheet.derived.nextCost.specialty} XP
            </button>
          </div>
          <div className="hint">One specialty per dot{ideas.length ? ` · ideas: ${ideas.join(', ')}` : ''}</div>
        </>
      )}
    </>
  );
}

function XpPanel({ sheet, act, head }: { sheet: Vtm5Sheet; act: Act; head: (type: string, name: string, lead: string) => ReactNode }) {
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');
  const canUndo = (index: number) => {
    const entry = sheet.xpLog[index];
    return entry.kind === 'spend' && !entry.undone && !sheet.xpLog.slice(index + 1).some((later) => later.kind === 'spend' && !later.undone && later.key === entry.key);
  };
  return (
    <>
      {head('Character', 'Experience', 'Earn XP each session and story, then spend it one dot at a time from any trait. Undo the latest spend on a trait to refund it.')}
      <table className="levels">
        <tbody>
          <tr><td>Earned</td><td>{sheet.derived.xpEarned}</td></tr>
          <tr><td>Spent</td><td>{sheet.derived.xpSpent}</td></tr>
          <tr className="current"><td>Available</td><td>{sheet.derived.xpAvailable}</td></tr>
        </tbody>
      </table>
      <h4>Add XP</h4>
      <div className="add-row">
        <input type="text" inputMode="numeric" value={amount} placeholder="1" style={{ maxWidth: 56 }} onChange={(event) => setAmount(event.target.value)} />
        <input type="text" value={note} placeholder="Session 4" onChange={(event) => setNote(event.target.value)} />
        <button type="button" className="btn" disabled={!(Number(amount) > 0)} onClick={() => void act({ type: 'awardXp', amount: Number(amount), note }).then(() => { setAmount(''); setNote(''); })}>
          Add
        </button>
      </div>
      <div className="hint">Usually 1 per session and 1 per finished story — the Storyteller's call</div>
      <h4>History</h4>
      {sheet.xpLog.length === 0 && <p className="vtm-muted">Nothing yet.</p>}
      {sheet.xpLog.map((entry, index) => ({ entry, index })).reverse().map(({ entry, index }) => (
        <div key={index} className="spec-row">
          {entry.kind === 'award' ? (
            <span><b className="red">+{entry.amount}</b> {entry.note}</span>
          ) : (
            <span style={entry.undone ? { textDecoration: 'line-through', opacity: 0.5 } : undefined}>{entry.label} <span className="vtm-muted">· −{entry.cost}</span></span>
          )}
          {entry.kind === 'spend' && (entry.undone ? <span>undone</span> : canUndo(index) ? <button type="button" className="red" onClick={() => void act({ type: 'undoXp', index })}>undo</button> : <span title="A later spend on the same trait comes first">—</span>)}
        </div>
      ))}
    </>
  );
}

function SavedInput({ value, onSave }: { value: string; onSave: (value: string) => Promise<void> }) {
  const [draft, setDraft] = useState(value);
  useEffect(() => setDraft(value), [value]);
  return <input type="text" value={draft} onChange={(event) => setDraft(event.target.value)} onBlur={() => draft !== value && void onSave(draft)} />;
}

function SavedTextarea({ value, placeholder, onSave }: { value: string; placeholder: string; onSave: (value: string) => Promise<void> }) {
  const [draft, setDraft] = useState(value);
  useEffect(() => setDraft(value), [value]);
  return <textarea value={draft} placeholder={placeholder} onChange={(event) => setDraft(event.target.value)} onBlur={() => draft !== value && void onSave(draft)} />;
}

function Stepper({ value, min, max, onChange }: { value: number; min: number; max: number; onChange: (value: number) => Promise<void> }) {
  return (
    <div className="rating">
      <button type="button" className="step" disabled={value <= min} onClick={() => void onChange(value - 1)}>−</button>
      <span className="rating__value" style={{ margin: '0 auto' }}>{value}</span>
      <button type="button" className="step" disabled={value >= max} onClick={() => void onChange(value + 1)}>+</button>
    </div>
  );
}

function HumanityBoxes({ humanity, stains }: { humanity: number; stains: number }) {
  return (
    <span className="boxes humanity">
      {Array.from({ length: 10 }, (_, i) => <i key={i} className={`box${i < humanity ? ' filled' : i >= 10 - stains ? ' superficial' : ''}`} />)}
    </span>
  );
}

function Dots({ value }: { value: number }) {
  return (
    <span className="dots">
      {[1, 2, 3, 4, 5].map((dot) => <i key={dot} className={`dot${dot <= value ? ' on' : ''}`} />)}
    </span>
  );
}

function Boxes({ total, track }: { total: number; track: Track }) {
  return (
    <span className="boxes">
      {Array.from({ length: total }, (_, i) => (
        <i key={i} className={`box${i < track.aggravated ? ' aggravated' : i < track.aggravated + track.superficial ? ' superficial' : ''}`} />
      ))}
    </span>
  );
}

function Vital({ title, big, max, onClick, children }: { title: string; big?: string; max?: number; onClick: () => void; children: ReactNode }) {
  return (
    <button type="button" className="dbox vbox" onClick={onClick}>
      <div className="dbox__title">{title}</div>
      {big !== undefined && <div className="vbox__big">{big}{max !== undefined && <small>/{max}</small>}</div>}
      {children}
    </button>
  );
}

function Row({ label, value, onClick }: { label: string; value: ReactNode; onClick: () => void }) {
  return (
    <button type="button" className="kv" onClick={onClick}>
      <span>{label}</span>
      <b>{value}</b>
    </button>
  );
}

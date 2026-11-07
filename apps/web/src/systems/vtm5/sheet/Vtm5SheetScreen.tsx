import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { apiFetch } from '../../../api/client';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { renameCharacter } from '../../../characters/api';
import { CharacterPortrait } from '../../../characters/CharacterPortrait';
import type { Portrait } from '../../../characters/portrait';
import { useLanguage } from '../../../language/language';
import type { SheetLayoutProps } from '../../../sheet/sheetLayouts';
import { AppHeader } from '../../../shell/AppHeader';
import { Vtm5Footer } from '../Vtm5Footer';
import { GROUPS, poolOf, termOf, traitsOfGroup, useVtm5Content, type Clan, type Vtm5Content } from '../content';
import { useVtm5Text } from '../locales/vtm5Messages';
import { useGeneration } from '../builder/vtm5Build';
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
type Head = (type: string, name: string, lead: string) => ReactNode;

const TABS = ['disciplines', 'merits', 'coterie', 'inventory', 'lore', 'notes'] as const;
/** Identity fields, with the English content name that labels each one. */
const IDENTITY: [string, string][] = [
  ['concept', 'Concept'], ['chronicle', 'Chronicle'], ['clan', 'Clan'], ['predator', 'Predator'], ['ambition', 'Ambition'],
  ['generation', 'Generation'], ['sire', 'Sire'], ['desire', 'Desire'], ['age', 'Age'],
];
const EDITABLE_IDENTITY = ['concept', 'chronicle', 'sire', 'ambition', 'desire'];
/** Identity fields whose value is a content name (shown translated); the others are the player's own text. */
const NAMED_IDENTITY = ['clan', 'predator', 'age'];

/** The VtM 5e sheet: sheet 2's layout, identity in its own tab, a detail sidebar that manages each trait. */
export function Vtm5SheetScreen({ character, userLabel }: SheetLayoutProps) {
  const t = useVtm5Text();
  const { locale } = useLanguage();
  const content = useVtm5Content();
  const [sheet, setSheet] = useState<Vtm5Sheet | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<(typeof TABS)[number]>('disciplines');
  const [detail, setDetail] = useState<Detail>(null);
  const [portrait, setPortrait] = useState<Portrait | null>(character.portrait ?? null);
  const [name, setName] = useState(character.name);
  const loadFailed = t('sheet.loadFailed');

  const load = useCallback(() => {
    apiFetch(`/api/vtm5/characters/${character.id}/sheet`)
      .then((response) => (response.ok ? response.json() : Promise.reject(new Error(loadFailed))))
      .then(setSheet, (cause: Error) => setError(cause.message));
  }, [character.id, loadFailed, locale]);
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
      setError(problem?.detail ?? t('sheet.saveFailed'));
      return;
    }
    setError(null);
    setSheet(await response.json());
  };

  const open = (kind: string, key: string) => setDetail({ kind, key });
  const term = (key: string) => (content ? termOf(content, key) : key);
  const generationOf = useGeneration();
  return (
    <div className="vtm5-page">
      <AppHeader userName={userLabel} backToListWithin={1220} compact systemId={character.systemId} />
      {sheet && content && (
        <div className={`vtm5-sheet${detail ? ' side-open' : ''}`}>
          <header className="head">
            <button type="button" className="portrait-button" title={t('sheet.changePortrait')} onClick={() => open('character', '')}>
              <CharacterPortrait portrait={portrait} systemId={character.systemId} className="portrait" />
            </button>
            <div>
              <button type="button" className="head__name click" onClick={() => open('character', '')}>{name}</button>
              <div className="head__line">
                <b>{term(sheet.identity.clan)}</b> · {term(sheet.identity.age)} · {term(sheet.identity.predator)} · {t('sheet.generationLine', { generation: generationOf(sheet.identity.generation) })}
              </div>
            </div>
            <div className="head__actions">
              <button type="button" className="btn" onClick={() => void pool.rouse()}>{t('terms.rouseCheck')}</button>
              <button type="button" className="btn btn--ghost" onClick={() => open('frenzy', '')}>{t('terms.frenzy')}</button>
              <button type="button" className="xp-chip" onClick={() => open('xp', '')}>
                XP <b>{sheet.derived.xpAvailable}</b>/{sheet.derived.xpEarned}
              </button>
            </div>
          </header>
          {error && <p role="alert" className="vtm-problem">{error}</p>}

          <section className="top">
            {GROUPS.map((group) => (
              <div key={group} className="dbox">
                <div className="dbox__title">{term(group)}</div>
                <div className="attrs">
                  {traitsOfGroup(content.attributes, group).map((trait) => (
                    <div key={trait} className={`abox${trait in pool.traits ? ' in-pool' : ''}`}>
                      <button type="button" className="abox__name click" onClick={() => open('attr', trait)}>{term(trait)}</button>
                      <button type="button" className="abox__value" title={t('sheet.addToPool')} onClick={() => pool.toggle(trait, sheet.attributes[trait] ?? 0)}>{sheet.attributes[trait] ?? 0}</button>
                      <Dots value={sheet.attributes[trait] ?? 0} />
                    </div>
                  ))}
                </div>
              </div>
            ))}
            <div className="vitals">
              <Vital title={term('Health')} onClick={() => open('blood', 'Health')} big={`${sheet.derived.healthMax - sheet.health.superficial - sheet.health.aggravated}`} max={sheet.derived.healthMax}>
                <Boxes total={sheet.derived.healthMax} track={sheet.health} />
              </Vital>
              <Vital title={term('Willpower')} onClick={() => open('blood', 'Willpower')} big={`${sheet.derived.willpowerMax - sheet.willpower.superficial - sheet.willpower.aggravated}`} max={sheet.derived.willpowerMax}>
                <Boxes total={sheet.derived.willpowerMax} track={sheet.willpower} />
              </Vital>
              <Vital title={term('Hunger')} onClick={() => open('blood', 'Hunger')}>
                <div className={`hunger-blood${sheet.hunger ? '' : ' zero'}`}><img src={bloodIcon} alt="" /><span>{sheet.hunger}</span></div>
              </Vital>
              <Vital title={term('Humanity')} onClick={() => open('blood', 'Humanity')} big={`${sheet.humanity}`}>
                <HumanityBoxes humanity={sheet.humanity} stains={sheet.stains} />
              </Vital>
            </div>
          </section>

          <section className="main">
            <div className="col">
              <div className="dbox">
                <div className="dbox__title">{t('terms.theBlood')}</div>
                <Row label={term('Blood Potency')} value={sheet.bloodPotency} onClick={() => open('blood', 'Blood Potency')} />
                <Row label={t('terms.bloodSurge')} value={sheet.derived.bloodSurge} onClick={() => open('blood', 'Blood Potency')} />
                <Row label={t('terms.mend')} value={t('sheet.mendValue', { value: sheet.derived.mend })} onClick={() => open('blood', 'Blood Potency')} />
                <Row label={t('terms.baneSeverity')} value={sheet.derived.baneSeverity} onClick={() => open('blood', 'Blood Potency')} />
                <Row label={term('Resonance')} value={sheet.resonance ? term(sheet.resonance) : '—'} onClick={() => open('blood', 'Resonance')} />
              </div>
              {sheet.derived.clan && (
                <div className="dbox">
                  <div className="dbox__title">{t('terms.baneAndCompulsion')}</div>
                  <div className="text">
                    <div className="entry"><b>{term(sheet.derived.clan.bane.name)}</b>{sheet.derived.clan.bane.text}</div>
                    <div className="entry"><b>{term(sheet.derived.clan.compulsion.name)}</b>{sheet.derived.clan.compulsion.text}</div>
                  </div>
                </div>
              )}
              <div className="dbox">
                <div className="dbox__title">{t('terms.convictionsAndTouchstones')}</div>
                <div className="text">
                  {sheet.convictions.map((c, i) => (
                    <button key={i} type="button" className="entry" onClick={() => open('conv', String(i))}>
                      <b>{t('builder.convictionNumber', { number: i + 1 })}</b>
                      {c.conviction || '—'} <span className="vtm-muted">→ {c.touchstone || '—'}</span>
                    </button>
                  ))}
                  {sheet.convictions.length < 3 && (
                    <button type="button" className="entry vtm-muted" onClick={() => void act({ type: 'conviction', index: -1 }).then(() => open('conv', String(sheet.convictions.length)))}>
                      {t('sheet.addConviction')}
                    </button>
                  )}
                </div>
              </div>
            </div>

            <div className="dbox skills">
              <div className="dbox__title">{t('terms.skills')}</div>
              {GROUPS.map((group) => (
                <div key={group}>
                  <div className="skills__group">{term(group)}</div>
                  {traitsOfGroup(content.skills, group).map((skill) => {
                    const value = sheet.skills[skill] ?? 0;
                    return (
                      <div key={skill} className={`skill${skill in pool.traits ? ' in-pool' : ''}`}>
                        <Dots value={value} />
                        <button type="button" className="click" onClick={() => open('skill', skill)}>
                          {term(skill)}
                          {sheet.specialties[skill]?.length ? <small>{sheet.specialties[skill].map(term).join(', ')}</small> : null}
                        </button>
                        <button type="button" className={`skill__rating${value ? '' : ' zero'}`} title={t('sheet.addToPool')} onClick={() => value && pool.toggle(skill, value)}>{value || '–'}</button>
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>

            <div className="dbox tabbed">
              <nav className="tabs">
                {TABS.map((id) => (
                  <button key={id} type="button" className={`tab${tab === id ? ' on' : ''}`} onClick={() => setTab(id)}>
                    {id === 'disciplines' ? t('terms.disciplines') : id === 'merits' ? t('terms.meritsAndFlaws') : id === 'coterie' ? t('terms.coterie') : id === 'lore' ? t('terms.loreAndIdentity') : t(`sheet.tabs.${id}`)}
                  </button>
                ))}
              </nav>
              <div style={{ maxHeight: '787px', overflowY: 'auto', paddingRight: '7px', height: 'stretch' }} >
              {tab === 'disciplines' && <DisciplinesTab sheet={sheet} content={content} open={open} pool={pool} />}
              {tab === 'merits' && (
                <>
                  {sheet.advantages.map((a) => (
                    <button key={a.name} type="button" className="mrow" onClick={() => open('adv', a.name)}>
                      <span><b>{term(a.name)}</b><small>{term(a.kind)}</small></span>
                      <Dots value={a.dots} />
                    </button>
                  ))}
                  <button type="button" className="mrow add-advantage" onClick={() => open('advAdd', '')}>
                    <span><b>{t('sheet.addAdvantage')}</b><small>{t('sheet.perDot')}</small></span>
                  </button>
                  <div className="section">{t('terms.loresheets')}</div>
                  {Object.entries(sheet.loresheets ?? {}).map(([loreName, levels]) => {
                    const loresheet = content.loresheets.find((l) => l.name === loreName);
                    return (
                      <button key={loreName} type="button" className="mrow" onClick={() => open('lore', loreName)}>
                        <span><b>{term(loreName)}</b><small>{levels.slice().sort().map((l) => (loresheet?.levels[l - 1] ? term(loresheet.levels[l - 1]) : t('sheet.level', { level: l }))).join(' · ')}</small></span>
                        <span className="dots">{[1, 2, 3, 4, 5].map((dot) => <i key={dot} className={`dot${levels.includes(dot) ? ' on' : ''}`} />)}</span>
                      </button>
                    );
                  })}
                  <button type="button" className="mrow add-advantage" onClick={() => open('loreAdd', '')}>
                    <span><b>{t('sheet.addLoresheet')}</b><small>{t('sheet.loresheetCost')}</small></span>
                  </button>
                  {sheet.thinBlood && (
                    <>
                      <div className="section">{t('terms.thinBlood')}</div>
                      {sheet.thinBlood.merits.map((m) => (
                        <div key={m} className="mrow"><span><b>{term(m)}</b><small>{content.thinBloodMerits.find((x) => x.name === m)?.short}{t('sheet.meritSuffix')}</small></span><span /></div>
                      ))}
                      {sheet.thinBlood.flaws.map((f) => (
                        <div key={f} className="mrow"><span><b>{term(f)}</b><small>{content.thinBloodFlaws.find((x) => x.name === f)?.short}{t('sheet.flawSuffix')}</small></span><span /></div>
                      ))}
                    </>
                  )}
                  <div className="section">{t('sheet.fromPredator')}</div>
                  <p className="tab-text">{content.predators.find((p) => p.name === sheet.identity.predator)?.extras ?? sheet.predatorExtras}</p>
                </>
              )}
              {tab === 'coterie' && <CoterieTab sheet={sheet} content={content} act={act} />}
              {tab === 'inventory' && <InventoryTab sheet={sheet} act={act} />}
              {tab === 'lore' && (
                <>
                  <div className="section">{t('terms.identity')}</div>
                  <div className="ident-grid">
                    {IDENTITY.map(([field, label]) => (
                      <button key={field} type="button" className="ifield" onClick={() => open('ident', field)}>
                        <span>{term(label)}</span>
                        <b>{(NAMED_IDENTITY.includes(field) ? term(sheet.identity[field] ?? '') : field === 'generation' ? generationOf(sheet.identity[field] ?? '') : sheet.identity[field]) || '—'}</b>
                      </button>
                    ))}
                  </div>
                </>
              )}
              {tab === 'notes' && (
                <div className="tab-text">
                  <SavedTextarea value={sheet.notes} placeholder={t('sheet.notesPlaceholder')} onSave={(value) => act({ type: 'notes', value })} />
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
                    <div className="side__type">{t('sheet.character')}</div>
                    <div className="side__name">{name}</div>
                    <h4>{t('sheet.name')}</h4>
                    <SavedInput
                      value={name}
                      onSave={async (value) => {
                        if (!value.trim()) return;
                        const renamed = await renameCharacter(character.id, value.trim()).catch(() => null);
                        if (renamed) setName(renamed.name);
                        else setError(t('sheet.renameFailed'));
                      }}
                    />
                    <h4>{t('sheet.portrait')}</h4>
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
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  return (
    <>
      {Object.entries(sheet.disciplines).map(([name, discipline]) => (
        <div key={name}>
          <div className="section">
            <button type="button" className="click" onClick={() => open('disc', name)}>{term(name)}</button>
            <Dots value={discipline.dots} />
            <button type="button" className="click vtm-muted" style={{ marginLeft: 'auto', textTransform: 'uppercase', fontSize: '12px' }} onClick={() => pool.toggle(name, discipline.dots)}>{t('sheet.addDotsToPool', { dots: discipline.dots })}</button>
          </div>
          <div className="table-head"><span>{t('terms.power')}</span><span>{t('explain.cost')}</span><span>{t('explain.pool')}</span><span>{t('sheet.lvl')}</span></div>
          {(content.disciplines[name]?.powers ?? []).filter((p) => discipline.powers.includes(p.name)).map((p) => (
            <div key={p.name} className="prow">
              <button type="button" className="click" onClick={() => open('power', `${name}|${p.name}`)}><b>{term(p.name)}</b><small>{p.summary}</small></button>
              <span>{p.cost}</span>
              {p.pool === '—' ? <span className="vtm-muted">—</span> : <button type="button" className="pool-btn" onClick={() => void pool.rollNow(p.pool.split(' + '), p.name)}>{poolOf(content, p.pool)}</button>}
              <span>{p.level}</span>
            </div>
          ))}
        </div>
      ))}
      {(sheet.rituals?.length ?? 0) > 0 && (
        <>
          <div className="section">{t('terms.rituals')}</div>
          {content.rituals.filter((r) => sheet.rituals?.includes(r.name)).map((r) => (
            <div key={r.name} className="prow">
              <span><b>{term(r.name)}</b><small>{r.summary}</small></span>
              <span>{t('sheet.oneRouseCheck')}</span>
              <span className="red">{poolOf(content, 'Intelligence + Blood Sorcery')}</span>
              <span>{r.level}</span>
            </div>
          ))}
        </>
      )}
      <p className="vtm-muted">{t('sheet.raiseHint')}</p>
    </>
  );
}

function DetailBody({ detail, sheet, content, act, open, rouse, dice }: { detail: NonNullable<Detail>; sheet: Vtm5Sheet; content: Vtm5Content; act: Act; open: (kind: string, key: string) => void; rouse: () => Promise<void>; dice: DicePool }) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const { kind, key } = detail;
  const available = sheet.derived.xpAvailable;
  const head: Head = (type, name, lead) => (
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
        {cost ? <>{t('sheet.nextDot')} <b>{t('sheet.nextDotCost', { cost })}</b>{t('sheet.youHave', { available })}</> : t('sheet.maximumRating')} ·{' '}
        <button type="button" className="red" onClick={() => open('xp', '')}>{t('sheet.undoInHistory')}</button>
      </div>
    </>
  );

  if (kind === 'attr' || kind === 'skill') {
    const isAttribute = kind === 'attr';
    const trait = (isAttribute ? content.attributes : content.skills)[key];
    const value = (isAttribute ? sheet.attributes : sheet.skills)[key] ?? 0;
    const scale = isAttribute ? content.attributeScale.map(term) : content.skillScale;
    const cost = (isAttribute ? sheet.derived.nextCost.attribute : sheet.derived.nextCost.skill)[key] ?? 0;
    return (
      <>
        {head(t(isAttribute ? 'explain.attributeKind' : 'explain.skillKind', { group: term(trait.group) }), term(key), trait.short)}
        {raise(isAttribute ? 'attribute' : 'skill', key, value, cost)}
        {trait.uses && (<><h4>{t('explain.usedFor')}</h4><p>{trait.uses}</p></>)}
        {!isAttribute && <Specialties skill={key} sheet={sheet} content={content} ideas={trait.specialties ?? []} act={act} />}
        <h4>{t('explain.eachDot')}</h4>
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
          {head(`${term(name)} ${'●'.repeat(power.level)}`, term(power.name), power.summary)}
          <table className="levels"><tbody><tr><td>{t('explain.cost')}</td><td>{power.cost}</td></tr><tr><td>{t('explain.pool')}</td><td>{poolOf(content, power.pool)}</td></tr></tbody></table>
        </>
      );
    }
    const owned = sheet.disciplines[name] ?? { dots: 0, powers: [] };
    const inClan = sheet.derived.clan?.disciplines.includes(name);
    return (
      <>
        {head(t('sheet.disciplineKind', { type: term(discipline.type), clanStatus: t(inClan ? 'terms.inClan' : 'terms.outOfClan') }), term(name), discipline.short)}
        {raise('discipline', name, owned.dots, sheet.derived.nextCost.discipline[name] ?? 0)}
        {!inClan && <p className="warn-note">{t('sheet.outOfClanNote')}</p>}
        <p className={`warn-note${sheet.resonance === discipline.resonance ? ' met' : ''}`}>
          {t('sheet.resonanceNote', { resonance: term(discipline.resonance), match: sheet.resonance === discipline.resonance ? t('sheet.resonanceMatch') : '' })}
        </p>
        {name === 'Thin-Blood Alchemy' && sheet.thinBlood?.alchemyMethod && (
          <>
            <h4>{t('sheet.distill')}</h4>
            <p>{t('sheet.distillMethod', { method: term(sheet.thinBlood.alchemyMethod) })}</p>
            <div className="side-actions">
              <button type="button" className="btn" onClick={() => {
                const attribute = content.alchemyMethods.find((m) => m.name === sheet.thinBlood?.alchemyMethod)?.attribute ?? 'Intelligence';
                void dice.rollNow([attribute, 'Thin-Blood Alchemy'], 'Distill');
              }}>{t('sheet.rollDistillation')}</button>
              <button type="button" className="btn btn--ghost" onClick={() => void rouse()}>{t('terms.rouseCheck')}</button>
            </div>
          </>
        )}
        {name === 'Blood Sorcery' && owned.dots > 0 && (
          <>
            <h4>{t('terms.rituals')}</h4>
            {content.rituals.map((r) => {
              const known = sheet.rituals?.includes(r.name);
              const ritualCost = sheet.derived.nextCost.ritual?.[r.name] ?? 0;
              return (
                <div key={r.name} className="spec-row">
                  <span>{'●'.repeat(r.level)} {term(r.name)} <span className="vtm-muted">— {r.summary}</span></span>
                  {known ? <span className="red">{t('sheet.known')}</span> : ritualCost ? (
                    <button type="button" className="red" disabled={ritualCost > available} onClick={() => void act({ type: 'spendXp', target: 'ritual', key: r.name })}>{t('sheet.learnCost', { cost: ritualCost })}</button>
                  ) : <span>—</span>}
                </div>
              );
            })}
          </>
        )}
        <h4>{t('sheet.powersHead', { powers: t('terms.powers'), known: owned.powers.length, dots: owned.dots })}</h4>
        {discipline.powers.map((p) => {
          const known = owned.powers.includes(p.name);
          const learnable = !known && p.level <= owned.dots && owned.powers.length < owned.dots;
          return (
            <div key={p.name} className="spec-row">
              <span>{'●'.repeat(p.level)} {term(p.name)} <span className="vtm-muted">— {p.summary}</span></span>
              {known ? <span className="red">{t('sheet.known')}</span> : learnable ? <button type="button" className="red" onClick={() => void act({ type: 'learnPower', discipline: name, power: p.name })}>{t('sheet.learn')}</button> : <span>—</span>}
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
        {head(advantage ? term(advantage.kind) : t('sheet.advantage'), term(key), advantage?.short ?? t('sheet.fromPredatorLead'))}
        {advantage && advantage.kind !== 'Flaw' ? (
          raise('advantage', key, owned, cost)
        ) : (
          <div className="rating"><Dots value={owned} /><span className="rating__value">{owned}</span></div>
        )}
        {advantage?.kind === 'Flaw' && <div className="hint">{t('sheet.flawsHint')}</div>}
      </>
    );
  }
  if (kind === 'advAdd') {
    const buyable = content.advantages.filter((a) => a.kind !== 'Flaw' && !sheet.advantages.some((owned) => owned.name === a.name));
    return (
      <>
        {head(t('sheet.advantages'), t('sheet.addAdvantage').replace(/^\+ /, ''), t('sheet.addAdvantageLead'))}
        {buyable.map((a) => {
          const cost = sheet.derived.nextCost.advantage?.[a.name] ?? 0;
          return (
            <div key={a.name} className="spec-row">
              <span><b>{term(a.name)}</b> <span className="vtm-muted">— {term(a.kind)} · {a.short}</span></span>
              <button type="button" className="red" disabled={!cost || cost > available} onClick={() => void act({ type: 'spendXp', target: 'advantage', key: a.name }).then(() => open('adv', a.name))}>
                {t('sheet.buyCost', { cost })}
              </button>
            </div>
          );
        })}
      </>
    );
  }
  if (kind === 'ident') {
    const label = IDENTITY.find(([field]) => field === key)?.[1] ?? key;
    return (
      <>
        {head(t('terms.identity'), term(label), content.identity[label] ?? '')}
        {EDITABLE_IDENTITY.includes(key) ? (
          <SavedInput value={sheet.identity[key] ?? ''} onSave={(value) => act({ type: 'identity', field: key, value })} />
        ) : (
          <>
            <p>{(NAMED_IDENTITY.includes(key) ? term(sheet.identity[key] ?? '') : sheet.identity[key]) || '—'}</p>
            <div className="hint">{t('sheet.setAtCreation')}</div>
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
        {head(t('terms.conviction'), t('builder.convictionNumber', { number: index + 1 }), content.convictionHelp)}
        <h4>{t('terms.conviction')}</h4>
        <SavedInput value={conviction.conviction} onSave={(value) => act({ type: 'conviction', index, conviction: value })} />
        <h4>{t('terms.touchstone')}</h4>
        <SavedInput value={conviction.touchstone} onSave={(value) => act({ type: 'conviction', index, touchstone: value })} />
        {sheet.convictions.length > 1 && (
          <div className="side-actions">
            <button type="button" className="btn btn--ghost" onClick={() => void act({ type: 'conviction', index, remove: true })}>{t('sheet.remove')}</button>
          </div>
        )}
      </>
    );
  }
  if (kind === 'lore' || kind === 'loreAdd') {
    const shown = kind === 'lore' ? content.loresheets.filter((l) => l.name === key) : content.loresheets.filter((l) => (sheet.derived.nextCost.loresheet?.[l.name] ?? []).some((c) => c > 0));
    return (
      <>
        {head(t('terms.loresheet'), kind === 'lore' ? term(key) : t('sheet.addLoresheet').replace(/^\+ /, ''), t('sheet.loreLead'))}
        {shown.map((l) => (
          <div key={l.name}>
            {kind === 'loreAdd' && <h4>{term(l.name)}{l.restriction ? ` · ${term(l.restriction)}` : ''}</h4>}
            {l.levels.map((entry, index) => {
              const owned = sheet.loresheets?.[l.name]?.includes(index + 1);
              const cost = sheet.derived.nextCost.loresheet?.[l.name]?.[index] ?? 0;
              return (
                <div key={entry} className="spec-row">
                  <span>{'●'.repeat(index + 1)} {term(entry)}</span>
                  {owned ? <span className="red">{t('sheet.owned')}</span> : cost ? (
                    <button type="button" className="red" disabled={cost > available} onClick={() => void act({ type: 'spendXp', target: 'loresheet', key: l.name, level: index + 1 })}>{t('sheet.buyCost', { cost })}</button>
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
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
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
      <div className="section">{t('terms.coterie')}</div>
      <div className="coterie-grid">
        {text('name', t('sheet.name'), t('sheet.coterieName'))}
        <div className="coterie-field">
          <span>{t('sheet.type')}</span>
          <select value={coterie.type ?? ''} onChange={(event) => void save({ type: event.target.value })}>
            <option value="">—</option>
            {content.coterieTypes.map((type) => <option key={type} value={type}>{term(type)}</option>)}
          </select>
          <small>{t('sheet.typeHelp')}</small>
        </div>
        {text('city', t('sheet.city'), t('sheet.cityHelp'))}
        {text('tenets', t('sheet.tenets'), t('sheet.tenetsHelp'))}
        {text('goals', t('sheet.goals'), t('sheet.goalsHelp'))}
        {text('boons', t('sheet.boons'), t('sheet.boonsHelp'))}
      </div>

      <div className="section">{t('terms.members')} <span className="vtm-muted" style={{ fontSize: '11px' }}>{t('sheet.eachGivesDot')}</span></div>
      {coterie.members.map((memberName, index) => (
        <div key={memberName + index} className="spec-row">
          <span>{memberName}</span>
          <button type="button" className="red" onClick={() => void save({ members: coterie.members.filter((_, i) => i !== index) })}>{t('sheet.removeLower')}</button>
        </div>
      ))}
      <div className="add-row">
        <input type="text" value={member} placeholder={t('sheet.memberPlaceholder')} onChange={(event) => setMember(event.target.value)} />
        <button type="button" className="btn" disabled={!member.trim()} onClick={() => void save({ members: [...coterie.members, member] }).then(() => setMember(''))}>{t('sheet.addMember')}</button>
      </div>

      <div className="section">{t('sheet.poolHead', { spent: sheet.derived.coterieSpent, pool: sheet.derived.coteriePool })}</div>
      <p className="tab-text vtm-muted">
        {t('sheet.poolExplain', { count: coterie.members.length + 1, contribution: coterie.contribution })}
      </p>
      <Stepper value={coterie.othersDonated} min={0} max={99} onChange={(value) => save({ othersDonated: value })} />

      <div className="section">{t('terms.domain')}</div>
      {Object.entries(content.domain).map(([trait, short]) => (
        <div key={trait} className="mrow">
          <span><b>{term(trait)}</b><small>{short}</small></span>
          <span className="dots pick">
            {[1, 2, 3, 4, 5].map((dot) => {
              const value = coterie.domain[trait] ?? 0;
              const next = value === dot ? dot - 1 : dot;
              return <i key={dot} className={`dot${dot <= value ? ' on' : ''}${next - value > left ? ' locked' : ''}`} onClick={() => next - value <= left && void save({ domain: { [trait]: next } })} />;
            })}
          </span>
        </div>
      ))}
      {text('domainDescription', t('sheet.description'), t('sheet.descriptionHelp'))}
      {text('domainResonance', term('Resonance'), t('sheet.domainResonanceHelp'))}

      <div className="section">{t('terms.coterieBackgrounds')}</div>
      {backgroundNames.map((backgroundName) => {
        const value = coterie.backgrounds.find((b) => b.name === backgroundName)?.dots ?? 0;
        return (
          <div key={backgroundName} className="mrow">
            <span><b>{term(backgroundName)}</b><small>{content.advantages.find((a) => a.name === backgroundName)?.short}</small></span>
            <span className="dots pick">
              {[1, 2, 3, 4, 5].map((dot) => {
                const next = value === dot ? dot - 1 : dot;
                return <i key={dot} className={`dot${dot <= value ? ' on' : ''}${next - value > left ? ' locked' : ''}`} onClick={() => next - value <= left && void setBackground(backgroundName, next)} />;
              })}
            </span>
          </div>
        );
      })}
    </>
  );
}

function InventoryTab({ sheet, act }: { sheet: Vtm5Sheet; act: Act }) {
  const t = useVtm5Text();
  const [name, setName] = useState('');
  const items = sheet.inventory ?? [];
  return (
    <>
      {items.length === 0 && <p className="tab-text vtm-muted">{t('sheet.nothingCarried')}</p>}
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
            <button type="button" className="red" onClick={() => void act({ type: 'removeItem', index })}>{t('sheet.removeLower')}</button>
          </span>
        </div>
      ))}
      <div className="add-row spaced">
        <input type="text" value={name} placeholder={t('sheet.itemPlaceholder')} onChange={(event) => setName(event.target.value)} />
        <button type="button" className="btn" disabled={!name.trim()} onClick={() => void act({ type: 'addItem', name, quantity: 1 }).then(() => setName(''))}>{t('sheet.addItem')}</button>
      </div>
    </>
  );
}

function FrenzyPanel({ sheet, dice, head }: { sheet: Vtm5Sheet; dice: DicePool; head: Head }) {
  const t = useVtm5Text();
  const [difficulty, setDifficulty] = useState(3);
  const willpower = sheet.derived.willpowerMax - sheet.willpower.superficial - sheet.willpower.aggravated;
  return (
    <>
      {head(t('terms.theBeast'), t('terms.frenzyCheck'), t('sheet.frenzyLead'))}
      <div className="cost">{t('sheet.frenzyPool', { dice: Math.max(1, willpower + Math.floor(sheet.humanity / 3)), willpower, humanity: sheet.humanity })}</div>
      <h4>{t('terms.difficulty')}</h4>
      <div className="rating">
        <button type="button" className="step" disabled={difficulty <= 1} onClick={() => setDifficulty(difficulty - 1)}>−</button>
        <span className="rating__value" style={{ margin: '0 auto' }}>{difficulty}</span>
        <button type="button" className="step" onClick={() => setDifficulty(difficulty + 1)}>+</button>
      </div>
      <div className="side-actions"><button type="button" className="btn" onClick={() => void dice.frenzy(difficulty)}>{t('sheet.rollFrenzy')}</button></div>
      <div className="hint">{t('sheet.toRollLog')}</div>
    </>
  );
}

function TrackerDetail({ name, sheet, content, act, head, rouse, remorse }: { name: string; sheet: Vtm5Sheet; content: Vtm5Content; act: Act; head: Head; rouse: () => Promise<void>; remorse: () => Promise<void> }) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const lead = content.blood[name] ?? '';
  if (name === 'Health' || name === 'Willpower') {
    const track = name.toLowerCase();
    const values = name === 'Health' ? sheet.health : sheet.willpower;
    const max = name === 'Health' ? sheet.derived.healthMax : sheet.derived.willpowerMax;
    const damage = (kind: string, amount: number) => void act({ type: 'damage', track, kind, amount });
    return (
      <>
        {head(t('terms.tracker'), term(name), lead)}
        <div className="track-edit"><Boxes total={max} track={values} /></div>
        <div className="side-actions">
          <button type="button" className="btn btn--ghost" onClick={() => damage('superficial', 1)}>{t('sheet.addSuperficial')}</button>
          <button type="button" className="btn btn--ghost" onClick={() => damage('aggravated', 1)}>{t('sheet.addAggravated')}</button>
          <button type="button" className="btn btn--ghost" onClick={() => damage('superficial', -1)}>− /</button>
          <button type="button" className="btn btn--ghost" onClick={() => damage('aggravated', -1)}>− X</button>
        </div>
        <div className="hint">{t('sheet.fullTrackHint')}</div>
      </>
    );
  }
  if (name === 'Hunger') {
    return (
      <>
        {head(t('terms.tracker'), term(name), lead)}
        <Stepper value={sheet.hunger} min={0} max={5} onChange={(value) => act({ type: 'hunger', value })} />
        <div className="side-actions"><button type="button" className="btn" onClick={() => void rouse()}>{t('terms.rouseCheck')}</button></div>
        <div className="hint">{t('sheet.rouseHint')}</div>
      </>
    );
  }
  if (name === 'Humanity') {
    return (
      <>
        {head(t('terms.tracker'), term(name), lead)}
        <Stepper value={sheet.humanity} min={0} max={10} onChange={(value) => act({ type: 'humanity', value })} />
        <h4>{t('terms.stains')}</h4>
        <Stepper value={sheet.stains} min={0} max={10 - sheet.humanity} onChange={(value) => act({ type: 'stains', value })} />
        <div className="side-actions">
          <button type="button" className="btn" disabled={sheet.stains === 0} onClick={() => void remorse()}>{t('sheet.rollRemorse')}</button>
        </div>
        <div className="hint">{t('sheet.remorseHint')}</div>
      </>
    );
  }
  if (name === 'Resonance') {
    return (
      <>
        {head(t('terms.theBlood'), term(name), lead)}
        <select value={sheet.resonance ?? ''} onChange={(event) => void act({ type: 'resonance', value: event.target.value })}>
          <option value="">—</option>
          {Object.keys(content.resonances).map((resonance) => <option key={resonance} value={resonance}>{term(resonance)}</option>)}
        </select>
        {sheet.resonance && <p>{t('sheet.resonanceBoosts', { disciplines: (content.resonances[sheet.resonance] ?? []).map(term).join(t('sheet.and')) })}</p>}
      </>
    );
  }
  const cost = sheet.derived.nextCost.bloodPotency;
  return (
    <>
      {head(t('terms.theBlood'), term('Blood Potency'), lead)}
      <div className="rating">
        <Dots value={sheet.bloodPotency} />
        <button type="button" className="step" disabled={!cost || cost > sheet.derived.xpAvailable} onClick={() => void act({ type: 'spendXp', target: 'bloodPotency', key: 'Blood Potency' })}>+</button>
        <span className="rating__value">{sheet.bloodPotency}</span>
      </div>
      <div className="cost">{cost ? <>{t('sheet.nextDot')} <b>{t('sheet.nextDotCost', { cost })}</b></> : t('sheet.maximum')}</div>
      <table className="levels">
        <tbody>
          <tr><th>{t('sheet.bpHead')}</th><th>{t('sheet.surgeHead')}</th><th>{t('sheet.mendHead')}</th><th>{t('sheet.baneHead')}</th></tr>
          {content.bpTable.slice(0, 6).map((row, i) => (
            <tr key={i} className={i === sheet.bloodPotency ? 'current' : undefined}><td>{i}</td><td>{row[0]}</td><td>{row[1]}</td><td>{row[4]}</td></tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

function Specialties({ skill, sheet, content, ideas, act }: { skill: string; sheet: Vtm5Sheet; content: Vtm5Content; ideas: string[]; act: Act }) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const [name, setName] = useState('');
  const owned = sheet.specialties[skill] ?? [];
  const canAdd = (sheet.skills[skill] ?? 0) > owned.length && sheet.derived.xpAvailable >= sheet.derived.nextCost.specialty;
  const shownIdeas = ideas.map(term);
  return (
    <>
      <h4>{t('terms.specialties')}</h4>
      {owned.length ? owned.map((s) => <div key={s} className="spec-row"><span>{term(s)} <span className="vtm-muted">{t('sheet.specialtyBonus')}</span></span></div>) : <p className="vtm-muted">{t('sheet.noneYet')}</p>}
      {(sheet.skills[skill] ?? 0) > 0 && (
        <>
          <div className="add-row">
            <input type="text" value={name} placeholder={shownIdeas[0] ? t('sheet.specialtyPlaceholder', { idea: shownIdeas[0] }) : t('terms.specialty')} onChange={(event) => setName(event.target.value)} />
            <button type="button" className="btn" disabled={!canAdd || !name.trim()} onClick={() => void act({ type: 'spendXp', target: 'specialty', key: skill, specialty: name }).then(() => setName(''))}>
              {t('sheet.addSpecialty', { cost: sheet.derived.nextCost.specialty })}
            </button>
          </div>
          <div className="hint">{t('sheet.specialtyHint')}{shownIdeas.length ? t('sheet.specialtyIdeas', { ideas: shownIdeas.join(', ') }) : ''}</div>
        </>
      )}
    </>
  );
}

function XpPanel({ sheet, act, head }: { sheet: Vtm5Sheet; act: Act; head: Head }) {
  const t = useVtm5Text();
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');
  const canUndo = (index: number) => {
    const entry = sheet.xpLog[index];
    return entry.kind === 'spend' && !entry.undone && !sheet.xpLog.slice(index + 1).some((later) => later.kind === 'spend' && !later.undone && later.key === entry.key);
  };
  return (
    <>
      {head(t('sheet.character'), t('terms.experience'), t('sheet.experienceLead'))}
      <table className="levels">
        <tbody>
          <tr><td>{t('sheet.earned')}</td><td>{sheet.derived.xpEarned}</td></tr>
          <tr><td>{t('sheet.spent')}</td><td>{sheet.derived.xpSpent}</td></tr>
          <tr className="current"><td>{t('sheet.available')}</td><td>{sheet.derived.xpAvailable}</td></tr>
        </tbody>
      </table>
      <h4>{t('sheet.addXp')}</h4>
      <div className="add-row">
        <input type="text" inputMode="numeric" value={amount} placeholder="1" style={{ maxWidth: 56 }} onChange={(event) => setAmount(event.target.value)} />
        <input type="text" value={note} placeholder={t('sheet.sessionPlaceholder')} onChange={(event) => setNote(event.target.value)} />
        <button type="button" className="btn" disabled={!(Number(amount) > 0)} onClick={() => void act({ type: 'awardXp', amount: Number(amount), note }).then(() => { setAmount(''); setNote(''); })}>
          {t('sheet.add')}
        </button>
      </div>
      <div className="hint">{t('sheet.xpHint')}</div>
      <h4>{t('sheet.history')}</h4>
      {sheet.xpLog.length === 0 && <p className="vtm-muted">{t('sheet.nothingYet')}</p>}
      {sheet.xpLog.map((entry, index) => ({ entry, index })).reverse().map(({ entry, index }) => (
        <div key={index} className="spec-row">
          {entry.kind === 'award' ? (
            <span><b className="red">+{entry.amount}</b> {entry.note}</span>
          ) : (
            <span style={entry.undone ? { textDecoration: 'line-through', opacity: 0.5 } : undefined}>{entry.label} <span className="vtm-muted">· −{entry.cost}</span></span>
          )}
          {entry.kind === 'spend' && (entry.undone ? <span>{t('sheet.undone')}</span> : canUndo(index) ? <button type="button" className="red" onClick={() => void act({ type: 'undoXp', index })}>{t('sheet.undo')}</button> : <span title={t('sheet.laterSpendFirst')}>—</span>)}
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

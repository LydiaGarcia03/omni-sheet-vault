import { useEffect, useRef, useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router';
import type { LevelUpPageProps } from '../../../levelup/levelUpPages';
import { AppHeader } from '../../../shell/AppHeader';
import { DaggerheartFooter } from '../DaggerheartFooter';
import { signed, TRAITS } from '../builder/daggerheartBuild';
import { DomainIcon, domainColor } from '../builder/DaggerheartParts';
import { capitalize } from '../daggerheartCatalogue';
import {
  cancelLevelUp,
  finishLevelUp,
  getLevelUp,
  picksUsed,
  saveLevelUp,
  type Advancement,
  type CardOption,
  type LevelEntry,
  type LevelSummary,
  type LevelUpView,
} from './daggerheartLevelUpApi';
import '../dh-theme.css';
import '../dh-levelup.css';

const SAVE_DELAY_MS = 300;
const PICKS = 2;

/** The Daggerheart level up (SRD pp. 53–54): tier achievement, two advancements from the sheet's grid, thresholds, a new card. */
export function DaggerheartLevelUpPage({ characterId, characterName, userName }: LevelUpPageProps) {
  const navigate = useNavigate();
  const [view, setView] = useState<LevelUpView | null>(null);
  const [entry, setEntry] = useState<LevelEntry>({});
  const [error, setError] = useState<string | null>(null);
  const [finishing, setFinishing] = useState(false);
  const sequence = useRef(0);

  useEffect(() => {
    getLevelUp(characterId).then(
      (loaded) => {
        setView(loaded);
        setEntry(loaded.draft ?? {});
      },
      (cause: Error) => setError(cause.message),
    );
  }, [characterId]);

  const change = (next: LevelEntry) => {
    setEntry(next);
    const current = ++sequence.current;
    window.setTimeout(() => {
      if (current !== sequence.current) return;
      saveLevelUp(characterId, next).then(
        (saved) => current === sequence.current && (setView(saved), setError(null)),
        (cause: Error) => setError(cause.message),
      );
    }, SAVE_DELAY_MS);
  };

  const back = () => navigate(`/characters/${characterId}`);
  const finish = async () => {
    setFinishing(true);
    try {
      await finishLevelUp(characterId);
      back();
    } catch (cause) {
      setError((cause as Error).message);
      setFinishing(false);
    }
  };
  const cancel = async () => {
    await cancelLevelUp(characterId).catch(() => undefined);
    back();
  };

  const advancements = entry.advancements ?? [];
  const setAdvancements = (next: Advancement[]) => change({ ...entry, advancements: next });

  return (
    <div className="dh-page">
      <AppHeader userName={userName} backToListWithin={1260} compact systemId="daggerheart" />
      {!view && error && (
        <p role="alert" className="problem dh-levelup-problem">
          {error}
        </p>
      )}
      {view && (
        <main className="dh-levelup">
          <section className="dh-frame navy lu-main">
            <header className="lu-head">
              <h1>
                {characterName} · Level {view.level} → {view.nextLevel}
              </h1>
              <span className="dh-badge">Tier {view.tier}</span>
            </header>
            {view.atMaxLevel ? (
              <p className="lu-note">Level 10 is the highest level.</p>
            ) : (
              <>
                <Step number={1} title="Tier achievement">
                  <Achievement view={view} entry={entry} onChange={change} />
                </Step>
                <Step number={2} title={`Advancements · ${picksUsed(entry, view.grid)} / ${PICKS}`}>
                  <p className="lu-note">Choose two from your tier or below. Proficiency and multiclassing take both picks.</p>
                  <Grid view={view} entry={entry} onTake={(advancement) => setAdvancements([...advancements, advancement])} />
                  {advancements.map((advancement, index) => (
                    <Pick
                      key={index}
                      view={view}
                      entry={entry}
                      advancement={advancement}
                      onChange={(next) => setAdvancements(advancements.map((current, i) => (i === index ? next : current)))}
                      onRemove={() => setAdvancements(advancements.filter((_, i) => i !== index))}
                    />
                  ))}
                </Step>
                <Step number={3} title="Damage thresholds">
                  <p className="lu-note">
                    All damage thresholds rise by 1: {view.before.majorThreshold} / {view.before.severeThreshold} → {view.before.majorThreshold + 1} /{' '}
                    {view.before.severeThreshold + 1}.
                  </p>
                </Step>
                <Step number={4} title="New domain card">
                  <p className="lu-note">A card at level {view.nextLevel} or lower from your domains. It goes to your loadout if there's room, otherwise to the vault.</p>
                  <CardPicker
                    cards={view.cardOptions.filter((card) => card.level <= view.nextLevel)}
                    taken={takenCards(entry)}
                    selected={entry.domainCard ?? ''}
                    onPick={(slug) => change({ ...entry, domainCard: slug })}
                  />
                </Step>
              </>
            )}
          </section>

          <aside className="dh-frame paper lu-side">
            <div className="dh-title">
              <span>What changes</span>
            </div>
            <Changes before={view.before} after={view.after} />
            {view.newFeatures.length > 0 && (
              <>
                <h4>New features</h4>
                {view.newFeatures.map((feature) => (
                  <div key={feature.name} className="lu-feature">
                    <b>{feature.name}</b> <small>{feature.origin}</small>
                    <p>{feature.text}</p>
                  </div>
                ))}
              </>
            )}
            {(view.missing.length > 0 || view.problems.length > 0 || error) && (
              <ul className="lu-issues" role="status">
                {error && <li className="problem-line">{error}</li>}
                {view.problems.map((problem) => (
                  <li key={problem} className="problem-line">
                    {problem}
                  </li>
                ))}
                {view.missing.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            )}
            <div className="acts">
              <button type="button" className="dh-btn gold" disabled={view.atMaxLevel || finishing || view.missing.length > 0 || view.problems.length > 0} onClick={() => void finish()}>
                {finishing ? 'Leveling up…' : `Become level ${view.nextLevel}`}
              </button>
              <button type="button" className="dh-btn light" onClick={() => void cancel()}>
                Cancel
              </button>
            </div>
          </aside>
        </main>
      )}
      <DaggerheartFooter />
    </div>
  );
}

function Step({ number, title, children }: { number: number; title: string; children: ReactNode }) {
  return (
    <section className="lu-step">
      <h2>
        <span className="lu-step__n">{number}</span> {title}
      </h2>
      {children}
    </section>
  );
}

function Achievement({ view, entry, onChange }: { view: LevelUpView; entry: LevelEntry; onChange: (entry: LevelEntry) => void }) {
  const { achievement } = view;
  if (!achievement.experience) {
    return <p className="lu-note">No tier achievement at level {view.nextLevel}; achievements come at levels 2, 5 and 8.</p>;
  }
  return (
    <>
      <p className="lu-note">
        Gain a new Experience at +2 and +1 Proficiency{achievement.clearsTraitMarks ? ', and clear the marks on your traits' : ''}.
      </p>
      <label className="lu-field">
        <span>New Experience</span>
        <input className="field" value={entry.experience ?? ''} placeholder="e.g. Quiet Steps" onChange={(event) => onChange({ ...entry, experience: event.target.value })} />
      </label>
    </>
  );
}

/** The sheet's advancement grid: each tier's options with their slots, marked by earlier levels and by this one. */
function Grid({ view, entry, onTake }: { view: LevelUpView; entry: LevelEntry; onTake: (advancement: Advancement) => void }) {
  const used = picksUsed(entry, view.grid);
  return (
    <div className="lu-grid">
      {view.grid.map((tier) => (
        <div key={tier.tier} className={`lu-tier${tier.open ? '' : ' closed'}`}>
          <h3>
            Tier {tier.tier} <small>{tier.tier === 2 ? 'levels 2–4' : tier.tier === 3 ? 'levels 5–7' : 'levels 8–10'}</small>
          </h3>
          {tier.options.map((option) => {
            const free = option.slots - option.marked - option.picked;
            const room = used + (option.takesBothPicks ? 2 : 1) <= PICKS;
            const disabled = !tier.open || option.crossedOut || free <= 0 || !room;
            return (
              <button
                key={option.id}
                type="button"
                className={`lu-option${option.crossedOut ? ' crossed' : ''}`}
                disabled={disabled}
                title={option.crossedOut ? 'Crossed out' : undefined}
                onClick={() => onTake({ option: option.id, tier: tier.tier })}
              >
                <span className={`lu-slots${option.takesBothPicks ? ' both' : ''}`}>
                  {Array.from({ length: option.slots * (option.takesBothPicks ? 2 : 1) }, (_, index) => {
                    const slot = option.takesBothPicks ? Math.floor(index / 2) : index;
                    return <i key={index} className={slot < option.marked ? 'marked' : slot < option.marked + option.picked ? 'picked' : ''} />;
                  })}
                </span>
                <span>{option.label}</span>
              </button>
            );
          })}
        </div>
      ))}
    </div>
  );
}

/** One chosen advancement, with what it still asks for. */
function Pick({ view, entry, advancement, onChange, onRemove }: { view: LevelUpView; entry: LevelEntry; advancement: Advancement; onChange: (next: Advancement) => void; onRemove: () => void }) {
  const option = view.grid.flatMap((tier) => tier.options).find((candidate) => candidate.id === advancement.option);
  return (
    <div className="lu-pick">
      <div className="lu-pick__head">
        <b>{option?.label ?? advancement.option}</b>
        <small>Tier {advancement.tier}</small>
        <button type="button" className="link-btn" onClick={onRemove}>
          remove
        </button>
      </div>
      {advancement.option === 'traits' && <TraitPick view={view} advancement={advancement} onChange={onChange} />}
      {advancement.option === 'experiences' && <ExperiencePick view={view} entry={entry} advancement={advancement} onChange={onChange} />}
      {advancement.option === 'domainCard' && (
        <CardPicker
          cards={view.cardOptions.filter((card) => card.level <= Math.min(view.nextLevel, advancement.tier === 2 ? 4 : advancement.tier === 3 ? 7 : 10))}
          taken={takenCards(entry).filter((slug) => slug !== advancement.card)}
          selected={advancement.card ?? ''}
          onPick={(card) => onChange({ ...advancement, card })}
        />
      )}
      {advancement.option === 'subclass' && view.subclassNext.card && (
        <div className="lu-sub">
          {view.subclassNext.subclass} {view.subclassNext.card}:
          {(view.subclassNext.features ?? []).map((feature) => (
            <div key={feature.name} className="lu-feature">
              <b>{feature.name}</b>
              <p>{feature.text}</p>
            </div>
          ))}
        </div>
      )}
      {advancement.option === 'multiclass' && <MulticlassPick view={view} advancement={advancement} onChange={onChange} />}
    </div>
  );
}

function TraitPick({ view, advancement, onChange }: { view: LevelUpView; advancement: Advancement; onChange: (next: Advancement) => void }) {
  const picked = advancement.traits ?? [];
  const toggle = (id: string) => onChange({ ...advancement, traits: picked.includes(id) ? picked.filter((trait) => trait !== id) : [...picked, id].slice(-2) });
  return (
    <div className="lu-choices">
      {TRAITS.map((trait) => {
        const marked = view.markedTraits.includes(trait.id);
        return (
          <button key={trait.id} type="button" className={picked.includes(trait.id) ? 'on' : ''} aria-pressed={picked.includes(trait.id)} disabled={marked} onClick={() => toggle(trait.id)}>
            {trait.name} {signed(view.before.traits[trait.id] ?? 0)}
            {marked && <small> marked</small>}
          </button>
        );
      })}
    </div>
  );
}

function ExperiencePick({ view, entry, advancement, onChange }: { view: LevelUpView; entry: LevelEntry; advancement: Advancement; onChange: (next: Advancement) => void }) {
  const names = [...view.experiences.map((experience) => experience.name), ...(view.achievement.experience && entry.experience ? [entry.experience] : [])];
  const picked = advancement.experiences ?? [];
  const toggle = (index: number) => onChange({ ...advancement, experiences: picked.includes(index) ? picked.filter((i) => i !== index) : [...picked, index].slice(-2) });
  return (
    <div className="lu-choices">
      {names.map((name, index) => (
        <button key={`${name}-${index}`} type="button" className={picked.includes(index) ? 'on' : ''} aria-pressed={picked.includes(index)} onClick={() => toggle(index)}>
          {name}
        </button>
      ))}
    </div>
  );
}

function MulticlassPick({ view, advancement, onChange }: { view: LevelUpView; advancement: Advancement; onChange: (next: Advancement) => void }) {
  const chosen = view.multiclassOptions.find((option) => option.slug === advancement.class);
  return (
    <div className="lu-multiclass">
      <label className="lu-field">
        <span>Class</span>
        <select className="field" value={advancement.class ?? ''} onChange={(event) => onChange({ ...advancement, class: event.target.value, domain: '', subclass: '' })}>
          <option value="">Choose a class…</option>
          {view.multiclassOptions.map((option) => (
            <option key={option.slug} value={option.slug}>
              {option.name} ({option.domains.map(capitalize).join(' & ')})
            </option>
          ))}
        </select>
      </label>
      {chosen && (
        <>
          <label className="lu-field">
            <span>Domain</span>
            <select className="field" value={advancement.domain ?? ''} onChange={(event) => onChange({ ...advancement, domain: event.target.value })}>
              <option value="">Choose one of its domains…</option>
              {chosen.domains.map((domain) => (
                <option key={domain} value={domain}>
                  {capitalize(domain)}
                </option>
              ))}
            </select>
          </label>
          <label className="lu-field">
            <span>Foundation card</span>
            <select className="field" value={advancement.subclass ?? ''} onChange={(event) => onChange({ ...advancement, subclass: event.target.value })}>
              <option value="">Choose a subclass…</option>
              {chosen.subclasses.map((subclass) => (
                <option key={subclass.slug} value={subclass.slug}>
                  {subclass.name}
                </option>
              ))}
            </select>
          </label>
          <p className="lu-note">You gain its class feature{chosen.features.length ? `: ${chosen.features.map((feature) => feature.name).join(', ')}` : ''}. Cards from the new domain go up to half your level.</p>
        </>
      )}
    </div>
  );
}

function CardPicker({ cards, taken, selected, onPick }: { cards: CardOption[]; taken: string[]; selected: string; onPick: (slug: string) => void }) {
  return (
    <div className="lu-cards">
      {cards.map((card) => {
        const used = taken.includes(card.slug) && card.slug !== selected;
        return (
          <button key={card.slug} type="button" className={`dcard${card.slug === selected ? ' is-selected' : ''}`} disabled={used} aria-pressed={card.slug === selected} onClick={() => onPick(card.slug)}>
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
      })}
    </div>
  );
}

function Changes({ before, after }: { before: LevelSummary; after: LevelSummary }) {
  const rows: [string, number, number][] = [
    ['Level', before.level, after.level],
    ['Proficiency', before.proficiency, after.proficiency],
    ['Evasion', before.evasion, after.evasion],
    ['Hit Points', before.hitPointSlots, after.hitPointSlots],
    ['Stress', before.stressSlots, after.stressSlots],
    ['Major threshold', before.majorThreshold, after.majorThreshold],
    ['Severe threshold', before.severeThreshold, after.severeThreshold],
    ...TRAITS.map((trait): [string, number, number] => [trait.name, before.traits[trait.id] ?? 0, after.traits[trait.id] ?? 0]),
  ];
  const newCards = after.domainCards.filter((card) => !before.domainCards.includes(card));
  return (
    <>
      <dl className="lu-changes">
        {rows.map(([label, from, to]) => (
          <div key={label} className={from !== to ? 'changed' : ''}>
            <dt>{label}</dt>
            <dd>{from === to ? to : `${from} → ${to}`}</dd>
          </div>
        ))}
      </dl>
      <h4>Experiences</h4>
      <ul className="lu-list">
        {after.experiences.map((experience, index) => {
          const was = before.experiences[index];
          return (
            <li key={`${experience.name}-${index}`} className={!was || was.bonus !== experience.bonus ? 'changed' : ''}>
              {experience.name} {signed(experience.bonus)}
            </li>
          );
        })}
      </ul>
      {newCards.length > 0 && (
        <>
          <h4>New cards</h4>
          <ul className="lu-list">
            {newCards.map((card) => (
              <li key={card} className="changed">
                {card}
              </li>
            ))}
          </ul>
        </>
      )}
    </>
  );
}

function takenCards(entry: LevelEntry): string[] {
  return [entry.domainCard ?? '', ...(entry.advancements ?? []).map((advancement) => advancement.card ?? '')].filter(Boolean);
}

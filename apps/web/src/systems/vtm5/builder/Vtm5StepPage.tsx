import { useEffect, useState, type ReactNode } from 'react';
import type { StepPageProps } from '../../../builder/builderDefinition';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { CharacterPortrait } from '../../../characters/CharacterPortrait';
import type { Portrait } from '../../../characters/portrait';
import { GROUPS, traitsOfGroup, useVtm5Content, type Trait, type Vtm5Content } from '../content';
import { ATTRIBUTE_SLOTS, countAt, setFocus, useFocus, type Vtm5Build } from './vtm5Build';

type StepBodyProps = { content: Vtm5Content; build: Vtm5Build; change: (patch: Partial<Vtm5Build>) => void };

/** One VtM builder step: the summary strip, then the step's choices; the explanation lives in the right column. */
export function Vtm5StepPage(props: StepPageProps) {
  const content = useVtm5Content();
  const build = (props.build ?? {}) as Vtm5Build;
  if (!content) {
    return <p className="vtm-muted">Loading…</p>;
  }
  const change = (patch: Partial<Vtm5Build>) => props.onBuildChange({ ...build, ...patch });
  const body: Record<string, ReactNode> = {
    basics: (
      <>
        <section className="dbox section">
          <h3>Portrait</h3>
          <PortraitPicker characterId={props.characterId} systemId={props.systemId} portrait={props.portrait} onChange={props.onPortraitChange} />
        </section>
        <Basics content={content} build={build} change={change} name={props.name} onNameChange={props.onNameChange} />
      </>
    ),
    clan: <ClanStep content={content} build={build} change={change} />,
    attributes: <AttributesStep content={content} build={build} change={change} />,
    skills: <SkillsStep content={content} build={build} change={change} />,
    disciplines: <DisciplinesStep content={content} build={build} change={change} />,
    predator: <PredatorStep content={content} build={build} change={change} />,
    advantages: <AdvantagesStep content={content} build={build} change={change} />,
    beliefs: <BeliefsStep content={content} build={build} change={change} />,
    review: (
      <section className="dbox section">
        <h3>Review</h3>
        {props.plan.problems.map((problem) => (
          <p key={problem} className="vtm-problem">{problem}</p>
        ))}
        <p>{props.plan.pendingCount === 0 ? 'Everything is chosen.' : `${props.plan.pendingCount} choices left — the checklist shows where.`}</p>
        <button type="button" className="btn" disabled={props.plan.pendingCount > 0 || props.finishing} onClick={props.onFinish}>
          Finish character
        </button>
      </section>
    ),
  };
  const [title, lead] = STEP_HEADS[props.stepId] ?? ['', ''];
  return (
    <div>
      <SummaryStrip content={content} build={build} name={props.name} pending={props.plan.pendingCount} portrait={props.portrait} systemId={props.systemId} />
      <h1 className="h1">{title}</h1>
      <p className="lead">{lead}</p>
      {body[props.stepId]}
    </div>
  );
}

const STEP_HEADS: Record<string, [string, string]> = {
  basics: ['Basics', 'Who the character is, and how long ago the Embrace was.'],
  clan: ['Clan', 'The blood you were Embraced into: three disciplines that come easily, a bane and a compulsion.'],
  attributes: ['Attributes', 'Click the dots. One attribute at 4, three at 3, four at 2 and one at 1.'],
  skills: ['Skills', 'Pick a distribution, then click the dots. Academics, Craft, Performance and Science need a specialty; one more specialty is free.'],
  disciplines: ['Disciplines', 'One clan discipline at 2 dots, another at 1, and one power for each dot.'],
  predator: ['Predator type', 'How you hunt. It gives a specialty, a discipline dot and some advantages or flaws.'],
  advantages: ['Advantages & flaws', 'Spend 7 points on merits and backgrounds and take at least 2 points of flaws.'],
  beliefs: ['Beliefs', '1 to 3 Convictions, each tied to a mortal Touchstone, plus an Ambition and a Desire.'],
  review: ['Review', 'Everything at once. Fix what is missing, then finish.'],
};

function SummaryStrip({ content, build, name, pending, portrait, systemId }: { content: Vtm5Content; build: Vtm5Build; name: string; pending: number; portrait: Portrait | null; systemId: string }) {
  const attributes = build.attributes ?? {};
  const age = content.ages[build.age ?? 'Neonate'];
  const predator = content.predators.find((p) => p.name === build.predator);
  const stat = (label: string, value: number | string | null) => (
    <div className="strip__stat">
      {label}
      <b className={value === null ? 'pending' : undefined}>{value ?? '?'}</b>
    </div>
  );
  return (
    <div className="dbox strip">
      <CharacterPortrait portrait={portrait} systemId={systemId} className="portrait" />
      <div className="strip__who">
        <b>{name || 'Unnamed'}</b>
        <span>{[build.clan ?? 'No clan yet', build.age, build.generation].filter(Boolean).join(' · ')}</span>
      </div>
      {stat('Health', attributes.Stamina ? attributes.Stamina + 3 : null)}
      {stat('Willpower', attributes.Composure && attributes.Resolve ? attributes.Composure + attributes.Resolve : null)}
      {stat('Humanity', 7 + (predator?.humanity ?? 0) - (build.age === 'Ancilla' ? 1 : 0))}
      {stat('Blood Pot.', build.clan === 'Thin-blooded' ? 0 : (age?.bp ?? 1) + (predator?.bp ?? 0))}
      {stat('XP', age?.xp ?? 0)}
      <span className="strip__left">{pending} choices left</span>
    </div>
  );
}

function Basics({ content, build, change, name, onNameChange }: StepBodyProps & { name: string; onNameChange: (name: string) => void }) {
  const field = (key: 'concept' | 'chronicle' | 'sire', label: string) => (
    <div className="field">
      <label>{label}</label>
      <input value={build[key] ?? ''} onChange={(event) => change({ [key]: event.target.value })} />
      <div className="help">{content.identity[label]}</div>
    </div>
  );
  const age = content.ages[build.age ?? 'Neonate'];
  return (
    <>
      <section className="dbox section">
        <h3>Identity</h3>
        <div className="two">
          <div className="field">
            <label>Name</label>
            <input value={name} onChange={(event) => onNameChange(event.target.value)} />
            <div className="help">What the Kindred call you.</div>
          </div>
          {field('concept', 'Concept')}
          {field('chronicle', 'Chronicle')}
          {field('sire', 'Sire')}
        </div>
      </section>
      <section className="dbox section">
        <h3>
          Age since the Embrace <small>sets Blood Potency, starting XP and generation</small>
        </h3>
        <div className="cgrid cols-3">
          {Object.entries(content.ages).map(([ageName, a]) => (
            <Option
              key={ageName}
              on={build.age === ageName}
              focusKey={['age', ageName]}
              onClick={() => change({ age: ageName, generation: a.generations.at(-1) })}
              title={ageName}
              tag={`BP ${a.bp} · ${a.xp} XP`}
              text={a.short}
            />
          ))}
        </div>
        <h3 className="spaced">Generation</h3>
        <div className="chiprow">
          {(age?.generations ?? []).map((generation) => (
            <button key={generation} type="button" className={`chip${build.generation === generation ? ' on' : ''}`} onClick={() => change({ generation })}>
              {generation}
            </button>
          ))}
        </div>
      </section>
    </>
  );
}

function ClanStep({ content, build, change }: StepBodyProps) {
  const focus = useFocus();
  return (
    <div className="cgrid cols-3">
      {content.clans.map((clan) => {
        const focused = focus?.kind === 'clan' && focus.key === clan.name;
        return (
          <button
            key={clan.name}
            type="button"
            className={`card${build.clan === clan.name ? ' on' : ''}${focused ? ' focus' : ''}`}
            style={{ ['--tint' as string]: clan.tint }}
            onClick={() => {
              setFocus({ kind: 'clan', key: clan.name });
              if (build.clan !== clan.name) change({ clan: clan.name, disciplineTwo: undefined, disciplineOne: undefined, powers: {} });
            }}
          >
            <span className="card__sigil"><i>{clan.name.replace('The ', '')[0]}</i></span>
            <span className="card__source">{clan.source}</span>
            <span className="card__nick">{clan.nick}</span>
            <span className="card__name">{clan.name}</span>
            <span className="card__disc">{clan.disciplines.join(' · ') || (clan.name === 'Caitiff' ? 'Any two' : 'Thin-blood alchemy')}</span>
          </button>
        );
      })}
    </div>
  );
}

function TraitGrid({ traits, values, max, onSet, kind, extra }: {
  traits: Record<string, Trait>;
  values: Record<string, number>;
  max: number;
  onSet: (name: string, dots: number) => void;
  kind: string;
  extra?: (name: string) => ReactNode;
}) {
  const focus = useFocus();
  return (
    <div className="cgrid cols-3">
      {GROUPS.map((group) => (
        <div key={group} className="cgrid">
          <div className="ghead">{group}</div>
          {traitsOfGroup(traits, group).map((name) => {
            const value = values[name] ?? 0;
            return (
              <div key={name} className={`tcell${focus?.kind === kind && focus.key === name ? ' focus' : ''}`}>
                <div className="tcell__top">
                  <button type="button" className="tcell__name" onClick={() => setFocus({ kind, key: name })}>
                    {name}
                  </button>
                  <span className="dots pick">
                    {[1, 2, 3, 4, 5].map((dot) => (
                      <i
                        key={dot}
                        className={`dot${dot <= value ? ' on' : ''}${dot > max ? ' locked' : ''}`}
                        onClick={() => {
                          if (dot <= max) {
                            setFocus({ kind, key: name });
                            onSet(name, value === dot ? 0 : dot);
                          }
                        }}
                      />
                    ))}
                  </span>
                </div>
                <div className="tcell__short">{traits[name].short}</div>
                {extra?.(name)}
              </div>
            );
          })}
        </div>
      ))}
    </div>
  );
}

function Counter({ slots, values }: { slots: Record<string, number>; values: Record<string, number> | undefined }) {
  return (
    <div className="counter">
      {Object.keys(slots)
        .sort((a, b) => Number(b) - Number(a))
        .map((dots) => {
          const have = countAt(values, Number(dots));
          const need = slots[dots];
          return (
            <span key={dots} className={have > need ? 'over' : have === need ? 'ok' : 'warn'}>
              At {dots}: <b>{have}/{need}</b>
            </span>
          );
        })}
    </div>
  );
}

function AttributesStep({ content, build, change }: StepBodyProps) {
  const attributes = build.attributes ?? {};
  return (
    <section className="dbox section">
      <h3>
        Placements <small>click a dot to set it, the same dot again to clear</small>
      </h3>
      <Counter slots={ATTRIBUTE_SLOTS} values={attributes} />
      <TraitGrid traits={content.attributes} values={attributes} max={4} kind="attr" onSet={(name, dots) => change({ attributes: { ...attributes, [name]: dots } })} />
    </section>
  );
}

function SpecialtyField({ content, build, change }: StepBodyProps) {
  const focus = useFocus();
  const skill = focus?.kind === 'skill' ? focus.key : null;
  if (!skill || !build.skills?.[skill]) {
    return null;
  }
  const specialties = build.specialties ?? {};
  const set = (value: string) => change({ specialties: { ...specialties, [skill]: value } });
  return (
    <div className="field spaced">
      <label>
        {skill} specialty {content.freeSpecialtySkills.includes(skill) ? '(required, free)' : '(your one free extra)'}
      </label>
      <input value={specialties[skill] ?? ''} onChange={(event) => set(event.target.value)} />
      <div className="chiprow spaced">
        {(content.skills[skill].specialties ?? []).map((idea) => (
          <button key={idea} type="button" className="chip" onClick={() => set(idea)}>
            {idea}
          </button>
        ))}
      </div>
    </div>
  );
}

function SkillsStep({ content, build, change }: StepBodyProps) {
  const skills = build.skills ?? {};
  const specialties = build.specialties ?? {};
  const distribution = build.distribution ? content.distributions[build.distribution] : undefined;
  const max = distribution ? Math.max(...Object.keys(distribution.slots).map(Number)) : 0;
  return (
    <>
      <section className="dbox section">
        <h3>
          Distribution <small>how wide or deep your training is</small>
        </h3>
        <div className="cgrid cols-3">
          {Object.entries(content.distributions).map(([name, d]) => (
            <Option
              key={name}
              on={build.distribution === name}
              focusKey={['dist', name]}
              onClick={() => change({ distribution: name })}
              title={name}
              tag={Object.entries(d.slots).sort((a, b) => Number(b[0]) - Number(a[0])).map(([v, n]) => `${n}×${v}`).join(' · ')}
              text={d.short}
            />
          ))}
        </div>
      </section>
      {distribution && (
        <section className="dbox section">
          <h3>
            Skills <small>click a skill name to set its specialty in the right column</small>
          </h3>
          <Counter slots={distribution.slots} values={skills} />
          <TraitGrid
            traits={content.skills}
            values={skills}
            max={max}
            kind="skill"
            onSet={(name, dots) => change({ skills: { ...skills, [name]: dots } })}
            extra={(name) =>
              specialties[name] && skills[name] ? (
                <div className="tcell__spec">◆ {specialties[name]}</div>
              ) : skills[name] && content.freeSpecialtySkills.includes(name) ? (
                <div className="tcell__spec need">needs a specialty</div>
              ) : null
            }
          />
          <SpecialtyField content={content} build={build} change={change} />
        </section>
      )}
    </>
  );
}

function DisciplinesStep({ content, build, change }: StepBodyProps) {
  const clan = content.clans.find((c) => c.name === build.clan);
  if (!clan) {
    return <section className="dbox section"><p>Choose a clan first: it decides which disciplines come easily.</p></section>;
  }
  if (clan.name === 'Thin-blooded' && !build.thinBloodMerits?.includes('Thin-blood Alchemist')) {
    return <section className="dbox section"><p>Thin-bloods start with no disciplines. Take the Thin-blood Alchemist merit (Advantages step) to brew formulas instead.</p></section>;
  }
  const thinBlood = clan.name === 'Thin-blooded';
  const names = clan.name === 'Caitiff' ? Object.keys(content.disciplines).filter((d) => d !== 'Thin-Blood Alchemy') : clan.disciplines;
  const dots: Record<string, number> = {};
  if (build.disciplineTwo) dots[build.disciplineTwo] = 2;
  if (build.disciplineOne) dots[build.disciplineOne] = (dots[build.disciplineOne] ?? 0) + 1;
  if (build.predatorDiscipline) dots[build.predatorDiscipline] = (dots[build.predatorDiscipline] ?? 0) + 1;
  if (thinBlood) dots['Thin-Blood Alchemy'] = 1;
  const powers = build.powers ?? {};
  const togglePower = (discipline: string, power: string, level: number) => {
    const chosen = powers[discipline] ?? [];
    setFocus({ kind: 'power', key: `${discipline}|${power}` });
    if (chosen.includes(power)) {
      change({ powers: { ...powers, [discipline]: chosen.filter((p) => p !== power) } });
    } else if (level <= (dots[discipline] ?? 0) && chosen.length < (dots[discipline] ?? 0)) {
      change({ powers: { ...powers, [discipline]: [...chosen, power] } });
    }
  };
  return (
    <>
      {!thinBlood && (
      <section className="dbox section">
        <h3>
          {clan.name === 'Caitiff' ? 'Any two disciplines' : 'Clan disciplines'} <small>one at 2 dots, another at 1</small>
        </h3>
        <div className="cgrid cols-3">
          {names.map((name) => (
            <div key={name} className={`dcard${dots[name] ? ' has' : ''}`}>
              <div className="dcard__head">
                <button type="button" className="dcard__name" onClick={() => setFocus({ kind: 'disc', key: name })}>{name}</button>
                <span className="dcard__meta">{content.disciplines[name].type}</span>
              </div>
              <div className="dcard__short">{content.disciplines[name].short}</div>
              <div className="dbtns">
                <button type="button" className={`dbtn${build.disciplineTwo === name ? ' on' : ''}`}
                  onClick={() => change({ disciplineTwo: build.disciplineTwo === name ? undefined : name, disciplineOne: build.disciplineOne === name ? undefined : build.disciplineOne })}>
                  ●● 2 dots
                </button>
                <button type="button" className={`dbtn${build.disciplineOne === name ? ' on' : ''}`}
                  onClick={() => change({ disciplineOne: build.disciplineOne === name ? undefined : name, disciplineTwo: build.disciplineTwo === name ? undefined : build.disciplineTwo })}>
                  ● 1 dot
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>
      )}
      {dots['Blood Sorcery'] && (
        <section className="dbox section">
          <h3>Free ritual <small>Blood Sorcery starts with one level-1 ritual</small></h3>
          {content.rituals.filter((r) => r.level === 1).map((r) => (
            <button key={r.name} type="button" className="prow" onClick={() => change({ ritual: r.name })}>
              <span className={`tick${build.ritual === r.name ? ' on' : ''}`}>✓</span>
              <span><b>{r.name}</b><small>{r.summary}</small></span>
              <span className="lvl">●</span>
            </button>
          ))}
        </section>
      )}
      {Object.entries(dots).map(([discipline, level]) => (
        <section key={discipline} className="dbox section">
          <h3>
            {discipline} {'●'.repeat(level)} <small>{(powers[discipline] ?? []).length}/{level} powers{discipline === build.predatorDiscipline ? ` · 1 dot from ${build.predator}` : ''}</small>
          </h3>
          {(content.disciplines[discipline]?.powers ?? []).map((power) => {
            const has = (powers[discipline] ?? []).includes(power.name);
            const off = power.level > level || (!has && (powers[discipline] ?? []).length >= level);
            return (
              <button key={power.name} type="button" className={`prow${off && !has ? ' off' : ''}`} onClick={() => togglePower(discipline, power.name, power.level)}>
                <span className={`tick${has ? ' on' : ''}`}>✓</span>
                <span><b>{power.name}</b><small>{power.summary} · {power.cost}</small></span>
                <span className="lvl">{'●'.repeat(power.level)}</span>
              </button>
            );
          })}
        </section>
      ))}
    </>
  );
}

function PredatorStep({ content, build, change }: StepBodyProps) {
  const focus = useFocus();
  const predator = content.predators.find((p) => p.name === build.predator);
  return (
    <>
      <div className="cgrid cols-3">
        {content.predators.map((p) => {
          const blocked = p.notFor?.includes(build.clan ?? '');
          return (
            <button
              key={p.name}
              type="button"
              className={`card card--text${build.predator === p.name ? ' on' : ''}${focus?.kind === 'pred' && focus.key === p.name ? ' focus' : ''}${blocked ? ' blocked' : ''}`}
              onClick={() => {
                setFocus({ kind: 'pred', key: p.name });
                if (!blocked && build.predator !== p.name) change({ predator: p.name, predatorSpecialty: undefined, predatorDiscipline: undefined });
              }}
            >
              <span className="card__name">{p.name}</span>
              <span className="card__disc">{p.disciplines.join(' or ')}</span>
              <span className="card__nick">{p.short}</span>
            </button>
          );
        })}
      </div>
      {predator && (
        <section className="dbox section spaced">
          <h3>{predator.name}: your picks</h3>
          <div className="chiprow">
            {predator.specialties.map((s, index) => (
              <button key={s.skill} type="button" className={`chip${build.predatorSpecialty === index ? ' on' : ''}`} onClick={() => change({ predatorSpecialty: index })}>
                {s.skill} ({s.specialty})
              </button>
            ))}
          </div>
          <div className="chiprow spaced">
            {predator.disciplines.map((d) => {
              const disabled = d === 'Blood Sorcery' && !content.sorceryClans.includes(build.clan ?? '');
              return (
                <button key={d} type="button" disabled={disabled} className={`chip${build.predatorDiscipline === d ? ' on' : ''}`} onClick={() => change({ predatorDiscipline: d })}>
                  {d}
                </button>
              );
            })}
          </div>
        </section>
      )}
    </>
  );
}

function AdvantagesStep({ content, build, change }: StepBodyProps) {
  const advantages = build.advantages ?? {};
  const loresheets = build.loresheets ?? {};
  const lorePoints = Object.values(loresheets).flat().reduce((total, level) => total + level, 0);
  const spent = (flaw: boolean) =>
    content.advantages.filter((a) => (a.kind === 'Flaw') === flaw).reduce((total, a) => total + (advantages[a.name] ?? 0), 0) + (flaw ? 0 : lorePoints + (build.coterieContribution ?? 0));
  const toggleLore = (name: string, level: number) => {
    const owned = loresheets[name] ?? [];
    const next = owned.includes(level) ? owned.filter((l) => l !== level) : [...owned, level];
    const copy = { ...loresheets, [name]: next };
    if (next.length === 0) delete copy[name];
    change({ loresheets: copy });
  };
  const loreRows = content.loresheets
    .filter((l) => !l.restriction || l.restriction === build.clan)
    .map((l) => (
      <div key={l.name} className="arow">
        <span>
          <b>{l.name}</b>
          <small>{l.book}{l.restriction ? ` · ${l.restriction} only` : ''} · each dot is its own entry, costing its level in points</small>
        </span>
        <span className="chiprow">
          {l.levels.map((entry, index) => (
            <button key={entry} type="button" title={entry} className={`chip${(loresheets[l.name] ?? []).includes(index + 1) ? ' on' : ''}`} onClick={() => toggleLore(l.name, index + 1)}>
              {'●'.repeat(index + 1)}
            </button>
          ))}
        </span>
      </div>
    ));
  const budget = 7 + (build.age === 'Ancilla' ? 2 : 0);
  const flawMin = 2 + (build.age === 'Ancilla' ? 2 : 0);
  const set = (name: string, dots: number) => {
    setFocus({ kind: 'adv', key: name });
    change({ advantages: { ...advantages, [name]: advantages[name] === dots ? 0 : dots } });
  };
  const rows = (flaw: boolean) =>
    content.advantages.filter((a) => (a.kind === 'Flaw') === flaw).map((a) => (
      <div key={a.name} className="arow">
        <span>
          <b onClick={() => setFocus({ kind: 'adv', key: a.name })}>{a.name}</b>
          <small>{a.kind} · {a.short}</small>
        </span>
        {a.fixed ? (
          <button type="button" className={`chip${advantages[a.name] ? ' on' : ''}`} onClick={() => set(a.name, a.fixed!)}>
            {'●'.repeat(a.fixed)} {advantages[a.name] ? 'Taken' : 'Take'}
          </button>
        ) : (
          <span className="dots pick">
            {[1, 2, 3, 4, 5].map((dot) => (
              <i key={dot} className={`dot${dot <= (advantages[a.name] ?? 0) ? ' on' : ''}${dot > a.max ? ' locked' : ''}`} onClick={() => dot <= a.max && set(a.name, dot)} />
            ))}
          </span>
        )}
      </div>
    ));
  const predator = content.predators.find((p) => p.name === build.predator);
  const meritsDone = spent(false) === budget;
  const flawsDone = spent(true) >= flawMin;
  return (
    <>
      {predator && <div className="free"><b>{predator.name}</b> already gives: {predator.extras}.</div>}
      {build.clan === 'Thin-blooded' && <ThinBloodPicks content={content} build={build} change={change} />}
      <BudgetTabs
        tabs={[
          { title: 'Merits & backgrounds', status: `${spent(false)}/${budget}`, done: meritsDone, hint: 'Spend exactly 7 points (9 for an ancilla). Click a name to read it in the right column.', body: rows(false) },
          { title: 'Flaws', status: `${spent(true)}/${flawMin}+`, done: flawsDone, hint: 'Take at least 2 points (4 for an ancilla). More flaws are allowed.', body: rows(true) },
          {
            title: 'Coterie',
            status: `${build.coterieContribution ?? 0} pts`,
            done: false,
            hint: 'Optional. Points given to your coterie (up to 7) come from the same advantage points; with one dot from every member they pay for the domain and shared backgrounds, set on the sheet.',
            body: (
              <div className="arow">
                <span><b>Contribute to the coterie</b><small>0 to 7 advantage points</small></span>
                <span className="dots pick">
                  {[1, 2, 3, 4, 5, 6, 7].map((point) => (
                    <i key={point} className={`dot${point <= (build.coterieContribution ?? 0) ? ' on' : ''}`} onClick={() => change({ coterieContribution: build.coterieContribution === point ? 0 : point })} />
                  ))}
                </span>
              </div>
            ),
          },
          { title: 'Loresheets', status: `${lorePoints} pts`, done: false, hint: 'Optional. Paid from the same advantage points: a level-3 entry costs 3. Some are for one clan only. Hover a dot to see its entry.', body: loreRows },
        ]}
      />
    </>
  );
}

function ThinBloodPicks({ content, build, change }: StepBodyProps) {
  const merits = build.thinBloodMerits ?? [];
  const flaws = build.thinBloodFlaws ?? [];
  const toggle = (list: string[], name: string, key: 'thinBloodMerits' | 'thinBloodFlaws', max: number) =>
    change({ [key]: list.includes(name) ? list.filter((n) => n !== name) : list.length < max ? [...list, name] : list });
  return (
    <section className="dbox section">
      <h3>Thin-blood merits & flaws <small>1 to 3 merits, the same number of flaws</small></h3>
      <div className="chiprow">
        {content.thinBloodMerits.map((m) => (
          <button key={m.name} type="button" title={m.short} className={`chip${merits.includes(m.name) ? ' on' : ''}`} onClick={() => toggle(merits, m.name, 'thinBloodMerits', 3)}>{m.name}</button>
        ))}
      </div>
      <p className="help spaced">{content.thinBloodMerits.filter((m) => merits.includes(m.name)).map((m) => `${m.name}: ${m.short}`).join(' · ')}</p>
      <div className="chiprow">
        {content.thinBloodFlaws.map((f) => (
          <button key={f.name} type="button" title={f.short} className={`chip${flaws.includes(f.name) ? ' on' : ''}`} onClick={() => toggle(flaws, f.name, 'thinBloodFlaws', Math.max(1, merits.length))}>{f.name}</button>
        ))}
      </div>
      <p className="help spaced">{content.thinBloodFlaws.filter((f) => flaws.includes(f.name)).map((f) => `${f.name}: ${f.short}`).join(' · ')}</p>
      {merits.includes('Thin-blood Alchemist') && (
        <>
          <h3>Alchemy method <small>the attribute you distill with; the formula is picked on the Disciplines step</small></h3>
          <div className="cgrid cols-3">
            {content.alchemyMethods.map((m) => (
              <button key={m.name} type="button" className={`opt${build.alchemyMethod === m.name ? ' on' : ''}`} onClick={() => change({ alchemyMethod: m.name })}>
                <b>{m.name}</b><i>{m.attribute} + Alchemy</i><span>{m.short}</span>
              </button>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

function BeliefsStep({ content, build, change }: StepBodyProps) {
  const convictions = build.convictions?.length ? build.convictions : [{ conviction: '', touchstone: '' }];
  const edit = (index: number, key: 'conviction' | 'touchstone', value: string) =>
    change({ convictions: convictions.map((c, i) => (i === index ? { ...c, [key]: value } : c)) });
  return (
    <>
      <section className="dbox section">
        <h3>Convictions & touchstones <small>{convictions.length}/3</small></h3>
        {convictions.map((c, index) => (
          <div key={index} className="two">
            <div className="field">
              <label>Conviction {index + 1}</label>
              <input value={c.conviction} onFocus={() => setFocus({ kind: 'conviction', key: '' })} onChange={(event) => edit(index, 'conviction', event.target.value)} placeholder="e.g. Never destroy a work of art." />
              <div className="help">A moral line your character won't cross, even as a monster. Breaking it costs Humanity.</div>
            </div>
            <div className="field">
              <label>Touchstone</label>
              <input value={c.touchstone} onChange={(event) => edit(index, 'touchstone', event.target.value)} placeholder="e.g. Léa, gallery owner" />
              <div className="help">A living mortal who embodies that conviction. Losing them shakes your Humanity.</div>
            </div>
          </div>
        ))}
        {convictions.length < 3 && (
          <button type="button" className="chip" onClick={() => change({ convictions: [...convictions, { conviction: '', touchstone: '' }] })}>
            + Add a conviction
          </button>
        )}
      </section>
      <section className="dbox section">
        <h3>Ambition & desire</h3>
        <div className="field">
          <label>Ambition</label>
          <input value={build.ambition ?? ''} onChange={(event) => change({ ambition: event.target.value })} placeholder="e.g. A seat at the Prince's table" />
          <div className="help">{content.identity.Ambition} Required.</div>
        </div>
        <div className="field">
          <label>Desire</label>
          <input value={build.desire ?? ''} onChange={(event) => change({ desire: event.target.value })} placeholder="e.g. Restore the lost fresco" />
          <div className="help">{content.identity.Desire} Optional; it changes from session to session.</div>
        </div>
      </section>
    </>
  );
}

function Option({ on, focusKey, onClick, title, tag, text }: { on: boolean; focusKey: [string, string]; onClick: () => void; title: string; tag: string; text: string }) {
  const focus = useFocus();
  const focused = focus?.kind === focusKey[0] && focus.key === focusKey[1];
  return (
    <button type="button" className={`opt${on ? ' on' : ''}${focused ? ' focus' : ''}`} onClick={() => { setFocus({ kind: focusKey[0], key: focusKey[1] }); onClick(); }}>
      <b>{title}</b>
      <i>{tag}</i>
      <span>{text}</span>
    </button>
  );
}

type BudgetTab = { title: string; status: string; done: boolean; hint: string; body: ReactNode };

/** Point budgets as tabs, both always in view; when the open one is met, the next unmet one opens. */
function BudgetTabs({ tabs }: { tabs: BudgetTab[] }) {
  const firstOpen = Math.max(0, tabs.findIndex((tab) => !tab.done));
  const [open, setOpen] = useState(firstOpen);
  const doneKey = tabs.map((tab) => tab.done).join();
  useEffect(() => {
    setOpen((current) => (tabs[current]?.done ? firstOpen : current));
  }, [doneKey]);
  return (
    <section className="dbox section budget">
      <div className="budget__tabs" role="tablist">
        {tabs.map((tab, index) => (
          <button key={tab.title} type="button" role="tab" aria-selected={index === open} className={`budget__tab${index === open ? ' on' : ''}`} onClick={() => setOpen(index)}>
            <span>{tab.title}</span>
            <span className={`budget__status${tab.done ? ' ok' : ''}`}>{tab.done ? '✓ ' : ''}{tab.status}</span>
          </button>
        ))}
      </div>
      <p className="help">{tabs[open].hint}</p>
      {tabs[open].body}
    </section>
  );
}
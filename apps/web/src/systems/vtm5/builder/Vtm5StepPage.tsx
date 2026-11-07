import { useEffect, useState, type ReactNode } from 'react';
import type { StepPageProps } from '../../../builder/builderDefinition';
import { PortraitPicker } from '../../../builder/PortraitPicker';
import { Spinner } from '../../../loading/Spinner';
import { CharacterPortrait } from '../../../characters/CharacterPortrait';
import type { Portrait } from '../../../characters/portrait';
import { GROUPS, termOf, traitsOfGroup, useVtm5Content, type Trait, type Vtm5Content } from '../content';
import { useVtm5Text } from '../locales/vtm5Messages';
import { ATTRIBUTE_SLOTS, countAt, setFocus, useFocus, useGeneration, useStepLabel, type Vtm5Build } from './vtm5Build';

type StepBodyProps = { content: Vtm5Content; build: Vtm5Build; change: (patch: Partial<Vtm5Build>) => void };

const STEP_LEADS = ['basics', 'clan', 'attributes', 'skills', 'disciplines', 'predator', 'advantages', 'beliefs', 'review'];

/** One VtM builder step: the summary strip, then the step's choices; the explanation lives in the right column. */
export function Vtm5StepPage(props: StepPageProps) {
  const t = useVtm5Text();
  const stepLabel = useStepLabel();
  const content = useVtm5Content();
  const build = (props.build ?? {}) as Vtm5Build;
  if (!content) {
    return <Spinner className="is-centred" />;
  }
  const change = (patch: Partial<Vtm5Build>) => props.onBuildChange({ ...build, ...patch });
  const body: Record<string, ReactNode> = {
    basics: (
      <>
        <section className="dbox section">
          <h3>{t('builder.portrait')}</h3>
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
        <h3>{t('steps.review')}</h3>
        {props.plan.problems.map((problem) => (
          <p key={problem} className="vtm-problem">{problem}</p>
        ))}
        <p>{props.plan.pendingCount === 0 ? t('builder.allChosen') : t('builder.choicesLeft', { count: props.plan.pendingCount })}</p>
        <button type="button" className="btn" disabled={props.plan.pendingCount > 0 || props.finishing} onClick={props.onFinish}>
          {t('builder.finish')}
        </button>
      </section>
    ),
  };
  const known = STEP_LEADS.includes(props.stepId);
  return (
    <div>
      <SummaryStrip content={content} build={build} name={props.name} pending={props.plan.pendingCount} portrait={props.portrait} systemId={props.systemId} />
      <h1 className="h1">{known ? stepLabel(props.stepId) : ''}</h1>
      <p className="lead">{known ? t.dynamic(`builder.heads.${props.stepId}`, '') : ''}</p>
      {body[props.stepId]}
    </div>
  );
}

function SummaryStrip({ content, build, name, pending, portrait, systemId }: { content: Vtm5Content; build: Vtm5Build; name: string; pending: number; portrait: Portrait | null; systemId: string }) {
  const t = useVtm5Text();
  const generationOf = useGeneration();
  const term = (key: string) => termOf(content, key);
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
        <b>{name || t('builder.unnamed')}</b>
        <span>{[build.clan ? term(build.clan) : t('builder.noClanYet'), build.age && term(build.age), build.generation && generationOf(build.generation)].filter(Boolean).join(' · ')}</span>
      </div>
      {stat(term('Health'), attributes.Stamina ? attributes.Stamina + 3 : null)}
      {stat(term('Willpower'), attributes.Composure && attributes.Resolve ? attributes.Composure + attributes.Resolve : null)}
      {stat(term('Humanity'), 7 + (predator?.humanity ?? 0) - (build.age === 'Ancilla' ? 1 : 0))}
      {stat(t('builder.bloodPotencyShort'), build.clan === 'Thin-blooded' ? 0 : (age?.bp ?? 1) + (predator?.bp ?? 0))}
      {stat(t('builder.xp'), age?.xp ?? 0)}
      <span className="strip__left">{t('builder.pendingChoices', { count: pending })}</span>
    </div>
  );
}

function Basics({ content, build, change, name, onNameChange }: StepBodyProps & { name: string; onNameChange: (name: string) => void }) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const field = (key: 'concept' | 'chronicle' | 'sire', label: string) => (
    <div className="field">
      <label>{term(label)}</label>
      <input value={build[key] ?? ''} onChange={(event) => change({ [key]: event.target.value })} />
      <div className="help">{content.identity[label]}</div>
    </div>
  );
  const generationOf = useGeneration();
  const age = content.ages[build.age ?? 'Neonate'];
  return (
    <>
      <section className="dbox section">
        <h3>{t('builder.identity')}</h3>
        <div className="two">
          <div className="field">
            <label>{t('builder.name')}</label>
            <input value={name} onChange={(event) => onNameChange(event.target.value)} />
            <div className="help">{t('builder.nameHelp')}</div>
          </div>
          {field('concept', 'Concept')}
          {field('chronicle', 'Chronicle')}
          {field('sire', 'Sire')}
        </div>
      </section>
      <section className="dbox section">
        <h3>
          {t('terms.ageSinceEmbrace')} <small>{t('builder.ageHelp')}</small>
        </h3>
        <div className="cgrid cols-3">
          {Object.entries(content.ages).map(([ageName, a]) => (
            <Option
              key={ageName}
              on={build.age === ageName}
              focusKey={['age', ageName]}
              onClick={() => change({ age: ageName, generation: a.generations.at(-1) })}
              title={term(ageName)}
              tag={t('builder.ageTag', { bp: a.bp, xp: a.xp })}
              text={a.short}
            />
          ))}
        </div>
        <h3 className="spaced">{term('Generation')}</h3>
        <div className="chiprow">
          {(age?.generations ?? []).map((generation) => (
            <button key={generation} type="button" className={`chip${build.generation === generation ? ' on' : ''}`} onClick={() => change({ generation })}>
              {generationOf(generation)}
            </button>
          ))}
        </div>
      </section>
    </>
  );
}

function ClanStep({ content, build, change }: StepBodyProps) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const focus = useFocus();
  return (
    <div className="cgrid cols-3">
      {content.clans.map((clan) => {
        const focused = focus?.kind === 'clan' && focus.key === clan.name;
        const shownName = term(clan.name);
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
            <span className="card__sigil"><i>{shownName.replace('The ', '')[0]}</i></span>
            <span className="card__source">{clan.source}</span>
            <span className="card__nick">{clan.nick}</span>
            <span className="card__name">{shownName}</span>
            <span className="card__disc">{clan.disciplines.map(term).join(' · ') || (clan.name === 'Caitiff' ? t('builder.anyTwo') : t('builder.thinBloodAlchemy'))}</span>
          </button>
        );
      })}
    </div>
  );
}

function TraitGrid({ content, traits, values, max, onSet, kind, extra }: {
  content: Vtm5Content;
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
          <div className="ghead">{termOf(content, group)}</div>
          {traitsOfGroup(traits, group).map((name) => {
            const value = values[name] ?? 0;
            return (
              <div key={name} className={`tcell${focus?.kind === kind && focus.key === name ? ' focus' : ''}`}>
                <div className="tcell__top">
                  <button type="button" className="tcell__name" onClick={() => setFocus({ kind, key: name })}>
                    {termOf(content, name)}
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
  const t = useVtm5Text();
  return (
    <div className="counter">
      {Object.keys(slots)
        .sort((a, b) => Number(b) - Number(a))
        .map((dots) => {
          const have = countAt(values, Number(dots));
          const need = slots[dots];
          return (
            <span key={dots} className={have > need ? 'over' : have === need ? 'ok' : 'warn'}>
              {t('builder.atDots', { dots })} <b>{have}/{need}</b>
            </span>
          );
        })}
    </div>
  );
}

function AttributesStep({ content, build, change }: StepBodyProps) {
  const t = useVtm5Text();
  const attributes = build.attributes ?? {};
  return (
    <section className="dbox section">
      <h3>
        {t('builder.placements')} <small>{t('builder.placementsHelp')}</small>
      </h3>
      <Counter slots={ATTRIBUTE_SLOTS} values={attributes} />
      <TraitGrid content={content} traits={content.attributes} values={attributes} max={4} kind="attr" onSet={(name, dots) => change({ attributes: { ...attributes, [name]: dots } })} />
    </section>
  );
}

function SpecialtyField({ content, build, change }: StepBodyProps) {
  const t = useVtm5Text();
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
        {t(content.freeSpecialtySkills.includes(skill) ? 'builder.specialtyRequired' : 'builder.specialtyFree', { skill: termOf(content, skill) })}
      </label>
      <input value={specialties[skill] ?? ''} onChange={(event) => set(event.target.value)} />
      <div className="chiprow spaced">
        {(content.skills[skill].specialties ?? []).map((idea) => (
          <button key={idea} type="button" className="chip" onClick={() => set(termOf(content, idea))}>
            {termOf(content, idea)}
          </button>
        ))}
      </div>
    </div>
  );
}

function SkillsStep({ content, build, change }: StepBodyProps) {
  const t = useVtm5Text();
  const skills = build.skills ?? {};
  const specialties = build.specialties ?? {};
  const distribution = build.distribution ? content.distributions[build.distribution] : undefined;
  const max = distribution ? Math.max(...Object.keys(distribution.slots).map(Number)) : 0;
  return (
    <>
      <section className="dbox section">
        <h3>
          {t('builder.distribution')} <small>{t('builder.distributionHelp')}</small>
        </h3>
        <div className="cgrid cols-3">
          {Object.entries(content.distributions).map(([name, d]) => (
            <Option
              key={name}
              on={build.distribution === name}
              focusKey={['dist', name]}
              onClick={() => change({ distribution: name })}
              title={termOf(content, name)}
              tag={Object.entries(d.slots).sort((a, b) => Number(b[0]) - Number(a[0])).map(([v, n]) => `${n}×${v}`).join(' · ')}
              text={d.short}
            />
          ))}
        </div>
      </section>
      {distribution && (
        <section className="dbox section">
          <h3>
            {t('terms.skills')} <small>{t('builder.skillsHelp')}</small>
          </h3>
          <Counter slots={distribution.slots} values={skills} />
          <TraitGrid
            content={content}
            traits={content.skills}
            values={skills}
            max={max}
            kind="skill"
            onSet={(name, dots) => change({ skills: { ...skills, [name]: dots } })}
            extra={(name) =>
              specialties[name] && skills[name] ? (
                <div className="tcell__spec">◆ {specialties[name]}</div>
              ) : skills[name] && content.freeSpecialtySkills.includes(name) ? (
                <div className="tcell__spec need">{t('builder.needsSpecialty')}</div>
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
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const clan = content.clans.find((c) => c.name === build.clan);
  if (!clan) {
    return <section className="dbox section"><p>{t('builder.chooseClanFirst')}</p></section>;
  }
  if (clan.name === 'Thin-blooded' && !build.thinBloodMerits?.includes('Thin-blood Alchemist')) {
    return <section className="dbox section"><p>{t('builder.thinBloodNoDisciplines')}</p></section>;
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
          {clan.name === 'Caitiff' ? t('builder.anyTwoDisciplines') : t('builder.clanDisciplines')} <small>{t('builder.disciplineDotsHelp')}</small>
        </h3>
        <div className="cgrid cols-3">
          {names.map((name) => (
            <div key={name} className={`dcard${dots[name] ? ' has' : ''}`}>
              <div className="dcard__head">
                <button type="button" className="dcard__name" onClick={() => setFocus({ kind: 'disc', key: name })}>{term(name)}</button>
                <span className="dcard__meta">{term(content.disciplines[name].type)}</span>
              </div>
              <div className="dcard__short">{content.disciplines[name].short}</div>
              <div className="dbtns">
                <button type="button" className={`dbtn${build.disciplineTwo === name ? ' on' : ''}`}
                  onClick={() => change({ disciplineTwo: build.disciplineTwo === name ? undefined : name, disciplineOne: build.disciplineOne === name ? undefined : build.disciplineOne })}>
                  {t('builder.twoDots')}
                </button>
                <button type="button" className={`dbtn${build.disciplineOne === name ? ' on' : ''}`}
                  onClick={() => change({ disciplineOne: build.disciplineOne === name ? undefined : name, disciplineTwo: build.disciplineTwo === name ? undefined : build.disciplineTwo })}>
                  {t('builder.oneDot')}
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>
      )}
      {dots['Blood Sorcery'] && (
        <section className="dbox section">
          <h3>{t('builder.freeRitual')} <small>{t('builder.freeRitualHelp')}</small></h3>
          {content.rituals.filter((r) => r.level === 1).map((r) => (
            <button key={r.name} type="button" className="prow" onClick={() => change({ ritual: r.name })}>
              <span className={`tick${build.ritual === r.name ? ' on' : ''}`}>✓</span>
              <span><b>{term(r.name)}</b><small>{r.summary}</small></span>
              <span className="lvl">●</span>
            </button>
          ))}
        </section>
      )}
      {Object.entries(dots).map(([discipline, level]) => (
        <section key={discipline} className="dbox section">
          <h3>
            {term(discipline)} {'●'.repeat(level)}{' '}
            <small>
              {t('builder.powersCount', { chosen: (powers[discipline] ?? []).length, level })}
              {discipline === build.predatorDiscipline ? t('builder.dotFromPredator', { predator: term(build.predator ?? '') }) : ''}
            </small>
          </h3>
          {(content.disciplines[discipline]?.powers ?? []).map((power) => {
            const has = (powers[discipline] ?? []).includes(power.name);
            const off = power.level > level || (!has && (powers[discipline] ?? []).length >= level);
            return (
              <button key={power.name} type="button" className={`prow${off && !has ? ' off' : ''}`} onClick={() => togglePower(discipline, power.name, power.level)}>
                <span className={`tick${has ? ' on' : ''}`}>✓</span>
                <span><b>{term(power.name)}</b><small>{power.summary} · {power.cost}</small></span>
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
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
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
              <span className="card__name">{term(p.name)}</span>
              <span className="card__disc">{p.disciplines.map(term).join(t('explain.or'))}</span>
              <span className="card__nick">{p.short}</span>
            </button>
          );
        })}
      </div>
      {predator && (
        <section className="dbox section spaced">
          <h3>{t('builder.yourPicks', { predator: term(predator.name) })}</h3>
          <div className="chiprow">
            {predator.specialties.map((s, index) => (
              <button key={s.skill} type="button" className={`chip${build.predatorSpecialty === index ? ' on' : ''}`} onClick={() => change({ predatorSpecialty: index })}>
                {term(s.skill)} ({term(s.specialty)})
              </button>
            ))}
          </div>
          <div className="chiprow spaced">
            {predator.disciplines.map((d) => {
              const disabled = d === 'Blood Sorcery' && !content.sorceryClans.includes(build.clan ?? '');
              return (
                <button key={d} type="button" disabled={disabled} className={`chip${build.predatorDiscipline === d ? ' on' : ''}`} onClick={() => change({ predatorDiscipline: d })}>
                  {term(d)}
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
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
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
          <b>{term(l.name)}</b>
          <small>{l.book}{l.restriction ? t('builder.loreOnly', { clan: term(l.restriction) }) : ''}{t('builder.loreEntryHelp')}</small>
        </span>
        <span className="chiprow">
          {l.levels.map((entry, index) => (
            <button key={entry} type="button" title={term(entry)} className={`chip${(loresheets[l.name] ?? []).includes(index + 1) ? ' on' : ''}`} onClick={() => toggleLore(l.name, index + 1)}>
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
          <b onClick={() => setFocus({ kind: 'adv', key: a.name })}>{term(a.name)}</b>
          <small>{term(a.kind)} · {a.short}</small>
        </span>
        {a.fixed ? (
          <button type="button" className={`chip${advantages[a.name] ? ' on' : ''}`} onClick={() => set(a.name, a.fixed!)}>
            {'●'.repeat(a.fixed)} {advantages[a.name] ? t('builder.taken') : t('builder.take')}
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
      {predator && <div className="free">{t('builder.predatorGives', { predator: term(predator.name), extras: predator.extras })}</div>}
      {build.clan === 'Thin-blooded' && <ThinBloodPicks content={content} build={build} change={change} />}
      <BudgetTabs
        tabs={[
          { id: 'merits', title: t('terms.meritsAndBackgrounds'), status: `${spent(false)}/${budget}`, done: meritsDone, hint: t('builder.budgetMeritsHint'), body: rows(false) },
          { id: 'flaws', title: t('terms.flaws'), status: `${spent(true)}/${flawMin}+`, done: flawsDone, hint: t('builder.budgetFlawsHint'), body: rows(true) },
          {
            id: 'coterie',
            title: t('terms.coterie'),
            status: t('builder.points', { count: build.coterieContribution ?? 0 }),
            done: false,
            hint: t('builder.budgetCoterieHint'),
            body: (
              <div className="arow">
                <span><b>{t('builder.contribute')}</b><small>{t('builder.contributeHelp')}</small></span>
                <span className="dots pick">
                  {[1, 2, 3, 4, 5, 6, 7].map((point) => (
                    <i key={point} className={`dot${point <= (build.coterieContribution ?? 0) ? ' on' : ''}`} onClick={() => change({ coterieContribution: build.coterieContribution === point ? 0 : point })} />
                  ))}
                </span>
              </div>
            ),
          },
          { id: 'lore', title: t('terms.loresheets'), status: t('builder.points', { count: lorePoints }), done: false, hint: t('builder.budgetLoreHint'), body: loreRows },
        ]}
      />
    </>
  );
}

function ThinBloodPicks({ content, build, change }: StepBodyProps) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const merits = build.thinBloodMerits ?? [];
  const flaws = build.thinBloodFlaws ?? [];
  const toggle = (list: string[], name: string, key: 'thinBloodMerits' | 'thinBloodFlaws', max: number) =>
    change({ [key]: list.includes(name) ? list.filter((n) => n !== name) : list.length < max ? [...list, name] : list });
  return (
    <section className="dbox section">
      <h3>{t('terms.thinBloodMeritsAndFlaws')} <small>{t('builder.thinBloodPicksHelp')}</small></h3>
      <div className="chiprow">
        {content.thinBloodMerits.map((m) => (
          <button key={m.name} type="button" title={m.short} className={`chip${merits.includes(m.name) ? ' on' : ''}`} onClick={() => toggle(merits, m.name, 'thinBloodMerits', 3)}>{term(m.name)}</button>
        ))}
      </div>
      <p className="help spaced">{content.thinBloodMerits.filter((m) => merits.includes(m.name)).map((m) => `${term(m.name)}: ${m.short}`).join(' · ')}</p>
      <div className="chiprow">
        {content.thinBloodFlaws.map((f) => (
          <button key={f.name} type="button" title={f.short} className={`chip${flaws.includes(f.name) ? ' on' : ''}`} onClick={() => toggle(flaws, f.name, 'thinBloodFlaws', Math.max(1, merits.length))}>{term(f.name)}</button>
        ))}
      </div>
      <p className="help spaced">{content.thinBloodFlaws.filter((f) => flaws.includes(f.name)).map((f) => `${term(f.name)}: ${f.short}`).join(' · ')}</p>
      {merits.includes('Thin-blood Alchemist') && (
        <>
          <h3>{t('terms.alchemyMethod')} <small>{t('builder.alchemyMethodHelp')}</small></h3>
          <div className="cgrid cols-3">
            {content.alchemyMethods.map((m) => (
              <button key={m.name} type="button" className={`opt${build.alchemyMethod === m.name ? ' on' : ''}`} onClick={() => change({ alchemyMethod: m.name })}>
                <b>{term(m.name)}</b><i>{t('builder.alchemyPool', { attribute: term(m.attribute) })}</i><span>{m.short}</span>
              </button>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

function BeliefsStep({ content, build, change }: StepBodyProps) {
  const t = useVtm5Text();
  const term = (key: string) => termOf(content, key);
  const convictions = build.convictions?.length ? build.convictions : [{ conviction: '', touchstone: '' }];
  const edit = (index: number, key: 'conviction' | 'touchstone', value: string) =>
    change({ convictions: convictions.map((c, i) => (i === index ? { ...c, [key]: value } : c)) });
  return (
    <>
      <section className="dbox section">
        <h3>{t('terms.convictionsAndTouchstones')} <small>{t('builder.convictionsHead', { count: convictions.length })}</small></h3>
        {convictions.map((c, index) => (
          <div key={index} className="two">
            <div className="field">
              <label>{t('builder.convictionNumber', { number: index + 1 })}</label>
              <input value={c.conviction} onFocus={() => setFocus({ kind: 'conviction', key: '' })} onChange={(event) => edit(index, 'conviction', event.target.value)} placeholder={t('builder.convictionPlaceholder')} />
              <div className="help">{t('builder.convictionHelp')}</div>
            </div>
            <div className="field">
              <label>{t('terms.touchstone')}</label>
              <input value={c.touchstone} onChange={(event) => edit(index, 'touchstone', event.target.value)} placeholder={t('builder.touchstonePlaceholder')} />
              <div className="help">{t('builder.touchstoneHelp')}</div>
            </div>
          </div>
        ))}
        {convictions.length < 3 && (
          <button type="button" className="chip" onClick={() => change({ convictions: [...convictions, { conviction: '', touchstone: '' }] })}>
            {t('builder.addConviction')}
          </button>
        )}
      </section>
      <section className="dbox section">
        <h3>{t('builder.ambitionAndDesire')}</h3>
        <div className="field">
          <label>{term('Ambition')}</label>
          <input value={build.ambition ?? ''} onChange={(event) => change({ ambition: event.target.value })} placeholder={t('builder.ambitionPlaceholder')} />
          <div className="help">{content.identity.Ambition} {t('builder.required')}</div>
        </div>
        <div className="field">
          <label>{term('Desire')}</label>
          <input value={build.desire ?? ''} onChange={(event) => change({ desire: event.target.value })} placeholder={t('builder.desirePlaceholder')} />
          <div className="help">{content.identity.Desire} {t('builder.desireOptional')}</div>
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

type BudgetTab = { id: string; title: string; status: string; done: boolean; hint: string; body: ReactNode };

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
          <button key={tab.id} type="button" role="tab" aria-selected={index === open} className={`budget__tab${index === open ? ' on' : ''}`} onClick={() => setOpen(index)}>
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

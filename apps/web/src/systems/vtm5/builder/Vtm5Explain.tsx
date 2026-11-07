import type { ReactNode } from 'react';
import type { Translate } from '../../../language/language';
import { poolOf, termOf, useVtm5Content, type Vtm5Content } from '../content';
import { useVtm5Text } from '../locales/vtm5Messages';
import type vtm5 from '../locales/vtm5.en.json';
import { useFocus, type Focus, type Vtm5Build } from './vtm5Build';

type T = Translate<typeof vtm5>;

/** The right column of the VtM builder: what the option in focus is and what picking it changes. */
export function Vtm5Explain({ build, stepId }: { build: unknown; stepId: string }) {
  const t = useVtm5Text();
  const content = useVtm5Content();
  const chosen = useFocus();
  const focus = chosen ?? defaultFocus(stepId, (build ?? {}) as Vtm5Build, content);
  return (
    <aside className="dbox explain-col sticky">
        <div className="explain-col__title">{t('explain.title')}</div>
        <div className="explain">{content ? explain(t, content, focus, (build ?? {}) as Vtm5Build) : null}</div>
    </aside>
  );
}

function explain(t: T, content: Vtm5Content, focus: Focus, build: Vtm5Build): ReactNode {
  const term = (name: string) => termOf(content, name);
  if (!focus) {
    return <p className="vtm-muted">{t('explain.empty')}</p>;
  }
  const { kind, key } = focus;
  if (kind === 'clan') {
    const clan = content.clans.find((c) => c.name === key);
    if (!clan) return null;
    return (
      <>
        <Kind text={t('explain.clanKind', { source: term(clan.source) })} />
        <h2>{term(clan.name)}</h2>
        <div className="sub">{clan.nick}</div>
        <p>{clan.long}</p>
        <p className="vtm-muted">{t('explain.playFor', { play: clan.play })}</p>
        <h4>{t('builder.clanDisciplines')}</h4>
        <div className="pills">
          {clan.disciplines.map((d) => (
            <div key={d} className="pill"><b>{term(d)}</b><span>{content.disciplines[d].short}</span></div>
          ))}
        </div>
        <h4>{t('terms.bane')} · {term(clan.bane.name)}</h4>
        <p>{clan.bane.text}</p>
        <h4>{t('terms.compulsion')} · {term(clan.compulsion.name)}</h4>
        <p>{clan.compulsion.text}</p>
      </>
    );
  }
  if (kind === 'attr' || kind === 'skill') {
    const trait = (kind === 'attr' ? content.attributes : content.skills)[key];
    const scale = kind === 'attr' ? content.attributeScale.slice(0, 4).map(term) : content.skillScale;
    const value = (kind === 'attr' ? build.attributes : build.skills)?.[key] ?? 0;
    const derived =
      key === 'Stamina'
        ? (n: number) => t('explain.healthAt', { value: n + 3 })
        : key === 'Composure' || key === 'Resolve'
          ? (n: number) => t('explain.willpowerWith', { value: n, other: term(key === 'Composure' ? 'Resolve' : 'Composure') })
          : null;
    return (
      <>
        <Kind text={t(kind === 'attr' ? 'explain.attributeKind' : 'explain.skillKind', { group: term(trait.group) })} />
        <h2>{term(key)}</h2>
        <p>{trait.short}</p>
        {trait.uses && (<><h4>{t('explain.usedFor')}</h4><p>{trait.uses}</p></>)}
        <h4>{t('explain.eachDot')}</h4>
        <table className="levels">
          <tbody>
            {scale.map((text, i) => (
              <tr key={text} className={i + 1 === value ? 'current' : undefined}>
                <td>{'●'.repeat(i + 1)}</td>
                <td>{text}</td>
                {derived && <td className="derived">{derived(i + 1)}</td>}
              </tr>
            ))}
          </tbody>
        </table>
      </>
    );
  }
  if (kind === 'disc' || kind === 'power') {
    const [disciplineName, powerName] = kind === 'disc' ? [key, null] : key.split('|');
    const discipline = content.disciplines[disciplineName];
    const power = discipline?.powers.find((p) => p.name === powerName);
    if (power) {
      return (
        <>
          <Kind text={`${term(disciplineName)} ${'●'.repeat(power.level)}`} />
          <h2>{term(power.name)}</h2>
          <p>{power.summary}</p>
          <table className="levels"><tbody><tr><td>{t('explain.cost')}</td><td>{power.cost}</td></tr><tr><td>{t('explain.pool')}</td><td>{poolOf(content, power.pool)}</td></tr></tbody></table>
        </>
      );
    }
    return (
      <>
        <Kind text={t('explain.disciplineKind', { type: term(discipline.type), resonance: term(discipline.resonance) })} />
        <h2>{term(disciplineName)}</h2>
        <p>{discipline.short}</p>
        <h4>{t('explain.powersByLevel')}</h4>
        {discipline.powers.map((p) => (
          <div key={p.name} className="change"><span><span className="red">{'●'.repeat(p.level)}</span> {term(p.name)}</span><b>{p.summary}</b></div>
        ))}
      </>
    );
  }
  if (kind === 'pred') {
    const predator = content.predators.find((p) => p.name === key);
    if (!predator) return null;
    return (
      <>
        <Kind text={t('terms.predatorType')} />
        <h2>{term(predator.name)}</h2>
        <p>{predator.short}</p>
        <h4>{t('explain.gives')}</h4>
        <div className="change"><span>{t('terms.specialty')}</span><b>{predator.specialties.map((s) => `${term(s.skill)} (${term(s.specialty)})`).join(t('explain.or'))}</b></div>
        <div className="change"><span>{t('explain.disciplineDot')}</span><b>{predator.disciplines.map(term).join(t('explain.or'))}</b></div>
        <div className="change"><span>{t('terms.advantagesAndFlaws')}</span><b>{predator.extras}</b></div>
        {predator.humanity ? <div className="change"><span>{term('Humanity')}</span><b className="new">{predator.humanity > 0 ? '+' : ''}{predator.humanity}</b></div> : null}
        {predator.bp ? <div className="change"><span>{term('Blood Potency')}</span><b className="new">+{predator.bp}</b></div> : null}
        {predator.notFor?.includes(build.clan ?? '') && <p className="vtm-problem">{t('explain.notOpenTo', { clan: term(build.clan ?? '') })}</p>}
      </>
    );
  }
  if (kind === 'age') {
    const age = content.ages[key];
    return (
      <>
        <Kind text={t('terms.ageSinceEmbrace')} />
        <h2>{term(key)}</h2>
        <p>{age.short}</p>
        <div className="change"><span>{term('Blood Potency')}</span><b>{age.bp}</b></div>
        <div className="change"><span>{t('explain.startingXp')}</span><b>{age.xp}</b></div>
        <div className="change"><span>{term('Generation')}</span><b>{age.generations.join(t('explain.or'))}</b></div>
        <div className="change"><span>{t('explain.also')}</span><b>{age.note}</b></div>
      </>
    );
  }
  if (kind === 'dist') {
    const distribution = content.distributions[key];
    return (
      <>
        <Kind text={t('builder.distribution')} />
        <h2>{term(key)}</h2>
        <p>{distribution.short}</p>
        <p className="vtm-muted">{t('explain.goodFor', { fits: distribution.fits })}</p>
      </>
    );
  }
  if (kind === 'adv') {
    const advantage = content.advantages.find((a) => a.name === key);
    if (!advantage) return null;
    return (
      <>
        <Kind text={term(advantage.kind)} />
        <h2>{term(advantage.name)}</h2>
        <p>{advantage.short}</p>
        <div className="change"><span>{t('explain.dots')}</span><b>{advantage.fixed ? t('explain.fixedDots', { dots: advantage.fixed }) : t('explain.dotRange', { max: advantage.max })}</b></div>
      </>
    );
  }
  if (kind === 'conviction') {
    return (<><Kind text={t('terms.beliefs')} /><h2>{t('terms.convictions')}</h2><p>{content.convictionHelp}</p></>);
  }
  return null;
}

function Kind({ text }: { text: string }) {
  return <div className="explain__kind">{text}</div>;
}

/** What the column explains before anything is clicked: the step's current pick, as the mockup opens. */
function defaultFocus(stepId: string, build: Vtm5Build, content: Vtm5Content | null): Focus {
  if (!content) return null;
  switch (stepId) {
    case 'basics': return { kind: 'age', key: build.age ?? 'Neonate' };
    case 'clan': return { kind: 'clan', key: build.clan ?? content.clans[0].name };
    case 'skills': return { kind: 'dist', key: build.distribution ?? 'Balanced' };
    case 'disciplines': return build.disciplineTwo ? { kind: 'disc', key: build.disciplineTwo } : null;
    case 'predator': return { kind: 'pred', key: build.predator ?? content.predators[0].name };
    case 'beliefs': return { kind: 'conviction', key: '' };
    default: return null;
  }
}

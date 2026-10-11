import type { ReactNode } from 'react';
import { useVtm5Content, type Vtm5Content } from '../content';
import { useFocus, type Focus, type Vtm5Build } from './vtm5Build';

/** The right column of the VtM builder: what the option in focus is and what picking it changes. */
export function Vtm5Explain({ build, stepId }: { build: unknown; stepId: string }) {
  const content = useVtm5Content();
  const chosen = useFocus();
  const focus = chosen ?? defaultFocus(stepId, (build ?? {}) as Vtm5Build, content);
  return (
    <aside className="dbox explain-col sticky">
        <div className="explain-col__title">What this choice means</div>
        <div className="explain">{content ? explain(content, focus, (build ?? {}) as Vtm5Build) : null}</div>
    </aside>
  );
}

function explain(content: Vtm5Content, focus: Focus, build: Vtm5Build): ReactNode {
  if (!focus) {
    return <p className="vtm-muted">Click any option, trait or power to read what it does and what it changes.</p>;
  }
  const { kind, key } = focus;
  if (kind === 'clan') {
    const clan = content.clans.find((c) => c.name === key);
    if (!clan) return null;
    return (
      <>
        <Kind text={`Clan · ${clan.source}`} />
        <h2>{clan.name}</h2>
        <div className="sub">{clan.nick}</div>
        <p>{clan.long}</p>
        <p className="vtm-muted">Play them for: {clan.play}</p>
        <h4>Clan disciplines</h4>
        <div className="pills">
          {clan.disciplines.map((d) => (
            <div key={d} className="pill"><b>{d}</b><span>{content.disciplines[d].short}</span></div>
          ))}
        </div>
        <h4>Bane · {clan.bane.name}</h4>
        <p>{clan.bane.text}</p>
        <h4>Compulsion · {clan.compulsion.name}</h4>
        <p>{clan.compulsion.text}</p>
      </>
    );
  }
  if (kind === 'attr' || kind === 'skill') {
    const trait = (kind === 'attr' ? content.attributes : content.skills)[key];
    const scale = kind === 'attr' ? content.attributeScale.slice(0, 4) : content.skillScale;
    const value = (kind === 'attr' ? build.attributes : build.skills)?.[key] ?? 0;
    const derived = key === 'Stamina' ? (n: number) => `Health ${n + 3}` : key === 'Composure' || key === 'Resolve' ? (n: number) => `Willpower ${n} + ${key === 'Composure' ? 'Resolve' : 'Composure'}` : null;
    return (
      <>
        <Kind text={`${trait.group} ${kind === 'attr' ? 'attribute' : 'skill'}`} />
        <h2>{key}</h2>
        <p>{trait.short}</p>
        {trait.uses && (<><h4>Used for</h4><p>{trait.uses}</p></>)}
        <h4>What each dot means</h4>
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
          <Kind text={`${disciplineName} ${'●'.repeat(power.level)}`} />
          <h2>{power.name}</h2>
          <p>{power.summary}</p>
          <table className="levels"><tbody><tr><td>Cost</td><td>{power.cost}</td></tr><tr><td>Pool</td><td>{power.pool}</td></tr></tbody></table>
        </>
      );
    }
    return (
      <>
        <Kind text={`${discipline.type} discipline · ${discipline.resonance} resonance`} />
        <h2>{disciplineName}</h2>
        <p>{discipline.short}</p>
        <h4>Powers by level</h4>
        {discipline.powers.map((p) => (
          <div key={p.name} className="change"><span><span className="red">{'●'.repeat(p.level)}</span> {p.name}</span><b>{p.summary}</b></div>
        ))}
      </>
    );
  }
  if (kind === 'pred') {
    const predator = content.predators.find((p) => p.name === key);
    if (!predator) return null;
    return (
      <>
        <Kind text="Predator type" />
        <h2>{predator.name}</h2>
        <p>{predator.short}</p>
        <h4>Gives</h4>
        <div className="change"><span>Specialty</span><b>{predator.specialties.map((s) => `${s.skill} (${s.specialty})`).join(' or ')}</b></div>
        <div className="change"><span>Discipline dot</span><b>{predator.disciplines.join(' or ')}</b></div>
        <div className="change"><span>Advantages & flaws</span><b>{predator.extras}</b></div>
        {predator.humanity ? <div className="change"><span>Humanity</span><b className="new">{predator.humanity > 0 ? '+' : ''}{predator.humanity}</b></div> : null}
        {predator.bp ? <div className="change"><span>Blood Potency</span><b className="new">+{predator.bp}</b></div> : null}
        {predator.notFor?.includes(build.clan ?? '') && <p className="vtm-problem">Not open to {build.clan}.</p>}
      </>
    );
  }
  if (kind === 'age') {
    const age = content.ages[key];
    return (
      <>
        <Kind text="Age since the Embrace" />
        <h2>{key}</h2>
        <p>{age.short}</p>
        <div className="change"><span>Blood Potency</span><b>{age.bp}</b></div>
        <div className="change"><span>Starting XP</span><b>{age.xp}</b></div>
        <div className="change"><span>Generation</span><b>{age.generations.join(' or ')}</b></div>
        <div className="change"><span>Also</span><b>{age.note}</b></div>
      </>
    );
  }
  if (kind === 'dist') {
    const distribution = content.distributions[key];
    return (
      <>
        <Kind text="Skill distribution" />
        <h2>{key}</h2>
        <p>{distribution.short}</p>
        <p className="vtm-muted">Good for: {distribution.fits}</p>
      </>
    );
  }
  if (kind === 'adv') {
    const advantage = content.advantages.find((a) => a.name === key);
    if (!advantage) return null;
    return (
      <>
        <Kind text={advantage.kind} />
        <h2>{advantage.name}</h2>
        <p>{advantage.short}</p>
        <div className="change"><span>Dots</span><b>{advantage.fixed ? `${advantage.fixed} (fixed)` : `1 to ${advantage.max}`}</b></div>
      </>
    );
  }
  if (kind === 'conviction') {
    return (<><Kind text="Beliefs" /><h2>Convictions</h2><p>{content.convictionHelp}</p></>);
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
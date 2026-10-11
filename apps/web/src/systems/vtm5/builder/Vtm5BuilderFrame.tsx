import { Link } from 'react-router';
import type { BuilderFrameProps } from '../../../builder/builderDefinition';
import { AppHeader } from '../../../shell/AppHeader';
import { Vtm5Footer } from '../Vtm5Footer';
import { Vtm5Explain } from './Vtm5Explain';
import { Vtm5StepPage } from './Vtm5StepPage';
import type { Vtm5Build } from './vtm5Build';
import '../vtm5-builder.css';

/** The VtM builder page as the chosen mockup draws it (builder v2 · C): progress, the step, the explanation column. */
export function Vtm5BuilderFrame(props: BuilderFrameProps) {
  const { characterId, steps, stepId, plan, userName, saveLabel, error } = props;
  const build = (props.build ?? {}) as Vtm5Build;
  const index = steps.findIndex((step) => step.id === stepId);
  const previous = steps[index - 1];
  const next = steps[index + 1];
  const leftOf = (id: string) =>
    plan.choices.filter((choice) => choice.type === id && !choice.optional).reduce((total, choice) => total + Math.max(0, choice.count - choice.selected.length), 0);
  const done = steps.filter((step) => step.id !== 'review' && leftOf(step.id) === 0).length;
  const subOf: Record<string, string | undefined> = { basics: build.age, clan: build.clan, skills: build.distribution, predator: build.predator };

  return (
    <div className="vtm5-page">
      <AppHeader userName={userName} backToListWithin={1260} compact systemId={props.systemId}>
        <span className="vtm5-draft">Draft</span>
        <span className="vtm5-saved" role="status">{saveLabel}</span>
      </AppHeader>
      <div className="vtm5-builder layout">
        <nav className="dbox progress sticky" aria-label="Creation steps">
          <div className="progress__head"><span>Progress</span><span>{done} of {steps.length - 1}</span></div>
          <div className="bar"><i style={{ width: `${(done / (steps.length - 1)) * 100}%` }} /></div>
          {steps.map((step, i) => {
            const left = leftOf(step.id);
            const complete = step.id !== 'review' && left === 0;
            return (
              <div key={step.id}>
                <Link className={`stepx${step.id === stepId ? ' on' : ''}`} to={`/characters/${characterId}/build/${step.id}`}>
                  <span className={`mark${complete ? ' ok' : ''}`}>{complete ? '✓' : i + 1}</span>
                  {step.label}
                  {left > 0 && <span className="left">{left} LEFT</span>}
                </Link>
                {subOf[step.id] && <div className="subx">{subOf[step.id]}</div>}
              </div>
            );
          })}
        </nav>

        <main>
          {error && <p className="vtm-problem" role="alert">{error}</p>}
          <Vtm5StepPage {...props} choices={[]} preview={undefined as never} />
          <div className="nav">
            {previous ? <Link className="btn btn--ghost" to={`/characters/${characterId}/build/${previous.id}`}>‹ {previous.label}</Link> : <span />}
            {next && <Link className="btn" to={`/characters/${characterId}/build/${next.id}`}>Next: {next.label} ›</Link>}
          </div>
        </main>

        <Vtm5Explain build={props.build} stepId={stepId} />
      </div>
      <Vtm5Footer />
    </div>
  );
}

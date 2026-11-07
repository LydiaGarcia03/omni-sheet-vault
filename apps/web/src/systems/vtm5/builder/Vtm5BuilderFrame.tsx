import { Link } from 'react-router';
import type { BuilderFrameProps } from '../../../builder/builderDefinition';
import { useBuilderText } from '../../../locales/platformMessages';
import { AppHeader } from '../../../shell/AppHeader';
import { termOf, useVtm5Content } from '../content';
import { useVtm5Text } from '../locales/vtm5Messages';
import { Vtm5Footer } from '../Vtm5Footer';
import { Vtm5Explain } from './Vtm5Explain';
import { Vtm5StepPage } from './Vtm5StepPage';
import { useStepLabel, type Vtm5Build } from './vtm5Build';
import '../vtm5-builder.css';

/** The VtM builder page as the chosen mockup draws it (builder v2 · C): progress, the step, the explanation column. */
export function Vtm5BuilderFrame(props: BuilderFrameProps) {
  const { characterId, steps, stepId, plan, userName, saveLabel, error } = props;
  const t = useVtm5Text();
  const tb = useBuilderText();
  const content = useVtm5Content();
  const stepLabel = useStepLabel();
  const build = (props.build ?? {}) as Vtm5Build;
  const index = steps.findIndex((step) => step.id === stepId);
  const previous = steps[index - 1];
  const next = steps[index + 1];
  const leftOf = (id: string) =>
    plan.choices.filter((choice) => choice.type === id && !choice.optional).reduce((total, choice) => total + Math.max(0, choice.count - choice.selected.length), 0);
  const done = steps.filter((step) => step.id !== 'review' && leftOf(step.id) === 0).length;
  const shown = (name: string | undefined) => (name && content ? termOf(content, name) : name);
  const subOf: Record<string, string | undefined> = { basics: shown(build.age), clan: shown(build.clan), skills: shown(build.distribution), predator: shown(build.predator) };

  return (
    <div className="vtm5-page">
      <AppHeader userName={userName} backToListWithin={1260} compact systemId={props.systemId}>
        <span className="vtm5-draft">{tb('draft')}</span>
        <span className="vtm5-saved" role="status">{saveLabel}</span>
      </AppHeader>
      <div className="vtm5-builder layout">
        <nav className="dbox progress sticky" aria-label={t('steps.creationSteps')}>
          <div className="progress__head"><span>{t('steps.progress')}</span><span>{t('steps.progressCount', { done, total: steps.length - 1 })}</span></div>
          <div className="bar"><i style={{ width: `${(done / (steps.length - 1)) * 100}%` }} /></div>
          {steps.map((step, i) => {
            const left = leftOf(step.id);
            const complete = step.id !== 'review' && left === 0;
            return (
              <div key={step.id}>
                <Link className={`stepx${step.id === stepId ? ' on' : ''}`} to={`/characters/${characterId}/build/${step.id}`}>
                  <span className={`mark${complete ? ' ok' : ''}`}>{complete ? '✓' : i + 1}</span>
                  {stepLabel(step.id)}
                  {left > 0 && <span className="left">{t('steps.left', { count: left })}</span>}
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
            {previous ? <Link className="btn btn--ghost" to={`/characters/${characterId}/build/${previous.id}`}>‹ {stepLabel(previous.id)}</Link> : <span />}
            {next && <Link className="btn" to={`/characters/${characterId}/build/${next.id}`}>{t('steps.next', { step: stepLabel(next.id) })}</Link>}
          </div>
        </main>

        <Vtm5Explain build={props.build} stepId={stepId} />
      </div>
      <Vtm5Footer />
    </div>
  );
}

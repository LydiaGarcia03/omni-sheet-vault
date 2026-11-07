import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router';
import type { BuilderFrameProps } from '../../../builder/builderDefinition';
import type { DraftPreview } from '../../../builder/draftApi';
import { AppHeader } from '../../../shell/AppHeader';
import { DaggerheartFooter } from '../DaggerheartFooter';
import { useDaggerheartCatalogue } from '../daggerheartCatalogue';
import { OPTIONAL_STEPS, picksLeft, type DaggerheartBuild } from './daggerheartBuild';
import { DaggerheartStepBody, STEP_INFO, STEP_SUBTITLES } from './DaggerheartSteps';
import { DaggerheartSummary } from './DaggerheartSummary';
import '../dh-theme.css';
import '../dh-builder.css';

/** How long the pointer rests on an option before its effect is previewed. */
const HOVER_PREVIEW_DELAY_MS = 180;

/** The Daggerheart builder page as builder v3 draws it: progress rail, the step's choices, the character summary. */
export function DaggerheartBuilderFrame(props: BuilderFrameProps) {
  const { characterId, steps, stepId, plan, userName, saveLabel, error, previewBuild } = props;
  const build = (props.build ?? {}) as DaggerheartBuild;
  const catalogue = useDaggerheartCatalogue();
  const [info, setInfo] = useState(false);
  const [lastChange, setLastChange] = useState<string | null>(null);
  const [hovered, setHovered] = useState<DaggerheartBuild | null>(null);
  const [hoverPreview, setHoverPreview] = useState<DraftPreview | null>(null);
  const hoverSequence = useRef(0);

  useEffect(() => {
    const sequence = ++hoverSequence.current;
    if (!hovered) {
      setHoverPreview(null);
      return;
    }
    const timer = window.setTimeout(() => {
      previewBuild(hovered)
        .then((result) => sequence === hoverSequence.current && setHoverPreview(result.preview))
        .catch(() => sequence === hoverSequence.current && setHoverPreview(null));
    }, HOVER_PREVIEW_DELAY_MS);
    return () => window.clearTimeout(timer);
  }, [hovered, previewBuild]);

  const change = useCallback(
    (next: DaggerheartBuild, note?: string) => {
      setHovered(null);
      if (note) {
        setLastChange(note);
      }
      props.onBuildChange(next);
    },
    [props],
  );

  const index = steps.findIndex((step) => step.id === stepId);
  const step = steps[index] ?? steps[0];
  const previous = steps[index - 1];
  const next = steps[index + 1];
  const isDone = (id: string) => (id === 'basics' ? props.name.trim() !== '' : !OPTIONAL_STEPS.has(id) && picksLeft(plan, id) === 0);
  const required = steps.filter((candidate) => !OPTIONAL_STEPS.has(candidate.id));
  const done = required.filter((candidate) => isDone(candidate.id)).length;

  return (
    <div className="dh-page">
      <AppHeader userName={userName} backToListWithin={1260} compact systemId={props.systemId}>
        <span className="dh-draft">Draft</span>
        <span className="dh-saved" role="status" data-tutorial="save-state">
          {saveLabel}
        </span>
      </AppHeader>
      <div className="dh-builder">
        <nav className="dh-frame navy rail" aria-label="Creation steps" data-tutorial="checklist">
          <div className="rail__progress">
            {done} of {required.length} required steps
          </div>
          <div className="rail__bar">
            <i style={{ width: `${(done / required.length) * 100}%` }} />
          </div>
          <div className="steps">
            {steps.map((candidate, position) => {
              const complete = isDone(candidate.id);
              const subtitle = STEP_SUBTITLES[candidate.id]?.(build, plan, props.name) ?? '';
              return (
                <Link
                  key={candidate.id}
                  className={`step${candidate.id === step.id ? ' on' : ''}${complete ? ' done' : ''}`}
                  to={`/characters/${characterId}/build/${candidate.id}`}
                  aria-current={candidate.id === step.id ? 'step' : undefined}
                >
                  <span className="step__box">
                    <b>{candidate.label}</b>
                    <small className={subtitle ? '' : 'todo'}>{subtitle || (OPTIONAL_STEPS.has(candidate.id) ? 'optional' : 'to choose')}</small>
                  </span>
                  <span className="step__check">{complete ? '✓' : position + 1}</span>
                </Link>
              );
            })}
          </div>
          <div className="rail__deco" />
          <button type="button" className="cs-btn" disabled={plan.pendingCount > 0 || props.finishing} onClick={props.onFinish}>
            <span>{props.finishing ? 'Finishing…' : 'Character sheet'}</span>
          </button>
        </nav>

        <section className="dh-frame navy choices" data-tutorial="step">
          <div className="choices__head">
            <h2>{step.label}</h2>
            <button type="button" className={`info-btn${info ? ' on' : ''}`} aria-pressed={info} aria-label="About this step" onClick={() => setInfo(!info)} data-tutorial="help">
              i
            </button>
            <StepBadge stepId={step.id} done={isDone(step.id)} left={picksLeft(plan, step.id)} />
          </div>
          {info && <div className="info">{STEP_INFO[step.id]}</div>}
          <div className="choices__body">
            {error && (
              <p className="problem" role="alert">
                {error}
              </p>
            )}
            {plan.problems.map((problem) => (
              <p key={problem} className="problem">
                {problem}
              </p>
            ))}
            {catalogue ? (
              <DaggerheartStepBody {...props} stepId={step.id} build={build} catalogue={catalogue} change={change} hover={setHovered} />
            ) : (
              <p className="muted">Loading the SRD…</p>
            )}
          </div>
          <div className="choices__foot" data-tutorial="stepnav">
            {previous ? <Link to={`/characters/${characterId}/build/${previous.id}`}>‹ {previous.label}</Link> : <span />}
            {next ? (
              <Link to={`/characters/${characterId}/build/${next.id}`}>{next.label} ›</Link>
            ) : (
              <button type="button" disabled={plan.pendingCount > 0 || props.finishing} onClick={props.onFinish}>
                Finish character ›
              </button>
            )}
          </div>
        </section>

        <DaggerheartSummary
          build={build}
          plan={plan}
          preview={props.preview}
          hoverPreview={hoverPreview}
          name={props.name}
          portrait={props.portrait}
          systemId={props.systemId}
          catalogue={catalogue}
          lastChange={lastChange}
        />
      </div>
      <DaggerheartFooter />
    </div>
  );
}

function StepBadge({ stepId, done, left }: { stepId: string; done: boolean; left: number }) {
  if (OPTIONAL_STEPS.has(stepId)) {
    return <span className="dh-badge">Optional</span>;
  }
  if (done) {
    return <span className="dh-badge done">Done</span>;
  }
  return <span className="dh-badge">{stepId === 'basics' ? 'Required' : `${left} left`}</span>;
}

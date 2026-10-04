import { Link } from 'react-router';
import { checklistLines, type BuilderDefinition } from './builderDefinition';
import type { CreationChoice } from './draftApi';

type BuilderChecklistProps = {
  characterId: string;
  definition: BuilderDefinition;
  currentStepId: string;
  choicesByStep: Map<string, CreationChoice[]>;
};

/** Where the character stands: each step, and under it one line per kind of choice, done or how many are left. */
export function BuilderChecklist({ characterId, definition, currentStepId, choicesByStep }: BuilderChecklistProps) {
  const all = [...choicesByStep.values()].flat().filter((choice) => !choice.optional);
  const done = all.filter((choice) => !choice.pending).length;
  const percent = all.length === 0 ? 0 : Math.round((done / all.length) * 100);

  return (
    <nav className="builder-frame builder-frame--side builder-checklist" aria-label="Creation steps">
      <div className="builder-frame__title">
        <span className="builder-grow">Progress</span>
        <span className="builder-muted builder-checklist__count">
          {done} of {all.length}
        </span>
      </div>
      <div className="builder-progress" role="progressbar" aria-valuenow={percent} aria-valuemin={0} aria-valuemax={100}>
        <div style={{ width: `${percent}%` }} />
      </div>
      {definition.steps.map((step, index) => {
        const choices = choicesByStep.get(step.id) ?? [];
        const pending = choices.filter((choice) => choice.pending).length;
        const complete = choices.length > 0 && pending === 0;
        const href = `/characters/${characterId}/build/${step.id}`;
        const lines = checklistLines(definition, choices);
        return (
          <div key={step.id} className="builder-checklist__group">
            <Link
              to={href}
              className={`builder-checklist__step${step.id === currentStepId ? ' is-current' : ''}${complete ? ' is-done' : ''}`}
              aria-current={step.id === currentStepId ? 'step' : undefined}
            >
              <span className="builder-checklist__dot">{complete ? '✓' : index}</span>
              <span className="builder-grow">{step.label}</span>
              {pending > 0 && <span className="builder-badge builder-badge--pending">{pending} left</span>}
            </Link>
            {lines.length > 1 &&
              lines.map((line) => (
                <Link key={line.group} to={href} className={`builder-checklist__sub${line.pending > 0 ? ' is-pending' : ''}`}>
                  <span>{line.group}</span>
                  <span className="builder-checklist__value">{line.pending > 0 ? `${line.pending} left` : '✓'}</span>
                </Link>
              ))}
          </div>
        );
      })}
    </nav>
  );
}

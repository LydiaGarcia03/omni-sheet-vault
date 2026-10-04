import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { SourceCodesProvider } from '../catalogue/SourceCodes';
import type { Portrait } from '../characters/portrait';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { BUILDER_COLUMN_WIDTH } from './builderLayout';
import { BuilderChecklist } from './BuilderChecklist';
import { BuilderSummary } from './BuilderSummary';
import { choicesByStep, withLocalAnswers, type BuilderDefinition } from './builderDefinition';
import { finishDraft, getDraft, saveDraft, type BuildPlan, type Draft, type DraftPreview } from './draftApi';
import '../systems/dnd5e/themes.css';
import './builder.css';

const AUTOSAVE_DELAY_MS = 600;

type SaveState = 'saved' | 'saving' | 'unsaved' | 'error';

type CharacterBuilderProps = {
  characterId: string;
  stepId: string | undefined;
  definitionFor: (systemId: string) => BuilderDefinition;
  userName: string;
};

/** The three-column creation flow: step checklist, one page per step, and the character summary. */
export function CharacterBuilder({ characterId, stepId, definitionFor, userName }: CharacterBuilderProps) {
  const [draft, setDraft] = useState<Draft | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    getDraft(characterId)
      .then(setDraft)
      .catch((error: Error) => setLoadError(error.message));
  }, [characterId]);

  if (loadError) {
    return (
      <div className="builder-page builder-message" role="alert">
        {loadError} <Link to="/">Back to your characters</Link>
      </div>
    );
  }
  if (!draft) {
    return <div className="builder-page builder-message">Loading…</div>;
  }
  const definition = definitionFor(draft.systemId);
  return (
    <SourceCodesProvider systemId={draft.systemId} leadingSources={definition.leadingSources}>
      <LoadedBuilder initial={draft} stepId={stepId} definition={definition} userName={userName} />
    </SourceCodesProvider>
  );
}

type LoadedBuilderProps = {
  initial: Draft;
  stepId: string | undefined;
  definition: BuilderDefinition;
  userName: string;
};

function LoadedBuilder({ initial, stepId, definition, userName }: LoadedBuilderProps) {
  const navigate = useNavigate();
  const [name, setName] = useState(initial.name);
  const [build, setBuild] = useState<unknown>(initial.build);
  const [plan, setPlan] = useState<BuildPlan>(initial.plan);
  const [preview, setPreview] = useState<DraftPreview>(initial.preview);
  const [portrait, setPortrait] = useState<Portrait | null>(initial.portrait ?? null);
  const [saveState, setSaveState] = useState<SaveState>('saved');
  const [error, setError] = useState<string | null>(null);
  const [finishing, setFinishing] = useState(false);
  const latestSave = useRef(0);
  const dirty = useRef(false);

  const save = useCallback(async () => {
    const sequence = ++latestSave.current;
    dirty.current = false;
    setSaveState('saving');
    try {
      const saved = await saveDraft(initial.id, name, build);
      if (sequence !== latestSave.current) {
        return;
      }
      setPlan(saved.plan);
      setPreview(saved.preview);
      setSaveState(dirty.current ? 'unsaved' : 'saved');
      setError(null);
      const pruned = definition.withoutStaleAnswers(build, saved.plan);
      if (pruned !== build && !dirty.current) {
        dirty.current = true;
        setBuild(pruned);
      }
    } catch (saveError) {
      if (sequence === latestSave.current) {
        setSaveState('error');
        setError((saveError as Error).message);
      }
    }
  }, [initial.id, name, build, definition]);

  useEffect(() => {
    if (!dirty.current) {
      return;
    }
    setSaveState('unsaved');
    const timer = window.setTimeout(save, AUTOSAVE_DELAY_MS);
    return () => window.clearTimeout(timer);
  }, [name, build, save]);

  const edit = <T,>(setter: (value: T) => void) => (value: T) => {
    dirty.current = true;
    setter(value);
  };

  const finish = async () => {
    setFinishing(true);
    try {
      if (dirty.current || saveState !== 'saved') {
        await save();
      }
      const character = await finishDraft(initial.id);
      navigate(`/characters/${character.id}`);
    } catch (finishError) {
      setError((finishError as Error).message);
      setFinishing(false);
    }
  };

  const livePlan = useMemo(() => withLocalAnswers(definition, plan, build), [definition, plan, build]);
  const byStep = useMemo(() => choicesByStep(definition, livePlan), [definition, livePlan]);
  const step = definition.steps.find((candidate) => candidate.id === stepId) ?? definition.steps[0];
  const stepIndex = definition.steps.indexOf(step);
  const previous = definition.steps[stepIndex - 1];
  const next = definition.steps[stepIndex + 1];
  const { StepPage } = definition;

  return (
    <div className={definition.pageClassName ? `builder-page ${definition.pageClassName}` : 'builder-page'}>
      <AppHeader userName={userName} backToListWithin={BUILDER_COLUMN_WIDTH} compact systemId={initial.systemId}>
        <span className="builder-topbar__pill">Draft</span>
        <span className="builder-topbar__saved" role="status">
          {SAVE_LABELS[saveState]}
        </span>
      </AppHeader>
      <div className="builder-layout">
        <aside className="builder-layout__side">
          <BuilderChecklist characterId={initial.id} definition={definition} currentStepId={step.id} choicesByStep={byStep} />
        </aside>
        <main className="builder-layout__main">
          {error && (
            <div className="builder-error" role="alert">
              {error}
            </div>
          )}
          <StepPage
            characterId={initial.id}
            systemId={initial.systemId}
            stepId={step.id}
            portrait={portrait}
            onPortraitChange={setPortrait}
            name={name}
            onNameChange={edit(setName)}
            build={build}
            onBuildChange={edit(setBuild)}
            plan={livePlan}
            preview={preview}
            choices={byStep.get(step.id) ?? []}
            onFinish={finish}
            finishing={finishing}
          />
          <div className="builder-stepnav">
            {previous ? (
              <Link className="builder-btn builder-btn--big" to={`/characters/${initial.id}/build/${previous.id}`}>
                ← {previous.label}
              </Link>
            ) : (
              <span />
            )}
            <span className="builder-muted">
              Step {stepIndex + 1} of {definition.steps.length} · saved automatically
            </span>
            {next ? (
              <Link className="builder-btn builder-btn--primary builder-btn--big" to={`/characters/${initial.id}/build/${next.id}`}>
                {next.label} →
              </Link>
            ) : (
              <span />
            )}
          </div>
        </main>
        <aside className="builder-layout__summary">
          <BuilderSummary
            name={name}
            description={definition.describe(build, livePlan)}
            pendingCount={plan.pendingCount}
            preview={preview}
            portrait={portrait}
            systemId={initial.systemId}
          />
        </aside>
      </div>
      <AppFooter />
    </div>
  );
}

const SAVE_LABELS: Record<SaveState, string> = {
  saved: 'All changes saved',
  saving: 'Saving…',
  unsaved: 'Unsaved changes',
  error: 'Not saved',
};

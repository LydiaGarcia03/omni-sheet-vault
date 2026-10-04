import type { ReactNode } from 'react';
import { Link } from 'react-router';
import type { StepPageProps } from '../../../builder/builderDefinition';
import { ChoiceField } from '../../../builder/ChoiceField';
import { SelectionCard } from '../../../builder/SelectionCard';
import { SourceBadge, SourceSelect } from '../../../builder/SourceSelect';
import type { CreationChoice, SelectionDetail } from '../../../builder/draftApi';
import { Dnd5eAbilitiesStep } from './Dnd5eAbilitiesStep';
import { Dnd5eBasicsStep } from './Dnd5eBasicsStep';
import { Dnd5eClassLevels } from './Dnd5eClassLevels';
import { Dnd5eReviewCards } from './Dnd5eReviewCards';
import { Dnd5eAsiOrFeatField } from './Dnd5eAsiOrFeatField';
import { Dnd5eEquipmentStep } from './Dnd5eEquipmentStep';
import { Dnd5eRolledHitPointsField, unrolledHitPointLevels } from './Dnd5eRolledHitPointsField';
import { isSpellChoice, SpellPicker } from './SpellPicker';
import {
  answerOf,
  characterLevel,
  dnd5eStepOf,
  withClassAdded,
  withClassLevel,
  withClassRemoved,
  withSelection,
  withStartingItemEquipped,
  type Dnd5eBuild,
} from './dnd5eBuild';

const MAX_CHARACTER_LEVEL = 20;
const ASI_OR_FEAT = 'ASI_OR_FEAT';
const ROLLED_HIT_POINTS = 'ROLLED_HIT_POINTS';

const STEP_LEADS: Record<string, string> = {
  basics: 'Name, portrait, the books this character draws from and the rules it follows.',
  species: 'Ability bonuses, speed, senses and a few proficiencies.',
  classes: 'Pick a class and its level. Add more classes to multiclass.',
  background: 'Where your character comes from. Personality and backstory are written on the sheet later.',
  abilities: 'Pick a method and set your base scores.',
  equipment: 'Take your starting equipment or its gold.',
  review: 'Check your choices, then finish to turn this draft into a character.',
};

const STEP_TITLES: Record<string, string> = {
  basics: 'Basics',
  species: 'Species',
  classes: 'Classes',
  background: 'Background',
  abilities: 'Ability scores',
  equipment: 'Equipment',
  review: 'Review',
};

export function Dnd5eStepPage(props: StepPageProps) {
  return (
    <>
      <div className="builder-step-head">
        <h1>{STEP_TITLES[props.stepId]}</h1>
        <p>{STEP_LEADS[props.stepId]}</p>
      </div>
      <StepContent {...props} />
    </>
  );
}

function StepContent(props: StepPageProps) {
  const build = props.build as Dnd5eBuild;
  const change = (next: Dnd5eBuild) => props.onBuildChange(next);
  const answer = (choiceId: string, selected: string[]) => change(withSelection(build, choiceId, selected));

  switch (props.stepId) {
    case 'basics':
      return (
        <Dnd5eBasicsStep
          characterId={props.characterId}
          systemId={props.systemId}
          name={props.name}
          onNameChange={props.onNameChange}
          portrait={props.portrait}
          onPortraitChange={props.onPortraitChange}
          build={build}
          onChange={change}
        />
      );
    case 'classes':
      return (
        <>
          <ClassesEditor build={build} choices={props.choices} onChange={change} />
          <ClassChoices {...props} build={build} onAnswer={answer} onBuildChange={change} />
        </>
      );
    case 'species':
      return (
        <DetailStep
          {...props}
          build={build}
          structuralIds={['build.species', 'build.subspecies', 'build.variant']}
          detail={props.preview.selections?.species}
          grantsTitle="Traits"
          onAnswer={answer}
          onBuildChange={change}
        />
      );
    case 'background':
      return (
        <DetailStep
          {...props}
          build={build}
          structuralIds={['build.background']}
          detail={props.preview.selections?.background}
          grantsTitle="What you get"
          onAnswer={answer}
          onBuildChange={change}
        />
      );
    case 'abilities':
      return (
        <Dnd5eAbilitiesStep
          characterId={props.characterId}
          build={build}
          choices={props.choices}
          problems={props.plan.problems}
          preview={props.preview}
          onChange={change}
        />
      );
    case 'equipment':
      return (
        <Dnd5eEquipmentStep
          choices={props.choices}
          onAnswer={answer}
          startingEquipment={props.preview.startingEquipment}
          onEquip={(lineKey, equipped) => change(withStartingItemEquipped(build, lineKey, equipped))}
        />
      );
    case 'review':
      return <Review {...props} />;
    default:
      return <ChoicesFrame title="Choices" characterId={props.characterId} choices={props.choices} build={build} onAnswer={answer} onBuildChange={change} />;
  }
}

function Frame({ title, children, badge }: { title: string; children: ReactNode; badge?: ReactNode }) {
  return (
    <section className="builder-frame">
      <div className="builder-frame__title">
        <span>{title}</span>
        {badge}
      </div>
      {children}
    </section>
  );
}

function PendingBadge({ choices }: { choices: CreationChoice[] }) {
  const pending = choices.filter((choice) => choice.pending).length;
  return pending > 0 ? (
    <span className="builder-badge builder-badge--pending">{pending} left</span>
  ) : (
    <span className="builder-badge builder-badge--done">done</span>
  );
}

type ChoicesFrameProps = {
  title: string;
  characterId: string;
  choices: CreationChoice[];
  build: Dnd5eBuild;
  onAnswer: (choiceId: string, selections: string[]) => void;
  onBuildChange: (build: Dnd5eBuild) => void;
};

function ChoicesFrame({ title, characterId, choices, build, onAnswer, onBuildChange }: ChoicesFrameProps) {
  if (choices.length === 0) {
    return (
      <Frame title={title}>
        <p className="builder-muted">Nothing to choose here yet.</p>
      </Frame>
    );
  }
  const { renderChoice, isNested } = choiceRenderer({ characterId, choices, build, onAnswer, onBuildChange });
  return (
    <Frame title={title} badge={<PendingBadge choices={choices} />}>
      {choices.filter((choice) => !isNested(choice)).map(renderChoice)}
    </Frame>
  );
}

type ChoiceRendererInput = Omit<ChoicesFrameProps, 'title'>;

type DetailStepProps = Omit<StepPageProps, 'build' | 'onBuildChange'> & {
  build: Dnd5eBuild;
  structuralIds: string[];
  detail: SelectionDetail | undefined;
  grantsTitle: string;
  onAnswer: (choiceId: string, selections: string[]) => void;
  onBuildChange: (build: Dnd5eBuild) => void;
};

/** The round-3 Species and Background layout: the selects, the chosen entry's card and grants, then its own choices. */
function DetailStep({ characterId, choices, build, structuralIds, detail, grantsTitle, onAnswer, onBuildChange }: DetailStepProps) {
  const structural = choices.filter((choice) => structuralIds.includes(choice.id));
  const own = choices.filter((choice) => !structuralIds.includes(choice.id));
  const { renderChoice } = choiceRenderer({ characterId, choices, build, onAnswer, onBuildChange });
  return (
    <>
      <Frame title="Choose">{structural.map(renderChoice)}</Frame>
      {detail && <SelectionCard detail={detail} grantsTitle={grantsTitle} />}
      {own.length > 0 && <ChoicesFrame title="Choices" characterId={characterId} choices={own} build={build} onAnswer={onAnswer} onBuildChange={onBuildChange} />}
    </>
  );
}

type ClassChoicesProps = Omit<StepPageProps, 'build' | 'onBuildChange'> & {
  build: Dnd5eBuild;
  onAnswer: (choiceId: string, selections: string[]) => void;
  onBuildChange: (build: Dnd5eBuild) => void;
};

/** The Classes step's choices: each class's by level with its own rolled hit points, and anything no class owns in its own frame. */
function ClassChoices({ characterId, choices, preview, build, onAnswer, onBuildChange }: ClassChoicesProps) {
  const stepChoices = choices.filter((choice) => choice.id !== 'build.classes');
  const classSlugs = new Set(build.classes.map((entry) => entry.classSlug));
  const inClass = stepChoices.filter((choice) => choice.group && classSlugs.has(choice.group));
  const hitPointChoice = stepChoices.find((choice) => choice.type === ROLLED_HIT_POINTS);
  const other = stepChoices.filter((choice) => (!choice.group || !classSlugs.has(choice.group)) && choice !== hitPointChoice);
  const hasHitPointRows = (classSlug: string) =>
    hitPointChoice?.options.some((option) => (option.data as { group?: string } | undefined)?.group === classSlug) ?? false;
  const renderHitPoints = (classSlug: string) =>
    hitPointChoice && hasHitPointRows(classSlug) ? (
      <Dnd5eRolledHitPointsField characterId={characterId} choice={hitPointChoice} build={build} onChange={onBuildChange} group={classSlug} />
    ) : null;
  const hitPointsLeftIn = (classSlug: string) => (hitPointChoice ? unrolledHitPointLevels(hitPointChoice, build, classSlug) : 0);
  const classOptions = choices.find((choice) => choice.id === 'build.classes')?.options ?? [];
  const classLabels = Object.fromEntries(classOptions.map((option) => [option.key, option.label]));
  const { renderChoice, isNested } = choiceRenderer({ characterId, choices: stepChoices, build, onAnswer, onBuildChange });
  const otherFrame =
    other.length > 0 ? (
      <ChoicesFrame title="Other class choices" characterId={characterId} choices={other} build={build} onAnswer={onAnswer} onBuildChange={onBuildChange} />
    ) : null;

  if (build.classes.length === 0) {
    return otherFrame;
  }
  return (
    <Dnd5eClassLevels
      build={build}
      classLabels={classLabels}
      choices={inClass}
      progressions={preview.progressions ?? []}
      renderChoice={renderChoice}
      isNested={isNested}
      beforeProgression={otherFrame}
      renderHitPoints={renderHitPoints}
      hitPointsLeftIn={hitPointsLeftIn}
    />
  );
}

/** How each kind of choice is drawn; a feat's own picks nest under their ASI-or-feat choice instead of standing alone. */
export function choiceRenderer({ characterId, choices, build, onAnswer, onBuildChange }: ChoiceRendererInput) {
  const asiOrFeatIds = new Set(choices.filter((choice) => choice.type === ASI_OR_FEAT).map((choice) => choice.id));
  const isNested = (choice: CreationChoice) => choice.parentChoiceId !== null && asiOrFeatIds.has(choice.parentChoiceId);
  const renderChoice = (choice: CreationChoice): ReactNode => {
    const onChange = (selected: string[]) => onAnswer(choice.id, selected);
    if (choice.type === ROLLED_HIT_POINTS) {
      return <Dnd5eRolledHitPointsField key={choice.id} characterId={characterId} choice={choice} build={build} onChange={onBuildChange} />;
    }
    if (choice.type === ASI_OR_FEAT) {
      return (
        <Dnd5eAsiOrFeatField
          key={choice.id}
          choice={choice}
          nested={choices.filter((candidate) => candidate.parentChoiceId === choice.id)}
          answerFor={(choiceId) => answerOf(build, choiceId) ?? []}
          onAnswer={onAnswer}
          renderChoice={renderChoice}
        />
      );
    }
    return isSpellChoice(choice) ? (
      <SpellPicker key={choice.id} choice={choice} onChange={onChange} />
    ) : (
      <ChoiceField key={choice.id} choice={choice} onChange={onChange} />
    );
  };
  return { renderChoice, isNested };
}

function ClassesEditor({ build, choices, onChange }: { build: Dnd5eBuild; choices: CreationChoice[]; onChange: (build: Dnd5eBuild) => void }) {
  const classChoice = choices.find((choice) => choice.id === 'build.classes');
  const options = classChoice?.options ?? [];
  const optionOf = (slug: string) => options.find((candidate) => candidate.key === slug);
  const addable = options.filter((option) => !build.classes.some((entry) => entry.classSlug === option.key));
  const total = characterLevel(build);

  return (
    <Frame title="Your classes" badge={build.classes.length === 0 ? <span className="builder-badge builder-badge--pending">1 left</span> : undefined}>
      {build.classes.map((entry, index) => (
        <div key={entry.classSlug} className="builder-row" style={{ padding: '6px 0' }}>
          <div className="builder-grow">
            <b>{optionOf(entry.classSlug)?.label ?? entry.classSlug}</b> <SourceBadge sourceBook={optionOf(entry.classSlug)?.sourceBook ?? null} />
            {index === 0 && <span className="builder-muted"> · starting class</span>}
            {optionOf(entry.classSlug)?.summary && <div className="builder-choice__from">{optionOf(entry.classSlug)?.summary}</div>}
          </div>
          <label className="builder-muted" htmlFor={`level-${entry.classSlug}`}>
            Level
          </label>
          <input
            id={`level-${entry.classSlug}`}
            className="builder-input builder-input--number"
            type="number"
            min={1}
            max={MAX_CHARACTER_LEVEL - total + entry.level}
            value={entry.level}
            onChange={(event) => onChange(withClassLevel(build, entry.classSlug, Math.max(1, Number(event.target.value) || 1)))}
          />
          <button type="button" className="builder-btn" onClick={() => onChange(withClassRemoved(build, entry.classSlug))}>
            Remove
          </button>
        </div>
      ))}
      <div className="builder-row" style={{ marginTop: 8 }}>
        <div style={{ width: 320, maxWidth: '100%' }}>
          <SourceSelect
            options={addable}
            value={null}
            onChange={(key) => key && onChange(withClassAdded(build, key))}
            placeholder={build.classes.length === 0 ? '— choose a class —' : '+ add another class'}
            ariaLabel={build.classes.length === 0 ? 'Choose a class' : 'Add another class'}
            disabled={total >= MAX_CHARACTER_LEVEL}
          />
        </div>
        <span className="builder-muted">Character level {total}</span>
      </div>
    </Frame>
  );
}

function Review({ characterId, plan, onFinish, finishing, build, name, preview }: StepPageProps) {
  const pending = plan.choices.filter((choice) => choice.pending);
  const ready = pending.length === 0 && plan.problems.length === 0;
  return (
    <>
      <Frame title={ready ? 'Ready to finish' : `${pending.length} choices left`}>
        {pending.map((choice) => (
          <div key={choice.id} className="builder-pending-item">
            <span className="builder-grow">
              <b>{choice.prompt}</b> <span className="builder-muted">{choice.sourceLabel}</span>
            </span>
            <Link className="builder-btn" to={`/characters/${characterId}/build/${dnd5eStepOf(choice)}`}>
              Choose →
            </Link>
          </div>
        ))}
        {plan.problems.map((problem) => (
          <p key={problem} className="builder-problem">
            {problem}
          </p>
        ))}
        {ready && <p className="builder-muted">Level {characterLevel(build as Dnd5eBuild)} · every choice is made.</p>}
      </Frame>
      <Dnd5eReviewCards characterId={characterId} name={name} build={build as Dnd5eBuild} plan={plan} preview={preview} />
      <div className="builder-row" style={{ justifyContent: 'flex-end', marginBottom: 14 }}>
        <button type="button" className="builder-btn builder-btn--primary builder-btn--big" disabled={!ready || finishing} onClick={onFinish}>
          {finishing ? 'Finishing…' : 'Finish character'}
        </button>
      </div>
    </>
  );
}

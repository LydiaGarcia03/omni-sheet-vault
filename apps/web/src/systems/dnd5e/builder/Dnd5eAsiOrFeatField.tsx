import { useState, type ReactNode } from 'react';
import type { CreationChoice } from '../../../builder/draftApi';
import { Segmented } from '../../../builder/Segmented';
import { SourceSelect } from '../../../builder/SourceSelect';
import { CatalogueSourceLabel } from '../../../sheet/CatalogueSourceLabel';
import { ABILITIES } from './dnd5eBuild';

const ASI_KEY = 'asi';

type Mode = 'asi' | 'feat';
type Split = 'plus2' | 'plus1';

type Dnd5eAsiOrFeatFieldProps = {
  /** The ASI_OR_FEAT choice: "asi" or one feat. */
  choice: CreationChoice;
  /** The plan's choices nested under it: the ability increase, or the chosen feat's own picks. */
  nested: CreationChoice[];
  answerFor: (choiceId: string) => string[];
  onAnswer: (choiceId: string, selections: string[]) => void;
  renderChoice: (choice: CreationChoice) => ReactNode;
};

/** Ability Score Improvement or a feat: pick which first, then how to split the increase or which feat. */
export function Dnd5eAsiOrFeatField({ choice, nested, answerFor, onAnswer, renderChoice }: Dnd5eAsiOrFeatFieldProps) {
  const picked = choice.selected[0] ?? null;
  const [featChosen, setFeatChosen] = useState(picked !== null && picked !== ASI_KEY);
  const mode: Mode | null = picked === ASI_KEY ? 'asi' : picked || featChosen ? 'feat' : null;
  const increase = nested.find((candidate) => candidate.type === 'ABILITY_SCORE');
  const featChoices = nested.filter((candidate) => candidate !== increase);
  const feats = choice.options.filter((option) => option.key !== ASI_KEY);
  const feat = feats.find((option) => option.key === picked);
  const pending = choice.pending || nested.some((candidate) => candidate.pending);

  const chooseMode = (next: Mode) => {
    setFeatChosen(next === 'feat');
    if (next === 'asi') {
      onAnswer(choice.id, [ASI_KEY]);
    } else if (picked === ASI_KEY) {
      onAnswer(choice.id, []);
    }
  };

  return (
    <div className={`builder-choice${pending ? ' is-pending' : ''}`}>
      <div>
        <div className="builder-choice__label">{choice.prompt}</div>
        <div className="builder-choice__from">{choice.sourceLabel}</div>
      </div>
      <div className="builder-asi-feat">
        <Segmented
          value={mode}
          options={[
            ['asi', 'Ability Score Improvement'],
            ['feat', 'Feat'],
          ]}
          onChange={chooseMode}
          ariaLabel={choice.prompt}
        />
        {mode === 'asi' && (
          <AbilityIncrease id={increase?.id ?? choice.id.replace(/asi-or-feat$/, 'asi')} choice={increase} answerFor={answerFor} onAnswer={onAnswer} />
        )}
        {mode === 'feat' && (
          <>
            <SourceSelect
              options={feats}
              value={feat?.key ?? null}
              onChange={(key) => onAnswer(choice.id, key ? [key] : [])}
              placeholder="— choose a feat —"
              ariaLabel="Feat"
              clearable
            />
            {feat && (
              <div className="builder-choice__desc">
                <b>{feat.label}</b>
                {feat.summary ? <span> · {feat.summary}</span> : <CatalogueSourceLabel sourceBook={feat.sourceBook} />}
              </div>
            )}
            {featChoices.length > 0 && <div className="builder-asi-feat__nested">{featChoices.map(renderChoice)}</div>}
          </>
        )}
      </div>
    </div>
  );
}

type AbilityIncreaseProps = {
  id: string;
  choice: CreationChoice | undefined;
  answerFor: (choiceId: string) => string[];
  onAnswer: (choiceId: string, selections: string[]) => void;
};

/** +2 to one ability (stored as that ability twice) or +1 to two different ones. */
function AbilityIncrease({ id, choice, answerFor, onAnswer }: AbilityIncreaseProps) {
  const values = answerFor(id);
  const stored: Split | null = values.length === 2 && values[0] === values[1] ? 'plus2' : values.length > 0 ? 'plus1' : null;
  const [split, setSplit] = useState<Split | null>(stored);
  const current = stored ?? split;
  const options = choice?.options ?? ABILITIES.map((ability) => ({ key: ability, label: ability.charAt(0).toUpperCase() + ability.slice(1), sourceBook: null }));

  const chooseSplit = (next: Split) => {
    setSplit(next);
    if (next !== current) {
      onAnswer(id, []);
    }
  };

  return (
    <>
      <Segmented
        value={current}
        options={[
          ['plus2', '+2 to one ability'],
          ['plus1', '+1 to two abilities'],
        ]}
        onChange={chooseSplit}
        ariaLabel="How to split the increase"
      />
      {current === 'plus2' && (
        <SourceSelect
          options={options}
          value={values[0] ?? null}
          onChange={(key) => onAnswer(id, key ? [key, key] : [])}
          placeholder="— choose an ability —"
          ariaLabel="Ability to increase by 2"
        />
      )}
      {current === 'plus1' && (
        <SourceSelect
          multiple
          options={options}
          values={[...new Set(values)]}
          max={2}
          onChange={(keys) => onAnswer(id, keys)}
          placeholder="— choose 2 abilities —"
          ariaLabel="Abilities to increase by 1"
        />
      )}
    </>
  );
}

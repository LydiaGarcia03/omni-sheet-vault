import { CatalogueSourceLabel } from '../sheet/CatalogueSourceLabel';
import type { CreationChoice } from './draftApi';
import { SourceSelect } from './SourceSelect';

type ChoiceFieldProps = {
  choice: CreationChoice;
  onChange: (selected: string[]) => void;
};

/** Renders any plan choice as a source select: a single pick, or several up to the choice's count. */
export function ChoiceField({ choice, onChange }: ChoiceFieldProps) {
  return (
    <div className={`builder-choice${choice.pending ? ' is-pending' : ''}`}>
      <div>
        <div className="builder-choice__label">{choice.prompt}</div>
        <div className="builder-choice__from">
          {choice.sourceLabel}
          {choice.count > 1 && ` · choose ${choice.count}`}
        </div>
      </div>
      <div>
        {choice.count === 1 ? (
          <SourceSelect
            options={choice.options}
            value={choice.selected[0] ?? null}
            onChange={(key) => onChange(key ? [key] : [])}
            placeholder="— choose —"
            ariaLabel={choice.prompt}
            clearable
          />
        ) : (
          <SourceSelect
            multiple
            options={choice.options}
            values={choice.selected}
            max={choice.count}
            onChange={onChange}
            placeholder={`— choose ${choice.count} —`}
            ariaLabel={choice.prompt}
          />
        )}
        <SelectedSummaries choice={choice} />
      </div>
    </div>
  );
}

function SelectedSummaries({ choice }: { choice: CreationChoice }) {
  const selected = choice.options.filter((option) => choice.selected.includes(option.key));
  if (selected.every((option) => !option.summary && !option.sourceBook)) {
    return null;
  }
  return (
    <div className="builder-choice__desc">
      {selected.map((option) => (
        <div key={option.key}>
          <b>{option.label}</b>
          {option.summary ? <span> · {option.summary}</span> : <CatalogueSourceLabel sourceBook={option.sourceBook} />}
        </div>
      ))}
    </div>
  );
}

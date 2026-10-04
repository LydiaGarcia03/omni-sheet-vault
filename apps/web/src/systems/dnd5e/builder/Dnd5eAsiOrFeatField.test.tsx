import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CreationChoice } from '../../../builder/draftApi';
import { Dnd5eAsiOrFeatField } from './Dnd5eAsiOrFeatField';

vi.mock('../../../catalogue/SourceCodes', () => ({
  useSourceCode: () => () => null,
  useLeadingSources: () => [],
}));

const ID = 'class:fighter:4:asi-or-feat';

function asiOrFeat(selected: string[]): CreationChoice {
  return {
    id: ID,
    type: 'ASI_OR_FEAT',
    parentChoiceId: null,
    prompt: 'Ability Score Improvement or a feat',
    sourceLabel: 'Fighter 4',
    count: 1,
    optional: false,
    pending: selected.length === 0,
    options: [
      { key: 'asi', label: 'Ability Score Improvement', sourceBook: null, summary: '+2 to one ability, or +1 to two' },
      { key: 'alert', label: 'Alert', sourceBook: "Player's Handbook", summary: null },
      { key: 'tough', label: 'Tough', sourceBook: "Player's Handbook", summary: null },
    ],
    selected,
  };
}

function renderField(selected: string[], answers: Record<string, string[]> = {}) {
  const onAnswer = vi.fn();
  render(
    <Dnd5eAsiOrFeatField
      choice={asiOrFeat(selected)}
      nested={[]}
      answerFor={(choiceId) => answers[choiceId] ?? []}
      onAnswer={onAnswer}
      renderChoice={() => null}
    />,
  );
  return onAnswer;
}

describe('Dnd5eAsiOrFeatField', () => {
  it('asks ASI or feat first, with nothing else shown', () => {
    renderField([]);

    expect(screen.getByRole('radio', { name: 'Ability Score Improvement' })).toHaveAttribute('aria-checked', 'false');
    expect(screen.getByRole('radio', { name: 'Feat' })).toHaveAttribute('aria-checked', 'false');
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
  });

  it('answers "asi" when Ability Score Improvement is picked', () => {
    const onAnswer = renderField([]);

    fireEvent.click(screen.getByRole('radio', { name: 'Ability Score Improvement' }));

    expect(onAnswer).toHaveBeenCalledWith(ID, ['asi']);
  });

  it('stores +2 to one ability as that ability twice', () => {
    const onAnswer = renderField(['asi']);

    fireEvent.click(screen.getByRole('radio', { name: '+2 to one ability' }));
    fireEvent.click(screen.getByRole('combobox', { name: 'Ability to increase by 2' }));
    fireEvent.click(screen.getByRole('option', { name: 'Strength' }));

    expect(onAnswer).toHaveBeenLastCalledWith('class:fighter:4:asi', ['strength', 'strength']);
  });

  it('reads a stored split back: the same ability twice is +2, two different ones are +1 each', () => {
    renderField(['asi'], { 'class:fighter:4:asi': ['dexterity', 'wisdom'] });

    expect(screen.getByRole('radio', { name: '+1 to two abilities' })).toHaveAttribute('aria-checked', 'true');
    expect(screen.getByRole('combobox', { name: 'Abilities to increase by 1' })).toHaveTextContent('Dexterity, Wisdom');
  });

  it('clears the ASI answer when switching to Feat', () => {
    const onAnswer = renderField(['asi']);

    fireEvent.click(screen.getByRole('radio', { name: 'Feat' }));

    expect(onAnswer).toHaveBeenCalledWith(ID, []);
  });

  it('lists only feats once Feat is picked', () => {
    const onAnswer = renderField([]);

    fireEvent.click(screen.getByRole('radio', { name: 'Feat' }));
    fireEvent.click(screen.getByRole('combobox', { name: 'Feat' }));
    expect(screen.queryByRole('option', { name: 'Ability Score Improvement' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('option', { name: "Tough (Player's Handbook)" }));

    expect(onAnswer).toHaveBeenLastCalledWith(ID, ['tough']);
  });
});

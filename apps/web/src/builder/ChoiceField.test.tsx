import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ChoiceField } from './ChoiceField';
import type { CreationChoice } from './draftApi';

function skills(selected: string[], count = 2): CreationChoice {
  return {
    id: 'class:fighter:skills:0',
    type: 'SKILL',
    parentChoiceId: null,
    prompt: 'Choose skills',
    sourceLabel: 'Fighter 1',
    count,
    optional: false,
    pending: selected.length < count,
    options: ['athletics', 'perception', 'history'].map((key) => ({ key, label: key, sourceBook: null, summary: null })),
    selected,
  };
}

describe('ChoiceField', () => {
  it('adds to a multi-pick until the count is reached', () => {
    const onChange = vi.fn();
    render(<ChoiceField choice={skills(['athletics'])} onChange={onChange} />);

    fireEvent.click(screen.getByRole('combobox', { name: 'Choose skills' }));
    fireEvent.click(screen.getByRole('option', { name: 'perception' }));

    expect(onChange).toHaveBeenCalledWith(['athletics', 'perception']);
  });

  it('ignores a new pick once the count is reached, but lets a selected one be removed', () => {
    const onChange = vi.fn();
    render(<ChoiceField choice={skills(['athletics', 'perception'])} onChange={onChange} />);

    fireEvent.click(screen.getByRole('combobox', { name: 'Choose skills' }));
    fireEvent.click(screen.getByRole('option', { name: 'history' }));
    fireEvent.click(screen.getByRole('option', { name: 'athletics' }));

    expect(screen.getByRole('option', { name: 'history' })).toHaveAttribute('aria-disabled', 'true');
    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith(['perception']);
  });

  it('uses a source select, with each option source, for a long single pick', () => {
    const choice: CreationChoice = {
      ...skills([], 1),
      id: 'build.species',
      prompt: 'Choose a species',
      options: Array.from({ length: 20 }, (_, index) => ({ key: `s${index}`, label: `Species ${index}`, sourceBook: 'PHB', summary: null })),
    };
    const onChange = vi.fn();
    render(<ChoiceField choice={choice} onChange={onChange} />);

    fireEvent.click(screen.getByRole('combobox', { name: 'Choose a species' }));
    fireEvent.click(screen.getByRole('option', { name: 'Species 3 (PHB)' }));

    expect(onChange).toHaveBeenCalledWith(['s3']);
  });

  it("shows a chosen option's effect in place of its book, and the book when it has none", () => {
    const choice: CreationChoice = {
      ...skills(['dueling', 'smiths-tools'], 2),
      options: [
        { key: 'dueling', label: 'Dueling', sourceBook: "Player's Handbook", summary: 'You gain a +2 bonus to damage rolls.' },
        { key: 'smiths-tools', label: "Smith's Tools", sourceBook: "Player's Handbook", summary: null },
      ],
    };
    const { container } = render(<ChoiceField choice={choice} onChange={vi.fn()} />);

    const lines = [...container.querySelectorAll('.builder-choice__desc > div')].map((line) => line.textContent);
    expect(lines).toEqual(['Dueling · You gain a +2 bonus to damage rolls.', "Smith's Tools · Player's Handbook"]);
  });
});

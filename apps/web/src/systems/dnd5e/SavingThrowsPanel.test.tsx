import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CalculatedValue, CharacterSheet, PaneRequest, RollNote } from '../../sheet/api';
import { SavingThrowPane } from './Dnd5eSavingThrowPanes';
import { SavingThrowsPanel } from './SavingThrowsPanel';

const zero: CalculatedValue = { value: 0, contributions: [] };

function sheet(rollNotes: RollNote[] = [], customizations: unknown = {}): CharacterSheet {
  const abilities = ['strength', 'dexterity', 'constitution', 'intelligence', 'wisdom', 'charisma'];
  return {
    savingThrows: Object.fromEntries(abilities.map((ability) => [ability, zero])),
    savingThrowProficiencies: {},
    rollModes: {},
    rollNotes,
    provenance: { customizations },
  } as unknown as CharacterSheet;
}

function renderPanel(sheetValue: CharacterSheet, onOpenPane = vi.fn()) {
  render(<SavingThrowsPanel sheet={sheetValue} onRoll={vi.fn()} onRollContextMenu={vi.fn()} onMutate={vi.fn()} onOpenPane={onOpenPane} />);
  return onOpenPane;
}

describe('SavingThrowsPanel notes', () => {
  it('lists a situational advantage with its source', () => {
    renderPanel(sheet([{ mode: 'ADVANTAGE', target: 'SAVING_THROWS', restriction: 'against poison', source: 'Dwarven Resilience' }]));

    expect(screen.getByText('against poison', { exact: false })).toBeInTheDocument();
    expect(screen.getByLabelText('Advantage: Dwarven Resilience, against poison')).toBeInTheDocument();
  });

  it('ignores notes about other rolls', () => {
    renderPanel(sheet([{ mode: 'ADVANTAGE', target: 'ABILITY_CHECKS', restriction: 'in dim light', source: 'Test' }]));

    expect(screen.queryByText('in dim light', { exact: false })).toBeNull();
  });
});

describe('SavingThrowsPanel panes', () => {
  it('opens the Saving Throws pane from the gear, and a save’s own pane from its abbreviation', () => {
    const onOpenPane = renderPanel(sheet());

    fireEvent.click(screen.getByRole('button', { name: 'Manage Saving Throws' }));
    const savingThrows = onOpenPane.mock.calls[0][0] as PaneRequest;
    render(<>{savingThrows.render(sheet())}</>);
    expect(screen.getByRole('heading', { name: 'Saving Throws' })).toBeInTheDocument();

    fireEvent.click(screen.getAllByRole('button', { name: 'Strength saving throw details' })[0]);
    const strength = onOpenPane.mock.calls[1][0] as PaneRequest;
    render(<>{strength.render(sheet())}</>);
    expect(screen.getByRole('heading', { name: /Strength Saving Throw/ })).toBeInTheDocument();
  });
});

describe('SavingThrowPane', () => {
  it('saves a Customize change with the save’s other stored values', () => {
    const onMutate = vi.fn();
    const stored = { savingThrows: { wisdom: { magicBonus: 1, magicBonusNotes: 'Cloak' } } };
    render(<SavingThrowPane sheet={sheet([], stored)} ability="wisdom" onMutate={onMutate} onOpenSavingThrows={vi.fn()} />);

    fireEvent.change(screen.getByLabelText('Saving Throw Proficiency Level'), { target: { value: 'EXPERT' } });

    expect(onMutate).toHaveBeenCalledWith({
      type: 'CUSTOMIZE',
      group: 'savingThrows',
      target: 'wisdom',
      value: expect.objectContaining({ magicBonus: 1, magicBonusNotes: 'Cloak', proficiencyLevel: 'EXPERT' }),
    });
  });
});

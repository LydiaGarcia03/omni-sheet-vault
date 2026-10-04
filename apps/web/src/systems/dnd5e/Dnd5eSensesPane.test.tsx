import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet } from '../../sheet/api';
import { Dnd5eSensesPane } from './Dnd5eSensesPane';

function sheet(customizations: unknown = {}): CharacterSheet {
  return {
    senses: {
      passivePerception: { value: 12, contributions: [] },
      passiveInvestigation: { value: 9, contributions: [] },
      passiveInsight: { value: 10, contributions: [] },
    },
    specialSenses: [{ type: 'DARKVISION', rangeFeet: 60, label: 'Darkvision 60 ft.' }],
    provenance: { customizations },
  } as unknown as CharacterSheet;
}

describe('Dnd5eSensesPane', () => {
  it('lists the passive scores and special senses as D&D Beyond does', () => {
    render(<Dnd5eSensesPane sheet={sheet()} onMutate={vi.fn()} rulesText="Rules." />);

    expect(screen.getByText('Passive Perception:').parentElement).toHaveTextContent('Passive Perception: 12');
    expect(screen.getByText('Darkvision:').parentElement).toHaveTextContent('Darkvision: 60 ft.');
  });

  it('saves a passive override and a sense distance with their notes', () => {
    const onMutate = vi.fn();
    render(<Dnd5eSensesPane sheet={sheet()} onMutate={onMutate} rulesText="Rules." />);

    fireEvent.change(screen.getByLabelText('Override Passive Insight'), { target: { value: '15' } });
    fireEvent.blur(screen.getByLabelText('Override Passive Insight'));
    fireEvent.change(screen.getByLabelText('Tremorsense distance'), { target: { value: '30' } });
    fireEvent.change(screen.getByLabelText('Tremorsense source notes'), { target: { value: 'Stone Sense' } });
    fireEvent.blur(screen.getByLabelText('Tremorsense source notes'));

    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'passives', target: 'passiveInsight', value: { value: 15, notes: null } });
    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'senses', target: 'TREMORSENSE', value: { value: 30, notes: 'Stone Sense' } });
  });
});

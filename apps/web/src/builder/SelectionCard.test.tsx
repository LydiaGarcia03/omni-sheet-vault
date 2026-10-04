import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { SelectionCard } from './SelectionCard';

vi.mock('../catalogue/SourceCodes', () => ({
  useSourceCode: () => () => 'PHB',
  useLeadingSources: () => [],
}));

describe('SelectionCard', () => {
  it('shows the facts as a card and each grant with its text', () => {
    render(
      <SelectionCard
        detail={{
          name: 'Mountain Dwarf',
          sourceBook: "Player's Handbook",
          facts: [
            { label: 'Speed', text: 'Speed 25 ft' },
            { label: 'Size', text: 'Medium' },
          ],
          grants: [
            { label: 'Dwarven Resilience', text: 'Advantage on saving throws against poison.' },
            { label: 'Stonecunning', text: null },
          ],
        }}
        grantsTitle="Traits"
      />,
    );

    expect(screen.getByText('Mountain Dwarf')).toBeInTheDocument();
    expect(screen.getByText('Speed 25 ft')).toBeInTheDocument();
    expect(screen.getByText('Traits')).toBeInTheDocument();
    expect(screen.getByText('Advantage on saving throws against poison.')).toBeInTheDocument();
    expect(screen.getByText('Stonecunning')).toBeInTheDocument();
  });

  it('skips the card when there are no facts', () => {
    const { container } = render(
      <SelectionCard
        detail={{ name: 'Soldier', sourceBook: null, facts: [], grants: [{ label: 'Feature: Military Rank', text: 'Soldiers defer to you.' }] }}
        grantsTitle="What you get"
      />,
    );

    expect(container.querySelector('.builder-facts')).toBeNull();
    expect(screen.getByText('What you get')).toBeInTheDocument();
  });
});

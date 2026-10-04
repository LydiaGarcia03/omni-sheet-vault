import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet } from '../../sheet/api';
import { ExplainerPanel } from '../../sheet/ExplainerPanel';
import { ProficienciesPanel } from './ProficienciesPanel';

function sheet(provenance: CharacterSheet['provenance']): CharacterSheet {
  return {
    armorProficiencies: ['Light'],
    weaponProficiencies: [],
    toolProficiencies: ["Thieves' Tools (Expertise)"],
    languages: ['Common'],
    provenance,
  } as unknown as CharacterSheet;
}

describe('ProficienciesPanel sources', () => {
  it('shows where each build-granted entry came from as its tooltip', () => {
    render(
      <ProficienciesPanel
        sheet={sheet({ abilityScores: {}, abilityBreakdowns: {}, speed: { value: 30, contributions: [] }, proficiencySources: [
          { kind: 'ARMOR', label: 'Light', source: 'Rogue 1' },
          { kind: 'TOOL', label: "Thieves' Tools (Expertise)", source: 'Background' },
        ] })}
        onMutate={vi.fn()}
        onOpenPane={vi.fn()}
      />,
    );

    expect(screen.getByText('Light', { exact: false })).toHaveAttribute('title', 'From Rogue 1');
    expect(screen.getByText("Thieves' Tools (Expertise)", { exact: false })).toHaveAttribute('title', 'From Background');
    expect(screen.getByText('Common', { exact: false })).not.toHaveAttribute('title');
  });

  it('works for a sheet with no provenance', () => {
    render(<ProficienciesPanel sheet={sheet(null)} onMutate={vi.fn()} onOpenPane={vi.fn()} />);

    expect(screen.getByText('Light', { exact: false })).not.toHaveAttribute('title');
  });
});

describe('ExplainerPanel heading', () => {
  it('puts the heading above the contributions', () => {
    render(
      <ExplainerPanel
        kind="explainer"
        title="Dexterity 18"
        formattedValue="+4"
        contributionsHeading="Total Score 18"
        contributions={[{ source: 'Base', amount: 15 }, { source: 'Changeling', amount: 2 }, { source: 'ASI', amount: 1 }]}
      />,
    );

    expect(screen.getByText('Total Score 18')).toBeInTheDocument();
    expect(screen.getByText('Changeling')).toBeInTheDocument();
  });
});

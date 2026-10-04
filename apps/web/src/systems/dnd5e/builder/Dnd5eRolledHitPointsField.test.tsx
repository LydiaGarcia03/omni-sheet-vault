import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CreationChoice } from '../../../builder/draftApi';
import { postCreationRoll } from '../../../dice/api';
import type { Dnd5eBuild } from './dnd5eBuild';
import { Dnd5eRolledHitPointsField, unrolledHitPointLevels } from './Dnd5eRolledHitPointsField';

vi.mock('../../../dice/api', () => ({ postCreationRoll: vi.fn() }));

const CHOICE: CreationChoice = {
  id: 'build.rolledHitPoints',
  type: 'ROLLED_HIT_POINTS',
  parentChoiceId: null,
  prompt: 'Roll one hit die per level after the first',
  sourceLabel: 'Hit Points',
  count: 2,
  optional: false,
  pending: true,
  options: [
    { key: '0', label: 'Fighter 2', sourceBook: null, summary: null, data: { group: 'fighter', sides: 10, average: 6, rollLabel: 'Hit points, Fighter 2 (d10)' } },
    { key: '1', label: 'Wizard 1', sourceBook: null, summary: null, data: { group: 'wizard', sides: 6, average: 4, rollLabel: 'Hit points, Wizard 1 (d6)' } },
  ],
  selected: [],
};

const BUILD: Dnd5eBuild = {
  speciesSlug: null,
  subspeciesName: null,
  speciesVariantName: null,
  backgroundSlug: null,
  classes: [],
  abilityScoreMethod: null,
  baseAbilityScores: {},
  hitPointMethod: 'ROLLED',
  rolledHitPoints: [],
  choices: [],
};

describe('Dnd5eRolledHitPointsField', () => {
  it('shows one row per level with its die, and the rolled total against the average', () => {
    render(<Dnd5eRolledHitPointsField characterId="c1" choice={CHOICE} build={{ ...BUILD, rolledHitPoints: [7, null] }} onChange={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'Roll d10' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Roll d6' })).toBeInTheDocument();
    expect(screen.getByLabelText('Hit points, Fighter 2')).toHaveValue(7);
    expect(screen.getByLabelText('Hit points, Wizard 1')).toHaveValue(null);
    expect(screen.getByText('Rolled total 7')).toBeInTheDocument();
    expect(screen.getByText('Average would be 10')).toBeInTheDocument();
  });

  it('keeps a typed result in its own row, leaving the others unrolled', () => {
    const onChange = vi.fn();
    render(<Dnd5eRolledHitPointsField characterId="c1" choice={CHOICE} build={BUILD} onChange={onChange} />);

    fireEvent.change(screen.getByLabelText('Hit points, Wizard 1'), { target: { value: '5' } });

    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ rolledHitPoints: [null, 5] }));
  });

  it("shows only one class's rows, writing each at its own place in the build", () => {
    const onChange = vi.fn();
    render(<Dnd5eRolledHitPointsField characterId="c1" choice={CHOICE} build={{ ...BUILD, rolledHitPoints: [7, null] }} onChange={onChange} group="wizard" />);

    expect(screen.queryByLabelText('Hit points, Fighter 2')).toBeNull();
    expect(screen.getByText('Average would be 4')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Hit points, Wizard 1'), { target: { value: '3' } });

    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ rolledHitPoints: [7, 3] }));
    expect(unrolledHitPointLevels(CHOICE, { ...BUILD, rolledHitPoints: [7, null] }, 'wizard')).toBe(1);
    expect(unrolledHitPointLevels(CHOICE, { ...BUILD, rolledHitPoints: [7, null] }, 'fighter')).toBe(0);
  });

  it("takes a level's fixed value instead of rolling it", () => {
    const onChange = vi.fn();
    render(<Dnd5eRolledHitPointsField characterId="c1" choice={CHOICE} build={{ ...BUILD, rolledHitPoints: [7, null] }} onChange={onChange} />);

    fireEvent.click(screen.getByRole('button', { name: 'Use fixed value for Wizard 1' }));

    expect(screen.getByRole('button', { name: 'Use fixed value for Fighter 2' })).toHaveTextContent('Use fixed (6)');
    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ rolledHitPoints: [7, 4] }));
  });

  it('rolls a level on the server with its label and writes the result', async () => {
    vi.mocked(postCreationRoll).mockResolvedValue({
      id: 'r1',
      expression: '1d10',
      context: 'Hit points, Fighter 2 (d10)',
      results: [8],
      dropped: [],
      total: 8,
      rolledAt: '2026-09-26T00:00:00Z',
    });
    const onChange = vi.fn();
    render(<Dnd5eRolledHitPointsField characterId="c1" choice={CHOICE} build={BUILD} onChange={onChange} />);

    fireEvent.click(screen.getByRole('button', { name: 'Roll d10' }));

    await waitFor(() => expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ rolledHitPoints: [8, null] })));
    expect(postCreationRoll).toHaveBeenCalledWith('c1', { sides: 10, count: 1, keepHighest: null, context: 'Hit points, Fighter 2 (d10)' });
    expect(screen.queryByLabelText(/^Rolled/)).toBeNull();
  });
});

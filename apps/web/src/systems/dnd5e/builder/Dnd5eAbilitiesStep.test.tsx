import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CreationChoice, DraftPreview } from '../../../builder/draftApi';
import { postCreationRoll } from '../../../dice/api';
import { Dnd5eAbilitiesStep } from './Dnd5eAbilitiesStep';
import type { Dnd5eBuild } from './dnd5eBuild';

vi.mock('../../../dice/api', () => ({ postCreationRoll: vi.fn() }));

const METHODS: CreationChoice = {
  id: 'build.abilityScores',
  type: 'ABILITY_SCORES',
  parentChoiceId: null,
  prompt: 'Set your ability scores',
  sourceLabel: 'Abilities',
  count: 1,
  optional: false,
  pending: false,
  options: [
    { key: 'STANDARD_ARRAY', label: 'Standard array', sourceBook: null, summary: null, data: { values: [15, 14, 13, 12, 10, 8] } },
    { key: 'POINT_BUY', label: 'Point buy', sourceBook: null, summary: null, data: { budget: 27, costs: { 8: 0, 9: 1, 10: 2, 11: 3, 12: 4, 13: 5, 14: 7, 15: 9 } } },
    {
      key: 'MANUAL',
      label: 'Manual / rolled',
      sourceBook: null,
      summary: null,
      data: { min: 1, max: 30, roll: { count: 4, sides: 6, keepHighest: 3, label: '4d6 drop lowest' } },
    },
  ],
  selected: ['POINT_BUY'],
};

const BUILD: Dnd5eBuild = {
  speciesSlug: 'dwarf',
  subspeciesName: 'Mountain',
  speciesVariantName: null,
  backgroundSlug: null,
  classes: [],
  abilityScoreMethod: 'POINT_BUY',
  baseAbilityScores: { strength: 15, dexterity: 15, constitution: 8, intelligence: 8, wisdom: 10, charisma: 8 },
  hitPointMethod: 'FIXED',
  rolledHitPoints: [],
  choices: [],
};

const PREVIEW: DraftPreview = {
  vitals: null,
  abilities: ['strength', 'dexterity', 'constitution', 'intelligence', 'wisdom', 'charisma'].map((ability) => {
    const base = BUILD.baseAbilityScores[ability];
    const bonus = ability === 'strength' || ability === 'constitution' ? 2 : 0;
    return {
      ability,
      score: base + bonus,
      modifier: Math.floor((base + bonus - 10) / 2),
      contributions: [{ source: 'Base', amount: base }, ...(bonus ? [{ source: 'Mountain Dwarf', amount: bonus }] : [])],
    };
  }),
};

describe('Dnd5eAbilitiesStep', () => {
  it('shows the point budget, each total with its species bonus, and the bonus source', () => {
    const { container } = render(<Dnd5eAbilitiesStep characterId="c1" build={BUILD} choices={[METHODS]} problems={[]} preview={PREVIEW} onChange={vi.fn()} />);

    expect(screen.getByText('7 / 27')).toBeInTheDocument();
    expect(container.querySelector('.builder-asi')?.textContent).toBe('Ability Score Increase · Mountain DwarfStrength +2, Constitution +2');
    expect(screen.getAllByText('(+2)')).toHaveLength(2);
  });

  it('shortens an Ability Score Improvement to ASI in the score calculations only', () => {
    const withAsi: DraftPreview = {
      ...PREVIEW,
      abilities: PREVIEW.abilities.map((ability) =>
        ability.ability === 'dexterity'
          ? { ...ability, score: ability.score + 2, contributions: [...ability.contributions, { source: 'Ability Score Improvement (Bard 4)', amount: 2 }] }
          : ability,
      ),
    };
    const { container } = render(<Dnd5eAbilitiesStep characterId="c1" build={BUILD} choices={[METHODS]} problems={[]} preview={withAsi} onChange={vi.fn()} />);

    const lines = [...container.querySelectorAll('.builder-asi')].map((line) => line.textContent);
    expect(lines).toContain('Ability Score Increase · Ability Score Improvement (Bard 4)Dexterity +2');
    expect(screen.getByText('ASI (Bard 4)')).toBeInTheDocument();
  });

  it('only offers scores the remaining points can pay for', () => {
    render(<Dnd5eAbilitiesStep characterId="c1" build={BUILD} choices={[METHODS]} problems={[]} preview={PREVIEW} onChange={vi.fn()} />);

    const constitution = screen.getByLabelText('con');
    const offered = [...constitution.querySelectorAll('option')].map((option) => option.value);
    expect(offered).toEqual(['8', '9', '10', '11', '12', '13', '14']);
  });

  it('asks before switching method, then clears the scores', () => {
    const onChange = vi.fn();
    render(<Dnd5eAbilitiesStep characterId="c1" build={BUILD} choices={[METHODS]} problems={[]} preview={PREVIEW} onChange={onChange} />);

    fireEvent.change(screen.getByRole('combobox', { name: 'Generation method' }), { target: { value: 'STANDARD_ARRAY' } });
    expect(onChange).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: 'Switch' }));

    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ abilityScoreMethod: 'STANDARD_ARRAY', baseAbilityScores: {} }));
  });

  it('rolls 4d6 drop lowest on the server, writes the total, and strikes the dropped die', async () => {
    vi.mocked(postCreationRoll).mockResolvedValue({
      id: 'r1',
      expression: '4d6kh3',
      context: 'Strength (4d6 drop lowest)',
      results: [5, 2, 6, 3],
      dropped: [1],
      total: 14,
      rolledAt: '2026-09-26T00:00:00Z',
    });
    const onChange = vi.fn();
    const manual: Dnd5eBuild = { ...BUILD, abilityScoreMethod: 'MANUAL' };
    const { container } = render(
      <Dnd5eAbilitiesStep characterId="c1" build={manual} choices={[METHODS]} problems={[]} preview={PREVIEW} onChange={onChange} />,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Roll Strength' }));

    expect(await screen.findByLabelText('Rolled 5, 2, 6, 3')).toBeInTheDocument();
    expect(postCreationRoll).toHaveBeenCalledWith('c1', {
      count: 4,
      sides: 6,
      keepHighest: 3,
      context: 'Strength (4d6 drop lowest)',
    });
    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({ baseAbilityScores: expect.objectContaining({ strength: 14 }) }));
    expect(container.querySelector('.builder-die.is-dropped')?.textContent).toBe('2');
  });

  it('offers no roll button outside the manual method', () => {
    render(<Dnd5eAbilitiesStep characterId="c1" build={BUILD} choices={[METHODS]} problems={[]} preview={PREVIEW} onChange={vi.fn()} />);

    expect(screen.queryByRole('button', { name: 'Roll Strength' })).toBeNull();
  });
});

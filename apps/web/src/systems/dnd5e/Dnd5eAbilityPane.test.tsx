import { fireEvent, render, screen, within } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { AbilityBreakdown } from '../../sheet/api';
import { Dnd5eAbilityPane } from './Dnd5eAbilityPane';

const STRENGTH: AbilityBreakdown = {
  total: 17,
  modifier: 3,
  base: 15,
  bonus: 2,
  bonusSources: [{ source: 'Mountain Dwarf', amount: 2 }],
  setScore: 0,
  stackingBonus: 0,
  otherModifier: null,
  overrideScore: null,
};

describe('Dnd5eAbilityPane', () => {
  it("lays the score out as D&D Beyond's table, with the bonus's sources under it", () => {
    render(<Dnd5eAbilityPane label="Strength" icon={null} breakdown={STRENGTH} onCustomize={vi.fn()} rulesText="Strength rules." />);

    expect(screen.getByRole('heading')).toHaveTextContent('Strength 17(+3)');
    const rows = within(screen.getByRole('table')).getAllByRole('row').map((row) => row.textContent);
    expect(rows).toEqual([
      'Total Score17',
      'Modifier+3',
      'Base Score15',
      'Bonus+2',
      'Mountain Dwarf(+2)',
      'Set Score0',
      'Stacking Bonus+0',
    ]);
  });

  it('saves the Other Modifier and the Override Score together when a field loses focus', () => {
    const onCustomize = vi.fn();
    render(<Dnd5eAbilityPane label="Strength" icon={null} breakdown={{ ...STRENGTH, overrideScore: 19 }} onCustomize={onCustomize} rulesText="" />);

    const other = screen.getByLabelText('Other Modifier');
    fireEvent.change(other, { target: { value: '2' } });
    fireEvent.blur(other);
    const override = screen.getByLabelText('Override Score');
    fireEvent.change(override, { target: { value: '' } });
    fireEvent.blur(override);

    expect(onCustomize).toHaveBeenNthCalledWith(1, { otherModifier: 2, overrideScore: 19 });
    expect(onCustomize).toHaveBeenNthCalledWith(2, { otherModifier: null, overrideScore: null });
  });
});

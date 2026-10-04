import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CreationChoice } from '../../../builder/draftApi';
import { SpellPicker } from './SpellPicker';

function spell(key: string, label: string, level: number, school: string, damageType: string | null, sourceBook = "Player's Handbook") {
  return {
    key,
    label,
    sourceBook,
    summary: null,
    data: {
      level,
      school,
      castingTime: '1 Action',
      range: '60 ft.',
      duration: 'Instantaneous',
      components: 'V, S',
      concentration: false,
      ritual: false,
      attackRoll: damageType !== null,
      saveAbility: null,
      damageType,
      damageDiceCount: damageType ? 1 : null,
      damageDiceSides: damageType ? 10 : null,
      effectSummary: null,
    },
  };
}

function cantrips(selected: string[]): CreationChoice {
  return {
    id: 'class:wizard:cantrips',
    type: 'CANTRIP',
    parentChoiceId: null,
    prompt: 'Choose 2 Wizard cantrip(s)',
    sourceLabel: 'Wizard 1',
    count: 2,
    optional: false,
    pending: selected.length < 2,
    options: [
      spell('fire-bolt', 'Fire Bolt', 0, 'evocation', 'fire'),
      spell('ray-of-frost', 'Ray of Frost', 0, 'evocation', 'cold'),
      spell('mage-hand', 'Mage Hand', 0, 'conjuration', null),
      spell('booming-blade', 'Booming Blade', 0, 'evocation', 'thunder', "Tasha's Cauldron of Everything"),
    ],
    selected,
  };
}

describe('SpellPicker', () => {
  it('filters by school, damage type and search', () => {
    render(<SpellPicker choice={cantrips([])} onChange={vi.fn()} />);

    expect(screen.queryByRole('combobox', { name: 'School' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /^Filters/ }));
    fireEvent.change(screen.getByRole('combobox', { name: 'School' }), { target: { value: 'evocation' } });
    fireEvent.change(screen.getByRole('combobox', { name: 'Damage type' }), { target: { value: 'fire' } });

    expect(screen.getByText('1 of 4 spells')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Filters · 2/ })).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByRole('button', { name: 'Choose Fire Bolt' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Clear filters' }));
    fireEvent.change(screen.getByRole('textbox', { name: 'Search spells' }), { target: { value: 'hand' } });

    expect(screen.getByText('1 of 4 spells')).toBeInTheDocument();
  });

  it('starts with the list hidden once the choice is complete, and shows it on request', () => {
    render(<SpellPicker choice={cantrips(['fire-bolt', 'mage-hand'])} onChange={vi.fn()} />);

    expect(screen.queryByRole('textbox', { name: 'Search spells' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /Show spells/ }));

    expect(screen.getByRole('textbox', { name: 'Search spells' })).toBeInTheDocument();
  });

  it('adds spells until the count is reached and removes a chosen one', () => {
    const onChange = vi.fn();
    render(<SpellPicker choice={cantrips(['fire-bolt', 'mage-hand'])} onChange={onChange} />);
    fireEvent.click(screen.getByRole('button', { name: /Show spells/ }));

    expect(screen.getByRole('button', { name: 'Choose Ray of Frost' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Remove Mage Hand' }));

    expect(onChange).toHaveBeenCalledWith(['fire-bolt']);
  });
});

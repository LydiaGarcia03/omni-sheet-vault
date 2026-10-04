import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet, DefenseEntry } from '../../sheet/api';
import { defenseLabels, displayedMovement, Dnd5eArmorClassPane, Dnd5eDefensesPane, Dnd5eInitiativePane, Dnd5eSpeedPane } from './Dnd5eCombatPanes';

const POISON: DefenseEntry = { key: null, type: 'RESISTANCE', name: 'Poison', source: 'Dwarf', custom: false, notes: null };
const COLD: DefenseEntry = { key: 'd1', type: 'RESISTANCE', name: 'Cold', source: null, custom: true, notes: 'Ring' };

function sheet(overrides: Partial<CharacterSheet> = {}, customizations: unknown = {}): CharacterSheet {
  return {
    speed: 25,
    initiative: { value: 3, contributions: [] },
    armorClass: { value: 16, contributions: [{ source: 'Armor (Scale Mail)', amount: 14 }, { source: 'Dexterity', amount: 2 }] },
    damageResistances: ['Poison', 'Cold'],
    defenses: [POISON, COLD],
    provenance: { customizations },
    ...overrides,
  } as unknown as CharacterSheet;
}

describe('displayedMovement', () => {
  it('shows walking unless the chosen movement has a speed', () => {
    expect(displayedMovement(sheet())).toEqual({ label: 'Walking', speed: 25 });
    expect(displayedMovement(sheet({}, { movementDisplay: 'flying' }))).toEqual({ label: 'Walking', speed: 25 });
    expect(displayedMovement(sheet({}, { movementDisplay: 'flying', speeds: { flying: { value: 50, notes: null } } }))).toEqual({
      label: 'Flying',
      speed: 50,
    });
  });
});

describe('defenseLabels', () => {
  it('marks a hand-added defense with an asterisk, and falls back without detailed defenses', () => {
    expect(defenseLabels(sheet(), 'RESISTANCE', [])).toEqual(['Poison', 'Cold*']);
    expect(defenseLabels(sheet({ defenses: undefined }), 'RESISTANCE', ['Fire'])).toEqual(['Fire']);
  });
});

describe('Dnd5eSpeedPane', () => {
  it('saves an overridden speed and the movement display', () => {
    const onMutate = vi.fn();
    render(<Dnd5eSpeedPane sheet={sheet()} onMutate={onMutate} />);

    fireEvent.change(screen.getByLabelText('Flying'), { target: { value: '40' } });
    fireEvent.blur(screen.getByLabelText('Flying'));
    fireEvent.change(screen.getByLabelText('Set Movement Display'), { target: { value: 'flying' } });

    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'speeds', target: 'flying', value: { value: 40, notes: null } });
    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'movementDisplay', target: 'flying', value: {} });
  });
});

describe('Dnd5eInitiativePane', () => {
  it('shows the initiative score with advantage and disadvantage', () => {
    render(<Dnd5eInitiativePane sheet={sheet()} />);

    expect(screen.getByText('13')).toBeInTheDocument();
    expect(screen.getByText('18')).toBeInTheDocument();
    expect(screen.getByText('8')).toBeInTheDocument();
  });
});

describe('Dnd5eArmorClassPane', () => {
  it('lists the contributions and saves an override', () => {
    const onMutate = vi.fn();
    render(<Dnd5eArmorClassPane sheet={sheet()} onMutate={onMutate} />);

    expect(screen.getByText('Armor (Scale Mail)')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Override AC'), { target: { value: '19' } });
    fireEvent.blur(screen.getByLabelText('Override AC'));

    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'armorClass', target: 'override', value: { value: 19, notes: null } });
  });
});

describe('Dnd5eDefensesPane', () => {
  it('lists defenses with their source, adds one from the sub-type and deletes a custom one', () => {
    const onMutate = vi.fn();
    const stored = { defenses: [{ key: 'd1', type: 'RESISTANCE', subtype: 'cold', notes: 'Ring' }] };
    render(<Dnd5eDefensesPane sheet={sheet({}, stored)} onMutate={onMutate} />);

    expect(screen.getByText('(Dwarf)')).toBeInTheDocument();
    expect(screen.getByText('(Custom)')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Defense Type'), { target: { value: 'IMMUNITY' } });
    fireEvent.change(screen.getByLabelText('Defense Sub-Type'), { target: { value: 'charmed' } });
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));

    expect(onMutate).toHaveBeenCalledWith({ type: 'CUSTOMIZE', group: 'defenses', target: 'new', value: { type: 'IMMUNITY', subtype: 'charmed' } });
    expect(onMutate).toHaveBeenCalledWith({ type: 'REMOVE_CUSTOMIZATION', group: 'defenses', target: 'd1' });
  });
});

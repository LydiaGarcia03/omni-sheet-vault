import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CreationChoice } from '../../../builder/draftApi';
import { Dnd5eEquipmentStep, StartingInventory } from './Dnd5eEquipmentStep';

vi.mock('../../../catalogue/SourceCodes', () => ({
  useSourceCode: () => () => null,
  useLeadingSources: () => [],
}));

const { postCreationRoll } = vi.hoisted(() => ({ postCreationRoll: vi.fn() }));
vi.mock('../../../dice/api', () => ({ postCreationRoll }));

function choice(id: string, type: string, overrides: Partial<CreationChoice> = {}): CreationChoice {
  return {
    id,
    type,
    parentChoiceId: null,
    prompt: 'Choose starting equipment',
    sourceLabel: 'Fighter',
    count: 1,
    optional: false,
    pending: true,
    options: [],
    selected: [],
    ...overrides,
  };
}

const METHOD = choice('class:fighter:equipment-method', 'EQUIPMENT_METHOD', {
  prompt: 'Take the starting equipment, or starting gold',
  options: [
    { key: 'equipment', label: 'Starting equipment', sourceBook: null, summary: null },
    { key: 'gold', label: 'Starting gold', sourceBook: null, summary: '5d4 × 10 gp' },
  ],
  selected: ['equipment'],
  pending: false,
});

const WEAPONS = choice('class:fighter:equipment:1', 'EQUIPMENT', {
  parentChoiceId: METHOD.id,
  options: [
    { key: 'a', label: '(a) a martial weapon and a shield', sourceBook: null, summary: null },
    { key: 'b', label: '(b) two martial weapons', sourceBook: null, summary: null },
  ],
  selected: ['b'],
});

const TWO_WEAPONS = choice('class:fighter:equipment:1:0', 'EQUIPMENT_ITEM', {
  parentChoiceId: WEAPONS.id,
  prompt: 'Choose 2 martial weapon',
  count: 2,
  options: [
    { key: 'longsword', label: 'Longsword', sourceBook: "Player's Handbook", summary: null },
    { key: 'warhammer', label: 'Warhammer', sourceBook: "Player's Handbook", summary: null },
  ],
  selected: ['longsword'],
});

describe('Dnd5eEquipmentStep', () => {
  it('shows equipment or gold as buttons and each option on its own line', () => {
    render(<Dnd5eEquipmentStep characterId="c1" choices={[METHOD, WEAPONS, TWO_WEAPONS]} onAnswer={vi.fn()} />);

    expect(screen.getByRole('radio', { name: 'Equipment' })).toHaveAttribute('aria-checked', 'true');
    expect(screen.getByRole('radio', { name: 'Gold' })).toHaveAttribute('aria-checked', 'false');
    expect(screen.getByRole('radio', { name: '(a) a martial weapon and a shield' })).toHaveAttribute('aria-checked', 'false');
    expect(screen.getByRole('radio', { name: '(b) two martial weapons' })).toHaveAttribute('aria-checked', 'true');
  });

  it('picks the other line of a pair', () => {
    const onAnswer = vi.fn();
    render(<Dnd5eEquipmentStep characterId="c1" choices={[METHOD, WEAPONS, TWO_WEAPONS]} onAnswer={onAnswer} />);

    fireEvent.click(screen.getByRole('radio', { name: '(a) a martial weapon and a shield' }));

    expect(onAnswer).toHaveBeenCalledWith(WEAPONS.id, ['a']);
  });

  it('opens one item select per unit under the chosen line, so the same weapon can be taken twice', () => {
    const onAnswer = vi.fn();
    render(<Dnd5eEquipmentStep characterId="c1" choices={[METHOD, WEAPONS, TWO_WEAPONS]} onAnswer={onAnswer} />);

    fireEvent.click(screen.getByRole('combobox', { name: 'Choose 2 martial weapon (2)' }));
    fireEvent.click(screen.getByRole('option', { name: "Longsword (Player's Handbook)" }));

    expect(onAnswer).toHaveBeenCalledWith(TWO_WEAPONS.id, ['longsword', 'longsword']);
  });

  it('hides the packages once gold is taken and shows the amount', () => {
    render(<Dnd5eEquipmentStep characterId="c1" choices={[{ ...METHOD, selected: ['gold'] }, WEAPONS]} onAnswer={vi.fn()} />);

    expect(screen.getByText('Starting gold: 5d4 × 10 gp')).toBeInTheDocument();
    expect(screen.queryByRole('radio', { name: '(b) two martial weapons' })).not.toBeInTheDocument();
  });

  const GOLD_ROLL = choice('class:fighter:equipment-gold', 'STARTING_GOLD', {
    parentChoiceId: METHOD.id,
    prompt: 'Roll your starting gold',
    options: Array.from({ length: 16 }, (_, index) => ({
      key: String(index + 5),
      label: `${(index + 5) * 10} gp`,
      sourceBook: null,
      summary: '5d4 × 10',
      data: { count: 5, sides: 4, multiplier: 10, gold: (index + 5) * 10, rollLabel: 'Starting gold, Fighter (5d4 × 10)' },
    })),
  });

  it('rolls the gold dice and answers with the total', async () => {
    postCreationRoll.mockResolvedValue({ total: 12 });
    const onAnswer = vi.fn();
    render(<Dnd5eEquipmentStep characterId="c1" choices={[{ ...METHOD, selected: ['gold'] }, GOLD_ROLL]} onAnswer={onAnswer} />);

    fireEvent.click(screen.getByRole('button', { name: 'Roll 5d4' }));

    await waitFor(() => expect(onAnswer).toHaveBeenCalledWith(GOLD_ROLL.id, ['12']));
    expect(postCreationRoll).toHaveBeenCalledWith('c1', { sides: 4, count: 5, keepHighest: null, context: 'Starting gold, Fighter (5d4 × 10)' });
  });

  it('shows a rolled total multiplied into gold pieces', () => {
    const { container } = render(
      <Dnd5eEquipmentStep characterId="c1" choices={[{ ...METHOD, selected: ['gold'] }, { ...GOLD_ROLL, selected: ['12'], pending: false }]} onAnswer={vi.fn()} />,
    );

    expect(container.querySelector('.builder-equip-gold__result')).toHaveTextContent('12 × 10 = 120 gp');
    expect(screen.getByRole('textbox', { name: 'Starting gold roll (5d4)' })).toHaveValue('12');
  });

  it('answers a typed total only when the dice can roll it', () => {
    const onAnswer = vi.fn();
    render(<Dnd5eEquipmentStep characterId="c1" choices={[{ ...METHOD, selected: ['gold'] }, GOLD_ROLL]} onAnswer={onAnswer} />);
    const input = screen.getByRole('textbox', { name: 'Starting gold roll (5d4)' });

    fireEvent.change(input, { target: { value: '3' } });
    expect(onAnswer).toHaveBeenLastCalledWith(GOLD_ROLL.id, []);
    expect(screen.getByText('5d4 rolls between 5 and 20.')).toBeInTheDocument();

    fireEvent.change(input, { target: { value: '17' } });
    expect(onAnswer).toHaveBeenLastCalledWith(GOLD_ROLL.id, ['17']);
  });
});

describe('StartingInventory', () => {
  const EQUIPMENT = {
    inventory: [
      { key: 'chain-mail#0', name: 'Chain Mail', quantity: 1, equipAction: 'WEAR' as const, equipped: true },
      { key: 'dagger#0', name: 'Dagger', quantity: 1, equipAction: 'WIELD' as const, equipped: false },
      { key: null, name: 'insignia of rank', quantity: 1, equipAction: null, equipped: false },
      { key: 'dagger#1', name: 'Dagger', quantity: 2, equipAction: 'WIELD' as const, equipped: false },
    ],
    gold: 10,
    silver: 0,
    copper: 5,
  };

  it('lists every starting item, with Wear or Wield only where it can start equipped, and the money read-only', () => {
    render(<StartingInventory startingEquipment={EQUIPMENT} onEquip={vi.fn()} />);

    expect(screen.getByText('Current inventory (4)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '✓ Worn' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getAllByRole('button', { name: 'Wield' })).toHaveLength(2);
    expect(screen.getByText('insignia of rank')).toBeInTheDocument();
    expect(screen.getByText('× 2')).toBeInTheDocument();
    expect(screen.getByText('10 gp · 5 cp')).toBeInTheDocument();
    expect(screen.queryByRole('textbox')).toBeNull();
  });

  it('toggles each line by its own key, so two lines of the same item are equipped apart', () => {
    const onEquip = vi.fn();
    render(<StartingInventory startingEquipment={EQUIPMENT} onEquip={onEquip} />);

    fireEvent.click(screen.getAllByRole('button', { name: 'Wield' })[1]);
    fireEvent.click(screen.getByRole('button', { name: '✓ Worn' }));

    expect(onEquip).toHaveBeenNthCalledWith(1, 'dagger#1', true);
    expect(onEquip).toHaveBeenNthCalledWith(2, 'chain-mail#0', false);
  });
});

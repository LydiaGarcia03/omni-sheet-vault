import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { Item } from '../../sheet/api';
import { equippableFirst } from './InventoryTab';
import { ItemRow } from './ItemRow';

function item(overrides: Partial<Item>): Item {
  return {
    key: 'item',
    name: 'Item',
    quantity: 1,
    cost: '',
    notes: '',
    equipped: false,
    attuned: false,
    requiresAttunement: false,
    charges: null,
    chargesUsed: 0,
    itemKind: 'GEAR',
    rarity: 'none',
    storageLocation: 'EQUIPMENT',
    weightKg: null,
    source: null,
    equippable: false,
    ...overrides,
  };
}

const LONGSWORD = item({ key: 'longsword', name: 'Longsword', itemKind: 'WEAPON', equippable: true });
const ROPE = item({ key: 'rope', name: 'Rope' });

function renderRow(row: Item, onMutate = vi.fn()) {
  render(<ItemRow item={row} onMutate={onMutate} onOpenDetail={vi.fn()} hasBagOfHolding={false} />);
  return onMutate;
}

describe('ItemRow', () => {
  it('offers the equip box for an item that can be equipped', () => {
    const onMutate = renderRow(LONGSWORD);

    fireEvent.click(screen.getByRole('button', { name: 'Longsword equipped: no' }));

    expect(onMutate).toHaveBeenCalledWith({ type: 'TOGGLE_ITEM_EQUIPPED', itemKey: 'longsword' });
  });

  it('has no equip box for an item that cannot be equipped', () => {
    renderRow(ROPE);

    expect(screen.queryByRole('button', { name: /Rope equipped/ })).toBeNull();
  });

  it('still lets an equipped item be taken off, even one that could not be equipped now', () => {
    renderRow({ ...ROPE, equipped: true });

    expect(screen.getByRole('button', { name: 'Rope equipped: yes' })).toBeInTheDocument();
  });
});

describe('equippableFirst', () => {
  it('lists equippable items first, keeping each group in its own order', () => {
    const shield = item({ key: 'shield', itemKind: 'SHIELD', equippable: true });
    const torch = item({ key: 'torch' });

    expect(equippableFirst([ROPE, LONGSWORD, torch, shield]).map((entry) => entry.key)).toEqual(['longsword', 'shield', 'rope', 'torch']);
  });
});

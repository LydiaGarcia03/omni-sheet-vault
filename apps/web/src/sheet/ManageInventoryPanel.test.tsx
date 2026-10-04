import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CatalogueEntry } from '../catalogue/api';
import type { Item } from './api';
import { ManageInventoryPanel } from './ManageInventoryPanel';

const catalogue = [
  { id: 'c1', name: 'Dagger', sourceBook: "Player's Handbook", data: { itemKind: 'WEAPON', typeLabel: 'Melee Weapon', rarity: 'none' } },
  { id: 'c2', name: 'Potion of Healing', sourceBook: "Dungeon Master's Guide", data: { itemKind: 'GEAR', typeLabel: 'Potion', rarity: 'common' } },
  { id: 'c3', name: 'Bedroll', sourceBook: "Player's Handbook", data: { itemKind: 'GEAR', typeLabel: 'Adventuring Gear', rarity: 'none' } },
] as unknown as CatalogueEntry[];

const items = [
  { key: 'i1', name: 'Chain Mail', quantity: 1, equipped: true, storageLocation: 'EQUIPMENT', weightKg: 25, rarity: null },
  { key: 'i2', name: 'Rations', quantity: 5, equipped: false, storageLocation: 'BACKPACK', weightKg: 1, rarity: null },
] as unknown as Item[];

function renderPanel(overrides: Partial<Parameters<typeof ManageInventoryPanel>[0]> = {}) {
  const props = {
    kind: 'manageInventory' as const,
    items,
    catalogue,
    onAdd: vi.fn(),
    onAddFromCatalogue: vi.fn(),
    onRemove: vi.fn(),
    onMove: vi.fn(),
    onToggleEquip: vi.fn(),
    onOpenItem: vi.fn(),
    sections: [
      { location: 'EQUIPMENT', itemCount: 1, weightKg: 25, capacityKg: null },
      { location: 'BACKPACK', itemCount: 1, weightKg: 5, capacityKg: 13.6 },
    ],
    ...overrides,
  };
  render(<ManageInventoryPanel {...props} />);
  return props;
}

describe('ManageInventoryPanel', () => {
  it('filters the catalogue by type chip and adds a match', () => {
    const props = renderPanel();

    fireEvent.click(screen.getByRole('button', { name: 'Potion' }));
    expect(screen.queryByText('Dagger')).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: /Add Potion of Healing/ }));

    expect(props.onAddFromCatalogue).toHaveBeenCalledWith('c2');
  });

  it('puts gear that fits no type under Other Gear', () => {
    renderPanel();

    fireEvent.click(screen.getByRole('button', { name: 'Other Gear' }));

    expect(screen.getByText('Bedroll')).toBeTruthy();
    expect(screen.queryByText('Potion of Healing')).toBeNull();
  });

  it('groups the inventory by location with its weight, and opens or equips an item', () => {
    const props = renderPanel();

    expect(screen.getByRole('button', { name: /Backpack \(1\)/ }).textContent).toContain('5.0 kg');
    fireEvent.click(screen.getByRole('button', { name: /Equipment \(1\)/ }));
    fireEvent.click(screen.getByRole('button', { name: 'Chain Mail' }));
    fireEvent.click(screen.getByRole('button', { name: 'Chain Mail equipped: yes' }));

    expect(props.onOpenItem).toHaveBeenCalledWith(items[0]);
    expect(props.onToggleEquip).toHaveBeenCalledWith('i1');
  });
});

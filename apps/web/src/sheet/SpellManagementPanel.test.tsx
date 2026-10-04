import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CatalogueEntry } from '../catalogue/api';
import type { Spell, SpellcastingClassInfo } from './api';
import { SpellManagementPanel } from './SpellManagementPanel';

const cleric = { className: 'Cleric', castingType: 'PREPARED', cantripsKnownMax: 3, spellsKnownMax: null, spellsPreparedMax: 4 } as SpellcastingClassInfo;

const catalogue = [
  { id: 'c1', slug: 'guidance', name: 'Guidance', sourceBook: "Player's Handbook", data: { level: 0 } },
  { id: 'c2', slug: 'bless', name: 'Bless', sourceBook: "Player's Handbook", data: { level: 1 } },
  { id: 'c3', slug: 'aid', name: 'Aid', sourceBook: "Player's Handbook", data: { level: 2 } },
] as unknown as CatalogueEntry[];

const spells = [
  { key: 'guidance', name: 'Guidance', level: 0, className: 'Cleric', prepared: false, alwaysPrepared: false },
  { key: 'bless', name: 'Bless', level: 1, className: 'Cleric', prepared: false, alwaysPrepared: false },
] as unknown as Spell[];

function renderPanel() {
  const props = {
    kind: 'spellManagement' as const,
    classes: [cleric],
    knownSpells: spells,
    catalogue,
    slots: [{ level: 1, maxSlots: 4, usedSlots: 1 }],
    onLearn: vi.fn(),
    onRemove: vi.fn(),
    onPrepare: vi.fn(),
    onUnprepare: vi.fn(),
  };
  render(<SpellManagementPanel {...props} />);
  return props;
}

describe('SpellManagementPanel', () => {
  it('summarizes the slots left per level', () => {
    renderPanel();

    expect(screen.getByRole('button', { name: /Spell Slots/ }).textContent).toContain('1st3');
  });

  it('learns or deletes from the class list, filtered by level', () => {
    const props = renderPanel();
    fireEvent.click(screen.getByRole('button', { name: 'Known Spells' }));

    fireEvent.click(screen.getByRole('button', { name: '2nd level' }));
    expect(screen.queryByText('Guidance')).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: /Learn Aid/ }));

    expect(props.onLearn).toHaveBeenCalledWith('c3', 'Cleric');
  });

  it('prepares a known spell from the Prepared Spells section', () => {
    const props = renderPanel();
    fireEvent.click(screen.getByRole('button', { name: 'Prepared Spells (1)' }));

    fireEvent.click(screen.getByRole('button', { name: 'Prepare Bless' }));

    expect(props.onPrepare).toHaveBeenCalledWith('bless');
  });
});

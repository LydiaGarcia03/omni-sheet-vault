import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { AttackRow as AttackRowData } from '../../sheet/api';
import { AttackRow } from './AttackRow';

const SPEAR: AttackRowData = {
  name: 'Spear',
  range: '5 ft. Reach',
  toHit: { value: 4, contributions: [] },
  damageDiceCount: 1,
  damageDiceSides: 6,
  damageModifier: 1,
  damageType: 'piercing',
  category: 'Melee Weapon',
  notes: '',
  actionType: 'ACTION',
  versatileDiceCount: 1,
  versatileDiceSides: 8,
};

function renderRow(attack: AttackRowData, onRollDamage = vi.fn(), onRollVersatileDamage = vi.fn()) {
  render(
    <AttackRow
      attack={attack}
      onRollHit={vi.fn()}
      onRollHitContextMenu={vi.fn()}
      onRollDamage={onRollDamage}
      onRollVersatileDamage={onRollVersatileDamage}
      onOpenDetail={vi.fn()}
    />,
  );
}

describe('AttackRow', () => {
  it('stacks the two-handed damage under the one-handed for a versatile weapon, each rolling its own', () => {
    const onRollDamage = vi.fn();
    const onRollVersatileDamage = vi.fn();
    renderRow(SPEAR, onRollDamage, onRollVersatileDamage);

    fireEvent.click(screen.getByRole('button', { name: 'Roll Spear two-handed damage, 1d8+1 piercing' }));
    expect(onRollVersatileDamage).toHaveBeenCalledOnce();
    expect(onRollDamage).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: 'Roll Spear damage, 1d6+1 piercing' }));
    expect(onRollDamage).toHaveBeenCalledOnce();
  });

  it('has no two-handed button without versatile dice', () => {
    renderRow({ ...SPEAR, versatileDiceCount: null, versatileDiceSides: null });

    expect(screen.queryByRole('button', { name: /two-handed/ })).not.toBeInTheDocument();
  });
});

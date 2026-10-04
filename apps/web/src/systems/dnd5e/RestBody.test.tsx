import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterSheet, HitDice } from '../../sheet/api';
import { LongRestBody, restRecovery, ShortRestBody } from './RestBody';

function hitDice(d8Used: number, d6Used: number, longRestRecoveryMax = 3): HitDice {
  return {
    dieSize: 8,
    max: 7,
    used: d8Used + d6Used,
    pools: [
      { dieSize: 8, max: 4, used: d8Used, classNames: ['Rogue'] },
      { dieSize: 6, max: 3, used: d6Used, classNames: ['Sorcerer'] },
    ],
    longRestRecoveryMax,
  };
}

describe('ShortRestBody', () => {
  it('heads each pool with its class, die and total', () => {
    render(<ShortRestBody hitDice={hitDice(0, 0)} conModifier={2} recover={['Channel Divinity']} onConfirm={vi.fn()} />);

    expect(screen.getByRole('heading', { name: 'Rogue(Hit Die: 1d8+2 • Total: 4)' })).toBeTruthy();
    expect(screen.getByText('Channel Divinity')).toBeTruthy();
  });

  it('spends the dice marked in each pool, never the spent ones', () => {
    const onConfirm = vi.fn();
    render(<ShortRestBody hitDice={hitDice(1, 0)} conModifier={0} recover={[]} onConfirm={onConfirm} />);

    expect(screen.getByRole('button', { name: 'd8 die 1' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Spend 2 d8' }));
    fireEvent.click(screen.getByRole('button', { name: 'Spend 1 d6' }));
    fireEvent.click(screen.getByRole('button', { name: 'Take Short Rest' }));

    expect(onConfirm).toHaveBeenCalledWith({ 8: 2, 6: 1 });
  });

  it('clears the marks on Reset', () => {
    const onConfirm = vi.fn();
    render(<ShortRestBody hitDice={hitDice(0, 0)} conModifier={0} recover={[]} onConfirm={onConfirm} />);

    fireEvent.click(screen.getByRole('button', { name: 'Spend 3 d8' }));
    fireEvent.click(screen.getByRole('button', { name: 'Reset' }));
    fireEvent.click(screen.getByRole('button', { name: 'Take Short Rest' }));

    expect(onConfirm).toHaveBeenCalledWith({});
  });
});

describe('LongRestBody', () => {
  it('asks nothing when there is no choice to make', () => {
    const onConfirm = vi.fn();
    render(<LongRestBody hitDice={hitDice(2, 0)} conModifier={0} recover={[]} onConfirm={onConfirm} />);

    expect(screen.queryByRole('button', { name: /Recover/ })).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: 'Take Long Rest' }));
    expect(onConfirm).toHaveBeenCalledWith(undefined);
  });

  it('lets the player choose which spent dice come back, within the limit', () => {
    const onConfirm = vi.fn();
    render(<LongRestBody hitDice={hitDice(2, 3)} conModifier={0} recover={[]} onConfirm={onConfirm} />);

    fireEvent.click(screen.getByRole('button', { name: 'Recover 1 d8' }));
    fireEvent.click(screen.getByRole('button', { name: 'Recover 1 d8' }));
    fireEvent.click(screen.getByRole('button', { name: 'Recover 3 d6' }));
    fireEvent.click(screen.getByRole('button', { name: 'Take Long Rest' }));

    expect(onConfirm).toHaveBeenCalledWith({ 8: 1, 6: 2 });
  });
});

describe('restRecovery', () => {
  const sheet = {
    hitDice: hitDice(0, 0),
    spellSlots: [
      { level: 1, maxSlots: 4, usedSlots: 0 },
      { level: 2, maxSlots: 1, usedSlots: 0 },
    ],
    featureTraits: [{ name: 'Second Wind', maxUses: 1, rechargeTrigger: 'SHORT_OR_LONG_REST' }],
    featureActions: [
      { name: 'Second Wind', maxUses: 1, rechargeTrigger: 'SHORT_OR_LONG_REST' },
      { name: 'Lay on Hands', maxUses: 5, rechargeTrigger: 'LONG_REST' },
    ],
  } as unknown as CharacterSheet;

  it('lists what each rest brings back', () => {
    expect(restRecovery(sheet, 'short')).toEqual(['Second Wind']);
    expect(restRecovery(sheet, 'long')).toEqual(['Up to 3 Hit Dice', '5 Spell Slots', 'Second Wind', 'Lay on Hands']);
  });
});

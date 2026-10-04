import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { HitPoints } from './HitPoints';

function renderBox() {
  const handlers = { onDamage: vi.fn(), onHeal: vi.fn(), onSetTemporary: vi.fn(), onOpenManagement: vi.fn() };
  render(<HitPoints current={48} max={48} temporary={0} {...handlers} />);
  return handlers;
}

describe('HitPoints', () => {
  it('applies damage and healing from the box without opening HP management', () => {
    const { onDamage, onHeal, onOpenManagement } = renderBox();
    const amount = screen.getByLabelText('Heal or damage amount');

    fireEvent.change(amount, { target: { value: '3' } });
    fireEvent.click(screen.getByRole('button', { name: 'Damage' }));
    fireEvent.change(amount, { target: { value: '2' } });
    fireEvent.click(screen.getByRole('button', { name: 'Heal' }));

    expect(onDamage).toHaveBeenCalledWith(3);
    expect(onHeal).toHaveBeenCalledWith(2);
    expect(onOpenManagement).not.toHaveBeenCalled();
  });

  it('keeps clicks on the amount controls inside, and opens HP management from the rest of the box', () => {
    const { onOpenManagement } = renderBox();

    fireEvent.click(screen.getByLabelText('Heal or damage amount'));
    expect(onOpenManagement).not.toHaveBeenCalled();

    fireEvent.click(screen.getByText('Hit Points'));
    expect(onOpenManagement).toHaveBeenCalledOnce();
  });
});

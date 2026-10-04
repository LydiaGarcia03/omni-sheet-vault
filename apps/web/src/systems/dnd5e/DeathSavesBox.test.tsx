import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { DeathSavesBox } from './DeathSavesBox';

const DYING = { successes: 1, failures: 2, dying: true, stable: false, dead: false };

function renderBox(saves = DYING) {
  const handlers = { onSet: vi.fn(), onRoll: vi.fn(), onOpenManagement: vi.fn() };
  render(<DeathSavesBox saves={saves} {...handlers} />);
  return handlers;
}

describe('DeathSavesBox', () => {
  it('fills up to the clicked circle', () => {
    const { onSet, onOpenManagement } = renderBox();

    fireEvent.click(screen.getByRole('button', { name: 'Success 3' }));

    expect(onSet).toHaveBeenCalledWith(3, 2);
    expect(onOpenManagement).not.toHaveBeenCalled();
  });

  it('empties the last filled circle when it is clicked', () => {
    const { onSet } = renderBox();

    fireEvent.click(screen.getByRole('button', { name: 'Failure 2, marked' }));

    expect(onSet).toHaveBeenCalledWith(1, 1);
  });

  it('rolls from the skull while the character is dying', () => {
    const { onRoll } = renderBox();

    fireEvent.click(screen.getByRole('button', { name: 'Roll a death saving throw' }));

    expect(onRoll).toHaveBeenCalledOnce();
  });

  it('has nothing to roll once the character is stable', () => {
    renderBox({ successes: 3, failures: 0, dying: true, stable: true, dead: false });

    expect(screen.getByRole('button', { name: 'Roll a death saving throw' })).toBeDisabled();
  });
});

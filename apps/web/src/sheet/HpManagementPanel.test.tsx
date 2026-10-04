import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { HpManagementRequest } from './api';
import { HpManagementPanel } from './HpManagementPanel';

function request(overrides: Partial<HpManagementRequest> = {}): HpManagementRequest {
  return {
    kind: 'hpManagement',
    current: 13,
    max: 13,
    temporary: 0,
    calculatedMax: 13,
    maxModifier: null,
    maxOverride: null,
    maxContributions: [],
    onDamage: vi.fn(),
    onHeal: vi.fn(),
    onSetTemporary: vi.fn(),
    onSetMaxModifier: vi.fn(),
    onSetMaxOverride: vi.fn(),
    ...overrides,
  };
}

describe('HpManagementPanel', () => {
  it('shows the calculated maximum beside a customized one', () => {
    render(<HpManagementPanel {...request({ current: 11, max: 16, maxModifier: 3 })} />);

    expect(screen.getByText('16')).toHaveClass('hp-management__number--positive');
    expect(screen.getByText('(13)')).toBeInTheDocument();
    expect(screen.getByLabelText('Max HP Modifier')).toHaveValue(3);
  });

  it('saves the Max HP Modifier and clears the Override Max HP when emptied', () => {
    const onSetMaxModifier = vi.fn();
    const onSetMaxOverride = vi.fn();
    render(<HpManagementPanel {...request({ max: 10, maxOverride: 10, onSetMaxModifier, onSetMaxOverride })} />);

    fireEvent.change(screen.getByLabelText('Max HP Modifier'), { target: { value: '3' } });
    fireEvent.blur(screen.getByLabelText('Max HP Modifier'));
    fireEvent.change(screen.getByLabelText('Override Max HP'), { target: { value: '' } });
    fireEvent.blur(screen.getByLabelText('Override Max HP'));

    expect(onSetMaxModifier).toHaveBeenCalledWith(3);
    expect(onSetMaxOverride).toHaveBeenCalledWith(null);
  });

  it('steps healing and damage up with +/− and applies them only through Apply Changes', () => {
    const onHeal = vi.fn();
    const onDamage = vi.fn();
    render(<HpManagementPanel {...request({ current: 8, onHeal, onDamage })} />);

    expect(screen.queryByRole('button', { name: 'Apply Changes' })).not.toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Healing amount'), { target: { value: '4' } });
    fireEvent.click(screen.getByRole('button', { name: 'Increase Hit Points' }));
    fireEvent.click(screen.getByRole('button', { name: 'Decrease Hit Points' }));
    expect(screen.getByLabelText('Healing amount')).toHaveValue(5);
    expect(screen.getByText('12')).toHaveClass('hp-management__new-hp-value');
    expect(onHeal).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: 'Apply Changes' }));

    expect(onDamage).toHaveBeenCalledWith(1);
    expect(onHeal).toHaveBeenCalledWith(5);
    expect(screen.queryByRole('button', { name: 'Apply Changes' })).not.toBeInTheDocument();
  });

  it('cancels pending healing and damage', () => {
    const onHeal = vi.fn();
    render(<HpManagementPanel {...request({ onHeal })} />);

    fireEvent.click(screen.getByRole('button', { name: 'Increase Hit Points' }));
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(screen.getByLabelText('Healing amount')).toHaveValue(null);
    expect(onHeal).not.toHaveBeenCalled();
  });

  it('ends with the death saving throw rules only while dying', () => {
    const { rerender } = render(<HpManagementPanel {...request({ deathSavesRulesText: 'Death rules.' })} />);
    expect(screen.queryByText('Death rules.')).not.toBeInTheDocument();

    rerender(<HpManagementPanel {...request({ current: 0, dying: true, deathSavesRulesText: 'Death rules.' })} />);

    expect(screen.getByRole('heading', { name: 'Death Saving Throws Rules' })).toBeInTheDocument();
    expect(screen.getByText('Death rules.')).toBeInTheDocument();
  });
});

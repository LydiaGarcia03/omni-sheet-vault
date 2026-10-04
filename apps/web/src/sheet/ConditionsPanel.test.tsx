import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { ConditionsRequest } from './api';
import { ConditionsPanel } from './ConditionsPanel';

function request(overrides: Partial<ConditionsRequest> = {}): ConditionsRequest {
  return {
    kind: 'conditions',
    title: 'Conditions',
    conditions: [
      { key: 'blinded', label: 'Blinded', active: false, rulesText: 'Blinded rules.' },
      { key: 'charmed', label: 'Charmed', active: false, rulesText: '' },
    ],
    onToggle: vi.fn(),
    exhaustionLevel: 0,
    exhaustionRulesText: 'Exhaustion rules.',
    onExhaustionChange: vi.fn(),
    activeEffects: [],
    onEndEffect: vi.fn(),
    ...overrides,
  };
}

describe('ConditionsPanel', () => {
  it('shows a condition’s rules text from its chevron, and no chevron without text', () => {
    render(<ConditionsPanel {...request()} />);

    expect(screen.queryByText('Blinded rules.')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Show Blinded rules' }));
    expect(screen.getByText('Blinded rules.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Show Charmed rules' })).not.toBeInTheDocument();
  });

  it('shows the exhaustion rules text from its chevron', () => {
    render(<ConditionsPanel {...request()} />);

    fireEvent.click(screen.getByRole('button', { name: 'Show Exhaustion rules' }));

    expect(screen.getByText('Exhaustion rules.')).toBeInTheDocument();
  });

  it('toggles a condition from its switch, which shows its state', () => {
    const onToggle = vi.fn();
    render(<ConditionsPanel {...request({ onToggle, conditions: [{ key: 'blinded', label: 'Blinded', active: true, rulesText: '' }] })} />);

    fireEvent.click(screen.getByRole('button', { name: 'Blinded: active' }));

    expect(screen.getByRole('button', { name: 'Blinded: active' })).toHaveAttribute('aria-pressed', 'true');
    expect(onToggle).toHaveBeenCalledWith('blinded');
  });

  it('sets exhaustion from its level bar and reads the level beside the name', () => {
    const onExhaustionChange = vi.fn();
    render(<ConditionsPanel {...request({ exhaustionLevel: 2, onExhaustionChange })} />);

    fireEvent.click(screen.getByRole('button', { name: 'Exhaustion level 4' }));
    fireEvent.click(screen.getByRole('button', { name: 'No exhaustion' }));

    expect(screen.getByText('Level 2')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Exhaustion level 2' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('button', { name: 'Exhaustion level 1' })).toHaveClass('exhaustion-levels__level--implied');
    expect(onExhaustionChange).toHaveBeenNthCalledWith(1, 4);
    expect(onExhaustionChange).toHaveBeenNthCalledWith(2, 0);
  });

  it('reads "Level --" without exhaustion', () => {
    render(<ConditionsPanel {...request()} />);

    expect(screen.getByText('Level --')).toBeInTheDocument();
  });
});

import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ProgressionPanel } from './ProgressionPanel';
import type { ProgressionTable } from './draftApi';

const FIGHTER: ProgressionTable = {
  key: 'fighter',
  name: 'Fighter (Battle Master)',
  currentLevel: 3,
  columns: ['Level', 'Proficiency Bonus', 'Features'],
  rows: [
    ['1st', '+2', 'Fighting Style, Second Wind'],
    ['2nd', '+2', 'Action Surge'],
    ['3rd', '+2', 'Martial Archetype, Combat Superiority'],
    ['4th', '+2', 'Ability Score Improvement'],
  ],
};

describe('ProgressionPanel', () => {
  it('starts collapsed, titled with the class', () => {
    const { container } = render(<ProgressionPanel table={FIGHTER} />);

    expect(container.querySelector('details')).not.toHaveAttribute('open');
    expect(screen.getByText('Class progression · Fighter (Battle Master)')).toBeInTheDocument();
  });

  it('marks the current level and previews the ones after it', () => {
    const { container } = render(<ProgressionPanel table={FIGHTER} />);
    fireEvent.click(screen.getByText('Class progression · Fighter (Battle Master)'));

    const current = container.querySelector('tr[aria-current="true"]');
    expect(current?.textContent).toBe('3rd+2Martial Archetype, Combat Superiority');
    expect(container.querySelectorAll('tr.is-reached')).toHaveLength(2);
    expect(screen.getByRole('columnheader', { name: 'Features' })).toBeInTheDocument();
  });
});

import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { Experience } from './api';
import { ManageExperiencePanel } from './ManageExperiencePanel';

const THRESHOLDS = [0, 300, 900, 2_700, 6_500, 14_000, 23_000, 34_000, 48_000, 64_000,
  85_000, 100_000, 120_000, 140_000, 165_000, 195_000, 225_000, 265_000, 305_000, 355_000];

const LEVEL_6: Experience = {
  advancement: 'XP',
  points: 14_000,
  level: 6,
  levelFromPoints: 6,
  currentLevelAt: 14_000,
  nextLevelAt: 23_000,
  levelUpAvailable: false,
  canLevelUp: true,
  thresholds: THRESHOLDS,
  classes: [],
};

describe('ManageExperiencePanel', () => {
  it('adds XP from where the level starts, reaching the next level', () => {
    const onApply = vi.fn();
    render(<ManageExperiencePanel kind="manageExperience" experience={LEVEL_6} onApply={onApply} />);

    expect(screen.getByText('Current XP Total: 14,000 (Level 6)')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('XP to add'), { target: { value: '9000' } });
    expect(screen.getByText('New XP Total: 23,000 (Level 7)')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Apply' }));

    expect(onApply).toHaveBeenCalledWith(23_000);
  });

  it('never previews fewer points than the current level starts at', () => {
    render(<ManageExperiencePanel kind="manageExperience" experience={{ ...LEVEL_6, points: 15_000 }} onApply={vi.fn()} />);

    fireEvent.click(screen.getByRole('tab', { name: 'Remove XP' }));
    fireEvent.change(screen.getByLabelText('XP to remove'), { target: { value: '5000' } });

    expect(screen.getByText('New XP Total: 14,000 (Level 6)')).toBeInTheDocument();
  });

  it('offers no level below the current one', () => {
    render(<ManageExperiencePanel kind="manageExperience" experience={LEVEL_6} onApply={vi.fn()} />);

    expect(screen.getByRole('option', { name: '5' })).toBeDisabled();
    expect(screen.getByRole('option', { name: '6' })).toBeEnabled();
  });
});

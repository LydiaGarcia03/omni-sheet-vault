import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { CharacterMenuRequest, Experience } from './api';
import { CharacterMenuPanel } from './CharacterMenuPanel';
import { levelForPoints, levelProgress } from './XpBar';

const THRESHOLDS = [0, 300, 900, 2700, 6500, 14000, 23000, 34000];

function experience(overrides: Partial<Experience> = {}): Experience {
  return {
    advancement: 'XP',
    points: 30000,
    level: 7,
    levelFromPoints: 7,
    currentLevelAt: 23000,
    nextLevelAt: 34000,
    levelUpAvailable: false,
    canLevelUp: true,
    thresholds: THRESHOLDS,
    classes: [{ name: 'Rogue', subclass: 'Thief', level: 7 }],
    ...overrides,
  };
}

function renderMenu(overrides: Partial<CharacterMenuRequest> = {}) {
  const props: CharacterMenuRequest = {
    kind: 'characterMenu',
    name: 'Vex',
    portrait: null,
    subtitle: 'Changeling',
    experience: experience(),
    onManageExperience: vi.fn(),
    onLevelUp: vi.fn(),
    onOpenLog: vi.fn(),
    onOpenShortRest: vi.fn(),
    onOpenLongRest: vi.fn(),
    icons: { shortRest: null, longRest: null, gameLog: null },
    ...overrides,
  };
  render(<CharacterMenuPanel {...props} />);
  return props;
}

describe('CharacterMenuPanel', () => {
  it('offers Manage experience, and no Level up yet, to an experience build', () => {
    renderMenu();

    expect(screen.getByRole('button', { name: /Manage experience/ })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Level up/ })).toBeNull();
    expect(screen.getByText('30,000 / 34,000 XP')).toBeInTheDocument();
  });

  it('shows Level up once the points reach the next level', () => {
    const props = renderMenu({ experience: experience({ points: 34000, levelUpAvailable: true }) });

    fireEvent.click(screen.getByRole('button', { name: /Level up/ }));

    expect(props.onLevelUp).toHaveBeenCalledOnce();
  });

  it('gives a milestone build a Milestone reached switch instead of experience controls, and no Level up until it is on', () => {
    const onSetExperience = vi.fn();
    renderMenu({ experience: experience({ advancement: 'MILESTONE', points: 23000 }), onSetExperience });

    expect(screen.queryByRole('button', { name: /Manage experience/ })).toBeNull();
    expect(screen.queryByRole('button', { name: /Level up/ })).toBeNull();
    const milestone = screen.getByRole('switch', { name: 'Milestone reached' });
    expect(milestone).toHaveAttribute('aria-checked', 'false');

    fireEvent.click(milestone);

    expect(onSetExperience).toHaveBeenCalledWith(34000);
  });

  it('shows Level up once the milestone is marked, and unmarking returns to the level start', () => {
    const onSetExperience = vi.fn();
    const props = renderMenu({ experience: experience({ advancement: 'MILESTONE', points: 34000, levelUpAvailable: true }), onSetExperience });

    fireEvent.click(screen.getByRole('button', { name: /Level up/ }));
    fireEvent.click(screen.getByRole('switch', { name: 'Milestone reached' }));

    expect(props.onLevelUp).toHaveBeenCalledOnce();
    expect(onSetExperience).toHaveBeenCalledWith(23000);
  });

  it('renames from the pencil when the field loses focus, and only with a changed, non-blank name', () => {
    const onRename = vi.fn();
    renderMenu({ onRename });

    fireEvent.click(screen.getByRole('button', { name: 'Rename Vex' }));
    const field = screen.getByLabelText('Character name');
    fireEvent.change(field, { target: { value: '  Vex the Bold ' } });
    fireEvent.keyDown(field, { key: 'Enter' });
    expect(onRename).not.toHaveBeenCalled();
    fireEvent.blur(field);

    expect(onRename).toHaveBeenCalledWith('Vex the Bold');
  });

  it('keeps the name when the field is left blank or the edit is cancelled', () => {
    const onRename = vi.fn();
    renderMenu({ onRename });

    fireEvent.click(screen.getByRole('heading', { name: 'Vex' }));
    fireEvent.change(screen.getByLabelText('Character name'), { target: { value: ' ' } });
    fireEvent.blur(screen.getByLabelText('Character name'));
    fireEvent.click(screen.getByRole('heading', { name: 'Vex' }));
    fireEvent.change(screen.getByLabelText('Character name'), { target: { value: 'Other' } });
    fireEvent.keyDown(screen.getByLabelText('Character name'), { key: 'Escape' });

    expect(onRename).not.toHaveBeenCalled();
    expect(screen.getByRole('heading', { name: 'Vex' })).toBeInTheDocument();
  });

  it('lists each class with its level and subclass', () => {
    renderMenu();

    expect(screen.getByText('Rogue')).toBeInTheDocument();
    expect(screen.getByText('Thief')).toBeInTheDocument();
  });

  it('draws the system emblem beside each class row', () => {
    const classIcon = vi.fn((className: string) => <img data-testid={`icon-${className}`} alt="" />);
    renderMenu({ classIcon });

    expect(classIcon).toHaveBeenCalledWith('Rogue');
    expect(screen.getByTestId('icon-Rogue')).toBeInTheDocument();
  });
});

describe('experience bar helpers', () => {
  it('reads the level a total reaches from the thresholds', () => {
    expect(levelForPoints(0, THRESHOLDS)).toBe(1);
    expect(levelForPoints(33999, THRESHOLDS)).toBe(7);
    expect(levelForPoints(34000, THRESHOLDS)).toBe(8);
  });

  it('measures progress within the current level, empty below it and full at the top', () => {
    expect(levelProgress(experience({ points: 28500 }))).toBe(0.5);
    expect(levelProgress(experience({ points: 0 }))).toBe(0);
    expect(levelProgress(experience({ nextLevelAt: null }))).toBe(1);
  });
});

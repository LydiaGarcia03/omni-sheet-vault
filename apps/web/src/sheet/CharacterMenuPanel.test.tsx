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

  it('always offers Level up to a milestone build, with no experience controls', () => {
    renderMenu({ experience: experience({ advancement: 'MILESTONE' }) });

    expect(screen.getByRole('button', { name: /Level up/ })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Manage experience/ })).toBeNull();
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

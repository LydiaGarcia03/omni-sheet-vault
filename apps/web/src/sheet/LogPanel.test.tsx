import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { RollResult } from '../dice/api';
import { describeRoll, LogPanel, timeAgo } from './LogPanel';

const now = Date.now();
const rolls: RollResult[] = [
  { id: 'b', expression: '1d8+3', context: 'Longsword: damage', results: [5], total: 8, rolledAt: new Date(now).toISOString() },
  { id: 'a', expression: '1d20+1', context: 'Acrobatics: check (disadvantage)', results: [3], total: 4, rolledAt: new Date(now - 120000).toISOString() },
];

describe('LogPanel', () => {
  it('names the action and roll type the way D&D Beyond does', () => {
    expect(describeRoll('Longsword: attack roll')).toEqual({ action: 'Longsword', type: 'to hit', tone: 'to-hit' });
    expect(describeRoll('Dexterity: saving throw (disadvantage)')).toEqual({ action: 'Dexterity', type: 'save (disadvantage)', tone: 'save' });
    expect(describeRoll('Strength (4d6 drop lowest)')).toEqual({ action: 'Strength (4d6 drop lowest)', type: null, tone: 'roll' });
  });

  it('stamps entries relative to now', () => {
    expect(timeAgo(new Date(now - 5000).toISOString(), now)).toBe('now');
    expect(timeAgo(new Date(now - 30000).toISOString(), now)).toBe('< 1 min ago');
    expect(timeAgo(new Date(now - 120000).toISOString(), now)).toBe('2 mins ago');
  });

  it('expands the newest roll only, and expands another on click', () => {
    render(<LogPanel kind="log" entries={rolls} senderName="Mez" />);

    expect(screen.getByText('5 + 3')).toBeTruthy();
    expect(screen.queryByText('1d20+1')).toBeNull();
    expect(screen.getAllByText('Mez')).toHaveLength(2);

    fireEvent.click(screen.getAllByRole('button', { expanded: false })[0]);

    expect(screen.getByText('1d20+1')).toBeTruthy();
  });
});

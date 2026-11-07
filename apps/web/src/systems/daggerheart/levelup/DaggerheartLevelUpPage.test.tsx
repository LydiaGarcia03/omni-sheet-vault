import { act, fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const api = vi.hoisted(() => ({ apiFetch: vi.fn() }));
vi.mock('../../../api/client', () => api);
vi.mock('../../../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));

import { DaggerheartLevelUpPage } from './DaggerheartLevelUpPage';

const summary = (level: number, hitPointSlots: number) => ({
  level, proficiency: level, evasion: 12, armorScore: 3, majorThreshold: 6 + level, severeThreshold: 13 + level, hitPointSlots, stressSlots: 6,
  traits: { agility: 2, strength: 0, finesse: 1, instinct: 1, presence: 0, knowledge: -1 },
  experiences: [{ name: 'Tracker', bonus: 2 }], domainCards: ['Bone Card'],
});

const option = (id: string, label: string, slots: number, takesBothPicks = false) => ({ id, label, slots, marked: 0, picked: 0, takesBothPicks, crossedOut: false });

const view = {
  level: 1, nextLevel: 2, tier: 2, atMaxLevel: false, draft: {},
  achievement: { experience: true, proficiency: true, clearsTraitMarks: false },
  grid: [
    { tier: 2, open: true, options: [option('traits', 'Gain a +1 bonus to two unmarked character traits and mark them', 3), option('hitPoint', 'Permanently gain one Hit Point slot', 2)] },
    { tier: 3, open: false, options: [option('proficiency', 'Increase your Proficiency by +1', 1, true)] },
  ],
  markedTraits: [], experiences: [{ name: 'Tracker', bonus: 2 }],
  cardOptions: [{ slug: 'sage-two', name: 'Sage Two', domain: 'sage', level: 2, type: 'spell', recallCost: 1, text: 'Card text.' }],
  multiclassOptions: [], subclassNext: { card: 'specialization' },
  problems: [], missing: ['Name the new Experience.', 'Choose 2 more advancements.', 'Take the new domain card.'],
  before: summary(1, 6), after: summary(2, 6), newFeatures: [],
};

describe('DaggerheartLevelUpPage', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    api.apiFetch.mockReset();
    api.apiFetch.mockImplementation(async (_path: string, init?: RequestInit) =>
      new Response(JSON.stringify(init?.method === 'PUT' ? { ...view, draft: JSON.parse(String(init.body)) } : view), { status: 200 }));
  });

  afterEach(() => vi.useRealTimers());

  it('shows the grid and what changes, and saves an advancement taken from it', async () => {
    render(
      <MemoryRouter>
        <DaggerheartLevelUpPage characterId="c1" characterName="Ilsa" userName="test" />
      </MemoryRouter>,
    );

    expect(await screen.findByText('Ilsa · Level 1 → 2')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Increase your Proficiency/ })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Become level 2' })).toBeDisabled();

    fireEvent.click(screen.getByRole('button', { name: /Permanently gain one Hit Point slot/ }));
    await act(async () => {
      await vi.advanceTimersByTimeAsync(400);
    });

    expect(api.apiFetch).toHaveBeenCalledWith(
      '/api/daggerheart/characters/c1/level-up',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ advancements: [{ option: 'hitPoint', tier: 2 }] }) }),
    );
    const side = screen.getByText('What changes').closest('aside') as HTMLElement;
    expect(within(side).getByText('7 → 8')).toBeInTheDocument();
  });
});

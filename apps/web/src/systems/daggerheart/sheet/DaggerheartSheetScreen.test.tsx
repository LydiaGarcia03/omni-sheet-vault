import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const api = vi.hoisted(() => ({ apiFetch: vi.fn() }));
vi.mock('../../../api/client', () => api);
vi.mock('../../../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));

import type { Character } from '../../../characters/api';
import { DaggerheartSheetScreen } from './DaggerheartSheetScreen';

const calc = (value: number, contributions = [{ source: 'Assigned', amount: value }]) => ({ value, contributions });

const sheet = {
  level: 1,
  proficiency: 1,
  identity: {
    class: { slug: 'ranger', name: 'Ranger' },
    subclass: { slug: 'wayfinder', name: 'Wayfinder', spellcastTrait: 'Agility' },
    domains: [{ slug: 'bone', name: 'Bone' }, { slug: 'sage', name: 'Sage' }],
    ancestry: { slug: 'fungril', name: 'Fungril' },
    community: { slug: 'slyborne', name: 'Slyborne' },
    transformation: null,
  },
  traits: { agility: 2, strength: 0, finesse: 1, instinct: 1, presence: 0, knowledge: -1 },
  features: [
    { source: 'hope', origin: 'Ranger', name: 'Hold Them Off', text: 'Spend 3 Hope when you succeed on an attack.' },
    { source: 'class', origin: 'Ranger', name: 'Ranger’s Focus', text: 'Spend a Hope and make an attack against a target.' },
    { source: 'ancestry', origin: 'Fungril', name: 'Fungril Network', text: 'Make an Instinct Roll (12).' },
    { source: 'community', origin: 'Slyborne', name: 'Scoundrel', text: 'Advantage on rolls to negotiate with criminals.' },
  ],
  experiences: [{ name: 'Tracker', bonus: 2 }, { name: 'Lone Wolf', bonus: 2 }],
  domainCards: [
    { slug: 'deft-maneuvers', name: 'Deft Maneuvers', domain: 'bone', level: 1, type: 'ability', recallCost: 0, text: 'Once per rest, mark a Stress to sprint.', inLoadout: true },
    { slug: 'gifted-tracker', name: 'Gifted Tracker', domain: 'sage', level: 1, type: 'ability', recallCost: 0, text: 'Spend Hope to learn about a creature.', inLoadout: true },
  ],
  weapons: {
    primary: { slug: 'shortbow', name: 'Shortbow', slot: 'primary', tier: 1, trait: 'Agility', range: 'Far', damage: { die: 'd6', bonus: 3, type: 'physical' }, burden: 2, feature: null },
    secondary: null,
  },
  armor: { slug: 'leather-armor', name: 'Leather Armor', tier: 1, baseThresholds: { major: 6, severe: 13 }, baseScore: 3, feature: null },
  inventory: [{ name: 'Torch', quantity: 1 }, { slug: 'minor-stamina-potion', name: 'Minor Stamina Potion', quantity: 1 }],
  gold: { handfuls: 1, bags: 0, chests: 0 },
  trackers: { hitPointsMarked: 0, stressMarked: 0, armorSlotsMarked: 0, hope: 2 },
  conditions: [],
  notes: '',
  build: { pronouns: 'she/her', story: { background: 'A blight-beast took my grove.' } },
  derived: {
    traits: { agility: calc(2), strength: calc(0), finesse: calc(1), instinct: calc(1), presence: calc(0), knowledge: calc(-1) },
    evasion: calc(12, [{ source: 'Ranger', amount: 12 }]),
    armorScore: calc(3, [{ source: 'Leather Armor', amount: 3 }]),
    majorThreshold: calc(7, [{ source: 'Leather Armor', amount: 6 }, { source: 'Level', amount: 1 }]),
    severeThreshold: calc(14, [{ source: 'Leather Armor', amount: 13 }, { source: 'Level', amount: 1 }]),
    hitPointSlots: calc(6, [{ source: 'Ranger', amount: 6 }]),
    stressSlots: calc(6, [{ source: 'Every character', amount: 6 }]),
    maxHope: 6,
    proficiency: 1,
    spellcastTrait: 'Agility',
    attacks: [{ name: 'Shortbow', slot: 'primary', trait: 'Agility', modifier: 2, range: 'Far', damage: '1d6+3', damageType: 'physical', burden: 2 }],
  },
};

const character: Character = { id: 'c1', name: 'Ilsa Thornwake', systemId: 'daggerheart', status: 'ACTIVE', createdAt: '2026-10-12T00:00:00Z', portrait: null, summary: [] };

function renderSheet() {
  render(
    <MemoryRouter>
      <DaggerheartSheetScreen character={character} userLabel="test" onBack={vi.fn()} />
    </MemoryRouter>,
  );
}

describe('DaggerheartSheetScreen', () => {
  beforeEach(() => {
    api.apiFetch.mockReset();
    api.apiFetch.mockImplementation(async (path: string) =>
      new Response(JSON.stringify(path.includes('/api/catalogue') ? [] : sheet), { status: 200 }));
  });

  it('shows the heritage line, the defenses, the thresholds and the loadout', async () => {
    renderSheet();

    expect(await screen.findByRole('heading', { name: 'Ilsa Thornwake' })).toBeInTheDocument();
    expect(api.apiFetch).toHaveBeenCalledWith('/api/daggerheart/characters/c1/sheet');
    expect(screen.getByText(/Slyborne Fungril Ranger/)).toHaveTextContent('Slyborne Fungril Ranger (Wayfinder) · Bone & Sage');
    expect(screen.getByRole('button', { name: /Damage thresholds/ })).toHaveTextContent('Minormark 1 HP7Majormark 2 HP14Severemark 3 HP');
    expect(screen.getByText('None — the Shortbow needs both hands')).toBeInTheDocument();
    expect(screen.getByText('Loadout')).toHaveTextContent('2 / 5');
    expect(screen.getAllByText('Empty slot')).toHaveLength(3);
    expect(screen.getByText(/3 for Hold Them Off/)).toBeInTheDocument();
  });

  it('opens how a value is made up in the sidebar, and closes it', async () => {
    renderSheet();

    fireEvent.click(await screen.findByRole('button', { name: /Damage thresholds/ }));
    const side = screen.getByRole('complementary', { name: 'Thresholds 7 / 14' });
    expect(within(side).getAllByText('Leather Armor')).toHaveLength(2);
    expect(within(side).getAllByText('Total')).toHaveLength(2);

    fireEvent.click(within(side).getByRole('button', { name: 'Close' }));
    expect(screen.queryByRole('complementary', { name: 'Thresholds 7 / 14' })).not.toBeInTheDocument();
  });

  it('marks a Hit Point slot and applies damage through the actions endpoint', async () => {
    api.apiFetch.mockImplementation(async (path: string, init?: RequestInit) => {
      if (init?.method === 'POST') {
        return new Response(JSON.stringify({ ...sheet, trackers: { ...sheet.trackers, hitPointsMarked: 3 } }), { status: 200 });
      }
      return new Response(JSON.stringify(path.includes('/api/catalogue') ? [] : sheet), { status: 200 });
    });
    renderSheet();

    fireEvent.click(await screen.findByRole('button', { name: 'Hit Points 3' }));
    expect(api.apiFetch).toHaveBeenLastCalledWith('/api/daggerheart/characters/c1/actions', expect.objectContaining({ body: JSON.stringify({ type: 'track', track: 'hitPoints', delta: 3 }) }));
    expect(await screen.findByText('3 / 6 marked')).toBeInTheDocument();

    fireEvent.change(screen.getByRole('textbox', { name: 'Damage taken' }), { target: { value: '9' } });
    expect(screen.getByText('Major · mark 2 HP')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Apply' }));
    expect(api.apiFetch).toHaveBeenLastCalledWith('/api/daggerheart/characters/c1/actions', expect.objectContaining({ body: JSON.stringify({ type: 'damage', amount: 9, useArmor: false }) }));
  });

  it('takes a short rest with the two downtime moves picked', async () => {
    renderSheet();

    fireEvent.click(await screen.findByRole('button', { name: 'Short rest' }));
    const side = screen.getByRole('complementary', { name: 'Short rest' });
    fireEvent.change(within(side).getAllByRole('combobox')[1], { target: { value: 'prepare' } });
    fireEvent.click(within(side).getByRole('checkbox', { name: 'With party members' }));
    fireEvent.click(within(side).getByRole('button', { name: 'Take the short rest' }));

    expect(api.apiFetch).toHaveBeenLastCalledWith(
      '/api/daggerheart/characters/c1/actions',
      expect.objectContaining({
        body: JSON.stringify({
          type: 'rest',
          kind: 'short',
          moves: [
            { move: 'tendToWounds', forAlly: false, withParty: false },
            { move: 'prepare', forAlly: false, withParty: true },
          ],
        }),
      }),
    );
  });

  it('opens a trait pool from the sheet, adds an Experience and advantage, and rolls it', async () => {
    const roll = {
      id: 'r1', kind: 'duality', label: 'Agility — Trait Roll', dice: [9, 4, 3], sides: [12, 12, 6], hope: 9, fear: 4, advantageDie: 3, modifier: 0, total: 20,
      difficulty: 0, critical: false, withHope: true, success: null, outcome: '20 with Hope', rolledAt: '2026-10-12T20:00:00Z',
    };
    api.apiFetch.mockImplementation(async (path: string) => {
      if (path.endsWith('/rolls/duality')) {
        return new Response(JSON.stringify({ roll, sheet: { ...sheet, trackers: { ...sheet.trackers, hope: 2 } } }), { status: 200 });
      }
      if (path.endsWith('/rolls')) return new Response('[]', { status: 200 });
      return new Response(JSON.stringify(path.includes('/api/catalogue') ? [] : sheet), { status: 200 });
    });
    renderSheet();

    fireEvent.click(await screen.findByRole('button', { name: 'Roll Agility' }));
    expect(screen.getByText('Trait Roll')).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: 'Modifier' })).toHaveValue('+2');
    fireEvent.click(screen.getByRole('button', { name: /^Tracker/ }));
    expect(screen.getByRole('textbox', { name: 'Modifier' })).toHaveValue('+4');
    fireEvent.click(screen.getByRole('button', { name: '+adv' }));
    expect(api.apiFetch).not.toHaveBeenCalledWith('/api/daggerheart/characters/c1/rolls/duality', expect.anything());
    fireEvent.click(screen.getByRole('button', { name: 'Roll' }));

    expect(await screen.findByText('With Hope')).toBeInTheDocument();
    expect(api.apiFetch).toHaveBeenCalledWith(
      '/api/daggerheart/characters/c1/rolls/duality',
      expect.objectContaining({
        body: JSON.stringify({ trait: 'agility', experiences: [0], advantage: 'advantage', modifier: 0, difficulty: 0, reaction: false, label: 'Agility — Trait Roll', dice: [] }),
      }),
    );
  });

  it('builds attack and damage pools from the weapon chips, and a reaction from the Duality pool', async () => {
    const roll = {
      id: 'r2', kind: 'custom', label: 'Shortbow — Damage Roll', dice: [5], sides: [6], hope: 0, fear: 0, advantageDie: 0, modifier: 3, total: 8,
      difficulty: 0, critical: false, withHope: false, success: null, outcome: '8', rolledAt: '2026-10-12T20:00:00Z',
    };
    api.apiFetch.mockImplementation(async (path: string) => {
      if (path.endsWith('/rolls/duality')) return new Response(JSON.stringify({ roll: { ...roll, id: 'r3', kind: 'duality', sides: [12, 12] }, sheet }), { status: 200 });
      if (path.endsWith('/rolls/custom')) return new Response(JSON.stringify(roll), { status: 200 });
      if (path.endsWith('/rolls')) return new Response('[]', { status: 200 });
      return new Response(JSON.stringify(path.includes('/api/catalogue') ? [] : sheet), { status: 200 });
    });
    renderSheet();

    fireEvent.click(await screen.findByRole('button', { name: /^1d6\+3damage/ }));
    expect(screen.getByText('Damage Roll')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Roll' }));
    await waitFor(() =>
      expect(api.apiFetch).toHaveBeenCalledWith(
        '/api/daggerheart/characters/c1/rolls/custom',
        expect.objectContaining({ body: JSON.stringify({ dice: [{ sides: 6, count: 1 }], bonus: 3, critical: false, label: 'Shortbow — Damage Roll' }) }),
      ),
    );

    fireEvent.click(await screen.findByRole('button', { name: 'Duality' }));
    fireEvent.click(screen.getByRole('button', { name: 'reaction' }));
    fireEvent.click(screen.getByRole('button', { name: 'Roll' }));
    await waitFor(() =>
      expect(api.apiFetch).toHaveBeenCalledWith('/api/daggerheart/characters/c1/rolls/duality', expect.objectContaining({ body: expect.stringContaining('"reaction":true') })),
    );
  });
  it('shows a card in full, the inventory and the story in their tabs', async () => {
    renderSheet();

    fireEvent.click(await screen.findByRole('button', { name: /Gifted Tracker/ }));
    expect(screen.getByRole('complementary', { name: 'Gifted Tracker' })).toHaveTextContent('Recall Cost0 Stress');

    fireEvent.click(screen.getByRole('button', { name: 'Equipment' }));
    expect(screen.getByText('Minor Stamina Potion')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Details' }));
    expect(screen.getByText('A blight-beast took my grove.')).toBeInTheDocument();
    expect(screen.getByText('she/her')).toBeInTheDocument();
  });
});

import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const api = vi.hoisted(() => ({ apiFetch: vi.fn() }));
vi.mock('../../../api/client', () => api);

import { useVtm5Dice } from './Vtm5DiceDock';

const roll = {
  id: 'r1', kind: 'pool', label: 'Dexterity + Larceny', faces: [10, 10, 3, 7], hunger: 1, successes: 5,
  difficulty: 3, margin: 2, win: true, critical: true, messy: false, bestial: false, outcome: 'Critical', rerolled: false,
  rolledAt: '2026-10-04T20:00:00Z',
};

function Harness({ onSheetChanged = () => undefined }: { onSheetChanged?: () => void }) {
  const { pool, dock } = useVtm5Dice('c1', onSheetChanged);
  return (
    <>
      <button type="button" onClick={() => { pool.toggle('Dexterity', 3); pool.toggle('Larceny', 2); }}>add traits</button>
      <button type="button" onClick={() => void pool.rollNow(['Juggling'], 'Juggle')}>bad roll</button>
      {dock(1)}
    </>
  );
}

describe('useVtm5Dice', () => {
  beforeEach(() => {
    api.apiFetch.mockReset();
    api.apiFetch.mockImplementation(async (_path: string, init?: RequestInit) =>
      new Response(JSON.stringify(init?.method === 'POST' ? roll : [roll]), { status: 200 }));
  });

  it('builds a pool from ratings, rolls it with the difficulty and shows the log', async () => {
    const onSheetChanged = vi.fn();
    render(<Harness onSheetChanged={onSheetChanged} />);

    fireEvent.click(screen.getByRole('button', { name: 'add traits' }));
    expect(screen.getByText('Dexterity')).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole('button', { name: '+' })[1]);
    fireEvent.click(screen.getByRole('button', { name: 'Roll' }));

    await waitFor(() => expect(onSheetChanged).toHaveBeenCalled());
    const post = api.apiFetch.mock.calls.find(([, init]) => init?.method === 'POST');
    expect(post?.[0]).toBe('/api/vtm5/characters/c1/rolls');
    expect(JSON.parse(post?.[1].body as string)).toEqual({ traits: ['Dexterity', 'Larceny'], bonus: 0, difficulty: 1 });
    expect(await screen.findByText(/vs 3 · margin 2/)).toBeInTheDocument();
  });

  it('shows a refused roll as an error', async () => {
    api.apiFetch.mockImplementation(async (_path: string, init?: RequestInit) =>
      init?.method === 'POST'
        ? new Response(JSON.stringify({ detail: 'Unknown trait: Juggling' }), { status: 400 })
        : new Response('[]', { status: 200 }));
    render(<Harness />);

    fireEvent.click(screen.getByRole('button', { name: 'add traits' }));
    fireEvent.click(screen.getByRole('button', { name: 'bad roll' }));

    expect(await screen.findByText('Unknown trait: Juggling')).toBeInTheDocument();
  });
});

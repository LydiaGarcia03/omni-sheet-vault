import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Character } from '../characters/api';
import { HomePage } from './HomePage';

const api = vi.hoisted(() => ({ listCharacters: vi.fn(), deleteCharacter: vi.fn() }));
vi.mock('../characters/api', () => api);
const drafts = vi.hoisted(() => ({ createDraft: vi.fn() }));
vi.mock('../builder/draftApi', () => drafts);
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));

function character(overrides: Partial<Character>): Character {
  return {
    id: 'c1',
    name: 'Vex',
    systemId: 'dnd-5e',
    status: 'ACTIVE',
    createdAt: '2026-09-23T00:00:00Z',
    portrait: null,
    summary: [
      { label: 'Classes', value: 'Rogue 4 / Sorcerer 3' },
      { label: 'Species', value: 'Changeling' },
    ],
    ...overrides,
  };
}

function renderHome(path = '/') {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/" element={<HomePage userName="Lydia" />} />
        <Route path="/characters/:id/build" element={<p>Builder page</p>} />
        <Route path="/characters/new/*" element={<p>Old system picker</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('HomePage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('lists characters as banners grouped by system, with each class level and the species', async () => {
    api.listCharacters.mockResolvedValue([
      character({}),
      character({ id: 'c2', name: 'New character', status: 'DRAFT', summary: [{ label: 'Classes', value: 'Bard 3' }] }),
    ]);
    renderHome();

    const group = await screen.findByRole('region', { name: 'D&D 5e' });
    expect(within(group).getByText('Rogue 4 / Sorcerer 3')).toBeInTheDocument();
    expect(within(group).getByText('Changeling')).toBeInTheDocument();
    expect(within(group).getByText('Draft · continue creating')).toBeInTheDocument();
    expect(within(group).getByRole('link', { name: 'Open Vex' })).toHaveAttribute('href', '/characters/c1');
    expect(within(group).getByRole('link', { name: 'Continue creating New character' })).toHaveAttribute('href', '/characters/c2/build');
  });

  it('offers only the systems that have characters as filters', async () => {
    api.listCharacters.mockResolvedValue([character({})]);
    renderHome('/?system=vampire');

    const filters = await screen.findByRole('group', { name: 'Filter by game system' });
    expect(within(filters).getAllByRole('button').map((button) => button.textContent)).toEqual(['All1', 'D&D 5e1']);
    expect(within(filters).getByRole('button', { name: 'All1' })).toHaveAttribute('aria-pressed', 'true');
  });

  it('shows every system to start from, only D&D 5e available', async () => {
    api.listCharacters.mockResolvedValue([]);
    renderHome();

    const strip = screen.getByRole('region', { name: 'Start something new' });
    expect(within(strip).getAllByRole('button', { name: 'Create character' })).toHaveLength(1);
    expect(within(strip).getAllByText('Coming soon').length).toBeGreaterThan(0);
    expect(await screen.findByText('Your vault is empty. Every hero starts somewhere.')).toBeInTheDocument();
  });

  it('creates a character from a system card and opens its builder, with no page in between', async () => {
    api.listCharacters.mockResolvedValue([]);
    drafts.createDraft.mockResolvedValue({ id: 'new-draft' });
    renderHome();

    const strip = screen.getByRole('region', { name: 'Start something new' });
    fireEvent.click(within(strip).getByRole('button', { name: 'Create character' }));

    expect(await screen.findByText('Builder page')).toBeInTheDocument();
    expect(drafts.createDraft).toHaveBeenCalledWith('New character', 'dnd-5e');
    expect(screen.queryByText('Old system picker')).not.toBeInTheDocument();
  });

  it('starts the only system from the header button too', async () => {
    api.listCharacters.mockResolvedValue([character({})]);
    drafts.createDraft.mockResolvedValue({ id: 'new-draft' });
    renderHome();

    fireEvent.click(await screen.findByRole('button', { name: '＋ Create character' }));

    expect(await screen.findByText('Builder page')).toBeInTheDocument();
    expect(drafts.createDraft).toHaveBeenCalledTimes(1);
  });

  it('asks before deleting a character', async () => {
    api.listCharacters.mockResolvedValue([character({})]);
    api.deleteCharacter.mockResolvedValue(undefined);
    renderHome();

    fireEvent.click(await screen.findByRole('button', { name: 'More actions for Vex' }));
    fireEvent.click(screen.getByRole('menuitem', { name: 'Delete' }));
    expect(api.deleteCharacter).not.toHaveBeenCalled();

    const dialog = screen.getByRole('alertdialog', { name: 'Delete character?' });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Delete' }));

    await waitFor(() => expect(api.deleteCharacter).toHaveBeenCalledWith('c1'));
  });

  it('cancelling the confirmation keeps the character', async () => {
    api.listCharacters.mockResolvedValue([character({})]);
    renderHome();

    fireEvent.click(await screen.findByRole('button', { name: 'More actions for Vex' }));
    fireEvent.click(screen.getByRole('menuitem', { name: 'Delete' }));
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    expect(api.deleteCharacter).not.toHaveBeenCalled();
  });
});

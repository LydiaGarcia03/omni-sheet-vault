import { render, screen } from '@testing-library/react';
import { StrictMode } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CreateCharacterPage } from './CreateCharacterPage';

const drafts = vi.hoisted(() => ({ createDraft: vi.fn() }));
vi.mock('./draftApi', () => drafts);
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));

function renderAt(path: string) {
  render(
    <StrictMode>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/" element={<p>Home</p>} />
          <Route path="/characters/new/:systemId" element={<CreateCharacterPage userName="Lydia" />} />
          <Route path="/characters/:id/build" element={<p>Builder page</p>} />
        </Routes>
      </MemoryRouter>
    </StrictMode>,
  );
}

describe('CreateCharacterPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('creates one draft, even under StrictMode, and goes straight to its builder', async () => {
    drafts.createDraft.mockResolvedValue({ id: 'd1' });

    renderAt('/characters/new/dnd-5e');

    expect(await screen.findByText('Builder page')).toBeInTheDocument();
    expect(drafts.createDraft).toHaveBeenCalledTimes(1);
    expect(drafts.createDraft).toHaveBeenCalledWith('New character', 'dnd-5e');
  });

  it('shows only a short creating message, never the system picker', () => {
    drafts.createDraft.mockReturnValue(new Promise(() => {}));

    renderAt('/characters/new/dnd-5e');

    expect(screen.getByRole('status')).toHaveTextContent('Creating your D&D 5e character…');
    expect(screen.queryByText('Create a character')).not.toBeInTheDocument();
  });

  it('sends an unknown system back home', () => {
    renderAt('/characters/new/not-a-system');

    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(drafts.createDraft).not.toHaveBeenCalled();
  });
});

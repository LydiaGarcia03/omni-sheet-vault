import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';

const edition = vi.hoisted(() => ({ isDesktopEdition: false }));
vi.mock('../edition', () => edition);
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));
const appTheme = vi.hoisted(() => ({ state: { theme: 'light', setTheme: (_theme: string) => {} } }));
vi.mock('../theme/appTheme', () => ({ useAppTheme: () => appTheme.state }));

import { AppHeader } from './AppHeader';

function openUserMenu() {
  render(
    <MemoryRouter>
      <AppHeader userName="Nicole" />
    </MemoryRouter>,
  );
  fireEvent.click(screen.getByRole('button', { name: /Nicole/ }));
}

describe('AppHeader', () => {
  afterEach(() => {
    edition.isDesktopEdition = false;
  });

  it('offers the account page and logging out on the server edition', () => {
    openUserMenu();

    expect(screen.getByRole('menuitem', { name: 'Account' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Log out' })).toBeInTheDocument();
  });

  it('switches the dark theme on from the user menu', () => {
    const setTheme = vi.fn();
    appTheme.state = { theme: 'light', setTheme };

    openUserMenu();
    const toggle = screen.getByRole('menuitemcheckbox', { name: 'Dark theme' });
    fireEvent.click(toggle);

    expect(toggle).toHaveAttribute('aria-checked', 'false');
    expect(setTheme).toHaveBeenCalledWith('dark');
  });

  it("shows the page's game system after the app's name", () => {
    render(
      <MemoryRouter>
        <AppHeader userName="Nicole" systemId="dnd-5e" compact />
      </MemoryRouter>,
    );

    expect(screen.getByText(/Dungeons/)).toHaveTextContent('Dungeons & Dragons');
    expect(screen.getByText('5th edition · 2014')).toBeInTheDocument();
  });

  it('shows no system without a systemId, nor for a system that has no mark', () => {
    const { container, rerender } = render(
      <MemoryRouter>
        <AppHeader userName="Nicole" />
      </MemoryRouter>,
    );
    expect(container.querySelector('.app-system')).toBeNull();

    rerender(
      <MemoryRouter>
        <AppHeader userName="Nicole" systemId="unknown-system" />
      </MemoryRouter>,
    );
    expect(container.querySelector('.app-system')).toBeNull();
  });

  it('has no account or logout in the desktop edition, where there is no login', () => {
    edition.isDesktopEdition = true;

    openUserMenu();

    expect(screen.getByRole('menuitem', { name: 'My characters' })).toBeInTheDocument();
    expect(screen.queryByRole('menuitem', { name: 'Account' })).toBeNull();
    expect(screen.queryByRole('menuitem', { name: 'Log out' })).toBeNull();
  });
});

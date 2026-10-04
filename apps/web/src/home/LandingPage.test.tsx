import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { LandingPage } from './LandingPage';

const signIn = vi.fn();
const signUp = vi.fn();
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn, signUp, signOut: vi.fn() }) }));

function renderLanding() {
  render(
    <MemoryRouter>
      <LandingPage />
    </MemoryRouter>,
  );
}

describe('LandingPage', () => {
  it('sends "Create character" through the login straight to the only system, skipping the system picker', () => {
    renderLanding();

    fireEvent.click(screen.getAllByRole('button', { name: 'Create character' })[0]);

    expect(signIn).toHaveBeenCalledWith('/characters/new/dnd-5e');
  });

  it('sends a system card straight to creating that system after the login', () => {
    renderLanding();

    const buttons = screen.getAllByRole('button', { name: 'Create character' });
    fireEvent.click(buttons[buttons.length - 1]);

    expect(signIn).toHaveBeenLastCalledWith('/characters/new/dnd-5e');
  });

  it('offers a login back to the character list', () => {
    renderLanding();

    fireEvent.click(screen.getByRole('button', { name: 'Log in' }));

    expect(signIn).toHaveBeenLastCalledWith('/');
  });

  it('sends "Create account" to the registration page, back to the character list afterwards', () => {
    renderLanding();

    fireEvent.click(screen.getByRole('button', { name: 'Create account' }));

    expect(signUp).toHaveBeenCalledWith('/');
  });

  it('shows the fan content notice and links to the credits', () => {
    renderLanding();

    expect(screen.getByText(/unofficial Fan Content permitted under the Fan Content Policy/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Credits and licenses' })).toHaveAttribute('href', '/credits');
  });
});

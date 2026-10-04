import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { CreditsPage } from './CreditsPage';

vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));

describe('CreditsPage', () => {
  it('credits the icon authors under their licenses, for visitors too', () => {
    render(
      <MemoryRouter>
        <CreditsPage userName={null} />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: 'Credits and licenses' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'CC BY 3.0' })).toBeInTheDocument();
    expect(screen.getByText(/by Lorc:/)).toBeInTheDocument();
    expect(screen.getByText(/"Fire" by meaicon and "Bullet" by Magnific/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Log in' })).toBeInTheDocument();
  });
});

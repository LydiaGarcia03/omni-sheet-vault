import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { SheetShell } from './SheetShell';

vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn() }) }));

function renderShell(reserveRight?: number) {
  render(
    <MemoryRouter>
      <SheetShell
        characterName="Mez"
        portrait={null}
        systemId="dnd-5e"
        summary="Warlock 4 / Fighter 3"
        userLabel="test"
        onOpenLog={vi.fn()}
        onOpenShortRest={vi.fn()}
        onOpenLongRest={vi.fn()}
        shortRestIcon={null}
        longRestIcon={null}
        gameLogIcon={null}
        reserveRight={reserveRight}
        overlay={<aside>Sidebar</aside>}
      >
        <p>Sheet body</p>
      </SheetShell>
    </MemoryRouter>,
  );
}

describe('SheetShell', () => {
  it('shows the summary line under the name', () => {
    renderShell();
    expect(screen.getByText('Warlock 4 / Fighter 3')).toBeInTheDocument();
  });

  it('renders the overlay outside the scaled column', () => {
    renderShell();
    const column = screen.getByText('Sheet body').closest('.sheet-column');
    expect(column).not.toBeNull();
    expect(column?.contains(screen.getByText('Sidebar'))).toBe(false);
  });

  it('keeps the reserved width free on the right', () => {
    renderShell(352);
    const page = screen.getByText('Sheet body').closest('.sheet-column')?.parentElement as HTMLElement;
    expect(page.style.padding).toBe('0px 352px 24px 24px');
  });

  it('keeps the normal gutter and no zoom without a reserve', () => {
    renderShell();
    const column = screen.getByText('Sheet body').closest('.sheet-column') as HTMLElement;
    expect((column.parentElement as HTMLElement).style.padding).toBe('0px 24px 24px');
    expect(column.style.zoom).toBe('');
  });
});

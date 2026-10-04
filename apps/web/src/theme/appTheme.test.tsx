import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AppPage } from './AppPage';
import { AppThemeProvider, useAppTheme } from './appTheme';

function mockSystemDark(dark: boolean) {
  vi.stubGlobal(
    'matchMedia',
    vi.fn().mockReturnValue({ matches: dark, addEventListener: vi.fn(), removeEventListener: vi.fn() }),
  );
}

function Toggle() {
  const { theme, setTheme } = useAppTheme();
  return (
    <button type="button" onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>
      toggle
    </button>
  );
}

function renderPage() {
  render(
    <AppThemeProvider>
      <AppPage>
        <Toggle />
      </AppPage>
    </AppThemeProvider>,
  );
  return screen.getByRole('button').closest('.app-page')!;
}

describe('app theme', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("follows the system's dark mode until the player chooses", () => {
    mockSystemDark(true);

    expect(renderPage().getAttribute('data-app-theme')).toBe('dark');
  });

  it("keeps the player's choice over the system's and remembers it", () => {
    mockSystemDark(true);
    const page = renderPage();

    fireEvent.click(screen.getByRole('button'));

    expect(page.getAttribute('data-app-theme')).toBe('light');
    expect(window.localStorage.getItem('osv.app-theme')).toBe('light');
  });

  it('starts from a remembered choice', () => {
    mockSystemDark(false);
    window.localStorage.setItem('osv.app-theme', 'dark');

    expect(renderPage().getAttribute('data-app-theme')).toBe('dark');
  });
});

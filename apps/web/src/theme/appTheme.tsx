import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';

export type AppTheme = 'light' | 'dark';

const STORAGE_KEY = 'osv.app-theme';
const DARK_QUERY = '(prefers-color-scheme: dark)';

type AppThemeState = {
  theme: AppTheme;
  setTheme: (theme: AppTheme) => void;
};

const AppThemeContext = createContext<AppThemeState | null>(null);

function storedTheme(): AppTheme | null {
  try {
    const value = window.localStorage.getItem(STORAGE_KEY);
    return value === 'light' || value === 'dark' ? value : null;
  } catch {
    return null;
  }
}

function storeTheme(theme: AppTheme) {
  try {
    window.localStorage.setItem(STORAGE_KEY, theme);
  } catch {
    // The choice then lasts for this visit only.
  }
}

function systemTheme(): AppTheme {
  return typeof window.matchMedia === 'function' && window.matchMedia(DARK_QUERY).matches ? 'dark' : 'light';
}

/** The landing and home theme: the player's choice, kept in this browser, or the system's until they choose. */
export function AppThemeProvider({ children }: { children: ReactNode }) {
  const [chosen, setChosen] = useState<AppTheme | null>(storedTheme);
  const [system, setSystem] = useState<AppTheme>(systemTheme);

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') {
      return;
    }
    const query = window.matchMedia(DARK_QUERY);
    const follow = () => setSystem(query.matches ? 'dark' : 'light');
    query.addEventListener('change', follow);
    return () => query.removeEventListener('change', follow);
  }, []);

  const setTheme = useCallback((theme: AppTheme) => {
    storeTheme(theme);
    setChosen(theme);
  }, []);

  const value = useMemo(() => ({ theme: chosen ?? system, setTheme }), [chosen, system, setTheme]);
  return <AppThemeContext.Provider value={value}>{children}</AppThemeContext.Provider>;
}

const LIGHT_ONLY: AppThemeState = { theme: 'light', setTheme: () => {} };

export function useAppTheme(): AppThemeState {
  return useContext(AppThemeContext) ?? LIGHT_ONLY;
}

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useOptionalAuth } from '../auth/AuthProvider';
import { PREFERENCE_COOKIES, savePreferences, writePreferenceCookie } from '../auth/preferences';

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
    return isAppTheme(value) ? value : null;
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

function isAppTheme(value: unknown): value is AppTheme {
  return value === 'light' || value === 'dark';
}

/**
 * The landing and home theme: the signed-in player's saved choice, else the one kept in this browser, else the
 * system's until they choose. The choice is mirrored to a cookie so the login pages match.
 */
export function AppThemeProvider({ children }: { children: ReactNode }) {
  const auth = useOptionalAuth();
  const signedIn = auth?.status === 'signed-in';
  const saved = auth?.user?.preferences?.theme;
  const [chosen, setChosen] = useState<AppTheme | null>(storedTheme);
  const [system, setSystem] = useState<AppTheme>(systemTheme);

  useEffect(() => {
    if (isAppTheme(saved)) {
      storeTheme(saved);
      setChosen(saved);
    }
  }, [saved]);

  useEffect(() => {
    writePreferenceCookie(PREFERENCE_COOKIES.theme, chosen ?? 'system');
  }, [chosen]);

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') {
      return;
    }
    const query = window.matchMedia(DARK_QUERY);
    const follow = () => setSystem(query.matches ? 'dark' : 'light');
    query.addEventListener('change', follow);
    return () => query.removeEventListener('change', follow);
  }, []);

  const setTheme = useCallback(
    (theme: AppTheme) => {
      storeTheme(theme);
      setChosen(theme);
      if (signedIn) {
        void savePreferences({ theme });
      }
    },
    [signedIn],
  );

  const value = useMemo(() => ({ theme: chosen ?? system, setTheme }), [chosen, system, setTheme]);
  return <AppThemeContext.Provider value={value}>{children}</AppThemeContext.Provider>;
}

const LIGHT_ONLY: AppThemeState = { theme: 'light', setTheme: () => {} };

export function useAppTheme(): AppThemeState {
  return useContext(AppThemeContext) ?? LIGHT_ONLY;
}

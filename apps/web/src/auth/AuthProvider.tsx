import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { redirectToLogin, redirectToRegistration, signOutOfSession } from './session';

export type AuthStatus = 'loading' | 'signed-in' | 'signed-out';

/** The signed-in player, as `GET /api/me` describes them. */
export type CurrentUser = {
  id: string;
  subject: string;
  displayName: string;
  email: string;
  roles: string[];
};

type AuthContextValue = {
  status: AuthStatus;
  user: CurrentUser | null;
  /** Goes to the login page and comes back to `returnTo` (default: the current page). */
  signIn: (returnTo?: string) => void;
  /** Goes to the account registration page and comes back signed in to `returnTo` (default: `/`). */
  signUp: (returnTo?: string) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

/** Asks the API whether this browser's session cookie belongs to a player; never forces a login. */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>('loading');
  const [user, setUser] = useState<CurrentUser | null>(null);

  useEffect(() => {
    fetch('/api/me')
      .then((response) => (response.ok ? (response.json() as Promise<CurrentUser>) : null))
      .catch(() => null)
      .then((found) => {
        setUser(found);
        setStatus(found ? 'signed-in' : 'signed-out');
      });
  }, []);

  const signIn = useCallback((returnTo?: string) => redirectToLogin(returnTo), []);
  const signUp = useCallback((returnTo?: string) => redirectToRegistration(returnTo), []);
  const signOut = useCallback(() => signOutOfSession(), []);
  const value = useMemo<AuthContextValue>(() => ({ status, user, signIn, signUp, signOut }), [status, user, signIn, signUp, signOut]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

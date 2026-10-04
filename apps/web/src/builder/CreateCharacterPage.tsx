import { useEffect, useRef } from 'react';
import { Link, Navigate, useParams } from 'react-router';
import { supportedSystem } from '../characters/supportedSystems';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { AppPage } from '../theme/AppPage';
import { useCreateCharacter } from './useCreateCharacter';
import '../home/home.css';

/**
 * Where a "Create character" lands after the login (`/characters/new/:systemId`): it creates that
 * system's draft and replaces itself with the builder, showing only a short "Creating…" meanwhile.
 */
export function CreateCharacterPage({ userName }: { userName: string }) {
  const { systemId } = useParams();
  const { create, error } = useCreateCharacter();
  /** Keeps StrictMode's second effect run from creating a second draft. */
  const started = useRef(false);
  const system = systemId ? supportedSystem(systemId) : undefined;

  useEffect(() => {
    if (system && !started.current) {
      started.current = true;
      void create(system.id, { replace: true });
    }
  }, [system, create]);

  if (!system) {
    return <Navigate to="/" replace />;
  }
  return (
    <AppPage>
      <AppHeader userName={userName} compact />
      <main className="app-wrap create-character">
        {error ? (
          <div className="home-error" role="alert">
            Could not create the character: {error} <Link to="/">Back to your characters</Link>
          </div>
        ) : (
          <p className="app-muted" role="status">
            Creating your {system.label} character…
          </p>
        )}
      </main>
      <AppFooter />
    </AppPage>
  );
}

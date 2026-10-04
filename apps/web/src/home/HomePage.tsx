import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { deleteCharacter, listCharacters, type Character } from '../characters/api';
import { useCreateCharacter } from '../builder/useCreateCharacter';
import { onlySystemId, SUPPORTED_SYSTEMS, systemLabel } from '../characters/supportedSystems';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { AppPage } from '../theme/AppPage';
import { CharacterBanner } from './CharacterBanner';
import { ConfirmDialog } from './ConfirmDialog';
import { SystemCards } from './SystemCards';
import './home.css';

const ALL = 'all';

/** The signed-in home: start a character in any system, and every character the player has, grouped by system. */
export function HomePage({ userName }: { userName: string }) {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [characters, setCharacters] = useState<Character[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toDelete, setToDelete] = useState<Character | null>(null);
  const [deleting, setDeleting] = useState(false);

  const { create, creating, error: createError } = useCreateCharacter();
  /** "Create character" without a system: straight into the only one, or the system picker once there are several. */
  const startCharacter = () => {
    const only = onlySystemId();
    if (only) {
      void create(only);
    } else {
      navigate('/characters/new');
    }
  };

  const refresh = useCallback(() => {
    listCharacters()
      .then(setCharacters)
      .catch(() => setError('Could not load your characters.'));
  }, []);

  useEffect(refresh, [refresh]);

  const systems = systemsWithCharacters(characters ?? []);
  const requested = searchParams.get('system') ?? ALL;
  const filter = requested === ALL || systems.includes(requested) ? requested : ALL;
  const shown = systems.filter((systemId) => filter === ALL || filter === systemId);
  const choose = (systemId: string) => setSearchParams(systemId === ALL ? {} : { system: systemId });

  const confirmDelete = async () => {
    if (!toDelete) {
      return;
    }
    setDeleting(true);
    try {
      await deleteCharacter(toDelete.id);
      setToDelete(null);
      refresh();
    } catch {
      setError(`Could not delete ${toDelete.name}.`);
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <AppPage>
      <AppHeader userName={userName} />
      <main className="app-wrap home-main">
        <div className="home-head">
          <h1 className="app-heading">Your characters</h1>
          {characters && <span className="app-muted">{countLabel(characters.length)}</span>}
          <button type="button" className="app-btn app-btn--primary" disabled={creating !== null} onClick={startCharacter}>
            {creating ? 'Creating…' : '＋ Create character'}
          </button>
        </div>

        <section className="home-new" aria-label="Start something new">
          <div className="home-label">Start something new</div>
          <SystemCards compact creating={creating} onCreate={(systemId) => void create(systemId)} />
        </section>

        {(error ?? createError) && (
          <div className="home-error" role="alert">
            {error ?? `Could not create the character: ${createError}`}
          </div>
        )}

        {characters && characters.length === 0 && (
          <div className="home-empty">
            <p>Your vault is empty. Every hero starts somewhere.</p>
            <button type="button" className="app-btn app-btn--primary" disabled={creating !== null} onClick={startCharacter}>
              Create your first character
            </button>
          </div>
        )}

        {characters && characters.length > 0 && (
          <>
            <div className="home-filters" role="group" aria-label="Filter by game system">
              <button type="button" className="app-chip" aria-pressed={filter === ALL} onClick={() => choose(ALL)}>
                All<small>{characters.length}</small>
              </button>
              {systems.map((systemId) => (
                <button key={systemId} type="button" className="app-chip" aria-pressed={filter === systemId} onClick={() => choose(systemId)}>
                  {systemLabel(systemId)}
                  <small>{characters.filter((character) => character.systemId === systemId).length}</small>
                </button>
              ))}
            </div>
            {shown.map((systemId) => {
              const inSystem = characters.filter((character) => character.systemId === systemId);
              return (
                <section key={systemId} className="home-group" aria-label={systemLabel(systemId)}>
                  <div className="home-group__head">
                    <h2 className="app-heading">{systemLabel(systemId)}</h2>
                    <span className="app-muted">{inSystem.length}</span>
                    <div className="home-group__line" />
                  </div>
                  <div className="home-banners">
                    {inSystem.map((character) => (
                      <CharacterBanner key={character.id} character={character} onDelete={setToDelete} />
                    ))}
                  </div>
                </section>
              );
            })}
          </>
        )}
      </main>
      <AppFooter />

      {toDelete && (
        <ConfirmDialog title="Delete character?" confirmLabel="Delete" busy={deleting} onConfirm={confirmDelete} onCancel={() => setToDelete(null)}>
          {toDelete.name} will be removed from your vault. This can't be undone.
        </ConfirmDialog>
      )}
    </AppPage>
  );
}

/** The systems that have characters, in the order the vault lists systems, then any other in first-seen order. */
function systemsWithCharacters(characters: Character[]): string[] {
  const present = [...new Set(characters.map((character) => character.systemId))];
  const known = SUPPORTED_SYSTEMS.map((system) => system.id).filter((id) => present.includes(id));
  return [...known, ...present.filter((id) => !known.includes(id))];
}

function countLabel(count: number): string {
  return count === 1 ? '1 character' : `${count} characters`;
}

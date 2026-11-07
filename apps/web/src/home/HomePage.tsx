import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { deleteCharacter, listCharacters, type Character } from '../characters/api';
import { useCreateCharacter } from '../builder/useCreateCharacter';
import { onlySystemId, SUPPORTED_SYSTEMS, systemLabel } from '../characters/supportedSystems';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { AppPage } from '../theme/AppPage';
import { useHomeText } from '../locales/platformMessages';
import { CharacterBanner } from './CharacterBanner';
import { ConfirmDialog } from './ConfirmDialog';
import { SystemCards } from './SystemCards';
import './home.css';

const ALL = 'all';

/** The signed-in home: start a character in any system, and every character the player has, grouped by system. */
export function HomePage({ userName }: { userName: string }) {
  const t = useHomeText();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [characters, setCharacters] = useState<Character[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toDelete, setToDelete] = useState<Character | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [collapsed, setCollapsed] = useStoredState<string[]>('osv.home.collapsed', []);
  const toggleGroup = (systemId: string) =>
    setCollapsed(collapsed.includes(systemId) ? collapsed.filter((id) => id !== systemId) : [...collapsed, systemId]);

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
      .catch(() => setError(t('home.loadFailed')));
  }, [t]);

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
      setError(t('home.deleteFailed', { name: toDelete.name }));
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
          <h1 className="app-heading">{t('home.title')}</h1>
          {characters && <span className="app-muted">{t('home.characterCount', { count: characters.length })}</span>}
          <button type="button" className="app-btn app-btn--primary" disabled={creating !== null} onClick={startCharacter}>
            {creating ? t('home.creating') : t('home.create')}
          </button>
        </div>

        <section className="home-new" aria-label={t('home.startNew')}>
          <div className="home-label">{t('home.startNew')}</div>
          <SystemCards compact creating={creating} onCreate={(systemId) => void create(systemId)} />
        </section>

        {(error ?? createError) && (
          <div className="home-error" role="alert">
            {error ?? t('home.createFailed', { error: createError ?? '' })}
          </div>
        )}

        {characters && characters.length === 0 && (
          <div className="home-empty">
            <p>{t('home.empty')}</p>
            <button type="button" className="app-btn app-btn--primary" disabled={creating !== null} onClick={startCharacter}>
              {t('home.createFirst')}
            </button>
          </div>
        )}

        {characters && characters.length > 0 && (
          <>
            <div className="home-filters" role="group" aria-label={t('home.filterLabel')}>
              <button type="button" className="app-chip" aria-pressed={filter === ALL} onClick={() => choose(ALL)}>
                {t('home.all')}<small>{characters.length}</small>
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
                  <button type="button" className="home-group__head home-toggle" aria-expanded={!collapsed.includes(systemId)} onClick={() => toggleGroup(systemId)}>
                    <h2 className="app-heading">{collapsed.includes(systemId) ? '▸' : '▾'} {systemLabel(systemId)}</h2>
                    <span className="app-muted">{inSystem.length}</span>
                    <div className="home-group__line" />
                  </button>
                  {!collapsed.includes(systemId) && (
                  <div className="home-banners">
                    {inSystem.map((character) => (
                      <CharacterBanner key={character.id} character={character} onDelete={setToDelete} />
                    ))}
                  </div>
                  )}
                </section>
              );
            })}
          </>
        )}
      </main>
      <AppFooter />

      {toDelete && (
        <ConfirmDialog title={t('home.deleteTitle')} confirmLabel={t('home.deleteConfirm')} busy={deleting} onConfirm={confirmDelete} onCancel={() => setToDelete(null)}>
          {t('home.deleteBody', { name: toDelete.name })}
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

/** A small UI preference kept in this browser; falls back to the default when storage is unavailable. */
function useStoredState<T>(key: string, initial: T): [T, (value: T) => void] {
  const [value, setValue] = useState<T>(() => {
    try {
      const stored = localStorage.getItem(key);
      return stored === null ? initial : (JSON.parse(stored) as T);
    } catch {
      return initial;
    }
  });
  const store = (next: T) => {
    setValue(next);
    try {
      localStorage.setItem(key, JSON.stringify(next));
    } catch {
      // Storage unavailable: the choice lasts for this visit only.
    }
  };
  return [value, store];
}
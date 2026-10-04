import { Link } from 'react-router';
import { SUPPORTED_SYSTEMS } from '../characters/supportedSystems';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { BUILDER_COLUMN_WIDTH } from './builderLayout';
import { useCreateCharacter } from './useCreateCharacter';
import '../systems/dnd5e/themes.css';
import './builder.css';

/**
 * Picks the game system for a new character, for when the vault offers more than one. With a
 * single system every "Create character" skips this page and starts that system directly.
 */
export function SystemPicker({ userName }: { userName: string }) {
  const { create, creating, error } = useCreateCharacter();

  return (
    <div className="builder-page">
      <AppHeader userName={userName} backToListWithin={BUILDER_COLUMN_WIDTH} compact />
      <main className="builder-system-picker">
        <div className="builder-step-head">
          <h1>Create a character</h1>
          <p>Choose the game system first. It decides which rules, choices and sheet your character gets.</p>
        </div>
        {error && (
          <div className="builder-error" role="alert">
            {error}
          </div>
        )}
        <div className="builder-system-picker__grid">
          {SUPPORTED_SYSTEMS.map((system) => (
            <button
              key={system.id}
              type="button"
              className="builder-frame builder-frame--side builder-system-card"
              disabled={creating !== null}
              onClick={() => void create(system.id)}
            >
              <span className="builder-frame__title">{system.label}</span>
              <span className="builder-system-card__description">{system.description}</span>
              <span className="builder-btn builder-btn--primary">{creating === system.id ? 'Creating…' : `Build a ${system.label} character`}</span>
            </button>
          ))}
        </div>
        <Link className="builder-btn" to="/">
          ← Back to your characters
        </Link>
      </main>
      <AppFooter />
    </div>
  );
}

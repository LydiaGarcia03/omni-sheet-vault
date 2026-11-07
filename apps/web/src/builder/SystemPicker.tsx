import { Link } from 'react-router';
import { SystemCards } from '../home/SystemCards';
import '../home/home.css';
import { useHomeText } from '../locales/platformMessages';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { useCreateCharacter } from './useCreateCharacter';

/**
 * Picks the game system for a new character, for when the vault offers more than one. With a
 * single system every "Create character" skips this page and starts that system directly.
 */
export function SystemPicker({ userName }: { userName: string }) {
  const { create, creating, error } = useCreateCharacter();
  const t = useHomeText();

  return (
    <div className="app-page">
      <AppHeader userName={userName} compact />
      <main className="app-wrap system-picker">
        <h1 className="app-heading">{t('picker.title')}</h1>
        <p className="system-picker__lead">{t('picker.lead')}</p>
        {error && (
          <p className="system-picker__error" role="alert">
            {error}
          </p>
        )}
        <SystemCards creating={creating} onCreate={(systemId) => void create(systemId)} />
        <Link className="app-btn app-btn--ghost system-picker__back" to="/">
          {t('picker.back')}
        </Link>
      </main>
      <AppFooter />
    </div>
  );
}

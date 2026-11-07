import { useEffect, useRef, useState, type CSSProperties, type ReactNode } from 'react';
import { Link } from 'react-router';
import logo from '../assets/brand/omni-logo-light.svg';
import { useAuth } from '../auth/AuthProvider';
import { isDesktopEdition } from '../edition';
import { LanguageMenu } from '../language/LanguageMenu';
import { useShellText } from '../locales/platformMessages';
import { useAppTheme } from '../theme/appTheme';
import { systemMarkFor } from './systemMarks';

type AppHeaderProps = {
  /** The signed-in player's name; null shows "Log in" instead of the user menu. */
  userName: string | null;
  /**
   * Adds a "← Your characters" link, right-aligned to the page's centred column of this width (px),
   * so it sits near the user button without leaving the page's own layout.
   */
  backToListWithin?: number;
  /** The 58 px bar with the smaller logo, for working pages (the builder, the sheet). */
  compact?: boolean;
  /** The page's own status next to the logo, e.g. the builder's draft badge and save state. */
  children?: ReactNode;
  /** The game system of the character on this page, shown after the app's name in that system's own style. */
  systemId?: string;
};

/** The app's top bar on every page: logo and name, an optional way back to the list, and the player's menu (or "Log in"). */
export function AppHeader({ userName, backToListWithin, compact = false, children, systemId }: AppHeaderProps) {
  const { signIn, signOut, user } = useAuth();
  const t = useShellText();
  const SystemMark = systemId === undefined ? undefined : systemMarkFor(systemId);
  return (
    <header className={`app-header${compact ? ' is-compact' : ''}`}>
      <div className="app-wrap">
        <Link className="app-brand" to="/">
          <img src={logo} alt="" />
          <span>Omni Sheet Vault</span>
        </Link>
        {SystemMark && (
          <div className="app-system">
            <SystemMark />
          </div>
        )}
        {children && <div className="app-header__extra">{children}</div>}
        {backToListWithin !== undefined && (
          <div className="app-header__column" style={{ '--app-header-column': `${backToListWithin}px` } as CSSProperties}>
            <Link className="app-header__back" to="/">
              {t('header.backToCharacters')}
            </Link>
          </div>
        )}
        <div className="app-header__right">
          <LanguageMenu />
          {userName === null ? (
            <button type="button" className="app-btn app-btn--ghost" onClick={() => signIn('/')}>
              {t('header.logIn')}
            </button>
          ) : (
            <UserMenu name={userName} avatarUrl={user?.avatarUrl ?? null} onSignOut={signOut} />
          )}
        </div>
      </div>
    </header>
  );
}

function UserMenu({ name, avatarUrl, onSignOut }: { name: string; avatarUrl: string | null; onSignOut: () => void }) {
  const [open, setOpen] = useState(false);
  const { theme, setTheme } = useAppTheme();
  const t = useShellText();
  const root = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (event: MouseEvent) => {
      if (!root.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    const closeOnEscape = (event: KeyboardEvent) => event.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, [open]);

  return (
    <div className="app-user" ref={root}>
      <button type="button" className="app-user__button" aria-haspopup="menu" aria-expanded={open} onClick={() => setOpen(!open)}>
        <span className="app-user__avatar" aria-hidden="true">
          {avatarUrl ? <img src={avatarUrl} alt="" /> : name.charAt(0).toUpperCase()}
        </span>
        {name} ▾
      </button>
      {open && (
        <div className="app-user__menu" role="menu">
          <Link role="menuitem" to="/" onClick={() => setOpen(false)}>
            {t('header.myCharacters')}
          </Link>
          <Link role="menuitem" to="/profile" onClick={() => setOpen(false)}>
            {t('header.profile')}
          </Link>
          <button
            type="button"
            role="menuitemcheckbox"
            aria-checked={theme === 'dark'}
            className="app-user__toggle"
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
          >
            {t('header.darkTheme')}
            <span className="app-user__switch" aria-hidden="true" />
          </button>
          {!isDesktopEdition && (
            <>
              <hr />
              <button type="button" role="menuitem" onClick={onSignOut}>
                {t('header.logOut')}
              </button>
            </>
          )}
        </div>
      )}
    </div>
  );
}

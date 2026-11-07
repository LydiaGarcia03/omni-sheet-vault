import { useRef, useState, type ChangeEvent, type FormEvent } from 'react';
import { useAuth, type CurrentUser } from '../auth/AuthProvider';
import { redirectToAccountAction } from '../auth/session';
import { PORTRAIT_ACCEPTED_TYPES, PORTRAIT_MAX_BYTES } from '../characters/portrait';
import { isDesktopEdition } from '../edition';
import { useProfileText } from '../locales/platformMessages';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';
import { AppPage } from '../theme/AppPage';
import { removeAvatar, saveDisplayName, uploadAvatar } from './api';

const PROFILE_PATH = '/profile';
const DISPLAY_NAME_MAX = 40;

/** The player's own profile: picture and shown name here; password and email on Keycloak's own screens. */
export function ProfilePage({ user }: { user: CurrentUser }) {
  const t = useProfileText();
  return (
    <AppPage className="profile-page">
      <AppHeader userName={user.displayName} backToListWithin={720} />
      <main className="app-wrap">
        <h1 className="app-heading">{t('title')}</h1>
        <PictureSection user={user} />
        <DetailsSection user={user} />
        {!isDesktopEdition && <SecuritySection />}
      </main>
      <AppFooter />
    </AppPage>
  );
}

function PictureSection({ user }: { user: CurrentUser }) {
  const t = useProfileText();
  const { updateUser } = useAuth();
  const input = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const [problem, setProblem] = useState<string | null>(null);

  const change = (action: () => Promise<CurrentUser>) => {
    setBusy(true);
    setProblem(null);
    action()
      .then(updateUser)
      .catch(() => setProblem(t('picture.failed')))
      .finally(() => setBusy(false));
  };

  const choose = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) {
      return;
    }
    if (!PORTRAIT_ACCEPTED_TYPES.includes(file.type)) {
      setProblem(t('picture.wrongType'));
    } else if (file.size > PORTRAIT_MAX_BYTES) {
      setProblem(t('picture.tooLarge'));
    } else {
      change(() => uploadAvatar(file));
    }
  };

  return (
    <section className="profile-section" aria-labelledby="profile-picture">
      <h2 id="profile-picture" className="app-heading">
        {t('picture.heading')}
      </h2>
      <div className="profile-picture">
        <span className="profile-picture__image" aria-hidden="true">
          {user.avatarUrl ? <img src={user.avatarUrl} alt="" /> : user.displayName.charAt(0).toUpperCase()}
        </span>
        <div className="profile-picture__actions">
          <input ref={input} type="file" accept={PORTRAIT_ACCEPTED_TYPES.join(',')} hidden onChange={choose} data-testid="avatar-file" />
          <button type="button" className="app-btn" disabled={busy} onClick={() => input.current?.click()}>
            {user.avatarUrl ? t('picture.replace') : t('picture.upload')}
          </button>
          {user.avatarUrl && (
            <button type="button" className="app-btn" disabled={busy} onClick={() => change(removeAvatar)}>
              {t('picture.remove')}
            </button>
          )}
          <p className="app-muted">{t('picture.hint')}</p>
          {problem && (
            <p className="profile-problem" role="alert">
              {problem}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}

function DetailsSection({ user }: { user: CurrentUser }) {
  const t = useProfileText();
  const { updateUser } = useAuth();
  const username = user.username ?? user.displayName;
  const [name, setName] = useState(user.displayName === username ? '' : user.displayName);
  const [state, setState] = useState<'idle' | 'saving' | 'saved' | 'failed'>('idle');

  const save = (event: FormEvent) => {
    event.preventDefault();
    setState('saving');
    saveDisplayName(name.trim())
      .then((saved) => {
        updateUser(saved);
        setState('saved');
      })
      .catch(() => setState('failed'));
  };

  return (
    <section className="profile-section" aria-labelledby="profile-details">
      <h2 id="profile-details" className="app-heading">
        {t('details.heading')}
      </h2>
      <form className="profile-form" onSubmit={save}>
        <label htmlFor="profile-display-name">{t('details.displayName')}</label>
        <div className="profile-form__row">
          <input
            id="profile-display-name"
            className="profile-input"
            value={name}
            maxLength={DISPLAY_NAME_MAX}
            placeholder={username}
            onChange={(event) => {
              setName(event.target.value);
              setState('idle');
            }}
          />
          <button type="submit" className="app-btn app-btn--primary" disabled={state === 'saving'}>
            {state === 'saving' ? t('details.saving') : t('details.save')}
          </button>
        </div>
        <p className="app-muted">{t('details.displayNameHint')}</p>
        {state === 'saved' && <p role="status">{t('details.saved')}</p>}
        {state === 'failed' && (
          <p className="profile-problem" role="alert">
            {t('details.failed')}
          </p>
        )}
      </form>
      <dl className="profile-facts">
        <dt>{t('details.username')}</dt>
        <dd>
          {username}
          <span className="app-muted"> · {t('details.usernameHint')}</span>
        </dd>
        {user.email && (
          <>
            <dt>{t('details.email')}</dt>
            <dd>{user.email}</dd>
          </>
        )}
      </dl>
    </section>
  );
}

function SecuritySection() {
  const t = useProfileText();
  return (
    <section className="profile-section" aria-labelledby="profile-security">
      <h2 id="profile-security" className="app-heading">
        {t('security.heading')}
      </h2>
      <p className="app-muted">{t('security.intro')}</p>
      <div className="profile-security__actions">
        <button type="button" className="app-btn" onClick={() => redirectToAccountAction('password', PROFILE_PATH)}>
          {t('security.changePassword')}
        </button>
        <button type="button" className="app-btn" onClick={() => redirectToAccountAction('email', PROFILE_PATH)}>
          {t('security.changeEmail')}
        </button>
      </div>
    </section>
  );
}

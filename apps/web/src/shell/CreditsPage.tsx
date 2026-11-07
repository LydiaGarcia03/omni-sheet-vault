import { useCreditsText } from '../locales/platformMessages';
import { AppFooter } from './AppFooter';
import { AppPage } from '../theme/AppPage';
import { AppHeader } from './AppHeader';

const GAME_ICONS: { author: string; icons: string }[] = [
  {
    author: 'Lorc',
    icons:
      'crossed-swords, high-shot, axe-swing, hammer-drop, beveled-star, reaper-scythe, ion-cannon-blast, focused-lightning, suspicious, eclipse-flare, lightning-storm, battle-gear',
  },
  { author: 'Skoll', icons: 'fist' },
  { author: 'Sbed', icons: 'acid' },
  { author: 'Daniel Zaitzev', icons: 'death-juice' },
];

const SOFTWARE: { component: string; license: string }[] = [
  { component: 'Spring Boot, Spring Security, Spring Data, Spring Session', license: 'Apache License 2.0' },
  { component: 'Flyway (community edition)', license: 'Apache License 2.0' },
  { component: 'AWS SDK for Java 2.x', license: 'Apache License 2.0' },
  { component: 'React, React DOM, React Router', license: 'MIT License' },
  { component: 'Vite', license: 'MIT License' },
  { component: 'Keycloak', license: 'Apache License 2.0' },
  { component: 'PostgreSQL', license: 'PostgreSQL License' },
  { component: 'MinIO', license: 'GNU AGPL v3' },
];

/** The full credits and licenses, mirroring NOTICE.md; public, so it works signed in or out. */
export function CreditsPage({ userName }: { userName: string | null }) {
  const t = useCreditsText();
  return (
    <AppPage className="credits-page">
      <AppHeader userName={userName} />
      <main className="app-wrap">
        <h1 className="app-heading">{t('title')}</h1>
        <p>{t('intro')}</p>

        <h2 className="app-heading">{t('fanContent')}</h2>
        <p>
          Omni Sheet Vault is unofficial Fan Content permitted under the Fan Content Policy. Not approved/endorsed by Wizards. Portions of the
          materials used are property of Wizards of the Coast. ©Wizards of the Coast LLC. Dungeons &amp; Dragons, D&amp;D and their rules and
          content are trademarks and property of Wizards of the Coast LLC.
        </p>

        <h2 className="app-heading">{t('gameContent')}</h2>
        <p>{t('gameContentText')}</p>

        <h2 className="app-heading">{t('graphics')}</h2>
        <ul>
          <li>{t('graphicsDdb')}</li>
          <li>
            {t('gameIconsBefore')} <a href="https://game-icons.net">game-icons.net</a>, {t('gameIconsUnder')}{' '}
            <a href="https://creativecommons.org/licenses/by/3.0/">CC BY 3.0</a>:
            <ul>
              {GAME_ICONS.map((entry) => (
                <li key={entry.author}>{t('iconsBy', { author: entry.author, icons: entry.icons })}</li>
              ))}
            </ul>
          </li>
          <li>
            {t('flaticonBefore')} <a href="https://www.flaticon.com">Flaticon</a>, {t('flaticonAfter')}
          </li>
        </ul>

        <h2 className="app-heading">{t('fonts')}</h2>
        <p>{t('fontsText')}</p>

        <h2 className="app-heading">{t('software')}</h2>
        <table>
          <thead>
            <tr>
              <th>{t('component')}</th>
              <th>{t('license')}</th>
            </tr>
          </thead>
          <tbody>
            {SOFTWARE.map((row) => (
              <tr key={row.component}>
                <td>{row.component}</td>
                <td>{row.license}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
      <AppFooter />
    </AppPage>
  );
}

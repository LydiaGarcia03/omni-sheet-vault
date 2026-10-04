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
  return (
    <AppPage className="credits-page">
      <AppHeader userName={userName} />
      <main className="app-wrap">
        <h1 className="app-heading">Credits and licenses</h1>
        <p>
          Omni Sheet Vault is a non-commercial fan project. Its own code and art are licensed under the PolyForm Noncommercial License 1.0.0.
          Everything below belongs to its owners and is used under their terms.
        </p>

        <h2 className="app-heading">Fan content</h2>
        <p>
          Omni Sheet Vault is unofficial Fan Content permitted under the Fan Content Policy. Not approved/endorsed by Wizards. Portions of the
          materials used are property of Wizards of the Coast. ©Wizards of the Coast LLC. Dungeons &amp; Dragons, D&amp;D and their rules and
          content are trademarks and property of Wizards of the Coast LLC.
        </p>

        <h2 className="app-heading">Game content</h2>
        <p>
          Rules text and data (spells, classes, species, backgrounds, feats, items) come from the 2014 D&amp;D books through the 5etools data
          files. The content belongs to Wizards of the Coast.
        </p>

        <h2 className="app-heading">Graphics</h2>
        <ul>
          <li>
            The default character portraits, item icons, sheet frames and several interface icons are D&amp;D Beyond / Wizards of the Coast
            art. The dice icons are Font Awesome icons as served by D&amp;D Beyond. The layout takes inspiration from the D&amp;D Beyond
            character sheet and builder.
          </li>
          <li>
            Icons from <a href="https://game-icons.net">game-icons.net</a>, under{' '}
            <a href="https://creativecommons.org/licenses/by/3.0/">CC BY 3.0</a>:
            <ul>
              {GAME_ICONS.map((entry) => (
                <li key={entry.author}>
                  by {entry.author}: {entry.icons}
                </li>
              ))}
            </ul>
          </li>
          <li>
            Icons from <a href="https://www.flaticon.com">Flaticon</a>, under the Flaticon License: "Fire" by meaicon and "Bullet" by Magnific.
          </li>
        </ul>

        <h2 className="app-heading">Fonts</h2>
        <p>Roboto and Roboto Condensed, by Google, under the SIL Open Font License 1.1.</p>

        <h2 className="app-heading">Software</h2>
        <table>
          <thead>
            <tr>
              <th>Component</th>
              <th>License</th>
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

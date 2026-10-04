import { Link } from 'react-router';

/** Every page's footer: the fan content notice and a link to the full credits. */
export function AppFooter() {
  return (
    <footer className="app-footer">
      <div className="app-wrap app-footer__inner">
        <p className="app-footer__notice">
          Omni Sheet Vault is unofficial Fan Content permitted under the Fan Content Policy. Not approved/endorsed by Wizards. Portions of the
          materials used are property of Wizards of the Coast. ©Wizards of the Coast LLC.
        </p>
        <p className="app-footer__links">
          A non-commercial fan project · Rules content from 5etools · Art from D&amp;D Beyond, game-icons.net and Flaticon ·{' '}
          <Link to="/credits">Credits and licenses</Link>
        </p>
      </div>
    </footer>
  );
}

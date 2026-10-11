import { useRef, type ReactNode } from 'react';
import { Link } from 'react-router';
import { useFooterScrollStop } from './useFooterScrollStop';

type AppFooterProps = {
  /** The fan content notice; a game system's own pages pass theirs (VtM: Paradox's). */
  notice?: ReactNode;
  /** The line of sources before the credits link. */
  sources?: ReactNode;
};

const DEFAULT_NOTICE = (
  <>
    Omni Sheet Vault is unofficial Fan Content permitted under the Fan Content Policy. Not approved/endorsed by Wizards. Portions of the materials
    used are property of Wizards of the Coast. ©Wizards of the Coast LLC.
  </>
);
const DEFAULT_SOURCES = <>A non-commercial fan project · Rules content from 5etools · Art from D&amp;D Beyond, game-icons.net and Flaticon</>;

/** Every page's footer: the fan content notice and a link to the full credits. */
export function AppFooter({ notice = DEFAULT_NOTICE, sources = DEFAULT_SOURCES }: AppFooterProps) {
  const footer = useRef<HTMLElement>(null);
  useFooterScrollStop(footer);
  return (
    <footer ref={footer} className="app-footer">
      <div className="app-wrap app-footer__inner">
        <p className="app-footer__notice">{notice}</p>
        <p className="app-footer__links">
          {sources} · <Link to="/credits">Credits and licenses</Link>
        </p>
      </div>
    </footer>
  );
}

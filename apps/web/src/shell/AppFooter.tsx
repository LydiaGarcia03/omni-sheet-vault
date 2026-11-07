import { useRef, type ReactNode } from 'react';
import { Link } from 'react-router';
import { useLanguage } from '../language/language';
import { useShellText } from '../locales/platformMessages';
import { useFooterScrollStop } from './useFooterScrollStop';

type AppFooterProps = {
  /** The fan content notice; a game system's own pages pass theirs (VtM: Paradox's). */
  notice?: ReactNode;
  /** The line of sources before the credits link. */
  sources?: ReactNode;
  /** A page that shows rules text (builder, sheet, level up): outside English it notes the text is an AI translation. */
  rulesText?: boolean;
};

const DEFAULT_NOTICE = (
  <>
    Omni Sheet Vault is unofficial Fan Content permitted under the Fan Content Policy. Not approved/endorsed by Wizards. Portions of the materials
    used are property of Wizards of the Coast. ©Wizards of the Coast LLC.
  </>
);
/** Every page's footer: the fan content notice and a link to the full credits. The legal notices stay in English. */
export function AppFooter({ notice = DEFAULT_NOTICE, sources, rulesText = false }: AppFooterProps) {
  const t = useShellText();
  const { locale } = useLanguage();
  const footer = useRef<HTMLElement>(null);
  useFooterScrollStop(footer);
  return (
    <footer ref={footer} className="app-footer">
      <div className="app-wrap app-footer__inner">
        {rulesText && locale !== 'en' && <p className="app-footer__translation">{t('footer.aiTranslation')}</p>}
        <p className="app-footer__notice">{notice}</p>
        <p className="app-footer__links">
          {sources ?? t('footer.sources')} · <Link to="/credits">{t('footer.credits')}</Link>
        </p>
      </div>
    </footer>
  );
}

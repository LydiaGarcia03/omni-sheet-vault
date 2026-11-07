import { AppFooter } from '../../shell/AppFooter';
import { useVtm5Text } from './locales/vtm5Messages';

/** The app footer on VtM pages, with the World of Darkness fan notice (kept in English). */
export function Vtm5Footer() {
  const t = useVtm5Text();
  return (
    <AppFooter
      rulesText
      notice={
        <>
          Omni Sheet Vault is an unofficial fan project, not approved or endorsed by Paradox Interactive. Vampire: The Masquerade and the World of
          Darkness are trademarks of Paradox Interactive AB.
        </>
      }
      sources={t('footer.sources')}
    />
  );
}

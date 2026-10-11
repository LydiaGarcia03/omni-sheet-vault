import { AppFooter } from '../../shell/AppFooter';

/** The app footer on VtM pages, with the World of Darkness fan notice. */
export function Vtm5Footer() {
  return (
    <AppFooter
      notice={
        <>
          Omni Sheet Vault is an unofficial fan project, not approved or endorsed by Paradox Interactive. Vampire: The Masquerade and the World of
          Darkness are trademarks of Paradox Interactive AB.
        </>
      }
      sources={<>A non-commercial fan project · Rules summarized in our own words from the owner's books · Portraits and icons from Demiplane</>}
    />
  );
}

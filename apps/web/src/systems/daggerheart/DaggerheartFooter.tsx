import { AppFooter } from '../../shell/AppFooter';

/** The app footer on Daggerheart pages, with the DPCGL attribution adr-0010 requires. */
export function DaggerheartFooter() {
  return (
    <AppFooter
      rulesText
      notice={
        <>
          Daggerheart™ Compatible. This product includes materials from the Daggerheart System Reference Document 2.0, © Critical Role, LLC. under
          the terms of the Darrington Press Community Gaming (DPCGL) License. More information can be found at https://www.daggerheart.com. There
          are no previous modifications by others.
        </>
      }
      sources={<>A non-commercial fan project · Rules from the Daggerheart SRD 2.0 · Portraits, frames and class art from Demiplane</>}
    />
  );
}

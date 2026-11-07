# ADR 0010 — Daggerheart content from the SRD 2.0 under the DPCGL

**Status:** accepted (owner, 2026-10-12)

## Context

Daggerheart (`systems/daggerheart/`) needs its classes, subclasses, ancestries,
communities, domain cards and equipment as catalogue content. The only open
source is the **Daggerheart SRD 2.0** (2026-08-25). It covers the Core Set and
the Hope & Fear expansion, and it is published as Public Game Content under the
**Darrington Press Community Gaming License** (DPCGL, 2025-07-30).

The DPCGL allows private, non-commercial play freely. *Sharing* the content
publicly is limited to print, streams, podcasts and five whitelisted VTTs;
anything else needs Darrington Press's written approval (§1.9). Campaign
frames can't be republished, and official logos and art can't be used
(`systems/daggerheart/character-rules.md` §0).

## Decision

1. **The SRD 2.0 is the single content source for Daggerheart.**
   - It is imported once into `content/daggerheart/*.json` (slice D1),
     summarized in our own words where text is shown.
   - It plays the role adr-0006 gives 5etools for D&D: no hand-authored game
     facts.
2. **Same posture as D&D:**
   - The content is for the owner's private use: the desktop edition and
     private instances for a gaming group.
   - SRD-derived content stays out of the public remote until Darrington
     Press approves a public deployment.
   - A public build without it uses adr-0005's redaction switch.
3. **Attribution goes in the footer** on Daggerheart pages, with the exact
   DPCGL notice from `character-rules.md` §0 and "Daggerheart™ Compatible".
   That is the same footer pattern VtM uses for its notice.
4. ~~**No official art in the product.**~~ **Amended 2026-10-12 (owner,
   D3): Demiplane's art is used in the app.**
   - The builder and sheet use the mockups' graphics as they are: class
     plates, banners and art, frames, stat frames, the domain icon sprite, the
     gold dagger logo and Demiplane's 33 default portraits. They live in
     `apps/web/src/systems/daggerheart/` and are committed with the code, as
     VtM's Demiplane portraits and icons already were.
   - The footer credits them ("Portraits, frames and class art from
     Demiplane").
   - Like the SRD content, they stay out of the public remote, and out of any
     public deployment, until their use is cleared. Before that, they are
     replaced with original art.
5. **Campaign frames are out of scope.**
6. **Amended 2026-11-07 (owner): the SRD content and Demiplane's art are
   pushed to the public repository** with the Daggerheart code, overriding the
   "stays out of the public remote" parts of decisions 2 and 4. The owner was
   told the DPCGL limits public sharing to the listed media without Darrington
   Press's written approval, and that the art's use is not cleared. A public
   *deployment* with Daggerheart still waits for both clearances.

## Consequences

- Slices D1–D7 can proceed with SRD content behind the redaction switch.
- A public release with Daggerheart needs both Darrington Press's approval for
  the SRD content and original art in place of Demiplane's (decision 4).

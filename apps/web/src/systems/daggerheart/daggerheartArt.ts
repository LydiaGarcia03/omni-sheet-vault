const ART = import.meta.glob<string>('./assets/class-*.webp', { eager: true, import: 'default' });

function asset(file: string): string | undefined {
  return ART[`./assets/${file}`];
}

/** A class's art: the character example, the name plate and the hanging banner (Demiplane's, see adr-0010). */
export function classArt(classSlug: string): { art?: string; plate?: string; banner?: string } {
  return {
    art: asset(`class-art-${classSlug}.webp`),
    plate: asset(`class-plate-${classSlug}.webp`),
    banner: asset(`class-banner-${classSlug}.webp`),
  };
}

/** Domains whose icon is in the 3×3 sprite; Dread has none and draws a ring. */
export const DOMAIN_COLORS: Record<string, string> = {
  arcana: '#6d3fd1',
  blade: '#b5312a',
  bone: '#6f7178',
  codex: '#2f6fbf',
  dread: '#4b1e5a',
  grace: '#c8417f',
  midnight: '#2b2d45',
  sage: '#1f8a4c',
  splendor: '#b8861f',
  valor: '#c8601c',
};

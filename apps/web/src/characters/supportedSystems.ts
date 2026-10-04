export type SupportedSystem = {
  id: string;
  label: string;
  description: string;
  /** One line under the name on the landing and home cards. */
  tagline: string;
  /** The system's colour: the left bar of its character banners. */
  color: string;
  /** The card's painted background. */
  cardBackground: string;
};

/** A game system shown on the landing and home pages as "Coming soon": marketing only, nothing can be created yet. */
export type UpcomingSystem = {
  label: string;
  tagline: string;
  cardBackground: string;
};

export const SUPPORTED_SYSTEMS: SupportedSystem[] = [
  {
    id: 'dnd-5e',
    label: 'D&D 5e',
    description: "Dungeons & Dragons, fifth edition (2014 rules): Player's Handbook species, classes and backgrounds, plus the expansions you enable.",
    tagline: 'Dungeons & Dragons · 2014 rules',
    color: '#C2412F',
    cardBackground: 'linear-gradient(135deg, #7A1F1B, #C2412F 55%, #E3963E)',
  },
];

export const UPCOMING_SYSTEMS: UpcomingSystem[] = [
  { label: 'Vampire', tagline: 'Vampire: The Masquerade', cardBackground: 'linear-gradient(135deg, #1A0608, #6E0E17 60%, #A3121F)' },
  { label: 'Daggerheart', tagline: 'Hope, fear and duality dice', cardBackground: 'linear-gradient(135deg, #1D2B4F, #3A5BA0 55%, #9B7BD0)' },
  { label: 'Pathfinder 2e', tagline: 'Three actions a turn', cardBackground: 'linear-gradient(135deg, #3D2A10, #8A5A1C 55%, #D0A548)' },
  { label: 'Call of Cthulhu', tagline: 'Sanity is a resource', cardBackground: 'linear-gradient(135deg, #0D2A26, #1D5B52 55%, #5E8F7F)' },
  { label: 'Tormenta20', tagline: 'Arton awaits', cardBackground: 'linear-gradient(135deg, #3A0D3D, #7B2177 55%, #E0A23B)' },
];

const FALLBACK_COLOR = '#92A2B3';

/** The system a plain "Create character" starts in, when the vault offers only one; null means the player must pick. */
export function onlySystemId(): string | null {
  return SUPPORTED_SYSTEMS.length === 1 ? SUPPORTED_SYSTEMS[0].id : null;
}

export function supportedSystem(systemId: string): SupportedSystem | undefined {
  return SUPPORTED_SYSTEMS.find((system) => system.id === systemId);
}

export function systemLabel(systemId: string): string {
  return supportedSystem(systemId)?.label ?? systemId;
}

export function systemColor(systemId: string): string {
  return supportedSystem(systemId)?.color ?? FALLBACK_COLOR;
}

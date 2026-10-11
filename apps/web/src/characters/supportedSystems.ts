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
  {
    id: 'vtm-v5',
    label: 'Vampire 5e',
    description: 'Vampire: The Masquerade, fifth edition: the core book plus the Camarilla and Anarch clans.',
    tagline: 'Vampire: The Masquerade',
    color: '#7A0A14',
    cardBackground: 'linear-gradient(135deg, #1A0608, #6E0E17 60%, #A3121F)',
  },
];

export const UPCOMING_SYSTEMS: UpcomingSystem[] = [
  { label: 'Daggerheart', tagline: 'Hope & fear and duality dice', cardBackground: 'linear-gradient(135deg, #1F2D5A 0%, #3E4EA1 30%, #6A42B8 68%, #CFA24A 100%)' },
  { label: 'Pathfinder 2e', tagline: 'Three actions a turn', cardBackground: 'linear-gradient(135deg, #1A0D08, #7A2E1B 60%, #B68A2E)' },
  { label: 'Call of Cthulhu', tagline: 'Sanity is a resource', cardBackground: 'radial-gradient(ellipse at 75% 30%, #3A766B 0%, transparent 42%), linear-gradient(120deg, #081C1A 0%, #123B35 100%)' },
  { label: 'Tormenta20', tagline: 'Arton awaits', cardBackground: 'linear-gradient(135deg, #231126 0%, #5A1E5F 46%, #16101A 100%), linear-gradient(60deg, transparent 0%, rgba(224, 162, 59, .16) 46%, rgba(255, 210, 120, .10) 52%, transparent 58%)' },
  { label: 'Candela Obscura', tagline: 'Join an esoteric order', cardBackground: 'radial-gradient(ellipse at 90% 10%, rgba(180,140,65,.8) 0%, transparent 35%), radial-gradient(ellipse at 35% 70%, rgba(95,78,38,.65) 0%, transparent 45%), radial-gradient(ellipse at 0% 100%, #2A281F 0%, transparent 50%), #0D0D0C' },
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

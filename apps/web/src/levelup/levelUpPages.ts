import type { ComponentType } from 'react';
import { Dnd5eLevelUpPage } from '../systems/dnd5e/levelup/Dnd5eLevelUpPage';

export type LevelUpPageProps = { characterId: string; characterName: string; userName: string };

const LEVEL_UP_PAGES: Record<string, ComponentType<LevelUpPageProps>> = {
  'dnd-5e': Dnd5eLevelUpPage,
};

/** The level up page for a game system, or undefined when the system has none. */
export function levelUpPageFor(systemId: string): ComponentType<LevelUpPageProps> | undefined {
  return LEVEL_UP_PAGES[systemId];
}

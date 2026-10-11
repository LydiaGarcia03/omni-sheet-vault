import type { ComponentType } from 'react';
import { Dnd5eSystemMark } from '../systems/dnd5e/Dnd5eSystemMark';
import { Vtm5SystemMark } from '../systems/vtm5/Vtm5SystemMark';

const MARKS_BY_SYSTEM: Record<string, ComponentType> = {
  'dnd-5e': Dnd5eSystemMark,
  'vtm-v5': Vtm5SystemMark,
};

/** The game system's styled name for the app header; undefined for a system without one. */
export function systemMarkFor(systemId: string): ComponentType | undefined {
  return MARKS_BY_SYSTEM[systemId];
}

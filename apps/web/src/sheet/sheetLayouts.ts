import type { ComponentType } from 'react';
import type { Character } from '../characters/api';
import { DaggerheartSheetScreen } from '../systems/daggerheart/sheet/DaggerheartSheetScreen';
import { Vtm5SheetScreen } from '../systems/vtm5/sheet/Vtm5SheetScreen';
import { CharacterSheetScreen } from './CharacterSheetScreen';

export type SheetLayoutProps = { character: Character; userLabel: string; onBack: () => void };

const SHEET_LAYOUTS: Record<string, ComponentType<SheetLayoutProps>> = {
  'dnd-5e': CharacterSheetScreen,
  'vtm-v5': Vtm5SheetScreen,
  daggerheart: DaggerheartSheetScreen,
};

/** The sheet screen of a game system, or undefined for a system without one. */
export function sheetLayoutFor(systemId: string): ComponentType<SheetLayoutProps> | undefined {
  return SHEET_LAYOUTS[systemId];
}

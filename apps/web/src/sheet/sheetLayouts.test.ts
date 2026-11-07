import { describe, expect, it, vi } from 'vitest';

vi.mock('./CharacterSheetScreen', () => ({ CharacterSheetScreen: () => null }));
vi.mock('../systems/daggerheart/sheet/DaggerheartSheetScreen', () => ({ DaggerheartSheetScreen: () => null }));

import { DaggerheartSheetScreen } from '../systems/daggerheart/sheet/DaggerheartSheetScreen';
import { CharacterSheetScreen } from './CharacterSheetScreen';
import { sheetLayoutFor } from './sheetLayouts';

describe('sheetLayoutFor', () => {
  it("gives D&D 5e its sheet screen", () => {
    expect(sheetLayoutFor('dnd-5e')).toBe(CharacterSheetScreen);
  });

  it('gives Daggerheart its own sheet screen', () => {
    expect(sheetLayoutFor('daggerheart')).toBe(DaggerheartSheetScreen);
  });

  it('has no layout for a system without one, so nothing falls back to D&D', () => {
    expect(sheetLayoutFor('unknown-system')).toBeUndefined();
  });
});

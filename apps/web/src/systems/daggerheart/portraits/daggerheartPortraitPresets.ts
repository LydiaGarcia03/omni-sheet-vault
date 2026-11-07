import type { PortraitPreset } from '../../../characters/portraitPresets';

const FILES = import.meta.glob<string>('./daggerheart_portrait_*.jpg', { eager: true, import: 'default' });

/** Daggerheart's default portraits, numbered by file; the preset id is that number. */
export const DAGGERHEART_PORTRAIT_PRESETS: PortraitPreset[] = Object.entries(FILES)
  .map(([path, url]) => ({ id: /portrait_(\d+)\./.exec(path)?.[1] ?? '', url }))
  .filter((preset) => preset.id !== '')
  .sort((a, b) => Number(a.id) - Number(b.id));

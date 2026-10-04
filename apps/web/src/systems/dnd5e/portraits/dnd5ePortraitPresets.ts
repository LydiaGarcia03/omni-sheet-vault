import type { PortraitPreset } from '../../../characters/portraitPresets';
import manifest from './portraits.json';

const FILES = import.meta.glob<string>('./dnd_portrait_*.png', { eager: true, import: 'default' });

/** D&D Beyond's painted-background default portraits, in the manifest's order; the preset id is Beyond's avatar id. */
export const DND5E_PORTRAIT_PRESETS: PortraitPreset[] = manifest.flatMap((entry) => {
  const url = FILES[`./${entry.file}`];
  return url ? [{ id: String(entry.beyondAvatarId), url }] : [];
});

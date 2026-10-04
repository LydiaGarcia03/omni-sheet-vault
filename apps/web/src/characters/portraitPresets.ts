import { DND5E_PORTRAIT_PRESETS } from '../systems/dnd5e/portraits/dnd5ePortraitPresets';
import type { Portrait } from './portrait';

export type PortraitPreset = { id: string; url: string };

const PRESETS_BY_SYSTEM: Record<string, PortraitPreset[]> = {
  'dnd-5e': DND5E_PORTRAIT_PRESETS,
};

/** The default portraits a game system offers; empty for a system without any. */
export function portraitPresetsFor(systemId: string): PortraitPreset[] {
  return PRESETS_BY_SYSTEM[systemId] ?? [];
}

/** The image to show for a portrait, or null for none (or a preset this build no longer ships). */
export function portraitImageUrl(portrait: Portrait | null | undefined, systemId: string): string | null {
  if (!portrait) {
    return null;
  }
  if (portrait.kind === 'UPLOAD') {
    return portrait.url;
  }
  return portraitPresetsFor(systemId).find((preset) => preset.id === portrait.presetId)?.url ?? null;
}

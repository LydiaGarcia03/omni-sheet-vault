import { describe, expect, it } from 'vitest';
import { portraitImageUrl, portraitPresetsFor } from './portraitPresets';

describe('portraitPresets', () => {
  it('ships the D&D 5e default portraits', () => {
    const presets = portraitPresetsFor('dnd-5e');

    expect(presets).toHaveLength(51);
    expect(presets[0].id).toBe('10064');
  });

  it('offers no presets for a system without any', () => {
    expect(portraitPresetsFor('not-a-system')).toEqual([]);
  });

  it('resolves a preset through its system and an upload through its URL', () => {
    expect(portraitImageUrl({ kind: 'PRESET', presetId: '10064', url: null }, 'dnd-5e')).toBe(portraitPresetsFor('dnd-5e')[0].url);
    expect(portraitImageUrl({ kind: 'UPLOAD', presetId: null, url: 'http://minio/x.png?sig' }, 'dnd-5e')).toBe('http://minio/x.png?sig');
  });

  it('shows nothing for no portrait or a preset this build no longer ships', () => {
    expect(portraitImageUrl(null, 'dnd-5e')).toBeNull();
    expect(portraitImageUrl({ kind: 'PRESET', presetId: '1', url: null }, 'dnd-5e')).toBeNull();
  });
});

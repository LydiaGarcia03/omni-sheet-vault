import { describe, expect, it } from 'vitest';
import { slotDelta } from './daggerheartSheet';

describe('slotDelta', () => {
  it('marks up to an empty slot', () => {
    expect(slotDelta(3, 1)).toBe(3);
  });

  it('clears from a marked slot on', () => {
    expect(slotDelta(1, 4)).toBe(-3);
    expect(slotDelta(0, 1)).toBe(-1);
  });
});

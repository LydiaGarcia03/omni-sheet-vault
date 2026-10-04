import { describe, expect, it } from 'vitest';
import { popupDirection } from './usePopupDirection';

describe('popupDirection', () => {
  it('opens downward when the popup fits below', () => {
    expect(popupDirection(400, 600, 300)).toBe('down');
  });

  it('opens upward when it does not fit below and there is more room above', () => {
    expect(popupDirection(120, 600, 300)).toBe('up');
  });

  it('stays downward when it fits on neither side but below has more room', () => {
    expect(popupDirection(250, 100, 300)).toBe('down');
  });
});

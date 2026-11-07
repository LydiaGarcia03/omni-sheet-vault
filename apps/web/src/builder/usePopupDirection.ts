import { useLayoutEffect, useState, type CSSProperties, type RefObject } from 'react';

export type PopupDirection = 'down' | 'up';

/** Gap kept between the popup and the edge of the window. */
const EDGE_MARGIN = 8;
/** Gap between the anchor and the popup. */
const ANCHOR_GAP = 4;

/** Opens downward when the popup fits below, else toward whichever side has more room. */
export function popupDirection(spaceBelow: number, spaceAbove: number, popupHeight: number): PopupDirection {
  if (popupHeight + EDGE_MARGIN <= spaceBelow) {
    return 'down';
  }
  return spaceAbove > spaceBelow ? 'up' : 'down';
}

export type PopupPlacement = { direction: PopupDirection; style: CSSProperties | undefined };

/**
 * Places an open popup against the window, fixed to its anchor, so a scrolling frame around the
 * anchor can't clip it; it follows the anchor while anything scrolls or the window resizes.
 */
export function usePopupDirection(open: boolean, anchor: RefObject<HTMLElement | null>, popup: RefObject<HTMLElement | null>): PopupPlacement {
  const [placement, setPlacement] = useState<PopupPlacement>({ direction: 'down', style: undefined });

  useLayoutEffect(() => {
    if (!open || !anchor.current || !popup.current) {
      setPlacement({ direction: 'down', style: undefined });
      return;
    }
    const place = () => {
      if (!anchor.current || !popup.current) {
        return;
      }
      const rect = anchor.current.getBoundingClientRect();
      const direction = popupDirection(window.innerHeight - rect.bottom, rect.top, popup.current.offsetHeight);
      const vertical = direction === 'down' ? { top: rect.bottom + ANCHOR_GAP, bottom: 'auto' } : { top: 'auto', bottom: window.innerHeight - rect.top + ANCHOR_GAP };
      setPlacement({ direction, style: { position: 'fixed', left: rect.left, right: 'auto', width: rect.width, ...vertical } });
    };
    place();
    window.addEventListener('scroll', place, true);
    window.addEventListener('resize', place);
    return () => {
      window.removeEventListener('scroll', place, true);
      window.removeEventListener('resize', place);
    };
  }, [open, anchor, popup]);

  return placement;
}

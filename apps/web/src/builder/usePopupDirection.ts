import { useLayoutEffect, useState, type RefObject } from 'react';

export type PopupDirection = 'down' | 'up';

/** Gap kept between the popup and the edge of the window. */
const EDGE_MARGIN = 8;

/** Opens downward when the popup fits below, else toward whichever side has more room. */
export function popupDirection(spaceBelow: number, spaceAbove: number, popupHeight: number): PopupDirection {
  if (popupHeight + EDGE_MARGIN <= spaceBelow) {
    return 'down';
  }
  return spaceAbove > spaceBelow ? 'up' : 'down';
}

/** Measures an open popup against the window and tells which way it should open from its anchor. */
export function usePopupDirection(open: boolean, anchor: RefObject<HTMLElement | null>, popup: RefObject<HTMLElement | null>): PopupDirection {
  const [direction, setDirection] = useState<PopupDirection>('down');

  useLayoutEffect(() => {
    if (!open || !anchor.current || !popup.current) {
      setDirection('down');
      return;
    }
    const rect = anchor.current.getBoundingClientRect();
    setDirection(popupDirection(window.innerHeight - rect.bottom, rect.top, popup.current.offsetHeight));
  }, [open, anchor, popup]);

  return direction;
}

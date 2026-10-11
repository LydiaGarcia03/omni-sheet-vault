import { useEffect, type RefObject } from 'react';

/** A wheel event this long after the previous one starts a new scroll gesture. */
const NEW_GESTURE_MS = 300;

/**
 * Stops a scroll that would reach the footer at the end of the page's content; a new scroll
 * gesture then reveals it. Once the footer is out of view again, the stop is armed again.
 * Wheel scrolling only.
 */
export function useFooterScrollStop(footer: RefObject<HTMLElement | null>) {
  useEffect(() => {
    let armed = true;
    let stoppedAt = 0;
    let lastWheel = 0;

    const rearmWhenHidden = () => {
      const element = footer.current;
      if (element && element.getBoundingClientRect().top > window.innerHeight + 2) {
        armed = true;
        stoppedAt = 0;
      }
    };

    const onWheel = (event: WheelEvent) => {
      const element = footer.current;
      const now = event.timeStamp;
      const newGesture = now - lastWheel > NEW_GESTURE_MS;
      lastWheel = now;
      if (!element || event.deltaY <= 0 || !armed) {
        return;
      }
      const footerTop = element.getBoundingClientRect().top;
      const viewportBottom = window.innerHeight;
      if (footerTop - event.deltaY >= viewportBottom) {
        return;
      }
      if (stoppedAt && newGesture) {
        armed = false;
        return;
      }
      event.preventDefault();
      window.scrollBy(0, Math.max(0, footerTop - viewportBottom));
      stoppedAt = now;
    };

    window.addEventListener('wheel', onWheel, { passive: false });
    window.addEventListener('scroll', rearmWhenHidden, { passive: true });
    return () => {
      window.removeEventListener('wheel', onWheel);
      window.removeEventListener('scroll', rearmWhenHidden);
    };
  }, [footer]);
}

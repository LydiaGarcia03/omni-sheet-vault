import { useCallback, useEffect, useId, useLayoutEffect, useRef, useState, type CSSProperties, type RefObject } from 'react';
import { createPortal } from 'react-dom';
import { useShellText } from '../locales/platformMessages';
import type { TutorialDefinition } from './tutorialDefinition';
import './tutorial.css';

/** Room left around the spotlit part. */
const SPOT_PADDING = 6;
/** Gap between the spotlit part and the bubble. */
const BUBBLE_GAP = 12;
const BUBBLE_WIDTH = 360;
/** The bubble's assumed height until its first render is measured. */
const BUBBLE_HEIGHT_GUESS = 260;
/** Space kept between the bubble and the window's edges. */
const EDGE_MARGIN = 16;
/** How long a step waits for its target to appear after a page change. */
const TARGET_WAIT_MS = 1500;
/** A target taller than this share of the window scrolls to its top instead of its middle. */
const TALL_TARGET_SHARE = 0.8;

type Rect = { top: number; left: number; width: number; height: number };

type TutorialProps = {
  definition: TutorialDefinition;
  /** Opens a builder step before a step that lives on it. */
  onNavigate: (builderStep: string) => void;
  /** Called on Skip or Escape: the tour counts as seen either way. */
  onClose: () => void;
  /** Called on Finish, after the last step; falls back to onClose. */
  onFinish?: () => void;
};

/** A guided tour over the page: dims everything but one part, and explains it in a bubble with Back, Next and Skip. */
export function Tutorial({ definition, onNavigate, onClose, onFinish = onClose }: TutorialProps) {
  const t = useShellText();
  const [index, setIndex] = useState(0);
  const step = definition.steps[index];
  const last = index === definition.steps.length - 1;
  const rect = useTargetRect(step.target, index);
  const bubble = useRef<HTMLDivElement>(null);
  const bubbleHeight = useBubbleHeight(bubble, index);
  const titleId = useId();

  useEffect(() => {
    const offset = scrollToFitBubble(rect, bubbleHeight);
    if (offset) {
      window.scrollBy({ top: offset });
    }
  }, [rect, bubbleHeight]);

  useEffect(() => {
    if (step.builderStep) {
      onNavigate(step.builderStep);
    }
  }, [step.builderStep, onNavigate]);

  useEffect(() => {
    bubble.current?.focus();
  }, [index]);

  const move = useCallback(
    (delta: number) => setIndex((current) => Math.max(0, Math.min(definition.steps.length - 1, current + delta))),
    [definition.steps.length],
  );

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      } else if (event.key === 'ArrowRight') {
        move(1);
      } else if (event.key === 'ArrowLeft') {
        move(-1);
      }
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [move, onClose]);

  return createPortal(
    <div className={`tutorial ${definition.className}`}>
      <div className="tutorial__blocker" />
      {rect ? <Spotlight rect={rect} /> : <div className="tutorial__shade" />}
      <div ref={bubble} className="tutorial__bubble" role="dialog" aria-modal="true" aria-labelledby={titleId} tabIndex={-1} style={bubbleStyle(rect, bubbleHeight)}>
        <div className="tutorial__count">
          {t('tutorial.count', { step: index + 1, total: definition.steps.length })}
        </div>
        <h2 id={titleId} className="tutorial__title">
          {step.title}
        </h2>
        <div className="tutorial__body">{step.body}</div>
        <div className="tutorial__actions">
          {!last && (
            <button type="button" className="tutorial__skip" onClick={onClose}>
              {t('tutorial.skip')}
            </button>
          )}
          <span className="tutorial__grow" />
          {index > 0 && (
            <button type="button" className="tutorial__btn" onClick={() => move(-1)}>
              {t('tutorial.back')}
            </button>
          )}
          <button type="button" className="tutorial__btn tutorial__btn--primary" onClick={() => (last ? onFinish() : move(1))}>
            {last ? t('tutorial.finish') : t('tutorial.next')}
          </button>
        </div>
      </div>
    </div>,
    document.body,
  );
}

/** Four shade panels around a clear hole plus a ring, all placed with transforms so following a scroll costs no layout. */
function Spotlight({ rect }: { rect: Rect }) {
  const top = Math.floor(rect.top - SPOT_PADDING);
  const left = Math.floor(rect.left - SPOT_PADDING);
  const width = Math.ceil(rect.width + SPOT_PADDING * 2);
  const height = Math.ceil(rect.height + SPOT_PADDING * 2);
  const panel = (x: number, y: number, w: number, h: number): CSSProperties => ({
    width: Math.max(0, w),
    height: Math.max(0, h),
    transform: `translate(${x}px, ${y}px)`,
  });
  return (
    <>
      <div className="tutorial__panel" style={panel(0, 0, window.innerWidth, top)} />
      <div className="tutorial__panel" style={panel(0, top + height, window.innerWidth, window.innerHeight - top - height)} />
      <div className="tutorial__panel" style={panel(0, top, left, height)} />
      <div className="tutorial__panel" style={panel(left + width, top, window.innerWidth - left - width, height)} />
      <div className="tutorial__ring" style={{ width, height, transform: `translate(${left}px, ${top}px)` }} />
    </>
  );
}

/**
 * The target's place on screen, scrolled into view first and followed through scrolls and resizes; null while it
 * isn't on the page. The search starts two frames late so a builder step opened for this step has rendered.
 */
function useTargetRect(selector: string | undefined, stepIndex: number): Rect | null {
  const [rect, setRect] = useState<Rect | null>(null);

  useLayoutEffect(() => {
    setRect(null);
    if (!selector) {
      return;
    }
    let element: Element | null = null;
    let frame = 0;
    let pending = 0;
    const started = performance.now();
    const measureNow = () => {
      pending = 0;
      if (!element?.isConnected) {
        element = document.querySelector(selector);
        if (!element) {
          setRect(null);
          return;
        }
      }
      const box = element.getBoundingClientRect();
      setRect((current) =>
        current && current.top === box.top && current.left === box.left && current.width === box.width && current.height === box.height
          ? current
          : { top: box.top, left: box.left, width: box.width, height: box.height },
      );
    };
    const measure = () => {
      if (!pending) {
        pending = requestAnimationFrame(measureNow);
      }
    };
    const find = () => {
      element = document.querySelector(selector);
      if (element) {
        const tall = element.getBoundingClientRect().height > window.innerHeight * TALL_TARGET_SHARE;
        element.scrollIntoView?.({ block: tall ? 'start' : 'center' });
        measureNow();
      } else if (performance.now() - started < TARGET_WAIT_MS) {
        frame = requestAnimationFrame(find);
      }
    };
    frame = requestAnimationFrame(() => {
      frame = requestAnimationFrame(find);
    });
    window.addEventListener('scroll', measure, true);
    window.addEventListener('resize', measure);
    return () => {
      cancelAnimationFrame(frame);
      cancelAnimationFrame(pending);
      window.removeEventListener('scroll', measure, true);
      window.removeEventListener('resize', measure);
    };
  }, [selector, stepIndex]);

  return rect;
}

/** The bubble's rendered height, measured again whenever the step changes. */
function useBubbleHeight(bubble: RefObject<HTMLDivElement | null>, stepIndex: number): number {
  const [height, setHeight] = useState(BUBBLE_HEIGHT_GUESS);

  useLayoutEffect(() => {
    const measured = bubble.current?.offsetHeight;
    if (measured) {
      setHeight(measured);
    }
  }, [bubble, stepIndex]);

  return height;
}

/** How far to scroll so the spotlit part sits at the top with the bubble below it; 0 when it already fits or never can. */
function scrollToFitBubble(rect: Rect | null, height: number): number {
  if (!rect) {
    return 0;
  }
  const width = Math.min(BUBBLE_WIDTH, window.innerWidth - EDGE_MARGIN * 2);
  const fitsBeside =
    rect.left + rect.width + SPOT_PADDING + BUBBLE_GAP + width + EDGE_MARGIN <= window.innerWidth ||
    rect.left - SPOT_PADDING - BUBBLE_GAP - width >= EDGE_MARGIN;
  const roomBelow = window.innerHeight - (rect.top + rect.height + SPOT_PADDING + BUBBLE_GAP) - EDGE_MARGIN;
  const roomAbove = rect.top - SPOT_PADDING - BUBBLE_GAP - EDGE_MARGIN;
  const needed = rect.height + SPOT_PADDING * 2 + BUBBLE_GAP + height + EDGE_MARGIN * 2;
  if (fitsBeside || height <= roomBelow || height <= roomAbove || needed > window.innerHeight) {
    return 0;
  }
  return Math.round(rect.top - SPOT_PADDING - EDGE_MARGIN);
}

/** Beside the spotlit part where the whole bubble fits (right, left, below, above), else kept inside the window. */
function bubbleStyle(rect: Rect | null, height: number): CSSProperties {
  const width = Math.min(BUBBLE_WIDTH, window.innerWidth - EDGE_MARGIN * 2);
  if (!rect) {
    return { width, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };
  }
  const clampTop = (top: number) => Math.max(EDGE_MARGIN, Math.min(top, window.innerHeight - height - EDGE_MARGIN));
  const clampLeft = (left: number) => Math.max(EDGE_MARGIN, Math.min(left, window.innerWidth - width - EDGE_MARGIN));
  const right = rect.left + rect.width + SPOT_PADDING + BUBBLE_GAP;
  if (right + width + EDGE_MARGIN <= window.innerWidth) {
    return { width, top: clampTop(rect.top), left: right };
  }
  const left = rect.left - SPOT_PADDING - BUBBLE_GAP - width;
  if (left >= EDGE_MARGIN) {
    return { width, top: clampTop(rect.top), left };
  }
  const below = rect.top + rect.height + SPOT_PADDING + BUBBLE_GAP;
  const roomBelow = window.innerHeight - below - EDGE_MARGIN;
  const roomAbove = rect.top - SPOT_PADDING - BUBBLE_GAP - EDGE_MARGIN;
  if (height <= roomBelow) {
    return { width, top: below, left: clampLeft(rect.left) };
  }
  if (height <= roomAbove) {
    return { width, top: rect.top - SPOT_PADDING - BUBBLE_GAP - height, left: clampLeft(rect.left) };
  }
  return { width, top: clampTop(roomBelow >= roomAbove ? below : rect.top - SPOT_PADDING - BUBBLE_GAP - height), left: clampLeft(rect.left) };
}

import { useCallback, useEffect, useId, useLayoutEffect, useRef, useState, type CSSProperties } from 'react';
import { createPortal } from 'react-dom';
import type { TutorialDefinition } from './tutorialDefinition';
import './tutorial.css';

/** Room left around the spotlit part. */
const SPOT_PADDING = 6;
/** Gap between the spotlit part and the bubble. */
const BUBBLE_GAP = 12;
const BUBBLE_WIDTH = 360;
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
  const [index, setIndex] = useState(0);
  const step = definition.steps[index];
  const last = index === definition.steps.length - 1;
  const rect = useTargetRect(step.target, index);
  const bubble = useRef<HTMLDivElement>(null);
  const titleId = useId();

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
      <div ref={bubble} className="tutorial__bubble" role="dialog" aria-modal="true" aria-labelledby={titleId} tabIndex={-1} style={bubbleStyle(rect)}>
        <div className="tutorial__count">
          {index + 1} of {definition.steps.length}
        </div>
        <h2 id={titleId} className="tutorial__title">
          {step.title}
        </h2>
        <div className="tutorial__body">{step.body}</div>
        <div className="tutorial__actions">
          {!last && (
            <button type="button" className="tutorial__skip" onClick={onClose}>
              Skip tutorial
            </button>
          )}
          <span className="tutorial__grow" />
          {index > 0 && (
            <button type="button" className="tutorial__btn" onClick={() => move(-1)}>
              Back
            </button>
          )}
          <button type="button" className="tutorial__btn tutorial__btn--primary" onClick={() => (last ? onFinish() : move(1))}>
            {last ? 'Finish' : 'Next'}
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

/** Beside the spotlit part where there's room (right, left, below, above), else centred on screen. */
function bubbleStyle(rect: Rect | null): CSSProperties {
  const width = Math.min(BUBBLE_WIDTH, window.innerWidth - 32);
  if (!rect) {
    return { width, top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };
  }
  const clampTop = (top: number) => Math.max(16, Math.min(top, window.innerHeight - 260));
  const clampLeft = (left: number) => Math.max(16, Math.min(left, window.innerWidth - width - 16));
  const right = rect.left + rect.width + SPOT_PADDING + BUBBLE_GAP;
  if (right + width + 16 <= window.innerWidth) {
    return { width, top: clampTop(rect.top), left: right };
  }
  const left = rect.left - SPOT_PADDING - BUBBLE_GAP - width;
  if (left >= 16) {
    return { width, top: clampTop(rect.top), left };
  }
  const below = rect.top + rect.height + SPOT_PADDING + BUBBLE_GAP;
  if (below + 220 <= window.innerHeight) {
    return { width, top: below, left: clampLeft(rect.left) };
  }
  return { width, bottom: window.innerHeight - rect.top + SPOT_PADDING + BUBBLE_GAP, left: clampLeft(rect.left) };
}

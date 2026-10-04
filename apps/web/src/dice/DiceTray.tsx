import { useEffect, useState } from 'react';
import { FrameIcon } from '../systems/dnd5e/FrameIcon';
import closeIconSvg from '../systems/dnd5e/frames/dnd_icon_close.svg?raw';
import type { RollResult } from './api';

type DiceTrayProps = {
  latestRoll: RollResult | null;
  error: string | null;
};

const AUTO_DISMISS_SECONDS = 30;

/**
 * Anchored top left — ui-design-system.md. No frame asset (component inventory:
 * "Not built") and no 3D rendering (out of scope for phase 5), so this is just the
 * result log's most recent entry, visible only while there's a roll or error to show
 * (renders nothing otherwise — direct owner report, 2026-09-20: an empty "No rolls
 * yet" placeholder sitting on the sheet at all times was unwanted).
 *
 * On failure this shows the error, never a fabricated result — a roll that doesn't
 * reach the server is not a roll, per ground-rules.md's Dice section.
 *
 * **Direct owner request, 2026-08-18, revised same day:** the tray now dismisses
 * itself automatically after 30s, replacing the click-to-dismiss version from
 * earlier the same day (kept in changelog.md) — the owner reasoned the *default*
 * should be "it goes away on its own" rather than "it sits there until clicked".
 * `latestRoll` is `sessionLatestRoll` from the parent (`CharacterSheetScreen`), not
 * `rollHistory[0]` — see that field's own doc comment for why: this stays purely
 * local, presentational state; the countdown finishing never touches `rollHistory`
 * or calls anything server-side, and the roll stays in the actual log exactly as
 * before. Keyed to the current entry's own identity (`latestRoll.id`, or the
 * error string itself, since an error has no id) rather than a plain boolean, so
 * the next roll or error — a different key — always starts a fresh, undismissed
 * 30s countdown regardless of what timed out before it.
 *
 * The countdown itself is a CSS animation (`.dice-tray__progress`, the thin bar
 * along the tray's own bottom edge), not a `setInterval`/`requestAnimationFrame`
 * loop — `animation-play-state: paused`/`running` already does exactly the
 * pause-and-resume-from-where-it-left-off this needs, natively and without a
 * render-per-frame cost, and `onAnimationEnd` is the dismissal trigger. Pausing
 * is two independent conditions, either one holding the countdown: the mouse
 * resting on the tray (`onMouseEnter`/`onMouseLeave` — this is *not* a `.roll`/
 * `.mutate` control, so it doesn't need those files' own hover conventions), or
 * the window itself not focused (`focus`/`blur` — switching tabs or apps
 * shouldn't burn down a countdown the owner isn't there to read). The progress
 * bar element carries `key={dismissKey}`, not the tray itself, so a fresh roll
 * remounts *just the bar* (restarting its animation cleanly) without
 * remounting the tray and losing its hover state mid-gesture.
 *
 * **Direct owner report, 2026-09-20:** reloading the page, or just reopening a
 * character's sheet, used to resurrect the *last roll ever made on that
 * character* (server-persisted `rollHistory[0]`) as if it had just happened,
 * countdown and all — a stopgap from 2026-09-03 mirrored dismissal into
 * `localStorage` to at least stop re-showing a roll the owner had already
 * watched dismiss once, but a reload before that 30s finished still resurrected
 * it. Fixed at the source instead of patching the symptom further: the parent
 * now tracks `sessionLatestRoll` separately from the persisted `rollHistory`
 * (`CharacterSheetScreen.tsx`'s own doc comment on that field), set only by a
 * roll made during the page's own lifetime — a fresh load or reload has none,
 * so there is nothing to resurrect and `dismissedKey` can go back to being
 * plain in-memory state. Same owner report also asked for a manual close
 * control instead of only the 30s wait — `dice-tray__close`, top-right,
 * `dnd_icon_close.svg` (already used by `CustomRollPicker.tsx`'s own close
 * button, same icon/precedent), dismissing immediately on click exactly as the
 * countdown finishing already does.
 *
 * **2026-09-04:** a roll with no dice at all (Unarmed Strike's flat
 * ability-modifier damage — `RollService` renders its own expression as
 * just the modifier, `results` empty) no longer shows a bare, contentless
 * `()` after the expression — the parenthesized results list only renders
 * when there's at least one die to show.
 */
export function DiceTray({ latestRoll, error }: DiceTrayProps) {
  const dismissKey = error ?? latestRoll?.id ?? null;
  const [dismissedKey, setDismissedKey] = useState<string | null>(null);
  const [isHovered, setIsHovered] = useState(false);
  const [isFocused, setIsFocused] = useState(() => document.hasFocus());
  const isDismissed = dismissKey !== null && dismissKey === dismissedKey;
  const isPaused = isHovered || !isFocused;

  useEffect(() => {
    function handleFocus() {
      setIsFocused(true);
    }
    function handleBlur() {
      setIsFocused(false);
    }
    window.addEventListener('focus', handleFocus);
    window.addEventListener('blur', handleBlur);
    return () => {
      window.removeEventListener('focus', handleFocus);
      window.removeEventListener('blur', handleBlur);
    };
  }, []);

  if (dismissKey === null || isDismissed) {
    return null;
  }

  return (
    <div className="dice-tray" onMouseEnter={() => setIsHovered(true)} onMouseLeave={() => setIsHovered(false)}>
      {error ? (
        <div className="dice-tray__error" role="alert">
          {error}
        </div>
      ) : (
        latestRoll && (
          <>
            <div className="dice-tray__context">{latestRoll.context}</div>
            <div className="dice-tray__total">{latestRoll.total}</div>
            <div className="dice-tray__expression">
              {latestRoll.expression}
              {latestRoll.results.length > 0 && ` (${latestRoll.results.join(', ')})`}
            </div>
          </>
        )
      )}
      <button
        type="button"
        className="dice-tray__close"
        aria-label="Dismiss"
        onClick={() => setDismissedKey(dismissKey)}
      >
        <FrameIcon className="dice-tray__close-icon" svg={closeIconSvg} aria-hidden />
      </button>
      <div
        key={dismissKey}
        className={`dice-tray__progress${isPaused ? ' dice-tray__progress--paused' : ''}`}
        style={{ animationDuration: `${AUTO_DISMISS_SECONDS}s` }}
        onAnimationEnd={() => setDismissedKey(dismissKey)}
        aria-hidden="true"
      />
    </div>
  );
}

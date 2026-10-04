import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame_proficiency_bonus_and_speed.svg?raw';
import inkSvg from './frames/dnd_frame_proficiency_bonus_and_speed_ink.svg?raw';

type StatBadgeProps = {
  heading: string;
  /** An extra class for the heading, e.g. `text-bold-uppercase`. */
  headingClassName?: string;
  value: string;
  caption: string;
  ariaLabel: string;
  onOpenExplainer: () => void;
};

/**
 * One frame serves proficiency bonus and speed alike — see frame-kit.html. Both
 * open the explainer sidebar per the trigger table in systems/dnd-5e/sheet-ui.md.
 */
export function StatBadge({ heading, headingClassName, value, caption, ariaLabel, onOpenExplainer }: StatBadgeProps) {
  return (
    <div className="frame-box badge">
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className={headingClassName ? `badge__heading ${headingClassName}` : 'badge__heading'}>{heading}</div>
      <button type="button" className="reveal badge__value" aria-label={ariaLabel} onClick={onOpenExplainer}>
        {value}
      </button>
      <div className="badge__caption">{caption}</div>
    </div>
  );
}

import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame_inspiration.svg?raw';
import inkSvg from './frames/dnd_frame_inspiration_ink.svg?raw';
import dotOnSvg from './frames/dnd_icon_inspiration_on.svg?raw';

type HeroicInspirationProps = {
  inspired: boolean;
  onToggle: () => void;
};

/**
 * frames/dnd_frame_inspiration.svg — a filled dot when inspired, same visual
 * language as the skill and condition dots. A mutate target, not a roll
 * target: toggling changes state and persists it, so it uses the `.mutate`
 * press affordance rather than `.roll`'s hover highlight.
 *
 * **Direct owner request, 2026-08-17:** the not-inspired state shows nothing
 * at all — no dot, hollow or otherwise — rather than an empty ring. The dot
 * element itself is only rendered when `inspired` is true, instead of always
 * rendering it and letting CSS hide its border via a `--on` modifier.
 */
export function HeroicInspiration({ inspired, onToggle }: HeroicInspirationProps) {
  return (
    <div className="inspiration-wrap">
      <button
        type="button"
        className="mutate frame-box inspiration"
        aria-pressed={inspired}
        aria-label={`Heroic inspiration: ${inspired ? 'inspired' : 'not inspired'}`}
        onClick={onToggle}
      >
        <FrameLayer svg={paperSvg} layer="paper" />
        <FrameLayer svg={inkSvg} layer="ink" />
        {inspired && (
          <span className="inspiration__dot" aria-hidden="true">
            <FrameLayer svg={dotOnSvg} layer="paper" />
            <FrameLayer svg={dotOnSvg} layer="ink" />
          </span>
        )}
      </button>
      <div className="inspiration-wrap__caption">Heroic Inspiration</div>
    </div>
  );
}

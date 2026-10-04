import type { KeyboardEvent, MouseEvent } from 'react';
import type { DeathSaves } from '../../sheet/api';
import { FrameIcon } from './FrameIcon';
import { FrameLayer } from './FrameLayer';
import skullSvg from './frames/dnd_icon_condition_unconscious.svg?raw';
import paperSvg from './frames/dnd_frame_hit_points.svg?raw';
import inkSvg from './frames/dnd_frame_hit_points_ink.svg?raw';

const MARKS = 3;

type DeathSavesBoxProps = {
  saves: DeathSaves;
  onSet: (successes: number, failures: number) => void;
  onRoll: () => void;
  onOpenManagement: () => void;
};

/** Clicking the nth circle fills up to it; clicking the last filled one empties it. */
function countAfterClick(count: number, index: number): number {
  return index + 1 === count ? index : index + 1;
}

/**
 * Replaces the hit points box at 0 hit points, in its frame and at its size: a skull, the
 * failure and success circles, and "Death Saves" below. The skull rolls the save; the rest of
 * the box opens HP management, where healing brings the hit points box back.
 */
export function DeathSavesBox({ saves, onSet, onRoll, onOpenManagement }: DeathSavesBoxProps) {
  const canRoll = !saves.stable && !saves.dead;

  const stop = (event: MouseEvent) => event.stopPropagation();
  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.target === event.currentTarget && (event.key === 'Enter' || event.key === ' ')) {
      event.preventDefault();
      onOpenManagement();
    }
  };

  const row = (label: string, kind: 'fail' | 'success', count: number) => (
    <div className="death-saves__group">
      <h2 className="death-saves__label">{label}</h2>
      <span className="death-saves__marks">
        {Array.from({ length: MARKS }, (_, index) => (
          <button
            key={index}
            type="button"
            className={`mutate death-saves__mark${index < count ? ` death-saves__mark--${kind}` : ''}`}
            aria-label={`${label} ${index + 1}${index < count ? ', marked' : ''}`}
            onClick={(event) => {
              stop(event);
              const next = countAfterClick(count, index);
              onSet(kind === 'success' ? next : saves.successes, kind === 'fail' ? next : saves.failures);
            }}
          />
        ))}
      </span>
    </div>
  );

  return (
    <div
      className="frame-box hit-points death-saves"
      role="button"
      tabIndex={0}
      onClick={onOpenManagement}
      onKeyDown={handleKeyDown}
      aria-label={`Death saves: ${saves.successes} successes, ${saves.failures} failures${saves.stable ? ', stable' : ''}${saves.dead ? ', dead' : ''}. Open HP management`}
    >
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <button
        type="button"
        className="mutate death-saves__icon"
        disabled={!canRoll}
        title={canRoll ? 'Roll a death saving throw' : undefined}
        aria-label="Roll a death saving throw"
        onClick={(event) => {
          stop(event);
          onRoll();
        }}
      >
        <FrameIcon svg={skullSvg} className="death-saves__skull" aria-hidden />
      </button>
      <div className="death-saves__rows">
        {row('Failure', 'fail', saves.failures)}
        {row('Success', 'success', saves.successes)}
      </div>
      <div className="death-saves__title">Death Saves</div>
    </div>
  );
}

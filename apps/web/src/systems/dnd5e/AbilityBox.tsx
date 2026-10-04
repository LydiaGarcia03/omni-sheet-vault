import type { KeyboardEvent, MouseEvent } from 'react';
import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame.svg?raw';
import inkSvg from './frames/dnd_frame_ink.svg?raw';

type AbilityBoxProps = {
  name: string;
  score: number;
  modifier: number;
  onRoll: () => void;
  onRollContextMenu: (x: number, y: number) => void;
  onOpenExplainer: () => void;
};

function formatModifier(modifier: number): string {
  return modifier >= 0 ? `+${modifier}` : `−${Math.abs(modifier)}`;
}

/**
 * DOM-measured against D&D Beyond's own AbilitySummary.tsx: the whole box is
 * a plain div with its own onClick opening the sidebar — not a button, and
 * not just the score. The roll button (a real nested interactive target)
 * stops propagation so rolling doesn't also open the sidebar; `.ability__score`
 * itself is inert, matching D&D Beyond's own `.ddbc-ability-summary__secondary`
 * (a bare div, no button semantics there either).
 */
export function AbilityBox({ name, score, modifier, onRoll, onRollContextMenu, onOpenExplainer }: AbilityBoxProps) {
  const formattedModifier = formatModifier(modifier);

  const handleKeyDown = (event: KeyboardEvent) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onOpenExplainer();
    }
  };

  return (
    <div
      className="frame-box ability"
      role="button"
      tabIndex={0}
      aria-label={`${name} score ${score}, open details`}
      onClick={onOpenExplainer}
      onKeyDown={handleKeyDown}
    >
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className="ability__label">{name}</div>
      <div className="ability__modifier">
        <button
          type="button"
          className="roll roll--modifier ability__roll"
          aria-label={`Roll ${name} check, ${formattedModifier}`}
          onClick={(event: MouseEvent) => {
            event.stopPropagation();
            onRoll();
          }}
          onContextMenu={(event) => {
            event.preventDefault();
            event.stopPropagation();
            onRollContextMenu(event.clientX, event.clientY);
          }}
        >
          {formattedModifier}
        </button>
      </div>
      <div className="ability__score" aria-hidden="true">
        {score}
      </div>
    </div>
  );
}

import type { ForcedRollMode } from '../../sheet/api';
import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame_initiative.svg?raw';
import inkSvg from './frames/dnd_frame_initiative_ink.svg?raw';
import { RollModeMarker } from './RollModeMarker';

type InitiativeProps = {
  modifier: number;
  rollMode?: ForcedRollMode;
  onRoll: () => void;
  onRollContextMenu: (x: number, y: number) => void;
  /** The caption opens the Initiative pane; the value rolls. */
  onOpenPane: () => void;
};

function formatModifier(modifier: number): string {
  return modifier >= 0 ? `+${modifier}` : `−${Math.abs(modifier)}`;
}

export function Initiative({ modifier, rollMode, onRoll, onRollContextMenu, onOpenPane }: InitiativeProps) {
  const formattedModifier = formatModifier(modifier);

  return (
    <div className="initiative-wrap">
      <button type="button" className="reveal initiative-wrap__caption" aria-label="Initiative, open details" onClick={onOpenPane}>
        Initiative
      </button>
      <div className="frame-box initiative">
        <FrameLayer svg={paperSvg} layer="paper" />
        <FrameLayer svg={inkSvg} layer="ink" />
        <button
          type="button"
          className="roll roll--modifier initiative__value"
          aria-label={`Roll initiative, ${formattedModifier}`}
          onClick={onRoll}
          onContextMenu={(event) => {
            event.preventDefault();
            onRollContextMenu(event.clientX, event.clientY);
          }}
        >
          {formattedModifier}
        </button>
        <RollModeMarker rollMode={rollMode} className="initiative__roll-mode" />
      </div>
    </div>
  );
}

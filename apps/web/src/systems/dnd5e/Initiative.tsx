import type { ForcedRollMode } from '../../sheet/api';
import { FrameLayer } from './FrameLayer';
import { useDnd5eText } from './locales/dnd5eMessages';
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
  const t = useDnd5eText();
  const formattedModifier = formatModifier(modifier);

  return (
    <div className="initiative-wrap">
      <button type="button" className="reveal initiative-wrap__caption" aria-label={t('vitals.initiativeAria')} onClick={onOpenPane}>
        {t('terms.labels.initiative')}
      </button>
      <div className="frame-box initiative">
        <FrameLayer svg={paperSvg} layer="paper" />
        <FrameLayer svg={inkSvg} layer="ink" />
        <button
          type="button"
          className="roll roll--modifier initiative__value"
          aria-label={t('vitals.initiativeRoll', { modifier: formattedModifier })}
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

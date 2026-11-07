import { FrameLayer } from './FrameLayer';
import { useDnd5eText } from './locales/dnd5eMessages';
import frameSvg from './frames/dnd_frame_saving_throw_attribute.svg?raw';
import type { ProficiencyLevel } from '../../sheet/api';
import { ProficiencyDot } from './ProficiencyDot';

type SavingThrowRowProps = {
  abbreviation: string;
  fullName: string;
  proficiency: ProficiencyLevel;
  modifier: number;
  onRoll: () => void;
  onRollContextMenu: (x: number, y: number) => void;
  /** Opens the save's own pane from its abbreviation. */
  onOpenDetail?: () => void;
};

function formatModifier(modifier: number): string {
  return modifier >= 0 ? `+${modifier}` : `−${Math.abs(modifier)}`;
}

export function SavingThrowRow({ abbreviation, fullName, proficiency, modifier, onRoll, onRollContextMenu, onOpenDetail }: SavingThrowRowProps) {
  const t = useDnd5eText();
  const formattedModifier = formatModifier(modifier);

  return (
    <div className="frame-box saving">
      <FrameLayer svg={frameSvg} layer="paper" />
      <FrameLayer svg={frameSvg} layer="ink" />
      <ProficiencyDot level={proficiency} className="saving__prof" />
      {onOpenDetail ? (
        <button type="button" className="reveal saving__abbr" aria-label={t('saves.detailsAria', { name: fullName })} onClick={onOpenDetail}>
          {abbreviation}
        </button>
      ) : (
        <span className="saving__abbr">{abbreviation}</span>
      )}
      <button
        type="button"
        className="roll roll--modifier saving__mod"
        aria-label={t('saves.rollAria', { name: fullName, modifier: formattedModifier })}
        onClick={onRoll}
        onContextMenu={(event) => {
          event.preventDefault();
          onRollContextMenu(event.clientX, event.clientY);
        }}
      >
        {formattedModifier}
      </button>
    </div>
  );
}

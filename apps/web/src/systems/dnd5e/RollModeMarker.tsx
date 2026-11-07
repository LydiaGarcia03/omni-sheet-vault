import type { ForcedRollMode } from '../../sheet/api';
import { FrameIcon } from './FrameIcon';
import { useDnd5eText } from './locales/dnd5eMessages';
import advantageSvg from './frames/dnd_icon_advantage.svg?raw';
import disadvantageSvg from './frames/dnd_icon_disadvantage.svg?raw';

type RollModeMarkerProps = {
  rollMode: ForcedRollMode | undefined;
  className: string;
};

/** The advantage/disadvantage badge on a roll the sheet forces a mode on; the tooltip names every source. */
export function RollModeMarker({ rollMode, className }: RollModeMarkerProps) {
  const t = useDnd5eText();
  if (!rollMode || rollMode.mode === 'NORMAL') {
    return null;
  }
  const advantage = rollMode.mode === 'ADVANTAGE';
  const sources = advantage ? rollMode.advantageSources : rollMode.disadvantageSources;
  const label = t(advantage ? 'panel.advantage' : 'panel.disadvantage', { sources: sources.join(', ') });
  return (
    <FrameIcon
      svg={advantage ? advantageSvg : disadvantageSvg}
      className={`roll-mode-marker ${className}`}
      title={label}
      aria-label={label}
    />
  );
}

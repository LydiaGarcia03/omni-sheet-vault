import { FrameLayer } from './FrameLayer';
import frameSvg from './frames/dnd_frame_sense_item.svg?raw';

type SenseRowProps = {
  value: number;
  label: string;
};

/**
 * D&D Beyond puts the value first, in the frame's round bulge, then the label
 * in the bar — the mirror of every other row we've built (skill, saving
 * throw), which puts the badge last. Uses dnd_frame_senses.svg (the owner's
 * asset, matching the reference's own pill-with-bulge shape) as the row's own
 * frame-box rather than a plain CSS badge. Senses have no roll or reveal
 * target yet (reveal is listed for them in systems/dnd-5e/sheet-ui.md's sidebar
 * trigger table, but the sidebar is phase 8), so this is inert display only.
 */
export function SenseRow({ value, label }: SenseRowProps) {
  return (
    <div className="frame-box sense-row">
      <FrameLayer svg={frameSvg} layer="paper" />
      <FrameLayer svg={frameSvg} layer="ink" />
      <div className="sense-row__value">{value}</div>
      <div className="sense-row__label">{label}</div>
    </div>
  );
}

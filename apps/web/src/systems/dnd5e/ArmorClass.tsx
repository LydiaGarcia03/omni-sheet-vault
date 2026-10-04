import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame_armor_class.svg?raw';
import inkSvg from './frames/dnd_frame_armor_class_ink.svg?raw';

type ArmorClassProps = {
  value: number;
  onOpenExplainer: () => void;
};

/**
 * Opens the explainer, per systems/dnd-5e/sheet-ui.md's sidebar trigger table — a reveal
 * target, not a roll target, unlike initiative.
 */
export function ArmorClass({ value, onOpenExplainer }: ArmorClassProps) {
  return (
    <div className="frame-box armor">
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className="armor__heading">Armor</div>
      <button
        type="button"
        className="reveal armor__value"
        aria-label={`Armor class ${value}, open details`}
        onClick={onOpenExplainer}
      >
        {value}
      </button>
      <div className="armor__caption">Class</div>
    </div>
  );
}

import { FrameLayer } from './FrameLayer';
import { useDnd5eText } from './locales/dnd5eMessages';
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
  const t = useDnd5eText();
  return (
    <div className="frame-box armor">
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className="armor__heading">{t('terms.labels.armorClassTop')}</div>
      <button
        type="button"
        className="reveal armor__value"
        aria-label={t('vitals.armorAria', { value })}
        onClick={onOpenExplainer}
      >
        {value}
      </button>
      <div className="armor__caption">{t('terms.labels.armorClassBottom')}</div>
    </div>
  );
}

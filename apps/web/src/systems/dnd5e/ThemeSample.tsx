import type { CSSProperties } from 'react';
import { FrameLayer } from './FrameLayer';
import paperSvg from './frames/dnd_frame.svg?raw';
import inkSvg from './frames/dnd_frame_ink.svg?raw';
import { useDnd5eTerms } from './locales/dnd5eMessages';

/** A sample ability box ("Dexterity +5 / 20") drawn in a theme's color, as D&D Beyond's theme tiles show. */
export function ThemeSample({ color }: { color: string }) {
  const terms = useDnd5eTerms();
  return (
    <div className="frame-box ability theme-sample" style={{ '--frame-ink': color } as CSSProperties} aria-hidden="true">
      <FrameLayer svg={paperSvg} layer="paper" />
      <FrameLayer svg={inkSvg} layer="ink" />
      <div className="ability__label">{terms.ability('dexterity')}</div>
      <div className="ability__modifier">
        <span className="theme-sample__modifier">+5</span>
      </div>
      <div className="ability__score">20</div>
    </div>
  );
}

import type { ReactNode } from 'react';
import { FrameLayer } from './FrameLayer';
import { FrameIcon } from './FrameIcon';
import settingsSvg from './frames/dnd_icon_gear.svg?raw';

type SectionPanelProps = {
  frameClass: string;
  svg: string;
  inkSvg?: string;
  contentClassName?: string;
  title?: string;
  children: ReactNode;
  onSettings?: () => void;
};

/**
 * A single fixed-aspect-ratio frame (phase 10 asset refactor) — one dedicated
 * SVG per section, replacing the old generic Panel/Panel9Slice stretchy-cap
 * technique now that every section has its own purpose-built asset. See
 * frames.css's "SECTION PANELS" block for the per-section frame classes this
 * expects (`${frameClass}` on the outer box, `${frameClass}__content` — or
 * `contentClassName` when the section needs its own content class — on the
 * padded inner wrapper). `svg` is the caller's own `?raw`-imported asset —
 * SectionPanel itself stays generic, not tied to any one section's file.
 *
 * `inkSvg` is optional: most of this app's own hand-traced frame assets are a
 * single-color hollow outline, so the same `svg` string paints both the
 * paper and ink layers (just recolored via CSS `color`) — that's still the
 * default when `inkSvg` is omitted. A few official D&D Beyond box assets are
 * genuinely two-color at the source (a solid body path plus a separate
 * bevelled-border path, each with its own theme color) — passing `inkSvg`
 * renders that border path as its own layer instead of collapsing both into
 * one flat color, which would print as a solid blob.
 */
export function SectionPanel({ frameClass, svg, inkSvg, contentClassName, title, children, onSettings }: SectionPanelProps) {
  return (
    <div className={`frame-box ${frameClass}`}>
      <FrameLayer svg={svg} layer="paper" />
      <FrameLayer svg={inkSvg ?? svg} layer="ink" />
      <div className={contentClassName ?? `${frameClass}__content`}>
        {children}
        {title &&
          (onSettings ? (
            <button type="button" className="reveal panel__title panel__title--manage" aria-label={`Manage ${title}`} onClick={onSettings}>
              {title}
              <FrameIcon className="panel__settings-icon" svg={settingsSvg} aria-hidden />
            </button>
          ) : (
            <div className="panel__title">{title}</div>
          ))}
      </div>
    </div>
  );
}

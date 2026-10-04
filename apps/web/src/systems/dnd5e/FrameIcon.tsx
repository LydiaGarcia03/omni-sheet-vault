import { extractRasterImage, stripXmlPreamble } from './svgUtils';

type FrameIconProps = {
  svg: string;
  className?: string;
  title?: string;
  'aria-hidden'?: boolean;
  'aria-label'?: string;
};

/**
 * A single inline icon, not a paper/ink pair — for a glyph that sits inline
 * with text (the rest buttons' icons) rather than standing alone as a
 * two-tone frame. Left at the SVG's own natural `preserveAspectRatio`
 * (contain, centered) instead of `FrameLayer`'s forced stretch, since these
 * icons aren't square and their box is — see frames.css's own note on why
 * `mask-size: contain` was used here specifically. Colors via inherited
 * `currentColor` from the surrounding text, same as before.
 *
 * A few icons (Flaticon's `fire`/`bullet`, whose SVG format is paywalled —
 * only the PNG is free) are a raster image wrapped in an `<svg>`, same as
 * `FrameLayer`'s own raster fallback and for the same reason: no
 * `currentColor` path to recolor. Falls back to the same `mask-image` +
 * `background-color: currentColor` technique (`.frame-icon--raster` in
 * frames.css), sized `contain` rather than stretched.
 *
 * `title`/`aria-hidden`/`aria-label` exist so a caller whose only reason to
 * wrap this in its own `<span>` was to carry a tooltip or accessibility
 * attribute can pass them here instead — this component always renders its
 * own single span (required for `dangerouslySetInnerHTML`), so a second,
 * purely presentational wrapper around it is never necessary.
 */
export function FrameIcon({ svg, className, title, 'aria-hidden': ariaHidden, 'aria-label': ariaLabel }: FrameIconProps) {
  const raster = extractRasterImage(svg);
  if (raster) {
    const rasterClassName = className ? `${className} frame-icon--raster` : 'frame-icon--raster';
    return (
      <span
        className={rasterClassName}
        title={title}
        aria-hidden={ariaHidden}
        aria-label={ariaLabel}
        style={{ maskImage: `url("${raster}")`, WebkitMaskImage: `url("${raster}")` }}
      />
    );
  }
  return (
    <span
      className={className}
      title={title}
      aria-hidden={ariaHidden}
      aria-label={ariaLabel}
      dangerouslySetInnerHTML={{ __html: stripXmlPreamble(svg) }}
    />
  );
}

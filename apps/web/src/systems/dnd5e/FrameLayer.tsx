import { extractRasterImage, forceStretch } from './svgUtils';

type FrameLayerProps = {
  svg: string;
  layer: 'paper' | 'ink';
};

/**
 * One layer (paper or ink) of the frame-kit's two-tone box technique.
 * Phase 10: replaced the earlier CSS `mask-image` div pair — a masked div
 * shows nothing in DevTools but a colored rectangle, not the actual frame
 * artwork, making it hard to inspect or debug against the reference. This
 * renders the real imported SVG markup (`import x from '...svg?raw'`) so
 * Inspect Element shows genuine `<path>` elements, matching how D&D Beyond's
 * own DOM works. `dangerouslySetInnerHTML` is safe here — the content is this
 * app's own bundled static asset, never user- or network-supplied. Stretched
 * to fill its box exactly (`forceStretch`), matching the old mask-size:100%
 * 100% behavior every consumer's own CSS dimensions were already tuned for.
 *
 * A few assets (see `extractRasterImage`'s own comment) are a raster PNG
 * wrapped in `<svg>` rather than real vector paths, so `currentColor` has
 * nothing to recolor. Those fall back to the pre-phase-10 `mask-image`
 * technique instead — the DevTools-inspectability phase 10 was chasing
 * doesn't apply to a raster asset either way, since there are no `<path>`
 * elements in it to inspect.
 */
export function FrameLayer({ svg, layer }: FrameLayerProps) {
  const raster = extractRasterImage(svg);
  if (raster) {
    return <span className={`frame-box__${layer} frame-box__${layer}--raster`} style={{ maskImage: `url("${raster}")`, WebkitMaskImage: `url("${raster}")` }} />;
  }
  return <span className={`frame-box__${layer}`} dangerouslySetInnerHTML={{ __html: forceStretch(svg) }} />;
}

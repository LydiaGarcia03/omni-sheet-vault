/**
 * Raw `?raw`-imported SVG source needs light normalization before injection —
 * see FrameLayer.tsx/FrameIcon.tsx's own doc comments for why real inline
 * `<svg>` markup replaced the earlier CSS mask-image technique (phase 10,
 * "let me inspect the actual paths in DevTools").
 */

/** Some source files carry an XML declaration/DOCTYPE preamble — invalid mid-DOM. */
export function stripXmlPreamble(raw: string): string {
  const index = raw.indexOf('<svg');
  return index === -1 ? raw : raw.slice(index);
}

/**
 * The old CSS mask technique always used `mask-size: 100% 100%` (non-uniform
 * stretch) regardless of each SVG's own `preserveAspectRatio` — safe there
 * because every consuming component's own box dimensions were already tuned
 * to each asset's native ratio (see frames.css's per-component comments), so
 * stretch and contain looked identical. A real inline `<svg>` element does
 * respect `preserveAspectRatio`, so this forces the same "trust the box, fill
 * it exactly" behavior explicitly rather than relying on it being absent.
 */
export function forceStretch(raw: string): string {
  const stripped = stripXmlPreamble(raw);
  return stripped.replace(/<svg([^>]*)>/, (_match, attrs: string) => {
    const withoutExisting = attrs.replace(/\s*preserveAspectRatio="[^"]*"/, '');
    return `<svg${withoutExisting} preserveAspectRatio="none">`;
  });
}

/**
 * A handful of frame assets (`dnd_frame.svg` among them) aren't real vector
 * art — they're a source PNG embedded via `<image href="data:...">` inside
 * an `<svg>` wrapper, kept only to preserve the original file's own colors.
 * `fill="currentColor"` has nothing to attach to on a raster image, so the
 * paper/ink two-tone technique (`FrameLayer.tsx`) can't recolor it the way
 * it recolors a real `<path>`-based asset — every consumer renders the same
 * baked-in pixels twice, stacked, which is why these boxes come out a flat,
 * uncontrollable color instead of responding to `--frame-paper`/`--frame-ink`.
 * Returns the image's own `data:` URI so `FrameLayer` can fall back to a CSS
 * `mask-image` (this codebase's pre-phase-10 technique) for just this asset,
 * recoloring it via `background-color: currentColor` same as every other
 * frame. Returns `null` for a real vector asset, which takes the normal path.
 */
export function extractRasterImage(raw: string): string | null {
  const match = raw.match(/<image[^>]*\shref="([^"]+)"/);
  return match ? match[1] : null;
}

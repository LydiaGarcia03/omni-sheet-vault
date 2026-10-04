import { MarkBox } from './MarkBox';

type BoxTrackProps = {
  maxUses: number;
  usedCount: number;
  rechargeLabel?: string | null;
  onUse?: () => void;
  onRestore?: () => void;
};

/**
 * Shared primitive (ui-design-system.md) — N boxes, marked or unmarked, with an
 * optional recharge label. Phase 9: spending and restoring a use are real
 * mutations — clicking an unmarked box spends one use (`onUse`), clicking a
 * marked box restores one (`onRestore`). Read-only (plain spans, no affordance)
 * when both handlers are omitted, which no current consumer does but keeps the
 * primitive usable for a future read-only display. Each box's own click stops
 * propagation — `FeatureActionRow`/`FeatureTraitRow` wrap this in a `ListRow`
 * that opens Entity Detail on any other click in the row.
 *
 * **Resized 2026-09-03**, DOM-measured against D&D Beyond's own
 * `.ct-slot-manager__slot` (direct owner report: "o quadrado é maior"): 20px
 * boxes with sharp corners and a light gray rest-state border (`#D8D8D8`),
 * not the 9px rounded-adjacent boxes this used to guess at. Gained a "/"
 * separator between the boxes and the recharge label, matching D&D Beyond's
 * own `.ct-feature-snippet__limited-use` structure; the label itself
 * corrected from an 8px uppercase muted caption to a plain 13px/400/black
 * run (`rechargeTriggerLabel`'s own text, e.g. "Short Rest", is already
 * title case — the old uppercase transform was fighting that, not
 * matching it).
 *
 * **Marked-box fill corrected 2026-09-04**, direct owner report ("os
 * quadrados... quando clicados são preenchidos de forma diferente"):
 * DOM-measured (`getComputedStyle`) against D&D Beyond's own
 * `.ct-slot-manager__slot--used` live, mid-click. A marked box was never a
 * solid-filled square — the box itself keeps its white background and
 * `#D8D8D8` border in both states; what marks it is a smaller solid square
 * centered inside (a `::before` there, 10px within the 20px box — half
 * size), colored `--accent-control` (the theme accent the filter chips and
 * tab underline also use). Reproduced here as a real centered child span rather
 * than a pseudo-element, same visual result. The box's faint inset shadow
 * (`0 0 4px #D8D8D8 inset`, present in both states — D&D Beyond's own
 * subtle bevel) came along with it. Confirmed live, same investigation:
 * clicking any box in a multi-box track (not just the exact one clicked)
 * fills from the left / empties from the right — already this component's
 * own behavior, since `onUse`/`onRestore` were never per-index to begin
 * with. **Corrected again same day, direct owner report ("não está 100%
 * alinhado no centro"):** the mark was `50%`/`50%`, which resolves against
 * the box's 18px content area (20px minus the 1px border on each side) —
 * 9px, an odd number that flexbox's centering then had to split
 * asymmetrically (4px one side, 5px the other) since it can't offset by
 * half a pixel cleanly. Fixed to the exact `10px` D&D Beyond itself uses,
 * which centers evenly in that 18px content area (4px both sides, no
 * rounding involved) — `50%` and `10px` happen to be visually close for a
 * 20px box, which is why this wasn't obvious without the pixel math.
 *
 * **The box+mark visual moved into `MarkBox.tsx`** once the Inventory tab's
 * equip control turned out to be a *separate* CSS clone of this same
 * treatment (`.item-row__flag`, 16px/3px-radius/8px-mark) rather than this
 * component reused — a real, silently-drifted mismatch against D&D Beyond's
 * own equal-size equip control, not just a styling duplication. Both now
 * render the identical `MarkBox`; only the interactive wrapper (this
 * component's per-box `<button>`, `ItemRow.tsx`'s single toggle button) stays
 * separate, since their aria semantics genuinely differ.
 */
export function BoxTrack({ maxUses, usedCount, rechargeLabel, onUse, onRestore }: BoxTrackProps) {
  const boxes = Array.from({ length: maxUses }, (_, index) => index < usedCount);
  const interactive = onUse != null || onRestore != null;

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <div style={{ display: 'flex', gap: '4px' }}>
        {boxes.map((marked, index) =>
          interactive ? (
            <button
              key={index}
              type="button"
              className="mutate mark-box-button"
              aria-label={marked ? `Restore use ${index + 1}` : `Spend use ${index + 1}`}
              onClick={(event) => {
                event.stopPropagation();
                (marked ? onRestore : onUse)?.();
              }}
            >
              <MarkBox marked={marked} />
            </button>
          ) : (
            <span key={index} aria-hidden="true">
              <MarkBox marked={marked} />
            </span>
          ),
        )}
      </div>
      {rechargeLabel && (
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-primary, #242528)' }}>
          <span aria-hidden="true" style={{ color: 'var(--text-muted, #6B7A85)' }}>
            /
          </span>
          {rechargeLabel}
        </span>
      )}
    </div>
  );
}

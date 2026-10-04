type MarkBoxProps = {
  marked: boolean;
};

/**
 * Shared primitive (ui-design-system.md) — the "marked box" visual
 * `BoxTrack.tsx` and the Inventory tab's equip control (`ItemRow.tsx`) both
 * use: a 20px box, sharp corners, that keeps its own paper background and
 * border in both states — marked "on" by a smaller accent-colored square
 * centered inside, never a solid fill. DOM-measured against D&D Beyond's own
 * `.ct-slot-manager__slot`, which the reference itself reuses for both a
 * feature's charge track and its inventory equip control (see `BoxTrack.tsx`'s
 * own doc comment for the full measurement history this was extracted from).
 * Purely presentational — callers own their own interactive wrapper (a
 * `<button>` with its own aria attributes), since a box track's per-box
 * semantics ("spend/restore use N") and the equip flag's ("item equipped:
 * yes/no") genuinely differ.
 */
export function MarkBox({ marked }: MarkBoxProps) {
  return <span className="mark-box">{marked && <span className="mark-box__mark" aria-hidden="true" />}</span>;
}

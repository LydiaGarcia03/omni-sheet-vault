import { FrameIcon } from './FrameIcon';
import { FrameLayer } from './FrameLayer';
import { MarkBox } from '../../sheet/MarkBox';
import type { Item, MutationHandler } from '../../sheet/api';
import battleGearSvg from './frames/dnd_icon_battle_gear.svg?raw';
import attunementSlotSvg from './frames/dnd_icon_attunement_slot.svg?raw';
import { rarityClassSuffix } from './itemRarity';

type AttunementSectionProps = {
  items: Item[];
  onMutate: MutationHandler;
};

const ATTUNEMENT_LIMIT = 3;

/**
 * A dedicated section below the item list, matching D&D Beyond's own
 * "Attunement" tab (confirmed live against dndbeyond.com/characters/50149479):
 * two columns, attuned items on the left and every attunement-eligible item on
 * the right — including ones already attuned (DOM-confirmed live: "Items
 * Requiring Attunement" lists an item regardless of its own attuned state, it
 * is not a "still eligible" filter). Only items flagged `requiresAttunement`
 * (set once at creation, see the inventory-add form) ever appear on the right
 * at all.
 *
 * The left column renders exactly three fixed slots (`AttunementSlot`,
 * empty or filled), the real D&D Beyond shape — its own numbered slot picker
 * is a catalog-backed drag target showing the item's own portrait art, which
 * this app has no catalog identity or artwork for (a freeform item has
 * neither at all; a catalogue-sourced item has no drag interaction built),
 * so a filled slot shows a generic equipment glyph (`dnd_icon_battle_gear.svg`,
 * game-icons.net, Lorc, CC BY 3.0 — a credits page/footer is still owed, same
 * as every other game-icons.net pick in this app, see `systems/dnd-5e/sheet-build.md`)
 * in the frame's own round medallion instead of a portrait, with the item's
 * name in the bar beside it. The slot's outline is the real asset D&D Beyond
 * itself uses (`AttunementSlotBoxSvg.tsx` in the design reference), extracted
 * as a single `currentColor` path — same technique as every other frame in
 * this app (`FrameLayer`), not the multi-theme colored-variant system D&D
 * Beyond's own component uses, since this app has no light/dark mode to
 * switch between.
 *
 * Removing an item from inventory belongs in the sidebar's own Entity Detail
 * panel once an item row opens one, not as a button here — the same
 * follow-up slice `ItemRow.tsx`'s own doc comment names. Attuning itself is
 * a checkbox, the same `MarkBox`/`.item-row__flag` control and name styling
 * (`.item-row__name`) the main item list's equip flag already uses, matching
 * D&D Beyond's own attunement slot manager (DOM-confirmed live: a
 * `role="checkbox"` control, the same one it reuses for equip).
 */
export function AttunementSection({ items, onMutate }: AttunementSectionProps) {
  const attunedItems = items.filter((item) => item.attuned);
  const attunableItems = items.filter((item) => item.requiresAttunement);
  const atLimit = attunedItems.length >= ATTUNEMENT_LIMIT;
  const slots: Array<Item | null> = Array.from(
    { length: ATTUNEMENT_LIMIT },
    (_, index) => attunedItems[index] ?? null,
  );

  return (
    <div className="attunement-section">
      <div className="attunement-section__col">
        <div className="attunement-section__heading">Attuned Items</div>
        {slots.map((slot, index) => (
          <AttunementSlot key={slot ? slot.key : `empty-${index}`} item={slot} />
        ))}
      </div>
      <div className="attunement-section__col">
        <div className="attunement-section__heading">Items Requiring Attunement</div>
        {attunableItems.length === 0 && <p className="attunement-section__empty">No eligible items.</p>}
        {attunableItems.map((item) => {
          const raritySuffix = rarityClassSuffix(item.rarity);
          return (
            <div key={item.key} className="attunement-row">
              <button
                type="button"
                className="mutate item-row__flag"
                aria-pressed={item.attuned}
                aria-label={`${item.name} attuned: ${item.attuned ? 'yes' : 'no'}`}
                disabled={!item.attuned && atLimit}
                onClick={() => onMutate({ type: 'TOGGLE_ITEM_ATTUNED', itemKey: item.key })}
              >
                <MarkBox marked={item.attuned} />
              </button>
              <span className={`item-row__name${raritySuffix ? ` item-row__name--${raritySuffix}` : ''}`}>{item.name}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

/**
 * The extracted path is a thin ornate frame outline, not a solid fill (confirmed
 * live once rendered — the original component layers it behind a separate photo/
 * background, it never fills a box on its own), so `currentColor` only colors the
 * outline itself, not a background. Filled uses the "ink" tone — the same dark
 * tone this app's filled proficiency dots already use for an "on"/active state
 * (`.saving__prof--on`/`.skill-row__prof--on` in frames.css), echoing D&D
 * Beyond's own filled-vs-empty color distinction (`ThemedAttunementSlotBoxSvg` vs
 * `EmptyAttunementSlotBoxSvg`) without literally reproducing its multi-theme
 * variant system. Empty stays "paper" (white, this app's usual frame tone), with
 * an explicit CSS border — the outline alone renders white-on-white otherwise,
 * since there is no dark ink line to fall back on the way the filled state has.
 */
function AttunementSlot({ item }: { item: Item | null }) {
  return (
    <div className={`attunement-slot ${item ? 'attunement-slot--filled' : 'attunement-slot--empty'}`}>
      <FrameLayer svg={attunementSlotSvg} layer={item ? 'ink' : 'paper'} />
      {item ? (
        <div className="attunement-slot__content">
          <span className="attunement-slot__icon">
            <FrameIcon className="attunement-slot__icon-glyph" svg={battleGearSvg} aria-label="Attuned item" />
          </span>
          <span className="attunement-slot__name">{item.name}</span>
        </div>
      ) : (
        <div className="attunement-slot__content attunement-slot__content--empty">Choose an item from the right</div>
      )}
    </div>
  );
}

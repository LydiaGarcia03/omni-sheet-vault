import { useEffect, useState } from 'react';
import { ListRow } from '../../sheet/ListRow';
import { MarkBox } from '../../sheet/MarkBox';
import { MoveButton } from '../../sheet/MoveButton';
import { STORAGE_LOCATIONS } from '../../sheet/ManageInventoryPanel';
import type { CharacterSheet, EntityDetailHandler, EntityDetailRequest, Item, MutationHandler } from '../../sheet/api';
import { customizationsOf, EMPTY_ITEM_CUSTOMIZATION, type ItemCustomization } from './customizations';
import { EntityCustomizeEditor, type CustomizeField } from './EntityCustomize';
import { FrameIcon } from './FrameIcon';
import { ItemPreviewIcon } from './ItemPreviewIcon';
import attunementSvg from './frames/dnd_icon_attunement.svg?raw';
import minusIconSvg from './frames/dnd_icon_minus.svg?raw';
import plusIconSvg from './frames/dnd_icon_plus.svg?raw';
import { rarityClassSuffix } from './itemRarity';

type ItemRowProps = {
  item: Item;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  hasBagOfHolding: boolean;
};

// Item Entity Detail action bar: +/- and direct typing both commit an absolute SET_ITEM_QUANTITY.
function ItemQuantityControl({ item, onMutate }: { item: Item; onMutate: MutationHandler }) {
  const [value, setValue] = useState(String(item.quantity));

  useEffect(() => {
    setValue(String(item.quantity));
  }, [item.quantity]);

  function commit() {
    const parsed = Number(value);
    if (Number.isInteger(parsed) && parsed >= 1) {
      onMutate({ type: 'SET_ITEM_QUANTITY', itemKey: item.key, quantity: parsed });
    } else {
      setValue(String(item.quantity));
    }
  }

  return (
    <div className="item-quantity">
      <div className="item-quantity__label">Quantity</div>
      <div className="item-quantity__controls">
        <button
          type="button"
          className="mutate item-quantity__button"
          aria-label="Decrease quantity"
          onClick={() => onMutate({ type: 'SET_ITEM_QUANTITY', itemKey: item.key, quantity: Math.max(1, item.quantity - 1) })}
        >
          <FrameIcon svg={minusIconSvg} className="stepper-icon" aria-hidden />
        </button>
        <input
          type="number"
          min={1}
          className="item-quantity__input"
          value={value}
          onChange={(event) => setValue(event.target.value)}
          onBlur={commit}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              (event.target as HTMLInputElement).blur();
            }
          }}
          aria-label="Item quantity"
        />
        <button
          type="button"
          className="mutate item-quantity__button"
          aria-label="Increase quantity"
          onClick={() => onMutate({ type: 'SET_ITEM_QUANTITY', itemKey: item.key, quantity: item.quantity + 1 })}
        >
          <FrameIcon svg={plusIconSvg} className="stepper-icon" aria-hidden />
        </button>
      </div>
    </div>
  );
}

/**
 * D&D Beyond's own `.ct-item-detail__actions` (DOM-confirmed live): a
 * plain item shows Quantity plus Move/Delete; an item flagged
 * `requiresAttunement` swaps Quantity for Equip/Unequip and Attune/Unattune
 * alongside the same Move/Delete pair — magic items are effectively always
 * singular, so a stepper there would be meaningless. Move lists every
 * storage location except the item's own current one; `hasBagOfHolding`
 * mirrors `ManageInventoryPanel`'s own gating so Bag of Holding is only ever
 * offered as a destination once the character actually owns one.
 */
function itemActionBar(item: Item, onMutate: MutationHandler, hasBagOfHolding: boolean) {
  const moveTargets = STORAGE_LOCATIONS.filter((location) => location.id !== 'BAG_OF_HOLDING' || hasBagOfHolding);
  return (
    <div className="item-action-bar">
      {item.requiresAttunement ? (
        <>
          <button type="button" className="mutate sidebar-action-button" onClick={() => onMutate({ type: 'TOGGLE_ITEM_EQUIPPED', itemKey: item.key })}>
            {item.equipped ? 'Unequip' : 'Equip'}
          </button>
          <button type="button" className="mutate sidebar-action-button" onClick={() => onMutate({ type: 'TOGGLE_ITEM_ATTUNED', itemKey: item.key })}>
            {item.attuned ? 'Unattune' : 'Attune'}
          </button>
        </>
      ) : (
        <>
          <ItemQuantityControl item={item} onMutate={onMutate} />
          <button type="button" className="mutate sidebar-action-button" onClick={() => onMutate({ type: 'TOGGLE_ITEM_EQUIPPED', itemKey: item.key })}>
            {item.equipped ? 'Unequip' : 'Equip'}
          </button>
        </>
      )}
      <MoveButton
        current={item.storageLocation}
        options={moveTargets}
        onMove={(storageLocation) => onMutate({ type: 'MOVE_ITEM', itemKey: item.key, storageLocation })}
      />
      <button type="button" className="mutate sidebar-action-button" onClick={() => onMutate({ type: 'REMOVE_ITEM', itemKey: item.key })}>
        Delete
      </button>
    </div>
  );
}

/**
 * No frame asset — same reasoning as AttackRow. The whole row opens Entity
 * Detail (`ListRow`); the equip flag stops propagation so it doesn't also
 * open it. Equipped is a mutate target rendered as a checkbox
 * (`.item-row__flag`) — matching D&D Beyond's own equip control, direct owner
 * request replacing an earlier letter-flag ("E") button. Attunement is **not**
 * toggled here — matching D&D Beyond, it lives in its own Attunement section
 * further down the tab (see AttunementSection.tsx), not as a checkbox next to
 * equip. Equipping an item whose `itemKind` is ARMOR/SHIELD feeds the armor
 * class derivation trace, and WEAPON feeds the Actions tab's own attack list
 * (`Dnd5eSheetCalculator`) — both keyed on `itemKind`, which only a real
 * catalogue item or explicitly-set freeform value carries; a freeform item
 * left at its default `GEAR` participates in neither.
 *
 * Removing an item from inventory belongs in this same Entity Detail panel's
 * action bar (`itemActionBar`, below), not as a separate row button.
 *
 * **Corrected 2026-09-20, direct owner report:** the checked state used to be
 * a solid ink-filled square with a reversed "✓" — this app's own already-
 * established "marked box" treatment (`BoxTrack.tsx`) never fills a box
 * solid, so the flag now uses the same box-plus-centered-mark technique.
 *
 * Renders the shared `MarkBox` (`sheet/MarkBox.tsx`) directly — the same
 * 20px/sharp-corner control `BoxTrack.tsx` uses for a feature's charge
 * track, matching D&D Beyond's own reuse of one control for both rather
 * than a separately maintained CSS clone.
 *
 * **2026-09-10:** a `requiresAttunement && !attuned` row now shows D&D
 * Beyond's own warning-triangle glyph (`FrameIcon` + `dnd_icon_attunement.svg`,
 * confirmed live next to "Cloak of Elvenkind" on dndbeyond.com's own
 * equipment list) trailing the name — this app previously had no indicator
 * at all for that state in the main list, only inside `AttunementSection`
 * further down the tab.
 */
const KG_PER_LB = 0.453592;

const PRICE_AND_TEXT_FIELDS: CustomizeField[] = [
  { key: 'costOverride', label: 'Cost Override', kind: 'number', decimal: true },
  { key: 'weightOverride', label: 'Weight Override (kg)', kind: 'number', decimal: true, scale: KG_PER_LB },
];

const NAME_AND_NOTES_FIELDS: CustomizeField[] = [
  { key: 'name', label: 'Name', kind: 'text', name: true },
  { key: 'notes', label: 'Notes', kind: 'text' },
];

/** D&D Beyond's weapon Customize; any other item only has cost, weight, name and notes. */
const WEAPON_FIELDS: CustomizeField[] = [
  { key: 'toHitOverride', label: 'To Hit Override', kind: 'number' },
  { key: 'toHitBonus', label: 'To Hit Bonus', kind: 'number' },
  { key: 'damageBonus', label: 'Damage Bonus', kind: 'number' },
  ...PRICE_AND_TEXT_FIELDS,
  { key: 'silvered', label: 'Silvered', kind: 'checkbox' },
  { key: 'adamantine', label: 'Adamantine', kind: 'checkbox' },
  { key: 'displayAsAttack', label: 'Display As Attack', kind: 'checkbox' },
  ...NAME_AND_NOTES_FIELDS,
];

const OTHER_ITEM_FIELDS: CustomizeField[] = [...PRICE_AND_TEXT_FIELDS, ...NAME_AND_NOTES_FIELDS];

export function hasBagOfHoldingIn(sheet: CharacterSheet): boolean {
  return sheet.items.some((candidate) => candidate.equipped && candidate.name === 'Bag of Holding');
}

function itemCustomize(item: Item, customization: ItemCustomization | undefined, onMutate: MutationHandler) {
  const values = customization ?? EMPTY_ITEM_CUSTOMIZATION;
  return {
    customized: customization != null,
    editor: (
      <EntityCustomizeEditor
        fields={item.itemKind === 'WEAPON' ? WEAPON_FIELDS : OTHER_ITEM_FIELDS}
        values={values}
        customized={customization != null}
        onSave={(next) => onMutate({ type: 'CUSTOMIZE', group: 'items', target: item.key, value: next })}
        onReset={() => onMutate({ type: 'REMOVE_CUSTOMIZATION', group: 'items', target: item.key })}
      />
    ),
  };
}

/** An item's Entity Detail, rebuilt from the live sheet so a rename or Customize change shows at once. */
export function buildItemDetailRequest(
  item: Item,
  customization: ItemCustomization | undefined,
  onMutate: MutationHandler,
  hasBagOfHolding: boolean,
): EntityDetailRequest {
  return {
    kind: 'entityDetail',
    entityKey: `item:${item.key}`,
    name: item.name,
    icon: <ItemPreviewIcon itemKind={item.itemKind} />,
    // D&D Beyond's own order (Bedroll, DOM-confirmed): Weight, Cost, Source. Weight is the
    // per-unit figure, same as D&D Beyond's own detail panel — the row list, not this
    // panel, is where a stack's quantity multiplies it out.
    metadata: [
      ...(item.weightKg != null ? [{ label: 'Weight', value: `${item.weightKg.toFixed(1)} kg` }] : []),
      ...(item.cost ? [{ label: 'Cost', value: item.cost }] : []),
      ...(item.properties && item.properties.length > 0 ? [{ label: 'Properties', value: item.properties.join(', ') }] : []),
      ...(item.source ? [{ label: 'Source', value: item.source }] : []),
    ],
    description: item.notes,
    actionBar: itemActionBar(item, onMutate, hasBagOfHolding),
    customize: itemCustomize(item, customization, onMutate),
    refresh: (sheet) => {
      const latest = sheet.items.find((candidate) => candidate.key === item.key);
      return latest
        ? buildItemDetailRequest(latest, customizationsOf(sheet).items[item.key], onMutate, hasBagOfHoldingIn(sheet))
        : null;
    },
  };
}

export function ItemRow({ item, onMutate, onOpenDetail, hasBagOfHolding }: ItemRowProps) {
  const openDetail = () => onOpenDetail(buildItemDetailRequest(item, undefined, onMutate, hasBagOfHolding));
  const raritySuffix = rarityClassSuffix(item.rarity);
  const weightKg = item.weightKg != null ? item.weightKg * item.quantity : null;

  return (
    <ListRow className="item-row reveal" ariaLabel={`Open ${item.name} details`} onOpen={openDetail}>
      <button
        type="button"
        className="mutate item-row__flag"
        aria-pressed={item.equipped}
        aria-label={`${item.name} equipped: ${item.equipped ? 'yes' : 'no'}`}
        onClick={(event) => {
          event.stopPropagation();
          onMutate({ type: 'TOGGLE_ITEM_EQUIPPED', itemKey: item.key });
        }}
      >
        <MarkBox marked={item.equipped} />
      </button>
      <span className={`reveal item-row__name${raritySuffix ? ` item-row__name--${raritySuffix}` : ''}`}>
        {item.name}
        {item.attuned && <span className="item-row__attuned" aria-label="Attuned"> ✦</span>}
        {item.requiresAttunement && !item.attuned && (
          <FrameIcon className="item-row__attunement-warning" svg={attunementSvg} aria-label="Requires attunement" />
        )}
      </span>
      <div className="item-row__weight">{weightKg != null ? `${weightKg.toFixed(1)} kg` : '—'}</div>
      <div className="item-row__quantity">×{item.quantity}</div>
      <div className="item-row__cost">{item.cost}</div>
      <div className="item-row__notes">{item.notes}</div>
    </ListRow>
  );
}

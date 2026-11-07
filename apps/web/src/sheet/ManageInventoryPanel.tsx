import { useState } from 'react';
import type { CatalogueEntry } from '../catalogue/api';
import { formatFixed } from '../language/format';
import { useLanguage, type Locale } from '../language/language';
import { useSheetText, type SheetText } from '../locales/platformMessages';
import { rarityClassSuffix } from '../systems/dnd5e/itemRarity';
import type { ManageInventoryRequest, NewItemDraft, StorageSection } from './api';
import { catalogueEntryLabel } from './CatalogueSourceLabel';
import { MarkBox } from './MarkBox';
import { MoveButton } from './MoveButton';
import { SidebarCollapsible, SidebarHeader } from './sidebarParts';

const EMPTY_DRAFT: NewItemDraft = { name: '', quantity: 1, cost: '', notes: '', requiresAttunement: false };

const STORAGE_LOCATIONS = [
  { id: 'EQUIPMENT', label: 'terms.equipment' },
  { id: 'BACKPACK', label: 'terms.backpack' },
  { id: 'BAG_OF_HOLDING', label: 'terms.bagOfHolding' },
  { id: 'OTHER_POSSESSIONS', label: 'terms.otherPossessions' },
] as const;

/** The storage locations, named in the player's language. */
export function storageLocations(t: SheetText): { id: string; label: string }[] {
  return STORAGE_LOCATIONS.map(({ id, label }) => ({ id, label: t(label) }));
}

type CatalogueItemData = { itemKind?: string; typeLabel?: string | null; rarity?: string | null };

const dataOf = (entry: CatalogueEntry) => (entry.data ?? {}) as CatalogueItemData;

const TYPE_FILTERS: { id: string; label: Parameters<SheetText>[0]; matches: (data: CatalogueItemData) => boolean }[] = [
  { id: 'ARMOR', label: 'terms.armor', matches: (data) => data.itemKind === 'ARMOR' || data.itemKind === 'SHIELD' },
  { id: 'POTION', label: 'terms.potion', matches: (data) => (data.typeLabel ?? '').startsWith('Potion') },
  { id: 'RING', label: 'terms.ring', matches: (data) => (data.typeLabel ?? '').startsWith('Ring') },
  { id: 'ROD', label: 'terms.rod', matches: (data) => (data.typeLabel ?? '').startsWith('Rod') },
  { id: 'SCROLL', label: 'terms.scroll', matches: (data) => (data.typeLabel ?? '').startsWith('Scroll') },
  { id: 'STAFF', label: 'terms.staff', matches: (data) => (data.typeLabel ?? '').startsWith('Staff') },
  { id: 'WAND', label: 'terms.wand', matches: (data) => (data.typeLabel ?? '').startsWith('Wand') },
  { id: 'WEAPON', label: 'terms.weapon', matches: (data) => data.itemKind === 'WEAPON' },
  { id: 'WONDROUS', label: 'terms.wondrous', matches: (data) => (data.typeLabel ?? '').includes('Wondrous') },
];

function isOtherGear(data: CatalogueItemData): boolean {
  return !TYPE_FILTERS.some((filter) => filter.matches(data));
}

const CATALOGUE_RESULT_LIMIT = 200;

/** The row's type line; 5etools treasure codes ("$A", "$G") read as Treasure. */
function typeLabelOf(entry: CatalogueEntry, t: SheetText): string {
  const label = dataOf(entry).typeLabel;
  if (!label) {
    return entry.sourceBook ?? '';
  }
  return label.startsWith('$') ? t('terms.treasure') : label;
}

function rarityClass(base: string, rarity: string | null | undefined): string {
  const suffix = rarityClassSuffix(rarity ?? null);
  return suffix ? `${base} item-row__name--${suffix}` : base;
}

/** D&D Beyond's equipment shop: a search, the type chips, then one row per match with its type and an Add button. */
function AddItemsSection({ catalogue, onAdd }: { catalogue: CatalogueEntry[]; onAdd: (catalogueEntryId: string) => void }) {
  const t = useSheetText();
  const [search, setSearch] = useState('');
  const [types, setTypes] = useState<string[]>([]);
  const toggleType = (id: string) => setTypes(types.includes(id) ? types.filter((type) => type !== id) : [...types, id]);
  const matchesType = (data: CatalogueItemData) =>
    types.length === 0 ||
    types.some((id) => (id === 'OTHER_GEAR' ? isOtherGear(data) : TYPE_FILTERS.find((filter) => filter.id === id)!.matches(data)));
  const filtered = catalogue
    .filter((entry) => matchesType(dataOf(entry)))
    .filter((entry) => entry.name.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => a.name.localeCompare(b.name));
  const visible = filtered.slice(0, CATALOGUE_RESULT_LIMIT);
  const chips = [...TYPE_FILTERS.map(({ id, label }) => ({ id, label: t(label) })), { id: 'OTHER_GEAR', label: t('terms.otherGear') }];

  return (
    <div className="inventory-manage__shop">
      <div className="inventory-manage__filter-group">
        <div className="inventory-manage__filter-label">{t('common.filter')}</div>
        <input
          className="inventory-manage__search"
          type="search"
          aria-label={t('inventory.searchLabel')}
          placeholder={t('inventory.searchPlaceholder')}
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>
      <div className="inventory-manage__filter-group">
        <div className="inventory-manage__filter-heading">{t('inventory.filterByType')}</div>
        <div className="inventory-manage__chips">
          {chips.map((chip) => (
            <button
              key={chip.id}
              type="button"
              className={types.includes(chip.id) ? 'inventory-manage__chip is-active' : 'inventory-manage__chip'}
              aria-pressed={types.includes(chip.id)}
              onClick={() => toggleType(chip.id)}
            >
              {chip.label}
            </button>
          ))}
        </div>
      </div>
      {(search !== '' || types.length > 0) && (
        <ul className="inventory-manage__results">
          {visible.map((entry) => (
            <li key={entry.id} className="inventory-manage__result">
              <span className="inventory-manage__result-text">
                <span className={rarityClass('inventory-manage__name', dataOf(entry).rarity)}>{entry.name}</span>
                <span className="inventory-manage__meta">{typeLabelOf(entry, t)}</span>
              </span>
              <button
                type="button"
                className="mutate inventory-manage__add"
                aria-label={t('common.addName', { name: catalogueEntryLabel(entry.name, entry.sourceBook) })}
                onClick={() => onAdd(entry.id)}
              >
                {t('common.add')}
              </button>
            </li>
          ))}
          {filtered.length > CATALOGUE_RESULT_LIMIT && (
            <li className="inventory-manage__more">{t('inventory.moreMatches', { count: filtered.length - CATALOGUE_RESULT_LIMIT })}</li>
          )}
        </ul>
      )}
    </div>
  );
}

/** D&D Beyond's Add Custom Item: name, cost, notes, quantity, and the attunement flag, added in one go. */
function AddCustomItemForm({ onAdd }: { onAdd: (draft: NewItemDraft) => void }) {
  const t = useSheetText();
  const [open, setOpen] = useState(false);
  const [draft, setDraft] = useState<NewItemDraft>(EMPTY_DRAFT);
  const set = <K extends keyof NewItemDraft>(field: K, value: NewItemDraft[K]) => setDraft((current) => ({ ...current, [field]: value }));

  const submit = () => {
    const name = draft.name.trim();
    if (!name) {
      return;
    }
    onAdd({ ...draft, name, quantity: Math.max(1, draft.quantity || 1) });
    setDraft(EMPTY_DRAFT);
    setOpen(false);
  };

  return (
    <details className="inventory-manage__custom" open={open} onToggle={(event) => setOpen((event.currentTarget as HTMLDetailsElement).open)}>
      <summary className="inventory-manage__custom-summary">{t('inventory.addCustomItem')}</summary>
      <div className="entity-customize inventory-manage__custom-editor">
        <div className="entity-customize__properties">
          <label className="entity-customize__property entity-customize__property--text">
            <input className="entity-customize__input" value={draft.name} onChange={(event) => set('name', event.target.value)} />
            <span className="entity-customize__label">{t('inventory.name')}</span>
          </label>
          <label className="entity-customize__property entity-customize__property--text">
            <input className="entity-customize__input" value={draft.cost} onChange={(event) => set('cost', event.target.value)} />
            <span className="entity-customize__label">{t('inventory.cost')}</span>
          </label>
          <label className="entity-customize__property entity-customize__property--text">
            <input className="entity-customize__input" value={draft.notes} onChange={(event) => set('notes', event.target.value)} />
            <span className="entity-customize__label">{t('inventory.notes')}</span>
          </label>
          <label className="entity-customize__property">
            <input
              className="entity-customize__input"
              type="number"
              min={1}
              value={draft.quantity}
              onChange={(event) => set('quantity', Number(event.target.value) || 1)}
            />
            <span className="entity-customize__label">{t('inventory.quantity')}</span>
          </label>
          <label className="entity-customize__property entity-customize__property--checkbox">
            <input
              className="entity-customize__checkbox"
              type="checkbox"
              checked={draft.requiresAttunement}
              onChange={(event) => set('requiresAttunement', event.target.checked)}
            />
            <span className="entity-customize__label">{t('terms.requiresAttunement')}</span>
          </label>
        </div>
        <div className="entity-customize__actions">
          <button type="button" className="mutate rest-pane__button rest-pane__button--primary" onClick={submit}>
            {t('common.add')}
          </button>
        </div>
      </div>
    </details>
  );
}

function weightLabel(location: string, sections: StorageSection[] | undefined, locale: Locale): string | undefined {
  const section = sections?.find((candidate) => candidate.location === location);
  return location === 'OTHER_POSSESSIONS' || !section ? undefined : `${formatFixed(section.weightKg, 1, locale)} kg`;
}

/**
 * D&D Beyond's Manage Inventory pane: Add Items (search, type chips, Add per match), Add Custom Item, then My
 * Inventory as one collapsible per storage location with its weight, each item with an equip box, its name (opening
 * its pane, where Delete lives) and Move. Bag of Holding is a target only once the character carries one equipped.
 */
export function ManageInventoryPanel({
  items,
  catalogue,
  onAdd,
  onAddFromCatalogue,
  onMove,
  onToggleEquip,
  onOpenItem,
  sections,
}: ManageInventoryRequest) {
  const t = useSheetText();
  const hasBagOfHolding = items.some((item) => item.equipped && item.name === 'Bag of Holding');
  const { locale } = useLanguage();
  const locations = storageLocations(t);
  const moveTargets = locations.filter((location) => location.id !== 'BAG_OF_HOLDING' || hasBagOfHolding);
  const groups = locations.map((location) => ({
    ...location,
    items: items.filter((item) => item.storageLocation === location.id),
  })).filter((group) => group.items.length > 0);

  return (
    <div className="inventory-manage">
      <SidebarHeader title={t('inventory.title')} />
      <SidebarCollapsible heading={t('inventory.addItems')} defaultOpen>
        <AddItemsSection catalogue={catalogue} onAdd={onAddFromCatalogue} />
      </SidebarCollapsible>
      <AddCustomItemForm onAdd={onAdd} />
      <h2 className="inventory-manage__subheading">{t('inventory.myInventory')}</h2>
      {groups.map((group) => (
        <SidebarCollapsible
          key={group.id}
          heading={`${group.label} (${group.items.length})`}
          callout={weightLabel(group.id, sections, locale)}
        >
          <ul className="inventory-manage__items">
            {group.items.map((item) => (
              <li key={item.key} className="inventory-manage__item">
                <span className="inventory-manage__item-action">
                  {onToggleEquip ? (
                    <button
                      type="button"
                      className="mutate mark-box-button inventory-manage__equip"
                      aria-pressed={item.equipped}
                      aria-label={t(item.equipped ? 'inventory.equipped' : 'inventory.notEquipped', { name: item.name })}
                      onClick={() => onToggleEquip(item.key)}
                    >
                      <MarkBox marked={item.equipped} />
                    </button>
                  ) : (
                    '--'
                  )}
                </span>
                <button type="button" className={rarityClass('inventory-manage__item-name', item.rarity)} onClick={() => onOpenItem?.(item)}>
                  {item.name}
                  {item.quantity > 1 && ` (${item.quantity})`}
                </button>
                <MoveButton current={item.storageLocation} options={moveTargets} onMove={(location) => onMove(item.key, location)} />
              </li>
            ))}
          </ul>
        </SidebarCollapsible>
      ))}
    </div>
  );
}

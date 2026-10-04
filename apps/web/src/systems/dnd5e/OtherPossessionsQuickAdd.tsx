import { useState } from 'react';
import type { MutationHandler } from '../../sheet/api';

type OtherPossessionsQuickAddProps = {
  onMutate: MutationHandler;
};

/**
 * The sidebar body opened from the Inventory tab's own "+ Add other
 * possessions..." prompt (InventoryTab.tsx), shown only while that section
 * is empty. D&D Beyond's own equivalent panel (confirmed live against
 * dndbeyond.com/characters/50149479) is a single freeform textarea; this
 * app models Other Possessions as real items instead (weight/encumbrance
 * initiative), so typing a name and pressing Enter (or blurring) adds one
 * new item under that storage location and clears the field, ready for the
 * next one.
 */
export function OtherPossessionsQuickAdd({ onMutate }: OtherPossessionsQuickAddProps) {
  const [name, setName] = useState('');

  const commit = () => {
    const trimmed = name.trim();
    if (!trimmed) {
      return;
    }
    onMutate({
      type: 'ADD_ITEM',
      name: trimmed,
      quantity: 1,
      cost: '',
      notes: '',
      requiresAttunement: false,
      storageLocation: 'OTHER_POSSESSIONS',
    });
    setName('');
  };

  return (
    <input
      type="text"
      value={name}
      onChange={(event) => setName(event.target.value)}
      onBlur={commit}
      onKeyDown={(event) => {
        if (event.key === 'Enter') {
          (event.target as HTMLInputElement).blur();
        }
      }}
      placeholder="Item name"
      aria-label="New other possession name"
      className="other-possessions-quick-add"
    />
  );
}

import { useMemo, useState } from 'react';
import type { MutationHandler } from '../../sheet/api';
import { EXTRA_CATEGORIES } from './ExtraRow';
import { challengeValue, useCreatureCatalogue, type Creature } from './useCreatureCatalogue';

/** How many matches the list draws at once; the filter narrows the rest. */
const SHOWN_LIMIT = 60;
const MAX_QUANTITY = 20;
const CHALLENGE_RATINGS = ['0', '1/8', '1/4', '1/2', ...Array.from({ length: 30 }, (_, index) => String(index + 1))];
const FAMILIAR_TYPE = '(Fey, Fiend, or Celestial)';

/**
 * D&D Beyond's "Add an Extra": choose a category (with its hint), filter the catalogue's creatures by name, type, size,
 * environment or tag and by challenge range, then ADD one, or open its preview to add several.
 */
export function AddExtraSection({ onMutate }: { onMutate: MutationHandler }) {
  const creatures = useCreatureCatalogue();
  const [categoryId, setCategoryId] = useState('');
  const [query, setQuery] = useState('');
  const [minimum, setMinimum] = useState('');
  const [maximum, setMaximum] = useState('');
  const [open, setOpen] = useState<string | null>(null);
  const category = EXTRA_CATEGORIES.find((entry) => entry.id === categoryId);

  const matches = useMemo(() => {
    if (!creatures) {
      return [];
    }
    const needle = query.trim().toLowerCase();
    const low = minimum === '' ? -Infinity : challengeValue(minimum);
    const high = maximum === '' ? Infinity : challengeValue(maximum);
    return creatures.filter((creature) => {
      const challenge = challengeValue(creature.data.challengeRating);
      if (challenge < low || challenge > high) {
        return false;
      }
      return (
        !needle ||
        [creature.name, creature.data.creatureType, creature.data.size, ...creature.data.environments, ...creature.tags].some((text) =>
          text.toLowerCase().includes(needle),
        )
      );
    });
  }, [creatures, query, minimum, maximum]);

  const add = (creature: Creature, quantity: number) =>
    onMutate({ type: 'ADD_CATALOGUE_EXTRA', catalogueEntryId: creature.id, category: categoryId, quantity });

  return (
    <div className="extras-add">
      <select className="extras-add__category" aria-label="Choose a category" value={categoryId} onChange={(event) => setCategoryId(event.target.value)}>
        <option value="">-- Choose a Category --</option>
        <optgroup label="Creature">
          {EXTRA_CATEGORIES.filter((entry) => entry.creatures).map((entry) => (
            <option key={entry.id} value={entry.id}>
              {entry.label}
            </option>
          ))}
        </optgroup>
        <optgroup label="Object">
          {EXTRA_CATEGORIES.filter((entry) => !entry.creatures).map((entry) => (
            <option key={entry.id} value={entry.id}>
              {entry.label}
            </option>
          ))}
        </optgroup>
      </select>
      {category && <p className="extras-add__hint">{category.hint}</p>}
      {category?.creatures && (
        <>
          <div className="extras-add__filter-label">Filter</div>
          <input
            className="extras-add__search"
            type="search"
            placeholder="Name, Type, Size, Habitat, Tag, etc."
            aria-label="Filter creatures"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <div className="extras-add__range">
            <span className="extras-add__range-label">Challenge Range</span>
            <ChallengeSelect label="Lowest challenge" value={minimum} onChange={setMinimum} />
            <span>–</span>
            <ChallengeSelect label="Highest challenge" value={maximum} onChange={setMaximum} />
          </div>
          {creatures === null ? (
            <p className="extras-add__note">Loading creatures…</p>
          ) : (
            <ul className="extras-add__list">
              {matches.slice(0, SHOWN_LIMIT).map((creature) => (
                <CreatureRow
                  key={creature.id}
                  creature={creature}
                  familiar={categoryId === 'FAMILIAR'}
                  open={open === creature.id}
                  onToggle={() => setOpen(open === creature.id ? null : creature.id)}
                  onAdd={(quantity) => add(creature, quantity)}
                />
              ))}
              {matches.length === 0 && <li className="extras-add__note">No creature matches this filter.</li>}
              {matches.length > SHOWN_LIMIT && (
                <li className="extras-add__note">
                  Showing {SHOWN_LIMIT} of {matches.length}. Narrow the filter to see the rest.
                </li>
              )}
            </ul>
          )}
        </>
      )}
    </div>
  );
}

function ChallengeSelect({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <select className="extras-add__challenge" aria-label={label} value={value} onChange={(event) => onChange(event.target.value)}>
      <option value="">--</option>
      {CHALLENGE_RATINGS.map((rating) => (
        <option key={rating} value={rating}>
          {rating}
        </option>
      ))}
    </select>
  );
}

type CreatureRowProps = { creature: Creature; familiar: boolean; open: boolean; onToggle: () => void; onAdd: (quantity: number) => void };

function CreatureRow({ creature, familiar, open, onToggle, onAdd }: CreatureRowProps) {
  const [quantity, setQuantity] = useState(1);
  const { data } = creature;
  const type = familiar ? FAMILIAR_TYPE : data.creatureType;
  return (
    <li className="extras-add__item">
      <div className="extras-add__row">
        <div className="extras-add__text">
          <span className="extras-add__name">{creature.name}</span>
          <span className="extras-add__meta">
            {data.size} {type}
            {creature.sourceBook && <i> • {creature.sourceBook}</i>}
          </span>
        </div>
        <button type="button" className="mutate extras-add__add" aria-label={`Add ${creature.name}`} onClick={() => onAdd(1)}>
          Add
        </button>
        <button type="button" className="extras-add__toggle" aria-expanded={open} aria-label={`Preview ${creature.name}`} onClick={onToggle}>
          {open ? '⌃' : '⌄'}
        </button>
      </div>
      {open && (
        <div className="extras-add__preview">
          <div className="extras-add__card">
            <div className="extras-add__card-name">{creature.name}</div>
            <div className="extras-add__card-meta">
              {data.size} {type}, {data.alignment}
            </div>
            <div className="extras-add__card-rule" />
            <div>
              <b>Armor Class</b> {data.armorClassText}
            </div>
            <div>
              <b>Hit Points</b> {data.hitPoints}
              {data.hitDice && ` (${data.hitDice})`}
            </div>
            <div>
              <b>Speed</b> {data.speed} ft.{data.additionalSpeeds && `, ${data.additionalSpeeds}`}
            </div>
            <div>
              <b>Challenge</b> {data.challengeRating}
            </div>
          </div>
          <div className="extras-add__quantity">
            <span>Quantity</span>
            <button type="button" aria-label="Fewer" onClick={() => setQuantity(Math.max(1, quantity - 1))}>
              −
            </button>
            <input
              aria-label="Quantity"
              inputMode="numeric"
              value={quantity}
              onChange={(event) => setQuantity(Math.min(MAX_QUANTITY, Math.max(1, Number(event.target.value.replace(/\D/g, '')) || 1)))}
            />
            <button type="button" aria-label="More" onClick={() => setQuantity(Math.min(MAX_QUANTITY, quantity + 1))}>
              +
            </button>
            <button type="button" className="mutate extras-add__add" onClick={() => onAdd(quantity)}>
              Add {quantity}
            </button>
          </div>
        </div>
      )}
    </li>
  );
}

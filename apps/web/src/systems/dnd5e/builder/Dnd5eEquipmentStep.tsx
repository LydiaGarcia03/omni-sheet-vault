import type { CreationChoice, StartingEquipment } from '../../../builder/draftApi';
import { Segmented } from '../../../builder/Segmented';
import { SourceSelect } from '../../../builder/SourceSelect';

type Dnd5eEquipmentStepProps = {
  choices: CreationChoice[];
  onAnswer: (choiceId: string, selections: string[]) => void;
  startingEquipment?: StartingEquipment;
  onEquip?: (lineKey: string, equipped: boolean) => void;
};

/** The starting items, with Wear/Wield toggles for what can start equipped, and the starting money, read-only. */
export function StartingInventory({ startingEquipment, onEquip }: { startingEquipment: StartingEquipment; onEquip: (lineKey: string, equipped: boolean) => void }) {
  const { inventory, gold, silver, copper } = startingEquipment;
  const coins = [
    [gold, 'gp'],
    [silver, 'sp'],
    [copper, 'cp'],
  ].filter(([amount]) => Number(amount) > 0);
  return (
    <>
      <section className="builder-frame">
        <div className="builder-frame__title">
          <span>Current inventory ({inventory.length})</span>
        </div>
        {inventory.length === 0 ? (
          <p className="builder-muted">Nothing yet. Choose your starting equipment above.</p>
        ) : (
          <>
            {inventory.map((item, index) => (
              <div key={`${item.key ?? item.name}-${index}`} className="builder-inventory__row">
                <span className="builder-grow">
                  <b>{item.name}</b>
                  {item.quantity > 1 && <span className="builder-muted"> × {item.quantity}</span>}
                </span>
                {item.equipAction && item.key && (
                  <button
                    type="button"
                    className={`builder-btn${item.equipped ? ' builder-btn--primary' : ''}`}
                    aria-pressed={item.equipped}
                    onClick={() => onEquip(item.key as string, !item.equipped)}
                  >
                    {item.equipped ? `✓ ${item.equipAction === 'WEAR' ? 'Worn' : 'Wielded'}` : item.equipAction === 'WEAR' ? 'Wear' : 'Wield'}
                  </button>
                )}
              </div>
            ))}
            <p className="builder-muted">What you wear or wield starts equipped on the sheet. The summary's armor class and attacks follow it.</p>
          </>
        )}
      </section>
      <section className="builder-frame">
        <div className="builder-frame__title">
          <span>Currency</span>
        </div>
        <p style={{ margin: 0 }}>{coins.length === 0 ? 'No starting money.' : coins.map(([amount, unit]) => `${amount} ${unit}`).join(' · ')}</p>
      </section>
    </>
  );
}

/**
 * D&D Beyond's starting-equipment layout: per class or background, equipment or gold as
 * buttons, each A-or-B pick as one line per option, and item picks inline under the line
 * that needs them.
 */
export function Dnd5eEquipmentStep({ choices, onAnswer, startingEquipment, onEquip }: Dnd5eEquipmentStepProps) {
  const fromClass = (source: string) => choices.some((choice) => choice.sourceLabel === source && choice.id.startsWith('class:'));
  const sources = [...new Set(choices.map((choice) => choice.sourceLabel))].sort((a, b) => Number(fromClass(b)) - Number(fromClass(a)));
  const inventory = startingEquipment && onEquip ? <StartingInventory startingEquipment={startingEquipment} onEquip={onEquip} /> : null;
  if (sources.length === 0) {
    return (
      <>
        <section className="builder-frame">
          <div className="builder-frame__title">Starting equipment</div>
          <p className="builder-muted">Choose a class and a background to see their starting equipment.</p>
        </section>
        {inventory}
      </>
    );
  }
  return (
    <>
      {sources.map((source) => (
        <SourcePackage key={source} source={source} choices={choices.filter((choice) => choice.sourceLabel === source)} onAnswer={onAnswer} />
      ))}
      {inventory}
    </>
  );
}

function SourcePackage({ source, choices, onAnswer }: { source: string; choices: CreationChoice[]; onAnswer: Dnd5eEquipmentStepProps['onAnswer'] }) {
  const method = choices.find((choice) => choice.type === 'EQUIPMENT_METHOD');
  const groups = choices.filter((choice) => choice.type === 'EQUIPMENT');
  const groupIds = new Set(groups.map((group) => group.id));
  const looseItems = choices.filter((choice) => choice.type === 'EQUIPMENT_ITEM' && !(choice.parentChoiceId && groupIds.has(choice.parentChoiceId)));
  const pending = choices.filter((choice) => choice.pending).length;
  const gold = method?.options.find((option) => option.key === 'gold');
  const takesGold = method?.selected[0] === 'gold';

  return (
    <section className="builder-frame">
      <div className="builder-frame__title">
        <span>{source} starting equipment</span>
        {pending > 0 ? <span className="builder-badge builder-badge--pending">{pending} left</span> : <span className="builder-badge builder-badge--done">done</span>}
      </div>
      {method && (
        <div className="builder-equip-method">
          <Segmented
            value={(method.selected[0] as 'equipment' | 'gold' | undefined) ?? null}
            options={[
              ['equipment', 'Equipment'],
              ['gold', 'Gold'],
            ]}
            onChange={(key) => onAnswer(method.id, [key])}
            ariaLabel={method.prompt}
          />
          {takesGold && gold?.summary && <span className="builder-muted">Starting gold: {gold.summary}</span>}
          {!method.selected.length && <span className="builder-muted">Take the class's equipment, or its gold to buy your own.</span>}
        </div>
      )}
      {!takesGold && (
        <>
          {groups.map((group, index) => (
            <EquipmentGroup
              key={group.id}
              group={group}
              label={`Choice ${index + 1}`}
              items={choices.filter((choice) => choice.parentChoiceId === group.id)}
              onAnswer={onAnswer}
            />
          ))}
          {looseItems.map((item) => (
            <ItemPick key={item.id} choice={item} onAnswer={onAnswer} />
          ))}
        </>
      )}
    </section>
  );
}

type EquipmentGroupProps = {
  group: CreationChoice;
  label: string;
  items: CreationChoice[];
  onAnswer: Dnd5eEquipmentStepProps['onAnswer'];
};

/** One A-or-B pick: a line per option; choosing one dims the others and opens its item picks below it. */
function EquipmentGroup({ group, label, items, onAnswer }: EquipmentGroupProps) {
  const picked = group.selected[0] ?? null;
  return (
    <div className="builder-equip-group" role="radiogroup" aria-label={label}>
      {group.options.map((option) => {
        const on = option.key === picked;
        return (
          <div key={option.key} className={`builder-equip-option${on ? ' is-on' : ''}${picked && !on ? ' is-dim' : ''}`}>
            <button type="button" role="radio" aria-checked={on} className="builder-equip-option__line" onClick={() => onAnswer(group.id, on ? [] : [option.key])}>
              <span className="builder-equip-option__box" aria-hidden="true">
                {on && '✓'}
              </span>
              <span>{option.label}</span>
            </button>
            {on && items.map((item) => <ItemPick key={item.id} choice={item} onAnswer={onAnswer} />)}
          </div>
        );
      })}
    </div>
  );
}

/** "Choose 2 martial weapons": one select per item, so the same weapon can be taken twice. */
function ItemPick({ choice, onAnswer }: { choice: CreationChoice; onAnswer: Dnd5eEquipmentStepProps['onAnswer'] }) {
  const slots: (string | null)[] = Array.from({ length: choice.count }, (_, index) => choice.selected[index] ?? null);
  const set = (index: number, key: string | null) => {
    const next = [...slots];
    next[index] = key;
    onAnswer(choice.id, next.filter((value): value is string => value !== null));
  };
  return (
    <div className={`builder-equip-item${choice.pending ? ' is-pending' : ''}`}>
      <span className="builder-equip-item__label">{choice.prompt}</span>
      {slots.map((value, index) => (
        <SourceSelect
          key={index}
          options={choice.options}
          value={value}
          onChange={(key) => set(index, key)}
          placeholder="— choose —"
          ariaLabel={choice.count > 1 ? `${choice.prompt} (${index + 1})` : choice.prompt}
          clearable
        />
      ))}
    </div>
  );
}

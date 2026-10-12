import { useEffect, useState } from 'react';
import type { CreationChoice, StartingEquipment } from '../../../builder/draftApi';
import { postCreationRoll } from '../../../dice/api';
import { PendingMark } from '../../../builder/PendingMark';
import { Segmented } from '../../../builder/Segmented';
import { SourceSelect } from '../../../builder/SourceSelect';

type Dnd5eEquipmentStepProps = {
  characterId: string;
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
export function Dnd5eEquipmentStep({ characterId, choices, onAnswer, startingEquipment, onEquip }: Dnd5eEquipmentStepProps) {
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
        <SourcePackage
          key={source}
          characterId={characterId}
          source={source}
          choices={choices.filter((choice) => choice.sourceLabel === source)}
          onAnswer={onAnswer}
        />
      ))}
      {inventory}
    </>
  );
}

type SourcePackageProps = { characterId: string; source: string; choices: CreationChoice[]; onAnswer: Dnd5eEquipmentStepProps['onAnswer'] };

function SourcePackage({ characterId, source, choices, onAnswer }: SourcePackageProps) {
  const method = choices.find((choice) => choice.type === 'EQUIPMENT_METHOD');
  const groups = choices.filter((choice) => choice.type === 'EQUIPMENT');
  const groupIds = new Set(groups.map((group) => group.id));
  const looseItems = choices.filter((choice) => choice.type === 'EQUIPMENT_ITEM' && !(choice.parentChoiceId && groupIds.has(choice.parentChoiceId)));
  const pending = choices.filter((choice) => choice.pending).length;
  const gold = method?.options.find((option) => option.key === 'gold');
  const takesGold = method?.selected[0] === 'gold';
  const goldRoll = choices.find((choice) => choice.type === 'STARTING_GOLD');

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
          {takesGold && !goldRoll && gold?.summary && <span className="builder-muted">Starting gold: {gold.summary}</span>}
          {!method.selected.length && <span className="builder-muted">Take the class's equipment, or its gold to buy your own.</span>}
        </div>
      )}
      {takesGold && goldRoll && <StartingGoldRoll characterId={characterId} choice={goldRoll} onAnswer={onAnswer} />}
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

const OPTION_LETTER = /^\((\w)\)\s*/;

/** One A-or-B pick: a titled radio list with "or" between options; choosing one dims the others and opens its item picks below it. */
function EquipmentGroup({ group, label, items, onAnswer }: EquipmentGroupProps) {
  const picked = group.selected[0] ?? null;
  return (
    <div className="builder-equip-group">
      <div className="builder-equip-group__head">
        <span className="builder-equip-group__title">{label}</span>
        <span className="builder-muted">Pick one of {group.options.length}</span>
        {group.pending && <PendingMark />}
      </div>
      <div role="radiogroup" aria-label={label}>
        {group.options.map((option, index) => {
          const on = option.key === picked;
          const letter = OPTION_LETTER.exec(option.label)?.[1];
          return (
            <div key={option.key}>
              {index > 0 && (
                <div className="builder-equip-or" aria-hidden="true">
                  <span>or</span>
                </div>
              )}
              <div className={`builder-equip-option${on ? ' is-on' : ''}${picked && !on ? ' is-dim' : ''}`}>
                <button type="button" role="radio" aria-checked={on} aria-label={option.label} className="builder-equip-option__line" onClick={() => onAnswer(group.id, on ? [] : [option.key])}>
                  <span className="builder-equip-option__box" aria-hidden="true" />
                  {letter && <span className="builder-equip-option__letter">{letter.toUpperCase()}</span>}
                  <span>{letter ? option.label.replace(OPTION_LETTER, '') : option.label}</span>
                </button>
                {on && items.map((item) => <ItemPick key={item.id} choice={item} onAnswer={onAnswer} />)}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

type GoldRules = { count: number; sides: number; multiplier: number; rollLabel: string };

type StartingGoldRollProps = { characterId: string; choice: CreationChoice; onAnswer: Dnd5eEquipmentStepProps['onAnswer'] };

/** The class's gold dice: a typed or rolled total, shown multiplied into gold pieces. */
function StartingGoldRoll({ characterId, choice, onAnswer }: StartingGoldRollProps) {
  const rules = choice.options[0]?.data as GoldRules | undefined;
  const answered = choice.selected[0] ?? null;
  const [typed, setTyped] = useState<string>(answered ?? '');
  const [rolling, setRolling] = useState(false);
  const [rollError, setRollError] = useState<string | null>(null);
  useEffect(() => {
    if (answered !== null) {
      setTyped(answered);
    }
  }, [answered]);

  if (!rules) {
    return null;
  }
  const dice = `${rules.count}d${rules.sides}`;
  const minimum = rules.count;
  const maximum = rules.count * rules.sides;
  const offered = (value: string) => choice.options.some((option) => option.key === value);

  const type = (raw: string) => {
    const digits = raw.replace(/\D/g, '');
    setTyped(digits);
    const total = String(Number(digits));
    onAnswer(choice.id, digits !== '' && offered(total) ? [total] : []);
  };

  const roll = async () => {
    setRolling(true);
    setRollError(null);
    try {
      const result = await postCreationRoll(characterId, { sides: rules.sides, count: rules.count, keepHighest: null, context: rules.rollLabel });
      onAnswer(choice.id, [String(result.total)]);
    } catch {
      setRollError('The roll failed. Try again.');
    } finally {
      setRolling(false);
    }
  };

  const outOfRange = typed !== '' && !offered(String(Number(typed)));
  const multiplied = rules.multiplier > 1 ? ` × ${rules.multiplier}` : '';
  return (
    <div className="builder-equip-gold">
      <p className="builder-hp__subtitle">
        {choice.prompt}: {dice}
        {multiplied} gp
        {choice.pending && <PendingMark />}
      </p>
      <div className="builder-equip-gold__row">
        <input
          className="builder-input builder-input--number"
          type="text"
          inputMode="numeric"
          maxLength={3}
          aria-label={`Starting gold roll (${dice})`}
          value={typed}
          onChange={(event) => type(event.target.value)}
        />
        <button type="button" className="builder-btn" disabled={rolling} onClick={roll}>
          {rolling ? 'Rolling…' : `Roll ${dice}`}
        </button>
        <span className="builder-equip-gold__result">
          {answered ? (
            <>
              {answered}
              {multiplied} = <b>{Number(answered) * rules.multiplier} gp</b>
            </>
          ) : (
            <span className="builder-muted">Average would be {Math.floor((rules.count * (rules.sides + 1) * rules.multiplier) / 2)} gp</span>
          )}
        </span>
      </div>
      {outOfRange && (
        <p className="builder-problem">
          {dice} rolls between {minimum} and {maximum}.
        </p>
      )}
      {rollError && <p className="builder-problem">{rollError}</p>}
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
      <span className="builder-equip-item__label">
        {choice.prompt}
        {choice.pending && <PendingMark />}
      </span>
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

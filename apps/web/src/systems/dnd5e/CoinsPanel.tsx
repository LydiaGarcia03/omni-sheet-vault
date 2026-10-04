import { useState } from 'react';
import type { Coins, MutationHandler } from '../../sheet/api';
import { FrameIcon } from './FrameIcon';
import minusIconSvg from './frames/dnd_icon_minus.svg?raw';
import plusIconSvg from './frames/dnd_icon_plus.svg?raw';

type CoinsPanelProps = {
  coins: Coins;
  onMutate: MutationHandler;
};

const DENOMINATIONS: { key: string; label: string; value: (coins: Coins) => number }[] = [
  { key: 'copper', label: 'CP', value: (coins) => coins.copper },
  { key: 'silver', label: 'SP', value: (coins) => coins.silver },
  { key: 'electrum', label: 'EP', value: (coins) => coins.electrum },
  { key: 'gold', label: 'GP', value: (coins) => coins.gold },
  { key: 'platinum', label: 'PP', value: (coins) => coins.platinum },
];

/**
 * No coin settings (lifestyle/expenses are cosmetic and omitted, per
 * systems/dnd-5e/sheet-ui.md's deviations list) — just totals per denomination, plus
 * add/remove, per the mold's own spec. One shared amount field feeds all five
 * rows' add/remove buttons, the same pattern HitPoints already uses for its
 * damage/heal/temp buttons.
 */
export function CoinsPanel({ coins, onMutate }: CoinsPanelProps) {
  const [amount, setAmount] = useState('');
  const parsedAmount = Number(amount) || 0;

  return (
    <div className="coins-panel">
      <input
        type="number"
        min={1}
        value={amount}
        onChange={(event) => setAmount(event.target.value)}
        placeholder="Amount"
        aria-label="Coin amount"
        className="coins-panel__amount"
      />
      {DENOMINATIONS.map(({ key, label, value }) => (
        <div key={key} className="coins-panel__row">
          <span className="coins-panel__label">{label}</span>
          <span className="coins-panel__value">{value(coins)}</span>
          <button
            type="button"
            className="mutate coins-panel__button"
            disabled={parsedAmount <= 0}
            aria-label={`Add ${label}`}
            onClick={() => onMutate({ type: 'ADD_COINS', denomination: key, amount: parsedAmount })}
          >
            <FrameIcon svg={plusIconSvg} className="stepper-icon" aria-hidden />
          </button>
          <button
            type="button"
            className="mutate coins-panel__button"
            disabled={parsedAmount <= 0}
            aria-label={`Remove ${label}`}
            onClick={() => onMutate({ type: 'REMOVE_COINS', denomination: key, amount: parsedAmount })}
          >
            <FrameIcon svg={minusIconSvg} className="stepper-icon" aria-hidden />
          </button>
        </div>
      ))}
    </div>
  );
}

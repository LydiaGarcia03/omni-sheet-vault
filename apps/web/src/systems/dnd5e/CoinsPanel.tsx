import { useState } from 'react';
import type { Coins, MutationHandler } from '../../sheet/api';
import { FrameIcon } from './FrameIcon';
import minusIconSvg from './frames/dnd_icon_minus.svg?raw';
import plusIconSvg from './frames/dnd_icon_plus.svg?raw';
import { useDnd5eText } from './locales/dnd5eMessages';

type CoinsPanelProps = {
  coins: Coins;
  onMutate: MutationHandler;
};

const DENOMINATIONS = [
  { key: 'copper', label: 'terms.coins.copper', value: (coins: Coins) => coins.copper },
  { key: 'silver', label: 'terms.coins.silver', value: (coins: Coins) => coins.silver },
  { key: 'electrum', label: 'terms.coins.electrum', value: (coins: Coins) => coins.electrum },
  { key: 'gold', label: 'terms.coins.gold', value: (coins: Coins) => coins.gold },
  { key: 'platinum', label: 'terms.coins.platinum', value: (coins: Coins) => coins.platinum },
] as const;

/**
 * No coin settings (lifestyle/expenses are cosmetic and omitted, per
 * systems/dnd-5e/sheet-ui.md's deviations list) — just totals per denomination, plus
 * add/remove, per the mold's own spec. One shared amount field feeds all five
 * rows' add/remove buttons, the same pattern HitPoints already uses for its
 * damage/heal/temp buttons.
 */
export function CoinsPanel({ coins, onMutate }: CoinsPanelProps) {
  const t = useDnd5eText();
  const [amount, setAmount] = useState('');
  const parsedAmount = Number(amount) || 0;

  return (
    <div className="coins-panel">
      <input
        type="number"
        min={1}
        value={amount}
        onChange={(event) => setAmount(event.target.value)}
        placeholder={t('inventory.amount')}
        aria-label={t('inventory.coinAmount')}
        className="coins-panel__amount"
      />
      {DENOMINATIONS.map(({ key, label, value }) => (
        <div key={key} className="coins-panel__row">
          <span className="coins-panel__label">{t(label)}</span>
          <span className="coins-panel__value">{value(coins)}</span>
          <button
            type="button"
            className="mutate coins-panel__button"
            disabled={parsedAmount <= 0}
            aria-label={t('inventory.addCoin', { coin: t(label) })}
            onClick={() => onMutate({ type: 'ADD_COINS', denomination: key, amount: parsedAmount })}
          >
            <FrameIcon svg={plusIconSvg} className="stepper-icon" aria-hidden />
          </button>
          <button
            type="button"
            className="mutate coins-panel__button"
            disabled={parsedAmount <= 0}
            aria-label={t('inventory.removeCoin', { coin: t(label) })}
            onClick={() => onMutate({ type: 'REMOVE_COINS', denomination: key, amount: parsedAmount })}
          >
            <FrameIcon svg={minusIconSvg} className="stepper-icon" aria-hidden />
          </button>
        </div>
      ))}
    </div>
  );
}

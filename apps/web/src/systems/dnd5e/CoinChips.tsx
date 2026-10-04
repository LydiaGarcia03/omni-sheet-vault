import type { Coins } from '../../sheet/api';

type CoinChipsProps = {
  coins: Coins;
  onOpenCoins: () => void;
};

const DENOMINATIONS: { key: string; label: string; value: (coins: Coins) => number }[] = [
  { key: 'platinum', label: 'PP', value: (coins) => coins.platinum },
  { key: 'gold', label: 'GP', value: (coins) => coins.gold },
  { key: 'electrum', label: 'EP', value: (coins) => coins.electrum },
  { key: 'silver', label: 'SP', value: (coins) => coins.silver },
  { key: 'copper', label: 'CP', value: (coins) => coins.copper },
];

/**
 * Compact per-denomination chips at the right of the Inventory tab's weight row, each count before its marker as on
 * D&D Beyond (Roboto Condensed 700 14px, a 16px marker) — confirmed
 * live (phase 10 slice 12) against D&D Beyond's own coin button
 * (`ct-currency-button`, `role="button" aria-label="Manage Coin..."`): a
 * single clickable control showing only nonzero denominations, opening a
 * coin-management panel on click. Reuses `CoinsPanel`'s existing add/remove
 * controls as the sidebar's `body` (`EntityDetailRequest`, phase 10 slice 10)
 * rather than rebuilding them — the chips replace where the controls surface
 * from, not the controls themselves. Each chip's dot is a plain colored
 * circle, not an imported icon (same "no asset exists" treatment as
 * DamageTypeIcon) — conventional metal tones, not copied from D&D Beyond's
 * own coin artwork (ui-design-system.md's "no UI assets copied from another
 * product").
 */
export function CoinChips({ coins, onOpenCoins }: CoinChipsProps) {
  const visible = DENOMINATIONS.filter((denomination) => denomination.value(coins) > 0);

  if (visible.length === 0) {
    return null;
  }

  return (
    <button type="button" className="reveal coin-chips" aria-label="Manage coins" onClick={onOpenCoins}>
      {visible.map((denomination) => (
        <span key={denomination.key} className="coin-chip" title={denomination.label}>
          {denomination.value(coins)}
          <span className={`coin-chip__dot coin-chip__dot--${denomination.key}`} aria-hidden="true" />
        </span>
      ))}
    </button>
  );
}

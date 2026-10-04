import { useState } from 'react';
import { FrameIcon } from '../systems/dnd5e/FrameIcon';
import closeIconSvg from '../systems/dnd5e/frames/dnd_icon_close.svg?raw';
import diceIconSvg from '../systems/dnd5e/frames/dnd_icon_dice_roll.svg?raw';
import d4IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d4.svg?raw';
import d6IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d6.svg?raw';
import d8IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d8.svg?raw';
import d10IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d10.svg?raw';
import d12IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d12.svg?raw';
import d20IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d20.svg?raw';
import d100IconSvg from '../systems/dnd5e/frames/dnd_icon_die_d100.svg?raw';
import type { DiceGroup, DieType } from './api';

const DIE_TYPES: DieType[] = ['D20', 'D12', 'D100', 'D10', 'D8', 'D6', 'D4'];
const MAX_PER_DIE = 20;

/**
 * Direct owner request, 2026-08-18: each die button in the picker grid gets
 * its own shape icon above the `d4`/`d6`/... label, matching D&D Beyond's
 * own die-select buttons (icon + name), instead of the plain text-only
 * buttons this picker shipped with. Originally shipped as six original flat
 * silhouettes (Flaticon's ready-made polyhedral set needed attribution this
 * app has nowhere to give, and no CC0/MIT/Apache set turned up either);
 * briefly replaced 2026-09-07 with `@dndbeyond/game-log-components`'s
 * wireframe `DieIcon` set (a chat-log icon family, wrong context) before
 * being corrected the same week to the icons D&D Beyond's own "Roll Dice"
 * popover actually uses — DOM-measured live against
 * dndbeyond.com/characters/50149479's real dice-select grid: these are
 * `@dndbeyond/fontawesome-cache`'s "light" style dice icons (`dice-d20`
 * confirmed byte-for-byte against this project's own cached copy; d12/d10/
 * d8/d6/d4 pulled live since the cache only had d20). D100 gets its own icon
 * (two D10s side by side) taken from the same live grid, instead of reusing
 * the D10 shape.
 */
const DIE_ICONS: Record<DieType, string> = {
  D4: d4IconSvg,
  D6: d6IconSvg,
  D8: d8IconSvg,
  D10: d10IconSvg,
  D12: d12IconSvg,
  D20: d20IconSvg,
  D100: d100IconSvg,
};

const EMPTY_COUNTS: Record<DieType, number> = { D4: 0, D6: 0, D8: 0, D10: 0, D12: 0, D20: 0, D100: 0 };

type CustomRollPickerProps = {
  onRoll: (dice: DiceGroup[]) => void;
};

/**
 * The bottom-left "manual/custom rolling" picker deferred since phase 5 — see
 * systems/dnd-5e/sheet-ui.md's "Deferred to later versions". Verified live against D&D
 * Beyond before building: a die button's count increments on click and decrements
 * on right-click, kept here on the same real `<button>` (systems/dnd-5e/sheet-build.md's
 * "every clickable is a button" — right-click is a second gesture on a real button,
 * not a hidden non-button target), with `aria-label`/`title` covering both gestures
 * since right-click has no visual affordance of its own. No 3D dice — results go
 * through the existing DiceTray text display, unchanged.
 *
 * **Redesigned 2026-09-08, direct owner request** ("faça com que fique
 * parecido"): the panel itself (header, colors, die-button grid, Reset/Roll/
 * Clear Dice) now matches D&D Beyond's own dark "Roll Dice" popover
 * (`@dndbeyond/pocket-dimension-dice`'s `CustomDiceRoller`, DOM-measured
 * live — see `dice.css`'s own header comment for the full measurement
 * notes). Its dice-set switcher (an avatar, a dice-set name, "Change Dice")
 * is deliberately left out: that row lets the player pick between 3D dice
 * skins, a feature this app has no equivalent of (no 3D rendering at all,
 * per the paragraph above). "Clear Dice" is new here — the reference has it
 * as a distinct action from "Reset" (a plain-text link, not a button, sat
 * bottom-right of the grid); this app treats it as resetting the counts and
 * closing the panel in one step, since "Reset" alone already covers
 * clearing counts while staying open to keep picking.
 */
export function CustomRollPicker({ onRoll }: CustomRollPickerProps) {
  const [open, setOpen] = useState(false);
  const [counts, setCounts] = useState<Record<DieType, number>>(EMPTY_COUNTS);

  const hasSelection = DIE_TYPES.some((type) => counts[type] > 0);

  function increment(type: DieType) {
    setCounts((current) => ({ ...current, [type]: Math.min(MAX_PER_DIE, current[type] + 1) }));
  }

  function decrement(type: DieType) {
    setCounts((current) => ({ ...current, [type]: Math.max(0, current[type] - 1) }));
  }

  function roll() {
    const dice: DiceGroup[] = DIE_TYPES.filter((type) => counts[type] > 0).map((type) => ({ type, count: counts[type] }));
    onRoll(dice);
    setCounts(EMPTY_COUNTS);
    setOpen(false);
  }

  function clearDice() {
    setCounts(EMPTY_COUNTS);
    setOpen(false);
  }

  return (
    <div className="custom-roll-picker">
      {open && (
        <div className="custom-roll-picker__panel">
          <div className="custom-roll-picker__header">
            Roll Dice
            <button type="button" className="custom-roll-picker__close" aria-label="Close" onClick={() => setOpen(false)}>
              <FrameIcon svg={closeIconSvg} />
            </button>
          </div>
          <div className="custom-roll-picker__grid">
            {DIE_TYPES.map((type) => {
              const count = counts[type];
              const label = type.toLowerCase();
              const className = count > 0 ? 'custom-roll-picker__die custom-roll-picker__die--selected' : 'custom-roll-picker__die';
              return (
                <button
                  key={type}
                  type="button"
                  className={className}
                  aria-label={`${label}, ${count} selected. Click to add, right-click to remove.`}
                  title={`${label} — click to add, right-click to remove`}
                  onClick={() => increment(type)}
                  onContextMenu={(event) => {
                    event.preventDefault();
                    decrement(type);
                  }}
                >
                  <FrameIcon svg={DIE_ICONS[type]} className="custom-roll-picker__die-icon" />
                  <span>{label}</span>
                  {count > 0 && <span className="custom-roll-picker__count">{count}</span>}
                </button>
              );
            })}
          </div>
          <div className="custom-roll-picker__actions">
            <button
              type="button"
              className="custom-roll-picker__reset"
              disabled={!hasSelection}
              onClick={() => setCounts(EMPTY_COUNTS)}
            >
              Reset
            </button>
            <button type="button" className="custom-roll-picker__roll" disabled={!hasSelection} onClick={roll}>
              Roll
            </button>
          </div>
          <div className="custom-roll-picker__clear-row">
            <button type="button" className="custom-roll-picker__clear" disabled={!hasSelection} onClick={clearDice}>
              Clear Dice
            </button>
          </div>
        </div>
      )}
      <button
        type="button"
        className="custom-roll-picker__trigger"
        aria-label="Roll custom dice"
        aria-expanded={open}
        onClick={() => setOpen((current) => !current)}
      >
        <FrameIcon svg={diceIconSvg} className="custom-roll-picker__trigger-icon" />
      </button>
    </div>
  );
}

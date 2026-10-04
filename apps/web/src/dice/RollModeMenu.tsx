import { useEffect, useRef, useState } from 'react';
import { FrameIcon } from '../systems/dnd5e/FrameIcon';
import advantageSvg from '../systems/dnd5e/frames/dnd_icon_advantage.svg?raw';
import disadvantageSvg from '../systems/dnd5e/frames/dnd_icon_disadvantage.svg?raw';
import normalSvg from '../systems/dnd5e/frames/dnd_icon_die_d20.svg?raw';
import type { RollKind } from './api';

export type RollModeRequest = {
  x: number;
  y: number;
  kind: RollKind;
  key: string | null;
  /** The already-known modifier, e.g. "+4" — every eligible kind is a flat 1d20 roll. */
  formattedModifier: string;
};

type RollModeMenuProps = {
  request: RollModeRequest;
  onConfirm: (kind: RollKind, key: string | null, advantage: boolean, disadvantage: boolean) => void;
  onClose: () => void;
};

type Mode = 'advantage' | 'normal' | 'disadvantage';

/**
 * The right-click "ROLL WITH: Advantage/Flat/Disadvantage" popover D&D Beyond opens on
 * any d20 check, save or attack — DOM-extracted live from dndbeyond.com/characters/50149479
 * (icons, colors, the picked-mode-until-confirmed flow). See ground-rules.md's Dice
 * section and systems/dnd-5e/sheet-ui.md's "Rolling with advantage/disadvantage".
 *
 * Deliberately narrower than the reference: D&D Beyond's own menu also carries a
 * "SEND TO: Everyone/Self" section above this one — left out, this app has no
 * chat/whisper concept to send a roll to. The reference's own checkmark icon on the
 * selected row is replaced with a plain highlight here rather than extracting a
 * fourth icon for it.
 */
export function RollModeMenu({ request, onConfirm, onClose }: RollModeMenuProps) {
  const [mode, setMode] = useState<Mode>('normal');
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        onClose();
      }
    }
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onClose();
      }
    }
    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [onClose]);

  function itemClassName(itemMode: Mode): string {
    return mode === itemMode ? 'roll-mode-menu__item roll-mode-menu__item--selected' : 'roll-mode-menu__item';
  }

  function confirm() {
    onConfirm(request.kind, request.key, mode === 'advantage', mode === 'disadvantage');
    onClose();
  }

  return (
    <div className="roll-mode-menu" ref={menuRef} style={{ left: request.x, top: request.y }}>
      <div className="roll-mode-menu__header">ROLL WITH:</div>
      <button type="button" className={itemClassName('advantage')} onClick={() => setMode('advantage')}>
        <FrameIcon svg={advantageSvg} className="roll-mode-menu__icon" />
        <span>Advantage</span>
      </button>
      <button type="button" className={itemClassName('normal')} onClick={() => setMode('normal')}>
        <FrameIcon svg={normalSvg} className="roll-mode-menu__icon roll-mode-menu__icon--flat" />
        <span>Flat (One Die)</span>
      </button>
      <button type="button" className={itemClassName('disadvantage')} onClick={() => setMode('disadvantage')}>
        <FrameIcon svg={disadvantageSvg} className="roll-mode-menu__icon" />
        <span>Disadvantage</span>
      </button>
      <button type="button" className="roll-mode-menu__confirm" onClick={confirm}>
        <FrameIcon svg={normalSvg} className="roll-mode-menu__confirm-icon" />
        ROLL 1d20{request.formattedModifier}
      </button>
    </div>
  );
}

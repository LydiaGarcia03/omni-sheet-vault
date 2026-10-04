import { useEffect, useState, type ReactNode } from 'react';
import { CharacterPortrait } from '../characters/CharacterPortrait';
import type { Portrait } from '../characters/portrait';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';

type SheetShellProps = {
  characterName: string;
  portrait: Portrait | null;
  systemId: string;
  level: number;
  userLabel: string;
  onOpenLog: () => void;
  onOpenShortRest: () => void;
  onOpenLongRest: () => void;
  shortRestIcon: ReactNode;
  longRestIcon: ReactNode;
  gameLogIcon: ReactNode;
  /** Shown under the level line (D&D 5e: the experience bar). */
  progress?: ReactNode;
  /** Makes the portrait and the name open the character sidebar. */
  onOpenCharacter?: () => void;
  /** Drawn beside the portrait (D&D 5e: the level up arrow). */
  portraitBadge?: ReactNode;
  /** The system's page backdrop (D&D 5e: the parchment). */
  pageClassName?: string;
  children: ReactNode;
};

/**
 * Shared shell every game system renders inside — see ui-design-system.md.
 * The app header sits on top, as D&D Beyond's site bar sits above its character header.
 * Header spec: ui-design-tokens.md's "Character header". Rest buttons open the
 * sidebar's Mechanic mold rather than acting immediately.
 */
export function SheetShell({
  characterName,
  portrait,
  systemId,
  level,
  userLabel,
  onOpenLog,
  onOpenShortRest,
  onOpenLongRest,
  shortRestIcon,
  longRestIcon,
  gameLogIcon,
  progress,
  onOpenCharacter,
  portraitBadge,
  pageClassName,
  children,
}: SheetShellProps) {
  const [column, setColumn] = useState<HTMLDivElement | null>(null);
  const columnWidth = useElementWidth(column);
  return (
    <>
      <AppHeader userName={userLabel} backToListWithin={columnWidth > 0 ? columnWidth : undefined} compact systemId={systemId} />
      <div className={pageClassName} style={{ position: 'relative', padding: '0 24px 24px', fontFamily: 'var(--font-body, Roboto, Helvetica, sans-serif)' }}>
        <div ref={setColumn} style={{ width: 'fit-content', margin: '0 auto' }}>
          <header className="sheet-header">
            <div className="sheet-header__identity">
              {onOpenCharacter ? (
                <button type="button" className="sheet-header__portrait-button" onClick={onOpenCharacter} aria-label={`Open ${characterName}'s character menu`}>
                  <CharacterPortrait portrait={portrait} systemId={systemId} className="sheet-header__portrait" alt="" />
                </button>
              ) : (
                <CharacterPortrait portrait={portrait} systemId={systemId} className="sheet-header__portrait" alt={`${characterName}'s portrait`} />
              )}
              {portraitBadge}
            </div>
            <div
              className={onOpenCharacter ? 'sheet-header__tidbits is-clickable' : 'sheet-header__tidbits'}
              onClick={onOpenCharacter}
            >
              <div className="sheet-header__name">{characterName}</div>
              <div className="sheet-header__level">Level {level}</div>
              {progress}
            </div>
            <div className="sheet-header__actions">
              <button type="button" className="sheet-header__button" onClick={onOpenShortRest}>
                <span className="sheet-header__button-icon">{shortRestIcon}</span>
                Short Rest
              </button>
              <button type="button" className="sheet-header__button" onClick={onOpenLongRest}>
                <span className="sheet-header__button-icon">{longRestIcon}</span>
                Long Rest
              </button>
              <button
                type="button"
                className="sheet-header__button sheet-header__button--icon-only"
                onClick={onOpenLog}
                aria-label="Game log"
                title="Game log"
              >
                <span className="sheet-header__button-icon">{gameLogIcon}</span>
              </button>
            </div>
          </header>
          {/* No padding reserved for the fixed dice tray — it doesn't overlap the
              centered sheet at normal desktop widths. */}
          <main>{children}</main>
        </div>
      </div>
      <AppFooter />
    </>
  );
}

/** The element's rendered width in px, kept current as the layout changes; 0 before it mounts. */
function useElementWidth(element: HTMLElement | null): number {
  const [width, setWidth] = useState(0);
  useEffect(() => {
    if (!element) {
      return;
    }
    setWidth(element.getBoundingClientRect().width);
    if (typeof ResizeObserver === 'undefined') {
      return;
    }
    const observer = new ResizeObserver(() => setWidth(element.getBoundingClientRect().width));
    observer.observe(element);
    return () => observer.disconnect();
  }, [element]);
  return width;
}

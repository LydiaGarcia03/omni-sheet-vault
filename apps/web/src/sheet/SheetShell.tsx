import { useEffect, useState, type ReactNode } from 'react';
import { CharacterPortrait } from '../characters/CharacterPortrait';
import type { Portrait } from '../characters/portrait';
import { useSheetText } from '../locales/platformMessages';
import { AppFooter } from '../shell/AppFooter';
import { AppHeader } from '../shell/AppHeader';

type SheetShellProps = {
  characterName: string;
  portrait: Portrait | null;
  systemId: string;
  /** The line under the name (D&D 5e: each class with its level). */
  summary: string;
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
  /** Px kept free on the right (a locked sidebar); the sheet then scales down to fit beside it, to MIN_SCALE at most. */
  reserveRight?: number;
  /** Rendered beside the sheet at real size, never scaled: the sidebar, the dice tray, menus. */
  overlay?: ReactNode;
  children: ReactNode;
};

const PAGE_GUTTER = 24;
const MIN_SCALE = 0.85;

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
  summary,
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
  reserveRight = 0,
  overlay,
  children,
}: SheetShellProps) {
  const t = useSheetText();
  const [page, setPage] = useState<HTMLDivElement | null>(null);
  const [column, setColumn] = useState<HTMLDivElement | null>(null);
  const columnWidth = useElementWidth(column);
  const pageWidth = useElementWidth(page);
  const scale = useFitScale(column, columnWidth, pageWidth - PAGE_GUTTER - (reserveRight > 0 ? reserveRight : PAGE_GUTTER), reserveRight > 0);
  return (
    <>
      <AppHeader userName={userLabel} backToListWithin={columnWidth > 0 ? columnWidth : undefined} compact systemId={systemId} />
      <div
        ref={setPage}
        className={pageClassName}
        style={{
          position: 'relative',
          padding: `0 ${reserveRight > 0 ? reserveRight : PAGE_GUTTER}px ${PAGE_GUTTER}px ${PAGE_GUTTER}px`,
          fontFamily: 'var(--font-body, Roboto, Helvetica, sans-serif)',
        }}
      >
        <div ref={setColumn} className="sheet-column" style={{ width: 'fit-content', margin: '0 auto', zoom: scale < 1 ? scale : undefined }}>
          <header className="sheet-header">
            <div className="sheet-header__identity">
              {onOpenCharacter ? (
                <button type="button" className="sheet-header__portrait-button" onClick={onOpenCharacter} aria-label={t('header.openMenu', { name: characterName })}>
                  <CharacterPortrait portrait={portrait} systemId={systemId} className="sheet-header__portrait" alt="" />
                </button>
              ) : (
                <CharacterPortrait portrait={portrait} systemId={systemId} className="sheet-header__portrait" alt={t('header.portrait', { name: characterName })} />
              )}
              {portraitBadge}
            </div>
            <div
              className={onOpenCharacter ? 'sheet-header__tidbits is-clickable' : 'sheet-header__tidbits'}
              onClick={onOpenCharacter}
            >
              <div className="sheet-header__name">{characterName}</div>
              <div className="sheet-header__level" title={summary}>{summary}</div>
              {progress}
            </div>
            <div className="sheet-header__actions">
              <button type="button" className="sheet-header__button" onClick={onOpenShortRest}>
                <span className="sheet-header__button-icon">{shortRestIcon}</span>
                {t('terms.shortRest')}
              </button>
              <button type="button" className="sheet-header__button" onClick={onOpenLongRest}>
                <span className="sheet-header__button-icon">{longRestIcon}</span>
                {t('terms.longRest')}
              </button>
              <button
                type="button"
                className="sheet-header__button sheet-header__button--icon-only"
                onClick={onOpenLog}
                aria-label={t('header.gameLog')}
                title={t('header.gameLog')}
              >
                <span className="sheet-header__button-icon">{gameLogIcon}</span>
              </button>
            </div>
          </header>
          {/* No padding reserved for the fixed dice tray — it doesn't overlap the
              centered sheet at normal desktop widths. */}
          <main>{children}</main>
        </div>
        {overlay}
      </div>
      <AppFooter rulesText />
    </>
  );
}

/** The zoom that fits the column into `available` px (between MIN_SCALE and 1), or 1 when fitting is off. */
function useFitScale(column: HTMLElement | null, renderedWidth: number, available: number, enabled: boolean): number {
  const [scale, setScale] = useState(1);
  useEffect(() => {
    if (!enabled || !column || renderedWidth <= 0 || available <= 0) {
      setScale(1);
      return;
    }
    setScale((current) => {
      const natural = renderedWidth / current;
      return Math.max(MIN_SCALE, Math.min(1, Math.floor((available / natural) * 100) / 100));
    });
  }, [column, renderedWidth, available, enabled]);
  return scale;
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

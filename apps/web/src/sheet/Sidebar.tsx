import { useEffect, useRef, useState } from 'react';
import { useSheetText } from '../locales/platformMessages';
import type { SidebarContent } from './api';
import { LockIcon, SidebarBorder, SidebarLeftIcon, SidebarRightIcon, UnlockIcon } from './sidebarIcons';
import { ChangeAppearancePanel } from './ChangeAppearancePanel';
import { CharacterMenuPanel } from './CharacterMenuPanel';
import { ConditionsPanel } from './ConditionsPanel';
import { EntityDetailPanel } from './EntityDetailPanel';
import { ExplainerPanel } from './ExplainerPanel';
import { FeatManagementPanel } from './FeatManagementPanel';
import { HpManagementPanel } from './HpManagementPanel';
import { LogPanel } from './LogPanel';
import { ManageExperiencePanel } from './ManageExperiencePanel';
import { ManageCustomActionsPanel } from './ManageCustomActionsPanel';
import { ManageInventoryPanel } from './ManageInventoryPanel';
import { MechanicPanel } from './MechanicPanel';
import { SpellManagementPanel } from './SpellManagementPanel';
import { TextFieldPanel } from './TextFieldPanel';

function renderContent(content: SidebarContent) {
  switch (content.kind) {
    case 'pane':
      return content.content ?? null;
    case 'characterMenu':
      return <CharacterMenuPanel {...content} />;
    case 'manageExperience':
      return <ManageExperiencePanel {...content} />;
    case 'changeAppearance':
      return <ChangeAppearancePanel {...content} />;
    case 'explainer':
      return <ExplainerPanel {...content} />;
    case 'entityDetail':
      return <EntityDetailPanel key={content.entityKey} {...content} />;
    case 'log':
      return <LogPanel {...content} />;
    case 'spellManagement':
      return <SpellManagementPanel {...content} />;
    case 'mechanic':
      return <MechanicPanel {...content} />;
    case 'textField':
      return <TextFieldPanel {...content} />;
    case 'conditions':
      return <ConditionsPanel {...content} />;
    case 'hpManagement':
      return <HpManagementPanel {...content} />;
    case 'manageCustomActions':
      return <ManageCustomActionsPanel {...content} />;
    case 'manageInventory':
      return <ManageInventoryPanel {...content} />;
    case 'featManagement':
      return <FeatManagementPanel {...content} />;
  }
}

const LOCKED_KEY = 'osv.sheet.sidebarLocked';

function readLocked(): boolean {
  try {
    return localStorage.getItem(LOCKED_KEY) === 'true';
  } catch {
    return false;
  }
}

function writeLocked(locked: boolean) {
  try {
    localStorage.setItem(LOCKED_KEY, String(locked));
  } catch {
    // The choice just isn't remembered.
  }
}

type SidebarProps = {
  /** The panel shown, kept while the sidebar is hidden so showing it again brings it back. */
  content: SidebarContent | null;
  hidden: boolean;
  onHide: () => void;
  onShow: () => void;
  /** Told the lock state on mount and on every change, so the sheet can make room for a locked sidebar. */
  onLockedChange?: (locked: boolean) => void;
};

/**
 * The one contextual surface, anchored right (ui-design-system.md): D&D Beyond's frame, its hide and lock
 * controls, and one panel at a time. Unlocked, Escape or a click outside hides it; locked, it stays open.
 */
export function Sidebar({ content, hidden, onHide, onShow, onLockedChange }: SidebarProps) {
  const t = useSheetText();
  const [locked, setLocked] = useState(readLocked);
  const root = useRef<HTMLElement>(null);

  useEffect(() => {
    onLockedChange?.(locked);
  }, [locked, onLockedChange]);

  useEffect(() => {
    if (hidden || locked) {
      return;
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onHide();
      }
    };
    const onPointerDown = (event: MouseEvent) => {
      if (root.current && !root.current.contains(event.target as Node)) {
        onHide();
      }
    };
    document.addEventListener('keydown', onKeyDown);
    document.addEventListener('mousedown', onPointerDown);
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      document.removeEventListener('mousedown', onPointerDown);
    };
  }, [hidden, locked, onHide]);

  const toggleLocked = () => {
    setLocked(!locked);
    writeLocked(!locked);
  };

  const dark = !hidden && content?.kind === 'characterMenu';
  return (
    <aside ref={root} className={`sidebar${hidden ? ' is-hidden' : ''}${dark ? ' sidebar--dark' : ''}`}>
      <div className="sidebar__controls">
        {locked ? (
          <button type="button" className="sidebar__control" title={t('sidebar.locked')} aria-label={t('sidebar.locked')} onClick={toggleLocked}>
            <LockIcon />
          </button>
        ) : (
          <>
            <button
              type="button"
              className="sidebar__control"
              title={hidden ? t('sidebar.show') : t('sidebar.hide')}
              aria-label={hidden ? t('sidebar.show') : t('sidebar.hide')}
              onClick={hidden ? onShow : onHide}
            >
              {hidden ? <SidebarLeftIcon /> : <SidebarRightIcon />}
            </button>
            {!hidden && (
              <button type="button" className="sidebar__control" title={t('sidebar.unlocked')} aria-label={t('sidebar.unlocked')} onClick={toggleLocked}>
                <UnlockIcon />
              </button>
            )}
          </>
        )}
      </div>
      {!hidden && (
        <div className="sidebar__pane">
          <SidebarBorder />
          <div className="sidebar__gap" />
          <div className="sidebar__content">
            {content ? renderContent(content) : <p className="sidebar__default">{t('sidebar.empty')}</p>}
          </div>
          <SidebarBorder bottom />
        </div>
      )}
    </aside>
  );
}

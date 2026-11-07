import { useSheetText } from '../locales/platformMessages';
import type { ChangeAppearanceRequest } from './api';
import { SidebarCollapsible, SidebarHeader } from './sidebarParts';

/**
 * D&D Beyond's Change Sheet Appearance pane, portrait and themes only: Current Decorations (the portrait and the
 * current theme's sample), then Browse Decorations with a Themes section of tiles. Picking a tile applies it at once.
 */
export function ChangeAppearancePanel({ themes, current, onSelect, renderSample, portrait }: ChangeAppearanceRequest) {
  const t = useSheetText();
  const currentTheme = themes.find((theme) => theme.id === current) ?? themes[0];
  return (
    <div className="change-appearance">
      <div className="change-appearance__current">
        <SidebarHeader title={t('appearance.current')} />
        <div className="change-appearance__current-grid">
          {portrait && (
            <div className="change-appearance__current-item">
              <span className="change-appearance__current-label">{t('appearance.portrait')}</span>
              <div className="change-appearance__current-inner">{portrait}</div>
            </div>
          )}
          <div className="change-appearance__current-item">
            <span className="change-appearance__current-label">{t('appearance.theme')}</span>
            <div className="change-appearance__current-inner change-appearance__current-inner--theme">
              <span className="change-appearance__sample">{renderSample(currentTheme)}</span>
              <span className="change-appearance__theme-name">{currentTheme.label}</span>
            </div>
          </div>
        </div>
      </div>
      <h2 className="change-appearance__subheading">{t('appearance.browse')}</h2>
      <SidebarCollapsible heading={t('appearance.themes')}>
        <ul className="change-appearance__grid">
          {themes.map((theme) => (
            <li key={theme.id}>
              <button
                type="button"
                className="change-appearance__tile"
                aria-pressed={theme.id === currentTheme.id}
                onClick={() => onSelect(theme.id)}
              >
                <span className="change-appearance__sample">{renderSample(theme)}</span>
                <span className="change-appearance__name">{theme.label}</span>
              </button>
            </li>
          ))}
        </ul>
      </SidebarCollapsible>
    </div>
  );
}

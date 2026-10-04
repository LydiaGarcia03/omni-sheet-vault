type Tab = { id: string; label: string };

type TabBarProps = {
  tabs: Tab[];
  activeTab: string;
  onSelect: (id: string) => void;
};

/**
 * The detail section's tab bar — a shared primitive (ui-design-system.md), not
 * system-specific. Styled via `.tab-bar`/`.tab-bar__tab` (frames.css). Font-size,
 * weight and padding are measured live against D&D Beyond's own tab buttons
 * (`.styles_tabButton__wvSLf`): 14px (0.875rem), 700 weight, uppercase, normal
 * letter-spacing, `padding: 3px 0` (no horizontal padding on the button itself —
 * D&D Beyond's own 16px gap does the spacing, re-measured 2026-09-03; the active
 * underline is 3px solid its accent blue-gray, not 2px solid the frame ink). Its
 * own tab row happens to fit its ~583px-wide box without scrolling; this app's
 * only caller (Dnd5eTabbedSection) is narrower (408px), so its six tabs don't all
 * fit even with shortened labels — that's fine, since D&D Beyond's own primary box
 * wraps its content area in `overflow-x/-y: auto` for exactly this "doesn't fit"
 * case (confirmed live: `.styles_content__QjnYw`, scrollHeight far exceeding
 * clientHeight), not wrapping or shrinking text to force a fit. This bar relies on
 * that same scroll container (`.tabbed-section__content`) rather than solving the
 * overflow itself.
 */
export function TabBar({ tabs, activeTab, onSelect }: TabBarProps) {
  return (
    <div role="tablist" className="tab-bar">
      {tabs.map((tab) => {
        const isActive = tab.id === activeTab;
        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            className="tab-bar__tab"
            aria-selected={isActive}
            onClick={() => onSelect(tab.id)}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}

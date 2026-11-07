import { useShellText } from '../locales/platformMessages';
import './tutorial.css';

/** Floating button in the page's bottom-right corner that reopens the tour; `themeClassName` is the tour's own theme class. */
export function TutorialLauncher({ onOpen, themeClassName }: { onOpen: () => void; themeClassName?: string }) {
  const t = useShellText();
  return (
    <button
      type="button"
      className={themeClassName ? `tutorial-launcher ${themeClassName}` : 'tutorial-launcher'}
      onClick={onOpen}
      data-tutorial="help"
      aria-label={t('tutorial.open')}
    >
      <span className="tutorial-launcher__mark" aria-hidden="true">
        ?
      </span>
      {t('tutorial.launcher')}
    </button>
  );
}

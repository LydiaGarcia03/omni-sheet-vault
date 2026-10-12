import './tutorial.css';

/** Floating button in the page's bottom-right corner that reopens the tour. */
export function TutorialLauncher({ onOpen }: { onOpen: () => void }) {
  return (
    <button type="button" className="tutorial-launcher" onClick={onOpen} data-tutorial="help" aria-label="Open the tutorial">
      <span className="tutorial-launcher__mark" aria-hidden="true">
        ?
      </span>
      Tutorial
    </button>
  );
}

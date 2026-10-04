import { useEffect, useRef, useState } from 'react';

type MoveButtonOption = { id: string; label: string };

type MoveButtonProps = {
  current: string;
  options: MoveButtonOption[];
  onMove: (id: string) => void;
};

/**
 * D&D Beyond's own `buttonWithMenu` (DOM-confirmed live,
 * `data-testid="theme-button-with-menu-move"`): an outline button that opens
 * a small menu of the other destinations, rather than a plain `<select>`.
 * System-agnostic — takes whatever `options`/`current` the caller has (a D&D
 * 5e storage location today, any other system's own bucket set tomorrow).
 */
export function MoveButton({ current, options, onMove }: MoveButtonProps) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) {
      return;
    }
    const handleOutsideClick = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, [open]);

  const targets = options.filter((option) => option.id !== current);

  return (
    <div className="move-button" ref={containerRef}>
      <button
        type="button"
        className="mutate sidebar-action-button"
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
      >
        Move
      </button>
      {open && (
        <ul className="move-button__menu" role="menu">
          {targets.map((option) => (
            <li key={option.id} role="none">
              <button
                type="button"
                className="mutate move-button__option"
                role="menuitem"
                onClick={() => {
                  onMove(option.id);
                  setOpen(false);
                }}
              >
                {option.label}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

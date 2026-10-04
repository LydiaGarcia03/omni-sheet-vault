import { useEffect, useRef, type ReactNode } from 'react';

type ConfirmDialogProps = {
  title: string;
  children: ReactNode;
  confirmLabel: string;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
};

/** A small modal that asks before a destructive action; Escape or a click outside cancels. */
export function ConfirmDialog({ title, children, confirmLabel, busy = false, onConfirm, onCancel }: ConfirmDialogProps) {
  const cancel = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    cancel.current?.focus();
    const onKey = (event: KeyboardEvent) => event.key === 'Escape' && onCancel();
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onCancel]);

  return (
    <div className="app-dialog-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onCancel()}>
      <div className="app-dialog" role="alertdialog" aria-modal="true" aria-labelledby="app-dialog-title">
        <h2 id="app-dialog-title" className="app-heading">
          {title}
        </h2>
        <p>{children}</p>
        <div className="app-dialog__actions">
          <button ref={cancel} type="button" className="app-btn" onClick={onCancel} disabled={busy}>
            Cancel
          </button>
          <button type="button" className="app-btn app-btn--danger" onClick={onConfirm} disabled={busy}>
            {busy ? `${confirmLabel}…` : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

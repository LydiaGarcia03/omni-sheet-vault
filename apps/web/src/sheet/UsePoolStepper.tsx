import type { CSSProperties, ReactNode } from 'react';

type UsePoolStepperProps = {
  maxUses: number;
  usedCount: number;
  rechargeLabel?: string | null;
  onUse?: () => void;
  onRestore?: () => void;
  useIcon?: ReactNode;
  restoreIcon?: ReactNode;
};

const buttonStyle: CSSProperties = {
  width: '20px',
  height: '20px',
  padding: 0,
  border: '1px solid var(--border-control, #D8D8D8)',
  borderRadius: '3px',
  background: 'var(--frame-paper, #FFFFFF)',
  boxSizing: 'border-box',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '12px',
  fontWeight: 700,
  lineHeight: 1,
};

// Sibling to BoxTrack for a large use pool; the small/large threshold is picked by the caller.
// Immediate mutation per click, no staged edit — matches BoxTrack's own convention.
// Each button stops propagation — see BoxTrack.tsx's own doc comment.
export function UsePoolStepper({ maxUses, usedCount, rechargeLabel, onUse, onRestore, useIcon, restoreIcon }: UsePoolStepperProps) {
  const remaining = maxUses - usedCount;

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <button
        type="button"
        className="mutate"
        aria-label="Spend a use"
        disabled={usedCount >= maxUses}
        onClick={(event) => {
          event.stopPropagation();
          onUse?.();
        }}
        style={buttonStyle}
      >
        {useIcon ?? '−'}
      </button>
      <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary, #242528)', minWidth: '52px', textAlign: 'center' }}>
        {remaining} of {maxUses}
      </span>
      <button
        type="button"
        className="mutate"
        aria-label="Restore a use"
        disabled={usedCount <= 0}
        onClick={(event) => {
          event.stopPropagation();
          onRestore?.();
        }}
        style={buttonStyle}
      >
        {restoreIcon ?? '+'}
      </button>
      {rechargeLabel && (
        <span style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-primary, #242528)' }}>
          <span aria-hidden="true" style={{ color: 'var(--text-muted, #6B7A85)' }}>
            /
          </span>
          {rechargeLabel}
        </span>
      )}
    </div>
  );
}

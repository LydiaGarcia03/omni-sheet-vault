import './loading.css';

type SpinnerProps = {
  /** Diameter in px. */
  size?: number;
  /** Announced to screen readers, and shown under the ring when `showLabel` is set. */
  label?: string;
  showLabel?: boolean;
  className?: string;
};

/** The app's loading ring: a gold arc that grows and shrinks around a dark track. */
export function Spinner({ size = 40, label = 'Loading…', showLabel = false, className }: SpinnerProps) {
  return (
    <div className={className ? `app-spinner ${className}` : 'app-spinner'} role="status">
      <svg width={size} height={size} viewBox="0 0 100 100" aria-hidden="true">
        <circle className="app-spinner__track" cx="50" cy="50" r="40" />
        <circle className="app-spinner__arc" cx="50" cy="50" r="40" />
      </svg>
      <span className={showLabel ? 'app-spinner__label' : 'app-visually-hidden'}>{label}</span>
    </div>
  );
}

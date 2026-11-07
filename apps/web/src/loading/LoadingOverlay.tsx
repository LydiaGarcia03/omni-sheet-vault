import { Spinner } from './Spinner';
import './loading.css';

/** Covers the whole window while a page-wide change loads: the page stays visible, blurred, behind the spinner. */
export function LoadingOverlay({ label }: { label?: string }) {
  return (
    <div className="app-loading-overlay" aria-busy="true">
      <Spinner size={72} label={label} />
    </div>
  );
}

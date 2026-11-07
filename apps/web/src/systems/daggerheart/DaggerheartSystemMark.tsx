import './systemMark.css';

/** Daggerheart's name in the app header: the gold dagger-in-flame, the name and the SRD edition. */
export function DaggerheartSystemMark() {
  return (
    <span className="dh-system-mark">
      <i className="dh-system-mark__logo" aria-hidden="true" />
      <span className="dh-system-mark__name">Daggerheart</span>
      <span className="app-system__secondary dh-system-mark__edition">SRD 2.0 · Hope &amp; Fear</span>
    </span>
  );
}

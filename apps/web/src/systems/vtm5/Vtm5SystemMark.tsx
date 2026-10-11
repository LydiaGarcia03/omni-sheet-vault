import './systemMark.css';

/** VtM 5e's name in the app header: a red diamond, "Vampire: The Masquerade" and the edition. */
export function Vtm5SystemMark() {
  return (
    <span className="vtm5-system-mark">
      <i className="vtm5-system-mark__diamond" aria-hidden="true" />
      <span className="vtm5-system-mark__name">Vampire: The Masquerade</span>
      <span className="app-system__secondary vtm5-system-mark__edition">5th edition</span>
    </span>
  );
}

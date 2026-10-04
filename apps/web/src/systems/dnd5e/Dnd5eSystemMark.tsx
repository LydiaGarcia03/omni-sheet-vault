import './systemMark.css';

/** D&D 5e's name in the app header: the ampersand logo and "Dungeons & Dragons" in the game's red, and the edition. */
export function Dnd5eSystemMark() {
  return (
    <span className="dnd5e-system-mark">
      <span className="dnd5e-system-mark__logo" aria-hidden="true" />
      <span className="dnd5e-system-mark__name">Dungeons &amp; Dragons</span>
      <span className="app-system__secondary dnd5e-system-mark__edition">5th edition · 2014</span>
    </span>
  );
}

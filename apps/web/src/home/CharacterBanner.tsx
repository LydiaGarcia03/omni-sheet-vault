import { useEffect, useRef, useState, type CSSProperties } from 'react';
import { Link } from 'react-router';
import type { Character } from '../characters/api';
import { CharacterPortrait } from '../characters/CharacterPortrait';
import { systemColor } from '../characters/supportedSystems';

type CharacterBannerProps = {
  character: Character;
  onDelete: (character: Character) => void;
};

/** One character in the vault: portrait, name and the facts its game system chose, with a bar in the system's colour. */
export function CharacterBanner({ character, onDelete }: CharacterBannerProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menu = useRef<HTMLDivElement>(null);
  const draft = character.status === 'DRAFT';
  const href = draft ? `/characters/${character.id}/build` : `/characters/${character.id}`;

  useEffect(() => {
    if (!menuOpen) {
      return;
    }
    const close = (event: MouseEvent) => {
      if (!menu.current?.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [menuOpen]);

  return (
    <article
      className={`character-banner${draft ? ' is-draft' : ''}`}
      style={{ '--system-color': systemColor(character.systemId) } as CSSProperties}
    >
      <Link className="character-banner__link" to={href} aria-label={draft ? `Continue creating ${character.name}` : `Open ${character.name}`} />
      <CharacterPortrait portrait={character.portrait} systemId={character.systemId} className="character-banner__portrait" />
      <div className="character-banner__body">
        <div className="character-banner__name">{character.name}</div>
        <div className="character-banner__facts">
          {draft && <span className="character-banner__draft">Draft · continue creating</span>}
          {character.summary.map((fact, index) => (
            <span key={fact.label} className={index === 0 ? 'is-lead' : undefined}>
              {fact.value}
            </span>
          ))}
        </div>
      </div>
      <div ref={menu}>
        <button
          type="button"
          className="character-banner__menu-button"
          aria-label={`More actions for ${character.name}`}
          aria-haspopup="menu"
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen(!menuOpen)}
        >
          ⋯
        </button>
        {menuOpen && (
          <div className="character-banner__menu" role="menu">
            <Link role="menuitem" to={href}>
              {draft ? 'Continue creating' : 'Open sheet'}
            </Link>
            <button
              type="button"
              role="menuitem"
              className="is-danger"
              onClick={() => {
                setMenuOpen(false);
                onDelete(character);
              }}
            >
              Delete
            </button>
          </div>
        )}
      </div>
    </article>
  );
}

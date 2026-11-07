import type { ReactNode } from 'react';
import { classArt, DOMAIN_COLORS } from '../daggerheartArt';
import { capitalize } from '../daggerheartCatalogue';

/** A domain's sprite icon (Dread draws a ring); white on navy, dark on paper. */
export function DomainIcon({ domain, dark = false }: { domain: string; dark?: boolean }) {
  return <i className={`dom-ico dom-${domain}${dark ? ' dark' : ''}`} aria-hidden="true" />;
}

export function DomainLabel({ domain, dark = false }: { domain: string; dark?: boolean }) {
  return (
    <span className="dom-label">
      <DomainIcon domain={domain} dark={dark} /> {capitalize(domain)}
    </span>
  );
}

export function domainColor(domain: string): string {
  return DOMAIN_COLORS[domain] ?? '#1e1a4f';
}

/** The class crest: its name plate on top, its banner hanging from behind the plate's left half. */
export function Crest({ classSlug, className = '' }: { classSlug: string; className?: string }) {
  const { plate, banner } = classArt(classSlug);
  return (
    <div className={`crest ${className}`}>
      {plate && <img className="crest__plate" src={plate} alt="" />}
      {banner && <div className="crest__banner" style={{ backgroundImage: `url(${banner})` }} />}
    </div>
  );
}

type TileProps = {
  on: boolean;
  title: ReactNode;
  children?: ReactNode;
  onPick: () => void;
  onHover?: (hovering: boolean) => void;
  disabled?: boolean;
};

/** One pickable option on paper: a title, a short line, a check when picked. */
export function Tile({ on, title, children, onPick, onHover, disabled }: TileProps) {
  return (
    <button
      type="button"
      className={`tile${on ? ' on' : ''}`}
      aria-pressed={on}
      disabled={disabled}
      onClick={onPick}
      onMouseEnter={() => onHover?.(true)}
      onMouseLeave={() => onHover?.(false)}
      onFocus={() => onHover?.(true)}
      onBlur={() => onHover?.(false)}
    >
      <b>{title}</b>
      {children && <small>{children}</small>}
    </button>
  );
}

export function Group({ title, note, children }: { title: ReactNode; note?: ReactNode; children: ReactNode }) {
  return (
    <div className="group">
      <div className="group__head">
        <h3>{title}</h3>
        {note && <span className="note">{note}</span>}
      </div>
      {children}
    </div>
  );
}

/** Catalogue prose, or a note when the server redacts it (adr-0005). */
export function Prose({ text }: { text: string | null | undefined }) {
  if (text === null || text === undefined) {
    return <span className="muted">Text hidden on this server.</span>;
  }
  return <>{text}</>;
}

export function FeatureList({ features }: { features: { name: string; text: string | null }[] }) {
  return (
    <dl className="features">
      {features.map((feature) => (
        <div key={feature.name}>
          <dt>{feature.name}</dt>
          <dd>
            <Prose text={feature.text} />
          </dd>
        </div>
      ))}
    </dl>
  );
}

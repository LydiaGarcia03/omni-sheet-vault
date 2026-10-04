import { useEffect, useRef, useState } from 'react';
import type { EntityDetailRequest } from './api';
import { Customize } from './sidebarParts';
import { PencilIcon } from './sidebarIcons';

/**
 * The Entity Detail mold's content (ui-design-system.md): a fixed four-section
 * layout — header (icon/name/subtitle), properties, description/tags, then
 * `actionBar` — each section separated by D&D Beyond's own `sidebarSeparator`
 * treatment (a top border with matching margin/padding, not a fixed gap; see
 * `.entity-detail__actions`/`__properties`/`__description`/`__tags` in
 * frames.css). Metadata renders as a vertical stacked properties list (D&D
 * Beyond's own `InfoItem`, `role="listitem"`) — one label/value pair per row.
 *
 * `actionsPosition` (default `'bottom'`) places `actionBar` after
 * description/tags, DOM-confirmed against D&D Beyond's own item sidebar
 * (Bedroll: Weight/Cost/Source, then its flavor text, then Quantity/Move/
 * Delete last) — the majority pattern across entity types. Spells are the
 * one confirmed exception (`buildSpellDetailRequest` passes `'top'`): their
 * Cast control renders right under the subtitle there, before Customize and
 * properties, with no separator line of its own. `'section'` puts it under the header with a separator (a feature
 * action's Limited Use).
 *
 * `customize` adds D&D Beyond's Customize section under the header, and a pencil beside the name that opens it and
 * focuses its name field.
 */
export function EntityDetailPanel({
  name,
  parent,
  onOpenParent,
  icon,
  subtitle,
  metadata,
  tags,
  description,
  body,
  actionBar,
  actionsPosition = 'bottom',
  customize,
}: EntityDetailRequest) {
  const [customizeOpen, setCustomizeOpen] = useState(false);
  const [focusName, setFocusName] = useState(false);
  const customizeRef = useRef<HTMLDivElement>(null);
  const actions = actionBar && <div className="entity-detail__actions">{actionBar}</div>;
  const marker = customize?.customized ? '*' : '';

  useEffect(() => {
    if (focusName && customizeOpen) {
      customizeRef.current?.querySelector<HTMLInputElement>('[data-customize-name]')?.focus();
      setFocusName(false);
    }
  }, [focusName, customizeOpen]);

  const toggleFromPencil = () => {
    setFocusName(!customizeOpen);
    setCustomizeOpen(!customizeOpen);
  };

  return (
    <div className="entity-detail">
      {parent && !onOpenParent && <div className="entity-detail__parent">{parent}</div>}
      {parent && onOpenParent && (
        <button type="button" className="entity-detail__parent entity-detail__parent--link" onClick={onOpenParent}>
          {parent}
        </button>
      )}
      <div className="entity-detail__heading">
        {icon}
        <h3 className="entity-detail__title">
          {name}
          {marker}
        </h3>
        {customize && (
          <button type="button" className="entity-detail__pencil" aria-label={`Rename ${name}`} onClick={toggleFromPencil}>
            <PencilIcon />
          </button>
        )}
      </div>
      {subtitle && <div className="entity-detail__subtitle">{subtitle}</div>}
      {actionsPosition === 'top' && actionBar && <div className="entity-detail__actions entity-detail__actions--top">{actionBar}</div>}
      {actionsPosition === 'section' && actionBar && <div className="entity-detail__actions entity-detail__actions--section">{actionBar}</div>}
      {customize && (
        <div ref={customizeRef} className="entity-detail__customize">
          <Customize label={`${customize.label ?? 'Customize'}${marker}`} open={customizeOpen} onToggle={setCustomizeOpen}>
            {customize.editor}
          </Customize>
        </div>
      )}
      {metadata.length > 0 && (
        <div className="entity-detail__properties" role="list">
          {metadata.map((entry) => (
            <div key={entry.label} className="entity-detail__property" role="listitem">
              <p className="entity-detail__property-label">{entry.label}</p>
              <p className="entity-detail__property-value">{entry.value}</p>
            </div>
          ))}
        </div>
      )}
      {body}
      {!body && description && <p className="entity-detail__description">{description}</p>}
      {tags && tags.length > 0 && (
        <div className="entity-detail__tags">
          {tags.map((tag) => (
            <span key={tag} className="entity-detail__tag">
              {tag}
            </span>
          ))}
        </div>
      )}
      {actionsPosition === 'bottom' && actions}
    </div>
  );
}

import { useState } from 'react';
import type { CatalogueEntry } from '../catalogue/api';
import type { FeatManagementRequest, FeatureTrait } from './api';
import { catalogueEntryLabel } from './CatalogueSourceLabel';
import { ChevronDown, SidebarHeader } from './sidebarParts';

type SourceGroupProps = {
  source: string;
  entries: CatalogueEntry[];
  knownBySlug: Map<string, FeatureTrait>;
  onLearn: (catalogueEntryId: string) => void;
  onRemove: (featureKey: string) => void;
};

/** One source book's feats, open by default, each with Add or (when taken) Remove. */
function SourceGroup({ source, entries, knownBySlug, onLearn, onRemove }: SourceGroupProps) {
  const [open, setOpen] = useState(true);
  return (
    <section className={open ? 'feat-manage__group is-open' : 'feat-manage__group'}>
      <button type="button" className="feat-manage__group-header" aria-expanded={open} onClick={() => setOpen(!open)}>
        <h2 className="feat-manage__group-heading">{source}</h2>
        <ChevronDown />
      </button>
      {open && (
        <ul className="feat-manage__feats">
          {entries.map((entry) => {
            const known = knownBySlug.get(entry.slug);
            const label = catalogueEntryLabel(entry.name, entry.sourceBook);
            return (
              <li key={entry.id} className="feat-manage__feat">
                <span className="feat-manage__feat-text">
                  <span className="feat-manage__feat-name">{entry.name}</span>
                  <span className="feat-manage__feat-source">{entry.sourceBook}</span>
                </span>
                {known ? (
                  <button type="button" className="mutate spell-manage__button spell-manage__button--filled" aria-label={`Remove ${known.name}`} onClick={() => onRemove(known.key)}>
                    Remove
                  </button>
                ) : (
                  <button type="button" className="mutate spell-manage__button feat-manage__add" aria-label={`Learn ${label}`} onClick={() => onLearn(entry.id)}>
                    Add
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}

/**
 * D&D Beyond's Manage Feats pane: every catalogue feat grouped by source book, Add or Remove per row. A feat taken
 * without a catalogue entry is listed at the top so it can still be removed.
 */
export function FeatManagementPanel({ knownFeats, catalogue, onLearn, onRemove }: FeatManagementRequest) {
  const knownBySlug = new Map(knownFeats.map((feat) => [feat.key, feat]));
  const catalogued = new Set(catalogue.map((entry) => entry.slug));
  const orphans = knownFeats.filter((feat) => !catalogued.has(feat.key));
  const groups = [...new Set(catalogue.map((entry) => entry.sourceBook ?? 'Other'))].map((source) => ({
    source,
    entries: catalogue.filter((entry) => (entry.sourceBook ?? 'Other') === source).sort((a, b) => a.name.localeCompare(b.name)),
  }));

  return (
    <div className="feat-manage">
      <SidebarHeader title="Manage Feats" />
      {orphans.length > 0 && (
        <ul className="feat-manage__feats feat-manage__feats--orphans">
          {orphans.map((feat) => (
            <li key={feat.key} className="feat-manage__feat">
              <span className="feat-manage__feat-text">
                <span className="feat-manage__feat-name">{feat.name}</span>
              </span>
              <button type="button" className="mutate spell-manage__button spell-manage__button--filled" aria-label={`Remove ${feat.name}`} onClick={() => onRemove(feat.key)}>
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}
      {groups.map((group) => (
        <SourceGroup key={group.source} source={group.source} entries={group.entries} knownBySlug={knownBySlug} onLearn={onLearn} onRemove={onRemove} />
      ))}
    </div>
  );
}

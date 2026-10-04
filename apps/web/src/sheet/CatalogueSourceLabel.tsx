/** Muted source-book label after a catalogue entry's name, so same-named entries from different books stay distinguishable. */
export function CatalogueSourceLabel({ sourceBook }: { sourceBook: string | null }) {
  if (!sourceBook) {
    return null;
  }
  return <span style={{ color: 'var(--text-muted, #6B7A85)' }}> · {sourceBook}</span>;
}

/** Accessible name for a catalogue row's action, including the source so two same-named rows don't share one label. */
export function catalogueEntryLabel(name: string, sourceBook: string | null): string {
  return sourceBook ? `${name} (${sourceBook})` : name;
}

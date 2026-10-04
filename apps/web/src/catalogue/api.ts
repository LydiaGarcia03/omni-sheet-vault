import { apiFetch } from '../api/client';

/**
 * Reference content — phase 7's catalogue (`GET /api/catalogue`), read-only from
 * the frontend. `description` is redactable per adr-0005: when redaction is
 * enabled server-side, `value` arrives null and `redacted` is true — the client
 * renders a placeholder rather than inventing text. `data`'s mechanical shape
 * varies by `kind`; the Spells picker (phase 9's "Manage spells") only reads its
 * own subset (level, school) — see `SpellCatalogueEntryData`.
 */
export type CatalogueEntry = {
  id: string;
  systemId: string;
  kind: string;
  slug: string;
  name: string;
  sourceBook: string | null;
  sourcePage: number | null;
  tags: string[];
  description: { value: string | null; redacted: boolean };
  data: unknown;
};

export type SpellCatalogueEntryData = {
  level: number;
  school: string;
  concentration: boolean;
  ritual: boolean;
  attackRoll: boolean;
  damageDiceCount: number | null;
  damageDiceSides: number | null;
  damageType: string | null;
};

/** A `BACKGROUND`-kind entry's `data` — phase 10's personality suggestion tables. */
export type BackgroundCatalogueEntryData = {
  personalityTraits: string[];
  ideals: string[];
  bonds: string[];
  flaws: string[];
};

/**
 * `sourceCode` is the book's short code from the content source (e.g. "PHB"), null when unknown; `playtest` marks
 * Unearthed Arcana; `partner` names the partner brand the book belongs to (e.g. "Critical Role"), null for none.
 */
export type CatalogueSource = { sourceBook: string; sourceCode: string | null; entryCount: number; playtest: boolean; partner: string | null };

/** Every imported source book with its entry count, largest first. */
export async function getCatalogueSources(systemId: string): Promise<CatalogueSource[]> {
  const response = await apiFetch(`/api/catalogue/sources?systemId=${encodeURIComponent(systemId)}`);
  if (!response.ok) {
    throw new Error(`Failed to load the catalogue sources: ${response.status}`);
  }
  return response.json() as Promise<CatalogueSource[]>;
}

/** A partner brand with its imported books. */
export type CataloguePartner = { name: string; sourceBooks: string[] };

/** The partner brands with imported books, in D&D Beyond's order. */
export async function getCataloguePartners(systemId: string): Promise<CataloguePartner[]> {
  const response = await apiFetch(`/api/catalogue/partners?systemId=${encodeURIComponent(systemId)}`);
  if (!response.ok) {
    throw new Error(`Failed to load the catalogue partners: ${response.status}`);
  }
  return response.json() as Promise<CataloguePartner[]>;
}

export async function getCatalogue(systemId: string, kind: string): Promise<CatalogueEntry[]> {
  const response = await apiFetch(`/api/catalogue?systemId=${encodeURIComponent(systemId)}&kind=${encodeURIComponent(kind)}`);
  if (!response.ok) {
    throw new Error(`Failed to load the catalogue: ${response.status}`);
  }
  return response.json() as Promise<CatalogueEntry[]>;
}

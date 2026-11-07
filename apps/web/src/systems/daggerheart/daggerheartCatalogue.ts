import { useEffect, useState } from 'react';
import { getCatalogue, type CatalogueEntry } from '../../catalogue/api';

export const DAGGERHEART = 'daggerheart';

export type Feature = { name: string; text: string; modifiers?: { target: string; value: number }[] };

export type ClassData = {
  domains: string[];
  startingEvasion: number;
  startingHitPoints: number;
  classItems: string;
  hopeFeature: Feature;
  features: Feature[];
  subclasses: string[];
  backgroundQuestions: string[];
  connectionQuestions: string[];
};
export type SubclassData = { classSlug: string; spellcastTrait: string | null; foundation: Feature[]; specialization: Feature[]; mastery: Feature[] };
export type AncestryData = { features: Feature[]; family: { name: string; description: string } | null };
export type CommunityData = { adjectives: string[]; feature: Feature };
export type TransformationData = { features: Feature[]; questions: string[] };
export type DomainCardData = { domain: string; level: number; type: string; recallCost: number };
export type WeaponData = {
  category: 'weapon';
  slot: string;
  tier: number;
  trait: string;
  range: string;
  damage: { die: string; bonus: number; type: string };
  burden: number;
  feature: Feature | null;
};
export type ArmorData = { category: 'armor'; tier: number; baseThresholds: { major: number; severe: number }; baseScore: number; feature: Feature | null };

export type Entry<T> = Omit<CatalogueEntry, 'data'> & { data: T };

/** Every Daggerheart catalogue kind the builder shows, by kind then slug. */
export type DaggerheartCatalogue = {
  classes: Map<string, Entry<ClassData>>;
  subclasses: Map<string, Entry<SubclassData>>;
  ancestries: Map<string, Entry<AncestryData>>;
  communities: Map<string, Entry<CommunityData>>;
  transformations: Map<string, Entry<TransformationData>>;
  domainCards: Map<string, Entry<DomainCardData>>;
  items: Map<string, Entry<WeaponData | ArmorData | { category: string }>>;
};

let loading: Promise<DaggerheartCatalogue> | null = null;

function bySlug<T>(entries: CatalogueEntry[]): Map<string, Entry<T>> {
  return new Map(entries.map((entry) => [entry.slug, entry as Entry<T>]));
}

async function load(): Promise<DaggerheartCatalogue> {
  const [classes, subclasses, ancestries, communities, transformations, domainCards, items] = await Promise.all(
    ['CLASS', 'SUBCLASS', 'ANCESTRY', 'COMMUNITY', 'TRANSFORMATION', 'DOMAIN_CARD', 'ITEM'].map((kind) => getCatalogue(DAGGERHEART, kind)),
  );
  return {
    classes: bySlug(classes),
    subclasses: bySlug(subclasses),
    ancestries: bySlug(ancestries),
    communities: bySlug(communities),
    transformations: bySlug(transformations),
    domainCards: bySlug(domainCards),
    items: bySlug(items),
  };
}

/** The catalogue, fetched once per page load; null while it loads. */
export function useDaggerheartCatalogue(): DaggerheartCatalogue | null {
  const [catalogue, setCatalogue] = useState<DaggerheartCatalogue | null>(null);
  useEffect(() => {
    let active = true;
    loading ??= load();
    loading.then((loaded) => active && setCatalogue(loaded)).catch(() => {
      loading = null;
    });
    return () => {
      active = false;
    };
  }, []);
  return catalogue;
}

export function capitalize(text: string): string {
  return text ? text[0].toUpperCase() + text.slice(1) : text;
}

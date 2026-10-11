import { useEffect, useState } from 'react';
import { apiFetch } from '../../api/client';

export type Power = { level: number; name: string; cost: string; pool: string; summary: string };
export type Discipline = { type: string; resonance: string; short: string; powers: Power[] };
export type Clan = {
  name: string;
  nick: string;
  source: string;
  tint: string;
  disciplines: string[];
  long: string;
  play: string;
  bane: { name: string; text: string };
  compulsion: { name: string; text: string };
};
export type Predator = {
  name: string;
  short: string;
  specialties: { skill: string; specialty: string }[];
  disciplines: string[];
  humanity?: number;
  bp?: number;
  extras: string;
  notFor?: string[];
};
export type Advantage = { name: string; kind: string; max: number; fixed?: number; short: string };
export type Trait = { group: string; short: string; uses?: string; specialties?: string[] };

/** VtM 5e's curated content, served by the API (`/api/vtm5/content`). */
export type Vtm5Content = {
  attributeScale: string[];
  skillScale: string[];
  attributes: Record<string, Trait>;
  skills: Record<string, Trait>;
  freeSpecialtySkills: string[];
  distributions: Record<string, { slots: Record<string, number>; short: string; fits: string }>;
  disciplines: Record<string, Discipline>;
  clans: Clan[];
  predators: Predator[];
  sorceryClans: string[];
  ages: Record<string, { short: string; bp: number; xp: number; generations: string[]; note: string }>;
  advantages: Advantage[];
  rituals: { level: number; name: string; summary: string }[];
  thinBloodMerits: { name: string; short: string }[];
  thinBloodFlaws: { name: string; short: string }[];
  alchemyMethods: { name: string; attribute: string; short: string }[];
  loresheets: { name: string; book: string; restriction: string; levels: string[] }[];
  coterieTypes: string[];
  domain: Record<string, string>;
  blood: Record<string, string>;
  resonances: Record<string, string[]>;
  bpTable: string[][];
  identity: Record<string, string>;
  convictionHelp: string;
};

export const GROUPS = ['Physical', 'Social', 'Mental'] as const;

let cached: Promise<Vtm5Content> | null = null;

function loadContent(): Promise<Vtm5Content> {
  cached ??= apiFetch('/api/vtm5/content').then((response) => {
    if (!response.ok) {
      cached = null;
      throw new Error('Could not load the Vampire content.');
    }
    return response.json() as Promise<Vtm5Content>;
  });
  return cached;
}

/** The VtM content, or null while it loads. */
export function useVtm5Content(): Vtm5Content | null {
  const [content, setContent] = useState<Vtm5Content | null>(null);
  useEffect(() => {
    let active = true;
    loadContent().then((loaded) => active && setContent(loaded), () => undefined);
    return () => {
      active = false;
    };
  }, []);
  return content;
}

export function traitsOfGroup(traits: Record<string, Trait>, group: string): string[] {
  return Object.keys(traits).filter((name) => traits[name].group === group);
}

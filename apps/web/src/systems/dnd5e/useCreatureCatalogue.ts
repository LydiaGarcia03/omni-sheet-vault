import { useEffect, useState } from 'react';
import { getCatalogue, type CatalogueEntry } from '../../catalogue/api';

/** The catalogue creature data Manage Extras lists and previews. */
export type CreatureData = {
  size: string;
  creatureType: string;
  alignment: string;
  armorClass: number;
  armorClassText: string;
  hitPoints: number;
  hitDice: string;
  speed: number;
  additionalSpeeds: string | null;
  challengeRating: string;
  familiar: boolean;
  environments: string[];
};

export type Creature = CatalogueEntry & { data: CreatureData };

let loading: Promise<Creature[]> | null = null;

/** Every catalogue creature, read once per page load and shared by every Manage Extras pane; null while loading. */
export function useCreatureCatalogue(): Creature[] | null {
  const [creatures, setCreatures] = useState<Creature[] | null>(null);
  useEffect(() => {
    let active = true;
    loading ??= getCatalogue('dnd-5e', 'CREATURE').then(
      (entries) => [...(entries as Creature[])].sort((a, b) => a.name.localeCompare(b.name)),
      (error) => {
        loading = null;
        throw error;
      },
    );
    loading.then(
      (list) => active && setCreatures(list),
      () => active && setCreatures([]),
    );
    return () => {
      active = false;
    };
  }, []);
  return creatures;
}

/** "1/4" → 0.25, so challenge ratings can be compared. */
export function challengeValue(rating: string): number {
  const [top, bottom] = rating.split('/');
  const value = bottom ? Number(top) / Number(bottom) : Number(top);
  return Number.isFinite(value) ? value : -1;
}

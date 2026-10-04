import { apiFetch } from '../api/client';

export type RollKind =
  | 'ABILITY_CHECK'
  | 'SAVING_THROW'
  | 'SKILL_CHECK'
  | 'INITIATIVE'
  | 'ATTACK_HIT'
  | 'ATTACK_DAMAGE'
  | 'ATTACK_DAMAGE_VERSATILE'
  | 'SPELL_ATTACK'
  | 'SPELL_DAMAGE'
  | 'SPELL_HEAL';

/**
 * Bound by the composing panel to a specific kind/key — leaf roll targets just call
 * it. `castAtLevel` is the spell slot level a `SPELL_DAMAGE`/`SPELL_HEAL` roll was
 * cast at, for upcasting to scale the dice server-side — see `SpellCast.tsx`. Every
 * other roll target omits it.
 */
export type RollHandler = (kind: RollKind, key: string | null, castAtLevel?: number) => void;

/**
 * Bound the same way as {@link RollHandler}, for the roll targets that also open the
 * advantage/disadvantage popover on right-click — see RollModeMenu.tsx.
 * `formattedModifier` is the modifier the row already displays (e.g. "+4"), reused as
 * the popover's "ROLL 1d20+4" label; it is never sent to the server.
 */
export type RollContextMenuHandler = (x: number, y: number, kind: RollKind, key: string | null, formattedModifier: string) => void;

export type DieType = 'D4' | 'D6' | 'D8' | 'D10' | 'D12' | 'D20' | 'D100';

export type DiceGroup = { type: DieType; count: number };

/** `dropped` lists the indexes in `results` left out of `total` (the lowest die of 4d6kh3); missing on older payloads. */
export type RollResult = {
  id: string;
  expression: string;
  context: string;
  results: number[];
  dropped?: number[];
  total: number;
  rolledAt: string;
};

/** The dice a character-builder roll asks for, as the build plan describes them; the server resolves the result. */
export type CreationRoll = { sides: number; count: number; keepHighest: number | null; context: string };

/** A roll made while building a character (an ability score, a level's hit points); allowed on a draft. */
export async function postCreationRoll(characterId: string, roll: CreationRoll): Promise<RollResult> {
  const response = await apiFetch(`/api/characters/${characterId}/rolls/creation`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ die: `D${roll.sides}`, count: roll.count, keepHighest: roll.keepHighest, context: roll.context }),
  });
  if (!response.ok) {
    throw new Error(`Failed to roll: ${response.status}`);
  }
  return response.json() as Promise<RollResult>;
}

export async function postRoll(
  characterId: string,
  kind: RollKind,
  key: string | null,
  mode?: { advantage?: boolean; disadvantage?: boolean },
  castAtLevel?: number,
): Promise<RollResult> {
  const response = await apiFetch(`/api/characters/${characterId}/rolls`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      kind,
      key,
      advantage: mode?.advantage ?? false,
      disadvantage: mode?.disadvantage ?? false,
      castAtLevel: castAtLevel ?? null,
    }),
  });
  if (!response.ok) {
    throw new Error(`Failed to roll: ${response.status}`);
  }
  return response.json() as Promise<RollResult>;
}

export async function postManualRoll(characterId: string, dice: DiceGroup[]): Promise<RollResult> {
  const response = await apiFetch(`/api/characters/${characterId}/rolls/manual`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ dice }),
  });
  if (!response.ok) {
    throw new Error(`Failed to roll: ${response.status}`);
  }
  return response.json() as Promise<RollResult>;
}

export async function getRollHistory(characterId: string): Promise<RollResult[]> {
  const response = await apiFetch(`/api/characters/${characterId}/rolls`);
  if (!response.ok) {
    throw new Error(`Failed to load roll history: ${response.status}`);
  }
  return response.json() as Promise<RollResult[]>;
}

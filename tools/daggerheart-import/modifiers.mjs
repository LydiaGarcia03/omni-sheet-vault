// Reads the always-on stat bonuses a feature's text states in the SRD's fixed phrasing ("−1 to Evasion").

const TRAITS = ['Agility', 'Strength', 'Finesse', 'Instinct', 'Presence', 'Knowledge'];

const TARGETS = {
  'evasion': ['evasion'],
  'armor score': ['armorScore'],
  'damage thresholds': ['majorThreshold', 'severeThreshold'],
  'severe damage threshold': ['severeThreshold'],
  'major damage threshold': ['majorThreshold'],
  'armor score and damage thresholds': ['armorScore', 'majorThreshold', 'severeThreshold'],
  'all character traits and evasion': [...TRAITS.map(trait => trait.toLowerCase()), 'evasion'],
  ...Object.fromEntries(TRAITS.map(trait => [trait.toLowerCase(), [trait.toLowerCase()]])),
};

const BONUS = /^(?:Gain a permanent )?([+−-]\d+)(?: bonus)?(?: to)?(?: your)? (.+?)(?: at character creation)?$/;
const SLOT = /^Gain an additional (Hit Point|Stress) slot(?: at character creation)?$/;

// Each clause must be exactly one bonus; conditional ones ("against attacks made by…") don't match.
export function modifiersOf(text) {
  const modifiers = [];
  for (const clause of text.split(/;\s*|\.\s+|\.$/).map(part => part.trim()).filter(Boolean)) {
    const slot = clause.match(SLOT);
    if (slot) {
      modifiers.push({ target: slot[1] === 'Stress' ? 'stressSlots' : 'hitPointSlots', value: 1 });
      continue;
    }
    const bonus = clause.match(BONUS);
    const targets = bonus && TARGETS[bonus[2].toLowerCase()];
    if (targets) targets.forEach(target => modifiers.push({ target, value: Number(bonus[1].replace('−', '-')) }));
  }
  return modifiers;
}

// The feature with its modifiers attached, when it has any.
export function withModifiers(feature) {
  if (!feature) return feature;
  const modifiers = modifiersOf(feature.text);
  return modifiers.length > 0 ? { ...feature, modifiers } : feature;
}

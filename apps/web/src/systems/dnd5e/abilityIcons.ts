import abilityCharismaSvg from './frames/dnd_icon_ability_charisma.svg?raw';
import abilityConstitutionSvg from './frames/dnd_icon_ability_constitution.svg?raw';
import abilityDexteritySvg from './frames/dnd_icon_ability_dexterity.svg?raw';
import abilityIntelligenceSvg from './frames/dnd_icon_ability_intelligence.svg?raw';
import abilityStrengthSvg from './frames/dnd_icon_ability_strength.svg?raw';
import abilityWisdomSvg from './frames/dnd_icon_ability_wisdom.svg?raw';

/** Each ability's icon (D&D Beyond's), keyed by ability. */
export const ABILITY_ICONS: Record<string, string> = {
  strength: abilityStrengthSvg,
  dexterity: abilityDexteritySvg,
  constitution: abilityConstitutionSvg,
  intelligence: abilityIntelligenceSvg,
  wisdom: abilityWisdomSvg,
  charisma: abilityCharismaSvg,
};

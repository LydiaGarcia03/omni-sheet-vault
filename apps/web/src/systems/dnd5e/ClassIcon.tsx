import artificerIcon from './classes/dnd5e_class_artificer.jpeg';
import barbarianIcon from './classes/dnd5e_class_barbarian.jpeg';
import bardIcon from './classes/dnd5e_class_bard.jpeg';
import bloodHunterIcon from './classes/dnd5e_class_blood_hunter.png';
import clericIcon from './classes/dnd5e_class_cleric.jpeg';
import druidIcon from './classes/dnd5e_class_druid.jpeg';
import fighterIcon from './classes/dnd5e_class_fighter.jpeg';
import illriggerIcon from './classes/dnd5e_class_illrigger.jpeg';
import monkIcon from './classes/dnd5e_class_monk.jpeg';
import paladinIcon from './classes/dnd5e_class_paladin.jpeg';
import rangerIcon from './classes/dnd5e_class_ranger.jpeg';
import rogueIcon from './classes/dnd5e_class_rogue.jpeg';
import sorcererIcon from './classes/dnd5e_class_sorcerer.jpeg';
import warlockIcon from './classes/dnd5e_class_warlock.jpeg';
import wizardIcon from './classes/dnd5e_class_wizard.jpeg';

const CLASS_ICONS: Record<string, string> = {
  artificer: artificerIcon,
  barbarian: barbarianIcon,
  bard: bardIcon,
  'blood hunter': bloodHunterIcon,
  cleric: clericIcon,
  druid: druidIcon,
  fighter: fighterIcon,
  illrigger: illriggerIcon,
  monk: monkIcon,
  paladin: paladinIcon,
  ranger: rangerIcon,
  rogue: rogueIcon,
  sorcerer: sorcererIcon,
  warlock: warlockIcon,
  wizard: wizardIcon,
};

/** The class's emblem for the character menu's class rows; nothing for a class without one. */
export function ClassIcon({ className }: { className: string }) {
  const src = CLASS_ICONS[className.trim().toLowerCase()];
  return src ? <img className="character-menu__class-icon" src={src} alt="" aria-hidden="true" /> : null;
}

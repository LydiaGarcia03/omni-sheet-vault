/**
 * The explanatory rules text of the D&D 5e sidebar panes, written by hand in Markdown (see RulesText). An empty string
 * shows nothing.
 */

export const ABILITY_RULES_TEXT: Record<string, string> = {
  strength: `
Strength measures bodily power, athletic training, and the extent to which you can exert raw physical force.

#### Strength Checks
A Strength check can model any attempt to lift, push, pull, or break something, to force your body through a space, or to otherwise apply brute force to a situation. The Athletics skill reflects aptitude in certain kinds of Strength checks.

#### Athletics
Your Strength (Athletics) check covers difficult situations you encounter while climbing, jumping, or swimming. Examples include the following activities:

* You attempt to climb a sheer or slippery cliff, avoid hazards while scaling a wall, or cling to a surface while something is trying to knock you off.
* You try to jump an unusually long distance or pull off a stunt midjump.
* You struggle to swim or stay afloat in treacherous currents, storm-tossed waves, or areas of thick seaweed. Or another creature tries to push or pull you underwater or otherwise interfere with your swimming.

#### Other Strength Checks
The DM might also call for a Strength check when you try to accomplish tasks like the following:

* Force open a stuck, locked, or barred door
* Break free of bonds
* Push through a tunnel that is too small
* Hang on to a wagon while being dragged behind it
* Tip over a statue
* Keep a boulder from rolling

#### Attack Rolls and Damage
You add your Strength modifier to your attack roll and your damage roll when attacking with a melee weapon such as a mace, a battleaxe, or a javelin. You use melee weapons to make melee attacks in hand-to-hand combat, and some of them can be thrown to make a ranged attack.

#### Lifting and Carrying
Your Strength score determines the amount of weight you can bear. The following terms define what you can lift or carry.

**Carrying Capacity.** Your carrying capacity is your Strength score multiplied by 15. This is the weight (in pounds) that you can carry, which is high enough that most characters don't usually have to worry about it.

**Push, Drag, or Lift.** You can push, drag, or lift a weight in pounds up to twice your carrying capacity (or 30 times your Strength score). While pushing or dragging weight in excess of your carrying capacity, your speed drops to 5 feet.

**Size and Strength.** Larger creatures can bear more weight, whereas Tiny creatures can carry less. For each size category above Medium, double the creature's carrying capacity and the amount it can push, drag, or lift. For a Tiny creature, halve these weights.

#### Variant: Encumbrance
The rules for lifting and carrying are intentionally simple. Here is a variant if you are looking for more detailed rules for determining how a character is hindered by the weight of equipment. When you use this variant, ignore the Strength column of the Armor table in chapter 5.

If you carry weight in excess of 5 times your Strength score, you are **encumbered**, which means your speed drops by 10 feet.

If you carry weight in excess of 10 times your Strength score, up to your maximum carrying capacity, you are instead **heavily encumbered**, which means your speed drops by 20 feet and you have disadvantage on ability checks, attack rolls, and saving throws that use Strength, Dexterity, or Constitution.`,
  dexterity: `
Dexterity measures agility, reflexes, and balance.

#### Dexterity Checks
A Dexterity check can model any attempt to move nimbly, quickly, or quietly, or to keep from falling on tricky footing. The Acrobatics, Sleight of Hand, and Stealth skills reflect aptitude in certain kinds of Dexterity checks.

#### Acrobatics
Your Dexterity (Acrobatics) check covers your attempt to stay on your feet in a tricky situation, such as when you're trying to run across a sheet of ice, balance on a tightrope, or stay upright on a rocking ship's deck. The DM might also call for a Dexterity (Acrobatics) check to see if you can perform acrobatic stunts, including dives, rolls, somersaults, and flips.

#### Sleight of Hand
Whenever you attempt an act of legerdemain or manual trickery, such as planting something on someone else or concealing an object on your person, make a Dexterity (Sleight of Hand) check. The DM might also call for a Dexterity (Sleight of Hand) check to determine whether you can lift a coin purse off another person or slip something out of another person's pocket.

#### Stealth
Make a Dexterity (Stealth) check when you attempt to conceal yourself from enemies, slink past guards, slip away without being noticed, or sneak up on someone without being seen or heard.

#### Other Dexterity Checks
The DM might call for a Dexterity check when you try to accomplish tasks like the following:

* Control a heavily laden cart on a steep descent
* Steer a chariot around a tight turn
* Pick a lock
* Disable a trap
* Securely tie up a prisoner
* Wriggle free of bonds
* Play a stringed instrument
* Craft a small or detailed object

#### Attack Rolls and Damage
You add your Dexterity modifier to your attack roll and your damage roll when attacking with a ranged weapon, such as a sling or a longbow. You can also add your Dexterity modifier to your attack roll and your damage roll when attacking with a melee weapon that has the finesse property, such as a dagger or a rapier.

#### Armor Class
Depending on the armor you wear, you might add some or all of your Dexterity modifier to your Armor Class, as described in chapter 5, "Equipment."

#### Initiative
At the beginning of every combat, you roll initiative by making a Dexterity check. Initiative determines the order of creatures' turns in combat, as described in chapter 9, "Combat."

blockquote
#### HIDING

The DM decides when circumstances are appropriate for hiding. When you try to hide, make a Dexterity (Stealth) check. Until you are discovered or you stop hiding, that check's total is contested by the Wisdom (Perception) check of any creature that actively searches for signs of your presence.

You can't hide from a creature that can see you clearly, and you give away your position if you make noise, such as shouting a warning or knocking over a vase. An invisible creature can always try to hide. Signs of its passage might still be noticed, and it does have to stay quiet.

In combat, most creatures stay alert for signs of danger all around, so if you come out of hiding and approach a creature, it usually sees you. However, under certain circumstances, the DM might allow you to stay hidden as you approach a creature that is distracted, allowing you to gain advantage on an attack roll before you are seen.

**Passive Perception.** When you hide, there's a chance someone will notice you even if they aren't searching. To determine whether such a creature notices you, the DM compares your Dexterity (Stealth) check with that creature's passive Wisdom (Perception) score, which equals 10 + the creature's Wisdom modifier, as well as any other bonuses or penalties. If the creature has advantage, add 5. For disadvantage, subtract 5.

For example, if a 1st-level character (with a proficiency bonus of +2) has a Wisdom of 15 (a +2 modifier) and proficiency in Perception, he or she has a passive Wisdom (Perception) of 14.

**What Can You See?** One of the main factors in determining whether you can find a hidden creature or object is how well you can see in an area, which might be **lightly** or **heavily obscured** as explained in chapter 8, "Adventuring."
fim-blockquote`,
  constitution: `
Constitution measures health, stamina, and vital force.

#### Constitution Checks
Constitution checks are uncommon, and no skills apply to Constitution checks, because the endurance this ability represents is largely passive rather than involving a specific effort on the part of a character or monster. A Constitution check can model your attempt to push beyond normal limits, however.

The DM might call for a Constitution check when you try to accomplish tasks like the following:

* Hold your breath
* March or labor for hours without rest
* Go without sleep
* Survive without food or water
* Quaff an entire stein of ale in one go

#### Hit Points
Your Constitution modifier contributes to your hit points. Typically, you add your Constitution modifier to each Hit Die you roll for your hit points.

If your Constitution modifier changes, your hit point maximum changes as well, as though you had the new modifier from 1st level. For example, if you raise your Constitution score when you reach 4th level and your Constitution modifier increases from +1 to +2, you adjust your hit point maximum as though the modifier had always been +2. So you add 3 hit points for your first three levels, and then roll your hit points for 4th level using your new modifier. Or if you're 7th level and some effect lowers your Constitution score so as to reduce your Constitution modifier by 1, your hit point maximum is reduced by 7.`,
  intelligence: `
Intelligence measures mental acuity, accuracy of recall, and the ability to reason.

#### Intelligence Checks
An Intelligence check comes into play when you need to draw on logic, education, memory, or deductive reasoning. The Arcana, History, Investigation, Nature, and Religion skills reflect aptitude in certain kinds of Intelligence checks.

#### Arcana
Your Intelligence (Arcana) check measures your ability to recall lore about spells, magic items, eldritch symbols, magical traditions, the planes of existence, and the inhabitants of those planes.

#### History
Your Intelligence (History) check measures your ability to recall lore about historical events, legendary people, ancient kingdoms, past disputes, recent wars, and lost civilizations.

#### Investigation
When you look around for clues and make deductions based on those clues, you make an Intelligence (Investigation) check. You might deduce the location of a hidden object, discern from the appearance of a wound what kind of weapon dealt it, or determine the weakest point in a tunnel that could cause it to collapse. Poring through ancient scrolls in search of a hidden fragment of knowledge might also call for an Intelligence (Investigation) check.

#### Nature
Your Intelligence (Nature) check measures your ability to recall lore about terrain, plants and animals, the weather, and natural cycles.

#### Religion
Your Intelligence (Religion) check measures your ability to recall lore about deities, rites and prayers, religious hierarchies, holy symbols, and the practices of secret cults.

#### Other Intelligence Checks
The DM might call for an Intelligence check when you try to accomplish tasks like the following:

* Communicate with a creature without using words
* Estimate the value of a precious item
* Pull together a disguise to pass as a city guard
* Forge a document
* Recall lore about a craft or trade
* Win a game of skill

#### Spellcasting Ability
Wizards use Intelligence as their spellcasting ability, which helps determine the saving throw DCs of spells they cast.`,
  wisdom: `
Wisdom reflects how attuned you are to the world around you and represents perceptiveness and intuition.

#### Wisdom Checks
A Wisdom check might reflect an effort to read body language, understand someone’s feelings, notice things about the environment, or care for an injured person. The Animal Handling, Insight, Medicine, Perception, and Survival skills reflect aptitude in certain kinds of Wisdom checks.

#### Animal Handling
When there is any question whether you can calm down a domesticated animal, keep a mount from getting spooked, or intuit an animal’s intentions, the DM might call for a Wisdom (Animal Handling) check. You also make a Wisdom (Animal Handling) check to control your mount when you attempt a risky maneuver.

#### Insight
Your Wisdom (Insight) check decides whether you can determine the true intentions of a creature, such as when searching out a lie or predicting someone’s next move. Doing so involves gleaning clues from body language, speech habits, and changes in mannerisms.

#### Medicine
A Wisdom (Medicine) check lets you try to stabilize a dying companion or diagnose an illness.

#### Perception
Your Wisdom (Perception) check lets you spot, hear, or otherwise detect the presence of something. It measures your general awareness of your surroundings and the keenness of your senses. For example, you might try to hear a conversation through a closed door, eavesdrop under an open window, or hear monsters moving stealthily in the forest. Or you might try to spot things that are obscured or easy to miss, whether they are orcs lying in ambush on a road, thugs hiding in the shadows of an alley, or candlelight under a closed secret door.

blockquote
#### FINDING A HIDDEN OBJECT

When your character searches for a hidden object such as a secret door or a trap, the DM typically asks you to make a Wisdom (Perception) check. Such a check can be used to find hidden details or other information and clues that you might otherwise overlook.

In most cases, you need to describe where you are looking in order for the DM to determine your chance of success. For example, a key is hidden beneath a set of folded clothes in the top drawer of a bureau. If you tell the DM that you pace around the room, looking at the walls and furniture for clues, you have no chance of finding the key, regardless of your Wisdom (Perception) check result. You would have to specify that you were opening the drawers or searching the bureau in order to have any chance of success.
fim-blockquote

#### Survival
The DM might ask you to make a Wisdom (Survival) check to follow tracks, hunt wild game, guide your group through frozen wastelands, identify signs that owlbears live nearby, predict the weather, or avoid quicksand and other natural hazards.

#### Other Wisdom Checks
The DM might call for a Wisdom check when you try to accomplish tasks like the following:

* Get a gut feeling about what course of action to follow
* Discern whether a seemingly dead or living creature is undead

#### Spellcasting Ability
Clerics, druids, and rangers use Wisdom as their spellcasting ability, which helps determine the saving throw DCs of spells they cast.`,
  charisma: `
Charisma measures your ability to interact effectively with others. It includes such factors as confidence and eloquence, and it can represent a charming or commanding personality.

#### Charisma Checks
A Charisma check might arise when you try to influence or entertain others, when you try to make an impression or tell a convincing lie, or when you are navigating a tricky social situation. The Deception, Intimidation, Performance, and Persuasion skills reflect aptitude in certain kinds of Charisma checks.

#### Deception
Your Charisma (Deception) check determines whether you can convincingly hide the truth, either verbally or through your actions. This deception can encompass everything from misleading others through ambiguity to telling outright lies. Typical situations include trying to fast-talk a guard, con a merchant, earn money through gambling, pass yourself off in a disguise, dull someone's suspicions with false assurances, or maintain a straight face while telling a blatant lie.

#### Intimidation
When you attempt to influence someone through overt threats, hostile actions, and physical violence, the DM might ask you to make a Charisma (Intimidation) check. Examples include trying to pry information out of a prisoner, convincing street thugs to back down from a confrontation, or using the edge of a broken bottle to convince a sneering vizier to reconsider a decision.

#### Performance
Your Charisma (Performance) check determines how well you can delight an audience with music, dance, acting, storytelling, or some other form of entertainment.

#### Persuasion
When you attempt to influence someone or a group of people with tact, social graces, or good nature, the DM might ask you to make a Charisma (Persuasion) check. Typically, you use persuasion when acting in good faith, to foster friendships, make cordial requests, or exhibit proper etiquette. Examples of persuading others include convincing a chamberlain to let your party see the king, negotiating peace between warring tribes, or inspiring a crowd of townsfolk.

#### Other Charisma Checks
The DM might call for a Charisma check when you try to accomplish tasks like the following:

* Find the best person to talk to for news, rumors, and gossip
* Blend into a crowd to get the sense of key topics of conversation

#### Spellcasting Ability
Bards, paladins, sorcerers, and warlocks use Charisma as their spellcasting ability, which helps determine the saving throw DCs of spells they cast.`,
};

export const PROFICIENCY_BONUS_RULES_TEXT = `
Characters have a proficiency bonus determined by level, as detailed in chapter 1. Monsters also have this bonus, which is incorporated in their stat blocks. The bonus is used in the rules on ability checks, saving throws, and attack rolls.

Your proficiency bonus can’t be added to a single die roll or other number more than once. For example, if two different rules say you can add your proficiency bonus to a Wisdom saving throw, you nevertheless add the bonus only once when you make the save.

Occasionally, your proficiency bonus might be multiplied or divided (doubled or halved, for example) before you apply it. For example, the rogue’s Expertise feature doubles the proficiency bonus for certain ability checks. If a circumstance suggests that your proficiency bonus applies more than once to the same roll, you still add it only once and multiply or divide it only once.

By the same token, if a feature or effect allows you to multiply your proficiency bonus when making an ability check that wouldn’t normally benefit from your proficiency bonus, you still don’t add the bonus to the check. For that check your proficiency bonus is 0, given the fact that multiplying 0 by any number is still 0. For instance, if you lack proficiency in the History skill, you gain no benefit from a feature that lets you double your proficiency bonus when you make Intelligence (History) checks.

In general, you don’t multiply your proficiency bonus for attack rolls or saving throws. If a feature or effect allows you to do so, these same rules apply.`;

export const SPEED_RULES_TEXT = `
Every character has a speed, which is the distance in feet that the character can walk in 1 round. This number assumes short bursts of energetic movement in the midst of a life-threatening situation.

While climbing or swimming, each foot of movement costs 1 extra foot (2 extra feet in difficult terrain), unless a creature has a climbing or swimming speed. At the DM’s option, climbing a slippery vertical surface or one with few handholds requires a successful Strength (Athletics) check. Similarly, gaining any distance in rough water might require a successful Strength (Athletics) check.

Your Strength determines how far you can jump.

**Long Jump.** When you make a long jump, you cover a number of feet up to your Strength score if you move at least 10 feet on foot immediately before the jump. When you make a standing long jump, you can leap only half that distance. Either way, each foot you clear on the jump costs a foot of movement.

This rule assumes that the height of your jump doesn't matter, such as a jump across a stream or chasm. At your DM's option, you must succeed on a DC 10 Strength (Athletics) check to clear a low obstacle (no taller than a quarter of the jump's distance), such as a hedge or low wall. Otherwise, you hit it.

When you land in difficult terrain, you must succeed on a DC 10 Dexterity (Acrobatics) check to land on your feet. Otherwise, you land prone.

**High Jump.** When you make a high jump, you leap into the air a number of feet equal to 3 + your Strength modifier (minimum of 0 feet) if you move at least 10 feet on foot immediately before the jump. When you make a standing high jump, you can jump only half that distance. Either way, each foot you clear on the jump costs a foot of movement. In some circumstances, your DM might allow you to make a Strength (Athletics) check to jump higher than you normally can.

You can extend your arms half your height above yourself during the jump. Thus, you can reach above you a distance equal to the height of the jump plus 1 1/2 times your height.`;

export const INITIATIVE_RULES_TEXT = `
Initiative scores can replace rolls at your DM's discretion. Your initiative score equals 10 plus your DEX modifier.

Initiative determines the order of turns during combat. When combat starts, every participant rolls Initiative; they make a Dexterity check that determines their place in the Initiative order. The DM rolls for monsters.

**Surprise.** If a combatant is surprised by combat starting, that combatant has Disadvantage on their Initiative roll. For example, if an ambusher starts combat while hidden from a foe who is unaware that combat is starting, that foe is surprised.

**Initiative Order.** A combatant’s check total is called their Initiative count, or Initiative for short. The DM ranks the combatants, from highest to lowest Initiative. This is the order in which they act during each round. The Initiative order remains the same from round to round.

**Ties.** If a tie occurs, the DM decides the order among tied monsters, and the players decide the order among tied characters. The DM decides the order if the tie is between a monster and a player character.

Sometimes a DM might have combatants use their Initiative scores instead of rolling Initiative. Your Initiative score equals 10 plus your Dexterity modifier. If you have Advantage on Initiative rolls, increase your Initiative score by 5. If you have Disadvantage on those rolls, decrease that score by 5.`;

export const ARMOR_CLASS_RULES_TEXT = `Your Armor Class (AC) represents how well your character avoids being wounded in battle. Things that contribute to your AC include the armor you wear, the shield you carry, and your Dexterity modifier. Not all characters wear armor or carry shields, however. Without armor or a shield, your character’s AC equals 10 + his or her Dexterity modifier. If your character wears armor, carries a shield, or both, calculate your AC using the rules in the Equipment section. Record your AC on your character sheet.`;

/** Shown both in the Saving Throws pane and in each single saving throw's pane. */
export const SAVING_THROWS_RULES_TEXT = `
A saving throw — also called a save — represents an attempt to resist a spell, a trap, a poison, a disease, or a similar threat. You don’t normally decide to make a saving throw; you are forced to make one because your character or monster is at risk of harm.

To make a saving throw, roll a d20 and add the appropriate ability modifier. For example, you use your Dexterity modifier for a Dexterity saving throw.

A saving throw can be modified by a situational bonus or penalty and can be affected by advantage and disadvantage, as determined by the DM.

Each class gives proficiency in at least two saving throws. The wizard, for example, is proficient in Intelligence saves. As with skill proficiencies, proficiency in a saving throw lets a character add his or her proficiency bonus to saving throws made using a particular ability score. Some monsters have saving throw proficiencies as well.

The Difficulty Class for a saving throw is determined by the effect that causes it. For example, the DC for a saving throw allowed by a spell is determined by the caster’s spellcasting ability and proficiency bonus.

The result of a successful or failed saving throw is also detailed in the effect that allows the save. Usually, a successful save means that a creature suffers no harm, or reduced harm, from an effect.`;

export const SENSES_RULES_TEXT = `
#### Passive Checks
A passive check is a special kind of ability check that doesn't involve any die rolls. Such a check can represent the average result for a task done repeatedly, such as searching for secret doors over and over again, or can be used when the DM wants to secretly determine whether the characters succeed at something without rolling dice, such as noticing a hidden monster.

Special senses are described below.

#### Blindsight
A monster with blindsight can perceive its surroundings without relying on sight, within a specific radius.

Creatures without eyes, such as grimlocks and gray oozes, typically have this special sense, as do creatures with echolocation or heightened senses, such as bats and true dragons.

If a monster is naturally blind, it has a parenthetical note to this effect, indicating that the radius of its blindsight defines the maximum range of its perception.

#### Darkvision
A monster with darkvision can see in the dark within a specific radius. The monster can see in dim light within the radius as if it were bright light, and in darkness as if it were dim light. The monster can't discern color in darkness, only shades of gray. Many creatures that live underground have this special sense.

#### Tremorsense
A monster with tremorsense can detect and pinpoint the origin of vibrations within a specific radius, provided that the monster and the source of the vibrations are in contact with the same ground or substance.

Tremorsense can't be used to detect flying or incorporeal creatures. Many burrowing creatures, such as ankhegs, have this special sense.

#### Truesight
A monster with truesight can, out to a specific range, see in normal and magical darkness, see invisible creatures and objects, automatically detect visual illusions and succeed on saving throws against them, and perceive the original form of a shapechanger or a creature that is transformed by magic. Furthermore, the monster can see into the Ethereal Plane within the same range.`;

export const SKILL_RULES_TEXT: Record<string, string> = {
  acrobatics: `Dexterity (Acrobatics) allows you to stay on your feet in a tricky situation, or perform an acrobatic stunt.`,
  animalHandling: `Wisdom (Animal Handling) allows you to calm or train an animal, or get an animal to behave in a certain way.`,
  arcana: `Intelligence (Arcana) allows you to recall lore about spells, magic items, and the planes of existence.`,
  athletics: `Strength (Athletics) allows you to jump farther than normal, stay afloat in rough water, or break something.`,
  deception: `Charisma (Deception) allows you to tell a convincing lie, or wear a disguise convincingly.`,
  history: `Intelligence (History) allows you to recall lore about historical events, people, nations, and cultures.`,
  insight: `Wisdom (Insight) allows you to discern a person’s mood and intentions.`,
  intimidation: `Charisma (Intimidation) allows you to awe or threaten someone into doing what you want.`,
  investigation: `Intelligence (Investigation) allows you to find obscure information in books, or deduce how something works.`,
  medicine: `Wisdom (Medicine) allows you to diagnose an illness, or determine what killed the recently slain.`,
  nature: `Intelligence (Nature) allows you to recall lore about terrain, plants, animals, and weather.`,
  perception: `Wisdom (Perception) allows you to use a combination of senses to notice something that’s easy to miss.`,
  performance: `Charisma (Performance) allows you to act, tell a story, perform music, or dance.`,
  persuasion: `Charisma (Persuasion) allows you to honestly and graciously convince someone of something.`,
  religion: `Intelligence (Religion) allows you to recall lore about gods, religious rituals, and holy symbols.`,
  sleightOfHand: `Dexterity (Sleight of Hand) allows you to pick a pocket, conceal a handheld object, or perform legerdemain.`,
  stealth: `Dexterity (Stealth) allows you to escape notice by moving quietly and hiding behind things.`,
  survival: `Wisdom (Survival) allows you to follow tracks, forage, find a trail, or avoid natural hazards.`,
};

/** Shown at the end of the HP Management pane, under "Death Saving Throws Rules", while the character is dying. */
export const DEATH_SAVES_RULES_TEXT = `
A player character must make a Death Saving Throw (also called a Death Save) if they start their turn with 0 Hit Points.

Whenever you start your turn with 0 Hit Points, you must make a Death Saving Throw to determine whether you creep closer to death or hang on to life. Unlike other saving throws, this one isn't tied to an ability score. You're in the hands of fate now.

**Three Successes/Failures.** Roll 1d20. If the roll is 10 or higher, you succeed. Otherwise, you fail. A success or failure has no effect by itself. On your third success, you become Stable. On your third failure, you die.

The successes and failures don't need to be consecutive; keep track of both until you collect three of a kind. The number of both is reset to zero when you regain any Hit Points or become Stable.

**Rolling a 1 or 20.** When you roll a 1 on the d20 for a Death Saving Throw, you suffer two failures. If you roll a 20 on the d20, you regain 1 Hit Point.

**Damage at 0 Hit Points.** If you take any damage while you have 0 Hit Points, you suffer a Death Saving Throw failure. If the damage is from a Critical Hit, you suffer two failures instead. If the damage equals or exceeds your Hit Point maximum, you die.`;

/** Each condition's rules, shown when its row in the Conditions pane is expanded. */
export const CONDITION_RULES_TEXT: Record<string, string> = {
  blinded: `A blinded creature can't see and automatically fails any ability check that requires sight.
Attack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.`,
  charmed: `A charmed creature can't attack the charmer or target the charmer with harmful abilities or magical effects.
The charmer has advantage on any ability check to interact socially with the creature.`,
  deafened: `A deafened creature can't hear and automatically fails any ability check that requires hearing.`,
  frightened: `A frightened creature has disadvantage on ability checks and attack rolls while the source of its fear is within line of sight.
The creature can't willingly move closer to the source of its fear.`,
  grappled: `A grappled creature's speed becomes 0, and it can't benefit from any bonus to its speed.
The condition ends if the grappler is incapacitated (see the condition).
The condition also ends if an effect removes the grappled creature from the reach of the grappler or grappling effect, such as when a creature is hurled away by the thunderwave spell.`,
  incapacitated: `An incapacitated creature can't take actions or reactions.`,
  invisible: `An invisible creature is impossible to see without the aid of magic or a special sense. For the purpose of hiding, the creature is heavily obscured. The creature's location can be detected by any noise it makes or any tracks it leaves.
Attack rolls against the creature have disadvantage, and the creature's attack rolls have advantage.`,
  paralyzed: `A paralyzed creature is incapacitated (see the condition) and can't move or speak.
The creature automatically fails Strength and Dexterity saving throws. Attack rolls against the creature have advantage.
Any attack that hits the creature is a critical hit if the attacker is within 5 feet of the creature.`,
  petrified: `A petrified creature is transformed, along with any nonmagical object it is wearing or carrying, into a solid inanimate substance (usually stone). Its weight increases by a factor of ten, and it ceases aging.
The creature is incapacitated (see the condition), can't move or speak, and is unaware of its surroundings.
Attack rolls against the creature have advantage.
The creature automatically fails Strength and Dexterity saving throws.
The creature has resistance to all damage.
The creature is immune to poison and disease, although a poison or disease already in its system is suspended, not neutralized.`,
  poisoned: `A poisoned creature has disadvantage on attack rolls and ability checks.`,
  prone: `A prone creature's only movement option is to crawl, unless it stands up and thereby ends the condition.
The creature has disadvantage on attack rolls.
An attack roll against the creature has advantage if the attacker is within 5 feet of the creature. Otherwise, the attack roll has disadvantage.`,
  restrained: `A restrained creature's speed becomes 0, and it can't benefit from any bonus to its speed.
Attack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.
The creature has disadvantage on Dexterity saving throws.`,
  stunned: `A stunned creature is incapacitated (see the condition), can't move, and can speak only falteringly.
The creature automatically fails Strength and Dexterity saving throws.
Attack rolls against the creature have advantage.`,
  unconscious: `An unconscious creature is incapacitated, can't move or speak, and is unaware of its surroundings.
The creature drops whatever it's holding and falls prone.
The creature automatically fails Strength and Dexterity saving throws.
Attack rolls against the creature have advantage.
Any attack that hits the creature is a critical hit if the attacker is within 5 feet of the creature.`,
};

/** Exhaustion's rules, shown when its row in the Conditions pane is expanded. */
export const EXHAUSTION_RULES_TEXT = `
Some special abilities and environmental hazards, such as starvation and the long-term effects of freezing or scorching temperatures, can lead to a special condition called exhaustion. Exhaustion is measured in six levels. An effect can give a creature one or more levels of exhaustion, as specified in the effect's description.

If an already exhausted creature suffers another effect that causes exhaustion, its current level of exhaustion increases by the amount specified in the effect's description.

A creature suffers the effect of its current level of exhaustion as well as all lower levels. For example, a creature suffering level 2 exhaustion has its speed halved and has disadvantage on ability checks.

An effect that removes exhaustion reduces its level as specified in the effect's description, with all exhaustion effects ending if a creature's exhaustion level is reduced below 1.

Finishing a long rest reduces a creature's exhaustion level by 1, provided that the creature has also ingested some food and drink. Also, being raised from the dead reduces a creature's exhaustion level by 1.

|Applied|Level|Effect|
|---|---|---|
| checkbox |1| Disadvantage on ability checks |
| checkbox |2| Speed halved |
| checkbox |3| Disadvantage on attack rolls and saving throws |
| checkbox |4| Hit point maximum halved |
| checkbox |5| Speed reduced to 0 |
| checkbox |6| You die |`;

/** Shown at the top of the Short Rest pane, above its controls. */
export const SHORT_REST_INTRO_TEXT = `A short rest is a period of downtime, at least 1 hour long, during which a character does nothing more strenuous than eating, drinking, reading, and tending to wounds.`;

/** Shown at the end of the Short Rest pane. */
export const SHORT_REST_RULES_TEXT = ``;

/** Shown at the top of the Long Rest pane, above its controls. */
export const LONG_REST_INTRO_TEXT = `
A Long Rest is a period of extended downtime—at least 8 hours—available to any creature. During a Long Rest, you sleep for at least 6 hours and perform no more than 2 hours of light activity, such as reading, talking, eating, or standing watch.

While asleep, you have the Unconscious condition. After you finish a Long Rest, you must wait at least 16 hours before starting another one.`;

/** Shown at the end of the Long Rest pane. */
export const LONG_REST_RULES_TEXT = `
**Benefits of the Rest.** To start a Long Rest, you must have at least 1 HP. When you finish the rest, you gain the following benefits:

* You regain all lost HP and all spent Hit Point Dice. If your HP maximum was reduced, it returns to normal.
* If any of your ability scores were reduced, they return to normal.
* If you have the Exhaustion condition, its level decreases by 1.
* If you have a feature that is recharged by a Long Rest, it recharges in the way specified in its description.

**Interrupting the Rest.** A Long Rest is stopped by the following interruptions:

* Rolling Initiative
* Casting a spell other than a cantrip
* Taking any damage
* 1 hour of walking or other physical exertion

If you rested at least 1 hour before the interruption, you gain the benefits of a Short Rest.

You can resume a Long Rest immediately after an interruption. If you do so, the rest requires 1 additional hour per interruption to finish.`;

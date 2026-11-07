import type { TutorialDefinition } from '../../../tutorial/tutorialDefinition';
import './daggerheartTutorial.css';

/** The Daggerheart builder tour: the three columns, then one stop per step with the rule it asks about, in our own words. */
export const DAGGERHEART_TUTORIAL: TutorialDefinition = {
  id: 'builder:daggerheart',
  className: 'tutorial--daggerheart',
  steps: [
    {
      title: 'Welcome to Daggerheart',
      body: (
        <>
          <p>This short tour walks through the character builder: what each column is for, and the rule behind each step.</p>
          <p className="tutorial__note">
            This tool does the arithmetic and lays your choices out. It doesn't replace the Daggerheart rules or your GM: agree on options with your table, and keep a
            printed sheet if your group plays with one.
          </p>
        </>
      ),
    },
    {
      title: 'Your progress',
      target: '[data-tutorial="checklist"]',
      builderStep: 'basics',
      body: (
        <>
          <p>Every step is listed here with what you picked so far. A blue check means it's done; Story is optional.</p>
          <p>Go in any order. The Character sheet button opens once nothing is missing.</p>
        </>
      ),
    },
    {
      title: 'The step',
      target: '[data-tutorial="step"]',
      builderStep: 'basics',
      body: (
        <>
          <p>The middle column holds the current step's choices. Start with a name, pronouns and a portrait; all three can change later.</p>
          <p>The (i) beside the title explains the step whenever you need a reminder.</p>
        </>
      ),
    },
    {
      title: 'Your character so far',
      target: '[data-tutorial="summary"]',
      builderStep: 'class',
      body: (
        <>
          <p>The summary adds everything up: Evasion, Hit Points, Stress, Hope, Armor Score, damage thresholds, traits and your hand of cards.</p>
          <p>Rest the pointer on an option to see what it would change, in purple, before you pick it.</p>
        </>
      ),
    },
    {
      title: 'Class and subclass',
      target: '[data-tutorial="step"]',
      builderStep: 'class',
      body: (
        <>
          <p>Your class gives your starting Evasion and Hit Points, a Hope feature that costs 3 Hope, its class features, and the two domains your cards come from.</p>
          <p>The subclass that follows names your Spellcast trait, if you have one, and gives its foundation features.</p>
        </>
      ),
    },
    {
      title: 'Heritage',
      target: '[data-tutorial="step"]',
      builderStep: 'heritage',
      body: (
        <>
          <p>Ancestry is your lineage, with two features; community is where you grew up, with one.</p>
          <p>For a mixed ancestry, take the first feature of one ancestry and the second of another. Transformations only appear if your GM allows them.</p>
        </>
      ),
    },
    {
      title: 'Traits',
      target: '[data-tutorial="step"]',
      builderStep: 'traits',
      body: (
        <>
          <p>Place +2, +1, +1, +0, +0 and −1 on Agility, Strength, Finesse, Instinct, Presence and Knowledge.</p>
          <p>On a roll you add the trait that fits what you do to your two Duality Dice, a Hope die and a Fear die.</p>
        </>
      ),
    },
    {
      title: 'Equipment',
      target: '[data-tutorial="step"]',
      builderStep: 'equipment',
      body: (
        <>
          <p>Pick Tier 1 gear: a two-handed weapon, or a one-handed weapon with a secondary one. Your armor sets your damage thresholds and Armor Slots.</p>
          <p>A potion, a class item and the usual supplies come with you.</p>
        </>
      ),
    },
    {
      title: 'Domain cards',
      target: '[data-tutorial="step"]',
      builderStep: 'domains',
      body: (
        <>
          <p>Take two level 1 cards from your class's domains. Each card's corner shows its Recall Cost: the Stress you mark to swap it in from the vault mid-scene.</p>
        </>
      ),
    },
    {
      title: 'Experiences and story',
      target: '[data-tutorial="step"]',
      builderStep: 'experiences',
      body: (
        <>
          <p>Write two Experiences at +2, short phrases you can spend a Hope on when they fit a roll.</p>
          <p>Story is optional: a description, your class's background questions and connections with the party.</p>
        </>
      ),
    },
    {
      title: 'Saved as you go',
      target: '[data-tutorial="save-state"]',
      body: <p>Every pick is saved on its own. Come back to this draft any time from your characters list. Reopen this tour from the button in the corner.</p>,
    },
  ],
};

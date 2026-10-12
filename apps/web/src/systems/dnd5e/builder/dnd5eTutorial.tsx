import type { TutorialDefinition } from '../../../tutorial/tutorialDefinition';
import './dnd5eTutorial.css';

/** The D&D 5e builder tour: one stop per part of the page and per step, with the rule basics each step asks about. */
export const DND5E_TUTORIAL: TutorialDefinition = {
  id: 'builder:dnd-5e',
  className: 'tutorial--dnd5e',
  steps: [
    {
      title: 'Welcome, adventurer',
      body: (
        <>
          <p>This short tour walks through the character builder: what each part of the page is for, and the basic rules behind each step.</p>
          <p className="tutorial__note">
            This tool does the arithmetic and lays your choices out so they are easy to see. It doesn't replace the rulebooks or your Dungeon Master: read the
            rules in your books, agree on options with your table, and keep a printed sheet if your group plays with one.
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
          <p>Every step of the build is listed here. A red count shows how many decisions are still open in that step, and a green check means it's done.</p>
          <p>Click any step or line to jump to it. You can go in any order.</p>
        </>
      ),
    },
    {
      title: 'Basics: who they are',
      target: '[data-tutorial="basics-identity"]',
      builderStep: 'basics',
      body: (
        <>
          <p>Give your character a name and a portrait. Both can be changed later on the sheet.</p>
          <p>The rest of Basics sets which books and rules this character follows. Ask your Dungeon Master first: these settings change what every other step offers.</p>
        </>
      ),
    },
    {
      title: 'Basics: books',
      target: '[data-tutorial="basics-sources"]',
      builderStep: 'basics',
      body: (
        <>
          <p>Only books turned on here show up in the builder: their species, classes and subclasses, backgrounds, feats, spells and items.</p>
          <p>
            The three core books (Player's Handbook, Dungeon Master's Guide and Monster Manual) are always on, since every other book builds on them. Use Core only if
            your table plays with just those.
          </p>
          <p className="tutorial__note">Turning a book off after choosing from it leaves those choices invalid, so pick your books before going further.</p>
        </>
      ),
    },
    {
      title: 'Basics: partnered and playtest',
      target: '[data-tutorial="basics-extra"]',
      builderStep: 'basics',
      body: (
        <>
          <p>Playtest content is Unearthed Arcana: rules still being tested, which may change or never be published. Many tables don't allow it.</p>
          <p>Partnered content comes from other publishers working with D&D. Turn it on only for the partners your table uses.</p>
        </>
      ),
    },
    {
      title: 'Basics: table rules',
      target: '[data-tutorial="basics-rules"]',
      builderStep: 'basics',
      body: (
        <>
          <ul>
            <li>
              <b>Advancement:</b> level up at story milestones, or by earning experience points.
            </li>
            <li>
              <b>Hit points on level up:</b> take each class's fixed value, or roll its hit die.
            </li>
            <li>
              <b>Optional class features:</b> extra and replacement features some classes can take instead of their originals.
            </li>
            <li>
              <b>Prerequisites:</b> with these on, feats and multiclassing are only offered when you meet their requirements.
            </li>
            <li>
              <b>Encumbrance:</b> whether the weight you carry is tracked.
            </li>
          </ul>
        </>
      ),
    },
    {
      title: 'Species',
      target: '[data-tutorial="step"] .builder-frame',
      builderStep: 'species',
      body: (
        <>
          <p>Your species sets your size and speed, gives a few traits (darkvision, resistances, extra languages) and usually some ability score increases.</p>
          <p>Some species ask you to pick which abilities increase, or offer a subspecies with its own traits.</p>
        </>
      ),
    },
    {
      title: 'Classes',
      target: '[data-tutorial="step"] .builder-frame',
      builderStep: 'classes',
      body: (
        <>
          <p>Your class is what your character does best: its hit die, proficiencies, features and, for casters, spells. Set the level here.</p>
          <p>Choices are grouped by the level that grants them; open a level to see them. To multiclass, add another class: each one has minimum ability scores.</p>
        </>
      ),
    },
    {
      title: 'Decisions still to make',
      target: '.builder-layout__main .builder-pending-mark',
      builderStep: 'classes',
      body: (
        <>
          <p>This red mark sits beside every decision that's still open. When a step has none left, its count in the progress list turns into a check.</p>
        </>
      ),
    },
    {
      title: 'Background',
      target: '[data-tutorial="step"] .builder-frame',
      builderStep: 'background',
      body: (
        <>
          <p>Your background is the life your character led before adventuring. It grants a couple of skills, tools or languages, some equipment and a feature.</p>
          <p>Personality traits, ideals, bonds and flaws are written later on the sheet.</p>
        </>
      ),
    },
    {
      title: 'Ability scores: the method',
      target: '[data-tutorial="ability-method"]',
      builderStep: 'abilities',
      body: (
        <>
          <p>Six scores describe your character: Strength, Dexterity, Constitution, Intelligence, Wisdom and Charisma.</p>
          <p>First pick how your table generates them: a fixed set of numbers to place, point buy with a budget, or your own rolls.</p>
        </>
      ),
    },
    {
      title: 'Ability scores: the totals',
      target: '[data-tutorial="ability-scores"]',
      builderStep: 'abilities',
      body: (
        <>
          <p>Each card shows how a total is reached: your base score plus bonuses from species, feats and improvements. The modifier under it is what gets added to rolls.</p>
          <p>Other modifier adds an extra bonus your table grants; Override score replaces the total outright.</p>
        </>
      ),
    },
    {
      title: 'Equipment',
      target: '[data-tutorial="step"] .builder-frame',
      builderStep: 'equipment',
      body: (
        <>
          <p>Take the starting equipment your class and background offer, picking one option in each choice, or take your class's starting gold and buy gear later.</p>
          <p>Taking gold asks for a roll; your background's coins are added to it.</p>
        </>
      ),
    },
    {
      title: 'Your character so far',
      target: '[data-tutorial="summary"]',
      body: (
        <>
          <p>A live preview of the character: ability scores, hit points, armor class and attacks update as you choose.</p>
        </>
      ),
    },
    {
      title: 'Review and finish',
      target: '[data-tutorial="step"] .builder-frame',
      builderStep: 'review',
      body: (
        <>
          <p>Review lists anything still open, with a link to each. When nothing is left, Finish character turns the draft into a playable sheet.</p>
          <p>Your draft saves on its own as you go, so you can leave and come back at any time.</p>
        </>
      ),
    },
    {
      title: 'That’s the tour',
      target: '[data-tutorial="help"]',
      body: (
        <>
          <p>Open it again from this button whenever you need it. Have fun at the table!</p>
        </>
      ),
    },
  ],
};

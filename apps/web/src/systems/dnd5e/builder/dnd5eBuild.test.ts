import { describe, expect, it } from 'vitest';
import type { BuildPlan, CreationChoice } from '../../../builder/draftApi';
import { checklistLines, withLocalAnswers } from '../../../builder/builderDefinition';
import { DND5E_BUILDER } from './dnd5eBuilderDefinition';
import {
  answerOf,
  dnd5eStepOf,
  orderOptionalSources,
  preferencesOf,
  withClassAdded,
  withClassLevel,
  withPartnerToggled,
  withSelection,
  withStartingItemEquipped,
  withoutStaleAnswers,
  type Dnd5eBuild,
} from './dnd5eBuild';

const EMPTY: Dnd5eBuild = {
  speciesSlug: null,
  subspeciesName: null,
  speciesVariantName: null,
  backgroundSlug: null,
  classes: [],
  abilityScoreMethod: null,
  baseAbilityScores: {},
  hitPointMethod: 'FIXED',
  rolledHitPoints: [],
  choices: [],
};

function choice(id: string): CreationChoice {
  return { id, type: 'X', parentChoiceId: null, prompt: id, sourceLabel: 'Test', count: 1, optional: false, pending: true, options: [], selected: [] };
}

function plan(...ids: string[]): BuildPlan {
  return { choices: ids.map(choice), problems: [], pendingCount: 0 };
}

describe('answerOf', () => {
  it('reads every pick back the way withSelection wrote it', () => {
    let build = withClassAdded(EMPTY, 'fighter');
    build = withSelection(build, 'build.species', ['dwarf']);
    build = withSelection(build, 'build.class.fighter.subclass', ['fighter-champion']);
    build = withSelection(build, 'class:fighter:skills:0', ['athletics', 'history']);

    expect(answerOf(build, 'build.species')).toEqual(['dwarf']);
    expect(answerOf(build, 'build.background')).toEqual([]);
    expect(answerOf(build, 'build.class.fighter.subclass')).toEqual(['fighter-champion']);
    expect(answerOf(build, 'class:fighter:skills:0')).toEqual(['athletics', 'history']);
    expect(answerOf(build, 'class:fighter:4:asi')).toEqual([]);
    expect(answerOf(build, 'build.classes')).toBeUndefined();
  });

  it('lets the screen show a pick before the saved plan catches up', () => {
    const build = withSelection(EMPTY, 'class:fighter:skills:0', ['athletics']);

    const live = withLocalAnswers(DND5E_BUILDER, plan('class:fighter:skills:0', 'build.classes'), build);

    expect(live.choices[0].selected).toEqual(['athletics']);
    expect(live.choices[0].pending).toBe(false);
    expect(live.choices[1].selected).toEqual([]);
  });
});

describe('checklist lines', () => {
  it('folds every pick of one kind into a single line, in a fixed order', () => {
    const typed = (id: string, type: string, pending: boolean): CreationChoice => ({ ...choice(id), type, pending });
    const lines = checklistLines(DND5E_BUILDER, [
      typed('class:bard:cantrips', 'CANTRIP', true),
      typed('class:bard:spells', 'SPELL', false),
      typed('class:wizard:spellbook', 'SPELLBOOK', true),
      typed('class:bard:skills:0', 'SKILL', false),
      typed('build.classes', 'CLASSES', false),
    ]);

    expect(lines).toEqual([
      { group: 'Class', total: 1, pending: 0 },
      { group: 'Proficiencies', total: 1, pending: 0 },
      { group: 'Spells', total: 3, pending: 2 },
    ]);
  });
});

describe('dnd5eStepOf', () => {
  it('places each choice on the step whose page lists it', () => {
    expect(dnd5eStepOf(choice('build.species'))).toBe('species');
    expect(dnd5eStepOf(choice('build.subspecies'))).toBe('species');
    expect(dnd5eStepOf(choice('species:dwarf:tools:0'))).toBe('species');
    expect(dnd5eStepOf(choice('build.background'))).toBe('background');
    expect(dnd5eStepOf(choice('background:acolyte:languages:0'))).toBe('background');
    expect(dnd5eStepOf(choice('build.classes'))).toBe('classes');
    expect(dnd5eStepOf(choice('build.class.fighter.subclass'))).toBe('classes');
    expect(dnd5eStepOf(choice('class:fighter:1:fighting-style'))).toBe('classes');
    expect(dnd5eStepOf(choice('build.abilityScores'))).toBe('abilities');
    expect(dnd5eStepOf(choice('class:fighter:equipment:0'))).toBe('equipment');
    expect(dnd5eStepOf(choice('background:acolyte:equipment:1'))).toBe('equipment');
  });
});

describe('withSelection', () => {
  it('sets structural fields and clears what depended on the old species', () => {
    const dwarf = withSelection(withSelection(EMPTY, 'build.species', ['dwarf']), 'build.subspecies', ['Mountain']);

    const elf = withSelection(dwarf, 'build.species', ['elf']);

    expect(dwarf.subspeciesName).toBe('Mountain');
    expect(elf).toMatchObject({ speciesSlug: 'elf', subspeciesName: null, speciesVariantName: null });
  });

  it('stores a subclass on its class entry', () => {
    const fighter = withSelection(withClassAdded(EMPTY, 'fighter'), 'build.class.fighter.subclass', ['champion']);

    expect(fighter.classes).toEqual([{ classSlug: 'fighter', subclassSlug: 'champion', level: 1 }]);
  });

  it('stores other picks as answers and removes an emptied one', () => {
    const answered = withSelection(EMPTY, 'class:fighter:skills:0', ['athletics', 'perception']);
    const cleared = withSelection(answered, 'class:fighter:skills:0', []);

    expect(answered.choices).toEqual([{ id: 'class:fighter:skills:0', selections: ['athletics', 'perception'] }]);
    expect(cleared.choices).toEqual([]);
  });
});

describe('classes', () => {
  it('adds a class once at level 1 and changes its level', () => {
    const build = withClassLevel(withClassAdded(withClassAdded(EMPTY, 'fighter'), 'fighter'), 'fighter', 3);

    expect(build.classes).toEqual([{ classSlug: 'fighter', subclassSlug: null, level: 3 }]);
  });
});

describe('withoutStaleAnswers', () => {
  it('drops answers the plan no longer offers and keeps the same build when nothing is stale', () => {
    const build = { ...EMPTY, choices: [{ id: 'species:dwarf:tools:0', selections: ['smiths-tools'] }, { id: 'class:fighter:skills:0', selections: ['athletics'] }] };

    const pruned = withoutStaleAnswers(build, plan('class:fighter:skills:0'));

    expect(pruned.choices).toEqual([{ id: 'class:fighter:skills:0', selections: ['athletics'] }]);
    expect(withoutStaleAnswers(pruned, plan('class:fighter:skills:0'))).toBe(pruned);
  });
});

describe('withStartingItemEquipped', () => {
  it('adds and removes a slug without duplicates', () => {
    const worn = withStartingItemEquipped(withStartingItemEquipped(EMPTY, 'chain-mail', true), 'chain-mail', true);

    expect(worn.equippedStartingItems).toEqual(['chain-mail']);
    expect(withStartingItemEquipped(worn, 'chain-mail', false).equippedStartingItems).toEqual([]);
  });
});

describe('withPartnerToggled', () => {
  const partners = ['Critical Role', 'Humblewood', 'Rick and Morty'];

  it('expands "every partner" into an explicit list when one is turned off', () => {
    expect(preferencesOf(withPartnerToggled(EMPTY, 'Humblewood', partners)).partners).toEqual(['Critical Role', 'Rick and Morty']);
  });

  it('collapses back to "every partner" once every partner is on again', () => {
    const off = withPartnerToggled(EMPTY, 'Humblewood', partners);

    expect(preferencesOf(withPartnerToggled(off, 'Humblewood', partners)).partners).toBeNull();
  });
});

describe('orderOptionalSources', () => {
  const source = (sourceCode: string, sourceBook: string) => ({ sourceCode, sourceBook, entryCount: 1 });

  it('lists the featured books first in their fixed order, then the rest alphabetically, without the locked ones', () => {
    const ordered = orderOptionalSources([
      source('EGW', "Explorer's Guide to Wildemount"),
      source('SCAG', "Sword Coast Adventurer's Guide"),
      source('PHB', "Player's Handbook"),
      source('AI', 'Acquisitions Incorporated'),
      source('TCE', "Tasha's Cauldron of Everything"),
      source('ERLW', 'Eberron: Rising from the Last War'),
      source('DMG', "Dungeon Master's Guide"),
      source('GGR', "Guildmasters' Guide to Ravnica"),
      source('XGE', "Xanathar's Guide to Everything"),
      source('BMT', 'The Book of Many Things'),
    ]);

    expect(ordered.map((entry) => entry.sourceCode)).toEqual(['XGE', 'TCE', 'GGR', 'ERLW', 'SCAG', 'AI', 'EGW', 'BMT']);
  });

  it('leaves partnered sources out, since their partner governs them', () => {
    const ordered = orderOptionalSources([source('XGE', "Xanathar's Guide to Everything"), { ...source('EGW', "Explorer's Guide to Wildemount"), partner: 'Critical Role' }]);

    expect(ordered.map((entry) => entry.sourceCode)).toEqual(['XGE']);
  });

  it('leaves playtest sources out, since their own switch governs them', () => {
    const ordered = orderOptionalSources([source('XGE', "Xanathar's Guide to Everything"), { ...source('UATheMysticClass', 'UA: The Mystic Class'), playtest: true }]);

    expect(ordered.map((entry) => entry.sourceCode)).toEqual(['XGE']);
  });
});

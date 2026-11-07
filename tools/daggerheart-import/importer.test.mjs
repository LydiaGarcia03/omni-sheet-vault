// Tests for the SRD importer's parsing steps, on synthetic text shaped like the SRD's layout.
// Run: node --test tools/daggerheart-import/importer.test.mjs

import assert from 'node:assert/strict';
import { test } from 'node:test';
import { splitColumns } from './columns.mjs';
import { parseRollTables, parseWeapons } from './equipment.mjs';
import { modifiersOf } from './modifiers.mjs';
import { features, slugify, titleCase, tokenize } from './text.mjs';
import { validate } from './validate.mjs';

const pad = (left, column, right) => left.padEnd(column) + right;

test('splitColumns reads the left column before the right one', () => {
  const page = [
    pad('LEFT HEADING', 66, 'RIGHT HEADING'),
    pad('left text one', 66, 'right text one'),
    pad('', 66, 'right only'),
    'left only',
  ].join('\n');

  assert.deepEqual(splitColumns(page).split('\n').filter(Boolean), [
    'LEFT HEADING', 'left text one', 'left only', 'RIGHT HEADING', 'right text one', 'right only',
  ]);
});

test('tokenize merges wrapped headings, splits label lines and rejoins paragraphs broken by a column', () => {
  const tokens = tokenize([
    '\u0000PAGE 4',
    'WORKING WITH',
    'YOUR FRIEND',
    'DOMAINS – Ash & Oak',
    'STARTING EVASION – 10',
    'CLASS ITEMS – A lantern or',
    'a map',
    '',
    'HOPE FEATURE',
    '',
    'A sentence that runs',
    '',
    'on into the next column.',
    '',
    '12 Daggerheart SRD',
  ].join('\n'));

  assert.deepEqual(tokens.map(token => token.type === 'heading' ? token.text : token.lines.join(' / ')), [
    'WORKING WITH YOUR FRIEND',
    'DOMAINS – Ash & Oak',
    'STARTING EVASION – 10',
    'CLASS ITEMS – A lantern or / a map',
    'HOPE FEATURE',
    'A sentence that runs / on into the next column.',
  ]);
  assert.equal(tokens[0].page, 4);
});

test('features splits "Name: text" entries, keeps asides and bullets with their feature', () => {
  const paragraphs = tokenize([
    'Steady Hands: You do a thing.',
    'Quick Feet: You do another thing.',
    '',
    '◦ First option.',
    '◦ Second option.',
    '',
    'Tip: An aside about it.',
  ].join('\n'));

  assert.deepEqual(features(paragraphs), [
    { name: 'Steady Hands', text: 'You do a thing.' },
    { name: 'Quick Feet', text: 'You do another thing.\n◦ First option.\n◦ Second option.\n\nTip: An aside about it.' },
  ]);
});

test('features accepts "Name. text" for the first feature of a section', () => {
  assert.deepEqual(features(tokenize('Encore. Your songs ring out.')), [{ name: 'Encore', text: 'Your songs ring out.' }]);
});

test('features rebuilds a roll table printed inside a feature', () => {
  const labelled = features(tokenize([
    'Phases: Roll a die.',
    '',
    'Roll Phase Effect',
    '1 Dim Gain a thing.',
    '2–3 Bright +1 to rolls',
    'Afterwards, clear the die.',
  ].join('\n')));
  const wrapped = features(tokenize([
    'Omens: Roll a die.',
    '',
    'Roll Effect',
    'You see a',
    '',
    '1–3 shape.',
    '',
    '4  You hear a sound.',
  ].join('\n')));

  assert.equal(labelled[0].text, 'Roll a die.\n◦ 1 (Dim): Gain a thing.\n◦ 2–3 (Bright): +1 to rolls\n\nAfterwards, clear the die.');
  assert.equal(wrapped[0].text, 'Roll a die.\n◦ 1–3: You see a shape.\n◦ 4: You hear a sound.');
});

test('parseWeapons reads wrapped names, wrapped damage types and features', () => {
  const page = [
    'TIER 2 (LEVELS 2–4)',
    'Name             Trait      Range       Damage     Burden      Feature',
    'Long             Agility    Melee       d10+3 phy  Two-Handed  Sharp: Deals more damage',
    'Blade                                                         to anyone.',
    'Odd Wand         Presence   Far         d6+2       One-Handed  —',
    '                                        phy/mag',
  ].join('\n');

  const [blade, wand] = parseWeapons([{ number: 57, text: page }], 57, 57);

  assert.equal(blade.name, 'Long Blade');
  assert.deepEqual(blade.damage, { die: 'd10', bonus: 3, type: 'physical' });
  assert.deepEqual(blade.feature, { name: 'Sharp', text: 'Deals more damage to anyone.' });
  assert.equal(blade.tier, 2);
  assert.equal(blade.burden, 2);
  assert.deepEqual(wand.damage, { die: 'd6', bonus: 2, type: 'physical-or-magic' });
  assert.equal(wand.feature, null);
});

test('parseRollTables reads two tables printed side by side', () => {
  const page = [
    'The following table includes the consumables from the Daggerheart Core Set.',
    'ROLL  LOOT          description                    ROLL  LOOT          description',
    '1     Red Tonic     Clear a thing.                 3     Tall Root     Leap far.',
    '2     Grey Salt     Rub it on a                    4     Sea Glass     See clearly.',
    '                    weapon.',
  ].join('\n');

  const rows = parseRollTables([{ number: 80, text: page }], 80, 80);

  assert.deepEqual(rows.map(row => [row.roll, row.name, row.description, row.set]), [
    [1, 'Red Tonic', 'Clear a thing.', 'core-set'],
    [3, 'Tall Root', 'Leap far.', 'core-set'],
    [2, 'Grey Salt', 'Rub it on a weapon.', 'core-set'],
    [4, 'Sea Glass', 'See clearly.', 'core-set'],
  ]);
});

test('modifiersOf reads always-on bonuses and skips conditional and roll bonuses', () => {
  assert.deepEqual(modifiersOf('−1 to Evasion; when you take a hit, you mark a thing.'), [{ target: 'evasion', value: -1 }]);
  assert.deepEqual(modifiersOf('Gain a permanent +1 bonus to your Armor Score and damage thresholds at character creation.'), [
    { target: 'armorScore', value: 1 }, { target: 'majorThreshold', value: 1 }, { target: 'severeThreshold', value: 1 },
  ]);
  assert.deepEqual(modifiersOf('Gain an additional Stress slot.'), [{ target: 'stressSlots', value: 1 }]);
  assert.deepEqual(modifiersOf('+1 to attack rolls'), []);
  assert.deepEqual(modifiersOf('You gain a +2 bonus to your Evasion against attacks made by a foe.'), []);
});

test('slugify and titleCase normalize SRD headings', () => {
  assert.equal(titleCase('WARDEN OF THE ELEMENTS'), 'Warden of the Elements');
  assert.equal(titleCase('SCORPION’S POISE'), 'Scorpion’s Poise');
  assert.equal(slugify('Scorpion’s Poise'), 'scorpions-poise');
});

test('validate rejects a parse whose counts do not match the SRD', () => {
  const empty = { domains: [], classes: [], ancestries: [], communities: [], transformations: [], cards: [], weapons: [], armor: [], loot: [], consumables: [], coreDomains: [], coreClasses: [], coreAncestries: [], coreCommunities: [] };
  assert.throws(() => validate(empty), /domains: expected 10, found 0/);
});

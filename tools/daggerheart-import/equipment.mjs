// Parses the equipment tables (weapons, armor, loot, consumables) from `pdftotext -table` pages.

import { normalize } from './text.mjs';

const FOOTER = /^\s*(\d+\s+)?Daggerheart SRD(\s+\d+)?\s*$/;
const DAMAGE = /^(d\d+)(?:\+(\d+))? (phy|mag|phy\/mag)$/;
const TIER_LINE = /^\s*TIER\s+(\d)\s/;
const DAMAGE_TYPES = { phy: 'physical', mag: 'magic', 'phy/mag': 'physical-or-magic' };

// Column start offsets taken from a table's header line.
function columns(header, names) {
  const lower = header.toLowerCase();
  let from = 0;
  return names.map(name => {
    const index = lower.indexOf(name.toLowerCase(), from);
    if (index < 0) throw new Error(`Column ${name} not found in "${header.trim()}"`);
    from = index + name.length;
    return Math.max(0, index - 1);
  });
}

function cells(line, starts, end = line.length) {
  return starts.map((start, i) => line.slice(start, i + 1 < starts.length ? starts[i + 1] : end).trim());
}

function pageLines(pages, first, last) {
  return pages
    .filter(page => page.number >= first && page.number <= last)
    .flatMap(page => [{ pageBreak: page.number }, ...normalize(page.text).split('\n').map(text => ({ text, page: page.number }))]);
}

// Joins a wrapped cell line; a line ending in a word hyphen continues the same word.
function append(text, more) {
  return /[A-Za-z]-$/.test(text) ? text + more : `${text} ${more}`;
}

export function parseFeatureCell(text) {
  if (!text || text === '—') return null;
  const match = text.match(/^([^:]+): (.+)$/s);
  if (!match) throw new Error(`Feature without a name: "${text}"`);
  return { name: match[1], text: match[2] };
}

const WEAPON_ROW = /^(\s*)(\S.*?)\s{2,}(?:(\d)\s{2,})?(Agility|Strength|Finesse|Instinct|Presence|Knowledge|Spellcast)\s+(Melee|Very Close|Close|Far|Very Far)\s+(d\d+(?:\+\d+)?(?: (?:phy|mag|phy\/mag))?)\s+((?:One|Two)-Handed)(?:\s+(\S.*))?$/;
const ARMOR_ROW = /^(\s*)(\S.*?)\s{2,}(\d+) \/ (\d+)\s{2,}(\d+)\s{2,}(\S.*)$/;

// Text segments of a line (runs separated by 2+ spaces) with their start columns.
function segments(text) {
  return [...text.matchAll(/\S+(?: \S+)*/g)].map(match => ({ start: match.index, text: match[0] }));
}

// Columns are taken from the row line itself: the name, the middle cells, and the feature text.
function rowColumns(text, firstMiddle, feature) {
  return { middle: text.indexOf(firstMiddle), feature: feature ? text.lastIndexOf(feature) : null };
}

function continueRow(row, text, layout, middleField) {
  for (const segment of segments(text)) {
    if (segment.start + segment.text.length < layout.middle) row.name = append(row.name, segment.text);
    else if (layout.feature !== null && segment.start >= layout.feature - 2) row.featureText = append(row.featureText, segment.text);
    else if (middleField) row[middleField] = append(row[middleField], segment.text);
    else return false;
  }
  return true;
}

function isContextLine(text) {
  return TIER_LINE.test(text) || /WEAPON TABLES|^\s*(Physical|Magic) Weapons\s*$|Frame Models\s*$|^\s*Name\s{2,}|^\s*Base\s{2,}Base/.test(text);
}

export function parseWeapons(pages, first, last) {
  const weapons = [];
  let slot = 'primary';
  let tier = null;
  let wheelchair = false;
  let open = null;
  for (const line of pageLines(pages, first, last)) {
    const text = line.text ?? '';
    if (line.pageBreak) open = null;
    if (line.pageBreak || text.trim() === '' || FOOTER.test(text)) continue;
    if (/SECONDARY WEAPON TABLES/.test(text)) slot = 'secondary';
    if (/Frame Models\s*$/.test(text)) {
      slot = 'primary';
      wheelchair = true;
    }
    const tierMatch = text.match(TIER_LINE);
    if (tierMatch) tier = Number(tierMatch[1]);
    if (isContextLine(text)) {
      open = null;
      continue;
    }
    const row = text.match(WEAPON_ROW);
    if (row) {
      open = {
        name: row[2],
        page: line.page,
        slot,
        tier: row[3] ? Number(row[3]) : tier,
        trait: row[4],
        range: row[5],
        damageText: row[6],
        burden: row[7].startsWith('Two') ? 2 : 1,
        featureText: row[8] ?? '',
        wheelchair,
        columns: rowColumns(text, row[3] ?? row[4], row[8]),
      };
      weapons.push(open);
    } else if (!open || !continueRow(open, text, open.columns, 'damageText')) {
      open = null;
    }
  }
  return weapons.map(({ featureText, damageText, columns: _, ...weapon }) => ({
    ...weapon,
    damage: parseDamage(damageText, weapon),
    feature: parseFeatureCell(featureText.trim()),
  }));
}

function parseDamage(text, weapon) {
  const damage = text.match(DAMAGE);
  if (!damage) throw new Error(`${weapon.name} (page ${weapon.page}): damage "${text}" does not parse`);
  return { die: damage[1], bonus: Number(damage[2] ?? 0), type: DAMAGE_TYPES[damage[3]] };
}

export function parseArmor(pages, first, last) {
  const armor = [];
  let tier = null;
  let open = null;
  for (const line of pageLines(pages, first, last)) {
    const text = line.text ?? '';
    if (line.pageBreak) open = null;
    if (line.pageBreak || text.trim() === '' || FOOTER.test(text)) continue;
    const tierMatch = text.match(TIER_LINE);
    if (tierMatch) tier = Number(tierMatch[1]);
    if (isContextLine(text)) {
      open = null;
      continue;
    }
    const row = text.match(ARMOR_ROW);
    if (row) {
      open = {
        name: row[2],
        page: line.page,
        tier,
        baseThresholds: { major: Number(row[3]), severe: Number(row[4]) },
        baseScore: Number(row[5]),
        featureText: row[6],
        columns: rowColumns(text, `${row[3]} /`, row[6]),
      };
      armor.push(open);
    } else if (open && !continueRow(open, text, open.columns, null)) {
      open = null;
    }
  }
  return armor.map(({ featureText, columns: _, ...entry }) => ({ ...entry, feature: parseFeatureCell(featureText.trim()) }));
}
// Loot and consumable tables: numbered rows, sometimes two tables side by side.
export function parseRollTables(pages, first, last) {
  const rows = [];
  let set = null;
  let tables = null;
  for (const line of pageLines(pages, first, last)) {
    if (line.pageBreak) {
      tables = null;
      continue;
    }
    const text = line.text;
    if (text.trim() === '' || FOOTER.test(text)) continue;
    const setMatch = text.match(/The following table includes the \w+ from the (.+?)\.\s*$/);
    if (setMatch) {
      set = /Hope & Fear/.test(setMatch[1]) ? 'hope-and-fear' : 'core-set';
      tables = null;
      continue;
    }
    if (/^\s*ROLL\s+Loot\s+description/i.test(text)) {
      tables = rollTableColumns(text);
      continue;
    }
    if (!tables) continue;
    let ended = false;
    tables.forEach((table, index) => {
      const [roll, name, description] = cells(text, table.starts, table.end);
      const open = table.open;
      if (/^\d+$/.test(roll) && name) {
        table.open = { set, roll: Number(roll), name, description, page: line.page, column: index };
        rows.push(table.open);
      } else if (!roll && open && (name || description)) {
        if (name) open.name = append(open.name, name);
        if (description) open.description = append(open.description, description);
      } else if (roll || (!open && (name || description))) {
        ended = true;
      }
    });
    if (ended) tables = null;
  }
  return rows.map(({ column, ...row }) => ({ ...row, name: row.name.replace(/- /g, '-'), description: row.description.replace(/\s+/g, ' ').trim() }));
}

// One column set per table on the line; a trailing prose heading (e.g. "GOLD") closes the left table.
function rollTableColumns(header) {
  const names = ['ROLL', 'LOOT', 'DESCRIPTION'];
  const rolls = [...header.matchAll(/ROLL/gi)].map(match => match.index);
  const trailing = header.match(/description\s{2,}([A-Z]{3,})\s*$/);
  if (rolls.length > 1) {
    const split = rolls[1] - 1;
    return [
      { starts: columns(header.slice(0, split), names), end: split },
      { starts: columns(' '.repeat(split) + header.slice(split), names), end: undefined },
    ];
  }
  const end = trailing ? header.lastIndexOf(trailing[1]) - 1 : undefined;
  return [{ starts: columns(end ? header.slice(0, end) : header, names), end }];
}

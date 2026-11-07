// Converts the Daggerheart SRD 2.0 PDF into catalogue files under content/daggerheart/.
// Usage: node tools/daggerheart-import/import.mjs [path/to/DH_SRD.pdf]

import { execFileSync } from 'node:child_process';
import { mkdirSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseClasses } from './classes.mjs';
import { splitPages } from './columns.mjs';
import { buildEntries } from './entries.mjs';
import { parseDomainCards, parseDomains } from './domains.mjs';
import { parseArmor, parseRollTables, parseWeapons } from './equipment.mjs';
import { coreSetNames, parseAncestries, parseCommunities, parseTransformations } from './heritage.mjs';
import { sectionText, tokenize } from './text.mjs';
import { validate } from './validate.mjs';

const repoRoot = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const pdf = resolve(process.argv[2] ?? join(repoRoot, 'tools/daggerheart-srd/DH_SRD_2_2026_08_25.pdf'));
const outputRoot = join(repoRoot, 'content/daggerheart');

// Page ranges of the SRD 2.0 (2026-08-25).
const PAGES = {
  domains: [7, 8],
  classes: [9, 31],
  heritage: [32, 45],
  weapons: [56, 71],
  armor: [72, 74],
  loot: [75, 79],
  consumables: [80, 84],
  cards: [206, 225],
};

function extract(mode) {
  const text = execFileSync('pdftotext', [`-${mode}`, '-enc', 'UTF-8', pdf, '-'], { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 });
  return text.split('\f').map((pageText, index) => ({ number: index + 1, text: pageText }));
}

function tokensFor(pages, [first, last]) {
  return tokenize(sectionText(pages, first, last));
}

function parse() {
  const layoutPages = splitPages(extract('layout').map(page => page.text).join('\f'));
  const tablePages = extract('table');
  const heritageTokens = tokensFor(layoutPages, PAGES.heritage);
  const domainTokens = tokensFor(layoutPages, PAGES.domains);
  return {
    domains: parseDomains(domainTokens),
    coreDomains: coreSetNames(domainTokens, 'DOMAINS'),
    classes: parseClasses(tokensFor(layoutPages, PAGES.classes)),
    coreClasses: coreSetNames(domainTokens, 'CLASSES'),
    ancestries: parseAncestries(heritageTokens),
    coreAncestries: coreSetNames(heritageTokens, 'ANCESTRIES'),
    communities: parseCommunities(heritageTokens),
    coreCommunities: coreSetNames(heritageTokens, 'COMMUNITIES'),
    transformations: parseTransformations(heritageTokens),
    cards: parseDomainCards(tokensFor(layoutPages, PAGES.cards)),
    weapons: parseWeapons(tablePages, ...PAGES.weapons),
    armor: parseArmor(tablePages, ...PAGES.armor),
    loot: parseRollTables(tablePages, ...PAGES.loot),
    consumables: parseRollTables(tablePages, ...PAGES.consumables),
  };
}

function write(entries) {
  rmSync(outputRoot, { recursive: true, force: true });
  for (const entry of entries) {
    const directory = join(outputRoot, entry.directory);
    mkdirSync(directory, { recursive: true });
    const { directory: _, ...json } = entry;
    writeFileSync(join(directory, `${entry.slug}.json`), `${JSON.stringify(json, null, 2)}\n`);
  }
}

const srd = parse();
validate(srd);
const entries = buildEntries(srd);
write(entries);
const byDirectory = Object.groupBy(entries, entry => entry.directory);
for (const [directory, list] of Object.entries(byDirectory)) console.log(`${directory.padEnd(16)} ${list.length}`);
console.log(`Wrote ${entries.length} entries to ${outputRoot}`);

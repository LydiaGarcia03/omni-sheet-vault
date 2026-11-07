// Fails the import when the parsed SRD does not match the counts and cross-references the SRD itself states.

const EXPECTED = {
  domains: 10,
  classes: 13,
  ancestries: 24,
  communities: 15,
  transformations: 6,
  cards: 210,
  weapons: 315,
  armor: 69,
  beastforms: 24,
  martialStances: 16,
};

export function validate(srd) {
  const errors = [];
  const check = (condition, message) => {
    if (!condition) errors.push(message);
  };
  const count = (name, actual) => check(actual === EXPECTED[name], `${name}: expected ${EXPECTED[name]}, found ${actual}`);

  for (const name of ['domains', 'classes', 'ancestries', 'communities', 'transformations', 'cards', 'weapons', 'armor']) {
    count(name, srd[name].length);
  }

  const domainNames = new Set(srd.domains.map(domain => domain.name));
  for (const playerClass of srd.classes) {
    const label = `class ${playerClass.name}`;
    check(playerClass.domains.length === 2 && playerClass.domains.every(domain => domainNames.has(domain)), `${label}: domains ${playerClass.domains}`);
    for (const domain of playerClass.domains) {
      const access = srd.domains.find(entry => entry.name === domain)?.classes ?? [];
      check(access.includes(playerClass.name), `${label}: domain ${domain} does not list the class`);
    }
    check(Number.isInteger(playerClass.startingEvasion) && Number.isInteger(playerClass.startingHitPoints), `${label}: Evasion/HP`);
    check(playerClass.classItems, `${label}: class items`);
    check(playerClass.hopeFeature, `${label}: Hope feature`);
    check(playerClass.features.length > 0, `${label}: class features`);
    check(playerClass.backgroundQuestions.length > 0 && playerClass.connections.length > 0, `${label}: questions`);
    check(playerClass.subclasses.length === 2, `${label}: ${playerClass.subclasses.length} subclasses`);
    for (const subclass of playerClass.subclasses) {
      for (const stage of ['foundation', 'specialization', 'mastery']) {
        check(subclass[stage].length > 0, `subclass ${subclass.name}: no ${stage} feature`);
      }
      check(subclass.pitch?.startsWith(`Play the ${subclass.name}`), `subclass ${subclass.name}: pitch "${subclass.pitch}"`);
    }
  }
  count('beastforms', srd.classes.find(entry => entry.name === 'Druid')?.beastforms?.length);
  count('martialStances', srd.classes.find(entry => entry.name === 'Brawler')?.martialStances?.length);

  for (const domain of srd.domains) {
    const levels = srd.cards.filter(card => card.domain === domain.name).map(card => card.level);
    const perLevel = Array.from({ length: 10 }, (_, index) => levels.filter(level => level === index + 1).length);
    check(perLevel.join() === '3,2,2,2,2,2,2,2,2,2', `domain ${domain.name}: cards per level ${perLevel}`);
  }

  srd.ancestries.forEach(entry => check(entry.features.length === 2, `ancestry ${entry.name}: ${entry.features.length} features`));
  srd.communities.forEach(entry => {
    check(entry.features.length === 1, `community ${entry.name}: ${entry.features.length} features`);
    check(entry.adjectives.length === 6, `community ${entry.name}: ${entry.adjectives.length} adjectives`);
  });
  srd.transformations.forEach(entry => check(entry.features.length === 2 && entry.questions.length > 0, `transformation ${entry.name}`));

  for (const [list, names] of [['domains', srd.coreDomains], ['classes', srd.coreClasses], ['ancestries', srd.coreAncestries], ['communities', srd.coreCommunities]]) {
    const known = new Set(srd[list].map(entry => entry.name));
    for (const name of names.filter(name => name !== 'Mixed Ancestry')) check(known.has(name), `Core Set ${list}: unknown "${name}"`);
  }

  for (const [name, rows] of [['loot', srd.loot], ['consumables', srd.consumables]]) {
    for (const set of ['core-set', 'hope-and-fear']) {
      const rolls = rows.filter(row => row.set === set).map(row => row.roll).sort((a, b) => a - b);
      check(rolls.join() === Array.from({ length: 60 }, (_, index) => index + 1).join(), `${name} ${set}: rolls ${rolls}`);
    }
  }

  if (errors.length > 0) throw new Error(`SRD import failed validation:\n- ${errors.join('\n- ')}`);
}

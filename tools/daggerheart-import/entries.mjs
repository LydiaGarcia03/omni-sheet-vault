// Maps the parsed SRD to catalogue entry files (the CatalogueEntryImport shape the API imports).

import { withModifiers } from './modifiers.mjs';
import { slugify } from './text.mjs';

const featureList = features => features.map(withModifiers);

const SOURCE = { sourceBook: 'Daggerheart SRD 2.0', sourceCode: 'DHSRD' };

function entry(directory, kind, name, page, description, data, tags = []) {
  return { directory, systemId: 'daggerheart', kind, slug: slugify(name), name, ...SOURCE, sourcePage: page, tags, description: description || null, data };
}

function setTag(name, coreNames) {
  return [coreNames.includes(name) ? 'core-set' : 'expansion'];
}

function featureLine(feature) {
  return feature ? `${feature.name}: ${feature.text}` : null;
}

function ruleSections(rules) {
  return rules.map(({ title, page, blocks }) => {
    const sections = [{ heading: null, paragraphs: [] }];
    for (const block of blocks) {
      if (block.heading) sections.push({ heading: block.heading, paragraphs: [] });
      else sections.at(-1).paragraphs.push(block.text);
    }
    return { title, page, sections: sections.filter(section => section.heading || section.paragraphs.length > 0) };
  });
}

function classEntries(srd) {
  return srd.classes.flatMap(playerClass => {
    const classSlug = slugify(playerClass.name);
    const data = {
      domains: playerClass.domains.map(slugify),
      startingEvasion: playerClass.startingEvasion,
      startingHitPoints: playerClass.startingHitPoints,
      classItems: playerClass.classItems,
      hopeFeature: withModifiers(playerClass.hopeFeature),
      features: featureList(playerClass.features),
      subclasses: playerClass.subclasses.map(subclass => slugify(subclass.name)),
      backgroundQuestions: playerClass.backgroundQuestions,
      connectionQuestions: playerClass.connections,
      rules: ruleSections(playerClass.rules),
      ...(playerClass.beastforms && { beastforms: playerClass.beastforms }),
      ...(playerClass.martialStances && { martialStances: playerClass.martialStances }),
    };
    const tags = setTag(playerClass.name, srd.coreClasses);
    const subclasses = playerClass.subclasses.map(subclass =>
      entry('subclasses', 'SUBCLASS', subclass.name, subclass.page, subclass.pitch, {
        classSlug,
        spellcastTrait: subclass.spellcastTrait,
        foundation: featureList(subclass.foundation),
        specialization: featureList(subclass.specialization),
        mastery: featureList(subclass.mastery),
      }, tags));
    return [entry('classes', 'CLASS', playerClass.name, playerClass.page, playerClass.description, data, tags), ...subclasses];
  });
}

function domainEntries(srd) {
  const domains = srd.domains.map(domain =>
    entry('domains', 'DOMAIN', domain.name, domain.page, domain.description, { classes: domain.classes.map(slugify) }, setTag(domain.name, srd.coreDomains)));
  const cards = srd.cards.map(card =>
    entry('domain_cards', 'DOMAIN_CARD', card.name, card.page, card.text,
      { domain: slugify(card.domain), level: card.level, type: card.type, recallCost: card.recallCost },
      setTag(card.domain, srd.coreDomains)));
  return [...domains, ...cards];
}

function heritageEntries(srd) {
  return [
    ...srd.ancestries.map(ancestry =>
      entry('ancestries', 'ANCESTRY', ancestry.name, ancestry.page, ancestry.description,
        { features: featureList(ancestry.features), family: ancestry.family }, setTag(ancestry.name, srd.coreAncestries))),
    ...srd.communities.map(community =>
      entry('communities', 'COMMUNITY', community.name, community.page, community.description,
        { adjectives: community.adjectives, feature: withModifiers(community.features[0]) }, setTag(community.name, srd.coreCommunities))),
    ...srd.transformations.map(transformation =>
      entry('transformations', 'TRANSFORMATION', transformation.name, transformation.page, transformation.description,
        { features: featureList(transformation.features), questions: transformation.questions })),
  ];
}

function itemEntries(srd) {
  return [
    ...srd.weapons.map(({ name, page, ...weapon }) => entry('items', 'ITEM', name, page, featureLine(weapon.feature), { category: 'weapon', ...weapon, feature: withModifiers(weapon.feature) })),
    ...srd.armor.map(({ name, page, ...armor }) => entry('items', 'ITEM', name, page, featureLine(armor.feature), { category: 'armor', ...armor, feature: withModifiers(armor.feature) })),
    ...srd.loot.map(row => entry('items', 'ITEM', row.name, row.page, row.description, { category: 'loot', roll: row.roll }, [row.set])),
    ...srd.consumables.map(row => entry('items', 'ITEM', row.name, row.page, row.description, { category: 'consumable', roll: row.roll }, [row.set])),
  ];
}

export function buildEntries(srd) {
  const entries = [...classEntries(srd), ...domainEntries(srd), ...heritageEntries(srd), ...itemEntries(srd)];
  const seen = new Map();
  for (const item of entries) {
    const key = `${item.directory}/${item.slug}`;
    if (seen.has(key)) throw new Error(`Two entries share ${key}: "${seen.get(key)}" and "${item.name}"`);
    seen.set(key, item.name);
  }
  return entries;
}

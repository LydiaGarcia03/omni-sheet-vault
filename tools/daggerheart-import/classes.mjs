// Parses the class chapter: classes, subclasses, and each class's extra rules (beastforms, stances, companion).

import { bulletItems, features, featureStart, joinParagraphs, paragraphText, prose, titleCase } from './text.mjs';

const LABEL = /^(DOMAINS|STARTING EVASION|STARTING HIT POINTS|CLASS ITEMS) – (.+)$/s;
const SUBCLASS_SECTIONS = {
  'SPELLCAST TRAIT': 'spellcastTrait',
  'FOUNDATION FEATURE': 'foundation',
  'FOUNDATION FEATURES': 'foundation',
  'SPECIALIZATION FEATURE': 'specialization',
  'SPECIALIZATION FEATURES': 'specialization',
  'MASTERY FEATURE': 'mastery',
  'MASTERY FEATURES': 'mastery',
};
// Rules chapters printed inside a class; the headings under them are sub-sections, not subclasses.
const RULES_CHAPTERS = new Set(['BEASTFORM OPTIONS', 'MARTIAL STANCES', 'STANCE FEATURES', 'RANGER COMPANION', 'SPHERE OF INFLUENCE EXAMPLES']);

export function parseClasses(tokens) {
  const classes = [];
  let current = null;
  let section = null;
  let subclass = null;
  let buffer = [];

  const flush = () => {
    if (!current || !section) return;
    applySection(current, subclass, section, buffer);
    buffer = [];
  };

  for (let i = 0; i < tokens.length; i++) {
    const token = tokens[i];
    const label = labelOf(token);
    if (token.type === 'heading' && !label && startsClass(tokens, i)) {
      flush();
      current = newClass(token);
      classes.push(current);
      section = { kind: 'intro' };
      subclass = null;
      continue;
    }
    if (!current) continue;
    if (label) {
      flush();
      section = { kind: 'label', name: label.name };
      buffer = [token];
      continue;
    }
    if (token.type === 'para') {
      buffer.push(token);
      continue;
    }
    flush();
    const heading = token.text;
    if (/’S HOPE FEATURE$/.test(heading)) section = { kind: 'hope' };
    else if (/^CLASS FEATURES?$/.test(heading)) section = { kind: 'classFeatures' };
    else if (/ SUBCLASSES$/.test(heading)) section = { kind: 'subclassIntro' };
    else if (heading === 'BACKGROUND QUESTIONS') section = { kind: 'background' };
    else if (heading === 'CONNECTIONS') section = { kind: 'connections' };
    else if (SUBCLASS_SECTIONS[heading] && subclass) section = { kind: 'subclass', field: SUBCLASS_SECTIONS[heading] };
    else if (RULES_CHAPTERS.has(heading)) {
      section = { kind: 'rules' };
      current.rules.push({ title: titleCase(heading), page: token.page, blocks: [] });
    } else if (section.kind === 'rules') {
      current.rules.at(-1).blocks.push({ heading: titleCase(heading) });
    } else if (section.kind === 'subclassIntro' || section.kind === 'subclass' || section.kind === 'subclassPitch') {
      subclass = newSubclass(current, token);
      section = { kind: 'subclassPitch' };
    } else {
      throw new Error(`${current.name}: unexpected heading "${heading}" (page ${token.page})`);
    }
  }
  flush();
  classes.forEach(finishClass);
  return classes;
}

function labelOf(token) {
  const text = token.type === 'heading' ? token.text : prose(token.lines);
  const match = text.match(LABEL);
  return match ? { name: match[1], value: match[2] } : null;
}

function startsClass(tokens, index) {
  for (let i = index + 1; i < tokens.length; i++) {
    const label = labelOf(tokens[i]);
    if (label) return label.name === 'DOMAINS';
    if (tokens[i].type === 'heading') return false;
  }
  return false;
}

function newClass(token) {
  return {
    name: titleCase(token.text),
    page: token.page,
    descriptionParas: [],
    domains: [],
    startingEvasion: null,
    startingHitPoints: null,
    classItems: null,
    hopeFeature: null,
    features: [],
    subclasses: [],
    backgroundQuestions: [],
    connections: [],
    rules: [],
  };
}

function newSubclass(current, token) {
  const subclass = {
    name: titleCase(token.text),
    page: token.page,
    pitch: null,
    spellcastTrait: null,
    foundation: [],
    specialization: [],
    mastery: [],
  };
  current.subclasses.push(subclass);
  return subclass;
}

function applySection(current, subclass, section, paras) {
  switch (section.kind) {
    case 'intro':
      current.descriptionParas.push(...paras);
      break;
    case 'label':
      applyLabel(current, paras);
      break;
    case 'hope': {
      const [feature, ...rest] = features(paras);
      if (!feature || rest.length > 0) throw new Error(`${current.name}: expected one Hope feature`);
      current.hopeFeature = feature;
      break;
    }
    case 'classFeatures':
      current.features.push(...features(paras));
      break;
    case 'subclassIntro':
      break;
    case 'subclassPitch':
      subclass.pitch = joinParagraphs(paras);
      break;
    case 'subclass':
      if (section.field === 'spellcastTrait') subclass.spellcastTrait = joinParagraphs(paras);
      else subclass[section.field].push(...features(paras));
      break;
    case 'background':
      current.backgroundQuestions.push(...paras.flatMap(bulletItems));
      break;
    case 'connections':
      current.connections.push(...paras.flatMap(bulletItems));
      break;
    case 'rules':
      current.rules.at(-1).blocks.push(...paras.map(token => ({ lines: token.lines, text: rulesParagraph(token) })));
      break;
    default:
      throw new Error(`Unknown class section ${section.kind}`);
  }
}

// A word grid (e.g. the warlock's spheres of influence) becomes one alphabetical, comma-separated list.
function rulesParagraph(token) {
  if (!token.lines.every(line => /\S {2,}\S/.test(line))) return paragraphText(token);
  return token.lines.flatMap(line => line.split(/ {2,}/)).sort((a, b) => a.localeCompare(b)).join(', ');
}

function applyLabel(current, paras) {
  const [first, ...rest] = paras;
  const label = labelOf(first);
  const value = [label.value, ...rest.map(paragraphText)].join(' ').trim();
  if (label.name === 'DOMAINS') current.domains = value.split(/\s*&\s*/);
  else if (label.name === 'STARTING EVASION') current.startingEvasion = Number(value);
  else if (label.name === 'STARTING HIT POINTS') current.startingHitPoints = Number(value);
  else current.classItems = value;
}

function finishClass(current) {
  current.description = joinParagraphs(current.descriptionParas);
  delete current.descriptionParas;
  for (const rules of current.rules) {
    if (rules.title === 'Beastform Options') current.beastforms = beastforms(rules);
    if (rules.title === 'Stance Features') current.martialStances = tieredFeatures(rules);
  }
}

function tieredFeatures(rules) {
  const out = [];
  let tier = null;
  for (const block of rules.blocks) {
    const tierMatch = (block.heading ?? '').match(/^Tier (\d)$/);
    if (tierMatch) tier = Number(tierMatch[1]);
    else if (block.lines && tier !== null) {
      for (const feature of features([{ lines: block.lines }])) out.push({ tier, ...feature });
    }
  }
  return out;
}

const BEAST_STATS = /^(\w+) ([+−-]\d+) \| Evasion ([+−-]\d+)$/;
const BEAST_ATTACK = /^(Melee|Very Close|Close|Far|Very Far) (\w+) (d\d+(?:\+\d+)?) (phy|mag)$/;

function beastforms(rules) {
  const forms = [];
  let tier = null;
  let form = null;
  for (const block of rules.blocks) {
    if (block.heading) {
      const tierMatch = block.heading.match(/^Tier (\d)$/);
      if (tierMatch) tier = Number(tierMatch[1]);
      else if (tier !== null) {
        form = { name: block.heading, tier, examples: null, trait: null, traitBonus: null, evasionBonus: null, attack: null, advantages: [], features: [], notes: [] };
        forms.push(form);
      }
      continue;
    }
    if (!form) continue;
    for (const part of splitBeastBlock(block.lines)) applyBeastPart(form, part);
  }
  return forms.map(finishBeastform);
}

function splitBeastBlock(lines) {
  const parts = [];
  for (const line of lines) {
    if (BEAST_STATS.test(line) || BEAST_ATTACK.test(line) || /^\(.*\)$/.test(line) || /^Gain advantage on:/.test(line) || featureStart(line)) {
      parts.push([line]);
    } else if (parts.length > 0) {
      parts.at(-1).push(line);
    } else {
      parts.push([line]);
    }
  }
  return parts;
}

function applyBeastPart(form, lines) {
  const text = prose(lines);
  const stats = text.match(BEAST_STATS);
  const attack = text.match(BEAST_ATTACK);
  if (/^\(.*\)$/.test(text)) form.examples = text.slice(1, -1).replace(/,? etc\.$/, '').split(/,\s*/);
  else if (stats) {
    form.trait = stats[1];
    form.traitBonus = signed(stats[2]);
    form.evasionBonus = signed(stats[3]);
  } else if (attack) form.attack = { range: attack[1], trait: attack[2], damage: attack[3], damageType: attack[4] === 'phy' ? 'physical' : 'magic' };
  else if (/^Gain advantage on:/.test(text)) form.advantages = text.replace(/^Gain advantage on:\s*/, '').split(/,\s*/);
  else {
    const start = featureStart(lines[0]);
    if (start) form.features.push({ name: start.name, text: prose([start.rest, ...lines.slice(1)]) });
    else form.notes.push(text);
  }
}

function finishBeastform(form) {
  if (form.notes.length === 0) delete form.notes;
  return form;
}

export function signed(text) {
  return Number(text.replace('−', '-'));
}

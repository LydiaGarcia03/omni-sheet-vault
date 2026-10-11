# VtM 5th edition — character rules reference

Compiled 2026-10-03 from the owner's books, for `systems/vtm-v5/features/implementation-plan.md`.
Mechanics and option lists only, summarized in our own words; read the book
for full text. Page numbers are the core book's printed pages.

**Status:** core rulebook read (clans, rules, characters, vampires,
disciplines, equipment, loresheets); Camarilla and Anarch sourcebooks scanned
for player options (§11, §12). **Not read:** core Advanced Systems
(pp. 289–316: scenes, extended tests, advanced combat, hunting, prestation),
cities, chronicles, antagonists — Storyteller material, not sheet options.

Sources are tagged **[Core]**, **[Cam]** (Camarilla) and **[Ana]** (Anarch).

## 1. Dice

- Pool = Attribute + Skill (or + Discipline); d10s; 6+ = success; Difficulty =
  successes needed. Pool never below 1 die.
- Pair of 10s = critical: the pair counts as 4 successes (each extra pair +4).
- Hunger dice replace regular dice one-for-one, up to Hunger (0–5).
  - Critical with a 10 on a Hunger die = **messy critical**.
  - Failed roll with a 1 on a Hunger die = **bestial failure** (often a
    Compulsion).
- Willpower: spend 1 (= 1 Superficial Willpower damage) to re-roll up to 3
  regular dice. Never Hunger dice, never checks or tracker rolls.
- **Rouse Check:** 1 die, 6+ = no change, else Hunger +1. Can't rouse
  voluntarily at Hunger 5; a forced failure there = hunger frenzy test (Diff 4).
- Margin = successes − Difficulty (damage, power effects). Win at a cost when
  short but with successes. Automatic win allowed if pool ≥ 2× Difficulty.
- Specialty: +1 die (one per roll).
- Remorse, Willpower, Humanity rolls: no Hunger dice.

## 2. Trackers and damage

- Health = Stamina + 3; Willpower = Composure + Resolve; Humanity 0–10
  (starts 7); Hunger 0–5; Blood Potency 0–10.
- Damage: Superficial `/` (halved, round up, for vampires from normal
  weapons), Aggravated `X` (fire, sun, fangs, supernatural claws).
- Full tracker = Impaired (−2 dice: Physical for Health, Social/Mental for
  Willpower); further damage turns `/` into `X`. Health full of `X` = torpor.
- Weapon bonus added to margin: improvised/stake +0, light impact +1, heavy
  impact +2, light piercing +2, light gunshot +2, heavy melee +3, medium
  gunshot +3, heavy gunshot +4, huge melee +4. Armor 2/2/4/6 (converts
  piercing Aggravated to Superficial, mostly for mortals).
- Recovery: vampires mend Superficial Health per Rouse Check (amount by BP);
  1 Aggravated per night for 3 extra Rouse Checks. Willpower Superficial:
  start of session, up to max(Composure, Resolve); +1 when acting on Desire;
  Aggravated: 1 per session worked toward Ambition.
- Humanity stains: marked right-to-left; end of session with stains → Remorse
  roll (dice = empty unstained boxes, min 1): any success clears stains, none
  = −1 Humanity. Stains overflowing the empty boxes → Impaired + Aggravated
  Willpower damage per overflow.
- Frenzy resist: Willpower + ⌊Humanity/3⌋ vs Difficulty (fury, hunger,
  terror tables p. 220).

## 3. Blood Potency (p. 216; first printing values)

| BP | Blood Surge | Mend / Rouse | Power bonus | Rouse re-roll | Bane Severity |
| --- | --- | --- | --- | --- | --- |
| 0 | — | 1 | — | — | 0 |
| 1 | +1 | 1 | — | level 1 | 1 |
| 2 | +1 | 2 | +1 | level 1 | 1 |
| 3 | +2 | 2 | +1 | ≤ 2 | 2 |
| 4 | +2 | 3 | +2 | ≤ 2 | 2 |
| 5 | +3 | 3 | +2 | ≤ 3 | 3 |

Feeding penalties: BP 2 animal/bagged blood slakes half; BP 3+ none; BP 4+
slake 1 less per human; BP 5 can't go below Hunger 2 without killing.
Generation sets BP range (12th–13th: 1–3; 10th–11th: 1–4; thin-blood 0).
**Decided 2026-10-03 (owner):** follow the book in hand, i.e. this table as
printed. The free basic-mechanics sheet is only a summary and never overrides
the book. Later printings changed some of these values (e.g. Blood Surge and
Bane Severity at BP 1). If the live comparison shows Demiplane using different
numbers, record it as a known difference and keep the book's.

## 4. Character creation (pp. 136–153)

1. Concept, clan, sire.
2. Attributes: one at 4, three at 3, four at 2, one at 1. The p. 144 prose
   says "four at 3, three at 2"; **decided 2026-10-03 (owner): follow
   Demiplane**, which uses the summary sheet's spread (seen live: one 4-dot
   slot, three 3-dot slots, one 1-dot slot, the rest stay at 2).
3. Skills, one distribution:
   - Jack of all trades: 1×3, 8×2, 10×1
   - Balanced: 3×3, 5×2, 7×1
   - Specialist: 1×4, 3×3, 3×2, 3×1
   - Free specialty on Academics, Craft, Performance, Science when taken; +1
     free specialty anywhere; +1 from Predator type. Max specialties = dots
     (except Craft).
   - Alternative: profession / life event / leisure packages (p. 145).
4. Disciplines: two clan disciplines, 2 dots + 1 dot; one power per dot
   (power level ≤ dots; amalgams need the other discipline). Caitiff: any two.
   Thin-blood: none.
5. Predator type (below): specialty, +1 discipline dot, its merits/flaws. A
   missing skill for the specialty grants 1 dot instead.
6. Advantages: 7 points of merits/backgrounds; at least 2 points of flaws
   (plus predator ones). Thin-blood: 1–3 thin-blood merits + equal flaws.
   Loresheets optional, each dot bought separately.
7. 1–3 Convictions, one Touchstone each; Humanity 7 (8 for fledgling games).
8. Ambition (sheet), Desire (per session, not stored).
9. Coterie: 1 dot per player (+ donated advantage dots) for Domain and shared
   backgrounds.
10. Sea of Time:
    - Childer: 12th–13th BP 1 (or thin-blood BP 0), no XP.
    - Neonate: 12th–13th, BP 1, +15 XP.
    - Ancilla: 10th–11th, BP 2, +2 advantages, +2 flaws, −1 Humanity, +35 XP.

**XP costs:** attribute new×5 · skill new×3 · specialty 3 · clan discipline
new×5 · other new×7 · Caitiff new×6 · ritual level×3 · formula level×3 ·
advantage 3/dot · BP new×10 · (Humanity new×10, Storyteller's call). One dot
at a time. Award: 1/session + 1/story.

## 5. Attributes and skills

Physical: Strength, Dexterity, Stamina · Social: Charisma, Manipulation,
Composure · Mental: Intelligence, Wits, Resolve.

| Physical | Social | Mental |
| --- | --- | --- |
| Athletics, Brawl, Craft, Drive, Firearms, Larceny, Melee, Stealth, Survival | Animal Ken, Etiquette, Insight, Intimidation, Leadership, Performance, Persuasion, Streetwise, Subterfuge | Academics, Awareness, Finance, Investigation, Medicine, Occult, Politics, Science, Technology |

Each skill has sample specialties (pp. 159–171) and a "what each dot means"
line, as Demiplane's sidebar shows.

## 6. Clans (core book)

| Clan | Disciplines | Bane (× Bane Severity) | Compulsion |
| --- | --- | --- | --- |
| Brujah | Celerity, Potence, Presence | −dice to resist fury frenzy (min 1) | Rebellion |
| Gangrel | Animalism, Fortitude, Protean | Animal features in frenzy, each −1 to an attribute | Feral Impulses (−3 Manipulation/Intelligence, one-word speech) |
| Malkavian | Auspex, Dominate, Obfuscate | On bestial failure/compulsion: −dice to one pool category for the scene | Delusion |
| Nosferatu | Animalism, Obfuscate, Potence | Repulsive (−2) flaw, no Looks; −dice to pass as human | Cryptophilia |
| Toreador | Auspex, Celerity, Presence | −dice to Discipline pools in ugly surroundings | Obsession |
| Tremere | Auspex, Dominate, Blood Sorcery | Can't Blood Bond Kindred; mortals need extra drinks | Perfectionism |
| Ventrue | Dominate, Fortitude, Presence | Feeding preference; others cost Willpower | Arrogance |
| Caitiff | any 3 (not in-clan; ×6 XP) | none; start with Suspect flaw, no Status at creation | — |
| Thin-blooded | Thin-Blood Alchemy | none; BP 0, mortal-like damage, temporary disciplines from Resonance | — |

Generic compulsions (roll d10): 1–3 Hunger, 4–5 Dominance, 6–7 Harm, 8–9
Paranoia, 0 clan. Each is a −2 dice penalty to unrelated actions until met.

## 7. Predator types (core, pp. 175–178)

| Type | Specialty (one of) | Discipline (one of) | Other |
| --- | --- | --- | --- |
| Alleycat | Intimidation (Stickups) / Brawl (Grappling) | Celerity / Potence | −1 Humanity; Contacts ••• (criminal) |
| Bagger | Larceny (Lockpicking) / Streetwise (Black Market) | Blood Sorcery (Tremere) / Obfuscate | Iron Gullet •••; Enemy ••; not Ventrue |
| Blood Leech | Brawl (Kindred) / Stealth (vs Kindred) | Celerity / Protean | −1 Humanity; +1 BP; Diablerist •• or Shunned ••; Prey Exclusion (mortals) •• |
| Cleaver | Persuasion (Gaslighting) / Subterfuge (Coverups) | Dominate / Animalism | Dark Secret • (Cleaver); Herd •• |
| Consensualist | Medicine (Phlebotomy) / Persuasion (Victims) | Auspex / Fortitude | +1 Humanity; Masquerade Breacher •; Prey Exclusion (non-consenting) • |
| Farmer | Animal Ken (animal) / Survival (Hunting) | Animalism / Protean | +1 Humanity; Vegan ••; not Ventrue; BP < 3 |
| Osiris | Occult (tradition) / Performance (field) | Blood Sorcery (Tremere) / Presence | 3 dots Fame/Herd; 2 dots Enemies/Mythic flaws |
| Sandman | Medicine (Anesthetics) / Stealth (Break-in) | Auspex / Obfuscate | Resources • |
| Scene Queen | Etiquette / Leadership / Streetwise (scene) | Dominate / Potence | Fame •; Contact •; Disliked • or Prey Exclusion • |
| Siren | Persuasion / Subterfuge (Seduction) | Fortitude / Presence | Looks •• Beautiful; Enemy • |

## 8. Advantages (pp. 179–194)

- **Merits/flaws:** Linguistics (•/language; Illiterate ••), Looks (Beautiful
  ••, Stunning ••••; Ugly •, Repulsive ••), Substance Use (High-Functioning
  •; Addiction •, Hopeless ••), Archaic (Living in the Past •, Archaic ••),
  Bonding (Bond Resistance •–•••, Short Bond ••, Unbondable •••••; Bond
  Junkie •, Long Bond •, Bondslave ••), Feeding (Bloodhound •, Iron Gullet
  •••; Prey Exclusion •, Methuselah's Thirst •, Vegan ••, Organovore ••),
  Mythic (Eat Food ••; Folkloric Bane •, Folkloric Block •, Stigmata •, Stake
  Bait ••).
- **Thin-blood (0-cost, paired):** merits Anarch Comrades, Camarilla Contact,
  Catenating Blood, Day Drinker, Discipline Affinity, Lifelike, Thin-blood
  Alchemist, Vampiric Resilience; flaws Baby Teeth, Bestial Temper, Branded,
  Clan Curse, Dead Flesh, Mortal Frailty, Shunned by the Anarchs, Vitae
  Dependency.
- **Backgrounds** (can repeat): Allies (effectiveness + reliability, max 6;
  Enemy flaw), Contacts •–•••, Fame •–••••• (Infamy / Dark Secret flaws),
  Haven •–••• plus haven merits (Armory, Cell, Watchmen, Laboratory, Library,
  Location, Luxury, Postern, Security, Surgery, Warding) and flaws (No Haven,
  Compromised, Creepy, Haunted), Herd •–••••• (Obvious Predator ••), Influence
  •–••••• (Disliked •, Despised ••), Mask •–•• (+ Zeroed, Cobbler; Known
  Corpse •, Known Blankbody ••), Mawla •–••••• (Adversary flaw), Resources
  •–••••• (Destitute •), Retainers •–••• (Stalkers •), Status •–•••••
  (Suspect •, Shunned ••), Loresheet.
- **Coterie:** Domain = Chasse (hunting Difficulty 6 − extra dots), Lien
  (+dice inside), Portillon (−dice to intruders); shared backgrounds; 13
  coterie types with fixed packages (Blood Cult, Cerberus, Champions,
  Commando, Day Watch, Fang Gang, Hunting Party, Maréchal, Nomads, Plumaire,
  Questari, Regency, Sbirri, Vehme, Watchmen).

## 9. Disciplines (core) — power, level, cost, pool

Rules: one power per dot, from that level or lower; equal dots and powers.
Bonus dice = ⌊BP/2⌋ to use or resist. One activation per turn. Learning
out-of-clan needs a taste of a user's Blood; XP needs matching Resonance.

| Discipline (type, Resonance) | Level 1 | Level 2 | Level 3 | Level 4 | Level 5 |
| --- | --- | --- | --- | --- | --- |
| Animalism (mental, animal) | Bond Famulus; Sense the Beast (free, Res+Anim) | Feral Whispers (1 rouse/type) | Animal Succulence (free); Quell the Beast (1); Unliving Hive (Obf 2) | Subsume the Spirit (1) | Animal Dominion (2); Drawing Out the Beast (1) |
| Auspex (mental, phlegmatic) | Heightened Senses (free, Wits+Res); Sense the Unseen (free) | Premonition (free/1, Res+Ausp) | Scry the Soul (1, Int+Ausp); Share the Senses (1, Res+Ausp) | Spirit's Touch (1, Int+Ausp) | Clairvoyance (1); Possession (2, Dom 3); Telepathy (1) |
| Celerity (physical, choleric) | Cat's Grace (free); Rapid Reflexes (free) | Fleetness (1) | Blink (1); Traversal (1) | Draught of Elegance (1); Unerring Aim (1, Ausp 2) | Lightning Strike (1); Split Second (1) |
| Dominate (mental, phlegmatic) | Cloud Memory (free, Cha+Dom); Compel (free, Cha+Dom) | Mesmerize (1, Man+Dom); Dementation (1, Obf 2) | The Forgetful Mind (1); Submerged Directive (—) | Rationalize (—) | Mass Manipulation (+1); Terminal Decree (—) |
| Fortitude (physical, melancholic) | Resilience (+Fort to Health); Unswayable Mind | Toughness (1); Enduring Beasts (Anim 1) | Defy Bane (1); Fortify the Inner Façade | Draught of Endurance (1) | Flesh of Marble (2); Prowess from Pain (1) |
| Obfuscate (mental, melancholic) | Cloak of Shadows; Silence of Death | Unseen Passage (1) | Ghost in the Machine; Mask of a Thousand Faces (1) | Conceal (1, Ausp 3); Vanish | Cloak the Gathering (+1); Impostor's Guise (1) |
| Potence (physical, choleric) | Lethal Body; Soaring Leap | Prowess (1) | Brutal Feed; Spark of Rage (1, Pres 3); Uncanny Grip (1) | Draught of Might (1) | Earthshock (2); Fist of Caine (1) |
| Presence (mental, sanguine) | Awe; Daunt | Lingering Kiss | Dread Gaze (1); Entrancement (1) | Irresistible Voice (Dom 1); Summon (1) | Majesty (2); Star Magnetism (+1) |
| Protean (physical, animal) | Eyes of the Beast; Weight of the Feather | Feral Weapons (1) | Earth Meld (1); Shapechange (1) | Metamorphosis (1) | Mist Form (1–3); The Unfettered Heart |
| Blood Sorcery (sorcery, sanguine) | Corrosive Vitae (1+); A Taste for Blood | Extinguish Vitae (1) | Blood of Potency (1); Scorpion's Touch (1+) | Theft of Vitae (1) | Baal's Caress (1+); Cauldron of Blood (1 + Stain) |

- **Rituals** (Rouse, 5 min/level, Int + Blood Sorcery vs level + 1; one free
  level-1 ritual at creation): L1 Blood Walk, Clinging of the Insect, Craft
  Bloodstone, Wake with Evening's Freshness, Ward against Ghouls · L2
  Communicate with Kindred Sire, Eyes of Babel, Illuminate the Trail of Prey,
  Truth of Blood, Ward against Spirits, Warding Circle against Ghouls · L3
  Dagon's Call, Deflection of Wooden Doom, Essence of Air, Firewalker, Ward
  against Lupines, Warding Circle against Spirits · L4 Defense of the Sacred
  Haven, Eyes of the Nighthawk, Incorporeal Passage, Ward against Cainites,
  Warding Circle against Lupines · L5 Escape to True Sanctuary, Heart of
  Stone, Shaft of Belated Dissolution, Warding Circle against Cainites.
- **Thin-Blood Alchemy:** method chosen at dot 1 (Athanor Corporis Stamina +
  Alchemy, Calcinatio Manipulation + Alchemy, Fixatio Intelligence +
  Alchemy); distill 1 Rouse; 2/4/6 successes = weak/normal/strong. Formulae: L1
  Far Reach, Haze, Profane Hieros Gamos · L2 Envelop (+ counterfeit level-1
  powers) · L3 Defractionate (+ level 2) · L4 Airborne Momentum (+ level 3,
  needs donor vitae) · L5 Awaken the Sleeper (+ level 4). One formula free per
  dot.

## 10. Blood, Resonance and the rest

- Resonance: Choleric → Celerity, Potence; Melancholy → Fortitude,
  Obfuscate; Phlegmatic → Auspex, Dominate; Sanguine → Blood Sorcery,
  Presence; animal → Animalism, Protean. Intense/acute: +1 die to those
  disciplines until next feeding or Hunger 5. Acute carries a Dyscrasia
  (sample list p. 230).
- Slaking: sip 1, safe drink 2, harmful 1–4, kill 5 (only way to Hunger 0).
- Gifts (each a Rouse Check): Blush of Life, Blood Surge (+BP dice to one
  attribute roll), mending.
- Humanity effects by rating (food, sex, day waking, social penalties −1 at
  5 to −8 at 1, frenzy bonus, torpor length) on pp. 236–241.
- Blood Bond (3 drinks, strength up to 6), ghouls, diablerie: pp. 233–235.

## 11. Clans and powers from the sect books

The sect books are mostly setting text. Their player options are two clans,
three new powers or rituals, and loresheets (§12). They add no predator
types, merits, flaws, backgrounds or coterie types.

| Clan | Disciplines | Bane (× Bane Severity) | Compulsion |
| --- | --- | --- | --- |
| Banu Haqim [Cam] | Blood Sorcery, Celerity, Obfuscate | Slaking ≥ 1 Hunger from vampire Blood → hunger frenzy test at Difficulty 2 + Bane Severity | Judgment: must slake from anyone who acts against one of their Convictions this scene, or −3 dice to everything |
| The Ministry [Ana] | Obfuscate, Presence, Protean | −dice to all pools under bright light aimed at them; + Bane Severity to sunlight damage | Transgression: −2 dice except on pushing someone (themselves included) to break a Tenet or Conviction (≥ 1 Stain ends it) |

New powers and rituals:
- **Eyes of the Serpent** [Ana] — Presence 1, amalgam Protean 1, free.
  Holds a mortal frozen by eye contact. Against a vampire: Charisma +
  Presence vs Wits + Composure; the vampire can break free with 1 Willpower
  from the second turn.
- **Ishtar's Touch** [Cam] — ritual 2. Narcotic vitae; the victim takes a
  penalty equal to the margin on Composure and Resolve resistance pools for
  the scene.
- **One with the Blade** [Cam] — ritual 3. Dedicates one melee weapon:
  +2 dice for a scene when anointed (1 Rouse Check). Used against its owner,
  it deals Aggravated damage.

Other sheet-relevant notes:
- The Camarilla no longer admits Caitiff, and the Gangrel and Brujah left it
  (setting, not rules).
- Lasombra, Tzimisce, Hecata, Ravnos, Salubri and Tremere/Banu Haqim
  alternatives are named in the setting, but **none is playable** in these
  three books.

## 12. Loresheets

Rules [Core p. 190]: each dot is its own purchase (3 XP per dot, or advantage
points at creation) and doesn't grant the lower dots. Some are restricted to
one clan. The Storyteller decides which are available. Listed from • to
•••••.

| Loresheet | Book | Restriction | • | •• | ••• | •••• | ••••• |
| --- | --- | --- | --- | --- | --- | --- | --- |
| The Bahari | Core | — | Dangerous Reputation | Ritual Scarification | Sacrifice the Children | The Womb's Blood | First-Cursed |
| Theo Bell | Core | — | Rebel Cell | True Anarch | Contact Information | Bell's Circle | Sect Neutrality |
| Cainite Heresy | Core | — | Let He Who Hath Understanding | Hand of the Heresy | Counter-Inquisition | Red Celebrant | The One Named in Prophecy |
| Carna | Core | — | Embrace the Vision | The Rebel Trail | Unorthodox Rituals | Reimagined Bond | Book of the Grave-War |
| The Circulatory System | Core | — | Tap into the System | Little Black Book | Farm Upstate | Secure Transit | Blood Sommelier |
| Convention of Thorns | Core | — | Thorns Historian | Tradition Master | Convention Secrets | Prospective Justicar | New Traditions |
| The First Inquisition | Core | — | Mistakes of the Past | Names of the Guilty | The Sect of St. James | The Second Act | Black Spot |
| Golconda | Core | — | Seeds of Golconda | The One True Way | Saulot's Disciple | Satisfy the Hunger | Greet the Sun |
| Descendant of Hardestadt | Core | Ventrue | Voice of Hardestadt | Supreme Leader | Ventrue Pillar | Line to the Founders | Hardestadt's Heir |
| Descendant of Helena | Core | Toreador | Skin-Deep | Real Talent | Embrace the Stereotype | Divine Purity | Succubus Club Franchise |
| Sect War Veteran | Core | — | Survivor | Active Participant | Trophy Kill | No Vampire's Land | Sect Agitator |
| The Trinity | Core | — | Constantinople | Antonius' Architecture | The Dream | The Dracon | The New Trinity |
| Jeanette/Therese Voerman | Core | — | Asylum Membership | Performing Monkey | Jeanette's Favorite | Therese's Favorite | Asylum Operator |
| The Week of Nightmares | Core | — | Oral History | Ravnos Remains | I Was There | The Red Star | Blood of Zapathasura |
| Rudi | Core | — | Newfound Rights | Them and Theirs | Gangrel Advocate | The Bear Pack | Rudi's Army |
| Descendant of Tyler | Core, Ana | Brujah | Instigator | Champion of the Cause | Tyler's Mercy | The Furores | Permanent Revolution |
| Descendant of Zelios | Core | Nosferatu | Sanctuary | Saboteur | On Commission | The Labyrinth | Sense the Ley Lines |
| Descendant of Vasantasena | Core | Malkavian | Agent of Chaos | Hear My Words | Scent the Bond | Destroy the Bond | Sabbat Becomes Camarilla |
| High Clan | Core | — | Peacock | Sway the Low | Elevate the Low | Embraced to Rule | Blessed, not Cursed |
| Low Clan | Core | — | Thick Hide | Cursed with Pride | Uncanny Kinship | Trade Among Equals | Criticality Incident |
| Ambrus Maropis | Core | — | True Believer | Clandestine Information | Taught by the Best | Back Door Panopticon | On Another Grid Entirely |
| Carmelita Neillson | Core, Cam | — | The Art of Story | The Art of Will | Neillson Library | Interview With the Methuselah | Ancestor's Tomb |
| Fiorenza Savona | Core, Cam | — | On Fiorenza's List | Breakfast with Fiorenza | Friendly Benefits | The Directorate | Government Motion |
| Descendant of Karl Schrekt | Core | Tremere | Remember the House | Hardliner | Ritual Preparedness | Archon's Bane | Know the World |
| Descendant of Xaviar | Core, Ana | Gangrel | Martyred Ancestor | Where the Bodies Are Buried | Loyal Hound | Monstrous Bat | Experienced the Antediluvian |
| Fatima al-Faqadi | Cam | — | Weapons Locker | Extended Web | Missed Hit | Recognized Judge | Open Contract |
| Pure Ventrue Lineage | Cam | Ventrue | Sire of Renown | A Lineage of Title | Recitation | Legendary Lineage (pick a line) | Name the Antediluvian |
| The Cult of Mithras | Cam | — | Neophyte | Nymphus | Leo | Perses | Unconquered |
| The Pyramid | Cam | Tremere (implied) | Apprentice | Savant | Regent | Pontifex | The New Council |
| Victoria Ash | Cam | — | Ashen Kiss | Vermilion Invitation | What Makes Them Tick | Celebrity Affectations | Patron, Lover, Companion |
| Salvador Garcia | Ana | — | A Way With Words | Old School | Enemy of the State | The War of Ideas | Rise Up |
| Agata Starek | Ana | — | Terrorizing the Powerful | Apprentice | A Favor Owed | Unlikely Allies | The Joy of Transgression |
| Hesha Ruhadze | Ana | dots 4–5 Ministry | One of Hesha's Works | Something Hesha Wants | Museum of the Faithful | Blood Cult | Hear the Blood Sing |
| The Church of Set | Ana | — | Congregation | Tap the Secret Vein | Freedom from the Aeons | Degenerative Process | Body of Set |
| Ruins of Carthage | Ana | — | Clan Historian | Punic Pride | Death to Tyrants | Moloch's Will or Troile's Wish ¹ | Carthage Anew |
| Blood Plagued | Ana | — | Detection | The Science of the Blood | Trace the Torpid Victims | Curative Vitae | Vector |
| Anarch Revolt | Ana | — | Critical Ancestor | Speak the Words | Enemy of the Establishment | Icon | Reignite the Revolt |

¹ Ruins of Carthage has six entries for five dots. Read as one level with
two alternatives (Moloch's Will or Troile's Wish at ••••, the opposite
creeds of Carthage), like Pure Ventrue Lineage's "choose one". This is
inferred: the dot markers are images that the text extraction drops. Confirm
on Anarch p. 196.

Loresheet effects follow a few shapes, which matter for modeling:
- once-per-story or once-per-chronicle bonuses (dice, re-rolls, a
  Storyteller clue);
- equivalent backgrounds (e.g. "a three-dot Mawla", "four dots among Haven,
  Herd, Resources…");
- cheaper XP for one skill;
- narrative permissions.

Most are narrative. Model them as an advantage with a note, not as rules
code, except the background equivalents and the XP discount.

## 13. What this means for the sheet

- Sheet data: attributes, skills (+ specialties), disciplines (dots +
  chosen powers, rituals, formulae), Health/Willpower tracks with `/` and `X`,
  Humanity with stains, Hunger, BP (+ derived surge/mend/bonus/bane),
  generation, clan, predator type, sect, convictions↔touchstones, ambition,
  desire, merits/flaws/backgrounds with dots, loresheet dots, XP total/spent.
- Server rules needed: pool roll with Hunger dice and outcome (successes,
  critical, messy, bestial), Willpower re-roll (regular dice only, +1 `/`),
  Rouse Check (with BP re-roll by power level), damage halving and
  impairment, Remorse, XP cost table.

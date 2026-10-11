package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * VtM 5e's play-state changes on a stored sheet: trackers, notes and identity, and experience
 * (awards, spends with the core cost table, undoing the latest spend on a trait).
 */
@Component
class Vtm5SheetActions {

    private static final Set<String> EDITABLE_IDENTITY = Set.of("concept", "chronicle", "sire", "ambition", "desire");
    private static final int MAX_DOTS = 5;

    private final Vtm5Content content;

    Vtm5SheetActions(Vtm5Content content) {
        this.content = content;
    }

    /** Applies one action to {@code sheet} in place. */
    void apply(ObjectNode sheet, JsonNode action) {
        switch (action.path("type").asString("")) {
            case "damage" -> damage(sheet, action);
            case "hunger" -> sheet.put("hunger", Math.clamp(action.path("value").asInt(), 0, 5));
            case "humanity" -> {
                sheet.put("humanity", Math.clamp(action.path("value").asInt(), 0, 10));
                sheet.put("stains", Math.min(sheet.path("stains").asInt(), 10 - sheet.path("humanity").asInt()));
            }
            case "stains" -> sheet.put("stains", Math.clamp(action.path("value").asInt(), 0, 10 - sheet.path("humanity").asInt()));
            case "resonance" -> sheet.put("resonance", action.path("value").asString(""));
            case "notes" -> sheet.put("notes", action.path("value").asString(""));
            case "identity" -> identity(sheet, action);
            case "conviction" -> conviction(sheet, action);
            case "awardXp" -> awardXp(sheet, action);
            case "spendXp" -> spendXp(sheet, action);
            case "undoXp" -> undoXp(sheet, action.path("index").asInt(-1));
            case "learnPower" -> learnPower(sheet, action);
            case "coterie" -> coterie(sheet, action);
            case "addItem" -> {
                String name = action.path("name").asString("").strip();
                if (name.isEmpty()) throw new InvalidBuildException("An item needs a name.");
                inventory(sheet).addObject().put("name", name).put("quantity", Math.max(1, action.path("quantity").asInt(1)))
                        .put("notes", action.path("notes").asString(""));
            }
            case "updateItem" -> {
                ObjectNode item = item(sheet, action.path("index").asInt(-1));
                if (action.has("name") && !action.path("name").asString("").isBlank()) item.put("name", action.path("name").asString().strip());
                if (action.has("quantity")) item.put("quantity", Math.max(1, action.path("quantity").asInt(1)));
                if (action.has("notes")) item.put("notes", action.path("notes").asString(""));
            }
            case "removeItem" -> {
                item(sheet, action.path("index").asInt(-1));
                inventory(sheet).remove(action.path("index").asInt());
            }
            default -> throw new InvalidBuildException("Unknown VtM action: " + action.path("type").asString(""));
        }
    }

    // Trackers

    private static void damage(ObjectNode sheet, JsonNode action) {
        String track = action.path("track").asString("");
        if (!track.equals("health") && !track.equals("willpower")) {
            throw new InvalidBuildException("Damage goes to health or willpower.");
        }
        int max = track.equals("health")
                ? sheet.path("attributes").path("Stamina").asInt() + 3
                : sheet.path("attributes").path("Composure").asInt() + sheet.path("attributes").path("Resolve").asInt();
        ObjectNode boxes = (ObjectNode) sheet.path(track);
        int superficial = boxes.path("superficial").asInt();
        int aggravated = boxes.path("aggravated").asInt();
        boolean isAggravated = "aggravated".equals(action.path("kind").asString(""));
        if (action.path("amount").asInt(1) < 0) {
            if (isAggravated) aggravated = Math.max(0, aggravated - 1); else superficial = Math.max(0, superficial - 1);
        } else if (superficial + aggravated < max) {
            if (isAggravated) aggravated++; else superficial++;
        } else if (superficial > 0) {
            superficial--;
            aggravated++;
        }
        boxes.put("superficial", superficial);
        boxes.put("aggravated", aggravated);
    }

    private static final Set<String> COTERIE_TEXT = Set.of("name", "type", "city", "tenets", "goals", "boons", "domainDescription", "domainResonance");
    private static final List<String> DOMAIN_TRAITS = List.of("Chasse", "Lien", "Portillon");

    /** Edits the character's coterie; the domain and coterie backgrounds can't spend more than the coterie's pool. */
    private void coterie(ObjectNode sheet, JsonNode action) {
        ObjectNode coterie = sheet.has("coterie") ? (ObjectNode) sheet.get("coterie") : sheet.putObject("coterie");
        COTERIE_TEXT.stream().filter(action::has).forEach(field -> coterie.put(field, action.path(field).asString("")));
        if (action.has("members")) {
            ArrayNode members = coterie.putArray("members");
            action.path("members").forEach(member -> {
                if (!member.asString("").isBlank()) members.add(member.asString().strip());
            });
        }
        if (action.has("othersDonated")) {
            coterie.put("othersDonated", Math.max(0, action.path("othersDonated").asInt()));
        }
        if (action.has("domain")) {
            ObjectNode domain = coterie.has("domain") ? (ObjectNode) coterie.get("domain") : coterie.putObject("domain");
            DOMAIN_TRAITS.stream().filter(action.path("domain")::has)
                    .forEach(trait -> domain.put(trait, Math.clamp(action.path("domain").path(trait).asInt(), 0, MAX_DOTS)));
        }
        if (action.has("backgrounds")) {
            ArrayNode backgrounds = coterie.putArray("backgrounds");
            for (JsonNode background : action.path("backgrounds")) {
                String name = background.path("name").asString("");
                boolean isBackground = content.advantage(name).map(a -> "Background".equals(a.path("kind").asString())).orElse(false);
                if (!isBackground) throw new InvalidBuildException(name + " is not a background.");
                int dots = Math.clamp(background.path("dots").asInt(), 0, MAX_DOTS);
                if (dots > 0) backgrounds.addObject().put("name", name).put("dots", dots);
            }
        }
        int pool = coteriePool(coterie);
        int spent = coterieSpent(coterie);
        if (spent > pool) {
            throw new InvalidBuildException("The coterie spends " + spent + " dots; its pool is " + pool + ".");
        }
    }

    /** One dot per member (this character included), its creation contribution and what the others donated. */
    static int coteriePool(JsonNode coterie) {
        return 1 + coterie.path("members").size() + coterie.path("contribution").asInt() + coterie.path("othersDonated").asInt();
    }

    static int coterieSpent(JsonNode coterie) {
        int spent = 0;
        for (String trait : DOMAIN_TRAITS) spent += coterie.path("domain").path(trait).asInt();
        for (JsonNode background : coterie.path("backgrounds")) spent += background.path("dots").asInt();
        return spent;
    }

    private static ArrayNode inventory(ObjectNode sheet) {
        return sheet.has("inventory") ? (ArrayNode) sheet.get("inventory") : sheet.putArray("inventory");
    }

    private static ObjectNode item(ObjectNode sheet, int index) {
        ArrayNode inventory = inventory(sheet);
        if (index < 0 || index >= inventory.size()) throw new InvalidBuildException("No such item.");
        return (ObjectNode) inventory.get(index);
    }

    private static void identity(ObjectNode sheet, JsonNode action) {
        String field = action.path("field").asString("");
        if (!EDITABLE_IDENTITY.contains(field)) {
            throw new InvalidBuildException(field + " is set at creation.");
        }
        ((ObjectNode) sheet.path("identity")).put(field, action.path("value").asString(""));
    }

    private static void conviction(ObjectNode sheet, JsonNode action) {
        ArrayNode convictions = (ArrayNode) sheet.path("convictions");
        int index = action.path("index").asInt(-1);
        if (action.path("remove").asBoolean(false)) {
            if (index >= 0 && index < convictions.size() && convictions.size() > 1) convictions.remove(index);
            return;
        }
        if (index < 0 || index >= convictions.size()) {
            if (convictions.size() >= 3) throw new InvalidBuildException("A character has at most three convictions.");
            convictions.addObject().put("conviction", "").put("touchstone", "");
            index = convictions.size() - 1;
        }
        ObjectNode entry = (ObjectNode) convictions.get(index);
        if (action.has("conviction")) entry.put("conviction", action.path("conviction").asString(""));
        if (action.has("touchstone")) entry.put("touchstone", action.path("touchstone").asString(""));
    }

    // Experience

    private static void awardXp(ObjectNode sheet, JsonNode action) {
        int amount = action.path("amount").asInt();
        if (amount <= 0) {
            throw new InvalidBuildException("Award a positive amount of XP.");
        }
        String note = action.path("note").asString("").isBlank() ? "Award" : action.path("note").asString();
        ((ArrayNode) sheet.path("xpLog")).addObject().put("kind", "award").put("amount", amount).put("note", note).put("at", Instant.now().toString());
    }

    private void spendXp(ObjectNode sheet, JsonNode action) {
        String target = action.path("target").asString("");
        String key = action.path("key").asString("");
        int cost;
        String label;
        switch (target) {
            case "attribute" -> {
                int now = requireTrait(sheet.path("attributes"), key, content.attributes());
                cost = (now + 1) * 5;
                label = key + " " + now + " → " + (now + 1);
            }
            case "skill" -> {
                int now = requireTrait(sheet.path("skills"), key, content.skills());
                cost = now == 0 ? 3 : (now + 1) * 3;
                label = key + " " + now + " → " + (now + 1);
            }
            case "discipline" -> {
                if (content.discipline(key).isEmpty()) throw new InvalidBuildException("Unknown discipline: " + key);
                if (key.equals(Vtm5CharacterCreationFlow.ALCHEMY) != isThinBlood(sheet)) {
                    throw new InvalidBuildException(isThinBlood(sheet) ? "Thin-bloods raise Thin-Blood Alchemy, not disciplines." : "Only thin-bloods practise Thin-Blood Alchemy.");
                }
                int now = sheet.path("disciplines").path(key).path("dots").asInt(0);
                if (now >= MAX_DOTS) throw new InvalidBuildException(key + " is already at " + MAX_DOTS + ".");
                cost = (now + 1) * disciplineMultiplier(sheet, key);
                label = key + " " + now + " → " + (now + 1);
            }
            case "specialty" -> {
                String specialty = action.path("specialty").asString("").strip();
                int dots = sheet.path("skills").path(key).asInt(0);
                int has = sheet.path("specialties").path(key).size();
                if (specialty.isEmpty() || dots == 0 || has >= dots) throw new InvalidBuildException("A specialty needs a name and a free skill dot.");
                cost = 3;
                label = "Specialty " + key + " (" + specialty + ")";
            }
            case "advantage" -> {
                JsonNode advantage = content.advantage(key).orElseThrow(() -> new InvalidBuildException("Unknown advantage: " + key));
                if ("Flaw".equals(advantage.path("kind").asString())) throw new InvalidBuildException("Flaws aren't bought with XP.");
                int now = advantageDots(sheet, key);
                int fixed = advantage.path("fixed").asInt(0);
                if (now >= advantage.path("max").asInt(MAX_DOTS) || (fixed > 0 && now > 0)) throw new InvalidBuildException(key + " is already at its maximum.");
                cost = fixed > 0 ? fixed * 3 : 3;
                label = key + " " + now + " → " + (fixed > 0 ? fixed : now + 1);
            }
            case "loresheet" -> {
                int level = action.path("level").asInt(0);
                cost = loresheetCost(sheet, key, level);
                if (cost == 0) throw new InvalidBuildException(key + " " + level + " can't be bought: unknown, already owned, or not for your clan.");
                label = "Loresheet " + key + " " + level;
            }
            case "ritual" -> {
                cost = ritualCost(sheet, key);
                if (cost == 0) throw new InvalidBuildException(key + " can't be learned: rituals go up to your Blood Sorcery rating, once each.");
                label = "Ritual " + key;
            }
            case "bloodPotency" -> {
                int now = sheet.path("bloodPotency").asInt();
                if (now >= MAX_DOTS) throw new InvalidBuildException("Blood Potency is at its maximum.");
                cost = (now + 1) * 10;
                label = "Blood Potency " + now + " → " + (now + 1);
            }
            default -> throw new InvalidBuildException("Unknown XP target: " + target);
        }
        if (cost > available(sheet)) {
            throw new InvalidBuildException("Not enough XP: " + label + " costs " + cost + ".");
        }
        raise(sheet, target, key, target.equals("loresheet") ? String.valueOf(action.path("level").asInt()) : action.path("specialty").asString("").strip(), 1);
        ((ArrayNode) sheet.path("xpLog")).addObject().put("kind", "spend").put("target", target).put("key", key)
                .put("specialty", target.equals("loresheet") ? String.valueOf(action.path("level").asInt()) : action.path("specialty").asString("").strip()).put("label", label).put("cost", cost)
                .put("at", Instant.now().toString());
    }

    private void undoXp(ObjectNode sheet, int index) {
        ArrayNode log = (ArrayNode) sheet.path("xpLog");
        JsonNode entry = log.path(index);
        if (!"spend".equals(entry.path("kind").asString("")) || entry.path("undone").asBoolean(false)) {
            throw new InvalidBuildException("Only an active spend can be undone.");
        }
        for (int later = index + 1; later < log.size(); later++) {
            JsonNode other = log.get(later);
            if ("spend".equals(other.path("kind").asString("")) && !other.path("undone").asBoolean(false)
                    && other.path("key").asString("").equals(entry.path("key").asString(""))) {
                throw new InvalidBuildException("Undo the later spend on " + entry.path("key").asString() + " first.");
            }
        }
        raise(sheet, entry.path("target").asString(), entry.path("key").asString(), entry.path("specialty").asString(""), -1);
        ((ObjectNode) entry).put("undone", true);
    }

    private void learnPower(ObjectNode sheet, JsonNode action) {
        String discipline = action.path("discipline").asString("");
        String power = action.path("power").asString("");
        JsonNode node = sheet.path("disciplines").path(discipline);
        if (!node.isObject()) {
            throw new InvalidBuildException("The character has no " + discipline + ".");
        }
        ObjectNode owned = (ObjectNode) node;
        int dots = owned.path("dots").asInt();
        ArrayNode powers = owned.has("powers") ? (ArrayNode) owned.get("powers") : owned.putArray("powers");
        boolean allowed = content.discipline(discipline).map(d -> {
            for (JsonNode p : d.path("powers")) {
                if (p.path("name").asString().equals(power)) return p.path("level").asInt() <= dots;
            }
            return false;
        }).orElse(false);
        if (!allowed || powers.size() >= dots) {
            throw new InvalidBuildException(power + " can't be learned now: one power per dot, at most your rating's level.");
        }
        powers.add(power);
    }

    private void raise(ObjectNode sheet, String target, String key, String specialty, int step) {
        switch (target) {
            case "attribute" -> ((ObjectNode) sheet.path("attributes")).put(key, sheet.path("attributes").path(key).asInt() + step);
            case "skill" -> ((ObjectNode) sheet.path("skills")).put(key, sheet.path("skills").path(key).asInt() + step);
            case "bloodPotency" -> sheet.put("bloodPotency", sheet.path("bloodPotency").asInt() + step);
            case "advantage" -> raiseAdvantage(sheet, key, step);
            case "loresheet" -> {
                ObjectNode loresheets = sheet.has("loresheets") ? (ObjectNode) sheet.get("loresheets") : sheet.putObject("loresheets");
                ArrayNode levels = loresheets.has(key) ? (ArrayNode) loresheets.get(key) : loresheets.putArray(key);
                int level = Integer.parseInt(specialty);
                if (step > 0) {
                    levels.add(level);
                } else {
                    for (int i = levels.size() - 1; i >= 0; i--) {
                        if (levels.get(i).asInt() == level) {
                            levels.remove(i);
                            break;
                        }
                    }
                    if (levels.isEmpty()) loresheets.remove(key);
                }
            }
            case "ritual" -> {
                ArrayNode rituals = sheet.has("rituals") ? (ArrayNode) sheet.get("rituals") : sheet.putArray("rituals");
                if (step > 0) {
                    rituals.add(key);
                } else {
                    for (int i = rituals.size() - 1; i >= 0; i--) {
                        if (rituals.get(i).asString().equals(key)) {
                            rituals.remove(i);
                            break;
                        }
                    }
                }
            }
            case "specialty" -> {
                ObjectNode specialties = (ObjectNode) sheet.path("specialties");
                ArrayNode list = specialties.has(key) ? (ArrayNode) specialties.get(key) : specialties.putArray(key);
                if (step > 0) {
                    list.add(specialty);
                } else {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        if (list.get(i).asString().equals(specialty)) {
                            list.remove(i);
                            break;
                        }
                    }
                }
            }
            case "discipline" -> {
                ObjectNode disciplines = (ObjectNode) sheet.path("disciplines");
                ObjectNode discipline = disciplines.has(key) ? (ObjectNode) disciplines.get(key) : disciplines.putObject(key);
                int dots = discipline.path("dots").asInt(0) + step;
                if (dots <= 0) {
                    disciplines.remove(key);
                    return;
                }
                discipline.put("dots", dots);
                ArrayNode powers = discipline.has("powers") ? (ArrayNode) discipline.get("powers") : discipline.putArray("powers");
                while (powers.size() > dots) powers.remove(powers.size() - 1);
            }
            default -> throw new InvalidBuildException("Unknown XP target: " + target);
        }
    }

    /** The XP each trait's next dot costs (0 when already at the maximum), for the sheet to show before spending. */
    ObjectNode nextCosts(JsonNode sheet, ObjectNode into) {
        ObjectNode attributes = into.putObject("attribute");
        content.attributes().forEach(name -> {
            int now = sheet.path("attributes").path(name).asInt();
            attributes.put(name, now >= MAX_DOTS ? 0 : (now + 1) * 5);
        });
        ObjectNode skills = into.putObject("skill");
        content.skills().forEach(name -> {
            int now = sheet.path("skills").path(name).asInt();
            skills.put(name, now >= MAX_DOTS ? 0 : now == 0 ? 3 : (now + 1) * 3);
        });
        ObjectNode disciplines = into.putObject("discipline");
        content.disciplineNames().forEach(name -> {
            int now = sheet.path("disciplines").path(name).path("dots").asInt(0);
            boolean allowed = name.equals(Vtm5CharacterCreationFlow.ALCHEMY) == isThinBlood(sheet);
            disciplines.put(name, now >= MAX_DOTS || !allowed ? 0 : (now + 1) * disciplineMultiplier(sheet, name));
        });
        int bloodPotency = sheet.path("bloodPotency").asInt();
        into.put("bloodPotency", bloodPotency >= MAX_DOTS ? 0 : (bloodPotency + 1) * 10);
        into.put("specialty", 3);
        ObjectNode loresheets = into.putObject("loresheet");
        content.all().path("loresheets").forEach(l -> {
            ArrayNode costs = loresheets.putArray(l.path("name").asString());
            for (int level = 1; level <= MAX_DOTS; level++) costs.add(loresheetCost(sheet, l.path("name").asString(), level));
        });
        ObjectNode rituals = into.putObject("ritual");
        content.all().path("rituals").forEach(r -> rituals.put(r.path("name").asString(), ritualCost(sheet, r.path("name").asString())));
        ObjectNode advantages = into.putObject("advantage");
        content.all().path("advantages").forEach(a -> {
            if ("Flaw".equals(a.path("kind").asString())) return;
            String name = a.path("name").asString();
            int now = advantageDots(sheet, name);
            int fixed = a.path("fixed").asInt(0);
            boolean maxed = now >= a.path("max").asInt(MAX_DOTS) || (fixed > 0 && now > 0);
            advantages.put(name, maxed ? 0 : fixed > 0 ? fixed * 3 : 3);
        });
        return into;
    }

    /** A loresheet entry's XP cost (level × 3), or 0 when it is unknown, owned or closed to the character's clan. */
    private int loresheetCost(JsonNode sheet, String name, int level) {
        Optional<JsonNode> loresheet = content.loresheet(name);
        if (loresheet.isEmpty() || level < 1 || level > MAX_DOTS) return 0;
        String restriction = loresheet.get().path("restriction").asString("");
        if (!restriction.isEmpty() && !restriction.equals(sheet.path("identity").path("clan").asString(""))) return 0;
        for (JsonNode owned : sheet.path("loresheets").path(name)) {
            if (owned.asInt() == level) return 0;
        }
        return level * 3;
    }

    /** A ritual's XP cost (level × 3), or 0 when it is unknown, already known or above the Blood Sorcery rating. */
    private int ritualCost(JsonNode sheet, String name) {
        int rating = sheet.path("disciplines").path(Vtm5CharacterCreationFlow.BLOOD_SORCERY).path("dots").asInt(0);
        boolean known = false;
        for (JsonNode owned : sheet.path("rituals")) known |= owned.asString().equals(name);
        if (known) return 0;
        return content.ritual(name).map(r -> r.path("level").asInt() <= rating ? r.path("level").asInt() * 3 : 0).orElse(0);
    }

    private void raiseAdvantage(ObjectNode sheet, String name, int step) {
        int fixed = content.advantage(name).map(a -> a.path("fixed").asInt(0)).orElse(0);
        int change = fixed > 0 ? fixed * step : step;
        ArrayNode advantages = sheet.has("advantages") ? (ArrayNode) sheet.get("advantages") : sheet.putArray("advantages");
        for (int i = 0; i < advantages.size(); i++) {
            ObjectNode entry = (ObjectNode) advantages.get(i);
            if (entry.path("name").asString().equals(name)) {
                int dots = entry.path("dots").asInt() + change;
                if (dots <= 0) advantages.remove(i); else entry.put("dots", dots);
                return;
            }
        }
        if (change > 0) {
            advantages.addObject().put("name", name).put("dots", change)
                    .put("kind", content.advantage(name).map(a -> a.path("kind").asString()).orElse("Merit"));
        }
    }

    private static boolean isThinBlood(JsonNode sheet) {
        return "Thin-blooded".equals(sheet.path("identity").path("clan").asString(""));
    }

    private static int advantageDots(JsonNode sheet, String name) {
        for (JsonNode entry : sheet.path("advantages")) {
            if (entry.path("name").asString().equals(name)) return entry.path("dots").asInt();
        }
        return 0;
    }

    private int disciplineMultiplier(JsonNode sheet, String discipline) {
        String clan = sheet.path("identity").path("clan").asString("");
        if (clan.equals("Caitiff")) return 6;
        return content.clan(clan).map(c -> contains(c.path("disciplines"), discipline) ? 5 : 7).orElse(7);
    }

    static int available(JsonNode sheet) {
        int total = 0;
        for (JsonNode entry : sheet.path("xpLog")) {
            if ("award".equals(entry.path("kind").asString(""))) total += entry.path("amount").asInt();
            else if (!entry.path("undone").asBoolean(false)) total -= entry.path("cost").asInt();
        }
        return total;
    }

    private static int requireTrait(JsonNode values, String key, List<String> names) {
        if (!names.contains(key)) throw new InvalidBuildException("Unknown trait: " + key);
        int now = values.path(key).asInt(0);
        if (now >= MAX_DOTS) throw new InvalidBuildException(key + " is already at " + MAX_DOTS + ".");
        return now;
    }

    private static boolean contains(JsonNode list, String value) {
        for (JsonNode entry : list) {
            if (entry.asString("").equals(value)) return true;
        }
        return false;
    }
}

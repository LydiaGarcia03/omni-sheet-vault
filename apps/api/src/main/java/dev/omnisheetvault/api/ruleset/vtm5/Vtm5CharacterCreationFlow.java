package dev.omnisheetvault.api.ruleset.vtm5;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * VtM 5e creation: one choice per builder step part, checked against the core rules
 * (attribute spread, skill distribution, two clan disciplines, predator type, advantage budgets).
 */
@Component
class Vtm5CharacterCreationFlow implements CharacterCreationFlow {

    static final Map<Integer, Integer> ATTRIBUTE_SLOTS = Map.of(4, 1, 3, 3, 2, 4, 1, 1);
    private static final int ADVANTAGE_POINTS = 7;
    private static final int FLAW_POINTS = 2;
    private static final int BASE_HUMANITY = 7;
    static final String BLOOD_SORCERY = "Blood Sorcery";
    static final String ALCHEMY = "Thin-Blood Alchemy";
    private static final String ALCHEMIST = "Thin-blood Alchemist";
    private static final int MAX_THIN_BLOOD_MERITS = 3;
    static final int MAX_COTERIE_CONTRIBUTION = 7;

    private final ObjectMapper objectMapper;
    private final Vtm5Content content;

    Vtm5CharacterCreationFlow(ObjectMapper objectMapper, Vtm5Content content) {
        this.objectMapper = objectMapper;
        this.content = content;
    }

    @Override
    public String systemId() {
        return Vtm5GameSystem.SYSTEM_ID;
    }

    @Override
    public String emptyDraft() {
        return """
                {"age":"Neonate","generation":"13th","attributes":{},"skills":{},"specialties":{},"powers":{},\
                "advantages":{},"convictions":[{"conviction":"","touchstone":""}]}""";
    }

    @Override
    public BuildPlan plan(String buildJson, CatalogueLookup catalogue) {
        JsonNode build = read(buildJson);
        List<CreationChoice> choices = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        basics(build, choices);
        clan(build, choices, problems);
        attributes(build, choices, problems);
        skills(build, choices, problems);
        disciplines(build, choices, problems);
        predator(build, choices, problems);
        advantages(build, choices, problems);
        thinBlood(build, choices, problems);
        beliefs(build, choices);
        return new BuildPlan(choices, problems);
    }

    @Override
    public CreationPreview preview(String buildJson, CatalogueLookup catalogue) {
        return new CreationPreview(Map.of(), null, List.of(), Map.of(), null);
    }

    @Override
    public MaterializedSheet materialize(String currentSheetJson, String buildJson, CatalogueLookup catalogue) {
        BuildPlan plan = plan(buildJson, catalogue);
        if (!plan.pending().isEmpty() || !plan.problems().isEmpty()) {
            return new MaterializedSheet(null, List.of(), 0, plan);
        }
        return new MaterializedSheet(objectMapper.writeValueAsString(sheet(read(buildJson))), List.of(), 0, plan);
    }

    @Override
    public Optional<String> buildOf(String sheetJson) {
        JsonNode build = read(sheetJson).path("build");
        return build.isObject() ? Optional.of(objectMapper.writeValueAsString(build)) : Optional.empty();
    }

    // Steps

    private void basics(JsonNode build, List<CreationChoice> choices) {
        choices.add(single("concept", "basics", "Concept", text(build, "concept")));
        choices.add(single("age", "basics", "Age since the Embrace", content.age(text(build, "age")).isPresent() ? text(build, "age") : ""));
    }

    private void clan(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        String clan = text(build, "clan");
        if (!clan.isEmpty() && content.clan(clan).isEmpty()) {
            problems.add("Unknown clan: " + clan);
        }
        List<CreationChoiceOption> options = new ArrayList<>();
        content.all().path("clans").forEach(c -> options.add(new CreationChoiceOption(
                c.path("name").asString(), c.path("name").asString(), c.path("source").asString(), c.path("nick").asString())));
        choices.add(new CreationChoice("clan", "clan", null, "Clan", null, 1, false, options, selectedIf(clan)));
    }

    private void attributes(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        List<String> placed = placed(build.path("attributes"), content.attributes(), 4, problems, "Attribute");
        choices.add(counted("attributes", "attributes", "Attribute dots", content.attributes().size(), placed));
        slotProblems(build.path("attributes"), content.attributes(), ATTRIBUTE_SLOTS, "attributes", problems);
    }

    private void skills(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        String distribution = text(build, "distribution");
        Map<Integer, Integer> slots = content.distribution(distribution);
        choices.add(single("distribution", "skills", "Skill distribution", slots.isEmpty() ? "" : distribution));
        if (slots.isEmpty()) {
            return;
        }
        int maxDots = slots.keySet().stream().max(Integer::compare).orElse(3);
        List<String> placed = placed(build.path("skills"), content.skills(), maxDots, problems, "Skill");
        int total = slots.values().stream().mapToInt(Integer::intValue).sum();
        choices.add(counted("skills", "skills", "Skill dots", total, placed));
        slotProblems(build.path("skills"), content.skills(), slots, "skills", problems);
        specialties(build, placed, choices);
    }

    private void specialties(JsonNode build, List<String> trainedSkills, List<CreationChoice> choices) {
        JsonNode specialties = build.path("specialties");
        List<String> required = trainedSkills.stream().filter(content.freeSpecialtySkills()::contains).toList();
        List<String> chosen = new ArrayList<>(required.stream().filter(skill -> !text(specialties, skill).isEmpty()).toList());
        trainedSkills.stream().filter(skill -> !required.contains(skill) && !text(specialties, skill).isEmpty()).findFirst().ifPresent(chosen::add);
        choices.add(counted("specialties", "skills", "Specialties", required.size() + 1, chosen));
    }

    private void disciplines(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        Optional<JsonNode> clan = content.clan(text(build, "clan"));
        if (clan.isPresent() && clan.get().path("name").asString().equals("Thin-blooded")) {
            powerChoice(build, choices, problems);
            return;
        }
        List<String> allowed = clanDisciplines(clan);
        String two = text(build, "disciplineTwo");
        String one = text(build, "disciplineOne");
        List<String> picked = new ArrayList<>();
        for (String discipline : List.of(two, one)) {
            if (discipline.isEmpty()) {
                continue;
            }
            if (!allowed.contains(discipline)) {
                problems.add(discipline + " is not one of your clan's disciplines.");
            }
            picked.add(discipline);
        }
        if (!two.isEmpty() && two.equals(one)) {
            problems.add("Pick two different disciplines.");
        }
        choices.add(counted("disciplines", "disciplines", "Clan disciplines", 2, picked));
        Map<String, Integer> dots = powerChoice(build, choices, problems);
        if (dots.containsKey(BLOOD_SORCERY)) {
            String ritual = text(build, "ritual");
            boolean levelOne = content.ritual(ritual).map(r -> r.path("level").asInt() == 1).orElse(false);
            if (!ritual.isEmpty() && !levelOne) {
                problems.add(ritual + " is not a level-1 ritual.");
            }
            choices.add(single("ritual", "disciplines", "Free ritual", levelOne ? ritual : ""));
        }
    }

    /** One power per discipline dot, of a level up to the rating; returns the dots it checked against. */
    private Map<String, Integer> powerChoice(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        Map<String, Integer> dots = disciplineDots(build);
        if (dots.isEmpty()) {
            return dots;
        }
        int needed = dots.values().stream().mapToInt(Integer::intValue).sum();
        List<String> powers = new ArrayList<>();
        dots.forEach((discipline, level) -> build.path("powers").path(discipline).forEach(power -> {
            if (isPowerAllowed(discipline, power.asString(), level)) {
                powers.add(discipline + ":" + power.asString());
            } else {
                problems.add(power.asString() + " is above your " + discipline + " rating.");
            }
        }));
        choices.add(counted("powers", "disciplines", "Powers", needed, powers.stream().limit(needed).toList()));
        return dots;
    }

    private void predator(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        String name = text(build, "predator");
        Optional<JsonNode> predator = content.predator(name);
        if (!name.isEmpty() && predator.isEmpty()) {
            problems.add("Unknown predator type: " + name);
        }
        predator.filter(p -> contains(p.path("notFor"), text(build, "clan")))
                .ifPresent(p -> problems.add(name + " is not open to " + text(build, "clan") + "."));
        choices.add(single("predator", "predator", "Predator type", predator.isPresent() ? name : ""));
        JsonNode specialty = build.path("predatorSpecialty");
        boolean specialtyValid = predator.isPresent() && specialty.isInt() && specialty.asInt() >= 0
                && specialty.asInt() < predator.get().path("specialties").size();
        choices.add(single("predator-specialty", "predator", "Predator specialty", specialtyValid ? String.valueOf(specialty.asInt()) : ""));
        String discipline = text(build, "predatorDiscipline");
        boolean disciplineValid = predator.isPresent() && contains(predator.get().path("disciplines"), discipline);
        if (disciplineValid && discipline.equals("Blood Sorcery") && !content.sorceryClans().contains(text(build, "clan"))) {
            problems.add("Blood Sorcery from a predator type needs a Tremere or Banu Haqim.");
        }
        choices.add(single("predator-discipline", "predator", "Predator discipline", disciplineValid ? discipline : ""));
    }

    private void advantages(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        int budget = ADVANTAGE_POINTS + (isAncilla(build) ? 2 : 0);
        int flawMinimum = FLAW_POINTS + (isAncilla(build) ? 2 : 0);
        List<String> merits = new ArrayList<>();
        List<String> flaws = new ArrayList<>();
        build.path("advantages").properties().forEach(entry -> {
            Optional<JsonNode> advantage = content.advantage(entry.getKey());
            if (advantage.isEmpty()) {
                problems.add("Unknown advantage: " + entry.getKey());
                return;
            }
            List<String> target = advantage.get().path("kind").asString().equals("Flaw") ? flaws : merits;
            for (int dot = 1; dot <= entry.getValue().asInt(); dot++) {
                target.add(entry.getKey() + ":" + dot);
            }
        });
        build.path("loresheets").properties().forEach(entry -> {
            Optional<JsonNode> loresheet = content.loresheet(entry.getKey());
            if (loresheet.isEmpty()) {
                problems.add("Unknown loresheet: " + entry.getKey());
                return;
            }
            String restriction = loresheet.get().path("restriction").asString("");
            if (!restriction.isEmpty() && !restriction.equals(text(build, "clan"))) {
                problems.add(entry.getKey() + " is only for the " + restriction + ".");
            }
            for (JsonNode level : entry.getValue()) {
                for (int dot = 1; dot <= level.asInt(); dot++) {
                    merits.add(entry.getKey() + ":" + level.asInt() + ":" + dot);
                }
            }
        });
        int contribution = build.path("coterieContribution").asInt(0);
        if (contribution < 0 || contribution > MAX_COTERIE_CONTRIBUTION) {
            problems.add("A coterie contribution is 0 to " + MAX_COTERIE_CONTRIBUTION + " points.");
        }
        for (int point = 1; point <= Math.clamp(contribution, 0, MAX_COTERIE_CONTRIBUTION); point++) {
            merits.add("coterie:" + point);
        }
        if (merits.size() > budget) {
            problems.add("Advantages spend " + merits.size() + " points; the budget is " + budget + ".");
        }
        choices.add(counted("advantages", "advantages", "Advantage points", budget, merits.stream().limit(budget).toList()));
        choices.add(counted("flaws", "advantages", "Flaw points", flawMinimum, flaws.stream().limit(flawMinimum).toList()));
    }

    private void thinBlood(JsonNode build, List<CreationChoice> choices, List<String> problems) {
        if (!"Thin-blooded".equals(text(build, "clan"))) {
            return;
        }
        List<String> merits = names(build.path("thinBloodMerits"));
        List<String> flaws = names(build.path("thinBloodFlaws"));
        merits.stream().filter(name -> !content.isThinBloodMerit(name)).forEach(name -> problems.add("Unknown thin-blood merit: " + name));
        flaws.stream().filter(name -> !content.isThinBloodFlaw(name)).forEach(name -> problems.add("Unknown thin-blood flaw: " + name));
        if (merits.size() > MAX_THIN_BLOOD_MERITS) {
            problems.add("At most " + MAX_THIN_BLOOD_MERITS + " thin-blood merits.");
        }
        choices.add(counted("thin-blood-merits", "advantages", "Thin-blood merits", 1, merits.stream().limit(1).toList()));
        choices.add(counted("thin-blood-flaws", "advantages", "Thin-blood flaws", Math.max(1, merits.size()), flaws.stream().limit(Math.max(1, merits.size())).toList()));
        if (flaws.size() > merits.size() && !merits.isEmpty()) {
            problems.add("Take as many thin-blood flaws as merits.");
        }
        if (merits.contains(ALCHEMIST)) {
            String method = text(build, "alchemyMethod");
            choices.add(single("alchemy-method", "advantages", "Alchemy method", content.isAlchemyMethod(method) ? method : ""));
        }
    }

    private static List<String> names(JsonNode list) {
        List<String> names = new ArrayList<>();
        list.forEach(name -> names.add(name.asString()));
        return names;
    }

    private void beliefs(JsonNode build, List<CreationChoice> choices) {
        List<String> complete = new ArrayList<>();
        build.path("convictions").forEach(c -> {
            if (!text(c, "conviction").isEmpty() && !text(c, "touchstone").isEmpty()) {
                complete.add(text(c, "conviction"));
            }
        });
        choices.add(counted("convictions", "beliefs", "Convictions with a touchstone", 1, complete.stream().limit(1).toList()));
        choices.add(single("ambition", "beliefs", "Ambition", text(build, "ambition")));
    }

    // The sheet a finished build becomes

    private ObjectNode sheet(JsonNode build) {
        JsonNode clan = content.clan(text(build, "clan")).orElseThrow();
        JsonNode predator = content.predator(text(build, "predator")).orElseThrow();
        JsonNode age = content.age(text(build, "age")).orElseThrow();
        boolean thinBlood = clan.path("name").asString().equals("Thin-blooded");

        ObjectNode sheet = objectMapper.createObjectNode();
        sheet.put("schemaVersion", 1);
        ObjectNode identity = sheet.putObject("identity");
        for (String field : List.of("concept", "chronicle", "sire", "age", "generation", "clan", "predator", "ambition", "desire")) {
            identity.put(field, text(build, field));
        }
        if (thinBlood) {
            identity.put("generation", "14th–16th");
        }
        sheet.set("attributes", build.path("attributes").deepCopy());

        ObjectNode skills = sheet.putObject("skills");
        content.skills().forEach(skill -> skills.put(skill, build.path("skills").path(skill).asInt(0)));
        ObjectNode specialties = sheet.putObject("specialties");
        build.path("specialties").properties().forEach(entry -> {
            if (!entry.getValue().asString("").isBlank() && skills.path(entry.getKey()).asInt() > 0) {
                specialties.putArray(entry.getKey()).add(entry.getValue().asString());
            }
        });
        JsonNode predatorSpecialty = predator.path("specialties").path(build.path("predatorSpecialty").asInt());
        String predatorSkill = predatorSpecialty.path("skill").asString();
        if (skills.path(predatorSkill).asInt() == 0) {
            skills.put(predatorSkill, 1);
        }
        ArrayNode predatorSkillSpecialties = specialties.has(predatorSkill)
                ? (ArrayNode) specialties.get(predatorSkill) : specialties.putArray(predatorSkill);
        predatorSkillSpecialties.add(predatorSpecialty.path("specialty").asString());

        ObjectNode disciplines = sheet.putObject("disciplines");
        disciplineDots(build).forEach((name, dots) -> {
            ObjectNode discipline = disciplines.putObject(name);
            discipline.put("dots", dots);
            discipline.set("powers", build.path("powers").path(name).deepCopy());
        });

        ArrayNode rituals = sheet.putArray("rituals");
        if (disciplines.has(BLOOD_SORCERY) && content.ritual(text(build, "ritual")).isPresent()) {
            rituals.add(text(build, "ritual"));
        }

        ArrayNode advantages = sheet.putArray("advantages");
        build.path("advantages").properties().forEach(entry -> {
            if (entry.getValue().asInt() > 0) {
                ObjectNode advantage = advantages.addObject();
                advantage.put("name", entry.getKey());
                advantage.put("dots", entry.getValue().asInt());
                advantage.put("kind", content.advantage(entry.getKey()).map(a -> a.path("kind").asString()).orElse("Merit"));
            }
        });
        sheet.put("predatorExtras", predator.path("extras").asString());
        sheet.set("loresheets", build.path("loresheets").isObject() ? build.path("loresheets").deepCopy() : objectMapper.createObjectNode());
        if (thinBlood) {
            ObjectNode thin = sheet.putObject("thinBlood");
            thin.set("merits", build.path("thinBloodMerits").deepCopy());
            thin.set("flaws", build.path("thinBloodFlaws").deepCopy());
            if (!text(build, "alchemyMethod").isEmpty()) {
                thin.put("alchemyMethod", text(build, "alchemyMethod"));
            }
        }
        sheet.putArray("inventory");
        ObjectNode coterie = sheet.putObject("coterie");
        coterie.put("contribution", Math.clamp(build.path("coterieContribution").asInt(0), 0, MAX_COTERIE_CONTRIBUTION));
        coterie.put("othersDonated", 0);
        coterie.putArray("members");
        coterie.putObject("domain").put("Chasse", 0).put("Lien", 0).put("Portillon", 0);
        coterie.putArray("backgrounds");
        sheet.set("convictions", build.path("convictions").deepCopy());

        int humanity = BASE_HUMANITY + predator.path("humanity").asInt(0) - (isAncilla(build) ? 1 : 0);
        sheet.put("humanity", humanity);
        sheet.put("stains", 0);
        sheet.put("bloodPotency", thinBlood ? 0 : age.path("bp").asInt() + predator.path("bp").asInt(0));
        sheet.put("hunger", 1);
        sheet.putObject("health").put("superficial", 0).put("aggravated", 0);
        sheet.putObject("willpower").put("superficial", 0).put("aggravated", 0);
        sheet.putNull("resonance");
        ArrayNode xpLog = sheet.putArray("xpLog");
        if (age.path("xp").asInt() > 0) {
            xpLog.addObject().put("kind", "award").put("amount", age.path("xp").asInt()).put("note", "Starting XP (" + text(build, "age") + ")");
        }
        sheet.put("notes", "");
        sheet.set("build", build.deepCopy());
        return sheet;
    }

    // Helpers

    Map<String, Integer> disciplineDots(JsonNode build) {
        Map<String, Integer> dots = new LinkedHashMap<>();
        if (!text(build, "disciplineTwo").isEmpty()) {
            dots.merge(text(build, "disciplineTwo"), 2, Integer::sum);
        }
        if (!text(build, "disciplineOne").isEmpty()) {
            dots.merge(text(build, "disciplineOne"), 1, Integer::sum);
        }
        String predatorDiscipline = text(build, "predatorDiscipline");
        if (!predatorDiscipline.isEmpty() && content.discipline(predatorDiscipline).isPresent()) {
            dots.merge(predatorDiscipline, 1, Integer::sum);
        }
        if ("Thin-blooded".equals(text(build, "clan")) && names(build.path("thinBloodMerits")).contains(ALCHEMIST)) {
            dots.merge(ALCHEMY, 1, Integer::sum);
        }
        dots.keySet().removeIf(name -> content.discipline(name).isEmpty());
        return dots;
    }

    private List<String> clanDisciplines(Optional<JsonNode> clan) {
        if (clan.isEmpty()) {
            return List.of();
        }
        if (clan.get().path("name").asString().equals("Caitiff")) {
            return content.disciplineNames().stream().filter(name -> !name.equals(ALCHEMY)).toList();
        }
        List<String> names = new ArrayList<>();
        clan.get().path("disciplines").forEach(d -> names.add(d.asString()));
        return names;
    }

    private boolean isPowerAllowed(String discipline, String power, int level) {
        return content.discipline(discipline).map(d -> {
            for (JsonNode p : d.path("powers")) {
                if (p.path("name").asString().equals(power)) {
                    return p.path("level").asInt() <= level;
                }
            }
            return false;
        }).orElse(false);
    }

    private static List<String> placed(JsonNode values, List<String> names, int maxDots, List<String> problems, String label) {
        List<String> placed = new ArrayList<>();
        values.properties().forEach(entry -> {
            int dots = entry.getValue().asInt(0);
            if (!names.contains(entry.getKey())) {
                problems.add("Unknown " + label.toLowerCase() + ": " + entry.getKey());
            } else if (dots > maxDots) {
                problems.add(label + " " + entry.getKey() + " can't start above " + maxDots + ".");
            } else if (dots > 0) {
                placed.add(entry.getKey());
            }
        });
        return placed;
    }

    private static void slotProblems(JsonNode values, List<String> names, Map<Integer, Integer> slots, String what, List<String> problems) {
        slots.forEach((dots, allowed) -> {
            long used = names.stream().filter(name -> values.path(name).asInt(0) == dots).count();
            if (used > allowed) {
                problems.add("Too many " + what + " at " + dots + ": " + used + " of " + allowed + ".");
            }
        });
    }

    private static CreationChoice single(String id, String step, String prompt, String selected) {
        return new CreationChoice(id, step, null, prompt, null, 1, false, List.of(), selectedIf(selected));
    }

    private static CreationChoice counted(String id, String step, String prompt, int count, List<String> selected) {
        return new CreationChoice(id, step, null, prompt, null, count, false, List.of(), selected);
    }

    private static List<String> selectedIf(String value) {
        return value == null || value.isBlank() ? List.of() : List.of(value);
    }

    private static boolean contains(JsonNode list, String value) {
        for (JsonNode entry : list) {
            if (entry.asString("").equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAncilla(JsonNode build) {
        return "Ancilla".equals(text(build, "age"));
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isString() ? value.asString().trim() : "";
    }

    private JsonNode read(String json) {
        try {
            JsonNode node = objectMapper.readTree(json == null ? emptyDraft() : json);
            if (!node.isObject()) {
                throw new InvalidBuildException("A VtM build must be a JSON object.");
            }
            return node;
        } catch (JacksonException malformed) {
            throw new InvalidBuildException("Malformed VtM build", malformed);
        }
    }
}

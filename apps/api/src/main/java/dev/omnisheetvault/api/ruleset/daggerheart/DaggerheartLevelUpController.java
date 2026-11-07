package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.CLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.DOMAIN_CARD;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.SUBCLASS;

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Daggerheart's level up (SRD pp. 53–54): the next level's entry is kept on the sheet as {@code levelUpDraft} while the
 * player fills it, checked against the advancement grid, and appended to the build's {@code levels} when finished.
 * The sheet is then assembled again with its play state kept.
 */
@RestController
class DaggerheartLevelUpController {

    private static final String DRAFT = "levelUpDraft";

    private final CharacterService characterService;
    private final CatalogueService catalogueService;
    private final DaggerheartCharacterCreationFlow flow;
    private final ObjectMapper objectMapper;
    private final DaggerheartSheetAssembler assembler;
    private final DaggerheartDerivedStats derivedStats;

    DaggerheartLevelUpController(CharacterService characterService, CatalogueService catalogueService, DaggerheartCharacterCreationFlow flow,
            ObjectMapper objectMapper) {
        this.characterService = characterService;
        this.catalogueService = catalogueService;
        this.flow = flow;
        this.objectMapper = objectMapper;
        this.assembler = new DaggerheartSheetAssembler(objectMapper);
        this.derivedStats = new DaggerheartDerivedStats(objectMapper);
    }

    @GetMapping("/api/daggerheart/characters/{id}/level-up")
    ObjectNode get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return view(storedSheet(jwt, id));
    }

    /** Stores the next level's entry as the player fills it; it is checked on every save and when finishing. */
    @PutMapping("/api/daggerheart/characters/{id}/level-up")
    ObjectNode save(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody JsonNode entry) {
        if (!entry.isObject()) {
            throw new InvalidBuildException("A level entry is a JSON object.");
        }
        ObjectNode sheet = storedSheet(jwt, id);
        sheet.set(DRAFT, entry);
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
        return view(sheet);
    }

    @DeleteMapping("/api/daggerheart/characters/{id}/level-up")
    void cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        ObjectNode sheet = storedSheet(jwt, id);
        sheet.remove(DRAFT);
        characterService.replaceMySheet(jwt, id, objectMapper.writeValueAsString(sheet));
    }

    /** Appends the entry to the build's levels and assembles the sheet again, keeping its play state. */
    @PostMapping("/api/daggerheart/characters/{id}/level-up/finish")
    ObjectNode finish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        ObjectNode sheet = storedSheet(jwt, id);
        ObjectNode view = view(sheet);
        if (view.path("atMaxLevel").asBoolean()) {
            throw new InvalidBuildException("Level " + DaggerheartLevels.MAX_LEVEL + " is the highest.");
        }
        if (!view.path("missing").isEmpty() || !view.path("problems").isEmpty()) {
            throw new InvalidBuildException("The level up isn't ready: " + String.join("; ", texts(view.path("missing"), view.path("problems"))));
        }
        ObjectNode build = (ObjectNode) sheet.path("build").deepCopy();
        ArrayNode levels = build.has("levels") ? (ArrayNode) build.get("levels") : build.putArray("levels");
        levels.add(sheet.path(DRAFT).deepCopy());
        sheet.remove(DRAFT);
        MaterializedSheet materialized = flow.materialize(objectMapper.writeValueAsString(sheet), objectMapper.writeValueAsString(build),
                catalogueService.lookup(DaggerheartGameSystem.SYSTEM_ID));
        if (!materialized.isMaterialized()) {
            throw new InvalidBuildException("The level up isn't ready: " + String.join("; ", materialized.plan().problems()));
        }
        characterService.replaceMySheet(jwt, id, materialized.sheetJson());
        ObjectNode leveled = (ObjectNode) objectMapper.readTree(materialized.sheetJson());
        leveled.set("derived", derivedStats.derive(leveled));
        return leveled;
    }

    private ObjectNode view(ObjectNode sheet) {
        DaggerheartCatalogue catalogue = new DaggerheartCatalogue(catalogueService.lookup(DaggerheartGameSystem.SYSTEM_ID));
        DaggerheartBuild build = DaggerheartBuild.read(objectMapper.writeValueAsString(sheet.path("build")), objectMapper);
        JsonNode draft = sheet.path(DRAFT).isObject() ? sheet.path(DRAFT) : objectMapper.createObjectNode();
        int level = build.level();
        int next = level + 1;
        int tier = DaggerheartLevels.tier(next);
        DaggerheartLevels.Walk before = DaggerheartLevels.walk(build, catalogue, null);
        DaggerheartLevels.Walk with = DaggerheartLevels.walk(build, catalogue, draft);

        ObjectNode view = objectMapper.createObjectNode();
        view.put("level", level);
        view.put("nextLevel", next);
        view.put("tier", tier);
        view.put("atMaxLevel", level >= DaggerheartLevels.MAX_LEVEL);
        view.set("draft", draft.deepCopy());
        view.putObject("achievement")
                .put("experience", DaggerheartLevels.ACHIEVEMENT_LEVELS.contains(next))
                .put("proficiency", DaggerheartLevels.ACHIEVEMENT_LEVELS.contains(next))
                .put("clearsTraitMarks", DaggerheartLevels.MARK_CLEARING_LEVELS.contains(next));
        grid(view.putArray("grid"), before, with, tier);
        ArrayNode marked = view.putArray("markedTraits");
        if (!DaggerheartLevels.MARK_CLEARING_LEVELS.contains(next)) {
            before.markedTraits.forEach(marked::add);
        }
        view.set("experiences", sheet.path("experiences").deepCopy());
        cardOptions(view.putArray("cardOptions"), build, catalogue, before, with, next);
        multiclassOptions(view.putArray("multiclassOptions"), build, catalogue, before, next);
        subclassNext(view, build, catalogue, before);
        String prefix = "Level " + next + ": ";
        ArrayNode problems = view.putArray("problems");
        with.problems.stream().filter(problem -> problem.startsWith(prefix)).map(problem -> problem.substring(prefix.length())).forEach(problems::add);
        missing(view.putArray("missing"), draft, next);

        ObjectNode leveledBuild = (ObjectNode) sheet.path("build").deepCopy();
        (leveledBuild.has("levels") ? (ArrayNode) leveledBuild.get("levels") : leveledBuild.putArray("levels")).add(draft.deepCopy());
        ObjectNode after = assembler.assemble(DaggerheartBuild.read(objectMapper.writeValueAsString(leveledBuild), objectMapper), catalogue);
        view.set("before", summary(sheet));
        view.set("after", summary(after));
        ArrayNode newFeatures = view.putArray("newFeatures");
        Set<String> had = new HashSet<>();
        sheet.path("features").forEach(feature -> had.add(feature.path("name").asString()));
        after.path("features").forEach(feature -> {
            if (!had.contains(feature.path("name").asString())) {
                newFeatures.add(feature.deepCopy());
            }
        });
        return view;
    }

    /** Each tier's options with their slots, the marks earlier levels left, this level's picks, and whether they can be taken now. */
    private void grid(ArrayNode grid, DaggerheartLevels.Walk before, DaggerheartLevels.Walk with, int tier) {
        for (int gridTier = 2; gridTier <= 4; gridTier++) {
            ObjectNode tierNode = grid.addObject().put("tier", gridTier).put("open", gridTier <= tier);
            ArrayNode options = tierNode.putArray("options");
            for (DaggerheartLevels.Option option : DaggerheartLevels.Option.values()) {
                if (gridTier < option.minTier) {
                    continue;
                }
                boolean crossed = switch (option) {
                    case SUBCLASS -> Integer.valueOf(gridTier).equals(before.multiclassTier) || before.subclassUpgrades >= DaggerheartLevels.SUBCLASS_UPGRADES;
                    case MULTICLASS -> before.multiclass != null || before.tiersWithSubclass.contains(gridTier);
                    default -> false;
                };
                int earlier = before.marks(gridTier, option);
                options.addObject()
                        .put("id", option.id)
                        .put("label", option.label + (option == DaggerheartLevels.Option.DOMAIN_CARD ? cardCapLabel(gridTier) : ""))
                        .put("slots", option.slots)
                        .put("marked", earlier)
                        .put("picked", with.marks(gridTier, option) - earlier)
                        .put("takesBothPicks", option.takesBothPicks)
                        .put("crossedOut", crossed);
            }
        }
    }

    private static String cardCapLabel(int tier) {
        return tier < 4 ? " (up to level " + DaggerheartLevels.extraCardCap(tier) + ")" : "";
    }

    /** Cards the new level can take: the class's domains up to the new level, and a multiclass domain up to half of it. */
    private void cardOptions(ArrayNode options, DaggerheartBuild build, DaggerheartCatalogue catalogue, DaggerheartLevels.Walk before,
            DaggerheartLevels.Walk with, int next) {
        Set<String> domains = new HashSet<>();
        catalogue.find(CLASS, build.classSlug()).ifPresent(entry -> entry.data().path("domains").forEach(domain -> domains.add(domain.asString())));
        String multiclassDomain = with.multiclass != null ? with.multiclass.domain() : "";
        catalogue.list(DOMAIN_CARD).stream()
                .filter(card -> !before.cards.contains(card.slug()))
                .filter(card -> {
                    String domain = card.data().path("domain").asString();
                    int cardLevel = card.data().path("level").asInt();
                    return domains.contains(domain) && cardLevel <= next
                            || domain.equals(multiclassDomain) && cardLevel <= DaggerheartLevels.multiclassCardCap(next);
                })
                .sorted(Comparator.comparing((CatalogueRecord card) -> card.data().path("level").asInt()).thenComparing(CatalogueRecord::name))
                .forEach(card -> {
                    ObjectNode node = options.addObject().put("slug", card.slug()).put("name", card.name());
                    node.setAll((ObjectNode) card.data().deepCopy());
                    node.put("text", card.description() == null ? "" : card.description());
                });
    }

    private void multiclassOptions(ArrayNode options, DaggerheartBuild build, DaggerheartCatalogue catalogue, DaggerheartLevels.Walk before, int next) {
        if (next < DaggerheartLevels.MULTICLASS_LEVEL || before.multiclass != null) {
            return;
        }
        catalogue.list(CLASS).stream().filter(entry -> !entry.slug().equals(build.classSlug())).sorted(Comparator.comparing(CatalogueRecord::name))
                .forEach(entry -> {
                    ObjectNode node = options.addObject().put("slug", entry.slug()).put("name", entry.name());
                    node.set("domains", entry.data().path("domains").deepCopy());
                    node.set("features", entry.data().path("features").deepCopy());
                    ArrayNode subclasses = node.putArray("subclasses");
                    catalogue.list(SUBCLASS, data -> data.path("classSlug").asString().equals(entry.slug())).forEach(subclass -> subclasses.addObject()
                            .put("slug", subclass.slug()).put("name", subclass.name()).set("foundation", subclass.data().path("foundation").deepCopy()));
                });
    }

    /** The subclass card an upgrade gives next: the specialization, then the mastery. */
    private void subclassNext(ObjectNode view, DaggerheartBuild build, DaggerheartCatalogue catalogue, DaggerheartLevels.Walk before) {
        String card = before.subclassUpgrades == 0 ? "specialization" : before.subclassUpgrades == 1 ? "mastery" : "";
        ObjectNode node = view.putObject("subclassNext").put("card", card);
        catalogue.find(SUBCLASS, build.subclassSlug()).ifPresent(subclass -> {
            node.put("subclass", subclass.name());
            node.set("features", card.isEmpty() ? objectMapper.createArrayNode() : subclass.data().path(card).deepCopy());
        });
    }

    private static void missing(ArrayNode missing, JsonNode draft, int next) {
        if (DaggerheartLevels.ACHIEVEMENT_LEVELS.contains(next) && DaggerheartBuild.text(draft, "experience").isEmpty()) {
            missing.add("Name the new Experience.");
        }
        int picks = 0;
        for (JsonNode advancement : draft.path("advancements")) {
            picks += DaggerheartLevels.Option.of(advancement.path("option").asString("")).map(option -> option.takesBothPicks ? 2 : 1).orElse(1);
        }
        if (picks < DaggerheartLevels.PICKS_PER_LEVEL) {
            missing.add("Choose " + (DaggerheartLevels.PICKS_PER_LEVEL - picks) + " more advancement" + (picks == 1 ? "" : "s") + ".");
        }
        if (DaggerheartBuild.text(draft, "domainCard").isEmpty()) {
            missing.add("Take the new domain card.");
        }
    }

    private ObjectNode summary(ObjectNode sheet) {
        ObjectNode derived = derivedStats.derive(sheet);
        ObjectNode summary = objectMapper.createObjectNode();
        summary.put("level", sheet.path("level").asInt(1));
        summary.put("proficiency", derived.path("proficiency").asInt());
        for (String field : List.of("evasion", "armorScore", "majorThreshold", "severeThreshold", "hitPointSlots", "stressSlots")) {
            summary.put(field, derived.path(field).path("value").asInt());
        }
        ObjectNode traits = summary.putObject("traits");
        DaggerheartBuild.TRAITS.forEach(trait -> traits.put(trait, derived.path("traits").path(trait).path("value").asInt()));
        summary.set("experiences", sheet.path("experiences").deepCopy());
        ArrayNode cards = summary.putArray("domainCards");
        sheet.path("domainCards").forEach(card -> cards.add(card.path("name").asString()));
        return summary;
    }

    private static List<String> texts(JsonNode... arrays) {
        List<String> texts = new ArrayList<>();
        for (JsonNode array : arrays) {
            array.forEach(text -> texts.add(text.asString()));
        }
        return texts;
    }

    private ObjectNode storedSheet(Jwt jwt, UUID id) {
        dev.omnisheetvault.api.character.Character character = characterService.getMineActive(jwt, id);
        if (!DaggerheartGameSystem.SYSTEM_ID.equals(character.systemId())) {
            throw new UnsupportedGameSystemException(character.systemId());
        }
        ObjectNode sheet = (ObjectNode) objectMapper.readTree(character.sheet());
        if (DaggerheartBuild.read(objectMapper.writeValueAsString(sheet.path("build")), objectMapper).level() >= DaggerheartLevels.MAX_LEVEL
                && sheet.has(DRAFT)) {
            sheet.remove(DRAFT);
        }
        return sheet;
    }
}

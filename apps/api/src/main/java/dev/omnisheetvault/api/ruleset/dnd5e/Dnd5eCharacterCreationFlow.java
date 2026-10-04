package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.ProgressionTable;
import dev.omnisheetvault.api.ruleset.SelectionDetail;
import dev.omnisheetvault.api.ruleset.StartingEquipmentPreview;
import dev.omnisheetvault.api.ruleset.StartingItem;
import java.util.Optional;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.LevelUp;
import dev.omnisheetvault.api.ruleset.LevelUpNotAllowedException;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** D&D 5e's character creation: reads a {@link Dnd5eCharacterBuild} and plans its choices with {@link Dnd5eBuildPlanner}. */
@Component
public class Dnd5eCharacterCreationFlow implements CharacterCreationFlow {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public Dnd5eCharacterCreationFlow(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = Dnd5eSheetJsonMapper.lenient(objectMapper);
        this.validator = validator;
    }

    @Override
    public String systemId() {
        return Dnd5eGameSystem.SYSTEM_ID;
    }

    @Override
    public String emptyDraft() {
        return objectMapper.writeValueAsString(new Dnd5eCharacterBuild(
                null, null, null, null, List.of(), null, Map.of(), Dnd5eHitPointMethod.FIXED, List.of(), List.of(), Map.of(),
                Dnd5ePreferences.forNewCharacter()));
    }

    @Override
    public BuildPlan plan(String buildJson, CatalogueLookup catalogue) {
        return new Dnd5eBuildPlanner(read(buildJson, Default.class), catalogue).plan();
    }

    @Override
    public CreationPreview preview(String buildJson, CatalogueLookup catalogue) {
        Dnd5eCharacterBuild build = read(buildJson, Default.class);
        Dnd5eBuildPlanner planner = new Dnd5eBuildPlanner(build, catalogue);
        planner.plan();
        String sheetJson = null;
        if (!planner.outcome().classes().isEmpty() && build.hasEveryAbilityScore()) {
            Dnd5eSheet sheet = new Dnd5eBuildMaterializer(build, planner.state(), planner.outcome(), catalogue).materialize(null);
            sheetJson = objectMapper.writeValueAsString(sheet);
        }
        return new CreationPreview(abilityScores(planner.state()), sheetJson, progressions(build, planner.outcome(), catalogue),
                selections(build, planner, catalogue), startingEquipment(build, planner.outcome(), catalogue));
    }

    /** The starting items as they go onto the sheet and as the Equipment step lists them, and the starting money in coins. */
    static StartingEquipmentPreview startingEquipment(Dnd5eCharacterBuild build, Dnd5eBuildOutcome outcome, CatalogueLookup catalogue) {
        List<StartingItem> items = Dnd5eBuildMaterializer.startingItems(outcome, build);
        List<String> keys = Dnd5eBuildMaterializer.lineKeys(outcome);
        List<StartingEquipmentPreview.Entry> inventory = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            StartingItem item = items.get(index);
            Optional<CatalogueRecord> record = item.catalogueSlug() == null ? Optional.empty() : catalogue.find("ITEM", item.catalogueSlug());
            String name = item.displayName() != null ? item.displayName()
                    : record.map(CatalogueRecord::name).orElse(item.customName() != null ? item.customName() : item.catalogueSlug());
            String action = record.map(entry -> equipAction(entry.data().path("itemKind").asString(""))).orElse(null);
            inventory.add(new StartingEquipmentPreview.Entry(keys.get(index), name, item.quantity(), action, item.equipped()));
        }
        int copper = outcome.startingCopper();
        return new StartingEquipmentPreview(items, inventory, copper / 100, copper % 100 / 10, copper % 10);
    }

    /** Armor and shields are worn, weapons wielded; anything else can't start equipped. */
    private static String equipAction(String itemKind) {
        return switch (itemKind) {
            case "ARMOR", "SHIELD" -> "WEAR";
            case "WEAPON" -> "WIELD";
            default -> null;
        };
    }

    /** The detail cards of the chosen species and background, by step. */
    private static Map<String, SelectionDetail> selections(Dnd5eCharacterBuild build, Dnd5eBuildPlanner planner, CatalogueLookup catalogue) {
        Dnd5eOptionSummaries summaries = new Dnd5eOptionSummaries(catalogue, !catalogue.redactsProse());
        Map<String, SelectionDetail> selections = new LinkedHashMap<>();
        Dnd5eBuildOutcome outcome = planner.outcome();
        if (outcome.speciesMechanics() != null && build.speciesSlug() != null) {
            catalogue.find("SPECIES", build.speciesSlug()).ifPresent(species ->
                    selections.put("species", Dnd5eSelectionDetails.species(species, outcome.speciesLabel(), outcome.speciesMechanics(), summaries)));
        }
        if (outcome.background() != null) {
            selections.put("background", Dnd5eSelectionDetails.background(outcome.background(), planner, summaries));
        }
        return selections;
    }

    /** One table per chosen class, with its subclass once one is chosen (even below the subclass level, as a preview). */
    private static List<ProgressionTable> progressions(Dnd5eCharacterBuild build, Dnd5eBuildOutcome outcome, CatalogueLookup catalogue) {
        return outcome.classes().stream().map(taken -> {
            CatalogueRecord subclass = taken.subclass();
            String subclassSlug = taken.buildClass().subclassSlug();
            if (subclass == null && subclassSlug != null) {
                subclass = catalogue.find("SUBCLASS", subclassSlug).orElse(null);
            }
            return Dnd5eClassProgression.table(taken.playerClass(), subclass, taken.buildClass().level(),
                    build.preferences().optionalClassFeatures());
        }).toList();
    }

    private static Map<String, CalculatedValue> abilityScores(Dnd5eGrantState state) {
        Map<String, CalculatedValue> scores = new LinkedHashMap<>();
        for (String ability : List.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma")) {
            List<Contribution> contributions = state.abilityContributions().getOrDefault(ability, List.of()).stream()
                    .map(part -> new Contribution(part.source(), part.amount()))
                    .toList();
            scores.put(ability, new CalculatedValue(state.score(ability), contributions));
        }
        return scores;
    }

    @Override
    public MaterializedSheet materialize(String currentSheetJson, String buildJson, CatalogueLookup catalogue) {
        Dnd5eCharacterBuild build = read(buildJson, Default.class, Dnd5eCharacterBuild.Complete.class);
        Dnd5eBuildPlanner planner = new Dnd5eBuildPlanner(build, catalogue);
        BuildPlan plan = planner.plan();
        if (!plan.pending().isEmpty() || !plan.problems().isEmpty()) {
            return new MaterializedSheet(null, List.of(), 0, plan);
        }
        Dnd5eBuildMaterializer materializer = new Dnd5eBuildMaterializer(build, planner.state(), planner.outcome(), catalogue);
        Dnd5eSheet current = currentSheetJson == null ? null : objectMapper.readValue(currentSheetJson, Dnd5eSheet.class);
        Dnd5eSheet sheet = materializer.materialize(current);
        return new MaterializedSheet(objectMapper.writeValueAsString(sheet), materializer.startingItems(),
                materializer.startingCopper(), plan);
    }

    @Override
    public Optional<String> buildOf(String sheetJson) {
        Dnd5eSheet sheet = objectMapper.readValue(sheetJson, Dnd5eSheet.class);
        return Optional.ofNullable(sheet.build()).map(objectMapper::writeValueAsString);
    }

    @Override
    public LevelUp withLevelUp(String buildJson, String classSlug, CatalogueLookup catalogue) {
        Dnd5eCharacterBuild build = read(buildJson, Default.class);
        if (build.characterLevel() >= Dnd5eExperience.MAX_LEVEL) {
            throw new LevelUpNotAllowedException("The character is already level " + Dnd5eExperience.MAX_LEVEL + ".");
        }
        if (catalogue.find("CLASS", classSlug).isEmpty()) {
            throw new LevelUpNotAllowedException("Unknown class: " + classSlug);
        }
        List<Dnd5eBuildClass> classes = new ArrayList<>();
        int classLevel = 1;
        int newLevelIndex = build.characterLevel();
        int levelsBefore = 0;
        for (Dnd5eBuildClass taken : build.classes()) {
            boolean leveled = taken.classSlug().equals(classSlug);
            if (leveled) {
                classLevel = taken.level() + 1;
                newLevelIndex = levelsBefore + taken.level();
            }
            levelsBefore += taken.level();
            classes.add(leveled ? new Dnd5eBuildClass(taken.classSlug(), taken.subclassSlug(), taken.level() + 1) : taken);
        }
        if (classLevel == 1) {
            classes.add(new Dnd5eBuildClass(classSlug, null, 1));
        }
        String leveledBuild = objectMapper.writeValueAsString(new Dnd5eCharacterBuild(build.speciesSlug(), build.subspeciesName(),
                build.speciesVariantName(), build.backgroundSlug(), classes, build.abilityScoreMethod(), build.baseAbilityScores(),
                build.hitPointMethod(), withUnrolledLevel(build.rolledHitPoints(), newLevelIndex), build.choices(),
                build.abilityScoreAdjustments(), build.preferences(), build.equippedStartingItems()));
        return new LevelUp(leveledBuild, classLevel);
    }

    /**
     * Rolled hit points are stored in class-level order, one per level after the first: the new level gets an empty
     * slot at its own place so no roll moves to another class's level.
     */
    private static List<Integer> withUnrolledLevel(List<Integer> rolledHitPoints, int newLevelIndex) {
        int slot = newLevelIndex - 1;
        if (slot < 0 || slot > rolledHitPoints.size()) {
            return rolledHitPoints;
        }
        List<Integer> shifted = new ArrayList<>(rolledHitPoints);
        shifted.add(slot, null);
        return shifted;
    }

    private Dnd5eCharacterBuild read(String buildJson, Class<?>... groups) {
        Dnd5eCharacterBuild build;
        try {
            build = objectMapper.readValue(buildJson, Dnd5eCharacterBuild.class);
        } catch (JacksonException e) {
            throw new InvalidBuildException("Unreadable D&D 5e build: " + e.getOriginalMessage(), e);
        }
        Set<ConstraintViolation<Dnd5eCharacterBuild>> violations = validator.validate(build, groups);
        if (!violations.isEmpty()) {
            throw new InvalidBuildException("Invalid D&D 5e build: " + violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }
        return build;
    }
}

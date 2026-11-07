package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.ProgressionTable;
import dev.omnisheetvault.api.shared.InterfaceLocale;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import tools.jackson.databind.JsonNode;

/**
 * A class's table for levels 1–20, from its catalogue data: proficiency bonus, the features gained
 * (the chosen subclass's in place of the "… feature" placeholders), the class's and subclass's own
 * columns (Rages, Invocations Known…) and, for a caster, its spell slots per level.
 */
final class Dnd5eClassProgression {

    private static final int MAX_LEVEL = 20;
    private static final int SPELL_LEVELS = 9;
    private static final String NONE = "—";

    private Dnd5eClassProgression() {
    }

    static ProgressionTable table(CatalogueRecord playerClass, CatalogueRecord subclass, int currentLevel, boolean optionalFeatures) {
        return table(playerClass, subclass, currentLevel, optionalFeatures, Dnd5eText.ENGLISH);
    }

    /**
     * The headers and level ordinals in {@code text}'s language, and the class's own column labels, feature names and title in
     * the catalogue's translation for the request's language; the features column is always the third.
     */
    static ProgressionTable table(CatalogueRecord playerClass, CatalogueRecord subclass, int currentLevel, boolean optionalFeatures, Dnd5eText text) {
        Locale locale = InterfaceLocale.current();
        UnaryOperator<String> classText = playerClass.texts(locale);
        UnaryOperator<String> subclassText = subclass == null ? UnaryOperator.identity() : subclass.texts(locale);
        List<String> columnLabels = new ArrayList<>();
        List<JsonNode> tableColumns = new ArrayList<>();
        playerClass.data().path("tableColumns").forEach(column -> {
            tableColumns.add(column);
            columnLabels.add(classText.apply(column.path("label").asString("")));
        });
        if (subclass != null) {
            subclass.data().path("tableColumns").forEach(column -> {
                tableColumns.add(column);
                columnLabels.add(subclassText.apply(column.path("label").asString("")));
            });
        }
        JsonNode slots = spellSlots(playerClass, subclass);
        int slotColumns = highestSlotLevel(slots);

        List<String> columns = new ArrayList<>(List.of(text.text("progression.level"), text.text("progression.proficiencyBonus"),
                text.text("progression.features")));
        columns.addAll(columnLabels);
        for (int spellLevel = 1; spellLevel <= slotColumns; spellLevel++) {
            columns.add(text.ordinal(spellLevel));
        }

        List<List<String>> rows = new ArrayList<>();
        for (int level = 1; level <= MAX_LEVEL; level++) {
            List<String> row = new ArrayList<>(List.of(text.ordinal(level), "+" + (2 + (level - 1) / 4),
                    features(playerClass, subclass, level, optionalFeatures, classText, subclassText)));
            for (JsonNode column : tableColumns) {
                JsonNode value = column.path("valuesByLevel").path(level - 1);
                row.add(value.isMissingNode() || value.isNull() ? NONE : value.asString());
            }
            for (int spellLevel = 1; spellLevel <= slotColumns; spellLevel++) {
                int count = slots.path(level - 1).path(spellLevel - 1).asInt(0);
                row.add(count == 0 ? NONE : String.valueOf(count));
            }
            rows.add(row);
        }
        String className = playerClass.localizedName(locale);
        String name = subclass == null ? className : className + " (" + subclass.localizedName(locale) + ")";
        return new ProgressionTable(playerClass.slug(), name, currentLevel, columns, rows);
    }

    /** The class's features at {@code level}; a subclass placeholder becomes the chosen subclass's own features, if any. */
    private static String features(CatalogueRecord playerClass, CatalogueRecord subclass, int level, boolean optionalFeatures,
            UnaryOperator<String> classText, UnaryOperator<String> subclassText) {
        List<String> names = new ArrayList<>();
        boolean placeholder = false;
        for (JsonNode feature : playerClass.data().path("features")) {
            if (feature.path("level").asInt() != level || (feature.path("optional").asBoolean(false) && !optionalFeatures)) {
                continue;
            }
            String name = feature.path("name").asString("");
            boolean isPlaceholder = feature.path("grantsSubclassFeature").asBoolean(false) && name.endsWith(" feature");
            placeholder |= feature.path("grantsSubclassFeature").asBoolean(false);
            if (!isPlaceholder || subclass == null) {
                names.add(classText.apply(name));
            }
        }
        if (subclass != null && placeholder) {
            for (JsonNode feature : subclass.data().path("features")) {
                String name = feature.path("name").asString("");
                if (feature.path("level").asInt() == level && !name.endsWith(" Options")) {
                    names.add(subclassText.apply(name));
                }
            }
        }
        return names.isEmpty() ? NONE : String.join(", ", names);
    }

    /** The slot table of the class, or of its subclass for a third caster (Eldritch Knight); missing for non-casters and pact magic. */
    private static JsonNode spellSlots(CatalogueRecord playerClass, CatalogueRecord subclass) {
        JsonNode slots = playerClass.data().path("spellcasting").path("spellSlotsByLevel");
        if ((slots.isMissingNode() || slots.isNull()) && subclass != null) {
            slots = subclass.data().path("spellcasting").path("spellSlotsByLevel");
        }
        return slots;
    }

    private static int highestSlotLevel(JsonNode slots) {
        int highest = 0;
        for (JsonNode levelSlots : slots) {
            for (int index = 0; index < Math.min(SPELL_LEVELS, levelSlots.size()); index++) {
                if (levelSlots.get(index).asInt(0) > 0) {
                    highest = Math.max(highest, index + 1);
                }
            }
        }
        return highest;
    }
}

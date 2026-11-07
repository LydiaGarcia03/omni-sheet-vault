package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;

/** Daggerheart's catalogue kinds (content/daggerheart, D1) read through the generic lookup. */
final class DaggerheartCatalogue {

    static final String CLASS = "CLASS";
    static final String SUBCLASS = "SUBCLASS";
    static final String ANCESTRY = "ANCESTRY";
    static final String COMMUNITY = "COMMUNITY";
    static final String TRANSFORMATION = "TRANSFORMATION";
    static final String DOMAIN = "DOMAIN";
    static final String DOMAIN_CARD = "DOMAIN_CARD";
    static final String ITEM = "ITEM";

    private final CatalogueLookup lookup;

    DaggerheartCatalogue(CatalogueLookup lookup) {
        this.lookup = lookup;
    }

    Optional<CatalogueRecord> find(String kind, String slug) {
        return slug == null || slug.isBlank() ? Optional.empty() : lookup.find(kind, slug);
    }

    List<CatalogueRecord> list(String kind) {
        return lookup.list(kind);
    }

    List<CatalogueRecord> list(String kind, Predicate<JsonNode> filter) {
        return lookup.list(kind).stream().filter(entry -> filter.test(entry.data())).toList();
    }

    /** An item of one category ("weapon", "armor", "loot", "consumable"). */
    Optional<CatalogueRecord> item(String slug, String category) {
        return find(ITEM, slug).filter(item -> category.equals(item.data().path("category").asString()));
    }

    String name(String kind, String slug) {
        return find(kind, slug).map(CatalogueRecord::name).orElse("");
    }

    boolean redactsProse() {
        return lookup.redactsProse();
    }
}

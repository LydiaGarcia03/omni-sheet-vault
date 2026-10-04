package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.List;
import java.util.Optional;

/**
 * The catalogue as a build's preferences allow it: listings (what the builder offers)
 * keep only allowed source books and partners and, unless switched on, no playtest material, while lookups by slug still resolve everything, so
 * an entry granted by an allowed one (a base item, a condition) never goes missing.
 */
final class Dnd5eSourceFilteredCatalogue implements CatalogueLookup {

    private final CatalogueLookup catalogue;
    private final Dnd5ePreferences preferences;

    Dnd5eSourceFilteredCatalogue(CatalogueLookup catalogue, Dnd5ePreferences preferences) {
        this.catalogue = catalogue;
        this.preferences = preferences;
    }

    @Override
    public Optional<CatalogueRecord> find(String kind, String slug) {
        return catalogue.find(kind, slug);
    }

    @Override
    public List<CatalogueRecord> list(String kind) {
        return catalogue.list(kind).stream().filter(preferences::allows).toList();
    }

    @Override
    public boolean redactsProse() {
        return catalogue.redactsProse();
    }
}

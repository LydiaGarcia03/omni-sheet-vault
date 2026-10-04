package dev.omnisheetvault.api.catalogue;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Where each catalogue kind's JSON files live under {@code content/<system>/}: the kind's plural, lower-cased. */
final class CatalogueContentLayout {

    private static final Map<CatalogueEntryKind, String> IRREGULAR_PLURALS = Map.of(CatalogueEntryKind.SPECIES, "species");

    private CatalogueContentLayout() {
    }

    static String directoryName(CatalogueEntryKind kind) {
        String singular = kind.name().toLowerCase(Locale.ROOT);
        return IRREGULAR_PLURALS.getOrDefault(kind, singular.endsWith("s") ? singular + "es" : singular + "s");
    }

    static Path directory(Path systemRoot, CatalogueEntryKind kind) {
        return systemRoot.resolve(directoryName(kind));
    }
}

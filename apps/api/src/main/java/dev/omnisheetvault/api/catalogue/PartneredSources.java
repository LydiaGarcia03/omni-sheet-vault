package dev.omnisheetvault.api.catalogue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * D&D Beyond's partner brands, in its "Choose Partners" order, and which imported sources
 * belong to each. Most brands have no source in 5etools' main data yet.
 */
final class PartneredSources {

    private static final String CRITICAL_ROLE = "Critical Role";
    private static final String RICK_AND_MORTY = "Rick and Morty";

    private static final List<String> PARTNERS = List.of(
            CRITICAL_ROLE,
            "Drakkenheim",
            "Humblewood",
            "GameConclave",
            "Grim Hollow",
            "Kobold Press",
            "MCDM",
            "Free League",
            "The Griffon’s Saddlebag",
            "1985 Games",
            "Road to Ithaka Press",
            "Avantris Entertainment",
            "Loot Tavern",
            "Mage Hand Press",
            "Roll & Play Press",
            "Beadle & Grimm's",
            "Chaosium",
            "MonkeyDM",
            "Paizo",
            "Palaeo Games",
            "Sterling Vermin",
            "Visionary Production and Design Inc.",
            "Baldman Games",
            "Vampire: The Masquerade",
            "Floral Dragons",
            "Minecraft",
            RICK_AND_MORTY);

    private static final Map<String, String> PARTNER_BY_CODE = Map.of(
            "EGW", CRITICAL_ROLE,
            "CRCOTN", CRITICAL_ROLE,
            "TOR", CRITICAL_ROLE,
            "DD", CRITICAL_ROLE,
            "FS", CRITICAL_ROLE,
            "US", CRITICAL_ROLE,
            "RMR", RICK_AND_MORTY,
            "RMBRE", RICK_AND_MORTY);

    private PartneredSources() {
    }

    /** Every partner brand, in D&D Beyond's order. */
    static List<String> partners() {
        return PARTNERS;
    }

    /** The partner a source belongs to, or {@code null} for a source outside every partner brand. */
    static String partnerOf(String sourceCode) {
        return sourceCode == null ? null : PARTNER_BY_CODE.get(sourceCode.toUpperCase(Locale.ROOT));
    }
}

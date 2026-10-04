package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.Set;

/**
 * The rules a build is made under, D&D Beyond's character preferences.
 * {@code sources} names the source books whose options the builder offers
 * ({@code null} = every imported book); the 2014 Player's Handbook and Dungeon
 * Master's Guide are always allowed. Playtest material (Unearthed Arcana) ignores
 * {@code sources} and is offered only when {@code playtestContent} is on; a partner
 * brand's books (Critical Role, Rick and Morty) ignore it too, and are offered only
 * while {@code partneredContent} is on and their brand is in {@code partners}
 * ({@code null} = every partner). The prerequisite switches decide
 * whether feats and multiclassing check their requirements;
 * {@code optionalClassFeatures} adds Tasha's optional class features.
 * {@code advancement}, {@code encumbrance} and {@code ignoreCoinWeight} are carried
 * onto the sheet.
 */
public record Dnd5ePreferences(
        Set<String> sources,
        Boolean optionalClassFeatures,
        Boolean featPrerequisites,
        Boolean multiclassPrerequisites,
        Dnd5eAdvancement advancement,
        Dnd5eEncumbrance encumbrance,
        Boolean ignoreCoinWeight,
        Boolean playtestContent,
        Boolean partneredContent,
        Set<String> partners) {

    public static final Set<String> LOCKED_SOURCES = Set.of("Player's Handbook", "Dungeon Master's Guide");

    public enum Dnd5eAdvancement { MILESTONE, XP }

    public enum Dnd5eEncumbrance { STANDARD, NONE }

    public Dnd5ePreferences {
        optionalClassFeatures = optionalClassFeatures != null && optionalClassFeatures;
        featPrerequisites = featPrerequisites == null || featPrerequisites;
        multiclassPrerequisites = multiclassPrerequisites == null || multiclassPrerequisites;
        advancement = advancement == null ? Dnd5eAdvancement.MILESTONE : advancement;
        encumbrance = encumbrance == null ? Dnd5eEncumbrance.NONE : encumbrance;
        ignoreCoinWeight = ignoreCoinWeight == null || ignoreCoinWeight;
        playtestContent = playtestContent != null && playtestContent;
        partneredContent = partneredContent == null || partneredContent;
    }

    public Dnd5ePreferences(Set<String> sources, Boolean optionalClassFeatures, Boolean featPrerequisites, Boolean multiclassPrerequisites,
            Dnd5eAdvancement advancement, Dnd5eEncumbrance encumbrance, Boolean ignoreCoinWeight) {
        this(sources, optionalClassFeatures, featPrerequisites, multiclassPrerequisites, advancement, encumbrance, ignoreCoinWeight, null, null, null);
    }

    /** What a build without preferences (every build file written before them) is made under: nothing new switched on. */
    public static Dnd5ePreferences defaults() {
        return new Dnd5ePreferences(null, null, null, null, null, null, null, null, null, null);
    }

    /** What a new draft starts with: every source and partner but playtest, standard encumbrance. */
    public static Dnd5ePreferences forNewCharacter() {
        return new Dnd5ePreferences(null, false, true, true, Dnd5eAdvancement.MILESTONE, Dnd5eEncumbrance.STANDARD, true, false, true, null);
    }

    /** Whether the builder may offer an entry from {@code sourceBook}; an entry without a book is always allowed. */
    public boolean allowsSource(String sourceBook) {
        return sources == null || sourceBook == null || LOCKED_SOURCES.contains(sourceBook) || sources.contains(sourceBook);
    }

    /** Whether the builder may offer this entry: playtest entries follow the playtest switch, partnered ones their partner, the rest their book. */
    public boolean allows(CatalogueRecord entry) {
        if (entry.playtest()) {
            return playtestContent;
        }
        if (entry.partner() != null) {
            return partneredContent && (partners == null || partners.contains(entry.partner()));
        }
        return allowsSource(entry.sourceBook());
    }
}

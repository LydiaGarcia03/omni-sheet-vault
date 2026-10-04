package dev.omnisheetvault.api.catalogue;

/**
 * The kinds of reference content the catalogue holds — see adr-0005.
 * {@code BACKGROUND} is phase 10's addition: a background's own personality
 * trait/ideal/bond/flaw suggestion tables (PHB), read by the Background tab's
 * Text Field mold panels to build the "roll a suggestion" control D&D Beyond
 * shows on those four fields.
 */
public enum CatalogueEntryKind {
    SPELL,
    ITEM,
    FEATURE,
    CREATURE,
    BACKGROUND,
    FEAT,
    CLASS,
    SUBCLASS,
    OPTIONAL_FEATURE,
    SPECIES,
    LANGUAGE,
    CONDITION
}

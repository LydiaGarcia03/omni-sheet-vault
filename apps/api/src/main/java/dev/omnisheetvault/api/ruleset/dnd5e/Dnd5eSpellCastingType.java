package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * How a spellcasting class handles its spell list — confirmed live against
 * D&D Beyond across four classes (phase 9's "Manage spells" slice):
 * {@code KNOWN} classes (Ranger, Sorcerer, Bard, Eldritch Knight) learn a
 * fixed number of spells individually and every one is always usable, no
 * separate preparation step. {@code PREPARED} classes (Cleric, Wizard)
 * separate "known" (learned into the spellbook, or the class's whole list)
 * from "prepared" (a daily-changeable subset, bounded by
 * {@link Dnd5eSpellcastingClass#spellsPreparedMax()}), plus features that
 * grant a spell as permanently prepared ({@link Dnd5eSpell#alwaysPrepared()}).
 * The finer real-game distinction between a Cleric's whole-class-list
 * auto-knowledge and a Wizard's spellbook (learned one at a time) is not
 * modeled — both learn from the same seeded catalogue subset via
 * {@link Dnd5eSheetMutator#learnSpell}, confirmed with the owner as this
 * slice's scope boundary (a full official class-spell-list catalogue is a
 * separate, much larger content-authoring effort).
 */
public enum Dnd5eSpellCastingType {
    KNOWN,
    PREPARED
}

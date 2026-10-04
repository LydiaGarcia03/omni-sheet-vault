package dev.omnisheetvault.api.character;

/**
 * The mechanical shape of a {@code SPELL}-kind catalogue entry's {@code data}
 * field — see {@code CatalogueEntryResponse}'s own doc comment ("nothing consumes
 * it yet") and {@code content/dnd-5e/spells/*.json} for real import files matching
 * this shape. {@code className} is deliberately absent: a catalogue spell is not
 * tied to one class, the player picks which of their own spellcasting classes
 * learns it (see {@link LearnSpellRequest}). {@code higherLevelsDescription} is
 * 5etools' own "At Higher Levels" scaling text (null for a spell with no such
 * text — most cantrips, and some leveled spells) — systems/dnd-5e/features/5etools-ingestion.md's
 * open question 5, resolved 2026-09-17: extracted by {@code SpellConverter} from
 * 5etools' {@code entriesHigherLevel} field. {@code higherLevelsDamageDiceCount}/
 * {@code higherLevelsDamageDiceSides} are the same "At Higher Levels" scaling,
 * structured instead of prose: the dice to add to {@code damageDiceCount}/
 * {@code damageDiceSides} per slot level above this spell's own {@code level} when
 * cast at a higher one (null together when the spell doesn't scale this way) — see
 * {@code Dnd5eMechanicResolver}'s own use of these for the actual roll math.
 */
record SpellCatalogueData(
        int level,
        String school,
        String castingTime,
        String range,
        boolean concentration,
        boolean ritual,
        boolean attackRoll,
        Integer damageDiceCount,
        Integer damageDiceSides,
        String damageType,
        String notes,
        String effectSummary,
        String saveAbility,
        String components,
        String materialComponent,
        String duration,
        String higherLevelsDescription,
        Integer higherLevelsDamageDiceCount,
        Integer higherLevelsDamageDiceSides) {
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * One class a character has levels in, as materialized from its build; the starting class
 * comes first. {@code hitDiceUsed} is how many of this class's hit dice are spent (0 on
 * sheets written before it was tracked per class).
 */
public record Dnd5eClassLevel(
        @NotBlank String classSlug,
        @NotBlank String className,
        String subclassSlug,
        String subclassName,
        @Min(1) @Max(20) int level,
        @Min(6) @Max(12) int hitDieSize,
        @Min(0) int hitDiceUsed) {

    public Dnd5eClassLevel(String classSlug, String className, String subclassSlug, String subclassName, int level, int hitDieSize) {
        this(classSlug, className, subclassSlug, subclassName, level, hitDieSize, 0);
    }

    Dnd5eClassLevel withHitDiceUsed(int used) {
        return new Dnd5eClassLevel(classSlug, className, subclassSlug, subclassName, level, hitDieSize, used);
    }
}

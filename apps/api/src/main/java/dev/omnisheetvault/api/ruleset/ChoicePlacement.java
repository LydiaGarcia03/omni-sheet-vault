package dev.omnisheetvault.api.ruleset;

/**
 * Where a {@link CreationChoice} arises, so a builder can file it: {@code group} is what grants it
 * (a class, by key) and {@code level} the group level it comes at; a null level belongs to the group
 * as a whole (e.g. spells pooled over every level of a class).
 */
public record ChoicePlacement(String group, Integer level) {
}

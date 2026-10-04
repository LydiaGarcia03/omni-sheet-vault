package dev.omnisheetvault.api.catalogue;

/**
 * One imported source book, its short code (e.g. "PHB", null when its entries carry none), how many
 * catalogue entries it contributes, whether it is playtest material (Unearthed Arcana), and the partner
 * brand it belongs to (e.g. "Critical Role", null for none).
 */
public record CatalogueSourceResponse(String sourceBook, String sourceCode, long entryCount, boolean playtest, String partner) {
}

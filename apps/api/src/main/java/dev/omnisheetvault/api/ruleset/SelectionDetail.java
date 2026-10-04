package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * What a chosen option (a species, a background) gives, for a builder to show under its select:
 * {@code facts} are short label/value pairs ("Speed" · "25 ft"), {@code grants} are what it grants,
 * each titled, with its text (null when the catalogue's prose is redacted).
 */
public record SelectionDetail(String name, String sourceBook, List<Entry> facts, List<Entry> grants) {

    public record Entry(String label, String text) {
    }
}

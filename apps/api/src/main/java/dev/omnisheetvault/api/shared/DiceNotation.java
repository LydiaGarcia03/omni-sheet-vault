package dev.omnisheetvault.api.shared;

import java.util.Optional;

/** Finds dice notation ("2d6") inside free text with a single linear scan. */
public final class DiceNotation {

    /** A die count and face count found in a text; {@code end} is the index just after the faces. */
    public record Found(int count, int faces, int end) {
    }

    private DiceNotation() {
    }

    /** The first "NdM" in the text, if any. */
    public static Optional<Found> first(String text) {
        for (int d = text.indexOf('d'); d >= 0; d = text.indexOf('d', d + 1)) {
            int countStart = digitsBefore(text, d);
            int facesEnd = digitsAfter(text, d + 1);
            if (countStart < d && facesEnd > d + 1) {
                return Optional.of(new Found(
                        Integer.parseInt(text.substring(countStart, d)), Integer.parseInt(text.substring(d + 1, facesEnd)), facesEnd));
            }
        }
        return Optional.empty();
    }

    /** The whole number starting at {@code start}, skipping leading spaces, or empty when there is none. */
    public static Optional<Integer> numberAt(String text, int start) {
        int from = start;
        while (from < text.length() && Character.isWhitespace(text.charAt(from))) {
            from++;
        }
        int end = digitsAfter(text, from);
        return end > from ? Optional.of(Integer.parseInt(text.substring(from, end))) : Optional.empty();
    }

    private static int digitsBefore(String text, int index) {
        int start = index;
        while (start > 0 && isDigit(text.charAt(start - 1))) {
            start--;
        }
        return start;
    }

    private static int digitsAfter(String text, int index) {
        int end = index;
        while (end < text.length() && isDigit(text.charAt(end))) {
            end++;
        }
        return end;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}

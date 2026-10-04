package dev.omnisheetvault.api.catalogue;

import java.util.Locale;

/** Which sources are playtest material: 5etools codes Unearthed Arcana with a {@code UA} prefix. */
final class PlaytestSources {

    private static final String PLAYTEST_PREFIX = "UA";

    private PlaytestSources() {
    }

    static boolean isPlaytest(String sourceCode) {
        return sourceCode != null && sourceCode.toUpperCase(Locale.ROOT).startsWith(PLAYTEST_PREFIX);
    }
}

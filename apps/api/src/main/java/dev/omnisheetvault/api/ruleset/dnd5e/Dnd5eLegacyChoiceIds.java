package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Folds answers stored under the per-level spell choice ids ({@code class:<c>:<n>:cantrips}, {@code :spells},
 * {@code :spellbook}) into the pooled id of their class ({@code class:<c>:cantrips}), in level order. An answer already
 * stored under the pooled id wins over the per-level ones.
 */
final class Dnd5eLegacyChoiceIds {

    private static final Pattern PER_LEVEL_SPELL_ID =
            Pattern.compile("^class:([a-z0-9-]+):(\\d+):(cantrips|spells|spellbook)$");

    private Dnd5eLegacyChoiceIds() {
    }

    static List<Dnd5eBuildChoice> pooled(List<Dnd5eBuildChoice> choices) {
        List<Dnd5eBuildChoice> legacy = choices.stream().filter(choice -> pooledId(choice).isPresent()).toList();
        if (legacy.isEmpty()) {
            return choices;
        }
        Set<String> answeredIds = choices.stream().map(Dnd5eBuildChoice::id).collect(Collectors.toSet());
        Map<String, Set<String>> merged = mergedByPooledId(legacy, answeredIds);
        List<Dnd5eBuildChoice> result = new ArrayList<>();
        for (Dnd5eBuildChoice choice : choices) {
            Optional<String> pooledId = pooledId(choice);
            if (pooledId.isEmpty()) {
                result.add(choice);
            } else if (merged.containsKey(pooledId.get())) {
                result.add(new Dnd5eBuildChoice(pooledId.get(), List.copyOf(merged.remove(pooledId.get()))));
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Set<String>> mergedByPooledId(List<Dnd5eBuildChoice> legacy, Set<String> answeredIds) {
        Map<String, Set<String>> merged = new LinkedHashMap<>();
        legacy.stream()
                .sorted(Comparator.comparingInt(Dnd5eLegacyChoiceIds::levelOf))
                .filter(choice -> !answeredIds.contains(pooledId(choice).orElseThrow()))
                .forEach(choice -> merged.computeIfAbsent(pooledId(choice).orElseThrow(), id -> new LinkedHashSet<>())
                        .addAll(choice.selections()));
        return merged;
    }

    private static int levelOf(Dnd5eBuildChoice choice) {
        Matcher matcher = PER_LEVEL_SPELL_ID.matcher(choice.id());
        return matcher.matches() ? Integer.parseInt(matcher.group(2)) : 0;
    }

    private static Optional<String> pooledId(Dnd5eBuildChoice choice) {
        Matcher matcher = PER_LEVEL_SPELL_ID.matcher(choice.id());
        return matcher.matches() ? Optional.of("class:" + matcher.group(1) + ":" + matcher.group(3)) : Optional.empty();
    }
}

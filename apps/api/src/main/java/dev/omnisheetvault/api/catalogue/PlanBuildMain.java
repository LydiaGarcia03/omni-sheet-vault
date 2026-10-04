package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.registry.CharacterCreationFlowRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Prints the choices a character build offers, read against the ingested content files
 * — no database needed: {@code ./gradlew :apps:api:planBuild --args="--build=<file>"}.
 * Options: {@code --system=<id>} (default {@code dnd-5e}), {@code --all} to include
 * answered choices as well as pending ones, {@code --materialize} to print the sheet a
 * fully answered build produces instead.
 */
public final class PlanBuildMain {

    private static final String DEFAULT_SYSTEM = "dnd-5e";

    private PlanBuildMain() {
    }

    /** A build file ({@code content/<system>/builds/*.json}) wraps the build with the character's name; a bare build is read as is. */
    private static String unwrapped(String json, ObjectMapper objectMapper) {
        JsonNode node = objectMapper.readTree(json);
        return node.has("build") ? objectMapper.writeValueAsString(node.get("build")) : json;
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> options = parseArgs(args);
        String buildFile = options.get("build");
        if (buildFile == null) {
            throw new IllegalArgumentException("Usage: --build=<path to build json> [--system=<id>] [--all]");
        }
        String systemId = options.getOrDefault("system", DEFAULT_SYSTEM);
        ObjectMapper objectMapper = new ObjectMapper();
        ContentDirectoryCatalogue catalogue = new ContentDirectoryCatalogue(Path.of("content", systemId), objectMapper);
        CharacterCreationFlow flow = CharacterCreationFlowRegistry.standalone(objectMapper).forSystem(systemId);
        String buildJson = unwrapped(withoutByteOrderMark(Files.readString(Path.of(buildFile))), objectMapper);
        if (options.containsKey("materialize")) {
            printMaterialized(flow.materialize(null, buildJson, catalogue), objectMapper);
            return;
        }
        BuildPlan plan = flow.plan(buildJson, catalogue);

        List<CreationChoice> shown = options.containsKey("all") ? plan.choices() : plan.pending();
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("answered", plan.choices().size() - plan.pending().size());
        output.put("pending", plan.pending().size());
        output.put("problems", plan.problems());
        output.put("choices", shown);
        System.out.println(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(output));
    }

    /** The sheet a fully answered build produces, or the plan explaining why it can't yet. */
    private static void printMaterialized(MaterializedSheet materialized, ObjectMapper objectMapper) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("materialized", materialized.isMaterialized());
        output.put("problems", materialized.plan().problems());
        output.put("pending", materialized.plan().pending().stream().map(CreationChoice::id).toList());
        output.put("startingItems", materialized.startingItems());
        output.put("startingCopper", materialized.startingCopper());
        output.put("sheet", materialized.sheetJson() == null ? null : objectMapper.readTree(materialized.sheetJson()));
        System.out.println(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(output));
    }

    /** Windows editors often save UTF-8 with a byte-order mark, which JSON parsers reject. */
    private static String withoutByteOrderMark(String content) {
        return !content.isEmpty() && content.charAt(0) == '﻿' ? content.substring(1) : content;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                continue;
            }
            int equals = arg.indexOf('=');
            if (equals > 0) {
                options.put(arg.substring(2, equals), arg.substring(equals + 1));
            } else {
                options.put(arg.substring(2), "true");
            }
        }
        return options;
    }
}

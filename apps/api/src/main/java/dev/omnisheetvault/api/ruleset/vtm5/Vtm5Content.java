package dev.omnisheetvault.api.ruleset.vtm5;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** VtM 5e's curated content (clans, disciplines, predator types, advantages…): short summaries, owner-curated. */
@Component
public class Vtm5Content {

    private final JsonNode root;

    public Vtm5Content(ObjectMapper objectMapper) {
        try (InputStream in = getClass().getResourceAsStream("/ruleset/vtm5/vtm5-content.json")) {
            this.root = objectMapper.readTree(in);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public JsonNode all() {
        return root;
    }

    List<String> attributes() {
        return fieldNames(root.path("attributes"));
    }

    List<String> skills() {
        return fieldNames(root.path("skills"));
    }

    String groupOf(String trait) {
        JsonNode node = root.path("attributes").has(trait) ? root.path("attributes").path(trait) : root.path("skills").path(trait);
        return node.path("group").asString("");
    }

    List<String> freeSpecialtySkills() {
        List<String> skills = new ArrayList<>();
        root.path("freeSpecialtySkills").forEach(skill -> skills.add(skill.asString()));
        return skills;
    }

    /** The distribution's slots, dots → how many skills. */
    Map<Integer, Integer> distribution(String name) {
        Map<Integer, Integer> slots = new TreeMap<>();
        root.path("distributions").path(name).path("slots").properties()
                .forEach(entry -> slots.put(Integer.parseInt(entry.getKey()), entry.getValue().asInt()));
        return slots;
    }

    Optional<JsonNode> clan(String name) {
        return find(root.path("clans"), name);
    }

    Optional<JsonNode> predator(String name) {
        return find(root.path("predators"), name);
    }

    Optional<JsonNode> age(String name) {
        JsonNode age = root.path("ages").path(name);
        return age.isMissingNode() ? Optional.empty() : Optional.of(age);
    }

    Optional<JsonNode> discipline(String name) {
        JsonNode discipline = root.path("disciplines").path(name);
        return discipline.isMissingNode() ? Optional.empty() : Optional.of(discipline);
    }

    List<String> disciplineNames() {
        return fieldNames(root.path("disciplines"));
    }

    Optional<JsonNode> loresheet(String name) {
        return find(root.path("loresheets"), name);
    }

    boolean isThinBloodMerit(String name) {
        return find(root.path("thinBloodMerits"), name).isPresent();
    }

    boolean isThinBloodFlaw(String name) {
        return find(root.path("thinBloodFlaws"), name).isPresent();
    }

    boolean isAlchemyMethod(String name) {
        return find(root.path("alchemyMethods"), name).isPresent();
    }

    Optional<JsonNode> ritual(String name) {
        return find(root.path("rituals"), name);
    }

    Optional<JsonNode> advantage(String name) {
        return find(root.path("advantages"), name);
    }

    List<String> sorceryClans() {
        List<String> clans = new ArrayList<>();
        root.path("sorceryClans").forEach(clan -> clans.add(clan.asString()));
        return clans;
    }

    JsonNode bloodPotencyRow(int bloodPotency) {
        return root.path("bpTable").path(Math.clamp(bloodPotency, 0, root.path("bpTable").size() - 1));
    }

    private static Optional<JsonNode> find(JsonNode list, String name) {
        for (JsonNode entry : list) {
            if (entry.path("name").asString("").equals(name)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.propertyNames().forEach(names::add);
        return names;
    }
}

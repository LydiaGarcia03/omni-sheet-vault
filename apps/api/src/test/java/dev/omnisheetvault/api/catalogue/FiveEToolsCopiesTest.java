package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** Fixtures follow the shapes real races.json entries use (Centaur, Goblin, Dragonborn). */
class FiveEToolsCopiesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void aCopyInheritsWhatItDoesNotDefineAndAppliesItsEntryMods() {
        JsonNode target = json("""
                {"name":"Goblin","source":"VGM","page":119,"srd":true,"size":["S"],"speed":30,
                 "entries":[{"name":"Age","entries":["Old."]},{"name":"Alignment","entries":["Evil."]},{"name":"Nimble","entries":["Fast."]}]}""");
        JsonNode copy = json("""
                {"name":"Goblin","source":"ERLW","page":26,"_copy":{"name":"Goblin","source":"VGM","_mod":{"entries":[
                  {"mode":"replaceArr","replace":"Alignment","items":{"name":"Alignment","entries":["Neutral."]}},
                  {"mode":"removeArr","names":"Age"},
                  {"mode":"appendArr","items":{"name":"Languages","entries":["Goblin."]}},
                  {"mode":"prependArr","items":{"name":"Intro","entries":["Hi."]}},
                  {"mode":"replaceTxt","replace":"fast","with":"quick","flags":"i"}]}}}""");

        ObjectNode resolved = FiveEToolsCopies.resolveCopy(copy, target);

        assertThat(resolved.get("source").asString()).isEqualTo("ERLW");
        assertThat(resolved.get("page").asInt()).isEqualTo(26);
        assertThat(resolved.get("speed").asInt()).isEqualTo(30);
        assertThat(resolved.get("srd")).isNull();
        assertThat(resolved.get("_copy")).isNull();
        assertThat(resolved.get("entries")).extracting(entry -> entry.get("name").asString())
                .containsExactly("Intro", "Alignment", "Nimble", "Languages");
        assertThat(resolved.get("entries").get(1).get("entries").get(0).asString()).isEqualTo("Neutral.");
        assertThat(resolved.get("entries").get(2).get("entries").get(0).asString()).isEqualTo("quick.");
    }

    @Test
    void expandsNamedAndTemplatedVersions() {
        JsonNode base = json("""
                {"name":"Dragonborn","source":"PHB","entries":[{"name":"Draconic Ancestry","entries":["Pick."]},{"name":"Breath","entries":["x"]}],
                 "_versions":[
                  {"name":"Dragonborn; Plain","source":"PHB","_mod":{"entries":{"mode":"removeArr","names":"Draconic Ancestry"}}},
                  {"_abstract":{"name":"Dragonborn ({{color}})","source":"PHB","_mod":{"entries":[
                     {"mode":"removeArr","names":"Draconic Ancestry"},
                     {"mode":"replaceArr","replace":"Breath","items":{"name":"Breath","entries":["{{damageType}} breath"]}}]}},
                   "_implementations":[{"_variables":{"color":"Black","damageType":"acid"},"resist":["acid"]},
                                       {"_variables":{"color":"Red","damageType":"fire"},"resist":["fire"]}]}]}""");

        List<ObjectNode> versions = FiveEToolsCopies.expandVersions(base);

        assertThat(versions).extracting(version -> version.get("name").asString())
                .containsExactly("Dragonborn; Plain", "Dragonborn (Black)", "Dragonborn (Red)");
        assertThat(versions.get(1).get("entries").get(0).get("entries").get(0).asString()).isEqualTo("acid breath");
        assertThat(versions.get(2).get("resist").get(0).asString()).isEqualTo("fire");
        assertThat(versions.get(1).get("_versions")).isNull();
    }

    @Test
    void failsOnAModeOrTargetOutsideTheSupportedSubset() {
        JsonNode target = json("""
                {"name":"A","source":"PHB","entries":[]}""");

        assertThatThrownBy(() -> FiveEToolsCopies.resolveCopy(json("""
                {"name":"B","source":"X","_copy":{"name":"A","source":"PHB","_mod":{"entries":{"mode":"scalarAddProp","prop":"x"}}}}"""), target))
                .isInstanceOf(FiveEToolsIngestException.class).hasMessageContaining("scalarAddProp");
        assertThatThrownBy(() -> FiveEToolsCopies.resolveCopy(json("""
                {"name":"B","source":"X","_copy":{"name":"A","source":"PHB","_mod":{"ability":{"mode":"appendArr","items":1}}}}"""), target))
                .isInstanceOf(FiveEToolsIngestException.class).hasMessageContaining("ability");
        assertThatThrownBy(() -> FiveEToolsCopies.resolveCopy(json("""
                {"name":"B","source":"X","_copy":{"name":"A","source":"PHB","_mod":{"entries":{"mode":"removeArr","names":"Missing"}}}}"""), target))
                .isInstanceOf(FiveEToolsIngestException.class).hasMessageContaining("Missing");
    }

    private JsonNode json(String text) {
        return objectMapper.readTree(text);
    }
}

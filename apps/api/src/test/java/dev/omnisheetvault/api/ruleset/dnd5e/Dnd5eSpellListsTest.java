package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class Dnd5eSpellListsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Dnd5eSpellLists lists = new Dnd5eSpellLists(catalogue(
            spell("blade-of-disaster-frhof", "Blade of Disaster", "Forgotten Realms: Heroes of Faerûn", "FRHoF", 9),
            spell("blade-of-disaster-tce", "Blade of Disaster", "Tasha's Cauldron of Everything", "TCE", 9),
            spell("sleep", "Sleep", "Player's Handbook", "PHB", 1),
            spell("sleep-xge", "Sleep", "Xanathar's Guide to Everything", "XGE", 1)));

    @Test
    void aReferenceWithASourceCodePicksThatBook() {
        assertThat(lists.byReference("blade of disaster|tce")).map(CatalogueRecord::slug).contains("blade-of-disaster-tce");
    }

    @Test
    void aBareReferencePrefersThePlayersHandbook() {
        assertThat(lists.byReference("sleep")).map(CatalogueRecord::slug).contains("sleep");
    }

    @Test
    void theSourceFilterMatchesTheFiveEToolsCode() {
        assertThat(lists.matching("source=XGE")).extracting(CatalogueRecord::slug).containsExactly("sleep-xge");
    }

    private static CatalogueRecord spell(String slug, String name, String book, String code, int level) {
        return new CatalogueRecord("SPELL", slug, name, book, 1, "", MAPPER.readTree("""
                {"level":%d,"school":"evocation","sourceCode":"%s","classes":[],"optionalClasses":[]}""".formatted(level, code)));
    }

    private static CatalogueLookup catalogue(CatalogueRecord... spells) {
        return new CatalogueLookup() {
            @Override
            public Optional<CatalogueRecord> find(String kind, String slug) {
                return List.of(spells).stream().filter(spell -> spell.slug().equals(slug)).findFirst();
            }

            @Override
            public List<CatalogueRecord> list(String kind) {
                return List.of(spells);
            }
        };
    }
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.StartingEquipmentPreview;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class Dnd5eStartingEquipmentPreviewTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, CatalogueRecord> ITEMS = Map.of(
            "chain-mail", item("chain-mail", "Chain Mail", "ARMOR"),
            "shield", item("shield", "Shield", "SHIELD"),
            "longsword", item("longsword", "Longsword", "WEAPON"),
            "explorers-pack", item("explorers-pack", "Explorer's Pack", "GEAR"));
    private static final CatalogueLookup CATALOGUE = new CatalogueLookup() {
        @Override
        public Optional<CatalogueRecord> find(String kind, String slug) {
            return "ITEM".equals(kind) ? Optional.ofNullable(ITEMS.get(slug)) : Optional.empty();
        }

        @Override
        public List<CatalogueRecord> list(String kind) {
            return List.of();
        }
    };

    @Test
    void listsEachStartingItemWithHowItCanStartEquippedAndSplitsTheMoney() {
        Dnd5eBuildOutcome outcome = new Dnd5eBuildOutcome();
        outcome.addStartingItem("chain-mail", null, 1, null);
        outcome.addStartingItem("shield", null, 1, null);
        outcome.addStartingItem("longsword", null, 1, null);
        outcome.addStartingItem("explorers-pack", null, 1, null);
        outcome.addStartingItem(null, "insignia of rank", 1, null);
        outcome.addStartingCopper(1234);
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(null, null, null, null, null, null, Map.of(), null, null, null, null, null,
                List.of("chain-mail#0", "longsword#0"));

        StartingEquipmentPreview preview = Dnd5eCharacterCreationFlow.startingEquipment(build, outcome, CATALOGUE);

        assertThat(preview.inventory())
                .extracting(StartingEquipmentPreview.Entry::key, StartingEquipmentPreview.Entry::name,
                        StartingEquipmentPreview.Entry::equipAction, StartingEquipmentPreview.Entry::equipped)
                .containsExactly(
                        tuple("chain-mail#0", "Chain Mail", "WEAR", true),
                        tuple("shield#0", "Shield", "WEAR", false),
                        tuple("longsword#0", "Longsword", "WIELD", true),
                        tuple("explorers-pack#0", "Explorer's Pack", null, false),
                        tuple(null, "insignia of rank", null, false));
        assertThat(preview.items()).hasSize(5);
        assertThat(preview.gold()).isEqualTo(12);
        assertThat(preview.silver()).isEqualTo(3);
        assertThat(preview.copper()).isEqualTo(4);
    }

    private static CatalogueRecord item(String slug, String name, String itemKind) {
        return new CatalogueRecord("ITEM", slug, name, "Player's Handbook", 1, "", MAPPER.readTree("{\"itemKind\":\"" + itemKind + "\"}"));
    }
}

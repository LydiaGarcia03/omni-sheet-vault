package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.MAPPER;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.json;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class DaggerheartSheetActionsTest {

    private final DaggerheartSheetActions actions = new DaggerheartSheetActions(MAPPER, sides -> 3);
    private ObjectNode sheet;

    /** The valid Ranger: thresholds 7 / 14, 6 HP, 6 Stress, Armor Score 3, Hope 2. */
    @BeforeEach
    void setUp() {
        DaggerheartCharacterCreationFlow flow = new DaggerheartCharacterCreationFlow(MAPPER);
        sheet = (ObjectNode) MAPPER.readTree(flow.materialize(null, json(valid()), new DaggerheartTestCatalogue()).sheetJson());
    }

    private void act(String json) {
        actions.apply(sheet, MAPPER.readTree(json));
    }

    private int tracker(String field) {
        return sheet.path("trackers").path(field).asInt();
    }

    @Test
    void damageMarksOneTwoOrThreeHitPointsByTheThresholds() {
        act("{\"type\":\"damage\",\"amount\":6}");
        assertThat(tracker("hitPointsMarked")).isEqualTo(1);
        act("{\"type\":\"damage\",\"amount\":7}");
        assertThat(tracker("hitPointsMarked")).isEqualTo(3);
        act("{\"type\":\"damage\",\"amount\":14}");
        assertThat(tracker("hitPointsMarked")).isEqualTo(6);
        assertThat(sheet.path("lastDamage").path("fell").asBoolean()).isTrue();
    }

    @Test
    void anArmorSlotDropsTheDamageOneThreshold() {
        act("{\"type\":\"damage\",\"amount\":14,\"useArmor\":true}");

        assertThat(tracker("hitPointsMarked")).isEqualTo(2);
        assertThat(tracker("armorSlotsMarked")).isEqualTo(1);
        assertThat(sheet.path("lastDamage").path("armorUsed").asBoolean()).isTrue();
    }

    @Test
    void stressThatCantBeMarkedMarksAHitPointAndTheLastStressMakesYouVulnerable() {
        act("{\"type\":\"track\",\"track\":\"stress\",\"delta\":6}");
        assertThat(sheet.path("conditions")).extracting(JsonNode::asString).containsExactly("Vulnerable");

        act("{\"type\":\"track\",\"track\":\"stress\",\"delta\":1}");
        assertThat(tracker("stressMarked")).isEqualTo(6);
        assertThat(tracker("hitPointsMarked")).isEqualTo(1);

        act("{\"type\":\"track\",\"track\":\"stress\",\"delta\":-1}");
        assertThat(sheet.path("conditions")).isEmpty();
    }

    @Test
    void trackersStayWithinTheirSlots() {
        act("{\"type\":\"track\",\"track\":\"hope\",\"delta\":9}");
        act("{\"type\":\"track\",\"track\":\"armorSlots\",\"delta\":-2}");

        assertThat(tracker("hope")).isEqualTo(6);
        assertThat(tracker("armorSlotsMarked")).isZero();
    }

    @Test
    void goldCarriesTenHandfulsIntoABag() {
        act("{\"type\":\"gold\",\"handfuls\":12,\"bags\":9,\"chests\":0}");

        assertThat(sheet.path("gold").path("handfuls").asInt()).isEqualTo(2);
        assertThat(sheet.path("gold").path("bags").asInt()).isZero();
        assertThat(sheet.path("gold").path("chests").asInt()).isEqualTo(1);
    }

    @Test
    void recallingACardFromTheVaultCostsItsRecallCostInStressUnlessItsFree() {
        ArrayNode cards = (ArrayNode) sheet.path("domainCards");
        cards.addObject().put("slug", "vault-card").put("name", "Vault Card").put("recallCost", 2).put("inLoadout", false);

        act("{\"type\":\"moveCard\",\"slug\":\"vault-card\",\"to\":\"loadout\"}");
        assertThat(tracker("stressMarked")).isEqualTo(2);

        act("{\"type\":\"moveCard\",\"slug\":\"vault-card\",\"to\":\"vault\"}");
        act("{\"type\":\"moveCard\",\"slug\":\"vault-card\",\"to\":\"loadout\",\"free\":true}");
        assertThat(tracker("stressMarked")).isEqualTo(2);
        assertThat(cards.get(cards.size() - 1).path("inLoadout").asBoolean()).isTrue();
    }

    @Test
    void theLoadoutHoldsFiveCards() {
        ArrayNode cards = (ArrayNode) sheet.path("domainCards");
        for (int i = cards.size(); i < 5; i++) {
            cards.addObject().put("slug", "card-" + i).put("recallCost", 0).put("inLoadout", true);
        }
        cards.addObject().put("slug", "sixth").put("recallCost", 0).put("inLoadout", false);

        assertThatThrownBy(() -> act("{\"type\":\"moveCard\",\"slug\":\"sixth\",\"to\":\"loadout\"}")).isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void aShortRestClearsOneD4PlusTierAndPrepareGainsHope() {
        ((ObjectNode) sheet.get("trackers")).put("hitPointsMarked", 5).put("hope", 2);

        act("{\"type\":\"rest\",\"kind\":\"short\",\"moves\":[{\"move\":\"tendToWounds\"},{\"move\":\"prepare\",\"withParty\":true}]}");

        assertThat(tracker("hitPointsMarked")).isEqualTo(1);
        assertThat(tracker("hope")).isEqualTo(4);
        assertThat(sheet.path("lastRest").path("moves").path(0).path("rolled").asInt()).isEqualTo(3);
        assertThat(sheet.path("shortRestsInARow").asInt()).isEqualTo(1);
    }

    @Test
    void afterThreeShortRestsTheNextMustBeLong() {
        for (int i = 0; i < 3; i++) {
            act("{\"type\":\"rest\",\"kind\":\"short\",\"moves\":[]}");
        }

        assertThatThrownBy(() -> act("{\"type\":\"rest\",\"kind\":\"short\",\"moves\":[]}")).isInstanceOf(InvalidBuildException.class);
        ((ObjectNode) sheet.get("trackers")).put("stressMarked", 4);
        act("{\"type\":\"rest\",\"kind\":\"long\",\"moves\":[{\"move\":\"clearAllStress\"}]}");
        assertThat(tracker("stressMarked")).isZero();
        assertThat(sheet.path("shortRestsInARow").asInt()).isZero();
    }

    @Test
    void aMoveForAnAllyLeavesThisSheetAlone() {
        ((ObjectNode) sheet.get("trackers")).put("hitPointsMarked", 3);

        act("{\"type\":\"rest\",\"kind\":\"long\",\"moves\":[{\"move\":\"tendToAllWounds\",\"forAlly\":true}]}");

        assertThat(tracker("hitPointsMarked")).isEqualTo(3);
    }

    @Test
    void countersTrackClassResourcesWithinTheirMaximum() {
        act("{\"type\":\"counter\",\"name\":\"Prayer Dice\"}");
        act("{\"type\":\"counter\",\"index\":0,\"max\":3,\"value\":5}");

        assertThat(sheet.path("counters").path(0).path("value").asInt()).isEqualTo(3);
        act("{\"type\":\"counter\",\"index\":0,\"remove\":true}");
        assertThat(sheet.path("counters")).isEmpty();
    }

    @Test
    void anUnknownActionIsRejected() {
        assertThatThrownBy(() -> act("{\"type\":\"teleport\"}")).isInstanceOf(InvalidBuildException.class);
    }
}

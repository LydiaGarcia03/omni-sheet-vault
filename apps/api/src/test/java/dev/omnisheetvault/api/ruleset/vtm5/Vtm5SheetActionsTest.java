package dev.omnisheetvault.api.ruleset.vtm5;

import static dev.omnisheetvault.api.ruleset.vtm5.Vtm5TestBuilds.CONTENT;
import static dev.omnisheetvault.api.ruleset.vtm5.Vtm5TestBuilds.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

class Vtm5SheetActionsTest {

    private final Vtm5SheetActions actions = new Vtm5SheetActions(CONTENT);
    private ObjectNode sheet;

    @BeforeEach
    void newSheet() {
        sheet = Vtm5TestBuilds.validSheet();
    }

    private void apply(String json) {
        actions.apply(sheet, MAPPER.readTree(json));
    }

    @Test
    void superficialDamageOnAFullTrackBecomesAggravated() {
        for (int i = 0; i < 5; i++) {
            apply("{\"type\":\"damage\",\"track\":\"health\",\"kind\":\"superficial\"}");
        }
        apply("{\"type\":\"damage\",\"track\":\"health\",\"kind\":\"superficial\"}");
        apply("{\"type\":\"damage\",\"track\":\"health\",\"kind\":\"aggravated\",\"amount\":-1}");

        assertThat(sheet.path("health").path("superficial").asInt()).isEqualTo(4);
        assertThat(sheet.path("health").path("aggravated").asInt()).isZero();
    }

    @Test
    void damageOnlyGoesToHealthOrWillpower() {
        apply("{\"type\":\"damage\",\"track\":\"willpower\",\"kind\":\"aggravated\"}");

        assertThat(sheet.path("willpower").path("aggravated").asInt()).isEqualTo(1);
        assertThatThrownBy(() -> apply("{\"type\":\"damage\",\"track\":\"humanity\"}")).isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void trackersStayInTheirRanges() {
        apply("{\"type\":\"hunger\",\"value\":9}");
        apply("{\"type\":\"stains\",\"value\":5}");
        apply("{\"type\":\"humanity\",\"value\":7}");

        assertThat(sheet.path("hunger").asInt()).isEqualTo(5);
        assertThat(sheet.path("stains").asInt()).isEqualTo(2);
        assertThat(sheet.path("humanity").asInt()).isEqualTo(7);
    }

    @Test
    void editsNotesResonanceAndTheEditableIdentityFields() {
        apply("{\"type\":\"notes\",\"value\":\"Met the Prince\"}");
        apply("{\"type\":\"resonance\",\"value\":\"Sanguine\"}");
        apply("{\"type\":\"identity\",\"field\":\"ambition\",\"value\":\"Prince\"}");

        assertThat(sheet.path("notes").asString()).isEqualTo("Met the Prince");
        assertThat(sheet.path("resonance").asString()).isEqualTo("Sanguine");
        assertThat(sheet.path("identity").path("ambition").asString()).isEqualTo("Prince");
        assertThatThrownBy(() -> apply("{\"type\":\"identity\",\"field\":\"clan\",\"value\":\"Ventrue\"}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"sing\"}")).isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void convictionsAreEditedAddedUpToThreeAndRemoved() {
        apply("{\"type\":\"conviction\",\"index\":0,\"touchstone\":\"Tomás\"}");
        apply("{\"type\":\"conviction\",\"index\":-1,\"conviction\":\"Protect artists\"}");
        apply("{\"type\":\"conviction\",\"index\":-1}");

        assertThat(sheet.path("convictions")).hasSize(3);
        assertThat(sheet.path("convictions").get(0).path("touchstone").asString()).isEqualTo("Tomás");
        assertThatThrownBy(() -> apply("{\"type\":\"conviction\",\"index\":-1}")).isInstanceOf(InvalidBuildException.class);

        apply("{\"type\":\"conviction\",\"index\":2,\"remove\":true}");
        assertThat(sheet.path("convictions")).hasSize(2);
    }

    @Test
    void spendsXpWithTheCoreCostsAndRefusesWhatItCantPay() {
        apply("{\"type\":\"spendXp\",\"target\":\"skill\",\"key\":\"Brawl\"}");
        apply("{\"type\":\"spendXp\",\"target\":\"attribute\",\"key\":\"Composure\"}");

        assertThat(sheet.path("skills").path("Brawl").asInt()).isEqualTo(1);
        assertThat(sheet.path("attributes").path("Composure").asInt()).isEqualTo(2);
        assertThat(Vtm5SheetActions.available(sheet)).isEqualTo(15 - 3 - 10);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"attribute\",\"key\":\"Intelligence\"}"))
                .isInstanceOf(InvalidBuildException.class).hasMessageContaining("Not enough XP");
    }

    @Test
    void disciplinesCostMoreOutsideTheClan() {
        apply("{\"type\":\"awardXp\",\"amount\":40,\"note\":\"Story\"}");
        ObjectNode costs = actions.nextCosts(sheet, MAPPER.createObjectNode());

        assertThat(costs.path("discipline").path("Presence").asInt()).isEqualTo(5);
        assertThat(costs.path("discipline").path("Potence").asInt()).isEqualTo(7);
        assertThat(costs.path("discipline").path("Auspex").asInt()).isEqualTo(20);
        assertThat(costs.path("bloodPotency").asInt()).isEqualTo(20);
        assertThat(costs.path("skill").path("Brawl").asInt()).isEqualTo(3);

        apply("{\"type\":\"spendXp\",\"target\":\"discipline\",\"key\":\"Potence\"}");
        apply("{\"type\":\"learnPower\",\"discipline\":\"Potence\",\"power\":\"Lethal Body\"}");
        assertThat(sheet.path("disciplines").path("Potence").path("powers").get(0).asString()).isEqualTo("Lethal Body");
        assertThatThrownBy(() -> apply("{\"type\":\"learnPower\",\"discipline\":\"Potence\",\"power\":\"Soaring Leap\"}"))
                .isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"learnPower\",\"discipline\":\"Dominate\",\"power\":\"Compel\"}"))
                .isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void addsASpecialtyForXpWhileThereIsADotForIt() {
        apply("{\"type\":\"spendXp\",\"target\":\"specialty\",\"key\":\"Etiquette\",\"specialty\":\"Elysium\"}");

        assertThat(sheet.path("specialties").path("Etiquette").get(0).asString()).isEqualTo("Elysium");
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"specialty\",\"key\":\"Brawl\",\"specialty\":\"Kicks\"}"))
                .isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void undoesOnlyTheLatestSpendOnATrait() {
        apply("{\"type\":\"awardXp\",\"amount\":10}");
        apply("{\"type\":\"spendXp\",\"target\":\"skill\",\"key\":\"Brawl\"}");
        apply("{\"type\":\"spendXp\",\"target\":\"skill\",\"key\":\"Brawl\"}");
        int firstSpend = sheet.path("xpLog").size() - 2;

        assertThatThrownBy(() -> apply("{\"type\":\"undoXp\",\"index\":" + firstSpend + "}")).isInstanceOf(InvalidBuildException.class);
        apply("{\"type\":\"undoXp\",\"index\":" + (firstSpend + 1) + "}");

        assertThat(sheet.path("skills").path("Brawl").asInt()).isEqualTo(1);
        assertThat(Vtm5SheetActions.available(sheet)).isEqualTo(25 - 3);
        assertThatThrownBy(() -> apply("{\"type\":\"undoXp\",\"index\":0}")).isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void undoingADisciplineDotDropsItsExtraPowerAndASpecialtyRemovesIt() {
        apply("{\"type\":\"awardXp\",\"amount\":30}");
        apply("{\"type\":\"spendXp\",\"target\":\"discipline\",\"key\":\"Presence\"}");
        apply("{\"type\":\"learnPower\",\"discipline\":\"Presence\",\"power\":\"Awe\"}");
        apply("{\"type\":\"undoXp\",\"index\":" + (sheet.path("xpLog").size() - 1) + "}");
        apply("{\"type\":\"spendXp\",\"target\":\"specialty\",\"key\":\"Etiquette\",\"specialty\":\"Elysium\"}");
        apply("{\"type\":\"undoXp\",\"index\":" + (sheet.path("xpLog").size() - 1) + "}");
        apply("{\"type\":\"spendXp\",\"target\":\"bloodPotency\",\"key\":\"Blood Potency\"}");

        assertThat(sheet.path("disciplines").has("Presence")).isFalse();
        assertThat(sheet.path("specialties").path("Etiquette")).isEmpty();
        assertThat(sheet.path("bloodPotency").asInt()).isEqualTo(2);
    }

    @Test
    void refusesBadAwardsAndUnknownTargets() {
        assertThatThrownBy(() -> apply("{\"type\":\"awardXp\",\"amount\":0}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"humanity\"}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"skill\",\"key\":\"Juggling\"}")).isInstanceOf(InvalidBuildException.class);
    }

    @Test
    void buysMeritsAndBackgroundsWithXpButNotFlaws() {
        apply("{\"type\":\"spendXp\",\"target\":\"advantage\",\"key\":\"Herd\"}");
        apply("{\"type\":\"spendXp\",\"target\":\"advantage\",\"key\":\"Bloodhound\"}");

        assertThat(sheet.path("advantages").findValuesAsString("name")).contains("Bloodhound");
        assertThat(Vtm5SheetActions.available(sheet)).isEqualTo(15 - 3 - 3);
        assertThat(actions.nextCosts(sheet, MAPPER.createObjectNode()).path("advantage").path("Bloodhound").asInt()).isZero();
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"advantage\",\"key\":\"Bloodhound\"}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"advantage\",\"key\":\"Enemy\"}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"advantage\",\"key\":\"Wings\"}")).isInstanceOf(InvalidBuildException.class);

        apply("{\"type\":\"undoXp\",\"index\":" + (sheet.path("xpLog").size() - 1) + "}");
        assertThat(sheet.path("advantages").findValuesAsString("name")).doesNotContain("Bloodhound");
    }

    @Test
    void ritualsCostTheirLevelTimesThreeUpToTheBloodSorceryRating() {
        apply("{\"type\":\"awardXp\",\"amount\":30}");
        assertThat(actions.nextCosts(sheet, MAPPER.createObjectNode()).path("ritual").path("Blood Walk").asInt()).isZero();

        apply("{\"type\":\"spendXp\",\"target\":\"discipline\",\"key\":\"Blood Sorcery\"}");
        apply("{\"type\":\"spendXp\",\"target\":\"ritual\",\"key\":\"Blood Walk\"}");

        assertThat(sheet.path("rituals").get(0).asString()).isEqualTo("Blood Walk");
        assertThat(Vtm5SheetActions.available(sheet)).isEqualTo(45 - 7 - 3);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"ritual\",\"key\":\"Blood Walk\"}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"ritual\",\"key\":\"Firewalker\"}")).isInstanceOf(InvalidBuildException.class);

        apply("{\"type\":\"undoXp\",\"index\":" + (sheet.path("xpLog").size() - 1) + "}");
        assertThat(sheet.path("rituals")).isEmpty();
    }

    @Test
    void keepsAnInventory() {
        apply("{\"type\":\"addItem\",\"name\":\"Restoration kit\",\"quantity\":2}");
        apply("{\"type\":\"updateItem\",\"index\":0,\"notes\":\"In the car\",\"quantity\":0}");

        assertThat(sheet.path("inventory").get(0).path("quantity").asInt()).isEqualTo(1);
        assertThat(sheet.path("inventory").get(0).path("notes").asString()).isEqualTo("In the car");
        assertThatThrownBy(() -> apply("{\"type\":\"addItem\",\"name\":\" \"}")).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> apply("{\"type\":\"removeItem\",\"index\":5}")).isInstanceOf(InvalidBuildException.class);

        apply("{\"type\":\"removeItem\",\"index\":0}");
        assertThat(sheet.path("inventory")).isEmpty();
    }

    @Test
    void buysLoresheetEntriesForTheirLevelTimesThree() {
        apply("{\"type\":\"spendXp\",\"target\":\"loresheet\",\"key\":\"Descendant of Helena\",\"level\":2}");

        assertThat(sheet.path("loresheets").path("Descendant of Helena").get(0).asInt()).isEqualTo(2);
        assertThat(Vtm5SheetActions.available(sheet)).isEqualTo(9);
        assertThat(actions.nextCosts(sheet, MAPPER.createObjectNode()).path("loresheet").path("Descendant of Helena").get(1).asInt()).isZero();
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"loresheet\",\"key\":\"Descendant of Tyler\",\"level\":1}"))
                .isInstanceOf(InvalidBuildException.class);

        apply("{\"type\":\"undoXp\",\"index\":" + (sheet.path("xpLog").size() - 1) + "}");
        assertThat(sheet.path("loresheets").has("Descendant of Helena")).isFalse();
    }

    @Test
    void onlyThinBloodsPractiseAlchemy() {
        apply("{\"type\":\"awardXp\",\"amount\":20}");
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"discipline\",\"key\":\"Thin-Blood Alchemy\"}")).isInstanceOf(InvalidBuildException.class);

        ((ObjectNode) sheet.path("identity")).put("clan", "Thin-blooded");
        assertThatThrownBy(() -> apply("{\"type\":\"spendXp\",\"target\":\"discipline\",\"key\":\"Potence\"}")).isInstanceOf(InvalidBuildException.class);
        apply("{\"type\":\"spendXp\",\"target\":\"discipline\",\"key\":\"Thin-Blood Alchemy\"}");
        assertThat(sheet.path("disciplines").path("Thin-Blood Alchemy").path("dots").asInt()).isEqualTo(1);
    }

    @Test
    void theCoterieSpendsNoMoreThanItsPool() {
        apply("{\"type\":\"coterie\",\"name\":\"The Gallery\",\"type2\":\"x\",\"members\":[\"Tomás\",\" \",\"Ines\"],\"othersDonated\":1}");
        assertThat(Vtm5SheetActions.coteriePool(sheet.path("coterie"))).isEqualTo(4);

        apply("{\"type\":\"coterie\",\"domain\":{\"Chasse\":2,\"Lien\":1},\"backgrounds\":[{\"name\":\"Haven\",\"dots\":1}]}");
        assertThat(sheet.path("coterie").path("name").asString()).isEqualTo("The Gallery");
        assertThat(Vtm5SheetActions.coterieSpent(sheet.path("coterie"))).isEqualTo(4);

        assertThatThrownBy(() -> apply("{\"type\":\"coterie\",\"domain\":{\"Portillon\":1}}"))
                .isInstanceOf(InvalidBuildException.class).hasMessageContaining("pool is 4");
        assertThatThrownBy(() -> apply("{\"type\":\"coterie\",\"backgrounds\":[{\"name\":\"Beautiful\",\"dots\":1}]}"))
                .isInstanceOf(InvalidBuildException.class);
    }
}

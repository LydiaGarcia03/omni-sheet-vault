package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Synthetic tag examples following 5etools' own Renderer.stripTags rules — not real book text. */
class TagMarkupStripperTest {

    @Test
    void leavesPlainTextUnchanged() {
        assertThat(TagMarkupStripper.strip("A mote of fire streaks toward a target.")).isEqualTo("A mote of fire streaks toward a target.");
    }

    @Test
    void rendersTheSinglePayloadWhenNoDisplaySegmentIsGiven() {
        assertThat(TagMarkupStripper.strip("Deals {@damage 1d10} fire damage.")).isEqualTo("Deals 1d10 fire damage.");
    }

    @Test
    void rendersTheNameNotTheSourceForAnEntityTag() {
        assertThat(TagMarkupStripper.strip("See {@spell fireball|xge}.")).isEqualTo("See fireball.");
    }

    @Test
    void rendersTheThirdSegmentAsDisplayTextForAnEntityTag() {
        assertThat(TagMarkupStripper.strip("Wield {@item dagger|phb|daggers}.")).isEqualTo("Wield daggers.");
        assertThat(TagMarkupStripper.strip("the {@class fighter|phb|Battle Master|Battle Master|phb|2-0} archetype"))
                .isEqualTo("the Battle Master archetype");
    }

    @Test
    void fallsBackToTheNameWhenTheDisplaySegmentIsEmpty() {
        assertThat(TagMarkupStripper.strip("The target is {@condition blinded|PHB|}.")).isEqualTo("The target is blinded.");
    }

    @Test
    void usesTheFourthSegmentForCardsAndDeities() {
        assertThat(TagMarkupStripper.strip("{@card Flames|Deck of Many Things|DMG|the Flames card}")).isEqualTo("the Flames card");
    }

    @Test
    void rendersDiceTagsWithTheirOwnSegmentOrder() {
        assertThat(TagMarkupStripper.strip("{@dice 4d4 × 10|4d4 × 10|Starting Gold}")).isEqualTo("4d4 × 10");
        assertThat(TagMarkupStripper.strip("{@hit 5} to hit, {@dc 15} save")).isEqualTo("+5 to hit, DC 15 save");
        assertThat(TagMarkupStripper.strip("a {@chance 50} chance")).isEqualTo("a 50 percent chance");
    }

    @Test
    void rendersAttackAndStandaloneTags() {
        assertThat(TagMarkupStripper.strip("{@atk mw} {@h}7 damage.")).isEqualTo("Melee Weapon Attack: Hit: 7 damage.");
    }

    @Test
    void rendersTheDisplayTextOfAQuickReference() {
        assertThat(TagMarkupStripper.strip("{@quickref Cover||3||half cover}")).isEqualTo("half cover");
        assertThat(TagMarkupStripper.strip("{@quickref difficult terrain||3}")).isEqualTo("difficult terrain");
    }

    @Test
    void failsOnATagWithNoKnownRule() {
        assertThatThrownBy(() -> TagMarkupStripper.strip("{@mystery thing}"))
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("@mystery");
    }

    @Test
    void resolvesNestedTagsFromTheInsideOut() {
        assertThat(TagMarkupStripper.strip("{@b Cast time: {@i 1 action}}")).isEqualTo("Cast time: 1 action");
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(TagMarkupStripper.strip(null)).isNull();
    }

    @Test
    void rendersThePerLevelIncrementForScaledamageNotTheBaseOrTheLevelRange() {
        assertThat(TagMarkupStripper.strip("the damage increases by {@scaledamage 3d10|1-9|1d10} for each slot level above 1st"))
                .isEqualTo("the damage increases by 1d10 for each slot level above 1st");
    }

    @Test
    void rendersThePerLevelIncrementForScaledice() {
        assertThat(TagMarkupStripper.strip("the healing increases by {@scaledice 1d8|1-9|1d8} for each slot level above 1st"))
                .isEqualTo("the healing increases by 1d8 for each slot level above 1st");
    }

    @Test
    void rendersTheNameSegmentForVariantruleNotTheSourceCode() {
        assertThat(TagMarkupStripper.strip("see {@variantrule Hit Point Dice|XPHB}")).isEqualTo("see Hit Point Dice");
    }
}

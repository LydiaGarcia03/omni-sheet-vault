package dev.omnisheetvault.api.dice;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A roll made while building a character (an ability score, a level's hit points): the dice the
 * build plan asked for, the highest dice that count ({@code keepHighest}, null = all), and the
 * label the roll history shows. The result is still resolved here, never reported by the client.
 */
record CreationRollRequest(
        @NotNull DieType die,
        @Min(1) @Max(20) int count,
        @Min(1) Integer keepHighest,
        @NotBlank @Size(max = 120) String context) {

    @AssertTrue(message = "keepHighest can't exceed count")
    boolean isKeepWithinCount() {
        return keepHighest == null || keepHighest <= count;
    }
}

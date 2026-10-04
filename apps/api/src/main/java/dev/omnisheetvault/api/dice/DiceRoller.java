package dev.omnisheetvault.api.dice;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/** The only place a random number is generated — everything else works with the result. */
@Component
class DiceRoller {

    int[] roll(int diceCount, int diceSides) {
        int[] results = new int[diceCount];
        for (int i = 0; i < diceCount; i++) {
            results[i] = ThreadLocalRandom.current().nextInt(1, diceSides + 1);
        }
        return results;
    }
}

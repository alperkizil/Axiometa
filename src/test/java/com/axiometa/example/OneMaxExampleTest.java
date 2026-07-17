package com.axiometa.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.axiometa.bitstring.BitString;
import com.axiometa.phase.AlgorithmState;
import org.junit.jupiter.api.Test;

class OneMaxExampleTest {

    @Test
    void reachesTheOptimumWithinTheIterationBudget() {
        AlgorithmState<BitString> finalState = OneMaxExample.run(42L);

        assertEquals(OneMaxExample.BIT_COUNT,
                OneMaxExample.best(finalState.population()).evaluation().objectiveValue(0));
    }

    @Test
    void isReproducibleFromTheRootSeed() {
        assertEquals(OneMaxExample.run(42L), OneMaxExample.run(42L));
    }
}

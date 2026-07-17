package com.axiometa.ga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Population;
import com.axiometa.ga.GaFixtures.TestRep;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class GenerationalReplacementTest {

    private final EvaluatedCandidate<TestRep> low = GaFixtures.member(1, 5.0);
    private final EvaluatedCandidate<TestRep> high = GaFixtures.member(2, 9.0);
    private final EvaluatedCandidate<TestRep> mid = GaFixtures.member(3, 7.0);
    private final Population<TestRep> current = new Population<>(List.of(low, high, mid));

    private final EvaluatedCandidate<TestRep> childA = GaFixtures.member(4, 2.0);
    private final EvaluatedCandidate<TestRep> childB = GaFixtures.member(5, 3.0);
    private final EvaluatedCandidate<TestRep> childC = GaFixtures.member(6, 4.0);

    @Test
    void keepsTheBestElitesThenFillsWithOffspringInOrder() {
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1);

        Population<TestRep> next = replacement.replace(current, List.of(childA, childB));

        assertEquals(List.of(high, childA, childB), next.members());
    }

    @Test
    void minimizingElitesKeepTheLowestFitness() {
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MINIMIZE, 1);

        Population<TestRep> next = replacement.replace(current, List.of(childA, childB));

        assertEquals(List.of(low, childA, childB), next.members());
    }

    @Test
    void pureGenerationalReplacementTakesOffspringOnly() {
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 0);

        Population<TestRep> next =
                replacement.replace(current, List.of(childA, childB, childC));

        assertEquals(List.of(childA, childB, childC), next.members());
    }

    @Test
    void eliteTiesKeepTheEarlierCurrentMember() {
        EvaluatedCandidate<TestRep> firstTied = GaFixtures.member(1, 4.0);
        EvaluatedCandidate<TestRep> secondTied = GaFixtures.member(2, 4.0);
        Population<TestRep> tied = new Population<>(
                List.of(firstTied, secondTied, GaFixtures.member(3, 1.0)));
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1);

        Population<TestRep> next = replacement.replace(tied, List.of(childA, childB));

        assertEquals(firstTied, next.members().get(0));
    }

    @Test
    void ignoresExcessOffspring() {
        Population<TestRep> pair = new Population<>(List.of(low, high));
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 0);

        Population<TestRep> next =
                replacement.replace(pair, List.of(childA, childB, childC));

        assertEquals(List.of(childA, childB), next.members());
    }

    @Test
    void rejectsTooFewOffspring() {
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> replacement.replace(current, List.of(childA)));
        assertEquals("replacement needs at least 2 offspring but got 1", e.getMessage());
    }

    @Test
    void rejectsElitismCountNotBelowThePopulationSize() {
        Population<TestRep> pair = new Population<>(List.of(low, high));
        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 2);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> replacement.replace(pair, List.of(childA, childB)));
        assertEquals("elitismCount must be smaller than the population size 2 but was 2",
                e.getMessage());
    }

    @Test
    void rejectsInvalidConstructionAndArguments() {
        assertEquals("sense must not be null",
                assertThrows(NullPointerException.class,
                        () -> new GenerationalReplacement<TestRep>(null, 1)).getMessage());
        assertEquals("elitismCount must be >= 0 but was -1",
                assertThrows(IllegalArgumentException.class,
                        () -> new GenerationalReplacement<TestRep>(
                                ObjectiveSense.MAXIMIZE, -1)).getMessage());

        GenerationalReplacement<TestRep> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 0);
        assertEquals("current must not be null",
                assertThrows(NullPointerException.class,
                        () -> replacement.replace(null, List.of(childA))).getMessage());
        assertEquals("offspring must not be null",
                assertThrows(NullPointerException.class,
                        () -> replacement.replace(current, null)).getMessage());
        assertEquals("offspring[0] must not be null",
                assertThrows(NullPointerException.class,
                        () -> replacement.replace(current,
                                Arrays.asList(null, childA, childB))).getMessage());
    }
}

package com.axiometa.phase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.Population;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlgorithmStateTest {

    private final Population<String> population = new Population<>(List.of(
            new EvaluatedCandidate<>(
                    new Candidate<>("a"), new Evaluation(new double[] {1.0}, new double[0]))));

    @Test
    void storesSnapshotValues() {
        AlgorithmState<String> state = new AlgorithmState<>(3, 120L, population);

        assertEquals(3, state.iteration());
        assertEquals(120L, state.evaluationCount());
        assertSame(population, state.population());
    }

    @Test
    void allowsZeroCounts() {
        AlgorithmState<String> state = new AlgorithmState<>(0, 0L, population);

        assertEquals(0, state.iteration());
        assertEquals(0L, state.evaluationCount());
    }

    @Test
    void rejectsNegativeIteration() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new AlgorithmState<>(-1, 0L, population));
        assertEquals("iteration must be >= 0 but was -1", e.getMessage());
    }

    @Test
    void rejectsNegativeEvaluationCount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new AlgorithmState<>(0, -5L, population));
        assertEquals("evaluationCount must be >= 0 but was -5", e.getMessage());
    }

    @Test
    void rejectsNullPopulation() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new AlgorithmState<String>(0, 0L, null));
        assertEquals("population must not be null", e.getMessage());
    }
}

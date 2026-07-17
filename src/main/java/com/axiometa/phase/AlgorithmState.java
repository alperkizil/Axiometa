package com.axiometa.phase;

import com.axiometa.core.Population;
import java.util.Objects;

/**
 * Immutable snapshot of an algorithm run, observed by {@link Termination}
 * conditions.
 *
 * <p>Counts are cumulative for the run: {@code iteration} is the number of
 * completed iterations, {@code evaluationCount} the number of fitness
 * evaluations performed so far.
 *
 * @param <R>             the representation type
 * @param iteration       completed iterations; must be {@code >= 0}
 * @param evaluationCount fitness evaluations performed; must be {@code >= 0}
 * @param population      the current population; must not be null
 */
public record AlgorithmState<R>(int iteration, long evaluationCount, Population<R> population) {

    /**
     * Validates the snapshot.
     *
     * @throws NullPointerException     if {@code population} is null
     * @throws IllegalArgumentException if {@code iteration} or
     *                                  {@code evaluationCount} is negative
     */
    public AlgorithmState {
        if (iteration < 0) {
            throw new IllegalArgumentException("iteration must be >= 0 but was " + iteration);
        }
        if (evaluationCount < 0) {
            throw new IllegalArgumentException(
                    "evaluationCount must be >= 0 but was " + evaluationCount);
        }
        Objects.requireNonNull(population, "population must not be null");
    }
}

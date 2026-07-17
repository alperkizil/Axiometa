package com.axiometa.phase;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import java.util.List;

/**
 * Selects members of a population to become parents.
 *
 * <p>Implementation contract: {@code select} must reject a null population
 * with {@link NullPointerException} and a {@code count < 1} with
 * {@link IllegalArgumentException}, and must return an immutable list of
 * exactly {@code count} members drawn from the given population. Whether the
 * same member may be selected more than once is implementation-defined and
 * must be documented. Stochastic implementations receive their random stream
 * by constructor injection.
 *
 * @param <R> the representation type
 */
public interface Selection<R extends Representation> extends AlgorithmPhase {

    /**
     * Selects parents from a population.
     *
     * @param population the population to select from; must not be null
     * @param count      number of selections to make; must be {@code >= 1}
     * @return immutable list of exactly {@code count} selected members
     * @throws NullPointerException     if {@code population} is null
     * @throws IllegalArgumentException if {@code count < 1}
     */
    List<EvaluatedCandidate<R>> select(Population<R> population, int count);
}

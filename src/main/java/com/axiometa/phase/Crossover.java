package com.axiometa.phase;

import com.axiometa.core.Candidate;
import com.axiometa.core.Representation;

/**
 * Recombines two parent candidates into two child candidates.
 *
 * <p>Implementation contract: {@code crossover} must reject null parents with
 * {@link NullPointerException} and must return a non-null pair. Children
 * equal to their parents are legal (for example when an implementation
 * applies crossover with a probability). Stochastic implementations receive
 * their random stream by constructor injection.
 *
 * @param <R> the representation type
 */
public interface Crossover<R extends Representation> extends AlgorithmPhase {

    /**
     * Recombines two parents.
     *
     * @param first  the first parent; must not be null
     * @param second the second parent; must not be null
     * @return the two children
     * @throws NullPointerException if either parent is null
     */
    OffspringPair<R> crossover(Candidate<R> first, Candidate<R> second);
}

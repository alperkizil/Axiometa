package com.axiometa.phase;

import com.axiometa.core.Candidate;
import com.axiometa.core.Representation;

/**
 * Produces a mutated variant of one candidate.
 *
 * <p>Implementation contract: {@code mutate} must reject a null candidate
 * with {@link NullPointerException} and must return a non-null candidate.
 * Returning a candidate equal to the input is legal (for example when a
 * per-gene mutation probability fires nowhere). Stochastic implementations
 * receive their random stream by constructor injection.
 *
 * @param <R> the representation type
 */
public interface Mutation<R extends Representation> extends AlgorithmPhase {

    /**
     * Mutates one candidate.
     *
     * @param candidate the candidate to mutate; must not be null
     * @return the mutated candidate, possibly equal to the input
     * @throws NullPointerException if {@code candidate} is null
     */
    Candidate<R> mutate(Candidate<R> candidate);
}

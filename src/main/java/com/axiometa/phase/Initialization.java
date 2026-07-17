package com.axiometa.phase;

import com.axiometa.core.Candidate;
import com.axiometa.core.Representation;
import java.util.List;

/**
 * Creates the initial candidates for an algorithm run.
 *
 * <p>Implementation contract: {@code initialize} must reject a
 * {@code populationSize < 1} with {@link IllegalArgumentException}, and must
 * return an immutable list of exactly {@code populationSize} non-null
 * candidates. Stochastic implementations receive their random stream by
 * constructor injection.
 *
 * @param <R> the representation type
 */
public interface Initialization<R extends Representation> extends AlgorithmPhase {

    /**
     * Creates the initial candidates.
     *
     * @param populationSize number of candidates to create; must be
     *                       {@code >= 1}
     * @return immutable list of exactly {@code populationSize} candidates
     * @throws IllegalArgumentException if {@code populationSize < 1}
     */
    List<Candidate<R>> initialize(int populationSize);
}

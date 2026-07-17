package com.axiometa.phase;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import java.util.List;

/**
 * Forms the next generation from the current population and the evaluated
 * offspring.
 *
 * <p>This is the reusable seam for generational and steady-state behavior:
 * a generational policy replaces wholesale, a steady-state policy folds few
 * offspring into the current population, and elitism is a policy detail.
 *
 * <p>Implementation contract: {@code replace} must reject null arguments with
 * {@link NullPointerException} and must return a non-null population. The
 * offspring list may be empty; the resulting population size is
 * implementation-defined and must be documented.
 *
 * @param <R> the representation type
 */
public interface Replacement<R extends Representation> extends AlgorithmPhase {

    /**
     * Forms the next generation.
     *
     * @param current   the current population; must not be null
     * @param offspring the evaluated offspring, possibly empty; must not be
     *                  null or contain nulls
     * @return the next generation
     * @throws NullPointerException if {@code current} or {@code offspring} is
     *                              null
     */
    Population<R> replace(Population<R> current, List<EvaluatedCandidate<R>> offspring);
}

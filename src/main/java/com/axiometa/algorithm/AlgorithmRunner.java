package com.axiometa.algorithm;

import com.axiometa.core.Representation;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.phase.Termination;
import java.util.Objects;

/**
 * The single shared algorithm loop: initialize, then step while the
 * termination condition says continue.
 *
 * <p>The termination condition observes every state, including the one
 * returned by {@code initialize()} — a run may therefore finish with zero
 * steps.
 */
public final class AlgorithmRunner {

    private AlgorithmRunner() {
    }

    /**
     * Runs an algorithm to termination.
     *
     * @param <R>         the representation type
     * @param algorithm   the algorithm to run; must not be null
     * @param termination the stop condition; must not be null
     * @return the final state
     * @throws NullPointerException if an argument is null, or if the
     *                              algorithm returns a null state
     */
    public static <R extends Representation> AlgorithmState<R> run(
            Algorithm<R> algorithm, Termination<R> termination) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(termination, "termination must not be null");

        AlgorithmState<R> state = Objects.requireNonNull(algorithm.initialize(),
                "algorithm returned a null state from initialize()");
        while (!termination.shouldTerminate(state)) {
            state = Objects.requireNonNull(algorithm.step(state),
                    "algorithm returned a null state from step()");
        }
        return state;
    }
}

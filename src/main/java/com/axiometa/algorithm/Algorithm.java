package com.axiometa.algorithm;

import com.axiometa.core.Representation;
import com.axiometa.phase.AlgorithmState;

/**
 * Step-based lifecycle of an optimization algorithm.
 *
 * <p>Implementations are thin orchestrators: they wire phase components
 * (initialization, selection, crossover, mutation, replacement) together and
 * own no phase logic themselves. They also never own a loop or a termination
 * condition — {@link AlgorithmRunner} drives the lifecycle.
 *
 * <p>Implementation contract:
 *
 * <ul>
 *   <li>{@link #initialize()} creates and evaluates generation zero and
 *       returns a state with iteration {@code 0}. It must never return
 *       null.</li>
 *   <li>{@link #step(AlgorithmState)} performs exactly one iteration from
 *       the given state and returns the successor state. It must reject a
 *       null state with {@link NullPointerException} and must never return
 *       null.</li>
 *   <li>The {@code evaluationCount} of returned states reflects the real
 *       evaluation attempts made so far — typically the evaluator's own
 *       counter.</li>
 * </ul>
 *
 * @param <R> the representation type
 */
public interface Algorithm<R extends Representation> {

    /**
     * Creates and evaluates generation zero.
     *
     * @return the initial state, iteration {@code 0}, never null
     */
    AlgorithmState<R> initialize();

    /**
     * Performs one iteration.
     *
     * @param state the state to advance from; must not be null
     * @return the successor state, never null
     * @throws NullPointerException if {@code state} is null
     */
    AlgorithmState<R> step(AlgorithmState<R> state);
}

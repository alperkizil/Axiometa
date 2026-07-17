package com.axiometa.phase;

import com.axiometa.core.Representation;

/**
 * Decides whether an algorithm run should stop.
 *
 * <p>The condition observes an immutable {@link AlgorithmState} snapshot.
 * Implementations may keep internal state across successive calls (for
 * example a future no-improvement-for-k-iterations condition) but must never
 * attempt to influence the run other than through the returned decision.
 * Composable combinators implementing this contract arrive with the
 * algorithm lifecycle slice.
 *
 * <p>Implementation contract: {@code shouldTerminate} must reject a null
 * state with {@link NullPointerException}.
 *
 * @param <R> the representation type
 */
public interface Termination<R extends Representation> extends AlgorithmPhase {

    /**
     * Decides whether to stop.
     *
     * @param state the current run state; must not be null
     * @return {@code true} if the run should stop
     * @throws NullPointerException if {@code state} is null
     */
    boolean shouldTerminate(AlgorithmState<R> state);
}

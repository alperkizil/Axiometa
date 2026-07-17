package com.axiometa.phase;

/**
 * Marker for algorithm phase contracts.
 *
 * <p>All six phase contracts — {@link Initialization}, {@link Selection},
 * {@link Crossover}, {@link Mutation}, {@link Replacement}, and
 * {@link Termination} — extend this interface. It declares no members; it
 * exists so phases share one common ancestor and can be recognized and
 * treated uniformly.
 */
public interface AlgorithmPhase {
}

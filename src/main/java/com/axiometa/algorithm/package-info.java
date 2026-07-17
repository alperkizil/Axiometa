/**
 * Algorithm lifecycle and termination composition.
 *
 * <p>An {@link com.axiometa.algorithm.Algorithm} exposes its execution as
 * {@code initialize()} plus repeatable {@code step(state)} transitions over
 * immutable {@link com.axiometa.phase.AlgorithmState} snapshots. The single
 * shared loop lives in {@link com.axiometa.algorithm.AlgorithmRunner}:
 * initialize, then step while the supplied termination condition says
 * continue — the condition observes every state, including generation zero.
 * Algorithms never own their loop or their termination.
 *
 * <p>{@link com.axiometa.algorithm.Terminations} provides the concrete
 * conditions (evaluation and iteration budgets) and the {@code anyOf} /
 * {@code allOf} combinators. Combinators consult every member condition on
 * every check, so stateful conditions always observe the complete state
 * sequence.
 */
package com.axiometa.algorithm;

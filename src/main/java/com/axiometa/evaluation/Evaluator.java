package com.axiometa.evaluation;

import com.axiometa.core.Candidate;
import java.util.List;

/**
 * Evaluates batches of candidates against the problem the evaluator was
 * constructed for.
 *
 * <p>Implementation contract:
 *
 * <ul>
 *   <li>{@code evaluate} returns an immutable list with exactly one
 *       {@link EvaluationOutcome} per input candidate, in input order,
 *       regardless of internal completion order. An empty batch yields an
 *       empty list.</li>
 *   <li>A null candidate list or a null element is a caller bug: it is
 *       rejected with {@link NullPointerException} before any evaluation
 *       starts, never collected as an outcome.</li>
 *   <li>{@link RuntimeException}s thrown by the problem become
 *       {@link EvaluationOutcome.Failure} outcomes and the batch continues;
 *       JVM {@link Error}s propagate immediately.</li>
 *   <li>{@code evaluationCount} is the cumulative number of evaluation
 *       attempts made against the problem — incremented once per attempted
 *       candidate, whether or not the attempt completed normally. Decorators
 *       that avoid real evaluations must report the count of the evaluator
 *       they wrap.</li>
 * </ul>
 *
 * @param <R> the representation type
 */
public interface Evaluator<R> {

    /**
     * Evaluates a batch of candidates.
     *
     * @param candidates the candidates to evaluate; must not be null or
     *                   contain nulls; may be empty
     * @return immutable list of one outcome per candidate, in input order
     * @throws NullPointerException if {@code candidates} or any element is
     *                              null
     */
    List<EvaluationOutcome<R>> evaluate(List<Candidate<R>> candidates);

    /**
     * Returns the cumulative number of evaluation attempts made against the
     * problem.
     *
     * @return the attempt count, {@code >= 0}
     */
    long evaluationCount();
}

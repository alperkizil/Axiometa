package com.axiometa.evaluation;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import java.util.Objects;

/**
 * Result of attempting to evaluate one candidate: either a
 * {@link Success} carrying the evaluated candidate or a {@link Failure}
 * carrying the candidate and the {@link RuntimeException} that prevented its
 * evaluation.
 *
 * <p>The interface is sealed, so consumers can pattern-match exhaustively
 * over {@code Success} and {@code Failure} without a default branch.
 *
 * @param <R> the representation type
 */
public sealed interface EvaluationOutcome<R> {

    /**
     * Returns the candidate this outcome belongs to, regardless of success
     * or failure.
     *
     * @return the candidate the evaluation was attempted for
     */
    Candidate<R> candidate();

    /**
     * Successful evaluation of one candidate.
     *
     * @param <R>    the representation type
     * @param result the evaluated candidate; must not be null
     */
    record Success<R>(EvaluatedCandidate<R> result) implements EvaluationOutcome<R> {

        /**
         * Validates the outcome.
         *
         * @throws NullPointerException if {@code result} is null
         */
        public Success {
            Objects.requireNonNull(result, "result must not be null");
        }

        @Override
        public Candidate<R> candidate() {
            return result.candidate();
        }
    }

    /**
     * Failed evaluation attempt for one candidate.
     *
     * @param <R>       the representation type
     * @param candidate the candidate whose evaluation failed; must not be null
     * @param cause     the exception that prevented evaluation; must not be
     *                  null
     */
    record Failure<R>(Candidate<R> candidate, RuntimeException cause)
            implements EvaluationOutcome<R> {

        /**
         * Validates the outcome.
         *
         * @throws NullPointerException if {@code candidate} or {@code cause}
         *                              is null
         */
        public Failure {
            Objects.requireNonNull(candidate, "candidate must not be null");
            Objects.requireNonNull(cause, "cause must not be null");
        }
    }
}

package com.axiometa.core;

import java.util.Objects;

/**
 * Pairing of a candidate with the evaluation computed for it.
 *
 * <p>The pairing itself does not verify that the evaluation's objective and
 * constraint counts match any particular problem; whoever produces the pair
 * is responsible for that consistency, as documented on {@link Problem}.
 *
 * @param <R>        the representation type
 * @param candidate  the evaluated candidate; must not be null
 * @param evaluation the evaluation of that candidate; must not be null
 */
public record EvaluatedCandidate<R>(Candidate<R> candidate, Evaluation evaluation) {

    /**
     * Validates the pairing.
     *
     * @throws NullPointerException if {@code candidate} or {@code evaluation}
     *                              is null
     */
    public EvaluatedCandidate {
        Objects.requireNonNull(candidate, "candidate must not be null");
        Objects.requireNonNull(evaluation, "evaluation must not be null");
    }
}

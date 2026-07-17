package com.axiometa.ga;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.ObjectiveSense;
import java.util.Comparator;

/**
 * Single-objective fitness comparison shared by the GA components.
 *
 * <p>Comparisons defensively reject evaluations that are not single-objective
 * and unconstrained; refusing is safer than silently mis-ranking.
 */
final class SingleObjectiveFitness {

    private SingleObjectiveFitness() {
    }

    /**
     * Returns a comparator ordering evaluated candidates best-first for the
     * given sense. The comparator validates every evaluation it touches.
     */
    static <R> Comparator<EvaluatedCandidate<R>> bestFirst(ObjectiveSense sense) {
        Comparator<EvaluatedCandidate<R>> byValue = Comparator.comparingDouble(
                member -> validated(member.evaluation()).objectiveValue(0));
        return sense == ObjectiveSense.MAXIMIZE ? byValue.reversed() : byValue;
    }

    /**
     * Rejects evaluations this package cannot rank.
     *
     * @throws IllegalArgumentException if the evaluation has more than one
     *                                  objective value or any constraint
     *                                  violations
     */
    static Evaluation validated(Evaluation evaluation) {
        if (evaluation.objectiveCount() != 1) {
            throw new IllegalArgumentException(
                    "single-objective components require exactly 1 objective value but found "
                            + evaluation.objectiveCount());
        }
        if (evaluation.constraintViolationCount() != 0) {
            throw new IllegalArgumentException(
                    "constraint handling is not implemented but the evaluation declares "
                            + evaluation.constraintViolationCount() + " constraint violations");
        }
        return evaluation;
    }
}

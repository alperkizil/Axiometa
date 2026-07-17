package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Proves the S1 contracts compose: a minimal test-only {@link Problem}
 * implementation is evaluated through the {@link Candidate} wrapper and
 * produces {@link Evaluation}s consistent with its declarations.
 */
class ProblemContractTest {

    /** Minimizes the value itself, subject to the value being non-negative. */
    private static final class NonNegativeMinimization implements Problem<Double> {

        private static final List<Objective> OBJECTIVES =
                List.of(new Objective("value", ObjectiveSense.MINIMIZE));
        private static final List<Constraint> CONSTRAINTS =
                List.of(new Constraint("non-negative"));

        @Override
        public List<Objective> objectives() {
            return OBJECTIVES;
        }

        @Override
        public List<Constraint> constraints() {
            return CONSTRAINTS;
        }

        @Override
        public Evaluation evaluate(Double representation) {
            Objects.requireNonNull(representation, "representation must not be null");
            double value = representation;
            return new Evaluation(
                    new double[] {value}, new double[] {Math.max(0.0, -value)});
        }

        @Override
        public EvaluationSemantics evaluationSemantics() {
            return EvaluationSemantics.DETERMINISTIC;
        }
    }

    private final NonNegativeMinimization problem = new NonNegativeMinimization();

    @Test
    void declaresObjectivesAndConstraints() {
        assertEquals(List.of(new Objective("value", ObjectiveSense.MINIMIZE)),
                problem.objectives());
        assertEquals(List.of(new Constraint("non-negative")), problem.constraints());
        assertEquals(EvaluationSemantics.DETERMINISTIC, problem.evaluationSemantics());
    }

    @Test
    void evaluationCountsMatchDeclarations() {
        Evaluation evaluation = problem.evaluate(1.0);

        assertEquals(problem.objectives().size(), evaluation.objectiveCount());
        assertEquals(problem.constraints().size(), evaluation.constraintViolationCount());
    }

    @Test
    void evaluatesSatisfyingCandidateAsFeasible() {
        Candidate<Double> candidate = new Candidate<>(3.0);
        Evaluation evaluation = problem.evaluate(candidate.representation());

        assertEquals(3.0, evaluation.objectiveValue(0));
        assertEquals(0.0, evaluation.constraintViolation(0));
        assertTrue(evaluation.isFeasible());
    }

    @Test
    void evaluatesViolatingCandidateWithViolationMagnitude() {
        Candidate<Double> candidate = new Candidate<>(-2.0);
        Evaluation evaluation = problem.evaluate(candidate.representation());

        assertEquals(-2.0, evaluation.objectiveValue(0));
        assertEquals(2.0, evaluation.constraintViolation(0));
        assertFalse(evaluation.isFeasible());
    }

    @Test
    void rejectsNullRepresentation() {
        assertThrows(NullPointerException.class, () -> problem.evaluate(null));
    }
}

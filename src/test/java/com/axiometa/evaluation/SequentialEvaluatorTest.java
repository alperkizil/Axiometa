package com.axiometa.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.axiometa.core.Candidate;
import com.axiometa.core.Constraint;
import com.axiometa.core.Evaluation;
import com.axiometa.core.Objective;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Problem;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class SequentialEvaluatorTest {

    /**
     * Deterministic fixture: minimizes string length. Special representations
     * trigger failure modes so collection semantics can be exercised.
     */
    private static final class ScriptedProblem implements Problem<String> {

        private int invocations;

        @Override
        public List<Objective> objectives() {
            return List.of(new Objective("length", ObjectiveSense.MINIMIZE));
        }

        @Override
        public List<Constraint> constraints() {
            return List.of();
        }

        @Override
        public Evaluation evaluate(String representation) {
            Objects.requireNonNull(representation, "representation must not be null");
            invocations++;
            if (representation.startsWith("boom")) {
                throw new RuntimeException("kaboom " + representation);
            }
            if (representation.equals("error")) {
                throw new AssertionError("fatal");
            }
            if (representation.equals("nullEvaluation")) {
                return null;
            }
            if (representation.equals("extraObjective")) {
                return new Evaluation(new double[] {1.0, 2.0}, new double[0]);
            }
            if (representation.equals("extraConstraint")) {
                return new Evaluation(new double[] {1.0}, new double[] {0.0});
            }
            return new Evaluation(new double[] {representation.length()}, new double[0]);
        }
    }

    private static List<Candidate<String>> candidates(String... representations) {
        return Arrays.stream(representations).map(Candidate<String>::new).toList();
    }

    private final ScriptedProblem problem = new ScriptedProblem();
    private final SequentialEvaluator<String> evaluator = new SequentialEvaluator<>(problem);

    @Test
    void evaluatesAllCandidatesInInputOrder() {
        List<Candidate<String>> batch = candidates("aa", "b", "cccc");

        List<EvaluationOutcome<String>> outcomes = evaluator.evaluate(batch);

        assertEquals(3, outcomes.size());
        for (int i = 0; i < batch.size(); i++) {
            assertSame(batch.get(i), outcomes.get(i).candidate());
        }
        assertEquals(2.0, successValue(outcomes.get(0)));
        assertEquals(1.0, successValue(outcomes.get(1)));
        assertEquals(4.0, successValue(outcomes.get(2)));
    }

    @Test
    void emptyBatchYieldsEmptyResultAndNoAttempts() {
        assertEquals(List.of(), evaluator.evaluate(List.of()));
        assertEquals(0L, evaluator.evaluationCount());
    }

    @Test
    void countsAttemptsCumulativelyAcrossBatches() {
        evaluator.evaluate(candidates("a", "bb"));
        evaluator.evaluate(candidates("c", "dd", "eee"));

        assertEquals(5L, evaluator.evaluationCount());
    }

    @Test
    void collectsFailuresAndContinuesTheBatch() {
        List<Candidate<String>> batch = candidates("aa", "boom:x", "bbb");

        List<EvaluationOutcome<String>> outcomes = evaluator.evaluate(batch);

        assertEquals(2.0, successValue(outcomes.get(0)));
        EvaluationOutcome.Failure<String> failure = asFailure(outcomes.get(1));
        assertSame(batch.get(1), failure.candidate());
        assertEquals("kaboom boom:x", failure.cause().getMessage());
        assertEquals(3.0, successValue(outcomes.get(2)));
        assertEquals(3L, evaluator.evaluationCount());
    }

    @Test
    void collectsEveryFailureInAnAllFailingBatch() {
        List<EvaluationOutcome<String>> outcomes =
                evaluator.evaluate(candidates("boom:1", "boom:2"));

        assertEquals(2, outcomes.size());
        outcomes.forEach(SequentialEvaluatorTest::asFailure);
        assertEquals(2L, evaluator.evaluationCount());
    }

    @Test
    void errorsPropagateImmediately() {
        assertThrows(AssertionError.class,
                () -> evaluator.evaluate(candidates("aa", "error", "bb")));
        assertEquals(2L, evaluator.evaluationCount());
        assertEquals(2, problem.invocations);
    }

    @Test
    void nullEvaluationBecomesContractViolationFailure() {
        EvaluationOutcome.Failure<String> failure = soleFailure("nullEvaluation");

        assertInstanceOf(IllegalStateException.class, failure.cause());
        assertEquals("problem returned a null evaluation", failure.cause().getMessage());
    }

    @Test
    void objectiveCountMismatchBecomesContractViolationFailure() {
        EvaluationOutcome.Failure<String> failure = soleFailure("extraObjective");

        assertInstanceOf(IllegalStateException.class, failure.cause());
        assertEquals("problem returned 2 objective values but declares 1 objectives",
                failure.cause().getMessage());
    }

    @Test
    void constraintCountMismatchBecomesContractViolationFailure() {
        EvaluationOutcome.Failure<String> failure = soleFailure("extraConstraint");

        assertInstanceOf(IllegalStateException.class, failure.cause());
        assertEquals("problem returned 1 constraint violations but declares 0 constraints",
                failure.cause().getMessage());
    }

    @Test
    void rejectsNullCandidateList() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> evaluator.evaluate(null));
        assertEquals("candidates must not be null", e.getMessage());
    }

    @Test
    void rejectsNullCandidateElementBeforeEvaluatingAnything() {
        List<Candidate<String>> batch = Arrays.asList(new Candidate<>("aa"), null);

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> evaluator.evaluate(batch));
        assertEquals("candidates[1] must not be null", e.getMessage());
        assertEquals(0L, evaluator.evaluationCount());
        assertEquals(0, problem.invocations);
    }

    @Test
    void rejectsNullProblem() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new SequentialEvaluator<String>(null));
        assertEquals("problem must not be null", e.getMessage());
    }

    @Test
    void resultListIsImmutable() {
        List<EvaluationOutcome<String>> outcomes = evaluator.evaluate(candidates("aa"));

        assertThrows(UnsupportedOperationException.class,
                () -> outcomes.add(new EvaluationOutcome.Failure<>(
                        new Candidate<>("x"), new RuntimeException())));
    }

    private static double successValue(EvaluationOutcome<String> outcome) {
        if (outcome instanceof EvaluationOutcome.Success<String> success) {
            assertTrue(success.result().evaluation().isFeasible());
            return success.result().evaluation().objectiveValue(0);
        }
        throw new AssertionError("expected a success but was: " + outcome);
    }

    private static EvaluationOutcome.Failure<String> asFailure(
            EvaluationOutcome<String> outcome) {
        if (outcome instanceof EvaluationOutcome.Failure<String> failure) {
            return failure;
        }
        throw new AssertionError("expected a failure but was: " + outcome);
    }

    private EvaluationOutcome.Failure<String> soleFailure(String representation) {
        List<EvaluationOutcome<String>> outcomes =
                evaluator.evaluate(candidates(representation));
        assertEquals(1, outcomes.size());
        return asFailure(outcomes.get(0));
    }
}

package com.axiometa.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import com.axiometa.core.Constraint;
import com.axiometa.core.Evaluation;
import com.axiometa.core.EvaluationSemantics;
import com.axiometa.core.Objective;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Problem;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class CachingEvaluatorTest {

    /** Minimizes string length; counts invocations; "boom*" always throws. */
    private static final class CountingProblem implements Problem<String> {

        private final EvaluationSemantics semantics;
        private int invocations;

        CountingProblem(EvaluationSemantics semantics) {
            this.semantics = semantics;
        }

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
            return new Evaluation(new double[] {representation.length()}, new double[0]);
        }

        @Override
        public EvaluationSemantics evaluationSemantics() {
            return semantics;
        }
    }

    private static List<Candidate<String>> candidates(String... representations) {
        return Arrays.stream(representations).map(Candidate<String>::new).toList();
    }

    private static double successValue(EvaluationOutcome<String> outcome) {
        if (outcome instanceof EvaluationOutcome.Success<String> success) {
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

    private final CountingProblem problem =
            new CountingProblem(EvaluationSemantics.DETERMINISTIC);
    private final SequentialEvaluator<String> inner = new SequentialEvaluator<>(problem);
    private final CachingEvaluator<String> evaluator = new CachingEvaluator<>(inner, problem);

    @Test
    void servesRepeatedRepresentationsFromCache() {
        evaluator.evaluate(candidates("a", "bb"));
        List<Candidate<String>> secondBatch = candidates("a", "bb");

        List<EvaluationOutcome<String>> outcomes = evaluator.evaluate(secondBatch);

        assertEquals(2L, evaluator.evaluationCount());
        assertEquals(2, problem.invocations);
        assertEquals(2L, evaluator.cacheHitCount());
        assertSame(secondBatch.get(0), outcomes.get(0).candidate());
        assertSame(secondBatch.get(1), outcomes.get(1).candidate());
        assertEquals(1.0, successValue(outcomes.get(0)));
        assertEquals(2.0, successValue(outcomes.get(1)));
    }

    @Test
    void preservesInputOrderWithMixedHitsAndMisses() {
        evaluator.evaluate(candidates("aa"));
        List<Candidate<String>> batch = candidates("bbb", "aa", "c");

        List<EvaluationOutcome<String>> outcomes = evaluator.evaluate(batch);

        assertEquals(3.0, successValue(outcomes.get(0)));
        assertEquals(2.0, successValue(outcomes.get(1)));
        assertEquals(1.0, successValue(outcomes.get(2)));
        for (int i = 0; i < batch.size(); i++) {
            assertSame(batch.get(i), outcomes.get(i).candidate());
        }
        assertEquals(3L, evaluator.evaluationCount());
        assertEquals(1L, evaluator.cacheHitCount());
    }

    @Test
    void forwardsAllWithinBatchDuplicatesOfUncachedRepresentations() {
        evaluator.evaluate(candidates("x", "x"));

        assertEquals(2L, evaluator.evaluationCount());
        assertEquals(0L, evaluator.cacheHitCount());

        evaluator.evaluate(candidates("x"));

        assertEquals(2L, evaluator.evaluationCount());
        assertEquals(1L, evaluator.cacheHitCount());
    }

    @Test
    void neverCachesFailures() {
        EvaluationOutcome.Failure<String> first =
                asFailure(evaluator.evaluate(candidates("boom:a")).get(0));
        EvaluationOutcome.Failure<String> second =
                asFailure(evaluator.evaluate(candidates("boom:a")).get(0));

        assertEquals("kaboom boom:a", first.cause().getMessage());
        assertEquals("kaboom boom:a", second.cause().getMessage());
        assertEquals(2L, evaluator.evaluationCount());
        assertEquals(0L, evaluator.cacheHitCount());
    }

    @Test
    void bypassesCacheForStochasticProblems() {
        CountingProblem stochastic = new CountingProblem(EvaluationSemantics.STOCHASTIC);
        CachingEvaluator<String> bypassing =
                new CachingEvaluator<>(new SequentialEvaluator<>(stochastic), stochastic);

        bypassing.evaluate(candidates("a"));
        bypassing.evaluate(candidates("a"));

        assertEquals(2L, bypassing.evaluationCount());
        assertEquals(2, stochastic.invocations);
        assertEquals(0L, bypassing.cacheHitCount());
    }

    @Test
    void hitsPlusRealAttemptsEqualTotalRequests() {
        evaluator.evaluate(candidates("a", "bb", "a"));
        evaluator.evaluate(candidates("a", "boom:z", "ccc"));
        evaluator.evaluate(candidates("boom:z", "bb"));

        long totalRequests = 3 + 3 + 2;
        assertEquals(totalRequests,
                evaluator.cacheHitCount() + evaluator.evaluationCount());
    }

    @Test
    void delegatesEvaluationCountToTheWrappedEvaluator() {
        evaluator.evaluate(candidates("a", "bb"));
        evaluator.evaluate(candidates("a", "ccc"));

        assertEquals(inner.evaluationCount(), evaluator.evaluationCount());
        assertEquals(3L, evaluator.evaluationCount());
    }

    @Test
    void rejectsNullCandidateList() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> evaluator.evaluate(null));
        assertEquals("candidates must not be null", e.getMessage());
    }

    @Test
    void rejectsNullElementBeforeTouchingTheInnerEvaluator() {
        List<Candidate<String>> batch = Arrays.asList(new Candidate<>("a"), null);

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> evaluator.evaluate(batch));
        assertEquals("candidates[1] must not be null", e.getMessage());
        assertEquals(0L, evaluator.evaluationCount());
        assertEquals(0, problem.invocations);
    }

    @Test
    void stochasticPathStillValidatesInput() {
        CountingProblem stochastic = new CountingProblem(EvaluationSemantics.STOCHASTIC);
        CachingEvaluator<String> bypassing =
                new CachingEvaluator<>(new SequentialEvaluator<>(stochastic), stochastic);

        assertThrows(NullPointerException.class, () -> bypassing.evaluate(null));
        assertEquals(0, stochastic.invocations);
    }

    @Test
    void rejectsNullConstructorArguments() {
        NullPointerException nullInner = assertThrows(NullPointerException.class,
                () -> new CachingEvaluator<>(null, problem));
        assertEquals("inner must not be null", nullInner.getMessage());

        NullPointerException nullProblem = assertThrows(NullPointerException.class,
                () -> new CachingEvaluator<>(inner, null));
        assertEquals("problem must not be null", nullProblem.getMessage());
    }

    @Test
    void rejectsNullEvaluationSemanticsAtConstruction() {
        CountingProblem brokenProblem = new CountingProblem(null);

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new CachingEvaluator<>(new SequentialEvaluator<>(brokenProblem),
                        brokenProblem));
        assertEquals("problem.evaluationSemantics() must not be null", e.getMessage());
    }

    @Test
    void detectsAnInnerEvaluatorViolatingTheBatchContract() {
        Evaluator<String> misbehaving = new Evaluator<>() {
            @Override
            public List<EvaluationOutcome<String>> evaluate(
                    List<Candidate<String>> candidates) {
                return List.of();
            }

            @Override
            public long evaluationCount() {
                return 0L;
            }
        };
        CachingEvaluator<String> caching = new CachingEvaluator<>(misbehaving, problem);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> caching.evaluate(candidates("a")));
        assertEquals("inner evaluator returned 0 outcomes for 1 candidates", e.getMessage());
    }

    @Test
    void resultListIsImmutable() {
        evaluator.evaluate(candidates("a"));
        List<EvaluationOutcome<String>> outcomes = evaluator.evaluate(candidates("a", "bb"));

        assertThrows(UnsupportedOperationException.class,
                () -> outcomes.add(new EvaluationOutcome.Failure<>(
                        new Candidate<>("x"), new RuntimeException())));
    }
}

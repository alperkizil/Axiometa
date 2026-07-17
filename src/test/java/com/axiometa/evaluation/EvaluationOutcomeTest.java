package com.axiometa.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import org.junit.jupiter.api.Test;

class EvaluationOutcomeTest {

    private final Candidate<String> candidate = new Candidate<>("abc");
    private final EvaluatedCandidate<String> evaluated = new EvaluatedCandidate<>(
            candidate, new Evaluation(new double[] {1.0}, new double[0]));

    @Test
    void successDerivesItsCandidateFromTheResult() {
        EvaluationOutcome.Success<String> success = new EvaluationOutcome.Success<>(evaluated);

        assertSame(candidate, success.candidate());
        assertSame(evaluated, success.result());
    }

    @Test
    void successRejectsNullResult() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new EvaluationOutcome.Success<>(null));
        assertEquals("result must not be null", e.getMessage());
    }

    @Test
    void failureCarriesCandidateAndCause() {
        RuntimeException cause = new RuntimeException("kaboom");
        EvaluationOutcome.Failure<String> failure =
                new EvaluationOutcome.Failure<>(candidate, cause);

        assertSame(candidate, failure.candidate());
        assertSame(cause, failure.cause());
    }

    @Test
    void failureRejectsNulls() {
        NullPointerException nullCandidate = assertThrows(NullPointerException.class,
                () -> new EvaluationOutcome.Failure<>(null, new RuntimeException()));
        assertEquals("candidate must not be null", nullCandidate.getMessage());

        NullPointerException nullCause = assertThrows(NullPointerException.class,
                () -> new EvaluationOutcome.Failure<>(candidate, null));
        assertEquals("cause must not be null", nullCause.getMessage());
    }

    @Test
    void sealedHierarchySupportsExhaustivePatternMatching() {
        EvaluationOutcome<String> success = new EvaluationOutcome.Success<>(evaluated);
        EvaluationOutcome<String> failure =
                new EvaluationOutcome.Failure<>(candidate, new RuntimeException("kaboom"));

        assertEquals("success", describe(success));
        assertEquals("failure: kaboom", describe(failure));
    }

    private static String describe(EvaluationOutcome<String> outcome) {
        return switch (outcome) {
            case EvaluationOutcome.Success<String> s -> "success";
            case EvaluationOutcome.Failure<String> f -> "failure: " + f.cause().getMessage();
        };
    }
}

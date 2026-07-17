package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EvaluatedCandidateTest {

    private final Candidate<String> candidate = new Candidate<>("abc");
    private final Evaluation evaluation = new Evaluation(new double[] {1.0}, new double[0]);

    @Test
    void storesCandidateAndEvaluation() {
        EvaluatedCandidate<String> pair = new EvaluatedCandidate<>(candidate, evaluation);

        assertSame(candidate, pair.candidate());
        assertSame(evaluation, pair.evaluation());
    }

    @Test
    void rejectsNullCandidate() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new EvaluatedCandidate<>(null, evaluation));
        assertEquals("candidate must not be null", e.getMessage());
    }

    @Test
    void rejectsNullEvaluation() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new EvaluatedCandidate<>(candidate, null));
        assertEquals("evaluation must not be null", e.getMessage());
    }

    @Test
    void equalityIsValueBased() {
        EvaluatedCandidate<String> a = new EvaluatedCandidate<>(
                new Candidate<>("abc"), new Evaluation(new double[] {1.0}, new double[0]));
        EvaluatedCandidate<String> b = new EvaluatedCandidate<>(
                new Candidate<>("abc"), new Evaluation(new double[] {1.0}, new double[0]));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}

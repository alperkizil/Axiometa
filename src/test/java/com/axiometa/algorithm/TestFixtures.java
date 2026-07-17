package com.axiometa.algorithm;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import com.axiometa.phase.AlgorithmState;
import java.util.List;

/** Shared minimal fixtures for the S6 lifecycle tests. */
final class TestFixtures {

    record FakeRepresentation(int value) implements Representation {
    }

    private TestFixtures() {
    }

    static Population<FakeRepresentation> population() {
        return new Population<>(List.of(new EvaluatedCandidate<>(
                new Candidate<>(new FakeRepresentation(0)),
                new Evaluation(new double[] {0.0}, new double[0]))));
    }

    static AlgorithmState<FakeRepresentation> state(int iteration, long evaluationCount) {
        return new AlgorithmState<>(iteration, evaluationCount, population());
    }
}

package com.axiometa.ga;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.Representation;

/** Shared minimal fixtures for the GA component tests. */
final class GaFixtures {

    record TestRep(int id) implements Representation {
    }

    private GaFixtures() {
    }

    static EvaluatedCandidate<TestRep> member(int id, double fitness) {
        return new EvaluatedCandidate<>(new Candidate<>(new TestRep(id)),
                new Evaluation(new double[] {fitness}, new double[0]));
    }

    static EvaluatedCandidate<TestRep> constrainedMember(int id, double violation) {
        return new EvaluatedCandidate<>(new Candidate<>(new TestRep(id)),
                new Evaluation(new double[] {1.0}, new double[] {violation}));
    }

    static EvaluatedCandidate<TestRep> biObjectiveMember(int id) {
        return new EvaluatedCandidate<>(new Candidate<>(new TestRep(id)),
                new Evaluation(new double[] {1.0, 2.0}, new double[0]));
    }
}

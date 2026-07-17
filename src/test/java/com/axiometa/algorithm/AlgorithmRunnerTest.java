package com.axiometa.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.algorithm.TestFixtures.FakeRepresentation;
import com.axiometa.phase.AlgorithmState;
import org.junit.jupiter.api.Test;

class AlgorithmRunnerTest {

    /**
     * Stub lifecycle: evaluates 10 candidates per generation and counts how
     * the runner drives it.
     */
    private static final class StubAlgorithm implements Algorithm<FakeRepresentation> {

        private static final long EVALUATIONS_PER_GENERATION = 10L;

        private int initializeCalls;
        private int stepCalls;

        @Override
        public AlgorithmState<FakeRepresentation> initialize() {
            initializeCalls++;
            return TestFixtures.state(0, EVALUATIONS_PER_GENERATION);
        }

        @Override
        public AlgorithmState<FakeRepresentation> step(
                AlgorithmState<FakeRepresentation> state) {
            stepCalls++;
            return TestFixtures.state(state.iteration() + 1,
                    state.evaluationCount() + EVALUATIONS_PER_GENERATION);
        }
    }

    private final StubAlgorithm algorithm = new StubAlgorithm();

    @Test
    void runsUntilTheIterationBudgetIsReached() {
        AlgorithmState<FakeRepresentation> finalState =
                AlgorithmRunner.run(algorithm, Terminations.maxIterations(5));

        assertEquals(5, finalState.iteration());
        assertEquals(60L, finalState.evaluationCount());
        assertEquals(1, algorithm.initializeCalls);
        assertEquals(5, algorithm.stepCalls);
    }

    @Test
    void runsUntilTheEvaluationBudgetIsReached() {
        AlgorithmState<FakeRepresentation> finalState =
                AlgorithmRunner.run(algorithm, Terminations.maxEvaluations(35L));

        assertEquals(3, finalState.iteration());
        assertEquals(40L, finalState.evaluationCount());
    }

    @Test
    void checksTerminationOnGenerationZero() {
        AlgorithmState<FakeRepresentation> finalState =
                AlgorithmRunner.run(algorithm, state -> true);

        assertEquals(0, finalState.iteration());
        assertEquals(1, algorithm.initializeCalls);
        assertEquals(0, algorithm.stepCalls);
    }

    @Test
    void composesBudgetsWithAnyOf() {
        AlgorithmState<FakeRepresentation> finalState = AlgorithmRunner.run(algorithm,
                Terminations.anyOf(
                        Terminations.maxIterations(100),
                        Terminations.maxEvaluations(30L)));

        assertEquals(2, finalState.iteration());
        assertEquals(30L, finalState.evaluationCount());
    }

    @Test
    void composesBudgetsWithAllOf() {
        AlgorithmState<FakeRepresentation> finalState = AlgorithmRunner.run(algorithm,
                Terminations.allOf(
                        Terminations.maxIterations(2),
                        Terminations.maxEvaluations(35L)));

        assertEquals(3, finalState.iteration());
        assertEquals(40L, finalState.evaluationCount());
    }

    @Test
    void rejectsNullArguments() {
        NullPointerException nullAlgorithm = assertThrows(NullPointerException.class,
                () -> AlgorithmRunner.run(null, Terminations.maxIterations(1)));
        assertEquals("algorithm must not be null", nullAlgorithm.getMessage());

        NullPointerException nullTermination = assertThrows(NullPointerException.class,
                () -> AlgorithmRunner.run(algorithm, null));
        assertEquals("termination must not be null", nullTermination.getMessage());
    }

    @Test
    void rejectsANullStateFromInitialize() {
        Algorithm<FakeRepresentation> broken = new Algorithm<>() {
            @Override
            public AlgorithmState<FakeRepresentation> initialize() {
                return null;
            }

            @Override
            public AlgorithmState<FakeRepresentation> step(
                    AlgorithmState<FakeRepresentation> state) {
                throw new AssertionError("must not be reached");
            }
        };

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> AlgorithmRunner.run(broken, state -> true));
        assertEquals("algorithm returned a null state from initialize()", e.getMessage());
    }

    @Test
    void rejectsANullStateFromStep() {
        Algorithm<FakeRepresentation> broken = new Algorithm<>() {
            @Override
            public AlgorithmState<FakeRepresentation> initialize() {
                return TestFixtures.state(0, 1);
            }

            @Override
            public AlgorithmState<FakeRepresentation> step(
                    AlgorithmState<FakeRepresentation> state) {
                return null;
            }
        };

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> AlgorithmRunner.run(broken, state -> false));
        assertEquals("algorithm returned a null state from step()", e.getMessage());
    }
}

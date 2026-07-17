package com.axiometa.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.axiometa.algorithm.TestFixtures.FakeRepresentation;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.phase.Termination;
import org.junit.jupiter.api.Test;

class TerminationsTest {

    /** Spy proving combinators consult every member on every check. */
    private static final class CountingTermination implements Termination<FakeRepresentation> {

        private final boolean answer;
        private int calls;

        CountingTermination(boolean answer) {
            this.answer = answer;
        }

        @Override
        public boolean shouldTerminate(AlgorithmState<FakeRepresentation> state) {
            calls++;
            return answer;
        }
    }

    @Test
    void maxIterationsStopsAtTheBoundary() {
        Termination<FakeRepresentation> condition = Terminations.maxIterations(3);

        assertFalse(condition.shouldTerminate(TestFixtures.state(2, 1_000)));
        assertTrue(condition.shouldTerminate(TestFixtures.state(3, 0)));
        assertTrue(condition.shouldTerminate(TestFixtures.state(4, 0)));
    }

    @Test
    void maxEvaluationsStopsAtTheBoundary() {
        Termination<FakeRepresentation> condition = Terminations.maxEvaluations(100L);

        assertFalse(condition.shouldTerminate(TestFixtures.state(50, 99)));
        assertTrue(condition.shouldTerminate(TestFixtures.state(0, 100)));
        assertTrue(condition.shouldTerminate(TestFixtures.state(0, 150)));
    }

    @Test
    void limitsRejectNonPositiveValues() {
        IllegalArgumentException iterations = assertThrows(IllegalArgumentException.class,
                () -> Terminations.<FakeRepresentation>maxIterations(0));
        assertEquals("limit must be >= 1 but was 0", iterations.getMessage());

        IllegalArgumentException evaluations = assertThrows(IllegalArgumentException.class,
                () -> Terminations.<FakeRepresentation>maxEvaluations(0L));
        assertEquals("limit must be >= 1 but was 0", evaluations.getMessage());
    }

    @Test
    void conditionsRejectNullState() {
        NullPointerException iterations = assertThrows(NullPointerException.class,
                () -> Terminations.<FakeRepresentation>maxIterations(1).shouldTerminate(null));
        assertEquals("state must not be null", iterations.getMessage());

        NullPointerException combined = assertThrows(NullPointerException.class,
                () -> Terminations.<FakeRepresentation>anyOf(state -> true)
                        .shouldTerminate(null));
        assertEquals("state must not be null", combined.getMessage());
    }

    @Test
    void anyOfStopsWhenAnyMemberStops() {
        assertFalse(Terminations.<FakeRepresentation>anyOf(state -> false, state -> false)
                .shouldTerminate(TestFixtures.state(0, 0)));
        assertTrue(Terminations.<FakeRepresentation>anyOf(state -> true, state -> false)
                .shouldTerminate(TestFixtures.state(0, 0)));
        assertTrue(Terminations.<FakeRepresentation>anyOf(state -> false, state -> true)
                .shouldTerminate(TestFixtures.state(0, 0)));
    }

    @Test
    void allOfStopsOnlyWhenEveryMemberStops() {
        assertTrue(Terminations.<FakeRepresentation>allOf(state -> true, state -> true)
                .shouldTerminate(TestFixtures.state(0, 0)));
        assertFalse(Terminations.<FakeRepresentation>allOf(state -> true, state -> false)
                .shouldTerminate(TestFixtures.state(0, 0)));
    }

    @Test
    void combinatorsConsultEveryMemberWithoutShortCircuiting() {
        CountingTermination alreadyStopping = new CountingTermination(true);
        CountingTermination stillRunning = new CountingTermination(false);

        Terminations.anyOf(alreadyStopping, stillRunning)
                .shouldTerminate(TestFixtures.state(0, 0));
        assertEquals(1, alreadyStopping.calls);
        assertEquals(1, stillRunning.calls);

        Terminations.allOf(stillRunning, alreadyStopping)
                .shouldTerminate(TestFixtures.state(0, 0));
        assertEquals(2, alreadyStopping.calls);
        assertEquals(2, stillRunning.calls);
    }

    @Test
    void combinatorsRejectEmptyConditions() {
        IllegalArgumentException any = assertThrows(IllegalArgumentException.class,
                () -> Terminations.<FakeRepresentation>anyOf());
        assertEquals("conditions must not be empty", any.getMessage());

        IllegalArgumentException all = assertThrows(IllegalArgumentException.class,
                () -> Terminations.<FakeRepresentation>allOf());
        assertEquals("conditions must not be empty", all.getMessage());
    }

    @Test
    void combinatorsRejectNullConditionArraysAndElements() {
        NullPointerException nullArray = assertThrows(NullPointerException.class,
                () -> Terminations.<FakeRepresentation>anyOf(
                        (Termination<FakeRepresentation>[]) null));
        assertEquals("conditions must not be null", nullArray.getMessage());

        NullPointerException nullElement = assertThrows(NullPointerException.class,
                () -> Terminations.<FakeRepresentation>allOf(state -> true, null));
        assertEquals("conditions[1] must not be null", nullElement.getMessage());
    }
}

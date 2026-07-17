package com.axiometa.ga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Population;
import com.axiometa.ga.GaFixtures.TestRep;
import java.util.List;
import org.junit.jupiter.api.Test;

class TournamentSelectionTest {

    private final EvaluatedCandidate<TestRep> weak = GaFixtures.member(1, 1.0);
    private final EvaluatedCandidate<TestRep> strong = GaFixtures.member(2, 5.0);
    private final Population<TestRep> population = new Population<>(List.of(weak, strong));

    @Test
    void picksTheBetterContenderWhenMaximizing() {
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MAXIMIZE, 2, new ScriptedRandomSource().withInts(0, 1));

        assertEquals(List.of(strong), selection.select(population, 1));
    }

    @Test
    void picksTheBetterContenderWhenMinimizing() {
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MINIMIZE, 2, new ScriptedRandomSource().withInts(0, 1));

        assertEquals(List.of(weak), selection.select(population, 1));
    }

    @Test
    void tieKeepsTheFirstSampledMember() {
        EvaluatedCandidate<TestRep> firstTied = GaFixtures.member(1, 3.0);
        EvaluatedCandidate<TestRep> secondTied = GaFixtures.member(2, 3.0);
        Population<TestRep> tied = new Population<>(List.of(firstTied, secondTied));
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MAXIMIZE, 2, new ScriptedRandomSource().withInts(1, 0));

        assertEquals(List.of(secondTied), selection.select(tied, 1));
    }

    @Test
    void tournamentSizeOnePicksTheSampledMember() {
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MAXIMIZE, 1, new ScriptedRandomSource().withInts(0));

        assertEquals(List.of(weak), selection.select(population, 1));
    }

    @Test
    void returnsExactlyCountAndMaySelectTheSameMemberRepeatedly() {
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MAXIMIZE, 2,
                new ScriptedRandomSource().withInts(0, 0, 0, 0));

        assertEquals(List.of(weak, weak), selection.select(population, 2));
    }

    @Test
    void rejectsMultiObjectiveEvaluations() {
        Population<TestRep> multi =
                new Population<>(List.of(GaFixtures.biObjectiveMember(1)));
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MAXIMIZE, 1, new ScriptedRandomSource().withInts(0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> selection.select(multi, 1));
        assertEquals("single-objective components require exactly 1 objective value "
                + "but found 2", e.getMessage());
    }

    @Test
    void rejectsConstrainedEvaluations() {
        Population<TestRep> constrained =
                new Population<>(List.of(GaFixtures.constrainedMember(1, 0.0)));
        TournamentSelection<TestRep> selection = new TournamentSelection<>(
                ObjectiveSense.MAXIMIZE, 1, new ScriptedRandomSource().withInts(0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> selection.select(constrained, 1));
        assertTrue(e.getMessage().startsWith("constraint handling is not implemented"),
                e.getMessage());
    }

    @Test
    void rejectsInvalidConstructionAndArguments() {
        ScriptedRandomSource random = new ScriptedRandomSource();

        assertEquals("sense must not be null",
                assertThrows(NullPointerException.class,
                        () -> new TournamentSelection<TestRep>(null, 2, random)).getMessage());
        assertEquals("tournamentSize must be >= 1 but was 0",
                assertThrows(IllegalArgumentException.class,
                        () -> new TournamentSelection<TestRep>(
                                ObjectiveSense.MAXIMIZE, 0, random)).getMessage());
        assertEquals("random must not be null",
                assertThrows(NullPointerException.class,
                        () -> new TournamentSelection<TestRep>(
                                ObjectiveSense.MAXIMIZE, 2, null)).getMessage());

        TournamentSelection<TestRep> selection =
                new TournamentSelection<>(ObjectiveSense.MAXIMIZE, 2, random);
        assertEquals("population must not be null",
                assertThrows(NullPointerException.class,
                        () -> selection.select(null, 1)).getMessage());
        assertEquals("count must be >= 1 but was 0",
                assertThrows(IllegalArgumentException.class,
                        () -> selection.select(population, 0)).getMessage());
    }
}

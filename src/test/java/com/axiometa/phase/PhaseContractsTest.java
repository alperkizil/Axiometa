package com.axiometa.phase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * Type-level proof that the six phase contracts compile against the S1/S2
 * core types and compose into one algorithm iteration, using deterministic
 * test doubles.
 */
class PhaseContractsTest {

    private record FakeRepresentation(int value) implements Representation {
    }

    private static EvaluatedCandidate<FakeRepresentation> evaluated(
            Candidate<FakeRepresentation> candidate) {
        return new EvaluatedCandidate<>(candidate,
                new Evaluation(new double[] {candidate.representation().value()}, new double[0]));
    }

    private final Initialization<FakeRepresentation> initialization = size ->
            IntStream.range(0, size)
                    .mapToObj(i -> new Candidate<>(new FakeRepresentation(i)))
                    .toList();

    private final Selection<FakeRepresentation> selection = (population, count) ->
            IntStream.range(0, count)
                    .mapToObj(i -> population.members().get(i % population.size()))
                    .toList();

    private final Crossover<FakeRepresentation> crossover = (first, second) ->
            new OffspringPair<>(second, first);

    private final Mutation<FakeRepresentation> mutation = candidate ->
            new Candidate<>(new FakeRepresentation(candidate.representation().value() + 1));

    private final Replacement<FakeRepresentation> replacement = (current, offspring) ->
            offspring.isEmpty() ? current : new Population<>(offspring);

    private final Termination<FakeRepresentation> termination = state ->
            state.evaluationCount() >= 8;

    @Test
    void allSixContractsShareTheAlgorithmPhaseAncestor() {
        assertInstanceOf(AlgorithmPhase.class, initialization);
        assertInstanceOf(AlgorithmPhase.class, selection);
        assertInstanceOf(AlgorithmPhase.class, crossover);
        assertInstanceOf(AlgorithmPhase.class, mutation);
        assertInstanceOf(AlgorithmPhase.class, replacement);
        assertInstanceOf(AlgorithmPhase.class, termination);
    }

    @Test
    void phasesComposeIntoOneIterationOverCoreTypes() {
        List<Candidate<FakeRepresentation>> initial = initialization.initialize(4);
        assertEquals(4, initial.size());

        Population<FakeRepresentation> population = new Population<>(
                initial.stream().map(PhaseContractsTest::evaluated).toList());

        List<EvaluatedCandidate<FakeRepresentation>> parents = selection.select(population, 4);
        assertEquals(4, parents.size());

        List<EvaluatedCandidate<FakeRepresentation>> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i += 2) {
            OffspringPair<FakeRepresentation> children = crossover.crossover(
                    parents.get(i).candidate(), parents.get(i + 1).candidate());
            offspring.add(evaluated(mutation.mutate(children.first())));
            offspring.add(evaluated(mutation.mutate(children.second())));
        }

        Population<FakeRepresentation> next = replacement.replace(population, offspring);

        // parents (0,1,2,3) -> swapped pairs (1,0),(3,2) -> mutated +1
        assertEquals(List.of(2, 1, 4, 3),
                next.members().stream()
                        .map(member -> member.candidate().representation().value())
                        .toList());

        assertFalse(termination.shouldTerminate(new AlgorithmState<>(1, 4, next)));
        assertTrue(termination.shouldTerminate(new AlgorithmState<>(2, 8, next)));
    }

    @Test
    void replacementMayKeepTheCurrentPopulationForEmptyOffspring() {
        Population<FakeRepresentation> population = new Population<>(
                List.of(evaluated(new Candidate<>(new FakeRepresentation(7)))));

        assertSame(population, replacement.replace(population, List.of()));
    }
}

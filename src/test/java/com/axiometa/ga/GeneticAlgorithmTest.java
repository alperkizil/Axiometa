package com.axiometa.ga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.bitstring.BitFlipMutation;
import com.axiometa.bitstring.BitString;
import com.axiometa.bitstring.OnePointCrossover;
import com.axiometa.bitstring.RandomBitStringInitialization;
import com.axiometa.core.Candidate;
import com.axiometa.core.Constraint;
import com.axiometa.core.Evaluation;
import com.axiometa.core.EvaluationSemantics;
import com.axiometa.core.Objective;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Problem;
import com.axiometa.evaluation.SequentialEvaluator;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.phase.Initialization;
import com.axiometa.phase.Selection;
import com.axiometa.random.RandomSource;
import com.axiometa.random.SeededRandomSource;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class GeneticAlgorithmTest {

    private static final int POPULATION_SIZE = 4;
    private static final int BIT_COUNT = 8;

    private static GeneticAlgorithm<BitString> oneMaxGa(Problem<BitString> problem,
            RandomSource root) {
        return new GeneticAlgorithm<>(POPULATION_SIZE,
                new RandomBitStringInitialization(BIT_COUNT, root.child("initialization")),
                new SequentialEvaluator<>(problem),
                new TournamentSelection<>(ObjectiveSense.MAXIMIZE, 2, root.child("selection")),
                new OnePointCrossover(0.9, root.child("crossover")),
                new BitFlipMutation(0.05, root.child("mutation")),
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1));
    }

    @Test
    void initializeProducesAnEvaluatedGenerationZero() {
        GeneticAlgorithm<BitString> ga =
                oneMaxGa(new OneMaxProblem(), SeededRandomSource.root(1L));

        AlgorithmState<BitString> state = ga.initialize();

        assertEquals(0, state.iteration());
        assertEquals(POPULATION_SIZE, state.population().size());
        assertEquals(POPULATION_SIZE, state.evaluationCount());
    }

    @Test
    void stepAdvancesIterationAndEvaluationAccounting() {
        GeneticAlgorithm<BitString> ga =
                oneMaxGa(new OneMaxProblem(), SeededRandomSource.root(1L));

        AlgorithmState<BitString> next = ga.step(ga.initialize());

        assertEquals(1, next.iteration());
        assertEquals(POPULATION_SIZE, next.population().size());
        assertEquals(2L * POPULATION_SIZE, next.evaluationCount());
    }

    @Test
    void rejectsOddOrTooSmallPopulationSizes() {
        RandomSource root = SeededRandomSource.root(1L);
        Problem<BitString> problem = new OneMaxProblem();

        IllegalArgumentException odd = assertThrows(IllegalArgumentException.class,
                () -> new GeneticAlgorithm<>(3,
                        new RandomBitStringInitialization(BIT_COUNT, root.child("init")),
                        new SequentialEvaluator<>(problem),
                        new TournamentSelection<>(ObjectiveSense.MAXIMIZE, 2,
                                root.child("sel")),
                        new OnePointCrossover(0.9, root.child("cx")),
                        new BitFlipMutation(0.05, root.child("mut")),
                        new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1)));
        assertEquals("populationSize must be an even number >= 2 but was 3",
                odd.getMessage());
    }

    @Test
    void rejectsNullComponents() {
        RandomSource root = SeededRandomSource.root(1L);
        Problem<BitString> problem = new OneMaxProblem();
        RandomBitStringInitialization initialization =
                new RandomBitStringInitialization(BIT_COUNT, root.child("init"));
        SequentialEvaluator<BitString> evaluator = new SequentialEvaluator<>(problem);
        TournamentSelection<BitString> selection =
                new TournamentSelection<>(ObjectiveSense.MAXIMIZE, 2, root.child("sel"));
        OnePointCrossover crossover = new OnePointCrossover(0.9, root.child("cx"));
        BitFlipMutation mutation = new BitFlipMutation(0.05, root.child("mut"));
        GenerationalReplacement<BitString> replacement =
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1);

        assertEquals("initialization must not be null",
                assertThrows(NullPointerException.class, () -> new GeneticAlgorithm<>(2,
                        null, evaluator, selection, crossover, mutation, replacement))
                        .getMessage());
        assertEquals("evaluator must not be null",
                assertThrows(NullPointerException.class, () -> new GeneticAlgorithm<>(2,
                        initialization, null, selection, crossover, mutation, replacement))
                        .getMessage());
        assertEquals("selection must not be null",
                assertThrows(NullPointerException.class, () -> new GeneticAlgorithm<>(2,
                        initialization, evaluator, null, crossover, mutation, replacement))
                        .getMessage());
        assertEquals("crossover must not be null",
                assertThrows(NullPointerException.class, () -> new GeneticAlgorithm<>(2,
                        initialization, evaluator, selection, null, mutation, replacement))
                        .getMessage());
        assertEquals("mutation must not be null",
                assertThrows(NullPointerException.class, () -> new GeneticAlgorithm<>(2,
                        initialization, evaluator, selection, crossover, null, replacement))
                        .getMessage());
        assertEquals("replacement must not be null",
                assertThrows(NullPointerException.class, () -> new GeneticAlgorithm<>(2,
                        initialization, evaluator, selection, crossover, mutation, null))
                        .getMessage());
    }

    @Test
    void evaluationFailureEndsTheRunLoudly() {
        Problem<BitString> failing = new Problem<>() {
            @Override
            public List<Objective> objectives() {
                return List.of(new Objective("ones", ObjectiveSense.MAXIMIZE));
            }

            @Override
            public List<Constraint> constraints() {
                return List.of();
            }

            @Override
            public Evaluation evaluate(BitString representation) {
                Objects.requireNonNull(representation, "representation must not be null");
                throw new RuntimeException("evaluation exploded");
            }

            @Override
            public EvaluationSemantics evaluationSemantics() {
                return EvaluationSemantics.DETERMINISTIC;
            }
        };
        GeneticAlgorithm<BitString> ga = oneMaxGa(failing, SeededRandomSource.root(1L));

        IllegalStateException e = assertThrows(IllegalStateException.class, ga::initialize);
        assertEquals("evaluation failed for 4 of 4 candidates", e.getMessage());
        assertNotNull(e.getCause());
        assertEquals("evaluation exploded", e.getCause().getMessage());
    }

    @Test
    void detectsASelectionContractViolation() {
        RandomSource root = SeededRandomSource.root(1L);
        Problem<BitString> problem = new OneMaxProblem();
        Selection<BitString> broken =
                (population, count) -> List.of(population.members().get(0));
        GeneticAlgorithm<BitString> ga = new GeneticAlgorithm<>(POPULATION_SIZE,
                new RandomBitStringInitialization(BIT_COUNT, root.child("initialization")),
                new SequentialEvaluator<>(problem),
                broken,
                new OnePointCrossover(0.9, root.child("crossover")),
                new BitFlipMutation(0.05, root.child("mutation")),
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1));

        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> ga.step(ga.initialize()));
        assertEquals("selection returned 1 parents but 4 were requested", e.getMessage());
    }

    @Test
    void detectsAnInitializationContractViolation() {
        RandomSource root = SeededRandomSource.root(1L);
        Problem<BitString> problem = new OneMaxProblem();
        Initialization<BitString> broken = populationSize ->
                List.of(new Candidate<>(new BitString(List.of(true))));
        GeneticAlgorithm<BitString> ga = new GeneticAlgorithm<>(POPULATION_SIZE,
                broken,
                new SequentialEvaluator<>(problem),
                new TournamentSelection<>(ObjectiveSense.MAXIMIZE, 2, root.child("selection")),
                new OnePointCrossover(0.9, root.child("crossover")),
                new BitFlipMutation(0.05, root.child("mutation")),
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1));

        IllegalStateException e = assertThrows(IllegalStateException.class, ga::initialize);
        assertEquals("initialization returned 1 candidates but 4 were requested",
                e.getMessage());
    }
}

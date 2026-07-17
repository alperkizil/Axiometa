package com.axiometa.ga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.axiometa.algorithm.AlgorithmRunner;
import com.axiometa.algorithm.Terminations;
import com.axiometa.bitstring.BitFlipMutation;
import com.axiometa.bitstring.BitString;
import com.axiometa.bitstring.OnePointCrossover;
import com.axiometa.bitstring.RandomBitStringInitialization;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.evaluation.CachingEvaluator;
import com.axiometa.evaluation.SequentialEvaluator;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.random.RandomSource;
import com.axiometa.random.SeededRandomSource;
import org.junit.jupiter.api.Test;

/**
 * End-to-end proof of the S1–S6 design: the generational GA, wired purely
 * from framework pieces (deterministic named random streams, sequential
 * evaluation behind the fitness cache, composed termination, the shared
 * runner), solves OneMax reproducibly.
 */
class OneMaxEndToEndTest {

    private static final int BIT_COUNT = 20;
    private static final int POPULATION_SIZE = 20;

    private static GeneticAlgorithm<BitString> wire(RandomSource root) {
        OneMaxProblem problem = new OneMaxProblem();
        return new GeneticAlgorithm<>(POPULATION_SIZE,
                new RandomBitStringInitialization(BIT_COUNT, root.child("initialization")),
                new CachingEvaluator<>(new SequentialEvaluator<>(problem), problem),
                new TournamentSelection<>(ObjectiveSense.MAXIMIZE, 2, root.child("selection")),
                new OnePointCrossover(0.9, root.child("crossover")),
                new BitFlipMutation(0.05, root.child("mutation")),
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, 1));
    }

    private static AlgorithmState<BitString> run(long rootSeed, int maxIterations) {
        return AlgorithmRunner.run(wire(SeededRandomSource.root(rootSeed)),
                Terminations.anyOf(
                        Terminations.maxIterations(maxIterations),
                        Terminations.maxEvaluations(100_000L)));
    }

    private static double bestFitness(AlgorithmState<BitString> state) {
        return state.population().members().stream()
                .mapToDouble(member -> member.evaluation().objectiveValue(0))
                .max()
                .orElseThrow();
    }

    @Test
    void reachesTheOneMaxOptimum() {
        AlgorithmState<BitString> finalState = run(42L, 100);

        assertEquals(BIT_COUNT, bestFitness(finalState));
    }

    @Test
    void identicalRunsFromTheSameRootSeed() {
        assertEquals(run(42L, 40), run(42L, 40));
    }

    @Test
    void differentRootSeedsProduceDifferentInitialPopulations() {
        AlgorithmState<BitString> first = wire(SeededRandomSource.root(1L)).initialize();
        AlgorithmState<BitString> second = wire(SeededRandomSource.root(2L)).initialize();

        assertNotEquals(first.population(), second.population());
    }

    @Test
    void eliteBestFitnessNeverRegressesAcrossASampledRun() {
        GeneticAlgorithm<BitString> ga = wire(SeededRandomSource.root(7L));
        AlgorithmState<BitString> state = ga.initialize();
        double best = bestFitness(state);
        for (int i = 0; i < 30; i++) {
            state = ga.step(state);
            double next = bestFitness(state);
            if (next < best) {
                throw new AssertionError("best fitness regressed from " + best
                        + " to " + next + " at iteration " + state.iteration());
            }
            best = next;
        }
    }
}

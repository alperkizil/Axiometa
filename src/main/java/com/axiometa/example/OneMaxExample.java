package com.axiometa.example;

import com.axiometa.algorithm.AlgorithmRunner;
import com.axiometa.algorithm.Terminations;
import com.axiometa.bitstring.BitFlipMutation;
import com.axiometa.bitstring.BitString;
import com.axiometa.bitstring.OnePointCrossover;
import com.axiometa.bitstring.RandomBitStringInitialization;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Population;
import com.axiometa.evaluation.CachingEvaluator;
import com.axiometa.evaluation.SequentialEvaluator;
import com.axiometa.ga.GenerationalReplacement;
import com.axiometa.ga.GeneticAlgorithm;
import com.axiometa.ga.TournamentSelection;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.phase.Termination;
import com.axiometa.random.RandomSource;
import com.axiometa.random.SeededRandomSource;

/**
 * Runnable demonstration of the whole framework stack: the generational
 * {@link GeneticAlgorithm} solving {@link OneMaxProblem} on 50-bit strings,
 * wired exactly the way a framework user is expected to wire it — one root
 * seed with named child streams, sequential evaluation behind the fitness
 * cache, pluggable phase components, composed termination, and the shared
 * {@link AlgorithmRunner} loop.
 *
 * <p>The run is fully reproducible: the same root seed always produces the
 * identical run, iteration by iteration.
 */
public final class OneMaxExample {

    /** Bits per candidate; the optimum fitness equals this length. */
    static final int BIT_COUNT = 50;

    /** Members per generation; must be even so parents pair exactly. */
    static final int POPULATION_SIZE = 40;

    /** Contenders sampled per tournament pick. */
    static final int TOURNAMENT_SIZE = 3;

    /** Chance that a parent pair is actually cut and recombined. */
    static final double CROSSOVER_PROBABILITY = 0.9;

    /** Per-bit flip chance: on average one flipped bit per child. */
    static final double PER_BIT_MUTATION_PROBABILITY = 1.0 / BIT_COUNT;

    /** Best current members preserved unchanged into the next generation. */
    static final int ELITISM_COUNT = 1;

    /** Iteration budget in case the optimum is never found. */
    static final int MAX_ITERATIONS = 300;

    private OneMaxExample() {
    }

    /**
     * Runs the example with root seed {@code 42} and prints the summary.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        run(42L);
    }

    /**
     * Wires the full stack, runs it to termination, prints a summary, and
     * returns the final state.
     *
     * @param rootSeed root seed of the run; equal seeds yield identical runs
     * @return the final algorithm state
     */
    static AlgorithmState<BitString> run(long rootSeed) {
        // 1. The problem: what "good" means. OneMax counts one-bits and wants
        //    as many as possible.
        OneMaxProblem problem = new OneMaxProblem();

        // 2. Randomness: one root seed, one independently named child stream
        //    per stochastic component, so the run is reproducible and no
        //    component's draws disturb another's.
        RandomSource random = SeededRandomSource.root(rootSeed);

        // 3. Evaluation: sequential scoring on this thread, behind the
        //    fitness cache — legal because OneMax declares itself
        //    deterministic, so repeated bit strings are served from memory.
        CachingEvaluator<BitString> evaluator =
                new CachingEvaluator<>(new SequentialEvaluator<>(problem), problem);

        // 4. The algorithm: a thin orchestrator over the five pluggable
        //    phase components.
        GeneticAlgorithm<BitString> algorithm = new GeneticAlgorithm<>(POPULATION_SIZE,
                new RandomBitStringInitialization(BIT_COUNT, random.child("initialization")),
                evaluator,
                new TournamentSelection<>(ObjectiveSense.MAXIMIZE, TOURNAMENT_SIZE,
                        random.child("selection")),
                new OnePointCrossover(CROSSOVER_PROBABILITY, random.child("crossover")),
                new BitFlipMutation(PER_BIT_MUTATION_PROBABILITY, random.child("mutation")),
                new GenerationalReplacement<>(ObjectiveSense.MAXIMIZE, ELITISM_COUNT));

        // 5. Stopping: a custom "optimum reached" condition combined with an
        //    iteration budget so the run always ends.
        Termination<BitString> reachedOptimum =
                state -> best(state.population()).evaluation().objectiveValue(0) >= BIT_COUNT;
        Termination<BitString> termination = Terminations.anyOf(
                Terminations.maxIterations(MAX_ITERATIONS), reachedOptimum);

        // 6. The shared loop: initialize, then step until termination.
        AlgorithmState<BitString> finalState = AlgorithmRunner.run(algorithm, termination);

        EvaluatedCandidate<BitString> best = best(finalState.population());
        System.out.println("root seed:        " + rootSeed);
        System.out.println("iterations:       " + finalState.iteration());
        System.out.println("real evaluations: " + finalState.evaluationCount());
        System.out.println("cache hits:       " + evaluator.cacheHitCount());
        System.out.println("best fitness:     " + (int) best.evaluation().objectiveValue(0)
                + " / " + BIT_COUNT);
        System.out.println("best bit string:  " + best.candidate().representation());
        return finalState;
    }

    /**
     * Returns the population member with the highest fitness.
     *
     * @param population the population to scan
     * @return the member with the largest objective value
     */
    static EvaluatedCandidate<BitString> best(Population<BitString> population) {
        EvaluatedCandidate<BitString> best = population.members().get(0);
        for (EvaluatedCandidate<BitString> member : population.members()) {
            if (member.evaluation().objectiveValue(0) > best.evaluation().objectiveValue(0)) {
                best = member;
            }
        }
        return best;
    }
}

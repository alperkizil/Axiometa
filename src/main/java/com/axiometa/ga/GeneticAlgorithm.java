package com.axiometa.ga;

import com.axiometa.algorithm.Algorithm;
import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import com.axiometa.evaluation.EvaluationOutcome;
import com.axiometa.evaluation.Evaluator;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.phase.Crossover;
import com.axiometa.phase.Initialization;
import com.axiometa.phase.Mutation;
import com.axiometa.phase.OffspringPair;
import com.axiometa.phase.Replacement;
import com.axiometa.phase.Selection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generational genetic algorithm as a thin orchestrator over the phase
 * contracts: it wires initialization, evaluation, selection, crossover,
 * mutation, and replacement together and owns no phase logic, no loop, and
 * no termination (see {@link com.axiometa.algorithm.AlgorithmRunner}).
 *
 * <p>Each step selects {@code populationSize} parents, pairs them in
 * selection order, produces two mutated children per pair, evaluates all
 * offspring, and delegates generation turnover to the replacement policy.
 * Any evaluation failure ends the run with {@link IllegalStateException} —
 * a research run must die loudly, never breed on missing fitness.
 *
 * @param <R> the representation type
 */
public final class GeneticAlgorithm<R extends Representation> implements Algorithm<R> {

    private final int populationSize;
    private final Initialization<R> initialization;
    private final Evaluator<R> evaluator;
    private final Selection<R> selection;
    private final Crossover<R> crossover;
    private final Mutation<R> mutation;
    private final Replacement<R> replacement;

    /**
     * Wires a genetic algorithm.
     *
     * @param populationSize generation size; must be an even number
     *                       {@code >= 2} so parents pair exactly
     * @param initialization creates generation zero; must not be null
     * @param evaluator      evaluates candidates; must not be null
     * @param selection      picks parents; must not be null
     * @param crossover      recombines parent pairs; must not be null
     * @param mutation       mutates each child; must not be null
     * @param replacement    forms the next generation; must not be null
     * @throws NullPointerException     if any component is null
     * @throws IllegalArgumentException if {@code populationSize} is odd or
     *                                  {@code < 2}
     */
    public GeneticAlgorithm(int populationSize, Initialization<R> initialization,
            Evaluator<R> evaluator, Selection<R> selection, Crossover<R> crossover,
            Mutation<R> mutation, Replacement<R> replacement) {
        if (populationSize < 2 || populationSize % 2 != 0) {
            throw new IllegalArgumentException(
                    "populationSize must be an even number >= 2 but was " + populationSize);
        }
        this.populationSize = populationSize;
        this.initialization =
                Objects.requireNonNull(initialization, "initialization must not be null");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator must not be null");
        this.selection = Objects.requireNonNull(selection, "selection must not be null");
        this.crossover = Objects.requireNonNull(crossover, "crossover must not be null");
        this.mutation = Objects.requireNonNull(mutation, "mutation must not be null");
        this.replacement = Objects.requireNonNull(replacement, "replacement must not be null");
    }

    @Override
    public AlgorithmState<R> initialize() {
        List<Candidate<R>> candidates = initialization.initialize(populationSize);
        if (candidates.size() != populationSize) {
            throw new IllegalStateException("initialization returned " + candidates.size()
                    + " candidates but " + populationSize + " were requested");
        }
        Population<R> population = new Population<>(evaluateAll(candidates));
        return new AlgorithmState<>(0, evaluator.evaluationCount(), population);
    }

    @Override
    public AlgorithmState<R> step(AlgorithmState<R> state) {
        Objects.requireNonNull(state, "state must not be null");
        List<EvaluatedCandidate<R>> parents =
                selection.select(state.population(), populationSize);
        if (parents.size() != populationSize) {
            throw new IllegalStateException("selection returned " + parents.size()
                    + " parents but " + populationSize + " were requested");
        }
        List<Candidate<R>> offspring = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i += 2) {
            OffspringPair<R> children = crossover.crossover(
                    parents.get(i).candidate(), parents.get(i + 1).candidate());
            offspring.add(mutation.mutate(children.first()));
            offspring.add(mutation.mutate(children.second()));
        }
        Population<R> next = replacement.replace(state.population(), evaluateAll(offspring));
        return new AlgorithmState<>(state.iteration() + 1, evaluator.evaluationCount(), next);
    }

    private List<EvaluatedCandidate<R>> evaluateAll(List<Candidate<R>> candidates) {
        List<EvaluationOutcome<R>> outcomes = evaluator.evaluate(candidates);
        List<EvaluatedCandidate<R>> evaluated = new ArrayList<>(outcomes.size());
        int failureCount = 0;
        RuntimeException firstCause = null;
        for (EvaluationOutcome<R> outcome : outcomes) {
            switch (outcome) {
                case EvaluationOutcome.Success<R> success -> evaluated.add(success.result());
                case EvaluationOutcome.Failure<R> failure -> {
                    failureCount++;
                    if (firstCause == null) {
                        firstCause = failure.cause();
                    }
                }
            }
        }
        if (failureCount > 0) {
            throw new IllegalStateException("evaluation failed for " + failureCount + " of "
                    + outcomes.size() + " candidates", firstCause);
        }
        return evaluated;
    }
}

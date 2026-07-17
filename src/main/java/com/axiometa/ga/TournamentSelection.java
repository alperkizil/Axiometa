package com.axiometa.ga;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import com.axiometa.phase.Selection;
import com.axiometa.random.RandomSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * k-tournament selection: each pick samples {@code tournamentSize} members
 * uniformly with replacement and selects the best by single-objective
 * fitness; on ties the earliest sampled member wins.
 *
 * <p>The same member may be selected any number of times across picks.
 * Exactly {@code tournamentSize} random draws are consumed per pick.
 * Single-objective, unconstrained evaluations only — anything else is
 * rejected with {@link IllegalArgumentException}.
 *
 * @param <R> the representation type
 */
public final class TournamentSelection<R extends Representation> implements Selection<R> {

    private final Comparator<EvaluatedCandidate<R>> bestFirst;
    private final int tournamentSize;
    private final RandomSource random;

    /**
     * Creates the selector.
     *
     * @param sense          direction of improvement of the single objective;
     *                       must not be null
     * @param tournamentSize contenders sampled per pick; must be {@code >= 1}
     * @param random         this component's own random stream; must not be
     *                       null
     * @throws NullPointerException     if {@code sense} or {@code random} is
     *                                  null
     * @throws IllegalArgumentException if {@code tournamentSize < 1}
     */
    public TournamentSelection(ObjectiveSense sense, int tournamentSize, RandomSource random) {
        Objects.requireNonNull(sense, "sense must not be null");
        if (tournamentSize < 1) {
            throw new IllegalArgumentException(
                    "tournamentSize must be >= 1 but was " + tournamentSize);
        }
        this.bestFirst = SingleObjectiveFitness.bestFirst(sense);
        this.tournamentSize = tournamentSize;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public List<EvaluatedCandidate<R>> select(Population<R> population, int count) {
        Objects.requireNonNull(population, "population must not be null");
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1 but was " + count);
        }
        List<EvaluatedCandidate<R>> members = population.members();
        List<EvaluatedCandidate<R>> selected = new ArrayList<>(count);
        for (int pick = 0; pick < count; pick++) {
            EvaluatedCandidate<R> best = members.get(random.nextInt(members.size()));
            SingleObjectiveFitness.validated(best.evaluation());
            for (int contender = 1; contender < tournamentSize; contender++) {
                EvaluatedCandidate<R> sampled = members.get(random.nextInt(members.size()));
                if (bestFirst.compare(sampled, best) < 0) {
                    best = sampled;
                }
            }
            selected.add(best);
        }
        return List.copyOf(selected);
    }
}

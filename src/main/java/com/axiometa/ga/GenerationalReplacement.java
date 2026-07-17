package com.axiometa.ga;

import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Population;
import com.axiometa.core.Representation;
import com.axiometa.phase.Replacement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Elitist generational replacement: the next generation consists of the best
 * {@code elitismCount} members of the current population (stable sort — ties
 * keep their current order) followed by the offspring in their given order,
 * sized exactly to the current population size. {@code elitismCount == 0} is
 * pure generational replacement.
 *
 * @param <R> the representation type
 */
public final class GenerationalReplacement<R extends Representation>
        implements Replacement<R> {

    private final Comparator<EvaluatedCandidate<R>> bestFirst;
    private final int elitismCount;

    /**
     * Creates the policy.
     *
     * @param sense        direction of improvement of the single objective;
     *                     must not be null
     * @param elitismCount current-generation members preserved; must be
     *                     {@code >= 0} and smaller than the population size
     *                     at every {@code replace} call
     * @throws NullPointerException     if {@code sense} is null
     * @throws IllegalArgumentException if {@code elitismCount < 0}
     */
    public GenerationalReplacement(ObjectiveSense sense, int elitismCount) {
        Objects.requireNonNull(sense, "sense must not be null");
        if (elitismCount < 0) {
            throw new IllegalArgumentException(
                    "elitismCount must be >= 0 but was " + elitismCount);
        }
        this.bestFirst = SingleObjectiveFitness.bestFirst(sense);
        this.elitismCount = elitismCount;
    }

    @Override
    public Population<R> replace(Population<R> current, List<EvaluatedCandidate<R>> offspring) {
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(offspring, "offspring must not be null");
        int size = current.size();
        if (elitismCount >= size) {
            throw new IllegalArgumentException("elitismCount must be smaller than the "
                    + "population size " + size + " but was " + elitismCount);
        }
        int needed = size - elitismCount;
        if (offspring.size() < needed) {
            throw new IllegalArgumentException("replacement needs at least " + needed
                    + " offspring but got " + offspring.size());
        }
        for (int i = 0; i < needed; i++) {
            if (offspring.get(i) == null) {
                throw new NullPointerException("offspring[" + i + "] must not be null");
            }
        }
        List<EvaluatedCandidate<R>> next = new ArrayList<>(size);
        if (elitismCount > 0) {
            List<EvaluatedCandidate<R>> ranked = new ArrayList<>(current.members());
            ranked.sort(bestFirst);
            next.addAll(ranked.subList(0, elitismCount));
        }
        next.addAll(offspring.subList(0, needed));
        return new Population<>(next);
    }
}

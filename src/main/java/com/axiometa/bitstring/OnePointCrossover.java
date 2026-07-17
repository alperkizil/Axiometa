package com.axiometa.bitstring;

import com.axiometa.core.Candidate;
import com.axiometa.phase.Crossover;
import com.axiometa.phase.OffspringPair;
import com.axiometa.random.RandomSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One-point crossover: with the configured probability, a cut point in
 * {@code [1, length - 1]} is drawn and the parents' tails are exchanged;
 * otherwise the children are the parents themselves.
 *
 * <p>Parents of length one have no cut point and always yield the parents as
 * children, without consuming any random draws. Otherwise exactly one
 * probability draw is consumed, plus one cut draw when crossover happens.
 */
public final class OnePointCrossover implements Crossover<BitString> {

    private final double probability;
    private final RandomSource random;

    /**
     * Creates the operator.
     *
     * @param probability chance of performing a cut, in {@code [0, 1]}
     * @param random      this component's own random stream; must not be null
     * @throws NullPointerException     if {@code random} is null
     * @throws IllegalArgumentException if {@code probability} is NaN or
     *                                  outside {@code [0, 1]}
     */
    public OnePointCrossover(double probability, RandomSource random) {
        if (Double.isNaN(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "probability must be in [0, 1] but was " + probability);
        }
        this.probability = probability;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public OffspringPair<BitString> crossover(
            Candidate<BitString> first, Candidate<BitString> second) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
        BitString a = first.representation();
        BitString b = second.representation();
        if (a.length() != b.length()) {
            throw new IllegalArgumentException("parents must have equal lengths but were "
                    + a.length() + " and " + b.length());
        }
        int length = a.length();
        if (length < 2 || random.nextDouble() >= probability) {
            return new OffspringPair<>(first, second);
        }
        int cut = 1 + random.nextInt(length - 1);
        List<Boolean> childA = new ArrayList<>(length);
        List<Boolean> childB = new ArrayList<>(length);
        childA.addAll(a.bits().subList(0, cut));
        childA.addAll(b.bits().subList(cut, length));
        childB.addAll(b.bits().subList(0, cut));
        childB.addAll(a.bits().subList(cut, length));
        return new OffspringPair<>(
                new Candidate<>(new BitString(childA)),
                new Candidate<>(new BitString(childB)));
    }
}

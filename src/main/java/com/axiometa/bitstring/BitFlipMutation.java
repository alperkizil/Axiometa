package com.axiometa.bitstring;

import com.axiometa.core.Candidate;
import com.axiometa.phase.Mutation;
import com.axiometa.random.RandomSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Independent per-bit flip mutation: each bit flips with the configured
 * probability. Exactly one draw is consumed per bit, so the operator's random
 * consumption is deterministic regardless of outcomes.
 */
public final class BitFlipMutation implements Mutation<BitString> {

    private final double perBitProbability;
    private final RandomSource random;

    /**
     * Creates the operator.
     *
     * @param perBitProbability chance of flipping each bit, in {@code [0, 1]}
     * @param random            this component's own random stream; must not
     *                          be null
     * @throws NullPointerException     if {@code random} is null
     * @throws IllegalArgumentException if {@code perBitProbability} is NaN or
     *                                  outside {@code [0, 1]}
     */
    public BitFlipMutation(double perBitProbability, RandomSource random) {
        if (Double.isNaN(perBitProbability)
                || perBitProbability < 0.0 || perBitProbability > 1.0) {
            throw new IllegalArgumentException(
                    "perBitProbability must be in [0, 1] but was " + perBitProbability);
        }
        this.perBitProbability = perBitProbability;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public Candidate<BitString> mutate(Candidate<BitString> candidate) {
        Objects.requireNonNull(candidate, "candidate must not be null");
        List<Boolean> bits = candidate.representation().bits();
        List<Boolean> mutated = new ArrayList<>(bits.size());
        for (Boolean bit : bits) {
            mutated.add(random.nextDouble() < perBitProbability ? !bit : bit);
        }
        return new Candidate<>(new BitString(mutated));
    }
}

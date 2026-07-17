package com.axiometa.bitstring;

import com.axiometa.core.Candidate;
import com.axiometa.phase.Initialization;
import com.axiometa.random.RandomSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Creates candidates with uniformly random bits.
 */
public final class RandomBitStringInitialization implements Initialization<BitString> {

    private final int bitCount;
    private final RandomSource random;

    /**
     * Creates the initializer.
     *
     * @param bitCount bits per candidate; must be {@code >= 1}
     * @param random   this component's own random stream; must not be null
     * @throws NullPointerException     if {@code random} is null
     * @throws IllegalArgumentException if {@code bitCount < 1}
     */
    public RandomBitStringInitialization(int bitCount, RandomSource random) {
        if (bitCount < 1) {
            throw new IllegalArgumentException("bitCount must be >= 1 but was " + bitCount);
        }
        this.bitCount = bitCount;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public List<Candidate<BitString>> initialize(int populationSize) {
        if (populationSize < 1) {
            throw new IllegalArgumentException(
                    "populationSize must be >= 1 but was " + populationSize);
        }
        List<Candidate<BitString>> candidates = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            List<Boolean> bits = new ArrayList<>(bitCount);
            for (int bit = 0; bit < bitCount; bit++) {
                bits.add(random.nextBoolean());
            }
            candidates.add(new Candidate<>(new BitString(bits)));
        }
        return List.copyOf(candidates);
    }
}

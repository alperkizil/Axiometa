package com.axiometa.phase;

import com.axiometa.core.Candidate;
import java.util.Objects;

/**
 * The two children produced by one {@link Crossover} application.
 *
 * <p>A dedicated pair type makes a wrong child count impossible by
 * construction.
 *
 * @param <R>    the representation type
 * @param first  the first child; must not be null
 * @param second the second child; must not be null
 */
public record OffspringPair<R>(Candidate<R> first, Candidate<R> second) {

    /**
     * Validates the pair.
     *
     * @throws NullPointerException if {@code first} or {@code second} is null
     */
    public OffspringPair {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
    }
}

package com.axiometa.core;

import java.util.Objects;

/**
 * Typed wrapper around one candidate representation.
 *
 * <p>The representation type {@code R} must be deeply immutable and must
 * define value-based {@link Object#equals(Object)} and
 * {@link Object#hashCode()}. Candidate equality delegates to the
 * representation (record component equality), so two candidates are equal if
 * and only if their representations are equal by value. This equality is the
 * contractual basis for the future fitness cache key: evaluations are cached
 * by representation value, never by object identity.
 *
 * @param <R>            the representation type
 * @param representation the wrapped representation; must not be null
 */
public record Candidate<R>(R representation) {

    /**
     * Validates the wrapper.
     *
     * @throws NullPointerException if {@code representation} is null
     */
    public Candidate {
        Objects.requireNonNull(representation, "representation must not be null");
    }
}

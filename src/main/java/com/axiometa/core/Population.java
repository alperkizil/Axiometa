package com.axiometa.core;

import java.util.List;
import java.util.Objects;

/**
 * Immutable, non-empty collection of evaluated candidates forming one
 * generation.
 *
 * <p>The member list is defensively copied to an immutable list at
 * construction; {@link #members()} returns that copy. A population always
 * contains at least one member: a zero-member generation is meaningless for
 * the supported algorithms.
 *
 * @param <R>     the representation type
 * @param members the evaluated candidates; must not be null, empty, or
 *                contain nulls
 */
public record Population<R>(List<EvaluatedCandidate<R>> members) {

    /**
     * Validates and defensively copies the member list.
     *
     * @throws NullPointerException     if {@code members} or any element is
     *                                  null
     * @throws IllegalArgumentException if {@code members} is empty
     */
    public Population {
        Objects.requireNonNull(members, "members must not be null");
        if (members.isEmpty()) {
            throw new IllegalArgumentException("members must not be empty");
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i) == null) {
                throw new NullPointerException("members[" + i + "] must not be null");
            }
        }
        members = List.copyOf(members);
    }

    /**
     * Returns the number of members.
     *
     * @return the population size, always {@code >= 1}
     */
    public int size() {
        return members.size();
    }
}

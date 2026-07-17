package com.axiometa.core;

import java.util.Objects;

/**
 * Declaration of a single optimization objective: a human-readable name and
 * the {@link ObjectiveSense sense} in which its values improve.
 *
 * <p>An objective declaration carries no evaluation logic; computing objective
 * values is the job of {@link Problem#evaluate(Object)}. Two declarations are
 * equal if and only if their names and senses are equal.
 *
 * @param name  human-readable objective name; must not be null or blank
 * @param sense direction of improvement; must not be null
 */
public record Objective(String name, ObjectiveSense sense) {

    /**
     * Validates the declaration.
     *
     * @throws NullPointerException     if {@code name} or {@code sense} is null
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public Objective {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(sense, "sense must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}

package com.axiometa.core;

import java.util.Objects;

/**
 * Declaration of a single constraint by name.
 *
 * <p>A constraint declaration carries no evaluation logic; computing the
 * violation magnitude is the job of {@link Problem#evaluate(Object)}. The
 * convention for violation values is defined by {@link Evaluation}: a
 * magnitude {@code >= 0} where {@code 0.0} means satisfied. Mapping an
 * equality constraint onto this convention (including any tolerance) is the
 * responsibility of the problem author.
 *
 * @param name human-readable constraint name; must not be null or blank
 */
public record Constraint(String name) {

    /**
     * Validates the declaration.
     *
     * @throws NullPointerException     if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public Constraint {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}

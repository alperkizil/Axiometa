package com.axiometa.core;

/**
 * Direction in which an {@link Objective}'s values improve.
 *
 * <p>Objective values are always stored and reported raw; the framework never
 * negates or otherwise transforms them. Components that compare candidates
 * must consult the sense of each objective.
 */
public enum ObjectiveSense {

    /** Smaller objective values are better. */
    MINIMIZE,

    /** Larger objective values are better. */
    MAXIMIZE
}

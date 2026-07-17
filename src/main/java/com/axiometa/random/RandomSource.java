package com.axiometa.random;

/**
 * Deterministic source of pseudo-random values with independently seeded,
 * named child streams.
 *
 * <p>Contract:
 *
 * <ul>
 *   <li>Instances are stateful and <strong>not thread-safe</strong>. Each
 *       instance has exactly one owning component; components and threads
 *       must never share an instance. Give each consumer its own
 *       {@link #child(String)} stream instead.</li>
 *   <li>{@code child} derivation depends only on this source's immutable
 *       seed and the given name — never on how many values have been drawn.
 *       Wiring order therefore cannot affect reproducibility.</li>
 *   <li>Derivation is a pure function: the same name on an equally seeded
 *       source always yields an identical stream. That is the determinism
 *       guarantee — and the reason wiring must give distinct components
 *       distinct names, or they will receive perfectly correlated
 *       randomness.</li>
 * </ul>
 */
public interface RandomSource {

    /**
     * Returns a uniformly distributed value in {@code [0, bound)}.
     *
     * @param bound the exclusive upper bound; must be {@code >= 1}
     * @return a value in {@code [0, bound)}
     * @throws IllegalArgumentException if {@code bound < 1}
     */
    int nextInt(int bound);

    /**
     * Returns a uniformly distributed value in {@code [0.0, 1.0)}.
     *
     * @return a value in {@code [0.0, 1.0)}
     */
    double nextDouble();

    /**
     * Returns a uniformly distributed boolean.
     *
     * @return {@code true} or {@code false} with equal probability
     */
    boolean nextBoolean();

    /**
     * Derives the independent child stream with the given name.
     *
     * @param name stable stream name; must not be null or blank
     * @return the child source; equal seeds and equal names always yield an
     *         identical stream
     * @throws NullPointerException     if {@code name} is null
     * @throws IllegalArgumentException if {@code name} is blank
     */
    RandomSource child(String name);
}

/**
 * Deterministic random infrastructure: one root seed, independent named child
 * streams.
 *
 * <p>An experiment owns a single root {@link com.axiometa.random.RandomSource}
 * created from its root seed. Every component that needs randomness receives
 * its own child stream, derived from the parent's seed and a stable name
 * (constructor injection per the phase contracts). Derivation is a pure
 * function of {@code (parent seed, name)} — platform-stable, independent of
 * draw order, and never based on {@code String.hashCode()}.
 *
 * <p>No two components or threads may share one source instance: instances
 * are stateful and not thread-safe by design. Reproducibility comes from the
 * root seed and the wiring names, not from scheduling or identity.
 */
package com.axiometa.random;

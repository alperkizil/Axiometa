/**
 * Fixed-length binary representation and its classic variation operators.
 *
 * <p>Specification provenance: binary (bit-string) representation with
 * one-point crossover and independent per-bit flip mutation as specified in
 * A.E. Eiben and J.E. Smith, <em>Introduction to Evolutionary Computing</em>,
 * 2nd ed., Springer, 2015 (ch. 4, binary representation and its variation
 * operators), tracing back to J.H. Holland, <em>Adaptation in Natural and
 * Artificial Systems</em>, 1975. No code was taken from any optimization
 * library.
 *
 * <p>All operators receive their {@link com.axiometa.random.RandomSource}
 * stream by constructor injection and consume a deterministic number of draws
 * per call, so runs are reproducible from the root seed and wiring names.
 */
package com.axiometa.bitstring;

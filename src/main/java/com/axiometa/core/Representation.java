package com.axiometa.core;

/**
 * Marker for types usable as candidate representations.
 *
 * <p>Implementing this interface is a declaration that the type honors the
 * representation contract:
 *
 * <ul>
 *   <li>deeply immutable — no observable state changes after construction and
 *       no exposure of mutable internals;</li>
 *   <li>value-based {@link Object#equals(Object)} and
 *       {@link Object#hashCode()} — equal-by-value instances are
 *       interchangeable. This equality is the contractual basis for the
 *       future fitness cache key.</li>
 * </ul>
 *
 * <p>The compiler cannot verify these rules; the marker records the promise
 * and lets phase contracts accept only types that made it. Algorithm phase
 * contracts bound their representation type parameter to this interface,
 * while the core model types ({@link Problem}, {@link Candidate}) remain
 * unbounded.
 */
public interface Representation {
}

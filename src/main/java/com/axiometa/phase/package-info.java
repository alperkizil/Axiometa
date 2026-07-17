/**
 * Contracts for the six algorithm phases: initialization, selection,
 * crossover, mutation, replacement, and termination.
 *
 * <p>Phases are pluggable, independently testable components; algorithm loops
 * are thin orchestrators that wire phases together and own no phase logic
 * themselves. Every phase contract extends the
 * {@link com.axiometa.phase.AlgorithmPhase} marker and bounds its
 * representation type parameter to
 * {@link com.axiometa.core.Representation}.
 *
 * <p>Stochastic phase implementations receive their randomness by constructor
 * injection of a dedicated, independently seeded random stream; phase
 * signatures never carry a randomness parameter. No two components may share
 * one mutable random source.
 *
 * <p>This package contains contracts only; concrete phase implementations
 * arrive with the algorithms that need them.
 */
package com.axiometa.phase;

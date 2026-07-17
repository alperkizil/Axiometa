package com.axiometa.core;

/**
 * Whether a {@link Problem}'s evaluation is deterministic or stochastic.
 *
 * <p>The declaration governs fitness caching: caching a stochastic problem
 * would silently convert it into a deterministic one, so caches must bypass
 * stochastic problems entirely. There is no default — every problem declares
 * its semantics explicitly.
 */
public enum EvaluationSemantics {

    /**
     * Evaluating equal representations always produces equal evaluations;
     * results may safely be cached.
     */
    DETERMINISTIC,

    /**
     * Repeated evaluation of the same representation may produce different
     * evaluations; results must never be cached.
     */
    STOCHASTIC
}

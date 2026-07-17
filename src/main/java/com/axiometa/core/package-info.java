/**
 * Immutable core model for optimization problems: {@link com.axiometa.core.Problem},
 * candidate wrappers, objective and constraint declarations, and evaluation results.
 *
 * <p>Every concrete type in this package is deeply immutable: types are final,
 * fields are final, array and collection inputs are defensively copied, and no
 * mutable internal state is ever exposed. Instances are therefore safe to share
 * freely across components and threads.
 *
 * <p>Constructors validate eagerly. Null arguments are rejected with
 * {@link java.lang.NullPointerException}; invalid values are rejected with
 * {@link java.lang.IllegalArgumentException}. Exception messages name the
 * offending parameter and the violated rule. Indexed accessors reject
 * out-of-range indices with {@link java.lang.IndexOutOfBoundsException}.
 *
 * <p>Constraint convention: evaluating a constraint yields a violation
 * magnitude {@code >= 0}, where {@code 0.0} means satisfied. A candidate is
 * feasible if and only if every constraint violation is zero. Mapping an
 * equality constraint onto this convention (including any tolerance) is the
 * responsibility of the problem author.
 */
package com.axiometa.core;

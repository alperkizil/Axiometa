package com.axiometa.core;

import java.util.List;

/**
 * An optimization problem over representations of type {@code R}: it declares
 * its objectives and constraints and evaluates representations against them.
 *
 * <p>Implementation contract:
 *
 * <ul>
 *   <li>{@link #objectives()} returns a non-empty immutable list and
 *       {@link #constraints()} a possibly empty immutable list. Both must
 *       return equal lists with no null elements on every call: declarations
 *       are fixed for the lifetime of the problem.</li>
 *   <li>{@link #evaluate(Object)} must reject a null representation with
 *       {@link NullPointerException}, must never return null, and must return
 *       an {@link Evaluation} whose objective and constraint counts equal the
 *       sizes of the declared lists, in declaration order.</li>
 *   <li>{@link #evaluationSemantics()} must return the same non-null constant
 *       on every call: the semantics are fixed for the lifetime of the
 *       problem.</li>
 *   <li>Thread-safety of implementations is intentionally unspecified at this
 *       stage; it will be fixed by the evaluator contract.</li>
 * </ul>
 *
 * @param <R> the representation type; must be deeply immutable with
 *            value-based {@code equals}/{@code hashCode} (see {@link Candidate})
 */
public interface Problem<R> {

    /**
     * Returns the declared objectives in evaluation order.
     *
     * @return non-empty immutable list of objective declarations
     */
    List<Objective> objectives();

    /**
     * Returns the declared constraints in evaluation order.
     *
     * @return possibly empty immutable list of constraint declarations
     */
    List<Constraint> constraints();

    /**
     * Evaluates one representation.
     *
     * @param representation the representation to evaluate; must not be null
     * @return the evaluation, with counts matching the declared objectives and
     *         constraints
     * @throws NullPointerException if {@code representation} is null
     */
    Evaluation evaluate(R representation);

    /**
     * Returns whether {@link #evaluate(Object)} is deterministic or
     * stochastic.
     *
     * @return the evaluation semantics, never null, constant for this problem
     */
    EvaluationSemantics evaluationSemantics();
}

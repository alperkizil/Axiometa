/**
 * Evaluation infrastructure: mapping candidates to evaluated candidates
 * against a {@link com.axiometa.core.Problem}.
 *
 * <p>An evaluator is constructed for one problem and evaluates batches of
 * candidates, returning exactly one
 * {@link com.axiometa.evaluation.EvaluationOutcome} per candidate in input
 * order — regardless of internal completion order, which later parallel
 * evaluators must also honor. Failures of individual evaluations are
 * collected as outcomes, never silently dropped; JVM {@link java.lang.Error}s
 * always propagate immediately.
 *
 * <p>Evaluators track the cumulative number of evaluation attempts they have
 * made against the problem; this counter is the scientific accounting unit
 * for experiment budgets. Evaluator instances have one owner and are not
 * thread-safe unless a concrete implementation documents otherwise.
 */
package com.axiometa.evaluation;

package com.axiometa.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable result of evaluating one candidate: objective values and
 * constraint violation magnitudes, ordered exactly as the producing
 * {@link Problem} declares its objectives and constraints.
 *
 * <p>An evaluation is a standalone value; it does not reference the problem
 * or the candidate it was computed for. Whoever pairs an evaluation with a
 * problem is responsible for the counts matching that problem's declarations.
 *
 * <p>Objective values may be any double except {@code NaN}; infinities are
 * permitted. Constraint violations additionally must be {@code >= 0}, where
 * {@code 0.0} means satisfied.
 *
 * <p>Two evaluations are equal if and only if their objective values and
 * constraint violations are equal by exact numerical representation
 * ({@link Arrays#equals(double[], double[])} semantics). In particular
 * {@code 0.0} and {@code -0.0} are distinct for equality, while
 * {@link #isFeasible()} compares numerically and treats a {@code -0.0}
 * violation as satisfied.
 */
public final class Evaluation {

    private final double[] objectiveValues;
    private final double[] constraintViolations;

    /**
     * Creates an evaluation from objective values and constraint violations.
     * Both arrays are defensively copied.
     *
     * @param objectiveValues      objective values in the problem's declared
     *                             objective order; at least one, none NaN
     * @param constraintViolations violation magnitudes in the problem's
     *                             declared constraint order; possibly empty,
     *                             none NaN, each {@code >= 0}
     * @throws NullPointerException     if either array is null
     * @throws IllegalArgumentException if {@code objectiveValues} is empty,
     *                                  any value is NaN, or any violation is
     *                                  negative
     */
    public Evaluation(double[] objectiveValues, double[] constraintViolations) {
        Objects.requireNonNull(objectiveValues, "objectiveValues must not be null");
        Objects.requireNonNull(constraintViolations, "constraintViolations must not be null");
        if (objectiveValues.length == 0) {
            throw new IllegalArgumentException("objectiveValues must contain at least one value");
        }
        // Copy before validating so a caller mutating its array concurrently
        // cannot bypass validation of the values actually stored.
        this.objectiveValues = objectiveValues.clone();
        this.constraintViolations = constraintViolations.clone();
        for (int i = 0; i < this.objectiveValues.length; i++) {
            if (Double.isNaN(this.objectiveValues[i])) {
                throw new IllegalArgumentException("objectiveValues[" + i + "] must not be NaN");
            }
        }
        for (int i = 0; i < this.constraintViolations.length; i++) {
            double violation = this.constraintViolations[i];
            if (Double.isNaN(violation)) {
                throw new IllegalArgumentException("constraintViolations[" + i + "] must not be NaN");
            }
            if (violation < 0.0) {
                throw new IllegalArgumentException(
                        "constraintViolations[" + i + "] must be >= 0 but was " + violation);
            }
        }
    }

    /**
     * Returns the number of objective values.
     *
     * @return the objective count, always {@code >= 1}
     */
    public int objectiveCount() {
        return objectiveValues.length;
    }

    /**
     * Returns the objective value at {@code index} in the problem's declared
     * objective order.
     *
     * @param index objective index
     * @return the objective value
     * @throws IndexOutOfBoundsException if {@code index} is not in
     *                                   {@code [0, objectiveCount())}
     */
    public double objectiveValue(int index) {
        Objects.checkIndex(index, objectiveValues.length);
        return objectiveValues[index];
    }

    /**
     * Returns the number of constraint violations.
     *
     * @return the constraint count, possibly {@code 0}
     */
    public int constraintViolationCount() {
        return constraintViolations.length;
    }

    /**
     * Returns the violation magnitude at {@code index} in the problem's
     * declared constraint order.
     *
     * @param index constraint index
     * @return the violation magnitude, always {@code >= 0}
     * @throws IndexOutOfBoundsException if {@code index} is not in
     *                                   {@code [0, constraintViolationCount())}
     */
    public double constraintViolation(int index) {
        Objects.checkIndex(index, constraintViolations.length);
        return constraintViolations[index];
    }

    /**
     * Returns whether every constraint violation is numerically zero. An
     * evaluation with no constraints is feasible.
     *
     * @return {@code true} if all violations are zero
     */
    public boolean isFeasible() {
        for (double violation : constraintViolations) {
            if (violation != 0.0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Evaluation that
                && Arrays.equals(this.objectiveValues, that.objectiveValues)
                && Arrays.equals(this.constraintViolations, that.constraintViolations);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(objectiveValues) + Arrays.hashCode(constraintViolations);
    }

    @Override
    public String toString() {
        return "Evaluation[objectiveValues=" + Arrays.toString(objectiveValues)
                + ", constraintViolations=" + Arrays.toString(constraintViolations) + "]";
    }
}

package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EvaluationTest {

    @Test
    void storesValuesInGivenOrder() {
        Evaluation evaluation = new Evaluation(
                new double[] {1.5, -2.0}, new double[] {0.0, 3.25});

        assertEquals(2, evaluation.objectiveCount());
        assertEquals(1.5, evaluation.objectiveValue(0));
        assertEquals(-2.0, evaluation.objectiveValue(1));
        assertEquals(2, evaluation.constraintViolationCount());
        assertEquals(0.0, evaluation.constraintViolation(0));
        assertEquals(3.25, evaluation.constraintViolation(1));
    }

    @Test
    void allowsEmptyConstraints() {
        Evaluation evaluation = new Evaluation(new double[] {1.0}, new double[0]);

        assertEquals(0, evaluation.constraintViolationCount());
        assertTrue(evaluation.isFeasible());
    }

    @Test
    void allowsInfiniteValues() {
        Evaluation evaluation = new Evaluation(
                new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY},
                new double[] {Double.POSITIVE_INFINITY});

        assertEquals(Double.POSITIVE_INFINITY, evaluation.objectiveValue(0));
        assertEquals(Double.NEGATIVE_INFINITY, evaluation.objectiveValue(1));
        assertEquals(Double.POSITIVE_INFINITY, evaluation.constraintViolation(0));
        assertFalse(evaluation.isFeasible());
    }

    @Test
    void feasibleOnlyWhenAllViolationsAreZero() {
        assertTrue(new Evaluation(new double[] {1.0}, new double[] {0.0, 0.0}).isFeasible());
        assertFalse(new Evaluation(new double[] {1.0}, new double[] {0.0, 0.001}).isFeasible());
    }

    @Test
    void negativeZeroViolationIsAcceptedAndFeasible() {
        assertTrue(new Evaluation(new double[] {1.0}, new double[] {-0.0}).isFeasible());
    }

    @Test
    void rejectsNullArrays() {
        NullPointerException objectives = assertThrows(NullPointerException.class,
                () -> new Evaluation(null, new double[0]));
        assertEquals("objectiveValues must not be null", objectives.getMessage());

        NullPointerException constraints = assertThrows(NullPointerException.class,
                () -> new Evaluation(new double[] {1.0}, null));
        assertEquals("constraintViolations must not be null", constraints.getMessage());
    }

    @Test
    void rejectsEmptyObjectives() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Evaluation(new double[0], new double[0]));
        assertEquals("objectiveValues must contain at least one value", e.getMessage());
    }

    @Test
    void rejectsNaNObjectiveValue() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Evaluation(new double[] {1.0, Double.NaN}, new double[0]));
        assertEquals("objectiveValues[1] must not be NaN", e.getMessage());
    }

    @Test
    void rejectsNaNConstraintViolation() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Evaluation(new double[] {1.0}, new double[] {Double.NaN}));
        assertEquals("constraintViolations[0] must not be NaN", e.getMessage());
    }

    @Test
    void rejectsNegativeConstraintViolation() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Evaluation(new double[] {1.0}, new double[] {0.0, -1.5}));
        assertEquals("constraintViolations[1] must be >= 0 but was -1.5", e.getMessage());
    }

    @Test
    void defensivelyCopiesInputArrays() {
        double[] objectiveValues = {1.0, 2.0};
        double[] constraintViolations = {0.5};
        Evaluation evaluation = new Evaluation(objectiveValues, constraintViolations);

        objectiveValues[0] = 99.0;
        constraintViolations[0] = 99.0;

        assertEquals(1.0, evaluation.objectiveValue(0));
        assertEquals(0.5, evaluation.constraintViolation(0));
    }

    @Test
    void rejectsOutOfRangeIndices() {
        Evaluation evaluation = new Evaluation(new double[] {1.0}, new double[] {0.0});

        assertThrows(IndexOutOfBoundsException.class, () -> evaluation.objectiveValue(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> evaluation.objectiveValue(1));
        assertThrows(IndexOutOfBoundsException.class, () -> evaluation.constraintViolation(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> evaluation.constraintViolation(1));
    }

    @Test
    void equalityIsValueBased() {
        Evaluation a = new Evaluation(new double[] {1.0, 2.0}, new double[] {0.5});
        Evaluation b = new Evaluation(new double[] {1.0, 2.0}, new double[] {0.5});

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Evaluation(new double[] {1.0, 2.5}, new double[] {0.5}));
        assertNotEquals(a, new Evaluation(new double[] {1.0, 2.0}, new double[] {0.75}));
        assertNotEquals(a, new Evaluation(new double[] {1.0, 2.0}, new double[0]));
    }

    @Test
    void equalityDistinguishesZeroSigns() {
        Evaluation positiveZero = new Evaluation(new double[] {0.0}, new double[0]);
        Evaluation negativeZero = new Evaluation(new double[] {-0.0}, new double[0]);

        assertNotEquals(positiveZero, negativeZero);
    }
}

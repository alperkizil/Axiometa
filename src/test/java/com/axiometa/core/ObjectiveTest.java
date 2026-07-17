package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ObjectiveTest {

    @Test
    void storesNameAndSense() {
        Objective objective = new Objective("cost", ObjectiveSense.MINIMIZE);

        assertEquals("cost", objective.name());
        assertEquals(ObjectiveSense.MINIMIZE, objective.sense());
    }

    @Test
    void rejectsNullName() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Objective(null, ObjectiveSense.MINIMIZE));
        assertEquals("name must not be null", e.getMessage());
    }

    @Test
    void rejectsNullSense() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Objective("cost", null));
        assertEquals("sense must not be null", e.getMessage());
    }

    @Test
    void rejectsBlankName() {
        IllegalArgumentException empty = assertThrows(IllegalArgumentException.class,
                () -> new Objective("", ObjectiveSense.MAXIMIZE));
        assertEquals("name must not be blank", empty.getMessage());

        IllegalArgumentException whitespace = assertThrows(IllegalArgumentException.class,
                () -> new Objective("   ", ObjectiveSense.MAXIMIZE));
        assertEquals("name must not be blank", whitespace.getMessage());
    }

    @Test
    void equalityIsValueBased() {
        Objective a = new Objective("cost", ObjectiveSense.MINIMIZE);
        Objective b = new Objective("cost", ObjectiveSense.MINIMIZE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Objective("profit", ObjectiveSense.MINIMIZE));
        assertNotEquals(a, new Objective("cost", ObjectiveSense.MAXIMIZE));
    }
}

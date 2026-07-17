package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConstraintTest {

    @Test
    void storesName() {
        assertEquals("capacity", new Constraint("capacity").name());
    }

    @Test
    void rejectsNullName() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Constraint(null));
        assertEquals("name must not be null", e.getMessage());
    }

    @Test
    void rejectsBlankName() {
        IllegalArgumentException empty = assertThrows(IllegalArgumentException.class,
                () -> new Constraint(""));
        assertEquals("name must not be blank", empty.getMessage());

        IllegalArgumentException whitespace = assertThrows(IllegalArgumentException.class,
                () -> new Constraint(" \t"));
        assertEquals("name must not be blank", whitespace.getMessage());
    }

    @Test
    void equalityIsValueBased() {
        assertEquals(new Constraint("capacity"), new Constraint("capacity"));
        assertEquals(new Constraint("capacity").hashCode(), new Constraint("capacity").hashCode());
        assertNotEquals(new Constraint("capacity"), new Constraint("budget"));
    }
}

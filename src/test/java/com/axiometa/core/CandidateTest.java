package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CandidateTest {

    @Test
    void exposesTheWrappedRepresentation() {
        List<Integer> representation = List.of(1, 2, 3);
        Candidate<List<Integer>> candidate = new Candidate<>(representation);

        assertSame(representation, candidate.representation());
    }

    @Test
    void rejectsNullRepresentation() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Candidate<>(null));
        assertEquals("representation must not be null", e.getMessage());
    }

    @Test
    void equalityDelegatesToRepresentationValue() {
        Candidate<List<Integer>> a = new Candidate<>(List.of(1, 2, 3));
        Candidate<List<Integer>> b = new Candidate<>(List.of(1, 2, 3));

        assertNotSame(a.representation(), b.representation(),
                "test requires distinct but equal representation instances");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void distinctRepresentationValuesAreNotEqual() {
        assertNotEquals(new Candidate<>(List.of(1, 2, 3)), new Candidate<>(List.of(3, 2, 1)));
    }
}

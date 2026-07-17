package com.axiometa.phase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import org.junit.jupiter.api.Test;

class OffspringPairTest {

    private final Candidate<String> childA = new Candidate<>("a");
    private final Candidate<String> childB = new Candidate<>("b");

    @Test
    void storesBothChildren() {
        OffspringPair<String> pair = new OffspringPair<>(childA, childB);

        assertSame(childA, pair.first());
        assertSame(childB, pair.second());
    }

    @Test
    void rejectsNullChildren() {
        NullPointerException first = assertThrows(NullPointerException.class,
                () -> new OffspringPair<>(null, childB));
        assertEquals("first must not be null", first.getMessage());

        NullPointerException second = assertThrows(NullPointerException.class,
                () -> new OffspringPair<>(childA, null));
        assertEquals("second must not be null", second.getMessage());
    }

    @Test
    void equalityIsValueBased() {
        assertEquals(new OffspringPair<>(childA, childB), new OffspringPair<>(childA, childB));
    }
}

package com.axiometa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PopulationTest {

    private static EvaluatedCandidate<String> member(String representation, double value) {
        return new EvaluatedCandidate<>(
                new Candidate<>(representation), new Evaluation(new double[] {value}, new double[0]));
    }

    @Test
    void storesMembersAndSize() {
        Population<String> population = new Population<>(
                List.of(member("a", 1.0), member("b", 2.0)));

        assertEquals(2, population.size());
        assertEquals(2, population.members().size());
        assertEquals(member("a", 1.0), population.members().get(0));
        assertEquals(member("b", 2.0), population.members().get(1));
    }

    @Test
    void defensivelyCopiesTheMemberList() {
        List<EvaluatedCandidate<String>> source = new ArrayList<>();
        source.add(member("a", 1.0));
        Population<String> population = new Population<>(source);

        source.add(member("b", 2.0));
        source.set(0, member("c", 3.0));

        assertEquals(1, population.size());
        assertEquals(member("a", 1.0), population.members().get(0));
    }

    @Test
    void exposesAnImmutableMemberList() {
        Population<String> population = new Population<>(List.of(member("a", 1.0)));

        assertThrows(UnsupportedOperationException.class,
                () -> population.members().add(member("b", 2.0)));
    }

    @Test
    void rejectsNullMemberList() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Population<String>(null));
        assertEquals("members must not be null", e.getMessage());
    }

    @Test
    void rejectsEmptyMemberList() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Population<String>(List.of()));
        assertEquals("members must not be empty", e.getMessage());
    }

    @Test
    void rejectsNullMember() {
        List<EvaluatedCandidate<String>> withNull = Arrays.asList(member("a", 1.0), null);

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Population<>(withNull));
        assertEquals("members[1] must not be null", e.getMessage());
    }

    @Test
    void equalityIsValueBased() {
        assertEquals(new Population<>(List.of(member("a", 1.0))),
                new Population<>(List.of(member("a", 1.0))));
        assertNotEquals(new Population<>(List.of(member("a", 1.0))),
                new Population<>(List.of(member("b", 2.0))));
    }
}

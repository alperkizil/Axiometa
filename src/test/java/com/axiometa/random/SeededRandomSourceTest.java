package com.axiometa.random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeededRandomSourceTest {

    private static final int DRAWS = 100;

    private static List<Integer> drawInts(RandomSource source, int bound) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < DRAWS; i++) {
            values.add(source.nextInt(bound));
        }
        return values;
    }

    private static List<Double> drawDoubles(RandomSource source) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < DRAWS; i++) {
            values.add(source.nextDouble());
        }
        return values;
    }

    @Test
    void sameRootSeedProducesIdenticalStreams() {
        RandomSource a = SeededRandomSource.root(123L);
        RandomSource b = SeededRandomSource.root(123L);

        assertEquals(drawInts(a, 1000), drawInts(b, 1000));
        assertEquals(drawDoubles(a), drawDoubles(b));
        for (int i = 0; i < DRAWS; i++) {
            assertEquals(a.nextBoolean(), b.nextBoolean());
        }
    }

    @Test
    void differentRootSeedsProduceDifferentStreams() {
        assertNotEquals(drawInts(SeededRandomSource.root(1L), 1000),
                drawInts(SeededRandomSource.root(2L), 1000));
    }

    @Test
    void sameNameReproducesTheIdenticalChildStream() {
        RandomSource first = SeededRandomSource.root(7L).child("mutation");
        RandomSource second = SeededRandomSource.root(7L).child("mutation");

        assertEquals(drawInts(first, 1000), drawInts(second, 1000));
    }

    @Test
    void distinctNamesProduceIndependentStreams() {
        RandomSource root = SeededRandomSource.root(7L);

        assertNotEquals(drawInts(root.child("mutation"), 1000),
                drawInts(root.child("crossover"), 1000));
    }

    @Test
    void sameNameUnderDifferentParentsProducesDifferentStreams() {
        assertNotEquals(drawInts(SeededRandomSource.root(1L).child("mutation"), 1000),
                drawInts(SeededRandomSource.root(2L).child("mutation"), 1000));
    }

    @Test
    void childStreamDiffersFromItsParentStream() {
        RandomSource parent = SeededRandomSource.root(7L);
        List<Integer> childValues = drawInts(parent.child("mutation"), 1000);

        assertNotEquals(drawInts(SeededRandomSource.root(7L), 1000), childValues);
    }

    @Test
    void childDerivationIgnoresParentDrawHistory() {
        RandomSource freshParent = SeededRandomSource.root(99L);
        RandomSource childBeforeDraws = freshParent.child("selection");

        RandomSource exhaustedParent = SeededRandomSource.root(99L);
        drawInts(exhaustedParent, 1000);
        RandomSource childAfterDraws = exhaustedParent.child("selection");

        assertEquals(drawInts(childBeforeDraws, 1000), drawInts(childAfterDraws, 1000));
    }

    @Test
    void grandchildrenAreReproducibleAndPathDependent() {
        RandomSource rootA = SeededRandomSource.root(5L);
        RandomSource rootB = SeededRandomSource.root(5L);

        assertEquals(drawInts(rootA.child("island").child("mutation"), 1000),
                drawInts(rootB.child("island").child("mutation"), 1000));
        assertNotEquals(drawInts(SeededRandomSource.root(5L).child("island").child("mutation"), 1000),
                drawInts(SeededRandomSource.root(5L).child("mutation").child("island"), 1000));
    }

    @Test
    void nextIntStaysWithinBound() {
        RandomSource source = SeededRandomSource.root(11L);
        for (int i = 0; i < DRAWS; i++) {
            int value = source.nextInt(3);
            assertTrue(value >= 0 && value < 3, "value out of [0,3): " + value);
        }
    }

    @Test
    void nextDoubleStaysWithinUnitInterval() {
        RandomSource source = SeededRandomSource.root(11L);
        for (int i = 0; i < DRAWS; i++) {
            double value = source.nextDouble();
            assertTrue(value >= 0.0 && value < 1.0, "value out of [0,1): " + value);
        }
    }

    @Test
    void nextIntRejectsNonPositiveBound() {
        RandomSource source = SeededRandomSource.root(1L);

        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
                () -> source.nextInt(0));
        assertEquals("bound must be >= 1 but was 0", zero.getMessage());

        IllegalArgumentException negative = assertThrows(IllegalArgumentException.class,
                () -> source.nextInt(-5));
        assertEquals("bound must be >= 1 but was -5", negative.getMessage());
    }

    @Test
    void childRejectsNullName() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> SeededRandomSource.root(1L).child(null));
        assertEquals("name must not be null", e.getMessage());
    }

    @Test
    void childRejectsBlankName() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SeededRandomSource.root(1L).child("  "));
        assertEquals("name must not be blank", e.getMessage());
    }

    /**
     * Change detector: pins the exact output of the approved generator
     * (L64X128MixRandom) and SHA-256 child-seed derivation. If this test
     * fails, the random infrastructure changed behavior and every experiment
     * would silently produce different results — that must be a loud,
     * deliberate decision, never an accident.
     */
    @Test
    void goldenValuesLockGeneratorAndDerivation() {
        RandomSource root = SeededRandomSource.root(42L);
        List<Integer> rootValues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rootValues.add(root.nextInt(100));
        }
        assertEquals(List.of(98, 1, 80, 2, 55), rootValues);

        RandomSource child = SeededRandomSource.root(42L).child("mutation");
        List<Integer> childValues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            childValues.add(child.nextInt(100));
        }
        assertEquals(List.of(55, 84, 97, 96, 21), childValues);
    }
}

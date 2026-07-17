package com.axiometa.bitstring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import com.axiometa.phase.OffspringPair;
import com.axiometa.random.SeededRandomSource;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnePointCrossoverTest {

    private static Candidate<BitString> bits(String pattern) {
        List<Boolean> bits = new ArrayList<>(pattern.length());
        for (char c : pattern.toCharArray()) {
            bits.add(c == '1');
        }
        return new Candidate<>(new BitString(bits));
    }

    @Test
    void swapsTailsAtTheDrawnCutPoint() {
        // probability draw 0.0 < 1.0, then cut = 1 + 2 = 3
        OnePointCrossover crossover = new OnePointCrossover(1.0,
                new ScriptedRandomSource().withDoubles(0.0).withInts(2));

        OffspringPair<BitString> children = crossover.crossover(bits("0000"), bits("1111"));

        assertEquals(bits("0001").representation(), children.first().representation());
        assertEquals(bits("1110").representation(), children.second().representation());
    }

    @Test
    void clonesParentsWhenTheProbabilityDrawFails() {
        OnePointCrossover crossover = new OnePointCrossover(0.5,
                new ScriptedRandomSource().withDoubles(0.9));
        Candidate<BitString> first = bits("0011");
        Candidate<BitString> second = bits("1100");

        OffspringPair<BitString> children = crossover.crossover(first, second);

        assertSame(first, children.first());
        assertSame(second, children.second());
    }

    @Test
    void probabilityZeroAlwaysClones() {
        OnePointCrossover crossover =
                new OnePointCrossover(0.0, SeededRandomSource.root(7L));
        Candidate<BitString> first = bits("01");
        Candidate<BitString> second = bits("10");

        OffspringPair<BitString> children = crossover.crossover(first, second);

        assertSame(first, children.first());
        assertSame(second, children.second());
    }

    @Test
    void singleBitParentsCloneWithoutConsumingDraws() {
        // an empty script proves no draw is consumed: any draw would fail
        OnePointCrossover crossover = new OnePointCrossover(1.0, new ScriptedRandomSource());
        Candidate<BitString> first = bits("0");
        Candidate<BitString> second = bits("1");

        OffspringPair<BitString> children = crossover.crossover(first, second);

        assertSame(first, children.first());
        assertSame(second, children.second());
    }

    @Test
    void rejectsParentsOfDifferentLengths() {
        OnePointCrossover crossover = new OnePointCrossover(1.0, new ScriptedRandomSource());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> crossover.crossover(bits("000"), bits("0000")));
        assertEquals("parents must have equal lengths but were 3 and 4", e.getMessage());
    }

    @Test
    void rejectsNullParents() {
        OnePointCrossover crossover = new OnePointCrossover(1.0, new ScriptedRandomSource());

        assertEquals("first must not be null",
                assertThrows(NullPointerException.class,
                        () -> crossover.crossover(null, bits("1"))).getMessage());
        assertEquals("second must not be null",
                assertThrows(NullPointerException.class,
                        () -> crossover.crossover(bits("1"), null)).getMessage());
    }

    @Test
    void rejectsInvalidProbability() {
        IllegalArgumentException tooBig = assertThrows(IllegalArgumentException.class,
                () -> new OnePointCrossover(1.1, new ScriptedRandomSource()));
        assertEquals("probability must be in [0, 1] but was 1.1", tooBig.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> new OnePointCrossover(-0.1, new ScriptedRandomSource()));
        assertThrows(IllegalArgumentException.class,
                () -> new OnePointCrossover(Double.NaN, new ScriptedRandomSource()));
    }

    @Test
    void rejectsNullRandom() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new OnePointCrossover(0.5, null));
        assertEquals("random must not be null", e.getMessage());
    }
}

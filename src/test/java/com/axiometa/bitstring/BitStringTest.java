package com.axiometa.bitstring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class BitStringTest {

    @Test
    void storesBitsWithLengthAndCompactToString() {
        BitString bits = new BitString(List.of(true, false, true));

        assertEquals(3, bits.length());
        assertEquals(List.of(true, false, true), bits.bits());
        assertEquals("101", bits.toString());
    }

    @Test
    void defensivelyCopiesTheInputList() {
        List<Boolean> source = new ArrayList<>(List.of(true, false));
        BitString bits = new BitString(source);

        source.set(0, false);

        assertEquals(List.of(true, false), bits.bits());
    }

    @Test
    void exposesAnImmutableBitList() {
        BitString bits = new BitString(List.of(true));

        assertThrows(UnsupportedOperationException.class, () -> bits.bits().add(false));
    }

    @Test
    void rejectsNullBitList() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new BitString(null));
        assertEquals("bits must not be null", e.getMessage());
    }

    @Test
    void rejectsEmptyBitList() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new BitString(List.of()));
        assertEquals("bits must contain at least one bit", e.getMessage());
    }

    @Test
    void rejectsNullBit() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new BitString(Arrays.asList(true, null)));
        assertEquals("bits[1] must not be null", e.getMessage());
    }

    @Test
    void equalityIsValueBased() {
        assertEquals(new BitString(List.of(true, false)), new BitString(List.of(true, false)));
        assertNotEquals(new BitString(List.of(true, false)),
                new BitString(List.of(false, true)));
    }
}

package com.axiometa.bitstring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import com.axiometa.random.SeededRandomSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class RandomBitStringInitializationTest {

    @Test
    void createsTheRequestedPopulationWithTheConfiguredLength() {
        RandomBitStringInitialization initialization =
                new RandomBitStringInitialization(8, SeededRandomSource.root(1L));

        List<Candidate<BitString>> candidates = initialization.initialize(5);

        assertEquals(5, candidates.size());
        candidates.forEach(candidate ->
                assertEquals(8, candidate.representation().length()));
    }

    @Test
    void sameSeedProducesIdenticalCandidates() {
        RandomBitStringInitialization first =
                new RandomBitStringInitialization(16, SeededRandomSource.root(42L));
        RandomBitStringInitialization second =
                new RandomBitStringInitialization(16, SeededRandomSource.root(42L));

        assertEquals(first.initialize(10), second.initialize(10));
    }

    @Test
    void rejectsNonPositiveBitCount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new RandomBitStringInitialization(0, SeededRandomSource.root(1L)));
        assertEquals("bitCount must be >= 1 but was 0", e.getMessage());
    }

    @Test
    void rejectsNullRandom() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new RandomBitStringInitialization(1, null));
        assertEquals("random must not be null", e.getMessage());
    }

    @Test
    void rejectsNonPositivePopulationSize() {
        RandomBitStringInitialization initialization =
                new RandomBitStringInitialization(4, SeededRandomSource.root(1L));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> initialization.initialize(0));
        assertEquals("populationSize must be >= 1 but was 0", e.getMessage());
    }
}

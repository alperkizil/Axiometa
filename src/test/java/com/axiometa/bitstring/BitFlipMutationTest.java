package com.axiometa.bitstring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.core.Candidate;
import com.axiometa.random.SeededRandomSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class BitFlipMutationTest {

    private static final Candidate<BitString> INPUT =
            new Candidate<>(new BitString(List.of(false, true, false)));

    @Test
    void probabilityOneFlipsEveryBit() {
        BitFlipMutation mutation = new BitFlipMutation(1.0, SeededRandomSource.root(1L));

        assertEquals(new BitString(List.of(true, false, true)),
                mutation.mutate(INPUT).representation());
    }

    @Test
    void probabilityZeroChangesNothing() {
        BitFlipMutation mutation = new BitFlipMutation(0.0, SeededRandomSource.root(1L));

        assertEquals(INPUT.representation(), mutation.mutate(INPUT).representation());
    }

    @Test
    void flipsExactlyTheScriptedBits() {
        BitFlipMutation mutation = new BitFlipMutation(0.5,
                new ScriptedRandomSource().withDoubles(0.4, 0.6, 0.3));

        assertEquals(new BitString(List.of(true, true, true)),
                mutation.mutate(INPUT).representation());
    }

    @Test
    void sameSeedProducesTheSameMutation() {
        BitFlipMutation first = new BitFlipMutation(0.5, SeededRandomSource.root(9L));
        BitFlipMutation second = new BitFlipMutation(0.5, SeededRandomSource.root(9L));

        assertEquals(first.mutate(INPUT), second.mutate(INPUT));
    }

    @Test
    void rejectsInvalidProbability() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new BitFlipMutation(-0.5, new ScriptedRandomSource()));
        assertEquals("perBitProbability must be in [0, 1] but was -0.5", e.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> new BitFlipMutation(1.5, new ScriptedRandomSource()));
        assertThrows(IllegalArgumentException.class,
                () -> new BitFlipMutation(Double.NaN, new ScriptedRandomSource()));
    }

    @Test
    void rejectsNullCandidate() {
        BitFlipMutation mutation = new BitFlipMutation(0.5, new ScriptedRandomSource());

        NullPointerException e = assertThrows(NullPointerException.class,
                () -> mutation.mutate(null));
        assertEquals("candidate must not be null", e.getMessage());
    }

    @Test
    void rejectsNullRandom() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new BitFlipMutation(0.5, null));
        assertEquals("random must not be null", e.getMessage());
    }
}

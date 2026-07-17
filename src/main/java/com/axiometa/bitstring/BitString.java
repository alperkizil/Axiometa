package com.axiometa.bitstring;

import com.axiometa.core.Representation;
import java.util.List;
import java.util.Objects;

/**
 * Immutable fixed-length bit string.
 *
 * <p>The bit list is defensively copied to an immutable list; record equality
 * is value-based over the bits, satisfying the {@link Representation}
 * contract.
 *
 * @param bits the bits, most significant first for display purposes; must not
 *             be null, empty, or contain nulls
 */
public record BitString(List<Boolean> bits) implements Representation {

    /**
     * Validates and defensively copies the bits.
     *
     * @throws NullPointerException     if {@code bits} or any element is null
     * @throws IllegalArgumentException if {@code bits} is empty
     */
    public BitString {
        Objects.requireNonNull(bits, "bits must not be null");
        if (bits.isEmpty()) {
            throw new IllegalArgumentException("bits must contain at least one bit");
        }
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i) == null) {
                throw new NullPointerException("bits[" + i + "] must not be null");
            }
        }
        bits = List.copyOf(bits);
    }

    /**
     * Returns the number of bits.
     *
     * @return the length, always {@code >= 1}
     */
    public int length() {
        return bits.size();
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(bits.size());
        for (Boolean bit : bits) {
            text.append(bit ? '1' : '0');
        }
        return text.toString();
    }
}

package com.axiometa.random;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * {@link RandomSource} backed by the JDK {@code L64X128MixRandom} generator.
 *
 * <p>Child seeds are the first 8 bytes (big-endian) of
 * {@code SHA-256(parent seed as 8 big-endian bytes || name as UTF-8)} — a
 * fully specified, platform-stable derivation. Changing the generator
 * algorithm or this derivation changes every experiment; both are pinned by
 * a golden-value test.
 */
public final class SeededRandomSource implements RandomSource {

    private static final RandomGeneratorFactory<RandomGenerator> FACTORY =
            RandomGeneratorFactory.of("L64X128MixRandom");

    private final long seed;
    private final RandomGenerator generator;

    private SeededRandomSource(long seed) {
        this.seed = seed;
        this.generator = FACTORY.create(seed);
    }

    /**
     * Creates the root source of a stream tree.
     *
     * @param seed the root seed; every long value, including zero and
     *             negatives, is valid
     * @return the root source
     */
    public static SeededRandomSource root(long seed) {
        return new SeededRandomSource(seed);
    }

    @Override
    public int nextInt(int bound) {
        if (bound < 1) {
            throw new IllegalArgumentException("bound must be >= 1 but was " + bound);
        }
        return generator.nextInt(bound);
    }

    @Override
    public double nextDouble() {
        return generator.nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return generator.nextBoolean();
    }

    @Override
    public RandomSource child(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new SeededRandomSource(deriveChildSeed(seed, name));
    }

    private static long deriveChildSeed(long parentSeed, String name) {
        MessageDigest digest = sha256();
        digest.update(ByteBuffer.allocate(Long.BYTES).putLong(parentSeed).array());
        digest.update(name.getBytes(StandardCharsets.UTF_8));
        return ByteBuffer.wrap(digest.digest()).getLong();
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 must be present in every JDK", e);
        }
    }

    @Override
    public String toString() {
        return "SeededRandomSource[seed=" + seed + "]";
    }
}

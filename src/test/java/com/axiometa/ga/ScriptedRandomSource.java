package com.axiometa.ga;

import com.axiometa.random.RandomSource;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Test double replaying scripted draws; fails loudly when the script runs
 * dry or a scripted value is out of range, so tests also prove exactly how
 * many draws a component consumes.
 */
final class ScriptedRandomSource implements RandomSource {

    private final Deque<Integer> ints = new ArrayDeque<>();
    private final Deque<Double> doubles = new ArrayDeque<>();
    private final Deque<Boolean> booleans = new ArrayDeque<>();

    ScriptedRandomSource withInts(int... values) {
        for (int value : values) {
            ints.add(value);
        }
        return this;
    }

    ScriptedRandomSource withDoubles(double... values) {
        for (double value : values) {
            doubles.add(value);
        }
        return this;
    }

    ScriptedRandomSource withBooleans(boolean... values) {
        for (boolean value : values) {
            booleans.add(value);
        }
        return this;
    }

    @Override
    public int nextInt(int bound) {
        if (ints.isEmpty()) {
            throw new AssertionError("no scripted int left (bound " + bound + ")");
        }
        int value = ints.poll();
        if (value < 0 || value >= bound) {
            throw new AssertionError("scripted int " + value + " out of [0, " + bound + ")");
        }
        return value;
    }

    @Override
    public double nextDouble() {
        if (doubles.isEmpty()) {
            throw new AssertionError("no scripted double left");
        }
        return doubles.poll();
    }

    @Override
    public boolean nextBoolean() {
        if (booleans.isEmpty()) {
            throw new AssertionError("no scripted boolean left");
        }
        return booleans.poll();
    }

    @Override
    public RandomSource child(String name) {
        throw new AssertionError("scripted source has no child streams");
    }
}

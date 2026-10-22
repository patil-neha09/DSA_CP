package com.adaptivehash.probing;

/**
 * Quadratic Probing collision resolution strategy.
 * Standard Formula: h(k, i) = (h'(k) + c1 * i + c2 * i^2) mod m
 * Helps alleviate primary clustering, but can still suffer from secondary clustering.
 * In practice, quadratic probing may only visit a subset of slots (at least (m+1)/2 when m is prime),
 * so probing attempts are strictly bounded to prevent infinite cycles.
 */
public class QuadraticProbing implements ProbingStrategy {

    private final int c1;
    private final int c2;

    public QuadraticProbing() {
        this(1, 1);
    }

    public QuadraticProbing(int c1, int c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public String getName() {
        return "Quadratic Probing";
    }

    @Override
    public int computeProbeIndex(int baseHash, int probeAttempt, int capacity, Object key) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacity);
        }
        long offset = (long) c1 * probeAttempt + (long) c2 * probeAttempt * probeAttempt;
        return (int) Math.floorMod((long) baseHash + offset, (long) capacity);
    }

    @Override
    public int getMaxProbes(int capacity) {
        // Upper bound probe attempts to capacity to prevent infinite cycling
        return Math.max(1, capacity);
    }

    public int getC1() {
        return c1;
    }

    public int getC2() {
        return c2;
    }
}

package com.adaptivehash.probing;

/**
 * Quadratic Probing collision resolution strategy.
 * Standard Formula: h(k, i) = (h'(k) + c1 * i + c2 * i^2) mod m
 * Helps alleviate primary clustering, but can still suffer from secondary clustering.
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
        // Skeleton method: Probe calculation
        long offset = (long) c1 * probeAttempt + (long) c2 * probeAttempt * probeAttempt;
        return (int) ((baseHash + offset) % capacity);
    }

    public int getC1() {
        return c1;
    }

    public int getC2() {
        return c2;
    }
}

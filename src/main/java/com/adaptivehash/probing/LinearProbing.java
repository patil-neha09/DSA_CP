package com.adaptivehash.probing;

/**
 * Linear Probing collision resolution strategy.
 * Formula: h(k, i) = (h'(k) + i) mod m
 * Simple and cache-friendly, but susceptible to primary clustering.
 */
public class LinearProbing implements ProbingStrategy {

    @Override
    public String getName() {
        return "Linear Probing";
    }

    @Override
    public int computeProbeIndex(int baseHash, int probeAttempt, int capacity, Object key) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacity);
        }
        long index = (long) baseHash + probeAttempt;
        return (int) Math.floorMod(index, (long) capacity);
    }
}

package com.adaptivehash.probing;

import com.adaptivehash.core.HashFunction;

/**
 * Double Hashing collision resolution strategy.
 * Formula: h(k, i) = (h1(k) + i * h2(k)) mod m
 * Eliminates both primary and secondary clustering by using a secondary hash step size.
 * Guarantees that step size h2(k) is coprime with table capacity m to ensure complete traversal.
 */
public class DoubleHashing implements ProbingStrategy {

    private final HashFunction<Object> hashFunction;

    public DoubleHashing() {
        this((HashFunction<Object>) null);
    }

    @SuppressWarnings("unchecked")
    public DoubleHashing(HashFunction<?> hashFunction) {
        this.hashFunction = (HashFunction<Object>) hashFunction;
    }

    @Override
    public String getName() {
        return "Double Hashing";
    }

    @Override
    public int computeProbeIndex(int baseHash, int probeAttempt, int capacity, Object key) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive, got: " + capacity);
        }

        int step = 1;
        if (hashFunction != null && key != null) {
            step = hashFunction.secondaryHash(key, capacity);
        } else {
            step = HashFunction.computeSecondaryHash(key, capacity);
        }

        if (step <= 0) {
            step = 1;
        }

        // Guarantee coprimality: gcd(step, capacity) == 1 ensures full cycle through all m slots
        while (gcd(step, capacity) > 1 && step < capacity) {
            step++;
        }
        if (gcd(step, capacity) > 1) {
            step = 1;
        }

        long index = (long) baseHash + (long) probeAttempt * step;
        return (int) Math.floorMod(index, (long) capacity);
    }

    @Override
    public int getMaxProbes(int capacity) {
        return Math.max(1, capacity);
    }

    /**
     * Computes the greatest common divisor using Euclidean algorithm.
     */
    private static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}

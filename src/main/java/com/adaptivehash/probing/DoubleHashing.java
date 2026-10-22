package com.adaptivehash.probing;

import com.adaptivehash.core.HashFunction;

/**
 * Double Hashing collision resolution strategy.
 * Formula: h(k, i) = (h1(k) + i * h2(k)) mod m
 * Eliminates both primary and secondary clustering by using a secondary hash step size.
 */
public class DoubleHashing implements ProbingStrategy {

    private final HashFunction<Object> hashFunction;

    public DoubleHashing() {
        this(null);
    }

    public DoubleHashing(HashFunction<Object> hashFunction) {
        this.hashFunction = hashFunction;
    }

    @Override
    public String getName() {
        return "Double Hashing";
    }

    @Override
    public int computeProbeIndex(int baseHash, int probeAttempt, int capacity, Object key) {
        // Skeleton method: Probe calculation
        int step = 1;
        if (hashFunction != null && key != null) {
            step = Math.max(1, hashFunction.secondaryHash(key, capacity));
        } else if (key != null) {
            // Default fallback step calculation using key hash
            int secondary = 7 - (Math.abs(key.hashCode()) % 7);
            step = secondary == 0 ? 1 : secondary;
        }
        long index = (baseHash + (long) probeAttempt * step) % capacity;
        return (int) ((index + capacity) % capacity);
    }
}

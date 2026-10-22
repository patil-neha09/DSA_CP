package com.adaptivehash.probing;

/**
 * Common interface representing a collision resolution probing strategy in open addressing.
 */
public interface ProbingStrategy {

    /**
     * Returns the human-readable display name of the probing strategy.
     *
     * @return name of the probing strategy
     */
    String getName();

    /**
     * Computes the next index in the probe sequence for the given attempt number.
     *
     * @param baseHash the initial primary hash index [0, capacity - 1]
     * @param probeAttempt the sequence attempt counter (0, 1, 2, ..., capacity - 1)
     * @param capacity the capacity of the hash table
     * @param key the key being probed (useful for secondary hashing)
     * @return the computed table slot index within [0, capacity - 1]
     */
    int computeProbeIndex(int baseHash, int probeAttempt, int capacity, Object key);

    /**
     * Maximum number of probe attempts permitted before terminating search/insert.
     * Guaranteed upper bound to prevent infinite loops.
     *
     * @param capacity current table capacity
     * @return maximum probe attempts
     */
    default int getMaxProbes(int capacity) {
        return Math.max(1, capacity);
    }
}

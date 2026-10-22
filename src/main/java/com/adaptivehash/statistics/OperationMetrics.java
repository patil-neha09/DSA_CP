package com.adaptivehash.statistics;

/**
 * Encapsulates the execution metrics for an individual hash table operation (Insert, Search, Delete).
 */
public class OperationMetrics {

    public enum OperationType {
        INSERT,
        SEARCH,
        DELETE
    }

    private final OperationType operationType;
    private final Object key;
    private final int initialHash;
    private final int finalSlotIndex;
    private final int probeCount;
    private final int collisionCount;
    private final boolean successful;
    private final long executionTimeNanos;
    private final long timestampMillis;

    public OperationMetrics(OperationType operationType,
                            Object key,
                            int initialHash,
                            int finalSlotIndex,
                            int probeCount,
                            int collisionCount,
                            boolean successful,
                            long executionTimeNanos) {
        this.operationType = operationType;
        this.key = key;
        this.initialHash = initialHash;
        this.finalSlotIndex = finalSlotIndex;
        this.probeCount = probeCount;
        this.collisionCount = collisionCount;
        this.successful = successful;
        this.executionTimeNanos = executionTimeNanos;
        this.timestampMillis = System.currentTimeMillis();
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Object getKey() {
        return key;
    }

    public int getInitialHash() {
        return initialHash;
    }

    public int getFinalSlotIndex() {
        return finalSlotIndex;
    }

    public int getProbeCount() {
        return probeCount;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    @Override
    public String toString() {
        return String.format("%s [key=%s, probes=%d, collisions=%d, success=%s, time=%dns]",
                operationType, key, probeCount, collisionCount, successful, executionTimeNanos);
    }
}

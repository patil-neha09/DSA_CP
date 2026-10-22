package com.adaptivehash.statistics;

/**
 * Aggregates runtime performance statistics across all hash table operations.
 */
public class Statistics {

    private long totalOperations;
    private long totalInsertions;
    private long totalSearches;
    private long totalDeletions;
    private long totalCollisions;
    private long totalProbes;
    private int maxProbesEncountered;

    public Statistics() {
        reset();
    }

    /**
     * Records an operation's metrics into the aggregated statistics.
     *
     * @param metrics the metrics of the completed operation
     */
    public synchronized void recordOperation(OperationMetrics metrics) {
        if (metrics == null) return;

        totalOperations++;
        totalCollisions += metrics.getCollisionCount();
        totalProbes += metrics.getProbeCount();

        if (metrics.getProbeCount() > maxProbesEncountered) {
            maxProbesEncountered = metrics.getProbeCount();
        }

        switch (metrics.getOperationType()) {
            case INSERT -> totalInsertions++;
            case SEARCH -> totalSearches++;
            case DELETE -> totalDeletions++;
        }
    }

    public synchronized double getAverageProbes() {
        return totalOperations == 0 ? 0.0 : (double) totalProbes / totalOperations;
    }

    public synchronized double getCollisionRate() {
        return totalOperations == 0 ? 0.0 : (double) totalCollisions / totalOperations;
    }

    public synchronized void reset() {
        this.totalOperations = 0;
        this.totalInsertions = 0;
        this.totalSearches = 0;
        this.totalDeletions = 0;
        this.totalCollisions = 0;
        this.totalProbes = 0;
        this.maxProbesEncountered = 0;
    }

    public long getTotalOperations() {
        return totalOperations;
    }

    public long getTotalInsertions() {
        return totalInsertions;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public long getTotalDeletions() {
        return totalDeletions;
    }

    public long getTotalCollisions() {
        return totalCollisions;
    }

    public long getTotalProbes() {
        return totalProbes;
    }

    public int getMaxProbesEncountered() {
        return maxProbesEncountered;
    }
}

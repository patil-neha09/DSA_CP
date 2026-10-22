package com.adaptivehash.adaptive;

import com.adaptivehash.core.HashTable;
import com.adaptivehash.statistics.OperationMetrics;
import com.adaptivehash.statistics.Statistics;

import java.util.List;

/**
 * Analyzes runtime performance metrics and detects clustering patterns or performance degradation.
 */
public class PerformanceAnalyzer {

    /**
     * Evaluates whether the current probe sequences indicate significant primary or secondary clustering.
     *
     * @param recentMetrics list of recent operation metrics
     * @param probeThreshold the threshold for average probes per operation
     * @return true if clustering / degradation is detected, false otherwise
     */
    public boolean isClusteringDetected(List<OperationMetrics> recentMetrics, double probeThreshold) {
        if (recentMetrics == null || recentMetrics.size() < 3) {
            return false;
        }

        long totalProbes = 0;
        for (OperationMetrics metric : recentMetrics) {
            totalProbes += metric.getProbeCount();
        }

        double avgProbes = (double) totalProbes / recentMetrics.size();
        return avgProbes > probeThreshold;
    }

    /**
     * Calculates the primary clustering factor across the hash table slots.
     * Evaluates contiguous clusters of occupied slots using weighted mean cluster length.
     *
     * @param table the hash table to analyze
     * @return calculated cluster index score
     */
    public double calculateClusterFactor(HashTable<?, ?> table) {
        if (table == null || table.isEmpty() || table.capacity() == 0) {
            return 0.0;
        }
        int capacity = table.capacity();
        HashTable.Entry<?, ?>[] entries = table.getTable();

        // If entire table is occupied
        if (table.size() == capacity) {
            return (double) capacity;
        }

        // Find a non-occupied slot to avoid splitting circular clusters
        int start = -1;
        for (int i = 0; i < capacity; i++) {
            HashTable.Entry<?, ?> entry = entries[i];
            if (entry == null || entry.isDeleted()) {
                start = i;
                break;
            }
        }
        if (start == -1) {
            return (double) capacity;
        }

        long sumSquaredClusterLengths = 0;
        int totalOccupied = 0;
        int currentCluster = 0;

        for (int step = 0; step < capacity; step++) {
            int idx = (start + 1 + step) % capacity;
            HashTable.Entry<?, ?> entry = entries[idx];
            if (entry != null && !entry.isDeleted()) {
                currentCluster++;
                totalOccupied++;
            } else {
                if (currentCluster > 0) {
                    sumSquaredClusterLengths += (long) currentCluster * currentCluster;
                    currentCluster = 0;
                }
            }
        }
        if (currentCluster > 0) {
            sumSquaredClusterLengths += (long) currentCluster * currentCluster;
        }

        if (totalOccupied == 0) {
            return 0.0;
        }
        return (double) sumSquaredClusterLengths / totalOccupied;
    }

    /**
     * Checks if collision rate exceeds acceptable threshold based on statistics.
     *
     * @param statistics current aggregated statistics
     * @param collisionRateThreshold threshold above which adaptation is triggered
     * @return true if collision rate is beyond threshold
     */
    public boolean isCollisionRateCritical(Statistics statistics, double collisionRateThreshold) {
        if (statistics == null || statistics.getTotalOperations() < 5) {
            return false;
        }
        return statistics.getCollisionRate() > collisionRateThreshold;
    }
}

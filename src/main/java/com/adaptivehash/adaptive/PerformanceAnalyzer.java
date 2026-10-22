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
        if (recentMetrics == null || recentMetrics.isEmpty()) {
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
     * Evaluates contiguous clusters of occupied slots.
     *
     * @param table the hash table to analyze
     * @return calculated cluster index score
     */
    public double calculateClusterFactor(HashTable<?, ?> table) {
        // Skeleton method: Clustering calculation algorithm
        return 0.0;
    }

    /**
     * Checks if collision rate exceeds acceptable threshold based on statistics.
     *
     * @param statistics current aggregated statistics
     * @param collisionRateThreshold threshold above which adaptation is triggered
     * @return true if collision rate is beyond threshold
     */
    public boolean isCollisionRateCritical(Statistics statistics, double collisionRateThreshold) {
        return statistics != null && statistics.getCollisionRate() > collisionRateThreshold;
    }
}

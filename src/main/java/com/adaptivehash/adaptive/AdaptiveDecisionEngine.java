package com.adaptivehash.adaptive;

import com.adaptivehash.probing.DoubleHashing;
import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.ProbingStrategy;
import com.adaptivehash.probing.QuadraticProbing;
import com.adaptivehash.statistics.OperationMetrics;
import com.adaptivehash.statistics.Statistics;

import java.util.List;
import java.util.Optional;

/**
 * Decision Engine that monitors hash table performance and recommends or triggers
 * dynamic adaptation to a superior probing strategy.
 */
public class AdaptiveDecisionEngine {

    // Configurable thresholds for adaptation decisions
    private double probeThreshold;
    private double collisionRateThreshold;
    private int maxProbeThreshold;
    private boolean autoAdaptEnabled;

    private final PerformanceAnalyzer analyzer;

    public AdaptiveDecisionEngine() {
        this(3.0, 0.40, 5, true);
    }

    public AdaptiveDecisionEngine(double probeThreshold,
                                  double collisionRateThreshold,
                                  int maxProbeThreshold,
                                  boolean autoAdaptEnabled) {
        this.probeThreshold = probeThreshold;
        this.collisionRateThreshold = collisionRateThreshold;
        this.maxProbeThreshold = maxProbeThreshold;
        this.autoAdaptEnabled = autoAdaptEnabled;
        this.analyzer = new PerformanceAnalyzer();
    }

    /**
     * Evaluates current performance metrics and recommends a new ProbingStrategy if necessary.
     *
     * @param currentStrategy the active probing strategy
     * @param stats overall statistics
     * @param recentHistory window of recent operations
     * @return Optional containing recommended new strategy, or empty if current strategy is optimal
     */
    public Optional<ProbingStrategy> evaluateAndRecommend(ProbingStrategy currentStrategy,
                                                          Statistics stats,
                                                          List<OperationMetrics> recentHistory) {
        if (!autoAdaptEnabled || currentStrategy == null) {
            return Optional.empty();
        }

        boolean highCollisions = analyzer.isCollisionRateCritical(stats, collisionRateThreshold);
        boolean clustering = analyzer.isClusteringDetected(recentHistory, probeThreshold);

        if (highCollisions || clustering) {
            if (currentStrategy instanceof LinearProbing) {
                return Optional.of(new QuadraticProbing());
            } else if (currentStrategy instanceof QuadraticProbing) {
                return Optional.of(new DoubleHashing());
            }
        }

        return Optional.empty();
    }

    // Configurable threshold getters and setters

    public double getProbeThreshold() {
        return probeThreshold;
    }

    public void setProbeThreshold(double probeThreshold) {
        this.probeThreshold = probeThreshold;
    }

    public double getCollisionRateThreshold() {
        return collisionRateThreshold;
    }

    public void setCollisionRateThreshold(double collisionRateThreshold) {
        this.collisionRateThreshold = collisionRateThreshold;
    }

    public int getMaxProbeThreshold() {
        return maxProbeThreshold;
    }

    public void setMaxProbeThreshold(int maxProbeThreshold) {
        this.maxProbeThreshold = maxProbeThreshold;
    }

    public boolean isAutoAdaptEnabled() {
        return autoAdaptEnabled;
    }

    public void setAutoAdaptEnabled(boolean autoAdaptEnabled) {
        this.autoAdaptEnabled = autoAdaptEnabled;
    }

    public PerformanceAnalyzer getAnalyzer() {
        return analyzer;
    }
}

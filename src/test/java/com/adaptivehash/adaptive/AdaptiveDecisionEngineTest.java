package com.adaptivehash.adaptive;

import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.ProbingStrategy;
import com.adaptivehash.probing.QuadraticProbing;
import com.adaptivehash.statistics.OperationMetrics;
import com.adaptivehash.statistics.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdaptiveDecisionEngineTest {

    private AdaptiveDecisionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AdaptiveDecisionEngine(2.5, 0.35, 5, true);
    }

    @Test
    @DisplayName("Should maintain configurable thresholds accurately")
    void testConfigurableThresholds() {
        assertEquals(2.5, engine.getProbeThreshold());
        assertEquals(0.35, engine.getCollisionRateThreshold());
        assertEquals(5, engine.getMaxProbeThreshold());
        assertTrue(engine.isAutoAdaptEnabled());

        engine.setProbeThreshold(4.0);
        assertEquals(4.0, engine.getProbeThreshold());

        engine.setCollisionRateThreshold(0.5);
        assertEquals(0.5, engine.getCollisionRateThreshold());

        engine.setAutoAdaptEnabled(false);
        assertFalse(engine.isAutoAdaptEnabled());
    }

    @Test
    @DisplayName("Should not recommend adaptation when auto-adapt is disabled")
    void testNoRecommendationWhenDisabled() {
        engine.setAutoAdaptEnabled(false);
        Statistics stats = new Statistics();
        List<OperationMetrics> history = new ArrayList<>();

        Optional<ProbingStrategy> result = engine.evaluateAndRecommend(new LinearProbing(), stats, history);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should recommend transition from Linear to Quadratic when clustering is detected")
    void testAdaptationFromLinearToQuadratic() {
        Statistics stats = new Statistics();
        List<OperationMetrics> highProbeHistory = new ArrayList<>();

        // Create operations with high probe count (e.g. 5 probes each, exceeding threshold 2.5)
        for (int i = 0; i < 5; i++) {
            OperationMetrics metric = new OperationMetrics(
                    OperationMetrics.OperationType.INSERT,
                    "key" + i,
                    1,
                    (1 + i),
                    5,
                    4,
                    true,
                    1000L
            );
            stats.recordOperation(metric);
            highProbeHistory.add(metric);
        }

        Optional<ProbingStrategy> recommendation = engine.evaluateAndRecommend(
                new LinearProbing(),
                stats,
                highProbeHistory
        );

        assertTrue(recommendation.isPresent());
        assertTrue(recommendation.get() instanceof QuadraticProbing);
    }
}

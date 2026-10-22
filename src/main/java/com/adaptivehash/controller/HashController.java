package com.adaptivehash.controller;

import com.adaptivehash.adaptive.AdaptiveDecisionEngine;
import com.adaptivehash.core.HashFunction;
import com.adaptivehash.core.HashTable;
import com.adaptivehash.history.OperationHistory;
import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.ProbingStrategy;
import com.adaptivehash.statistics.OperationMetrics;
import com.adaptivehash.statistics.Statistics;

import java.util.Optional;

/**
 * Controller coordinating operations between the Java Swing UI, the Hash Table Engine,
 * the Adaptive Decision Engine, Statistics tracker, and Operation History.
 */
public class HashController {

    private final HashTable<String, String> hashTable;
    private final AdaptiveDecisionEngine decisionEngine;
    private final Statistics statistics;
    private final OperationHistory history;

    public HashController() {
        this(HashTable.DEFAULT_CAPACITY, new LinearProbing());
    }

    public HashController(int initialCapacity, ProbingStrategy initialStrategy) {
        // Default simple hash function for strings
        HashFunction<String> hashFunction = new HashFunction<>() {
            @Override
            public int hash(String key, int capacity) {
                if (key == null || capacity <= 0) return 0;
                return Math.floorMod(key.hashCode(), capacity);
            }

            @Override
            public int secondaryHash(String key, int capacity) {
                if (key == null || capacity <= 0) return 1;
                int step = 7 - Math.floorMod(Math.abs(key.hashCode()), 7);
                return step == 0 ? 1 : step;
            }
        };

        this.hashTable = new HashTable<>(initialCapacity, initialStrategy, hashFunction);
        this.decisionEngine = new AdaptiveDecisionEngine();
        this.statistics = new Statistics();
        this.history = new OperationHistory();
    }

    /**
     * Executes an insert operation and evaluates adaptive strategy recommendations.
     *
     * @param key the key to insert
     * @param value the value to associate with key
     * @return OperationMetrics containing probe and collision data
     */
    public OperationMetrics insert(String key, String value) {
        OperationMetrics metrics = hashTable.insert(key, value);
        if (metrics != null) {
            statistics.recordOperation(metrics);
            history.record(metrics);
            evaluateAdaptation();
        }
        return metrics;
    }

    /**
     * Executes a search operation.
     *
     * @param key the key to search for
     * @return Optional containing the value if present
     */
    public Optional<String> search(String key) {
        return hashTable.search(key);
    }

    /**
     * Executes a delete operation.
     *
     * @param key the key to delete
     * @return true if deleted, false otherwise
     */
    public boolean delete(String key) {
        return hashTable.delete(key);
    }

    /**
     * Evaluates whether the probing strategy should be adapted based on performance metrics.
     */
    public void evaluateAdaptation() {
        Optional<ProbingStrategy> recommended = decisionEngine.evaluateAndRecommend(
                hashTable.getProbingStrategy(),
                statistics,
                history.getRecent(20)
        );

        recommended.ifPresent(this::switchProbingStrategy);
    }

    /**
     * Switches the active probing strategy and triggers necessary rehash.
     *
     * @param newStrategy the new probing strategy
     */
    public void switchProbingStrategy(ProbingStrategy newStrategy) {
        hashTable.setProbingStrategy(newStrategy);
    }

    public void clear() {
        hashTable.clear();
        statistics.reset();
        history.clear();
    }

    public HashTable<String, String> getHashTable() {
        return hashTable;
    }

    public AdaptiveDecisionEngine getDecisionEngine() {
        return decisionEngine;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public OperationHistory getHistory() {
        return history;
    }
}

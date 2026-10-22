package com.adaptivehash.core;

import com.adaptivehash.probing.ProbingStrategy;
import com.adaptivehash.statistics.OperationMetrics;

import java.util.Optional;

/**
 * Core generic Hash Table implementation using open addressing.
 * Supports configurable probing strategies and dynamic adaptation.
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 */
public class HashTable<K, V> {

    /**
     * Default initial capacity (typically a prime number for open addressing).
     */
    public static final int DEFAULT_CAPACITY = 17;

    /**
     * Default maximum load factor before triggering a rehash/resize.
     */
    public static final double DEFAULT_LOAD_FACTOR_THRESHOLD = 0.70;

    /**
     * Represents a single slot inside the open-addressed hash table.
     */
    public static class Entry<K, V> {
        private final K key;
        private V value;
        private boolean isDeleted;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.isDeleted = false;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public void markDeleted() {
            this.isDeleted = true;
        }
    }

    private Entry<K, V>[] table;
    private int capacity;
    private int size;
    private ProbingStrategy probingStrategy;
    private HashFunction<K> hashFunction;
    private double maxLoadFactor;

    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity, ProbingStrategy probingStrategy, HashFunction<K> hashFunction) {
        this.capacity = initialCapacity > 0 ? initialCapacity : DEFAULT_CAPACITY;
        this.probingStrategy = probingStrategy;
        this.hashFunction = hashFunction;
        this.maxLoadFactor = DEFAULT_LOAD_FACTOR_THRESHOLD;
        this.table = (Entry<K, V>[]) new Entry[this.capacity];
        this.size = 0;
    }

    /**
     * Inserts a key-value pair into the hash table.
     * Handles duplicate keys, deleted slots, negative keys, and full capacity.
     *
     * @param key the key to insert
     * @param value the associated value
     * @return the metrics recorded during this insertion operation
     */
    public OperationMetrics insert(K key, V value) {
        // Skeleton method: Algorithm logic will be implemented in subsequent phase
        return null;
    }

    /**
     * Searches for a value associated with the specified key.
     *
     * @param key the key to search for
     * @return an Optional containing the value if found, or empty if absent
     */
    public Optional<V> search(K key) {
        // Skeleton method: Search logic with probe sequence tracking
        return Optional.empty();
    }

    /**
     * Deletes the key-value mapping for the specified key using tombstone/lazy deletion.
     *
     * @param key the key to delete
     * @return true if key was found and deleted, false otherwise
     */
    public boolean delete(K key) {
        // Skeleton method: Deletion logic
        return false;
    }

    /**
     * Checks if the table contains the specified key.
     *
     * @param key the key to check
     * @return true if present, false otherwise
     */
    public boolean contains(K key) {
        return search(key).isPresent();
    }

    /**
     * Rehashes all active entries into a new table with a new capacity or strategy.
     *
     * @param newCapacity the new capacity to resize to
     */
    public void rehash(int newCapacity) {
        // Skeleton method: Rehash logic
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public double loadFactor() {
        return (double) size / capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            table[i] = null;
        }
        size = 0;
    }

    public ProbingStrategy getProbingStrategy() {
        return probingStrategy;
    }

    public void setProbingStrategy(ProbingStrategy probingStrategy) {
        this.probingStrategy = probingStrategy;
    }

    public HashFunction<K> getHashFunction() {
        return hashFunction;
    }

    public void setHashFunction(HashFunction<K> hashFunction) {
        this.hashFunction = hashFunction;
    }

    public Entry<K, V>[] getTable() {
        return table;
    }
}

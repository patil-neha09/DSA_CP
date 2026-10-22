package com.adaptivehash.core;

import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.ProbingStrategy;
import com.adaptivehash.statistics.OperationMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Core generic Hash Table implementation using open addressing.
 * Supports configurable probing strategies, explicit slot states (EMPTY, OCCUPIED, DELETED),
 * lazy deletion / tombstones, dynamic resizing at 70% load factor threshold,
 * and adaptive collision resolution.
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 */
public class HashTable<K, V> {

    /**
     * Default initial capacity (prime number for open addressing).
     */
    public static final int DEFAULT_CAPACITY = 17;

    /**
     * Default maximum load factor before triggering a rehash/resize.
     */
    public static final double DEFAULT_LOAD_FACTOR_THRESHOLD = 0.70;

    /**
     * Explicit state representation for slots in the open-addressed table.
     */
    public enum SlotState {
        EMPTY,
        OCCUPIED,
        DELETED
    }

    /**
     * Represents a single slot inside the open-addressed hash table.
     */
    public static class Entry<K, V> {
        private final K key;
        private V value;
        private SlotState state;
        private boolean isDeleted;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.state = SlotState.OCCUPIED;
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

        public SlotState getState() {
            return state;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public boolean isOccupied() {
            return state == SlotState.OCCUPIED;
        }

        public void markDeleted() {
            this.state = SlotState.DELETED;
            this.isDeleted = true;
        }
    }

    private Entry<K, V>[] table;
    private int capacity;
    private int size;
    private int deletedCount;
    private ProbingStrategy probingStrategy;
    private HashFunction<K> hashFunction;
    private double maxLoadFactor;
    private OperationMetrics lastOperationMetrics;
    private final List<OperationMetrics> metricsHistory;

    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity, ProbingStrategy probingStrategy, HashFunction<K> hashFunction) {
        if (initialCapacity <= 0) {
            this.capacity = DEFAULT_CAPACITY;
        } else {
            this.capacity = initialCapacity;
        }
        this.probingStrategy = (probingStrategy != null) ? probingStrategy : new LinearProbing();
        this.hashFunction = (hashFunction != null) ? hashFunction : (HashFunction<K>) HashFunction.DEFAULT_OBJECT_HASH;
        this.maxLoadFactor = DEFAULT_LOAD_FACTOR_THRESHOLD;
        this.table = (Entry<K, V>[]) new Entry[this.capacity];
        this.size = 0;
        this.deletedCount = 0;
        this.metricsHistory = new ArrayList<>();
    }

    public HashTable() {
        this(DEFAULT_CAPACITY, new LinearProbing(), null);
    }

    public HashTable(int initialCapacity) {
        this(initialCapacity, new LinearProbing(), null);
    }

    /**
     * Inserts a key-value pair into the hash table.
     * Prevents duplicate keys, safely handles negative keys, and reuses tombstone slots.
     * Automatically resizes when load factor would exceed 70%.
     *
     * @param key the key to insert
     * @param value the associated value
     * @return the metrics recorded during this insertion operation
     */
    public OperationMetrics insert(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Check if load factor exceeds threshold or if table is full of entries and tombstones
        if ((double) (size + 1) / capacity > maxLoadFactor || (size + deletedCount + 1) >= capacity) {
            int newCapacity = HashFunction.nextPrime(capacity * 2);
            rehash(newCapacity);
        }

        long startTime = System.nanoTime();
        int baseHash = hashFunction.hash(key, capacity);
        int maxAttempts = probingStrategy.getMaxProbes(capacity);

        int probeCount = 0;
        int collisionCount = 0;
        int firstDeletedSlot = -1;
        int finalSlotIndex = -1;
        boolean inserted = false;
        boolean duplicateFound = false;

        boolean[] visited = new boolean[capacity];

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int slot = probingStrategy.computeProbeIndex(baseHash, attempt, capacity, key);
            probeCount++;

            if (visited[slot]) {
                // Detected probe cycle
                break;
            }
            visited[slot] = true;

            Entry<K, V> entry = table[slot];

            if (entry == null) {
                // Found EMPTY slot: probe chain terminates without finding duplicate
                if (firstDeletedSlot != -1) {
                    finalSlotIndex = firstDeletedSlot;
                    table[finalSlotIndex] = new Entry<>(key, value);
                    deletedCount--;
                } else {
                    finalSlotIndex = slot;
                    table[finalSlotIndex] = new Entry<>(key, value);
                }
                size++;
                inserted = true;
                break;
            }

            if (entry.isDeleted()) {
                // Tombstone encountered: collision occurred, record first deleted slot and continue
                collisionCount++;
                if (firstDeletedSlot == -1) {
                    firstDeletedSlot = slot;
                }
                continue;
            }

            // Occupied slot: check for duplicate key
            if (entry.getKey().equals(key)) {
                duplicateFound = true;
                finalSlotIndex = slot;
                break;
            }

            // Occupied by different key -> collision
            collisionCount++;
        }

        // If loop finished without inserting and key wasn't duplicate, reuse tombstone if available
        if (!inserted && !duplicateFound && firstDeletedSlot != -1) {
            finalSlotIndex = firstDeletedSlot;
            table[finalSlotIndex] = new Entry<>(key, value);
            deletedCount--;
            size++;
            inserted = true;
        }

        long elapsedNanos = System.nanoTime() - startTime;
        OperationMetrics metrics = new OperationMetrics(
                OperationMetrics.OperationType.INSERT,
                key,
                baseHash,
                finalSlotIndex,
                probeCount,
                collisionCount,
                inserted,
                elapsedNanos
        );
        this.lastOperationMetrics = metrics;
        this.metricsHistory.add(metrics);
        return metrics;
    }

    /**
     * Convenience method to insert a key with itself as the value (e.g. for Integer sets).
     * Returns true if insertion succeeded, false if duplicate or table full.
     *
     * @param key the key to insert
     * @return true if successfully inserted, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean insert(K key) {
        V val = null;
        try {
            val = (V) key;
        } catch (Exception ignored) {
        }
        OperationMetrics m = insert(key, val);
        return m != null && m.isSuccessful();
    }

    /**
     * Searches for a value associated with the specified key.
     * Follows the probe chain and safely bypasses tombstones without breaking the chain.
     *
     * @param key the key to search for
     * @return an Optional containing the value if found, or empty if absent
     */
    public Optional<V> search(K key) {
        if (key == null) {
            return Optional.empty();
        }

        long startTime = System.nanoTime();
        int baseHash = hashFunction.hash(key, capacity);
        int maxAttempts = probingStrategy.getMaxProbes(capacity);

        int probeCount = 0;
        int collisionCount = 0;
        int finalSlotIndex = -1;
        Optional<V> result = Optional.empty();

        boolean[] visited = new boolean[capacity];

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int slot = probingStrategy.computeProbeIndex(baseHash, attempt, capacity, key);
            probeCount++;

            if (visited[slot]) {
                break;
            }
            visited[slot] = true;

            Entry<K, V> entry = table[slot];

            if (entry == null) {
                // Empty slot reached: end of probe sequence, key not present
                break;
            }

            if (entry.isDeleted()) {
                // Deleted slot: do not terminate! Key may have been inserted after this slot was deleted
                collisionCount++;
                continue;
            }

            if (entry.getKey().equals(key)) {
                // Key found
                result = Optional.ofNullable(entry.getValue());
                finalSlotIndex = slot;
                break;
            }

            collisionCount++;
        }

        long elapsedNanos = System.nanoTime() - startTime;
        OperationMetrics metrics = new OperationMetrics(
                OperationMetrics.OperationType.SEARCH,
                key,
                baseHash,
                finalSlotIndex,
                probeCount,
                collisionCount,
                result.isPresent(),
                elapsedNanos
        );
        this.lastOperationMetrics = metrics;
        this.metricsHistory.add(metrics);
        return result;
    }

    /**
     * Deletes the key-value mapping for the specified key using tombstone/lazy deletion.
     *
     * @param key the key to delete
     * @return true if key was found and deleted, false otherwise
     */
    public boolean delete(K key) {
        if (key == null) {
            return false;
        }

        long startTime = System.nanoTime();
        int baseHash = hashFunction.hash(key, capacity);
        int maxAttempts = probingStrategy.getMaxProbes(capacity);

        int probeCount = 0;
        int collisionCount = 0;
        int finalSlotIndex = -1;
        boolean deleted = false;

        boolean[] visited = new boolean[capacity];

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int slot = probingStrategy.computeProbeIndex(baseHash, attempt, capacity, key);
            probeCount++;

            if (visited[slot]) {
                break;
            }
            visited[slot] = true;

            Entry<K, V> entry = table[slot];

            if (entry == null) {
                break;
            }

            if (entry.isDeleted()) {
                collisionCount++;
                continue;
            }

            if (entry.getKey().equals(key)) {
                entry.markDeleted();
                size--;
                deletedCount++;
                deleted = true;
                finalSlotIndex = slot;
                break;
            }

            collisionCount++;
        }

        long elapsedNanos = System.nanoTime() - startTime;
        OperationMetrics metrics = new OperationMetrics(
                OperationMetrics.OperationType.DELETE,
                key,
                baseHash,
                finalSlotIndex,
                probeCount,
                collisionCount,
                deleted,
                elapsedNanos
        );
        this.lastOperationMetrics = metrics;
        this.metricsHistory.add(metrics);
        return deleted;
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
     * Purges tombstones and rebuilds probe sequences cleanly.
     *
     * @param newCapacity the new capacity to resize to
     */
    @SuppressWarnings("unchecked")
    public void rehash(int newCapacity) {
        int primeCapacity = newCapacity;
        if (!HashFunction.isPrime(primeCapacity)) {
            primeCapacity = HashFunction.nextPrime(primeCapacity);
        }
        if (primeCapacity <= size) {
            primeCapacity = HashFunction.nextPrime(size * 2 + 1);
        }

        Entry<K, V>[] oldTable = table;
        int oldCapacity = capacity;

        this.capacity = primeCapacity;
        this.table = (Entry<K, V>[]) new Entry[this.capacity];
        this.size = 0;
        this.deletedCount = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Entry<K, V> entry = oldTable[i];
            if (entry != null && !entry.isDeleted()) {
                insert(entry.getKey(), entry.getValue());
            }
        }
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public double loadFactor() {
        return (double) size / capacity;
    }

    public double totalLoadFactor() {
        return (double) (size + deletedCount) / capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            table[i] = null;
        }
        size = 0;
        deletedCount = 0;
        metricsHistory.clear();
    }

    /**
     * Retrieves the unmodifiable chronological list of all operation metrics recorded by this hash table.
     *
     * @return unmodifiable list of OperationMetrics
     */
    public List<OperationMetrics> getMetrics() {
        return Collections.unmodifiableList(new ArrayList<>(metricsHistory));
    }

    /**
     * Retrieves the most recent operation metric recorded for the specified key.
     *
     * @param key the key to find metrics for
     * @return OperationMetrics for the key, or null if not found
     */
    public OperationMetrics getMetrics(K key) {
        for (int i = metricsHistory.size() - 1; i >= 0; i--) {
            OperationMetrics m = metricsHistory.get(i);
            if (key != null && key.equals(m.getKey())) {
                return m;
            }
        }
        return null;
    }

    public ProbingStrategy getProbingStrategy() {
        return probingStrategy;
    }

    public void setProbingStrategy(ProbingStrategy probingStrategy) {
        this.probingStrategy = (probingStrategy != null) ? probingStrategy : new LinearProbing();
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

    public SlotState getSlotState(int index) {
        if (index < 0 || index >= capacity || table[index] == null) {
            return SlotState.EMPTY;
        }
        return table[index].getState();
    }

    public OperationMetrics getLastOperationMetrics() {
        return lastOperationMetrics;
    }

    public double getMaxLoadFactor() {
        return maxLoadFactor;
    }

    public void setMaxLoadFactor(double maxLoadFactor) {
        if (maxLoadFactor <= 0.0 || maxLoadFactor > 1.0) {
            throw new IllegalArgumentException("Max load factor must be in range (0.0, 1.0]");
        }
        this.maxLoadFactor = maxLoadFactor;
    }
}

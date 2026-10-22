package com.adaptivehash.core;

/**
 * Interface representing a hash function generator for open-address hash tables.
 * Supports primary hashing and secondary hashing (used in Double Hashing).
 *
 * @param <K> the type of keys maintained by the hash table
 */
public interface HashFunction<K> {

    /**
     * Computes the primary hash index for a given key within the table capacity.
     * Ensures the returned index is non-negative and strictly in range [0, capacity - 1].
     *
     * @param key the key to hash
     * @param capacity the current capacity of the hash table
     * @return a valid slot index between 0 and capacity - 1
     */
    int hash(K key, int capacity);

    /**
     * Computes the secondary step size for double hashing.
     * Must return a positive integer coprime with table capacity (or at least > 0).
     *
     * @param key the key to hash
     * @param capacity the current capacity of the hash table
     * @return a positive step size, typically 1 <= step < capacity
     */
    int secondaryHash(K key, int capacity);
}

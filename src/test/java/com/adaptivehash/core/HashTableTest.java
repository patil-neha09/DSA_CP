package com.adaptivehash.core;

import com.adaptivehash.probing.LinearProbing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashTableTest {

    private HashTable<String, String> hashTable;

    @BeforeEach
    void setUp() {
        HashFunction<String> hashFunction = new HashFunction<>() {
            @Override
            public int hash(String key, int capacity) {
                return key == null ? 0 : Math.floorMod(key.hashCode(), capacity);
            }

            @Override
            public int secondaryHash(String key, int capacity) {
                return 1;
            }
        };

        hashTable = new HashTable<>(17, new LinearProbing(), hashFunction);
    }

    @Test
    @DisplayName("Should initialize hash table with specified capacity and empty state")
    void testInitialState() {
        assertNotNull(hashTable);
        assertEquals(17, hashTable.capacity());
        assertEquals(0, hashTable.size());
        assertTrue(hashTable.isEmpty());
        assertEquals(0.0, hashTable.loadFactor());
    }

    @Test
    @DisplayName("Should clear all entries in hash table")
    void testClear() {
        hashTable.clear();
        assertEquals(0, hashTable.size());
        assertTrue(hashTable.isEmpty());
    }

    @Test
    @DisplayName("Should maintain probing strategy reference correctly")
    void testProbingStrategy() {
        assertNotNull(hashTable.getProbingStrategy());
        assertEquals("Linear Probing", hashTable.getProbingStrategy().getName());
    }
}

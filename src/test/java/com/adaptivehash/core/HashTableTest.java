package com.adaptivehash.core;

import com.adaptivehash.probing.DoubleHashing;
import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.QuadraticProbing;
import com.adaptivehash.statistics.OperationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    private HashTable<Integer, String> intTable;
    private HashTable<String, String> stringTable;

    @BeforeEach
    void setUp() {
        intTable = new HashTable<>(7, new LinearProbing(), HashFunction.DEFAULT_INTEGER_HASH);
        stringTable = new HashTable<>(17, new LinearProbing(), new HashFunction<>() {
            @Override
            public int hash(String key, int capacity) {
                return HashFunction.computePrimaryHash(key, capacity);
            }

            @Override
            public int secondaryHash(String key, int capacity) {
                return HashFunction.computeSecondaryHash(key, capacity);
            }
        });
    }

    @Test
    @DisplayName("Should initialize hash table with specified capacity and empty state")
    void testInitialState() {
        assertNotNull(intTable);
        assertEquals(7, intTable.capacity());
        assertEquals(0, intTable.size());
        assertEquals(0, intTable.getDeletedCount());
        assertTrue(intTable.isEmpty());
        assertEquals(0.0, intTable.loadFactor());
    }

    @Test
    @DisplayName("Should clear all entries and tombstones in hash table")
    void testClear() {
        intTable.insert(10, "ten");
        intTable.insert(20, "twenty");
        intTable.delete(10);
        assertEquals(1, intTable.size());
        assertEquals(1, intTable.getDeletedCount());

        intTable.clear();
        assertEquals(0, intTable.size());
        assertEquals(0, intTable.getDeletedCount());
        assertTrue(intTable.isEmpty());
        assertFalse(intTable.contains(20));
    }

    @Test
    @DisplayName("Should maintain probing strategy reference correctly")
    void testProbingStrategy() {
        assertNotNull(stringTable.getProbingStrategy());
        assertEquals("Linear Probing", stringTable.getProbingStrategy().getName());

        stringTable.setProbingStrategy(new QuadraticProbing());
        assertEquals("Quadratic Probing", stringTable.getProbingStrategy().getName());
    }

    @Test
    @DisplayName("Should insert and search keys correctly")
    void testBasicInsertionAndSearch() {
        OperationMetrics m1 = intTable.insert(1, "one");
        assertTrue(m1.isSuccessful());
        assertEquals(1, intTable.size());

        Optional<String> res1 = intTable.search(1);
        assertTrue(res1.isPresent());
        assertEquals("one", res1.get());

        OperationMetrics m2 = stringTable.insert("apple", "fruit");
        assertTrue(m2.isSuccessful());
        Optional<String> res2 = stringTable.search("apple");
        assertTrue(res2.isPresent());
        assertEquals("fruit", res2.get());
    }

    @Test
    @DisplayName("Should prevent and reject duplicate keys")
    void testDuplicateKeyRejection() {
        OperationMetrics m1 = intTable.insert(42, "first");
        assertTrue(m1.isSuccessful());
        assertEquals(1, intTable.size());

        OperationMetrics m2 = intTable.insert(42, "duplicate");
        assertFalse(m2.isSuccessful(), "Duplicate insertion must be rejected");
        assertEquals(1, intTable.size());
        assertEquals("first", intTable.search(42).orElse(null));
    }

    @Test
    @DisplayName("Should safely handle negative keys using Math.floorMod")
    void testNegativeKeys() {
        // -4 mod 7 = 3 (since -4 = -1*7 + 3)
        OperationMetrics mNeg = intTable.insert(-4, "negFour");
        assertTrue(mNeg.isSuccessful());
        assertEquals(3, mNeg.getInitialHash());
        assertEquals(3, mNeg.getFinalSlotIndex());

        Optional<String> res = intTable.search(-4);
        assertTrue(res.isPresent());
        assertEquals("negFour", res.get());

        // Extreme negative value: Integer.MIN_VALUE
        OperationMetrics mMin = intTable.insert(Integer.MIN_VALUE, "minVal");
        assertTrue(mMin.isSuccessful());
        assertTrue(mMin.getInitialHash() >= 0 && mMin.getInitialHash() < intTable.capacity());
        assertEquals("minVal", intTable.search(Integer.MIN_VALUE).orElse(null));
    }

    @Test
    @DisplayName("Should correctly handle collisions and sequence probing")
    void testCollisionHandling() {
        // 10 mod 7 = 3, 17 mod 7 = 3 -> COLLISION at index 3
        OperationMetrics m1 = intTable.insert(10, "val10");
        OperationMetrics m2 = intTable.insert(17, "val17");

        assertTrue(m1.isSuccessful());
        assertTrue(m2.isSuccessful());

        assertEquals(3, m1.getFinalSlotIndex());
        assertEquals(1, m1.getProbeCount());
        assertEquals(0, m1.getCollisionCount());

        assertEquals(4, m2.getFinalSlotIndex(), "Linear probing should place 17 in slot 4");
        assertEquals(2, m2.getProbeCount());
        assertEquals(1, m2.getCollisionCount());

        assertEquals("val10", intTable.search(10).orElse(null));
        assertEquals("val17", intTable.search(17).orElse(null));
    }

    @Test
    @DisplayName("Should perform lazy deletion using tombstones and track counts separately")
    void testLazyDeletion() {
        intTable.insert(10, "val10");
        intTable.insert(17, "val17");
        assertEquals(2, intTable.size());
        assertEquals(0, intTable.getDeletedCount());

        boolean deleted = intTable.delete(10);
        assertTrue(deleted);
        assertEquals(1, intTable.size());
        assertEquals(1, intTable.getDeletedCount());
        assertEquals(HashTable.SlotState.DELETED, intTable.getSlotState(3));
        assertFalse(intTable.contains(10));
    }

    @Test
    @DisplayName("Search must bypass tombstones and not break probe chains")
    void testSearchBypassesTombstones() {
        // 10 placed at slot 3, 17 placed at slot 4
        intTable.insert(10, "val10");
        intTable.insert(17, "val17");

        // Delete key 10 at slot 3
        intTable.delete(10);

        // Searching for 17 must bypass deleted slot 3 and find 17 at slot 4!
        Optional<String> res = intTable.search(17);
        assertTrue(res.isPresent(), "Search must bypass tombstone at slot 3 to find key 17 at slot 4");
        assertEquals("val17", res.get());
    }

    @Test
    @DisplayName("Should reuse deleted slots on subsequent insertions without breaking search")
    void testReusingDeletedSlots() {
        // Insert 10 at slot 3, 17 at slot 4
        intTable.insert(10, "val10");
        intTable.insert(17, "val17");

        // Delete 10 -> slot 3 is DELETED
        intTable.delete(10);
        assertEquals(1, intTable.getDeletedCount());

        // Insert 24 (24 mod 7 = 3) -> should reuse slot 3
        OperationMetrics mReuse = intTable.insert(24, "val24");
        assertTrue(mReuse.isSuccessful());
        assertEquals(3, mReuse.getFinalSlotIndex(), "Slot 3 should be reused");
        assertEquals(0, intTable.getDeletedCount(), "Deleted count should decrement on reuse");
        assertEquals(2, intTable.size());

        // Verify both 24 and 17 can still be found
        assertEquals("val24", intTable.search(24).orElse(null));
        assertEquals("val17", intTable.search(17).orElse(null));
    }

    @Test
    @DisplayName("Should return empty Optional when searching for missing keys")
    void testMissingKey() {
        intTable.insert(1, "one");
        intTable.insert(2, "two");

        assertFalse(intTable.search(99).isPresent());
        assertFalse(intTable.contains(99));

        OperationMetrics searchMetrics = intTable.getLastOperationMetrics();
        assertNotNull(searchMetrics);
        assertFalse(searchMetrics.isSuccessful());
    }

    @Test
    @DisplayName("Should automatically resize table when load factor exceeds 70% and preserve all keys")
    void testResizingAndKeyPreservation() {
        // Capacity = 7. Threshold = 70% (5 keys would be 5/7 = 0.71 > 0.70)
        assertEquals(7, intTable.capacity());

        intTable.insert(1, "v1");
        intTable.insert(2, "v2");
        intTable.insert(3, "v3");
        intTable.insert(4, "v4");
        assertEquals(7, intTable.capacity());

        // 5th key triggers resize
        intTable.insert(5, "v5");
        assertTrue(intTable.capacity() > 7, "Table must resize past capacity 7");
        assertTrue(HashFunction.isPrime(intTable.capacity()), "New capacity must be prime");
        assertEquals(5, intTable.size());

        // All 5 keys must still be found!
        for (int k = 1; k <= 5; k++) {
            assertTrue(intTable.contains(k), "Key " + k + " must be preserved after resize");
            assertEquals("v" + k, intTable.search(k).orElse(null));
        }
    }

    @Test
    @DisplayName("Should support Quadratic Probing and Double Hashing strategies correctly")
    void testAlternativeProbingStrategies() {
        // Quadratic Probing table
        HashTable<Integer, String> qpTable = new HashTable<>(17, new QuadraticProbing(1, 1), HashFunction.DEFAULT_INTEGER_HASH);
        qpTable.insert(3, "three");
        qpTable.insert(20, "twenty"); // 20 mod 17 = 3 (collision)
        assertTrue(qpTable.contains(3));
        assertTrue(qpTable.contains(20));

        // Double Hashing table
        HashTable<Integer, String> dhTable = new HashTable<>(17, new DoubleHashing(), HashFunction.DEFAULT_INTEGER_HASH);
        dhTable.insert(5, "five");
        dhTable.insert(22, "twentyTwo"); // 22 mod 17 = 5 (collision)
        assertTrue(dhTable.contains(5));
        assertTrue(dhTable.contains(22));
    }

    @Test
    @DisplayName("Should validate invalid arguments and throw appropriate exceptions")
    void testInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> intTable.insert(null, "val"));
        assertThrows(IllegalArgumentException.class, () -> intTable.setMaxLoadFactor(-0.5));
        assertThrows(IllegalArgumentException.class, () -> intTable.setMaxLoadFactor(1.5));
    }

    @Test
    @DisplayName("Should record accurate operation metrics with non-zero timestamps and nanosecond execution times")
    void testOperationMetricsDetails() {
        OperationMetrics m = intTable.insert(15, "fifteen");
        assertNotNull(m);
        assertEquals(OperationMetrics.OperationType.INSERT, m.getOperationType());
        assertEquals(15, m.getKey());
        assertTrue(m.getExecutionTimeNanos() >= 0);
        assertTrue(m.getTimestampMillis() > 0);
        assertTrue(m.getProbeCount() >= 1);
        assertTrue(m.getCollisionCount() >= 0);
    }

    // =========================================================================
    // Required Verification Scenario (per task spec)
    // =========================================================================

    /**
     * Verifies the exact collision scenario:
     * - capacity 7, Linear Probing
     * - insert(10) -> hash=3, placed at slot 3, 1 probe, 0 collisions -> returns true
     * - insert(17) -> hash=3, slot 3 occupied -> collision! probes slot 4 -> placed at slot 4,
     *                 2 probes, 1 collision -> returns true
     * - getMetrics() contains both operations; the metric for key 17 reports probes==2, collisions==1
     * - search(10) and search(17) both succeed
     */
    @Test
    @DisplayName("insert(10) and insert(17) must collide at index 3: probes==2 and collisions==1 for key 17")
    void testCollisionScenarioWithBooleanInsertAndGetMetrics() {
        // Use a fresh capacity-7 Linear Probing table with DEFAULT_INTEGER_HASH
        HashTable<Integer, Integer> table =
                new HashTable<>(7, new LinearProbing(), HashFunction.DEFAULT_INTEGER_HASH);

        // --- Precondition: hashes must both be 3 ---
        assertEquals(3, HashFunction.computePrimaryHash(10, 7),
                "10 mod 7 must equal 3");
        assertEquals(3, HashFunction.computePrimaryHash(17, 7),
                "17 mod 7 must equal 3");

        // --- insert(10) via boolean convenience method ---
        boolean inserted10 = table.insert(10);
        assertTrue(inserted10, "insert(10) must return true");
        assertEquals(1, table.size());

        // Confirm key 10 occupies slot 3
        OperationMetrics m10 = table.getMetrics(10);
        assertNotNull(m10, "getMetrics(10) must return a recorded metric");
        assertEquals(OperationMetrics.OperationType.INSERT, m10.getOperationType());
        assertEquals(3, m10.getInitialHash(), "Initial hash for key 10 must be 3");
        assertEquals(3, m10.getFinalSlotIndex(), "Key 10 must land at slot 3");
        assertEquals(1, m10.getProbeCount(), "insert(10) into empty table needs exactly 1 probe");
        assertEquals(0, m10.getCollisionCount(), "insert(10) into empty table must report 0 collisions");
        assertTrue(m10.isSuccessful());

        // --- insert(17) via boolean convenience method ---
        boolean inserted17 = table.insert(17);
        assertTrue(inserted17, "insert(17) must return true");
        assertEquals(2, table.size());

        // Confirm key 17 was pushed to slot 4 by the collision at slot 3
        OperationMetrics m17 = table.getMetrics(17);
        assertNotNull(m17, "getMetrics(17) must return a recorded metric");
        assertEquals(OperationMetrics.OperationType.INSERT, m17.getOperationType());
        assertEquals(3, m17.getInitialHash(), "Initial hash for key 17 must be 3 (same as 10)");
        assertEquals(4, m17.getFinalSlotIndex(), "Key 17 must be probed to slot 4");
        assertEquals(2, m17.getProbeCount(),
                "insert(17) requires exactly 2 probes: slot 3 (occupied), slot 4 (empty)");
        assertEquals(1, m17.getCollisionCount(),
                "insert(17) must record exactly 1 collision (slot 3 was occupied)");
        assertTrue(m17.isSuccessful());

        // --- getMetrics() list must contain both records ---
        List<OperationMetrics> allMetrics = table.getMetrics();
        // Only INSERT operations have been recorded (no searches yet)
        assertEquals(2, allMetrics.size(), "getMetrics() must hold exactly 2 INSERT records");

        boolean found10 = allMetrics.stream()
                .anyMatch(m -> Integer.valueOf(10).equals(m.getKey())
                        && m.getOperationType() == OperationMetrics.OperationType.INSERT);
        boolean found17 = allMetrics.stream()
                .anyMatch(m -> Integer.valueOf(17).equals(m.getKey())
                        && m.getOperationType() == OperationMetrics.OperationType.INSERT
                        && m.getProbeCount() == 2
                        && m.getCollisionCount() == 1);
        assertTrue(found10, "getMetrics() must contain an INSERT record for key 10");
        assertTrue(found17,
                "getMetrics() must contain an INSERT record for key 17 with probes==2 and collisions==1");

        // --- Physical table layout: slot 3 = key 10, slot 4 = key 17 ---
        HashTable.Entry<Integer, Integer>[] slots = table.getTable();
        assertNotNull(slots[3], "Slot 3 must be non-null");
        assertEquals(HashTable.SlotState.OCCUPIED, slots[3].getState());
        assertEquals(10, slots[3].getKey(), "Slot 3 must hold key 10");

        assertNotNull(slots[4], "Slot 4 must be non-null");
        assertEquals(HashTable.SlotState.OCCUPIED, slots[4].getState());
        assertEquals(17, slots[4].getKey(), "Slot 4 must hold key 17");

        // --- Search must find both keys ---
        Optional<Integer> search10 = table.search(10);
        assertTrue(search10.isPresent(), "search(10) must succeed");
        assertEquals(10, search10.get(), "search(10) must return value 10");

        Optional<Integer> search17 = table.search(17);
        assertTrue(search17.isPresent(), "search(17) must succeed");
        assertEquals(17, search17.get(), "search(17) must return value 17");
    }
}

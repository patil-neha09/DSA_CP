package com.adaptivehash.probing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProbingStrategyTest {

    @Test
    @DisplayName("Linear Probing should increment index sequentially modulo capacity")
    void testLinearProbing() {
        ProbingStrategy strategy = new LinearProbing();
        int capacity = 11;
        int baseHash = 4;

        assertEquals("Linear Probing", strategy.getName());
        assertEquals(4, strategy.computeProbeIndex(baseHash, 0, capacity, "key1"));
        assertEquals(5, strategy.computeProbeIndex(baseHash, 1, capacity, "key1"));
        assertEquals(6, strategy.computeProbeIndex(baseHash, 2, capacity, "key1"));
        assertEquals(7, strategy.computeProbeIndex(baseHash, 3, capacity, "key1"));
        // Test wrap-around: 4 + 7 = 11 % 11 = 0
        assertEquals(0, strategy.computeProbeIndex(baseHash, 7, capacity, "key1"));
    }

    @Test
    @DisplayName("Quadratic Probing should increment by c1*i + c2*i^2 modulo capacity")
    void testQuadraticProbing() {
        ProbingStrategy strategy = new QuadraticProbing(1, 1);
        int capacity = 17;
        int baseHash = 3;

        assertEquals("Quadratic Probing", strategy.getName());
        // i = 0: (3 + 0) % 17 = 3
        assertEquals(3, strategy.computeProbeIndex(baseHash, 0, capacity, "key1"));
        // i = 1: (3 + 1 + 1) % 17 = 5
        assertEquals(5, strategy.computeProbeIndex(baseHash, 1, capacity, "key1"));
        // i = 2: (3 + 2 + 4) % 17 = 9
        assertEquals(9, strategy.computeProbeIndex(baseHash, 2, capacity, "key1"));
    }

    @Test
    @DisplayName("Double Hashing should step according to secondary hash")
    void testDoubleHashing() {
        ProbingStrategy strategy = new DoubleHashing();
        int capacity = 17;
        int baseHash = 2;

        assertEquals("Double Hashing", strategy.getName());
        int firstSlot = strategy.computeProbeIndex(baseHash, 0, capacity, "testKey");
        assertEquals(2, firstSlot);
    }
}

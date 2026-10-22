package com.adaptivehash.probing;

import com.adaptivehash.core.HashFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        // Wrap-around: (4 + 7) % 11 = 0
        assertEquals(0, strategy.computeProbeIndex(baseHash, 7, capacity, "key1"));
        // Negative baseHash safety
        assertEquals(3, strategy.computeProbeIndex(-4, 0, 7, "key1"));
    }

    @Test
    @DisplayName("Quadratic Probing should increment by c1*i + c2*i^2 modulo capacity")
    void testQuadraticProbing() {
        QuadraticProbing strategy = new QuadraticProbing(1, 1);
        int capacity = 17;
        int baseHash = 3;

        assertEquals("Quadratic Probing", strategy.getName());
        assertEquals(1, strategy.getC1());
        assertEquals(1, strategy.getC2());

        // i = 0: (3 + 0) % 17 = 3
        assertEquals(3, strategy.computeProbeIndex(baseHash, 0, capacity, "key1"));
        // i = 1: (3 + 1 + 1) % 17 = 5
        assertEquals(5, strategy.computeProbeIndex(baseHash, 1, capacity, "key1"));
        // i = 2: (3 + 2 + 4) % 17 = 9
        assertEquals(9, strategy.computeProbeIndex(baseHash, 2, capacity, "key1"));
        // i = 3: (3 + 3 + 9) % 17 = 15
        assertEquals(15, strategy.computeProbeIndex(baseHash, 3, capacity, "key1"));
        // i = 4: (3 + 4 + 16) % 17 = 23 % 17 = 6
        assertEquals(6, strategy.computeProbeIndex(baseHash, 4, capacity, "key1"));

        assertEquals(17, strategy.getMaxProbes(17));
    }

    @Test
    @DisplayName("Double Hashing should step according to secondary hash and guarantee coprime traversal")
    void testDoubleHashing() {
        ProbingStrategy strategy = new DoubleHashing();
        int capacity = 17;
        int baseHash = 2;

        assertEquals("Double Hashing", strategy.getName());
        int slot0 = strategy.computeProbeIndex(baseHash, 0, capacity, 10);
        assertEquals(2, slot0);

        int slot1 = strategy.computeProbeIndex(baseHash, 1, capacity, 10);
        assertTrue(slot1 >= 0 && slot1 < capacity);
        assertNotEquals(slot0, slot1);

        // Ensure all probes in cycle visit distinct slots when capacity is prime
        boolean[] seen = new boolean[capacity];
        for (int i = 0; i < capacity; i++) {
            int slot = strategy.computeProbeIndex(baseHash, i, capacity, 42);
            assertFalse(seen[slot], "Double hashing must visit distinct slots in first m attempts for prime capacity");
            seen[slot] = true;
        }
    }

    @Test
    @DisplayName("HashFunction utilities should compute floorMod, primes, and step sizes correctly")
    void testHashFunctionUtilities() {
        // Validation
        assertThrows(IllegalArgumentException.class, () -> HashFunction.validateCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> HashFunction.validateCapacity(-5));

        // Primary hash with negative numbers
        assertEquals(3, HashFunction.computePrimaryHash(-4, 7));
        assertEquals(3, HashFunction.computePrimaryHash(10, 7));
        assertEquals(3, HashFunction.computePrimaryHash(17, 7));

        // Prime checks
        assertTrue(HashFunction.isPrime(2));
        assertTrue(HashFunction.isPrime(3));
        assertTrue(HashFunction.isPrime(7));
        assertTrue(HashFunction.isPrime(17));
        assertTrue(HashFunction.isPrime(19));
        assertFalse(HashFunction.isPrime(1));
        assertFalse(HashFunction.isPrime(4));
        assertFalse(HashFunction.isPrime(15));

        // Next prime
        assertEquals(17, HashFunction.nextPrime(14));
        assertEquals(19, HashFunction.nextPrime(17));
        assertEquals(23, HashFunction.nextPrime(19));

        // Largest prime less than n
        assertEquals(13, HashFunction.findLargestPrimeLessThan(17));
        assertEquals(7, HashFunction.findLargestPrimeLessThan(11));
        assertEquals(5, HashFunction.findLargestPrimeLessThan(7));

        // Secondary hash step size must be strictly positive and < capacity
        int step = HashFunction.computeSecondaryHash(10, 7);
        assertTrue(step >= 1 && step < 7);
    }
}

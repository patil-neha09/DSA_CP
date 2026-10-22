package com.adaptivehash.core;

/**
 * Interface and utility toolkit representing a hash function generator for open-address hash tables.
 * Supports primary hashing, secondary hashing (for Double Hashing), capacity validation,
 * and prime-number mathematical utilities.
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
     * @throws IllegalArgumentException if capacity <= 0
     */
    int hash(K key, int capacity);

    /**
     * Computes the secondary step size for double hashing.
     * Must return a positive integer coprime with table capacity (or at least > 0).
     *
     * @param key the key to hash
     * @param capacity the current capacity of the hash table
     * @return a positive step size, strictly 1 <= step < capacity (or 1 if capacity <= 2)
     * @throws IllegalArgumentException if capacity <= 0
     */
    int secondaryHash(K key, int capacity);

    // =========================================================================
    // Static Utility Methods
    // =========================================================================

    /**
     * Validates that table capacity is strictly positive.
     *
     * @param capacity table capacity to validate
     * @throws IllegalArgumentException if capacity <= 0
     */
    static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Hash table capacity must be strictly positive, got: " + capacity);
        }
    }

    /**
     * Computes primary hash index for an integer key safely using Math.floorMod to handle negative keys.
     *
     * @param key integer key
     * @param capacity table capacity
     * @return valid non-negative slot index in [0, capacity - 1]
     */
    static int computePrimaryHash(int key, int capacity) {
        validateCapacity(capacity);
        return Math.floorMod(key, capacity);
    }

    /**
     * Computes primary hash index for a generic key safely using Math.floorMod on hashCode().
     *
     * @param key generic key
     * @param capacity table capacity
     * @return valid non-negative slot index in [0, capacity - 1]
     */
    static int computePrimaryHash(Object key, int capacity) {
        validateCapacity(capacity);
        if (key == null) {
            return 0;
        }
        if (key instanceof Number) {
            long val = ((Number) key).longValue();
            return (int) Math.floorMod(val, (long) capacity);
        }
        if (key instanceof CharSequence) {
            try {
                long val = Long.parseLong(key.toString().trim());
                return (int) Math.floorMod(val, (long) capacity);
            } catch (NumberFormatException ignored) {
            }
        }
        return Math.floorMod(key.hashCode(), capacity);
    }

    /**
     * Computes secondary hash step size for double hashing: h2(k) = q - (k mod q)
     * where q is the largest prime strictly less than capacity.
     * Guarantees 1 <= step < capacity (or 1 if capacity <= 2).
     *
     * @param key integer key
     * @param capacity table capacity
     * @return non-zero step size
     */
    static int computeSecondaryHash(int key, int capacity) {
        validateCapacity(capacity);
        if (capacity <= 2) {
            return 1;
        }
        int q = findLargestPrimeLessThan(capacity);
        if (q <= 1) {
            q = 1;
        }
        int rem = Math.floorMod(key, q);
        int step = q - rem;
        return (step <= 0) ? 1 : step;
    }

    /**
     * Computes secondary hash step size for a generic key.
     *
     * @param key generic key
     * @param capacity table capacity
     * @return non-zero step size
     */
    static int computeSecondaryHash(Object key, int capacity) {
        validateCapacity(capacity);
        if (key == null || capacity <= 2) {
            return 1;
        }
        if (key instanceof Number) {
            long l = ((Number) key).longValue();
            return computeSecondaryHash((int) l, capacity);
        }
        if (key instanceof CharSequence) {
            try {
                long l = Long.parseLong(key.toString().trim());
                return computeSecondaryHash((int) l, capacity);
            } catch (NumberFormatException ignored) {
            }
        }
        return computeSecondaryHash(key.hashCode(), capacity);
    }

    /**
     * Checks if a given integer is prime.
     *
     * @param n integer to check
     * @return true if prime, false otherwise
     */
    static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; (long) i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the smallest prime number strictly greater than n.
     *
     * @param n lower bound
     * @return next prime number
     */
    static int nextPrime(int n) {
        if (n <= 1) return 2;
        int candidate = (n % 2 == 0) ? n + 1 : n + 2;
        while (!isPrime(candidate)) {
            candidate += 2;
            if (candidate < 0) {
                return Integer.MAX_VALUE;
            }
        }
        return candidate;
    }

    /**
     * Finds the largest prime number strictly less than n.
     *
     * @param n upper bound
     * @return largest prime < n, or 1 if none exists
     */
    static int findLargestPrimeLessThan(int n) {
        for (int candidate = n - 1; candidate >= 2; candidate--) {
            if (isPrime(candidate)) {
                return candidate;
            }
        }
        return 1;
    }

    /**
     * Default hash function implementation for Integer keys.
     */
    HashFunction<Integer> DEFAULT_INTEGER_HASH = new HashFunction<>() {
        @Override
        public int hash(Integer key, int capacity) {
            return HashFunction.computePrimaryHash(key != null ? key : 0, capacity);
        }

        @Override
        public int secondaryHash(Integer key, int capacity) {
            return HashFunction.computeSecondaryHash(key != null ? key : 0, capacity);
        }
    };

    /**
     * Default hash function implementation for generic Object keys.
     */
    HashFunction<Object> DEFAULT_OBJECT_HASH = new HashFunction<>() {
        @Override
        public int hash(Object key, int capacity) {
            return HashFunction.computePrimaryHash(key, capacity);
        }

        @Override
        public int secondaryHash(Object key, int capacity) {
            return HashFunction.computeSecondaryHash(key, capacity);
        }
    };
}
